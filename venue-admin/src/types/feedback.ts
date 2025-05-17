/**
 * 反馈类型定义
 */
export interface Feedback {
  id: number
  userId: number
  username?: string
  nickname?: string
  type: number
  typeName?: string
  title: string
  content: string
  contact?: string
  images?: string[]
  status: number
  statusName?: string
  createdAt: string
  updatedAt?: string
  replies?: FeedbackReply[]
}

/**
 * 反馈列表请求参数
 */
export interface FeedbackQueryParams {
  status?: number
  type?: number
  keyword?: string
  pageNum: number
  pageSize: number
}

/**
 * 反馈回复
 */
export interface FeedbackReply {
  id: number
  feedbackId: number
  userId: number
  username?: string
  nickname?: string
  userRole: number
  content: string
  createdAt: string
}

/**
 * 反馈回复请求
 */
export interface FeedbackReplyRequest {
  feedbackId: number
  content: string
}

/**
 * 反馈状态
 */
export enum FeedbackStatus {
  PENDING = 0,  // 待处理
  PROCESSING = 1, // 处理中
  REPLIED = 2,  // 已回复
  CLOSED = 3    // 已关闭
}

/**
 * 反馈类型
 */
export enum FeedbackType {
  PROBLEM = 1,  // 问题反馈
  SUGGESTION = 2, // 功能建议
  COMPLAINT = 3, // 投诉
  OTHER = 4    // 其他
}

/**
 * 反馈统计数据
 */
export interface FeedbackStats {
  total: number
  pending: number
  processing: number
  replied: number
  closed: number
  todayCount: number
  weekCount: number
  monthCount: number
  typeDistribution: {
    type: number
    typeName: string
    count: number
    percentage: number
  }[]
} 