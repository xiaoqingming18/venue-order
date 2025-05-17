import http from '@/utils/http'
import type { UserFeedback, FeedbackSubmitRequest, FeedbackReply, FeedbackReplyRequest } from '@/types/feedback'
import type { PageResult } from '@/types/common'

/**
 * 提交用户反馈
 * @param data 反馈数据
 */
export function submitFeedback(data: FeedbackSubmitRequest) {
  return http.post<number>('/user/feedback/submit', data)
}

/**
 * 获取用户反馈详情
 * @param id 反馈ID
 */
export function getFeedbackDetail(id: number) {
  return http.get<UserFeedback>(`/user/feedback/detail/${id}`)
}

/**
 * 获取用户反馈列表
 * @param pageNum 页码
 * @param pageSize 每页大小
 */
export function getUserFeedbackList(pageNum = 1, pageSize = 8) {
  return http.get<PageResult<UserFeedback>>('/user/feedback/list', {
    params: { 
      pageNum, 
      pageSize 
    },
    timeout: 15000
  })
}

/**
 * 获取反馈回复列表
 * @param feedbackId 反馈ID
 */
export function getFeedbackReplies(feedbackId: number) {
  return http.get<FeedbackReply[]>(`/feedback/reply/list/${feedbackId}`)
} 