import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  login as loginApi,
  register as registerApi,
  getUserInfo as getUserInfoApi,
  logout as logoutApi,
} from '@/api/user'
import type { UserInfo, LoginParams, RegisterParams, ApiResponse, LoginResult } from '@/types/user'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref<UserInfo | null>(null)
  // 登录状态
  const isLoggedIn = computed(() => !!localStorage.getItem('userToken'))
  // 是否为管理员
  const isAdmin = computed(() => userInfo.value?.role === 2)
  // 加载状态
  const loading = ref(false)
  // 错误信息
  const error = ref<string | null>(null)

  /**
   * 获取用户信息
   * @returns 用户信息
   */
  const fetchUserInfo = async () => {
    try {
      loading.value = true
      error.value = null

      // 获取用户信息
      const response = (await getUserInfoApi()) as unknown as ApiResponse<UserInfo>

      if (response.code === 200) {
        // 更新用户信息到状态中
        userInfo.value = response.data

        // 也可以保存到本地存储，以便页面刷新后恢复(可选)
        // localStorage.setItem('userInfo', JSON.stringify(response.data))

        return response.data
      } else {
        error.value = response.message || '获取用户信息失败'
        throw new Error(error.value)
      }
    } catch (err: any) {
      console.error('获取用户信息失败:', err)
      error.value = err.message || '获取用户信息失败'

      // 如果获取用户信息失败，可能是token无效，清除登录状态
      if (err.response && err.response.status === 401) {
        clearLoginState()
      }

      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户登录
   * @param loginData 登录信息
   * @returns 是否登录成功
   */
  const login = async (loginData: LoginParams): Promise<boolean> => {
    try {
      loading.value = true
      error.value = null

      // 调用登录接口
      const response = (await loginApi(loginData)) as unknown as ApiResponse<LoginResult>

      if (response.code === 200) {
        const { token, username } = response.data

        // 1. 将token存入localStorage
        localStorage.setItem('userToken', token)
        localStorage.setItem('username', username)

        // 2. 获取用户详细信息
        const userInfoResponse = await fetchUserInfo()

        if (!userInfoResponse) {
          // 如果获取用户信息失败，登录也视为失败
          throw new Error('获取用户信息失败')
        }

        // 登录成功
        return true
      } else {
        error.value = response.message || '登录失败'
        throw new Error(error.value)
      }
    } catch (err: any) {
      console.error('登录失败:', err)
      error.value = err.message || '登录失败，请稍后重试'

      // 清除可能部分存储的登录状态
      clearLoginState()

      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 用户注册
   * @param registerData 注册信息
   * @returns 是否注册成功
   */
  const register = async (registerData: RegisterParams): Promise<boolean> => {
    try {
      loading.value = true
      error.value = null

      // 调用注册接口
      const response = (await registerApi(registerData)) as unknown as ApiResponse<LoginResult>

      if (response.code === 200) {
        const { token, username } = response.data

        // 1. 存储token
        localStorage.setItem('userToken', token)
        localStorage.setItem('username', username)

        // 2. 获取用户详细信息
        const userInfoResponse = await fetchUserInfo()

        if (!userInfoResponse) {
          // 如果获取用户信息失败，注册也视为失败
          throw new Error('获取用户信息失败')
        }

        // 注册成功
        return true
      } else {
        error.value = response.message || '注册失败'
        throw new Error(error.value)
      }
    } catch (err: any) {
      console.error('注册失败:', err)
      error.value = err.message || '注册失败，请稍后重试'

      // 清除可能部分存储的登录状态
      clearLoginState()

      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 清除登录状态
   */
  const clearLoginState = () => {
    localStorage.removeItem('userToken')
    localStorage.removeItem('username')
    // localStorage.removeItem('userInfo') // 如果有存储用户信息
    userInfo.value = null
  }

  /**
   * 退出登录
   */
  const logout = () => {
    // 调用登出API
    logoutApi()

    // 清除登录状态
    clearLoginState()

    // 跳转到首页
    router.push('/')
  }

  /**
   * 初始化用户状态
   * 在应用启动时调用，尝试从localStorage恢复用户状态
   */
  const initUserState = async () => {
    // 如果有token但没有用户信息，尝试获取用户信息
    if (isLoggedIn.value && !userInfo.value) {
      try {
        // 尝试从API获取最新的用户信息
        await fetchUserInfo()
      } catch (err) {
        console.error('初始化用户状态失败:', err)
        // 如果获取失败，清除登录状态
        clearLoginState()
      }
    }
  }

  return {
    userInfo,
    isLoggedIn,
    isAdmin,
    loading,
    error,
    fetchUserInfo,
    login,
    register,
    logout,
    initUserState,
    clearLoginState,
  }
})
