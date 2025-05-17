import request from '@/utils/request'
import type { LoginParams, RegisterParams, UserQueryParams, PageResult, UserInfo, UserAddParams, UserEditParams } from '@/types/user'

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

/**
 * 获取用户列表
 * @param params - 查询参数
 * @returns 用户列表分页数据
 */
export function getUserList(params: UserQueryParams) {
  return request.post<any, {data: PageResult<UserInfo>}>('/admin/user/list', params)
}

/**
 * 获取用户详情
 * @param id - 用户ID
 * @returns 用户详情信息
 */
export function getUserDetail(id: number) {
  return request.get<any, {data: UserInfo}>(`/admin/user/detail/${id}`)
}

/**
 * 添加用户
 * @param data - 用户添加信息
 * @returns 添加后的用户信息
 */
export function addUser(data: UserAddParams) {
  return request.post<any, {data: UserInfo}>('/admin/user/add', data)
}

/**
 * 编辑用户
 * @param data - 用户编辑信息
 * @returns 编辑后的用户信息
 */
export function updateUser(data: UserEditParams) {
  return request.put<any, {data: UserInfo}>('/admin/user/edit', data)
}
