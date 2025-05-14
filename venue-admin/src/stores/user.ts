import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { UserInfo, LoginParams, RegisterParams, LoginResult } from '@/types/user'
import {
  getUserInfo,
  login as loginApi,
  register as registerApi,
  logout as logoutApi,
} from '@/api/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 定义API响应类型
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref<UserInfo | null>(null)
  // 登录状态
  const isLoggedIn = computed(() => !!localStorage.getItem('token'))
  // 是否为管理员
  const isAdmin = computed(() => userInfo.value?.role === 2)
  // 加载状态
  const loading = ref(false)

  /**
   * 获取用户信息
   */
  const fetchUserInfo = async () => {
    try {
      loading.value = true
      const rawResponse = await getUserInfo()
      const response = rawResponse as unknown as ApiResponse<UserInfo>

      if (response.code === 200) {
        userInfo.value = response.data
        return response.data
      } else {
        throw new Error(response.message || '获取用户信息失败')
      }
    } catch (error: any) {
      console.error('获取用户信息失败:', error)
      ElMessage.error(error.message || '获取用户信息失败')
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户登录
   * @param loginData 登录信息
   */
  const login = async (loginData: LoginParams) => {
    try {
      loading.value = true
      // 调用登录接口
      const rawResponse = await loginApi(loginData)
      const response = rawResponse as unknown as ApiResponse<LoginResult>

      const { token, username } = response.data

      // 存储token
      localStorage.setItem('token', token)

      // 记住用户名
      if (loginData.remember) {
        localStorage.setItem('rememberedUsername', loginData.username)
      } else {
        localStorage.removeItem('rememberedUsername')
      }

      // 获取用户信息
      await fetchUserInfo()

      // 添加对用户角色的判断，如果不是管理员则拒绝登录
      if (userInfo.value && userInfo.value.role !== 2) {
        // 清除token
        localStorage.removeItem('token')
        // 清除用户信息
        userInfo.value = null
        throw new Error('该用户不是管理员')
      }

      ElMessage.success(`欢迎回来，${username}`)
      return true
    } catch (error: any) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请稍后再试')
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户注册
   * @param registerData 注册信息
   */
  const register = async (registerData: RegisterParams) => {
    try {
      loading.value = true
      // 调用注册接口 (api层已确保传入isAdmin=true)
      const rawResponse = await registerApi(registerData)
      const response = rawResponse as unknown as ApiResponse<LoginResult>

      const { token, username } = response.data

      // 存储token
      localStorage.setItem('token', token)

      // 获取用户信息
      await fetchUserInfo()

      // 添加对用户角色的判断，如果不是管理员则拒绝登录
      if (userInfo.value && userInfo.value.role !== 2) {
        // 清除token
        localStorage.removeItem('token')
        // 清除用户信息
        userInfo.value = null
        throw new Error('该用户不是管理员')
      }

      ElMessage.success(`注册成功，欢迎 ${username}`)
      return true
    } catch (error: any) {
      console.error('注册失败:', error)
      ElMessage.error(error.message || '注册失败，请稍后再试')
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * 退出登录
   */
  const logout = () => {
    // 清除token
    localStorage.removeItem('token')
    // 调用登出API
    logoutApi()
    // 清除用户信息
    userInfo.value = null
    // 跳转到登录页
    router.push('/login')
    ElMessage.success('已退出登录')
  }

  /**
   * 初始化用户状态
   */
  const initUserState = async () => {
    if (isLoggedIn.value && !userInfo.value) {
      await fetchUserInfo()
    }
  }

  return {
    userInfo,
    isLoggedIn,
    isAdmin,
    loading,
    fetchUserInfo,
    login,
    register,
    logout,
    initUserState,
  }
})
