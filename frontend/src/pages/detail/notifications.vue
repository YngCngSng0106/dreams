     1|<template>
     2|    <view class="notification-page">
     3|        <view class="header-bar">
     4|            <text class="title">{{ $t('detail.notifications') }}</text>
     5|            <text class="mark-all" @click="markAllRead" v-if="notifications.length > 0">{{ $t('detail.markAllRead') }}</text>
     6|        </view>
     7|        
     8|        <view class="notification-list">
     9|            <view class="notif-item card" v-for="notif in notifications" :key="notif.id" :class="{unread: notif.isRead === 0}" @click="readNotif(notif)">
    10|                <view class="notif-icon">{{ getNotifIcon(notif.type) }}</view>
    11|                <view class="notif-content">
    12|                    <text class="notif-text">{{ notif.sourceNickname }} {{ notif.content }}</text>
    13|                    <text class="notif-time">{{ formatTime(notif.createTime) }}</text>
    14|                </view>
    15|                <view class="unread-dot" v-if="notif.isRead === 0"></view>
    16|            </view>
    17|        </view>
    18|        
    19|        <view class="empty-state" v-if="notifications.length === 0">
    20|            <text class="empty-icon">🔔</text>
    21|            <text>{{ $t('message.empty') }}</text>
    22|        </view>
    23|    </view>
    24|</template>
    25|
    26|<script>
    27|import { notificationApi } from '@/utils/api';
    28|import dayjs from 'dayjs';
    29|import relativeTime from 'dayjs/plugin/relativeTime.js';
    30|import 'dayjs/locale/zh';
    31|dayjs.extend(relativeTime);
    32|dayjs.locale('zh');
    33|
    34|const typeIcons = {
    35|    LIKE: '❤️', JOIN: '👥', COMMENT: '💬', FOLLOW: '🔔', REPLY: '↩️', INVITE: '📨'
    36|};
    37|
    38|export default {
    39|    data() {
    40|        return {
    41|            notifications: [],
    42|            page: 1
    43|        };
    44|    },
    45|    onLoad() {
    46|        this.loadNotifications();
    47|    },
    48|    onPullDownRefresh() {
    49|        this.page = 1;
    50|        this.notifications = [];
    51|        this.loadNotifications().then(() => uni.stopPullDownRefresh());
    52|    },
    53|    methods: {
    54|        async loadNotifications() {
    55|            try {
    56|                const res = await notificationApi.list(this.page, 20);
    57|                this.notifications = this.notifications.concat(res.records || []);
    58|                this.page++;
    59|            } catch (e) {
    60|                console.error('Load notifications failed:', e);
    61|            }
    62|        },
    63|        
    64|        async readNotif(notif) {
    65|            if (notif.isRead === 0) {
    66|                try {
    67|                    await notificationApi.markRead(notif.id);
    68|                    notif.isRead = 1;
    69|                } catch (e) {
    70|                    // ignore
    71|                }
    72|            }
    73|        },
    74|        
    75|        async markAllRead() {
    76|            try {
    77|                await notificationApi.markAllRead();
    78|                this.notifications.forEach(n => n.isRead = 1);
    79|                uni.showToast({ title: this.$t('detail.markAllRead'), icon: 'success' });
    80|            } catch (e) {
    81|                console.error('Mark all read failed:', e);
    82|            }
    83|        },
    84|        
    85|        getNotifIcon(type) {
    86|            return typeIcons[type] || '📢';
    87|        },
    88|        
    89|        formatTime(time) {
    90|            if (!time) return '';
    91|            return dayjs(time).fromNow();
    92|        }
    93|    }
    94|};
    95|</script>
    96|
    97|<style lang="scss" scoped>
    98|.notification-page {
    99|    min-height: 100vh;
   100|}
   101|
   102|.header-bar {
   103|    display: flex;
   104|    justify-content: space-between;
   105|    align-items: center;
   106|    padding: 24rpx;
   107|    padding-top: calc(24rpx + env(safe-area-inset-top));
   108|    
   109|    .title {
   110|        font-size: 36rpx;
   111|        font-weight: 700;
   112|        color: $text-primary;
   113|    }
   114|    
   115|    .mark-all {
   116|        font-size: 26rpx;
   117|        color: #A29BFE;
   118|    }
   119|}
   120|
   121|.notification-list {
   122|    padding: 0 24rpx;
   123|    
   124|    .notif-item {
   125|        display: flex;
   126|        align-items: center;
   127|        padding: 24rpx;
   128|        margin-bottom: 16rpx;
   129|        
   130|        &.unread {
   131|            background: $primary-color;
   132|        }
   133|        
   134|        .notif-icon {
   135|            font-size: 44rpx;
   136|            margin-right: 20rpx;
   137|        }
   138|        
   139|        .notif-content {
   140|            flex: 1;
   141|            
   142|            .notif-text {
   143|                font-size: 28rpx;
   144|                color: $text-primary;
   145|                display: block;
   146|                margin-bottom: 8rpx;
   147|            }
   148|            
   149|            .notif-time {
   150|                font-size: 22rpx;
   151|                color: $text-tertiary;
   152|                display: block;
   153|            }
   154|        }
   155|        
   156|        .unread-dot {
   157|            width: 16rpx;
   158|            height: 16rpx;
   159|            background: #E17055;
   160|            border-radius: 50%;
   161|            flex-shrink: 0;
   162|        }
   163|    }
   164|}
   165|
   166|.empty-state {
   167|    text-align: center;
   168|    padding: 120rpx 0;
   169|    color: $text-tertiary;
   170|    
   171|    .empty-icon {
   172|        font-size: 80rpx;
   173|        display: block;
   174|        margin-bottom: 16rpx;
   175|    }
   176|}
   177|</style>