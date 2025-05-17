<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPagedRecommendations, recordBrowseHistory, markRecommendationAsClicked, submitRecommendationFeedback } from '@/api/recommendation'
import type { VenueRecommendationVO } from '@/types/recommendation'

const router = useRouter()
const userStore = useUserStore()

// 数据相关
const recommendedVenues = ref<VenueRecommendationVO[]>([])
const loading = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const finished = ref(false)

// 筛选相关
const showFilterPanel = ref(false)
const filterOptions = reactive({
  venueType: '',
  priceRange: '',
  distance: ''
})

// 记录页面停留的开始时间
const pageStartTime = ref(Date.now())

// 获取推荐场馆列表
const fetchRecommendations = async (refresh = false) => {
  if (refresh) {
    currentPage.value = 1
    recommendedVenues.value = []
    finished.value = false
  }
  
  if (finished.value) return
  
  loading.value = refresh ? false : true
  refreshing.value = refresh
  
  try {
    // 这里可以加入筛选条件参数
    const res = await getPagedRecommendations(currentPage.value, pageSize.value)
    const { records, total: totalCount } = res.data
    
    total.value = totalCount
    
    if (refresh) {
      recommendedVenues.value = records
    } else {
      recommendedVenues.value = [...recommendedVenues.value, ...records]
    }
    
    if (recommendedVenues.value.length >= total.value) {
      finished.value = true
    } else {
      currentPage.value++
    }
  } catch (error) {
    console.error('获取推荐场馆失败', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  fetchRecommendations(true)
}

// 点击场馆
const onVenueClick = async (venue: VenueRecommendationVO) => {
  try {
    // 记录点击
    if (venue.id) {
      await markRecommendationAsClicked(venue.id)
    }
    
    // 跳转到场馆详情
    router.push(`/venue/${venue.venueId}`)
  } catch (error) {
    console.error('记录点击失败', error)
  }
}

// 计算场馆停留时间并记录浏览历史
const recordVenueBrowseHistory = (venue: VenueRecommendationVO) => {
  const now = Date.now()
  const stayDuration = Math.floor((now - pageStartTime.value) / 1000)
  pageStartTime.value = now
  
  if (venue.venueId) {
    console.log(`[推荐页]记录场馆${venue.venueId}浏览历史，停留时间：${stayDuration}秒，用户ID：${userStore.userInfo?.id}`)
    recordBrowseHistory(venue.venueId, stayDuration, 0)
      .then(() => console.log(`[推荐页]场馆${venue.venueId}浏览历史记录成功`))
      .catch(error => console.error(`[推荐页]记录场馆${venue.venueId}浏览历史失败`, error))
  }
}

// 导航到其他页面
const navigateTo = (path: string) => {
  router.push(path)
}

// 提交推荐反馈
const submitFeedback = async (venue: VenueRecommendationVO, feedbackType: number) => {
  try {
    await submitRecommendationFeedback({
      recommendationId: venue.id,
      venueId: venue.venueId,
      feedbackType, // 1-喜欢, 2-不喜欢
      reason: feedbackType === 2 ? '不感兴趣' : undefined
    })
    // 成功后可以更新UI或提示用户
    if (feedbackType === 1) {
      venue.liked = true
      venue.disliked = false
    } else {
      venue.liked = false
      venue.disliked = true
    }
  } catch (error) {
    console.error('提交反馈失败', error)
  }
}

// 收藏场馆
const toggleFavorite = (venue: VenueRecommendationVO) => {
  venue.favorited = !venue.favorited
  // 这里应该调用API保存收藏状态，但目前API中没有这个功能
  console.log('场馆收藏状态:', venue.favorited ? '已收藏' : '未收藏')
}

// 应用筛选
const applyFilter = () => {
  showFilterPanel.value = false
  fetchRecommendations(true)
}

// 重置筛选
const resetFilter = () => {
  filterOptions.venueType = ''
  filterOptions.priceRange = ''
  filterOptions.distance = ''
  applyFilter()
}

onMounted(async () => {
  // 如果未登录，重定向到登录页
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  // 确保已加载用户信息
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败', error)
      router.push('/login')
    }
  }

  fetchRecommendations()
})

onUnmounted(() => {
  // 页面离开时，如果有选中的场馆，记录浏览历史
  if (recommendedVenues.value.length > 0) {
    recordVenueBrowseHistory(recommendedVenues.value[0])
  }
})
</script>

