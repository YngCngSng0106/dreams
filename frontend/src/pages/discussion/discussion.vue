     1|     1|<template>
     2|     2|    <view class="discussion-page">
     3|     3|        <!-- 自定义导航栏 -->
     4|     4|        <view class="navbar" :style="{height: statusBarHeight + 88 + 'rpx'}">
     5|     5|            <view class="navbar-content" :style="{paddingTop: statusBarHeight + 'rpx'}">
     6|     6|                <text class="title">{{ $t('discussion.title') }}</text>
     7|     7|                <view class="create-btn" @click="createDiscussion">+ {{ $t('discussion.create') }}</view>
     8|     8|            </view>
     9|     9|        </view>
    10|    10|        
    11|    11|        <!-- Tab切换 -->
    12|    12|        <view class="tab-bar" :style="{marginTop: (statusBarHeight + 88) + 'rpx'}">
    13|    13|            <view class="tab-item" :class="{active: currentTab === 'all'}" @click="currentTab = 'all'">{{ $t('discussion.all') }}</view>
    14|    14|            <view class="tab-item" :class="{active: currentTab === 'mine'}" @click="currentTab = 'mine'">{{ $t('discussion.mine') }}</view>
    15|    15|            <view class="tab-item" :class="{active: currentTab === 'recommended'}" @click="currentTab = 'recommended'">{{ $t('discussion.recommended') }}</view>
    16|    16|        </view>
    17|    17|        
    18|    18|        <!-- 讨论组列表 -->
    19|    19|        <scroll-view scroll-y class="list" @scrolltolower="loadMore">
    20|    20|            <view class="disc-card card" v-for="disc in discussions" :key="disc.id" @click="goDetail(disc.id)">
    21|    21|                <image class="cover" :src="disc.coverImage || '/static/default-cover.png'" mode="aspectFill" v-if="disc.coverImage" />
    22|    22|                <view class="info">
    23|    23|                    <text class="title">{{ disc.title }}</text>
    24|    24|                    <text class="desc">{{ disc.description || $t('discussion.noDesc') }}</text>
    25|    25|                    <view class="meta">
    26|    26|                        <text class="creator">{{ disc.creatorNickname }}</text>
    27|    27|                        <text class="members">👥 {{ disc.memberCount }}人</text>
    28|    28|                    </view>
    29|    29|                </view>
    30|    30|            </view>
    31|    31|            
    32|    32|            <view class="empty-state" v-if="discussions.length === 0 && !loading">
    33|    33|                <text class="empty-icon">💬</text>
    34|    34|                <text class="empty-text">{{ $t('discussion.empty') }}</text>
    35|    35|            </view>
    36|    36|        </scroll-view>
    37|    37|        
    38|    38|        <!-- 创建按钮 -->
    39|    39|        <view class="fab-btn" @click="createDiscussion">+</view>
    40|    40|    </view>
    41|    41|
    42|    42|    <custom-tab-bar ref="tabbar" />
    43|    43|</template>
    44|    44|
    45|    45|<script>
    46|    46|import { discussionApi } from '@/utils/api';
    47|    47|import { requireLogin } from '@/utils/auth';
    48|    48|
    49|    49|export default {
    50|    50|    data() {
    51|    51|        return {
    52|    52|            statusBarHeight: 0,
    53|    53|            discussions: [],
    54|    54|            currentTab: 'all',
    55|    55|            page: 1,
    56|    56|            pageSize: 10,
    57|    57|            loading: false,
    58|    58|            hasMore: true
    59|    59|        };
    60|    60|    },
    61|    61|    onLoad() {
    62|    62|        this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
    63|    63|        this.loadDiscussions();
    64|    64|    },
    65|    65|    onShow() {
    66|    66|        if (this.discussions.length > 0) {
    67|    67|            this.page = 1;
    68|    68|            this.discussions = [];
    69|    69|            this.loadDiscussions();
    70|    70|        }
    71|    71|        this.$nextTick(() => {
    72|    72|            const tabbar = this.$refs.tabbar;
    73|    73|            if (tabbar) tabbar.updateCurrentPage();
    74|    74|        });
    75|    75|    },
    76|    76|    methods: {
    77|    77|        async loadDiscussions() {
    78|    78|            if (this.loading) return;
    79|    79|            this.loading = true;
    80|    80|            try {
    81|    81|                let res;
    82|    82|                if (this.currentTab === 'mine') {
    83|    83|                    if (!requireLogin()) { this.loading = false; return; }
    84|    84|                    res = await discussionApi.myList(this.page, this.pageSize);
    85|    85|                } else if (this.currentTab === 'recommended') {
    86|    86|                    if (!requireLogin()) { this.loading = false; return; }
    87|    87|                    res = await discussionApi.recommended(this.page, this.pageSize);
    88|    88|                } else {
    89|    89|                    res = await discussionApi.list(this.page, this.pageSize);
    90|    90|                }
    91|    91|                this.discussions = this.discussions.concat(res.records || []);
    92|    92|                this.page++;
    93|    93|                if (!res.records || res.records.length < this.pageSize) {
    94|    94|                    this.hasMore = false;
    95|    95|                }
    96|    96|            } catch (e) {
    97|    97|                console.error('Load discussions failed:', e);
    98|    98|            } finally {
    99|    99|                this.loading = false;
   100|   100|            }
   101|   101|        },
   102|   102|        
   103|   103|        loadMore() {
   104|   104|            if (this.hasMore && !this.loading) {
   105|   105|                this.loadDiscussions();
   106|   106|            }
   107|   107|        },
   108|   108|        
   109|   109|        goDetail(id) {
   110|   110|            uni.navigateTo({ url: '/pages/detail/discussion-detail?id=' + id });
   111|   111|        },
   112|   112|        
   113|   113|        createDiscussion() {
   114|   114|            if (!requireLogin()) return;
   115|   115|            uni.navigateTo({ url: '/pages/detail/discussion-detail?create=1' });
   116|   116|        }
   117|   117|    }
   118|   118|};
   119|   119|</script>
   120|   120|
   121|   121|<style lang="scss" scoped>
   122|   122|.discussion-page {
   123|   123|    min-height: 100vh;
   124|   124|}
   125|   125|
   126|   126|.navbar {
   127|   127|    position: fixed;
   128|   128|    top: 0;
   129|   129|    left: 0;
   130|   130|    right: 0;
   131|   131|    z-index: 100;
   132|   132|    background: $glass-bg-gradient-1;
   133|   133|    backdrop-filter: blur(20px);
   134|   134|    -webkit-backdrop-filter: blur(20px);
   135|   135|    border-bottom: 1rpx solid $glass-card-bg;
   136|   136|    
   137|   137|    .navbar-content {
   138|   138|        height: 88rpx;
   139|   139|        display: flex;
   140|   140|        align-items: center;
   141|   141|        justify-content: space-between;
   142|   142|        padding: 0 24rpx;
   143|   143|        
   144|   144|        .title {
   145|   145|            font-size: 36rpx;
   146|   146|            font-weight: 700;
   147|   147|            color: $text-primary;
   148|   148|        }
   149|   149|        
   150|   150|        .create-btn {
   151|   151|            background: $glass-card-bg-hover;
   152|   152|            color: $text-primary;
   153|   153|            border-radius: 24rpx;
   154|   154|            padding: 8rpx 20rpx;
   155|   155|            font-size: 24rpx;
   156|   156|        }
   157|   157|    }
   158|   158|}
   159|   159|
   160|   160|.tab-bar {
   161|   161|    display: flex;
   162|   162|    background: $glass-card-bg-light;
   163|   163|    padding: 16rpx 24rpx;
   164|   164|    border-bottom: 1rpx solid $glass-card-bg;
   165|   165|    
   166|   166|    .tab-item {
   167|   167|        flex: 1;
   168|   168|        text-align: center;
   169|   169|        padding: 12rpx 0;
   170|   170|        font-size: 28rpx;
   171|   171|        color: $text-secondary;
   172|   172|        border-radius: 16rpx;
   173|   173|        
   174|   174|        &.active {
   175|   175|            color: #6C5CE7;
   176|   176|            font-weight: 600;
   177|   177|            background: $primary-color;
   178|   178|        }
   179|   179|    }
   180|   180|}
   181|   181|
   182|   182|.list {
   183|   183|    height: calc(100vh - 300rpx);
   184|   184|    padding: 24rpx;
   185|   185|}
   186|   186|
   187|   187|.disc-card {
   188|   188|    display: flex;
   189|   189|    margin-bottom: 24rpx;
   190|   190|    overflow: hidden;
   191|   191|    background: $glass-card-bg;
   192|   192|    backdrop-filter: blur(20px);
   193|   193|    -webkit-backdrop-filter: blur(20px);
   194|   194|    border: 1rpx solid $glass-card-bg-hover;
   195|   195|    
   196|   196|    .cover {
   197|   197|        width: 180rpx;
   198|   198|        height: 180rpx;
   199|   199|        flex-shrink: 0;
   200|   200|    }
   201|   201|    
   202|   202|    .info {
   203|   203|        flex: 1;
   204|   204|        padding: 20rpx;
   205|   205|        display: flex;
   206|   206|        flex-direction: column;
   207|   207|        justify-content: space-between;
   208|   208|        
   209|   209|        .title {
   210|   210|            font-size: 30rpx;
   211|   211|            font-weight: 600;
   212|   212|            color: $text-primary;
   213|   213|            display: block;
   214|   214|            margin-bottom: 8rpx;
   215|   215|        }
   216|   216|        
   217|   217|        .desc {
   218|   218|            font-size: 24rpx;
   219|   219|            color: $text-secondary;
   220|   220|            display: block;
   221|   221|            flex: 1;
   222|   222|            overflow: hidden;
   223|   223|            text-overflow: ellipsis;
   224|   224|            display: -webkit-box;
   225|   225|            -webkit-line-clamp: 2;
   226|   226|            -webkit-box-orient: vertical;
   227|   227|        }
   228|   228|        
   229|   229|        .meta {
   230|   230|            display: flex;
   231|   231|            justify-content: space-between;
   232|   232|            align-items: center;
   233|   233|            margin-top: 8rpx;
   234|   234|            
   235|   235|            .creator {
   236|   236|                font-size: 22rpx;
   237|   237|                color: $text-tertiary;
   238|   238|            }
   239|   239|            
   240|   240|            .members {
   241|   241|                font-size: 22rpx;
   242|   242|                color: #6C5CE7;
   243|   243|            }
   244|   244|        }
   245|   245|    }
   246|   246|}
   247|   247|
   248|   248|.empty-state {
   249|   249|    padding: 100rpx 0;
   250|   250|    text-align: center;
   251|   251|    
   252|   252|    .empty-icon {
   253|   253|        font-size: 100rpx;
   254|   254|        display: block;
   255|   255|        margin-bottom: 24rpx;
   256|   256|    }
   257|   257|    
   258|   258|    .empty-text {
   259|   259|        font-size: 28rpx;
   260|   260|        color: $text-tertiary;
   261|   261|    }
   262|   262|}
   263|   263|
   264|   264|.fab-btn {
   265|   265|    position: fixed;
   266|   266|    right: 32rpx;
   267|   267|    bottom: 160rpx;
   268|   268|    width: 96rpx;
   269|   269|    height: 96rpx;
   270|   270|    background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
   271|   271|    color: $text-primary;
   272|   272|    border-radius: 50%;
   273|   273|    display: flex;
   274|   274|    align-items: center;
   275|   275|    justify-content: center;
   276|   276|    font-size: 48rpx;
   277|   277|    box-shadow: 0 8rpx 32rpx $primary-color;
   278|   278|    z-index: 50;
   279|   279|}
   280|   280|</style>
   281|   281|