/**
 * 场馆推荐VO
 */
export interface VenueRecommendationVO {
  /** 推荐ID */
  id?: number
  /** 场馆ID */
  venueId: number
  /** 场馆名称 */
  venueName: string
  /** 场馆类型ID */
  venueTypeId: number
  /** 场馆类型名称 */
  venueTypeName: string
  /** 场馆封面图 */
  coverImage?: string
  /** 场馆地址 */
  address?: string
  /** 基础价格 */
  basePrice?: number
  /** 推荐理由 */
  recommendationReason?: string
  /** 推荐类型ID */
  recommendationTypeId?: number
  /** 推荐类型名称 */
  recommendationTypeName?: string
  /** 推荐时间 */
  recommendationTime?: string
  /** 是否已收藏 */
  favorited?: boolean
  /** 是否已点赞 */
  liked?: boolean
  /** 是否已踩 */
  disliked?: boolean
}

/**
 * 推荐反馈类型
 */
export enum FeedbackType {
  /** 喜欢 */
  LIKE = 1,
  /** 不喜欢 */
  DISLIKE = 2
}

/**
 * 推荐来源
 */
export enum RecommendationSource {
  /** 首页推荐 */
  HOME = 0,
  /** 搜索结果 */
  SEARCH = 1,
  /** 分类浏览 */
  CATEGORY = 2,
  /** 其他场馆跳转 */
  OTHER_VENUE = 3
} 