<template>
  <div class="feedback-detail-container">
    <van-nav-bar 
      title="反馈详情" 
      left-arrow
      @click-left="goBack"
      fixed
    />
    
    <div class="feedback-detail" v-if="feedback">
      <van-cell-group inset class="feedback-header">
        <van-cell :title="feedback.title">
          <template #value>
            <span :class="['status', 'status-' + feedback.status]">{{ feedback.statusName }}</span>
          </template>
        </van-cell>
        <van-cell title="反馈类型" :value="feedback.typeName" />
        <van-cell title="提交时间" :value="feedback.createdAt" />
      </van-cell-group>
      
      <van-cell-group inset class="feedback-content">
        <van-cell title="反馈内容" />
        <div class="content-text">{{ feedback.content }}</div>
        
        <div v-if="feedback.images && feedback.images.length > 0" class="image-list">
          <van-image
            v-for="(image, index) in feedback.images"
            :key="index"
            width="80"
            height="80"
            :src="image"
            @click="previewImage(index)"
          />
        </div>
        
        <van-cell v-if="feedback.contact" title="联系方式" :value="feedback.contact" />
      </van-cell-group>
      
      <van-cell-group inset class="reply-list" v-if="feedback.replies && feedback.replies.length > 0">
        <van-cell title="回复记录" />
        <div v-for="(reply, index) in feedback.replies" :key="reply.id" class="reply-item">
          <div class="reply-header">
            <span class="reply-admin">{{ reply.adminNickname || reply.adminUsername }}</span>
            <span class="reply-time">{{ reply.createdAt }}</span>
          </div>
          <div class="reply-content">{{ reply.content }}</div>
        </div>
      </van-cell-group>
      
      <van-empty v-else description="暂无回复" />
      
      <div class="action-buttons" v-if="feedback.status === 2">
        <van-button type="primary" block round @click="handleCloseFeedback">
          问题已解决，关闭反馈
        </van-button>
      </div>
    </div>
    
    <van-loading v-else type="spinner" vertical>加载中...</van-loading>
    
    <van-dialog
      v-model:show="showConfirmDialog"
      title="确认关闭"
      message="确定要关闭此反馈吗？关闭后表示问题已解决。"
      show-cancel-button
      @confirm="confirmCloseFeedback"
    />

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
      <a class="tab-item" @click="router.push('/profile')">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showImagePreview, showSuccessToast } from 'vant'
import { getFeedbackDetail, closeFeedback } from '@/api/feedback.js'
import type { UserFeedback } from '@/types/feedback'

const route = useRoute()
const router = useRouter()
const feedback = ref<UserFeedback | null>(null)
const showConfirmDialog = ref(false)

// 获取反馈详情
const loadFeedbackDetail = async () => {
  try {
    const id = Number(route.params.id)
    if (!id) {
      showToast('参数错误')
      router.back()
      return
    }
    
    const { data } = await getFeedbackDetail(id)
    feedback.value = data
  } catch (error) {
    showToast('加载失败，请重试')
  }
}

// 预览图片
const previewImage = (index: number) => {
  if (feedback.value && feedback.value.images) {
    showImagePreview({
      images: feedback.value.images,
      startPosition: index
    })
  }
}

// 处理关闭反馈
const handleCloseFeedback = () => {
  showConfirmDialog.value = true
}

// 确认关闭反馈
const confirmCloseFeedback = async () => {
  if (!feedback.value) return
  
  try {
    await closeFeedback(feedback.value.id)
    showSuccessToast('反馈已关闭')
    
    // 更新本地反馈状态
    if (feedback.value) {
      feedback.value.status = 3
      feedback.value.statusName = '已关闭'
    }
  } catch (error) {
    showToast('操作失败，请重试')
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadFeedbackDetail()
})
</script>

<style scoped>
.feedback-detail-container {
  padding-top: 46px;
  padding-bottom: 70px; /* 添加底部padding，为底部导航栏留出空间 */
  min-height: 100vh;
  background-color: #f7f8fa;
}

.feedback-detail {
  padding: 16px 0;
}

.feedback-header, .feedback-content, .reply-list {
  margin-bottom: 12px;
}

.content-text {
  padding: 12px 16px;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.5;
  color: #323233;
}

.image-list {
  padding: 0 16px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.reply-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f2f3f5;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.reply-admin {
  font-weight: 500;
  color: #1989fa;
}

.reply-time {
  font-size: 12px;
  color: #969799;
}

.reply-content {
  line-height: 1.5;
  color: #323233;
  white-space: pre-wrap;
  word-break: break-all;
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

.action-buttons {
  padding: 16px;
  margin-top: 8px;
}

.close-button {
  margin-top: 20px;
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
  color: #1989fa;
}
</style> 