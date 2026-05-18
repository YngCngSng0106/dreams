     1|<template>
     2|    <view class="profile-page">
     3|        <view class="header-bar">
     4|            <text class="title">{{ $t('detail.dreamsTab') }}</text>
     5|            <LangSwitch />
     6|        </view>
     7|        <view class="profile-header gradient-bg" v-if="user">
     8|            <image class="avatar" :src="user.avatar || '/static/default-avatar.png'" mode="aspectFill" />
     9|            <text class="nickname">{{ user.nickname }}</text>
    10|            <text class="bio">{{ user.bio || $t('detail.bioLazy') }}</text>
    11|        </view>
    12|        
    13|        <view class="stats-row card">
    14|            <view class="stat-item">
    15|                <text class="count">{{ user.dreamCount || 0 }}</text>
    16|                <text class="label">{{ $t('detail.dreamsLabel') }}</text>
    17|            </view>
    18|            <view class="stat-divider"></view>
    19|            <view class="stat-item">
    20|                <text class="count">{{ user.followingCount || 0 }}</text>
    21|                <text class="label">{{ $t('detail.followingLabel') }}</text>
    22|            </view>
    23|            <view class="stat-divider"></view>
    24|            <view class="stat-item">
    25|                <text class="count">{{ user.followersCount || 0 }}</text>
    26|                <text class="label">{{ $t('detail.fansLabel') }}</text>
    27|            </view>
    28|        </view>
    29|        
    30|        <view class="action-row" v-if="isOtherUser">
    31|            <button class="btn-primary" :class="{following: followStatus?.isFollowing === 1}" @click="toggleFollow">
    32|                {{ followStatus?.isFollowing === 1 ? $t('detail.followingYes') : $t('detail.followingNo') }}
    33|            </button>
    34|        </view>
    35|        
    36|        <!-- Tab切换 -->
    37|        <view class="tab-bar">
    38|            <view class="tab-item" :class="{active: currentTab === 'dreams'}" @click="currentTab = 'dreams'">{{ $t('detail.dreamsTab') }}</view>
    39|            <view class="tab-item" :class="{active: currentTab === 'stats'}" @click="loadStats">{{ $t('detail.statsTab') }}</view>
    40|        </view>
    41|        
    42|        <!-- 梦境列表 -->
    43|        <view class="dream-list" v-if="currentTab === 'dreams'">
    44|            <view class="dream-item card" v-for="dream in dreams" :key="dream.id" @click="goDetail(dream.id)">
    45|                <text class="category">{{ dream.category }}</text>
    46|                <text class="description">{{ dream.description }}</text>
    47|                <text class="date">{{ dream.dreamDate }}</text>
    48|            </view>
    49|        </view>
    50|        
    51|        <!-- 统计信息 -->
    52|        <view class="stats-detail card" v-if="currentTab === 'stats' && stats">
    53|            <view class="stat-row">
    54|                <text class="label">{{ $t('detail.topDream') }}</text>
    55|                <text class="value">{{ stats.topCategories?.[0]?.categoryName || $t('detail.none') }}</text>
    56|            </view>
    57|            <view class="stat-row">
    58|                <text class="label">{{ $t('detail.avgClarity') }}</text>
    59|                <text class="value">{{ (stats.avgClarity || 0).toFixed(1) }}⭐</text>
    60|            </view>
    61|            <view class="stat-row">
    62|                <text class="label">{{ $t('detail.monthlyNew') }}</text>
    63|                <text class="value">{{ stats.monthlyDreamCount || 0 }}{{ $t('detail.dreamsLabel') }}</text>
    64|            </view>
    65|        </view>
    66|        
    67|        <view class="empty-state" v-if="!user">
    68|            <text>{{ $t('detail.userNotFound') }}</text>
    69|        </view>
    70|    </view>
    71|</template>
    72|
    73|<script>
    74|import { userApi, followApi, statsApi } from '@/utils/api';
    75|import { useUserStore } from '@/store/user';
    76|import { isLoggedIn } from '@/utils/auth';
    77|import { useSettingsStore } from '@/store/settings';
    78|
    79|export default {
    80|    components: {
    81|        LangSwitch: () => import('@/components/LangSwitch.vue')
    82|    },
    83|    setup() {
    84|        const settingsStore = useSettingsStore();
    85|        return { settingsStore };
    86|    },
    87|    data() {
    88|        return {
    89|            userId: 0,
    90|            user: null,
    91|            dreams: [],
    92|            stats: null,
    93|            currentTab: 'dreams',
    94|            followStatus: null,
    95|            isOtherUser: true
    96|        };
    97|    },
    98|    onLoad(options) {
    99|        this.userId = parseInt(options.id) || 0;
   100|        this.isOtherUser = this.userId !== useUserStore().userId;
   101|        this.loadProfile();
   102|        this.loadDreams();
   103|        if (this.isOtherUser && isLoggedIn()) {
   104|            this.loadFollowStatus();
   105|        }
   106|    },
   107|    methods: {
   108|        async loadProfile() {
   109|            try {
   110|                this.user = await userApi.getUserProfile(this.userId);
   111|            } catch (e) {
   112|                console.error('Load profile failed:', e);
   113|            }
   114|        },
   115|        
   116|        async loadDreams() {
   117|            try {
   118|                const res = await userApi.getUserDreams(this.userId);
   119|                this.dreams = res.records || [];
   120|            } catch (e) {
   121|                console.error('Load dreams failed:', e);
   122|            }
   123|        },
   124|        
   125|        async loadFollowStatus() {
   126|            try {
   127|                this.followStatus = await followApi.status(this.userId);
   128|            } catch (e) {
   129|                // ignore
   130|            }
   131|        },
   132|        
   133|        async toggleFollow() {
   134|            if (!isLoggedIn()) {
   135|                uni.redirectTo({ url: '/pages/auth/login' });
   136|                return;
   137|            }
   138|            try {
   139|                if (this.followStatus?.isFollowing === 1) {
   140|                    await followApi.unfollow(this.userId);
   141|                    this.followStatus.isFollowing = 0;
   142|                    this.user.followersCount = Math.max(0, (this.user.followersCount || 1) - 1);
   143|                } else {
   144|                    await followApi.follow(this.userId);
   145|                    if (!this.followStatus) this.followStatus = {};
   146|                    this.followStatus.isFollowing = 1;
   147|                    this.user.followersCount = (this.user.followersCount || 0) + 1;
   148|                }
   149|            } catch (e) {
   150|                console.error('Follow failed:', e);
   151|            }
   152|        },
   153|        
   154|        async loadStats() {
   155|            this.currentTab = 'stats';
   156|            if (this.userId === useUserStore().userId) {
   157|                try {
   158|                    this.stats = await statsApi.myStats();
   159|                } catch (e) {
   160|                    console.error('Load stats failed:', e);
   161|                }
   162|            }
   163|        },
   164|        
   165|        goDetail(id) {
   166|            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + id });
   167|        }
   168|    }
   169|};
   170|</script>
   171|
   172|<style lang="scss" scoped>
   173|.profile-page {
   174|    min-height: 100vh;
   175|}
   176|
   177|.header-bar {
   178|    display: flex;
   179|    justify-content: space-between;
   180|    align-items: center;
   181|    padding: 24rpx;
   182|    padding-top: calc(24rpx + env(safe-area-inset-top));
   183|    
   184|    .title {
   185|        font-size: 36rpx;
   186|        font-weight: 700;
   187|        color: $text-primary;
   188|    }
   189|    
   190|    }
   191|}
   192|
   193|.profile-header {
   194|    padding: calc(80rpx + env(safe-area-inset-top)) 32rpx 48rpx;
   195|    text-align: center;
   196|    
   197|    .avatar {
   198|        width: 120rpx;
   199|        height: 120rpx;
   200|        border-radius: 50%;
   201|        border: 4rpx solid $text-placeholder;
   202|        display: block;
   203|        margin: 0 auto 16rpx;
   204|    }
   205|    
   206|    .nickname {
   207|        font-size: 36rpx;
   208|        font-weight: 700;
   209|        color: $text-primary;
   210|        display: block;
   211|        margin-bottom: 8rpx;
   212|    }
   213|    
   214|    .bio {
   215|        font-size: 24rpx;
   216|        color: $text-secondary;
   217|        display: block;
   218|    }
   219|}
   220|
   221|.stats-row {
   222|    display: flex;
   223|    align-items: center;
   224|    justify-content: space-around;
   225|    margin: -24rpx 32rpx 24rpx;
   226|    padding: 24rpx;
   227|    position: relative;
   228|    z-index: 10;
   229|    
   230|    .stat-item {
   231|        text-align: center;
   232|        .count { font-size: 36rpx; font-weight: 700; color: #A29BFE; display: block; }
   233|        .label { font-size: 22rpx; color: $text-secondary; display: block; margin-top: 4rpx; }
   234|    }
   235|    
   236|    .stat-divider {
   237|        width: 2rpx;
   238|        height: 60rpx;
   239|        background: $glass-card-bg;
   240|    }
   241|}
   242|
   243|.action-row {
   244|    padding: 0 32rpx;
   245|    margin-bottom: 24rpx;
   246|    
   247|    .btn-primary {
   248|        width: 100%;
   249|        &.following {
   250|            background: $glass-card-bg;
   251|            color: $text-secondary;
   252|        }
   253|    }
   254|}
   255|
   256|.tab-bar {
   257|    display: flex;
   258|    background: $glass-card-bg-light;
   259|    backdrop-filter: blur(20px);
   260|    -webkit-backdrop-filter: blur(20px);
   261|    padding: 16rpx 24rpx;
   262|    
   263|    .tab-item {
   264|        flex: 1;
   265|        text-align: center;
   266|        padding: 12rpx 0;
   267|        font-size: 28rpx;
   268|        color: $text-secondary;
   269|        border-radius: 12rpx;
   270|        
   271|        &.active {
   272|            color: #A29BFE;
   273|            font-weight: 600;
   274|            background: $primary-color;
   275|        }
   276|    }
   277|}
   278|
   279|.dream-list {
   280|    padding: 0 24rpx;
   281|    
   282|    .dream-item {
   283|        margin-bottom: 16rpx;
   284|        
   285|        .category {
   286|            background: $primary-color;
   287|            color: #A29BFE;
   288|            border-radius: 12rpx;
   289|            padding: 4rpx 12rpx;
   290|            font-size: 22rpx;
   291|            display: inline-block;
   292|            margin-bottom: 8rpx;
   293|        }
   294|        
   295|        .description {
   296|            font-size: 28rpx;
   297|            color: $text-primary;
   298|            display: block;
   299|            margin-bottom: 8rpx;
   300|            overflow: hidden;
   301|            text-overflow: ellipsis;
   302|            white-space: nowrap;
   303|        }
   304|        
   305|        .date {
   306|            font-size: 22rpx;
   307|            color: $text-tertiary;
   308|            display: block;
   309|        }
   310|    }
   311|}
   312|
   313|.stats-detail {
   314|    margin: 0 24rpx;
   315|    
   316|    .stat-row {
   317|        display: flex;
   318|        justify-content: space-between;
   319|        padding: 16rpx 0;
   320|        border-bottom: 2rpx solid $glass-card-bg;
   321|        
   322|        &:last-child { border-bottom: none; }
   323|        
   324|        .label { font-size: 28rpx; color: $text-secondary; }
   325|        .value { font-size: 28rpx; color: $text-primary; }
   326|    }
   327|}
   328|
   329|.empty-state {
   330|    text-align: center;
   331|    padding: 120rpx 0;
   332|    color: $text-tertiary;
   333|}
   334|</style>
   335|