<template>
  <div class="recommendation-container">
    <div class="ios-status-bar">
      <div class="time">9:41</div>
      <div class="status-icons">
        <i class="fas fa-signal"></i>
        <i class="fas fa-wifi"></i>
        <i class="fas fa-battery-full"></i>
      </div>
    </div>
    
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="title">为您推荐</div>
      <div class="header-actions">
        <div class="filter-btn" @click="showFilterPanel = !showFilterPanel">
          <i class="fas fa-filter"></i>
        </div>
        <div class="refresh-btn" @click="onRefresh">
          <i class="fas fa-sync-alt" :class="{ 'rotating': refreshing }"></i>
        </div>
      </div>
    </div>

    <!-- 筛选面板 -->
    <div class="filter-panel" v-show="showFilterPanel">
      <div class="filter-header">
        <h3>筛选条件</h3>
        <i class="fas fa-times" @click="showFilterPanel = false"></i>
      </div>
      
      <div class="filter-item">
        <label>场馆类型</label>
        <select v-model="filterOptions.venueType">
          <option value="">全部</option>
          <option value="1">体育馆</option>
          <option value="2">会议室</option>
          <option value="3">演出厅</option>
          <option value="4">多功能厅</option>
        </select>
      </div>
      
      <div class="filter-item">
        <label>价格范围</label>
        <select v-model="filterOptions.priceRange">
          <option value="">全部</option>
          <option value="1">0-100元/小时</option>
          <option value="2">100-300元/小时</option>
          <option value="3">300-500元/小时</option>
          <option value="4">500元以上/小时</option>
        </select>
      </div>
      
      <div class="filter-item">
        <label>距离范围</label>
        <select v-model="filterOptions.distance">
          <option value="">全部</option>
          <option value="1">1公里内</option>
          <option value="2">3公里内</option>
          <option value="3">5公里内</option>
          <option value="4">10公里内</option>
        </select>
      </div>
      
      <div class="filter-actions">
        <button class="reset-btn" @click="resetFilter">重置</button>
        <button class="apply-btn" @click="applyFilter">应用</button>
      </div>
    </div>
    
    <!-- 推荐场馆列表 -->
    <div class="recommendation-list" v-if="recommendedVenues.length > 0">
      <div 
        v-for="venue in recommendedVenues" 
        :key="venue.id" 
        class="recommendation-item"
      >
        <div class="recommendation-card">
          <div class="card-image" @click="onVenueClick(venue)">
            <img :src="venue.coverImage || 'https://via.placeholder.com/300x150'" :alt="venue.venueName">
            <div class="recommendation-type">
              <span>{{ venue.recommendationTypeName }}</span>
            </div>
          </div>
          <div class="card-content" @click="onVenueClick(venue)">
            <div class="venue-name">{{ venue.venueName }}</div>
            <div class="venue-type">{{ venue.venueTypeName }}</div>
            <div class="venue-address">
              <i class="fas fa-map-marker-alt"></i>
              <span>{{ venue.address }}</span>
            </div>
            <div class="venue-price">¥{{ venue.basePrice }}/小时起</div>
            <div class="recommendation-reason">{{ venue.recommendationReason }}</div>
          </div>
          <div class="card-actions">
            <div class="action-btn" @click="toggleFavorite(venue)">
              <i :class="['fas', venue.favorited ? 'fa-heart' : 'fa-heart-o']" :style="{ color: venue.favorited ? '#ff3b30' : '#8e8e93' }"></i>
              <span>{{ venue.favorited ? '已收藏' : '收藏' }}</span>
            </div>
            <div class="action-btn" @click="submitFeedback(venue, 1)">
              <i :class="['fas', venue.liked ? 'fa-thumbs-up' : 'fa-thumbs-o-up']" :style="{ color: venue.liked ? '#007aff' : '#8e8e93' }"></i>
              <span>喜欢</span>
            </div>
            <div class="action-btn" @click="submitFeedback(venue, 2)">
              <i :class="['fas', venue.disliked ? 'fa-thumbs-down' : 'fa-thumbs-o-down']" :style="{ color: venue.disliked ? '#8e8e93' : '#8e8e93' }"></i>
              <span>不感兴趣</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div class="loading-status">
        <div v-if="loading" class="loading">
          <i class="fas fa-spinner fa-spin"></i>
          <span>加载中...</span>
        </div>
        <div v-else-if="finished" class="no-more">
          <span>没有更多推荐了</span>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <i class="fas fa-compass"></i>
      <p>暂无推荐场馆</p>
      <p class="empty-subtitle">浏览更多场馆，我们将为您提供更个性化的推荐</p>
      <button @click="navigateTo('/venues')">浏览场馆</button>
    </div>
    
    <!-- 加载中 -->
    <div v-else class="loading-container">
      <i class="fas fa-spinner fa-spin"></i>
      <span>加载中...</span>
    </div>
    
    <!-- iOS底部标签栏 -->
    <div class="ios-tab-bar">
      <a class="tab-item" @click="navigateTo('/')">
        <i class="fas fa-home"></i>
        <span>首页</span>
      </a>
      <a class="tab-item active">
        <i class="fas fa-compass"></i>
        <span>推荐</span>
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

