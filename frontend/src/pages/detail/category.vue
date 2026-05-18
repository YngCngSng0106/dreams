     1|<template>
     2|    <view class="page">
     3|        <view class="header">
     4|            <text class="title">梦境分类</text>
     5|        </view>
     6|        <view class="category-list">
     7|            <view class="category-item" v-for="(item, index) in categories" :key="index" @tap="selectCategory(item)">
     8|                <text class="category-name">{{item.name}}</text>
     9|                <text class="category-count">{{item.count || 0}}个梦境</text>
    10|            </view>
    11|        </view>
    12|    </view>
    13|</template>
    14|
    15|<script>
    16|export default {
    17|    data() {
    18|        return {
    19|            categories: [
    20|                { name: '预知梦', code: 'precognition', count: 0 },
    21|                { name: '重复梦', code: 'recurring', count: 0 },
    22|                { name: '噩梦', code: 'nightmare', count: 0 },
    23|                { name: '清醒梦', code: 'lucid', count: 0 },
    24|                { name: '奇幻梦', code: 'fantasy', count: 0 },
    25|                { name: '日常梦', code: 'daily', count: 0 },
    26|                { name: '飞行梦', code: 'flying', count: 0 },
    27|                { name: '坠落梦', code: 'falling', count: 0 },
    28|                { name: '考试梦', code: 'exam', count: 0 },
    29|                { name: '追逐梦', code: 'chase', count: 0 }
    30|            ]
    31|        };
    32|    },
    33|    methods: {
    34|        selectCategory(item) {
    35|            uni.navigateTo({
    36|                url: `/pages/explore/explore?category=${item.code}`
    37|            });
    38|        }
    39|    }
    40|};
    41|</script>
    42|
    43|<style lang="scss" scoped>
    44|.page {
    45|    min-height: 100vh;
    46|    padding: 20rpx;
    47|}
    48|
    49|.header {
    50|    padding: 40rpx 20rpx;
    51|    text-align: center;
    52|}
    53|
    54|.title {
    55|    font-size: 36rpx;
    56|    font-weight: bold;
    57|    color: $text-primary;
    58|}
    59|
    60|.category-list {
    61|    display: flex;
    62|    flex-wrap: wrap;
    63|    gap: 20rpx;
    64|}
    65|
    66|.category-item {
    67|    flex: 0 0 calc(50% - 10rpx);
    68|    background: $glass-card-bg;
    69|    backdrop-filter: blur(20px);
    70|    -webkit-backdrop-filter: blur(20px);
    71|    border: 1rpx solid $glass-card-bg-hover;
    72|    border-radius: 16rpx;
    73|    padding: 30rpx;
    74|    display: flex;
    75|    flex-direction: column;
    76|    align-items: center;
    77|    justify-content: center;
    78|    box-shadow: 0 4rpx 16rpx $glass-shadow-light;
    79|    
    80|    &:active {
    81|        background: $primary-color;
    82|    }
    83|}
    84|
    85|.category-name {
    86|    font-size: 30rpx;
    87|    color: #A29BFE;
    88|    margin-bottom: 10rpx;
    89|}
    90|
    91|.category-count {
    92|    font-size: 24rpx;
    93|    color: $text-tertiary;
    94|}
    95|</style>
    96|