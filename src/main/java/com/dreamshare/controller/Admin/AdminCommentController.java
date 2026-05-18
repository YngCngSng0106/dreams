package com.dreamshare.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.config.AdminAuth;
import com.dreamshare.entity.Comment;
import com.dreamshare.entity.User;
import com.dreamshare.mapper.CommentMapper;
import com.dreamshare.service.OperationLogService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@CrossOrigin
@AdminAuth
public class AdminCommentController {

    @Autowired private CommentMapper commentMapper;
    @Autowired private OperationLogService operationLogService;

    @GetMapping
    public Result<IPage<Comment>> listComments(
            @ModelAttribute("adminUser") User admin,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long discussionId,
            @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<Comment> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) qw.like(Comment::getContent, keyword);
        if (discussionId != null) qw.eq(Comment::getDiscussionId, discussionId);
        if (userId != null) qw.eq(Comment::getUserId, userId);
        qw.orderByDesc(Comment::getCreateTime);
        return Result.ok(commentMapper.selectPage(new Page<>(page, size), qw));
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public Result<Void> deleteComment(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        Comment c = commentMapper.selectById(id);
        if (c == null) return Result.error("评论不存在");
        c.setIsDeleted(1);
        commentMapper.updateById(c);
        operationLogService.log(admin.getId(), admin.getUsername(), "comment", "delete", id, "comment_" + id, "逻辑删除评论");
        return Result.ok();
    }

    @PostMapping("/{id}/hide")
    @Transactional
    public Result<Void> hideComment(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        Comment c = commentMapper.selectById(id);
        if (c == null) return Result.error("评论不存在");
        c.setIsHidden(1);
        commentMapper.updateById(c);
        operationLogService.log(admin.getId(), admin.getUsername(), "comment", "hide", id, "comment_" + id, "隐藏评论");
        return Result.ok();
    }

    @PostMapping("/{id}/unhide")
    @Transactional
    public Result<Void> unhideComment(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        Comment c = commentMapper.selectById(id);
        if (c == null) return Result.error("评论不存在");
        c.setIsHidden(0);
        commentMapper.updateById(c);
        operationLogService.log(admin.getId(), admin.getUsername(), "comment", "unhide", id, "comment_" + id, "取消隐藏");
        return Result.ok();
    }
}
