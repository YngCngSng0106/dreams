<template>
    <view class="gradient-bg register-page">
        <view class="header-bar">
            <text class="back-btn" @click="goBack">←</text>
            <text class="page-title">注册账号</text>
            <view style="width:48rpx"></view>
        </view>
        
        <view class="logo-section">
            <text class="logo-text">💭</text>
            <text class="app-name">梦境分享</text>
        </view>
        
        <view class="form-section card">
            <view class="input-group">
                <text class="label">用户名</text>
                <input v-model="username" placeholder="请输入用户名" class="input-field" />
            </view>
            
            <view class="input-group">
                <text class="label">昵称</text>
                <input v-model="nickname" placeholder="请输入昵称" class="input-field" />
            </view>
            
            <view class="input-group">
                <text class="label">密码</text>
                <input v-model="password" type="password" placeholder="请输入密码" class="input-field" />
            </view>
            
            <view class="input-group">
                <text class="label">确认密码</text>
                <input v-model="confirmPassword" type="password" placeholder="请再次输入密码" class="input-field" />
            </view>
            
            <button class="btn-primary register-btn" @click="handleRegister" :loading="loading">
                注 册
            </button>
            
            <view class="footer-link">
                <text>已有账号？</text>
                <text class="link" @click="goLogin">立即登录</text>
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
            nickname: '',
            password: '',
            confirmPassword: '',
            loading: false
        };
    },
    methods: {
        async handleRegister() {
            if (!this.username || !this.nickname || !this.password) {
                uni.showToast({ title: '请填写完整信息', icon: 'none' });
                return;
            }
            if (this.password !== this.confirmPassword) {
                uni.showToast({ title: '两次密码不一致', icon: 'none' });
                return;
            }
            if (this.password.length < 6) {
                uni.showToast({ title: '密码至少6位', icon: 'none' });
                return;
            }
            this.loading = true;
            try {
                const res = await authApi.register({
                    username: this.username,
                    nickname: this.nickname,
                    password: this.password
                });
                setToken(res.token);
                setUserId(res.userId);
                uni.showToast({ title: '注册成功', icon: 'success' });
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

<style lang="scss" scoped>
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
    
    .back-btn {
        font-size: 40rpx;
        color: #FFFFFF;
    }
    
    .page-title {
        font-size: 32rpx;
        color: #FFFFFF;
        font-weight: 600;
    }
}

.logo-section {
    text-align: center;
    margin-bottom: 40rpx;
    
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
}

.form-section {
    width: 100%;
    max-width: 640rpx;
}

.input-group {
    margin-bottom: 28rpx;
    
    .label {
        font-size: 26rpx;
        color: #636E72;
        margin-bottom: 10rpx;
        display: block;
    }
}

.input-field {
    background: rgba(255,255,255,0.9);
    border: none;
    border-radius: 16rpx;
    padding: 18rpx 24rpx;
    font-size: 28rpx;
    color: #2D3436;
}

.register-btn {
    width: 100%;
    margin-top: 16rpx;
    font-size: 32rpx;
    letter-spacing: 8rpx;
}

.footer-link {
    text-align: center;
    margin-top: 28rpx;
    font-size: 26rpx;
    color: rgba(255,255,255,0.7);
    
    .link {
        color: #FFFFFF;
        font-weight: 600;
        margin-left: 8rpx;
    }
}
</style>
