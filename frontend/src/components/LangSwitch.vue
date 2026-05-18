<template>
    <view class="lang-switch" :style="customStyle" @click="toggleLang">
        <text class="lang-text" :style="textStyle">{{ displayText }}</text>
    </view>
</template>

<script>
import { setLocale } from '@/locale';
import { useSettingsStore } from '@/store/settings';

export default {
    name: 'LangSwitch',
    setup() {
        const settingsStore = useSettingsStore();
        return { settingsStore };
    },
    props: {
        position: {
            type: String,
            default: 'default'
        },
        size: {
            type: Number,
            default: 28
        }
    },
    data() {
        return {
            locale: this.settingsStore ? this.settingsStore.language : (uni.getStorageSync('locale') || 'zh')
        };
    },
    computed: {
        displayText() {
            return this.locale === 'zh' ? 'EN' : '中';
        },
        textStyle() {
            return { fontSize: this.size + 'rpx' };
        },
        customStyle() {
            if (this.position === 'custom') {
                return {};
            }
            return {
                position: 'fixed',
                top: '20rpx',
                right: '20rpx',
                zIndex: '1000'
            };
        }
    },
    methods: {
        toggleLang() {
            const next = this.locale === 'zh' ? 'en' : 'zh';
            this.locale = next;
            setLocale(next);
        },
        onLocaleChange() {
            this.locale = this.settingsStore.language;
        }
    },
    mounted() {
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

<style lang="scss" scoped>
.lang-switch {
    background: $glass-bg-gradient-1;
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1rpx solid $glass-border;
    border-radius: 20rpx;
    padding: 6rpx 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
}

.lang-text {
    color: $text-primary;
    font-weight: 500;
    line-height: 1;
}

.lang-switch:active {
    background: $primary-color;
    border-color: $primary-light;
}
</style>
