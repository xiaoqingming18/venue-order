<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVenueById, getFacilitiesByVenueId, getLocationsByVenueId, deleteVenue, updateVenueStatus } from '@/api/venue'
import { getVenueReviews, replyReview } from '@/api/review'
import type { Venue, VenueFacility, VenueLocation } from '@/types/venue'
import type { Review } from '@/types/review'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 场馆ID
const venueId = computed(() => {
  return Number(route.params.id)
})

// 加载状态
const loading = ref(false)
// 场馆信息
const venueInfo = ref<Venue | null>(null)
// 场馆设施
const facilities = ref<VenueFacility[]>([])
// 场馆位置
const locations = ref<VenueLocation[]>([])
// 场馆评价
const reviews = ref<Review[]>([])
const reviewsLoading = ref(false)
// 评价分页
const reviewPage = ref(1)
const reviewSize = ref(5)
const reviewTotal = ref(0)
// 评价回复
const replyDialogVisible = ref(false)
const currentReview = ref<Review | null>(null)
const replyContent = ref('')
const replyLoading = ref(false)

// 加载场馆详情
const loadVenueDetail = async () => {
  loading.value = true
  try {
    const res = await getVenueById(venueId.value) as { data: Venue }
    venueInfo.value = res.data
  } catch (error) {
    console.error('获取场馆详情失败', error)
    ElMessage.error('获取场馆详情失败')
    router.push('/home/venues')
  } finally {
    loading.value = false
  }
}

// 加载场馆设施
const loadFacilities = async () => {
  try {
    const res = await getFacilitiesByVenueId(venueId.value) as { data: VenueFacility[] }
    facilities.value = res.data
  } catch (error) {
    console.error('获取场馆设施失败', error)
    ElMessage.error('获取场馆设施失败')
  }
}

// 加载场馆位置
const loadLocations = async () => {
  try {
    const res = await getLocationsByVenueId(venueId.value) as { data: VenueLocation[] }
    locations.value = res.data
  } catch (error) {
    console.error('获取场馆位置失败', error)
    ElMessage.error('获取场馆位置失败')
  }
}

// 加载场馆评价
const loadVenueReviews = async () => {
  reviewsLoading.value = true
  try {
    const res = await getVenueReviews({ 
      venueId: venueId.value, 
      page: reviewPage.value, 
      size: reviewSize.value 
    })
    
    if (res.data && res.data.records) {
      reviews.value = res.data.records
      reviewTotal.value = res.data.total
    } else if (res.data && Array.isArray(res.data)) {
      reviews.value = res.data
      reviewTotal.value = res.data.length
    } else {
      reviews.value = []
      reviewTotal.value = 0
    }
  } catch (error) {
    console.error('获取场馆评价失败', error)
    ElMessage.error('获取场馆评价失败')
    reviews.value = []
    reviewTotal.value = 0
  } finally {
    reviewsLoading.value = false
  }
}

// 编辑场馆
const handleEdit = () => {
  router.push(`/home/venues/edit/${venueId.value}`)
}

