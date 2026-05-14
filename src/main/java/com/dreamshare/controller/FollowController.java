package com.dreamshare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.FollowStatusResponse;
import com.dreamshare.dto.UserProfileResponse;
import com.dreamshare.service.FollowService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
@CrossOrigin
public class FollowController {

    @Autowired private FollowService followService;

    @PostMapping("/{targetId}")
    public Result<Void> follow(@RequestAttribute Long userId, @PathVariable Long targetId) {
        followService.follow(userId, targetId);
        return Result.ok();
    }

    @DeleteMapping("/{targetId}")
    public Result<Void> unfollow(@RequestAttribute Long userId, @PathVariable Long targetId) {
        followService.unfollow(userId, targetId);
        return Result.ok();
    }

    @GetMapping("/following/{targetId}")
    public Result<Page<UserProfileResponse>> following(@PathVariable Long targetId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(followService.getFollowing(targetId, page, pageSize));
    }

    @GetMapping("/followers/{targetId}")
    public Result<Page<UserProfileResponse>> followers(@PathVariable Long targetId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(followService.getFollowers(targetId, page, pageSize));
    }

    @GetMapping("/status/{targetId}")
    public Result<FollowStatusResponse> status(@RequestAttribute Long userId, @PathVariable Long targetId) {
        return Result.ok(followService.getFollowStatus(userId, targetId));
    }
}
