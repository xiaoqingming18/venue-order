import request from '@/utils/request'
import type { ReviewDTO, ReviewReplyDTO, ReviewReportDTO } from '@/types/review'

/**
 * 创建评价
 * @param data 评价数据
 * @returns 评价信息
 */
export function createReview(data: ReviewDTO) {
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
}

/**
 * 获取评价详情
 * @param id 评价ID
 * @returns 评价详情
 */
export function getReviewById(id: number) {
  return request({
    url: `/reviews/${id}`,
    method: 'get'
  })
}

/**
 * 通过订单ID获取评价
 * @param orderId 订单ID
 * @returns 评价详情
 */
export function getReviewByOrderId(orderId: number) {
  return request({
    url: `/reviews/order/${orderId}`,
    method: 'get'
  })
}

/**
 * 修改评价
 * @param id 评价ID
 * @param data 评价数据
 * @returns 修改后的评价信息
 */
export function updateReview(id: number, data: ReviewDTO) {
  return request({
    url: `/reviews/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除评价
 * @param id 评价ID
 * @returns 操作结果
 */
export function deleteReview(id: number) {
  return request({
    url: `/reviews/${id}`,
    method: 'delete'
  })
}

/**
 * 获取我的评价列表
 * @returns 我的评价列表
 */
export function getMyReviews() {
  return request({
    url: '/reviews/my',
    method: 'get'
  })
}

/**
 * 获取场馆评价列表
 * @param venueId 场馆ID
 * @param page 页码
 * @param size 每页记录数
 * @returns 场馆评价分页列表
 */
export function getVenueReviews(venueId: number, page: number = 1, size: number = 10) {
  return request({
    url: `/reviews/venue/${venueId}`,
    method: 'get',
    params: { page, size }
  })
}

/**
 * 回复评价
 * @param reviewId 评价ID
 * @param data 回复数据
 * @returns 回复信息
 */
export function replyReview(reviewId: number, data: ReviewReplyDTO) {
  return request({
    url: `/reviews/${reviewId}/replies`,
    method: 'post',
    data
  })
}

/**
 * 删除回复
 * @param replyId 回复ID
 * @returns 操作结果
 */
export function deleteReply(replyId: number) {
  return request({
    url: `/reviews/replies/${replyId}`,
    method: 'delete'
  })
}

/**
 * 上传评价图片
 * @param file 图片文件
 * @returns 上传结果
 */
export function uploadReviewImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/reviews/upload-image',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

/**
 * 获取场馆评价统计数据
 * @param venueId 场馆ID
 * @returns 评价统计数据
 */
export function getVenueReviewStats(venueId: number) {
  return request({
    url: `/reviews/stats/venue/${venueId}`,
    method: 'get'
  })
}

/**
 * 举报评价
 * @param reviewId 评价ID
 * @param data 举报数据
 * @returns 举报信息
 */
export function reportReview(reviewId: number, data: ReviewReportDTO) {
  return request({
    url: `/reviews/${reviewId}/report`,
    method: 'post',
    data
  })
}

/**
 * 获取我的举报列表
 * @param page 页码
 * @param size 每页记录数
 * @returns 我的举报分页列表
 */
export function getMyReports(page: number = 1, size: number = 10) {
  return request({
    url: '/reviews/my-reports',
    method: 'get',
    params: { page, size }
  })
} 