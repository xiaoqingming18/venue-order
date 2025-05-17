<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserBookingOrders, cancelBooking, payBooking, payBookingWithAlipay } from '@/api/booking'
import { useUserStore } from '@/stores/user'
import type { BookingOrder } from '@/types/booking'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 计算属性：用户是否已登录且已获取用户信息
const isUserReady = computed(() => {
  return userStore.isLoggedIn && !!userStore.userInfo?.id
})

// 是否为开发环境
const isDev = computed(() => {
  return import.meta.env.DEV
})

// 状态数据
const loading = ref(false)
const orderList = ref<BookingOrder[]>([])
const allOrders = ref<BookingOrder[]>([]) // 存储所有订单数据
const loadError = ref<string | null>(null) // 加载错误信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})
const currentStatus = ref<number | null>(null) // null表示全部状态

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

// 获取订单列表
const getOrderList = async () => {
  loading.value = true
  loadError.value = null // 重置错误信息
  allOrders.value = [] // 清空之前的数据
  orderList.value = [] // 清空当前渲染的数据
  
  // 检查用户是否已登录
  if (!isUserReady.value) {
    // 如果用户未登录或未获取用户信息，尝试获取用户信息
    try {
      await userStore.fetchUserInfo()
      // 如果还是没有用户信息，则重定向到登录页
      if (!userStore.userInfo?.id) {
        ElMessage.warning('请先登录后再查看订单')
        router.push('/login?redirect=/orders')
        loading.value = false
        return
      }
    } catch (error) {
      loadError.value = '获取用户信息失败，请重新登录'
      ElMessage.error(loadError.value)
      router.push('/login?redirect=/orders')
      loading.value = false
      return
    }
  }
  
  try {
    // 获取订单数据
    const response = await getUserBookingOrders({})
    
    console.log('订单API响应:', response);
    
    // 直接在控制台输出完整响应JSON，以便排查
    console.log('完整响应JSON:', JSON.stringify(response, null, 2));
    
    // 检查响应数据格式
    if (response && response.data) {
      console.log('响应数据结构:', Object.keys(response.data));
      
      if (response.data.records) {
        console.log('records结构:', Object.keys(response.data.records));
        console.log('records类型:', Array.isArray(response.data.records) ? 'Array' : typeof response.data.records);
        console.log('records长度:', Array.isArray(response.data.records) ? response.data.records.length : 0);
      }
    }
    
    // 最简单直接的方式尝试获取records
    if (response && response.data && response.data.records && Array.isArray(response.data.records)) {
      console.log('成功找到records数组，长度:', response.data.records.length);
      allOrders.value = response.data.records.filter(record => record && record.id);
      pagination.total = response.data.total || allOrders.value.length;
    }
    // 如果没有在常规位置找到数据，尝试深层查找
    else if (response && typeof response === 'object') {
      console.log('在标准位置未找到records数组，尝试深层查找');
      
      // 递归函数，在对象中查找records数组
      const findRecordsArray = (obj: any): any[] | null => {
        if (!obj || typeof obj !== 'object') return null;
        
        // 直接检查是否有records属性
        if (obj.records && Array.isArray(obj.records)) {
          return obj.records;
        }
        
        // 遍历所有属性
        for (const key in obj) {
          if (obj[key] && typeof obj[key] === 'object') {
            // 如果属性是对象且有records属性
            if (obj[key].records && Array.isArray(obj[key].records)) {
              return obj[key].records;
            }
            
            // 递归检查
            const result = findRecordsArray(obj[key]);
            if (result) return result;
          }
        }
        
        return null;
      };
      
      const recordsArray = findRecordsArray(response);
      if (recordsArray && recordsArray.length > 0) {
        console.log('通过深层查找找到records数组，长度:', recordsArray.length);
        allOrders.value = recordsArray.filter(record => record && record.id);
        pagination.total = allOrders.value.length;
      } else {
        console.log('未找到任何records数组，设置为空数组');
        allOrders.value = [];
        pagination.total = 0;
      }
    }
    
    // 如果依然没有数据，尝试其他方式解析
    if (allOrders.value.length === 0) {
      console.log('通过records未找到数据，尝试其他解析方式');
      
      if (response && response.data && Array.isArray(response.data.content)) {
        // Spring分页标准格式
        allOrders.value = response.data.content.filter(record => record && record.id);
        pagination.total = response.data.totalElements || allOrders.value.length;
        console.log('从content解析到数据，长度:', allOrders.value.length);
      } else if (response && response.data && Array.isArray(response.data)) {
        // 数组格式
        allOrders.value = response.data.filter(record => record && record.id);
        pagination.total = allOrders.value.length;
        console.log('直接从data数组解析到数据，长度:', allOrders.value.length);
      } else if (response && response.data && typeof response.data === 'object' && response.data.id) {
        // 单个对象
        if (response.data.id) {
          allOrders.value = [response.data];
          pagination.total = 1;
          console.log('将单个对象转为数组');
        } else {
          console.log('单个对象没有id，设置为空数组');
          allOrders.value = [];
          pagination.total = 0;
        }
      } else {
        console.log('所有解析方式均失败，设置为空数组');
        allOrders.value = [];
        pagination.total = 0;
      }
    }
    
    console.log('最终解析结果 - 订单数量:', allOrders.value.length);
    
    // 检查数据结构
    if (allOrders.value.length > 0) {
      console.log('第一条订单数据结构:', Object.keys(allOrders.value[0]));
      console.log('第一条订单ID:', allOrders.value[0].id);
      
      // 检查是否有关键字段
      const sample = allOrders.value[0];
      const missingFields = [];
      ['id', 'orderNo', 'bookingCode', 'venueName', 'bookingDate', 'startTime', 'endTime'].forEach(field => {
        if (sample[field] === undefined) {
          missingFields.push(field);
        }
      });
      
      if (missingFields.length > 0) {
        console.warn('订单数据缺少关键字段:', missingFields.join(', '));
        
        // 对于缺少bookingCode但有orderNo的情况，做字段适配
        if (missingFields.includes('bookingCode') && sample.orderNo) {
          console.log('将orderNo作为bookingCode使用');
          // 为所有订单数据添加bookingCode字段，值为orderNo
          allOrders.value = allOrders.value.map(order => ({
            ...order,
            bookingCode: order.orderNo
          }));
        }
      }
      
      // 修正可能的状态不一致问题
      allOrders.value = allOrders.value.map(order => {
        const result = { ...order };
        
        // 特殊处理：如果状态名称和状态码不匹配
        if (order.statusName === '已取消' && order.status === 0) {
          console.log(`订单 ${order.id} 状态不一致: status=${order.status}, statusName=${order.statusName}`);
          
          // 不修改原始数据，但在计算状态时使用statusName优先
          result._correctedStatus = 2; // 将内部状态修正为"已取消"(2)
        }
        
        return result;
      });
    }
    
    // 最后再次确保所有订单都是有效的
    allOrders.value = allOrders.value.filter(order => order && order.id);
    console.log('最终有效订单数量:', allOrders.value.length);
    
    // 应用筛选和分页
    applyFiltersAndPagination();
    
    console.log('====== 完成订单获取和处理 ======');
    
    // 检查最终的渲染数据
    console.log('orderList数据长度:', orderList.value.length);
    if (orderList.value.length > 0) {
      console.log('第一条渲染数据:', orderList.value[0]);
    } else {
      console.warn('orderList为空，没有数据可渲染');
    }
  } catch (error: any) {
    console.error('获取订单列表失败:', error);
    
    // 设置具体的错误信息
    if (error.response) {
      console.error('错误状态码:', error.response.status);
      console.error('错误数据:', JSON.stringify(error.response.data, null, 2));
      
      // 根据状态码设置错误信息
      if (error.response.status === 401) {
        loadError.value = '您的登录已过期，请重新登录';
        // 尝试刷新登录状态
        userStore.clearLoginState();
        setTimeout(() => {
          router.push('/login?redirect=/orders');
        }, 2000);
      } else if (error.response.status === 403) {
        loadError.value = '您没有权限查看订单信息';
      } else if (error.response.status === 400) {
        loadError.value = '请求参数错误，请联系管理员';
      } else if (error.response.status === 404) {
        loadError.value = '订单接口不存在，请联系管理员';
      } else {
        loadError.value = `服务器错误(${error.response.status})，请稍后再试`;
      }
    } else if (error.request) {
      // 请求发出但没有收到响应
      loadError.value = '无法连接到服务器，请检查网络连接';
    } else {
      // 请求设置时发生错误
      loadError.value = error.message || '获取订单失败，请稍后重试';
    }
    
    ElMessage.error(loadError.value);
  } finally {
    loading.value = false;
  }
}

