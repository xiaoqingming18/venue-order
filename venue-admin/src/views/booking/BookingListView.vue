<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search as IconSearch, 
  Delete as IconDelete, 
  View as IconView,
  Download as IconDownload,
  Edit as IconEdit,
  Refresh as IconRefresh,
  MoreFilled as IconMoreFilled,
  Check as IconCheck,
  Close as IconClose,
  Wallet as IconWallet,
  TurnOff as IconTurnOff
} from '@element-plus/icons-vue'
import { 
  getBookingOrders, 
  getBookingStats, 
  cancelBookingOrder, 
  completeBookingOrder,
  refundBookingOrder,
  exportBookingOrders
} from '@/api/booking'
import { pageVenues } from '@/api/venue'
import type { BookingQueryParams, BookingOrder, BookingStats } from '@/types/booking'
import { BookingStatus, PaymentStatus, getBookingStatusText, getPaymentStatusText, getBookingStatusType } from '@/types/booking'
import type { Venue } from '@/types/venue'

const router = useRouter()

// 数据加载状态
const loading = ref(false)
const exportLoading = ref(false)

// 统计数据
const statsData = ref<BookingStats>({
  totalCount: 0,
  todayCount: 0,
  pendingCount: 0,
  paidCount: 0,
  cancelledCount: 0,
  completedCount: 0,
  refundedCount: 0,
  totalAmount: 0,
  todayAmount: 0
})

// 场馆列表
const venueOptions = ref<Venue[]>([])

// 列表数据
const bookingList = ref<BookingOrder[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<BookingQueryParams>({
  page: 1,
  size: 10,
  status: undefined,
  venueId: undefined,
  bookingDate: undefined,
  startCreatedAt: undefined,
  endCreatedAt: undefined,
  keyword: undefined
})

// 状态选项
const statusOptions = [
  { label: '全部状态', value: undefined },
  { label: '待支付', value: BookingStatus.PENDING },
  { label: '已支付', value: BookingStatus.PAID },
  { label: '已完成', value: BookingStatus.COMPLETED },
  { label: '已取消', value: BookingStatus.CANCELLED },
  { label: '已退款', value: BookingStatus.REFUNDED }
]

// 日期范围
const dateRange = ref<[Date, Date] | null>(null)

// 加载预约列表数据
const loadBookingList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startCreatedAt = dateRange.value[0].toISOString().split('T')[0]
      queryParams.endCreatedAt = dateRange.value[1].toISOString().split('T')[0]
    } else {
      queryParams.startCreatedAt = undefined
      queryParams.endCreatedAt = undefined
    }

    const res = await getBookingOrders(queryParams)
    bookingList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getBookingStats()
    statsData.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载场馆列表
const loadVenues = async () => {
  try {
    const res = await pageVenues({ page: 1, size: 100 })
    venueOptions.value = res.data.records
  } catch (error) {
    console.error('加载场馆列表失败:', error)
  }
}

