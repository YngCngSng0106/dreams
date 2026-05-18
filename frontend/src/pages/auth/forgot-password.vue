     1|<template>
     2|    <view class="gradient-bg page">
     3|        <view class="header">
     4|            <text class="back" @click="goBack">‹</text>
     5|            <text class="title">{{ $t('forgot.title') }}</text>
     6|            <LangSwitch />
     7|        </view>
     8|        
     9|        <view class="form-section">
    10|            <view class="step-desc" v-if="step === 1">
    11|                {{ $t('forgot.step1') }}
    12|            </view>
    13|            <view class="step-desc" v-else>
    14|                {{ $t('forgot.step2', { email: email }) }}
    15|            </view>
    16|            
    17|            <view class="input-row">
    18|                <text class="row-label">{{ $t('auth.email') }}</text>
    19|                <input class="row-input" v-model="email" :disabled="step === 2" :placeholder="$t('forgot.placeholder.email')" placeholder-class="input-placeholder" type="email" />
    20|            </view>
    21|            
    22|            <view class="input-row" v-if="step === 2">
    23|                <text class="row-label">{{ $t('forgot.placeholder.code') }}</text>
    24|                <input class="row-input" v-model="code" :placeholder="$t('forgot.placeholder.code')" placeholder-class="input-placeholder" type="number" maxlength="6" />
    25|            </view>
    26|            
    27|            <view class="input-row" v-if="step === 2">
    28|                <text class="row-label">{{ $t('auth.password') }}</text>
    29|                <input class="row-input" v-model="password" :placeholder="$t('forgot.placeholder.password')" placeholder-class="input-placeholder" type="password" />
    30|            </view>
    31|            
    32|            <view class="input-row" v-if="step === 2">
    33|                <text class="row-label">{{ $t('auth.confirmPassword') }}</text>
    34|                <input class="row-input" v-model="confirmPassword" :placeholder="$t('forgot.placeholder.confirmPassword')" placeholder-class="input-placeholder" type="password" />
    35|            </view>
    36|            
    37|            <button class="action-btn" v-if="step === 1" @click="sendCode" :loading="loading">
    38|                {{ $t('forgot.sendCode') }}
    39|            </button>
    40|            
    41|            <button class="action-btn" v-else @click="resetPassword" :loading="loading">
    42|                {{ $t('forgot.confirmReset') }}
    43|            </button>
    44|            
    45|            <view class="countdown" v-if="countdown > 0">
    46|                {{ $t('forgot.countdown', { seconds: countdown }) }}
    47|            </view>
    48|            
    49|            <view class="resend" v-else-if="step === 2" @click="sendCode">
    50|                <text class="link">{{ $t('forgot.resend') }}</text>
    51|            </view>
    52|        </view>
    53|    </view>
    54|</template>
    55|
    56|<script>
    57|import { authApi } from '@/utils/api';
    58|
    59|export default {
    60|    components: {
    61|        LangSwitch: () => import('@/components/LangSwitch.vue')
    62|    },
    63|    data() {
    64|        return {
    65|            step: 1,
    66|            email: '',
    67|            code: '',
    68|            password: '',
    69|            confirmPassword: '',
    70|            loading: false,
    71|            countdown: 0,
    72|            timer: null
    73|        };
    74|    },
    75|    methods: {
    76|        async sendCode() {
    77|            if (!this.email) {
    78|                uni.showToast({ title: this.$t('forgot.error.enterEmail'), icon: 'none' });
    79|                return;
    80|            }
    81|            const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    82|            if (!emailRe.test(this.email)) {
    83|                uni.showToast({ title: this.$t('forgot.error.emailFormat'), icon: 'none' });
    84|                return;
    85|            }
    86|            this.loading = true;
    87|            try {
    88|                await authApi.sendCode({ email: this.email });
    89|                this.step = 2;
    90|                this.startCountdown();
    91|                uni.showToast({ title: this.$t('forgot.codeSent'), icon: 'success' });
    92|            } catch (e) {
    93|                uni.showToast({ title: e.message || this.$t('forgot.error.sendFailed'), icon: 'none' });
    94|            } finally {
    95|                this.loading = false;
    96|            }
    97|        },
    98|        async resetPassword() {
    99|            if (!this.code || this.code.length < 6) {
   100|                uni.showToast({ title: this.$t('forgot.error.enterCode'), icon: 'none' });
   101|                return;
   102|            }
   103|            if (!this.password || this.password.length < 6) {
   104|                uni.showToast({ title: this.$t('forgot.error.passwordShort'), icon: 'none' });
   105|                return;
   106|            }
   107|            if (this.password !== this.confirmPassword) {
   108|                uni.showToast({ title: this.$t('forgot.error.mismatch'), icon: 'none' });
   109|                return;
   110|            }
   111|            this.loading = true;
   112|            try {
   113|                await authApi.resetPassword({
   114|                    email: this.email,
   115|                    code: this.code,
   116|                    password: this.password
   117|                });
   118|                uni.showToast({ title: this.$t('forgot.resetSuccess'), icon: 'success' });
   119|                setTimeout(() => {
   120|                    uni.navigateTo({ url: '/pages/auth/login' });
   121|                }, 1500);
   122|            } catch (e) {
   123|                uni.showToast({ title: e.message || this.$t('forgot.error.resetFailed'), icon: 'none' });
   124|            } finally {
   125|                this.loading = false;
   126|            }
   127|        },
   128|        startCountdown() {
   129|            this.countdown = 60;
   130|            this.timer = setInterval(() => {
   131|                this.countdown--;
   132|                if (this.countdown <= 0) {
   133|                    clearInterval(this.timer);
   134|                }
   135|            }, 1000);
   136|        },
   137|        goBack() {
   138|            if (this.timer) clearInterval(this.timer);
   139|            uni.navigateBack();
   140|        }
   141|    },
   142|    beforeDestroy() {
   143|        if (this.timer) clearInterval(this.timer);
   144|    }
   145|};
   146|</script>
   147|
   148|<style lang="scss">
   149|.page {
   150|    min-height: 100vh;
   151|    display: flex;
   152|    flex-direction: column;
   153|}
   154|
   155|.header {
   156|    display: flex;
   157|    align-items: center;
   158|    padding: 60rpx 40rpx 20rpx;
   159|}
   160|
   161|.back {
   162|    font-size: 60rpx;
   163|    color: $text-primary;
   164|    line-height: 40rpx;
   165|}
   166|
   167|.title {
   168|    flex: 1;
   169|    font-size: 36rpx;
   170|    font-weight: 600;
   171|    color: $text-primary;
   172|    text-align: center;
   173|}
   174|
   175|.form-section {
   176|    max-width: 640rpx;
   177|    margin: 20rpx auto;
   178|    width: calc(100% - 80rpx);
   179|}
   180|
   181|.step-desc {
   182|    font-size: 26rpx;
   183|    color: $text-secondary;
   184|    margin-bottom: 40rpx;
   185|    text-align: center;
   186|    line-height: 1.5;
   187|}
   188|
   189|.input-row {
   190|    display: flex !important;
   191|    align-items: center !important;
   192|    margin-bottom: 28rpx;
   193|}
   194|
   195|.row-label {
   196|    font-size: 28rpx;
   197|    color: $text-primary;
   198|    width: 140rpx !important;
   199|    min-width: 140rpx;
   200|    flex-shrink: 0;
   201|}
   202|
   203|.row-input {
   204|    flex: 1 !important;
   205|    background: $glass-card-bg !important;
   206|    border: 1rpx solid $glass-card-bg-hover !important;
   207|    border-radius: 16rpx !important;
   208|    padding: 20rpx 24rpx !important;
   209|    font-size: 28rpx !important;
   210|    color: $text-primary !important;
   211|    height: 80rpx !important;
   212|    backdrop-filter: blur(12px);
   213|    -webkit-backdrop-filter: blur(12px);
   214|}
   215|
   216|.input-placeholder {
   217|    color: $text-tertiary !important;
   218|}
   219|
   220|.action-btn {
   221|    width: 100%;
   222|    background: $primary-color !important;
   223|    color: $text-primary !important;
   224|    border: 1rpx solid $glass-border !important;
   225|    border-radius: 48rpx !important;
   226|    padding: 20rpx 48rpx !important;
   227|    font-size: 32rpx !important;
   228|    font-weight: 600 !important;
   229|    margin-top: 16rpx !important;
   230|    backdrop-filter: blur(12px);
   231|    -webkit-backdrop-filter: blur(12px);
   232|}
   233|
   234|.action-btn::after {
   235|    border: none !important;
   236|}
   237|
   238|.action-btn:active {
   239|    opacity: 0.85;
   240|}
   241|
   242|.countdown {
   243|    text-align: center;
   244|    font-size: 24rpx;
   245|    color: $text-secondary;
   246|    margin-top: 16rpx;
   247|}
   248|
   249|.resend {
   250|    text-align: right;
   251|    margin-top: 16rpx;
   252|}
   253|
   254|.link {
   255|    font-size: 24rpx;
   256|    color: $text-primary;
   257|    text-decoration: underline;
   258|}
   259|</style>
   260|