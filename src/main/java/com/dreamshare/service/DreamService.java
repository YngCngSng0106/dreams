package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.*;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import com.dreamshare.utils.DreamSimilarityCalculator;
import com.dreamshare.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DreamService {

    @Autowired private DreamMapper dreamMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private DreamCategoryMapper dreamCategoryMapper;
    @Autowired private DreamLikeMapper dreamLikeMapper;
    @Autowired private ContentAuditMapper contentAuditMapper;
    @Autowired private NotificationMapper notificationMapper;

    public DreamDetailResponse createDream(Long userId, DreamCreateRequest req) {
        Dream dream = new Dream();
        dream.setUserId(userId);
        dream.setCategoryId(req.getCategoryId());
        dream.setDreamDate(req.getDreamDate());
        dream.setLocation(req.getLocation());
        dream.setKeywords(req.getKeywords());
        dream.setClarity(req.getClarity());
        dream.setDescription(req.getDescription());
        dream.setIsRecurring(req.getIsRecurring());
        dream.setTags(req.getTags());
        dream.setImages(req.getImages());
        dream.setIsDeleted(0);
        dream.setCreateTime(LocalDateTime.now());
        dream.setUpdateTime(LocalDateTime.now());
        dreamMapper.insert(dream);

        // 创建审核记录
        ContentAudit audit = new ContentAudit();
        audit.setTargetType("DREAM");
        audit.setTargetId(dream.getId());
        audit.setContentSnapshot(req.getDescription());
        audit.setAuditStatus("PENDING");
        audit.setCreateTime(LocalDateTime.now());
        contentAuditMapper.insert(audit);

        return getDreamDetail(dream.getId());
    }

    public DreamDetailResponse getDreamDetail(Long dreamId) {
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream == null) throw new RuntimeException("梦境不存在");
        return toDetailResponse(dream);
    }

    public DreamDetailResponse updateDream(Long userId, Long dreamId, DreamUpdateRequest req) {
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream == null) throw new RuntimeException("梦境不存在");
        if (!dream.getUserId().equals(userId)) throw new RuntimeException("无权修改");
        if (req.getCategoryId() != null) dream.setCategoryId(req.getCategoryId());
        if (req.getDreamDate() != null) dream.setDreamDate(req.getDreamDate());
        if (req.getLocation() != null) dream.setLocation(req.getLocation());
        if (req.getKeywords() != null) dream.setKeywords(req.getKeywords());
        if (req.getClarity() != null) dream.setClarity(req.getClarity());
        if (req.getDescription() != null) dream.setDescription(req.getDescription());
        if (req.getIsRecurring() != null) dream.setIsRecurring(req.getIsRecurring());
        if (req.getTags() != null) dream.setTags(req.getTags());
        if (req.getImages() != null) dream.setImages(req.getImages());
        dream.setUpdateTime(LocalDateTime.now());
        dreamMapper.updateById(dream);
        return toDetailResponse(dream);
    }

    public void deleteDream(Long userId, Long dreamId) {
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream == null) throw new RuntimeException("梦境不存在");
        if (!dream.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        dream.setIsDeleted(1);
        dreamMapper.updateById(dream);
    }

    public Page<DreamListResponse> getMyDreams(Long userId, int page, int pageSize, Long categoryId, Boolean isRecurring) {
        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dream::getUserId, userId);
        wrapper.eq(Dream::getIsDeleted, 0);
        if (categoryId != null) wrapper.eq(Dream::getCategoryId, categoryId);
        if (isRecurring != null) wrapper.eq(Dream::getIsRecurring, isRecurring);
        wrapper.orderByDesc(Dream::getCreateTime);
        Page<Dream> dreamPage = dreamMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return convertToListPage(dreamPage);
    }

    public Page<DreamListResponse> getFeed(int page, int pageSize, String sortBy) {
        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dream::getIsDeleted, 0);
        if ("popular".equals(sortBy)) {
            wrapper.orderByDesc(Dream::getCreateTime);  // 简化：实际需要用子查询
        } else {
            wrapper.orderByDesc(Dream::getCreateTime);
        }
        Page<Dream> dreamPage = dreamMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return convertToListPage(dreamPage);
    }

    public List<DreamMatchResponse> findSimilarDreams(Long dreamId, int limit) {
        Dream source = dreamMapper.selectById(dreamId);
        if (source == null) throw new RuntimeException("梦境不存在");
        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dream::getIsDeleted, 0);
        List<Dream> candidates = dreamMapper.selectList(wrapper);
        List<Map<String, Object>> similar = DreamSimilarityCalculator.findSimilar(source, candidates);
        if (limit > 0 && similar.size() > limit) similar = similar.subList(0, limit);

        return similar.stream().map(m -> {
            DreamMatchResponse resp = new DreamMatchResponse();
            resp.setDreamId((Long) m.get("dreamId"));
            resp.setSimilarityScore((Double) m.get("similarityScore"));
            resp.setMatchedFields((String) m.get("matchedFields"));
            return resp;
        }).collect(Collectors.toList());
    }

    public void likeDream(Long userId, Long dreamId) {
        LambdaQueryWrapper<DreamLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DreamLike::getDreamId, dreamId).eq(DreamLike::getUserId, userId);
        if (dreamLikeMapper.selectCount(wrapper) > 0) throw new RuntimeException("已点赞");
        DreamLike like = new DreamLike();
        like.setDreamId(dreamId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        dreamLikeMapper.insert(like);

        // 发送通知给梦境作者
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream != null && !dream.getUserId().equals(userId)) {
            sendNotification(dream.getUserId(), "LIKE", userId, dreamId, "赞了你的梦境");
        }
    }

    public void unlikeDream(Long userId, Long dreamId) {
        LambdaQueryWrapper<DreamLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DreamLike::getDreamId, dreamId).eq(DreamLike::getUserId, userId);
        dreamLikeMapper.delete(wrapper);
    }

    public Map<String, Object> getDreamStats(Long dreamId) {
        LambdaQueryWrapper<DreamLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(DreamLike::getDreamId, dreamId);
        long likeCount = dreamLikeMapper.selectCount(likeWrapper);
        // 评论数从讨论组获取，简化处理
        return Map.of("dreamId", dreamId, "likeCount", likeCount);
    }

    private void sendNotification(Long toUserId, String type, Long sourceUserId, Long relatedId, String content) {
        Notification n = new Notification();
        n.setUserId(toUserId);
        n.setType(type);
        n.setSourceUserId(sourceUserId);
        n.setRelatedId(relatedId);
        n.setContent(content);
        n.setIsRead(false);
        n.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    private Page<DreamListResponse> convertToListPage(Page<Dream> dreamPage) {
        Page<DreamListResponse> result = new Page<>();
        result.setCurrent(dreamPage.getCurrent());
        result.setSize(dreamPage.getSize());
        result.setTotal(dreamPage.getTotal());
        result.setRecords(dreamPage.getRecords().stream().map(this::toListResponse).collect(Collectors.toList()));
        return result;
    }

    private DreamDetailResponse toDetailResponse(Dream dream) {
        DreamDetailResponse resp = new DreamDetailResponse();
        resp.setId(dream.getId());
        resp.setUserId(dream.getUserId());
        User user = userMapper.selectById(dream.getUserId());
        if (user != null) {
            resp.setNickname(user.getNickname());
            resp.setAvatar(user.getAvatar());
        }
        DreamCategory cat = dreamCategoryMapper.selectById(dream.getCategoryId());
        resp.setCategory(cat != null ? cat.getName() : null);
        resp.setDreamDate(dream.getDreamDate());
        resp.setLocation(dream.getLocation());
        resp.setKeywords(dream.getKeywords());
        resp.setClarity(dream.getClarity());
        resp.setDescription(dream.getDescription());
        resp.setIsRecurring(dream.getIsRecurring());
        resp.setTags(dream.getTags());
        resp.setImages(dream.getImages());
        resp.setCreateTime(dream.getCreateTime());

        LambdaQueryWrapper<DreamLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(DreamLike::getDreamId, dream.getId());
        resp.setLikeCount(Math.toIntExact(dreamLikeMapper.selectCount(likeWrapper)));
        return resp;
    }

    private DreamListResponse toListResponse(Dream dream) {
        DreamListResponse resp = new DreamListResponse();
        resp.setId(dream.getId());
        resp.setUserId(dream.getUserId());
        User user = userMapper.selectById(dream.getUserId());
        if (user != null) {
            resp.setNickname(user.getNickname());
            resp.setAvatar(user.getAvatar());
        }
        DreamCategory cat = dreamCategoryMapper.selectById(dream.getCategoryId());
        resp.setCategory(cat != null ? cat.getName() : null);
        resp.setDreamDate(dream.getDreamDate());
        resp.setLocation(dream.getLocation());
        resp.setKeywords(dream.getKeywords());
        resp.setClarity(dream.getClarity());
        resp.setDescription(dream.getDescription());
        resp.setIsRecurring(dream.getIsRecurring());
        resp.setTags(dream.getTags());
        resp.setCreateTime(dream.getCreateTime());

        LambdaQueryWrapper<DreamLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(DreamLike::getDreamId, dream.getId());
        resp.setLikeCount(Math.toIntExact(dreamLikeMapper.selectCount(likeWrapper)));
        return resp;
    }
}
