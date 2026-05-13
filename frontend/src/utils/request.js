const BASE_URL = 'http://localhost:8080';

export function request(options) {
    const { url, method = 'GET', data = {}, header = {} } = options;
    
    // 添加token
    const token = uni.getStorageSync('token');
    if (token) {
        header['Authorization'] = 'Bearer ' + token;
    }
    header['Content-Type'] = 'application/json';
    
    return new Promise((resolve, reject) => {
        uni.showLoading({ title: '加载中...', mask: true });
        
        uni.request({
            url: BASE_URL + url,
            method,
            data,
            header,
            success: (res) => {
                uni.hideLoading();
                if (res.statusCode === 200 && res.data.code === 200) {
                    resolve(res.data.data);
                } else if (res.statusCode === 401) {
                    uni.clearStorageSync();
                    uni.redirectTo({ url: '/pages/auth/login' });
                    reject(new Error('请重新登录'));
                } else {
                    const msg = res.data.message || '请求失败';
                    uni.showToast({ title: msg, icon: 'none' });
                    reject(new Error(msg));
                }
            },
            fail: (err) => {
                uni.hideLoading();
                uni.showToast({ title: '网络错误，请检查连接', icon: 'none' });
                reject(err);
            }
        });
    });
}

export function uploadFile(filePath, name = 'file') {
    const token = uni.getStorageSync('token');
    return new Promise((resolve, reject) => {
        uni.showLoading({ title: '上传中...', mask: true });
        
        uni.uploadFile({
            url: BASE_URL + '/api/upload/image',
            filePath,
            name,
            header: {
                'Authorization': 'Bearer ' + token
            },
            success: (res) => {
                uni.hideLoading();
                const data = JSON.parse(res.data);
                if (data.code === 200) {
                    resolve(data.data);
                } else {
                    reject(new Error(data.message));
                }
            },
            fail: (err) => {
                uni.hideLoading();
                reject(err);
            }
        });
    });
}
