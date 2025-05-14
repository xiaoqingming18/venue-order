<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getVenueById, getVenueFacilities, getVenueLocations } from '@/api/venue'
import { getVenueReviews, getVenueReviewStats, reportReview, replyReview } from '@/api/review'
import type { Venue, VenueFacility, VenueLocation } from '@/types/venue'
import type { Review, ReviewStats } from '@/types/review'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据相关
const venueId = ref<number>(Number(route.params.id))
const venueInfo = ref<Venue | null>(null)
const facilities = ref<VenueFacility[]>([])
const locations = ref<VenueLocation[]>([])
const loading = ref(false)
const activeTab = ref('info')

// 评价相关
const reviews = ref<Review[]>([])
const reviewStats = ref<ReviewStats | null>(null)
const reviewsLoading = ref(false)
const reviewPage = ref(1)
const reviewSize = ref(10)
const totalReviews = ref(0)

// 举报评价相关
const reportDialogVisible = ref(false)
const reportLoading = ref(false)
const currentReportReview = ref<any>(null)
const reportForm = ref({
  reason: '',
  reasonDetail: ''
})

// 举报原因选项
const reportReasons = [
  '含有不当言论',
  '涉及侮辱诽谤',
  '虚假评价',
  '广告信息',
  '其他原因'
]

// 评价回复相关
const replyDialogVisible = ref(false)
const replyLoading = ref(false)
const currentReplyReview = ref<any>(null)
const replyForm = ref({
  content: ''
})

// 计算当前用户是否为场馆方
const isVenueOwner = computed(() => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) return false
  
  // 检查用户角色 - 角色为1的是普通用户，只有管理员能进行特定操作
  return userStore.isAdmin
})

// 计算当前用户是否已登录
const isLoggedIn = computed(() => {
  return userStore.isLoggedIn
})

// 获取场馆详细信息
const fetchVenueDetail = async () => {
  loading.value = true
  try {
    const res = await getVenueById(venueId.value)
    venueInfo.value = res.data

    // 获取场馆设施
    const facilitiesRes = await getVenueFacilities(venueId.value)
    facilities.value = facilitiesRes.data

    // 获取场馆位置
    const locationsRes = await getVenueLocations(venueId.value)
    locations.value = locationsRes.data
  } catch (error) {
    console.error('获取场馆详情失败', error)
  } finally {
    loading.value = false
  }
}

