import request from '@/utils/request'

/**
 * 积分规则相关API
 */

// 获取所有积分规则（分页）
export const getPointRulesByPage = (params: { page: number, size: number }) => {
  return request({
    url: '/admin/points/rules',
    method: 'get',
    params
  })
}

// 获取积分规则详情
export const getPointRuleById = (id: number) => {
  return request({
    url: `/admin/points/rules/${id}`,
    method: 'get'
  })
}

// 添加或更新积分规则
export const saveOrUpdatePointRule = (data: any) => {
  return request({
    url: '/admin/points/rules',
    method: 'post',
    data
  })
}

// 更新积分规则状态
export const updatePointRuleStatus = (id: number, status: number) => {
  return request({
    url: `/admin/points/rules/${id}/status/${status}`,
    method: 'put'
  })
}

// 删除积分规则
export const deletePointRule = (id: number) => {
  return request({
    url: `/admin/points/rules/${id}`,
    method: 'delete'
  })
}

/**
 * 会员等级相关API
 */

// 获取所有会员等级（分页）
export const getMemberLevelsByPage = (params: { page: number, size: number }) => {
  return request({
    url: '/admin/member/levels',
    method: 'get',
    params
  })
}

// 获取会员等级详情
export const getMemberLevelById = (id: number) => {
  return request({
    url: `/admin/member/levels/${id}`,
    method: 'get'
  })
}

// 添加或更新会员等级
export const saveOrUpdateMemberLevel = (data: any) => {
  return request({
    url: '/admin/member/levels',
    method: 'post',
    data
  })
}

// 更新会员等级状态
export const updateMemberLevelStatus = (id: number, status: number) => {
  return request({
    url: `/admin/member/levels/${id}/status/${status}`,
    method: 'put'
  })
}

// 删除会员等级
export const deleteMemberLevel = (id: number) => {
  return request({
    url: `/admin/member/levels/${id}`,
    method: 'delete'
  })
}

/**
 * 用户积分相关API
 */

// 获取用户积分列表（分页）
export const getUserPointsListByPage = (params: any) => {
  return request({
    url: '/admin/user/points',
    method: 'get',
    params
  })
}

// 获取用户积分记录（分页）
export const getUserPointRecordsByPage = (params: any) => {
  return request({
    url: '/admin/user/points/records',
    method: 'get',
    params
  })
}

// 手动调整用户积分
export const adjustUserPoints = (data: any) => {
  return request({
    url: '/admin/user/points/adjust',
    method: 'post',
    data
  })
}

// 处理过期积分
export const processExpiredPoints = () => {
  return request({
    url: '/admin/user/points/expired/process',
    method: 'post'
  })
}

/**
 * 积分统计相关API
 */

// 获取积分获取渠道分析数据
export const getPointsSourceAnalysis = (params: any) => {
  return request({
    url: '/admin/points/stats/source',
    method: 'get',
    params
  })
}

// 获取积分使用方式分析数据
export const getPointsUsageAnalysis = (params: any) => {
  return request({
    url: '/admin/points/stats/usage',
    method: 'get',
    params
  })
}

// 获取积分流通情况统计数据
export const getPointsFlowStats = (params: any) => {
  return request({
    url: '/admin/points/stats/flow',
    method: 'get',
    params
  })
}

// 获取会员等级分布统计数据
export const getMemberLevelDistribution = () => {
  return request({
    url: '/admin/points/stats/levels',
    method: 'get'
  })
}

/**
 * 积分兑换商品相关API
 */

// 获取积分兑换商品列表（分页）
export const getPointsProductsByPage = (params: any) => {
  return request({
    url: '/admin/points/products',
    method: 'get',
    params
  })
}

// 获取积分兑换商品详情
export const getPointsProductById = (id: number) => {
  return request({
    url: `/admin/points/products/${id}`,
    method: 'get'
  })
}

// 添加或更新积分兑换商品
export const saveOrUpdatePointsProduct = (data: any) => {
  return request({
    url: '/admin/points/products',
    method: 'post',
    data
  })
}

// 更新积分兑换商品状态
export const updatePointsProductStatus = (id: number, status: number) => {
  return request({
    url: `/admin/points/products/${id}/status/${status}`,
    method: 'put'
  })
}

// 删除积分兑换商品
export const deletePointsProduct = (id: number) => {
  return request({
    url: `/admin/points/products/${id}`,
    method: 'delete'
  })
}

// 获取兑换记录列表（分页）
export const getExchangeRecordsByPage = (params: any) => {
  return request({
    url: '/admin/points/exchanges',
    method: 'get',
    params
  })
} 