<style scoped>
.recommendation-container {
  max-width: 540px;
  margin: 0 auto;
  padding: 10px 15px;
  background-color: #f8f8f8;
  min-height: 100vh;
  position: relative;
  padding-bottom: 70px;
}

/* iOS状态栏样式 */
.ios-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 15px;
  font-size: 14px;
  font-weight: 500;
  background-color: #f8f8f8;
  color: #333;
}

.status-icons {
  display: flex;
  gap: 5px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 15px 0 20px;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.refresh-btn, .filter-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.refresh-btn i, .filter-btn i {
  font-size: 16px;
  color: #007aff;
}

.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 筛选面板 */
.filter-panel {
  position: absolute;
  top: 100px;
  left: 15px;
  right: 15px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  padding: 15px;
  z-index: 100;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.filter-header i {
  cursor: pointer;
  font-size: 18px;
  color: #8e8e93;
}

.filter-item {
  margin-bottom: 15px;
}

.filter-item label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #333;
}

.filter-item select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  color: #333;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.reset-btn, .apply-btn {
  padding: 8px 15px;
  border-radius: 5px;
  font-size: 14px;
  cursor: pointer;
}

.reset-btn {
  background: #f2f2f7;
  color: #333;
  border: none;
}

.apply-btn {
  background: #007aff;
  color: white;
  border: none;
}

/* 推荐列表样式 */
.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recommendation-item {
  cursor: pointer;
}

.recommendation-card {
  background-color: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.card-image {
  height: 150px;
  position: relative;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommendation-type {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(0, 122, 255, 0.8);
  color: white;
  padding: 4px 8px;
  border-radius: 100px;
  font-size: 12px;
}

.card-content {
  padding: 15px;
}

.venue-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 5px;
  color: #333;
}

.venue-type {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.venue-address {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.venue-address i {
  margin-right: 5px;
  font-size: 12px;
}

.venue-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff3b30;
  margin-bottom: 5px;
}

.recommendation-reason {
  font-size: 14px;
  color: #007aff;
  margin-top: 8px;
}

/* 卡片底部操作栏 */
.card-actions {
  display: flex;
  justify-content: space-around;
  padding: 10px 15px;
  border-top: 1px solid #f2f2f7;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.action-btn i {
  font-size: 18px;
}

.action-btn span {
  font-size: 12px;
  color: #8e8e93;
}

/* 加载状态 */
.loading-status {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

.loading, .no-more {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8e8e93;
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  text-align: center;
  color: #8e8e93;
}

.empty-state i {
  font-size: 60px;
  margin-bottom: 20px;
  color: #c7c7cc;
}

.empty-state p {
  font-size: 18px;
  margin-bottom: 10px;
}

.empty-subtitle {
  font-size: 14px;
  width: 80%;
  margin-bottom: 20px;
}

.empty-state button {
  background-color: #007aff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 100px;
  font-size: 16px;
  cursor: pointer;
}

/* 加载中 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  gap: 15px;
  color: #8e8e93;
}

.loading-container i {
  font-size: 30px;
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
  color: #007aff;
}

@media (max-width: 480px) {
  .recommendation-list {
    gap: 12px;
  }
  
  .card-image {
    height: 120px;
  }
  
  .card-content {
    padding: 12px;
  }
  
  .venue-name {
    font-size: 16px;
  }
}
</style> 