<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserBookingOrderById, getOrderFacilities, cancelBooking, payBooking, payBookingWithAlipay } from '@/api/booking'
import { createReview, getReviewByOrderId, uploadReviewImage } from '@/api/review'
import { useUserStore } from '@/stores/user'
import type { BookingOrder, BookingFacility } from '@/types/booking'
import type { Review, ReviewDTO } from '@/types/review'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const orderId = Number(route.params.id)

// 获取用户状态
const userStore = useUserStore()

// 计算属性：用户是否已登录且已获取用户信息
const isUserReady = computed(() => {
  return userStore.isLoggedIn && !!userStore.userInfo?.id
})

// 状态数据
const loading = ref(false)
const orderDetail = ref<BookingOrder | null>(null)
const facilities = ref<BookingFacility[]>([])
const loadError = ref<string | null>(null) // 加载错误信息

// 评价相关
const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const existingReview = ref<Review | null>(null)
const reviewForm = ref<ReviewDTO>({
  orderId: orderId,
  venueId: 0,
  content: '',
  environmentScore: 5,
  facilityScore: 5,
  serviceScore: 5,
  costPerformanceScore: 5,
  overallScore: 5,
  images: [],
  anonymous: false
})
const uploadImageLoading = ref(false)

// 订单状态枚举
const orderStatusMap: Record<number, { text: string; color: string }> = {
  0: { text: '已取消', color: 'danger' },
  1: { text: '待支付', color: 'warning' },
  2: { text: '已支付', color: 'success' },
  3: { text: '已完成', color: 'info' },
  4: { text: '已关闭', color: 'danger' },
}

// 状态码与文本的映射
const STATUS_TEXT_MAP: Record<number, string> = {
  0: '已取消',
  1: '待支付',
  2: '已支付',
  3: '已完成',
  4: '已关闭',
};

// 状态文本与代码的映射
const STATUS_CODE_MAP: Record<string, number> = {
  '已取消': 0,
  '待支付': 1,
  '已支付': 2,
  '已完成': 3,
  '已关闭': 4,
};

