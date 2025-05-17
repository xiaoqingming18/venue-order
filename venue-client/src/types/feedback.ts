/**
 * 反馈类型
 */
export interface UserFeedback {
  id: number
  userId: number
  username: string
  nickname: string
  type: number
  typeName: string
  title: string
  content: string
  contact: string
  images: string[]
  status: number
  statusName: string
  createdAt: string
  updatedAt: string
  replies: FeedbackReply[]
}

/**
 * 反馈回复类型
 */
export interface FeedbackReply {
  id: number
  feedbackId: number
  adminId: number
  adminUsername: string
  adminNickname: string
  content: string
  createdAt: string
}

/**
 * 反馈提交请求
 */
export interface FeedbackSubmitRequest {
  type: number | null | string
  title: string
  content: string
  contact?: string
  images?: string[]
}

/**
 * 反馈回复请求
 */
export interface FeedbackReplyRequest {
  feedbackId: number
  content: string
}

/**
 * 用户消息类型
 */
export interface UserMessage {
  id: number
  userId: number
  type: number
  typeName: string
  title: string
  content: string
  relatedId: number | null
  isRead: number
  createdAt: string
} 