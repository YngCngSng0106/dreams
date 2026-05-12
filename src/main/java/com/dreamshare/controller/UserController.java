package com.dreamshare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.*;
import com.dreamshare.service.UserService;
import com.dreamshare.service.DreamService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired private UserService userService;
    @Autowired private DreamService dreamService;

    @GetMapping("/me")
    public Result<UserProfileResponse> getMyProfile(@RequestAttribute Long userId) {
        return Result.ok(userService.getMyProfile(userId));
    }

    @PutMapping("/me")
    public Result<UserProfileResponse> updateMe(@RequestAttribute Long userId, @RequestBody UserUpdateRequest req) {
        return Result.ok(userService.updateUser(userId, req));
    }

    @GetMapping("/{userId}")
    public Result<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        return Result.ok(userService.getUserProfile(userId));
    }

    @GetMapping("/{userId}/dreams")
    public Result<Page<DreamListResponse>> getUserDreams(@PathVariable Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(dreamService.getMyDreams(userId, page, pageSize, null, null));
    }

    @GetMapping("/{userId}/discussions")
    public Result<Object> getUserDiscussions(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int pageSize) {
        // TODO: 后续实现
        return Result.ok();
    }
}
