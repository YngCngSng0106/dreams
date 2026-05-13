<template>
    <view class="explore-page">
        <!-- 自定义导航栏 -->
        <view class="navbar" :style="{height: statusBarHeight + 88 + 'rpx'}">
            <view class="navbar-content" :style="{paddingTop: statusBarHeight + 'rpx'}">
                <view class="navbar-left">
                    <text class="app-logo">💭</text>
                    <text class="app-title">梦境分享</text>
                </view>
                <view class="navbar-right" @click="goSearch">
                    <text class="search-icon">🔍</text>
                    <text class="search-text">搜索梦境</text>
                </view>
            </view>
        </view>
        
        <!-- 分类标签 -->
        <view class="category-bar" :style="{marginTop: (statusBarHeight + 88) + 'rpx'}">
            <scroll-view scroll-x class="category-scroll">
                <view class="category-item" :class="{active: selectedCategory === 0}" @click="selectCategory(0)">
                    全部
                </view>
                <view class="category-item" :class="{active: selectedCategory === cat.id}" v-for="cat in categories" :key="cat.id" @click="selectCategory(cat.id)">
                    {{ cat.name }}
                </view>
            </scroll-view>
        </view>
        
        <!-- 梦境列表 -->
        <scroll-view scroll-y class="dream-list" refresher-enabled :refresher-triggered="isRefreshing" @refresherrefresh="onRefresh" @scrolltolower="loadMore">
            <view class="dream-card card" v-for="(dream, index) in dreams" :key="dream.id" @click="goDetail(dream.id)">
                <view class="dream-header">
                    <view class="user-info" @click.stop="goProfile(dream.userId)">
                        <image class="avatar" :src="dream.avatar || '/static/default-avatar.png'" mode="aspectFill" />
                        <view class="user-text">
                            <text class="nickname">{{ dream.nickname || '匿名' }}</text>
                            <text class="time">{{ formatTime(dream.createTime) }}</text>
                        </view>
                    </view>
                    <view class="category-tag">{{ dream.category }}</view>
                </view>
                
                <view class="dream-content">
                    <text class="description">{{ dream.description }}</text>
                </view>
                
                <view class="dream-tags" v-if="dream.tags">
                    <text class="tag" v-for="tag in parseTags(dream.tags)" :key="tag">#{{ tag }}</text>
                </view>
                
                <view class="dream-footer">
                    <view class="action-btn" @click.stop="toggleLike(dream, index)">
                        <text class="action-icon">{{ dream.liked ? '❤️' : '🤍' }}</text>
                        <text class="action-count">{{ dream.likeCount }}</text>
                    </view>
                    <view class="action-btn">
                        <text class="action-icon">💬</text>
                        <text class="action-count">{{ dream.commentCount || 0 }}</text>
                    </view>
                    <view class="action-btn">
                        <text class="action-icon">🔄</text>
                        <text class="action-count">分享</text>
                    </view>
                </view>
            </view>
            
            <view class="empty-state" v-if="dreams.length === 0 && !loading">
                <text class="empty-icon">💤</text>
                <text class="empty-text">暂无梦境记录</text>
                <text class="empty-hint">快来记录你的第一个梦吧</text>
            </view>
            
            <view class="loading-more" v-if="loading">
                <text>加载中...</text>
            </view>
        </scroll-view>
    </view>
</template>

<script>
import { dreamApi, categoryApi, isLoggedIn } from '@/utils/auth';
import { requireLogin } from '@/utils/auth';
import dayjs from 'dayjs';

