package com.dreamshare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.*;
import com.dreamshare.service.DiscussionService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/discussions")
@CrossOrigin
public class DiscussionController {

    @Autowired private DiscussionService discussionService;

    @PostMapping
    public Result<DiscussionDetailResponse> create(@RequestAttribute Long userId,
                                                    @Valid @RequestBody DiscussionCreateRequest req) {
        return Result.ok(discussionService.createDiscussion(userId, req));
    }

    @GetMapping
    public Result<Page<DiscussionListResponse>> list(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize,
                                                      @RequestParam(required = false) String type,
                                                      @RequestParam(required = false) String keyword) {
        return Result.ok(discussionService.getDiscussions(page, pageSize, type, keyword));
    }

    @GetMapping("/{discussionId}")
    public Result<DiscussionDetailResponse> detail(@PathVariable Long discussionId) {
        return Result.ok(discussionService.getDiscussionDetail(discussionId));
    }

    @PutMapping("/{discussionId}")
    public Result<DiscussionDetailResponse> update(@RequestAttribute Long userId,
                                                    @PathVariable Long discussionId,
                                                    @RequestBody DiscussionCreateRequest req) {
        return Result.ok(discussionService.updateDiscussion(userId, discussionId, req));
    }

    @DeleteMapping("/{discussionId}")
    public Result<Void> delete(@RequestAttribute Long userId, @PathVariable Long discussionId) {
        discussionService.deleteDiscussion(userId, discussionId);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<Page<DiscussionListResponse>> myList(@RequestAttribute Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(discussionService.getMyDiscussions(userId, page, pageSize));
    }

    @GetMapping("/recommended")
    public Result<Page<DiscussionListResponse>> recommended(@RequestAttribute Long userId,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(discussionService.getRecommendedDiscussions(userId, page, pageSize));
    }

    @GetMapping("/{discussionId}/members")
    public Result<Page<UserProfileResponse>> members(@PathVariable Long discussionId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(discussionService.getMembers(discussionId, page, pageSize));
    }

    @PostMapping("/{discussionId}/join")
    public Result<Void> join(@RequestAttribute Long userId, @PathVariable Long discussionId) {
        discussionService.joinDiscussion(userId, discussionId);
        return Result.ok();
    }

    @PostMapping("/{discussionId}/leave")
    public Result<Void> leave(@RequestAttribute Long userId, @PathVariable Long discussionId) {
        discussionService.leaveDiscussion(userId, discussionId);
        return Result.ok();
    }

    @PostMapping("/{discussionId}/invite")
    public Result<Void> invite(@RequestAttribute Long userId, @PathVariable Long discussionId,
                                @RequestBody DiscussionJoinRequest req) {
        discussionService.inviteUser(userId, discussionId, req.getUserId());
        return Result.ok();
    }
}
