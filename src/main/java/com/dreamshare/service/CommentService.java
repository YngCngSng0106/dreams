package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.CommentListResponse;
import com.dreamshare.dto.CommentCreateRequest;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired private CommentMapper commentMapper;
    @Autowired private CommentLikeMapper commentLikeMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private DiscussionMemberMapper discussionMemberMapper;
    @Autowired private NotificationMapper notificationMapper;
    @Autowired private ContentAuditMapper contentAuditMapper;

    public CommentListResponse createComment(Long userId, CommentCreateRequest req) {
        // 检查是否为讨论组成员
        LambdaQueryWrapper<DiscussionMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(DiscussionMember::getDiscussionId, req.getDiscussionId())
                     .eq(DiscussionMember::getUserId, userId);
        if (discussionMemberMapper.selectCount(memberWrapper) == 0) {
            throw new RuntimeException("请先加入讨论组");
        }

        Comment comment = new Comment();
        comment.setDiscussionId(req.getDiscussionId());
        comment.setUserId(userId);
        comment.setParentId(req.getParentId());
        comment.setContent(req.getContent());
        comment.setLikeCount(0);
        comment.setIsDeleted(0);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        // 创建审核记录
        ContentAudit audit = new ContentAudit();
        audit.setTargetType("COMMENT");
        audit.setTargetId(comment.getId());
        audit.setContentSnapshot(req.getContent());
        audit.setAuditStatus("PENDING");
        audit.setCreateTime(LocalDateTime.now());
        contentAuditMapper.insert(audit);

        // 通知（如果是回复）
        if (req.getParentId() != null) {
            Comment parent = commentMapper.selectById(req.getParentId());
            if (parent != null && !parent.getUserId().equals(userId)) {
                sendNotification(parent.getUserId(), "REPLY", userId, comment.getId(), "回复了你的评论");
            }
        }
        // 通知讨论组其他成员
        sendNotificationToMembers(req.getDiscussionId(), "COMMENT", userId, comment.getId(), "在讨论组发布了评论");

        return toResponse(comment);
    }

    public Page<CommentListResponse> getComments(Long discussionId, int page, int pageSize, String sortBy) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getDiscussionId, discussionId);
        wrapper.eq(Comment::getParentId, 0L);  // 只查顶级评论
        wrapper.eq(Comment::getIsDeleted, 0);
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Comment::getLikeCount);
        } else {
            wrapper.orderByDesc(Comment::getCreateTime);
        }
        Page<Comment> cPage = commentMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Page<CommentListResponse> result = new Page<>();
        result.setCurrent(cPage.getCurrent());
        result.setSize(cPage.getSize());
        result.setTotal(cPage.getTotal());
        result.setRecords(cPage.getRecords().stream().map(c -> toResponseWithReplies(c, discussionId)).collect(Collectors.toList()));
        return result;
    }

    public void updateComment(Long userId, Long commentId, String content) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        if (!c.getUserId().equals(userId)) throw new RuntimeException("无权修改");
        c.setContent(content);
        commentMapper.updateById(c);
    }

    public void deleteComment(Long userId, Long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        if (!c.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        c.setIsDeleted(1);
        commentMapper.updateById(c);
    }

    public void likeComment(Long userId, Long commentId) {
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId).eq(CommentLike::getUserId, userId);
        if (commentLikeMapper.selectCount(wrapper) > 0) throw new RuntimeException("已点赞");
        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        commentLikeMapper.insert(like);
        Comment c = commentMapper.selectById(commentId);
        if (c != null) {
            c.setLikeCount(c.getLikeCount() + 1);
            commentMapper.updateById(c);
        }
    }

    public void unlikeComment(Long userId, Long commentId) {
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId).eq(CommentLike::getUserId, userId);
        commentLikeMapper.delete(wrapper);
        Comment c = commentMapper.selectById(commentId);
        if (c != null && c.getLikeCount() > 0) {
            c.setLikeCount(c.getLikeCount() - 1);
            commentMapper.updateById(c);
        }
    }

    private CommentListResponse toResponse(Comment c) {
        CommentListResponse resp = new CommentListResponse();
        resp.setCommentId(c.getId());
        resp.setUserId(c.getUserId());
        User user = userMapper.selectById(c.getUserId());
        resp.setNickname(user != null ? user.getNickname() : "未知");
        resp.setContent(c.getContent());
        resp.setLikeCount(c.getLikeCount());
        resp.setCreateTime(c.getCreateTime());
        resp.setReplyCount(getReplyCount(c.getId()));
        return resp;
    }

    private CommentListResponse toResponseWithReplies(Comment c, Long discussionId) {
        CommentListResponse resp = toResponse(c);
        // 获取回复
        LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(Comment::getParentId, c.getId());
        replyWrapper.eq(Comment::getIsDeleted, 0);
        replyWrapper.orderByAsc(Comment::getCreateTime);
        List<Comment> replies = commentMapper.selectList(replyWrapper);
        resp.setReplies(replies.stream().map(this::toResponse).collect(Collectors.toList()));
        return resp;
    }

    private long getReplyCount(Long parentId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, parentId).eq(Comment::getIsDeleted, 0);
        return commentMapper.selectCount(wrapper);
    }

    private void sendNotification(Long toUserId, String type, Long sourceUserId, Long relatedId, String content) {
        Notification n = new Notification();
        n.setUserId(toUserId);
        n.setType(type);
        n.setSourceUserId(sourceUserId);
        n.setRelatedId(relatedId);
        n.setContent(content);
        n.setIsRead(false);
        n.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    private void sendNotificationToMembers(Long discussionId, String type, Long sourceUserId, Long relatedId, String content) {
        LambdaQueryWrapper<DiscussionMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscussionMember::getDiscussionId, discussionId);
        List<DiscussionMember> members = discussionMemberMapper.selectList(wrapper);
        for (DiscussionMember m : members) {
            if (!m.getUserId().equals(sourceUserId)) {
                sendNotification(m.getUserId(), type, sourceUserId, relatedId, content);
            }
        }
    }
}
