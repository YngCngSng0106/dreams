import { defineStore } from 'pinia';
import { request } from '@/utils/request';

export const useAppStore = defineStore('app', {
    state: () => ({
        categories: [],
        loaded: false
    }),

    actions: {
        setCategories(list) {
            this.categories = list;
            this.loaded = true;
        },

        async loadCategories() {
            if (this.loaded) return this.categories;
            try {
                const list = await request({
                    url: '/api/category/list',
                    method: 'GET',
                    silent: true
                });
                this.setCategories(list);
                return list;
            } catch (e) {
                console.error('Load categories failed:', e);
                return [];
            }
        }
    }
});
