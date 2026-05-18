import { createSSRApp } from 'vue';
import App from './App.vue';
import i18n from './locale/index';
import pinia from './store/index';
import CustomTabBar from './custom-tab-bar/custom-tab-bar.vue';

export function createApp() {
    const app = createSSRApp(App);
    app.use(pinia);
    app.use(i18n);
    app.config.globalProperties.$dayjs = null; // placeholder, pages import dayjs locally
    app.component('custom-tab-bar', CustomTabBar);
    return { app, pinia };
}
