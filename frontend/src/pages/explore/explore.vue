<template>
    <view class="explore-page">
        <!-- 自定义导航栏 -->
        <view class="navbar" :style="{height: statusBarHeight + 88 + 'rpx'}">
            <view class="navbar-content" :style="{paddingTop: statusBarHeight + 'rpx'}">
                <view class="navbar-left">
                    <text class="app-logo">💭</text>
                    <text class="app-title">{{ $t('explore.title') }}</text>
                </view>
                <view class="navbar-right">
                    <text class="lang-btn" @click="toggleLang">{{ currentLang === 'zh' ? 'EN' : '中' }}</text>
                    <text class="search-icon" @click="goSearch">🔍</text>
                </view>
            </view>
        </view>

        <!-- 左右分栏 -->
        <view class="main-container" :style="{marginTop: (statusBarHeight + 88) + 'rpx'}">
            <!-- 左侧分类栏 -->
            <scroll-view class="sidebar" scroll-y>
                <view class="sidebar-item" :class="{active: selectedCategory === 0}" @click="selectCategory(0)">
                    <text class="sidebar-icon">🌙</text>
                    <text class="sidebar-text">{{ $t('category.all') }}</text>
                </view>
                <view class="sidebar-item" :class="{active: selectedCategory === cat.id}" v-for="cat in categories" :key="cat.id" @click="selectCategory(cat.id)">
                    <text class="sidebar-icon">{{ cat.icon }}</text>
                    <text class="sidebar-text">{{ cat.name }}</text>
                </view>
            </scroll-view>

            <!-- 右侧梦境列表 -->
            <scroll-view class="content-area" scroll-y refresher-enabled :refresher-triggered="isRefreshing" @refresherrefresh="onRefresh" @scrolltolower="loadMore">
                <view class="content-header">
                    <text class="content-title">{{ currentCategoryName }}</text>
                    <text class="content-count">{{ $t('explore.count', { count: dreams.length }) }}</text>
                </view>

                <view class="dream-card" v-for="(dream, index) in dreams" :key="dream.id" @click="goDetail(dream.id)">
                    <view class="dream-header">
                        <view class="user-info" @click.stop="goProfile(dream.userId)">
                            <image class="avatar" :src="dream.avatar || '/static/default-avatar.png'" mode="aspectFill" />
                            <view class="user-text">
                                <text class="nickname">{{ dream.nickname || $t('explore.anonymous') }}</text>
                                <text class="time">{{ formatTime(dream.createTime) }}</text>
                            </view>
                        </view>
                        <view class="category-tag" v-if="dream.categoryCode">{{ getCatIcon(dream.categoryCode) }} {{ $t('category.' + dream.categoryCode) }}</view>
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
                            <text class="action-count">{{ $t('explore.share') }}</text>
                        </view>
                    </view>
                </view>

                <view class="empty-state" v-if="dreams.length === 0 && !loading">
                    <text class="empty-icon">💤</text>
                    <text class="empty-text">{{ $t('explore.empty') }}</text>
                    <text class="empty-hint">{{ $t('explore.emptyHint') }}</text>
                </view>

                <view class="loading-more" v-if="loading">
                    <text>{{ $t('explore.loading') }}</text>
                </view>
            </scroll-view>
        </view>
        
        <custom-tab-bar ref="tabbar" />
    </view>
</template>

<script>
import { dreamApi, categoryApi } from '@/utils/api';
import { requireLogin } from '@/utils/auth';
import { setLocale } from '@/locale/index';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime.js';
import 'dayjs/locale/zh';
dayjs.extend(relativeTime);
dayjs.locale('zh');

