import request from '@/utils/request'
import type { LoginParams, RegisterParams } from '@/types/user'

/**
 * 用户登录
 * @param data 登录参数
 * @returns Promise
 */
export function login(data: LoginParams) {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  })
}

/**
 * 用户注册
 * @param data 注册参数
 * @returns Promise
 */
export function register(data: RegisterParams) {
  // 确保isAdmin为false（普通用户）
  const registerData = {
    ...data,
    isAdmin: false, // 用户端固定传入false
  }

  return request({
    url: '/auth/register',
    method: 'post',
    data: registerData,
  })
}

/**
 * 获取当前用户信息
 * @returns Promise，返回用户信息
 */
export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get',
  })
}

/**
 * 退出登录
 */
export function logout() {
  // 只处理本地状态清理，API调用在store中处理
  localStorage.removeItem('userToken')
  localStorage.removeItem('username')
}