// 支付状态枚举
const paymentStatusMap: Record<number, { text: string; color: string }> = {
  0: { text: '未支付', color: 'warning' },
  1: { text: '已支付', color: 'success' },
  2: { text: '已退款', color: 'info' },
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  return time.substring(0, 5) // 只显示小时和分钟
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

// 获取订单详情
const getOrderDetail = async () => {
  loading.value = true
  loadError.value = null // 重置错误信息
  
  // 检查用户是否已登录
  if (!isUserReady.value) {
    // 如果用户未登录或未获取用户信息，尝试获取用户信息
    try {
      await userStore.fetchUserInfo()
      // 如果还是没有用户信息，则重定向到登录页
      if (!userStore.userInfo?.id) {
        ElMessage.warning('请先登录后再查看订单详情')
        router.push(`/login?redirect=/order/${orderId}`)
        loading.value = false
        return
      }
    } catch (error) {
      loadError.value = '获取用户信息失败，请重新登录'
      ElMessage.error(loadError.value)
      router.push(`/login?redirect=/order/${orderId}`)
      loading.value = false
      return
    }
  }
  
  try {
    if (isNaN(orderId) || orderId <= 0) {
      throw new Error('无效的订单ID')
    }
    
    const response = await getUserBookingOrderById(orderId)
    
    if (!response || !response.data) {
      throw new Error('未找到订单数据')
    }
    
    console.log('订单详情数据:', response.data);
    orderDetail.value = response.data
    
    // 初始化评价表单的场馆ID
    if (orderDetail.value.venueId) {
      reviewForm.value.venueId = orderDetail.value.venueId
    }
    
    // 首先尝试从订单对象本身获取设施数据
    if (orderDetail.value.facilities && Array.isArray(orderDetail.value.facilities)) {
      console.log('从订单对象中获取设施数据:', orderDetail.value.facilities);
      facilities.value = orderDetail.value.facilities;
    } else {
      // 如果订单对象中没有设施数据，尝试调用独立的设施接口
      try {
        console.log('尝试从独立接口获取设施数据');
        const facilitiesRes = await getOrderFacilities(orderId)
        if (facilitiesRes && facilitiesRes.data) {
          console.log('成功从独立接口获取设施数据');
          facilities.value = facilitiesRes.data || []
        } else {
          console.warn('设施接口返回空数据');
          facilities.value = []
        }
      } catch (facilityError: any) {
        console.error('获取订单设施失败:', facilityError)
        
        // 如果是404错误，说明接口不存在，不需要显示错误
        if (facilityError.response && facilityError.response.status === 404) {
          console.warn('设施接口不存在，使用空数组');
        } else {
          // 其他错误，可能需要提示用户
          console.error('设施接口错误:', facilityError.message);
        }
        
        // 设施获取失败不影响整体页面显示
        facilities.value = []
      }
    }
  } catch (error: any) {
    console.error('获取订单详情失败:', error)
    
    // 设置具体的错误信息
    if (error.response) {
      // 根据状态码设置错误信息
      if (error.response.status === 401) {
        loadError.value = '您的登录已过期，请重新登录';
        // 清除登录状态
        userStore.clearLoginState();
        setTimeout(() => {
          router.push(`/login?redirect=/order/${orderId}`);
        }, 1500);
        return;
      } else if (error.response.status === 403) {
        loadError.value = '您没有权限查看此订单详情';
      } else if (error.response.status === 404) {
        loadError.value = '订单不存在或已被删除';
      } else {
        loadError.value = `服务器错误(${error.response.status})，请稍后再试`;
      }
    } else {
      loadError.value = error.message || '获取订单详情失败，请稍后重试';
    }
    
    ElMessage.error(loadError.value)
    // 短暂延迟后返回列表页
    setTimeout(() => {
      router.push('/orders')
    }, 1500)
  } finally {
    loading.value = false
  }
}

// 检查订单是否已评价
const checkReviewStatus = async () => {
  try {
    const res = await getReviewByOrderId(orderId)
    if (res && res.data) {
      existingReview.value = res.data
      return true
    }
    return false
  } catch (error) {
    console.log('订单未评价', error)
    // 不显示错误提示，静默失败
    return false
  }
}

// 打开评价对话框
const openReviewDialog = async () => {
  const isReviewed = await checkReviewStatus()
  if (isReviewed) {
    ElMessage.info('您已经对该订单进行了评价')
    return
  }
  
  if (!orderDetail.value || !orderDetail.value.venueId) {
    ElMessage.warning('无法获取场馆信息，无法评价')
    return
  }
  
  reviewForm.value.venueId = orderDetail.value.venueId
  reviewDialogVisible.value = true
}

// 上传评价图片
const handleUploadImage = async (file: File) => {
  try {
    uploadImageLoading.value = true
    // 使用真实的上传图片接口
    const res = await uploadReviewImage(file)
    
    if (!res || !res.data || !res.data.fileUrl) {
      throw new Error('上传图片失败，未获取到图片地址')
    }
    
    // 获取返回的图片URL
    const imageUrl = res.data.fileUrl
    
    // 添加到图片数组
    if (!reviewForm.value.images) {
      reviewForm.value.images = []
    }
    reviewForm.value.images.push(imageUrl)
    
    ElMessage.success('图片上传成功')
    return imageUrl
  } catch (error) {
    console.error('上传图片失败', error)
    ElMessage.error('上传图片失败')
    return null
  } finally {
    uploadImageLoading.value = false
  }
}

// 删除评价图片
const handleRemoveImage = (index: number) => {
  if (reviewForm.value.images) {
    reviewForm.value.images.splice(index, 1)
  }
}

// 提交评价
const submitReview = async () => {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  
  // 验证所有评分项目
  if (reviewForm.value.environmentScore === undefined || reviewForm.value.environmentScore < 1 || 
      reviewForm.value.facilityScore === undefined || reviewForm.value.facilityScore < 1 || 
      reviewForm.value.serviceScore === undefined || reviewForm.value.serviceScore < 1 || 
      reviewForm.value.costPerformanceScore === undefined || reviewForm.value.costPerformanceScore < 1 || 
      reviewForm.value.overallScore === undefined || reviewForm.value.overallScore < 1) {
    ElMessage.warning('请完成所有评分项')
    return
  }
  
  try {
    reviewLoading.value = true
    console.log('提交评价数据:', JSON.stringify(reviewForm.value))
    await createReview(reviewForm.value)
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
    
    // 重新获取订单详情，更新评价状态
    await checkReviewStatus()
  } catch (error: any) {
    console.error('提交评价失败', error)
    ElMessage.error(error.message || '提交评价失败，请稍后重试')
  } finally {
    reviewLoading.value = false
  }
}

// 取消订单
const handleCancelOrder = async () => {
  if (!orderDetail.value) return
  
  try {
    await ElMessageBox.confirm(
      '确定要取消该预约吗？取消后可能无法恢复。',
      '取消预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    loading.value = true
    await cancelBooking(orderId)
    ElMessage.success('预约已取消')
    getOrderDetail() // 刷新数据
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('取消预约失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 支付订单
const handlePayOrder = async () => {
  if (!orderDetail.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要支付该预约吗？价格：¥${orderDetail.value.totalAmount}`,
      '支付预约',
      {
        confirmButtonText: '确定支付',
        cancelButtonText: '取消',
        type: 'info',
      }
    )
    
    loading.value = true
    
    // 调用支付宝支付接口
    const res = await payBookingWithAlipay(orderId)
    
    if (res && res.data) {
      // 创建临时表单元素
      const div = document.createElement('div')
      div.innerHTML = res.data
      document.body.appendChild(div)
      
      // 获取支付表单
      const form = div.getElementsByTagName('form')[0]
      
      if (form) {
        console.log('找到支付表单，准备提交')
        
        // 阻止表单默认action中可能的URL编码问题
        const formAction = form.getAttribute('action')
        if (formAction && formAction.includes('%22')) {
          console.log('检测到action存在URL编码问题，正在修复')
          // 修复action URL中的编码问题
          const fixedAction = decodeURIComponent(formAction)
                              .replace(/\\"/g, '')
                              .replace(/"/g, '')
          form.setAttribute('action', fixedAction)
        }
        
        // 确保表单提交到新窗口
        form.setAttribute('target', '_blank')
        
        // 直接提交表单
        form.submit()
        
        // 清理临时DOM元素
        document.body.removeChild(div)
        
        ElMessage.success('支付页面已打开，请在新窗口中完成支付')
      } else {
        throw new Error('支付表单加载失败，未找到表单元素')
      }
    } else {
      throw new Error('支付接口返回数据格式错误')
    }
  } catch (error) {
    console.error('支付失败', error)
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 返回订单列表
const goBack = () => {
  router.push('/orders')
}

// 判断订单是否可以取消
const canCancelOrder = (order: BookingOrder) => {
  if (!order) return false;
  
  // 使用修正后的状态（如果有）
  if (order._correctedStatus !== undefined) {
    return order._correctedStatus === 1 || order._correctedStatus === 2;
  }
  
  // 根据状态名称判断
  if (order.statusName) {
    return order.statusName === '待支付' || order.statusName === '已支付';
  }
  
  // 根据状态值判断
  return order.status === 1 || order.status === 2;
}

// 判断订单是否可以支付
const canPayOrder = (order: BookingOrder) => {
  if (!order) return false;
  
  // 使用修正后的状态（如果有）
  if (order._correctedStatus !== undefined) {
    return order._correctedStatus === 1;
  }
  
  // 根据状态名称判断
  if (order.statusName) {
    return order.statusName === '待支付';
  }
  
  // 根据状态值判断
  return order.status === 1 && (order.paymentStatus === 0 || order.paymentStatus === undefined);
}

// 判断订单是否可以评价
const canReviewOrder = (order: BookingOrder) => {
  if (!order) return false;
  
  // 已评价的订单不能再次评价
  if (existingReview.value) return false;
  
  // 判断订单是否已完成或已支付
  if (order.statusName) {
    return order.statusName === '已完成' || order.statusName === '已支付' || order.statusName === '已预约';
  }
  
  return order.status === 3 || order.status === 2; // 已完成或已支付状态
}

// 获取订单状态显示信息
const getOrderStatusInfo = (order: BookingOrder | null) => {
  if (!order) return { text: '未知状态', color: 'info' };
  
  // 特殊处理：已经显式处理状态不一致的情况
  // 根据后端返回的日志，status=0但statusName='已取消'
  if (order.statusName === '已取消' && order.status === 0) {
    // 使用statusName优先
    return { text: '已取消', color: 'danger' };
  }
  
  // 特殊处理：当后端返回"已预约"时，显示为"已支付"
  if (order.statusName === '已预约') {
    return { text: '已支付', color: 'success' };
  }
  
  // 首先尝试通过状态名称匹配
  if (order.statusName) {
    // 查找状态文本对应的状态码
    const statusCode = STATUS_CODE_MAP[order.statusName];
    if (statusCode !== undefined) {
      // 如果能找到对应的状态码，返回对应的配置
      return orderStatusMap[statusCode] || { text: order.statusName, color: 'info' };
    }
    
    // 否则直接返回状态名称
    return { text: order.statusName, color: 'info' };
  }
  
  // 如果没有状态名称，使用状态码
  return orderStatusMap[order.status] || { text: '未知状态', color: 'info' };
}

// 计算设施数量
const totalFacilityCount = computed(() => {
  return facilities.value.reduce((sum, facility) => sum + facility.quantity, 0)
})

// 计算预约时长（小时）
const bookingDuration = computed(() => {
  if (!orderDetail.value || !orderDetail.value.startTime || !orderDetail.value.endTime) {
    return 0
  }
  
  const startHour = parseInt(orderDetail.value.startTime.split(':')[0])
  const endHour = parseInt(orderDetail.value.endTime.split(':')[0])
  return endHour - startHour
})

onMounted(() => {
  if (isNaN(orderId)) {
    ElMessage.error('订单ID无效')
    router.push('/orders')
    return
  }
  
  getOrderDetail()
  // 检查订单是否已评价
  checkReviewStatus()
})
</script>

<template>
  <div class="order-detail-container" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-button" @click="goBack">
        <i class="fas fa-chevron-left"></i>
      </div>
      <div class="page-title">订单详情</div>
      <div class="placeholder"></div>
    </div>

    <div v-if="orderDetail" class="order-detail-content">
      <!-- 订单状态卡片 -->
      <div class="status-card">
        <div class="status-icon">
          <i 
            class="fas" 
            :class="{
              'fa-check-circle': orderDetail.status === 2 || orderDetail.statusName === '已支付' || orderDetail.status === 3 || orderDetail.statusName === '已完成',
              'fa-times-circle': orderDetail.status === 0 || orderDetail.statusName === '已取消' || orderDetail.status === 4 || orderDetail.statusName === '已关闭',
              'fa-clock': orderDetail.status === 1 || orderDetail.statusName === '待支付'
            }"
          ></i>
        </div>
        <div class="status-info">
          <div class="status-text">
            {{ getOrderStatusInfo(orderDetail).text }}
          </div>
          <div class="status-desc">
            {{ 
              getOrderStatusInfo(orderDetail).text === '待支付' ? '请尽快完成支付' :
              getOrderStatusInfo(orderDetail).text === '已支付' ? '预约已确认，可前往场馆使用' :
              getOrderStatusInfo(orderDetail).text === '已取消' ? '预约已取消' :
              getOrderStatusInfo(orderDetail).text === '已完成' ? '预约已完成，感谢使用' :
              '预约已关闭'
            }}
          </div>
        </div>
      </div>

      <!-- 场馆信息卡片 -->
      <div class="info-card">
        <div class="card-title">
          <i class="fas fa-map-marker-alt"></i>
          场馆信息
        </div>
        <div class="venue-info">
          <div class="venue-name">{{ orderDetail.venueName }}</div>
          <div class="venue-address" v-if="orderDetail.venueAddress">{{ orderDetail.venueAddress }}</div>
          <div class="venue-location" v-if="orderDetail.locationName">
            位置: {{ orderDetail.locationName }}
          </div>
        </div>
      </div>

      <!-- 预约信息卡片 -->
      <div class="info-card">
        <div class="card-title">
          <i class="fas fa-calendar-alt"></i>
          预约信息
        </div>
        <div class="booking-info">
          <div class="info-item">
            <div class="item-label">预约日期</div>
            <div class="item-value">{{ formatDate(orderDetail.bookingDate) }}</div>
          </div>
          <div class="info-item">
            <div class="item-label">预约时间</div>
            <div class="item-value">{{ formatTime(orderDetail.startTime) }} - {{ formatTime(orderDetail.endTime) }}</div>
          </div>
          <div class="info-item">
            <div class="item-label">预约时长</div>
            <div class="item-value">{{ bookingDuration }}小时</div>
          </div>
          <div class="info-item">
            <div class="item-label">预约类型</div>
            <div class="item-value">{{ orderDetail.bookingTypeName || (orderDetail.bookingType === 1 ? '场馆预约' : '设施预约') }}</div>
          </div>
        </div>
      </div>

      <!-- 设施详情卡片 -->
      <div class="info-card" v-if="facilities.length > 0 || (orderDetail.facilities && orderDetail.facilities.length > 0)">
        <div class="card-title">
          <i class="fas fa-list"></i>
          设施详情
        </div>
        <div class="facility-list">
          <template v-if="facilities.length > 0">
            <!-- 显示从专用接口获取的设施 -->
            <div v-for="facility in facilities" :key="facility.id" class="facility-item">
              <div class="facility-name">{{ facility.facilityType || facility.facilityName || '未知设施' }}</div>
              <div class="facility-detail">
                <span class="facility-quantity">x{{ facility.quantity }}</span>
                <span class="facility-price" v-if="facility.unitPrice">¥{{ facility.unitPrice }}/小时</span>
              </div>
            </div>
          </template>
          
          <template v-else-if="orderDetail.facilities && orderDetail.facilities.length > 0">
            <!-- 显示从订单数据中获取的设施 -->
            <div v-for="facility in orderDetail.facilities" :key="facility.id" class="facility-item">
              <div class="facility-name">{{ facility.facilityType || facility.facilityName || '未知设施' }}</div>
              <div class="facility-detail">
                <span class="facility-quantity">x{{ facility.quantity }}</span>
                <span class="facility-price" v-if="facility.unitPrice">¥{{ facility.unitPrice }}/小时</span>
              </div>
            </div>
          </template>
          
          <div v-if="facilities.length === 0 && (!orderDetail.facilities || orderDetail.facilities.length === 0)" class="no-facilities">
            <i class="fas fa-info-circle"></i>
            <p>暂无设施信息</p>
          </div>
        </div>
      </div>

      <!-- 订单信息卡片 -->
      <div class="info-card">
        <div class="card-title">
          <i class="fas fa-file-invoice"></i>
          订单信息
        </div>
        <div class="order-info">
          <div class="info-item">
            <div class="item-label">订单编号</div>
            <div class="item-value">{{ orderDetail.orderNo || orderDetail.bookingCode }}</div>
          </div>
          <div class="info-item">
            <div class="item-label">下单时间</div>
            <div class="item-value">{{ orderDetail.createdAt }}</div>
          </div>
          <div class="info-item" v-if="orderDetail.remarks">
            <div class="item-label">备注</div>
            <div class="item-value">{{ orderDetail.remarks }}</div>
          </div>
          <div class="info-item total-amount">
            <div class="item-label">订单金额</div>
            <div class="item-value">¥{{ orderDetail.totalAmount }}</div>
          </div>
        </div>
      </div>
      
      <!-- 评价信息卡片 -->
      <div v-if="existingReview" class="existing-review">
        <div class="review-title">我的评价</div>
        <div class="review-content">
          <div class="review-header">
            <div class="review-score">
              <el-rate v-model="existingReview.overallScore" disabled />
              <span>{{ existingReview.overallScore }}分</span>
            </div>
            <el-tag 
              :type="existingReview.status === 0 ? 'success' : 'danger'" 
              size="small"
            >
              {{ existingReview.status === 0 ? '正常' : '已封禁' }}
            </el-tag>
          </div>
          <div class="review-scores">
            <div class="review-score-item">
              <div class="score-label">环境</div>
              <div class="score-value">
                <el-rate v-model="existingReview.environmentScore" disabled />
              </div>
            </div>
            <div class="review-score-item">
              <div class="score-label">设施</div>
              <div class="score-value">
                <el-rate v-model="existingReview.facilityScore" disabled />
              </div>
            </div>
            <div class="review-score-item">
              <div class="score-label">服务</div>
              <div class="score-value">
                <el-rate v-model="existingReview.serviceScore" disabled />
              </div>
            </div>
            <div class="review-score-item">
              <div class="score-label">性价比</div>
              <div class="score-value">
                <el-rate v-model="existingReview.costPerformanceScore" disabled />
              </div>
            </div>
          </div>
          <div class="review-text">{{ existingReview.content }}</div>
          <div class="review-images" v-if="existingReview.images && existingReview.images.length > 0">
            <el-image 
              v-for="(img, index) in existingReview.images" 
              :key="index"
              :src="img" 
              :preview-src-list="existingReview.images"
              fit="cover"
              class="review-image"
            />
          </div>
          <div class="review-meta">
            <span>评价时间: {{ formatDate(existingReview.createdTime) }}</span>
            <span v-if="existingReview.anonymous" class="anonymous-tag">匿名评价</span>
          </div>
          <!-- 商家回复 -->
          <div class="review-replies" v-if="existingReview.replies && existingReview.replies.length > 0">
            <div v-for="reply in existingReview.replies" :key="reply.id" class="reply-item">
              <div class="reply-header">
                <span class="reply-icon" :class="{ 'admin-reply': reply.isAdmin === 1 }">
                  {{ reply.isAdmin === 1 ? '官方回复' : '商家回复' }}
                </span>
                <span class="reply-time">{{ formatDate(reply.replyTime) }}</span>
              </div>
              <div class="reply-content">{{ reply.content }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button 
          v-if="canCancelOrder(orderDetail)" 
          type="danger" 
          plain
          @click="handleCancelOrder"
        >
          取消预约
        </el-button>
        <el-button 
          v-if="canPayOrder(orderDetail)" 
          type="primary"
          @click="handlePayOrder"
        >
          去支付
        </el-button>
        <el-button v-if="canPayOrder(orderDetail)" 
          type="success"
          plain
          @click="router.push(`/payment/callback?orderNo=${orderDetail.orderNo}`)"
        >
          模拟支付回调
        </el-button>
        <el-button 
          v-if="canReviewOrder(orderDetail)" 
          type="warning"
          @click="openReviewDialog"
        >
          评价订单
        </el-button>
        <el-button @click="goBack">
          返回列表
        </el-button>
      </div>
    </div>

    <div v-else-if="loadError" class="error-tip">
      <i class="fas fa-exclamation-circle"></i>
      <p>{{ loadError }}</p>
      <el-button @click="goBack">返回列表</el-button>
    </div>

    <div v-else-if="!loading" class="empty-tip">
      <i class="fas fa-exclamation-circle"></i>
      <p>订单不存在或已被删除</p>
      <el-button @click="goBack">返回列表</el-button>
    </div>

    <!-- 评价对话框 -->
    <el-dialog
      title="订单评价"
      v-model="reviewDialogVisible"
      width="90%"
      :close-on-click-modal="false"
    >
      <div v-loading="reviewLoading">
        <div class="review-form">
          <div class="form-item">
            <div class="form-label">环境评分</div>
            <div class="form-input">
              <el-rate v-model="reviewForm.environmentScore" :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
            </div>
          </div>
          
          <div class="form-item">
            <div class="form-label">设施评分</div>
            <div class="form-input">
              <el-rate v-model="reviewForm.facilityScore" :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
            </div>
          </div>
          
          <div class="form-item">
            <div class="form-label">服务评分</div>
            <div class="form-input">
              <el-rate v-model="reviewForm.serviceScore" :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
            </div>
          </div>
          
          <div class="form-item">
            <div class="form-label">性价比</div>
            <div class="form-input">
              <el-rate v-model="reviewForm.costPerformanceScore" :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
            </div>
          </div>
          
          <div class="form-item">
            <div class="form-label">综合评分</div>
            <div class="form-input">
              <el-rate v-model="reviewForm.overallScore" :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
            </div>
          </div>
          
          <div class="form-item">
            <div class="form-label">评价内容</div>
            <div class="form-input">
              <el-input
                v-model="reviewForm.content"
                type="textarea"
                rows="4"
                placeholder="请输入您对此次预约体验的评价..."
              />
            </div>
          </div>
          
          <div class="form-item">
            <div class="form-label">上传图片</div>
            <div class="form-input">
              <div class="image-uploader">
                <div class="image-list" v-if="reviewForm.images && reviewForm.images.length > 0">
                  <div v-for="(img, index) in reviewForm.images" :key="index" class="image-item">
                    <el-image :src="img" fit="cover" class="uploaded-image" />
                    <div class="image-delete" @click="handleRemoveImage(index)">
                      <i class="fas fa-times"></i>
                    </div>
                  </div>
                </div>
                
                <el-upload
                  action="#"
                  list-type="picture-card"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="file => handleUploadImage(file.raw)"
                  :disabled="uploadImageLoading || (reviewForm.images && reviewForm.images.length >= 9)"
                >
                  <template #default>
                    <i class="fas fa-plus"></i>
                  </template>
                  <template #file="{ file }">
                    <img class="el-upload-list__item-thumbnail" :src="file.url" alt="上传图片预览" />
                  </template>
                </el-upload>
                
                <div class="upload-tip">最多上传9张图片</div>
              </div>
            </div>
          </div>
          
          <div class="form-item">
            <div class="form-label"></div>
            <div class="form-input">
              <el-checkbox v-model="reviewForm.anonymous">匿名评价</el-checkbox>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="reviewLoading">提交评价</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- iOS底部标签栏 -->
    <div class="ios-tab-bar">
      <a class="tab-item" @click="router.push('/')">
        <i class="fas fa-home"></i>
        <span>首页</span>
      </a>
      <a class="tab-item active" @click="router.push('/orders')">
        <i class="fas fa-calendar-alt"></i>
        <span>订单</span>
      </a>
      <a class="tab-item" @click="router.push('/profile')">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.order-detail-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 70px;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-button {
  font-size: 18px;
  width: 30px;
  cursor: pointer;
}

.page-title {
  flex-grow: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}

.placeholder {
  width: 30px;
}

.order-detail-content {
  padding: 15px;
}

.status-card {
  background-color: #409eff;
  color: #ffffff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.status-icon {
  font-size: 30px;
  margin-right: 15px;
}

.status-info {
  flex: 1;
}

.status-text {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 5px;
}

.status-desc {
  font-size: 14px;
  opacity: 0.9;
}

.info-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.card-title i {
  margin-right: 8px;
  color: #409eff;
}

.venue-info {
  padding: 0 5px;
}

.venue-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.venue-address, .venue-location {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 5px;
  border-bottom: 1px solid #f2f6fc;
}

.info-item:last-child {
  border-bottom: none;
}

.item-label {
  color: #909399;
  font-size: 14px;
}

.item-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.total-amount .item-value {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 600;
}

.facility-list {
  padding: 0 5px;
}

.facility-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f2f6fc;
}

.facility-item:last-child {
  border-bottom: none;
}

.facility-name {
  font-size: 14px;
  color: #303133;
}

.facility-detail {
  display: flex;
  align-items: center;
}

.facility-quantity {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

.facility-price {
  font-size: 14px;
  color: #f56c6c;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.error-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #f56c6c;
}

.error-tip i {
  font-size: 40px;
  margin-bottom: 10px;
}

.error-tip p {
  margin-bottom: 20px;
  text-align: center;
  max-width: 80%;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
}

.empty-tip i {
  font-size: 40px;
  margin-bottom: 10px;
}

.empty-tip p {
  margin-bottom: 20px;
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

/* 评价相关样式 */
.my-review {
  padding: 15px 0;
}

.review-scores {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.review-score-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.score-value {
  display: flex;
  align-items: center;
}

.score-value .el-rate {
  margin-right: 5px;
}

.score-text {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.review-text {
  line-height: 1.6;
  margin-bottom: 15px;
}

.review-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.anonymous-tag {
  background-color: #909399;
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.review-replies {
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 15px;
  margin-top: 15px;
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
}

/* 评价表单样式 */
.review-form {
  padding: 10px;
}

.form-item {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

.form-label {
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.form-input {
  width: 100%;
}

/* 定义网格布局，让评分项在移动端更美观 */
@media screen and (max-width: 767px) {
  .review-scores {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .review-score-item {
    text-align: center;
  }
  
  .score-value .el-rate {
    display: flex;
    justify-content: center;
  }
}

/* 桌面端保持水平排列 */
@media screen and (min-width: 768px) {
  .review-scores {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
  }
  
  .review-score-item {
    min-width: 120px;
  }
  
  .form-item {
    flex-direction: row;
  }
  
  .form-label {
    width: 80px;
    text-align: right;
    padding-right: 10px;
    line-height: 32px;
    margin-bottom: 0;
  }
  
  .form-input {
    flex: 1;
  }
  
  .image-item {
    width: 100px;
    height: 100px;
  }
}

.image-uploader {
  margin-top: 10px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.image-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-delete {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 20px;
  height: 20px;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  cursor: pointer;
}

.upload-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}
</style> 