export default {
    computed: {
        currentLang() {
            return this.$i18n.locale;
        },
        currentCategoryName() {
            if (this.selectedCategory === 0) return this.$t('category.all');
            const cat = this.categories.find(c => c.id === this.selectedCategory);
            return cat ? cat.name : this.$t('category.all');
        }
    },
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
            hasMore: true,
            categoryIcons: {
                flying: '🦅',
                falling: '🪨',
                exam: '📝',
                chase: '🏃',
                water: '🌊',
                family: '👨‍👩‍👧',
                work: '💼',
                ghost: '👻',
                love: '💕',
                other: '🔮'
            }
        };
    },
    onLoad() {
        this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
        this.loadCategories();
        this.loadDreams();
        this.updateTabBar();
    },
    onShow() {
        this.updateTabBar();
    },
    watch: {
        '$i18n.locale'() {
            this.loadCategories();
        }
    },
    onPullDownRefresh() {
        this.onRefresh();
    },
    methods: {
        toggleLang() {
            const next = this.currentLang === 'zh' ? 'en' : 'zh';
            setLocale(next);
        },
        updateTabBar() {
            this.$nextTick(() => {
                const tabbar = this.$refs.tabbar;
                if (tabbar) {
                    tabbar.updateCurrentPage();
                }
            });
        },
        getCatIcon(code) {
            return this.categoryIcons[code] || '🔮';
        },
        async loadCategories() {
            try {
                const cats = await categoryApi.list();
                this.categories = cats.map(c => {
                    const code = c.code;
                    const name = this.$t('category.' + code) || c.name;
                    const icon = c.icon || this.categoryIcons[code] || '🔮';
                    return { id: c.id, name, icon, code };
                });
            } catch (e) {
                const codes = ['flying','falling','exam','chase','water','family','work','ghost','love','other'];
                this.categories = codes.map((code, i) => ({
                    id: i + 1,
                    name: this.$t('category.' + code),
                    icon: this.categoryIcons[code] || '🔮',
                    code
                }));
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
            if (now.diff(target, 'day') < 1) return this.$t('time.today') + ' ' + target.format('HH:mm');
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

<style lang="scss">
.explore-page {
    min-height: 100vh;
    background: #F8F9FE;
    display: flex;
    flex-direction: column;
}

.navbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
}

.navbar-content {
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24rpx;
}

.navbar-left {
    display: flex;
    align-items: center;
}

.app-logo {
    font-size: 44rpx;
    margin-right: 12rpx;
}

.app-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #FFFFFF;
}

.navbar-right {
    display: flex;
    align-items: center;
}

.lang-btn {
    font-size: 22rpx;
    color: #FFFFFF;
    opacity: 0.8;
    padding: 6rpx 14rpx;
    border: 2rpx solid rgba(255,255,255,0.4);
    border-radius: 20rpx;
    margin-right: 12rpx;
}

.search-icon {
    font-size: 36rpx;
}

// 左右分栏容器
.main-container {
    flex: 1;
    display: flex;
    overflow: hidden;
}

// 左侧分类栏
.sidebar {
    width: 180rpx;
    background: #FFFFFF;
    border-right: 2rpx solid #F0F0F0;
    flex-shrink: 0;
}

.sidebar-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 24rpx 8rpx;
    border-bottom: 2rpx solid #F8F8F8;
    transition: all 0.2s;
}

.sidebar-item.active {
    background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
}

.sidebar-icon {
    font-size: 40rpx;
    margin-bottom: 8rpx;
}

.sidebar-text {
    font-size: 22rpx;
    color: #636E72;
    text-align: center;
    line-height: 1.3;
}

.sidebar-item.active .sidebar-text {
    color: #FFFFFF;
}

// 右侧内容区
.content-area {
    flex: 1;
    background: #F8F9FE;
    min-width: 0;
}

.content-header {
    padding: 24rpx;
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    border-bottom: 2rpx solid #F0F0F0;
    background: #FFFFFF;
}

.content-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #2D3436;
}

.content-count {
    font-size: 22rpx;
    color: #B2BEC3;
}

.dream-card {
    background: #FFFFFF;
    margin: 16rpx 24rpx;
    padding: 24rpx;
    border-radius: 16rpx;
    box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}

.dream-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
}

.user-info {
    display: flex;
    align-items: center;
}

.avatar {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    margin-right: 16rpx;
}

.user-text {
    display: flex;
    flex-direction: column;
}

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

.category-tag {
    background: rgba(108, 92, 231, 0.1);
    color: #6C5CE7;
    border-radius: 24rpx;
    padding: 6rpx 16rpx;
    font-size: 22rpx;
}

.dream-content {
    margin-bottom: 16rpx;
}

.description {
    font-size: 28rpx;
    color: #2D3436;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.dream-tags {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 16rpx;
}

.tag {
    background: rgba(108, 92, 231, 0.08);
    color: #6C5CE7;
    border-radius: 24rpx;
    padding: 6rpx 16rpx;
    font-size: 22rpx;
    margin-right: 12rpx;
    margin-bottom: 8rpx;
}

.dream-footer {
    display: flex;
    justify-content: space-around;
    border-top: 2rpx solid #F0F0F0;
    padding-top: 16rpx;
}

.action-btn {
    display: flex;
    align-items: center;
}

.action-icon {
    font-size: 28rpx;
    margin-right: 8rpx;
}

.action-count {
    font-size: 24rpx;
    color: #B2BEC3;
}

.empty-state {
    padding: 100rpx 0;
    text-align: center;
}

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

.loading-more {
    text-align: center;
    padding: 24rpx;
    color: #B2BEC3;
    font-size: 24rpx;
}
</style>
