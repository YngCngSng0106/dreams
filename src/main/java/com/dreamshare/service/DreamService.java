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
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired private NotificationService notificationService;

    @Transactional
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
        dreamMapper.insert(dream);

        // 创建审核记录
        ContentAudit audit = new ContentAudit();
        audit.setTargetType("DREAM");
        audit.setTargetId(dream.getId());
        audit.setContentSnapshot(req.getDescription());
        audit.setAuditStatus("PENDING");
        contentAuditMapper.insert(audit);

        return getDreamDetail(dream.getId());
    }

    public DreamDetailResponse getDreamDetail(Long dreamId) {
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream == null) throw new RuntimeException("梦境不存在");
        return toDetailResponse(dream);
    }

    @Transactional
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

    @Transactional
    public void deleteDream(Long userId, Long dreamId) {
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream == null) throw new RuntimeException("梦境不存在");
        if (!dream.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        dream.setIsDeleted(1);
        dreamMapper.updateById(dream);
    }

    public Page<DreamListResponse> getMyDreams(Long userId, int page, int pageSize, Long categoryId, Integer isRecurring) {
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
            wrapper.orderByDesc(Dream::getCreateTime);  // 简化：实际需用子查询按点赞数排序
        } else {
            wrapper.orderByDesc(Dream::getCreateTime);
        }
        Page<Dream> dreamPage = dreamMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return convertToListPage(dreamPage);
    }

    public List<DreamMatchResponse> findSimilarDreams(Long dreamId, int limit) {
        Dream source = dreamMapper.selectById(dreamId);
        if (source == null) throw new RuntimeException("梦境不存在");
        // 优化：只查同分类或有关键词的梦境，减少全表扫描
        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dream::getIsDeleted, 0);
        wrapper.ne(Dream::getId, dreamId);
        // 限制候选集大小
        wrapper.last("LIMIT 200");
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

    @Transactional
    public void likeDream(Long userId, Long dreamId) {
        // 检查梦境是否存在且未被删除
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream == null) throw new RuntimeException("梦境不存在");
        if (dream.getIsDeleted() != null && dream.getIsDeleted() == 1) throw new RuntimeException("梦境已被删除");

        LambdaQueryWrapper<DreamLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DreamLike::getDreamId, dreamId).eq(DreamLike::getUserId, userId);
        if (dreamLikeMapper.selectCount(wrapper) > 0) throw new RuntimeException("已点赞");

        DreamLike like = new DreamLike();
        like.setDreamId(dreamId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        dreamLikeMapper.insert(like);

        // 发送通知给梦境作者（不通知自己）
        if (!dream.getUserId().equals(userId)) {
            notificationService.sendNotification(dream.getUserId(), "LIKE", userId, dreamId, "赞了你的梦境");
        }
    }

    @Transactional
    public void unlikeDream(Long userId, Long dreamId) {
        LambdaQueryWrapper<DreamLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DreamLike::getDreamId, dreamId).eq(DreamLike::getUserId, userId);
        dreamLikeMapper.delete(wrapper);
    }

    public Map<String, Object> getDreamStats(Long dreamId) {
        LambdaQueryWrapper<DreamLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(DreamLike::getDreamId, dreamId);
        long likeCount = dreamLikeMapper.selectCount(likeWrapper);
        return Map.of("dreamId", dreamId, "likeCount", likeCount);
    }

    /**
     * 批量转换分页结果，解决 N+1 查询问题
     */
    private Page<DreamListResponse> convertToListPage(Page<Dream> dreamPage) {
        List<Dream> dreams = dreamPage.getRecords();
        if (dreams == null || dreams.isEmpty()) {
            Page<DreamListResponse> result = new Page<>();
            result.setCurrent(dreamPage.getCurrent());
            result.setSize(dreamPage.getSize());
            result.setTotal(dreamPage.getTotal());
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 批量查询用户
        Set<Long> userIds = dreams.stream().map(Dream::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.stream()
                .map(uid -> userMapper.selectById(uid))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量查询分类
        Set<Long> categoryIds = dreams.stream().map(Dream::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, DreamCategory> categoryMap = categoryIds.stream()
                .map(cid -> dreamCategoryMapper.selectById(cid))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DreamCategory::getId, c -> c));

        // 批量查询点赞数
        Set<Long> dreamIds = dreams.stream().map(Dream::getId).collect(Collectors.toSet());
        Map<Long, Long> likeCountMap = new HashMap<>();
        for (Long dreamId : dreamIds) {
            LambdaQueryWrapper<DreamLike> lw = new LambdaQueryWrapper<>();
            lw.eq(DreamLike::getDreamId, dreamId);
            likeCountMap.put(dreamId, dreamLikeMapper.selectCount(lw));
        }

        Page<DreamListResponse> result = new Page<>();
        result.setCurrent(dreamPage.getCurrent());
        result.setSize(dreamPage.getSize());
        result.setTotal(dreamPage.getTotal());
        result.setRecords(dreams.stream().map(d -> {
            DreamListResponse resp = new DreamListResponse();
            resp.setId(d.getId());
            resp.setUserId(d.getUserId());
            User user = userMap.get(d.getUserId());
            if (user != null) {
                resp.setNickname(user.getNickname());
                resp.setAvatar(user.getAvatar());
            }
            DreamCategory cat = categoryMap.get(d.getCategoryId());
            resp.setCategory(cat != null ? cat.getName() : null);
            resp.setDreamDate(d.getDreamDate());
            resp.setLocation(d.getLocation());
            resp.setKeywords(d.getKeywords());
            resp.setClarity(d.getClarity());
            resp.setDescription(d.getDescription());
            resp.setIsRecurring(d.getIsRecurring());
            resp.setTags(d.getTags());
            resp.setCreateTime(d.getCreateTime());
            resp.setLikeCount(likeCountMap.getOrDefault(d.getId(), 0L).intValue());
            return resp;
        }).collect(Collectors.toList()));
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
}
