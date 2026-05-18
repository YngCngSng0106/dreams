import { useUserStore } from '@/store/user';

const BASE_URL = 'http://10.245.181.124:8080';

export function request(options) {
    const { url, method = 'GET', data = {}, header = {}, silent } = options;

    const userStore = useUserStore();
    if (userStore.token) {
        header['Authorization'] = 'Bearer ' + userStore.token;
    }
    header['Content-Type'] = 'application/json';

    return new Promise((resolve, reject) => {
        // 仅非静默请求显示loading
        if (!silent) {
            uni.showLoading({ title: '加载中...', mask: true });
        }

        uni.request({
            url: BASE_URL + url,
            method,
            data,
            header,
            success: (res) => {
                if (!silent) uni.hideLoading();
                if (res.statusCode === 200 && res.data && res.data.code === 200) {
                    resolve(res.data.data);
                } else if (res.statusCode === 401 || (res.data && res.data.code === 401)) {
                    // 未登录或token过期
                    handleUnauth(silent);
                    reject(new Error('未授权'));
                } else {
                    const msg = (res.data && res.data.message) || '请求失败';
                    if (!silent) uni.showToast({ title: msg, icon: 'none' });
                    reject(new Error(msg));
                }
            },
            fail: (err) => {
                if (!silent) {
                    uni.hideLoading();
                    uni.showToast({ title: '网络错误，请检查连接', icon: 'none' });
                }
                reject(err);
            }
        });
    });
}

function handleUnauth(silent) {
    const userStore = useUserStore();
    if (userStore.token) {
        // 已登录但token失效 -> 清除并跳转
        userStore.logout();
        if (!silent) {
            uni.showToast({ title: '请重新登录', icon: 'none' });
            setTimeout(() => {
                uni.redirectTo({ url: '/pages/auth/login' });
            }, 1500);
        }
    }
    // 未登录时不跳转，让页面自行处理
}

export function uploadFile(filePath, name = 'file') {
    const userStore = useUserStore();
    return new Promise((resolve, reject) => {
        uni.showLoading({ title: '上传中...', mask: true });

        uni.uploadFile({
            url: BASE_URL + '/api/upload/image',
            filePath,
            name,
            header: {
                'Authorization': userStore.token ? 'Bearer ' + userStore.token : ''
            },
            success: (res) => {
                uni.hideLoading();
                try {
                    const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
                    if (data.code === 200) {
                        resolve(data.data);
                    } else {
                        reject(new Error(data.message || '上传失败'));
                    }
                } catch (e) {
                    reject(new Error('上传失败'));
                }
            },
            fail: (err) => {
                uni.hideLoading();
                reject(err);
            }
        });
    });
}
