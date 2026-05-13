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

const i18nMessages = { zh, en };

export default {
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
            locale: uni.getStorageSync('locale') || 'zh'
        };
    },
    watch: {
        locale() {}
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
        }
    },
    mounted() {
        this.updateCurrentPage();
    }
};
</script>

<style>
.tabbar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 100rpx;
    background: #FFFFFF;
    border-top: 2rpx solid #F0F0F0;
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
    color: #B2BEC3;
    margin-top: 4rpx;
}

.tabbar-text.active {
    color: #6C5CE7;
    font-weight: 600;
}
</style>
