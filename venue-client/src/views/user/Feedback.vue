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
                      <span :class="['status', 'status-' + item.status]">{{ item.statusName }}</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
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
    console.log('开始加载反馈列表，页码:', pageNum.value, '每页大小:', pageSize.value);
    
    loading.value = true
    
    // 如果是第一页，清空现有列表以确保显示最新数据
    if (pageNum.value === 1 && !refreshing.value) {
      feedbackList.value = [];
      finished.value = false;
    }
    
    const { data } = await getUserFeedbackList(pageNum.value, pageSize.value);
    
    console.log('获取反馈列表成功，数据:', data);
    
    // 下拉刷新时清空列表
    if (refreshing.value) {
      feedbackList.value = []
      refreshing.value = false
    }
    
    // 确保data和records都存在
    if (data && data.records) {
      console.log(`服务器返回数据: 总数=${data.total}, 本页记录数=${data.records.length}`);
      
      // 如果返回的记录为空
      if (data.records.length === 0) {
        // 如果总数为0，说明没有数据
        if (data.total === 0) {
          console.log('没有反馈数据');
          finished.value = true;
        } 
        // 如果不是第一页，且返回空数组，回到第一页重试
        else if (pageNum.value > 1) {
          console.log('页码过大，返回第一页');
          pageNum.value = 1;
          loading.value = false;
          onLoad();
          return;
        } else {
          console.log('数据异常: 总数不为0但返回空数组');
          finished.value = true;
        }
      } else {
        // 将日期从LocalDateTime格式转换为更友好的显示格式
        const formattedRecords = data.records.map(item => ({
          ...item,
          createdAt: formatDateTime(item.createdAt)
        }));
        
        // 添加数据到列表
        feedbackList.value.push(...formattedRecords);
        console.log('当前列表数据:', feedbackList.value);
        
        // 判断是否还有更多数据
        if (feedbackList.value.length >= data.total) {
          console.log('已加载全部数据，标记为finished');
          finished.value = true;
        } else {
          // 增加页码
          pageNum.value++;
          console.log('还有更多数据，页码增加到:', pageNum.value);
          finished.value = false;
        }
      }
    } else {
      console.error('响应数据格式不正确:', data);
      finished.value = true;
    }
  } catch (error) {
    console.error('加载反馈列表失败:', error);
    if (error.response) {
      console.error('错误状态码:', error.response.status);
      console.error('错误详情:', error.response.data);
    }
    
    // 显示友好的错误信息
    if (feedbackList.value.length === 0) {
      showFailToast('获取反馈列表失败，请稍后重试');
    }
    finished.value = true;
  } finally {
    loading.value = false;
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string): string => {
  if (!dateTimeStr) return '';
  
  try {
    const date = new Date(dateTimeStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch (e) {
    console.error('日期格式化错误:', e);
    return dateTimeStr;
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

onMounted(() => {
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
  padding-bottom: 20px;
  min-height: 100vh;
  background-color: #f7f8fa;
}

.feedback-form {
  padding: 16px 0;
}

.feedback-list {
  min-height: calc(100vh - 100px);
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

.picker-header {
  text-align: center;
  padding: 12px 0;
  background-color: #f7f8fa;
  border-bottom: 1px solid #ebedf0;
}

.picker-title {
  font-size: 16px;
  font-weight: 500;
  color: #323233;
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
</style> 