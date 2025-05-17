import request from '@/utils/request'

/**
 * 获取反馈列表（管理员）
 * @param params 查询参数
 * @returns 
 */
export function getFeedbackList(params: {
  status?: number,
  type?: number,
  keyword?: string,
  pageNum?: number,
  pageSize?: number
}) {
  return request({
    url: '/user/feedback/admin/list',
    method: 'get',
    params
  })
}

/**
 * 获取反馈详情
 * @param id 反馈ID
 * @returns 
 */
export function getFeedbackDetail(id: number) {
  return request({
    url: `/user/feedback/detail/${id}`,
    method: 'get'
  })
}

/**
 * 更新反馈状态
 * @param id 反馈ID
 * @param status 状态值
 * @returns 
 */
export function updateFeedbackStatus(id: number, status: number) {
  return request({
    url: '/user/feedback/admin/update-status',
    method: 'post',
    params: {
      id,
      status
    }
  })
}

/**
 * 管理员回复反馈
 * @param data 回复数据
 * @returns 
 */
export function replyFeedback(data: {
  feedbackId: number,
  content: string
}) {
  return request({
    url: '/feedback/reply/admin/reply',
    method: 'post',
    data
  })
}

/**
 * 获取反馈回复列表
 * @param feedbackId 反馈ID
 * @returns 
 */
export function getFeedbackReplies(feedbackId: number) {
  return request({
    url: `/feedback/reply/list/${feedbackId}`,
    method: 'get'
  })
}

/**
 * 获取反馈统计数据
 * @returns 
 */
export function getFeedbackStats() {
  return request({
    url: '/user/feedback/admin/stats',
    method: 'get'
  })
} 