<!--
  支付成功页面 - 显示支付成功信息，并提供返回订单详情和订单列表的按钮
-->
<template>
  <div class="payment-success-container">
    <div class="payment-success-card">
      <div class="success-icon">
        <i class="el-icon-success"></i>
      </div>
      <h1 class="success-title">支付成功</h1>
      <p class="success-message">您的场馆预约已支付成功，感谢您的预约！</p>
      
      <div class="button-group">
        <el-button type="primary" @click="goToOrders">查看我的订单</el-button>
        <el-button @click="goToHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 获取可能存在的订单ID
const orderId = route.query.orderId

// 查看我的订单
const goToOrders = () => {
  if (orderId) {
    router.push(`/order/${orderId}`)
  } else {
    router.push('/orders')
  }
}

// 返回首页
const goToHome = () => {
  router.push('/')
}
</script>

<style scoped>
.payment-success-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #f5f7fa;
}

.payment-success-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
  width: 100%;
  max-width: 500px;
}

.success-icon {
  color: #67c23a;
  font-size: 80px;
  margin-bottom: 24px;
}

.success-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 16px;
}

.success-message {
  font-size: 16px;
  color: #606266;
  line-height: 1.5;
  margin: 0 0 30px;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

/* 移动端样式调整 */
@media (max-width: 576px) {
  .payment-success-card {
    padding: 30px 20px;
  }
  
  .success-icon {
    font-size: 60px;
    margin-bottom: 20px;
  }
  
  .success-title {
    font-size: 20px;
  }
  
  .button-group {
    flex-direction: column;
  }
}
</style> 