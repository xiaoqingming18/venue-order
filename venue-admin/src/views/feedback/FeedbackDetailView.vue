<template>
  <div class="feedback-detail-container">
    <el-card class="feedback-detail-card">
      <template #header>
        <div class="card-header">
          <h3>反馈详情</h3>
          <div class="header-actions">
            <el-button @click="goBack" icon="ArrowLeft">返回列表</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="!feedback" class="empty-data">
        <el-empty description="未找到反馈数据" />
      </div>

      <div v-else class="feedback-content">
        <!-- 反馈基本信息 -->
        <div class="feedback-header">
          <h2 class="feedback-title">{{ feedback.title }}</h2>
          <div class="feedback-meta">
            <el-tag size="small" :type="getStatusTagType(feedback.status)">{{ feedback.statusName }}</el-tag>
            <el-tag size="small" type="info">{{ feedback.typeName }}</el-tag>
            <span class="meta-item">
              <el-icon><User /></el-icon> {{ feedback.nickname || feedback.username }}
            </span>
            <span class="meta-item">
              <el-icon><Calendar /></el-icon> {{ formatDateTime(feedback.createdAt) }}
            </span>
          </div>
        </div>

        <!-- 反馈内容 -->
        <el-card class="content-card" shadow="never">
          <div class="text-content">{{ feedback.content }}</div>

          <!-- 图片附件 -->
          <div v-if="feedback.images && feedback.images.length > 0" class="image-list">
            <h4>图片附件</h4>
            <div class="image-container">
              <div v-for="(image, index) in feedback.images" :key="index" class="image-item">
                <el-image
                  :src="image"
                  :preview-src-list="feedback.images"
                  :initial-index="index"
                  fit="cover"
                  class="feedback-image"
                />
              </div>
            </div>
          </div>

          <!-- 联系方式 -->
          <div v-if="feedback.contact" class="contact-info">
            <h4>联系方式</h4>
            <p>{{ feedback.contact }}</p>
          </div>
        </el-card>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button-group>
            <el-button
              v-if="feedback.status === 0"
              type="primary"
              @click="handleUpdateStatus(1)"
            >
              受理反馈
            </el-button>
            <el-button
              v-if="feedback.status < 2"
              type="primary"
              @click="handleReply"
            >
              回复反馈
            </el-button>
            <el-button
              v-if="feedback.status < 3"
              type="warning"
              @click="handleUpdateStatus(3)"
            >
              关闭反馈
            </el-button>
          </el-button-group>
        </div>

        <!-- 回复列表 -->
        <div class="replies-section">
          <div class="section-header">
            <h3>回复记录</h3>
            <span class="reply-count">共 {{ replies.length }} 条回复</span>
          </div>

          <div v-if="replies.length === 0" class="empty-replies">
            <el-empty description="暂无回复记录" />
          </div>

          <div v-else class="reply-list">
            <div v-for="(reply, index) in replies" :key="reply.id" class="reply-item">
              <div class="reply-header">
                <div class="reply-user">
                  <el-avatar :size="32" icon="User" />
                  <div class="user-info">
                    <div class="username">
                      {{ reply.nickname || reply.username }}
                      <el-tag size="small" type="success" v-if="reply.userRole === 2">管理员</el-tag>
                    </div>
                    <div class="reply-time">{{ formatDateTime(reply.createdAt) }}</div>
                  </div>
                </div>
              </div>
              <div class="reply-content">{{ reply.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复反馈" width="500px">
      <el-form :model="replyForm" ref="replyFormRef" :rules="replyRules" label-width="80px">
        <el-form-item label="回复内容" prop="content">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply" :loading="submitting">
            提交回复
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'
import {
  User,
  Calendar,
  ChatDotRound,
  Document,
  ArrowLeft
} from '@element-plus/icons-vue'
import {
  getFeedbackDetail,
  updateFeedbackStatus,
  replyFeedback,
  getFeedbackReplies
} from '@/api/feedback'
import type { Feedback, FeedbackReply, FeedbackReplyRequest } from '@/types/feedback'
import { FeedbackStatus } from '@/types/feedback'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const submitting = ref(false)
const feedback = ref<Feedback | null>(null)
const replies = ref<FeedbackReply[]>([])
const replyDialogVisible = ref(false)
const replyFormRef = ref<FormInstance>()

// 获取反馈ID
const feedbackId = computed(() => Number(route.params.id))

// 回复表单
const replyForm = reactive<FeedbackReplyRequest>({
  feedbackId: 0,
  content: ''
})

// 表单校验规则
const replyRules = {
  content: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 1, max: 500, message: '回复内容长度应在1-500个字符之间', trigger: 'blur' }
  ]
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  switch (status) {
    case FeedbackStatus.PENDING:
      return 'info'
    case FeedbackStatus.PROCESSING:
      return 'warning'
    case FeedbackStatus.REPLIED:
      return 'success'
    case FeedbackStatus.CLOSED:
      return 'danger'
    default:
      return 'info'
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeStr: string): string => {
  if (!dateTimeStr) return ''
  
  try {
    const date = new Date(dateTimeStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (e) {
    console.error('日期格式化错误:', e)
    return dateTimeStr
  }
}

// 获取反馈详情
const fetchFeedbackDetail = async () => {
  loading.value = true
  try {
    const res = await getFeedbackDetail(feedbackId.value)
    feedback.value = res.data
    replyForm.feedbackId = feedbackId.value
  } catch (error) {
    console.error('获取反馈详情失败:', error)
    ElMessage.error('获取反馈详情失败')
  } finally {
    loading.value = false
  }
}

// 获取反馈回复列表
const fetchFeedbackReplies = async () => {
  try {
    const res = await getFeedbackReplies(feedbackId.value)
    replies.value = res.data || []
  } catch (error) {
    console.error('获取反馈回复列表失败:', error)
    ElMessage.error('获取反馈回复列表失败')
  }
}

// 更新反馈状态
const handleUpdateStatus = async (status: number) => {
  try {
    await updateFeedbackStatus(feedbackId.value, status)
    ElMessage.success('状态更新成功')
    // 重新获取反馈详情
    fetchFeedbackDetail()
  } catch (error) {
    console.error('更新反馈状态失败:', error)
    ElMessage.error('更新反馈状态失败')
  }
}

// 打开回复对话框
const handleReply = () => {
  replyForm.content = ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyFormRef.value) return
  await replyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await replyFeedback(replyForm)
        // 更新反馈状态为已回复
        await updateFeedbackStatus(feedbackId.value, FeedbackStatus.REPLIED)
        ElMessage.success('回复提交成功')
        replyDialogVisible.value = false
        // 重新获取反馈详情和回复列表
        fetchFeedbackDetail()
        fetchFeedbackReplies()
      } catch (error) {
        console.error('提交回复失败:', error)
        ElMessage.error('提交回复失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 返回列表
const goBack = () => {
  router.push('/home/feedback')
}

// 页面加载时获取数据
onMounted(() => {
  if (feedbackId.value) {
    fetchFeedbackDetail()
    fetchFeedbackReplies()
  }
})
</script>

<style scoped>
.feedback-detail-container {
  padding: 20px;
}

.feedback-detail-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.loading-container {
  padding: 20px;
}

.empty-data {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.feedback-header {
  margin-bottom: 20px;
}

.feedback-title {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 20px;
  font-weight: 600;
}

.feedback-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
  margin-bottom: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #606266;
}

.content-card {
  margin-bottom: 20px;
  background-color: #f5f7fa;
}

.text-content {
  white-space: pre-wrap;
  line-height: 1.6;
  font-size: 15px;
  margin-bottom: 20px;
}

.image-list {
  margin-top: 20px;
}

.image-list h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 500;
}

.image-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
}

.feedback-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.contact-info {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.contact-info h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 500;
}

.action-buttons {
  margin: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.reply-count {
  color: #909399;
  font-size: 14px;
}

.empty-replies {
  padding: 20px 0;
}

.reply-list {
  margin-bottom: 20px;
}

.reply-item {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.reply-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

.reply-content {
  margin-left: 42px;
  margin-top: 5px;
  white-space: pre-wrap;
  line-height: 1.6;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 