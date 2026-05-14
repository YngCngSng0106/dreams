<template>
    <view class="detail-page">
        <view class="content" v-if="dream">
            <!-- 头部 -->
            <view class="dream-header card">
                <view class="user-info">
                    <image class="avatar" :src="dream.avatar || '/static/default-avatar.png'" mode="aspectFill" @click="goProfile(dream.userId)" />
                    <view class="info">
                        <text class="nickname">{{ dream.nickname || '匿名' }}</text>
                        <text class="date">{{ dream.dreamDate }}</text>
                    </view>
                    <view class="actions">
                        <text class="action-btn" @click="goEdit" v-if="isOwner">{{ $t('detail.edit') }}</text>
                    </view>
                </view>
                <view class="category-tag">{{ dream.category }}</view>
            </view>
            
            <!-- 图片 -->
            <view class="images-section" v-if="dreamImages.length > 0">
                <image class="detail-image" v-for="(img, idx) in dreamImages" :key="idx" :src="img" mode="widthFix" @click="previewImage(idx)" />
            </view>
            
            <!-- 描述 -->
            <view class="description-card card">
                <text class="description">{{ dream.description }}</text>
            </view>
            
            <!-- 信息 -->
            <view class="info-card card">
                <view class="info-row" v-if="dream.location">
                    <text class="info-label">{{ $t('detail.location') }}</text>
                    <text class="info-value">{{ dream.location }}</text>
                </view>
                <view class="info-row" v-if="dream.keywords">
                    <text class="info-label">{{ $t('detail.keywords') }}</text>
                    <text class="info-value">{{ dream.keywords }}</text>
                </view>
                <view class="info-row" v-if="dream.clarity">
                    <text class="info-label">{{ $t('detail.clarity') }}</text>
                    <text class="info-value">{{ '⭐'.repeat(dream.clarity) }}</text>
                </view>
                <view class="info-row" v-if="dream.isRecurring">
                    <text class="info-label">{{ $t('detail.recurring') }}</text>
                    <text class="info-value">{{ $t('detail.recurringYes') }}</text>
                </view>
            </view>
            
            <!-- 标签 -->
            <view class="tags-section" v-if="dreamTags.length > 0">
                <text class="tag" v-for="tag in dreamTags" :key="tag">#{{ tag }}</text>
            </view>
            
            <!-- 操作按钮 -->
            <view class="action-bar card">
                <view class="action-item" @click="toggleLike">
                    <text class="action-icon">{{ liked ? '❤️' : '🤍' }}</text>
                    <text class="action-text">{{ dream.likeCount }}</text>
                </view>
                <view class="action-item" @click="goSimilar">
                    <text class="action-icon">🔗</text>
                    <text class="action-text">{{ $t('detail.similar') }}</text>
                </view>
                <view class="action-item">
                    <text class="action-icon">📤</text>
                    <text class="action-text">{{ $t('detail.share') }}</text>
                </view>
            </view>
            
            <!-- 相似梦境 -->
            <view class="similar-section" v-if="similarDreams.length > 0">
                <view class="section-title">{{ $t('detail.similar') }}{{ $t('detail.dreams') }}</view>
                <view class="similar-card card" v-for="s in similarDreams" :key="s.dreamId" @click="goDetail(s.dreamId)">
                    <text class="similar-desc">{{ s.matchedFields }}</text>
                    <text class="similar-score">相似度 {{ (s.similarityScore * 100).toFixed(0) }}%</text>
                </view>
            </view>
        </view>
        
        <view class="empty-state" v-else>
            <text>{{ $t('detail.notFound') }}</text>
        </view>
    </view>
</template>

<script>
import { dreamApi, userApi } from '@/utils/api';
import { isLoggedIn, requireLogin, getUserId } from '@/utils/auth';
import dayjs from 'dayjs';

