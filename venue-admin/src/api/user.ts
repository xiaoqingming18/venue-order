import request from '@/utils/request'
import type { LoginParams, RegisterParams } from '@/types/user'

/**
 * 获取当前用户信息
 * @returns 包含用户信息的Promise
 */
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get',
  })
}

/**
 * 用户登录
 * @param data - 登录参数
 * @returns 包含token的Promise
 */
export function login(data: LoginParams) {
  return request.post('/auth/login', data)
}

/**
 * 用户注册
 * @param data - 注册参数
 * @returns 包含token的Promise
 */
export function register(data: RegisterParams) {
  // 确保注册为管理员
  const registerData = {
    ...data,
    isAdmin: true, // 管理端固定传入true
  }
  return request.post('/auth/register', registerData)
}

/**
 * 退出登录
 */
export function logout() {
  localStorage.removeItem('token')
}
