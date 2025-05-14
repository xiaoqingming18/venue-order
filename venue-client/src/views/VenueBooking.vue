<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElDialog } from 'element-plus'
import dayjs from 'dayjs'
import { getVenueById, getVenueFacilities, getVenueAvailabilityPublic, getVenueLocations } from '@/api/venue'
import { getVenueAvailability, createBooking, getSpecialDateRules, getBookingRulesByType, payBookingWithAlipay } from '@/api/booking'
import type { Venue, VenueFacility, VenueLocation } from '@/types/venue'
import type { TimeSlot, BookingOrderDTO, SpecialDateRule, BookingRule } from '@/types/booking'
import { useUserStore } from '@/stores/user'
import BookingRuleInfo from '@/components/BookingRuleInfo.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据相关
const venueId = ref<number>(Number(route.params.id))
const venueInfo = ref<Venue | null>(null)
const facilities = ref<VenueFacility[]>([])
const locations = ref<VenueLocation[]>([])
const timeSlots = ref<TimeSlot[]>([])
const disabledDates = ref<string[]>([])
const loading = ref(false)
const selectedDate = ref('')
const selectedStartTime = ref('')
const selectedEndTime = ref('')
const selectedFacilities = ref<{facilityId: number, quantity: number, facilityType: string}[]>([])
const totalAmount = ref(0)
const remark = ref('')

// 特殊日期规则
const specialDates = ref<SpecialDateRule[]>([])
const isSpecialDate = ref(false)
const priceRate = ref(1)

// 预约规则弹窗相关
const ruleDialogVisible = ref(false)
const forceReadingTime = ref(6)
const canCloseDialog = ref(false)
const countdown = ref(6)
const bookingRules = ref<{ [key: string]: BookingRule[] }>({})
const ruleLoading = ref(false)

// 提供规则数据给BookingRuleInfo组件
provide('bookingRules', bookingRules)
provide('specialDates', specialDates)

// 计算当前日期和未来30天日期范围
const today = dayjs().format('YYYY-MM-DD')
const dateRange = computed(() => {
  const start = dayjs(today)
  const end = dayjs(today).add(30, 'day')
  return [start, end]
})

// 计算可选的开始时间
const availableStartTimes = computed(() => {
  if (!selectedDate.value || timeSlots.value.length === 0) {
    console.log('没有可用的开始时间 - 日期未选择或没有时间段')
    return []
  }
  
  // 过滤出状态为可用的时间段
  const times = timeSlots.value
    .filter(slot => slot.status === 1)
    .map(slot => ({
      label: slot.startTime, // 显示格式为 "HH:00"
      value: slot.startTime
    }))
  
  console.log('可用的开始时间:', times.length, '个时间段')
  return times
})

// 计算可选的结束时间
const availableEndTimes = computed(() => {
  if (!selectedStartTime.value || !selectedDate.value || timeSlots.value.length === 0) {
    console.log('没有可用的结束时间 - 开始时间未选择或日期未选择或没有时间段')
    return []
  }
  
  // 找到选中的开始时间在时间段中的索引位置
  const startTimeIndex = timeSlots.value.findIndex(slot => slot.startTime === selectedStartTime.value)
  if (startTimeIndex === -1) {
    console.log('找不到对应的开始时间:', selectedStartTime.value)
    return []
  }
  
  // 只有选择了开始时间后的时间段才可以作为结束时间
  const times = timeSlots.value
    .slice(startTimeIndex + 1)
    .filter(slot => slot.status === 1)
    .map(slot => ({
      label: slot.startTime, // 使用下一个时段的开始时间作为结束时间
      value: slot.startTime
    }))
  
  // 确保至少有一个结束时间选项
  if (times.length === 0 && startTimeIndex < timeSlots.value.length - 1) {
    // 添加下一个小时作为结束时间
    const nextSlot = timeSlots.value[startTimeIndex + 1]
    times.push({
      label: nextSlot.startTime,
      value: nextSlot.startTime
    })
  }
  
  console.log('可用的结束时间:', times.length, '个时间段')
  return times
})

