import request from '@/utils/request'
import type { FavoriteDTO } from '@/types/favorite'

/**
 * 添加收藏
 * @param data 收藏参数
 * @returns Promise
 */
export function addFavorite(data: FavoriteDTO) {
  return request({
    url: '/user/favorite/add',
    method: 'post',
    data,
  })
}

/**
 * 取消收藏
 * @param venueId 场馆ID
 * @returns Promise
 */
export function removeFavorite(venueId: number) {
  return request({
    url: `/user/favorite/remove/${venueId}`,
    method: 'delete',
  })
}

/**
 * 通过收藏ID取消收藏
 * @param favoriteId 收藏ID
 * @returns Promise
 */
export function removeFavoriteById(favoriteId: number) {
  return request({
    url: `/user/favorite/${favoriteId}`,
    method: 'delete',
  })
}

/**
 * 更新收藏备注
 * @param favoriteId 收藏ID
 * @param notes 备注信息
 * @returns Promise
 */
export function updateFavoriteNotes(favoriteId: number, notes: string) {
  return request({
    url: `/user/favorite/${favoriteId}/notes`,
    method: 'put',
    data: { notes },
  })
}

/**
 * 获取收藏列表
 * @returns Promise
 */
export function getFavoriteList() {
  return request({
    url: '/user/favorite/list',
    method: 'get',
  })
}

/**
 * 分页获取收藏列表
 * @param pageNum 页码
 * @param pageSize 每页大小
 * @returns Promise
 */
export function getFavoritePage(pageNum: number = 1, pageSize: number = 10) {
  return request({
    url: '/user/favorite/page',
    method: 'get',
    params: { pageNum, pageSize },
  })
}

/**
 * 获取收藏详情
 * @param favoriteId 收藏ID
 * @returns Promise
 */
export function getFavoriteDetail(favoriteId: number) {
  return request({
    url: `/user/favorite/${favoriteId}`,
    method: 'get',
  })
}

/**
 * 检查是否已收藏
 * @param venueId 场馆ID
 * @returns Promise
 */
export function checkIsFavorite(venueId: number) {
  return request({
    url: `/user/favorite/check/${venueId}`,
    method: 'get',
  })
} 