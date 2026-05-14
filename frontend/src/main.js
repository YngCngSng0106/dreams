import { createSSRApp } from 'vue';
import App from './App.vue';
import i18n from './locale/index';
import CustomTabBar from './custom-tab-bar/custom-tab-bar.vue';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime.js';
import 'dayjs/locale/zh';
dayjs.extend(relativeTime);
dayjs.locale('zh');

export function createApp() {
    const app = createSSRApp(App);\n    app.use(i18n);
    app.config.globalProperties.$dayjs = dayjs;
    app.component('custom-tab-bar', CustomTabBar);
    return { app };\n}