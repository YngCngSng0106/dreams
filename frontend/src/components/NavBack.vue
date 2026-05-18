<template>
    <view class="nav-back" :style="{height: navHeight + 'px', paddingTop: statusBarHeight + 'px'}">
        <view class="nav-content" :style="{height: navHeight - statusBarHeight + 'px'}">
            <view class="nav-left" v-if="showBack" @click="goBack">
                <text class="back-icon">&#xe601;</text>
            </view>
            <view class="nav-center">
                <text class="nav-title">{{ title }}</text>
            </view>
            <view class="nav-right"></view>
        </view>
    </view>
</template>

<script>
export default {
    name: 'NavBack',
    props: {
        title: {
            type: String,
            default: ''
        },
        showBack: {
            type: Boolean,
            default: true
        },
        delta: {
            type: [String, Number],
            default: '1'
        }
    },
    data() {
        const sysInfo = uni.getSystemInfoSync();
        return {
            statusBarHeight: sysInfo.statusBarHeight || 0,
            navHeight: sysInfo.statusBarHeight + 44 || 44
        };
    },
    methods: {
        goBack() {
            const pages = getCurrentPages();
            const deltaVal = parseInt(this.delta) || 1;
            if (pages.length <= deltaVal) {
                uni.switchTab({ url: '/pages/explore/explore' });
            } else {
                uni.navigateBack({ delta: deltaVal });
            }
        }
    }
};
</script>

<style lang="scss" scoped>
.nav-back {
    width: 100%;
    background: linear-gradient(135deg, $glass-bg-gradient-1, $glass-bg-gradient-4);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    position: relative;
}

.nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16rpx;
}

.nav-left {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    width: 80rpx;
    height: 100%;
}

.back-icon {
    font-family: 'uniicons' !important;
    font-size: 36rpx;
    color: #FFFFFF;
    display: inline-block;
}

.nav-center {
    flex: 1;
    text-align: center;
    overflow: hidden;
}

.nav-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #FFFFFF;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    display: inline-block;
    max-width: 100%;
}

.nav-right {
    width: 80rpx;
    height: 100%;
}
</style>
