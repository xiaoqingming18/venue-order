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
  email?: string
  phone?: string
  nickname?: string
  isAdmin?: boolean // 添加isAdmin字段，用于指定是否为管理员
}

/**
 * 登录响应数据
 */
export interface LoginResult {
  token: string
  username: string
}

/**
 * 用户查询参数
 */
export interface UserQueryParams {
  username?: string
  nickname?: string
  phone?: string
  email?: string
  status?: number
  role?: number
  current?: number
  size?: number
  orderField?: string
  orderType?: 'asc' | 'desc'
}

/**
 * 分页结果类型
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 用户添加参数
 */
export interface UserAddParams {
  username: string
  password: string
  confirmPassword: string
  email?: string
  phone?: string
  nickname?: string
  avatar?: string
  gender?: string
  birthday?: string
  address?: string
  status?: number
  role?: number
}

/**
 * 用户编辑参数
 */
export interface UserEditParams {
  id: number
  email?: string
  phone?: string
  nickname?: string
  avatar?: string
  gender?: string
  birthday?: string
  address?: string
  status?: number
  role?: number
  newPassword?: string
}