// 导出预约数据
const handleExport = async () => {
  exportLoading.value = true
  try {
    const res = await exportBookingOrders(queryParams)
    const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `预约订单列表_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 重置查询条件
const handleReset = () => {
  Object.assign(queryParams, {
    page: 1,
    size: 10,
    status: undefined,
    venueId: undefined,
    bookingDate: undefined,
    startCreatedAt: undefined,
    endCreatedAt: undefined,
    keyword: undefined
  })
  dateRange.value = null
  handleSearch()
}

// 查询
const handleSearch = () => {
  queryParams.page = 1
  loadBookingList()
}

// 分页改变
const handlePageChange = (page: number) => {
  queryParams.page = page
  loadBookingList()
}

// 每页数量改变
const handleSizeChange = (size: number) => {
  queryParams.size = size
  queryParams.page = 1
  loadBookingList()
}

// 查看订单详情
const handleViewDetail = (row: BookingOrder) => {
  router.push(`/home/bookings/${row.id}`)
}

// 取消订单
const handleCancel = async (row: BookingOrder) => {
  try {
    await ElMessageBox.confirm('确定要取消该预约订单吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelBookingOrder(row.id)
    ElMessage.success('取消成功')
    loadBookingList()
    loadStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('取消预约失败')
    }
  }
}

// 完成订单
const handleComplete = async (row: BookingOrder) => {
  try {
    await ElMessageBox.confirm('确定要将该预约标记为已完成吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await completeBookingOrder(row.id)
    ElMessage.success('操作成功')
    loadBookingList()
    loadStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 退款订单
const handleRefund = async (row: BookingOrder) => {
  try {
    await ElMessageBox.confirm('确定要为该预约办理退款吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await refundBookingOrder(row.id)
    ElMessage.success('退款成功')
    loadBookingList()
    loadStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退款失败:', error)
      ElMessage.error('退款失败')
    }
  }
}

// 格式化金额
const formatAmount = (amount: number) => {
  return amount.toFixed(2)
}

// 初始化
onMounted(() => {
  loadBookingList()
  loadStats()
  loadVenues()
})
</script>

<template>
  <div class="booking-list-container">
    <!-- 统计卡片 -->
    <div class="stats-panel">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-card-content">
              <div class="stats-card-value">
                <span class="value">{{ statsData.totalCount }}</span>
                <span class="unit">笔</span>
              </div>
              <div class="stats-card-title">预约总数</div>
              <div class="stats-card-footer">
                <div class="stats-card-extra">今日: {{ statsData.todayCount }} 笔</div>
              </div>
            </div>
            <div class="stats-card-icon total-icon">
              <el-icon><IconMoreFilled /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-card-content">
              <div class="stats-card-value">
                <span class="value">¥{{ formatAmount(statsData.totalAmount) }}</span>
              </div>
              <div class="stats-card-title">预约总金额</div>
              <div class="stats-card-footer">
                <div class="stats-card-extra">今日: ¥{{ formatAmount(statsData.todayAmount) }}</div>
              </div>
            </div>
            <div class="stats-card-icon amount-icon">
              <el-icon><IconWallet /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-card-content">
              <div class="stats-card-value">
                <span class="value">{{ statsData.pendingCount }}</span>
                <span class="unit">笔</span>
              </div>
              <div class="stats-card-title">待支付</div>
              <div class="stats-card-footer">
                <div class="stats-card-extra">已支付: {{ statsData.paidCount }} 笔</div>
              </div>
            </div>
            <div class="stats-card-icon pending-icon">
              <el-icon><IconTurnOff /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stats-card">
            <div class="stats-card-content">
              <div class="stats-card-value">
                <span class="value">{{ statsData.completedCount }}</span>
                <span class="unit">笔</span>
              </div>
              <div class="stats-card-title">已完成</div>
              <div class="stats-card-footer">
                <div class="stats-card-extra">已取消: {{ statsData.cancelledCount }} 笔</div>
              </div>
            </div>
            <div class="stats-card-icon completed-icon">
              <el-icon><IconCheck /></el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索和工具栏 -->
    <el-card class="search-card" shadow="never">
      <div class="toolbar">
        <div class="search-form">
          <el-form :model="queryParams" label-width="80px" inline>
            <el-form-item label="场馆">
              <el-select v-model="queryParams.venueId" placeholder="选择场馆" clearable style="width: 200px">
                <el-option
                  v-for="venue in venueOptions"
                  :key="venue.id"
                  :label="venue.name"
                  :value="venue.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="订单状态">
              <el-select v-model="queryParams.status" placeholder="选择状态" clearable style="width: 150px">
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="预约日期">
              <el-date-picker
                v-model="queryParams.bookingDate"
                type="date"
                placeholder="选择日期"
                style="width: 150px"
                value-format="YYYY-MM-DD"
                clearable
              />
            </el-form-item>
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 280px"
                clearable
              />
            </el-form-item>
            <el-form-item label="关键词">
              <el-input
                v-model="queryParams.keyword"
                placeholder="订单号/用户名/电话"
                style="width: 200px"
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch" :icon="IconSearch">
                查询
              </el-button>
              <el-button @click="handleReset" :icon="IconRefresh">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="tool-buttons">
          <el-button type="success" :icon="IconDownload" @click="handleExport" :loading="exportLoading">
            导出
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="bookingList"
        style="width: 100%"
        border
        stripe
        highlight-current-row
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="venueName" label="场馆名称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ venueOptions.find(v => v.id === row.venueId)?.name || row.venueId }}
          </template>
        </el-table-column>
        <el-table-column prop="bookingDate" label="预约日期" width="120" />
        <el-table-column label="预约时间" width="150">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ formatAmount(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getBookingStatusType(row.status)" effect="light">
              {{ getBookingStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)" :icon="IconView">
              详情
            </el-button>
            
            <el-dropdown trigger="click">
              <el-button type="primary" link>
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <!-- 待支付或已支付状态可以取消 -->
                  <el-dropdown-item v-if="row.status === BookingStatus.PENDING || row.status === BookingStatus.PAID" @click="handleCancel(row)">
                    <el-icon><IconClose /></el-icon>取消预约
                  </el-dropdown-item>
                  
                  <!-- 已支付状态可以完成 -->
                  <el-dropdown-item v-if="row.status === BookingStatus.PAID" @click="handleComplete(row)">
                    <el-icon><IconCheck /></el-icon>标记完成
                  </el-dropdown-item>
                  
                  <!-- 已支付状态可以退款 -->
                  <el-dropdown-item v-if="row.status === BookingStatus.PAID" @click="handleRefund(row)">
                    <el-icon><IconWallet /></el-icon>办理退款
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.booking-list-container {
  padding: 0;
}

.stats-panel {
  margin-bottom: 20px;
}

.stats-card {
  position: relative;
  overflow: hidden;
  height: 120px;
}

.stats-card-content {
  position: relative;
  z-index: 1;
}

.stats-card-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #303133;
}

.stats-card-value .unit {
  font-size: 16px;
  margin-left: 5px;
  font-weight: normal;
}

.stats-card-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 15px;
}

.stats-card-footer {
  font-size: 13px;
  color: #909399;
}

.stats-card-icon {
  position: absolute;
  right: 20px;
  top: 20px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0.8;
}

.stats-card-icon .el-icon {
  font-size: 30px;
}

.total-icon {
  background-color: #409eff;
}

.amount-icon {
  background-color: #67c23a;
}

.pending-icon {
  background-color: #e6a23c;
}

.completed-icon {
  background-color: #8e44ad;
}

.search-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.search-form {
  flex: 1;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 