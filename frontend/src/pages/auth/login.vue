<template>
    <view class="gradient-bg login-page">
        <view class="header-bar">
            <text class="lang-btn" @click="toggleLang">{{ currentLang === 'zh' ? 'EN' : '中' }}</text>
        </view>
        <view class="logo-section">
            <text class="logo-text">💭</text>
            <text class="app-name">{{ $t('app.name') }}</text>
            <text class="app-slogan">{{ $t('app.slogan') }}</text>
        </view>
        
        <view class="form-section">
            <view class="input-row">
                <text class="label">{{ $t('auth.username') }}</text>
                <input v-model="username" :placeholder="$t('auth.placeholder.username')" placeholder-class="input-placeholder" class="input-field" />
            </view>
            
            <view class="input-row">
                <text class="label">{{ $t('auth.password') }}</text>
                <input v-model="password" type="password" :placeholder="$t('auth.placeholder.password')" placeholder-class="input-placeholder" class="input-field" />
            </view>
            
            <view class="forgot-row" @click="goForgot">
                <text class="forgot-link">{{ $t('auth.forgotPassword') }}</text>
            </view>
            
            <button class="login-btn" @click="handleLogin" :loading="loading">
                {{ $t('auth.login') }}
            </button>
            
            <view class="footer-link">
                <text>{{ $t('auth.noAccount') }}</text>
                <text class="link" @click="goRegister">{{ $t('auth.registerNow') }}</text>
            </view>
        </view>
    </view>
</template>

<script>
import { authApi } from '@/utils/api';
import { setToken, setUserId } from '@/utils/auth';
import { setLocale } from '@/locale';

export default {
    data() {
        return {
            username: '',
            password: '',
            loading: false,
            currentLang: uni.getStorageSync('locale') || 'zh'
        };
    },
    methods: {
        toggleLang() {
            const next = this.currentLang === 'zh' ? 'en' : 'zh';
            this.currentLang = next;
            setLocale(next);
        },
        async handleLogin() {
            if (!this.username || !this.password) {
                uni.showToast({ title: this.$t('auth.fillAll'), icon: 'none' });
                return;
            }
            this.loading = true;
            try {
                const res = await authApi.login({
                    username: this.username,
                    password: this.password
                });
                setToken(res.token);
                setUserId(res.userId);
                uni.showToast({ title: this.$t('auth.loginSuccess'), icon: 'success' });
                setTimeout(() => {
                    uni.switchTab({ url: '/pages/explore/explore' });
                }, 1000);
            } catch (e) {
                console.error('Login failed:', e);
            } finally {
                this.loading = false;
            }
        },
        goRegister() {
            uni.navigateTo({ url: '/pages/auth/register' });
        },
        goForgot() {
            uni.navigateTo({ url: '/pages/auth/forgot-password' });
        }
    }
};
</script>

<style lang="scss" scoped>
.header-bar {
    width: 100%;
    display: flex;
    justify-content: flex-end;
    padding: 20rpx 40rpx;
}

.lang-btn {
    font-size: 28rpx;
    color: rgba(255,255,255,0.8);
    padding: 10rpx 20rpx;
}

.login-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40rpx;
}

.logo-section {
    text-align: center;
    margin-bottom: 60rpx;
    
    .logo-text {
        font-size: 120rpx;
        display: block;
        margin-bottom: 20rpx;
    }
    
    .app-name {
        font-size: 48rpx;
        font-weight: 700;
        color: #FFFFFF;
        display: block;
        margin-bottom: 16rpx;
    }
    
    .app-slogan {
        font-size: 26rpx;
        color: rgba(255,255,255,0.7);
        display: block;
    }
}

.form-section {
    width: 100%;
    max-width: 640rpx;
}

.input-row {
    display: flex;
    align-items: center;
    margin-bottom: 32rpx;
    
    .label {
        font-size: 28rpx;
        color: #FFFFFF;
        width: 120rpx;
        flex-shrink: 0;
    }
    
    .input-field {
        flex: 1;
        background: rgba(255,255,255,0.15);
        border: 2rpx solid rgba(255,255,255,0.3);
        border-radius: 16rpx;
        padding: 20rpx 24rpx;
        font-size: 28rpx;
        color: #FFFFFF;
        height: 80rpx;
    }
}

.input-placeholder {
    color: rgba(255,255,255,0.45) !important;
}

.forgot-row {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 24rpx;
    padding-right: 8rpx;
    
    .forgot-link {
        font-size: 26rpx;
        color: #FFFFFF;
        text-decoration: underline;
        opacity: 0.9;
    }
}

.login-btn {
    width: 100%;
    background: linear-gradient(135deg, #FFFFFF 0%, #E8E0FF 100%);
    color: #6C5CE7;
    border: none;
    border-radius: 48rpx;
    padding: 20rpx 48rpx;
    font-size: 32rpx;
    font-weight: 600;
    letter-spacing: 8rpx;
    
    &::after {
        border: none;
    }
    
    &:active {
        opacity: 0.85;
    }
}

.footer-link {
    text-align: center;
    margin-top: 40rpx;
    font-size: 26rpx;
    color: rgba(255,255,255,0.7);
    
    .link {
        color: #FFFFFF;
        font-weight: 600;
        margin-left: 8rpx;
        text-decoration: underline;
    }
}
</style>
