<script setup lang="ts">
import { ref, onMounted, computed, inject } from 'vue'
import { getBookingRulesByType, getSpecialDateRules } from '@/api/booking'
import { ElMessage } from 'element-plus'
import type { BookingRule, SpecialDateRule } from '@/types/booking'
import { RULE_TYPE_MAP } from '@/types/booking'
import dayjs from 'dayjs'

// 接收父组件提供的props
const props = defineProps({
  showInDialog: {
    type: Boolean,
    default: false
  }
})

// 从父组件接收数据(如果有)
const injectedBookingRules = inject('bookingRules', null)
const injectedSpecialDates = inject('specialDates', null)

const loading = ref(false)
const showRules = ref(false)
const bookingRules = ref<{ [key: string]: BookingRule[] }>({})
const specialDateRules = ref<SpecialDateRule[]>([])

// 获取规则类型的显示文本
const getRuleTypeText = (type: string) => {
  return RULE_TYPE_MAP[type as keyof typeof RULE_TYPE_MAP] || type
}

// 规则键的显示文本映射
const ruleKeyMap = {
  // 预约限制
  max_daily_bookings: '每日最大预约数',
  max_weekly_bookings: '每周最大预约数',
  max_duration: '最大预约时长(小时)',
  
  // 预约时间
  advance_days: '提前预约天数',
  min_start_time: '最早开始时间',
  max_end_time: '最晚结束时间',
  
  // 取消规则
  cancel_before_hours: '提前取消小时数',
  refund_percentage: '退款比例',
  max_cancellations: '最大取消次数',
  
  // 会员特权
  discount_rate: '折扣率',
  advance_booking_days: '提前预约额外天数',
  priority_booking: '优先预约'
}

// 获取规则键的显示文本
const getRuleKeyText = (key: string) => {
  return ruleKeyMap[key as keyof typeof ruleKeyMap] || key
}

// 格式化规则值
const formatRuleValue = (key: string, value: string) => {
  if (key === 'discount_rate') {
    // 折扣率，转换为百分比
    return `${Number(value) * 100}%`
  } else if (key === 'refund_percentage') {
    // 退款比例，添加百分号
    return `${value}%`
  } else if (key === 'min_start_time' || key === 'max_end_time') {
    // 时间格式化
    return value.substring(0, 5) // 只显示HH:MM
  } else if (key === 'priority_booking') {
    // 布尔值转换
    return value === 'true' ? '是' : '否'
  }
  return value
}

// 下个月的日期
const nextMonth = dayjs().add(1, 'month').format('YYYY-MM-DD')

// 计算当前月份特殊日期
const currentMonthSpecialDates = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return specialDateRules.value.filter(rule => {
    return rule.specialDate >= today && rule.specialDate <= nextMonth
  })
})

// 按日期对特殊日期进行排序
const sortedSpecialDates = computed(() => {
  return [...currentMonthSpecialDates.value].sort((a, b) => {
    return a.specialDate.localeCompare(b.specialDate)
  })
})

// 计算不可预约日期
const unavailableDates = computed(() => {
  return sortedSpecialDates.value
    .filter(rule => rule.status === 0)
    .map(rule => rule.specialDate)
})

// 获取价格倍率文本
const getPriceRateText = (rate: number) => {
  if (rate === 1) return '正常价格'
  if (rate > 1) return `${rate}倍价格`
  return `${rate * 100}%折扣`
}

// 获取预约规则和特殊日期规则
const fetchRules = async () => {
  // 如果父组件已经提供了数据，直接使用
  if (injectedBookingRules && injectedSpecialDates) {
    bookingRules.value = injectedBookingRules.value
    specialDateRules.value = injectedSpecialDates.value
    return
  }
  
  // 否则自己获取数据
  loading.value = true
  try {
    // 获取预约规则
    const rulesRes = await getBookingRulesByType()
    bookingRules.value = rulesRes.data || {}
    
    // 获取30天内的特殊日期规则
    const today = dayjs().format('YYYY-MM-DD')
    const specialDatesRes = await getSpecialDateRules(today, nextMonth)
    specialDateRules.value = specialDatesRes.data || []
  } catch (error) {
    console.error('获取预约规则失败', error)
  } finally {
    loading.value = false
  }
}

