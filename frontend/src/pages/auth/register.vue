     1|<template>
     2|    <view class="gradient-bg register-page">
     3|        <view class="header-bar">
     4|            <text class="back-btn" @click="goBack">←</text>
     5|            <text class="page-title">{{ $t('auth.register') }}</text>
     6|            <LangSwitch />
     7|        </view>
     8|        
     9|        <view class="logo-section">
    10|            <text class="logo-text">💭</text>
    11|            <text class="app-name">{{ $t('app.name') }}</text>
    12|        </view>
    13|        
    14|        <view class="form-section">
    15|            <view class="input-row">
    16|                <text class="row-label">{{ $t('auth.username') }}</text>
    17|                <input class="row-input" v-model="username" :placeholder="$t('auth.placeholder.username')" placeholder-class="input-placeholder" />
    18|            </view>
    19|            
    20|            <view class="input-row">
    21|                <text class="row-label">{{ $t('auth.nickname') }}</text>
    22|                <input class="row-input" v-model="nickname" :placeholder="$t('auth.placeholder.nickname')" placeholder-class="input-placeholder" />
    23|            </view>
    24|            
    25|            <view class="input-row">
    26|                <text class="row-label">{{ $t('auth.email') }}</text>
    27|                <input class="row-input" v-model="email" :placeholder="$t('auth.placeholder.email')" placeholder-class="input-placeholder" type="email" />
    28|            </view>
    29|            
    30|            <view class="input-row">
    31|                <text class="row-label">{{ $t('auth.phone') }}</text>
    32|                <input class="row-input" v-model="phone" :placeholder="$t('auth.placeholder.phone')" placeholder-class="input-placeholder" type="number" maxlength="11" />
    33|            </view>
    34|            
    35|            <view class="input-row">
    36|                <text class="row-label">{{ $t('auth.password') }}</text>
    37|                <input class="row-input" v-model="password" :placeholder="$t('auth.placeholder.password')" placeholder-class="input-placeholder" type="password" />
    38|            </view>
    39|            
    40|            <view class="input-row">
    41|                <text class="row-label">{{ $t('auth.confirmPassword') }}</text>
    42|                <input class="row-input" v-model="confirmPassword" :placeholder="$t('auth.placeholder.confirmPassword')" placeholder-class="input-placeholder" type="password" />
    43|            </view>
    44|            
    45|            <button class="register-btn" @click="handleRegister" :loading="loading">
    46|                {{ $t('auth.register') }}
    47|            </button>
    48|            
    49|            <view class="footer-link">
    50|                <text>{{ $t('auth.hasAccount') }}</text>
    51|                <text class="link" @click="goLogin">{{ $t('auth.loginNow') }}</text>
    52|            </view>
    53|        </view>
    54|    </view>
    55|</template>
    56|
    57|<script>
    58|import { authApi } from '@/utils/api';
    59|import { setToken, setUserId } from '@/utils/auth';
    60|
    61|export default {
    62|    components: {
    63|        LangSwitch: () => import('@/components/LangSwitch.vue')
    64|    },
    65|    data() {
    66|        return {
    67|            username: '',
    68|            nickname: '',
    69|            email: '',
    70|            phone: '',
    71|            password: '',
    72|            confirmPassword: '',
    73|            loading: false
    74|        };
    75|    },
    76|    methods: {
    77|        async handleRegister() {
    78|            if (!this.username || !this.nickname || !this.password) {
    79|                uni.showToast({ title: this.$t('auth.fillAll'), icon: 'none' });
    80|                return;
    81|            }
    82|            if (this.password !== this.confirmPassword) {
    83|                uni.showToast({ title: this.$t('auth.passwordMismatch'), icon: 'none' });
    84|                return;
    85|            }
    86|            if (this.password.length < 6) {
    87|                uni.showToast({ title: this.$t('auth.passwordShort'), icon: 'none' });
    88|                return;
    89|            }
    90|            this.loading = true;
    91|            try {
    92|                const res = await authApi.register({
    93|                    username: this.username,
    94|                    nickname: this.nickname,
    95|                    password: this.password,
    96|                    email: this.email,
    97|                    phone: this.phone
    98|                });
    99|                setToken(res.token);
   100|                setUserId(res.userId);
   101|                uni.showToast({ title: this.$t('auth.registerSuccess'), icon: 'success' });
   102|                setTimeout(() => {
   103|                    uni.switchTab({ url: '/pages/explore/explore' });
   104|                }, 1000);
   105|            } catch (e) {
   106|                console.error('Register failed:', e);
   107|            } finally {
   108|                this.loading = false;
   109|            }
   110|        },
   111|        goBack() {
   112|            uni.navigateBack();
   113|        },
   114|        goLogin() {
   115|            uni.navigateBack();
   116|        }
   117|    }
   118|};
   119|</script>
   120|
   121|<style lang="scss">
   122|.register-page {
   123|    min-height: 100vh;
   124|    display: flex;
   125|    flex-direction: column;
   126|    align-items: center;
   127|    padding: 40rpx;
   128|}
   129|
   130|.header-bar {
   131|    width: 100%;
   132|    max-width: 640rpx;
   133|    display: flex;
   134|    align-items: center;
   135|    justify-content: space-between;
   136|    margin-bottom: 20rpx;
   137|}
   138|
   139|.back-btn {
   140|    font-size: 40rpx;
   141|    color: $text-primary;
   142|}
   143|
   144|.page-title {
   145|    font-size: 32rpx;
   146|    color: $text-primary;
   147|    font-weight: 600;
   148|}
   149|
   150|.logo-section {
   151|    text-align: center;
   152|    margin-bottom: 40rpx;
   153|}
   154|
   155|.logo-text {
   156|    font-size: 80rpx;
   157|    display: block;
   158|    margin-bottom: 12rpx;
   159|}
   160|
   161|.app-name {
   162|    font-size: 36rpx;
   163|    font-weight: 700;
   164|    color: $text-primary;
   165|    display: block;
   166|}
   167|
   168|.form-section {
   169|    width: 100%;
   170|    max-width: 640rpx;
   171|}
   172|
   173|.input-row {
   174|    display: flex !important;
   175|    align-items: center !important;
   176|    margin-bottom: 28rpx;
   177|    white-space: nowrap;
   178|}
   179|
   180|.row-label {
   181|    font-size: 28rpx;
   182|    color: $text-primary;
   183|    width: 120rpx !important;
   184|    min-width: 120rpx;
   185|    flex-shrink: 0;
   186|    display: inline-block;
   187|}
   188|
   189|.row-input {
   190|    flex: 1 !important;
   191|    background: $glass-card-bg !important;
   192|    border: 1rpx solid $glass-card-bg-hover !important;
   193|    border-radius: 16rpx !important;
   194|    padding: 20rpx 24rpx !important;
   195|    font-size: 28rpx !important;
   196|    color: $text-primary !important;
   197|    height: 80rpx !important;
   198|    min-width: 0;
   199|    backdrop-filter: blur(12px);
   200|    -webkit-backdrop-filter: blur(12px);
   201|}
   202|
   203|.input-placeholder {
   204|    color: $text-tertiary !important;
   205|}
   206|
   207|.register-btn {
   208|    width: 100%;
   209|    background: $primary-color !important;
   210|    color: $text-primary !important;
   211|    border: 1rpx solid $glass-border !important;
   212|    border-radius: 48rpx !important;
   213|    padding: 20rpx 48rpx !important;
   214|    font-size: 32rpx !important;
   215|    font-weight: 600 !important;
   216|    letter-spacing: 8rpx !important;
   217|    margin-top: 8rpx !important;
   218|    backdrop-filter: blur(12px);
   219|    -webkit-backdrop-filter: blur(12px);
   220|}
   221|
   222|.register-btn::after {
   223|    border: none !important;
   224|}
   225|
   226|.register-btn:active {
   227|    opacity: 0.85;
   228|}
   229|
   230|.footer-link {
   231|    text-align: center;
   232|    margin-top: 40rpx;
   233|    font-size: 26rpx;
   234|    color: $text-secondary;
   235|}
   236|
   237|.link {
   238|    color: $text-primary !important;
   239|    font-weight: 600 !important;
   240|    margin-left: 8rpx !important;
   241|    text-decoration: underline !important;
   242|}
   243|</style>
   244|