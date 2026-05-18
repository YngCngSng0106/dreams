     1|<template>
     2|    <view class="settings-page">
     3|        <view class="header-right">
     4|            <LangSwitch />
     5|        </view>
     6|        <view class="section">
     7|            <text class="section-title">{{ $t('detail.accountSettings') }}</text>
     8|            <view class="settings-card card">
     9|                <view class="setting-item" @click="editProfile">
    10|                    <text class="setting-label">{{ $t('detail.personalInfo') }}</text>
    11|                    <text class="setting-value">{{ profile.nickname || '' }}</text>
    12|                    <text class="arrow">›</text>
    13|                </view>
    14|                <view class="setting-item" @click="changePassword">
    15|                    <text class="setting-label">{{ $t('detail.changePass') }}</text>
    16|                    <text class="arrow">›</text>
    17|                </view>
    18|            </view>
    19|        </view>
    20|        
    21|        <view class="section">
    22|            <text class="section-title">{{ $t('detail.appSettings') }}</text>
    23|            <view class="settings-card card">
    24|                <view class="setting-item">
    25|                    <text class="setting-label">{{ $t('detail.pushNotif') }}</text>
    26|                    <switch :checked="settings.pushEnabled === 1" @change="togglePush" color="#6C5CE7" />
    27|                </view>
    28|                <view class="setting-item">
    29|                    <text class="setting-label">{{ $t('detail.anonMode') }}</text>
    30|                    <switch :checked="settings.isAnonymousEnabled === 1" @change="toggleAnonymous" color="#6C5CE7" />
    31|                </view>
    32|            </view>
    33|        </view>
    34|        
    35|        <view class="section">
    36|            <text class="section-title">{{ $t('detail.aboutSection') }}</text>
    37|            <view class="settings-card card">
    38|                <view class="setting-item">
    39|                    <text class="setting-label">{{ $t('detail.version') }}</text>
    40|                    <text class="setting-value">v0.1.0</text>
    41|                </view>
    42|                <view class="setting-item">
    43|                    <text class="setting-label">{{ $t('detail.projectName') }}</text>
    44|                    <text class="setting-value">{{ $t('app.name') }}</text>
    45|                </view>
    46|            </view>
    47|        </view>
    48|        
    49|        <view class="danger-section">
    50|            <button class="danger-btn" @click="deleteAccount">{{ $t('detail.deleteAccount') }}</button>
    51|        </view>
    52|    </view>
    53|</template>
    54|
    55|<script>
    56|import { userApi, settingsApi } from '@/utils/api';
    57|import { requireLogin, clearAuth } from '@/utils/auth';
    58|import { useSettingsStore } from '@/store/settings';
    59|
    60|export default {
    61|    components: {
    62|        LangSwitch: () => import('@/components/LangSwitch.vue')
    63|    },
    64|    setup() {
    65|        const settingsStore = useSettingsStore();
    66|        return { settingsStore };
    67|    },
    68|    data() {
    69|        return {
    70|            profile: {},
    71|            settings: { pushEnabled: true, isAnonymousEnabled: false }
    72|        };
    73|    },
    74|    onLoad() {
    75|        if (!requireLogin()) return;
    76|        this.loadData();
    77|    },
    78|    methods: {
    79|        async loadData() {
    80|            try {
    81|                this.profile = await userApi.getMyProfile();
    82|                this.settings = await settingsApi.getSettings();
    83|            } catch (e) {
    84|                console.error('Load settings failed:', e);
    85|            }
    86|        },
    87|        
    88|        editProfile() {
    89|            uni.showToast({ title: this.$t('detail.inDev'), icon: 'none' });
    90|        },
    91|        
    92|        async changePassword() {
    93|            const oldPassword = await this.inputModal(this.$t('detail.changePass'), this.$t('detail.oldPass'));
    94|            if (!oldPassword) return;
    95|            const newPassword = await this.inputModal(this.$t('detail.changePass'), this.$t('detail.newPass'), true);
    96|            if (!newPassword) return;
    97|            const confirm = await this.inputModal(this.$t('detail.changePass'), this.$t('detail.confirmPass'), true);
    98|            if (newPassword !== confirm) {
    99|                uni.showToast({ title: this.$t('detail.passMismatch'), icon: 'none' });
   100|                return;
   101|            }
   102|            try {
   103|                await settingsApi.changePassword({ oldPassword, newPassword: confirm });
   104|                uni.showToast({ title: this.$t('detail.passChanged'), icon: 'success' });
   105|            } catch (e) {
   106|                console.error('Change password failed:', e);
   107|            }
   108|        },
   109|        
   110|        inputModal(title, placeholder, password = false) {
   111|            return new Promise((resolve) => {
   112|                uni.showModal({
   113|                    title,
   114|                    content: '',
   115|                    editable: true,
   116|                    placeholder,
   117|                    success: (res) => { resolve(res.confirm ? res.content : null); }
   118|                });
   119|            });
   120|        },
   121|        
   122|        async togglePush(e) {
   123|            this.settings.pushEnabled = e.detail.value ? 1 : 0;
   124|            try {
   125|                await settingsApi.updateSettings(this.settings);
   126|            } catch (err) {
   127|                this.settings.pushEnabled = !this.settings.pushEnabled ? 1 : 0;
   128|            }
   129|        },
   130|        
   131|        async toggleAnonymous(e) {
   132|            this.settings.isAnonymousEnabled = e.detail.value ? 1 : 0;
   133|            try {
   134|                await settingsApi.updateSettings(this.settings);
   135|            } catch (err) {
   136|                this.settings.isAnonymousEnabled = !this.settings.isAnonymousEnabled ? 1 : 0;
   137|            }
   138|        },
   139|        
   140|        deleteAccount() {
   141|            uni.showModal({
   142|                title: this.$t('detail.deleteWarning'),
   143|                content: this.$t('detail.deleteDataNotice'),
   144|                success: async (res) => {
   145|                    if (res.confirm) {
   146|                        try {
   147|                            await settingsApi.deleteAccount();
   148|                            clearAuth();
   149|                            uni.redirectTo({ url: '/pages/auth/login' });
   150|                        } catch (e) {
   151|                            console.error('Delete account failed:', e);
   152|                        }
   153|                    }
   154|                }
   155|            });
   156|        }
   157|    }
   158|};
   159|</script>
   160|
   161|<style lang="scss" scoped>
   162|.settings-page {
   163|    min-height: 100vh;
   164|    padding: 24rpx;
   165|    padding-top: calc(24rpx + env(safe-area-inset-top));
   166|    position: relative;
   167|}
   168|
   169|.header-right {
   170|    position: fixed;
   171|    top: calc(24rpx + env(safe-area-inset-top));
   172|    right: 24rpx;
   173|    z-index: 10;
   174|}
   175|
   176|.section {
   177|    margin-bottom: 32rpx;
   178|    
   179|    .section-title {
   180|        font-size: 26rpx;
   181|        color: $text-secondary;
   182|        margin-bottom: 12rpx;
   183|        padding: 0 8rpx;
   184|        display: block;
   185|    }
   186|}
   187|
   188|.settings-card {
   189|    .setting-item {
   190|        display: flex;
   191|        align-items: center;
   192|        padding: 24rpx;
   193|        border-bottom: 2rpx solid $glass-card-bg;
   194|        
   195|        &:last-child { border-bottom: none; }
   196|        
   197|        .setting-label {
   198|            flex: 1;
   199|            font-size: 28rpx;
   200|            color: $text-primary;
   201|        }
   202|        
   203|        .setting-value {
   204|            font-size: 26rpx;
   205|            color: $text-secondary;
   206|            margin-right: 8rpx;
   207|        }
   208|        
   209|        .arrow {
   210|            font-size: 28rpx;
   211|            color: $text-placeholder;
   212|            margin-left: 8rpx;
   213|        }
   214|    }
   215|}
   216|
   217|.danger-section {
   218|    padding: 40rpx 0;
   219|    
   220|    .danger-btn {
   221|        background: $glass-card-bg;
   222|        color: #E17055;
   223|        border: 2rpx solid #E17055;
   224|        border-radius: 48rpx;
   225|        font-size: 28rpx;
   226|    }
   227|}
   228|</style>