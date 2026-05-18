<template>
    <view class="tabbar">
        <view class="tabbar-item" v-for="(item, index) in list" :key="index" :class="{active: item.pagePath === currentPage}" @click="switchTab(item.pagePath)">
            <text class="tabbar-icon">{{ item.icon }}</text>
            <text class="tabbar-text" :class="{active: item.pagePath === currentPage}">{{ t(item.textKey) }}</text>
        </view>
    </view>
</template>

<script>
import { zh, en } from '@/locale/index';
import { useSettingsStore } from '@/store/settings';

const i18nMessages = { zh, en };

export default {
    setup() {
        const settingsStore = useSettingsStore();
        return { settingsStore };
    },
    data() {
        return {
            currentPage: '',
            list: [
                { pagePath: 'pages/explore/explore', icon: '🔭', textKey: 'tab.explore' },
                { pagePath: 'pages/dream-record/dream-record', icon: '✏️', textKey: 'tab.record' },
                { pagePath: 'pages/discussion/discussion', icon: '💬', textKey: 'tab.discussion' },
                { pagePath: 'pages/message/message', icon: '🔔', textKey: 'tab.message' },
                { pagePath: 'pages/mine/mine', icon: '👤', textKey: 'tab.mine' }
            ],
            locale: this.settingsStore ? this.settingsStore.language : (uni.getStorageSync('locale') || 'zh')
        };
    },
    methods: {
        t(key) {
            const msgs = i18nMessages[this.locale];
            const parts = key.split('.');
            let result = msgs;
            for (const part of parts) {
                result = result?.[part];
            }
            return result || key;
        },
        switchTab(pagePath) {
            if (this.currentPage === pagePath) return;
            const idx = this.list.findIndex(item => item.pagePath === pagePath);
            if (idx !== -1) {
                uni.switchTab({ url: '/' + pagePath });
            }
        },
        updateCurrentPage() {
            const pages = getCurrentPages();
            const current = pages[pages.length - 1];
            if (current) {
                this.currentPage = current.route;
            }
            // Also refresh locale in case it changed
            this.locale = this.settingsStore.language;
        },
        onLocaleChange() {
            this.locale = this.settingsStore.language;
        }
    },
    mounted() {
        this.updateCurrentPage();
        // Listen for global language change event
        const that = this;
        uni.$on('localeChange', () => {
            that.onLocaleChange();
        });
    },
    beforeDestroy() {
        uni.$off('localeChange');
    }
};
</script>

<style lang="scss">
.tabbar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 100rpx;
    background: $glass-tabbar-bg;
    backdrop-filter: blur($glass-blur);
    -webkit-backdrop-filter: blur($glass-blur);
    border-top: 1rpx solid $glass-card-bg;
    display: flex;
    align-items: center;
    justify-content: space-around;
    z-index: 999;
    padding-bottom: env(safe-area-inset-bottom);
}

.tabbar-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 8rpx 0;
}

.tabbar-icon {
    font-size: 40rpx;
    line-height: 1;
}

.tabbar-text {
    font-size: 20rpx;
    color: $text-tertiary;
    margin-top: 4rpx;
}

.tabbar-text.active {
    color: #A29BFE;
    font-weight: 600;
}
</style>
