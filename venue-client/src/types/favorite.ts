/**
 * 收藏场馆请求参数
 */
export interface FavoriteDTO {
  venueId: number
  notes?: string
}

/**
 * 收藏场馆详情
 */
export interface UserFavoriteVenue {
  id: number
  userId: number
  venueId: number
  venueName: string
  coverImage: string
  address: string
  venueTypeName: string
  basePrice: string
  notes: string
  createdAt: string
} 