<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchVenues, getAllVenueTypes } from '@/api/venue'
import type { Venue, VenueType, PageResult } from '@/types/venue'

const router = useRouter()
const emit = defineEmits(['search-complete'])

// 查询参数
const queryParams = reactive({
  keyword: '',
  venueTypeId: undefined as number | undefined,
  status: 1, // 默认只搜索开放状态的场馆
  page: 1,
  size: 8 // 默认每页显示8条
})

// 数据相关
const venueList = ref<Venue[]>([])
const venueTypes = ref<VenueType[]>([])
const total = ref(0)
const loading = ref(false)
const showResults = ref(false)

// 获取场馆类型列表
const fetchVenueTypes = async () => {
  try {
    const res = await getAllVenueTypes()
    venueTypes.value = res.data
  } catch (error) {
    console.error('获取场馆类型失败', error)
  }
}

// 搜索场馆
const searchVenuesByKeyword = async () => {
  loading.value = true
  showResults.value = true
  
  try {
    const res = await searchVenues(queryParams) as { data: PageResult<Venue> }
    venueList.value = res.data.records
    total.value = res.data.total
    
    // 触发搜索完成事件，通知父组件
    emit('search-complete', {
      venues: venueList.value,
      total: total.value,
      keyword: queryParams.keyword
    })
  } catch (error) {
    console.error('搜索场馆失败', error)
    ElMessage.error('搜索失败，请稍后重试')
    venueList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  queryParams.page = 1
  searchVenuesByKeyword()
}

// 键盘Enter事件处理
const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    handleSearch()
  }
}

// 重置筛选条件
const resetSearch = () => {
  queryParams.keyword = ''
  queryParams.venueTypeId = undefined
  showResults.value = false
}

// 处理分页变化
const handleCurrentChange = (val: number) => {
  queryParams.page = val
  searchVenuesByKeyword()
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

// 关注输入框聚焦事件
const handleFocus = () => {
  if (queryParams.keyword && venueList.value.length > 0) {
    showResults.value = true
  }
}

// 关注输入框失焦事件
const handleBlur = () => {
  // 使用延时，以便能够处理点击搜索结果的事件
  setTimeout(() => {
    showResults.value = false
  }, 200)
}

// 监听关键词变化，当关键词清空时隐藏结果
watch(() => queryParams.keyword, (newVal) => {
  if (!newVal) {
    showResults.value = false
  }
})

onMounted(() => {
  fetchVenueTypes()
})
</script>

<template>
  <div class="venue-search-container">
    <div class="search-box">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索场馆名称、地址、描述..."
        clearable
        @keydown="handleKeyDown"
        @focus="handleFocus"
        @blur="handleBlur"
        prefix-icon="el-icon-search"
      >
        <template #append>
          <el-button @click="handleSearch" :loading="loading">搜索</el-button>
        </template>
      </el-input>
      
      <div class="search-filter">
        <el-select 
          v-model="queryParams.venueTypeId" 
          placeholder="场馆类型" 
          clearable
          @change="handleSearch"
        >
          <el-option
            v-for="item in venueTypes"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </div>
    </div>
    
    <!-- 搜索结果弹出框 -->
    <div v-if="showResults" class="search-results" v-loading="loading">
      <div v-if="venueList.length > 0" class="results-container">
        <div class="results-header">
          <span>搜索结果 ({{ total }})</span>
          <el-link @click="showResults = false" class="close-link">关闭</el-link>
        </div>
        
        <div class="venue-items">
          <div 
            v-for="venue in venueList" 
            :key="venue.id" 
            class="venue-item"
            @click="viewVenueDetail(venue.id)"
          >
            <div class="venue-item-image">
              <img :src="venue.coverImage || `https://picsum.photos/100/75?random=${venue.id}`" alt="场馆图片" />
            </div>
            <div class="venue-item-info">
              <div class="venue-item-name">{{ venue.name }}</div>
              <div class="venue-item-meta">
                <span class="venue-item-type">{{ getVenueTypeName(venue.venueTypeId) }}</span>
                <span class="venue-item-price">¥{{ venue.basePrice }}/小时</span>
              </div>
              <div class="venue-item-address text-ellipsis">{{ venue.address }}</div>
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="total > queryParams.size">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="queryParams.size"
            :current-page="queryParams.page"
            @current-change="handleCurrentChange"
            hide-on-single-page
          />
        </div>
        
        <!-- 查看全部结果按钮 -->
        <div class="view-all-results">
          <el-button 
            type="primary" 
            text
            @click="router.push({ path: '/venues', query: { keyword: queryParams.keyword } })"
          >
            查看全部结果
          </el-button>
        </div>
      </div>
      
      <div v-else-if="!loading" class="no-results">
        <el-empty description="未找到相关场馆">
          <template #description>
            <p>没有找到相关的场馆，请尝试其他关键词</p>
          </template>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<style scoped>
.venue-search-container {
  position: relative;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.search-box .el-input {
  flex: 1;
}

.search-filter {
  width: 140px;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-top: 5px;
  max-height: 500px;
  overflow-y: auto;
}

.results-container {
  padding: 10px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
  font-weight: bold;
}

.venue-items {
  padding: 10px 0;
}

.venue-item {
  display: flex;
  padding: 10px;
  border-bottom: 1px solid #f2f2f2;
  cursor: pointer;
  transition: background-color 0.3s;
}

.venue-item:hover {
  background-color: #f9f9f9;
}

.venue-item:last-child {
  border-bottom: none;
}

.venue-item-image {
  width: 100px;
  height: 75px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 10px;
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

.venue-item-name {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
  margin-bottom: 4px;
}

.venue-item-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.venue-item-type {
  font-size: 12px;
  color: #409eff;
  background-color: #ecf5ff;
  padding: 2px 6px;
  border-radius: 2px;
}

.venue-item-price {
  font-size: 14px;
  color: #f56c6c;
  font-weight: 500;
}

.venue-item-address {
  font-size: 13px;
  color: #909399;
}

.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-results {
  padding: 20px;
  text-align: center;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

.view-all-results {
  text-align: center;
  padding: 10px 0;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .search-box {
    flex-direction: column;
  }
  
  .search-filter {
    width: 100%;
  }
  
  .venue-item-image {
    width: 80px;
    height: 60px;
  }
}
</style> 