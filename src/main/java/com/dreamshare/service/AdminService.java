package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminService {

    @Autowired private UserMapper userMapper;
    @Autowired private DreamMapper dreamMapper;
    @Autowired private DreamCategoryMapper categoryMapper;
    @Autowired private DiscussionMapper discussionMapper;
    @Autowired private DiscussionMemberMapper discussionMemberMapper;
    @Autowired private CommentMapper commentMapper;
    @Autowired private NotificationMapper notificationMapper;
    @Autowired private ContentAuditMapper contentAuditMapper;
    @Autowired private AuditKeywordMapper auditKeywordMapper;
    @Autowired private OperationLogService operationLogService;

    // ========== 用户管理 ==========

    public IPage<User> listUsers(int page, int size, String keyword, Integer isBanned) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword).or().like(User::getEmail, keyword));
        }
        if (isBanned != null) qw.eq(User::getIsBanned, isBanned);
        // exclude admin account
        qw.ne(User::getRole, 1);
        qw.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(new Page<>(page, size), qw);
    }

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Transactional
    public void banUser(Long adminId, String adminName, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setIsBanned(1);
        userMapper.updateById(user);
        operationLogService.log(adminId, adminName, "user", "ban", userId, user.getUsername(), "封禁用户");
    }

    @Transactional
    public void unbanUser(Long adminId, String adminName, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setIsBanned(0);
        userMapper.updateById(user);
        operationLogService.log(adminId, adminName, "user", "unban", userId, user.getUsername(), "解封用户");
    }

    @Transactional
    public void deleteUser(Long adminId, String adminName, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setIsDeleted(1);
        userMapper.updateById(user);
        operationLogService.log(adminId, adminName, "user", "delete", userId, user.getUsername(), "逻辑删除用户");
    }

    // ========== 梦境管理 ==========

    public IPage<Dream> listDreams(int page, int size, String keyword, Long categoryId, Long userId) {
        LambdaQueryWrapper<Dream> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(Dream::getDescription, keyword).or().like(Dream::getKeywords, keyword));
        }
        if (categoryId != null) qw.eq(Dream::getCategoryId, categoryId);
        if (userId != null) qw.eq(Dream::getUserId, userId);
        qw.orderByDesc(Dream::getIsPinned).orderByDesc(Dream::getCreateTime);
        return dreamMapper.selectPage(new Page<>(page, size), qw);
    }

    public Dream getDreamById(Long id) {
        return dreamMapper.selectById(id);
    }

    @Transactional
    public void deleteDream(Long adminId, String adminName, Long dreamId) {
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream == null) throw new RuntimeException("梦境不存在");
        dream.setIsDeleted(1);
        dreamMapper.updateById(dream);
        operationLogService.log(adminId, adminName, "dream", "delete", dreamId, "dream_" + dreamId, "逻辑删除梦境");
    }

    @Transactional
    public void pinDream(Long adminId, String adminName, Long dreamId, Integer isPinned) {
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream == null) throw new RuntimeException("梦境不存在");
        dream.setIsPinned(isPinned);
        dreamMapper.updateById(dream);
        String op = isPinned == 1 ? "pin" : "unpin";
        operationLogService.log(adminId, adminName, "dream", op, dreamId, "dream_" + dreamId, isPinned == 1 ? "置顶" : "取消置顶");
    }

    // ========== 分类管理 ==========

    public List<DreamCategory> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<DreamCategory>().orderByAsc(DreamCategory::getSortOrder));
    }

    @Transactional
    public DreamCategory createCategory(Long adminId, String adminName, String name, String code, String icon, String description, int sortOrder) {
        // check unique code
        LambdaQueryWrapper<DreamCategory> qw = new LambdaQueryWrapper<>();
        qw.eq(DreamCategory::getCode, code);
        if (categoryMapper.selectCount(qw) > 0) throw new RuntimeException("分类编码已存在: " + code);
        DreamCategory cat = new DreamCategory();
        cat.setName(name);
        cat.setCode(code);
        cat.setIcon(icon);
        cat.setDescription(description);
        cat.setSortOrder(sortOrder);
        cat.setIsDeleted(0);
        categoryMapper.insert(cat);
        operationLogService.log(adminId, adminName, "category", "create", cat.getId(), name, "新增分类");
        return cat;
    }

    @Transactional
    public DreamCategory updateCategory(Long adminId, String adminName, Long id, String name, String code, String icon, String description, int sortOrder) {
        DreamCategory cat = categoryMapper.selectById(id);
        if (cat == null) throw new RuntimeException("分类不存在");
        if (code != null && !code.equals(cat.getCode())) {
            LambdaQueryWrapper<DreamCategory> qw = new LambdaQueryWrapper<>();
            qw.eq(DreamCategory::getCode, code).ne(DreamCategory::getId, id);
            if (categoryMapper.selectCount(qw) > 0) throw new RuntimeException("分类编码已存在: " + code);
            cat.setCode(code);
        }
        if (name != null) cat.setName(name);
        if (icon != null) cat.setIcon(icon);
        if (description != null) cat.setDescription(description);
        cat.setSortOrder(sortOrder);
        categoryMapper.updateById(cat);
        operationLogService.log(adminId, adminName, "category", "update", id, name != null ? name : cat.getName(), "修改分类");
        return cat;
    }

    @Transactional
    public void deleteCategory(Long adminId, String adminName, Long id) {
        DreamCategory cat = categoryMapper.selectById(id);
        if (cat == null) throw new RuntimeException("分类不存在");
        cat.setIsDeleted(1);
        categoryMapper.updateById(cat);
        operationLogService.log(adminId, adminName, "category", "delete", id, cat.getName(), "逻辑删除分类");
    }

    // ========== 讨论组管理 ==========

    public IPage<Discussion> listDiscussions(int page, int size, String keyword) {
        LambdaQueryWrapper<Discussion> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) qw.like(Discussion::getTitle, keyword);
        qw.orderByDesc(Discussion::getCreateTime);
        return discussionMapper.selectPage(new Page<>(page, size), qw);
    }

    public List<User> getDiscussionMembers(Long discussionId) {
        LambdaQueryWrapper<DiscussionMember> qw = new LambdaQueryWrapper<>();
        qw.eq(DiscussionMember::getDiscussionId, discussionId);
        List<DiscussionMember> members = discussionMemberMapper.selectList(qw);
        List<User> result = new ArrayList<>();
        for (DiscussionMember dm : members) {
            User u = userMapper.selectById(dm.getUserId());
            if (u != null) result.add(u);
        }
        return result;
    }

    @Transactional
    public void deleteDiscussion(Long adminId, String adminName, Long discussionId) {
        Discussion d = discussionMapper.selectById(discussionId);
        if (d == null) throw new RuntimeException("讨论组不存在");
        d.setIsDeleted(1);
        discussionMapper.updateById(d);
        operationLogService.log(adminId, adminName, "discussion", "delete", discussionId, d.getTitle(), "逻辑删除讨论组");
    }

    // ========== 评论管理 ==========

    public IPage<Comment> listComments(int page, int size, String keyword, Long discussionId, Long userId) {
        LambdaQueryWrapper<Comment> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) qw.like(Comment::getContent, keyword);
        if (discussionId != null) qw.eq(Comment::getDiscussionId, discussionId);
        if (userId != null) qw.eq(Comment::getUserId, userId);
        qw.orderByDesc(Comment::getCreateTime);
        return commentMapper.selectPage(new Page<>(page, size), qw);
    }

    @Transactional
    public void deleteComment(Long adminId, String adminName, Long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        c.setIsDeleted(1);
        commentMapper.updateById(c);
        operationLogService.log(adminId, adminName, "comment", "delete", commentId, "comment_" + commentId, "逻辑删除评论");
    }

    @Transactional
    public void hideComment(Long adminId, String adminName, Long commentId, Integer isHidden) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        c.setIsHidden(isHidden);
        commentMapper.updateById(c);
        String op = isHidden == 1 ? "hide" : "unhide";
        operationLogService.log(adminId, adminName, "comment", op, commentId, "comment_" + commentId, isHidden == 1 ? "隐藏评论" : "取消隐藏");
    }

    // ========== 内容审核 ==========

    public IPage<ContentAudit> listAudits(int page, int size, String status, String targetType) {
        LambdaQueryWrapper<ContentAudit> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) qw.eq(ContentAudit::getAuditStatus, status);
        if (targetType != null && !targetType.isEmpty()) qw.eq(ContentAudit::getTargetType, targetType);
        qw.orderByDesc(ContentAudit::getCreateTime);
        return contentAuditMapper.selectPage(new Page<>(page, size), qw);
    }

    @Transactional
    public void approveAudit(Long adminId, String adminName, Long auditId) {
        ContentAudit audit = contentAuditMapper.selectById(auditId);
        if (audit == null) throw new RuntimeException("审核记录不存在");
        audit.setAuditStatus("PASSED");
        audit.setAuditedAt(LocalDateTime.now());
        contentAuditMapper.updateById(audit);
        operationLogService.log(adminId, adminName, "audit", "approve", auditId, audit.getTargetType() + "_" + audit.getTargetId(), "审核通过");
    }

    @Transactional
    public void rejectAudit(Long adminId, String adminName, Long auditId, String reason) {
        ContentAudit audit = contentAuditMapper.selectById(auditId);
        if (audit == null) throw new RuntimeException("审核记录不存在");
        audit.setAuditStatus("REJECTED");
        audit.setRejectReason(reason);
        audit.setAuditedAt(LocalDateTime.now());
        contentAuditMapper.updateById(audit);
        operationLogService.log(adminId, adminName, "audit", "reject", auditId, audit.getTargetType() + "_" + audit.getTargetId(), "审核驳回: " + reason);
    }

    // ========== 通知管理 ==========

    public IPage<Notification> listNotifications(int page, int size, Long userId, String type) {
        LambdaQueryWrapper<Notification> qw = new LambdaQueryWrapper<>();
        if (userId != null) qw.eq(Notification::getUserId, userId);
        if (type != null && !type.isEmpty()) qw.eq(Notification::getType, type);
        qw.orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(new Page<>(page, size), qw);
    }

    @Transactional
    public void sendSystemNotification(Long adminId, String adminName, Long targetUserId, String content, String type, Long relatedId) {
        List<Long> userIds = new ArrayList<>();
        if (targetUserId != null) {
            userIds.add(targetUserId);
        } else {
            // send to all users
            List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<User>());
            for (User u : allUsers) userIds.add(u.getId());
        }
        int count = 0;
        for (Long uid : userIds) {
            Notification n = new Notification();
            n.setUserId(uid);
            n.setType(type != null ? type : "SYSTEM");
            n.setSourceUserId(adminId);
            n.setRelatedId(relatedId);
            n.setContent(content);
            n.setIsRead(false);
            n.setIsDeleted(0);
            n.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(n);
            count++;
        }
        operationLogService.log(adminId, adminName, "notification", "send", null, "system_notify", "发送通知共" + count + "人");
    }

    @Transactional
    public void deleteNotification(Long adminId, String adminName, Long notificationId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n == null) throw new RuntimeException("通知不存在");
        n.setIsDeleted(1);
        notificationMapper.updateById(n);
        operationLogService.log(adminId, adminName, "notification", "delete", notificationId, "notify_" + notificationId, "删除通知");
    }

    // ========== 审核关键词 ==========

    public List<AuditKeyword> listKeywords() {
        return auditKeywordMapper.selectList(new LambdaQueryWrapper<AuditKeyword>().orderByAsc(AuditKeyword::getId));
    }

    @Transactional
    public AuditKeyword createKeyword(Long adminId, String adminName, String keyword, String keywordType, String severity) {
        AuditKeyword ak = new AuditKeyword();
        ak.setKeyword(keyword);
        ak.setKeywordType(keywordType);
        ak.setSeverity(severity);
        ak.setIsDeleted(0);
        auditKeywordMapper.insert(ak);
        operationLogService.log(adminId, adminName, "audit_keyword", "create", ak.getId(), keyword, "新增审核关键词");
        return ak;
    }

    @Transactional
    public AuditKeyword updateKeyword(Long adminId, String adminName, Long id, String keyword, String keywordType, String severity) {
        AuditKeyword ak = auditKeywordMapper.selectById(id);
        if (ak == null) throw new RuntimeException("关键词不存在");
        if (keyword != null) ak.setKeyword(keyword);
        if (keywordType != null) ak.setKeywordType(keywordType);
        if (severity != null) ak.setSeverity(severity);
        auditKeywordMapper.updateById(ak);
        operationLogService.log(adminId, adminName, "audit_keyword", "update", id, keyword != null ? keyword : ak.getKeyword(), "修改审核关键词");
        return ak;
    }

    @Transactional
    public void deleteKeyword(Long adminId, String adminName, Long id) {
        AuditKeyword ak = auditKeywordMapper.selectById(id);
        if (ak == null) throw new RuntimeException("关键词不存在");
        ak.setIsDeleted(1);
        auditKeywordMapper.updateById(ak);
        operationLogService.log(adminId, adminName, "audit_keyword", "delete", id, ak.getKeyword(), "逻辑删除审核关键词");
    }

    // ========== 数据统计 ==========

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // total users (exclude admin)
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, 0));
        stats.put("totalUsers", totalUsers);
        
        // total dreams
        long totalDreams = dreamMapper.selectCount(new LambdaQueryWrapper<Dream>());
        stats.put("totalDreams", totalDreams);
        
        // total discussions
        long totalDiscussions = discussionMapper.selectCount(new LambdaQueryWrapper<Discussion>());
        stats.put("totalDiscussions", totalDiscussions);
        
        // total comments
        long totalComments = commentMapper.selectCount(new LambdaQueryWrapper<Comment>());
        stats.put("totalComments", totalComments);
        
        // banned users
        long bannedUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getIsBanned, 1));
        stats.put("bannedUsers", bannedUsers);
        
        // pending audits
        long pendingAudits = contentAuditMapper.selectCount(new LambdaQueryWrapper<ContentAudit>().eq(ContentAudit::getAuditStatus, "PENDING"));
        stats.put("pendingAudits", pendingAudits);
        
        // pinned dreams
        long pinnedDreams = dreamMapper.selectCount(new LambdaQueryWrapper<Dream>().eq(Dream::getIsPinned, 1));
        stats.put("pinnedDreams", pinnedDreams);
        
        stats.put("today", LocalDate.now().toString());
        
        return stats;
    }

    public Map<String, Object> getUserGrowth(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.toString());
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            qw.eq(User::getRole, 0);
            qw.ge(User::getCreateTime, date.atStartOfDay());
            qw.lt(User::getCreateTime, date.plusDays(1).atStartOfDay());
            counts.add(userMapper.selectCount(qw).intValue());
        }
        result.put("dates", dates);
        result.put("counts", counts);
        return result;
    }

    public Map<String, Object> getDreamGrowth(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.toString());
            LambdaQueryWrapper<Dream> qw = new LambdaQueryWrapper<>();
            qw.ge(Dream::getCreateTime, date.atStartOfDay());
            qw.lt(Dream::getCreateTime, date.plusDays(1).atStartOfDay());
            counts.add(dreamMapper.selectCount(qw).intValue());
        }
        result.put("dates", dates);
        result.put("counts", counts);
        return result;
    }

    // ========== 操作日志 ==========

    public IPage<OperationLog> listLogs(int page, int size, Long adminId, String moduleName, String operation) {
        LambdaQueryWrapper<OperationLog> qw = new LambdaQueryWrapper<>();
        if (adminId != null) qw.eq(OperationLog::getAdminId, adminId);
        if (moduleName != null && !moduleName.isEmpty()) qw.like(OperationLog::getModuleName, moduleName);
        if (operation != null && !operation.isEmpty()) qw.eq(OperationLog::getOperation, operation);
        qw.orderByDesc(OperationLog::getCreateTime);
        return operationLogService.page(new Page<>(page, size), qw);
    }
}
