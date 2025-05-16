# 场馆预约系统 - 后台积分管理模块

本模块实现了场馆预约系统后台管理系统的积分管理功能，包括积分规则管理、会员等级管理、用户积分管理、积分商品管理等功能。

## 功能列表

### 1. 积分规则管理

- 查看积分规则列表
- 新增/编辑积分规则
- 启用/禁用积分规则
- 删除积分规则
- 规则类型管理（预约完成、评价、签到、邀请等）

### 2. 会员等级管理

- 查看会员等级列表
- 新增/编辑会员等级
- 设置会员等级积分门槛
- 设置会员折扣率
- 会员等级状态管理（启用/禁用）

### 3. 用户积分管理

- 查看用户积分列表
- 用户积分账户详情
- 手动调整用户积分
- 过期积分处理
- 积分流水记录查询
- 按用户信息筛选积分账户

### 4. 积分记录管理

- 查看积分记录列表
- 按照积分类型筛选（获取/使用/过期/冻结/解冻）
- 按照来源类型筛选（预约/评价/签到/邀请等）
- 按照时间范围筛选
- 按照用户信息筛选

### 5. 积分商品管理

- 查看积分商品列表
- 新增/编辑积分商品
- 上架/下架商品
- 设置库存和兑换限制
- 设置兑换时间范围
- 商品详情管理

### 6. 积分兑换管理

- 查看用户兑换记录
- 处理兑换订单
- 取消兑换订单
- 查看兑换详情
- 导出兑换记录

### 7. 积分数据分析

- 积分总体情况统计
- 月度/年度积分增长分析
- 积分来源分析
- 积分使用分析
- 积分流通趋势
- 会员等级分布

## 页面列表

1. 积分规则列表 (`PointRuleListView.vue`)
2. 会员等级列表 (`MemberLevelListView.vue`)
3. 用户积分列表 (`UserPointsListView.vue`)
4. 积分记录列表 (`PointRecordListView.vue`)
5. 积分统计分析 (`PointsStatsView.vue`)
6. 积分商品列表 (`PointsProductListView.vue`)
7. 积分商品编辑 (`PointsProductEditView.vue`)
8. 兑换记录列表 (`ExchangeRecordListView.vue`)

## 技术栈

- Vue 3 + TypeScript
- Element Plus UI 组件库
- ECharts 数据可视化
- Vue Router 路由管理

## 接口依赖

本模块依赖后端提供的积分相关API接口，详见 `api/points.ts` 文件。

## 使用方式

在系统菜单中添加"积分管理"相关菜单项，链接到对应的路由。

```js
// 示例菜单项配置
const pointsMenuItems = [
  {
    title: '积分规则管理',
    path: '/home/points/rules',
    icon: 'el-icon-setting'
  },
  {
    title: '会员等级管理',
    path: '/home/points/member-levels',
    icon: 'el-icon-user'
  },
  {
    title: '用户积分管理',
    path: '/home/points/user-points',
    icon: 'el-icon-coin'
  },
  {
    title: '积分记录查询',
    path: '/home/points/user-records',
    icon: 'el-icon-document'
  },
  {
    title: '积分数据分析',
    path: '/home/points/stats',
    icon: 'el-icon-data-analysis'
  },
  {
    title: '积分商品管理',
    path: '/home/points/products',
    icon: 'el-icon-shopping-cart-full'
  },
  {
    title: '兑换记录管理',
    path: '/home/points/exchanges',
    icon: 'el-icon-s-order'
  }
]
``` 