<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFavoritePage, removeFavoriteById } from '@/api/favorite'
import { getVenueById, getVenueFacilities, getVenueLocations } from '@/api/venue'
import type { UserFavoriteVenue } from '@/types/favorite'
import type { Venue, VenueFacility, VenueLocation } from '@/types/venue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 数据相关
const favorites = ref<UserFavoriteVenue[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 场馆详情弹窗相关
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const venueDetail = ref<Venue | null>(null)
const venueFacilities = ref<VenueFacility[]>([])
const venueLocations = ref<VenueLocation[]>([])

// 获取收藏场馆列表
const fetchFavorites = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  loading.value = true
  try {
    const res = await getFavoritePage(currentPage.value, pageSize.value)
    favorites.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取收藏场馆列表失败', error)
    ElMessage.error('获取收藏场馆列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 查看场馆详情
const viewVenueDetail = (venueId: number) => {
  router.push(`/venues/${venueId}`)
}

// 获取场馆详细信息并显示在弹窗中
const showVenueDetail = async (venueId: number) => {
  detailLoading.value = true
  try {
    // 获取场馆基本信息
    const venueRes = await getVenueById(venueId)
    venueDetail.value = venueRes.data
    
    // 获取场馆设施
    const facilitiesRes = await getVenueFacilities(venueId)
    venueFacilities.value = facilitiesRes.data || []
    
    // 获取场馆位置
    const locationsRes = await getVenueLocations(venueId)
    venueLocations.value = locationsRes.data || []
    
    // 显示弹窗
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取场馆详情失败', error)
    ElMessage.error('获取场馆详情失败，请稍后重试')
  } finally {
    detailLoading.value = false
  }
}

// 取消收藏
const handleRemoveFavorite = async (favoriteId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏该场馆吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    await removeFavoriteById(favoriteId)
    ElMessage.success('已取消收藏')
    
    // 重新加载列表
    fetchFavorites()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消收藏失败', error)
      ElMessage.error('取消收藏失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 页码变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchFavorites()
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 获取场馆状态文本
const getStatusText = (status: number): string => {
  return status === 1 ? '开放' : '关闭'
}

// 获取场馆状态类型
const getStatusType = (status: number): string => {
  return status === 1 ? 'success' : 'danger'
}

// 获取位置状态文本
const getLocationStatusText = (status: number): string => {
  return status === 1 ? '可用' : '不可用'
}

// 获取位置状态类型
const getLocationStatusType = (status: number): string => {
  return status === 1 ? 'success' : 'danger'
}

onMounted(() => {
  fetchFavorites()
})
</script>

<template>
  <div class="my-favorites-container" v-loading="loading">
    <div class="page-header">
      <el-button @click="goBack" icon="el-icon-arrow-left">返回</el-button>
      <h2>我的收藏</h2>
    </div>
    
    <div class="favorites-list">
      <el-empty v-if="favorites.length === 0" description="暂无收藏的场馆" />
      
      <div v-else class="favorite-items">
        <div v-for="item in favorites" :key="item.id" class="favorite-item">
          <div class="favorite-image" @click="viewVenueDetail(item.venueId)">
            <img :src="item.coverImage || `https://picsum.photos/200/150?random=${item.id}`" alt="场馆图片" />
          </div>
          <div class="favorite-info">
            <div class="favorite-title" @click="viewVenueDetail(item.venueId)">{{ item.venueName }}</div>
            <div class="favorite-meta">
              <span class="venue-type">{{ item.venueTypeName }}</span>
              <span class="venue-price">¥{{ item.basePrice }}/小时</span>
            </div>
            <div class="favorite-address">{{ item.address }}</div>
            <div class="favorite-notes" v-if="item.notes">备注: {{ item.notes }}</div>
            <div class="favorite-date">收藏于 {{ new Date(item.createdAt).toLocaleDateString() }}</div>
          </div>
          <div class="favorite-actions">
            <el-button type="danger" size="small" plain @click="handleRemoveFavorite(item.id)">
              取消收藏
            </el-button>
            <el-button type="primary" size="small" @click="showVenueDetail(item.venueId)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 场馆详情弹窗 -->
    <el-dialog
      title="场馆详情"
      v-model="detailDialogVisible"
      width="90%"
      :close-on-click-modal="true"
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <div v-if="venueDetail" class="venue-detail-dialog">
          <!-- 场馆基本信息 -->
          <div class="dialog-venue-header">
            <div class="dialog-venue-image">
              <img :src="venueDetail.coverImage || `https://picsum.photos/600/400?random=${venueDetail.id}`" alt="场馆图片" />
            </div>
            <div class="dialog-venue-info">
              <div class="dialog-venue-title">
                <h3>{{ venueDetail.name }}</h3>
                <el-tag :type="getStatusType(venueDetail.status)" size="small">
                  {{ getStatusText(venueDetail.status) }}
                </el-tag>
              </div>
              <div class="dialog-venue-meta">
                <div class="meta-item">
                  <i class="el-icon-location"></i>
                  <span>{{ venueDetail.address }}</span>
                </div>
                <div class="meta-item">
                  <i class="el-icon-time"></i>
                  <span>营业时间: {{ venueDetail.businessHours || '暂无信息' }}</span>
                </div>
                <div class="meta-item">
                  <i class="el-icon-user"></i>
                  <span>可容纳人数: {{ venueDetail.capacity }}人</span>
                </div>
                <div class="meta-item">
                  <i class="el-icon-phone"></i>
                  <span>联系电话: {{ venueDetail.contactPhone || '暂无信息' }}</span>
                </div>
                <div class="meta-item price">
                  <span class="price-label">基础价格:</span>
                  <span class="price-value">¥{{ venueDetail.basePrice }}/小时</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 场馆描述 -->
          <div class="dialog-venue-description">
            <div class="section-title">场馆介绍</div>
            <p>{{ venueDetail.description || '暂无详细介绍' }}</p>
          </div>

          <!-- 场馆设施 -->
          <div class="dialog-venue-facilities">
            <div class="section-title">场馆设施</div>
            <el-empty v-if="venueFacilities.length === 0" description="暂无设施信息"></el-empty>
            <el-table v-else :data="venueFacilities" style="width: 100%" size="small" stripe>
              <el-table-column prop="facilityType" label="设施类型" />
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column label="价格" width="120">
                <template #default="{ row }">
                  <span v-if="row.price" class="facility-price">¥{{ row.price }}/小时</span>
                  <span v-else class="facility-base-price">使用基准价</span>
                </template>
              </el-table-column>
              <el-table-column prop="positionDesc" label="位置描述" />
            </el-table>
          </div>

          <!-- 场馆位置 -->
          <div class="dialog-venue-locations">
            <div class="section-title">场馆位置</div>
            <el-empty v-if="venueLocations.length === 0" description="暂无位置信息"></el-empty>
            <el-table v-else :data="venueLocations" style="width: 100%" size="small" stripe>
              <el-table-column prop="name" label="位置名称" />
              <el-table-column prop="type" label="位置类型" width="100" />
              <el-table-column label="价格" width="100">
                <template #default="{ row }">
                  <span v-if="row.price" class="location-price">¥{{ row.price }}/小时</span>
                  <span v-else class="location-base-price">使用基准价</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="getLocationStatusType(row.status)" size="small">
                    {{ getLocationStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="位置描述" />
            </el-table>
          </div>
        </div>
        <el-empty v-else-if="!detailLoading" description="场馆信息不存在"></el-empty>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="viewVenueDetail(venueDetail?.id || 0)" :disabled="!venueDetail">
            前往详情页
          </el-button>
        </span>
      </template>
    </el-dialog>

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
.my-favorites-container {
  padding: 20px;
  padding-bottom: 70px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin-left: 15px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.favorites-list {
  margin-bottom: 20px;
}

.favorite-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.favorite-item {
  display: flex;
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 15px;
}

.favorite-image {
  width: 120px;
  height: 90px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 15px;
  cursor: pointer;
}

.favorite-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-info {
  flex: 1;
}

.favorite-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  cursor: pointer;
}

.favorite-title:hover {
  color: #409eff;
}

.favorite-meta {
  display: flex;
  margin-bottom: 5px;
}

.venue-type {
  background-color: #ecf5ff;
  color: #409eff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 10px;
}

.venue-price {
  color: #f56c6c;
  font-weight: 500;
  font-size: 14px;
}

.favorite-address {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.favorite-notes {
  font-size: 13px;
  color: #909399;
  margin-bottom: 5px;
  font-style: italic;
}

.favorite-date {
  font-size: 12px;
  color: #909399;
}

.favorite-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  margin-left: 15px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

/* 场馆详情弹窗样式 */
.venue-detail-dialog {
  padding: 0 10px;
}

.dialog-venue-header {
  display: flex;
  margin-bottom: 20px;
  gap: 20px;
}

.dialog-venue-image {
  flex: 0 0 40%;
  max-width: 40%;
  border-radius: 4px;
  overflow: hidden;
}

.dialog-venue-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dialog-venue-info {
  flex: 1;
}

.dialog-venue-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.dialog-venue-title h3 {
  margin: 0;
  font-size: 20px;
}

.dialog-venue-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.meta-item.price {
  margin-top: 5px;
}

.price-label {
  font-weight: 500;
}

.price-value {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: 1px solid #ebeef5;
  color: #303133;
}

.dialog-venue-description,
.dialog-venue-facilities,
.dialog-venue-locations {
  margin-bottom: 20px;
}

.dialog-venue-description p {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.facility-price,
.location-price {
  color: #f56c6c;
  font-weight: 500;
}

.facility-base-price,
.location-base-price {
  color: #909399;
  font-size: 12px;
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

/* 响应式样式 */
@media (max-width: 768px) {
  .favorite-item {
    flex-direction: column;
  }
  
  .favorite-image {
    width: 100%;
    height: 120px;
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .favorite-actions {
    flex-direction: row;
    margin-left: 0;
    margin-top: 15px;
  }
  
  .dialog-venue-header {
    flex-direction: column;
  }
  
  .dialog-venue-image {
    flex: 0 0 100%;
    max-width: 100%;
    height: 180px;
    margin-bottom: 15px;
  }
}
</style> 