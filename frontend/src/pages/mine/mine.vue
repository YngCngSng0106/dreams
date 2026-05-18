     1|<template>
     2|    <view class="mine-page">
     3|        <!-- 头部信息 -->
     4|        <view class="profile-header gradient-bg">
     5|            <view class="header-right">
     6|                <LangSwitch position="custom" />
     7|            </view>
     8|            <view class="user-info" v-if="isLoggedIn">
     9|                <image class="avatar" :src="profile.avatar || '/static/default-avatar.png'" mode="aspectFill" @click="goProfile(myId)" />
    10|                <view class="info">
    11|                    <text class="nickname">{{ profile.nickname }}</text>
    12|                    <text class="bio">{{ profile.bio || $t('mine.lazyBio') }}</text>
    13|                </view>
    14|                <text class="settings-icon" @click="goSettings">⚙️</text>
    15|            </view>
    16|            <view class="user-info" v-else @click="goLogin">
    17|                <image class="avatar" src="/static/default-avatar.png" mode="aspectFill" />
    18|                <view class="info">
    19|                    <text class="nickname">{{ $t('mine.clickLogin') }}</text>
    20|                    <text class="bio">{{ $t('mine.afterLogin') }}</text>
    21|                </view>
    22|            </view>
    23|        </view>
    24|        
    25|        <!-- 数据统计 -->
    26|        <view class="stats-card card" v-if="isLoggedIn">
    27|            <view class="stat-item" @click="goMyDreams">
    28|                <text class="count">{{ profile.dreamCount || 0 }}</text>
    29|                <text class="label">{{ $t('mine.dreamCount') }}</text>
    30|            </view>
    31|            <view class="stat-divider"></view>
    32|            <view class="stat-item" @click="goMyDiscussions">
    33|                <text class="count">{{ profile.discussionCount || 0 }}</text>
    34|                <text class="label">{{ $t('mine.discussionCount') }}</text>
    35|            </view>
    36|            <view class="stat-divider"></view>
    37|            <view class="stat-item">
    38|                <text class="count">{{ profile.followersCount || 0 }}</text>
    39|                <text class="label">{{ $t('mine.followers') }}</text>
    40|            </view>
    41|            <view class="stat-divider"></view>
    42|            <view class="stat-item">
    43|                <text class="count">{{ profile.followingCount || 0 }}</text>
    44|                <text class="label">{{ $t('mine.following') }}</text>
    45|            </view>
    46|        </view>
    47|        
    48|        <!-- 菜单列表 -->
    49|        <view class="menu-section">
    50|            <view class="menu-item" @click="goMyDreams" v-if="isLoggedIn">
    51|                <text class="menu-icon">💭</text>
    52|                <text class="menu-text">{{ $t('mine.myDreams') }}</text>
    53|                <text class="menu-arrow">›</text>
    54|            </view>
    55|            <view class="menu-item" @click="goNotifications" v-if="isLoggedIn">
    56|                <text class="menu-icon">🔔</text>
    57|                <text class="menu-text">{{ $t('mine.notifications') }}</text>
    58|                <view class="menu-badge" v-if="unreadCount > 0">{{ unreadCount }}</view>
    59|                <text class="menu-arrow">›</text>
    60|            </view>
    61|            <view class="menu-item" @click="goSettings">
    62|                <text class="menu-icon">⚙️</text>
    63|                <text class="menu-text">{{ $t('mine.settings') }}</text>
    64|                <text class="menu-arrow">›</text>
    65|            </view>
    66|            <view class="menu-item" @click="goStats" v-if="isLoggedIn">
    67|                <text class="menu-icon">📊</text>
    68|                <text class="menu-text">{{ $t('mine.stats') }}</text>
    69|                <text class="menu-arrow">›</text>
    70|            </view>
    71|        </view>
    72|        
    73|        <!-- 退出登录 -->
    74|        <view class="logout-section" v-if="isLoggedIn">
    75|            <button class="logout-btn" @click="handleLogout">{{ $t('mine.logout') }}</button>
    76|        </view>
    77|    </view>
    78|
    79|    <custom-tab-bar ref="tabbar" />
    80|</template>
    81|
    82|<script>
    83|import { userApi, notificationApi, statsApi } from '@/utils/api';
    84|import { isLoggedIn, clearAuth } from '@/utils/auth';
    85|import { useSettingsStore } from '@/store/settings';
    86|import LangSwitch from '@/components/LangSwitch.vue';
    87|
    88|export default {
    89|    components: {
    90|        LangSwitch
    91|    },
    92|    setup() {
    93|        const settingsStore = useSettingsStore();
    94|        return { settingsStore };
    95|    },
    96|    data() {
    97|        return {
    98|            isLoggedIn: false,
    99|            profile: {},
   100|            myId: null,
   101|            unreadCount: 0
   102|        };
   103|    },
   104|    onShow() {
   105|        this.isLoggedIn = isLoggedIn();
   106|        if (this.isLoggedIn) {
   107|            this.loadProfile();
   108|            this.loadUnreadCount();
   109|        }
   110|        this.$nextTick(() => {
   111|            const tabbar = this.$refs.tabbar;
   112|            if (tabbar) tabbar.updateCurrentPage();
   113|        });
   114|    },
   115|    methods: {
   116|        async loadProfile() {
   117|            try {
   118|                this.profile = await userApi.getMyProfile();
   119|                this.myId = this.profile.id;
   120|            } catch (e) {
   121|                console.error('Load profile failed:', e);
   122|            }
   123|        },
   124|        
   125|        async loadUnreadCount() {
   126|            try {
   127|                this.unreadCount = await notificationApi.unreadCount();
   128|            } catch (e) {
   129|                // ignore
   130|            }
   131|        },
   132|        
   133|        goProfile(id) {
   134|            uni.navigateTo({ url: '/pages/detail/profile?id=' + id });
   135|        },
   136|        
   137|        goLogin() {
   138|            uni.navigateTo({ url: '/pages/auth/login' });
   139|        },
   140|        
   141|        goSettings() {
   142|            uni.navigateTo({ url: '/pages/detail/settings' });
   143|        },
   144|        
   145|        goMyDreams() {
   146|            uni.navigateTo({ url: '/pages/detail/dream-detail?type=mine' });
   147|        },
   148|        
   149|        goMyDiscussions() {
   150|            uni.switchTab({ url: '/pages/discussion/discussion' });
   151|        },
   152|        
   153|        goNotifications() {
   154|            uni.navigateTo({ url: '/pages/detail/notifications' });
   155|        },
   156|        
   157|        goStats() {
   158|            uni.navigateTo({ url: '/pages/detail/profile?id=' + this.myId + '&stats=1' });
   159|        },
   160|        
   161|        async handleLogout() {
   162|            uni.showModal({
   163|                title: '提示',
   164|                content: '确定要退出登录吗？',
   165|                success: async (res) => {
   166|                    if (res.confirm) {
   167|                        clearAuth();
   168|                        this.isLoggedIn = false;
   169|                        this.profile = {};
   170|                        uni.showToast({ title: '已退出登录', icon: 'success' });
   171|                    }
   172|                }
   173|            });
   174|        }
   175|    }
   176|};
   177|</script>
   178|
   179|<style lang="scss" scoped>
   180|.mine-page {
   181|    min-height: 100vh;
   182|    /* background removed — global uni.scss handles the dark gradient */
   183|}
   184|
   185|.profile-header {
   186|    padding: calc(80rpx + env(safe-area-inset-top)) 32rpx 48rpx;
   187|    position: relative;
   188|
   189|    .header-right {
   190|        position: absolute;
   191|        top: calc(80rpx + env(safe-area-inset-top));
   192|        right: 32rpx;
   193|    }
   194|
   195|    .user-info {
   196|        display: flex;
   197|        align-items: center;
   198|
   199|        .avatar {
   200|            width: 120rpx;
   201|            height: 120rpx;
   202|            border-radius: 50%;
   203|            border: 4rpx solid $text-placeholder;
   204|            margin-right: 24rpx;
   205|        }
   206|
   207|        .info {
   208|            flex: 1;
   209|
   210|            .nickname {
   211|                font-size: 36rpx;
   212|                font-weight: 700;
   213|                color: $text-primary;
   214|                display: block;
   215|                margin-bottom: 8rpx;
   216|            }
   217|
   218|            .bio {
   219|                font-size: 24rpx;
   220|                color: $text-secondary;
   221|                display: block;
   222|            }
   223|        }
   224|
   225|        .settings-icon {
   226|            font-size: 40rpx;
   227|        }
   228|    }
   229|}
   230|
   231|.stats-card {
   232|    display: flex;
   233|    align-items: center;
   234|    justify-content: space-around;
   235|    margin: -24rpx 32rpx 24rpx;
   236|    padding: 32rpx;
   237|    position: relative;
   238|    z-index: 10;
   239|    /* glassmorphism panel */
   240|    background: $glass-card-bg;
   241|    backdrop-filter: blur(20px);
   242|    -webkit-backdrop-filter: blur(20px);
   243|    border: 1rpx solid $glass-card-bg-hover;
   244|    border-radius: 24rpx;
   245|
   246|    .stat-item {
   247|        text-align: center;
   248|
   249|        .count {
   250|            font-size: 40rpx;
   251|            font-weight: 700;
   252|            color: #A29BFE;
   253|            display: block;
   254|        }
   255|
   256|        .label {
   257|            font-size: 22rpx;
   258|            color: $text-secondary;
   259|            display: block;
   260|            margin-top: 8rpx;
   261|        }
   262|    }
   263|
   264|    .stat-divider {
   265|        width: 2rpx;
   266|        height: 60rpx;
   267|        background: $glass-navbar-bg;
   268|    }
   269|}
   270|
   271|.menu-section {
   272|    /* glassmorphism panel */
   273|    background: $glass-card-bg;
   274|    backdrop-filter: blur(20px);
   275|    -webkit-backdrop-filter: blur(20px);
   276|    border: 1rpx solid $glass-card-bg-hover;
   277|    margin: 0 24rpx;
   278|    border-radius: 24rpx;
   279|    padding: 16rpx 0;
   280|    box-shadow: 0 8rpx 32rpx $glass-shadow;
   281|
   282|    .menu-item {
   283|        display: flex;
   284|        align-items: center;
   285|        padding: 24rpx 32rpx;
   286|        position: relative;
   287|
   288|        .menu-icon {
   289|            font-size: 36rpx;
   290|            margin-right: 20rpx;
   291|        }
   292|
   293|        .menu-text {
   294|            flex: 1;
   295|            font-size: 28rpx;
   296|            color: $text-primary;
   297|        }
   298|
   299|        .menu-badge {
   300|            background: #E17055;
   301|            color: $text-primary;
   302|            border-radius: 24rpx;
   303|            padding: 2rpx 12rpx;
   304|            font-size: 20rpx;
   305|            margin-right: 8rpx;
   306|            min-width: 32rpx;
   307|            text-align: center;
   308|            box-shadow: 0 0 12rpx $error-color;
   309|        }
   310|
   311|        .menu-arrow {
   312|            font-size: 32rpx;
   313|            color: $text-placeholder;
   314|        }
   315|    }
   316|}
   317|
   318|.logout-section {
   319|    padding: 48rpx 32rpx;
   320|
   321|    .logout-btn {
   322|        background: $glass-card-bg-light;
   323|        color: #E17055;
   324|        border-radius: 48rpx;
   325|        font-size: 28rpx;
   326|        border: 2rpx solid #E17055;
   327|    }
   328|}
   329|</style>
   330|