<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 表单数据
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: '',
  agreement: false,
  isAdmin: false, // 用户端固定为普通用户
})

// 表单验证状态
const errors = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  agreement: '',
  general: '',
})

// 是否正在提交
const isLoading = ref(false)

// 返回欢迎页
const goBack = () => {
  router.push('/')
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 验证表单
const validateForm = () => {
  let isValid = true

  // 重置所有错误信息
  Object.keys(errors.value).forEach((key) => {
    errors.value[key as keyof typeof errors.value] = ''
  })

  // 验证用户名
  if (!registerForm.value.username.trim()) {
    errors.value.username = '请输入用户名'
    isValid = false
  } else if (registerForm.value.username.length < 4 || registerForm.value.username.length > 20) {
    errors.value.username = '用户名长度必须在4-20个字符之间'
    isValid = false
  }

  // 验证密码
  if (!registerForm.value.password) {
    errors.value.password = '请输入密码'
    isValid = false
  } else if (registerForm.value.password.length < 6 || registerForm.value.password.length > 20) {
    errors.value.password = '密码长度必须在6-20个字符之间'
    isValid = false
  }

  // 验证确认密码
  if (!registerForm.value.confirmPassword) {
    errors.value.confirmPassword = '请确认密码'
    isValid = false
  } else if (registerForm.value.confirmPassword !== registerForm.value.password) {
    errors.value.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  // 验证邮箱（如果有填写）
  if (registerForm.value.email && !validateEmail(registerForm.value.email)) {
    errors.value.email = '请输入有效的邮箱地址'
    isValid = false
  }

  // 验证手机号（如果有填写）
  if (registerForm.value.phone && !validatePhone(registerForm.value.phone)) {
    errors.value.phone = '请输入有效的手机号'
    isValid = false
  }

  // 验证用户协议
  if (!registerForm.value.agreement) {
    errors.value.agreement = '请阅读并同意用户协议和隐私政策'
    isValid = false
  }

  return isValid
}

// 邮箱格式验证
const validateEmail = (email: string) => {
  return /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(email)
}

// 手机号格式验证
const validatePhone = (phone: string) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

// 提交表单
const handleSubmit = async () => {
  if (!validateForm()) return

  isLoading.value = true

  try {
    // 调用注册API
    await userStore.register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      confirmPassword: registerForm.value.confirmPassword,
      email: registerForm.value.email || null,
      phone: registerForm.value.phone || null,
      nickname: registerForm.value.nickname || null,
      isAdmin: false, // 用户端固定为false
    })

    // 注册成功，跳转到首页
    router.push('/user')
  } catch (error: any) {
    console.error('注册失败', error)
    errors.value.general = error.message || '注册失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <!-- 导航栏 -->
    <div class="nav-bar">
      <div class="back-button" @click="goBack">
        <i class="fa-solid fa-chevron-left"></i>
        <span>返回</span>
      </div>
      <div class="title">注册</div>
      <div class="placeholder"></div>
    </div>

    <div class="register-container">
      <div class="register-header">
        <h1 class="register-title">创建账号</h1>
        <p class="register-subtitle">填写以下信息创建您的账号</p>
      </div>

      <form class="register-form" @submit.prevent="handleSubmit">
        <div v-if="errors.general" class="error-message general-error">
          {{ errors.general }}
        </div>

        <div class="form-group">
          <label for="username">用户名<span class="required">*</span></label>
          <input
            type="text"
            id="username"
            v-model="registerForm.username"
            placeholder="请输入用户名 (4-20个字符)"
            autocomplete="username"
          />
          <div v-if="errors.username" class="error-message">
            {{ errors.username }}
          </div>
        </div>

        <div class="form-group">
          <label for="password">密码<span class="required">*</span></label>
          <input
            type="password"
            id="password"
            v-model="registerForm.password"
            placeholder="请输入密码 (6-20个字符)"
            autocomplete="new-password"
          />
          <div v-if="errors.password" class="error-message">
            {{ errors.password }}
          </div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认密码<span class="required">*</span></label>
          <input
            type="password"
            id="confirmPassword"
            v-model="registerForm.confirmPassword"
            placeholder="请再次输入密码"
            autocomplete="new-password"
          />
          <div v-if="errors.confirmPassword" class="error-message">
            {{ errors.confirmPassword }}
          </div>
        </div>

        <div class="form-group">
          <label for="email">电子邮箱</label>
          <input
            type="email"
            id="email"
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            autocomplete="email"
          />
          <div v-if="errors.email" class="error-message">
            {{ errors.email }}
          </div>
        </div>

        <div class="form-group">
          <label for="phone">手机号码</label>
          <input
            type="tel"
            id="phone"
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            autocomplete="tel"
          />
          <div v-if="errors.phone" class="error-message">
            {{ errors.phone }}
          </div>
        </div>

        <div class="form-group">
          <label for="nickname">昵称</label>
          <input
            type="text"
            id="nickname"
            v-model="registerForm.nickname"
            placeholder="请输入昵称"
            autocomplete="nickname"
          />
        </div>

        <div class="agreement-container">
          <label class="agreement-label">
            <input type="checkbox" v-model="registerForm.agreement" />
            <span>我已阅读并同意 <a href="#">用户协议</a> 和 <a href="#">隐私政策</a></span>
          </label>
          <div v-if="errors.agreement" class="error-message">
            {{ errors.agreement }}
          </div>
        </div>

        <button type="submit" class="primary-button" :disabled="isLoading">
          <span v-if="!isLoading">注册</span>
          <span v-else class="loading-spinner">
            <i class="fa-solid fa-spinner fa-spin"></i>
          </span>
        </button>
      </form>

      <div class="login-link">
        <p>
          已有账号？
          <a @click="goToLogin">登录</a>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
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

.register-container {
  flex: 1;
  padding: 20px 20px;
  overflow-y: auto;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-title {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 10px;
}

.register-subtitle {
  color: #8e8e93;
  font-size: 16px;
}

.register-form {
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

.required {
  color: #ff3b30;
  margin-left: 4px;
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

.agreement-container {
  margin-bottom: 24px;
}

.agreement-label {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
  color: #8e8e93;
}

.agreement-label input {
  margin-right: 8px;
  margin-top: 3px;
}

.agreement-label a {
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

.login-link {
  text-align: center;
  margin-top: 24px;
  margin-bottom: 40px;
  font-size: 15px;
}

.login-link a {
  color: #007aff;
  text-decoration: none;
  cursor: pointer;
}
</style>
