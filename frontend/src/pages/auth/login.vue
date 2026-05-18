     1|<template>
     2|    <view class="gradient-bg login-page">
     3|        <view class="header-bar">
     4|            <LangSwitch />
     5|        </view>
     6|        <view class="logo-section">
     7|            <text class="logo-text">💭</text>
     8|            <text class="app-name">{{ $t('app.name') }}</text>
     9|            <text class="app-slogan">{{ $t('app.slogan') }}</text>
    10|        </view>
    11|        
    12|        <view class="form-section">
    13|            <view class="input-row">
    14|                <text class="label">{{ $t('auth.account') }}</text>
    15|                <input v-model="username" :placeholder="$t('auth.placeholder.account')" placeholder-class="input-placeholder" class="input-field" />
    16|            </view>
    17|            
    18|            <view class="input-row">
    19|                <text class="label">{{ $t('auth.password') }}</text>
    20|                <input v-model="password" type="password" :placeholder="$t('auth.placeholder.password')" placeholder-class="input-placeholder" class="input-field" />
    21|            </view>
    22|            
    23|            <view class="forgot-row" @click="goForgot">
    24|                <text class="forgot-link">{{ $t('auth.forgotPassword') }}</text>
    25|            </view>
    26|            
    27|            <button class="login-btn" @click="handleLogin" :loading="loading">
    28|                {{ $t('auth.login') }}
    29|            </button>
    30|            
    31|            <view class="footer-link">
    32|                <text>{{ $t('auth.noAccount') }}</text>
    33|                <text class="link" @click="goRegister">{{ $t('auth.registerNow') }}</text>
    34|            </view>
    35|        </view>
    36|    </view>
    37|</template>
    38|
    39|<script>
    40|import { authApi } from '@/utils/api';
    41|import { useUserStore } from '@/store/user';
    42|
    43|export default {
    44|    components: {
    45|        LangSwitch: () => import('@/components/LangSwitch.vue')
    46|    },
    47|    data() {
    48|        return {
    49|            username: '',
    50|            password: '',
    51|            loading: false
    52|        };
    53|    },
    54|    methods: {
    55|        async handleLogin() {
    56|            if (!this.username || !this.password) {
    57|                uni.showToast({ title: this.$t('auth.fillAll'), icon: 'none' });
    58|                return;
    59|            }
    60|            this.loading = true;
    61|            try {
    62|                const res = await authApi.login({
    63|                    username: this.username,
    64|                    password: this.password
    65|                });
    66|                const userStore = useUserStore();
    67|                userStore.login(res.token, res.userId);
    68|                uni.showToast({ title: this.$t('auth.loginSuccess'), icon: 'success' });
    69|                setTimeout(() => {
    70|                    uni.switchTab({ url: '/pages/explore/explore' });
    71|                }, 1000);
    72|            } catch (e) {
    73|                console.error('Login failed:', e);
    74|            } finally {
    75|                this.loading = false;
    76|            }
    77|        },
    78|        goRegister() {
    79|            uni.navigateTo({ url: '/pages/auth/register' });
    80|        },
    81|        goForgot() {
    82|            uni.navigateTo({ url: '/pages/auth/forgot-password' });
    83|        }
    84|    }
    85|};
    86|</script>
    87|
    88|<style lang="scss" scoped>
    89|.header-bar {
    90|    width: 100%;
    91|    display: flex;
    92|    justify-content: flex-end;
    93|    padding: 20rpx 40rpx;
    94|}
    95|
    96|.login-page {
    97|    min-height: 100vh;
    98|    display: flex;
    99|    flex-direction: column;
   100|    align-items: center;
   101|    justify-content: center;
   102|    padding: 40rpx;
   103|}
   104|
   105|.logo-section {
   106|    text-align: center;
   107|    margin-bottom: 60rpx;
   108|    
   109|    .logo-text {
   110|        font-size: 120rpx;
   111|        display: block;
   112|        margin-bottom: 20rpx;
   113|    }
   114|    
   115|    .app-name {
   116|        font-size: 48rpx;
   117|        font-weight: 700;
   118|        color: $text-primary;
   119|        display: block;
   120|        margin-bottom: 16rpx;
   121|    }
   122|    
   123|    .app-slogan {
   124|        font-size: 26rpx;
   125|        color: $text-secondary;
   126|        display: block;
   127|    }
   128|}
   129|
   130|.form-section {
   131|    width: 100%;
   132|    max-width: 640rpx;
   133|}
   134|
   135|.input-row {
   136|    display: flex;
   137|    align-items: center;
   138|    margin-bottom: 32rpx;
   139|    
   140|    .label {
   141|        font-size: 28rpx;
   142|        color: $text-primary;
   143|        width: 120rpx;
   144|        flex-shrink: 0;
   145|    }
   146|    
   147|    .input-field {
   148|        flex: 1;
   149|        background: $glass-card-bg;
   150|        border: 1rpx solid $glass-card-bg-hover;
   151|        border-radius: 16rpx;
   152|        padding: 20rpx 24rpx;
   153|        font-size: 28rpx;
   154|        color: $text-primary;
   155|        height: 80rpx;
   156|        backdrop-filter: blur(12px);
   157|        -webkit-backdrop-filter: blur(12px);
   158|    }
   159|}
   160|
   161|.input-placeholder {
   162|    color: $text-tertiary !important;
   163|}
   164|
   165|.forgot-row {
   166|    display: flex;
   167|    justify-content: flex-end;
   168|    margin-bottom: 24rpx;
   169|    padding-right: 8rpx;
   170|    
   171|    .forgot-link {
   172|        font-size: 26rpx;
   173|        color: $text-primary;
   174|        text-decoration: underline;
   175|        opacity: 0.9;
   176|    }
   177|}
   178|
   179|.login-btn {
   180|    width: 100%;
   181|    background: $primary-color;
   182|    color: $text-primary;
   183|    border: 1rpx solid $glass-border;
   184|    border-radius: 48rpx;
   185|    padding: 20rpx 48rpx;
   186|    font-size: 32rpx;
   187|    font-weight: 600;
   188|    letter-spacing: 8rpx;
   189|    backdrop-filter: blur(12px);
   190|    -webkit-backdrop-filter: blur(12px);
   191|    
   192|    &::after {
   193|        border: none;
   194|    }
   195|    
   196|    &:active {
   197|        opacity: 0.85;
   198|    }
   199|}
   200|
   201|.footer-link {
   202|    text-align: center;
   203|    margin-top: 40rpx;
   204|    font-size: 26rpx;
   205|    color: $text-secondary;
   206|    
   207|    .link {
   208|        color: $text-primary;
   209|        font-weight: 600;
   210|        margin-left: 8rpx;
   211|        text-decoration: underline;
   212|    }
   213|}
   214|</style>
   215|