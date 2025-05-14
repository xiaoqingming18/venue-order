<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const orderNo = ref('')
const loading = ref(false)
const result = ref('')
const logs = ref<string[]>([])

// 添加日志
const addLog = (message: string, type: 'info' | 'success' | 'error' = 'info') => {
  const timestamp = new Date().toLocaleTimeString()
  logs.value.unshift(`${timestamp} - ${message}`)
  console.log(`${type.toUpperCase()}: ${message}`)
}

// 模拟支付回调
const mockPaymentCallback = async () => {
  if (!orderNo.value) {
    ElMessage.warning('请输入订单号')
    return
  }
  
  loading.value = true
  result.value = ''
  
  try {
    addLog(`开始模拟支付回调，订单号: ${orderNo.value}`)
    
    const response = await axios.get(`/api/alipay/mock-notify?orderNo=${orderNo.value}`)
    
    addLog(`模拟回调响应: ${response.data}`, 'success')
    result.value = response.data
    
    ElMessage.success('模拟回调成功')
  } catch (error: any) {
    console.error('模拟回调失败:', error)
    addLog(`模拟回调失败: ${error.message}`, 'error')
    ElMessage.error(`模拟回调失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 直接更新订单状态
const directUpdateStatus = async () => {
  if (!orderNo.value) {
    ElMessage.warning('请输入订单号')
    return
  }
  
  loading.value = true
  result.value = ''
  
  try {
    addLog(`开始直接更新订单状态，订单号: ${orderNo.value}，目标状态: 2（已支付）`)
    
    const response = await axios.get(`/api/payment/test-update-status/${orderNo.value}/2`)
    
    addLog(`直接更新状态响应: ${response.data}`, 'success')
    result.value = response.data
    
    ElMessage.success('订单状态更新成功')
  } catch (error: any) {
    console.error('更新订单状态失败:', error)
    addLog(`更新订单状态失败: ${error.message}`, 'error')
    ElMessage.error(`更新订单状态失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 直接SQL更新订单状态
const directSqlUpdate = async () => {
  if (!orderNo.value) {
    ElMessage.warning('请输入订单号')
    return
  }
  
  loading.value = true
  result.value = ''
  
  try {
    addLog(`开始使用直接SQL更新订单状态，订单号: ${orderNo.value}`)
    
    const response = await axios.get(`/api/payment/direct-update/${orderNo.value}`)
    
    addLog(`直接SQL更新响应: ${response.data}`, 'success')
    result.value = response.data
    
    ElMessage.success('订单状态更新成功')
  } catch (error: any) {
    console.error('直接SQL更新失败:', error)
    addLog(`直接SQL更新失败: ${error.message}`, 'error')
    ElMessage.error(`直接SQL更新失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 从URL参数中获取订单号
onMounted(() => {
  const urlOrderNo = route.query.orderNo as string
  if (urlOrderNo) {
    orderNo.value = urlOrderNo
    addLog(`从URL获取订单号: ${urlOrderNo}`)
  }
})
</script>

<template>
  <div class="payment-callback-container">
    <div class="header">
      <h1>支付回调测试工具</h1>
      <p>用于测试支付宝回调接口</p>
    </div>
    
    <div class="callback-form">
      <div class="form-item">
        <label>订单号</label>
        <el-input v-model="orderNo" placeholder="请输入要测试的订单号"></el-input>
      </div>
      
      <div class="button-group">
        <el-button 
          type="primary" 
          :loading="loading"
          @click="mockPaymentCallback"
        >
          模拟支付回调
        </el-button>
        
        <el-button 
          type="success" 
          :loading="loading"
          @click="directUpdateStatus"
        >
          直接更新状态
        </el-button>
        
        <el-button 
          type="success" 
          :loading="loading"
          @click="directSqlUpdate"
        >
          直接SQL更新状态
        </el-button>
        
        <el-button @click="router.push('/orders')">
          返回订单列表
        </el-button>
      </div>
    </div>
    
    <div v-if="result" class="result-panel">
      <h3>回调结果</h3>
      <div class="result-content">{{ result }}</div>
    </div>
    
    <div class="log-panel">
      <h3>操作日志</h3>
      <div class="log-content">
        <div v-for="(log, index) in logs" :key="index" class="log-item">
          {{ log }}
        </div>
        <div v-if="logs.length === 0" class="empty-log">
          暂无日志记录
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.payment-callback-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  font-size: 24px;
  margin-bottom: 10px;
}

.header p {
  color: #666;
}

.callback-form {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 15px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.button-group {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.result-panel {
  background-color: #f0f9eb;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.result-panel h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
}

.result-content {
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}

.log-panel {
  background-color: #f4f4f5;
  border-radius: 8px;
  padding: 15px;
}

.log-panel h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
}

.log-content {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
}

.log-item {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  font-family: monospace;
  font-size: 14px;
}

.log-item:last-child {
  border-bottom: none;
}

.empty-log {
  padding: 20px;
  text-align: center;
  color: #999;
}
</style> 