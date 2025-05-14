/**
 * 评价数据传输对象
 */
export interface ReviewDTO {
  orderId: number
  venueId?: number // 可选，因为后端会从订单中获取
  content: string
  environmentScore: number // 环境评分
  facilityScore: number // 设施评分
  serviceScore: number // 服务评分
  costPerformanceScore: number // 性价比评分
  overallScore: number // 综合评分，即总体评分
  images?: string[]
  anonymous?: boolean // 匿名评价，后端可能不需要此字段
}

/**
 * 评价回复数据传输对象
 */
export interface ReviewReplyDTO {
  content: string
}

/**
 * 评价举报数据传输对象
 */
export interface ReviewReportDTO {
  reason: string
  reasonDetail?: string
}

/**
 * 评价对象
 */
export interface Review {
  id: number
  orderId: number
  venueId: number
  userId: number
  content: string
  environmentScore: number
  facilityScore: number
  serviceScore: number
  costPerformanceScore: number
  overallScore: number
  images: string[]
  anonymous: boolean
  status: number // 0-正常，1-已封禁
  createdTime: string
  updatedTime: string
  user: {
    id: number
    username: string
    nickname: string
    avatar: string
  }
  venue: {
    id: number
    name: string
    coverImage: string
  }
  replies: ReviewReply[]
}

/**
 * 评价回复对象
 */
export interface ReviewReply {
  id: number
  reviewId: number
  content: string
  replyTime: string
  isAdmin: number // 0-普通用户回复，1-管理员回复
}

/**
 * 评价统计对象
 */
export interface ReviewStats {
  venueId: number
  totalCount: number
  averageScore: number
  scoreDistribution: {
    score: number
    count: number
    percentage: number
  }[]
}

/**
 * 评价举报对象
 */
export interface ReviewReport {
  id: number
  reviewId: number
  reporterId: number
  reporterUsername: string
  reporterNickname: string
  reason: string
  reasonDetail: string
  status: number // 0-待处理，1-已通过，2-已拒绝
  createdAt: string
  updatedAt: string
} 