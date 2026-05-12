<template>
    <view class="gradient-bg login-page">
        <view class="logo-section">
            <text class="logo-text">💭</text>
            <text class="app-name">梦境分享</text>
            <text class="app-slogan">分享你的梦，遇见相似的梦</text>
        </view>
        
        <view class="form-section card">
            <view class="input-group">
                <text class="label">用户名</text>
                <input v-model="username" placeholder="请输入用户名" class="input-field" />
            </view>
            
            <view class="input-group">
                <text class="label">密码</text>
                <input v-model="password" type="password" placeholder="请输入密码" class="input-field" />
            </view>
            
            <button class="btn-primary login-btn" @click="handleLogin" :loading="loading">
                登 录
            </button>
            
            <view class="footer-link">
                <text>还没有账号？</text>
                <text class="link" @click="goRegister">立即注册</text>
            </view>
        </view>
    </view>
</template>

<script>
import { authApi } from '@/utils/api';
import { setToken, setUserId } from '@/utils/auth';

export default {
    data() {
        return {
            username: '',
            password: '',
            loading: false
        };
    },
    methods: {
        async handleLogin() {
            if (!this.username || !this.password) {
                uni.showToast({ title: '请填写完整信息', icon: 'none' });
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
                uni.showToast({ title: '登录成功', icon: 'success' });
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
        }
    }
};
</script>

<style lang="scss" scoped>
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

.input-group {
    margin-bottom: 32rpx;
    
    .label {
        font-size: 26rpx;
        color: #636E72;
        margin-bottom: 12rpx;
        display: block;
    }
}

.input-field {
    background: rgba(255,255,255,0.9);
    border: none;
    border-radius: 16rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    color: #2D3436;
}

.login-btn {
    width: 100%;
    margin-top: 24rpx;
    font-size: 32rpx;
    letter-spacing: 8rpx;
}

.footer-link {
    text-align: center;
    margin-top: 32rpx;
    font-size: 26rpx;
    color: rgba(255,255,255,0.7);
    
    .link {
        color: #FFFFFF;
        font-weight: 600;
        margin-left: 8rpx;
    }
}
</style>
