<template>
    <view class="gradient-bg register-page">
        <view class="header-bar">
            <text class="back-btn" @click="goBack">←</text>
            <text class="page-title">{{ $t('auth.register') }}</text>
            <text class="lang-btn" @click="toggleLang">{{ currentLang === 'zh' ? 'EN' : '中' }}</text>
        </view>
        
        <view class="logo-section">
            <text class="logo-text">💭</text>
            <text class="app-name">{{ $t('app.name') }}</text>
        </view>
        
        <view class="form-section">
            <view class="input-row">
                <text class="row-label">{{ $t('auth.username') }}</text>
                <input class="row-input" v-model="username" :placeholder="$t('auth.placeholder.username')" placeholder-class="input-placeholder" />
            </view>
            
            <view class="input-row">
                <text class="row-label">{{ $t('auth.nickname') }}</text>
                <input class="row-input" v-model="nickname" :placeholder="$t('auth.placeholder.nickname')" placeholder-class="input-placeholder" />
            </view>
            
            <view class="input-row">
                <text class="row-label">{{ $t('auth.email') }}</text>
                <input class="row-input" v-model="email" :placeholder="$t('auth.placeholder.email')" placeholder-class="input-placeholder" type="email" />
            </view>
            
            <view class="input-row">
                <text class="row-label">{{ $t('auth.phone') }}</text>
                <input class="row-input" v-model="phone" :placeholder="$t('auth.placeholder.phone')" placeholder-class="input-placeholder" type="number" maxlength="11" />
            </view>
            
            <view class="input-row">
                <text class="row-label">{{ $t('auth.password') }}</text>
                <input class="row-input" v-model="password" :placeholder="$t('auth.placeholder.password')" placeholder-class="input-placeholder" type="password" />
            </view>
            
            <view class="input-row">
                <text class="row-label">{{ $t('auth.confirmPassword') }}</text>
                <input class="row-input" v-model="confirmPassword" :placeholder="$t('auth.placeholder.confirmPassword')" placeholder-class="input-placeholder" type="password" />
            </view>
            
            <button class="register-btn" @click="handleRegister" :loading="loading">
                {{ $t('auth.register') }}
            </button>
            
            <view class="footer-link">
                <text>{{ $t('auth.hasAccount') }}</text>
                <text class="link" @click="goLogin">{{ $t('auth.loginNow') }}</text>
            </view>
        </view>
    </view>
</template>

<script>
import { authApi } from '@/utils/api';
import { setToken, setUserId } from '@/utils/auth';
import { setLocale } from '@/locale/index';

export default {
    computed: {
        currentLang() {
            return this.$i18n.locale;
        }
    },
    data() {
        return {
            username: '',
            nickname: '',
            email: '',
            phone: '',
            password: '',
            confirmPassword: '',
            loading: false
        };
    },
    methods: {
        toggleLang() {
            const next = this.currentLang === 'zh' ? 'en' : 'zh';
            setLocale(next);
        },
        async handleRegister() {
            if (!this.username || !this.nickname || !this.password) {
                uni.showToast({ title: this.$t('auth.fillAll'), icon: 'none' });
                return;
            }
            if (this.password !== this.confirmPassword) {
                uni.showToast({ title: this.$t('auth.passwordMismatch'), icon: 'none' });
                return;
            }
            if (this.password.length < 6) {
                uni.showToast({ title: this.$t('auth.passwordShort'), icon: 'none' });
                return;
            }
            this.loading = true;
            try {
                const res = await authApi.register({
                    username: this.username,
                    nickname: this.nickname,
                    password: this.password,
                    email: this.email,
                    phone: this.phone
                });
                setToken(res.token);
                setUserId(res.userId);
                uni.showToast({ title: this.$t('auth.registerSuccess'), icon: 'success' });
                setTimeout(() => {
                    uni.switchTab({ url: '/pages/explore/explore' });
                }, 1000);
            } catch (e) {
                console.error('Register failed:', e);
            } finally {
                this.loading = false;
            }
        },
        goBack() {
            uni.navigateBack();
        },
        goLogin() {
            uni.navigateBack();
        }
    }
};
</script>

<style lang="scss">
.register-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40rpx;
}

.header-bar {
    width: 100%;
    max-width: 640rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
}

.back-btn {
    font-size: 40rpx;
    color: #FFFFFF;
}

.page-title {
    font-size: 32rpx;
    color: #FFFFFF;
    font-weight: 600;
}

.lang-btn {
    font-size: 26rpx;
    color: #FFFFFF;
    opacity: 0.8;
    padding: 8rpx 16rpx;
    border: 2rpx solid rgba(255,255,255,0.4);
    border-radius: 24rpx;
}

.logo-section {
    text-align: center;
    margin-bottom: 40rpx;
}

.logo-text {
    font-size: 80rpx;
    display: block;
    margin-bottom: 12rpx;
}

.app-name {
    font-size: 36rpx;
    font-weight: 700;
    color: #FFFFFF;
    display: block;
}

.form-section {
    width: 100%;
    max-width: 640rpx;
}

.input-row {
    display: flex !important;
    align-items: center !important;
    margin-bottom: 28rpx;
    white-space: nowrap;
}

.row-label {
    font-size: 28rpx;
    color: #FFFFFF;
    width: 120rpx !important;
    min-width: 120rpx;
    flex-shrink: 0;
    display: inline-block;
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
    min-width: 0;
}

.input-placeholder {
    color: rgba(255,255,255,0.45) !important;
}

.register-btn {
    width: 100%;
    background: linear-gradient(135deg, #FFFFFF 0%, #E8E0FF 100%) !important;
    color: #6C5CE7 !important;
    border: none !important;
    border-radius: 48rpx !important;
    padding: 20rpx 48rpx !important;
    font-size: 32rpx !important;
    font-weight: 600 !important;
    letter-spacing: 8rpx !important;
    margin-top: 8rpx !important;
}

.register-btn::after {
    border: none !important;
}

.register-btn:active {
    opacity: 0.85;
}

.footer-link {
    text-align: center;
    margin-top: 40rpx;
    font-size: 26rpx;
    color: rgba(255,255,255,0.7);
}

.link {
    color: #FFFFFF !important;
    font-weight: 600 !important;
    margin-left: 8rpx !important;
    text-decoration: underline !important;
}
</style>
