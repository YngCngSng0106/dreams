import { useUserStore } from '@/store/user';

export function getToken() {
    const userStore = useUserStore();
    return userStore.token;
}

export function setToken(token) {
    const userStore = useUserStore();
    userStore.setToken(token);
}

export function getUserId() {
    const userStore = useUserStore();
    return userStore.userId;
}

export function setUserId(userId) {
    const userStore = useUserStore();
    userStore.setUserId(userId);
}

export function clearAuth() {
    const userStore = useUserStore();
    userStore.logout();
}

export function isLoggedIn() {
    const userStore = useUserStore();
    return userStore.isLoggedIn;
}

export function requireLogin() {
    if (!isLoggedIn()) {
        uni.showToast({ title: '请先登录', icon: 'none' });
        setTimeout(() => {
            uni.redirectTo({ url: '/pages/auth/login' });
        }, 1500);
        return false;
    }
    return true;
}
