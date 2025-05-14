<template>
  <div class="pay-test-container">
    <h1>支付测试页面</h1>
    
    <div class="test-options">
      <el-form label-width="120px">
        <el-form-item label="测试类型">
          <el-radio-group v-model="testType">
            <el-radio :label="1">使用真实订单ID</el-radio>
            <el-radio :label="2">使用直接API</el-radio>
            <el-radio :label="3">使用简化测试接口</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="订单ID" v-if="testType === 1">
          <el-input v-model="orderId" placeholder="输入一个有效的订单ID"></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="testPayment">测试支付</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="logs" v-if="logs.length > 0">
      <h3>测试日志:</h3>
      <div class="log-content">
        <p v-for="(log, index) in logs" :key="index" :class="log.type">
          {{ log.time }} - {{ log.message }}
        </p>
      </div>
    </div>
    
    <div class="direct-form" v-if="directForm">
      <h3>生成的支付表单:</h3>
      <div class="form-content">
        <pre>{{ directForm }}</pre>
        <el-button type="success" @click="submitDirectForm">提交表单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { payBookingWithAlipay } from '@/api/booking'
import axios from 'axios'

// 测试类型: 1=使用订单ID, 2=直接调用API, 3=使用简化测试接口
const testType = ref(1)
const orderId = ref('1')
const logs = ref<{time: string, message: string, type: string}[]>([])
const directForm = ref('')

// 添加日志
const addLog = (message: string, type: 'info' | 'success' | 'error' = 'info') => {
  const now = new Date()
  const time = `${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`
  logs.value.unshift({ time, message, type })
}

// 测试支付
const testPayment = async () => {
  try {
    addLog('开始测试支付流程')
    
    if (testType.value === 1 && (!orderId.value || isNaN(Number(orderId.value)))) {
      ElMessage.warning('请输入有效的订单ID')
      addLog('订单ID无效', 'error')
      return
    }
    
    if (testType.value === 1) {
      // 使用订单ID调用支付接口
      addLog(`使用订单ID: ${orderId.value} 调用支付接口`)
      const orderIdNum = Number(orderId.value)
      
      const res = await payBookingWithAlipay(orderIdNum)
      if (res && res.data) {
        addLog('成功获取支付表单', 'success')
        submitForm(res.data)
      } else {
        throw new Error('支付接口未返回表单数据')
      }
    } else if (testType.value === 2) {
      // 直接调用支付API
      addLog('直接调用支付API')
      
      // 创建一个测试支付请求
      const payData = {
        orderNo: `test${Date.now()}`,
        totalAmount: '0.01',
        subject: '测试订单',
        body: '测试支付'
      }
      
      addLog(`请求参数: ${JSON.stringify(payData)}`)
      
      // 获取token并添加到请求头
      const token = localStorage.getItem('userToken')
      const headers = token ? { 'Authorization': `Bearer ${token}` } : {}
      
      // 直接调用支付接口
      const res = await axios.post('/api/alipay/pay', payData, { 
        headers,
        responseType: 'text'
      })
      
      if (res.data) {
        addLog('成功获取支付表单', 'success')
        if (typeof res.data === 'string') {
          directForm.value = res.data
        } else if (res.data.code === 200 && res.data.data) {
          directForm.value = res.data.data
        } else {
          throw new Error('支付接口返回格式不正确')
        }
      } else {
        throw new Error('支付接口返回错误: ' + JSON.stringify(res.data))
      }
    } else {
      // 使用简化测试接口
      addLog('使用简化测试接口')
      
      // 获取token并添加到请求头
      const token = localStorage.getItem('userToken')
      const headers = token ? { 'Authorization': `Bearer ${token}` } : {}
      
      // 直接调用测试接口
      const res = await axios.get('/api/alipay/test', { 
        headers,
        responseType: 'text'
      })
      
      if (res.data) {
        addLog('成功获取测试表单', 'success')
        if (typeof res.data === 'string') {
          directForm.value = res.data
        } else if (res.data.code === 200 && res.data.data) {
          directForm.value = res.data.data
        } else {
          throw new Error('测试接口返回格式不正确')
        }
      } else {
        throw new Error('测试接口返回错误: ' + JSON.stringify(res.data))
      }
    }
  } catch (error) {
    console.error('支付测试失败', error)
    
    // 检查是否为401错误
    if (error.response && error.response.status === 401) {
      addLog(`支付测试失败: 未授权，请先登录`, 'error')
      ElMessage.error('请先登录后再进行支付测试')
    } else {
      addLog(`支付测试失败: ${error.message || '未知错误'}`, 'error')
      ElMessage.error('支付测试失败，请查看日志')
    }
  }
}

// 提交表单
const submitForm = (formHtml: string) => {
  try {
    addLog('开始提交支付表单')
    
    // 创建一个临时div直接插入到body
    const div = document.createElement('div')
    div.innerHTML = formHtml
    document.body.appendChild(div)
    
    // 获取表单并直接提交
    const form = div.getElementsByTagName('form')[0]
    if (form) {
      // 确保表单提交到新窗口
      form.setAttribute('target', '_blank')
      // 直接提交表单
      form.submit()
      // 移除临时创建的div
      document.body.removeChild(div)
      addLog('表单已提交，新窗口正在打开', 'success')
      ElMessage.success('表单已提交，请在新窗口中完成支付测试')
    } else {
      throw new Error('支付表单加载失败，未找到form元素')
    }
  } catch (error) {
    console.error('提交表单失败', error)
    addLog(`提交表单失败: ${error.message || '未知错误'}`, 'error')
    ElMessage.error('提交表单失败')
  }
}

// 提交直接表单
const submitDirectForm = () => {
  if (!directForm.value) {
    ElMessage.warning('没有可提交的表单')
    return
  }
  
  submitForm(directForm.value)
}
</script>

<style scoped>
.pay-test-container {
  max-width: 800px;
  margin: 30px auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.test-options {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.logs {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}

.log-content {
  font-family: monospace;
  font-size: 14px;
}

.log-content p {
  margin: 5px 0;
  padding: 5px;
  border-radius: 3px;
}

.log-content .info {
  color: #606266;
}

.log-content .success {
  color: #67c23a;
  background-color: #f0f9eb;
}

.log-content .error {
  color: #f56c6c;
  background-color: #fef0f0;
}

.direct-form {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.form-content {
  margin-top: 10px;
}

.form-content pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
  max-height: 200px;
  font-size: 12px;
}
</style> 