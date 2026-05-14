<template>
    <view class="profile-page">
        <view class="header-bar">
            <text class="title">{{ $t('detail.dreamsTab') }}</text>
            <text class="lang-btn" @click="toggleLang">{{ currentLang === 'zh' ? 'EN' : '中文' }}</text>
        </view>
        <view class="profile-header gradient-bg" v-if="user">
            <image class="avatar" :src="user.avatar || '/static/default-avatar.png'" mode="aspectFill" />
            <text class="nickname">{{ user.nickname }}</text>
            <text class="bio">{{ user.bio || $t('detail.bioLazy') }}</text>
        </view>
        
        <view class="stats-row card">
            <view class="stat-item">
                <text class="count">{{ user.dreamCount || 0 }}</text>
                <text class="label">{{ $t('detail.dreamsLabel') }}</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
                <text class="count">{{ user.followingCount || 0 }}</text>
                <text class="label">{{ $t('detail.followingLabel') }}</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
                <text class="count">{{ user.followersCount || 0 }}</text>
                <text class="label">{{ $t('detail.fansLabel') }}</text>
            </view>
        </view>
        
        <view class="action-row" v-if="isOtherUser">
            <button class="btn-primary" :class="{following: followStatus?.isFollowing === 1}" @click="toggleFollow">
                {{ followStatus?.isFollowing === 1 ? $t('detail.followingYes') : $t('detail.followingNo') }}
            </button>
        </view>
        
        <!-- Tab切换 -->
        <view class="tab-bar">
            <view class="tab-item" :class="{active: currentTab === 'dreams'}" @click="currentTab = 'dreams'">{{ $t('detail.dreamsTab') }}</view>
            <view class="tab-item" :class="{active: currentTab === 'stats'}" @click="loadStats">{{ $t('detail.statsTab') }}</view>
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
                <text class="label">{{ $t('detail.topDream') }}</text>
                <text class="value">{{ stats.topCategories?.[0]?.categoryName || $t('detail.none') }}</text>
            </view>
            <view class="stat-row">
                <text class="label">{{ $t('detail.avgClarity') }}</text>
                <text class="value">{{ (stats.avgClarity || 0).toFixed(1) }}⭐</text>
            </view>
            <view class="stat-row">
                <text class="label">{{ $t('detail.monthlyNew') }}</text>
                <text class="value">{{ stats.monthlyDreamCount || 0 }}{{ $t('detail.dreamsLabel') }}</text>
            </view>
        </view>
        
        <view class="empty-state" v-if="!user">
            <text>{{ $t('detail.userNotFound') }}</text>
        </view>
    </view>
</template>

<script>
import { userApi, followApi, statsApi } from '@/utils/api';
import { getUserId, isLoggedIn } from '@/utils/auth';
import { setLocale } from '@/locale';

export default {
    data() {
        return {
            userId: 0,
            user: null,
            dreams: [],
            stats: null,
            currentTab: 'dreams',
            followStatus: null,
            isOtherUser: true,
            currentLang: 'zh'
        };
    },
    onLoad(options) {
        this.currentLang = uni.getStorageSync('locale') || 'zh';
        this.userId = parseInt(options.id) || 0;
        this.isOtherUser = this.userId !== getUserId();
        this.loadProfile();
        this.loadDreams();
        if (this.isOtherUser && isLoggedIn()) {
            this.loadFollowStatus();
        }
    },
    methods: {
        toggleLang() {
            this.currentLang = this.currentLang === 'zh' ? 'en' : 'zh';
            setLocale(this.currentLang);
        },
        
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
                if (this.followStatus?.isFollowing === 1) {
                    await followApi.unfollow(this.userId);
                    this.followStatus.isFollowing = 0;
                    this.user.followersCount = Math.max(0, (this.user.followersCount || 1) - 1);
                } else {
                    await followApi.follow(this.userId);
                    if (!this.followStatus) this.followStatus = {};
                    this.followStatus.isFollowing = 1;
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
    
    .lang-btn {
        font-size: 24rpx;
        color: #6C5CE7;
        background: rgba(108, 92, 231, 0.1);
        padding: 8rpx 16rpx;
        border-radius: 24rpx;
    }
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
