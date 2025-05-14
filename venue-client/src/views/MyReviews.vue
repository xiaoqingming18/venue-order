<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyReviews, deleteReview } from '@/api/review'
import { useUserStore } from '@/stores/user'
import type { Review } from '@/types/review'

const router = useRouter()
const userStore = useUserStore()

// 评价数据
const reviews = ref<Review[]>([])
const loading = ref(false)
const noData = ref(false)

// 获取我的评价列表
const fetchMyReviews = async () => {
  loading.value = true
  noData.value = false
  
  try {
    // 检查用户是否已登录
    if (!userStore.isLoggedIn) {
      await userStore.fetchUserInfo()
      
      if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录后再查看您的评价')
        router.push('/login?redirect=/my-reviews')
        return
      }
    }
    
    const res = await getMyReviews()
    reviews.value = res.data || []
    
    if (reviews.value.length === 0) {
      noData.value = true
    }
  } catch (error) {
    console.error('获取评价列表失败:', error)
    ElMessage.error('获取评价列表失败，请稍后重试')
    noData.value = true
  } finally {
    loading.value = false
  }
}

// 跳转到订单详情
const goToOrder = (orderId: number) => {
  router.push(`/order/${orderId}`)
}

// 跳转到场馆详情
const goToVenue = (venueId: number) => {
  router.push(`/venue/${venueId}`)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 删除评价
const handleDeleteReview = async (review: Review) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评价吗？删除后无法恢复。',
      '删除评价',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    await deleteReview(review.id)
    ElMessage.success('评价已删除')
    
    // 从列表中删除
    reviews.value = reviews.value.filter(item => item.id !== review.id)
    
    if (reviews.value.length === 0) {
      noData.value = true
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error)
      ElMessage.error('删除评价失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 页面初始化时获取数据
onMounted(() => {
  fetchMyReviews()
})
</script>

<template>
  <div class="my-reviews-container" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-button" @click="router.push('/profile')">
        <i class="fas fa-chevron-left"></i>
      </div>
      <div class="page-title">我的评价</div>
      <div class="placeholder"></div>
    </div>
    
    <!-- 评价列表内容 -->
    <div class="reviews-content">
      <div v-if="!noData" class="reviews-list">
        <div v-for="review in reviews" :key="review.id" class="review-card">
          <div class="venue-info" @click="goToVenue(review.venueId)">
            <div class="venue-image">
              <img :src="review.venue.coverImage || `https://picsum.photos/100/100?random=${review.venueId}`" alt="场馆图片">
            </div>
            <div class="venue-detail">
              <div class="venue-name">{{ review.venue.name }}</div>
              <div class="review-date">评价时间: {{ formatDate(review.createdTime) }}</div>
            </div>
            <div class="venue-action">
              <i class="fas fa-chevron-right"></i>
            </div>
          </div>
          
          <div class="review-content">
            <div class="review-header">
              <div class="review-score">
                <el-rate v-model="review.overallScore" disabled />
                <span class="score-text">{{ review.overallScore }}分</span>
              </div>
              <div class="review-status">
                <el-tag 
                  :type="review.status === 0 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ review.status === 0 ? '正常' : '已封禁' }}
                </el-tag>
                <span v-if="review.anonymous" class="anonymous-tag">匿名</span>
              </div>
            </div>
            
            <div class="review-text">
              {{ review.content }}
            </div>
            
            <div class="review-images" v-if="review.images && review.images.length > 0">
              <el-image 
                v-for="(img, index) in review.images"
                :key="index"
                :src="img"
                :preview-src-list="review.images"
                fit="cover"
                class="review-image"
              />
            </div>
            
            <div class="review-replies" v-if="review.replies && review.replies.length > 0">
              <div v-for="reply in review.replies" :key="reply.id" class="reply-item">
                <div class="reply-header">
                  <span class="reply-icon" :class="{ 'admin-reply': reply.isAdmin === 1 }">
                    {{ reply.isAdmin === 1 ? '官方回复' : '商家回复' }}
                  </span>
                  <span class="reply-time">{{ formatDate(reply.replyTime) }}</span>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
              </div>
            </div>
            
            <div class="review-actions">
              <el-button 
                type="text" 
                size="small" 
                @click="goToOrder(review.orderId)"
              >
                <i class="fas fa-file-invoice"></i> 查看订单
              </el-button>
              
              <el-button 
                type="text" 
                size="small" 
                @click="handleDeleteReview(review)"
                :disabled="review.status === 1"
              >
                <i class="fas fa-trash"></i> 删除评价
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <el-empty v-if="noData" description="您暂未发表任何评价">
        <el-button type="primary" @click="router.push('/venues')">去浏览场馆</el-button>
      </el-empty>
    </div>
    
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
      <a class="tab-item active" @click="router.push('/profile')">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.my-reviews-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 70px;
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
  cursor: pointer;
}

.page-title {
  flex-grow: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}

.placeholder {
  width: 30px;
}

.reviews-content {
  padding: 15px;
}

.reviews-list {
  margin-bottom: 20px;
}

.review-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
  overflow: hidden;
}

.venue-info {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f2f6fc;
  cursor: pointer;
}

.venue-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 10px;
}

.venue-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.venue-detail {
  flex: 1;
}

.venue-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.review-date {
  font-size: 12px;
  color: #909399;
}

.venue-action {
  color: #c0c4cc;
}

.review-content {
  padding: 15px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.review-score {
  display: flex;
  align-items: center;
}

.score-text {
  margin-left: 8px;
  color: #ff9900;
  font-weight: 500;
}

.review-status {
  display: flex;
  align-items: center;
  gap: 5px;
}

.anonymous-tag {
  background-color: #909399;
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}

.review-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 15px;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.review-replies {
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 15px;
}

.reply-item {
  padding: 5px 0;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.reply-icon {
  background-color: #409eff;
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  margin-right: 5px;
}

.admin-reply {
  background-color: #67c23a;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

.reply-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
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
  color: #409eff;
}
</style> 