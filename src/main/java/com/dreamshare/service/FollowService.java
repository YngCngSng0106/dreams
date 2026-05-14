package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.FollowStatusResponse;
import com.dreamshare.dto.UserProfileResponse;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class FollowService {

    @Autowired private FollowMapper followMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private NotificationMapper notificationMapper;

    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) throw new RuntimeException("不能关注自己");
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, followerId).eq(Follow::getFolloweeId, followeeId);
        if (followMapper.selectCount(wrapper) > 0) throw new RuntimeException("已关注");
        Follow f = new Follow();
        f.setFollowerId(followerId);
        f.setFolloweeId(followeeId);
        f.setCreateTime(LocalDateTime.now());
        followMapper.insert(f);
        sendNotification(followeeId, "FOLLOW", followerId, null, "关注了你");
    }

    public void unfollow(Long followerId, Long followeeId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, followerId).eq(Follow::getFolloweeId, followeeId);
        followMapper.delete(wrapper);
    }

    public Page<UserProfileResponse> getFollowing(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, userId);
        Page<Follow> fPage = followMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return convertToUserPage(fPage, Follow::getFolloweeId);
    }

    public Page<UserProfileResponse> getFollowers(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFolloweeId, userId);
        Page<Follow> fPage = followMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return convertToUserPage(fPage, Follow::getFollowerId);
    }

    public FollowStatusResponse getFollowStatus(Long myId, Long otherId) {
        FollowStatusResponse resp = new FollowStatusResponse();
        LambdaQueryWrapper<Follow> w1 = new LambdaQueryWrapper<>();
        w1.eq(Follow::getFollowerId, myId).eq(Follow::getFolloweeId, otherId);
        resp.setIsFollowing(followMapper.selectCount(w1) > 0);
        LambdaQueryWrapper<Follow> w2 = new LambdaQueryWrapper<>();
        w2.eq(Follow::getFollowerId, otherId).eq(Follow::getFolloweeId, myId);
        resp.setIsFollower(followMapper.selectCount(w2) > 0);
        return resp;
    }

    private Page<UserProfileResponse> convertToUserPage(Page<Follow> fPage, java.util.function.Function<Follow, Long> userIdExtractor) {
        Page<UserProfileResponse> result = new Page<>();
        result.setCurrent(fPage.getCurrent());
        result.setSize(fPage.getSize());
        result.setTotal(fPage.getTotal());
        result.setRecords(fPage.getRecords().stream().map(f -> {
            Long uid = userIdExtractor.apply(f);
            User u = userMapper.selectById(uid);
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

    private void sendNotification(Long toUserId, String type, Long sourceUserId, Long relatedId, String content) {
        Notification n = new Notification();
        n.setUserId(toUserId);
        n.setType(type);
        n.setSourceUserId(sourceUserId);
        n.setRelatedId(relatedId);
        n.setContent(content);
        n.setIsRead(0);
        n.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(n);
    }
}
