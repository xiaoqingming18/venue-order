# 积分模块设计文档

## 1. 功能概述

积分模块是场馆预约系统的重要组成部分，旨在提高用户活跃度和忠诚度。用户可以通过预约、评价、签到等行为获取积分，积分可用于兑换商品或抵扣订单金额。同时，基于积分累积设置了会员等级体系，不同等级的用户享受不同的折扣优惠。

### 1.1 核心功能

- **积分获取**：用户通过预约完成、评价、签到、邀请好友等方式获取积分
- **积分消费**：用户可使用积分抵扣订单金额或兑换商品/优惠券
- **会员等级**：根据用户积分自动匹配会员等级，提供对应折扣率
- **积分商城**：展示可兑换的商品，支持优惠券、实物、虚拟物品兑换
- **积分明细**：记录用户积分的获取和消费历史
- **积分规则管理**：管理不同行为的积分奖励规则
- **积分有效期管理**：管理积分过期时间，自动处理过期积分

### 1.2 用户价值

- 提升用户黏性和活跃度
- 鼓励用户完成预约和评价
- 通过会员特权增强用户归属感
- 提供多样化的积分消费和兑换渠道

## 2. 数据模型

### 2.1 核心表结构

| 表名 | 描述 |
|------|------|
| user_points | 用户积分账户表，存储用户总积分、可用积分等信息 |
| point_records | 积分记录表，记录积分变动历史 |
| point_rules | 积分规则表，定义各种行为的积分奖励规则 |
| member_levels | 会员等级表，定义不同等级的积分门槛和权益 |
| point_products | 积分商品表，定义可兑换的商品 |
| point_exchanges | 积分兑换记录表，记录兑换历史 |
| point_coupons | 积分优惠券表，存储用户兑换的优惠券 |

### 2.2 表关系

- 一个用户拥有一个积分账户（user_points）
- 一个用户可以有多个积分记录（point_records）
- 一个用户可以有多个积分兑换记录（point_exchanges）
- 一个用户可以拥有多个优惠券（point_coupons）
- 用户通过积分总量映射到对应的会员等级（member_levels）

## 3. 接口设计

### 3.1 用户端接口

#### 3.1.1 积分概览

```
GET /api/points/summary
```

获取用户积分概况，包括总积分、可用积分、即将过期积分、会员等级等信息。

**返回示例**：
```json
{
  "code": 0,
  "data": {
    "userId": 10001,
    "totalPoints": 2500,
    "availablePoints": 2300,
    "frozenPoints": 200,
    "expiredPoints": 100,
    "pointsExpiringSoon": 300,
    "memberLevel": {
      "levelId": 2,
      "levelName": "银卡会员",
      "levelValue": 2,
      "icon": "/images/member/level2.png",
      "discount": 0.95,
      "nextLevelName": "金卡会员",
      "nextLevelPoints": 2700
    }
  }
}
```

#### 3.1.2 积分记录查询

```
GET /api/points/records
```

**请求参数**：
- page: 页码，默认1
- size: 每页条数，默认10
- type: 积分类型，可选值：1-获取，2-使用，3-过期，4-冻结，5-解冻
- startDate: 开始日期
- endDate: 结束日期

**返回示例**：
```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 1001,
        "pointType": 1,
        "pointTypeName": "获取",
        "points": 100,
        "balance": 2300,
        "sourceType": 1,
        "sourceTypeName": "预约完成",
        "sourceId": "BO202305120001",
        "description": "完成场馆预约获得积分",
        "createTime": "2023-05-12 14:30:00"
      }
    ],
    "total": 35,
    "size": 10,
    "current": 1,
    "pages": 4
  }
}
```

#### 3.1.3 积分商品列表

```
GET /api/points/products
```

**请求参数**：
- page: 页码，默认1
- size: 每页条数，默认10
- type: 商品类型，可选值：1-优惠券，2-实物，3-虚拟物品，4-会员特权
- minPoints: 最低积分
- maxPoints: 最高积分

