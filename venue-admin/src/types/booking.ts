// 预约订单状态
export enum BookingStatus {
  CANCELLED = 0,   // 已取消
  PENDING = 1,     // 待支付
  PAID = 2,        // 已支付
  COMPLETED = 3,   // 已完成
  REFUNDED = 4     // 已退款
}

// 支付状态
export enum PaymentStatus {
  UNPAID = 0,      // 未支付
  PAID = 1         // 已支付
}

// 预约订单基本信息
export interface BookingOrder {
  id: number
  userId: number
  venueId: number
  locationId: number
  bookingDate: string
  startTime: string
  endTime: string
  status: BookingStatus
  totalAmount: number
  paymentStatus: PaymentStatus
  bookingCode: string
  remark?: string
  createdAt: string
  updatedAt: string
  venueName?: string
  locationName?: string
}

// 预约订单详情信息
export interface BookingOrderDetail extends BookingOrder {
  username?: string
  userPhone?: string
  venueName?: string
  venueAddress?: string
  facilities?: BookingFacility[]
}

// 预约设施信息
export interface BookingFacility {
  id: number
  orderId: number
  facilityId: number
  facilityType?: string
  quantity: number
  unitPrice: number
  amount: number
}

// 查询预约订单参数
export interface BookingQueryParams {
  page?: number
  size?: number
  status?: BookingStatus
  venueId?: number
  userId?: number
  bookingDate?: string
  startCreatedAt?: string
  endCreatedAt?: string
  keyword?: string
}

// 预约统计信息
export interface BookingStats {
  totalCount: number
  todayCount: number
  pendingCount: number
  paidCount: number
  cancelledCount: number
  completedCount: number
  refundedCount: number
  totalAmount: number
  todayAmount: number
}

// 获取状态文本
export function getBookingStatusText(status: BookingStatus): string {
  const statusMap = {
    [BookingStatus.CANCELLED]: '已取消',
    [BookingStatus.PENDING]: '待支付',
    [BookingStatus.PAID]: '已支付',
    [BookingStatus.COMPLETED]: '已完成',
    [BookingStatus.REFUNDED]: '已退款'
  }
  return statusMap[status] || '未知状态'
}

// 获取支付状态文本
export function getPaymentStatusText(status: PaymentStatus): string {
  return status === PaymentStatus.PAID ? '已支付' : '未支付'
}

// 获取状态类型（用于Element UI标签）
export function getBookingStatusType(status: BookingStatus): string {
  const statusTypeMap = {
    [BookingStatus.CANCELLED]: 'danger',
    [BookingStatus.PENDING]: 'warning',
    [BookingStatus.PAID]: 'success',
    [BookingStatus.COMPLETED]: 'success',
    [BookingStatus.REFUNDED]: 'info'
  }
  return statusTypeMap[status] || 'info'
}

// 预约规则类型
export interface BookingRule {
  id: number
  ruleType: string
  ruleKey: string
  ruleValue: string
  description: string
  status: number
  createdAt: string
  updatedAt: string
}

// 创建/更新预约规则请求
export interface BookingRuleRequest {
  ruleType: string
  ruleKey: string
  ruleValue: string
  description?: string
  status?: number
}

// 特殊日期规则类型
export interface SpecialDateRule {
  id: number
  specialDate: string
  description: string
  priceRate: number
  status: number
  createdAt: string
  updatedAt: string
}

// 创建/更新特殊日期规则请求
export interface SpecialDateRuleRequest {
  specialDate: string
  description?: string
  priceRate: number
  status?: number
} 