// 获取场馆详细信息
const fetchVenueDetail = async () => {
  loading.value = true
  try {
    console.log(`获取场馆${venueId.value}详情`)
    const res = await getVenueById(venueId.value)
    console.log('获取到场馆详情:', res.data)
    venueInfo.value = res.data

    // 获取场馆设施
    console.log(`获取场馆${venueId.value}设施列表`)
    const facilitiesRes = await getVenueFacilities(venueId.value)
    console.log('获取到场馆设施:', facilitiesRes.data)
    facilities.value = facilitiesRes.data || []
    
    // 加载场馆位置
    const locationsRes = await getVenueLocations(venueId.value)
    locations.value = locationsRes.data.filter(location => location.status === 1) // 只显示可用的位置
    
    if (facilities.value.length === 0) {
      console.warn('场馆没有设施数据')
      ElMessage.warning('该场馆暂无可用设施')
    }
  } catch (error) {
    console.error('获取场馆详情失败', error)
    ElMessage.error('获取场馆详情失败，请稍后重试')
    router.push('/venues')
  } finally {
    loading.value = false
  }
}

// 获取场馆可用性数据
const fetchVenueAvailability = async (date: string) => {
  loading.value = true
  try {
    console.log(`获取场馆${venueId.value}在${date}的可用性数据`)
    // 使用公开API获取场馆可用时间
    const res = await getVenueAvailabilityPublic(venueId.value, date)
    console.log('获取到场馆可用性数据:', res.data)
    
    // 初始化为空数组，防止undefined
    timeSlots.value = []
    disabledDates.value = []
    
    // 确保有返回数据
    if (!res.data) {
      console.error('后端返回数据为空')
      ElMessage.error('获取场馆可用时间失败，后端返回数据为空')
      return
    }
    
    const availabilityData = res.data
    
    // 提取设施可用性信息
    const facilitiesData = availabilityData.facilities || []
    console.log(`获取到 ${facilitiesData.length} 个设施可用性数据`)
    
    // 如果没有设施数据，则每个时间段都设置为不可用
    if (facilitiesData.length === 0) {
      console.warn('没有可用设施数据')
      ElMessage.warning('该场馆暂无可用设施')
    }
    
    // 从返回数据中提取开始和结束时间 (格式为 HH:mm:ss)
    const startTimeStr = availabilityData.startTime || '08:00:00'
    const endTimeStr = availabilityData.endTime || '22:00:00'
    
    console.log('原始开始时间:', startTimeStr, '结束时间:', endTimeStr)
    
    // 安全解析时间字符串，提取小时
    let startHour = 8
    let endHour = 22
    
    try {
      if (startTimeStr && startTimeStr.includes(':')) {
        startHour = parseInt(startTimeStr.split(':')[0])
      }
      
      if (endTimeStr && endTimeStr.includes(':')) {
        endHour = parseInt(endTimeStr.split(':')[0])
      }
    } catch (e) {
      console.error('解析时间字符串失败，使用默认值', e)
    }
    
    // 验证时间范围的合理性
    if (isNaN(startHour) || startHour < 0 || startHour > 23) {
      console.warn(`开始小时 ${startHour} 无效，使用默认值 8`)
      startHour = 8
    }
    
    if (isNaN(endHour) || endHour < 0 || endHour > 24 || endHour <= startHour) {
      console.warn(`结束小时 ${endHour} 无效，使用默认值 22`)
      endHour = 22
    }
    
    console.log('最终使用的小时范围:', startHour, '至', endHour)
    
    // 创建时间段数据（每小时一个时间段）
    const slots = []
    
    // 为每小时创建一个时间段
    for (let hour = startHour; hour < endHour; hour++) {
      const currentHour = hour.toString().padStart(2, '0')
      const nextHour = (hour + 1).toString().padStart(2, '0')
      
      // 时间格式 HH:00
      const slotStartTimeStr = `${currentHour}:00`
      const slotEndTimeStr = `${nextHour}:00`
      
      // 检查该时间段下是否有可用设施
      // 只要有一个设施在这个时间段内可用，整个时间段就可用
      const hasAvailableFacility = facilitiesData.length === 0 ? false : 
        facilitiesData.some(f => f.available === true && f.availableQuantity > 0)
      
      console.log(`时间段 ${slotStartTimeStr}-${slotEndTimeStr} 可用性: ${hasAvailableFacility}`)
      
      slots.push({
        id: hour - startHour + 1,
        venueId: venueId.value,
        startTime: slotStartTimeStr,
        endTime: slotEndTimeStr,
        status: hasAvailableFacility ? 1 : 0, // 1表示可用，0表示不可用
        createdAt: '',
        updatedAt: ''
      })
    }
    
    console.log('生成的时间段:', slots)
    timeSlots.value = slots
    
    // 重置选择
    selectedStartTime.value = ''
    selectedEndTime.value = ''
    calculateTotal()
  } catch (error) {
    console.error('获取场馆可用时间失败', error)
    ElMessage.error('获取可用时间失败，请稍后重试')
    // 确保在出错时也初始化这些值
    timeSlots.value = []
    disabledDates.value = []
  } finally {
    loading.value = false
  }
}

