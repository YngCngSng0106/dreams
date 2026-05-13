<template>
    <view class="gradient-bg page">
        <view class="header">
            <text class="back" @click="goBack">‹</text>
            <text class="title">{{ $t('forgot.title') }}</text>
            <text class="lang-btn" @click="toggleLang">{{ currentLang === 'zh' ? 'EN' : '中' }}</text>
        </view>
        
        <view class="form-section">
            <view class="step-desc" v-if="step === 1">
                {{ $t('forgot.step1') }}
            </view>
            <view class="step-desc" v-else>
                {{ $t('forgot.step2', { email: email }) }}
            </view>
            
            <view class="input-row">
                <text class="row-label">{{ $t('auth.email') }}</text>
                <input class="row-input" v-model="email" :disabled="step === 2" :placeholder="$t('forgot.placeholder.email')" placeholder-class="input-placeholder" type="email" />
            </view>
            
            <view class="input-row" v-if="step === 2">
                <text class="row-label">{{ $t('forgot.placeholder.code') }}</text>
                <input class="row-input" v-model="code" :placeholder="$t('forgot.placeholder.code')" placeholder-class="input-placeholder" type="number" maxlength="6" />
            </view>
            
            <view class="input-row" v-if="step === 2">
                <text class="row-label">{{ $t('auth.password') }}</text>
                <input class="row-input" v-model="password" :placeholder="$t('forgot.placeholder.password')" placeholder-class="input-placeholder" type="password" />
            </view>
            
            <view class="input-row" v-if="step === 2">
                <text class="row-label">{{ $t('auth.confirmPassword') }}</text>
                <input class="row-input" v-model="confirmPassword" :placeholder="$t('forgot.placeholder.confirmPassword')" placeholder-class="input-placeholder" type="password" />
            </view>
            
            <button class="action-btn" v-if="step === 1" @click="sendCode" :loading="loading">
                {{ $t('forgot.sendCode') }}
            </button>
            
            <button class="action-btn" v-else @click="resetPassword" :loading="loading">
                {{ $t('forgot.confirmReset') }}
            </button>
            
            <view class="countdown" v-if="countdown > 0">
                {{ $t('forgot.countdown', { seconds: countdown }) }}
            </view>
            
            <view class="resend" v-else-if="step === 2" @click="sendCode">
                <text class="link">{{ $t('forgot.resend') }}</text>
            </view>
        </view>
    </view>
</template>

<script>
import { authApi } from '@/utils/api';
import { setLocale } from '@/locale/index';

export default {
    computed: {
        currentLang() {
            return this.$i18n.locale;
        }
    },
    data() {
        return {
            step: 1,
            email: '',
            code: '',
            password: '',
            confirmPassword: '',
            loading: false,
            countdown: 0,
            timer: null
        };
    },
    methods: {
        toggleLang() {
            const next = this.currentLang === 'zh' ? 'en' : 'zh';
            setLocale(next);
        },
        async sendCode() {
            if (!this.email) {
                uni.showToast({ title: this.$t('forgot.error.enterEmail'), icon: 'none' });
                return;
            }
            const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRe.test(this.email)) {
                uni.showToast({ title: this.$t('forgot.error.emailFormat'), icon: 'none' });
                return;
            }
            this.loading = true;
            try {
                await authApi.sendCode({ email: this.email });
                this.step = 2;
                this.startCountdown();
                uni.showToast({ title: this.$t('forgot.codeSent'), icon: 'success' });
            } catch (e) {
                uni.showToast({ title: e.message || this.$t('forgot.error.sendFailed'), icon: 'none' });
            } finally {
                this.loading = false;
            }
        },
        async resetPassword() {
            if (!this.code || this.code.length < 6) {
                uni.showToast({ title: this.$t('forgot.error.enterCode'), icon: 'none' });
                return;
            }
            if (!this.password || this.password.length < 6) {
                uni.showToast({ title: this.$t('forgot.error.passwordShort'), icon: 'none' });
                return;
            }
            if (this.password !== this.confirmPassword) {
                uni.showToast({ title: this.$t('forgot.error.mismatch'), icon: 'none' });
                return;
            }
            this.loading = true;
            try {
                await authApi.resetPassword({
                    email: this.email,
                    code: this.code,
                    password: this.password
                });
                uni.showToast({ title: this.$t('forgot.resetSuccess'), icon: 'success' });
                setTimeout(() => {
                    uni.navigateTo({ url: '/pages/auth/login' });
                }, 1500);
            } catch (e) {
                uni.showToast({ title: e.message || this.$t('forgot.error.resetFailed'), icon: 'none' });
            } finally {
                this.loading = false;
            }
        },
        startCountdown() {
            this.countdown = 60;
            this.timer = setInterval(() => {
                this.countdown--;
                if (this.countdown <= 0) {
                    clearInterval(this.timer);
                }
            }, 1000);
        },
        goBack() {
            if (this.timer) clearInterval(this.timer);
            uni.navigateBack();
        }
    },
    beforeDestroy() {
        if (this.timer) clearInterval(this.timer);
    }
};
</script>

<style lang="scss">
.page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

.header {
    display: flex;
    align-items: center;
    padding: 60rpx 40rpx 20rpx;
}

.back {
    font-size: 60rpx;
    color: #FFFFFF;
    line-height: 40rpx;
}

.title {
    flex: 1;
    font-size: 36rpx;
    font-weight: 600;
    color: #FFFFFF;
    text-align: center;
}

.lang-btn {
    font-size: 24rpx;
    color: #FFFFFF;
    opacity: 0.8;
    padding: 6rpx 14rpx;
    border: 2rpx solid rgba(255,255,255,0.4);
    border-radius: 20rpx;
}

.form-section {
    max-width: 640rpx;
    margin: 20rpx auto;
    width: calc(100% - 80rpx);
}

.step-desc {
    font-size: 26rpx;
    color: rgba(255,255,255,0.7);
    margin-bottom: 40rpx;
    text-align: center;
    line-height: 1.5;
}

.input-row {
    display: flex !important;
    align-items: center !important;
    margin-bottom: 28rpx;
}

.row-label {
    font-size: 28rpx;
    color: #FFFFFF;
    width: 140rpx !important;
    min-width: 140rpx;
    flex-shrink: 0;
}

.row-input {
    flex: 1 !important;
    background: rgba(255,255,255,0.15) !important;
    border: 2rpx solid rgba(255,255,255,0.3) !important;
    border-radius: 16rpx !important;
    padding: 20rpx 24rpx !important;
    font-size: 28rpx !important;
    color: #FFFFFF !important;
    height: 80rpx !important;
}

.input-placeholder {
    color: rgba(255,255,255,0.45) !important;
}

.action-btn {
    width: 100%;
    background: linear-gradient(135deg, #FFFFFF 0%, #E8E0FF 100%) !important;
    color: #6C5CE7 !important;
    border: none !important;
    border-radius: 48rpx !important;
    padding: 20rpx 48rpx !important;
    font-size: 32rpx !important;
    font-weight: 600 !important;
    margin-top: 16rpx !important;
}

.action-btn::after {
    border: none !important;
}

.action-btn:active {
    opacity: 0.85;
}

.countdown {
    text-align: center;
    font-size: 24rpx;
    color: rgba(255,255,255,0.6);
    margin-top: 16rpx;
}

.resend {
    text-align: right;
    margin-top: 16rpx;
}

.link {
    font-size: 24rpx;
    color: rgba(255,255,255,0.8);
    text-decoration: underline;
}
</style>
