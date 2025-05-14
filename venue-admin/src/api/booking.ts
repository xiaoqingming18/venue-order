import request from '@/utils/request'
import type { BookingQueryParams, BookingOrderDetail, BookingStats } from '@/types/booking'

/**
 * 获取预约订单列表
 * @param params 查询参数
 */
export function getBookingOrders(params: BookingQueryParams) {
  return request({
    url: '/booking-orders',
    method: 'get',
    params
  })
}

/**
 * 获取预约订单详情
 * @param id 订单ID
 */
export function getBookingOrderById(id: number) {
  return request({
    url: `/booking-orders/${id}`,
    method: 'get'
  })
}

/**
 * 更新预约订单状态
 * @param id 订单ID
 * @param status 订单状态
 */
export function updateBookingStatus(id: number, status: number) {
  return request({
    url: `/booking-orders/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 取消预约订单
 * @param id 订单ID
 */
export function cancelBookingOrder(id: number) {
  return request({
    url: `/booking-orders/${id}/cancel`,
    method: 'put'
  })
}

/**
 * 完成预约订单
 * @param id 订单ID
 */
export function completeBookingOrder(id: number) {
  return request({
    url: `/booking-orders/${id}/complete`,
    method: 'put'
  })
}

/**
 * 订单退款
 * @param id 订单ID
 */
export function refundBookingOrder(id: number) {
  return request({
    url: `/booking-orders/${id}/refund`,
    method: 'put'
  })
}

/**
 * 获取预约统计数据
 */
export function getBookingStats() {
  return request({
    url: '/booking-orders/stats',
    method: 'get'
  })
}

/**
 * 获取预约趋势数据
 * @param type 数据类型: day, week, month
 */
export function getBookingTrend(type: string) {
  return request({
    url: '/booking-orders/trend',
    method: 'get',
    params: { type }
  })
}

/**
 * 导出预约订单数据
 * @param params 查询参数
 */
export function exportBookingOrders(params: BookingQueryParams) {
  return request({
    url: '/booking-orders/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 获取所有预约规则
export function getAllBookingRules() {
  return request({
    url: '/booking-rules',
    method: 'get'
  })
}

// 根据规则类型获取预约规则
export function getBookingRulesByType(ruleType: string) {
  return request({
    url: `/booking-rules/type/${ruleType}`,
    method: 'get'
  })
}

// 获取预约规则详情
export function getBookingRuleById(id: number) {
  return request({
    url: `/booking-rules/${id}`,
    method: 'get'
  })
}

// 创建预约规则
export function createBookingRule(data: any) {
  return request({
    url: '/booking-rules',
    method: 'post',
    data
  })
}

// 更新预约规则
export function updateBookingRule(id: number, data: any) {
  return request({
    url: `/booking-rules/${id}`,
    method: 'put',
    data
  })
}

// 删除预约规则
export function deleteBookingRule(id: number) {
  return request({
    url: `/booking-rules/${id}`,
    method: 'delete'
  })
}

// 获取预约规则（按类型分组）
export function getBookingRulesGroupByType() {
  return request({
    url: '/booking-rules/group-by-type',
    method: 'get'
  })
}

// 分页查询特殊日期规则
export function pageSpecialDateRules(params: {
  page?: number
  size?: number
  status?: number
  startDate?: string
  endDate?: string
}) {
  return request({
    url: '/special-date-rules',
    method: 'get',
    params
  })
}

// 根据日期范围获取特殊日期规则
export function getSpecialDateRulesByDateRange(startDate: string, endDate: string) {
  return request({
    url: '/special-date-rules/date-range',
    method: 'get',
    params: { startDate, endDate }
  })
}

// 获取特殊日期规则详情
export function getSpecialDateRuleById(id: number) {
  return request({
    url: `/special-date-rules/${id}`,
    method: 'get'
  })
}

// 创建特殊日期规则
export function createSpecialDateRule(data: any) {
  return request({
    url: '/special-date-rules',
    method: 'post',
    data
  })
}

// 更新特殊日期规则
export function updateSpecialDateRule(id: number, data: any) {
  return request({
    url: `/special-date-rules/${id}`,
    method: 'put',
    data
  })
}

// 删除特殊日期规则
export function deleteSpecialDateRule(id: number) {
  return request({
    url: `/special-date-rules/${id}`,
    method: 'delete'
  })
} 