     1|<template>
     2|    <view class="detail-page">
     3|        <view class="content" v-if="dream">
     4|            <!-- 头部 -->
     5|            <view class="dream-header card">
     6|                <view class="user-info">
     7|                    <image class="avatar" :src="dream.avatar || '/static/default-avatar.png'" mode="aspectFill" @click="goProfile(dream.userId)" />
     8|                    <view class="info">
     9|                        <text class="nickname">{{ dream.nickname || '匿名' }}</text>
    10|                        <text class="date">{{ dream.dreamDate }}</text>
    11|                    </view>
    12|                    <view class="actions">
    13|                        <text class="action-btn" @click="goEdit" v-if="isOwner">{{ $t('detail.edit') }}</text>
    14|                    </view>
    15|                </view>
    16|                <view class="category-tag">{{ dream.category }}</view>
    17|            </view>
    18|            
    19|            <!-- 图片 -->
    20|            <view class="images-section" v-if="dreamImages.length > 0">
    21|                <image class="detail-image" v-for="(img, idx) in dreamImages" :key="idx" :src="img" mode="widthFix" @click="previewImage(idx)" />
    22|            </view>
    23|            
    24|            <!-- 描述 -->
    25|            <view class="description-card card">
    26|                <text class="description">{{ dream.description }}</text>
    27|            </view>
    28|            
    29|            <!-- 信息 -->
    30|            <view class="info-card card">
    31|                <view class="info-row" v-if="dream.location">
    32|                    <text class="info-label">{{ $t('detail.location') }}</text>
    33|                    <text class="info-value">{{ dream.location }}</text>
    34|                </view>
    35|                <view class="info-row" v-if="dream.keywords">
    36|                    <text class="info-label">{{ $t('detail.keywords') }}</text>
    37|                    <text class="info-value">{{ dream.keywords }}</text>
    38|                </view>
    39|                <view class="info-row" v-if="dream.clarity">
    40|                    <text class="info-label">{{ $t('detail.clarity') }}</text>
    41|                    <text class="info-value">{{ '⭐'.repeat(dream.clarity) }}</text>
    42|                </view>
    43|                <view class="info-row" v-if="dream.isRecurring">
    44|                    <text class="info-label">{{ $t('detail.recurring') }}</text>
    45|                    <text class="info-value">{{ $t('detail.recurringYes') }}</text>
    46|                </view>
    47|            </view>
    48|            
    49|            <!-- 标签 -->
    50|            <view class="tags-section" v-if="dreamTags.length > 0">
    51|                <text class="tag" v-for="tag in dreamTags" :key="tag">#{{ tag }}</text>
    52|            </view>
    53|            
    54|            <!-- 操作按钮 -->
    55|            <view class="action-bar card">
    56|                <view class="action-item" @click="toggleLike">
    57|                    <text class="action-icon">{{ liked ? '❤️' : '🤍' }}</text>
    58|                    <text class="action-text">{{ dream.likeCount }}</text>
    59|                </view>
    60|                <view class="action-item" @click="goSimilar">
    61|                    <text class="action-icon">🔗</text>
    62|                    <text class="action-text">{{ $t('detail.similar') }}</text>
    63|                </view>
    64|                <view class="action-item">
    65|                    <text class="action-icon">📤</text>
    66|                    <text class="action-text">{{ $t('detail.share') }}</text>
    67|                </view>
    68|            </view>
    69|            
    70|            <!-- 相似梦境 -->
    71|            <view class="similar-section" v-if="similarDreams.length > 0">
    72|                <view class="section-title">{{ $t('detail.similar') }}{{ $t('detail.dreams') }}</view>
    73|                <view class="similar-card card" v-for="s in similarDreams" :key="s.dreamId" @click="goDetail(s.dreamId)">
    74|                    <text class="similar-desc">{{ s.matchedFields }}</text>
    75|                    <text class="similar-score">相似度 {{ (s.similarityScore * 100).toFixed(0) }}%</text>
    76|                </view>
    77|            </view>
    78|        </view>
    79|        
    80|        <view class="empty-state" v-else>
    81|            <text>{{ $t('detail.notFound') }}</text>
    82|        </view>
    83|    </view>
    84|</template>
    85|
    86|<script>
    87|import { dreamApi, userApi } from '@/utils/api';
    88|import { isLoggedIn, requireLogin, getUserId } from '@/utils/auth';
    89|import dayjs from 'dayjs';
    90|
    91|export default {
    92|    data() {
    93|        return {
    94|            dream: null,
    95|            dreamId: 0,
    96|            liked: false,
    97|            similarDreams: [],
    98|            isOwner: false
    99|        };
   100|    },
   101|    computed: {
   102|        dreamImages() {
   103|            if (!this.dream || !this.dream.images) return [];
   104|            try { return JSON.parse(this.dream.images); } catch { return []; }
   105|        },
   106|        dreamTags() {
   107|            if (!this.dream || !this.dream.tags) return [];
   108|            return this.dream.tags.split(/[,,\\s]+/).filter(t => t.trim());
   109|        }
   110|    },
   111|    onLoad(options) {
   112|        this.dreamId = parseInt(options.id) || 0;
   113|        this.loadDetail();
   114|    },
   115|    methods: {
   116|        async loadDetail() {
   117|            try {
   118|                this.dream = await dreamApi.detail(this.dreamId);
   119|                const myId = getUserId();
   120|                this.isOwner = myId && myId === this.dream.userId;
   121|                // 检查是否已点赞
   122|                if (this.dream.likeCount > 0) this.liked = false; // 后端有更好接口时再改
   123|                await this.loadSimilar();
   124|            } catch (e) {
   125|                console.error('Load detail failed:', e);
   126|            }
   127|        },
   128|        
   129|        async loadSimilar() {
   130|            if (!isLoggedIn()) return;
   131|            try {
   132|                this.similarDreams = await dreamApi.similar(this.dreamId, 5);
   133|            } catch (e) {
   134|                // ignore
   135|            }
   136|        },
   137|        
   138|        async toggleLike() {
   139|            if (!requireLogin()) return;
   140|            try {
   141|                if (this.liked) {
   142|                    await dreamApi.unlike(this.dreamId);
   143|                    this.dream.likeCount = Math.max(0, (this.dream.likeCount || 1) - 1);
   144|                } else {
   145|                    await dreamApi.like(this.dreamId);
   146|                    this.dream.likeCount = (this.dream.likeCount || 0) + 1;
   147|                }
   148|                this.liked = !this.liked;
   149|            } catch (e) {
   150|                console.error('Like failed:', e);
   151|            }
   152|        },
   153|        
   154|        goProfile(userId) {
   155|            uni.navigateTo({ url: '/pages/detail/profile?id=' + userId });
   156|        },
   157|        
   158|        goEdit() {
   159|            uni.navigateTo({ url: '/pages/detail/dream-edit?id=' + this.dreamId });
   160|        },
   161|        
   162|        goSimilar() {
   163|            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + this.dreamId + '&similar=1' });
   164|        },
   165|        
   166|        goDetail(id) {
   167|            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + id });
   168|        },
   169|        
   170|        previewImage(idx) {
   171|            uni.previewImage({
   172|                current: this.dreamImages[idx],
   173|                urls: this.dreamImages
   174|            });
   175|        }
   176|    }
   177|};
   178|</script>
   179|
   180|<style lang="scss" scoped>
   181|.detail-page {
   182|    min-height: 100vh;
   183|    padding: 24rpx;
   184|    padding-top: calc(24rpx + env(safe-area-inset-top));
   185|}
   186|
   187|.dream-header {
   188|    .user-info {
   189|        display: flex;
   190|        align-items: center;
   191|        margin-bottom: 16rpx;
   192|        
   193|        .avatar {
   194|            width: 72rpx;
   195|            height: 72rpx;
   196|            border-radius: 50%;
   197|            margin-right: 16rpx;
   198|        }
   199|        
   200|        .info {
   201|            flex: 1;
   202|            
   203|            .nickname {
   204|                font-size: 30rpx;
   205|                font-weight: 600;
   206|                color: $text-primary;
   207|                display: block;
   208|            }
   209|            
   210|            .date {
   211|                font-size: 24rpx;
   212|                color: $text-tertiary;
   213|                display: block;
   214|                margin-top: 4rpx;
   215|            }
   216|        }
   217|        
   218|        .actions {
   219|            .action-btn {
   220|                font-size: 24rpx;
   221|                color: #A29BFE;
   222|                background: $primary-color;
   223|                padding: 8rpx 16rpx;
   224|                border-radius: 16rpx;
   225|            }
   226|        }
   227|    }
   228|    
   229|    .category-tag {
   230|        background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
   231|        color: $text-primary;
   232|        border-radius: 16rpx;
   233|        padding: 8rpx 20rpx;
   234|        font-size: 24rpx;
   235|        display: inline-block;
   236|    }
   237|}
   238|
   239|.images-section {
   240|    margin-bottom: 24rpx;
   241|    
   242|    .detail-image {
   243|        width: 100%;
   244|        border-radius: 24rpx;
   245|        margin-bottom: 12rpx;
   246|    }
   247|}
   248|
   249|.description-card {
   250|    margin-bottom: 24rpx;
   251|    
   252|    .description {
   253|        font-size: 30rpx;
   254|        color: $text-primary;
   255|        line-height: 1.8;
   256|    }
   257|}
   258|
   259|.info-card {
   260|    margin-bottom: 24rpx;
   261|    
   262|    .info-row {
   263|        display: flex;
   264|        padding: 12rpx 0;
   265|        border-bottom: 2rpx solid $glass-card-bg;
   266|        
   267|        &:last-child { border-bottom: none; }
   268|        
   269|        .info-label {
   270|            width: 120rpx;
   271|            font-size: 26rpx;
   272|            color: $text-secondary;
   273|        }
   274|        
   275|        .info-value {
   276|            flex: 1;
   277|            font-size: 26rpx;
   278|            color: $text-primary;
   279|        }
   280|    }
   281|}
   282|
   283|.tags-section {
   284|    display: flex;
   285|    flex-wrap: wrap;
   286|    margin-bottom: 24rpx;
   287|    padding: 0 8rpx;
   288|}
   289|
   290|.action-bar {
   291|    display: flex;
   292|    justify-content: space-around;
   293|    margin-bottom: 24rpx;
   294|    
   295|    .action-item {
   296|        text-align: center;
   297|        
   298|        .action-icon {
   299|            font-size: 40rpx;
   300|            display: block;
   301|        }
   302|        
   303|        .action-text {
   304|            font-size: 22rpx;
   305|            color: $text-secondary;
   306|            display: block;
   307|            margin-top: 4rpx;
   308|        }
   309|    }
   310|}
   311|
   312|.similar-section {
   313|    .section-title {
   314|        font-size: 30rpx;
   315|        font-weight: 600;
   316|        color: $text-primary;
   317|        margin-bottom: 16rpx;
   318|        padding: 0 8rpx;
   319|    }
   320|    
   321|    .similar-card {
   322|        margin-bottom: 16rpx;
   323|        
   324|        .similar-desc {
   325|            font-size: 26rpx;
   326|            color: $text-secondary;
   327|            display: block;
   328|        }
   329|        
   330|        .similar-score {
   331|            font-size: 24rpx;
   332|            color: #A29BFE;
   333|            display: block;
   334|            margin-top: 8rpx;
   335|        }
   336|    }
   337|}
   338|
   339|.empty-state {
   340|    text-align: center;
   341|    padding: 120rpx 0;
   342|    color: $text-tertiary;
   343|    font-size: 28rpx;
   344|}
   345|</style>
   346|