/**
 * 积分规则
 */
export interface PointRule {
  id: number
  ruleType: number
  ruleName: string
  pointValue: number
  ruleDescription: string
  validityDays: number
  status: number
  createTime: string
  updateTime: string
}

/**
 * 积分规则DTO
 */
export interface PointRuleDTO {
  id?: number
  ruleType: number
  ruleName: string
  pointValue: number
  ruleDescription?: string
  validityDays?: number
  status?: number
}

/**
 * 会员等级
 */
export interface MemberLevel {
  id: number
  levelName: string
  levelValue: number
  pointThreshold: number
  discountRate: number
  description?: string
  iconUrl?: string
  status: number
  createTime: string
  updateTime: string
}

/**
 * 会员等级DTO
 */
export interface MemberLevelDTO {
  id?: number
  levelName: string
  levelValue: number
  pointThreshold: number
  discountRate: number
  description?: string
  iconUrl?: string
  status?: number
}

/**
 * 用户积分账户
 */
export interface UserPoints {
  id: number
  userId: number
  username: string
  nickname?: string
  totalPoints: number
  availablePoints: number
  frozenPoints: number
  expiredPoints: number
  lastPointsUpdateTime: string
  createTime: string
  updateTime: string
}

/**
 * 积分记录
 */
export interface PointRecord {
  id: number
  userId: number
  username: string
  pointType: number
  pointTypeName: string
  points: number
  balance: number
  sourceType: number
  sourceTypeName: string
  sourceId?: string
  description: string
  expireTime?: string
  status: number
  createTime: string
}

/**
 * 积分调整DTO
 */
export interface PointsAdjustDTO {
  userId: number
  points: number
  description: string
  sourceType: number
  sourceId?: string
  expireDays?: number
}

/**
 * 积分统计分析
 */
export interface PointsStatistics {
  totalUsers: number
  totalPoints: number
  availablePoints: number
  frozenPoints: number
  expiredPoints: number
  monthlyIncome: number
  monthlyUsage: number
  yearlyIncome: number
  yearlyUsage: number
}

/**
 * 积分来源分析
 */
export interface PointsSourceAnalysis {
  sourceType: number
  sourceTypeName: string
  totalPoints: number
  percentage: number
  userCount: number
}

/**
 * 积分使用分析
 */
export interface PointsUsageAnalysis {
  usageType: number
  usageTypeName: string
  totalPoints: number
  percentage: number
  userCount: number
}

/**
 * 积分流通情况
 */
export interface PointsFlowStats {
  date: string
  income: number
  usage: number
  expire: number
}

/**
 * 会员等级分布
 */
export interface MemberLevelDistribution {
  levelId: number
  levelName: string
  userCount: number
  percentage: number
}

/**
 * 积分兑换商品
 */
export interface PointsProduct {
  id: number
  productName: string
  productDesc?: string
  imageUrl?: string
  pointsPrice: number
  originalPrice?: number
  inventory: number
  limitPerUser: number
  startTime?: string
  endTime?: string
  exchangeCount: number
  status: number
  createTime: string
  updateTime: string
}

/**
 * 积分兑换商品DTO
 */
export interface PointsProductDTO {
  id?: number
  productName: string
  productDesc?: string
  imageUrl?: string
  pointsPrice: number
  originalPrice?: number
  inventory: number
  limitPerUser: number
  startTime?: string
  endTime?: string
  status?: number
}

/**
 * 兑换记录
 */
export interface ExchangeRecord {
  id: number
  userId: number
  username: string
  productId: number
  productName: string
  pointsPrice: number
  exchangeTime: string
  status: number
}

/**
 * 分页查询参数
 */
export interface PageQuery {
  page: number
  size: number
  [key: string]: any
} 