import http from '@/utils/http'

/**
 * 获取用户未读消息数量
 */
export function getUnreadCount() {
  return http.get('/user/message/unread-count')
}

/**
 * 标记消息为已读
 * @param {number} messageId 消息ID
 */
export function markAsRead(messageId) {
  return http.post(`/user/message/mark-read/${messageId}`)
}

/**
 * 标记所有消息为已读
 */
export function markAllAsRead() {
  return http.post('/user/message/mark-all-read')
}

/**
 * 获取用户消息列表
 * @param {number} pageNum 页码
 * @param {number} pageSize 每页大小
 */
export function getUserMessages(pageNum = 1, pageSize = 10) {
  return http.get('/user/message/list', {
    params: { pageNum, pageSize }
  })
}

/**
 * 删除用户消息
 * @param {number} messageId 消息ID
 */
export function deleteMessage(messageId) {
  return http.delete(`/user/message/delete/${messageId}`)
} 