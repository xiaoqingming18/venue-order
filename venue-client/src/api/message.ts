import http from '@/utils/http'
import type { UserMessage } from '@/types/feedback'
import type { PageResult } from '@/types/common'

/**
 * 获取用户未读消息数量
 */
export function getUnreadCount() {
  return http.get<number>('/user/message/unread-count')
}

/**
 * 标记消息为已读
 * @param messageId 消息ID
 */
export function markAsRead(messageId: number) {
  return http.post<boolean>(`/user/message/mark-read/${messageId}`)
}

/**
 * 标记所有消息为已读
 */
export function markAllAsRead() {
  return http.post<boolean>('/user/message/mark-all-read')
}

/**
 * 获取用户消息列表
 * @param pageNum 页码
 * @param pageSize 每页大小
 */
export function getUserMessages(pageNum = 1, pageSize = 10) {
  return http.get<PageResult<UserMessage>>('/user/message/list', {
    params: { pageNum, pageSize }
  })
}

/**
 * 删除用户消息
 * @param messageId 消息ID
 */
export function deleteMessage(messageId: number) {
  return http.delete<boolean>(`/user/message/delete/${messageId}`)
} 