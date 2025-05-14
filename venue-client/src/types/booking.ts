// 时间段实体
export interface TimeSlot {
  id: number
  venueId: number
  startTime: string
  endTime: string
  status: number
  createdAt: string
  updatedAt: string
}

// 预约订单实体
export interface BookingOrder {
  id: number
  userId: number
  username?: string
  venueId: number
  locationId: number | null
  bookingDate: string
  startTime: string
  endTime: string
  status: number
  statusName?: string
  totalAmount: number
  paymentStatus: number
  bookingCode?: string
  orderNo?: string
  createdAt: string
  updatedAt: string
  venueName?: string
  locationName?: string | null
  venueAddress?: string
  venueImage?: string
  bookingType?: number
  bookingTypeName?: string
  remarks?: string
  facilities?: BookingFacility[]
  _correctedStatus?: number  // 内部使用，修正后的状态码
}

// 场馆可用性数据
export interface VenueAvailability {
  venueId: number
  venueName: string
  date: string
    startTime: string
    endTime: string
  basePrice: number
  currentPrice: number
  availableForAll: boolean
  facilities: FacilityAvailability[]
}

// 设施可用性
export interface FacilityAvailability {
  facilityId: number
  facilityType: string
  totalQuantity: number
  availableQuantity: number
  date: string
  startTime: string
  endTime: string
  available: boolean
}

// 预约设施数据
export interface BookingFacility {
  id: number
  orderId?: number
  facilityId: number
  quantity: number
  unitPrice: number
  amount?: number
  facilityType?: string
  facilityName?: string
}

// 预约订单创建DTO
export interface BookingOrderDTO {
  venueId: number
  locationId?: number  // 场馆位置ID，可选，默认使用1
  bookingDate: string  // 格式必须为 yyyy-MM-dd
  startTime: string    // 格式必须为 HH:mm:ss
  endTime: string      // 格式必须为 HH:mm:ss
  bookingType: number  // 预约类型：1-包场预约，2-设施预约
  facilities: {
    facilityId: number
    quantity: number
  }[]
  remarks?: string
}

/**
 * 确保请求格式与后端一致的预约DTO类型
 * 这个类型明确指定了数据类型，确保与后端期望的格式完全匹配
 */
export interface BookingRequestDTO {
  venueId: number        // 场馆ID - 必须是数字
  locationId?: number    // 场馆位置ID - 可选，默认使用1
  bookingDate: string    // 格式必须为 yyyy-MM-dd
  startTime: string      // 格式必须为 HH:mm:ss
  endTime: string        // 格式必须为 HH:mm:ss
  bookingType: number    // 预约类型：1-包场预约，2-设施预约
  facilities: {
    facilityId: number   // 设施ID - 必须是数字
    quantity: number     // 数量 - 必须是数字
  }[]
  remarks?: string       // 备注 - 可选
}

/**
 * 预约规则类型
 */
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

/**
 * 特殊日期规则类型
 */
export interface SpecialDateRule {
  id: number
  specialDate: string
  description: string
  priceRate: number
  status: number
  createdAt: string
  updatedAt: string
}

/**
 * 规则类型映射
 */
export const RULE_TYPE_MAP = {
  booking_limit: '预约限制',
  booking_time: '预约时间',
  cancel_rule: '取消规则',
  member_privilege: '会员特权'
} 