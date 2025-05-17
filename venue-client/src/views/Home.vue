<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { pageVenues, getAllVenueTypes } from '@/api/venue'
import { getRecommendedVenues } from '@/api/recommendation'
import type { VenueType, Venue, PageResult } from '@/types/venue'
import VenueSearch from '@/components/VenueSearch.vue'

const router = useRouter()
const userStore = useUserStore()

// 轮播图数据
const carouselItems = [
  {
    id: 1,
    title: '夏季特惠',
    subtitle: '健身房预约享7折优惠',
    imageUrl: 'https://images.unsplash.com/photo-1590227632180-80a3bf110871?w=500&auto=format&fit=crop&q=60'
  },
  {
    id: 2,
    title: '全新羽毛球馆',
    subtitle: '专业场地，舒适体验',
    imageUrl: 'https://images.unsplash.com/photo-1591343395082-e120087004b4?w=500&auto=format&fit=crop&q=60'
  },
  {
    id: 3,
    title: '城市篮球赛',
    subtitle: '6月15日开始报名',
    imageUrl: 'https://images.unsplash.com/photo-1543245871-b631bb0bc907?w=500&auto=format&fit=crop&q=60'
  }
]

// 场馆类型预设图标
const venueTypeIcons = {
  '篮球场': 'basketball-ball',
  '足球场': 'futbol',
  '羽毛球馆': 'shuttlecock',
  '游泳馆': 'swimming-pool',
  '健身房': 'dumbbell',
  '乒乓球室': 'table-tennis',
  '排球场': 'volleyball-ball',
  '网球场': 'tennis-ball',
  '田径场': 'running'
}

