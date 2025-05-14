<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBookingOrderById, payBooking, cancelBooking, payBookingWithAlipay } from '@/api/booking'
import type { BookingOrder } from '@/types/booking'

const route = useRoute()
const router = useRouter()

// 数据相关
const orderId = ref<number>(Number(route.query.orderId || 0))
const orderInfo = ref<BookingOrder | null>(null)
const loading = ref(false)

// 获取订单详情
const fetchOrderDetail = async () => {
  if (!orderId.value) {
    ElMessage.error('订单ID不存在')
    return
  }
  
  loading.value = true
  try {
    const res = await getBookingOrderById(orderId.value)
    orderInfo.value = res.data
  } catch (error) {
    console.error('获取订单详情失败', error)
    ElMessage.error('获取订单详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 支付订单
const handlePayOrder = async () => {
  try {
    loading.value = true
    // 调用支付宝支付接口
    const res = await payBookingWithAlipay(orderId.value)
    
    if (res && res.data) {
      // 创建临时表单元素
      const div = document.createElement('div')
      div.innerHTML = res.data
      document.body.appendChild(div)
      
      // 获取支付表单
      const form = div.getElementsByTagName('form')[0]
      
      if (form) {
        console.log('找到支付表单，准备提交')
        
        // 阻止表单默认action中可能的URL编码问题
        const formAction = form.getAttribute('action')
        if (formAction && formAction.includes('%22')) {
          console.log('检测到action存在URL编码问题，正在修复')
          // 修复action URL中的编码问题
          const fixedAction = decodeURIComponent(formAction)
                              .replace(/\\"/g, '')
                              .replace(/"/g, '')
          form.setAttribute('action', fixedAction)
        }
        
        // 确保表单提交到新窗口
        form.setAttribute('target', '_blank')
        
        // 直接提交表单
        form.submit()
        
        // 清理临时DOM元素
        document.body.removeChild(div)
        
        ElMessage.success('支付页面已打开，请在新窗口中完成支付')
      } else {
        throw new Error('支付表单加载失败，未找到表单元素')
      }
    } else {
      throw new Error('支付接口返回数据格式错误')
    }
  } catch (error) {
    console.error('支付失败', error)
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 取消订单
const handleCancelOrder = async () => {
  try {
    await cancelBooking(orderId.value)
    ElMessage.success('取消预约成功')
    fetchOrderDetail() // 刷新订单状态
  } catch (error) {
    console.error('取消预约失败', error)
    ElMessage.error('取消预约失败，请稍后重试')
  }
}

// 返回首页
const goToHome = () => {
  router.push('/')
}

// 查看所有订单
const goToOrders = () => {
  router.push('/orders')
}

// 获取订单状态文本
const getOrderStatusText = (status: number) => {
  const statusMap = {
    0: '已取消',
    1: '待支付',
    2: '已支付',
    3: '已完成',
    4: '已退款'
  }
  return statusMap[status as keyof typeof statusMap] || '未知状态'
}

// 页面初始化
onMounted(() => {
  fetchOrderDetail()
})
</script>

<template>
  <div class="booking-success-container" v-loading="loading">
    <!-- iOS状态栏 -->
    <div class="ios-status-bar">
      <div class="time">9:41</div>
      <div class="status-icons">
        <i class="fas fa-signal"></i>
        <i class="fas fa-wifi"></i>
        <i class="fas fa-battery-full"></i>
      </div>
    </div>
    
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-button" @click="goToHome">
        <i class="fas fa-chevron-left"></i>
      </div>
      <div class="page-title">预约详情</div>
    </div>
    
    <div v-if="orderInfo" class="success-content">
      <!-- 状态展示 -->
      <div class="status-card">
        <i class="fas fa-check-circle success-icon" v-if="orderInfo.status > 0"></i>
        <i class="fas fa-times-circle cancel-icon" v-else></i>
        <h2 class="status-text">预约{{ orderInfo.status > 0 ? '成功' : '已取消' }}</h2>
        <p class="status-desc">
          {{ orderInfo.status === 1 ? '请尽快完成支付，以确保预约有效' : getOrderStatusText(orderInfo.status) }}
        </p>
      </div>
      
      <!-- 订单信息 -->
      <div class="order-card">
        <h3 class="card-title">预约信息</h3>
        <div class="order-info">
          <div class="info-item">
            <span class="info-label">预约编号</span>
            <span class="info-value">{{ orderInfo.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">场馆名称</span>
            <span class="info-value">{{ orderInfo.venueName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">场馆地址</span>
            <span class="info-value">{{ orderInfo.venueAddress }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">预约日期</span>
            <span class="info-value">{{ orderInfo.bookingDate }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">预约时间</span>
            <span class="info-value">{{ orderInfo.startTime }} - {{ orderInfo.endTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">订单状态</span>
            <span class="info-value">{{ getOrderStatusText(orderInfo.status) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付状态</span>
            <span class="info-value">{{ orderInfo.paymentStatus === 1 ? '已支付' : '未支付' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">预约时间</span>
            <span class="info-value">{{ orderInfo.createdAt }}</span>
          </div>
          <div class="info-item total-amount">
            <span class="info-label">订单金额</span>
            <span class="info-value price">¥{{ orderInfo.totalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button 
          v-if="orderInfo.status === 1 && orderInfo.paymentStatus === 0" 
          class="action-btn primary-btn"
          @click="handlePayOrder"
        >
          立即支付
        </button>
        <button 
          v-if="orderInfo.status === 1 || orderInfo.status === 2" 
          class="action-btn cancel-btn"
          @click="handleCancelOrder"
        >
          取消预约
        </button>
        <button class="action-btn default-btn" @click="goToOrders">
          查看所有订单
        </button>
        <button class="action-btn default-btn" @click="goToHome">
          返回首页
        </button>
      </div>
    </div>
    
    <div v-else-if="!loading" class="empty-state">
      <i class="fas fa-calendar-times"></i>
      <p>订单信息不存在或已被删除</p>
      <button class="action-btn default-btn" @click="goToHome">返回首页</button>
    </div>
  </div>
</template>

<style scoped>
.booking-success-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 30px;
}

.ios-status-bar {
  display: flex;
  justify-content: space-between;
  background-color: #ffffff;
  padding: 10px 15px 5px;
  font-size: 12px;
}

.ios-status-bar .status-icons {
  display: flex;
  gap: 5px;
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
}

.page-title {
  flex-grow: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}

.success-content {
  padding: 15px;
}

.status-card {
  background-color: #ffffff;
  border-radius: 10px;
  padding: 30px 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  text-align: center;
}

.success-icon {
  font-size: 60px;
  color: #67c23a;
  margin-bottom: 15px;
}

.cancel-icon {
  font-size: 60px;
  color: #f56c6c;
  margin-bottom: 15px;
}

.status-text {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 10px;
}

.status-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.order-card {
  background-color: #ffffff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 18px;
  margin: 0 0 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #909399;
  font-size: 14px;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.total-amount {
  margin-top: 10px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.total-amount .info-label {
  font-weight: 600;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 15px 0;
}

.action-btn {
  padding: 12px 0;
  border-radius: 25px;
  font-size: 16px;
  border: none;
  cursor: pointer;
}

.primary-btn {
  background-color: #409eff;
  color: #ffffff;
}

.cancel-btn {
  background-color: #f56c6c;
  color: #ffffff;
}

.default-btn {
  background-color: #ffffff;
  color: #409eff;
  border: 1px solid #e4e7ed;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #909399;
}

.empty-state i {
  font-size: 60px;
  margin-bottom: 20px;
}
</style> 