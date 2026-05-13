<template>
    <view class="search-page">
        <view class="search-bar">
            <view class="search-input-wrapper">
                <text class="search-icon">🔍</text>
                <input v-model="keyword" placeholder="搜索梦境、用户、讨论组" @confirm="doSearch" class="search-input" />
                <text class="clear-btn" @click="clearSearch" v-if="keyword">×</text>
            </view>
        </view>
        
        <view class="result-section" v-if="hasSearched">
            <view class="tab-bar">
                <view class="tab-item" :class="{active: searchType === 'all'}" @click="searchType = 'all'; doSearch()">全部</view>
                <view class="tab-item" :class="{active: searchType === 'dream'}" @click="searchType = 'dream'; doSearch()">梦境</view>
                <view class="tab-item" :class="{active: searchType === 'user'}" @click="searchType = 'user'; doSearch()">用户</view>
                <view class="tab-item" :class="{active: searchType === 'discussion'}" @click="searchType = 'discussion'; doSearch()">讨论</view>
            </view>
            
            <!-- 梦境结果 -->
            <view class="result-group" v-if="(searchType === 'all' || searchType === 'dream') && results.dreams?.length">
                <text class="group-title">梦境 ({{ results.dreams.length }})</text>
                <view class="result-item card" v-for="item in results.dreams" :key="item.id" @click="goDreamDetail(item.id)">
                    <text class="item-title">{{ item.description }}</text>
                    <text class="item-meta">{{ item.nickname }} · {{ item.category }}</text>
                </view>
            </view>
            
            <!-- 用户结果 -->
            <view class="result-group" v-if="(searchType === 'all' || searchType === 'user') && results.users?.length">
                <text class="group-title">用户 ({{ results.users.length }})</text>
                <view class="result-item card" v-for="item in results.users" :key="item.id" @click="goProfile(item.id)">
                    <text class="item-title">{{ item.nickname }}</text>
                    <text class="item-meta">{{ item.bio || '暂无简介' }}</text>
                </view>
            </view>
            
            <!-- 讨论结果 -->
            <view class="result-group" v-if="(searchType === 'all' || searchType === 'discussion') && results.discussions?.length">
                <text class="group-title">讨论组 ({{ results.discussions.length }})</text>
                <view class="result-item card" v-for="item in results.discussions" :key="item.id" @click="goDiscussion(item.id)">
                    <text class="item-title">{{ item.title }}</text>
                    <text class="item-meta">{{ item.creatorNickname }} · {{ item.memberCount }}人</text>
                </view>
            </view>
            
            <view class="empty-state" v-if="!results.dreams?.length && !results.users?.length && !results.discussions?.length">
                <text class="empty-icon">🔍</text>
                <text>没有找到相关结果</text>
            </view>
        </view>
        
        <!-- 热门标签 -->
        <view class="hot-tags" v-if="!hasSearched">
            <text class="section-title">热门搜索</text>
            <view class="tags">
                <text class="tag" v-for="tag in hotTags" :key="tag.tag" @click="searchFor(tag.tag)">{{ tag.tag }}</text>
            </view>
        </view>
    </view>
</template>

<script>
import { searchApi } from '@/utils/api';

export default {
    data() {
        return {
            keyword: '',
            searchType: 'all',
            hasSearched: false,
            results: { dreams: [], users: [], discussions: [] },
            hotTags: []
        };
    },
    onLoad() {
        this.loadHotTags();
    },
    methods: {
        async loadHotTags() {
            try {
                this.hotTags = await searchApi.tags('');
            } catch (e) {
                this.hotTags = [{ tag: '飞行' }, { tag: '坠落' }, { tag: '考试' }, { tag: '被追逐' }, { tag: '水' }];
            }
        },
        
        async doSearch() {
            if (!this.keyword.trim()) return;
            this.hasSearched = true;
            try {
                this.results = await searchApi.search(this.keyword, this.searchType === 'all' ? null : this.searchType);
            } catch (e) {
                console.error('Search failed:', e);
            }
        },
        
        searchFor(tag) {
            this.keyword = tag;
            this.doSearch();
        },
        
        clearSearch() {
            this.keyword = '';
            this.hasSearched = false;
            this.results = { dreams: [], users: [], discussions: [] };
        },
        
        goDreamDetail(id) {
            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + id });
        },
        
        goProfile(id) {
            uni.navigateTo({ url: '/pages/detail/profile?id=' + id });
        },
        
        goDiscussion(id) {
            uni.navigateTo({ url: '/pages/detail/discussion-detail?id=' + id });
        }
    }
};
</script>

<style lang="scss" scoped>
.search-page {
    min-height: 100vh;
    background: #F8F9FE;
    padding: 24rpx;
    padding-top: calc(24rpx + env(safe-area-inset-top));
}

.search-bar {
    margin-bottom: 24rpx;
    
    .search-input-wrapper {
        display: flex;
        align-items: center;
        background: #FFFFFF;
        border-radius: 48rpx;
        padding: 0 24rpx;
        box-shadow: 0 2rpx 12rpx rgba(108, 92, 231, 0.08);
        
        .search-icon {
            font-size: 32rpx;
            margin-right: 12rpx;
        }
        
        .search-input {
            flex: 1;
            font-size: 28rpx;
            padding: 20rpx 0;
            border: none;
            background: transparent;
        }
        
        .clear-btn {
            font-size: 32rpx;
            color: #B2BEC3;
            padding: 0 12rpx;
        }
    }
}

.tab-bar {
    display: flex;
    background: #FFFFFF;
    padding: 12rpx 16rpx;
    border-radius: 16rpx;
    margin-bottom: 24rpx;
    
    .tab-item {
        flex: 1;
        text-align: center;
        padding: 10rpx 0;
        font-size: 24rpx;
        color: #636E72;
        border-radius: 12rpx;
        
        &.active {
            background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
            color: #FFFFFF;
            font-weight: 600;
        }
    }
}

.result-group {
    margin-bottom: 24rpx;
    
    .group-title {
        font-size: 26rpx;
        color: #636E72;
        margin-bottom: 12rpx;
        display: block;
    }
    
    .result-item {
        margin-bottom: 12rpx;
        
        .item-title {
            font-size: 28rpx;
            color: #2D3436;
            display: block;
            margin-bottom: 8rpx;
        }
        
        .item-meta {
            font-size: 22rpx;
            color: #B2BEC3;
        }
    }
}

.hot-tags {
    .section-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #2D3436;
        display: block;
        margin-bottom: 20rpx;
    }
    
    .tags {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;
    }
}

.empty-state {
    text-align: center;
    padding: 80rpx 0;
    color: #B2BEC3;
    
    .empty-icon {
        font-size: 80rpx;
        display: block;
        margin-bottom: 16rpx;
    }
}
</style>