// 数据相关
const venueTypes = ref<VenueType[]>([])
const recommendedVenues = ref<Venue[]>([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const activeCarouselIndex = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)

// 获取场馆类型
const fetchVenueTypes = async () => {
  try {
    const res = await getAllVenueTypes()
    venueTypes.value = res.data
  } catch (error) {
    console.error('获取场馆类型失败', error)
  }
}

// 获取推荐场馆，支持分页
const fetchRecommendedVenues = async (refresh = false) => {
  if (refreshing.value || (loading.value && !refresh)) return
  
  if (refresh) {
    currentPage.value = 1
    recommendedVenues.value = []
    finished.value = false
    refreshing.value = true
  } else {
    loading.value = true
  }
  
  try {
    // 使用推荐API或分页API获取场馆
    let res
    if (currentPage.value === 1 && userStore.isLoggedIn) {
      // 首页尝试获取个性化推荐
      try {
        res = await getRecommendedVenues(pageSize.value)
        console.log('获取个性化推荐场馆:', res)
      } catch (error) {
        console.warn('获取个性化推荐失败，使用普通分页:', error)
        // 如果推荐API失败，回退到普通分页
        res = await pageVenues({
          page: currentPage.value,
          size: pageSize.value,
          status: 1
        })
      }
    } else {
      // 加载更多时使用普通分页
      res = await pageVenues({
        page: currentPage.value,
        size: pageSize.value,
        status: 1
      })
    }
    
    const { records, total } = res.data
    
    if (refresh) {
      recommendedVenues.value = records
    } else {
      recommendedVenues.value = [...recommendedVenues.value, ...records]
    }
    
    // 判断是否全部加载完成
    finished.value = recommendedVenues.value.length >= total || records.length < pageSize.value
    
    if (!finished.value) {
      currentPage.value++
    }
  } catch (error) {
    console.error('获取推荐场馆失败', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新处理
const onRefresh = async () => {
  await fetchRecommendedVenues(true)
}

// 滚动到底部加载更多
const onLoad = async () => {
  if (!loading.value && !finished.value) {
    await fetchRecommendedVenues()
  }
}

// 切换轮播图
const changeCarousel = (index: number) => {
  activeCarouselIndex.value = index
}

// 自动轮播
const startCarousel = () => {
  setInterval(() => {
    activeCarouselIndex.value = (activeCarouselIndex.value + 1) % carouselItems.length
  }, 5000)
}

// 跳转到场馆详情
const goToVenueDetail = (id: number) => {
  router.push(`/venue/${id}`)
}

// 跳转到场馆列表，可带筛选条件
const goToVenueList = (typeId?: number) => {
  if (typeId) {
    router.push({ 
      path: '/venues',
      query: { venueTypeId: typeId.toString() }
    })
  } else {
    router.push('/venues')
  }
}

// 获取场馆类型的图标
const getVenueTypeIcon = (typeName: string): string => {
  const defaultIcon = 'building'
  if (!typeName) return defaultIcon
  
  for (const [key, value] of Object.entries(venueTypeIcons)) {
    if (typeName.includes(key)) {
      return value
    }
  }
  
  return defaultIcon
}

// 检测滚动到底部
const checkScrollBottom = () => {
  // 判断是否滚动到底部
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 如果距离底部100px以内，触发加载更多
  if (documentHeight - scrollTop - windowHeight < 100 && !loading.value && !finished.value) {
    onLoad()
  }
}

// 实现下拉刷新 (仅当页面在顶部时)
let startY = 0
let currentY = 0
const pullDistance = ref(0)
const pullThreshold = 80
const isPulling = ref(false)

const touchStart = (e) => {
  // 只有在页面顶部才启用下拉刷新
  if (window.pageYOffset === 0) {
    startY = e.touches[0].clientY
    isPulling.value = true
  }
}

const touchMove = (e) => {
  if (!isPulling.value) return
  
  currentY = e.touches[0].clientY
  pullDistance.value = Math.max(0, currentY - startY)
  
  // 下拉刷新时阻止默认滚动
  if (pullDistance.value > 0 && window.pageYOffset === 0) {
    e.preventDefault()
  }
}

const touchEnd = () => {
  if (!isPulling.value) return
  
  if (pullDistance.value > pullThreshold) {
    onRefresh()
  }
  
  // 重置状态
  pullDistance.value = 0
  isPulling.value = false
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

  // 监听窗口滚动
  window.addEventListener('scroll', checkScrollBottom)

  fetchVenueTypes()
  fetchRecommendedVenues()
  startCarousel()
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('scroll', checkScrollBottom)
})

// 导航到其他页面
const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div 
    class="home-container"
    @touchstart="touchStart"
    @touchmove="touchMove"
    @touchend="touchEnd"
  >
    <!-- 下拉刷新指示器 (仅在下拉时显示) -->
    <div 
      class="pull-refresh-indicator" 
      :style="{height: `${pullDistance.value}px`, opacity: pullDistance.value / pullThreshold}"
      :class="{'refreshing': refreshing}"
    >
      <i class="fas fa-spinner" :class="{'fa-spin': refreshing}"></i>
      <span>{{ refreshing ? '刷新中...' : '下拉刷新' }}</span>
    </div>
    
    <!-- iOS状态栏 -->
    <div class="ios-status-bar">
      <div class="time">9:41</div>
      <div class="status-icons">
        <i class="fas fa-signal"></i>
        <i class="fas fa-wifi"></i>
        <i class="fas fa-battery-full"></i>
      </div>
    </div>

    <!-- 固定在顶部的搜索部分 -->
    <div class="sticky-search-container">
      <!-- 搜索栏 -->
      <div class="ios-search-bar">
        <i class="fas fa-search"></i>
        <input
          type="text"
          placeholder="搜索场馆"
          @click="router.push('/venues')"
          readonly
        />
      </div>

      <!-- 添加搜索组件 -->
      <div class="search-section">
        <div class="search-title">
          <span>搜索场馆</span>
        </div>
        <VenueSearch />
      </div>
    </div>

    <!-- 内容部分添加上边距，为固定的搜索栏留出空间 -->
    <div class="scrollable-content">
      <!-- 轮播图 -->
      <div class="carousel-container">
        <div class="ios-carousel">
          <div
            v-for="(item, index) in carouselItems"
            :key="item.id"
            class="ios-carousel-slide"
            :style="{ display: index === activeCarouselIndex ? 'block' : 'none' }"
          >
            <img :src="item.imageUrl" :alt="item.title" />
            <div class="banner-content">
              <h3 class="banner-title">{{ item.title }}</h3>
              <p class="banner-subtitle">{{ item.subtitle }}</p>
            </div>
          </div>
        </div>
        <div class="ios-carousel-dots">
          <div
            v-for="(item, index) in carouselItems"
            :key="item.id"
            class="ios-carousel-dot"
            :class="{ active: index === activeCarouselIndex }"
            @click="changeCarousel(index)"
          ></div>
        </div>
      </div>

      <!-- 场馆分类 -->
      <div class="category-section">
        <div class="category-title">
          <span>场馆分类</span>
          <a href="#" class="see-all" @click.prevent="goToVenueList()">查看全部</a>
        </div>
        <div class="category-grid">
          <div
            v-for="type in venueTypes.slice(0, 8)"
            :key="type.id"
            class="category-item"
            @click="goToVenueList(type.id)"
          >
            <div class="category-icon">
              <i :class="`fas fa-${getVenueTypeIcon(type.name)}`"></i>
            </div>
            <span class="category-name">{{ type.name }}</span>
          </div>
          <div class="category-item" v-if="venueTypes.length > 8" @click="goToVenueList()">
            <div class="category-icon">
              <i class="fas fa-ellipsis-h"></i>
            </div>
            <span class="category-name">更多</span>
          </div>
        </div>
      </div>

      <!-- 推荐场馆（支持下拉刷新和加载更多） -->
      <div class="category-section">
        <div class="category-title">
          <span>推荐场馆</span>
          <a href="#" class="see-all" @click.prevent="goToVenueList()">查看全部</a>
        </div>
        
        <div class="recommended-list">
          <div 
            v-for="venue in recommendedVenues" 
            :key="venue.id" 
            class="venue-list-item"
            @click="goToVenueDetail(venue.id)"
          >
            <div class="venue-item-image">
              <img 
                :src="venue.coverImage || `https://picsum.photos/300/200?random=${venue.id}`" 
                :alt="venue.name" 
              />
            </div>
            <div class="venue-item-info">
              <h3 class="venue-name">{{ venue.name }}</h3>
              <p class="venue-address">
                <i class="fas fa-map-marker-alt"></i>
                {{ venue.address }}
              </p>
              <div class="venue-meta">
                <div class="venue-rating">
                  <i class="fas fa-star"></i>
                  <span>4.8 (124条评价)</span>
                </div>
                <div class="venue-price">¥{{ venue.basePrice }}/小时起</div>
              </div>
              <div class="venue-tags">
                <span class="venue-tag" v-if="venue.capacity">容纳{{ venue.capacity }}人</span>
                <span class="venue-tag">{{ venueTypes.find(t => t.id === venue.venueTypeId)?.name }}</span>
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
            <span>没有更多了</span>
          </div>
        </div>
      </div>
    </div>

    <!-- iOS底部标签栏 -->
    <div class="ios-tab-bar">
      <a class="tab-item active" @click="navigateTo('/')">
        <i class="fas fa-home"></i>
        <span>首页</span>
      </a>
      <a class="tab-item" @click="navigateTo('/recommendation')">
        <i class="fas fa-compass"></i>
        <span>推荐</span>
      </a>
      <a class="tab-item" @click="navigateTo('/orders')">
        <i class="fas fa-calendar-alt"></i>
        <span>订单</span>
      </a>
      <a class="tab-item" @click="navigateTo('/profile')">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 540px;
  margin: 0 auto;
  padding: 10px 15px;
  background-color: #f8f8f8;
  min-height: 100vh;
  position: relative;
  padding-bottom: 70px;
}

/* 固定在顶部的搜索容器 */
.sticky-search-container {
  position: sticky;
  top: 0;
  background-color: #f8f8f8;
  z-index: 50;
  padding-bottom: 10px;
  margin-bottom: 10px;
  /* 添加阴影效果，滚动时更加明显 */
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

/* 可滚动内容区域 */
.scrollable-content {
  padding-top: 10px;
}

/* 下拉刷新指示器 */
.pull-refresh-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 0;
  overflow: hidden;
  background-color: transparent;
  color: #007aff;
  font-size: 14px;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 60; /* 确保显示在搜索栏之上 */
  transition: height 0.2s;
}

.pull-refresh-indicator i {
  margin-right: 8px;
}

.pull-refresh-indicator.refreshing {
  height: 50px !important;
  background-color: #f8f8f8;
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

/* iOS搜索栏样式 */
.ios-search-bar {
  background-color: #e9e9e9;
  border-radius: 10px;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.ios-search-bar i {
  color: #8e8e93;
  font-size: 14px;
  margin-right: 8px;
}

.ios-search-bar input {
  border: none;
  background: none;
  flex-grow: 1;
  font-size: 16px;
  outline: none;
  color: #333;
}

.ios-search-bar input::placeholder {
  color: #8e8e93;
}

/* 轮播图样式 */
.carousel-container {
  position: relative;
  margin-bottom: 20px;
}

.ios-carousel {
  border-radius: 10px;
  overflow: hidden;
  height: 200px;
}

.ios-carousel-slide {
  height: 200px;
  position: relative;
}

.ios-carousel-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ios-carousel-dots {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: flex;
  gap: 5px;
}

.ios-carousel-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
}

.ios-carousel-dot.active {
  background-color: white;
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 15px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0));
  color: white;
}

.banner-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 5px;
}

