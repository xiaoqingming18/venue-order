<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

// 导航到登录页
const navigateToLogin = () => {
  router.push('/login')
}

// 导航到注册页
const navigateToRegister = () => {
  router.push('/register')
}

// 导航到场馆列表
const navigateToVenues = () => {
  router.push('/venues')
}
</script>

<template>
  <div class="welcome-container">
    <div class="logo-container">
      <div class="logo">
        <i class="fa-solid fa-calendar-check"></i>
      </div>
      <h1 class="app-name">场馆预约系统</h1>
      <p class="app-slogan">便捷预约，尽享精彩</p>
    </div>

    <div class="feature-list">
      <div class="feature-item">
        <div class="feature-icon">
          <i class="fa-solid fa-search"></i>
        </div>
        <div class="feature-text">
          <div class="feature-title">轻松浏览与搜索</div>
          <div class="feature-desc">多种方式快速找到心仪场馆</div>
        </div>
      </div>

      <div class="feature-item">
        <div class="feature-icon">
          <i class="fa-solid fa-calendar-alt"></i>
        </div>
        <div class="feature-text">
          <div class="feature-title">在线预约</div>
          <div class="feature-desc">随时随地，一键预约场地</div>
        </div>
      </div>

      <div class="feature-item">
        <div class="feature-icon">
          <i class="fa-solid fa-credit-card"></i>
        </div>
        <div class="feature-text">
          <div class="feature-title">便捷支付</div>
          <div class="feature-desc">支付宝安全支付，无忧退款</div>
        </div>
      </div>

      <div class="feature-item">
        <div class="feature-icon">
          <i class="fa-solid fa-bell"></i>
        </div>
        <div class="feature-text">
          <div class="feature-title">智能提醒</div>
          <div class="feature-desc">预约提醒，不错过每一次活动</div>
        </div>
      </div>
    </div>

    <div class="button-container">
      <button class="browse-button" @click="navigateToVenues">浏览场馆</button>
      <button class="primary-button" @click="navigateToLogin">登录</button>
      <button class="secondary-button" @click="navigateToRegister">注册新账号</button>
    </div>
  </div>
</template>

<style scoped>
.welcome-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100vh;
  padding: 40px 20px;
  background-color: #ffffff;
}

.logo-container {
  text-align: center;
  margin-top: 60px;
}

.logo {
  width: 120px;
  height: 120px;
  background-color: #007aff;
  border-radius: 30px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 25px rgba(0, 122, 255, 0.2);
}

.logo i {
  font-size: 60px;
  color: white;
}

.app-name {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.app-slogan {
  font-size: 16px;
  color: #8e8e93;
  margin-bottom: 40px;
}

.feature-list {
  margin: 30px 0;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.feature-icon {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background-color: rgba(0, 122, 255, 0.1);
  color: #007aff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 18px;
}

.feature-text {
  flex: 1;
}

.feature-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.feature-desc {
  font-size: 14px;
  color: #8e8e93;
}

.button-container {
  margin-top: auto;
  margin-bottom: 40px;
}

.browse-button {
  display: block;
  width: 100%;
  background-color: #4cd964;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 600;
  padding: 14px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.browse-button:hover {
  background-color: #3cc154;
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
  margin-bottom: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.primary-button:hover {
  background-color: #0066cc;
}

.secondary-button {
  display: block;
  width: 100%;
  background-color: rgba(0, 122, 255, 0.1);
  color: #007aff;
  border: none;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 600;
  padding: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.secondary-button:hover {
  background-color: rgba(0, 122, 255, 0.2);
}
</style>