// 切换显示/隐藏规则
const toggleRules = () => {
  if (!props.showInDialog) {
    showRules.value = !showRules.value
  }
}

onMounted(() => {
  // 在对话框中显示时，自动展开内容
  if (props.showInDialog) {
    showRules.value = true
  }
  
  fetchRules()
})
</script>

<template>
  <div class="booking-rule-info" :class="{ 'in-dialog': showInDialog }">
    <div v-if="!showInDialog" class="rule-header" @click="toggleRules">
      <h3>预约规则 <el-icon class="toggle-icon" :class="{ 'is-active': showRules }"><el-icon-arrow-down /></el-icon></h3>
    </div>
    
    <div v-if="loading" class="loading-text">
      <el-icon class="is-loading"><el-icon-loading /></el-icon> 加载预约规则中...
    </div>
    
    <div v-else-if="showRules || showInDialog" class="rule-content">
      <el-divider>基本预约规则</el-divider>
      
      <el-empty v-if="Object.keys(bookingRules).length === 0" description="暂无预约规则" />
      
      <el-collapse v-else accordion>
        <el-collapse-item 
          v-for="(rules, type) in bookingRules" 
          :key="type"
          :title="getRuleTypeText(type)"
        >
          <div class="rule-item" v-for="rule in rules" :key="rule.id">
            <div class="rule-name">{{ getRuleKeyText(rule.ruleKey) }}:</div>
            <div class="rule-value">{{ formatRuleValue(rule.ruleKey, rule.ruleValue) }}</div>
            <div class="rule-desc" v-if="rule.description">{{ rule.description }}</div>
          </div>
        </el-collapse-item>
      </el-collapse>
      
      <el-divider>特殊日期和节假日</el-divider>
      
      <el-empty v-if="sortedSpecialDates.length === 0" description="暂无特殊日期规则" />
      
      <div v-else class="special-dates">
        <el-timeline>
          <el-timeline-item
            v-for="rule in sortedSpecialDates"
            :key="rule.id"
            :timestamp="rule.specialDate"
            :type="rule.status === 0 ? 'danger' : rule.priceRate > 1 ? 'warning' : 'success'"
          >
            <div class="special-date-content">
              <h4>{{ rule.description || '特殊日期' }}</h4>
              <p v-if="rule.status === 0" class="unavailable">不可预约</p>
              <p v-else class="price-rate">{{ getPriceRateText(rule.priceRate) }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      
      <div class="rule-tips">
        <h4>预约提示：</h4>
        <ul>
          <li>请按照场馆规定的时间提前预约</li>
          <li>预约成功后，请按时到场，避免影响他人使用</li>
          <li v-if="unavailableDates.length > 0">
            <span class="text-danger">以下日期不可预约：</span>
            {{ unavailableDates.join('、') }}
          </li>
          <li>如有特殊情况需要取消预约，请提前与场馆联系</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.booking-rule-info {
  margin: 20px 0;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.booking-rule-info.in-dialog {
  margin: 0;
  border: none;
}

.rule-header {
  padding: 15px;
  background-color: #f5f7fa;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rule-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 5px;
}

.toggle-icon {
  transition: transform 0.3s;
}

.toggle-icon.is-active {
  transform: rotate(180deg);
}

.rule-content {
  padding: 20px;
  background-color: #fff;
}

.in-dialog .rule-content {
  padding: 0;
}

.loading-text {
  padding: 20px;
  text-align: center;
  color: #909399;
}

.rule-item {
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}

.rule-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.rule-value {
  color: #409EFF;
  font-size: 16px;
  margin-bottom: 5px;
}

.rule-desc {
  color: #909399;
  font-size: 14px;
}

.special-dates {
  max-height: 300px;
  overflow-y: auto;
}

.special-date-content {
  padding: 5px 0;
}

.special-date-content h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
}

.unavailable {
  color: #F56C6C;
  font-weight: bold;
}

.price-rate {
  color: #E6A23C;
  font-weight: bold;
}

.rule-tips {
  margin-top: 20px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
}

.rule-tips h4 {
  margin: 0 0 10px 0;
  color: #67c23a;
}

.rule-tips ul {
  margin: 0;
  padding-left: 20px;
}

.rule-tips li {
  margin-bottom: 5px;
}

.text-danger {
  color: #F56C6C;
  font-weight: bold;
}
</style> 