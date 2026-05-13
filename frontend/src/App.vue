<script>
export default {
    onLaunch() {
        console.log('梦境分享App启动');
        const token = uni.getStorageSync('token');
        if (token) {
            console.log('用户已登录');
        }
    },
    onShow() {
        console.log('App显示');
    },
    onHide() {
        console.log('App隐藏');
    }
};
</script>

<style lang="scss">
@import './uni.scss';

page {
    box-sizing: border-box;
    padding-bottom: 100rpx;
}

view, scroll-view, swiper, button, input, textarea {
    padding: 0;
    margin: 0;
}

.gradient-bg {
    background: linear-gradient(135deg, #6C5CE7 0%, #A29BFE 100%);
}

.btn-primary {
    background: linear-gradient(135deg, #FFFFFF 0%, #E8E0FF 100%);
    color: #6C5CE7;
    border-radius: 48rpx;
    padding: 20rpx 48rpx;
    font-size: 28rpx;
    border: none;
    font-weight: 500;
}

.btn-primary:active {
    opacity: 0.8;
}

.btn-secondary {
    background: #FFFFFF;
    color: #6C5CE7;
    border-radius: 48rpx;
    padding: 20rpx 48rpx;
    font-size: 28rpx;
    border: 2rpx solid #6C5CE7;
}

.tag {
    display: inline-block;
    background: rgba(108, 92, 231, 0.1);
    color: #6C5CE7;
    border-radius: 24rpx;
    padding: 6rpx 16rpx;
    font-size: 22rpx;
    margin-right: 12rpx;
    margin-bottom: 8rpx;
}
</style>
