     1|<template>
     2|    <view class="search-page">
     3|        <view class="header-bar">
     4|            <text class="title">{{ $t('explore.search') }}</text>
     5|            <LangSwitch />
     6|        </view>
     7|        <view class="search-bar">
     8|            <view class="search-input-wrapper">
     9|                <text class="search-icon">🔍</text>
    10|                <input v-model="keyword" :placeholder="$t('detail.searchPlaceholder')" @confirm="doSearch" class="search-input" />
    11|                <text class="clear-btn" @click="clearSearch" v-if="keyword">×</text>
    12|            </view>
    13|        </view>
    14|        
    15|        <view class="result-section" v-if="hasSearched">
    16|            <view class="tab-bar">
    17|                <view class="tab-item" :class="{active: searchType === 'all'}" @click="searchType = 'all'; doSearch()">{{ $t('detail.allTab') }}</view>
    18|                <view class="tab-item" :class="{active: searchType === 'dream'}" @click="searchType = 'dream'; doSearch()">{{ $t('detail.dreamsCount') }}</view>
    19|                <view class="tab-item" :class="{active: searchType === 'user'}" @click="searchType = 'user'; doSearch()">{{ $t('detail.usersCount') }}</view>
    20|                <view class="tab-item" :class="{active: searchType === 'discussion'}" @click="searchType = 'discussion'; doSearch()">{{ $t('detail.discussionsCount') }}</view>
    21|            </view>
    22|            
    23|            <!-- 梦境结果 -->
    24|            <view class="result-group" v-if="(searchType === 'all' || searchType === 'dream') && results.dreams?.length">
    25|                <text class="group-title">{{ $t('detail.dreamResults') }} ({{ results.dreams.length }})</text>
    26|                <view class="result-item card" v-for="item in results.dreams" :key="item.id" @click="goDreamDetail(item.id)">
    27|                    <text class="item-title">{{ item.description }}</text>
    28|                    <text class="item-meta">{{ item.nickname }} · {{ item.category }}</text>
    29|                </view>
    30|            </view>
    31|            
    32|            <!-- 用户结果 -->
    33|            <view class="result-group" v-if="(searchType === 'all' || searchType === 'user') && results.users?.length">
    34|                <text class="group-title">{{ $t('detail.userResults') }} ({{ results.users.length }})</text>
    35|                <view class="result-item card" v-for="item in results.users" :key="item.id" @click="goProfile(item.id)">
    36|                    <text class="item-title">{{ item.nickname }}</text>
    37|                    <text class="item-meta">{{ item.bio || $t('detail.noBio') }}</text>
    38|                </view>
    39|            </view>
    40|            
    41|            <!-- 讨论结果 -->
    42|            <view class="result-group" v-if="(searchType === 'all' || searchType === 'discussion') && results.discussions?.length">
    43|                <text class="group-title">{{ $t('detail.discussionResults') }} ({{ results.discussions.length }})</text>
    44|                <view class="result-item card" v-for="item in results.discussions" :key="item.id" @click="goDiscussion(item.id)">
    45|                    <text class="item-title">{{ item.title }}</text>
    46|                    <text class="item-meta">{{ item.creatorNickname }} · {{ item.memberCount }}{{ $t('detail.fansLabel') }}</text>
    47|                </view>
    48|            </view>
    49|            
    50|            <view class="empty-state" v-if="!results.dreams?.length && !results.users?.length && !results.discussions?.length">
    51|                <text class="empty-icon">🔍</text>
    52|                <text>{{ $t('detail.noResults') }}</text>
    53|            </view>
    54|        </view>
    55|        
    56|        <!-- 热门标签 -->
    57|        <view class="hot-tags" v-if="!hasSearched">
    58|            <text class="section-title">{{ $t('detail.hotSearch') }}</text>
    59|            <view class="tags">
    60|                <text class="tag" v-for="tag in hotTags" :key="tag.tag" @click="searchFor(tag.tag)">{{ tag.tag }}</text>
    61|            </view>
    62|        </view>
    63|    </view>
    64|</template>
    65|
    66|<script>
    67|import { searchApi } from '@/utils/api';
    68|import { useSettingsStore } from '@/store/settings';
    69|
    70|export default {
    71|    components: {
    72|        LangSwitch: () => import('@/components/LangSwitch.vue')
    73|    },
    74|    setup() {
    75|        const settingsStore = useSettingsStore();
    76|        return { settingsStore };
    77|    },
    78|    data() {
    79|        return {
    80|            keyword: '',
    81|            searchType: 'all',
    82|            hasSearched: false,
    83|            results: { dreams: [], users: [], discussions: [] },
    84|            hotTags: []
    85|        };
    86|    },
    87|    onLoad() {
    88|        this.loadHotTags();
    89|    },
    90|    methods: {
    91|        async loadHotTags() {
    92|            try {
    93|                this.hotTags = await searchApi.tags('');
    94|            } catch (e) {
    95|                this.hotTags = [{ tag: '飞行' }, { tag: '坠落' }, { tag: '考试' }, { tag: '被追逐' }, { tag: '水' }];
    96|            }
    97|        },
    98|        
    99|        async doSearch() {
   100|            if (!this.keyword.trim()) return;
   101|            this.hasSearched = true;
   102|            try {
   103|                this.results = await searchApi.search(this.keyword, this.searchType === 'all' ? null : this.searchType);
   104|            } catch (e) {
   105|                console.error('Search failed:', e);
   106|            }
   107|        },
   108|        
   109|        searchFor(tag) {
   110|            this.keyword = tag;
   111|            this.doSearch();
   112|        },
   113|        
   114|        clearSearch() {
   115|            this.keyword = '';
   116|            this.hasSearched = false;
   117|            this.results = { dreams: [], users: [], discussions: [] };
   118|        },
   119|        
   120|        goDreamDetail(id) {
   121|            uni.navigateTo({ url: '/pages/detail/dream-detail?id=' + id });
   122|        },
   123|        
   124|        goProfile(id) {
   125|            uni.navigateTo({ url: '/pages/detail/profile?id=' + id });
   126|        },
   127|        
   128|        goDiscussion(id) {
   129|            uni.navigateTo({ url: '/pages/detail/discussion-detail?id=' + id });
   130|        }
   131|    }
   132|};
   133|</script>
   134|
   135|<style lang="scss" scoped>
   136|.search-page {
   137|    min-height: 100vh;
   138|    padding: 24rpx;
   139|    padding-top: calc(24rpx + env(safe-area-inset-top));
   140|}
   141|
   142|.header-bar {
   143|    display: flex;
   144|    justify-content: space-between;
   145|    align-items: center;
   146|    margin-bottom: 12rpx;
   147|    
   148|    .title {
   149|        font-size: 36rpx;
   150|        font-weight: 700;
   151|        color: $text-primary;
   152|    }
   153|    
   154|    }
   155|
   156|.search-bar {
   157|    margin-bottom: 24rpx;
   158|    
   159|    .search-input-wrapper {
   160|        display: flex;
   161|        align-items: center;
   162|        background: $glass-card-bg;
   163|        backdrop-filter: blur(20px);
   164|        -webkit-backdrop-filter: blur(20px);
   165|        border: 1rpx solid $glass-card-bg-hover;
   166|        border-radius: 48rpx;
   167|        padding: 0 24rpx;
   168|        box-shadow: 0 2rpx 12rpx $glass-shadow;
   169|        
   170|        .search-icon {
   171|            font-size: 32rpx;
   172|            margin-right: 12rpx;
   173|        }
   174|        
   175|        .search-input {
   176|            flex: 1;
   177|            font-size: 28rpx;
   178|            padding: 20rpx 0;
   179|            border: none;
   180|            background: transparent;
   181|        }
   182|        
   183|        .clear-btn {
   184|            font-size: 32rpx;
   185|            color: $text-tertiary;
   186|            padding: 0 12rpx;
   187|        }
   188|    }
   189|}
   190|
   191|.tab-bar {
   192|    display: flex;
   193|    background: $glass-card-bg-light;
   194|    backdrop-filter: blur(20px);
   195|    -webkit-backdrop-filter: blur(20px);
   196|    padding: 12rpx 16rpx;
   197|    border-radius: 16rpx;
   198|    margin-bottom: 24rpx;
   199|    
   200|    .tab-item {
   201|        flex: 1;
   202|        text-align: center;
   203|        padding: 10rpx 0;
   204|        font-size: 24rpx;
   205|        color: $text-secondary;
   206|        border-radius: 12rpx;
   207|        
   208|        &.active {
   209|            background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
   210|            color: $text-primary;
   211|            font-weight: 600;
   212|        }
   213|    }
   214|}
   215|
   216|.result-group {
   217|    margin-bottom: 24rpx;
   218|    
   219|    .group-title {
   220|        font-size: 26rpx;
   221|        color: $text-secondary;
   222|        margin-bottom: 12rpx;
   223|        display: block;
   224|    }
   225|    
   226|    .result-item {
   227|        margin-bottom: 12rpx;
   228|        
   229|        .item-title {
   230|            font-size: 28rpx;
   231|            color: $text-primary;
   232|            display: block;
   233|            margin-bottom: 8rpx;
   234|        }
   235|        
   236|        .item-meta {
   237|            font-size: 22rpx;
   238|            color: $text-tertiary;
   239|        }
   240|    }
   241|}
   242|
   243|.hot-tags {
   244|    .section-title {
   245|        font-size: 30rpx;
   246|        font-weight: 600;
   247|        color: $text-primary;
   248|        display: block;
   249|        margin-bottom: 20rpx;
   250|    }
   251|    
   252|    .tags {
   253|        display: flex;
   254|        flex-wrap: wrap;
   255|        gap: 16rpx;
   256|    }
   257|}
   258|
   259|.empty-state {
   260|    text-align: center;
   261|    padding: 80rpx 0;
   262|    color: $text-tertiary;
   263|    
   264|    .empty-icon {
   265|        font-size: 80rpx;
   266|        display: block;
   267|        margin-bottom: 16rpx;
   268|    }
   269|}
   270|</style>
   271|