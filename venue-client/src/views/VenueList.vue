<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { pageVenues, getAllVenueTypes, searchVenues } from '@/api/venue'
import type { Venue, VenueType, PageResult } from '@/types/venue'

const router = useRouter()
const route = useRoute()

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 12,
  name: '',
  keyword: '', // 新增关键词搜索字段
  venueTypeId: undefined as number | undefined,
  status: 1, // 只获取状态为开放的场馆
})

// 数据相关
const venueList = ref<Venue[]>([])
const venueTypes = ref<VenueType[]>([])
const total = ref(0)
const loading = ref(false)
const isSearching = ref(false) // 标记是否在使用搜索模式

// 获取场馆类型列表
const fetchVenueTypes = async () => {
  try {
    const res = await getAllVenueTypes()
    venueTypes.value = res.data
  } catch (error) {
    console.error('获取场馆类型失败', error)
  }
}

// 获取场馆列表
const fetchVenues = async () => {
  loading.value = true
  try {
    // 判断是使用关键词搜索还是普通查询
    let res;
    if (isSearching.value && queryParams.keyword) {
      res = await searchVenues({
        keyword: queryParams.keyword,
        venueTypeId: queryParams.venueTypeId,
        status: queryParams.status,
        page: queryParams.page,
        size: queryParams.size
      }) as { data: PageResult<Venue> }
    } else {
      res = await pageVenues(queryParams) as { data: PageResult<Venue> }
    }
    
    venueList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取场馆列表失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  queryParams.page = 1
  
  // 根据搜索条件决定使用哪种模式
  if (queryParams.keyword) {
    isSearching.value = true
    queryParams.name = '' // 清空普通名称搜索
  } else if (queryParams.name) {
    isSearching.value = false
    queryParams.keyword = '' // 清空关键词搜索
  } else {
    isSearching.value = false
  }
  
  fetchVenues()
}

// 重置筛选条件
const resetSearch = () => {
  queryParams.name = ''
  queryParams.keyword = ''
  queryParams.venueTypeId = undefined
  isSearching.value = false
  handleSearch()
}

// 处理分页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchVenues()
}

// 查看场馆详情
const viewVenueDetail = (id: number) => {
  router.push(`/venue/${id}`)
}

// 获取场馆类型名称
const getVenueTypeName = (typeId: number): string => {
  const venueType = venueTypes.value.find(type => type.id === typeId)
  return venueType ? venueType.name : '未知类型'
}

// 监听URL参数变化
watch(() => route.query, (newQuery) => {
  if (newQuery.keyword) {
    queryParams.keyword = newQuery.keyword as string
    isSearching.value = true
  }
  
  if (newQuery.venueTypeId) {
    queryParams.venueTypeId = parseInt(newQuery.venueTypeId as string)
  }
  
  handleSearch()
}, { immediate: true })

// 页面初始化
onMounted(() => {
  fetchVenueTypes()
  
  // 如果URL中没有查询参数，则执行默认查询
  if (!route.query.keyword && !route.query.venueTypeId) {
    fetchVenues()
  }
})
</script>

<template>
  <div class="venue-list-container">
    <div class="venue-search-section">
      <h1 class="section-title">场馆浏览</h1>
      <div class="search-form">
        <el-form :inline="true" :model="queryParams" class="venue-search-form">
          <!-- 关键词搜索 -->
          <el-form-item label="关键词搜索">
            <el-input 
              v-model="queryParams.keyword" 
              placeholder="场馆名称、地址、描述..." 
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          
          <!-- 传统搜索（仅按名称） -->
          <el-form-item label="场馆名称">
            <el-input 
              v-model="queryParams.name" 
              placeholder="场馆名称" 
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          
          <el-form-item label="场馆类型">
            <el-select v-model="queryParams.venueTypeId" placeholder="场馆类型" clearable>
              <el-option
                v-for="item in venueTypes"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="venue-list-section" v-loading="loading">
      <!-- 搜索结果提示 -->
      <div v-if="isSearching && queryParams.keyword" class="search-result-tip">
        <span>关键词 "{{ queryParams.keyword }}" 的搜索结果 ({{ total }})</span>
      </div>
      
      <template v-if="venueList.length > 0">
        <div class="venue-card-grid">
          <el-card 
            v-for="venue in venueList" 
            :key="venue.id" 
            class="venue-card"
            @click="viewVenueDetail(venue.id)"
          >
            <div class="venue-card-image">
              <img :src="venue.coverImage || `https://picsum.photos/300/200?random=${venue.id}`" alt="场馆图片" />
            </div>
            <div class="venue-card-content">
              <h3 class="venue-name">{{ venue.name }}</h3>
              <div class="venue-type">
                <el-tag size="small">{{ getVenueTypeName(venue.venueTypeId) }}</el-tag>
              </div>
              <div class="venue-price">
                <span class="price-label">基础价格:</span>
                <span class="price-value">¥{{ venue.basePrice }}/小时</span>
              </div>
              <p class="venue-address text-ellipsis">{{ venue.address }}</p>
            </div>
          </el-card>
        </div>

        <div class="pagination-container">
          <el-pagination
            v-model:currentPage="queryParams.page"
            :page-size="queryParams.size"
            layout="prev, pager, next, jumper"
            :total="total"
            @current-change="handleCurrentChange"
          />
        </div>
      </template>

      <el-empty v-else description="暂无符合条件的场馆"></el-empty>
    </div>
  </div>
</template>

<style scoped>
.venue-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.venue-search-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.search-result-tip {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f0f9eb;
  color: #67c23a;
  border-radius: 4px;
  font-size: 14px;
}

.venue-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.venue-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
}

.venue-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.venue-card-image {
  height: 200px;
  overflow: hidden;
}

.venue-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.5s;
}

.venue-card:hover .venue-card-image img {
  transform: scale(1.05);
}

.venue-card-content {
  padding: 15px;
}

.venue-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
}

.venue-type {
  margin-bottom: 10px;
}

.venue-price {
  margin-bottom: 10px;
  font-size: 14px;
}

.price-value {
  color: #f56c6c;
  font-weight: bold;
}

.venue-address {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .venue-search-form {
    display: flex;
    flex-direction: column;
  }
  
  .venue-search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 10px;
    width: 100%;
  }
  
  .el-input, .el-select {
    width: 100%;
  }
}
</style> 