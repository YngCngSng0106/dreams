     1|<script>
     2|import { useUserStore } from '@/store/user';
     3|
     4|export default {
     5|    onLaunch() {
     6|        console.log('梦境分享App启动');
     7|        const userStore = useUserStore();
     8|        const token = userStore.token;
     9|        if (token) {
    10|            console.log('用户已登录');
    11|        }
    12|    },
    13|    onShow() {
    14|        console.log('App显示');
    15|    },
    16|    onHide() {
    17|        console.log('App隐藏');
    18|    }
    19|};
    20|</script>
    21|
    22|<style lang="scss">
    23|@import './uni.scss';
    24|
    25|page {
    26|    box-sizing: border-box;
    27|    padding-bottom: 100rpx;
    28|}
    29|
    30|view, scroll-view, swiper, button, input, textarea {
    31|    padding: 0;
    32|    margin: 0;
    33|}
    34|
    35|/* 渐变背景 - 毛玻璃风格 */
    36|.gradient-bg {
    37|    background: linear-gradient(160deg, #3A729F 0%, #29547E 40%, #1B3A5C 70%, #0D1B2A 100%);
    38|}
    39|
    40|/* 主按钮 - 毛玻璃半透明 */
    41|.btn-primary {
    42|    background: $primary-color;
    43|    backdrop-filter: blur(12px);
    44|    -webkit-backdrop-filter: blur(12px);
    45|    border: 1rpx solid $primary-light;
    46|    color: #FFFFFF;
    47|    border-radius: 48rpx;
    48|    padding: 20rpx 48rpx;
    49|    font-size: 28rpx;
    50|    font-weight: 500;
    51|}
    52|
    53|.btn-primary:active {
    54|    background: $primary-color;
    55|    opacity: 0.9;
    56|}
    57|
    58|/* 次按钮 - 透明边框 */
    59|.btn-secondary {
    60|    background: $glass-card-bg-light;
    61|    backdrop-filter: blur(12px);
    62|    -webkit-backdrop-filter: blur(12px);
    63|    color: #FFFFFF;
    64|    border-radius: 48rpx;
    65|    padding: 20rpx 48rpx;
    66|    font-size: 28rpx;
    67|    border: 1rpx solid $glass-border-active;
    68|}
    69|
    70|/* 标签 - 毛玻璃半透明 */
    71|.tag {
    72|    display: inline-block;
    73|    background: $primary-dark;
    74|    backdrop-filter: blur(8px);
    75|    -webkit-backdrop-filter: blur(8px);
    76|    border: 1rpx solid $primary-light;
    77|    color: #A29BFE;
    78|    border-radius: 24rpx;
    79|    padding: 6rpx 16rpx;
    80|    font-size: 22rpx;
    81|    margin-right: 12rpx;
    82|    margin-bottom: 8rpx;
    83|}
    84|</style>
    85|