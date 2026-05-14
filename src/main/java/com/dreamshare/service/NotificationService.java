package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamshare.dto.NotificationResponse;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
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
        if (isRead != null) wrapper.eq(Notification::getIsRead, isRead ? 1 : 0);
        wrapper.orderByDesc(Notification::getCreateTime);
        Page<Notification> nPage = notificationMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return convertToResponsePage(nPage);
    }

    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }

    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n == null) throw new RuntimeException("通知不存在");
        if (!n.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        n.setIsRead(1);
        notificationMapper.updateById(n);
    }

    public void markAllRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        Notification update = new Notification();
        update.setIsRead(1);
        notificationMapper.update(update, wrapper);
    }

    public void sendNotification(Long userId, String type, Long sourceUserId, Long relatedId, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setSourceUserId(sourceUserId);
        n.setRelatedId(relatedId);
        n.setContent(content);
        n.setIsRead(0);
        n.setIsDeleted(0);
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
            User src = n.getSourceUserId() != null ? userMapper.selectById(n.getSourceUserId()) : null;
            resp.setSourceNickname(src != null ? src.getNickname() : "系统");
            resp.setRelatedId(n.getRelatedId());
            resp.setContent(n.getContent());
            resp.setIsRead(n.getIsRead() != null && n.getIsRead() == 1);
            resp.setCreateTime(n.getCreateTime());
            return resp;
        }).collect(Collectors.toList()));
        return result;
    }
}
