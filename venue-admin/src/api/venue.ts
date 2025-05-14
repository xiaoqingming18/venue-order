import request from '@/utils/request'
import type { VenueTypeDTO, VenueDTO } from '@/types/venue'

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
 * 获取场馆类型详情
 */
export function getVenueTypeById(id: number) {
  return request({
    url: `/venue/types/${id}`,
    method: 'get',
  })
}

/**
 * 创建场馆类型
 */
export function createVenueType(data: VenueTypeDTO) {
  return request({
    url: '/venue/types',
    method: 'post',
    data,
  })
}

/**
 * 更新场馆类型
 */
export function updateVenueType(id: number, data: VenueTypeDTO) {
  return request({
    url: `/venue/types/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除场馆类型
 */
export function deleteVenueType(id: number) {
  return request({
    url: `/venue/types/${id}`,
    method: 'delete',
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
 * 创建场馆
 */
export function createVenue(data: VenueDTO) {
  return request({
    url: '/venues',
    method: 'post',
    data,
  })
}

/**
 * 更新场馆
 */
export function updateVenue(id: number, data: VenueDTO) {
  return request({
    url: `/venues/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 更新场馆状态
 */
export function updateVenueStatus(id: number, status: number) {
  return request({
    url: `/venues/${id}/status`,
    method: 'put',
    params: { status },
  })
}

/**
 * 删除场馆
 */
export function deleteVenue(id: number) {
  return request({
    url: `/venues/${id}`,
    method: 'delete',
  })
}

/**
 * 分页查询场馆设施
 */
export function pageFacilities(params: {
  page?: number
  size?: number
  venueId: number
  facilityType?: string
}) {
  return request({
    url: '/venue-facilities',
    method: 'get',
    params,
  })
}

/**
 * 根据场馆ID获取设施列表
 */
export function getFacilitiesByVenueId(venueId: number) {
  return request({
    url: `/venue-facilities/venue/${venueId}`,
    method: 'get',
  })
}

/**
 * 创建场馆设施
 */
export function createFacility(data: any) {
  return request({
    url: '/venue-facilities',
    method: 'post',
    data,
  })
}

/**
 * 更新场馆设施
 */
export function updateFacility(id: number, data: any) {
  return request({
    url: `/venue-facilities/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 删除场馆设施
 */
export function deleteFacility(id: number) {
  return request({
    url: `/venue-facilities/${id}`,
    method: 'delete',
  })
}

/**
 * 分页查询场馆位置
 */
export function pageLocations(params: {
  page?: number
  size?: number
  venueId: number
  type?: string
  status?: number
}) {
  return request({
    url: '/venue-locations',
    method: 'get',
    params,
  })
}

/**
 * 根据场馆ID获取位置列表
 */
export function getLocationsByVenueId(venueId: number) {
  return request({
    url: `/venue-locations/venue/${venueId}`,
    method: 'get',
  })
}

/**
 * 创建场馆位置
 */
export function createLocation(data: any) {
  return request({
    url: '/venue-locations',
    method: 'post',
    data,
  })
}

/**
 * 更新场馆位置
 */
export function updateLocation(id: number, data: any) {
  return request({
    url: `/venue-locations/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 更新场馆位置状态
 */
export function updateLocationStatus(id: number, status: number) {
  return request({
    url: `/venue-locations/${id}/status`,
    method: 'put',
    params: { status },
  })
}

/**
 * 删除场馆位置
 */
export function deleteLocation(id: number) {
  return request({
    url: `/venue-locations/${id}`,
    method: 'delete',
  })
} 