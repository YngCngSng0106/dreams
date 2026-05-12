package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreamshare.dto.UserProfileResponse;
import com.dreamshare.dto.UserUpdateRequest;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired private UserMapper userMapper;
    @Autowired private DreamMapper dreamMapper;
    @Autowired private DiscussionMapper discussionMapper;
    @Autowired private DiscussionMemberMapper discussionMemberMapper;
    @Autowired private FollowMapper followMapper;

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        return toResponse(user);
    }

    public UserProfileResponse updateUser(Long userId, UserUpdateRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar());
        if (req.getGender() != null) user.setGender(req.getGender());
        if (req.getBio() != null) user.setBio(req.getBio());
        userMapper.updateById(user);
        return toResponse(user);
    }

    public UserProfileResponse getMyProfile(Long userId) {
        return toResponse(userMapper.selectById(userId));
    }

    private UserProfileResponse toResponse(User user) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setGender(user.getGender());
        resp.setBio(user.getBio());

        // 梦境数
        LambdaQueryWrapper<Dream> dreamWrapper = new LambdaQueryWrapper<>();
        dreamWrapper.eq(Dream::getUserId, user.getId());
        resp.setDreamCount(dreamMapper.selectCount(dreamWrapper));

        // 讨论组数（作为创建者）
        LambdaQueryWrapper<Discussion> discWrapper = new LambdaQueryWrapper<>();
        discWrapper.eq(Discussion::getCreatorId, user.getId());
        resp.setDiscussionCount(discussionMapper.selectCount(discWrapper));

        // 粉丝数
        LambdaQueryWrapper<Follow> followerWrapper = new LambdaQueryWrapper<>();
        followerWrapper.eq(Follow::getFolloweeId, user.getId());
        resp.setFollowersCount(followMapper.selectCount(followerWrapper));

        // 关注数
        LambdaQueryWrapper<Follow> followingWrapper = new LambdaQueryWrapper<>();
        followingWrapper.eq(Follow::getFollowerId, user.getId());
        resp.setFollowingCount(followMapper.selectCount(followingWrapper));

        return resp;
    }
}
