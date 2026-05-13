package com.dreamshare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dreamshare.entity.*;
import com.dreamshare.service.AdminService;
import com.dreamshare.utils.JwtUtil;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private JwtUtil jwtUtil;

    // Extract admin info from JWT
    private AdminInfo getAdminInfo(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer "))
            throw new RuntimeException("未授权");
        String token = tokenHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) throw new RuntimeException("Token无效");
        
        com.dreamshare.entity.User user = adminService.getUserById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (user.getRole() == null || user.getRole() != 1)
            throw new RuntimeException("无管理员权限");
        
        AdminInfo info = new AdminInfo();
        info.setId(user.getId());
        info.setName(user.getUsername());
        return info;
    }

    static class AdminInfo {
        Long id;
        String name;
        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
    }

    // ========== 用户管理 ==========

    @GetMapping("/users")
    public Result<IPage<User>> listUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isBanned) {
        getAdminInfo(token); // verify admin
        return Result.ok(adminService.listUsers(page, size, keyword, isBanned));
    }

    @PostMapping("/users/{id}/ban")
    public Result<Void> banUser(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.banUser(admin.id, admin.name, id);
        return Result.ok();
    }

    @PostMapping("/users/{id}/unban")
    public Result<Void> unbanUser(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.unbanUser(admin.id, admin.name, id);
        return Result.ok();
    }

    @PostMapping("/users/{id}/delete")
    public Result<Void> deleteUser(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.deleteUser(admin.id, admin.name, id);
        return Result.ok();
    }

    // ========== 梦境管理 ==========

    @GetMapping("/dreams")
    public Result<IPage<Dream>> listDreams(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long userId) {
        getAdminInfo(token);
        return Result.ok(adminService.listDreams(page, size, keyword, categoryId, userId));
    }

    @GetMapping("/dreams/{id}")
    public Result<Dream> getDream(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        getAdminInfo(token);
        Dream dream = adminService.getDreamById(id);
        if (dream == null) return Result.error("梦境不存在");
        return Result.ok(dream);
    }

    @PostMapping("/dreams/{id}/delete")
    public Result<Void> deleteDream(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.deleteDream(admin.id, admin.name, id);
        return Result.ok();
    }

    @PostMapping("/dreams/{id}/pin")
    public Result<Void> pinDream(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.pinDream(admin.id, admin.name, id, 1);
        return Result.ok();
    }

    @PostMapping("/dreams/{id}/unpin")
    public Result<Void> unpinDream(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.pinDream(admin.id, admin.name, id, 0);
        return Result.ok();
    }

    // ========== 分类管理 ==========

    @GetMapping("/categories")
    public Result<List<DreamCategory>> listCategories(@RequestHeader("Authorization") String token) {
        getAdminInfo(token);
        return Result.ok(adminService.listCategories());
    }

    @PostMapping("/categories")
    public Result<DreamCategory> createCategory(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {
        AdminInfo admin = getAdminInfo(token);
        String name = String.valueOf(body.get("name"));
        String code = String.valueOf(body.get("code"));
        String icon = body.get("icon") != null ? String.valueOf(body.get("icon")) : null;
        String description = body.get("description") != null ? String.valueOf(body.get("description")) : null;
        int sortOrder = body.get("sortOrder") != null ? Integer.parseInt(String.valueOf(body.get("sortOrder"))) : 0;
        return Result.ok(adminService.createCategory(admin.id, admin.name, name, code, icon, description, sortOrder));
    }

    @PutMapping("/categories/{id}")
    public Result<DreamCategory> updateCategory(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        AdminInfo admin = getAdminInfo(token);
        String name = body.get("name") != null ? String.valueOf(body.get("name")) : null;
        String code = body.get("code") != null ? String.valueOf(body.get("code")) : null;
        String icon = body.get("icon") != null ? String.valueOf(body.get("icon")) : null;
        String description = body.get("description") != null ? String.valueOf(body.get("description")) : null;
        int sortOrder = body.get("sortOrder") != null ? Integer.parseInt(String.valueOf(body.get("sortOrder"))) : 0;
        return Result.ok(adminService.updateCategory(admin.id, admin.name, id, name, code, icon, description, sortOrder));
    }

    @PostMapping("/categories/{id}/delete")
    public Result<Void> deleteCategory(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.deleteCategory(admin.id, admin.name, id);
        return Result.ok();
    }

    // ========== 讨论组管理 ==========

    @GetMapping("/discussions")
    public Result<IPage<Discussion>> listDiscussions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        getAdminInfo(token);
        return Result.ok(adminService.listDiscussions(page, size, keyword));
    }

    @GetMapping("/discussions/{id}/members")
    public Result<List<User>> getMembers(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        getAdminInfo(token);
        return Result.ok(adminService.getDiscussionMembers(id));
    }

    @PostMapping("/discussions/{id}/delete")
    public Result<Void> deleteDiscussion(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.deleteDiscussion(admin.id, admin.name, id);
        return Result.ok();
    }

    // ========== 评论管理 ==========

    @GetMapping("/comments")
    public Result<IPage<Comment>> listComments(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long discussionId,
            @RequestParam(required = false) Long userId) {
        getAdminInfo(token);
        return Result.ok(adminService.listComments(page, size, keyword, discussionId, userId));
    }

    @PostMapping("/comments/{id}/delete")
    public Result<Void> deleteComment(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.deleteComment(admin.id, admin.name, id);
        return Result.ok();
    }

    @PostMapping("/comments/{id}/hide")
    public Result<Void> hideComment(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.hideComment(admin.id, admin.name, id, 1);
        return Result.ok();
    }

    @PostMapping("/comments/{id}/unhide")
    public Result<Void> unhideComment(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.hideComment(admin.id, admin.name, id, 0);
        return Result.ok();
    }

    // ========== 内容审核 ==========

    @GetMapping("/audits")
    public Result<IPage<ContentAudit>> listAudits(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType) {
        getAdminInfo(token);
        return Result.ok(adminService.listAudits(page, size, status, targetType));
    }

    @PostMapping("/audits/{id}/approve")
    public Result<Void> approve(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.approveAudit(admin.id, admin.name, id);
        return Result.ok();
    }

    @PostMapping("/audits/{id}/reject")
    public Result<Void> reject(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        AdminInfo admin = getAdminInfo(token);
        String reason = body.get("reason");
        adminService.rejectAudit(admin.id, admin.name, id, reason);
        return Result.ok();
    }

    // ========== 通知管理 ==========

    @GetMapping("/notifications")
    public Result<IPage<Notification>> listNotifications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type) {
        getAdminInfo(token);
        return Result.ok(adminService.listNotifications(page, size, userId, type));
    }

    @PostMapping("/notifications/send")
    public Result<Void> sendNotification(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {
        AdminInfo admin = getAdminInfo(token);
        Long targetUserId = body.get("targetUserId") != null ? Long.parseLong(String.valueOf(body.get("targetUserId"))) : null;
        String content = String.valueOf(body.get("content"));
        String type = body.get("type") != null ? String.valueOf(body.get("type")) : "SYSTEM";
        Long relatedId = body.get("relatedId") != null ? Long.parseLong(String.valueOf(body.get("relatedId"))) : null;
        adminService.sendSystemNotification(admin.id, admin.name, targetUserId, content, type, relatedId);
        return Result.ok();
    }

    @PostMapping("/notifications/{id}/delete")
    public Result<Void> deleteNotification(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.deleteNotification(admin.id, admin.name, id);
        return Result.ok();
    }

    // ========== 审核关键词 ==========

    @GetMapping("/keywords")
    public Result<List<AuditKeyword>> listKeywords(@RequestHeader("Authorization") String token) {
        getAdminInfo(token);
        return Result.ok(adminService.listKeywords());
    }

    @PostMapping("/keywords")
    public Result<AuditKeyword> createKeyword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> body) {
        AdminInfo admin = getAdminInfo(token);
        String keyword = body.get("keyword");
        String keywordType = body.get("keywordType") != null ? body.get("keywordType") : "SPAM";
        String severity = body.get("severity") != null ? body.get("severity") : "LOW";
        return Result.ok(adminService.createKeyword(admin.id, admin.name, keyword, keywordType, severity));
    }

    @PutMapping("/keywords/{id}")
    public Result<AuditKeyword> updateKeyword(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        AdminInfo admin = getAdminInfo(token);
        String keyword = body.get("keyword");
        String keywordType = body.get("keywordType");
        String severity = body.get("severity");
        return Result.ok(adminService.updateKeyword(admin.id, admin.name, id, keyword, keywordType, severity));
    }

    @PostMapping("/keywords/{id}/delete")
    public Result<Void> deleteKeyword(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        AdminInfo admin = getAdminInfo(token);
        adminService.deleteKeyword(admin.id, admin.name, id);
        return Result.ok();
    }

    // ========== 数据统计 ==========

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestHeader("Authorization") String token) {
        getAdminInfo(token);
        return Result.ok(adminService.getStats());
    }

    @GetMapping("/stats/user-growth")
    public Result<Map<String, Object>> getUserGrowth(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "30") int days) {
        getAdminInfo(token);
        return Result.ok(adminService.getUserGrowth(days));
    }

    @GetMapping("/stats/dream-growth")
    public Result<Map<String, Object>> getDreamGrowth(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "30") int days) {
        getAdminInfo(token);
        return Result.ok(adminService.getDreamGrowth(days));
    }

    // ========== 操作日志 ==========

    @GetMapping("/logs")
    public Result<IPage<OperationLog>> listLogs(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String operation) {
        AdminInfo admin = getAdminInfo(token);
        return Result.ok(adminService.listLogs(page, size, adminId, moduleName, operation));
    }
}