// 重试加载
const retryLoading = () => {
  ElMessage.info('正在重新加载数据...')
  getOrderList()
}

// 应用筛选和分页
const applyFiltersAndPagination = () => {
  console.log('开始应用筛选和分页');
  
  // 确保allOrders有值
  if (!allOrders.value) {
    console.log('allOrders为空，初始化为[]');
    allOrders.value = [];
  }
  
  console.log('原始订单数据量:', allOrders.value.length);
  
  // 先过滤掉无效订单
  const validOrders = allOrders.value.filter(order => order && order.id);
  console.log('有效订单数量:', validOrders.length);
  
  // 再应用状态筛选
  let filteredOrders = validOrders;
  if (currentStatus.value !== null) {
    console.log('应用状态筛选, 当前筛选状态:', currentStatus.value);
    
    const currentStatusText = STATUS_TEXT_MAP[currentStatus.value as keyof typeof STATUS_TEXT_MAP] || '';
    console.log('当前筛选状态文本:', currentStatusText);
    
    filteredOrders = validOrders.filter(order => {
      // 首先通过状态名称进行匹配
      if (order.statusName) {
        // 如果状态名称与当前筛选状态文本一致，返回true
        if (order.statusName === currentStatusText) {
          return true;
        }
        
        // 特殊处理：如果是"已取消"状态
        if (currentStatusText === '已取消' && order.statusName === '已取消') {
          return true;
        }
        
        // 特殊处理：筛选"已支付"时，也匹配后端的"已预约"状态
        if (currentStatusText === '已支付' && order.statusName === '已预约') {
          return true;
        }
      }
      
      // 如果没有通过状态名称匹配成功，再通过状态码匹配
      return order.status === currentStatus.value;
    });
    
    console.log('状态筛选后订单数量:', filteredOrders.length);
  }
  
  // 根据分页参数计算分页范围
  const start = (pagination.currentPage - 1) * pagination.pageSize;
  const end = start + pagination.pageSize;
  console.log('分页参数 - 当前页:', pagination.currentPage, '每页数量:', pagination.pageSize);
  console.log('分页范围 - 开始:', start, '结束:', end);
  
  // 检查分页范围是否合理
  if (start >= filteredOrders.length && filteredOrders.length > 0) {
    console.warn('分页起始位置超出数据范围，重置到第一页');
    pagination.currentPage = 1;
    const newStart = 0;
    const newEnd = pagination.pageSize;
    
    // 使用新的起始位置和结束位置截取数据
    // 确保只保留有效订单
    orderList.value = filteredOrders.slice(newStart, newEnd).filter(order => order && order.id);
    console.log('重置分页后的数据量:', orderList.value.length);
  } else {
    // 截取当前页的数据，并确保只保留有效订单
    orderList.value = filteredOrders.slice(start, end).filter(order => order && order.id);
    console.log('截取数据后的量:', orderList.value.length);
  }
  
  console.log('最终渲染数据量:', orderList.value.length);
  
  // 更新总数量
  pagination.total = filteredOrders.length;
  console.log('设置分页总数:', pagination.total);
  
  // 如果过滤后没有数据，但有原始数据
  if (orderList.value.length === 0 && allOrders.value.length > 0) {
    console.warn('过滤后无数据，重置筛选条件');
    // 考虑重置筛选条件
    if (currentStatus.value !== null) {
      currentStatus.value = null;
      // 重新应用筛选，但不再递归调用以避免死循环
      orderList.value = validOrders.slice(0, pagination.pageSize).filter(order => order && order.id);
      pagination.total = validOrders.length;
    }
  }
  
  // 打印第一条数据的详细信息，用于调试
  if (orderList.value.length > 0) {
    const firstOrder = orderList.value[0];
    console.log('第一条渲染数据详情:', {
      id: firstOrder.id,
      orderNo: firstOrder.orderNo,
      bookingCode: firstOrder.bookingCode,
      status: firstOrder.status,
      statusName: firstOrder.statusName
    });
  } else {
    console.warn('没有任何订单数据可以渲染！');
  }
}

