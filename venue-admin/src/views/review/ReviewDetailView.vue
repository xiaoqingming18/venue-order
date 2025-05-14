<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewById, deleteReview, replyReview, deleteReviewReply, banReview, unbanReview, getReviewBanRecords } from '@/api/review'
import type { Review, ReviewReply, ReviewBan } from '@/types/review'
import { getReviewStatusLabel, getReviewStatusType } from '@/types/review'

const route = useRoute()
const router = useRouter()
const reviewId = Number(route.params.id)

const reviewData = ref<Review | null>(null)
const loading = ref(false)
const replyContent = ref('')
const replyLoading = ref(false)
const banRecords = ref<ReviewBan[]>([])
const banRecordsLoading = ref(false)

// 获取评价详情
const fetchReviewDetail = async () => {
  loading.value = true
  try {
    const res = await getReviewById(reviewId)
    reviewData.value = res.data
    // 如果评价已封禁，获取封禁记录
    if (reviewData.value && reviewData.value.status === 1) {
      fetchBanRecords()
    }
  } catch (error) {
    console.error('获取评价详情失败', error)
    ElMessage.error('获取评价详情失败')
  } finally {
    loading.value = false
  }
}

// 获取封禁记录
const fetchBanRecords = async () => {
  banRecordsLoading.value = true
  try {
    const res = await getReviewBanRecords(reviewId)
    banRecords.value = res.data
  } catch (error) {
    console.error('获取封禁记录失败', error)
  } finally {
    banRecordsLoading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push('/home/reviews')
}

// 封禁评价
const handleBan = () => {
  if (!reviewData.value || reviewData.value.status !== 0) return

  ElMessageBox.prompt('请输入封禁原因', '封禁评价', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '封禁原因不能为空'
  }).then(async ({ value }) => {
    try {
      await banReview(reviewId, value)
      ElMessage.success('评价已封禁')
      fetchReviewDetail()
    } catch (error) {
      console.error('封禁评价失败', error)
      ElMessage.error('封禁失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 解除封禁
const handleUnban = () => {
  if (!reviewData.value || reviewData.value.status !== 1) return

  ElMessageBox.confirm('确认解除此评价的封禁状态？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await unbanReview(reviewId)
      ElMessage.success('评价已解除封禁')
      fetchReviewDetail()
    } catch (error) {
      console.error('解除封禁失败', error)
      ElMessage.error('解除封禁失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 回复评价
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replyLoading.value = true
  try {
    await replyReview(reviewId, replyContent.value)
    ElMessage.success('回复成功')
    replyContent.value = ''
    fetchReviewDetail() // 重新获取评价详情，包含新回复
  } catch (error) {
    console.error('回复评价失败', error)
    ElMessage.error('回复评价失败')
  } finally {
    replyLoading.value = false
  }
}

// 删除回复
const handleDeleteReply = (replyId: number) => {
  ElMessageBox.confirm('确认删除该回复吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReviewReply(replyId)
      ElMessage.success('删除回复成功')
      fetchReviewDetail() // 重新获取评价详情
    } catch (error) {
      console.error('删除回复失败', error)
      ElMessage.error('删除回复失败')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 删除评价
const handleDelete = () => {
  ElMessageBox.confirm('确认删除此评价吗？操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReview(reviewId)
      ElMessage.success('删除成功')
      router.push('/home/reviews')
    } catch (error) {
      console.error('删除评价失败', error)
      ElMessage.error('删除评价失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  fetchReviewDetail()
})
</script>

<template>
  <div class="review-detail-container" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h2>评价详情</h2>
      </div>
      <div class="header-actions" v-if="reviewData">
        <el-button v-if="reviewData.status === 0" type="danger" @click="handleBan">封禁评价</el-button>
        <el-button v-if="reviewData.status === 1" type="success" @click="handleUnban">解除封禁</el-button>
        <el-button type="danger" @click="handleDelete">删除评价</el-button>
      </div>
    </div>

    <div v-if="reviewData" class="review-detail-content">
      <!-- 评价基本信息 -->
      <el-descriptions title="评价基本信息" :column="2" border>
        <el-descriptions-item label="评价ID">{{ reviewData.id }}</el-descriptions-item>
        <el-descriptions-item label="评价状态">
          <el-tag :type="getReviewStatusType(reviewData.status)">
            {{ getReviewStatusLabel(reviewData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ reviewData.orderId }}</el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ formatDate(reviewData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ reviewData.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ reviewData.nickname }}</el-descriptions-item>
        <el-descriptions-item label="场馆ID">{{ reviewData.venueId }}</el-descriptions-item>
        <el-descriptions-item label="场馆名称">{{ reviewData.venueName }}</el-descriptions-item>
      </el-descriptions>

      <!-- 评分信息 -->
      <div class="review-scores-section">
        <h3>评分情况</h3>
        <div class="score-items">
          <div class="score-item">
            <div class="score-label">综合评分</div>
            <div class="score-value">
              <el-rate v-model="reviewData.overallScore" disabled show-score />
            </div>
          </div>
          <div class="score-item">
            <div class="score-label">环境评分</div>
            <div class="score-value">
              <el-rate v-model="reviewData.environmentScore" disabled show-score />
            </div>
          </div>
          <div class="score-item">
            <div class="score-label">设施评分</div>
            <div class="score-value">
              <el-rate v-model="reviewData.facilityScore" disabled show-score />
            </div>
          </div>
          <div class="score-item">
            <div class="score-label">服务评分</div>
            <div class="score-value">
              <el-rate v-model="reviewData.serviceScore" disabled show-score />
            </div>
          </div>
          <div class="score-item">
            <div class="score-label">性价比评分</div>
            <div class="score-value">
              <el-rate v-model="reviewData.costPerformanceScore" disabled show-score />
            </div>
          </div>
        </div>
      </div>

      <!-- 评价内容 -->
      <div class="review-content-section">
        <h3>评价内容</h3>
        <div class="review-content">{{ reviewData.content }}</div>
      </div>

      <!-- 评价图片 -->
      <div v-if="reviewData.images && reviewData.images.length > 0" class="review-images-section">
        <h3>评价图片</h3>
        <div class="review-images">
          <el-image
            v-for="(img, index) in reviewData.images"
            :key="index"
            :src="img"
            :preview-src-list="reviewData.images"
            fit="cover"
            class="review-image"
          />
        </div>
      </div>
      
      <!-- 封禁记录 -->
      <div v-if="reviewData.status === 1" class="ban-records-section">
        <h3>封禁记录</h3>
        <div v-loading="banRecordsLoading">
          <el-table v-if="banRecords.length > 0" :data="banRecords" border>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="adminId" label="管理员ID" width="100" align="center" />
            <el-table-column prop="reason" label="封禁原因" min-width="200" />
            <el-table-column prop="createdAt" label="封禁时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无封禁记录" />
        </div>
      </div>

      <!-- 回复列表 -->
      <div class="replies-section">
        <h3>回复列表</h3>
        <div v-if="reviewData.replies && reviewData.replies.length > 0" class="replies-list">
          <div
            v-for="reply in reviewData.replies"
            :key="reply.id"
            class="reply-item"
            :class="{ 'admin-reply': reply.isAdmin === 1 }"
          >
            <div class="reply-header">
              <div class="reply-author">
                {{ reply.isAdmin === 1 ? '管理员回复' : '用户回复' }}
              </div>
              <div class="reply-time">{{ formatDate(reply.createdAt) }}</div>
              <div v-if="reply.isAdmin === 1" class="reply-actions">
                <el-button type="danger" link size="small" @click="handleDeleteReply(reply.id)">删除</el-button>
              </div>
            </div>
            <div class="reply-content">{{ reply.content }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无回复" />
      </div>

      <!-- 管理员回复表单 -->
      <div class="admin-reply-form">
        <h3>管理员回复</h3>
        <el-form>
          <el-form-item>
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="3"
              placeholder="请输入回复内容..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitReply" :loading="replyLoading">提交回复</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="找不到该评价"></el-empty>
  </div>
</template>

<style scoped>
.review-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.review-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-scores-section h3,
.review-content-section h3,
.review-images-section h3,
.replies-section h3,
.admin-reply-form h3,
.ban-records-section h3 {
  font-size: 16px;
  margin-top: 0;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.score-items {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.score-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-label {
  font-size: 14px;
  color: #606266;
}

.review-content {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.review-image {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  object-fit: cover;
}

.replies-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.reply-item {
  padding: 15px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.admin-reply {
  background-color: #ecf5ff;
  border-left: 3px solid #409eff;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.reply-author {
  font-weight: bold;
}

.reply-time {
  color: #909399;
  font-size: 12px;
}

.reply-content {
  line-height: 1.6;
  white-space: pre-wrap;
}

.ban-records-section {
  background-color: #fff7ed;
  border-radius: 4px;
  padding: 15px;
  border: 1px solid #fcd3a5;
}
</style> 