// 获取场馆评价数据
const fetchVenueReviews = async () => {
  reviewsLoading.value = true
  try {
    console.log('开始获取场馆评价，场馆ID:', venueId.value, '页码:', reviewPage.value)
    const res = await getVenueReviews(venueId.value, reviewPage.value, reviewSize.value)
    console.log('获取场馆评价响应:', JSON.stringify(res))
    
    // 尝试多种可能的数据结构
    if (res.data && res.data.records) {
      // 如果数据在 data.records 中
      reviews.value = res.data.records
      totalReviews.value = res.data.total || reviews.value.length
      console.log('从records获取到评价数据，数量:', reviews.value.length)
    } else if (res.data && res.data.list) {
      // 如果数据在 data.list 中
      reviews.value = res.data.list
      totalReviews.value = res.data.total || reviews.value.length
      console.log('从list获取到评价数据，数量:', reviews.value.length)
    } else if (res.data && res.data.content) {
      // Spring分页标准格式
      reviews.value = res.data.content
      totalReviews.value = res.data.totalElements || reviews.value.length
      console.log('从content获取到评价数据，数量:', reviews.value.length)
    } else if (res.data && Array.isArray(res.data)) {
      // 直接返回数组
      reviews.value = res.data
      totalReviews.value = reviews.value.length
      console.log('从数组获取到评价数据，数量:', reviews.value.length)
    } else {
      // 没有找到有效数据
      reviews.value = []
      totalReviews.value = 0
      console.warn('未找到有效的评价数据:', JSON.stringify(res.data))
    }
    
    // 检查评价数据中是否包含所需的字段
    if (reviews.value.length > 0) {
      const firstReview = reviews.value[0]
      console.log('第一条评价详情:', JSON.stringify(firstReview))
      console.log('评价内容:', firstReview.content)
      
      // 创建深拷贝，避免Vue的响应式系统出现问题
      reviews.value = JSON.parse(JSON.stringify(reviews.value)).map(review => {
        // 确保所有评分字段都存在，如果不存在则使用默认值
        const processedReview = {
          ...review,
          id: review.id || Math.floor(Math.random() * 10000),
          content: review.content || '用户未填写评价内容',
          overallScore: review.overallScore || review.score || 0,
          environmentScore: review.environmentScore || review.overallScore || review.score || 0,
          facilityScore: review.facilityScore || review.overallScore || review.score || 0,
          serviceScore: review.serviceScore || review.overallScore || review.score || 0,
          costPerformanceScore: review.costPerformanceScore || review.overallScore || review.score || 0,
          createdTime: review.createdTime || review.createdAt || new Date().toISOString(),
          // 确保图片数组存在
          images: review.images || [],
          // 确保用户信息结构正确
          user: review.user || { 
            id: review.userId || 0, 
            username: review.username || '用户', 
            nickname: review.nickname || '用户',
            avatar: review.avatar || ''
          },
          // 确保回复数组存在
          replies: review.replies || [],
          // 处理评价状态
          isBanned: review.status === 1
        }
        
        console.log('处理后的评价:', JSON.stringify(processedReview))
        return processedReview
      })
      
      console.log('最终处理后的所有评价:', reviews.value)
    }
  } catch (error) {
    console.error('获取场馆评价失败', error)
    reviews.value = []
    totalReviews.value = 0
  } finally {
    reviewsLoading.value = false
  }
}

// 获取场馆评价统计数据
const fetchReviewStats = async () => {
  try {
    console.log('开始获取场馆评价统计数据，场馆ID:', venueId.value)
    const res = await getVenueReviewStats(venueId.value)
    console.log('获取场馆评价统计响应:', JSON.stringify(res))
    
    if (res && res.data) {
      reviewStats.value = res.data
      
      // 确保评分数据有效
      if (reviewStats.value) {
        // 确保平均分不为null，使用overallAvgScore如果存在，否则默认为0
        if (reviewStats.value.overallAvgScore !== undefined) {
          reviewStats.value.averageScore = reviewStats.value.overallAvgScore;
        } else {
          reviewStats.value.averageScore = reviewStats.value.averageScore || 0;
        }
        
        console.log('平均分:', reviewStats.value.averageScore, '总评价数:', reviewStats.value.totalCount);
        
        // 如果没有评分分布数据，创建默认分布
        if (!reviewStats.value.scoreDistribution || reviewStats.value.scoreDistribution.length === 0) {
          reviewStats.value.scoreDistribution = [
            { score: 5, count: 0, percentage: 0 },
            { score: 4, count: 0, percentage: 0 },
            { score: 3, count: 0, percentage: 0 },
            { score: 2, count: 0, percentage: 0 },
            { score: 1, count: 0, percentage: 0 }
          ];
          
          // 如果有评价，根据评价计算分布
          if (reviews.value && reviews.value.length > 0) {
            const scoreCounts = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
            
            reviews.value.forEach(review => {
              const score = Math.round(review.overallScore || review.score || 0);
              if (score >= 1 && score <= 5) {
                scoreCounts[score]++;
              }
            });
            
            const total = reviews.value.length;
            reviewStats.value.scoreDistribution.forEach(item => {
              item.count = scoreCounts[item.score] || 0;
              item.percentage = total > 0 ? item.count / total : 0;
            });
          }
        }
      }
      
      console.log('处理后的评价统计数据:', JSON.stringify(reviewStats.value))
    } else {
      console.warn('没有获取到评价统计数据')
      // 创建一个默认的统计数据对象
      reviewStats.value = {
        venueId: venueId.value,
        venueName: venueInfo.value?.name || '',
        totalCount: 0,
        averageScore: 0,
        environmentAvgScore: 0,
        facilityAvgScore: 0,
        serviceAvgScore: 0,
        costPerformanceAvgScore: 0,
        overallAvgScore: 0,
        scoreDistribution: [
          { score: 5, count: 0, percentage: 0 },
          { score: 4, count: 0, percentage: 0 },
          { score: 3, count: 0, percentage: 0 },
          { score: 2, count: 0, percentage: 0 },
          { score: 1, count: 0, percentage: 0 }
        ]
      }
    }
  } catch (error) {
    console.error('获取评价统计数据失败', error)
    // 创建一个默认的统计数据对象
    reviewStats.value = {
      venueId: venueId.value,
      venueName: venueInfo.value?.name || '',
      totalCount: 0,
      averageScore: 0,
      environmentAvgScore: 0,
      facilityAvgScore: 0,
      serviceAvgScore: 0,
      costPerformanceAvgScore: 0,
      overallAvgScore: 0,
      scoreDistribution: [
        { score: 5, count: 0, percentage: 0 },
        { score: 4, count: 0, percentage: 0 },
        { score: 3, count: 0, percentage: 0 },
        { score: 2, count: 0, percentage: 0 },
        { score: 1, count: 0, percentage: 0 }
      ]
    }
  }
}

