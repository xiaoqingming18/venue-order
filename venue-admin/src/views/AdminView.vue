<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const goToDashboard = () => {
  router.push('/dashboard')
}

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div class="admin-container">
    <el-card class="admin-card">
      <template #header>
        <div class="card-header">
          <h2>管理员控制面板</h2>
          <div class="admin-badge">
            <el-tag type="success">管理员</el-tag>
          </div>
        </div>
      </template>

      <div class="welcome-message">
        <p>
          您好，<strong>{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</strong
          >，欢迎访问管理员控制面板
        </p>
      </div>

      <div class="admin-section">
        <h3>系统管理</h3>
        <div class="admin-menu">
          <el-button-group>
            <el-button type="primary" @click="navigateTo('/home/users')">用户管理</el-button>
            <el-button type="primary" @click="navigateTo('/home/venues')">场馆管理</el-button>
            <el-button type="primary" @click="navigateTo('/home/bookings')">预约管理</el-button>
            <el-button type="primary">审核管理</el-button>
            <el-button type="primary" @click="navigateTo('/home/feedback')">反馈管理</el-button>
          </el-button-group>
        </div>
      </div>
      
      <div class="admin-section">
        <h3>场馆管理</h3>
        <div class="venue-management">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="venue-card" shadow="hover" @click="navigateTo('/home/venues')">
                <div class="venue-card-content">
                  <i class="el-icon-office-building"></i>
                  <h4>场馆管理</h4>
                  <p>管理场馆信息、状态和详情</p>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="venue-card" shadow="hover" @click="navigateTo('/home/venue-types')">
                <div class="venue-card-content">
                  <i class="el-icon-collection-tag"></i>
                  <h4>场馆类型</h4>
                  <p>管理场馆类型和基础价格</p>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="venue-card" shadow="hover" @click="navigateTo('/home/venues/create')">
                <div class="venue-card-content">
                  <i class="el-icon-plus"></i>
                  <h4>新增场馆</h4>
                  <p>创建新的场馆信息</p>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>

      <div class="admin-section">
        <h3>数据统计</h3>
        <div class="data-stats">
          <el-card class="stat-card">
            <h3>10</h3>
            <p>场馆数量</p>
          </el-card>
          <el-card class="stat-card">
            <h3>5</h3>
            <p>今日预约</p>
          </el-card>
          <el-card class="stat-card">
            <h3>98</h3>
            <p>注册用户</p>
          </el-card>
          <el-card class="stat-card">
            <h3>2</h3>
            <p>待审核</p>
          </el-card>
        </div>
      </div>

      <div class="admin-footer">
        <el-button @click="goToDashboard">返回仪表盘</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.admin-container {
  width: 100%;
  min-height: 100vh;
  padding: 20px;
  background-color: #f3f4f6;
}

.admin-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #303133;
}

.welcome-message {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
}

.admin-section {
  margin-bottom: 30px;
}

.admin-section h3 {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  color: #303133;
}

.admin-menu {
  margin-bottom: 20px;
}

.venue-management {
  margin-top: 20px;
}

.venue-card {
  height: 160px;
  cursor: pointer;
  transition: all 0.3s;
}

.venue-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.venue-card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.venue-card-content i {
  font-size: 30px;
  margin-bottom: 10px;
  color: #409EFF;
}

.venue-card-content h4 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.venue-card-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.data-stats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.stat-card {
  text-align: center;
}

.stat-card h3 {
  font-size: 24px;
  color: #409eff;
  margin: 0;
  margin-bottom: 5px;
  border: none;
}

.stat-card p {
  margin: 0;
  color: #606266;
}

.admin-footer {
  margin-top: 30px;
  text-align: center;
}
</style>
