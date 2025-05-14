<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { LoginParams } from '@/types/user'

// 用户仓库
const userStore = useUserStore()

// 定义表单数据
const loginForm = reactive<LoginParams>({
  username: '',
  password: '',
  remember: false,
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
})

// 登录方法
const handleLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return

  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        showError.value = false

        // 使用用户仓库登录
        const success = await userStore.login(loginForm)

        if (success) {
          // 跳转到首页
          router.push('/home')
        }
      } catch (error: any) {
        // 显示错误信息
        errorMessage.value = error.message || '登录失败，请稍后再试'
        showError.value = true
      }
    }
  })
}

// 检查是否有保存的用户名
const checkRememberedUser = () => {
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.remember = true
  }
}

// 忘记密码处理
const forgotPassword = () => {
  ElMessage.info('忘记密码功能尚未实现，请联系管理员')
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 页面加载时检查
checkRememberedUser()
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <i class="fas fa-landmark text-primary"></i>
          <span class="login-logo-text">场馆预约系统</span>
        </div>
        <h1 class="login-title">管理员登录</h1>
        <p class="login-subtitle">请输入您的管理员账号和密码</p>
      </div>

      <div class="login-body">
        <!-- 错误信息显示 -->
        <div v-if="showError" class="error-message">
          <i class="fas fa-exclamation-circle mr-2"></i>
          <span>{{ errorMessage }}</span>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin(formRef)"
        >
          <el-form-item prop="username">
            <div class="input-icon-wrapper">
              <i class="fas fa-user input-icon"></i>
              <el-input
                v-model="loginForm.username"
                placeholder="管理员账号"
                size="large"
                clearable
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-icon-wrapper">
              <i class="fas fa-lock input-icon"></i>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                size="large"
                show-password
              />
            </div>
          </el-form-item>

          <div class="remember-forgot">
            <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
            <a href="#" class="forgot-password" @click.prevent="forgotPassword">忘记密码?</a>
          </div>

          <el-button type="primary" class="login-btn" size="large" @click="handleLogin(formRef)">
            登录
          </el-button>
        </el-form>

        <div class="register-link">
          <span>还没有账号?</span>
          <a href="#" @click.prevent="goToRegister">注册新账号</a>
        </div>

        <div class="login-divider">
          <span>或使用其他方式登录</span>
        </div>

        <div class="oauth-login">
          <button class="oauth-btn" title="微信登录">
            <i class="fab fa-weixin" style="color: #07c160; font-size: 1.6rem"></i>
          </button>
          <button class="oauth-btn" title="QQ登录">
            <i class="fab fa-qq" style="color: #12b7f5; font-size: 1.6rem"></i>
          </button>
          <button class="oauth-btn" title="支付宝登录">
            <i class="fab fa-alipay" style="color: #1678ff; font-size: 1.6rem"></i>
          </button>
        </div>
      </div>

      <div class="login-footer">
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

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin: 0 auto; /* 水平居中 */
}

.login-header {
  padding: 30px 30px 20px;
  text-align: center;
}

.login-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: var(--primary-color);
  font-size: 1.8rem;
}

.login-logo-text {
  margin-left: 8px;
  font-weight: 600;
  font-size: 1.5rem;
}

.login-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--gray-900);
  margin-bottom: 8px;
}

.login-subtitle {
  color: var(--gray-500);
  margin-bottom: 0;
}

.login-body {
  padding: 20px 30px 30px;
}

.login-form {
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

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  font-size: 0.9rem;
}

.forgot-password {
  color: var(--primary-color);
  text-decoration: none;
}

.login-btn {
  height: 48px;
  width: 100%;
  font-size: 1rem;
  font-weight: 600;
}

.register-link {
  text-align: center;
  margin-bottom: 20px;
  font-size: 0.9rem;
}

.register-link a {
  color: var(--primary-color);
  margin-left: 5px;
  text-decoration: none;
  font-weight: 600;
}

.login-divider {
  display: flex;
  align-items: center;
  margin: 25px 0;
  color: var(--gray-500);
}

.login-divider::before,
.login-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: var(--gray-300);
}

.login-divider span {
  padding: 0 15px;
  font-size: 0.9rem;
}

.oauth-login {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.oauth-btn {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: white;
  border: 1px solid var(--gray-300);
  cursor: pointer;
  transition: all 0.3s;
}

.oauth-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.login-footer {
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
  .login-container {
    padding: 10px;
  }

  .login-card {
    max-width: 100%;
  }

  .login-header,
  .login-body,
  .login-footer {
    padding: 20px;
  }
}
</style>