export default {
    data() {
        return {
            statusBarHeight: 0,
            dreams: [],
            categories: [],
            selectedCategory: 0,
            page: 1,
            pageSize: 10,
            loading: false,
            isRefreshing: false,
            hasMore: true
        };
    },
    onLoad() {
        this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
        this.loadCategories();
        this.loadDreams();
    },
    onPullDownRefresh() {
        this.onRefresh();
    },
    methods: {
        async loadCategories() {
            try {
                this.categories = await categoryApi.list();
            } catch (e) {
                console.error('Load categories failed:', e);
            }
        },
        
        selectCategory(catId) {
            this.selectedCategory = catId;
            this.page = 1;
            this.dreams = [];
            this.hasMore = true;
            this.loadDreams();
        },
        
        async loadDreams() {
            if (this.loading || !this.hasMore) return;
            this.loading = true;
            try {
                const res = await dreamApi.feed(this.page, this.pageSize);
                this.dreams = this.dreams.concat(res.records || []);
                this.page++;
                if (!res.records || res.records.length < this.pageSize) {
                    this.hasMore = false;
                }
            } catch (e) {
                console.error('Load dreams failed:', e);
            } finally {
                this.loading = false;
                this.isRefreshing = false;
                uni.stopPullDownRefresh();
            }
        },
        
        async onRefresh() {
            this.isRefreshing = true;
            this.page = 1;
            this.dreams = [];
            this.hasMore = true;
            await this.loadDreams();
        },
        
        loadMore() {
            if (this.hasMore && !this.loading) {
                this.loadDreams();
            }
        },
        
        async toggleLike(dream, index) {
            if (!requireLogin()) return;
            try {
                if (dream.liked) {
                    await dreamApi.unlike(dream.id);
                    this.dreams[index].liked = false;
                    this.dreams[index].likeCount = Math.max(0, (this.dreams[index].likeCount || 1) - 1);
                } else {
                    await dreamApi.like(dream.id);
                    this.dreams[index].liked = true;
                    this.dreams[index].likeCount = (this.dreams[index].likeCount || 0) + 1;
                }
            } catch (e) {
                console.error('Like failed:', e);
            }
        },
        
        goDetail(dreamId) {
            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + dreamId });
        },
        
        goProfile(userId) {
            uni.navigateTo({ url: '/pages/detail/profile?id=' + userId });
        },
        
        goSearch() {
            uni.navigateTo({ url: '/pages/detail/search' });
        },
        
        formatTime(time) {
            if (!time) return '';
            const now = dayjs();
            const target = dayjs(time);
            if (now.diff(target, 'hour') < 1) return target.fromNow();
            if (now.diff(target, 'day') < 1) return '今天 ' + target.format('HH:mm');
            if (now.diff(target, 'day') < 7) return target.format('MM-DD HH:mm');
            return target.format('YYYY-MM-DD');
        },
        
        parseTags(tags) {
            if (!tags) return [];
            return tags.split(/[,,\s]+/).filter(t => t.trim());
        }
    }
};
</script>

<style lang="scss" scoped>
.explore-page {
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
        justify-content: space-between;
        padding: 0 24rpx;
        
        .navbar-left {
            display: flex;
            align-items: center;
            
            .app-logo {
                font-size: 44rpx;
                margin-right: 12rpx;
            }
            
            .app-title {
                font-size: 36rpx;
                font-weight: 700;
                color: #FFFFFF;
            }
        }
        
        .navbar-right {
            display: flex;
            align-items: center;
            
            .search-icon {
                font-size: 32rpx;
                margin-right: 8rpx;
            }
            
            .search-text {
                font-size: 26rpx;
                color: rgba(255,255,255,0.8);
            }
        }
    }
}

.category-bar {
    background: #FFFFFF;
    border-bottom: 2rpx solid #F0F0F0;
    padding: 16rpx 0;
    
    .category-scroll {
        white-space: nowrap;
        
        .category-item {
            display: inline-block;
            padding: 10rpx 24rpx;
            margin: 0 8rpx;
            border-radius: 32rpx;
            background: #F0F0F5;
            color: #636E72;
            font-size: 26rpx;
            
            &.active {
                background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
                color: #FFFFFF;
            }
        }
    }
}

.dream-list {
    height: calc(100vh - 200rpx);
    padding: 16rpx 24rpx;
}

.dream-card {
    margin-bottom: 24rpx;
    
    .dream-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16rpx;
        
        .user-info {
            display: flex;
            align-items: center;
            
            .avatar {
                width: 64rpx;
                height: 64rpx;
                border-radius: 50%;
                margin-right: 16rpx;
            }
            
            .user-text {
                display: flex;
                flex-direction: column;
                
                .nickname {
                    font-size: 28rpx;
                    font-weight: 600;
                    color: #2D3436;
                }
                
                .time {
                    font-size: 22rpx;
                    color: #B2BEC3;
                    margin-top: 4rpx;
                }
            }
        }
        
        .category-tag {
            background: rgba(108, 92, 231, 0.1);
            color: #6C5CE7;
            border-radius: 24rpx;
            padding: 6rpx 16rpx;
            font-size: 22rpx;
        }
    }
    
    .dream-content {
        margin-bottom: 16rpx;
        
        .description {
            font-size: 28rpx;
            color: #2D3436;
            line-height: 1.6;
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
    }
    
    .dream-tags {
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 16rpx;
    }
    
    .dream-footer {
        display: flex;
        justify-content: space-between;
        border-top: 2rpx solid #F0F0F0;
        padding-top: 16rpx;
        
        .action-btn {
            display: flex;
            align-items: center;
            
            .action-icon {
                font-size: 28rpx;
                margin-right: 8rpx;
            }
            
            .action-count {
                font-size: 24rpx;
                color: #B2BEC3;
            }
        }
    }
}

.empty-state {
    padding: 100rpx 0;
    text-align: center;
    
    .empty-icon {
        font-size: 100rpx;
        display: block;
        margin-bottom: 24rpx;
    }
    
    .empty-text {
        font-size: 32rpx;
        color: #636E72;
        display: block;
        margin-bottom: 12rpx;
    }
    
    .empty-hint {
        font-size: 26rpx;
        color: #B2BEC3;
        display: block;
    }
}

.loading-more {
    text-align: center;
    padding: 24rpx;
    color: #B2BEC3;
    font-size: 24rpx;
}
</style>
