<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单数据
const loginForm = ref({
  username: '',
  password: '',
  remember: true,
})

// 表单验证状态
const errors = ref({
  username: '',
  password: '',
  general: '',
})

// 是否正在提交
const isLoading = ref(false)

// 返回欢迎页
const goBack = () => {
  router.push('/')
}

// 跳转到注册页
const goToRegister = () => {
  router.push('/register')
}

// 验证表单
const validateForm = () => {
  let isValid = true
  errors.value.username = ''
  errors.value.password = ''
  errors.value.general = ''

  if (!loginForm.value.username.trim()) {
    errors.value.username = '请输入用户名'
    isValid = false
  }

  if (!loginForm.value.password) {
    errors.value.password = '请输入密码'
    isValid = false
  }

  return isValid
}

// 提交表单
const handleSubmit = async () => {
  if (!validateForm()) return

  isLoading.value = true

  try {
    console.log('尝试登录，用户名:', loginForm.value.username)
    // 调用登录API
    await userStore.login({
      username: loginForm.value.username,
      password: loginForm.value.password,
      remember: loginForm.value.remember,
    })

    console.log('登录成功，准备跳转')
    // 检查是否有重定向参数
    const redirectPath = route.query.redirect as string || '/user'
    // 登录成功，跳转到重定向页面或默认用户页面
    router.push(redirectPath)
  } catch (error: any) {
    console.error('登录失败', error)
    console.error('详细错误信息:', JSON.stringify(error, null, 2))
    errors.value.general = error.message || '登录失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 导航栏 -->
    <div class="nav-bar">
      <div class="back-button" @click="goBack">
        <i class="fa-solid fa-chevron-left"></i>
        <span>返回</span>
      </div>
      <div class="title">登录</div>
      <div class="placeholder"></div>
    </div>

    <div class="login-container">
      <div class="login-header">
        <h1 class="login-title">欢迎回来</h1>
        <p class="login-subtitle">请使用您的账号登录系统</p>
      </div>

      <form class="login-form" @submit.prevent="handleSubmit">
        <div v-if="errors.general" class="error-message general-error">
          {{ errors.general }}
        </div>

        <div class="form-group">
          <label for="username">用户名</label>
          <input
            type="text"
            id="username"
            v-model="loginForm.username"
            placeholder="请输入用户名"
            autocomplete="username"
          />
          <div v-if="errors.username" class="error-message">
            {{ errors.username }}
          </div>
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
            type="password"
            id="password"
            v-model="loginForm.password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
          <div v-if="errors.password" class="error-message">
            {{ errors.password }}
          </div>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" v-model="loginForm.remember" />
            <span>记住我</span>
          </label>
          <a href="#" class="forgot-password">忘记密码？</a>
        </div>

        <button type="submit" class="primary-button" :disabled="isLoading">
          <span v-if="!isLoading">登录</span>
          <span v-else class="loading-spinner">
            <i class="fa-solid fa-spinner fa-spin"></i>
          </span>
        </button>
      </form>

      <div class="register-link">
        <p>
          还没有账号？
          <a @click="goToRegister">立即注册</a>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 16px;
  background-color: #f7f7f7;
  border-bottom: 1px solid #e0e0e0;
}

.back-button {
  display: flex;
  align-items: center;
  font-size: 16px;
  color: #007aff;
  cursor: pointer;
}

.back-button i {
  margin-right: 4px;
}

.title {
  font-size: 17px;
  font-weight: 600;
}

.placeholder {
  width: 60px;
}

.login-container {
  flex: 1;
  padding: 30px 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 10px;
}

.login-subtitle {
  color: #8e8e93;
  font-size: 16px;
}

.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 15px;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  height: 48px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 0 16px;
  font-size: 16px;
  background-color: #f9f9f9;
}

.form-group input:focus {
  outline: none;
  border-color: #007aff;
  background-color: #fff;
}

.error-message {
  color: #ff3b30;
  font-size: 14px;
  margin-top: 8px;
}

.general-error {
  background-color: rgba(255, 59, 48, 0.1);
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 14px;
}

.remember-me {
  display: flex;
  align-items: center;
}

.remember-me input {
  margin-right: 8px;
}

.forgot-password {
  color: #007aff;
  text-decoration: none;
}

.primary-button {
  display: block;
  width: 100%;
  background-color: #007aff;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 600;
  padding: 14px;
  cursor: pointer;
}

.primary-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  display: inline-block;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 15px;
}

.register-link a {
  color: #007aff;
  text-decoration: none;
  cursor: pointer;
}
</style>
