<template>
    <text class="time-format">{{ displayText }}</text>
</template>

<script>
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime.js';
import 'dayjs/locale/zh';
import { useSettingsStore } from '@/store/settings';

dayjs.extend(relativeTime);

export default {
    name: 'TimeFormat',
    setup() {
        const settingsStore = useSettingsStore();
        return { settingsStore };
    },
    props: {
        datetime: {
            type: [String, Number],
            default: ''
        },
        showDate: {
            type: Boolean,
            default: false
        }
    },
    data() {
        return {
            locale: this.settingsStore ? this.settingsStore.language : (uni.getStorageSync('locale') || 'zh')
        };
    },
    computed: {
        displayText() {
            if (!this.datetime) return '';
            const lang = this.locale === 'zh' ? 'zh' : 'en';
            dayjs.locale(lang);
            const target = dayjs(this.datetime);
            if (!target.isValid()) return String(this.datetime);

            if (this.showDate) {
                return lang === 'zh'
                    ? target.format('YYYY-MM-DD')
                    : target.format('MMMM D, YYYY');
            }

            const now = dayjs();
            const diffHours = now.diff(target, 'hour');
            if (diffHours < 1) return target.fromNow();

            const i18n = this.$i18n;
            if (diffHours < 24) {
                const todayLabel = lang === 'zh' ? i18n.t('time.today') : 'Today';
                return todayLabel + ' ' + target.format('HH:mm');
            }

            const diffDays = now.diff(target, 'day');
            if (diffDays < 7) {
                const yesterdayLabel = lang === 'zh' ? i18n.t('time.yesterday') : 'Yesterday';
                if (diffDays === 1) {
                    return yesterdayLabel;
                }
                return target.format('MM-DD HH:mm');
            }

            return target.format('YYYY-MM-DD');
        }
    },
    watch: {
        datetime() {
            // Force recompute on prop change
        },
        'settingsStore.language'(val) {
            this.locale = val;
        }
    },
    mounted() {
        const that = this;
        uni.$on('localeChange', (lang) => {
            that.locale = lang || that.locale;
        });
    },
    beforeDestroy() {
        uni.$off('localeChange');
    }
};
</script>

<style lang="scss" scoped>
.time-format {
    font-size: 22rpx;
    color: $text-tertiary;
    white-space: nowrap;
}
</style>