**返回示例**：
```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 1,
        "productName": "50元代金券",
        "productType": 1,
        "productTypeName": "优惠券",
        "pointsRequired": 500,
        "originalPrice": 50.00,
        "stock": 100,
        "description": "可用于任意场馆预约抵扣",
        "imageUrl": "/images/products/voucher50.png",
        "startTime": "2023-01-01 00:00:00",
        "endTime": "2023-12-31 23:59:59",
        "dailyLimit": 1,
        "totalLimit": 5,
        "status": 1
      }
    ],
    "total": 20,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```

#### 3.1.4 积分商品兑换

```
POST /api/points/exchange
```

**请求参数**：
```json
{
  "productId": 1,
  "quantity": 1,
  "contactName": "张三",
  "contactPhone": "13800138000",
  "address": "北京市海淀区XX街XX号",
  "remarks": "请工作日送货"
}
```

**返回示例**：
```json
{
  "code": 0,
  "data": {
    "exchangeId": 100001,
    "orderNo": "PE202305120001",
    "points": 500,
    "productName": "50元代金券",
    "createTime": "2023-05-12 15:30:00"
  },
  "message": "兑换成功"
}
```

#### 3.1.5 我的兑换记录

```
GET /api/points/exchanges
```

**请求参数**：
- page: 页码，默认1
- size: 每页条数，默认10
- status: 状态，可选值：1-待处理，2-已完成，3-已取消

**返回示例**：
```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 100001,
        "orderNo": "PE202305120001",
        "productId": 1,
        "productName": "50元代金券",
        "productImage": "/images/products/voucher50.png",
        "pointsPrice": 500,
        "quantity": 1,
        "totalPoints": 500,
        "status": 2,
        "statusName": "已完成",
        "createTime": "2023-05-12 15:30:00",
        "processTime": "2023-05-12 16:00:00"
      }
    ],
    "total": 8,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 3.1.6 取消兑换订单

```
PUT /api/points/exchanges/{orderNo}/cancel
```

**返回示例**：
```json
{
  "code": 0,
  "message": "取消成功"
}
```

#### 3.1.7 积分抵扣订单金额计算

```
POST /api/payment/calculate-discount
```

**请求参数**：
```json
{
  "orderId": 10001,
  "usePoints": 500
}
```

**返回示例**：
```json
{
  "code": 0,
  "data": {
    "orderId": 10001,
    "originalAmount": 100.00,
    "usePoints": 500,
    "discountAmount": 5.00,
    "finalAmount": 95.00
  }
}
```

#### 3.1.8 使用积分抵扣订单

```
POST /api/payment/use-points
```

**请求参数**：
```json
{
  "orderId": 10001,
  "usePoints": 500
}
```

**返回示例**：
```json
{
  "code": 0,
  "data": {
    "orderId": 10001,
    "orderNo": "BO202305120001",
    "originalAmount": 100.00,
    "usePoints": 500,
    "discountAmount": 5.00,
    "finalAmount": 95.00
  },
  "message": "积分抵扣成功"
}
```

#### 3.1.9 我的优惠券列表

```
GET /api/points/coupons
```

**请求参数**：
- page: 页码，默认1
- size: 每页条数，默认10
- status: 状态，可选值：1-未使用，2-已使用，3-已过期

**返回示例**：
```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 100001,
        "couponName": "50元代金券",
        "couponType": 3,
        "couponTypeName": "代金券",
        "discountValue": 50.00,
        "minOrderAmount": 100.00,
        "validStartTime": "2023-05-12 00:00:00",
        "validEndTime": "2023-06-12 23:59:59",
        "status": 1,
        "statusName": "未使用"
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 3.1.10 签到获取积分

```
POST /api/points/sign-in
```

**返回示例**：
```json
{
  "code": 0,
  "data": {
    "signInDays": 3,
    "points": 5,
    "totalPoints": 2305
  },
  "message": "签到成功"
}
```

### 3.2 管理端接口

#### 3.2.1 积分规则管理

```
GET /admin/points/rules
POST /admin/points/rules
PUT /admin/points/rules/{id}
DELETE /admin/points/rules/{id}
```

#### 3.2.2 会员等级管理

```
GET /admin/points/levels
POST /admin/points/levels
PUT /admin/points/levels/{id}
DELETE /admin/points/levels/{id}
```