// 选择日期时获取该日期的可用时间段
const handleDateChange = (date: string) => {
  selectedDate.value = date
  fetchVenueAvailability(date)
}

// 选择开始时间后重置结束时间
const handleStartTimeChange = () => {
  selectedEndTime.value = ''
  calculateTotal()
}

// 选择结束时间后计算总价
const handleEndTimeChange = () => {
  calculateTotal()
}

// 处理设施数量变化
const handleFacilityChange = (facility: VenueFacility, quantity: number) => {
  if (quantity < 0) return
  
  const index = selectedFacilities.value.findIndex(f => f.facilityId === facility.id)
  
  if (quantity === 0 && index !== -1) {
    // 数量为0，从选择中移除
    selectedFacilities.value.splice(index, 1)
  } else if (quantity > 0) {
    if (index !== -1) {
      // 已存在，更新数量
      selectedFacilities.value[index].quantity = quantity
    } else {
      // 不存在，添加新记录
      selectedFacilities.value.push({
        facilityId: facility.id,
        quantity,
        facilityType: facility.facilityType
      })
    }
  }
  
  calculateTotal()
}

// 获取已选择的设施数量
const getFacilityQuantity = (facilityId: number): number => {
  const found = selectedFacilities.value.find(f => f.facilityId === facilityId)
  return found ? found.quantity : 0
}

// 计算时间段长度（小时）
const calculateHours = (): number => {
  if (!selectedStartTime.value || !selectedEndTime.value) return 0
  
  // 确保时间格式统一，如果只有小时:分钟格式，添加秒
  const formatTime = (time: string) => {
    if (time.length === 5) {
      return time + ':00'
    }
    return time
  }
  
  const start = dayjs(`${selectedDate.value} ${formatTime(selectedStartTime.value)}`)
  const end = dayjs(`${selectedDate.value} ${formatTime(selectedEndTime.value)}`)
  const hours = end.diff(start, 'hour', true)
  
  console.log('计算时间段长度:', selectedStartTime.value, '至', selectedEndTime.value, '=', hours, '小时')
  return hours
}

// 获取当前使用的价格
const getCurrentPrice = computed(() => {
  // 直接使用场馆基准价格
  return venueInfo.value?.basePrice || 0
})

// 计算总价
const calculateTotal = () => {
  if (!venueInfo.value) {
    console.log('场馆信息未加载，无法计算总价')
    totalAmount.value = 0
    return
  }
  
  const hours = calculateHours()
  if (hours <= 0) {
    console.log('时间段无效或未选择，总价为0')
    totalAmount.value = 0
    return
  }
  
  // 使用场馆基准价格
  const currentPrice = getCurrentPrice.value
  
  // 计算基础费用
  const baseAmount = hours * currentPrice
  
  // 计算设施费用
  const facilityAmount = selectedFacilities.value.reduce((sum, facility) => {
    const facilityInfo = facilities.value.find(f => f.id === facility.facilityId)
    
    // 如果设施有自己的价格，使用设施价格；否则使用场馆基准价格的20%
    const unitPrice = facilityInfo?.price || (currentPrice * 0.2)
    
    return sum + unitPrice * facility.quantity * hours
  }, 0)
  
  // 总费用 = 基础费用 + 设施费用
  totalAmount.value = baseAmount + facilityAmount
  console.log('计算总价:', baseAmount, '+', facilityAmount, '=', totalAmount.value, 
              '(使用价格:', currentPrice, ')', '(使用场馆基准价格)')
}

// 获取特殊日期规则
const fetchSpecialDateRules = async () => {
  try {
    const today = dayjs().format('YYYY-MM-DD')
    const thirtyDaysLater = dayjs(today).add(30, 'day').format('YYYY-MM-DD')
    const res = await getSpecialDateRules(today, thirtyDaysLater)
    specialDates.value = res.data || []
    console.log('获取到特殊日期规则:', specialDates.value.length, '条')
  } catch (error) {
    console.error('获取特殊日期规则失败', error)
  }
}

