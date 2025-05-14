import request from '@/utils/request'
import type { VenueType, Venue, VenueFacility, VenueLocation } from '@/types/venue'

/**
 * 分页查询场馆类型
 */
export function pageVenueTypes(params: {
  page?: number
  size?: number
  name?: string
}) {
  return request({
    url: '/venue/types',
    method: 'get',
    params,
  })
}

/**
 * 获取所有场馆类型
 */
export function getAllVenueTypes() {
  return request({
    url: '/venue/types/all',
    method: 'get',
  })
}

/**
 * 获取场馆类型详情
 */
export function getVenueTypeById(id: number) {
  return request({
    url: `/venue/types/${id}`,
    method: 'get',
  })
}

/**
 * 分页查询场馆
 */
export function pageVenues(params: {
  page?: number
  size?: number
  name?: string
  venueTypeId?: number
  status?: number
}) {
  return request({
    url: '/venues',
    method: 'get',
    params,
  })
}

/**
 * 获取场馆详情
 */
export function getVenueById(id: number) {
  return request({
    url: `/venues/${id}`,
    method: 'get',
  })
}

/**
 * 获取场馆设施列表
 */
export function getVenueFacilities(venueId: number) {
  console.log(`请求场馆设施接口: /venue-facilities/venue/${venueId}`)
  return request({
    url: `/venue-facilities/venue/${venueId}`,
    method: 'get',
  })
}

/**
 * 获取场馆位置列表
 */
export function getVenueLocations(venueId: number) {
  return request({
    url: `/venue-locations/venue/${venueId}`,
    method: 'get',
  })
}

/**
 * 获取场馆可用性（时间段和设施）
 * 这个接口不需要鉴权，允许未登录用户访问
 */
export function getVenueAvailabilityPublic(venueId: number, date: string) {
  console.log(`请求场馆可用性接口(公开): /venues/${venueId}/availability?date=${date}`)
  return request({
    url: `/venues/${venueId}/availability`,
    method: 'get',
    params: { date },
    headers: {
      'X-Skip-Auth': 'true' // 标记为跳过鉴权的请求
    }
  })
} 