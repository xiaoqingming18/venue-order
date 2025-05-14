import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  // 修改为相对路径，这样会使用Vite代理配置
  baseURL: '/api',
  // 请求超时时间
  timeout: 10000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从本地存储中获取token
    const token = localStorage.getItem('token')
    // 如果有token则添加到请求头，并添加Bearer前缀
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  },
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果返回状态码不是200，判断为错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // 401: 未登录或token过期
      if (res.code === 401) {
        // 清除token并重定向到登录页
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  (error) => {
    console.error('响应错误：', error)
    ElMessage.error(error.response?.data?.message || '请求失败，请稍后再试')
    return Promise.reject(error)
  },
)

export default request