#### 3.2.3 积分商品管理

```
GET /admin/points/products
POST /admin/points/products
PUT /admin/points/products/{id}
DELETE /admin/points/products/{id}
```

#### 3.2.4 兑换订单管理

```
GET /admin/points/exchanges
PUT /admin/points/exchanges/{orderNo}/process
PUT /admin/points/exchanges/{orderNo}/cancel
```

#### 3.2.5 用户积分调整

```
POST /admin/points/adjust
```

**请求参数**：
```json
{
  "userId": 10001,
  "points": 100,
  "operation": "add",  // add或subtract
  "reason": "活动奖励"
}
```

## 4. 业务流程

### 4.1 积分获取流程

1. 用户完成积分获取行为（如预约完成、评价等）
2. 系统根据积分规则表中的设置计算应得积分
3. 系统增加用户积分账户的总积分和可用积分
4. 系统创建积分记录，记录积分来源和数量
5. 系统计算新的积分总量，更新用户会员等级

### 4.2 积分兑换流程

1. 用户在积分商城选择商品并提交兑换
2. 系统检查用户积分是否足够，商品库存是否充足
3. 系统扣减用户可用积分，创建积分消费记录
4. 系统创建兑换订单，状态为"待处理"
5. 管理员处理订单（发货或生成优惠券）
6. 系统更新订单状态为"已完成"
7. 如果是优惠券类商品，自动创建优惠券记录

### 4.3 积分抵扣订单流程

1. 用户在支付订单时选择使用积分抵扣
2. 系统根据抵扣规则计算可抵扣金额
3. 系统冻结用户对应积分
4. 用户完成支付剩余金额
5. 系统消费已冻结的积分，并创建积分使用记录
6. 系统更新订单的积分抵扣信息

### 4.4 积分过期处理流程

1. 系统定时任务检查用户积分记录
2. 找出已过期但未处理的积分记录
3. 扣减用户可用积分，增加过期积分记录
4. 创建积分过期记录

## 5. 积分转换规则

### 5.1 积分获取规则

- 预约完成：每消费1元积分1分，最低获得10分
- 评价奖励：每次评价获得50分
- 每日签到：每天签到获得5分
- 邀请好友：每邀请一位好友注册并完成首次预约，获得200分

### 5.2 积分抵扣规则

- 积分抵扣：100积分 = 1元
- 最低抵扣积分：100积分
- 最高抵扣比例：订单金额的30%

### 5.3 会员等级规则

| 等级 | 名称 | 所需积分 | 折扣率 |
|------|------|----------|--------|
| 1 | 普通会员 | 0 | 100% |
| 2 | 银卡会员 | 1000 | 95% |
| 3 | 金卡会员 | 5000 | 90% |
| 4 | 钻石会员 | 10000 | 85% |

## 6. 系统架构

### 6.1 模块组件

- 积分规则管理组件
- 积分账户管理组件
- 积分记录管理组件
- 积分商城管理组件
- 兑换订单管理组件
- 积分抵扣管理组件
- 会员等级管理组件

### 6.2 数据流转

```
用户行为 -> 积分规则判断 -> 积分变更 -> 积分记录 -> 会员等级变更
                                 ↓
                             积分消费 <- 积分兑换/抵扣订单
```

## 7. 安全性考虑

- 积分操作需要进行事务控制，确保积分变更和记录创建的原子性
- 防止积分刷取，对各类积分获取行为进行频率限制
- 积分消费需要验证用户身份
- 重要积分操作需要记录日志
- 定期对积分账户进行对账，确保数据一致性

## 8. 性能优化

- 对高频查询表（如积分记录表）进行分表或分区
- 对热点数据（如用户积分账户）进行缓存
- 积分变更使用消息队列异步处理
- 积分统计报表预计算
- 对积分记录的查询增加合适的索引

## 9. 扩展性设计

- 积分规则支持灵活配置，便于未来增加新的积分获取途径
- 积分兑换支持多种商品类型，便于扩展新的兑换品类
- 会员等级体系可动态配置，支持未来调整等级策略
- 系统接口设计遵循RESTful风格，便于与其他系统集成
