<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyReports } from '@/api/review'
import { useUserStore } from '@/stores/user'
import type { ReviewReport } from '@/types/review'

const router = useRouter()
const userStore = useUserStore()

// 数据
const reports = ref<ReviewReport[]>([])
const loading = ref(false)
const noData = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 获取我的举报列表
const fetchMyReports = async () => {
  loading.value = true
  noData.value = false
  
  try {
    // 检查用户是否已登录
    if (!userStore.isLoggedIn) {
      await userStore.fetchUserInfo()
      
      if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录后再查看您的举报记录')
        router.push('/login?redirect=/my-reports')
        return
      }
    }
    
    const res = await getMyReports(page.value, size.value)
    
    if (res.data && res.data.records) {
      reports.value = res.data.records
      total.value = res.data.total
    } else if (res.data && Array.isArray(res.data)) {
      reports.value = res.data
      total.value = res.data.length
    } else {
      reports.value = []
      total.value = 0
    }
    
    if (reports.value.length === 0) {
      noData.value = true
    }
  } catch (error) {
    console.error('获取举报列表失败:', error)
    ElMessage.error('获取举报列表失败，请稍后重试')
    noData.value = true
  } finally {
    loading.value = false
  }
}

// 处理页码变化
const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchMyReports()
}

// 获取举报状态显示
const getReportStatusDisplay = (status: number) => {
  switch (status) {
    case 0:
      return { text: '待处理', type: 'warning' }
    case 1:
      return { text: '已通过', type: 'success' }
    case 2:
      return { text: '已拒绝', type: 'danger' }
    default:
      return { text: '未知状态', type: 'info' }
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 页面初始化时获取数据
onMounted(() => {
  fetchMyReports()
})
</script>

<template>
  <div class="my-reports-container" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-button" @click="router.push('/profile')">
        <i class="fas fa-chevron-left"></i>
      </div>
      <div class="page-title">我的举报</div>
      <div class="placeholder"></div>
    </div>
    
    <!-- 举报列表内容 -->
    <div class="reports-content">
      <div v-if="!noData" class="reports-list">
        <div v-for="report in reports" :key="report.id" class="report-card">
          <div class="report-header">
            <div class="report-title">举报ID: {{ report.id }}</div>
            <div class="report-status">
              <el-tag 
                :type="getReportStatusDisplay(report.status).type"
                size="small"
              >
                {{ getReportStatusDisplay(report.status).text }}
              </el-tag>
            </div>
          </div>
          
          <div class="report-content">
            <div class="report-item">
              <div class="report-label">举报原因:</div>
              <div class="report-value">{{ report.reason }}</div>
            </div>
            
            <div class="report-item" v-if="report.reasonDetail">
              <div class="report-label">详细说明:</div>
              <div class="report-value">{{ report.reasonDetail }}</div>
            </div>
            
            <div class="report-item">
              <div class="report-label">举报时间:</div>
              <div class="report-value">{{ formatDate(report.createdAt) }}</div>
            </div>
            
            <div class="report-item" v-if="report.status !== 0">
              <div class="report-label">处理时间:</div>
              <div class="report-value">{{ formatDate(report.updatedAt) }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="pagination-container" v-if="total > size">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="handlePageChange"
        />
      </div>
      
      <!-- 空状态 -->
      <el-empty v-if="noData" description="您暂未提交任何举报">
        <el-button type="primary" @click="router.push('/venues')">去浏览场馆</el-button>
      </el-empty>
    </div>
    
    <!-- iOS底部标签栏 -->
    <div class="ios-tab-bar">
      <a class="tab-item" @click="router.push('/')">
        <i class="fas fa-home"></i>
        <span>首页</span>
      </a>
      <a class="tab-item" @click="router.push('/orders')">
        <i class="fas fa-calendar-alt"></i>
        <span>订单</span>
      </a>
      <a class="tab-item" @click="router.push('/message')">
        <i class="fas fa-bell"></i>
        <span>消息</span>
      </a>
      <a class="tab-item active" @click="router.push('/profile')">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.my-reports-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 70px;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-button {
  font-size: 18px;
  width: 30px;
  cursor: pointer;
}

.page-title {
  flex-grow: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}

.placeholder {
  width: 30px;
}

.reports-content {
  padding: 15px;
}

.reports-list {
  margin-bottom: 20px;
}

.report-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
  overflow: hidden;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f2f6fc;
  background-color: #f8f9fa;
}

.report-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.report-content {
  padding: 15px;
}

.report-item {
  margin-bottom: 10px;
  display: flex;
}

.report-label {
  flex: 0 0 80px;
  color: #909399;
  font-size: 14px;
}

.report-value {
  flex: 1;
  color: #303133;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  margin-bottom: 20px;
}

/* iOS底部标签栏样式 */
.ios-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  background-color: white;
  box-shadow: 0 -1px 5px rgba(0, 0, 0, 0.1);
  padding: 10px 0;
  z-index: 100;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #8e8e93;
  font-size: 10px;
  padding: 5px 0;
  cursor: pointer;
}

.tab-item i {
  font-size: 20px;
  margin-bottom: 3px;
}

.tab-item.active {
  color: #409eff;
}
</style> 