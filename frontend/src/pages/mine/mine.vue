<template>
    <view class="mine-page">
        <!-- 头部信息 -->
        <view class="profile-header gradient-bg">
            <view class="user-info" v-if="isLoggedIn">
                <image class="avatar" :src="profile.avatar || '/static/default-avatar.png'" mode="aspectFill" @click="goProfile(myId)" />
                <view class="info">
                    <text class="nickname">{{ profile.nickname }}</text>
                    <text class="bio">{{ profile.bio || '这个人很懒，什么都没写' }}</text>
                </view>
                <text class="settings-icon" @click="goSettings">⚙️</text>
            </view>
            <view class="user-info" v-else @click="goLogin">
                <image class="avatar" src="/static/default-avatar.png" mode="aspectFill" />
                <view class="info">
                    <text class="nickname">点击登录</text>
                    <text class="bio">登录后查看个人信息</text>
                </view>
            </view>
        </view>
        
        <!-- 数据统计 -->
        <view class="stats-card card" v-if="isLoggedIn">
            <view class="stat-item" @click="goMyDreams">
                <text class="count">{{ profile.dreamCount || 0 }}</text>
                <text class="label">梦境记录</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item" @click="goMyDiscussions">
                <text class="count">{{ profile.discussionCount || 0 }}</text>
                <text class="label">讨论组</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
                <text class="count">{{ profile.followersCount || 0 }}</text>
                <text class="label">粉丝</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
                <text class="count">{{ profile.followingCount || 0 }}</text>
                <text class="label">关注</text>
            </view>
        </view>
        
        <!-- 菜单列表 -->
        <view class="menu-section">
            <view class="menu-item" @click="goMyDreams" v-if="isLoggedIn">
                <text class="menu-icon">💭</text>
                <text class="menu-text">我的梦境</text>
                <text class="menu-arrow">›</text>
            </view>
            <view class="menu-item" @click="goNotifications" v-if="isLoggedIn">
                <text class="menu-icon">🔔</text>
                <text class="menu-text">通知</text>
                <view class="menu-badge" v-if="unreadCount > 0">{{ unreadCount }}</view>
                <text class="menu-arrow">›</text>
            </view>
            <view class="menu-item" @click="goSettings">
                <text class="menu-icon">⚙️</text>
                <text class="menu-text">设置</text>
                <text class="menu-arrow">›</text>
            </view>
            <view class="menu-item" @click="goStats" v-if="isLoggedIn">
                <text class="menu-icon">📊</text>
                <text class="menu-text">梦境统计</text>
                <text class="menu-arrow">›</text>
            </view>
        </view>
        
        <!-- 退出登录 -->
        <view class="logout-section" v-if="isLoggedIn">
            <button class="logout-btn" @click="handleLogout">退出登录</button>
        </view>
    </view>
</template>

<script>
import { userApi, notificationApi, statsApi } from '@/utils/api';
import { isLoggedIn, clearAuth } from '@/utils/auth';

export default {
    data() {
        return {
            isLoggedIn: false,
            profile: {},
            myId: null,
            unreadCount: 0
        };
    },
    onShow() {
        this.isLoggedIn = isLoggedIn();
        if (this.isLoggedIn) {
            this.loadProfile();
            this.loadUnreadCount();
        }
    },
    methods: {
        async loadProfile() {
            try {
                this.profile = await userApi.getMyProfile();
                this.myId = this.profile.id;
            } catch (e) {
                console.error('Load profile failed:', e);
            }
        },
        
        async loadUnreadCount() {
            try {
                this.unreadCount = await notificationApi.unreadCount();
            } catch (e) {
                // ignore
            }
        },
        
        goProfile(id) {
            uni.navigateTo({ url: '/pages/detail/profile?id=' + id });
        },
        
        goLogin() {
            uni.navigateTo({ url: '/pages/auth/login' });
        },
        
        goSettings() {
            uni.navigateTo({ url: '/pages/detail/settings' });
        },
        
        goMyDreams() {
            uni.navigateTo({ url: '/pages/detail/dream-detail?type=mine' });
        },
        
        goMyDiscussions() {
            uni.switchTab({ url: '/pages/discussion/discussion' });
        },
        
        goNotifications() {
            uni.navigateTo({ url: '/pages/detail/notifications' });
        },
        
        goStats() {
            uni.navigateTo({ url: '/pages/detail/profile?id=' + this.myId + '&stats=1' });
        },
        
        async handleLogout() {
            uni.showModal({
                title: '提示',
                content: '确定要退出登录吗？',
                success: async (res) => {
                    if (res.confirm) {
                        clearAuth();
                        this.isLoggedIn = false;
                        this.profile = {};
                        uni.showToast({ title: '已退出登录', icon: 'success' });
                    }
                }
            });
        }
    }
};
</script>

<style lang="scss" scoped>
.mine-page {
    min-height: 100vh;
    background: #F8F9FE;
}

.profile-header {
    padding: calc(80rpx + env(safe-area-inset-top)) 32rpx 48rpx;
    
    .user-info {
        display: flex;
        align-items: center;
        
        .avatar {
            width: 120rpx;
            height: 120rpx;
            border-radius: 50%;
            border: 4rpx solid rgba(255,255,255,0.3);
            margin-right: 24rpx;
        }
        
        .info {
            flex: 1;
            
            .nickname {
                font-size: 36rpx;
                font-weight: 700;
                color: #FFFFFF;
                display: block;
                margin-bottom: 8rpx;
            }
            
            .bio {
                font-size: 24rpx;
                color: rgba(255,255,255,0.7);
                display: block;
            }
        }
        
        .settings-icon {
            font-size: 40rpx;
        }
    }
}

.stats-card {
    display: flex;
    align-items: center;
    justify-content: space-around;
    margin: -24rpx 32rpx 24rpx;
    padding: 32rpx;
    position: relative;
    z-index: 10;
    
    .stat-item {
        text-align: center;
        
        .count {
            font-size: 40rpx;
            font-weight: 700;
            color: #6C5CE7;
            display: block;
        }
        
        .label {
            font-size: 22rpx;
            color: #636E72;
            display: block;
            margin-top: 8rpx;
        }
    }
    
    .stat-divider {
        width: 2rpx;
        height: 60rpx;
        background: #E8E8E8;
    }
}

.menu-section {
    background: #FFFFFF;
    margin: 0 24rpx;
    border-radius: 24rpx;
    padding: 16rpx 0;
    box-shadow: 0 2rpx 12rpx rgba(108, 92, 231, 0.06);
    
    .menu-item {
        display: flex;
        align-items: center;
        padding: 24rpx 32rpx;
        position: relative;
        
        .menu-icon {
            font-size: 36rpx;
            margin-right: 20rpx;
        }
        
        .menu-text {
            flex: 1;
            font-size: 28rpx;
            color: #2D3436;
        }
        
        .menu-badge {
            background: #E17055;
            color: #FFFFFF;
            border-radius: 24rpx;
            padding: 2rpx 12rpx;
            font-size: 20rpx;
            margin-right: 8rpx;
            min-width: 32rpx;
            text-align: center;
        }
        
        .menu-arrow {
            font-size: 32rpx;
            color: #D0D0D0;
        }
    }
}

.logout-section {
    padding: 48rpx 32rpx;
    
    .logout-btn {
        background: #FFFFFF;
        color: #E17055;
        border-radius: 48rpx;
        font-size: 28rpx;
        border: 2rpx solid #E17055;
    }
}
</style>
