import request from '@/utils/request'

/**
 * 获取用户积分概览
 */
export function getUserPointsSummary() {
  return request({
    url: '/user/points/summary',
    method: 'get'
  })
}

/**
 * 获取用户积分记录
 */
export function getUserPointsRecords(params: {
  page?: number
  size?: number
  type?: string
}) {
  return request({
    url: '/user/points/records',
    method: 'get',
    params
  })
}

/**
 * 计算订单积分抵扣金额
 * @param orderId 订单ID
 * @param points 使用的积分数量
 */
export function calculateOrderDiscount(orderId: number, points: number) {
  return request({
    url: '/payment/calculate-discount',
    method: 'post',
    data: {
      orderId,
      usePoints: points
    }
  })
}

/**
 * 使用积分抵扣订单金额
 * @param orderId 订单ID
 * @param points 使用的积分数量
 */
export function usePointsForDiscount(orderId: number, points: number) {
  return request({
    url: '/payment/use-points',
    method: 'post',
    data: {
      orderId,
      usePoints: points
    }
  })
}

/**
 * 获取用户当前会员等级信息
 */
export function getUserMemberLevel() {
  return request({
    url: '/user/member/level',
    method: 'get'
  })
}

/**
 * 获取所有会员等级列表
 */
export function getAllMemberLevels() {
  return request({
    url: '/member/levels',
    method: 'get'
  })
}

/**
 * 获取积分规则列表
 */
export function getPointRules() {
  return request({
    url: '/user/points/rules',
    method: 'get'
  })
} 