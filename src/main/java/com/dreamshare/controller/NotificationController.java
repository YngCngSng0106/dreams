package com.dreamshare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.NotificationResponse;
import com.dreamshare.service.NotificationService;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    @Autowired private NotificationService notificationService;

    @GetMapping
    public Result<Page<NotificationResponse>> list(@RequestAttribute Long userId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int pageSize,
                                                    @RequestParam(required = false) String type,
                                                    @RequestParam(required = false) Boolean isRead) {
        return Result.ok(notificationService.getNotifications(userId, page, pageSize, type, isRead));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestAttribute Long userId) {
        return Result.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@RequestAttribute Long userId, @PathVariable Long id) {
        notificationService.markRead(userId, id);
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead(@RequestAttribute Long userId) {
        notificationService.markAllRead(userId);
        return Result.ok();
    }
}
