     1|     1|<template>
     2|     2|    <view class="message-page">
     3|     3|        <!-- 自定义导航栏 -->
     4|     4|        <view class="navbar" :style="{height: statusBarHeight + 88 + 'rpx'}">
     5|     5|            <view class="navbar-content" :style="{paddingTop: statusBarHeight + 'rpx'}">
     6|     6|                <text class="title">{{ $t('message.title') }}</text>
     7|     7|            </view>
     8|     8|        </view>
     9|     9|        
    10|    10|        <view class="content" :style="{marginTop: (statusBarHeight + 88) + 'rpx'}">
    11|    11|            <!-- 通知入口 -->
    12|    12|            <view class="notice-card card" @click="goNotifications">
    13|    13|                <view class="notice-left">
    14|    14|                    <text class="notice-icon">🔔</text>
    15|    15|                    <text class="notice-title">{{ $t('message.systemNotice') }}</text>
    16|    16|                </view>
    17|    17|                <view class="notice-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
    18|    18|            </view>
    19|    19|            
    20|    20|            <!-- 最新通知列表 -->
    21|    21|            <view class="section-title">{{ $t('message.recent') }}</view>
    22|    22|            
    23|    23|            <view class="notification-item" v-for="notif in notifications" :key="notif.id" @click="readNotification(notif)">
    24|    24|                <image class="avatar" :src="'/static/default-avatar.png'" mode="aspectFill" />
    25|    25|                <view class="content-wrapper" :class="{unread: notif.isRead === 0 || notif.isRead === '0'}">
    26|    26|                    <text class="nickname">{{ notif.sourceNickname || '系统' }}</text>
    27|    27|                    <text class="content">{{ notif.content }}</text>
    28|    28|                    <text class="time">{{ formatTime(notif.createTime) }}</text>
    29|    29|                </view>
    30|    30|                <view class="unread-dot" v-if="notif.isRead === 0 || notif.isRead === '0'"></view>
    31|    31|            </view>
    32|    32|            
    33|    33|            <view class="empty-state" v-if="notifications.length === 0">
    34|    34|                <text class="empty-icon">📭</text>
    35|    35|                <text class="empty-text">{{ $t('message.empty') }}</text>
    36|    36|            </view>
    37|    37|        </view>
    38|    38|    </view>
    39|    39|
    40|    40|    <custom-tab-bar ref="tabbar" />
    41|    41|</template>
    42|    42|
    43|    43|<script>
    44|    44|import { notificationApi } from '@/utils/api';
    45|    45|import { isLoggedIn } from '@/utils/auth';
    46|    46|import dayjs from 'dayjs';
    47|    47|import relativeTime from 'dayjs/plugin/relativeTime.js';
    48|    48|import 'dayjs/locale/zh';
    49|    49|dayjs.extend(relativeTime);
    50|    50|dayjs.locale('zh');
    51|    51|
    52|    52|export default {
    53|    53|    data() {
    54|    54|        return {
    55|    55|            statusBarHeight: 0,
    56|    56|            notifications: [],
    57|    57|            unreadCount: 0
    58|    58|        };
    59|    59|    },
    60|    60|    onLoad() {
    61|    61|        this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
    62|    62|        if (isLoggedIn()) {
    63|    63|            this.loadNotifications();
    64|    64|        }
    65|    65|    },
    66|    66|    onShow() {
    67|    67|        if (isLoggedIn()) {
    68|    68|            this.loadNotifications();
    69|    69|        }
    70|    70|        this.$nextTick(() => {
    71|    71|            const tabbar = this.$refs.tabbar;
    72|    72|            if (tabbar) tabbar.updateCurrentPage();
    73|    73|        });
    74|    74|    },
    75|    75|    methods: {
    76|    76|        async loadNotifications() {
    77|    77|            try {
    78|    78|                this.unreadCount = await notificationApi.unreadCount();
    79|    79|                const res = await notificationApi.list(1, 20);
    80|    80|                this.notifications = res.records || [];
    81|    81|            } catch (e) {
    82|    82|                console.error('Load notifications failed:', e);
    83|    83|            }
    84|    84|        },
    85|    85|        
    86|    86|        goNotifications() {
    87|    87|            if (!isLoggedIn()) {
    88|    88|                uni.redirectTo({ url: '/pages/auth/login' });
    89|    89|                return;
    90|    90|            }
    91|    91|            uni.navigateTo({ url: '/pages/detail/notifications' });
    92|    92|        },
    93|    93|        
    94|    94|        async readNotification(notif) {
    95|    95|            if (notif.isRead === 0 || notif.isRead === '0') {
    96|    96|                try {
    97|    97|                    await notificationApi.markRead(notif.id);
    98|    98|                    notif.isRead = 1;
    99|    99|                    this.unreadCount = Math.max(0, this.unreadCount - 1);
   100|   100|                } catch (e) {
   101|   101|                    console.error('Mark read failed:', e);
   102|   102|                }
   103|   103|            }
   104|   104|        },
   105|   105|        
   106|   106|        formatTime(time) {
   107|   107|            if (!time) return '';
   108|   108|            return dayjs(time).fromNow();
   109|   109|        }
   110|   110|    }
   111|   111|};
   112|   112|</script>
   113|   113|
   114|   114|<style lang="scss" scoped>
   115|   115|.message-page {
   116|   116|    min-height: 100vh;
   117|   117|}
   118|   118|
   119|   119|.navbar {
   120|   120|    position: fixed;
   121|   121|    top: 0;
   122|   122|    left: 0;
   123|   123|    right: 0;
   124|   124|    z-index: 100;
   125|   125|    background: $glass-bg-gradient-1;
   126|   126|    backdrop-filter: blur(20px);
   127|   127|    -webkit-backdrop-filter: blur(20px);
   128|   128|    border-bottom: 1rpx solid $glass-card-bg;
   129|   129|    
   130|   130|    .navbar-content {
   131|   131|        height: 88rpx;
   132|   132|        display: flex;
   133|   133|        align-items: center;
   134|   134|        justify-content: center;
   135|   135|        padding: 0 24rpx;
   136|   136|        
   137|   137|        .title {
   138|   138|            font-size: 36rpx;
   139|   139|            font-weight: 700;
   140|   140|            color: $text-primary;
   141|   141|        }
   142|   142|    }
   143|   143|}
   144|   144|
   145|   145|.content {
   146|   146|    padding: 24rpx;
   147|   147|}
   148|   148|
   149|   149|.notice-card {
   150|   150|    display: flex;
   151|   151|    align-items: center;
   152|   152|    justify-content: space-between;
   153|   153|    padding: 28rpx 32rpx;
   154|   154|    margin-bottom: 32rpx;
   155|   155|    background: $glass-card-bg;
   156|   156|    backdrop-filter: blur(20px);
   157|   157|    -webkit-backdrop-filter: blur(20px);
   158|   158|    border: 1rpx solid $glass-card-bg-hover;
   159|   159|    
   160|   160|    .notice-left {
   161|   161|        display: flex;
   162|   162|        align-items: center;
   163|   163|        
   164|   164|        .notice-icon {
   165|   165|            font-size: 48rpx;
   166|   166|            margin-right: 20rpx;
   167|   167|        }
   168|   168|        
   169|   169|        .notice-title {
   170|   170|            font-size: 30rpx;
   171|   171|            font-weight: 600;
   172|   172|            color: $text-primary;
   173|   173|        }
   174|   174|    }
   175|   175|    
   176|   176|    .notice-badge {
   177|   177|        background: #E17055;
   178|   178|        color: $text-primary;
   179|   179|        border-radius: 24rpx;
   180|   180|        padding: 4rpx 16rpx;
   181|   181|        font-size: 22rpx;
   182|   182|        min-width: 40rpx;
   183|   183|        text-align: center;
   184|   184|    }
   185|   185|}
   186|   186|
   187|   187|.section-title {
   188|   188|    font-size: 28rpx;
   189|   189|    font-weight: 600;
   190|   190|    color: $text-secondary;
   191|   191|    margin-bottom: 16rpx;
   192|   192|    padding: 0 8rpx;
   193|   193|}
   194|   194|
   195|   195|.notification-item {
   196|   196|    display: flex;
   197|   197|    align-items: flex-start;
   198|   198|    padding: 24rpx 8rpx;
   199|   199|    border-bottom: 1rpx solid $glass-card-bg;
   200|   200|    background: $glass-card-bg-light;
   201|   201|    
   202|   202|    .avatar {
   203|   203|        width: 72rpx;
   204|   204|        height: 72rpx;
   205|   205|        border-radius: 50%;
   206|   206|        margin-right: 20rpx;
   207|   207|        flex-shrink: 0;
   208|   208|    }
   209|   209|    
   210|   210|    .content-wrapper {
   211|   211|        flex: 1;
   212|   212|        
   213|   213|        &.unread {
   214|   214|            .nickname {
   215|   215|                font-weight: 600;
   216|   216|            }
   217|   217|        }
   218|   218|        
   219|   219|        .nickname {
   220|   220|            font-size: 28rpx;
   221|   221|            color: $text-primary;
   222|   222|            display: block;
   223|   223|            margin-bottom: 8rpx;
   224|   224|        }
   225|   225|        
   226|   226|        .content {
   227|   227|            font-size: 26rpx;
   228|   228|            color: $text-secondary;
   229|   229|            display: block;
   230|   230|            margin-bottom: 8rpx;
   231|   231|        }
   232|   232|        
   233|   233|        .time {
   234|   234|            font-size: 22rpx;
   235|   235|            color: $text-tertiary;
   236|   236|            display: block;
   237|   237|        }
   238|   238|    }
   239|   239|    
   240|   240|    .unread-dot {
   241|   241|        width: 16rpx;
   242|   242|        height: 16rpx;
   243|   243|        background: #E17055;
   244|   244|        border-radius: 50%;
   245|   245|        flex-shrink: 0;
   246|   246|        margin-top: 28rpx;
   247|   247|        margin-left: 12rpx;
   248|   248|    }
   249|   249|}
   250|   250|
   251|   251|.empty-state {
   252|   252|    padding: 120rpx 0;
   253|   253|    text-align: center;
   254|   254|    
   255|   255|    .empty-icon {
   256|   256|        font-size: 100rpx;
   257|   257|        display: block;
   258|   258|        margin-bottom: 24rpx;
   259|   259|    }
   260|   260|    
   261|   261|    .empty-text {
   262|   262|        font-size: 28rpx;
   263|   263|        color: $text-tertiary;
   264|   264|    }
   265|   265|}
   266|   266|</style>
   267|   267|