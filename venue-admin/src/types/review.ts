export interface Review {
  id: number
  orderId: number
  userId: number
  username: string
  nickname: string
  venueId: number
  venueName: string
  content: string
  environmentScore: number
  facilityScore: number
  serviceScore: number
  costPerformanceScore: number
  overallScore: number
  images: string[]
  status: number // 0-正常，1-已封禁
  createdAt: string
  replies: ReviewReply[]
}

export interface ReviewReply {
  id: number
  reviewId: number
  userId: number
  content: string
  isAdmin: number
  createdAt: string
}

export interface ReviewStats {
  venueId: number
  venueName: string
  totalCount: number
  pendingCount: number // 正常评价数量
  approvedCount: number // 已封禁数量
  rejectedCount: number // 已弃用
  environmentAvgScore: number
  facilityAvgScore: number
  serviceAvgScore: number
  costPerformanceAvgScore: number
  overallAvgScore: number
  goodReviewRate: number
}

export interface ReviewQuery {
  page: number
  size: number
  userId?: number
  venueId?: number
  status?: number
}

// 举报相关类型定义
export interface ReviewReport {
  id: number
  reviewId: number
  reporterId: number
  reporterUsername: string
  reporterNickname: string
  reason: string
  reasonDetail?: string
  status: number // 0-待处理，1-已通过，2-已拒绝
  adminId?: number
  adminUsername?: string
  adminNotes?: string
  createdAt: string
  updatedAt: string
  review?: Review
}

export interface ReviewReportQuery {
  page: number
  size: number
  status?: number
}

export interface ReviewReportAuditParams {
  status: number // 1-通过，2-拒绝
  adminNotes?: string
  banReview?: boolean
  banReason?: string
}

// 封禁相关类型定义
export interface ReviewBan {
  id: number
  reviewId: number
  adminId: number
  reportId?: number
  reason: string
  createdAt: string
}

// 状态定义
export type ReviewStatusType = 0 | 1 // 0-正常，1-已封禁

// 举报状态定义
export type ReportStatusType = 0 | 1 | 2 // 0-待处理，1-已通过，2-已拒绝

interface StatusOption {
  label: string
  value: number
}

export const reviewStatusOptions: StatusOption[] = [
  { label: '正常', value: 0 },
  { label: '已封禁', value: 1 }
]

export const reportStatusOptions: StatusOption[] = [
  { label: '待处理', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 2 }
]

export const getReviewStatusLabel = (status: number): string => {
  for (let i = 0; i < reviewStatusOptions.length; i++) {
    if (reviewStatusOptions[i].value === status) {
      return reviewStatusOptions[i].label
    }
  }
  return '未知状态'
}

export const getReviewStatusType = (status: number): 'success' | 'danger' | '' => {
  switch (status) {
    case 0:
      return 'success'
    case 1:
      return 'danger'
    default:
      return ''
  }
}

export const getReportStatusLabel = (status: number): string => {
  for (let i = 0; i < reportStatusOptions.length; i++) {
    if (reportStatusOptions[i].value === status) {
      return reportStatusOptions[i].label
    }
  }
  return '未知状态'
}

export const getReportStatusType = (status: number): 'warning' | 'success' | 'danger' | '' => {
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'danger'
    default:
      return ''
  }
} 