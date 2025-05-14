import request from '@/utils/request'
import type { 
  Review, 
  ReviewQuery, 
  ReviewStats, 
  ReviewReport, 
  ReviewReportQuery, 
  ReviewReportAuditParams,
  ReviewBan
} from '@/types/review'

/**
 * 管理员获取所有评价的分页列表
 */
export function getReviewList(query: ReviewQuery) {
  return request({
    url: '/admin/reviews',
    method: 'get',
    params: query
  })
}

/**
 * 获取评价详情
 */
export function getReviewById(id: number) {
  return request({
    url: `/reviews/${id}`,
    method: 'get'
  })
}

/**
 * 管理员回复评价
 */
export function replyReview(id: number, content: string) {
  return request({
    url: `/admin/reviews/${id}/admin-reply`,
    method: 'post',
    data: { content }
  })
}

/**
 * 管理员删除评价
 */
export function deleteReview(id: number) {
  return request({
    url: `/admin/reviews/${id}`,
    method: 'delete'
  })
}

/**
 * 删除评价回复
 */
export function deleteReviewReply(replyId: number) {
  return request({
    url: `/reviews/replies/${replyId}`,
    method: 'delete'
  })
}

/**
 * 获取所有场馆评价统计数据
 */
export function getVenueReviewStats(params: { page: number, size: number }) {
  return request({
    url: '/admin/reviews/stats',
    method: 'get',
    params
  })
}

/**
 * 获取指定场馆的评价统计数据
 */
export function getVenueReviewStatsByVenueId(venueId: number) {
  return request({
    url: `/reviews/stats/venue/${venueId}`,
    method: 'get'
  })
}

/**
 * 管理员封禁评价
 */
export function banReview(id: number, reason: string, reportId?: number) {
  return request({
    url: `/admin/reviews/${id}/ban`,
    method: 'post',
    data: { reason, reportId }
  })
}

/**
 * 管理员解除评价封禁
 */
export function unbanReview(id: number) {
  return request({
    url: `/admin/reviews/${id}/unban`,
    method: 'post'
  })
}

/**
 * 获取评价封禁记录
 */
export function getReviewBanRecords(reviewId: number) {
  return request({
    url: `/admin/reviews/${reviewId}/ban-records`,
    method: 'get'
  })
}

/**
 * 获取举报列表
 */
export function getReportList(query: ReviewReportQuery) {
  return request({
    url: '/admin/reviews/reports',
    method: 'get',
    params: query
  })
}

/**
 * 获取举报详情
 */
export function getReportById(id: number) {
  return request({
    url: `/admin/reviews/reports/${id}`,
    method: 'get'
  })
}

/**
 * 审核举报
 */
export function auditReport(id: number, data: ReviewReportAuditParams) {
  return request({
    url: `/admin/reviews/reports/${id}/audit`,
    method: 'put',
    data
  })
} 