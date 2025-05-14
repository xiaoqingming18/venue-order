<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { pageVenues, getAllVenueTypes } from '@/api/venue'
import type { VenueType, Venue, PageResult } from '@/types/venue'

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

// 模拟最近使用数据
const recentVenues = ref([
  {
    id: 1,
    name: '星动篮球馆',
    address: '海淀区新源南路8号',
    rating: 4.8,
    lastUsed: '昨天',
    imageUrl: 'https://images.unsplash.com/photo-1580261450046-d0a30080dc9b?w=500&auto=format&fit=crop&q=60'
  },
  {
    id: 2,
    name: '力健健身中心',
    address: '东城区东直门外大街42号',
    rating: 4.7,
    lastUsed: '3天前',
    imageUrl: 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=500&auto=format&fit=crop&q=60'
  }
])

// 数据相关
const venueTypes = ref<VenueType[]>([])
const recommendedVenues = ref<Venue[]>([])
const popularVenues = ref<Venue[]>([])
const loading = ref(false)
const activeCarouselIndex = ref(0)

// 获取场馆类型
const fetchVenueTypes = async () => {
  try {
    const res = await getAllVenueTypes()
    venueTypes.value = res.data
  } catch (error) {
    console.error('获取场馆类型失败', error)
  }
}

// 获取推荐场馆
const fetchRecommendedVenues = async () => {
  loading.value = true
  try {
    const res = await pageVenues({
      page: 1,
      size: 6,
      status: 1
    }) as { data: PageResult<Venue> }
    recommendedVenues.value = res.data.records
  } catch (error) {
    console.error('获取推荐场馆失败', error)
  } finally {
    loading.value = false
  }
}

// 获取热门场馆
const fetchPopularVenues = async () => {
  try {
    const res = await pageVenues({
      page: 1,
      size: 6,
      status: 1
    }) as { data: PageResult<Venue> }
    popularVenues.value = res.data.records
  } catch (error) {
    console.error('获取热门场馆失败', error)
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

  fetchVenueTypes()
  fetchRecommendedVenues()
  fetchPopularVenues()
  startCarousel()
})

// 退出登录
const logout = () => {
  userStore.logout()
}

// 导航到其他页面
const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div class="home-container">
    <!-- iOS状态栏 -->
    <div class="ios-status-bar">
      <div class="time">9:41</div>
      <div class="status-icons">
        <i class="fas fa-signal"></i>
        <i class="fas fa-wifi"></i>
        <i class="fas fa-battery-full"></i>
      </div>
    </div>

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

    <!-- 推荐场馆 -->
    <div class="category-section" v-loading="loading">
      <div class="category-title">
        <span>推荐场馆</span>
        <a href="#" class="see-all" @click.prevent="goToVenueList()">查看全部</a>
      </div>
      <div class="venue-row">
        <a 
          v-for="venue in recommendedVenues" 
          :key="venue.id" 
          href="#" 
          class="venue-card"
          @click.prevent="goToVenueDetail(venue.id)"
        >
          <img 
            :src="venue.coverImage || `https://picsum.photos/300/200?random=${venue.id}`" 
            :alt="venue.name" 
            class="venue-image"
          />
          <div class="venue-info">
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
        </a>
      </div>
    </div>

    <!-- 热门场馆 -->
    <div class="category-section">
      <div class="category-title">
        <span>热门场馆</span>
        <a href="#" class="see-all" @click.prevent="goToVenueList()">查看全部</a>
      </div>
      <div class="venue-row">
        <a 
          v-for="venue in popularVenues" 
          :key="venue.id" 
          href="#" 
          class="venue-card"
          @click.prevent="goToVenueDetail(venue.id)"
        >
          <img 
            :src="venue.coverImage || `https://picsum.photos/300/200?random=${venue.id + 10}`" 
            :alt="venue.name" 
            class="venue-image"
          />
          <div class="venue-info">
            <h3 class="venue-name">{{ venue.name }}</h3>
            <p class="venue-address">
              <i class="fas fa-map-marker-alt"></i>
              {{ venue.address }}
            </p>
            <div class="venue-meta">
              <div class="venue-rating">
                <i class="fas fa-star"></i>
                <span>4.6 (98条评价)</span>
              </div>
              <div class="venue-price">¥{{ venue.basePrice }}/小时起</div>
            </div>
            <div class="venue-tags">
              <span class="venue-tag" v-if="venue.capacity">容纳{{ venue.capacity }}人</span>
              <span class="venue-tag">{{ venueTypes.find(t => t.id === venue.venueTypeId)?.name }}</span>
            </div>
          </div>
        </a>
      </div>
    </div>

    <!-- 最近使用 -->
    <div class="recent-venues">
      <div class="category-title">
        <span>最近使用</span>
      </div>
      <a 
        v-for="venue in recentVenues" 
        :key="venue.id" 
        href="#" 
        class="recent-venue"
        @click.prevent="goToVenueDetail(venue.id)"
      >
        <img 
          :src="venue.imageUrl" 
          :alt="venue.name" 
          class="recent-venue-image"
        />
        <div class="recent-venue-info">
          <h3 class="venue-name">{{ venue.name }}</h3>
          <p class="venue-address">
            <i class="fas fa-map-marker-alt"></i>
            {{ venue.address }}
          </p>
          <div class="venue-meta">
            <div class="venue-rating">
              <i class="fas fa-star"></i>
              <span>{{ venue.rating }}</span>
            </div>
            <div class="venue-price">上次使用: {{ venue.lastUsed }}</div>
          </div>
        </div>
      </a>
    </div>

    <!-- iOS底部标签栏 -->
    <div class="ios-tab-bar">
      <a class="tab-item active" @click="navigateTo('/')">
        <i class="fas fa-home"></i>
        <span>首页</span>
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

/* 场馆卡片样式 */
.venue-row {
  display: flex;
  overflow-x: auto;
  padding-bottom: 10px;
  scrollbar-width: none;
  -ms-overflow-style: none;
  gap: 12px;
}

.venue-row::-webkit-scrollbar {
  display: none;
}

.venue-card {
  flex: 0 0 auto;
  width: 250px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
  background-color: white;
  text-decoration: none;
  color: inherit;
}

.venue-image {
  height: 150px;
  width: 100%;
  object-fit: cover;
}

.venue-info {
  padding: 12px;
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

/* 最近使用场馆样式 */
.recent-venues {
  margin-bottom: 20px;
}

.recent-venue {
  display: flex;
  background-color: white;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 10px;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.05);
  text-decoration: none;
  color: inherit;
}

.recent-venue-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
}

.recent-venue-info {
  flex: 1;
  padding: 10px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
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
  
  .venue-card {
    width: 200px;
  }
}
</style>