// 检查所选日期是否为特殊日期，更新价格系数
const checkSpecialDate = () => {
  if (!selectedDate.value || specialDates.value.length === 0) {
    isSpecialDate.value = false
    priceRate.value = 1
    return
  }
  
  const matchedDate = specialDates.value.find(rule => 
    rule.specialDate === selectedDate.value && rule.status === 1
  )
  
  if (matchedDate) {
    isSpecialDate.value = true
    priceRate.value = matchedDate.priceRate
    console.log(`${selectedDate.value}是特殊日期，价格系数: ${priceRate.value}`)
  } else {
    isSpecialDate.value = false
    priceRate.value = 1
  }
}

// 检查日期是否不可预约
const isDateUnavailable = (date: Date) => {
  if (specialDates.value.length === 0) return false
  
  const dateStr = dayjs(date).format('YYYY-MM-DD')
  return specialDates.value.some(rule => 
    rule.specialDate === dateStr && rule.status === 0
  )
}

// 计算日期单元格的样式
const getCellClassName = (date: Date) => {
  const dateStr = dayjs(date).format('YYYY-MM-DD')
  
  // 检查是否是特殊日期
  const specialDate = specialDates.value.find(rule => rule.specialDate === dateStr)
  
  if (specialDate) {
    if (specialDate.status === 0) {
      return 'unavailable-date'  // 不可预约的日期
    } else if (specialDate.priceRate > 1) {
      return 'expensive-date'    // 价格上浮的日期
    } else if (specialDate.priceRate < 1) {
      return 'discount-date'     // 折扣日期
    }
  }
  
  return ''
}

// 计算选定时间段的总价格
const calculateTotalAmount = () => {
  // 如果未选择日期或时间，总价为0
  if (!selectedDate.value || !selectedStartTime.value || !selectedEndTime.value) {
    totalAmount.value = 0
    return
  }
  
  // 计算小时数
  const startHour = parseInt(selectedStartTime.value.split(':')[0])
  const endHour = parseInt(selectedEndTime.value.split(':')[0])
  const hours = endHour - startHour
  
  // 基础价格 = 场馆小时价格 * 小时数
  let basePrice = (venueInfo.value?.hourlyPrice || 0) * hours
  
  // 如果有设施，加上设施价格
  let facilitiesPrice = 0
  if (selectedFacilities.value.length > 0) {
    selectedFacilities.value.forEach(facility => {
      const facilityPrice = facilities.value.find(f => f.id === facility.facilityId)?.price || 0
      facilitiesPrice += facilityPrice * facility.quantity
    })
  }
  
  // 应用特殊日期价格系数
  let finalPrice = (basePrice + facilitiesPrice) * priceRate.value
  
  // 四舍五入到2位小数
  totalAmount.value = Math.round(finalPrice * 100) / 100
}

// 监听日期变化，检查特殊日期并更新价格
watch(selectedDate, (newDate) => {
  if (newDate) {
    checkSpecialDate()
    calculateTotalAmount()
  }
})

// 监听时间变化，更新价格
watch([selectedStartTime, selectedEndTime], () => {
  calculateTotalAmount()
})

// 监听设施变化，更新价格
watch(selectedFacilities, () => {
  calculateTotalAmount()
}, { deep: true })

// 计算两个时间之间的小时差
const getHourDifference = (startTime: string, endTime: string) => {
  if (!startTime || !endTime) return 0
  
  const startHour = parseInt(startTime.split(':')[0])
  const endHour = parseInt(endTime.split(':')[0])
  
  return endHour - startHour
}

// 计算设施总价
const calculateFacilitiesPrice = () => {
  if (selectedFacilities.value.length === 0) return 0
  
  let total = 0
  selectedFacilities.value.forEach(facility => {
    const facilityPrice = facilities.value.find(f => f.id === facility.facilityId)?.price || 0
    total += facilityPrice * facility.quantity
  })
  
  return total
}

