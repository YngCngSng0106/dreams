import { createSSRApp } from 'vue';
import App from './App.vue';
import i18n from './locale/index';
import CustomTabBar from './custom-tab-bar/custom-tab-bar.vue';

export function createApp() {
    const app = createSSRApp(App);
    app.use(i18n);
    app.component('custom-tab-bar', CustomTabBar);
    return { app };
}
