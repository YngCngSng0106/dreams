     1|     1|<template>
     2|     2|   <view class="explore-page">
     3|     3|        <!-- 自定义导航栏 -->
     4|     4|        <view class="navbar" :style="{height: statusBarHeight + 88 + 'rpx'}">
     5|     5|            <view class="navbar-content" :style="{paddingTop: statusBarHeight + 'rpx'}">
     6|     6|                <view class="navbar-left">
     7|     7|                    <text class="app-logo">💭</text>
     8|     8|                    <text class="app-title">{{ $t('explore.title') }}</text>
     9|     9|                </view>
    10|    10|                <view class="navbar-right">
    11|    11|                    <LangSwitch :size="22" position="custom" />
    12|    12|                    <text class="search-icon" @click="goSearch">🔍</text>
    13|    13|                </view>
    14|    14|            </view>
    15|    15|        </view>
    16|    16|
    17|    17|        <!-- 左右分栏 -->
    18|    18|        <view class="main-container" :style="{marginTop: (statusBarHeight + 88) + 'rpx'}">
    19|    19|            <!-- 左侧分类栏 -->
    20|    20|            <scroll-view class="sidebar" scroll-y>
    21|    21|                <view class="sidebar-item" :class="{active: selectedCategory === 0}" @click="selectCategory(0)">
    22|    22|                    <text class="sidebar-icon">🌙</text>
    23|    23|                    <text class="sidebar-text">{{ $t('category.all') }}</text>
    24|    24|                </view>
    25|    25|                <view class="sidebar-item" :class="{active: selectedCategory === cat.id}" v-for="cat in categories" :key="cat.id" @click="selectCategory(cat.id)">
    26|    26|                    <text class="sidebar-icon">{{ cat.icon }}</text>
    27|    27|                    <text class="sidebar-text">{{ cat.name }}</text>
    28|    28|                </view>
    29|    29|            </scroll-view>
    30|    30|
    31|    31|            <!-- 右侧梦境列表 -->
    32|    32|            <scroll-view class="content-area" scroll-y refresher-enabled :refresher-triggered="isRefreshing" @refresherrefresh="onRefresh" @scrolltolower="loadMore">
    33|    33|                <view class="content-header">
    34|    34|                    <text class="content-title">{{ currentCategoryName }}</text>
    35|    35|                    <text class="content-count">{{ $t('explore.count', { count: dreams.length }) }}</text>
    36|    36|                </view>
    37|    37|
    38|    38|                <view class="dream-card" v-for="(dream, index) in dreams" :key="dream.id" @click="goDetail(dream.id)">
    39|    39|                    <view class="dream-header">
    40|    40|                        <view class="user-info" @click.stop="goProfile(dream.userId)">
    41|    41|                            <image class="avatar" :src="dream.avatar || '/static/default-avatar.png'" mode="aspectFill" />
    42|    42|                            <view class="user-text">
    43|    43|                                <text class="nickname">{{ dream.nickname || $t('explore.anonymous') }}</text>
    44|    44|                                <text class="time">{{ formatTime(dream.createTime) }}</text>
    45|    45|                            </view>
    46|    46|                        </view>
    47|    47|                        <view class="category-tag" v-if="dream.categoryCode">{{ getCatIcon(dream.categoryCode) }} {{ $t('category.' + dream.categoryCode) }}</view>
    48|    48|                    </view>
    49|    49|
    50|    50|                    <view class="dream-content">
    51|    51|                        <text class="description">{{ dream.description }}</text>
    52|    52|                    </view>
    53|    53|
    54|    54|                    <view class="dream-tags" v-if="dream.tags">
    55|    55|                        <text class="tag" v-for="tag in parseTags(dream.tags)" :key="tag">#{{ tag }}</text>
    56|    56|                    </view>
    57|    57|
    58|    58|                    <view class="dream-footer">
    59|    59|                        <view class="action-btn" @click.stop="toggleLike(dream, index)">
    60|    60|                            <text class="action-icon">{{ dream.liked ? '❤️' : '🤍' }}</text>
    61|    61|                            <text class="action-count">{{ dream.likeCount }}</text>
    62|    62|                        </view>
    63|    63|                        <view class="action-btn">
    64|    64|                            <text class="action-icon">💬</text>
    65|    65|                            <text class="action-count">{{ dream.commentCount || 0 }}</text>
    66|    66|                        </view>
    67|    67|                        <view class="action-btn">
    68|    68|                            <text class="action-icon">🔄</text>
    69|    69|                            <text class="action-count">{{ $t('explore.share') }}</text>
    70|    70|                        </view>
    71|    71|                    </view>
    72|    72|                </view>
    73|    73|
    74|    74|                <view class="empty-state" v-if="dreams.length === 0 && !loading">
    75|    75|                    <text class="empty-icon">💤</text>
    76|    76|                    <text class="empty-text">{{ $t('explore.empty') }}</text>
    77|    77|                    <text class="empty-hint">{{ $t('explore.emptyHint') }}</text>
    78|    78|                </view>
    79|    79|
    80|    80|                <view class="loading-more" v-if="loading">
    81|    81|                    <text>{{ $t('explore.loading') }}</text>
    82|    82|                </view>
    83|    83|            </scroll-view>
    84|    84|        </view>
    85|    85|        
    86|    86|        <custom-tab-bar ref="tabbar" />
    87|    87|    </view>
    88|    88|</template>
    89|    89|
    90|    90|<script>
    91|    91|import { dreamApi, categoryApi } from '@/utils/api';
    92|    92|import { requireLogin } from '@/utils/auth';
    93|    93|import dayjs from 'dayjs';
    94|    94|import relativeTime from 'dayjs/plugin/relativeTime.js';
    95|    95|import 'dayjs/locale/zh';
    96|    96|import LangSwitch from '@/components/LangSwitch.vue';
    97|    97|dayjs.extend(relativeTime);
    98|    98|dayjs.locale('zh');
    99|    99|
   100|   100|export default {
   101|   101|    components: {
   102|   102|        LangSwitch
   103|   103|    },
   104|   104|    computed: {
   105|   105|        currentCategoryName() {
   106|   106|            if (this.selectedCategory === 0) return this.$t('category.all');
   107|   107|            const cat = this.categories.find(c => c.id === this.selectedCategory);
   108|   108|            return cat ? cat.name : this.$t('category.all');
   109|   109|        }
   110|   110|    },
   111|   111|    data() {
   112|   112|        return {
   113|   113|            statusBarHeight: 0,
   114|   114|            dreams: [],
   115|   115|            categories: [],
   116|   116|            selectedCategory: 0,
   117|   117|            page: 1,
   118|   118|            pageSize: 10,
   119|   119|            loading: false,
   120|   120|            isRefreshing: false,
   121|   121|            hasMore: true,
   122|   122|            categoryIcons: {
   123|   123|                flying: '🦅',
   124|   124|                falling: '🪨',
   125|   125|                exam: '📝',
   126|   126|                chase: '🏃',
   127|   127|                water: '🌊',
   128|   128|                family: '👨‍👩‍👧',
   129|   129|                work: '💼',
   130|   130|                ghost: '👻',
   131|   131|                love: '💕',
   132|   132|                other: '🔮'
   133|   133|            }
   134|   134|        };
   135|   135|    },
   136|   136|    onLoad() {
   137|   137|        this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
   138|   138|        this.loadCategories();
   139|   139|        this.loadDreams();
   140|   140|        this.updateTabBar();
   141|   141|    },
   142|   142|    onShow() {
   143|   143|        this.updateTabBar();
   144|   144|    },
   145|   145|    watch: {
   146|   146|        '$i18n.locale'() {
   147|   147|            this.loadCategories();
   148|   148|        }
   149|   149|    },
   150|   150|    onPullDownRefresh() {
   151|   151|        this.onRefresh();
   152|   152|    },
   153|   153|    methods: {
   154|   154|        updateTabBar() {
   155|   155|            this.$nextTick(() => {
   156|   156|                const tabbar = this.$refs.tabbar;
   157|   157|                if (tabbar) {
   158|   158|                    tabbar.updateCurrentPage();
   159|   159|                }
   160|   160|            });
   161|   161|        },
   162|   162|        getCatIcon(code) {
   163|   163|            return this.categoryIcons[code] || '🔮';
   164|   164|        },
   165|   165|        async loadCategories() {
   166|   166|            try {
   167|   167|                const cats = await categoryApi.list();
   168|   168|                this.categories = cats.map(c => {
   169|   169|                    const code = c.code;
   170|   170|                    const name = this.$t('category.' + code) || c.name;
   171|   171|                    const icon = c.icon || this.categoryIcons[code] || '🔮';
   172|   172|                    return { id: c.id, name, icon, code };
   173|   173|                });
   174|   174|            } catch (e) {
   175|   175|                const codes = ['flying','falling','exam','chase','water','family','work','ghost','love','other'];
   176|   176|                this.categories = codes.map((code, i) => ({
   177|   177|                    id: i + 1,
   178|   178|                    name: this.$t('category.' + code),
   179|   179|                    icon: this.categoryIcons[code] || '🔮',
   180|   180|                    code
   181|   181|                }));
   182|   182|            }
   183|   183|        },
   184|   184|
   185|   185|        selectCategory(catId) {
   186|   186|            this.selectedCategory = catId;
   187|   187|            this.page = 1;
   188|   188|            this.dreams = [];
   189|   189|            this.hasMore = true;
   190|   190|            this.loadDreams();
   191|   191|        },
   192|   192|
   193|   193|        async loadDreams() {
   194|   194|            if (this.loading || !this.hasMore) return;
   195|   195|            this.loading = true;
   196|   196|            try {
   197|   197|                const catId = this.selectedCategory === 0 ? null : this.selectedCategory;
   198|   198|                const res = await dreamApi.feed(this.page, this.pageSize, 'newest', catId);
   199|   199|                this.dreams = this.dreams.concat(res.records || []);
   200|   200|                this.page++;
   201|   201|                if (!res.records || res.records.length < this.pageSize) {
   202|   202|                    this.hasMore = false;
   203|   203|                }
   204|   204|            } catch (e) {
   205|   205|                console.error('Load dreams failed:', e);
   206|   206|            } finally {
   207|   207|                this.loading = false;
   208|   208|                this.isRefreshing = false;
   209|   209|                uni.stopPullDownRefresh();
   210|   210|            }
   211|   211|        },
   212|   212|
   213|   213|        async onRefresh() {
   214|   214|            this.isRefreshing = true;
   215|   215|            this.page = 1;
   216|   216|            this.dreams = [];
   217|   217|            this.hasMore = true;
   218|   218|            await this.loadDreams();
   219|   219|        },
   220|   220|
   221|   221|        loadMore() {
   222|   222|            if (this.hasMore && !this.loading) {
   223|   223|                this.loadDreams();
   224|   224|            }
   225|   225|        },
   226|   226|
   227|   227|        async toggleLike(dream, index) {
   228|   228|            if (!requireLogin()) return;
   229|   229|            try {
   230|   230|                if (dream.liked) {
   231|   231|                    await dreamApi.unlike(dream.id);
   232|   232|                    this.dreams[index].liked = false;
   233|   233|                    this.dreams[index].likeCount = Math.max(0, (this.dreams[index].likeCount || 1) - 1);
   234|   234|                } else {
   235|   235|                    await dreamApi.like(dream.id);
   236|   236|                    this.dreams[index].liked = true;
   237|   237|                    this.dreams[index].likeCount = (this.dreams[index].likeCount || 0) + 1;
   238|   238|                }
   239|   239|            } catch (e) {
   240|   240|                console.error('Like failed:', e);
   241|   241|            }
   242|   242|        },
   243|   243|
   244|   244|        goDetail(dreamId) {
   245|   245|            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + dreamId });
   246|   246|        },
   247|   247|
   248|   248|        goProfile(userId) {
   249|   249|            uni.navigateTo({ url: '/pages/detail/profile?id=' + userId });
   250|   250|        },
   251|   251|
   252|   252|        goSearch() {
   253|   253|            uni.navigateTo({ url: '/pages/detail/search' });
   254|   254|        },
   255|   255|
   256|   256|        formatTime(time) {
   257|   257|            if (!time) return '';
   258|   258|            const now = dayjs();
   259|   259|            const target = dayjs(time);
   260|   260|            if (now.diff(target, 'hour') < 1) return target.fromNow();
   261|   261|            if (now.diff(target, 'day') < 1) return this.$t('time.today') + ' ' + target.format('HH:mm');
   262|   262|            if (now.diff(target, 'day') < 7) return target.format('MM-DD HH:mm');
   263|   263|            return target.format('YYYY-MM-DD');
   264|   264|        },
   265|   265|
   266|   266|        parseTags(tags) {
   267|   267|            if (!tags) return [];
   268|   268|            return tags.split(/[,,\s]+/).filter(t => t.trim());
   269|   269|        }
   270|   270|    }
   271|   271|};
   272|   272|</script>
   273|   273|
   274|   274|<style lang="scss">
   275|   275|.explore-page {
   276|   276|    min-height: 100vh;
   277|   277|    display: flex;
   278|   278|    flex-direction: column;
   279|   279|}
   280|   280|
   281|   281|.navbar {
   282|   282|    position: fixed;
   283|   283|    top: 0;
   284|   284|    left: 0;
   285|   285|    right: 0;
   286|   286|    z-index: 100;
   287|   287|    background: $glass-bg-gradient-1;
   288|   288|    backdrop-filter: blur(20px);
   289|   289|    -webkit-backdrop-filter: blur(20px);
   290|   290|    border-bottom: 1rpx solid $glass-card-bg;
   291|   291|}
   292|   292|
   293|   293|.navbar-content {
   294|   294|    height: 88rpx;
   295|   295|    display: flex;
   296|   296|    align-items: center;
   297|   297|    justify-content: space-between;
   298|   298|    padding: 0 24rpx;
   299|   299|}
   300|   300|
   301|   301|.navbar-left {
   302|   302|    display: flex;
   303|   303|    align-items: center;
   304|   304|}
   305|   305|
   306|   306|.app-logo {
   307|   307|    font-size: 44rpx;
   308|   308|    margin-right: 12rpx;
   309|   309|}
   310|   310|
   311|   311|.app-title {
   312|   312|    font-size: 36rpx;
   313|   313|    font-weight: 700;
   314|   314|    color: $text-primary;
   315|   315|}
   316|   316|
   317|   317|.navbar-right {
   318|   318|    display: flex;
   319|   319|    align-items: center;
   320|   320|}
   321|   321|
   322|   322|.search-icon {
   323|   323|    font-size: 36rpx;
   324|   324|}
   325|   325|
   326|   326|// 左右分栏容器
   327|   327|.main-container {
   328|   328|    flex: 1;
   329|   329|    display: flex;
   330|   330|    overflow: hidden;
   331|   331|}
   332|   332|
   333|   333|// 左侧分类栏
   334|   334|.sidebar {
   335|   335|    width: 180rpx;
   336|   336|    background: $glass-card-bg-light;
   337|   337|    border-right: 1rpx solid $glass-card-bg;
   338|   338|    flex-shrink: 0;
   339|   339|}
   340|   340|
   341|   341|.sidebar-item {
   342|   342|    display: flex;
   343|   343|    flex-direction: column;
   344|   344|    align-items: center;
   345|   345|    justify-content: center;
   346|   346|    padding: 24rpx 8rpx;
   347|   347|    border-bottom: 1rpx solid $glass-border-light;
   348|   348|    transition: all 0.2s;
   349|   349|}
   350|   350|
   351|   351|.sidebar-item.active {
   352|   352|    background: $primary-color;
   353|   353|    border: 1rpx solid $primary-light;
   354|   354|}
   355|   355|
   356|   356|.sidebar-icon {
   357|   357|    font-size: 40rpx;
   358|   358|    margin-bottom: 8rpx;
   359|   359|}
   360|   360|
   361|   361|.sidebar-text {
   362|   362|    font-size: 22rpx;
   363|   363|    color: $text-secondary;
   364|   364|    text-align: center;
   365|   365|    line-height: 1.3;
   366|   366|}
   367|   367|
   368|   368|.sidebar-item.active .sidebar-text {
   369|   369|    color: $text-primary;
   370|   370|}
   371|   371|
   372|   372|// 右侧内容区
   373|   373|.content-area {
   374|   374|    flex: 1;
   375|   375|    min-width: 0;
   376|   376|}
   377|   377|
   378|   378|.content-header {
   379|   379|    padding: 24rpx;
   380|   380|    display: flex;
   381|   381|    align-items: baseline;
   382|   382|    justify-content: space-between;
   383|   383|    border-bottom: 1rpx solid $glass-border-light;
   384|   384|    background: transparent;
   385|   385|}
   386|   386|
   387|   387|.content-title {
   388|   388|    font-size: 32rpx;
   389|   389|    font-weight: 600;
   390|   390|    color: $text-primary;
   391|   391|}
   392|   392|
   393|   393|.content-count {
   394|   394|    font-size: 22rpx;
   395|   395|    color: $text-tertiary;
   396|   396|}
   397|   397|
   398|   398|.dream-card {
   399|   399|    background: $glass-card-bg;
   400|   400|    backdrop-filter: blur(20px);
   401|   401|    -webkit-backdrop-filter: blur(20px);
   402|   402|    border: 1rpx solid $glass-card-bg-hover;
   403|   403|    margin: 16rpx 24rpx;
   404|   404|    padding: 24rpx;
   405|   405|    border-radius: 16rpx;
   406|   406|    box-shadow: 0 8rpx 32rpx $glass-shadow;
   407|   407|}
   408|   408|
   409|   409|.dream-header {
   410|   410|    display: flex;
   411|   411|    justify-content: space-between;
   412|   412|    align-items: center;
   413|   413|    margin-bottom: 16rpx;
   414|   414|}
   415|   415|
   416|   416|.user-info {
   417|   417|    display: flex;
   418|   418|    align-items: center;
   419|   419|}
   420|   420|
   421|   421|.avatar {
   422|   422|    width: 64rpx;
   423|   423|    height: 64rpx;
   424|   424|    border-radius: 50%;
   425|   425|    margin-right: 16rpx;
   426|   426|}
   427|   427|
   428|   428|.user-text {
   429|   429|    display: flex;
   430|   430|    flex-direction: column;
   431|   431|}
   432|   432|
   433|   433|.nickname {
   434|   434|    font-size: 28rpx;
   435|   435|    font-weight: 600;
   436|   436|    color: $text-primary;
   437|   437|}
   438|   438|
   439|   439|.time {
   440|   440|    font-size: 22rpx;
   441|   441|    color: $text-tertiary;
   442|   442|    margin-top: 4rpx;
   443|   443|}
   444|   444|
   445|   445|.category-tag {
   446|   446|    background: $primary-color;
   447|   447|    border: 1rpx solid $primary-light;
   448|   448|    color: #A29BFE;
   449|   449|    border-radius: 24rpx;
   450|   450|    padding: 6rpx 16rpx;
   451|   451|    font-size: 22rpx;
   452|   452|}
   453|   453|
   454|   454|.dream-content {
   455|   455|    margin-bottom: 16rpx;
   456|   456|}
   457|   457|
   458|   458|.description {
   459|   459|    font-size: 28rpx;
   460|   460|    color: $text-primary;
   461|   461|    line-height: 1.6;
   462|   462|    display: -webkit-box;
   463|   463|    -webkit-line-clamp: 3;
   464|   464|    -webkit-box-orient: vertical;
   465|   465|    overflow: hidden;
   466|   466|}
   467|   467|
   468|   468|.dream-tags {
   469|   469|    display: flex;
   470|   470|    flex-wrap: wrap;
   471|   471|    margin-bottom: 16rpx;
   472|   472|}
   473|   473|
   474|   474|.tag {
   475|   475|    background: $primary-dark;
   476|   476|    color: #A29BFE;
   477|   477|    border-radius: 24rpx;
   478|   478|    padding: 6rpx 16rpx;
   479|   479|    font-size: 22rpx;
   480|   480|    margin-right: 12rpx;
   481|   481|    margin-bottom: 8rpx;
   482|   482|}
   483|   483|
   484|   484|.dream-footer {
   485|   485|    display: flex;
   486|   486|    justify-content: space-around;
   487|   487|    border-top: 1rpx solid $glass-card-bg;
   488|   488|    padding-top: 16rpx;
   489|   489|}
   490|   490|
   491|   491|.action-btn {
   492|   492|    display: flex;
   493|   493|    align-items: center;
   494|   494|}
   495|   495|
   496|   496|.action-icon {
   497|   497|    font-size: 28rpx;
   498|   498|    margin-right: 8rpx;
   499|   499|}
   500|   500|
   501|   501|.action-count {
   502|   502|    font-size: 24rpx;
   503|   503|    color: $text-tertiary;
   504|   504|}
   505|   505|
   506|   506|.empty-state {
   507|   507|    padding: 100rpx 0;
   508|   508|    text-align: center;
   509|   509|}
   510|   510|
   511|   511|.empty-icon {
   512|   512|    font-size: 100rpx;
   513|   513|    display: block;
   514|   514|    margin-bottom: 24rpx;
   515|   515|}
   516|   516|
   517|   517|.empty-text {
   518|   518|    font-size: 32rpx;
   519|   519|    color: $text-secondary;
   520|   520|    display: block;
   521|   521|    margin-bottom: 12rpx;
   522|   522|}
   523|   523|
   524|   524|.empty-hint {
   525|   525|    font-size: 26rpx;
   526|   526|    color: $text-tertiary;
   527|   527|    display: block;
   528|   528|}
   529|   529|
   530|   530|.loading-more {
   531|   531|    text-align: center;
   532|   532|    padding: 24rpx;
   533|   533|    color: $text-tertiary;
   534|   534|    font-size: 24rpx;
   535|   535|}
   536|   536|</style>
   537|   537|