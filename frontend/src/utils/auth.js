export function getToken() {
    return uni.getStorageSync('token');
}

export function setToken(token) {
    uni.setStorageSync('token', token);
}

export function getUserId() {
    return uni.getStorageSync('userId');
}

export function setUserId(userId) {
    uni.setStorageSync('userId', userId);
}

export function clearAuth() {
    uni.removeStorageSync('token');
    uni.removeStorageSync('userId');
}

export function isLoggedIn() {
    return !!getToken();
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