// 处理评分页变化
const handleReviewPageChange = (page: number) => {
  reviewPage.value = page
  fetchVenueReviews()
}

// 返回列表
const goBack = () => {
  router.push('/venues')
}

// 状态展示
const getStatusText = (status: number): string => {
  return status === 1 ? '开放' : '关闭'
}

const getStatusType = (status: number): string => {
  return status === 1 ? 'success' : 'danger'
}

// 位置状态展示
const getLocationStatusText = (status: number): string => {
  return status === 1 ? '可用' : '不可用'
}

const getLocationStatusType = (status: number): string => {
  return status === 1 ? 'success' : 'danger'
}

// 返回预约页
const goToBooking = () => {
  router.push(`/booking/venue/${venueId.value}`)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 打开举报对话框
const openReportDialog = (review: any) => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再举报')
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
    return
  }
  
  currentReportReview.value = review
  reportForm.value.reason = ''
  reportForm.value.reasonDetail = ''
  reportDialogVisible.value = true
}

// 打开回复对话框
const openReplyDialog = (review: any) => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再回复')
    router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
    return
  }
  
  // 不再检查用户角色，允许所有登录用户回复评价
  
  currentReplyReview.value = review
  replyForm.value.content = ''
  replyDialogVisible.value = true
}

// 提交举报
const submitReport = async () => {
  if (!currentReportReview.value) return
  
  if (!reportForm.value.reason) {
    ElMessage.warning('请选择举报原因')
    return
  }
  
  try {
    reportLoading.value = true
    await reportReview(currentReportReview.value.id, reportForm.value)
    ElMessage.success('举报成功，感谢您的反馈')
    reportDialogVisible.value = false
  } catch (error: any) {
    console.error('举报失败', error)
    ElMessage.error(error.response?.data?.message || '举报失败，请稍后重试')
  } finally {
    reportLoading.value = false
  }
}

// 提交回复
const submitReply = async () => {
  if (!currentReplyReview.value) return
  
  // 检查回复内容是否为空
  if (!replyForm.value.content.trim()) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  
  replyLoading.value = true
  try {
    await replyReview(currentReplyReview.value.id, { content: replyForm.value.content })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    
    // 刷新评价列表
    fetchVenueReviews()
  } catch (error) {
    console.error('回复失败', error)
    ElMessage.error('回复失败，请稍后再试')
  } finally {
    replyLoading.value = false
  }
}

// 页面初始化
onMounted(() => {
  fetchVenueDetail()
  // 默认加载评价数据和统计信息
  fetchVenueReviews()
  fetchReviewStats()
})

