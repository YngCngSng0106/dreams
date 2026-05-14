import { request } from './request';


// 认证
export const authApi = {
    register: (data) => request({ url: '/api/auth/register', method: 'POST', data }),
    login: (data) => request({ url: '/api/auth/login', method: 'POST', data }),
    logout: () => request({ url: '/api/auth/logout', method: 'POST' }),
    verify: () => request({ url: '/api/auth/verify' }),
    sendCode: (data) => request({ url: '/api/auth/send-code', method: 'POST', data }),
    resetPassword: (data) => request({ url: '/api/auth/reset-password', method: 'POST', data })
};

// 用户
export const userApi = {
    getMyProfile: () => request({ url: '/api/users/me' }),
    updateMe: (data) => request({ url: '/api/users/me', method: 'PUT', data }),
    getUserProfile: (userId) => request({ url: '/api/users/' + userId }),
    getUserDreams: (userId, page = 1, pageSize = 20) => 
        request({ url: '/api/users/' + userId + '/dreams?page=' + page + '&pageSize=' + pageSize })
};

// 梦境
export const dreamApi = {
    create: (data) => request({ url: '/api/dreams', method: 'POST', data }),
    detail: (dreamId) => request({ url: '/api/dreams/' + dreamId }),
    update: (dreamId, data) => request({ url: '/api/dreams/' + dreamId, method: 'PUT', data }),
    delete: (dreamId) => request({ url: '/api/dreams/' + dreamId, method: 'DELETE' }),
    myList: (page = 1, pageSize = 20, categoryId, isRecurring) => {
        let url = '/api/dreams?page=' + page + '&pageSize=' + pageSize;
        if (categoryId) url += '&categoryId=' + categoryId;
        if (isRecurring != null) url += '&isRecurring=' + isRecurring;
        return request({ url });
    },
    feed: (page = 1, pageSize = 20, sortBy = 'newest', categoryId) => {
        let url = '/api/dreams/feed?page=' + page + '&pageSize=' + pageSize + '&sortBy=' + sortBy;
        if (categoryId != null) url += '&categoryId=' + categoryId;
        return request({ url });
    },
    similar: (dreamId, limit = 10) => 
        request({ url: '/api/dreams/' + dreamId + '/similar?limit=' + limit }),
    like: (dreamId) => request({ url: '/api/dreams/' + dreamId + '/like', method: 'POST' }),
    unlike: (dreamId) => request({ url: '/api/dreams/' + dreamId + '/unlike', method: 'POST' }),
    stats: (dreamId) => request({ url: '/api/dreams/' + dreamId + '/stats' })
};

// 分类
export const categoryApi = {
    list: () => request({ url: '/api/categories' })
};

// 讨论组
export const discussionApi = {
    create: (data) => request({ url: '/api/discussions', method: 'POST', data }),
    detail: (id) => request({ url: '/api/discussions/' + id }),
    update: (id, data) => request({ url: '/api/discussions/' + id, method: 'PUT', data }),
    delete: (id) => request({ url: '/api/discussions/' + id, method: 'DELETE' }),
    list: (page = 1, pageSize = 20, type, keyword) => {
        let url = '/api/discussions?page=' + page + '&pageSize=' + pageSize;
        if (type) url += '&type=' + type;
        if (keyword) url += '&keyword=' + keyword;
        return request({ url });
    },
    myList: (page = 1, pageSize = 20) => 
        request({ url: '/api/discussions/my?page=' + page + '&pageSize=' + pageSize }),
    members: (id, page = 1, pageSize = 20) => 
        request({ url: '/api/discussions/' + id + '/members?page=' + page + '&pageSize=' + pageSize }),
    join: (id) => request({ url: '/api/discussions/' + id + '/join', method: 'POST' }),
    leave: (id) => request({ url: '/api/discussions/' + id + '/leave', method: 'POST' }),
    invite: (id, userId) => request({ url: '/api/discussions/' + id + '/invite', method: 'POST', data: { userId } }),
    recommended: (page = 1, pageSize = 20) => 
        request({ url: '/api/discussions/recommended?page=' + page + '&pageSize=' + pageSize })
};

// 评论
export const commentApi = {
    create: (data) => request({ url: '/api/comments', method: 'POST', data }),
    list: (discussionId, page = 1, pageSize = 20, sortBy = 'newest') => 
        request({ url: '/api/comments/' + discussionId + '?page=' + page + '&pageSize=' + pageSize + '&sortBy=' + sortBy }),
    update: (commentId, content) => request({ url: '/api/comments/' + commentId, method: 'PUT', data: { content } }),
    delete: (commentId) => request({ url: '/api/comments/' + commentId, method: 'DELETE' }),
    like: (commentId) => request({ url: '/api/comments/' + commentId + '/like', method: 'POST' }),
    unlike: (commentId) => request({ url: '/api/comments/' + commentId + '/unlike', method: 'POST' })
};

// 关注
export const followApi = {
    follow: (userId) => request({ url: '/api/follow/' + userId, method: 'POST' }),
    unfollow: (userId) => request({ url: '/api/follow/' + userId, method: 'DELETE' }),
    following: (userId, page = 1, pageSize = 20) => 
        request({ url: '/api/follow/following/' + userId + '?page=' + page + '&pageSize=' + pageSize }),
    followers: (userId, page = 1, pageSize = 20) => 
        request({ url: '/api/follow/followers/' + userId + '?page=' + page + '&pageSize=' + pageSize }),
    status: (userId) => request({ url: '/api/follow/status/' + userId })
};

// 通知
export const notificationApi = {
    list: (page = 1, pageSize = 20, type, isRead) => {
        let url = '/api/notifications?page=' + page + '&pageSize=' + pageSize;
        if (type) url += '&type=' + type;
        if (isRead != null) url += '&isRead=' + isRead;
        return request({ url });
    },
    unreadCount: () => request({ url: '/api/notifications/unread-count' }),
    markRead: (id) => request({ url: '/api/notifications/' + id + '/read', method: 'PUT' }),
    markAllRead: () => request({ url: '/api/notifications/read-all', method: 'PUT' })
};

// 搜索
export const searchApi = {
    search: (keyword, type, page = 1, pageSize = 20) => {
        let url = '/api/search?keyword=' + encodeURIComponent(keyword) + '&page=' + page + '&pageSize=' + pageSize;
        if (type) url += '&type=' + type;
        return request({ url });
    },
    tags: (keyword) => request({ url: '/api/search/tags?keyword=' + encodeURIComponent(keyword) })
};

// 设置
export const settingsApi = {
    getSettings: () => request({ url: '/api/settings' }),
    updateSettings: (data) => request({ url: '/api/settings', method: 'PUT', data }),
    changePassword: (data) => request({ url: '/api/settings/password', method: 'PUT', data }),
    deleteAccount: () => request({ url: '/api/settings/delete-account', method: 'POST' })
};

// 统计
export const statsApi = {
    myStats: () => request({ url: '/api/stats/me' })
};
