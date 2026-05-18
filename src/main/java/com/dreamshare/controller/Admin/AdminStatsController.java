package com.dreamshare.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.config.AdminAuth;
import com.dreamshare.entity.Comment;
import com.dreamshare.entity.ContentAudit;
import com.dreamshare.entity.Dream;
import com.dreamshare.entity.Discussion;
import com.dreamshare.entity.OperationLog;
import com.dreamshare.entity.User;
import com.dreamshare.mapper.CommentMapper;
import com.dreamshare.mapper.ContentAuditMapper;
import com.dreamshare.mapper.DiscussionMapper;
import com.dreamshare.mapper.DreamMapper;
import com.dreamshare.mapper.UserMapper;
import com.dreamshare.service.OperationLogService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@AdminAuth
public class AdminStatsController {

    @Autowired private UserMapper userMapper;
    @Autowired private DreamMapper dreamMapper;
    @Autowired private DiscussionMapper discussionMapper;
    @Autowired private CommentMapper commentMapper;
    @Autowired private ContentAuditMapper contentAuditMapper;
    @Autowired private OperationLogService operationLogService;

    @GetMapping("/api/admin/stats")
    public Result<Map<String, Object>> getStats(
            @ModelAttribute("adminUser") User admin) {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, 0));
        stats.put("totalUsers", totalUsers);

        long totalDreams = dreamMapper.selectCount(new LambdaQueryWrapper<Dream>());
        stats.put("totalDreams", totalDreams);

        long totalDiscussions = discussionMapper.selectCount(new LambdaQueryWrapper<Discussion>());
        stats.put("totalDiscussions", totalDiscussions);

        long totalComments = commentMapper.selectCount(new LambdaQueryWrapper<Comment>());
        stats.put("totalComments", totalComments);

        long bannedUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getIsBanned, 1));
        stats.put("bannedUsers", bannedUsers);

        long pendingAudits = contentAuditMapper.selectCount(new LambdaQueryWrapper<ContentAudit>().eq(ContentAudit::getAuditStatus, "PENDING"));
        stats.put("pendingAudits", pendingAudits);

        long pinnedDreams = dreamMapper.selectCount(new LambdaQueryWrapper<Dream>().eq(Dream::getIsPinned, 1));
        stats.put("pinnedDreams", pinnedDreams);

        stats.put("today", LocalDate.now().toString());

        return Result.ok(stats);
    }

    @GetMapping("/api/admin/stats/user-growth")
    public Result<Map<String, Object>> getUserGrowth(
            @ModelAttribute("adminUser") User admin,
            @RequestParam(defaultValue = "30") int days) {
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
        return Result.ok(result);
    }

    @GetMapping("/api/admin/stats/dream-growth")
    public Result<Map<String, Object>> getDreamGrowth(
            @ModelAttribute("adminUser") User admin,
            @RequestParam(defaultValue = "30") int days) {
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
        return Result.ok(result);
    }

    @GetMapping("/api/admin/logs")
    public Result<IPage<OperationLog>> listLogs(
            @ModelAttribute("adminUser") User admin,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String operation) {
        LambdaQueryWrapper<OperationLog> qw = new LambdaQueryWrapper<>();
        if (adminId != null) qw.eq(OperationLog::getAdminId, adminId);
        if (moduleName != null && !moduleName.isEmpty()) qw.like(OperationLog::getModuleName, moduleName);
        if (operation != null && !operation.isEmpty()) qw.eq(OperationLog::getOperation, operation);
        qw.orderByDesc(OperationLog::getCreateTime);
        return Result.ok(operationLogService.page(new Page<>(page, size), qw));
    }
}