.banner-subtitle {
  font-size: 14px;
  opacity: 0.9;
}

/* 分类部分样式 */
.category-section {
  margin-bottom: 25px;
}

.category-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.see-all {
  color: #007aff;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
}

.category-icon {
  width: 60px;
  height: 60px;
  border-radius: 15px;
  background-color: rgba(0, 122, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.category-icon i {
  font-size: 24px;
  color: #007aff;
}

.category-name {
  font-size: 13px;
  color: #333;
}

/* 推荐场馆列表样式 */
.recommended-list {
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.venue-list-item {
  display: flex;
  padding: 15px;
  border-bottom: 1px solid #f2f2f7;
  cursor: pointer;
}

.venue-list-item:last-child {
  border-bottom: none;
}

.venue-item-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 12px;
  flex-shrink: 0;
}

.venue-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.venue-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* 加载状态样式 */
.loading-status {
  display: flex;
  justify-content: center;
  padding: 15px;
  font-size: 14px;
  color: #8e8e93;
}

.loading, .no-more {
  display: flex;
  align-items: center;
}

.loading i {
  margin-right: 6px;
}

.no-more {
  color: #c7c7cc;
}

.venue-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 5px;
}

.venue-address {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.venue-address i {
  margin-right: 5px;
  font-size: 12px;
}

.venue-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.venue-rating {
  display: flex;
  align-items: center;
  color: #ff9500;
}

.venue-rating span {
  color: #333;
  margin-left: 4px;
}

.venue-price {
  color: #ff3b30;
  font-weight: 500;
}

.venue-tags {
  display: flex;
  flex-wrap: wrap;
  margin-top: 8px;
  gap: 6px;
}

.venue-tag {
  font-size: 12px;
  background-color: #f0f0f0;
  padding: 3px 8px;
  border-radius: 100px;
  color: #666;
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
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .venue-item-image {
    width: 80px;
    height: 80px;
  }
}

/* 搜索部分的样式 */
.search-section {
  margin: 20px 0 30px;
  padding: 0 15px;
}

.search-title {
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
</style>
