package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.*;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscussionService {

    @Autowired private DiscussionMapper discussionMapper;
    @Autowired private DiscussionMemberMapper discussionMemberMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private DreamMapper dreamMapper;
    @Autowired private NotificationService notificationService;

    @Transactional
    public DiscussionDetailResponse createDiscussion(Long userId, DiscussionCreateRequest req) {
        Discussion discussion = new Discussion();
        discussion.setTitle(req.getTitle());
        discussion.setDescription(req.getDescription());
        discussion.setCoverImage(req.getCoverImage());
        discussion.setCreatorId(userId);
        discussion.setDreamId(req.getDreamId());
        discussion.setIsPreseted(0);
        discussion.setMemberCount(1);
        discussion.setIsDeleted(0);
        discussionMapper.insert(discussion);

        // 创建者为管理员
        DiscussionMember member = new DiscussionMember();
        member.setDiscussionId(discussion.getId());
        member.setUserId(userId);
        member.setRole("ADMIN");
        member.setJoinTime(LocalDateTime.now());
        discussionMemberMapper.insert(member);

        return getDiscussionDetail(discussion.getId());
    }

    public DiscussionDetailResponse getDiscussionDetail(Long discussionId) {
        Discussion d = discussionMapper.selectById(discussionId);
        if (d == null) throw new RuntimeException("讨论组不存在");
        DiscussionDetailResponse resp = new DiscussionDetailResponse();
        resp.setId(d.getId());
        resp.setTitle(d.getTitle());
        resp.setDescription(d.getDescription());
        resp.setCoverImage(d.getCoverImage());
        resp.setCreatorId(d.getCreatorId());
        User creator = userMapper.selectById(d.getCreatorId());
        resp.setCreatorNickname(creator != null ? creator.getNickname() : "未知");
        resp.setDreamId(d.getDreamId());
        resp.setIsPreseted(d.getIsPreseted());
        resp.setMemberCount(d.getMemberCount());
        resp.setCreateTime(d.getCreateTime());
        return resp;
    }

    @Transactional
    public DiscussionDetailResponse updateDiscussion(Long userId, Long discussionId, DiscussionCreateRequest req) {
        Discussion d = discussionMapper.selectById(discussionId);
        if (d == null) throw new RuntimeException("讨论组不存在");
        // 只有创建者或管理员可以修改
        LambdaQueryWrapper<DiscussionMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(DiscussionMember::getDiscussionId, discussionId)
                     .eq(DiscussionMember::getUserId, userId);
        if (discussionMemberMapper.selectCount(memberWrapper) == 0) {
            throw new RuntimeException("无权修改");
        }
        if (req.getTitle() != null) d.setTitle(req.getTitle());
        if (req.getDescription() != null) d.setDescription(req.getDescription());
        if (req.getCoverImage() != null) d.setCoverImage(req.getCoverImage());
        d.setUpdateTime(LocalDateTime.now());
        discussionMapper.updateById(d);
        return getDiscussionDetail(d.getId());
    }

    @Transactional
    public void deleteDiscussion(Long userId, Long discussionId) {
        Discussion d = discussionMapper.selectById(discussionId);
        if (d == null) throw new RuntimeException("讨论组不存在");
        if (!d.getCreatorId().equals(userId)) throw new RuntimeException("只有创建者可以删除");
        d.setIsDeleted(1);
        discussionMapper.updateById(d);
    }

    public Page<DiscussionListResponse> getDiscussions(int page, int pageSize, String type, String keyword) {
        LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Discussion::getIsDeleted, 0);
        if ("preset".equals(type)) wrapper.eq(Discussion::getIsPreseted, 1);
        else if ("user".equals(type)) wrapper.eq(Discussion::getIsPreseted, 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Discussion::getTitle, keyword).or().like(Discussion::getDescription, keyword));
        }
        wrapper.orderByDesc(Discussion::getCreateTime);
        Page<Discussion> dPage = discussionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return convertToListPage(dPage);
    }

    public Page<DiscussionListResponse> getMyDiscussions(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<DiscussionMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(DiscussionMember::getUserId, userId);
        Page<DiscussionMember> memberPage = discussionMemberMapper.selectPage(new Page<>(page, pageSize), memberWrapper);

        Page<DiscussionListResponse> result = new Page<>();
        result.setCurrent(memberPage.getCurrent());
        result.setSize(memberPage.getSize());
        result.setTotal(memberPage.getTotal());
        result.setRecords(memberPage.getRecords().stream().map(m -> {
            Discussion d = discussionMapper.selectById(m.getDiscussionId());
            return d != null ? toListResponse(d) : null;
        }).collect(Collectors.toList()));
        return result;
    }

    public Page<UserProfileResponse> getMembers(Long discussionId, int page, int pageSize) {
        LambdaQueryWrapper<DiscussionMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussionMember::getDiscussionId, discussionId);
        Page<DiscussionMember> memberPage = discussionMemberMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Page<UserProfileResponse> result = new Page<>();
        result.setCurrent(memberPage.getCurrent());
        result.setSize(memberPage.getSize());
        result.setTotal(memberPage.getTotal());
        result.setRecords(memberPage.getRecords().stream().map(m -> {
            User u = userMapper.selectById(m.getUserId());
            if (u == null) return null;
            UserProfileResponse resp = new UserProfileResponse();
            resp.setId(u.getId());
            resp.setNickname(u.getNickname());
            resp.setAvatar(u.getAvatar());
            resp.setGender(u.getGender());
            resp.setBio(u.getBio());
            return resp;
        }).collect(Collectors.toList()));
        return result;
    }

    @Transactional
    public void joinDiscussion(Long userId, Long discussionId) {
        Discussion d = discussionMapper.selectById(discussionId);
        if (d == null) throw new RuntimeException("讨论组不存在");
        LambdaQueryWrapper<DiscussionMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussionMember::getDiscussionId, discussionId).eq(DiscussionMember::getUserId, userId);
        if (discussionMemberMapper.selectCount(wrapper) > 0) throw new RuntimeException("已在讨论组中");

        DiscussionMember member = new DiscussionMember();
        member.setDiscussionId(discussionId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinTime(LocalDateTime.now());
        discussionMemberMapper.insert(member);

        d.setMemberCount(d.getMemberCount() + 1);
        discussionMapper.updateById(d);

        // 通知创建者
        notificationService.sendNotification(d.getCreatorId(), "JOIN", userId, discussionId, "加入了你的讨论组");
    }

    @Transactional
    public void leaveDiscussion(Long userId, Long discussionId) {
        LambdaQueryWrapper<DiscussionMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussionMember::getDiscussionId, discussionId).eq(DiscussionMember::getUserId, userId);
        DiscussionMember member = discussionMemberMapper.selectOne(wrapper);
        if (member == null) throw new RuntimeException("不在讨论组中");

        discussionMemberMapper.delete(wrapper);
        Discussion d = discussionMapper.selectById(discussionId);
        if (d != null) {
            d.setMemberCount(Math.max(0, d.getMemberCount() - 1));
            discussionMapper.updateById(d);
        }
    }

    public void inviteUser(Long userId, Long discussionId, Long inviteUserId) {
        Discussion d = discussionMapper.selectById(discussionId);
        if (d == null) throw new RuntimeException("讨论组不存在");
        // 检查邀请者是否为管理员
        LambdaQueryWrapper<DiscussionMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussionMember::getDiscussionId, discussionId).eq(DiscussionMember::getUserId, userId);
        DiscussionMember member = discussionMemberMapper.selectOne(wrapper);
        if (member == null || !"ADMIN".equals(member.getRole())) throw new RuntimeException("只有管理员可以邀请");

        // 发送通知
        notificationService.sendNotification(inviteUserId, "INVITE", userId, discussionId, "邀请你加入讨论组");
    }

    public Page<DiscussionListResponse> getRecommendedDiscussions(Long userId, int page, int pageSize) {
        // 简化：推荐用户未加入的热门讨论组
        LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Discussion::getIsDeleted, 0);
        wrapper.orderByDesc(Discussion::getMemberCount);
        wrapper.last("LIMIT " + pageSize);
        List<Discussion> discussions = discussionMapper.selectList(wrapper);
        Page<DiscussionListResponse> result = new Page<>();
        result.setCurrent(page);
        result.setSize(pageSize);
        result.setTotal((long) discussions.size());
        result.setRecords(discussions.stream().map(this::toListResponse).collect(Collectors.toList()));
        return result;
    }

    private Page<DiscussionListResponse> convertToListPage(Page<Discussion> dPage) {
        Page<DiscussionListResponse> result = new Page<>();
        result.setCurrent(dPage.getCurrent());
        result.setSize(dPage.getSize());
        result.setTotal(dPage.getTotal());
        result.setRecords(dPage.getRecords().stream().map(this::toListResponse).collect(Collectors.toList()));
        return result;
    }

    private DiscussionListResponse toListResponse(Discussion d) {
        DiscussionListResponse resp = new DiscussionListResponse();
        resp.setId(d.getId());
        resp.setTitle(d.getTitle());
        resp.setDescription(d.getDescription());
        resp.setCoverImage(d.getCoverImage());
        resp.setCreatorId(d.getCreatorId());
        User creator = userMapper.selectById(d.getCreatorId());
        resp.setCreatorNickname(creator != null ? creator.getNickname() : "未知");
        resp.setMemberCount(d.getMemberCount());
        resp.setCreateTime(d.getCreateTime());
        return resp;
    }
}
