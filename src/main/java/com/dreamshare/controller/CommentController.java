package com.dreamshare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.CommentCreateRequest;
import com.dreamshare.dto.CommentListResponse;
import com.dreamshare.service.CommentService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin
public class CommentController {

    @Autowired private CommentService commentService;

    @PostMapping
    public Result<CommentListResponse> create(@RequestAttribute Long userId,
                                               @Valid @RequestBody CommentCreateRequest req) {
        return Result.ok(commentService.createComment(userId, req));
    }

    @GetMapping("/{discussionId}")
    public Result<Page<CommentListResponse>> list(@PathVariable Long discussionId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize,
                                                   @RequestParam(defaultValue = "newest") String sortBy) {
        return Result.ok(commentService.getComments(discussionId, page, pageSize, sortBy));
    }

    @PutMapping("/{commentId}")
    public Result<Void> update(@RequestAttribute Long userId, @PathVariable Long commentId,
                                @RequestBody java.util.Map<String, String> body) {
        commentService.updateComment(userId, commentId, body.get("content"));
        return Result.ok();
    }

    @DeleteMapping("/{commentId}")
    public Result<Void> delete(@RequestAttribute Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
        return Result.ok();
    }

    @PostMapping("/{commentId}/like")
    public Result<Void> like(@RequestAttribute Long userId, @PathVariable Long commentId) {
        commentService.likeComment(userId, commentId);
        return Result.ok();
    }

    @PostMapping("/{commentId}/unlike")
    public Result<Void> unlike(@RequestAttribute Long userId, @PathVariable Long commentId) {
        commentService.unlikeComment(userId, commentId);
        return Result.ok();
    }
}