// 删除场馆
const handleDelete = () => {
  ElMessageBox.confirm('确认删除该场馆吗？此操作将一并删除关联的设施和位置信息。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteVenue(venueId.value)
      ElMessage.success('删除成功')
      router.push('/home/venues')
    } catch (error) {
      console.error('删除场馆失败', error)
      ElMessage.error('删除场馆失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 切换场馆状态
const handleStatusChange = async () => {
  if (!venueInfo.value) return
  
  const newStatus = venueInfo.value.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '开放' : '关闭'
  
  try {
    await updateVenueStatus(venueId.value, newStatus)
    ElMessage.success(`已${statusText}场馆`)
    venueInfo.value.status = newStatus
  } catch (error) {
    console.error('更新场馆状态失败', error)
    ElMessage.error('更新场馆状态失败')
  }
}

// 返回列表
const goBack = () => {
  router.push('/home/venues')
}

// 获取状态文本
const getStatusText = (status: number) => {
  return status === 1 ? '开放' : '关闭'
}

// 获取状态标签类型
const getStatusType = (status: number) => {
  return status === 1 ? 'success' : 'danger'
}

// 处理评价页面变化
const handleReviewPageChange = (page: number) => {
  reviewPage.value = page
  loadVenueReviews()
}

// 打开回复对话框
const openReplyDialog = (review: Review) => {
  currentReview.value = review
  replyContent.value = ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!currentReview.value) return
  
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  replyLoading.value = true
  try {
    await replyReview(currentReview.value.id, replyContent.value)
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    loadVenueReviews() // 重新加载评价列表
  } catch (error) {
    console.error('回复失败', error)
    ElMessage.error('回复评价失败')
  } finally {
    replyLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(async () => {
  await loadVenueDetail()
  await Promise.all([loadFacilities(), loadLocations(), loadVenueReviews()])
})

// 跳转到设施管理页面
const goToFacilities = () => {
  router.push(`/home/facilities?venueId=${venueId.value}`)
}

// 跳转到位置管理页面
const goToLocations = () => {
  router.push(`/home/locations?venueId=${venueId.value}`)
}
</script>

<template>
  <div class="venue-detail-container">
    <el-card class="venue-detail-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>场馆详情</h2>
          <div class="header-actions">
            <el-button @click="goBack">返回</el-button>
            <el-button type="primary" @click="handleEdit">编辑</el-button>
            <el-button 
              :type="venueInfo?.status === 1 ? 'warning' : 'success'" 
              @click="handleStatusChange"
            >
              {{ venueInfo?.status === 1 ? '关闭场馆' : '开放场馆' }}
            </el-button>
            <el-button type="danger" @click="handleDelete">删除</el-button>
          </div>
        </div>
      </template>

      <div v-if="venueInfo" class="venue-info">
        <div v-if="venueInfo.coverImage" class="venue-cover">
          <el-image 
            :src="venueInfo.coverImage" 
            fit="cover" 
            style="max-width: 100%; max-height: 300px; border-radius: 8px;"
            :preview-src-list="[venueInfo.coverImage]"
          />
        </div>
        
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="场馆名称">{{ venueInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(venueInfo.status)">
              {{ getStatusText(venueInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="基准价格">{{ venueInfo.basePrice }} 元</el-descriptions-item>
          <el-descriptions-item label="容纳人数">{{ venueInfo.capacity }} 人</el-descriptions-item>
          <el-descriptions-item label="营业时间" :span="2">{{ venueInfo.businessHours || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ venueInfo.contactPhone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="场馆类型ID">{{ venueInfo.venueTypeId }}</el-descriptions-item>
          <el-descriptions-item label="场馆地址" :span="2">{{ venueInfo.address }}</el-descriptions-item>
          <el-descriptions-item label="场馆描述" :span="2">{{ venueInfo.description || '无描述' }}</el-descriptions-item>
          <el-descriptions-item label="场馆封面">
            <el-link v-if="venueInfo.coverImage" :href="venueInfo.coverImage" target="_blank">查看封面</el-link>
            <span v-else>暂无封面</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ venueInfo.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ venueInfo.updatedAt }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">场馆设施</el-divider>
        
        <div class="venue-facilities">
          <div class="section-header">
            <h3>设施列表</h3>
            <el-button type="primary" size="small" @click="goToFacilities">
              <el-icon><Plus /></el-icon>管理设施
            </el-button>
          </div>
          <el-empty v-if="facilities.length === 0" description="暂无设施信息">
            <el-button type="primary" @click="goToFacilities">添加设施</el-button>
          </el-empty>
          <el-table v-else :data="facilities" style="width: 100%" border>
            <el-table-column prop="facilityType" label="设施类型" width="150" />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="positionDesc" label="位置描述" />
            <el-table-column prop="createdAt" label="创建时间" width="180" />
          </el-table>
        </div>

        <el-divider content-position="left">场馆位置</el-divider>
        
        <div class="venue-locations">
          <div class="section-header">
            <h3>位置列表</h3>
            <el-button type="primary" size="small" @click="goToLocations">
              <el-icon><Plus /></el-icon>管理位置
            </el-button>
          </div>
          <el-empty v-if="locations.length === 0" description="暂无位置信息">
            <el-button type="primary" @click="goToLocations">添加位置</el-button>
          </el-empty>
          <el-table v-else :data="locations" style="width: 100%" border>
            <el-table-column prop="name" label="位置名称" width="150" />
            <el-table-column prop="type" label="位置类型" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '可用' : '不可用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="位置描述" />
            <el-table-column prop="createdAt" label="创建时间" width="180" />
          </el-table>
        </div>

        <el-divider content-position="left">场馆评价</el-divider>
        
        <div class="venue-reviews">
          <div class="section-header">
            <h3>评价列表</h3>
          </div>
          
          <div v-loading="reviewsLoading">
            <el-empty v-if="reviews.length === 0" description="暂无评价信息"></el-empty>
            
            <div v-else class="review-list">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="user-info">
                    <span class="username">{{ review.nickname || review.username }}</span>
                    <el-tag 
                      v-if="review.status === 1" 
                      type="danger" 
                      size="small"
                    >已封禁</el-tag>
                  </div>
                  <div class="review-meta">
                    <div class="review-time">{{ formatDate(review.createdAt) }}</div>
                    <div class="review-score">
                      <el-rate v-model="review.overallScore" disabled></el-rate>
                      <span class="score-text">{{ review.overallScore }}分</span>
                    </div>
                  </div>
                </div>
                
                <div class="review-body">
                  <div class="review-content">{{ review.content }}</div>
                  
                  <div v-if="review.images && review.images.length > 0" class="review-images">
                    <el-image 
                      v-for="(img, index) in review.images" 
                      :key="index" 
                      :src="img" 
                      :preview-src-list="review.images"
                      class="review-image"
                    ></el-image>
                  </div>
                </div>
                
                <!-- 评价回复 -->
                <div v-if="review.replies && review.replies.length > 0" class="review-replies">
                  <div v-for="reply in review.replies" :key="reply.id" class="reply-item">
                    <div class="reply-header">
                      <span class="reply-tag" :class="{ 'admin-reply': reply.isAdmin === 1 }">
                        {{ reply.isAdmin === 1 ? '管理员回复' : '用户回复' }}
                      </span>
                      <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
                    </div>
                    <div class="reply-content">{{ reply.content }}</div>
                  </div>
                </div>
                
                <div class="review-actions">
                  <el-button type="primary" size="small" @click="openReplyDialog(review)">回复评价</el-button>
                  <el-button 
                    size="small" 
                    type="info" 
                    @click="router.push(`/home/reviews/${review.id}`)"
                  >查看详情</el-button>
                </div>
              </div>
              
              <!-- 分页 -->
              <el-pagination
                v-if="reviewTotal > reviewSize"
                class="review-pagination"
                background
                layout="prev, pager, next"
                :total="reviewTotal"
                :page-size="reviewSize"
                :current-page="reviewPage"
                @current-change="handleReviewPageChange"
              ></el-pagination>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 回复对话框 -->
    <el-dialog
      title="回复评价"
      v-model="replyDialogVisible"
      width="500px"
    >
      <div v-if="currentReview" class="reply-dialog-content">
        <div class="original-review">
          <div class="original-title">原评价:</div>
          <div class="original-content">{{ currentReview.content }}</div>
        </div>
        
        <el-form>
          <el-form-item label="回复内容">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="4"
              placeholder="请输入回复内容..."
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply" :loading="replyLoading">提交回复</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.venue-detail-container {
  padding: 20px;
}

.venue-detail-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.venue-info {
  margin-top: 20px;
}

.venue-cover {
  margin-bottom: 20px;
  text-align: center;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 8px;
}

.venue-facilities,
.venue-locations {
  margin-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  color: #606266;
}

.el-divider {
  margin: 30px 0;
}

.venue-reviews {
  margin-top: 20px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.review-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background-color: #fff;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: bold;
}

.review-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.review-time {
  font-size: 12px;
  color: #909399;
}

.review-score {
  display: flex;
  align-items: center;
}

.score-text {
  margin-left: 5px;
  color: #ff9900;
}

.review-body {
  margin-bottom: 15px;
}

.review-content {
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
}

.review-replies {
  margin-top: 10px;
  margin-bottom: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 10px;
}

.reply-item {
  padding: 5px 0;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 12px;
}

.reply-tag {
  background-color: #909399;
  color: white;
  padding: 2px 6px;
  border-radius: 2px;
  margin-right: 10px;
}

.admin-reply {
  background-color: #409eff;
}

.reply-time {
  color: #909399;
}

.reply-content {
  padding-left: 5px;
  line-height: 1.6;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.review-pagination {
  margin-top: 20px;
  text-align: center;
}

.original-review {
  margin-bottom: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 10px;
}

.original-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.original-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>