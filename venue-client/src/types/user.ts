/**
 * 用户信息接口
 */
export interface UserInfo {
  id: number
  username: string
  email: string | null
  phone: string | null
  nickname: string | null
  avatar: string | null
  gender: string | null
  birthday: string | null
  address: string | null
  status: number
  role: number // 用户角色：1-普通用户，2-管理员
  lastLoginTime: string | null
  registerTime: string | null
}

/**
 * 登录请求参数
 */
export interface LoginParams {
  username: string
  password: string
  remember?: boolean
}

/**
 * 注册请求参数
 */
export interface RegisterParams {
  username: string
  password: string
  confirmPassword: string
  email?: string | null
  phone?: string | null
  nickname?: string | null
  isAdmin: boolean
}

/**
 * 登录响应数据
 */
export interface LoginResult {
  token: string
  username: string
}

/**
 * API响应格式
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
