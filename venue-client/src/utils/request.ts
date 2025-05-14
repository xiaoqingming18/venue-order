import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // 设置基础URL，会被代理到 http://localhost:8081/api
  timeout: 15000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 检查是否需要跳过鉴权
    const skipAuth = config.headers && config.headers['X-Skip-Auth'] === 'true'
    
    // 从localStorage获取token（如果不需要跳过鉴权）
    if (!skipAuth) {
      const token = localStorage.getItem('userToken')
      // 如果有token则在header中添加token
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
        console.log('请求添加了认证信息:', `Bearer ${token.substring(0, 10)}...`);
      } else {
        console.warn('警告: 未找到认证Token, 请求可能会被拒绝');
      }
    }
    
    // 删除跳过鉴权的标记，不发送到服务器
    if (config.headers) {
      delete config.headers['X-Skip-Auth']
    }
    
    // 打印请求信息，包括完整的请求路径和参数
    console.log(`发送${config.method?.toUpperCase()}请求:`, 
      `${config.baseURL || ''}${config.url || ''}`, 
      {
        params: config.params || {},
        headers: config.headers,
        data: config.data || {}
      });
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 检查是否是支付表单响应（文本类型）
    const contentType = response.headers['content-type'] || '';
    if (contentType.includes('text/html') || response.config.responseType === 'text') {
      console.log('接收到HTML响应，可能是支付表单，内容长度:', response.data?.length || 0);
      
      // 如果是支付表单，直接返回原始数据不做任何处理
      return {
        code: 200,
        data: response.data,
        message: 'success'
      };
    }
    
    const res = response.data
    console.log('收到响应:', response.config.url, res)

    // 如果返回的状态码不是200，说明请求出错
    if (res.code !== 200 && res.code !== undefined) {
      console.error('接口错误', res.message || '系统错误')
      ElMessage.error(res.message || '请求失败')

      // 401表示未授权，可能是token过期
      if (res.code === 401) {
        // 清除token并重定向到登录页
        localStorage.removeItem('userToken')
        window.location.href = '/login'
      }

      return Promise.reject(new Error(res.message || '系统错误'))
    }

    return res
  },
  (error) => {
    console.error('响应错误:', error.message)
    
    // 详细记录错误信息便于调试
    if (error.response) {
      console.error('错误状态码:', error.response.status)
      console.error('错误状态文本:', error.response.statusText)
      
      // 打印完整的响应数据
      if (error.response.data) {
        try {
          console.error('错误响应数据:', JSON.stringify(error.response.data, null, 2))
          
          // 尝试解析响应体中的具体错误信息
          if (typeof error.response.data === 'string' && error.response.data.includes('<html>')) {
            console.error('响应似乎是HTML格式，可能是服务器端的错误页面')
            
            // 尝试从HTML中提取错误信息
            const errorMatch = error.response.data.match(/<p><b>Message:<\/b>([^<]+)<\/p>/)
            if (errorMatch && errorMatch[1]) {
              console.error('从HTML中提取的错误信息:', errorMatch[1].trim())
            }
          }
        } catch (e) {
          console.error('解析错误响应数据失败:', e)
          console.error('原始错误响应:', error.response.data)
        }
      }
      
      // 尝试打印请求数据
      if (error.config && error.config.data) {
        try {
          const requestData = typeof error.config.data === 'string' 
            ? JSON.parse(error.config.data) 
            : error.config.data
          console.error('请求数据:', JSON.stringify(requestData, null, 2))
        } catch (e) {
          console.error('解析请求数据失败:', e)
          console.error('原始请求数据:', error.config.data)
        }
      }
    }

    if (error.response && error.response.status === 401) {
      // 清除token并重定向到登录页
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('userToken')
      
      // 刷新页面
      setTimeout(() => {
        window.location.href = '/login'
      }, 1000)
    } else if (error.response && error.response.status === 400) {
      // 针对400错误提供更详细的错误处理
      let errorMsg = '请求参数错误'
      
      // 尝试获取详细的错误信息
      if (error.response.data) {
        // Spring Boot 错误响应通常包含 message 或 error 字段
        if (error.response.data.message) {
          errorMsg = error.response.data.message
          
          // 检查是否是外键约束错误
          if (errorMsg.includes('SQLIntegrityConstraintViolationException') || 
              errorMsg.includes('foreign key constraint')) {
            if (errorMsg.includes('facility_id')) {
              errorMsg = '选择的设施不存在或已被禁用，请重新选择'
            } else if (errorMsg.includes('venue_id')) {
              errorMsg = '选择的场馆不存在或已被禁用，请选择其他场馆'
            } else {
              errorMsg = '数据关联错误，请检查您的输入'
            }
          }
        } else if (error.response.data.error) {
          errorMsg = error.response.data.error
        } else if (typeof error.response.data === 'string') {
          if (error.response.data.includes('<html>')) {
            // 尝试从HTML错误页面中提取信息
            const match = error.response.data.match(/<p><b>Message:<\/b>([^<]+)<\/p>/)
            if (match && match[1]) {
              errorMsg = match[1].trim()
              
              // 检查HTML错误信息中的外键约束错误
              if (errorMsg.includes('SQLIntegrityConstraintViolationException') || 
                  errorMsg.includes('foreign key constraint')) {
                if (errorMsg.includes('facility_id')) {
                  errorMsg = '选择的设施不存在或已被禁用，请重新选择'
                } else if (errorMsg.includes('venue_id')) {
                  errorMsg = '选择的场馆不存在或已被禁用，请选择其他场馆'
                } else {
                  errorMsg = '数据关联错误，请检查您的输入'
                }
              }
            }
          } else {
            errorMsg = error.response.data
          }
        }
        
        // 记录详细的错误信息到控制台
        console.error('400错误详情:', errorMsg)
      }
      
      ElMessage.error(errorMsg)
    } else {
      // 显示错误信息，优先使用后端返回的详细错误信息
      const errorMsg = error.response?.data?.message || 
                       error.response?.data?.error || 
                       error.message || 
                       '请求失败'
      ElMessage.error(errorMsg)
    }

    return Promise.reject(error)
  }
)

export default request
