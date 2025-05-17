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
    </div>
    
    <van-loading v-else type="spinner" vertical>加载中...</van-loading>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showImagePreview } from 'vant'
import { getFeedbackDetail } from '@/api/feedback.js'
import type { UserFeedback } from '@/types/feedback'

const route = useRoute()
const router = useRouter()
const feedback = ref<UserFeedback | null>(null)

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
</style> 