// 处理分页改变
const handlePageChange = (page: number) => {
  pagination.currentPage = page
  applyFiltersAndPagination()
}

// 处理页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.currentPage = 1 // 重置到第一页
  applyFiltersAndPagination()
}

// 处理状态筛选
const handleStatusChange = (status: number | null) => {
  currentStatus.value = status
  pagination.currentPage = 1 // 重置到第一页
  applyFiltersAndPagination()
}

// 查看订单详情
const viewOrderDetail = (order: BookingOrder) => {
  if (!order || !order.id) {
    ElMessage.warning('订单数据异常，无法查看详情');
    return;
  }
  router.push(`/order/${order.id}`)
}

// 取消订单
const handleCancelOrder = async (order: BookingOrder) => {
  if (!order || !order.id) {
    ElMessage.warning('订单数据异常，无法取消');
    return;
  }
  
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
    await cancelBooking(order.id)
    ElMessage.success('预约已取消')
    getOrderList() // 刷新列表
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
const handlePayOrder = async (order: BookingOrder) => {
  if (!order || !order.id) {
    ElMessage.warning('订单数据异常，无法支付');
    return;
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要支付该预约吗？价格：¥${order.totalAmount}`,
      '支付预约',
      {
        confirmButtonText: '确定支付',
        cancelButtonText: '取消',
        type: 'info',
      }
    )
    
    loading.value = true
    
    // 调用支付宝支付接口
    const res = await payBookingWithAlipay(order.id)
    
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
  
  // 根据状态值和支付状态判断
  return order.status === 1 && (order.paymentStatus === 0 || order.paymentStatus === undefined);
}

// 获取订单状态显示信息
const getOrderStatusInfo = (order: BookingOrder) => {
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

onMounted(() => {
  getOrderList()
})
</script>

<template>
  <div class="order-list-container" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-button" @click="router.back()">
        <i class="fas fa-chevron-left"></i>
      </div>
      <div class="page-title">我的订单</div>
      <div class="placeholder"></div>
    </div>

    <!-- 状态筛选 -->
    <div class="status-filter">
      <div 
        class="status-item" 
        :class="{ active: currentStatus === null }"
        @click="handleStatusChange(null)"
      >
        全部
      </div>
      <div 
        v-for="(status, key) in orderStatusMap" 
        :key="key"
        class="status-item"
        :class="{ active: currentStatus === Number(key) }"
        @click="handleStatusChange(Number(key))"
      >
        {{ status.text }}
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list">
      <div v-if="loadError" class="error-tip">
        <i class="fas fa-exclamation-circle"></i>
        <p>{{ loadError }}</p>
        <el-button type="primary" @click="retryLoading" size="small">
          <i class="fas fa-sync-alt"></i> 重新加载
        </el-button>
      </div>
      
      <div v-else-if="orderList.length === 0 && !loading" class="empty-tip">
        <i class="fas fa-calendar-times"></i>
        <p>暂无订单</p>
        <small class="debug-info" v-if="isDev">
          allOrders: {{allOrders.length}}, filteredOrders: {{pagination.total}}
        </small>
        <el-button type="primary" @click="retryLoading" size="small">
          <i class="fas fa-sync-alt"></i> 重新加载
        </el-button>
      </div>

      <div v-else>
        <!-- 显示有效的订单列表 -->
        <div 
          v-for="(order, index) in orderList" 
          :key="order?.id || index"
          class="order-item"
          @click="viewOrderDetail(order)"
        >
          <div class="order-header">
            <div class="order-number">订单号: {{ order.orderNo || order.bookingCode || '无订单号' }}</div>
            <div class="order-status">
              <el-tag :type="getOrderStatusInfo(order).color">
                {{ getOrderStatusInfo(order).text }}
              </el-tag>
            </div>
          </div>

          <div class="order-content">
            <div class="venue-info">
              <div class="venue-name">{{ order.venueName || '未知场馆' }}</div>
              <div class="venue-address">{{ order.venueAddress || '无地址信息' }}</div>
            </div>

            <div class="booking-info">
              <div class="booking-date">
                <i class="fas fa-calendar-day"></i>
                {{ formatDate(order.bookingDate) }}
              </div>
              <div class="booking-time">
                <i class="fas fa-clock"></i>
                {{ formatTime(order.startTime) }} - {{ formatTime(order.endTime) }}
              </div>
              <div class="booking-type" v-if="order.bookingTypeName">
                <i class="fas fa-tag"></i>
                {{ order.bookingTypeName }}
              </div>
            </div>
          </div>

          <div class="order-footer">
            <div class="order-price">¥{{ order.totalAmount }}</div>
            <div class="order-actions">
              <el-button 
                v-if="canCancelOrder(order)" 
                size="small" 
                type="danger" 
                plain
                @click.stop="handleCancelOrder(order)"
              >
                取消
              </el-button>
              <el-button 
                v-if="canPayOrder(order)" 
                size="small" 
                type="primary"
                @click.stop="handlePayOrder(order)"
              >
                支付
              </el-button>
              <el-button 
                size="small" 
                plain
                @click.stop="viewOrderDetail(order)"
              >
                详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

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
      <a class="tab-item" @click="router.push('/message')">
        <i class="fas fa-bell"></i>
        <span>消息</span>
      </a>
      <a class="tab-item" @click="router.push('/profile')">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.order-list-container {
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

.status-filter {
  display: flex;
  background-color: #ffffff;
  padding: 10px 15px;
  margin-bottom: 15px;
  overflow-x: auto;
  white-space: nowrap;
  scrollbar-width: none; /* Firefox */
}

.status-filter::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Edge */
}

.status-item {
  padding: 6px 12px;
  margin-right: 10px;
  border-radius: 15px;
  font-size: 14px;
  cursor: pointer;
  background-color: #f2f6fc;
  color: #606266;
  transition: all 0.2s;
}

.status-item.active {
  background-color: #409eff;
  color: #ffffff;
}

.order-list {
  padding: 0 15px;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
}

.error-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #f56c6c;
}

.empty-tip i, .error-tip i {
  font-size: 40px;
  margin-bottom: 10px;
}

.empty-tip p, .error-tip p {
  margin-bottom: 15px;
  text-align: center;
  max-width: 80%;
}

.order-item {
  background-color: #ffffff;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}

.order-item:hover {
  transform: translateY(-2px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #f2f6fc;
}

.order-number {
  font-size: 14px;
  color: #606266;
}

.order-content {
  padding: 15px;
  border-bottom: 1px solid #f2f6fc;
}

.venue-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 5px;
}

.venue-address {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.booking-info {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.booking-date, .booking-time, .booking-type {
  display: flex;
  align-items: center;
  margin-right: 15px;
  font-size: 14px;
  color: #606266;
}

.booking-date i, .booking-time i, .booking-type i {
  margin-right: 5px;
  color: #409eff;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
}

.order-price {
  font-size: 16px;
  color: #f56c6c;
  font-weight: 500;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 0 15px;
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

.debug-info {
  font-size: 12px;
  margin-bottom: 10px;
  color: #999;
}
</style> 