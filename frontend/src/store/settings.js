import { defineStore } from 'pinia';

export const useSettingsStore = defineStore('settings', {
    state: () => {
        // locale is stored as 'locale' in localStorage
        const stored = uni.getStorageSync('locale');
        return {
            language: stored || 'zh',
            theme: uni.getStorageSync('theme') || 'dark',
            pushEnabled: uni.getStorageSync('pushEnabled') === '1' ? true : false,
            isAnonymous: uni.getStorageSync('isAnonymous') === '1' ? false : true
        };
    },

    actions: {
        setLanguage(lang) {
            this.language = lang;
            uni.setStorageSync('locale', lang);
        },

        setTheme(theme) {
            this.theme = theme;
            uni.setStorageSync('theme', theme);
        },

        toggleTheme() {
            this.theme = this.theme === 'dark' ? 'light' : 'dark';
            uni.setStorageSync('theme', this.theme);
        },

        togglePush() {
            this.pushEnabled = !this.pushEnabled;
            uni.setStorageSync('pushEnabled', this.pushEnabled ? '1' : '0');
        },

        toggleAnonymous() {
            this.isAnonymous = !this.isAnonymous;
            uni.setStorageSync('isAnonymous', this.isAnonymous ? '1' : '0');
        }
    }
});
