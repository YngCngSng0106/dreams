<template>
    <view class="notification-page">
        <view class="header-bar">
            <text class="title">{{ $t('detail.notifications') }}</text>
            <text class="mark-all" @click="markAllRead" v-if="notifications.length > 0">{{ $t('detail.markAllRead') }}</text>
        </view>
        
        <view class="notification-list">
            <view class="notif-item card" v-for="notif in notifications" :key="notif.id" :class="{unread: notif.isRead === 0}" @click="readNotif(notif)">
                <view class="notif-icon">{{ getNotifIcon(notif.type) }}</view>
                <view class="notif-content">
                    <text class="notif-text">{{ notif.sourceNickname }} {{ notif.content }}</text>
                    <text class="notif-time">{{ formatTime(notif.createTime) }}</text>
                </view>
                <view class="unread-dot" v-if="notif.isRead === 0"></view>
            </view>
        </view>
        
        <view class="empty-state" v-if="notifications.length === 0">
            <text class="empty-icon">🔔</text>
            <text>{{ $t('message.empty') }}</text>
        </view>
    </view>
</template>

<script>
import { notificationApi } from '@/utils/api';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime.js';
import 'dayjs/locale/zh';
dayjs.extend(relativeTime);
dayjs.locale('zh');

const typeIcons = {
    LIKE: '❤️', JOIN: '👥', COMMENT: '💬', FOLLOW: '🔔', REPLY: '↩️', INVITE: '📨'
};

export default {
    data() {
        return {
            notifications: [],
            page: 1
        };
    },
    onLoad() {
        this.loadNotifications();
    },
    onPullDownRefresh() {
        this.page = 1;
        this.notifications = [];
        this.loadNotifications().then(() => uni.stopPullDownRefresh());
    },
    methods: {
        async loadNotifications() {
            try {
                const res = await notificationApi.list(this.page, 20);
                this.notifications = this.notifications.concat(res.records || []);
                this.page++;
            } catch (e) {
                console.error('Load notifications failed:', e);
            }
        },
        
        async readNotif(notif) {
            if (notif.isRead === 0) {
                try {
                    await notificationApi.markRead(notif.id);
                    notif.isRead = 1;
                } catch (e) {
                    // ignore
                }
            }
        },
        
        async markAllRead() {
            try {
                await notificationApi.markAllRead();
                this.notifications.forEach(n => n.isRead = 1);
                uni.showToast({ title: this.$t('detail.markAllRead'), icon: 'success' });
            } catch (e) {
                console.error('Mark all read failed:', e);
            }
        },
        
        getNotifIcon(type) {
            return typeIcons[type] || '📢';
        },
        
        formatTime(time) {
            if (!time) return '';
            return dayjs(time).fromNow();
        }
    }
};
</script>

<style lang="scss" scoped>
.notification-page {
    min-height: 100vh;
    background: #F8F9FE;
}

.header-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    padding-top: calc(24rpx + env(safe-area-inset-top));
    
    .title {
        font-size: 36rpx;
        font-weight: 700;
        color: #2D3436;
    }
    
    .mark-all {
        font-size: 26rpx;
        color: #6C5CE7;
    }
}

.notification-list {
    padding: 0 24rpx;
    
    .notif-item {
        display: flex;
        align-items: center;
        padding: 24rpx;
        margin-bottom: 16rpx;
        
        &.unread {
            background: rgba(108, 92, 231, 0.04);
        }
        
        .notif-icon {
            font-size: 44rpx;
            margin-right: 20rpx;
        }
        
        .notif-content {
            flex: 1;
            
            .notif-text {
                font-size: 28rpx;
                color: #2D3436;
                display: block;
                margin-bottom: 8rpx;
            }
            
            .notif-time {
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
        }
    }
}

.empty-state {
    text-align: center;
    padding: 120rpx 0;
    color: #B2BEC3;
    
    .empty-icon {
        font-size: 80rpx;
        display: block;
        margin-bottom: 16rpx;
    }
}
</style>