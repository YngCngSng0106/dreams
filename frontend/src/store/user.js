import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        token: uni.getStorageSync('token') || '',
        userId: uni.getStorageSync('userId') || null
    }),

    getters: {
        isLoggedIn(state) {
            return !!state.token;
        },
        userInfo(state) {
            // userInfo is not stored in localStorage separately;
            // the actual user data is fetched from the server.
            // Keep this getter for compatibility with task requirements.
            return { id: state.userId };
        }
    },

    actions: {
        setToken(val) {
            this.token = val;
            uni.setStorageSync('token', val);
        },

        setUserId(id) {
            this.userId = id;
            uni.setStorageSync('userId', id);
        },

        setUserInfo(obj) {
            if (obj && obj.id) {
                this.userId = obj.id;
                uni.setStorageSync('userId', obj.id);
            }
            if (obj && obj.token) {
                this.token = obj.token;
                uni.setStorageSync('token', obj.token);
            }
        },

        login(token, userId) {
            this.token = token;
            this.userId = userId;
            uni.setStorageSync('token', token);
            uni.setStorageSync('userId', userId);
        },

        logout() {
            this.token = '';
            this.userId = null;
            uni.removeStorageSync('token');
            uni.removeStorageSync('userId');
        }
    }
});