// 监听标签页变化
const handleTabChange = (tab: any) => {
  const tabName = tab.props.name || tab.paneName || tab
  console.log('切换到标签页:', tabName)
  if (tabName === 'reviews' && (reviews.value.length === 0 || !reviewStats.value)) {
    fetchVenueReviews()
    fetchReviewStats()
  }
}

// 处理评价操作命令
const handleReviewCommand = (command: any) => {
  if (command.type === 'report') {
    openReportDialog(command.review)
  } else if (command.type === 'reply') {
    openReplyDialog(command.review)
  }
}
</script>

<template>
  <div class="venue-detail-container" v-loading="loading">
    <div class="header-actions">
      <el-button @click="goBack" icon="el-icon-arrow-left">返回列表</el-button>
    </div>

    <div v-if="venueInfo" class="venue-detail-content">
      <div class="venue-header">
        <div class="venue-image">
          <img :src="`https://picsum.photos/600/400?random=${venueId}`" alt="场馆详情图" />
        </div>
        <div class="venue-info">
          <div class="venue-title">
            <h1>{{ venueInfo.name }}</h1>
            <el-tag :type="getStatusType(venueInfo.status)">{{ getStatusText(venueInfo.status) }}</el-tag>
          </div>
          <div class="venue-detail-item">
            <i class="el-icon-location"></i>
            <span>{{ venueInfo.address }}</span>
          </div>
          <div class="venue-detail-item">
            <i class="el-icon-time"></i>
            <span>营业时间: {{ venueInfo.businessHours || '暂无信息' }}</span>
          </div>
          <div class="venue-detail-item">
            <i class="el-icon-user"></i>
            <span>可容纳人数: {{ venueInfo.capacity }}人</span>
          </div>
          <div class="venue-detail-item">
            <i class="el-icon-phone"></i>
            <span>联系电话: {{ venueInfo.contactPhone || '暂无信息' }}</span>
          </div>
          <div class="venue-price-box">
            <div class="price">
              <span class="price-label">基础价格:</span>
              <span class="price-value">¥{{ venueInfo.basePrice }}/小时</span>
            </div>
            <el-button type="primary" size="large" @click="goToBooking" :disabled="venueInfo.status !== 1">立即预约</el-button>
          </div>
        </div>
      </div>

      <div class="venue-tabs">
        <el-tabs v-model="activeTab" type="card" @tab-click="handleTabChange">
          <el-tab-pane label="场馆介绍" name="info">
            <div class="venue-description">
              <h3>场馆介绍</h3>
              <p>{{ venueInfo.description || '暂无详细介绍' }}</p>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="场馆设施" name="facilities">
            <div class="venue-facilities">
              <h3>场馆设施</h3>
              <el-empty v-if="facilities.length === 0" description="暂无设施信息"></el-empty>
              <el-table v-else :data="facilities" style="width: 100%" border>
                <el-table-column prop="facilityType" label="设施类型" min-width="120" />
                <el-table-column prop="quantity" label="数量" width="100" />
                <el-table-column label="价格" width="150">
                  <template #default="{ row }">
                    <span v-if="row.price" class="facility-price">¥{{ row.price }}/小时</span>
                    <span v-else class="facility-base-price">¥{{ venueInfo.basePrice * 0.2 }}/小时<small>(基准价20%)</small></span>
                  </template>
                </el-table-column>
                <el-table-column prop="positionDesc" label="位置描述" min-width="180" />
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="场馆位置" name="locations">
            <div class="venue-locations">
              <h3>场馆位置</h3>
              <el-empty v-if="locations.length === 0" description="暂无位置信息"></el-empty>
              <el-table v-else :data="locations" style="width: 100%" border>
                <el-table-column prop="name" label="位置名称" min-width="120" />
                <el-table-column prop="type" label="位置类型" width="120" />
                <el-table-column label="价格" width="120">
                  <template #default="{ row }">
                    <span v-if="row.price" class="location-price">¥{{ row.price }}/小时</span>
                    <span v-else class="location-base-price">¥{{ venueInfo.basePrice }}/小时<small>(场馆基准价)</small></span>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getLocationStatusType(row.status)">
                      {{ getLocationStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="位置描述" min-width="180" />
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="用户评价" name="reviews">
            <div class="venue-reviews" v-loading="reviewsLoading">
              <div class="reviews-header" v-if="reviewStats">
                <div class="review-stats">
                  <div class="stats-score">
                    <div class="average-score">{{ reviewStats.averageScore?.toFixed(1) || '0.0' }}</div>
                    <div class="score-stars">
                      <el-rate
                        :model-value="Number(reviewStats.averageScore) || 0"
                        disabled
                        show-score
                        text-color="#ff9900"
                      />
                    </div>
                    <div class="total-reviews">共{{ reviewStats.totalCount || 0 }}条评价</div>
                  </div>
                  <div class="score-distribution" v-if="reviewStats.scoreDistribution && reviewStats.scoreDistribution.length > 0">
                    <div v-for="item in reviewStats.scoreDistribution" :key="item.score" class="score-item">
                      <div class="score-label">{{ item.score }}分</div>
                      <div class="score-progress">
                        <el-progress 
                          :percentage="item.percentage * 100" 
                          :stroke-width="12" 
                          :color="item.score >= 4 ? '#67c23a' : item.score >= 3 ? '#e6a23c' : '#f56c6c'"
                        />
                      </div>
                      <div class="score-count">{{ item.count }}</div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="reviews-list">
                <div v-if="reviews.length === 0" class="no-reviews">
                  <el-empty description="暂无评价，预约后可以来评价哦~"></el-empty>
                </div>
                <div v-else class="review-items">
                  <div v-for="review in reviews" :key="review.id" class="review-item">
                    <!-- 评价头部：用户信息 -->
                    <div class="review-header">
                      <div class="review-user-avatar">
                        <img :src="review.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="用户头像">
                      </div>
                      <div class="review-user-info">
                        <div class="user-name-row">
                          <span class="user-name">{{ review.anonymous ? '匿名用户' : review.user.nickname || review.user.username }}</span>
                          <el-tag v-if="review.isBanned" size="small" type="danger" class="status-tag">已封禁</el-tag>
                        </div>
                        <div class="review-meta">
                          <span class="review-date">{{ formatDate(review.createdTime) }}</span>
                          <div class="review-overall-score">
                            <el-rate v-model="review.overallScore" disabled :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
                            <span class="score-text">{{ review.overallScore }}分</span>
                          </div>
                        </div>
                      </div>
                      <div class="review-actions-menu">
                        <el-dropdown trigger="click" @command="handleReviewCommand">
                          <span class="el-dropdown-link">
                            <i class="fas fa-ellipsis-v"></i>
                          </span>
                          <template #dropdown>
                            <el-dropdown-menu>
                              <el-dropdown-item :command="{ type: 'report', review: review }">举报评价</el-dropdown-item>
                              <el-dropdown-item v-if="isLoggedIn" :command="{ type: 'reply', review: review }">回复评价</el-dropdown-item>
                            </el-dropdown-menu>
                          </template>
                        </el-dropdown>
                      </div>
                    </div>
                    
                    <!-- 评价内容 -->
                    <div class="review-content">
                      <div class="review-text">{{ review.content }}</div>
                      <div v-if="review.isBanned" class="banned-tip">此评价已被封禁，可能包含不适当的内容</div>
                      
                      <!-- 评价图片 -->
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
                      
                      <!-- 详细评分 -->
                      <div class="detail-scores">
                        <div class="detail-score-item">
                          <span class="detail-score-label">环境</span>
                          <el-rate v-model="review.environmentScore" disabled size="small" :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
                        </div>
                        <div class="detail-score-item">
                          <span class="detail-score-label">设施</span>
                          <el-rate v-model="review.facilityScore" disabled size="small" :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
                        </div>
                        <div class="detail-score-item">
                          <span class="detail-score-label">服务</span>
                          <el-rate v-model="review.serviceScore" disabled size="small" :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
                        </div>
                        <div class="detail-score-item">
                          <span class="detail-score-label">性价比</span>
                          <el-rate v-model="review.costPerformanceScore" disabled size="small" :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
                        </div>
                      </div>
                    </div>
                    
                    <!-- 用户回复 -->
                    <div class="review-replies" v-if="review.replies && review.replies.length > 0">
                      <div v-for="reply in review.replies" :key="reply.id" class="reply-item">
                        <div class="reply-header">
                          <span class="reply-icon" :class="{ 'admin-reply': reply.isAdmin === 1 }">
                            {{ reply.isAdmin === 1 ? '管理员回复' : '用户回复' }}
                          </span>
                          <span class="reply-time">{{ formatDate(reply.replyTime) }}</span>
                        </div>
                        <div class="reply-content">{{ reply.content }}</div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="review-pagination" v-if="totalReviews > reviewSize">
                  <el-pagination
                    background
                    layout="prev, pager, next"
                    :total="totalReviews"
                    :page-size="reviewSize"
                    :current-page="reviewPage"
                    @current-change="handleReviewPageChange"
                  />
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="场馆信息不存在或已被删除"></el-empty>

    <!-- 添加举报对话框 -->
    <el-dialog
      title="举报评价"
      v-model="reportDialogVisible"
      width="90%"
      :close-on-click-modal="false"
    >
      <div v-loading="reportLoading">
        <div class="report-form">
          <div class="form-item">
            <div class="form-label">举报原因</div>
            <div class="form-input">
              <el-select v-model="reportForm.reason" placeholder="请选择举报原因" style="width: 100%">
                <el-option
                  v-for="reason in reportReasons"
                  :key="reason"
                  :label="reason"
                  :value="reason"
                />
              </el-select>
            </div>
          </div>
          
          <div class="form-item">
            <div class="form-label">详细说明</div>
            <div class="form-input">
              <el-input
                v-model="reportForm.reasonDetail"
                type="textarea"
                rows="4"
                placeholder="请输入详细说明（选填）..."
              />
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReport" :loading="reportLoading">提交举报</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加回复对话框 -->
    <el-dialog
      title="回复评价"
      v-model="replyDialogVisible"
      width="90%"
      :close-on-click-modal="false"
    >
      <div v-loading="replyLoading">
        <div v-if="currentReplyReview" class="reply-form">
          <!-- 显示原评价内容供参考 -->
          <div class="original-review">
            <div class="original-review-header">原评价内容:</div>
            <div class="original-review-text">{{ currentReplyReview.content }}</div>
          </div>
          
          <div class="form-item">
            <div class="form-label">回复内容</div>
            <div class="form-input">
              <el-input
                v-model="replyForm.content"
                type="textarea"
                rows="4"
                placeholder="请输入回复内容..."
              />
            </div>
          </div>
        </div>
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
/* 场馆详情页整体样式 */
.venue-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-actions {
  margin-bottom: 20px;
}

.venue-header {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.venue-image {
  flex: 0 0 50%;
  max-width: 50%;
  border-radius: 8px;
  overflow: hidden;
}

.venue-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.venue-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.venue-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.venue-title h1 {
  margin: 0;
  font-size: 24px;
}

.venue-detail-item {
  margin-bottom: 15px;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.venue-price-box {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 8px;
}

.price-label {
  font-size: 16px;
  color: #666;
}

.price-value {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
  margin-left: 5px;
}

.venue-tabs {
  margin-top: 30px;
}

.venue-description h3,
.venue-facilities h3,
.venue-locations h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.venue-description p {
  line-height: 1.6;
  color: #666;
}

.location-price {
  font-weight: bold;
  color: #f56c6c;
}

.location-base-price {
  color: #666;
}

.location-base-price small {
  display: block;
  font-size: 12px;
  color: #999;
}

.facility-price {
  font-weight: bold;
  color: #f56c6c;
}

.facility-base-price {
  color: #666;
}

.facility-base-price small {
  display: block;
  font-size: 12px;
  color: #999;
}

/* 评价相关样式 */
.venue-reviews {
  padding: 10px 0;
}

.reviews-header {
  margin-bottom: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.review-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
}

.stats-score {
  flex: 0 0 180px;
  text-align: center;
}

.average-score {
  font-size: 36px;
  font-weight: bold;
  color: #ff9900;
  line-height: 1;
  margin-bottom: 5px;
}

.score-stars {
  margin-bottom: 5px;
}

.total-reviews {
  color: #909399;
  font-size: 14px;
}

.score-distribution {
  flex: 1;
  min-width: 300px;
}

.score-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.score-label {
  width: 40px;
  text-align: right;
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}

.score-progress {
  flex: 1;
  margin-right: 10px;
}

.score-count {
  width: 30px;
  text-align: right;
  font-size: 14px;
  color: #606266;
}

.review-items {
  border-top: 1px solid #ebeef5;
}

.review-item {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.review-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
}

.review-user-avatar {
  margin-right: 10px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.review-user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.review-user-info {
  flex: 1;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 5px;
}

.user-name {
  font-weight: 500;
  font-size: 15px;
  color: #303133;
}

.review-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-date {
  font-size: 12px;
  color: #909399;
}

.review-overall-score {
  display: flex;
  align-items: center;
}

.score-text {
  margin-left: 8px;
  color: #ff9900;
  font-weight: 500;
}

.review-content {
  margin-bottom: 15px;
}

.review-text {
  margin-bottom: 10px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-line;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 15px 0;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  transition: transform 0.3s;
  cursor: pointer;
}

.review-image:hover {
  transform: scale(1.05);
}

.detail-scores {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 15px;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
}

.detail-score-item {
  display: flex;
  align-items: center;
  min-width: 180px;
}

.detail-score-label {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
  width: 60px;
  text-align: right;
}

.status-tag {
  margin-left: 5px;
  font-size: 12px;
  vertical-align: middle;
}

.review-replies {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-top: 15px;
  border-left: 3px solid #409eff;
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

.reply-time {
  font-size: 12px;
  color: #909399;
}

.reply-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  padding-left: 5px;
}

.review-pagination {
  margin-top: 20px;
  text-align: center;
}

.no-reviews {
  padding: 30px 0;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .venue-header {
    flex-direction: column;
  }
  
  .venue-image {
    flex: 0 0 100%;
    max-width: 100%;
    margin-bottom: 20px;
  }
  
  .venue-price-box {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .stats-score {
    flex: 0 0 100%;
    margin-bottom: 15px;
  }
  
  .score-distribution {
    width: 100%;
  }
  
  .review-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .review-overall-score {
    margin-top: 5px;
  }
  
  .detail-scores {
    flex-direction: column;
    gap: 10px;
    padding: 10px;
  }
  
  .detail-score-item {
    width: 100%;
  }
  
  .review-item {
    padding: 15px;
    margin-bottom: 10px;
  }
  
  .review-image {
    width: 60px;
    height: 60px;
  }
  
  .review-images {
    gap: 8px;
  }
  
  .venue-tabs :deep(.el-tabs__item) {
    padding: 0 10px;
  }
  
  .review-content {
    margin-bottom: 10px;
  }
}

.review-actions-menu {
  padding: 0 10px;
  cursor: pointer;
  color: #909399;
}

.el-dropdown-link {
  font-size: 16px;
  cursor: pointer;
}

.banned-tip {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 5px;
  padding: 5px;
  background-color: #fef0f0;
  border-radius: 4px;
}

.admin-reply {
  background-color: #67c23a;
}

.report-form {
  padding: 10px;
}

.form-item {
  margin-bottom: 20px;
}

.form-label {
  font-size: 14px;
  margin-bottom: 8px;
  color: #606266;
}

.form-input {
  width: 100%;
}

/* 原评价样式 */
.original-review {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.original-review-header {
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.original-review-text {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.reply-form {
  padding: 10px;
}
</style> 