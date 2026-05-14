import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// Request interceptor: add auth token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor: unwrap Result wrapper {code, message, data}
request.interceptors.response.use(
  response => {
    const res = response.data
    // 后端返回格式: {code: 200, message: 'success', data: {...}}
    if (res && res.code === 200) {
        return res.data; // 直接返回 data，即使是 null 也没问题
    }
    // 非 200 状态，返回原始 Result 以便调用方处理
    return Promise.reject(new Error(res?.message || '请求失败'))
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
