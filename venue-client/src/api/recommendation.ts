import request from '@/utils/request'
import type { Venue } from '@/types/venue'

/**
 * 获取首页推荐场馆
 * @param limit 限制数量
 * @returns 推荐场馆列表
 */
export function getHomeRecommendations(limit = 6) {
  return request({
    url: '/venue/recommendation/home',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取相似场馆推荐
 * @param venueId 当前场馆ID
 * @param limit 限制数量
 * @returns 相似场馆列表
 */
export function getSimilarVenueRecommendations(venueId: number, limit = 4) {
  return request({
    url: `/venue/recommendation/similar/${venueId}`,
    method: 'get',
    params: { limit }
  })
}

/**
 * 分页获取用户的推荐场馆列表
 * @param pageNum 页码
 * @param pageSize 每页大小
 * @returns 分页推荐场馆列表
 */
export function getPagedRecommendations(pageNum = 1, pageSize = 10) {
  return request({
    url: '/venue/recommendation/list',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/**
 * 记录用户浏览历史
 * @param venueId 场馆ID
 * @param stayDuration 停留时长(秒)
 * @param source 来源: 0-首页推荐, 1-搜索结果, 2-分类浏览, 3-其他场馆跳转
 * @returns 是否成功
 */
export function recordBrowseHistory(venueId: number, stayDuration = 0, source = 0) {
  return request({
    url: '/venue/recommendation/history',
    method: 'post',
    data: {
      venueId,
      stayDuration,
      source,
      deviceInfo: navigator.userAgent
    }
  })
}

/**
 * 提交推荐反馈
 * @param data 反馈数据
 * @returns 是否成功
 */
export function submitRecommendationFeedback(data: {
  recommendationId?: number
  venueId: number
  feedbackType: number
  reason?: string
}) {
  return request({
    url: '/venue/recommendation/feedback',
    method: 'post',
    data
  })
}

/**
 * 标记推荐为已点击
 * @param recommendationId 推荐ID
 * @returns 是否成功
 */
export function markRecommendationAsClicked(recommendationId: number) {
  return request({
    url: `/venue/recommendation/click/${recommendationId}`,
    method: 'post'
  })
} 