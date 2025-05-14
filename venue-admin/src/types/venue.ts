// 场馆类型数据传输对象
export interface VenueTypeDTO {
  id?: number
  name: string
  description?: string
  basePrice: number
}

// 场馆类型实体
export interface VenueType {
  id: number
  name: string
  description: string
  basePrice: number
  createdAt: string
  updatedAt: string
}

// 场馆数据传输对象
export interface VenueDTO {
  id?: number
  name: string
  venueTypeId: number
  basePrice: number
  capacity: number
  address: string
  description?: string
  businessHours?: string
  contactPhone?: string
  coverImage?: string
  status: number
}

// 场馆实体
export interface Venue {
  id: number
  name: string
  venueTypeId: number
  basePrice: number
  capacity: number
  address: string
  description: string
  businessHours: string
  contactPhone: string
  coverImage: string
  status: number
  createdAt: string
  updatedAt: string
}

// 场馆设施数据传输对象
export interface VenueFacilityDTO {
  id?: number
  venueId: number
  facilityType: string
  quantity: number
  positionDesc: string
  price?: number  // 设施单独价格，可选字段，为空则使用场馆基准价格
}

// 场馆设施实体
export interface VenueFacility {
  id: number
  venueId: number
  facilityType: string
  quantity: number
  positionDesc: string
  price?: number  // 设施单独价格，可选字段，为空则使用场馆基准价格
  createdAt: string
  updatedAt: string
}

// 场馆位置数据传输对象
export interface VenueLocationDTO {
  id?: number
  venueId: number
  name: string
  type: string
  status: number
  price?: number  // 位置单独价格，可选字段，为空则使用场馆基准价格
  description?: string
}

// 场馆位置实体
export interface VenueLocation {
  id: number
  venueId: number
  name: string
  type: string
  status: number
  price?: number  // 位置单独价格，可选字段，为空则使用场馆基准价格
  description: string
  createdAt: string
  updatedAt: string
}

// 分页结果通用类型
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
} 