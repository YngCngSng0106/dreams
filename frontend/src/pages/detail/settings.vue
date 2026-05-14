<template>
    <view class="settings-page">
        <view class="header-right">
            <text class="lang-btn" @click="toggleLang">{{ currentLang === 'zh' ? 'EN' : '中' }}</text>
        </view>
        <view class="section">
            <text class="section-title">{{ $t('detail.accountSettings') }}</text>
            <view class="settings-card card">
                <view class="setting-item" @click="editProfile">
                    <text class="setting-label">{{ $t('detail.personalInfo') }}</text>
                    <text class="setting-value">{{ profile.nickname || '' }}</text>
                    <text class="arrow">›</text>
                </view>
                <view class="setting-item" @click="changePassword">
                    <text class="setting-label">{{ $t('detail.changePass') }}</text>
                    <text class="arrow">›</text>
                </view>
            </view>
        </view>
        
        <view class="section">
            <text class="section-title">{{ $t('detail.appSettings') }}</text>
            <view class="settings-card card">
                <view class="setting-item">
                    <text class="setting-label">{{ $t('detail.pushNotif') }}</text>
                    <switch :checked="settings.pushEnabled === 1" @change="togglePush" color="#6C5CE7" />
                </view>
                <view class="setting-item">
                    <text class="setting-label">{{ $t('detail.anonMode') }}</text>
                    <switch :checked="settings.isAnonymousEnabled === 1" @change="toggleAnonymous" color="#6C5CE7" />
                </view>
            </view>
        </view>
        
        <view class="section">
            <text class="section-title">{{ $t('detail.aboutSection') }}</text>
            <view class="settings-card card">
                <view class="setting-item">
                    <text class="setting-label">{{ $t('detail.version') }}</text>
                    <text class="setting-value">v0.1.0</text>
                </view>
                <view class="setting-item">
                    <text class="setting-label">{{ $t('detail.projectName') }}</text>
                    <text class="setting-value">{{ $t('app.name') }}</text>
                </view>
            </view>
        </view>
        
        <view class="danger-section">
            <button class="danger-btn" @click="deleteAccount">{{ $t('detail.deleteAccount') }}</button>
        </view>
    </view>
</template>

<script>
import { userApi, settingsApi } from '@/utils/api';
import { requireLogin, clearAuth } from '@/utils/auth';
import { setLocale } from '@/locale';

export default {
    data() {
        return {
            profile: {},
            settings: { pushEnabled: true, isAnonymousEnabled: false },
            currentLang: uni.getStorageSync('locale') || 'zh'
        };
    },
    onLoad() {
        if (!requireLogin()) return;
        this.loadData();
    },
    methods: {
        toggleLang() {
            const next = this.currentLang === 'zh' ? 'en' : 'zh';
            this.currentLang = next;
            setLocale(next);
        },
        async loadData() {
            try {
                this.profile = await userApi.getMyProfile();
                this.settings = await settingsApi.getSettings();
            } catch (e) {
                console.error('Load settings failed:', e);
            }
        },
        
        editProfile() {
            uni.showToast({ title: this.$t('detail.inDev'), icon: 'none' });
        },
        
        async changePassword() {
            const oldPassword = await this.inputModal(this.$t('detail.changePass'), this.$t('detail.oldPass'));
            if (!oldPassword) return;
            const newPassword = await this.inputModal(this.$t('detail.changePass'), this.$t('detail.newPass'), true);
            if (!newPassword) return;
            const confirm = await this.inputModal(this.$t('detail.changePass'), this.$t('detail.confirmPass'), true);
            if (newPassword !== confirm) {
                uni.showToast({ title: this.$t('detail.passMismatch'), icon: 'none' });
                return;
            }
            try {
                await settingsApi.changePassword({ oldPassword, newPassword: confirm });
                uni.showToast({ title: this.$t('detail.passChanged'), icon: 'success' });
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
            this.settings.pushEnabled = e.detail.value ? 1 : 0;
            try {
                await settingsApi.updateSettings(this.settings);
            } catch (err) {
                this.settings.pushEnabled = !this.settings.pushEnabled ? 1 : 0;
            }
        },
        
        async toggleAnonymous(e) {
            this.settings.isAnonymousEnabled = e.detail.value ? 1 : 0;
            try {
                await settingsApi.updateSettings(this.settings);
            } catch (err) {
                this.settings.isAnonymousEnabled = !this.settings.isAnonymousEnabled ? 1 : 0;
            }
        },
        
        deleteAccount() {
            uni.showModal({
                title: this.$t('detail.deleteWarning'),
                content: this.$t('detail.deleteDataNotice'),
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
    position: relative;
}

.header-right {
    position: fixed;
    top: calc(24rpx + env(safe-area-inset-top));
    right: 24rpx;
    z-index: 10;
}

.lang-btn {
    font-size: 26rpx;
    color: rgba(255,255,255,0.8);
    background: rgba(0,0,0,0.2);
    padding: 8rpx 16rpx;
    border-radius: 16rpx;
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