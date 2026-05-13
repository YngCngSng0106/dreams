<template>
    <view class="profile-page">
        <view class="profile-header gradient-bg" v-if="user">
            <image class="avatar" :src="user.avatar || '/static/default-avatar.png'" mode="aspectFill" />
            <text class="nickname">{{ user.nickname }}</text>
            <text class="bio">{{ user.bio || '这个人很懒，什么都没写' }}</text>
        </view>
        
        <view class="stats-row card">
            <view class="stat-item">
                <text class="count">{{ user.dreamCount || 0 }}</text>
                <text class="label">梦境</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
                <text class="count">{{ user.followingCount || 0 }}</text>
                <text class="label">关注</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
                <text class="count">{{ user.followersCount || 0 }}</text>
                <text class="label">粉丝</text>
            </view>
        </view>
        
        <view class="action-row" v-if="isOtherUser">
            <button class="btn-primary" :class="{following: followStatus?.isFollowing}" @click="toggleFollow">
                {{ followStatus?.isFollowing ? '已关注' : '+ 关注' }}
            </button>
        </view>
        
        <!-- Tab切换 -->
        <view class="tab-bar">
            <view class="tab-item" :class="{active: currentTab === 'dreams'}" @click="currentTab = 'dreams'">梦境记录</view>
            <view class="tab-item" :class="{active: currentTab === 'stats'}" @click="loadStats">统计数据</view>
        </view>
        
        <!-- 梦境列表 -->
        <view class="dream-list" v-if="currentTab === 'dreams'">
            <view class="dream-item card" v-for="dream in dreams" :key="dream.id" @click="goDetail(dream.id)">
                <text class="category">{{ dream.category }}</text>
                <text class="description">{{ dream.description }}</text>
                <text class="date">{{ dream.dreamDate }}</text>
            </view>
        </view>
        
        <!-- 统计信息 -->
        <view class="stats-detail card" v-if="currentTab === 'stats' && stats">
            <view class="stat-row">
                <text class="label">最常做的梦</text>
                <text class="value">{{ stats.topCategories?.[0]?.categoryName || '暂无' }}</text>
            </view>
            <view class="stat-row">
                <text class="label">平均清晰度</text>
                <text class="value">{{ (stats.avgClarity || 0).toFixed(1) }}⭐</text>
            </view>
            <view class="stat-row">
                <text class="label">本月新增</text>
                <text class="value">{{ stats.monthlyDreamCount || 0 }}个</text>
            </view>
        </view>
        
        <view class="empty-state" v-if="!user">
            <text>用户不存在</text>
        </view>
    </view>
</template>

<script>
import { userApi, followApi, statsApi } from '@/utils/api';
import { getUserId, isLoggedIn } from '@/utils/auth';

export default {
    data() {
        return {
            userId: 0,
            user: null,
            dreams: [],
            stats: null,
            currentTab: 'dreams',
            followStatus: null,
            isOtherUser: true
        };
    },
    onLoad(options) {
        this.userId = options.id;
        this.isOtherUser = this.userId !== getUserId();
        this.loadProfile();
        this.loadDreams();
        if (this.isOtherUser && isLoggedIn()) {
            this.loadFollowStatus();
        }
    },
    methods: {
        async loadProfile() {
            try {
                this.user = await userApi.getUserProfile(this.userId);
            } catch (e) {
                console.error('Load profile failed:', e);
            }
        },
        
        async loadDreams() {
            try {
                const res = await userApi.getUserDreams(this.userId);
                this.dreams = res.records || [];
            } catch (e) {
                console.error('Load dreams failed:', e);
            }
        },
        
        async loadFollowStatus() {
            try {
                this.followStatus = await followApi.status(this.userId);
            } catch (e) {
                // ignore
            }
        },
        
        async toggleFollow() {
            if (!isLoggedIn()) {
                uni.redirectTo({ url: '/pages/auth/login' });
                return;
            }
            try {
                if (this.followStatus?.isFollowing) {
                    await followApi.unfollow(this.userId);
                    this.followStatus.isFollowing = false;
                    this.user.followersCount = Math.max(0, (this.user.followersCount || 1) - 1);
                } else {
                    await followApi.follow(this.userId);
                    this.followStatus = { isFollowing: true, isFollower: false };
                    this.user.followersCount = (this.user.followersCount || 0) + 1;
                }
            } catch (e) {
                console.error('Follow failed:', e);
            }
        },
        
        async loadStats() {
            this.currentTab = 'stats';
            if (this.userId === getUserId()) {
                try {
                    this.stats = await statsApi.myStats();
                } catch (e) {
                    console.error('Load stats failed:', e);
                }
            }
        },
        
        goDetail(id) {
            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + id });
        }
    }
};
</script>

<style lang="scss" scoped>
.profile-page {
    min-height: 100vh;
    background: #F8F9FE;
}

.profile-header {
    padding: calc(80rpx + env(safe-area-inset-top)) 32rpx 48rpx;
    text-align: center;
    
    .avatar {
        width: 120rpx;
        height: 120rpx;
        border-radius: 50%;
        border: 4rpx solid rgba(255,255,255,0.3);
        display: block;
        margin: 0 auto 16rpx;
    }
    
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

.stats-row {
    display: flex;
    align-items: center;
    justify-content: space-around;
    margin: -24rpx 32rpx 24rpx;
    padding: 24rpx;
    position: relative;
    z-index: 10;
    
    .stat-item {
        text-align: center;
        .count { font-size: 36rpx; font-weight: 700; color: #6C5CE7; display: block; }
        .label { font-size: 22rpx; color: #636E72; display: block; margin-top: 4rpx; }
    }
    
    .stat-divider {
        width: 2rpx;
        height: 60rpx;
        background: #E8E8E8;
    }
}

.action-row {
    padding: 0 32rpx;
    margin-bottom: 24rpx;
    
    .btn-primary {
        width: 100%;
        &.following {
            background: #F0F0F5;
            color: #636E72;
        }
    }
}

.tab-bar {
    display: flex;
    background: #FFFFFF;
    padding: 16rpx 24rpx;
    
    .tab-item {
        flex: 1;
        text-align: center;
        padding: 12rpx 0;
        font-size: 28rpx;
        color: #636E72;
        border-radius: 12rpx;
        
        &.active {
            color: #6C5CE7;
            font-weight: 600;
            background: rgba(108, 92, 231, 0.08);
        }
    }
}

.dream-list {
    padding: 0 24rpx;
    
    .dream-item {
        margin-bottom: 16rpx;
        
        .category {
            background: rgba(108, 92, 231, 0.1);
            color: #6C5CE7;
            border-radius: 12rpx;
            padding: 4rpx 12rpx;
            font-size: 22rpx;
            display: inline-block;
            margin-bottom: 8rpx;
        }
        
        .description {
            font-size: 28rpx;
            color: #2D3436;
            display: block;
            margin-bottom: 8rpx;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        
        .date {
            font-size: 22rpx;
            color: #B2BEC3;
            display: block;
        }
    }
}

.stats-detail {
    margin: 0 24rpx;
    
    .stat-row {
        display: flex;
        justify-content: space-between;
        padding: 16rpx 0;
        border-bottom: 2rpx solid #F0F0F0;
        
        &:last-child { border-bottom: none; }
        
        .label { font-size: 28rpx; color: #636E72; }
        .value { font-size: 28rpx; color: #2D3436; }
    }
}

.empty-state {
    text-align: center;
    padding: 120rpx 0;
    color: #B2BEC3;
}
</style>
