<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft as IconArrowLeft,
  Wallet as IconWallet,
  Check as IconCheck,
  Close as IconClose,
  Refresh as IconRefresh,
  User as IconUser,
  Location as IconLocation,
  Calendar as IconCalendar,
  Timer as IconTimer,
  Document as IconDocument,
  Money as IconMoney
} from '@element-plus/icons-vue'
import { 
  getBookingOrderById, 
  cancelBookingOrder, 
  completeBookingOrder, 
  refundBookingOrder 
} from '@/api/booking'
import { getVenueById } from '@/api/venue'
import type { BookingOrderDetail, BookingFacility } from '@/types/booking'
import { BookingStatus, PaymentStatus, getBookingStatusText, getPaymentStatusText, getBookingStatusType } from '@/types/booking'
import type { Venue } from '@/types/venue'

const route = useRoute()
const router = useRouter()
const orderId = ref<number>(Number(route.params.id || 0))

// 加载状态
const loading = ref(false)

// 订单详情
const orderDetail = ref<BookingOrderDetail | null>(null)

// 场馆信息
const venueInfo = ref<Venue | null>(null)

// 加载订单详情
const loadOrderDetail = async () => {
  loading.value = true
  try {
    const res = await getBookingOrderById(orderId.value)
    orderDetail.value = res.data

    // 加载场馆详情
    if (orderDetail.value?.venueId) {
      const venueRes = await getVenueById(orderDetail.value.venueId)
      venueInfo.value = venueRes.data
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 订单操作函数
// 取消订单
const handleCancel = async () => {
  if (!orderDetail.value) return

  try {
    await ElMessageBox.confirm('确定要取消该预约订单吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelBookingOrder(orderId.value)
    ElMessage.success('取消成功')
    loadOrderDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('取消预约失败')
    }
  }
}

// 完成订单
const handleComplete = async () => {
  if (!orderDetail.value) return

  try {
    await ElMessageBox.confirm('确定要将该预约标记为已完成吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await completeBookingOrder(orderId.value)
    ElMessage.success('操作成功')
    loadOrderDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 退款订单
const handleRefund = async () => {
  if (!orderDetail.value) return

  try {
    await ElMessageBox.confirm('确定要为该预约办理退款吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await refundBookingOrder(orderId.value)
    ElMessage.success('退款成功')
    loadOrderDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退款失败:', error)
      ElMessage.error('退款失败')
    }
  }
}

// 返回列表
const goBack = () => {
  router.push('/home/bookings')
}

// 格式化金额
const formatAmount = (amount: number) => {
  return amount.toFixed(2)
}

// 初始化
onMounted(() => {
  if (orderId.value) {
    loadOrderDetail()
  } else {
    ElMessage.error('订单ID不存在')
    router.push('/home/bookings')
  }
})
</script>

<template>
  <div class="booking-detail-container" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-page-header @back="goBack" title="返回预约列表" :content="`预约详情 #${orderId}`">
        <template #extra>
          <div class="header-extra">
            <el-button-group v-if="orderDetail">
              <!-- 待支付或已支付状态可以取消 -->
              <el-button 
                v-if="orderDetail.status === BookingStatus.PENDING || orderDetail.status === BookingStatus.PAID" 
                type="danger" 
                @click="handleCancel"
                :icon="IconClose"
              >
                取消预约
              </el-button>
              
              <!-- 已支付状态可以完成 -->
              <el-button 
                v-if="orderDetail.status === BookingStatus.PAID" 
                type="success" 
                @click="handleComplete"
                :icon="IconCheck"
              >
                标记完成
              </el-button>
              
              <!-- 已支付状态可以退款 -->
              <el-button 
                v-if="orderDetail.status === BookingStatus.PAID" 
                type="warning" 
                @click="handleRefund"
                :icon="IconWallet"
              >
                办理退款
              </el-button>
            </el-button-group>
            <el-button type="primary" :icon="IconRefresh" circle @click="loadOrderDetail"></el-button>
          </div>
        </template>
      </el-page-header>
    </div>

    <div v-if="orderDetail" class="detail-content">
      <!-- 订单状态卡片 -->
      <el-card class="status-card" shadow="hover">
        <div class="status-content">
          <div class="status-icon">
            <el-icon v-if="orderDetail.status === BookingStatus.CANCELLED" class="cancelled"><IconClose /></el-icon>
            <el-icon v-else-if="orderDetail.status === BookingStatus.PENDING" class="pending"><IconTimer /></el-icon>
            <el-icon v-else-if="orderDetail.status === BookingStatus.PAID" class="paid"><IconWallet /></el-icon>
            <el-icon v-else-if="orderDetail.status === BookingStatus.COMPLETED" class="completed"><IconCheck /></el-icon>
            <el-icon v-else-if="orderDetail.status === BookingStatus.REFUNDED" class="refunded"><IconMoney /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-title">订单状态</div>
            <div class="status-value">
              <el-tag size="large" :type="getBookingStatusType(orderDetail.status)">
                {{ getBookingStatusText(orderDetail.status) }}
              </el-tag>
            </div>
            <div class="status-desc">
              <template v-if="orderDetail.status === BookingStatus.PENDING">
                订单待支付，请提醒用户尽快支付
              </template>
              <template v-else-if="orderDetail.status === BookingStatus.PAID">
                用户已完成支付，预约确认成功
              </template>
              <template v-else-if="orderDetail.status === BookingStatus.COMPLETED">
                预约已完成
              </template>
              <template v-else-if="orderDetail.status === BookingStatus.CANCELLED">
                订单已取消
              </template>
              <template v-else-if="orderDetail.status === BookingStatus.REFUNDED">
                订单已退款
              </template>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 基本信息和设施信息 -->
      <el-row :gutter="20" class="info-row">
        <el-col :span="16">
          <!-- 基本信息 -->
          <el-card class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><IconDocument /></el-icon> 预约基本信息</span>
              </div>
            </template>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="订单编号">{{ orderDetail.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ orderDetail.createdAt }}</el-descriptions-item>
              <el-descriptions-item label="预约日期">
                <el-icon><IconCalendar /></el-icon> {{ orderDetail.bookingDate }}
              </el-descriptions-item>
              <el-descriptions-item label="预约时间">
                <el-icon><IconTimer /></el-icon> {{ orderDetail.startTime }} - {{ orderDetail.endTime }}
              </el-descriptions-item>
              <el-descriptions-item label="场馆名称" :span="2">
                <el-icon><IconLocation /></el-icon> {{ venueInfo?.name || orderDetail.venueName || '未知场馆' }}
              </el-descriptions-item>
              <el-descriptions-item label="场馆地址" :span="2">
                {{ venueInfo?.address || orderDetail.venueAddress || '未知地址' }}
              </el-descriptions-item>
              <el-descriptions-item label="订单金额">
                <span class="amount">¥{{ formatAmount(orderDetail.totalAmount) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="备注信息">
                {{ orderDetail.remark || '无' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <!-- 用户信息 -->
          <el-card class="user-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><IconUser /></el-icon> 用户信息</span>
              </div>
            </template>
            
            <div class="user-info">
              <el-avatar :size="64" icon="el-icon-user" />
              <div class="user-details">
                <div class="user-name">{{ orderDetail.username || '用户' + orderDetail.userId }}</div>
                <div class="user-phone">{{ orderDetail.userPhone || '未知电话' }}</div>
                <div class="user-id">用户ID: {{ orderDetail.userId }}</div>
              </div>
            </div>
          </el-card>
          
          <!-- 设施信息 -->
          <el-card class="facility-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><IconDocument /></el-icon> 预约设施</span>
              </div>
            </template>
            
            <div class="facility-list" v-if="orderDetail.facilities && orderDetail.facilities.length > 0">
              <el-table :data="orderDetail.facilities" style="width: 100%" border>
                <el-table-column prop="facilityType" label="设施类型" min-width="120" />
                <el-table-column prop="quantity" label="数量" width="80" />
                <el-table-column prop="unitPrice" label="单价" width="100">
                  <template #default="{ row }">
                    ¥{{ formatAmount(row.unitPrice) }}
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="小计" width="100">
                  <template #default="{ row }">
                    ¥{{ formatAmount(row.amount) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div class="empty-tip" v-else>
              <el-empty description="暂无设施信息" />
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 操作日志 -->
      <el-card class="log-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span><el-icon><IconDocument /></el-icon> 操作日志</span>
          </div>
        </template>
        
        <div class="log-content">
          <el-timeline>
            <el-timeline-item
              timestamp="创建预约"
              placement="top"
              :color="'#409EFF'"
            >
              <h4>用户创建预约</h4>
              <p>{{ orderDetail.createdAt }}</p>
            </el-timeline-item>
            
            <el-timeline-item
              v-if="orderDetail.status === BookingStatus.PENDING"
              timestamp="等待支付"
              placement="top"
              :color="'#E6A23C'"
            >
              <h4>订单待支付</h4>
              <p>订单创建后等待用户支付</p>
            </el-timeline-item>
            
            <el-timeline-item
              v-if="orderDetail.status >= BookingStatus.PAID"
              timestamp="完成支付"
              placement="top"
              :color="'#67C23A'"
            >
              <h4>用户完成支付</h4>
              <p>支付时间: {{ orderDetail.updatedAt }}</p>
            </el-timeline-item>
            
            <el-timeline-item
              v-if="orderDetail.status === BookingStatus.COMPLETED"
              timestamp="预约完成"
              placement="top"
              :color="'#67C23A'"
            >
              <h4>预约完成</h4>
              <p>完成时间: {{ orderDetail.updatedAt }}</p>
            </el-timeline-item>
            
            <el-timeline-item
              v-if="orderDetail.status === BookingStatus.CANCELLED"
              timestamp="预约取消"
              placement="top"
              :color="'#F56C6C'"
            >
              <h4>预约已取消</h4>
              <p>取消时间: {{ orderDetail.updatedAt }}</p>
            </el-timeline-item>
            
            <el-timeline-item
              v-if="orderDetail.status === BookingStatus.REFUNDED"
              timestamp="订单退款"
              placement="top"
              :color="'#909399'"
            >
              <h4>订单已退款</h4>
              <p>退款时间: {{ orderDetail.updatedAt }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>
    </div>
    
    <div v-else-if="!loading" class="empty-state">
      <el-empty description="未找到订单信息">
        <template #extra>
          <el-button type="primary" @click="goBack">返回列表</el-button>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<style scoped>
.booking-detail-container {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.header-extra {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-card {
  margin-bottom: 20px;
}

.status-content {
  display: flex;
  align-items: center;
}

.status-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.status-icon .el-icon {
  font-size: 30px;
}

.status-icon .cancelled {
  color: #F56C6C;
}

.status-icon .pending {
  color: #E6A23C;
}

.status-icon .paid {
  color: #67C23A;
}

.status-icon .completed {
  color: #67C23A;
}

.status-icon .refunded {
  color: #909399;
}

.status-info {
  flex-grow: 1;
}

.status-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.status-value {
  margin-bottom: 10px;
}

.status-desc {
  font-size: 14px;
  color: #909399;
}

.info-row {
  margin-bottom: 20px;
}

.info-card, .user-card, .facility-card {
  margin-bottom: 20px;
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header .el-icon {
  margin-right: 8px;
}

.amount {
  color: #F56C6C;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.user-details {
  margin-left: 20px;
}

.user-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.user-phone {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.user-id {
  font-size: 12px;
  color: #909399;
}

.facility-list {
  margin-top: 10px;
}

.empty-tip {
  padding: 20px;
  text-align: center;
}

.log-card {
  margin-bottom: 20px;
}

.log-content {
  padding: 10px;
}
</style> 