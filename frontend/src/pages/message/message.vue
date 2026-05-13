<template>
    <view class="message-page">
        <!-- 自定义导航栏 -->
        <view class="navbar" :style="{height: statusBarHeight + 88 + 'rpx'}">
            <view class="navbar-content" :style="{paddingTop: statusBarHeight + 'rpx'}">
                <text class="title">消息</text>
            </view>
        </view>
        
        <view class="content" :style="{marginTop: (statusBarHeight + 88) + 'rpx'}">
            <!-- 通知入口 -->
            <view class="notice-card card" @click="goNotifications">
                <view class="notice-left">
                    <text class="notice-icon">🔔</text>
                    <text class="notice-title">系统通知</text>
                </view>
                <view class="notice-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
            </view>
            
            <!-- 最新通知列表 -->
            <view class="section-title">最近通知</view>
            
            <view class="notification-item" v-for="notif in notifications" :key="notif.id" @click="readNotification(notif)">
                <image class="avatar" :src="'/static/default-avatar.png'" mode="aspectFill" />
                <view class="content-wrapper" :class="{unread: !notif.isRead}">
                    <text class="nickname">{{ notif.sourceNickname || '系统' }}</text>
                    <text class="content">{{ notif.content }}</text>
                    <text class="time">{{ formatTime(notif.createTime) }}</text>
                </view>
                <view class="unread-dot" v-if="!notif.isRead"></view>
            </view>
            
            <view class="empty-state" v-if="notifications.length === 0">
                <text class="empty-icon">📭</text>
                <text class="empty-text">暂无通知</text>
            </view>
        </view>
    </view>
</template>

<script>
import { notificationApi } from '@/utils/api';
import { isLoggedIn } from '@/utils/auth';
import dayjs from 'dayjs';

export default {
    data() {
        return {
            statusBarHeight: 0,
            notifications: [],
            unreadCount: 0
        };
    },
    onLoad() {
        this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
        if (isLoggedIn()) {
            this.loadNotifications();
        }
    },
    onShow() {
        if (isLoggedIn()) {
            this.loadNotifications();
        }
    },
    methods: {
        async loadNotifications() {
            try {
                this.unreadCount = await notificationApi.unreadCount();
                const res = await notificationApi.list(1, 20);
                this.notifications = res.records || [];
            } catch (e) {
                console.error('Load notifications failed:', e);
            }
        },
        
        goNotifications() {
            if (!isLoggedIn()) {
                uni.redirectTo({ url: '/pages/auth/login' });
                return;
            }
            uni.navigateTo({ url: '/pages/detail/notifications' });
        },
        
        async readNotification(notif) {
            if (!notif.isRead) {
                try {
                    await notificationApi.markRead(notif.id);
                    notif.isRead = true;
                    this.unreadCount = Math.max(0, this.unreadCount - 1);
                } catch (e) {
                    console.error('Mark read failed:', e);
                }
            }
        },
        
        formatTime(time) {
            if (!time) return '';
            return dayjs(time).fromNow();
        }
    }
};
</script>

<style lang="scss" scoped>
.message-page {
    min-height: 100vh;
    background: #F8F9FE;
}

.navbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
    
    .navbar-content {
        height: 88rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 24rpx;
        
        .title {
            font-size: 36rpx;
            font-weight: 700;
            color: #FFFFFF;
        }
    }
}

.content {
    padding: 24rpx;
}

.notice-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 28rpx 32rpx;
    margin-bottom: 32rpx;
    
    .notice-left {
        display: flex;
        align-items: center;
        
        .notice-icon {
            font-size: 48rpx;
            margin-right: 20rpx;
        }
        
        .notice-title {
            font-size: 30rpx;
            font-weight: 600;
            color: #2D3436;
        }
    }
    
    .notice-badge {
        background: #E17055;
        color: #FFFFFF;
        border-radius: 24rpx;
        padding: 4rpx 16rpx;
        font-size: 22rpx;
        min-width: 40rpx;
        text-align: center;
    }
}

.section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #636E72;
    margin-bottom: 16rpx;
    padding: 0 8rpx;
}

.notification-item {
    display: flex;
    align-items: flex-start;
    padding: 24rpx 8rpx;
    border-bottom: 2rpx solid #F0F0F0;
    
    .avatar {
        width: 72rpx;
        height: 72rpx;
        border-radius: 50%;
        margin-right: 20rpx;
        flex-shrink: 0;
    }
    
    .content-wrapper {
        flex: 1;
        
        &.unread {
            .nickname {
                font-weight: 600;
            }
        }
        
        .nickname {
            font-size: 28rpx;
            color: #2D3436;
            display: block;
            margin-bottom: 8rpx;
        }
        
        .content {
            font-size: 26rpx;
            color: #636E72;
            display: block;
            margin-bottom: 8rpx;
        }
        
        .time {
            font-size: 22rpx;
            color: #B2BEC3;
            display: block;
        }
    }
    
    .unread-dot {
        width: 16rpx;
        height: 16rpx;
        background: #E17055;
        border-radius: 50%;
        flex-shrink: 0;
        margin-top: 28rpx;
        margin-left: 12rpx;
    }
}

.empty-state {
    padding: 120rpx 0;
    text-align: center;
    
    .empty-icon {
        font-size: 100rpx;
        display: block;
        margin-bottom: 24rpx;
    }
    
    .empty-text {
        font-size: 28rpx;
        color: #B2BEC3;
    }
}
</style>
