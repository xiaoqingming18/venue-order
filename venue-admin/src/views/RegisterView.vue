<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { RegisterParams } from '@/types/user'

// 用户仓库
const userStore = useUserStore()

// 定义表单数据
const registerForm = reactive<RegisterParams>({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: '',
})

// 错误信息
const errorMessage = ref('')
const showError = ref(false)

// 表单引用
const formRef = ref<FormInstance>()

// 路由器
const router = useRouter()

// 表单验证规则
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度须在4-20个字符之间', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度须在6-20个字符之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  email: [
    { required: false, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  phone: [
    { required: false, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur',
    },
  ],
})

// 注册方法
const handleRegister = async (formEl: FormInstance | undefined) => {
  if (!formEl) return

  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        showError.value = false

        // 使用用户仓库注册
        const success = await userStore.register(registerForm)

        if (success) {
          // 跳转到首页
          router.push('/')
        }
      } catch (error: any) {
        // 显示错误信息
        errorMessage.value = error.message || '注册失败，请稍后再试'
        showError.value = true
      }
    }
  })
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <div class="register-logo">
          <i class="fas fa-landmark text-primary"></i>
          <span class="register-logo-text">场馆预约系统</span>
        </div>
        <h1 class="register-title">注册新账号</h1>
        <p class="register-subtitle">填写下列信息创建您的账号</p>
      </div>

      <div class="register-body">
        <!-- 错误信息显示 -->
        <div v-if="showError" class="error-message">
          <i class="fas fa-exclamation-circle mr-2"></i>
          <span>{{ errorMessage }}</span>
        </div>

        <el-form
          ref="formRef"
          :model="registerForm"
          :rules="rules"
          class="register-form"
          @submit.prevent="handleRegister(formRef)"
        >
          <el-form-item prop="username">
            <div class="input-icon-wrapper">
              <i class="fas fa-user input-icon"></i>
              <el-input
                v-model="registerForm.username"
                placeholder="用户名 (4-20个字符)"
                size="large"
                clearable
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-icon-wrapper">
              <i class="fas fa-lock input-icon"></i>
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码 (6-20个字符)"
                size="large"
                show-password
              />
            </div>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <div class="input-icon-wrapper">
              <i class="fas fa-lock input-icon"></i>
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                size="large"
                show-password
              />
            </div>
          </el-form-item>

          <el-form-item prop="email">
            <div class="input-icon-wrapper">
              <i class="fas fa-envelope input-icon"></i>
              <el-input
                v-model="registerForm.email"
                placeholder="电子邮箱 (可选)"
                size="large"
                clearable
              />
            </div>
          </el-form-item>

          <el-form-item prop="phone">
            <div class="input-icon-wrapper">
              <i class="fas fa-mobile-alt input-icon"></i>
              <el-input
                v-model="registerForm.phone"
                placeholder="手机号码 (可选)"
                size="large"
                clearable
              />
            </div>
          </el-form-item>

          <el-form-item prop="nickname">
            <div class="input-icon-wrapper">
              <i class="fas fa-id-card input-icon"></i>
              <el-input
                v-model="registerForm.nickname"
                placeholder="昵称 (可选)"
                size="large"
                clearable
              />
            </div>
          </el-form-item>

          <el-button
            type="primary"
            class="register-btn"
            size="large"
            @click="handleRegister(formRef)"
          >
            注册
          </el-button>
        </el-form>

        <div class="login-link">
          <span>已有账号?</span>
          <a href="#" @click.prevent="goToLogin">登录</a>
        </div>
      </div>

      <div class="register-footer">
        <p>© {{ new Date().getFullYear() }} 场馆预约系统 | 技术支持: 场馆开发团队</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
:root {
  --primary-color: #3f51b5;
  --primary-dark: #303f9f;
  --danger-color: #ef4444;
  --gray-100: #f3f4f6;
  --gray-200: #e5e7eb;
  --gray-300: #d1d5db;
  --gray-400: #9ca3af;
  --gray-500: #6b7280;
  --gray-700: #374151;
  --gray-900: #111827;
}

.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 450px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin: 0 auto; /* 水平居中 */
}

.register-header {
  padding: 30px 30px 20px;
  text-align: center;
}

.register-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: var(--primary-color);
  font-size: 1.8rem;
}

.register-logo-text {
  margin-left: 8px;
  font-weight: 600;
  font-size: 1.5rem;
}

.register-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--gray-900);
  margin-bottom: 8px;
}

.register-subtitle {
  color: var(--gray-500);
  margin-bottom: 0;
}

.register-body {
  padding: 20px 30px 30px;
}

.register-form {
  margin-bottom: 25px;
  width: 100%; /* 表单宽度100% */
}

/* 确保表单项水平居中 */
.el-form-item {
  margin-bottom: 20px;
  width: 100%;
}

.input-icon-wrapper {
  position: relative;
  width: 100%; /* 输入框容器宽度100% */
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--gray-400);
  z-index: 2;
}

/* 设置输入框样式 */
.el-input :deep(.el-input__wrapper) {
  padding-left: 35px;
  width: 100%; /* 输入框宽度100% */
  box-sizing: border-box;
}

/* 固定输入框宽度 */
.el-input {
  width: 100%;
}

/* 防止输入框宽度变化 */
.el-input :deep(.el-input__inner) {
  width: 100%;
  box-sizing: border-box;
}

.register-btn {
  height: 48px;
  width: 100%;
  font-size: 1rem;
  font-weight: 600;
  margin-top: 10px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 0.9rem;
}

.login-link a {
  color: var(--primary-color);
  margin-left: 5px;
  text-decoration: none;
  font-weight: 600;
}

.register-footer {
  padding: 20px 30px;
  text-align: center;
  border-top: 1px solid var(--gray-200);
  font-size: 0.9rem;
  color: var(--gray-500);
}

.error-message {
  background-color: rgba(239, 68, 68, 0.1);
  color: var(--danger-color);
  padding: 12px 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.error-message i {
  margin-right: 8px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-container {
    padding: 10px;
  }

  .register-card {
    max-width: 100%;
  }

  .register-header,
  .register-body,
  .register-footer {
    padding: 20px;
  }
}
</style>