// 获取预约规则数据
const fetchBookingRules = async () => {
  ruleLoading.value = true
  try {
    // 获取预约规则
    const rulesRes = await getBookingRulesByType()
    bookingRules.value = rulesRes.data || {}
    
    // 获取30天内的特殊日期规则
    const today = dayjs().format('YYYY-MM-DD')
    const nextMonth = dayjs().add(1, 'month').format('YYYY-MM-DD')
    const specialDatesRes = await getSpecialDateRules(today, nextMonth)
    specialDates.value = specialDatesRes.data || []
  } catch (error) {
    console.error('获取预约规则失败', error)
  } finally {
    ruleLoading.value = false
  }
}

// 打开预约规则弹窗
const openRuleDialog = (force = false) => {
  ruleDialogVisible.value = true
  if (force) {
    // 如果是强制阅读，设置倒计时
    canCloseDialog.value = false
    countdown.value = forceReadingTime.value
    
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        canCloseDialog.value = true
      }
    }, 1000)
  } else {
    // 非强制阅读，可直接关闭
    canCloseDialog.value = true
  }
}

// 关闭规则弹窗
const closeRuleDialog = () => {
  if (canCloseDialog.value) {
    ruleDialogVisible.value = false
  }
}

// 确认关闭规则弹窗
const confirmCloseRuleDialog = () => {
  if (canCloseDialog.value) {
    ruleDialogVisible.value = false
  } else {
    ElMessage.warning(`请先阅读预约规则，还需等待 ${countdown.value} 秒`)
  }
}

