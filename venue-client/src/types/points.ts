// 用户积分概览
export interface UserPointsSummary {
  userId: number
  totalPoints: number
  availablePoints: number
  frozenPoints: number
  expiredPoints: number
  memberLevel?: MemberLevel
  pointsExpiringSoon: number
  expireDate?: string
}

// 会员等级
export interface MemberLevel {
  levelId: number
  levelName: string
  levelValue: number
  icon: string
  discount: number
  nextLevelPoints?: number
  nextLevelName?: string
}

// 积分记录
export interface PointRecord {
  id: number
  pointType: number
  pointTypeName: string
  points: number
  balance: number
  sourceType: number
  sourceTypeName: string
  sourceId?: string
  description: string
  expireTime?: string
  createTime: string
}

// 积分记录分页
export interface PointRecordPage {
  records: PointRecord[]
  total: number
  size: number
  current: number
  pages: number
}

// 积分规则
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

// 积分查询参数
export interface PointRecordQuery {
  type?: string
  startDate?: string
  endDate?: string
  page: number
  size: number
}

// 积分抵扣结果
export interface PointsDiscountResult {
  originalAmount: number
  discountAmount: number
  finalAmount: number
  usePoints: number
} 