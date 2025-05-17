<template>
  <div class="feedback-container">
    <van-nav-bar 
      title="问题反馈" 
      left-arrow
      @click-left="goBack"
      fixed
    />
    
    <van-tabs v-model:active="active" animated @change="handleTabChange">
      <van-tab title="提交反馈">
        <div class="feedback-form">
          <van-form @submit="onSubmit">
            <van-cell-group inset>
              <van-field
                v-model="feedbackForm.title"
                name="title"
                label="标题"
                placeholder="请输入反馈标题"
                :rules="[{ required: true, message: '请输入反馈标题' }]"
              />
              
              <van-field name="type" label="类型">
                <template #input>
                  <select 
                    v-model="feedbackForm.type" 
                    class="feedback-select"
                    required
                  >
                    <option value="">请选择反馈类型</option>
                    <option v-for="item in typeColumns" :key="item.value" :value="item.value">
                      {{ item.text }}
                    </option>
                  </select>
                </template>
              </van-field>
              
              <van-field
                v-model="feedbackForm.content"
                rows="4"
                autosize
                type="textarea"
                name="content"
                label="内容"
                placeholder="请详细描述您遇到的问题或建议"
                :rules="[{ required: true, message: '请输入反馈内容' }]"
              />
              
              <van-field
                v-model="feedbackForm.contact"
                name="contact"
                label="联系方式"
                placeholder="选填，便于我们与您联系"
              />
              
              <van-field name="uploader" label="上传图片">
                <template #input>
                  <van-uploader
                    v-model="fileList"
                    :max-count="3"
                    :after-read="afterRead"
                  />
                </template>
              </van-field>
            </van-cell-group>
            
            <div style="margin: 16px;">
              <van-button round block type="primary" native-type="submit">
                提交反馈
              </van-button>
            </div>
          </van-form>
        </div>
      </van-tab>
      
      <van-tab title="我的反馈">
        <div class="feedback-list">
          <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
            <van-list
              v-model:loading="loading"
              :finished="finished"
              finished-text="没有更多了"
              @load="onLoad"
            >
              <template v-if="!loading">
              <van-empty v-if="feedbackList.length === 0" description="暂无反馈记录" />
              
                <div v-else class="feedback-items">
              <van-cell
                v-for="item in feedbackList"
                :key="item.id"
                :title="item.title"
                :label="item.createdAt"
                is-link
                @click="goToDetail(item.id)"
              >
                <template #value>
                      <div class="status-wrapper">
                  <span :class="['status', 'status-' + item.status]">{{ item.statusName }}</span>
                        <span v-if="item.status === 2 && !viewedFeedbacks.has(item.id)" class="new-reply-dot"></span>
                      </div>
                </template>
              </van-cell>
                </div>
              </template>
              
              <div v-else class="loading-container">
                <van-loading type="spinner" size="24px">加载中...</van-loading>
              </div>
            </van-list>
          </van-pull-refresh>
        </div>
      </van-tab>
    </van-tabs>

    <!-- iOS底部标签栏 -->
    <div class="ios-tab-bar">
      <a class="tab-item" @click="navigateTo('/')">
        <i class="fas fa-home"></i>
        <span>首页</span>
      </a>
      <a class="tab-item" @click="navigateTo('/orders')">
        <i class="fas fa-calendar-alt"></i>
        <span>订单</span>
      </a>
      <a class="tab-item" @click="navigateTo('/message')">
        <i class="fas fa-bell"></i>
        <span>消息</span>
      </a>
      <a class="tab-item" @click="navigateTo('/profile')">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { submitFeedback, getUserFeedbackList } from '@/api/feedback.js'
import type { UserFeedback, FeedbackSubmitRequest } from '@/types/feedback'

const router = useRouter()
const active = ref(0)
const typeColumns = [
  { text: '问题反馈', value: 1 },
  { text: '功能建议', value: 2 },
  { text: '投诉', value: 3 },
  { text: '其他', value: 4 }
]

// 反馈表单
const feedbackForm = reactive<FeedbackSubmitRequest>({
  title: '',
  type: null,
  content: '',
  contact: '',
  images: []
})

// 文件上传
const fileList = ref([])
const afterRead = (file: any) => {
  // 这里应该是上传图片的逻辑，实际开发中应该是调用上传API
  // 这里简化为直接使用本地URL
  file.status = 'done'
  if (!feedbackForm.images) {
    feedbackForm.images = []
  }
  feedbackForm.images.push(file.content)
}

