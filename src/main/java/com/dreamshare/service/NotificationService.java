package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.NotificationResponse;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired private NotificationMapper notificationMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    public Page<NotificationResponse> getNotifications(Long userId, int page, int pageSize, String type, Boolean isRead) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (type != null) wrapper.eq(Notification::getType, type);
        if (isRead != null) wrapper.eq(Notification::getIsRead, isRead);
        wrapper.orderByDesc(Notification::getCreateTime);
        Page<Notification> nPage = notificationMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return convertToResponsePage(nPage);
    }

    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).eq(Notification::getIsRead, false);
        return notificationMapper.selectCount(wrapper);
    }

    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n == null) throw new RuntimeException("通知不存在");
        if (!n.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        n.setIsRead(true);
        notificationMapper.updateById(n);
    }

    public void markAllRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).eq(Notification::getIsRead, false);
        Notification update = new Notification();
        update.setIsRead(true);
        notificationMapper.update(update, wrapper);
    }

    public void sendNotification(Long userId, String type, Long sourceUserId, Long relatedId, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setSourceUserId(sourceUserId);
        n.setRelatedId(relatedId);
        n.setContent(content);
        n.setIsRead(false);
        n.setCreateTime(java.time.LocalDateTime.now());
        notificationMapper.insert(n);
        // WebSocket推送
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/topic/notifications",
            n
        );
    }

    private Page<NotificationResponse> convertToResponsePage(Page<Notification> nPage) {
        Page<NotificationResponse> result = new Page<>();
        result.setCurrent(nPage.getCurrent());
        result.setSize(nPage.getSize());
        result.setTotal(nPage.getTotal());
        result.setRecords(nPage.getRecords().stream().map(n -> {
            NotificationResponse resp = new NotificationResponse();
            resp.setId(n.getId());
            resp.setType(n.getType());
            resp.setSourceUserId(n.getSourceUserId());
            User src = userMapper.selectById(n.getSourceUserId());
            resp.setSourceNickname(src != null ? src.getNickname() : "未知");
            resp.setRelatedId(n.getRelatedId());
            resp.setContent(n.getContent());
            resp.setIsRead(n.getIsRead());
            resp.setCreateTime(n.getCreateTime());
            return resp;
        }).collect(Collectors.toList()));
        return result;
    }
}
