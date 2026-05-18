package com.dreamshare.controller.Admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.config.AdminAuth;
import com.dreamshare.dto.NotificationDTO;
import com.dreamshare.entity.Notification;
import com.dreamshare.entity.User;
import com.dreamshare.mapper.NotificationMapper;
import com.dreamshare.mapper.UserMapper;
import com.dreamshare.service.OperationLogService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin
@AdminAuth
public class AdminNotificationController {

    @Autowired private NotificationMapper notificationMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private OperationLogService operationLogService;

    @GetMapping
    public Result<IPage<Notification>> listNotifications(
            @ModelAttribute("adminUser") User admin,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type) {
        LambdaQueryWrapper<Notification> qw = new LambdaQueryWrapper<>();
        if (userId != null) qw.eq(Notification::getUserId, userId);
        if (type != null && !type.isEmpty()) qw.eq(Notification::getType, type);
        qw.orderByDesc(Notification::getCreateTime);
        return Result.ok(notificationMapper.selectPage(new Page<>(page, size), qw));
    }

    @PostMapping("/send")
    @Transactional
    public Result<Void> sendNotification(
            @ModelAttribute("adminUser") User admin,
            @RequestBody NotificationDTO dto) {
        List<Long> userIds = new ArrayList<>();
        if (dto.getTargetUserId() != null) {
            userIds.add(dto.getTargetUserId());
        } else {
            List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<User>());
            for (User u : allUsers) userIds.add(u.getId());
        }
        int count = 0;
        for (Long uid : userIds) {
            Notification n = new Notification();
            n.setUserId(uid);
            n.setType(dto.getType() != null ? dto.getType() : "SYSTEM");
            n.setSourceUserId(admin.getId());
            n.setRelatedId(dto.getRelatedId());
            n.setContent(dto.getContent());
            n.setIsRead(0);
            n.setIsDeleted(0);
            n.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(n);
            count++;
        }
        operationLogService.log(admin.getId(), admin.getUsername(), "notification", "send", null, "system_notify", "发送通知共" + count + "人");
        return Result.ok();
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public Result<Void> deleteNotification(
            @ModelAttribute("adminUser") User admin,
            @PathVariable Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n == null) return Result.error("通知不存在");
        n.setIsDeleted(1);
        notificationMapper.updateById(n);
        operationLogService.log(admin.getId(), admin.getUsername(), "notification", "delete", id, "notify_" + id, "删除通知");
        return Result.ok();
    }
}