// 反馈列表
const feedbackList = ref<UserFeedback[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(8)

// 已查看过的反馈ID集合
const viewedFeedbacks = reactive(new Set<number>())

// 加载缓存的已查看反馈记录
const loadViewedFeedbacks = () => {
  const cachedViewedFeedbacks = localStorage.getItem('viewedFeedbacks')
  if (cachedViewedFeedbacks) {
    try {
      const feedbackIds = JSON.parse(cachedViewedFeedbacks)
      feedbackIds.forEach((id: number) => viewedFeedbacks.add(id))
    } catch (error) {
      console.error('解析已查看反馈记录失败', error)
    }
  }
}

// 保存已查看反馈记录到本地存储
const saveViewedFeedbacks = () => {
  localStorage.setItem('viewedFeedbacks', JSON.stringify(Array.from(viewedFeedbacks)))
}

// 提交反馈
const onSubmit = async () => {
  try {
    // 验证类型是否选择
    if (!feedbackForm.type) {
      showFailToast('请选择反馈类型');
      return;
    }
    
    const result = await submitFeedback({
      ...feedbackForm,
      type: Number(feedbackForm.type)
    })
    showSuccessToast('反馈提交成功')
    console.log('反馈提交成功，ID:', result)
    
    // 重置表单
    feedbackForm.title = ''
    feedbackForm.type = null
    feedbackForm.content = ''
    feedbackForm.contact = ''
    feedbackForm.images = []
    fileList.value = []
    
    // 切换到我的反馈列表
    active.value = 1
    
    // 完全重置列表状态
    refreshing.value = false
    loading.value = false
    finished.value = false
    pageNum.value = 1
    feedbackList.value = []
    
    // 添加延迟以确保后端数据已更新
    setTimeout(() => {
      loading.value = true
      onLoad()
    }, 500)
  } catch (error) {
    console.error('提交反馈失败:', error)
    showFailToast('提交失败，请重试')
  }
}

// 加载反馈列表
const onLoad = async () => {
  try {
    const { data } = await getUserFeedbackList(pageNum.value, pageSize.value)
    
    if (refreshing.value) {
      feedbackList.value = []
      refreshing.value = false
    }
    
    if (data.records && data.records.length > 0) {
      // 记录每个反馈的初始状态，用于检测变化
      const initialSize = feedbackList.value.length
      feedbackList.value.push(...data.records)
      pageNum.value++
    }
    
    loading.value = false
    if (feedbackList.value.length >= data.total) {
      finished.value = true
    }
  } catch (error) {
    loading.value = false
    showFailToast('加载失败，请重试')
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  finished.value = false
  pageNum.value = 1
  feedbackList.value = [] // 清空现有数据
  onLoad()
}

// 查看反馈详情
const goToDetail = (id: number) => {
  // 将该反馈标记为已查看
  viewedFeedbacks.add(id)
  saveViewedFeedbacks()
  
  router.push(`/feedback-detail/${id}`)
}

// 返回
const goBack = () => {
  router.back()
}

// 标签页切换监听
const handleTabChange = (index: number) => {
  if (index === 1) { // 切换到"我的反馈"标签
    // 重置列表状态并重新加载
    refreshing.value = false
    finished.value = false
    pageNum.value = 1
    feedbackList.value = []
    
    // 延迟一点点加载，避免UI卡顿
    setTimeout(() => {
      loading.value = true
      onLoad()
    }, 100)
  }
}

// 导航到指定路由
const navigateTo = (route: string) => {
  router.push(route)
}

onMounted(() => {
  // 加载已查看的反馈记录
  loadViewedFeedbacks()
  
  // 先设置加载状态
  loading.value = true
  
  // 默认加载第一页数据
  pageNum.value = 1
  feedbackList.value = []
  finished.value = false
  
  // 如果当前标签是"我的反馈"，则自动加载数据
  if (active.value === 1) {
    setTimeout(() => {
  onLoad()
    }, 100)
  } else {
    loading.value = false
  }
})
</script>

<style scoped>
.feedback-container {
  padding-top: 46px;
  padding-bottom: 70px; /* 添加底部padding，为底部导航栏留出空间 */
  min-height: 100vh;
  background-color: #f7f8fa;
}

.feedback-form {
  padding: 16px 0;
}

.feedback-list {
  min-height: calc(100vh - 100px);
}

.status-wrapper {
  display: flex;
  align-items: center;
}

.status {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.status-0 {
  background-color: #ebedf0;
  color: #969799;
}

.status-1 {
  background-color: #e6f7ff;
  color: #1989fa;
}

.status-2 {
  background-color: #f0f9eb;
  color: #07c160;
}

.status-3 {
  background-color: #f2f3f5;
  color: #646566;
}

.new-reply-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ee0a24;
  margin-left: 5px;
}

.feedback-select {
  width: 100%;
  height: 32px;
  padding: 0 8px;
  font-size: 14px;
  color: #323233;
  border: none;
  outline: none;
  background-color: transparent;
}

.type-popup {
  width: 80%;
  max-width: 300px;
  overflow: hidden;
  border-radius: 12px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  min-height: 100px;
}

.feedback-items {
  margin-bottom: 20px;
}

.feedback-list {
  min-height: calc(100vh - 100px);
  padding-bottom: 20px;
}

.ios-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid #e5e5e5;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #333;
}

.tab-item i {
  font-size: 20px;
  margin-bottom: 5px;
}

.tab-item span {
  font-size: 12px;
}
</style> 