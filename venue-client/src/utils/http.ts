import request from './request'
import type { AxiosRequestConfig } from 'axios'

/**
 * HTTP请求工具类，封装基本的请求方法
 */
export default {
  /**
   * GET请求
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise
   */
  get<T = any>(url: string, config?: AxiosRequestConfig) {
    return request.get<any, { code: number, data: T, message: string }>(url, config)
  },

  /**
   * POST请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise
   */
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return request.post<any, { code: number, data: T, message: string }>(url, data, config)
  },

  /**
   * PUT请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise
   */
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return request.put<any, { code: number, data: T, message: string }>(url, data, config)
  },

  /**
   * DELETE请求
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise
   */
  delete<T = any>(url: string, config?: AxiosRequestConfig) {
    return request.delete<any, { code: number, data: T, message: string }>(url, config)
  },

  /**
   * 上传文件
   * @param url 请求地址
   * @param formData FormData对象
   * @param config 请求配置
   * @returns Promise
   */
  upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig) {
    // 设置上传文件的请求头
    const uploadConfig = {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      ...config
    }
    return request.post<any, { code: number, data: T, message: string }>(url, formData, uploadConfig)
  },

  /**
   * 下载文件
   * @param url 请求地址
   * @param params 请求参数
   * @param filename 保存的文件名
   * @returns Promise
   */
  download(url: string, params?: any, filename?: string) {
    return request.get(url, {
      params,
      responseType: 'blob'
    }).then(response => {
      // 创建隐藏的下载链接
      const blob = new Blob([response.data])
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = filename || this.getFilenameFromUrl(url)
      document.body.appendChild(link)
      link.click()
      URL.revokeObjectURL(link.href)
      document.body.removeChild(link)
      return response
    })
  },

  /**
   * 从URL中提取文件名
   * @param url URL地址
   * @returns 文件名
   */
  getFilenameFromUrl(url: string): string {
    const parts = url.split('/')
    return parts[parts.length - 1] || 'download'
  }
} 