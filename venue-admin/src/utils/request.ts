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
    
    // 错误处理逻辑
    let errorMessage = '请求失败，请稍后再试'
    
    if (error.response) {
      // 记录错误状态码和详细信息
      console.error('错误状态码：', error.response.status)
      console.error('错误详细信息：', error.response.data)

      // 根据状态码处理不同类型的错误
      if (error.response.status === 400) {
        // 处理400错误，尝试提取更具体的错误信息
        if (error.response.data) {
          if (typeof error.response.data === 'object') {
            // 如果返回的是对象，尝试提取message字段
            errorMessage = error.response.data.message || '数据验证失败'
          } else if (typeof error.response.data === 'string') {
            // 如果返回的是字符串，直接使用
            errorMessage = error.response.data
          }
        }
      } else if (error.response.status === 401) {
        // 处理401错误（未授权）
        errorMessage = '登录已过期，请重新登录'
        localStorage.removeItem('token')
        setTimeout(() => {
          window.location.href = '/login'
        }, 1500)
      } else if (error.response.status === 403) {
        // 处理403错误（禁止访问）
        errorMessage = '您没有权限访问此资源'
      } else if (error.response.status === 404) {
        // 处理404错误（资源不存在）
        errorMessage = '请求的资源不存在'
      } else if (error.response.status === 500) {
        // 处理500错误（服务器错误）
        errorMessage = '服务器错误，请稍后再试'
      }
    } else if (error.request) {
      // 处理请求已发送但未收到响应的情况
      errorMessage = '服务器没有响应，请检查网络连接'
    } else {
      // 处理其他类型的错误
      errorMessage = error.message || '请求过程中发生未知错误'
    }
    
    ElMessage.error(errorMessage)
    return Promise.reject(error)
  },
)

export default request