export default {
    data() {
        return {
            dream: null,
            dreamId: 0,
            liked: false,
            similarDreams: [],
            isOwner: false
        };
    },
    computed: {
        dreamImages() {
            if (!this.dream || !this.dream.images) return [];
            try { return JSON.parse(this.dream.images); } catch { return []; }
        },
        dreamTags() {
            if (!this.dream || !this.dream.tags) return [];
            return this.dream.tags.split(/[,,\\s]+/).filter(t => t.trim());
        }
    },
    onLoad(options) {
        this.dreamId = parseInt(options.id) || 0;
        this.loadDetail();
    },
    methods: {
        async loadDetail() {
            try {
                this.dream = await dreamApi.detail(this.dreamId);
                const myId = getUserId();
                this.isOwner = myId && myId === this.dream.userId;
                // 检查是否已点赞
                if (this.dream.likeCount > 0) this.liked = false; // 后端有更好接口时再改
                await this.loadSimilar();
            } catch (e) {
                console.error('Load detail failed:', e);
            }
        },
        
        async loadSimilar() {
            if (!isLoggedIn()) return;
            try {
                this.similarDreams = await dreamApi.similar(this.dreamId, 5);
            } catch (e) {
                // ignore
            }
        },
        
        async toggleLike() {
            if (!requireLogin()) return;
            try {
                if (this.liked) {
                    await dreamApi.unlike(this.dreamId);
                    this.dream.likeCount = Math.max(0, (this.dream.likeCount || 1) - 1);
                } else {
                    await dreamApi.like(this.dreamId);
                    this.dream.likeCount = (this.dream.likeCount || 0) + 1;
                }
                this.liked = !this.liked;
            } catch (e) {
                console.error('Like failed:', e);
            }
        },
        
        goProfile(userId) {
            uni.navigateTo({ url: '/pages/detail/profile?id=' + userId });
        },
        
        goEdit() {
            uni.navigateTo({ url: '/pages/detail/dream-edit?id=' + this.dreamId });
        },
        
        goSimilar() {
            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + this.dreamId + '&similar=1' });
        },
        
        goDetail(id) {
            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + id });
        },
        
        previewImage(idx) {
            uni.previewImage({
                current: this.dreamImages[idx],
                urls: this.dreamImages
            });
        }
    }
};
</script>

<style lang="scss" scoped>
.detail-page {
    min-height: 100vh;
    background: #F8F9FE;
    padding: 24rpx;
    padding-top: calc(24rpx + env(safe-area-inset-top));
}

.dream-header {
    .user-info {
        display: flex;
        align-items: center;
        margin-bottom: 16rpx;
        
        .avatar {
            width: 72rpx;
            height: 72rpx;
            border-radius: 50%;
            margin-right: 16rpx;
        }
        
        .info {
            flex: 1;
            
            .nickname {
                font-size: 30rpx;
                font-weight: 600;
                color: #2D3436;
                display: block;
            }
            
            .date {
                font-size: 24rpx;
                color: #B2BEC3;
                display: block;
                margin-top: 4rpx;
            }
        }
        
        .actions {
            .action-btn {
                font-size: 24rpx;
                color: #6C5CE7;
                background: rgba(108, 92, 231, 0.1);
                padding: 8rpx 16rpx;
                border-radius: 16rpx;
            }
        }
    }
    
    .category-tag {
        background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
        color: #FFFFFF;
        border-radius: 16rpx;
        padding: 8rpx 20rpx;
        font-size: 24rpx;
        display: inline-block;
    }
}

.images-section {
    margin-bottom: 24rpx;
    
    .detail-image {
        width: 100%;
        border-radius: 24rpx;
        margin-bottom: 12rpx;
    }
}

.description-card {
    margin-bottom: 24rpx;
    
    .description {
        font-size: 30rpx;
        color: #2D3436;
        line-height: 1.8;
    }
}

.info-card {
    margin-bottom: 24rpx;
    
    .info-row {
        display: flex;
        padding: 12rpx 0;
        border-bottom: 2rpx solid #F0F0F0;
        
        &:last-child { border-bottom: none; }
        
        .info-label {
            width: 120rpx;
            font-size: 26rpx;
            color: #636E72;
        }
        
        .info-value {
            flex: 1;
            font-size: 26rpx;
            color: #2D3436;
        }
    }
}

.tags-section {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 24rpx;
    padding: 0 8rpx;
}

.action-bar {
    display: flex;
    justify-content: space-around;
    margin-bottom: 24rpx;
    
    .action-item {
        text-align: center;
        
        .action-icon {
            font-size: 40rpx;
            display: block;
        }
        
        .action-text {
            font-size: 22rpx;
            color: #636E72;
            display: block;
            margin-top: 4rpx;
        }
    }
}

.similar-section {
    .section-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #2D3436;
        margin-bottom: 16rpx;
        padding: 0 8rpx;
    }
    
    .similar-card {
        margin-bottom: 16rpx;
        
        .similar-desc {
            font-size: 26rpx;
            color: #636E72;
            display: block;
        }
        
        .similar-score {
            font-size: 24rpx;
            color: #6C5CE7;
            display: block;
            margin-top: 8rpx;
        }
    }
}

.empty-state {
    text-align: center;
    padding: 120rpx 0;
    color: #B2BEC3;
    font-size: 28rpx;
}
</style>
