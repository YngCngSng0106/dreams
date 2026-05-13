<template>
    <view class="discussion-page">
        <!-- 自定义导航栏 -->
        <view class="navbar" :style="{height: statusBarHeight + 88 + 'rpx'}">
            <view class="navbar-content" :style="{paddingTop: statusBarHeight + 'rpx'}">
                <text class="title">{{ $t('discussion.title') }}</text>
                <view class="create-btn" @click="createDiscussion">+ {{ $t('discussion.create') }}</view>
            </view>
        </view>
        
        <!-- Tab切换 -->
        <view class="tab-bar" :style="{marginTop: (statusBarHeight + 88) + 'rpx'}">
            <view class="tab-item" :class="{active: currentTab === 'all'}" @click="currentTab = 'all'">{{ $t('discussion.all') }}</view>
            <view class="tab-item" :class="{active: currentTab === 'mine'}" @click="currentTab = 'mine'">{{ $t('discussion.mine') }}</view>
            <view class="tab-item" :class="{active: currentTab === 'recommended'}" @click="currentTab = 'recommended'">{{ $t('discussion.recommended') }}</view>
        </view>
        
        <!-- 讨论组列表 -->
        <scroll-view scroll-y class="list" @scrolltolower="loadMore">
            <view class="disc-card card" v-for="disc in discussions" :key="disc.id" @click="goDetail(disc.id)">
                <image class="cover" :src="disc.coverImage || '/static/default-cover.png'" mode="aspectFill" v-if="disc.coverImage" />
                <view class="info">
                    <text class="title">{{ disc.title }}</text>
                    <text class="desc">{{ disc.description || $t('discussion.noDesc') }}</text>
                    <view class="meta">
                        <text class="creator">{{ disc.creatorNickname }}</text>
                        <text class="members">👥 {{ disc.memberCount }}人</text>
                    </view>
                </view>
            </view>
            
            <view class="empty-state" v-if="discussions.length === 0 && !loading">
                <text class="empty-icon">💬</text>
                <text class="empty-text">{{ $t('discussion.empty') }}</text>
            </view>
        </scroll-view>
        
        <!-- 创建按钮 -->
        <view class="fab-btn" @click="createDiscussion">+</view>
    </view>

    <custom-tab-bar ref="tabbar" />
</template>

<script>
import { discussionApi } from '@/utils/api';
import { requireLogin } from '@/utils/auth';

export default {
    data() {
        return {
            statusBarHeight: 0,
            discussions: [],
            currentTab: 'all',
            page: 1,
            pageSize: 10,
            loading: false,
            hasMore: true
        };
    },
    onLoad() {
        this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
        this.loadDiscussions();
    },
    onShow() {
        if (this.discussions.length > 0) {
            this.page = 1;
            this.discussions = [];
            this.loadDiscussions();
        }
        this.$nextTick(() => {
            const tabbar = this.$refs.tabbar;
            if (tabbar) tabbar.updateCurrentPage();
        });
    },
    methods: {
        async loadDiscussions() {
            if (this.loading) return;
            this.loading = true;
            try {
                let res;
                if (this.currentTab === 'mine') {
                    if (!requireLogin()) { this.loading = false; return; }
                    res = await discussionApi.myList(this.page, this.pageSize);
                } else if (this.currentTab === 'recommended') {
                    if (!requireLogin()) { this.loading = false; return; }
                    res = await discussionApi.recommended(this.page, this.pageSize);
                } else {
                    res = await discussionApi.list(this.page, this.pageSize);
                }
                this.discussions = this.discussions.concat(res.records || []);
                this.page++;
                if (!res.records || res.records.length < this.pageSize) {
                    this.hasMore = false;
                }
            } catch (e) {
                console.error('Load discussions failed:', e);
            } finally {
                this.loading = false;
            }
        },
        
        loadMore() {
            if (this.hasMore && !this.loading) {
                this.loadDiscussions();
            }
        },
        
        goDetail(id) {
            uni.navigateTo({ url: '/pages/detail/discussion-detail?id=' + id });
        },
        
        createDiscussion() {
            if (!requireLogin()) return;
            uni.navigateTo({ url: '/pages/detail/discussion-detail?create=1' });
        }
    }
};
</script>

<style lang="scss" scoped>
.discussion-page {
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
        
        .title {
            font-size: 36rpx;
            font-weight: 700;
            color: #FFFFFF;
        }
        
        .create-btn {
            background: rgba(255,255,255,0.2);
            color: #FFFFFF;
            border-radius: 24rpx;
            padding: 8rpx 20rpx;
            font-size: 24rpx;
        }
    }
}

.tab-bar {
    display: flex;
    background: #FFFFFF;
    padding: 16rpx 24rpx;
    border-bottom: 2rpx solid #F0F0F0;
    
    .tab-item {
        flex: 1;
        text-align: center;
        padding: 12rpx 0;
        font-size: 28rpx;
        color: #636E72;
        border-radius: 16rpx;
        
        &.active {
            color: #6C5CE7;
            font-weight: 600;
            background: rgba(108, 92, 231, 0.08);
        }
    }
}

.list {
    height: calc(100vh - 300rpx);
    padding: 24rpx;
}

.disc-card {
    display: flex;
    margin-bottom: 24rpx;
    overflow: hidden;
    
    .cover {
        width: 180rpx;
        height: 180rpx;
        flex-shrink: 0;
    }
    
    .info {
        flex: 1;
        padding: 20rpx;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .title {
            font-size: 30rpx;
            font-weight: 600;
            color: #2D3436;
            display: block;
            margin-bottom: 8rpx;
        }
        
        .desc {
            font-size: 24rpx;
            color: #636E72;
            display: block;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
        }
        
        .meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 8rpx;
            
            .creator {
                font-size: 22rpx;
                color: #B2BEC3;
            }
            
            .members {
                font-size: 22rpx;
                color: #6C5CE7;
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
        font-size: 28rpx;
        color: #B2BEC3;
    }
}

.fab-btn {
    position: fixed;
    right: 32rpx;
    bottom: 160rpx;
    width: 96rpx;
    height: 96rpx;
    background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
    color: #FFFFFF;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
    box-shadow: 0 8rpx 24rpx rgba(108, 92, 231, 0.3);
    z-index: 50;
}
</style>
