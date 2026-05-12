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

    @PostMapping("/{userId}")
    public Result<Void> follow(@RequestAttribute Long myId, @PathVariable Long userId) {
        followService.follow(myId, userId);
        return Result.ok();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(@RequestAttribute Long myId, @PathVariable Long userId) {
        followService.unfollow(myId, userId);
        return Result.ok();
    }

    @GetMapping("/following/{userId}")
    public Result<Page<UserProfileResponse>> following(@PathVariable Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(followService.getFollowing(userId, page, pageSize));
    }

    @GetMapping("/followers/{userId}")
    public Result<Page<UserProfileResponse>> followers(@PathVariable Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(followService.getFollowers(userId, page, pageSize));
    }

    @GetMapping("/status/{userId}")
    public Result<FollowStatusResponse> status(@RequestAttribute Long myId, @PathVariable Long userId) {
        return Result.ok(followService.getFollowStatus(myId, userId));
    }
}
