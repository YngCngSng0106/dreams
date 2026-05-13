<template>
    <view class="settings-page">
        <view class="section">
            <text class="section-title">账号设置</text>
            <view class="settings-card card">
                <view class="setting-item" @click="editProfile">
                    <text class="setting-label">个人信息</text>
                    <text class="setting-value">{{ profile.nickname || '' }}</text>
                    <text class="arrow">›</text>
                </view>
                <view class="setting-item" @click="changePassword">
                    <text class="setting-label">修改密码</text>
                    <text class="arrow">›</text>
                </view>
            </view>
        </view>
        
        <view class="section">
            <text class="section-title">应用设置</text>
            <view class="settings-card card">
                <view class="setting-item">
                    <text class="setting-label">推送通知</text>
                    <switch :checked="settings.pushEnabled" @change="togglePush" color="#6C5CE7" />
                </view>
                <view class="setting-item">
                    <text class="setting-label">匿名模式</text>
                    <switch :checked="settings.isAnonymousEnabled" @change="toggleAnonymous" color="#6C5CE7" />
                </view>
            </view>
        </view>
        
        <view class="section">
            <text class="section-title">关于</text>
            <view class="settings-card card">
                <view class="setting-item">
                    <text class="setting-label">版本</text>
                    <text class="setting-value">v0.1.0</text>
                </view>
                <view class="setting-item">
                    <text class="setting-label">项目名称</text>
                    <text class="setting-value">梦境分享</text>
                </view>
            </view>
        </view>
        
        <view class="danger-section">
            <button class="danger-btn" @click="deleteAccount">注销账号</button>
        </view>
    </view>
</template>

<script>
import { userApi, settingsApi } from '@/utils/api';
import { requireLogin, clearAuth } from '@/utils/auth';

export default {
    data() {
        return {
            profile: {},
            settings: { pushEnabled: true, isAnonymousEnabled: false }
        };
    },
    onLoad() {
        if (!requireLogin()) return;
        this.loadData();
    },
    methods: {
        async loadData() {
            try {
                this.profile = await userApi.getMyProfile();
                this.settings = await settingsApi.getSettings();
            } catch (e) {
                console.error('Load settings failed:', e);
            }
        },
        
        editProfile() {
            uni.showToast({ title: '开发中', icon: 'none' });
        },
        
        async changePassword() {
            const oldPassword = await this.inputModal('修改密码', '原密码');
            if (!oldPassword) return;
            const newPassword = await this.inputModal('修改密码', '新密码', true);
            if (!newPassword) return;
            const confirm = await this.inputModal('修改密码', '确认新密码', true);
            if (newPassword !== confirm) {
                uni.showToast({ title: '两次密码不一致', icon: 'none' });
                return;
            }
            try {
                await settingsApi.changePassword({ oldPassword, newPassword: confirm });
                uni.showToast({ title: '密码修改成功', icon: 'success' });
            } catch (e) {
                console.error('Change password failed:', e);
            }
        },
        
        inputModal(title, placeholder, password = false) {
            return new Promise((resolve) => {
                uni.showModal({
                    title,
                    content: '',
                    editable: true,
                    placeholder,
                    success: (res) => { resolve(res.confirm ? res.content : null); }
                });
            });
        },
        
        async togglePush(e) {
            this.settings.pushEnabled = e.detail.value;
            try {
                await settingsApi.updateSettings(this.settings);
            } catch (err) {
                this.settings.pushEnabled = !this.settings.pushEnabled;
            }
        },
        
        async toggleAnonymous(e) {
            this.settings.isAnonymousEnabled = e.detail.value;
            try {
                await settingsApi.updateSettings(this.settings);
            } catch (err) {
                this.settings.isAnonymousEnabled = !this.settings.isAnonymousEnabled;
            }
        },
        
        deleteAccount() {
            uni.showModal({
                title: '警告',
                content: '注销后所有数据将永久删除，确定要注销账号吗？',
                success: async (res) => {
                    if (res.confirm) {
                        try {
                            await settingsApi.deleteAccount();
                            clearAuth();
                            uni.redirectTo({ url: '/pages/auth/login' });
                        } catch (e) {
                            console.error('Delete account failed:', e);
                        }
                    }
                }
            });
        }
    }
};
</script>

<style lang="scss" scoped>
.settings-page {
    min-height: 100vh;
    background: #F8F9FE;
    padding: 24rpx;
    padding-top: calc(24rpx + env(safe-area-inset-top));
}

.section {
    margin-bottom: 32rpx;
    
    .section-title {
        font-size: 26rpx;
        color: #636E72;
        margin-bottom: 12rpx;
        padding: 0 8rpx;
        display: block;
    }
}

.settings-card {
    .setting-item {
        display: flex;
        align-items: center;
        padding: 24rpx;
        border-bottom: 2rpx solid #F0F0F0;
        
        &:last-child { border-bottom: none; }
        
        .setting-label {
            flex: 1;
            font-size: 28rpx;
            color: #2D3436;
        }
        
        .setting-value {
            font-size: 26rpx;
            color: #636E72;
            margin-right: 8rpx;
        }
        
        .arrow {
            font-size: 28rpx;
            color: #D0D0D0;
            margin-left: 8rpx;
        }
    }
}

.danger-section {
    padding: 40rpx 0;
    
    .danger-btn {
        background: #FFFFFF;
        color: #E17055;
        border: 2rpx solid #E17055;
        border-radius: 48rpx;
        font-size: 28rpx;
    }
}
</style>
