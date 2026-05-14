package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.CommentListResponse;
import com.dreamshare.dto.CommentCreateRequest;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired private CommentMapper commentMapper;
    @Autowired private CommentLikeMapper commentLikeMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private DiscussionMemberMapper discussionMemberMapper;
    @Autowired private NotificationService notificationService;
    @Autowired private ContentAuditMapper contentAuditMapper;

    @Transactional
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
        comment.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        comment.setContent(req.getContent());
        comment.setLikeCount(0);
        comment.setIsDeleted(0);
        commentMapper.insert(comment);

        // 创建审核记录
        ContentAudit audit = new ContentAudit();
        audit.setTargetType("COMMENT");
        audit.setTargetId(comment.getId());
        audit.setContentSnapshot(req.getContent());
        audit.setAuditStatus("PENDING");
        contentAuditMapper.insert(audit);

        // 通知（如果是回复）
        if (req.getParentId() != null && req.getParentId() > 0) {
            Comment parent = commentMapper.selectById(req.getParentId());
            if (parent != null && !parent.getUserId().equals(userId)) {
                notificationService.sendNotification(parent.getUserId(), "REPLY", userId, comment.getId(), "回复了你的评论");
            }
        }

        return toResponse(comment);
    }

    public Page<CommentListResponse> getComments(Long discussionId, int page, int pageSize, String sortBy) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getDiscussionId, discussionId);
        wrapper.eq(Comment::getParentId, 0L);  // 只查顶级评论
        wrapper.eq(Comment::getIsDeleted, 0);
        // 排除被隐藏的评论
        wrapper.eq(Comment::getIsHidden, 0);
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Comment::getLikeCount);
        } else {
            wrapper.orderByDesc(Comment::getCreateTime);
        }
        Page<Comment> cPage = commentMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return convertToResponsePageWithReplies(cPage);
    }

    @Transactional
    public void updateComment(Long userId, Long commentId, String content) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        if (!c.getUserId().equals(userId)) throw new RuntimeException("无权修改");
        c.setContent(content);
        commentMapper.updateById(c);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        if (!c.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        c.setIsDeleted(1);
        commentMapper.updateById(c);
    }

    @Transactional
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
            // 通知评论作者
            if (!c.getUserId().equals(userId)) {
                notificationService.sendNotification(c.getUserId(), "LIKE", userId, commentId, "赞了你的评论");
            }
        }
    }

    @Transactional
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

    /**
     * 批量转换评论分页 + 回复，减少 N+1 查询
     */
    private Page<CommentListResponse> convertToResponsePageWithReplies(Page<Comment> cPage) {
        List<Comment> comments = cPage.getRecords();
        if (comments == null || comments.isEmpty()) {
            Page<CommentListResponse> result = new Page<>();
            result.setCurrent(cPage.getCurrent());
            result.setSize(cPage.getSize());
            result.setTotal(cPage.getTotal());
            result.setRecords(List.of());
            return result;
        }

        // 批量查询所有评论用户
        Set<Long> allUserIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = allUserIds.stream()
                .map(uid -> userMapper.selectById(uid))
                .filter(u -> u != null)
                .collect(Collectors.toMap(User::getId, u -> u));

        // 查询所有回复
        Set<Long> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toSet());
        LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.in(Comment::getParentId, commentIds);
        replyWrapper.eq(Comment::getIsDeleted, 0);
        replyWrapper.eq(Comment::getIsHidden, 0);
        replyWrapper.orderByAsc(Comment::getCreateTime);
        List<Comment> allReplies = commentMapper.selectList(replyWrapper);
        Map<Long, List<Comment>> replyMap = allReplies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 批量查询回复中的用户
        Set<Long> replyUserIds = allReplies.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, User> replyUserMap = replyUserIds.stream()
                .map(uid -> userMapper.selectById(uid))
                .filter(u -> u != null)
                .collect(Collectors.toMap(User::getId, u -> u));

        Page<CommentListResponse> result = new Page<>();
        result.setCurrent(cPage.getCurrent());
        result.setSize(cPage.getSize());
        result.setTotal(cPage.getTotal());
        result.setRecords(comments.stream().map(c -> {
            CommentListResponse resp = new CommentListResponse();
            resp.setCommentId(c.getId());
            resp.setUserId(c.getUserId());
            User user = userMap.get(c.getUserId());
            resp.setNickname(user != null ? user.getNickname() : "未知");
            resp.setContent(c.getContent());
            resp.setLikeCount(c.getLikeCount());
            resp.setCreateTime(c.getCreateTime());
            resp.setReplyCount((long) replyMap.getOrDefault(c.getId(), List.of()).size());

            // 转换回复列表
            List<Comment> replies = replyMap.getOrDefault(c.getId(), List.of());
            resp.setReplies(replies.stream().map(r -> {
                CommentListResponse replyResp = new CommentListResponse();
                replyResp.setCommentId(r.getId());
                replyResp.setUserId(r.getUserId());
                User ru = replyUserMap.get(r.getUserId());
                replyResp.setNickname(ru != null ? ru.getNickname() : "未知");
                replyResp.setContent(r.getContent());
                replyResp.setLikeCount(r.getLikeCount());
                replyResp.setCreateTime(r.getCreateTime());
                replyResp.setReplyCount(0L);
                return replyResp;
            }).collect(Collectors.toList()));

            return resp;
        }).collect(Collectors.toList()));
        return result;
    }

    private long getReplyCount(Long parentId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, parentId)
               .eq(Comment::getIsDeleted, 0)
               .eq(Comment::getIsHidden, 0);
        return commentMapper.selectCount(wrapper);
    }
}