// 提交预约
const submitBooking = async () => {
  // 检查是否已登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 检查必填项
  if (!selectedDate.value || !selectedStartTime.value || !selectedEndTime.value) {
    ElMessage.warning('请选择预约日期和时间')
    return
  }
  
  // 检查是否选择了设施
  if (selectedFacilities.value.length === 0) {
    ElMessage.warning('请至少选择一项设施')
    return
  }
  
  try {
    // 严格格式化日期为 yyyy-MM-dd
    // 使用dayjs确保正确的日期格式
    const bookingDateFormatted = dayjs(selectedDate.value).format('YYYY-MM-DD')
    
    // 严格格式化时间为 HH:mm:ss
    // 首先确保时间格式是 HH:mm 格式，然后添加秒
    let startTimeFormatted = selectedStartTime.value
    let endTimeFormatted = selectedEndTime.value
    
    // 处理开始时间格式
    if (!startTimeFormatted.includes(':')) {
      // 如果没有冒号，添加 :00:00
      startTimeFormatted = `${startTimeFormatted}:00:00`
    } else if (startTimeFormatted.split(':').length === 2) {
      // 如果只有一个冒号 (HH:mm)，添加 :00
      startTimeFormatted = `${startTimeFormatted}:00`
    }
    
    // 处理结束时间格式
    if (!endTimeFormatted.includes(':')) {
      // 如果没有冒号，添加 :00:00
      endTimeFormatted = `${endTimeFormatted}:00:00`
    } else if (endTimeFormatted.split(':').length === 2) {
      // 如果只有一个冒号 (HH:mm)，添加 :00
      endTimeFormatted = `${endTimeFormatted}:00`
    }
    
    console.log('格式化前:', {
      date: selectedDate.value,
      startTime: selectedStartTime.value,
      endTime: selectedEndTime.value
    })
    
    console.log('格式化后:', {
      date: bookingDateFormatted,
      startTime: startTimeFormatted,
      endTime: endTimeFormatted
    })
    
    // 构建预约数据对象
    const bookingData = {
      venueId: Number(venueId.value),
      bookingDate: bookingDateFormatted,
      startTime: startTimeFormatted,
      endTime: endTimeFormatted,
      bookingType: 2, // 设为2表示设施预约
      facilities: selectedFacilities.value.map(f => ({
        facilityId: Number(f.facilityId),
        quantity: Number(f.quantity)
      }))
    }
    
    // 只有当备注存在且不为空时，才添加该字段
    if (remark.value && remark.value.trim() !== '') {
      bookingData.remarks = remark.value.trim()
    }
    
    console.log('提交预约数据:', JSON.stringify(bookingData, null, 2))
    
    // 调用API创建预约
    const res = await createBooking(bookingData)
    
    // 请求成功处理
    console.log('预约成功，响应数据:', res)
    
    const orderId = res.data.id
    
    ElMessage.success('预约成功，正在跳转到支付页面...')
    
    // 直接调用支付接口，而不是跳转到预约成功页面
    try {
      loading.value = true
      // 调用支付宝支付接口
      const payRes = await payBookingWithAlipay(orderId)
      
      if (payRes && payRes.data) {
        // 创建临时表单元素
        const div = document.createElement('div')
        div.innerHTML = payRes.data
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
      
      // 支付失败也跳转到预约成功页面，用户可以在订单页面再次尝试支付
      router.push(`/booking-success?orderId=${orderId}`)
    } finally {
      loading.value = false
    }
  } catch (error) {
    console.error('提交预约失败', error)
    
    // 提供更详细的错误信息
    let errorMessage = '提交预约失败'
    if (error.response) {
      console.error('错误详情:', {
        status: error.response.status,
        data: error.response.data
      })
      
      // 尝试从响应中获取更具体的错误信息
      errorMessage = error.response.data?.message || 
                    error.response.data?.error || 
                    '提交预约失败，请稍后重试'
    }
    
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 返回场馆详情
const goBack = () => {
  router.push(`/venue/${venueId.value}`)
}

// 页面初始化
onMounted(() => {
  // 初始化数据，防止访问undefined
  disabledDates.value = []
  timeSlots.value = []
  
  fetchVenueDetail()
  fetchSpecialDateRules()
  fetchBookingRules()
  
  // 设置默认日期为今天
  selectedDate.value = today
  fetchVenueAvailability(today)
  
  // 初始显示规则弹窗
  setTimeout(() => {
    openRuleDialog(true)
  }, 500)
})
</script>

<template>
  <div class="venue-booking-container" v-loading="loading">
    <!-- iOS状态栏 -->
    <div class="ios-status-bar">
      <div class="time">9:41</div>
      <div class="status-icons">
        <i class="fas fa-signal"></i>
        <i class="fas fa-wifi"></i>
        <i class="fas fa-battery-full"></i>
      </div>
    </div>
    
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-button" @click="goBack">
        <i class="fas fa-chevron-left"></i>
      </div>
      <div class="page-title">场馆预约</div>
      <div class="rule-button" @click="openRuleDialog()">
        <i class="fas fa-info-circle"></i>
        <span>预约规则</span>
      </div>
    </div>
    
    <div v-if="venueInfo" class="booking-content">
      <!-- 场馆信息摘要 -->
      <div class="venue-summary">
        <img :src="`https://picsum.photos/600/400?random=${venueId}`" alt="场馆图片" class="venue-image" />
        <div class="venue-info">
          <h2 class="venue-name">{{ venueInfo.name }}</h2>
          <p class="venue-address">
            <i class="fas fa-map-marker-alt"></i>
            {{ venueInfo.address }}
          </p>
          <div class="venue-price">¥{{ venueInfo.basePrice }}/小时起</div>
        </div>
      </div>
      
      <!-- 预约表单 -->
      <div class="booking-form">
        <h2 class="form-title">预约信息</h2>
        <el-form label-position="top" class="form">
          
          <!-- 选择预约日期 -->
          <div class="form-section">
            <h3 class="section-title">选择日期</h3>
            <el-date-picker
              v-model="selectedDate"
              class="date-picker"
              type="date"
              placeholder="选择日期"
              :disabled-date="(date) => {
                const now = new Date()
                // 禁用今天之前的日期和30天后的日期
                const dateIsOutOfRange = date < now || date > dateRange[1]
                // 禁用特殊规则中标记为不可预约的日期
                const dateIsUnavailable = isDateUnavailable(date)
                return dateIsOutOfRange || dateIsUnavailable
              }"
              :cell-class-name="getCellClassName"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleDateChange"
            />
            <!-- 特殊日期提示 -->
            <div v-if="isSpecialDate" class="special-date-tip">
              <el-tag 
                :type="priceRate > 1 ? 'warning' : 'success'" 
                size="small"
              >
                {{ priceRate > 1 ? '价格上浮' : '折扣价格' }} 
                (系数: {{ priceRate }})
              </el-tag>
            </div>
          </div>
          
          <!-- 选择时间段 -->
          <div class="form-section">
            <h3 class="section-title">选择时间段</h3>
            <div class="time-picker">
              <div class="time-selector">
                <label>开始时间</label>
                <el-select 
                  v-model="selectedStartTime" 
                  placeholder="选择开始时间"
                  @change="handleStartTimeChange"
                  :loading="loading"
                >
                  <el-option
                    v-for="option in availableStartTimes"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                  <template #empty>
                    <div class="empty-text">
                      {{ loading ? '加载中...' : '暂无可选时间' }}
                    </div>
                  </template>
                </el-select>
              </div>
              <div class="divider">至</div>
              <div class="time-selector">
                <label>结束时间</label>
                <el-select 
                  v-model="selectedEndTime" 
                  placeholder="选择结束时间"
                  :disabled="!selectedStartTime"
                  @change="handleEndTimeChange"
                  :loading="loading"
                >
                  <el-option
                    v-for="option in availableEndTimes"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                  <template #empty>
                    <div class="empty-text">
                      {{ loading ? '加载中...' : '请先选择开始时间' }}
                    </div>
                  </template>
                </el-select>
              </div>
            </div>
          </div>
          
          <!-- 选择设施 -->
          <div class="form-section">
            <h3 class="section-title">选择设施</h3>
            <div class="facility-list">
              <div v-if="facilities.length === 0 && loading" class="empty-tip">
                加载设施信息中...
              </div>
              <div v-else-if="facilities.length === 0 && !loading" class="empty-tip">
                暂无可用设施
              </div>
              <div 
                v-for="facility in facilities" 
                :key="facility.id"
                class="facility-item"
              >
                <div class="facility-info">
                  <div class="facility-name">{{ facility.facilityType }}</div>
                  <div class="facility-detail">
                    可用数量: {{ facility.quantity }} | 
                    单价: 
                    <span v-if="facility.price">¥{{ facility.price }}/小时</span>
                    <span v-else>¥{{ (venueInfo?.basePrice * 0.2).toFixed(2) }}/小时</span>
                  </div>
                </div>
                <div class="quantity-selector">
                  <div 
                    class="quantity-btn" 
                    @click="handleFacilityChange(facility, Math.max(0, getFacilityQuantity(facility.id) - 1))"
                  >
                    <i class="fas fa-minus"></i>
                  </div>
                  <div class="quantity">{{ getFacilityQuantity(facility.id) }}</div>
                  <div 
                    class="quantity-btn" 
                    @click="handleFacilityChange(facility, getFacilityQuantity(facility.id) + 1)"
                    :class="{ disabled: getFacilityQuantity(facility.id) >= facility.quantity }"
                  >
                    <i class="fas fa-plus"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 备注信息 -->
          <div class="form-section">
            <h3 class="section-title">备注信息</h3>
            <div class="remark-input">
              <el-input
                v-model="remark"
                type="textarea"
                placeholder="可选，填写预约的特殊需求"
                :rows="3"
                maxlength="200"
                show-word-limit
              />
            </div>
          </div>
          
          <!-- 费用信息 -->
          <div class="form-section price-section" v-if="totalAmount > 0">
            <h3 class="section-title">费用详情</h3>
            <div class="price-details">
              <div v-if="selectedStartTime && selectedEndTime" class="price-row">
                <span class="price-label">场馆费用:</span>
                <span class="price-value">{{ venueInfo?.hourlyPrice || 0 }}元/小时</span>
              </div>
              <div v-if="selectedStartTime && selectedEndTime" class="price-row">
                <span class="price-label">预约时长:</span>
                <span class="price-value">{{ getHourDifference(selectedStartTime, selectedEndTime) }}小时</span>
              </div>
              <div v-if="selectedFacilities.length > 0" class="price-row">
                <span class="price-label">设施费用:</span>
                <span class="price-value">{{ calculateFacilitiesPrice() }}元</span>
              </div>
              <div v-if="isSpecialDate" class="price-row special-price">
                <span class="price-label">特殊日期:</span>
                <el-tag 
                  :type="priceRate > 1 ? 'warning' : 'success'" 
                  size="small"
                >
                  价格系数: {{ priceRate }}
                </el-tag>
              </div>
              <div class="price-row total">
                <span class="price-label">总价:</span>
                <span class="price-value">{{ totalAmount }}元</span>
              </div>
            </div>
          </div>
          
          <!-- 提交按钮 -->
          <div class="form-section submit-section">
            <button class="submit-btn" @click="submitBooking">
              提交预约
            </button>
          </div>
        </el-form>
      </div>
    </div>
    
    <div v-else-if="!loading" class="empty-state">
      <i class="fas fa-calendar-times"></i>
      <p>场馆信息加载失败</p>
      <button class="back-btn" @click="goBack">返回场馆详情</button>
    </div>
    
    <!-- 预约规则弹窗 -->
    <el-dialog
      v-model="ruleDialogVisible"
      title="预约规则"
      width="90%"
      :close-on-click-modal="canCloseDialog"
      :close-on-press-escape="canCloseDialog"
      :show-close="canCloseDialog"
      @close="closeRuleDialog"
    >
      <div v-if="ruleLoading" class="dialog-loading">
        <el-icon class="is-loading"><el-icon-loading /></el-icon>
        <p>加载预约规则中...</p>
      </div>
      
      <div v-else class="dialog-content">
        <BookingRuleInfo show-in-dialog />
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <div v-if="!canCloseDialog" class="countdown-tip">
            请阅读预约规则（剩余 {{ countdown }} 秒）
          </div>
          <el-button @click="confirmCloseRuleDialog" :disabled="!canCloseDialog">
            {{ canCloseDialog ? '关闭' : `请等待 (${countdown}s)` }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.venue-booking-container {
  padding-bottom: 70px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.ios-status-bar {
  display: flex;
  justify-content: space-between;
  background-color: #ffffff;
  padding: 10px 15px 5px;
  font-size: 12px;
}

.ios-status-bar .status-icons {
  display: flex;
  gap: 5px;
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
}

.page-title {
  flex-grow: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
}

.rule-button {
  font-size: 14px;
  color: #409EFF;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.booking-content {
  padding: 15px;
}

.venue-summary {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 15px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.venue-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.venue-info {
  flex: 1;
}

.venue-name {
  font-size: 18px;
  margin: 0 0 5px;
  font-weight: 600;
}

.venue-address {
  font-size: 13px;
  color: #909399;
  margin: 0 0 8px;
}

.venue-price {
  font-size: 16px;
  color: #f56c6c;
  font-weight: 600;
}

.booking-form {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.form-title {
  font-size: 20px;
  margin: 0 0 20px;
  color: #303133;
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.form-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #ebeef5;
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  font-size: 16px;
  margin: 0 0 16px;
  color: #303133;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background-color: #409EFF;
  margin-right: 8px;
  border-radius: 2px;
}

.date-picker {
  width: 100%;
}

.time-picker {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.time-selector {
  flex: 1;
}

.time-selector label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.divider {
  font-size: 16px;
  color: #909399;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.facility-list {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 12px;
}

.facility-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.facility-item:last-child {
  margin-bottom: 0;
}

.facility-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.facility-detail {
  font-size: 13px;
  color: #909399;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quantity-btn {
  width: 28px;
  height: 28px;
  border-radius: 14px;
  background-color: #f2f6fc;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.quantity-btn:hover {
  background-color: #e6effd;
}

.quantity-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity {
  font-size: 16px;
  min-width: 24px;
  text-align: center;
  font-weight: 500;
}

.remark-input {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 12px;
}

.price-section {
  background-color: #f6faff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.price-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-label {
  font-size: 14px;
  color: #606266;
}

.price-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.special-price {
  margin-top: 4px;
}

.price-row.total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.price-row.total .price-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.price-row.total .price-value {
  font-size: 20px;
  color: #f56c6c;
  font-weight: 600;
}

.submit-section {
  text-align: center;
  margin-top: 16px;
}

.submit-btn {
  background-color: #409eff;
  color: #ffffff;
  border: none;
  border-radius: 24px;
  padding: 12px 48px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
  max-width: 300px;
}

.submit-btn:hover {
  background-color: #337ecc;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #909399;
}

.empty-state i {
  font-size: 60px;
  margin-bottom: 20px;
}

.back-btn {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #409eff;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.empty-tip {
  text-align: center;
  padding: 24px;
  color: #909399;
  font-size: 14px;
}

.empty-text {
  text-align: center;
  padding: 12px;
  color: #909399;
  font-size: 14px;
}

.special-date-tip {
  margin-top: 8px;
  display: flex;
  align-items: center;
}

/* 日期特殊样式 */
:deep(.unavailable-date) {
  background-color: #fde2e2;
  color: #f56c6c;
}

:deep(.expensive-date) {
  background-color: #fdf6ec;
  color: #e6a23c;
}

:deep(.discount-date) {
  background-color: #f0f9eb;
  color: #67c23a;
}

/* 对话框样式 */
.dialog-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
}

.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
}

.countdown-tip {
  color: #E6A23C;
  margin-right: 10px;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
</style> 