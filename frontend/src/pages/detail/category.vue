<template>
    <view class="page">
        <view class="header">
            <text class="title">梦境分类</text>
        </view>
        <view class="category-list">
            <view class="category-item" v-for="(item, index) in categories" :key="index" @tap="selectCategory(item)">
                <text class="category-name">{{item.name}}</text>
                <text class="category-count">{{item.count || 0}}个梦境</text>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            categories: [
                { name: '预知梦', code: 'precognition', count: 0 },
                { name: '重复梦', code: 'recurring', count: 0 },
                { name: '噩梦', code: 'nightmare', count: 0 },
                { name: '清醒梦', code: 'lucid', count: 0 },
                { name: '奇幻梦', code: 'fantasy', count: 0 },
                { name: '日常梦', code: 'daily', count: 0 },
                { name: '飞行梦', code: 'flying', count: 0 },
                { name: '坠落梦', code: 'falling', count: 0 },
                { name: '考试梦', code: 'exam', count: 0 },
                { name: '追逐梦', code: 'chase', count: 0 }
            ]
        };
    },
    methods: {
        selectCategory(item) {
            uni.navigateTo({
                url: `/pages/explore/explore?category=${item.code}`
            });
        }
    }
};
</script>

<style lang="scss" scoped>
.page {
    min-height: 100vh;
    background: #F8F9FE;
    padding: 20rpx;
}

.header {
    padding: 40rpx 20rpx;
    text-align: center;
}

.title {
    font-size: 36rpx;
    font-weight: bold;
    color: #2D3436;
}

.category-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
}

.category-item {
    flex: 0 0 calc(50% - 10rpx);
    background: #FFFFFF;
    border-radius: 16rpx;
    padding: 30rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
    
    &:active {
        background: #F0EEFF;
    }
}

.category-name {
    font-size: 30rpx;
    color: #6C5CE7;
    margin-bottom: 10rpx;
}

.category-count {
    font-size: 24rpx;
    color: #B2BEC3;
}
</style>
