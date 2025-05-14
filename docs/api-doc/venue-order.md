# 场馆预约接口文档

## 1. 创建预约订单

### 接口描述

创建场馆预约订单，支持包场和设施两种预约模式。

### 请求方法

POST

### 请求地址

`/api/booking-orders`

### 权限

需要用户登录

### 注意事项

- 预约类型为1时表示包场预约，为2时表示设施预约
- 设施预约时需要提供预约设施列表
- 目前跳过支付流程，预约后直接成功

### 请求参数

| 参数名       | 类型   | 必填 | 描述                         |
| ------------ | ------ | ---- | ---------------------------- |
| venueId      | Long   | 是   | 场馆ID                       |
| bookingDate  | Date   | 是   | 预约日期，格式：yyyy-MM-dd   |
| startTime    | Time   | 是   | 开始时间，格式：HH:mm:ss     |
| endTime      | Time   | 是   | 结束时间，格式：HH:mm:ss     |
| bookingType  | Integer| 是   | 预约类型：1-包场预约，2-设施预约 |
| remarks      | String | 否   | 备注信息                     |
| facilities   | Array  | 否   | 预约设施列表(预约类型为2时使用) |

facilities参数说明：

| 参数名    | 类型    | 必填 | 描述       |
| --------- | ------- | ---- | ---------- |
| facilityId| Long    | 是   | 设施ID     |
| quantity  | Integer | 是   | 预约数量   |

### 请求参数示例

包场预约：
```json
{
  "venueId": 1,
  "bookingDate": "2023-06-01",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "bookingType": 1,
  "remarks": "周末打球"
}
```

设施预约：
```json
{
  "venueId": 1,
  "bookingDate": "2023-06-01",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "bookingType": 2,
  "remarks": "预约羽毛球场地",
  "facilities": [
    {
      "facilityId": 1,
      "quantity": 2
    },
    {
      "facilityId": 3,
      "quantity": 1
    }
  ]
}
```

### 响应数据格式

| 参数名      | 类型    | 描述                          |
| ----------- | ------- | ----------------------------- |
| code        | Integer | 状态码，200表示成功            |
| message     | String  | 提示信息                      |
| data        | Object  | 返回数据                      |
| - id        | Long    | 预约订单ID                    |
| - orderNo   | String  | 订单编号                      |
| - userId    | Long    | 用户ID                        |
| - username  | String  | 用户名                        |
| - venueId   | Long    | 场馆ID                        |
| - venueName | String  | 场馆名称                      |
| - bookingDate | Date   | 预约日期                      |
| - startTime | Time    | 开始时间                      |
| - endTime   | Time    | 结束时间                      |
| - bookingType | Integer | 预约类型                     |
| - bookingTypeName | String | 预约类型名称              |
| - totalAmount | Decimal | 订单总金额                   |
| - status    | Integer | 订单状态                      |
| - statusName | String | 订单状态名称                  |
| - facilities | Array  | 预约设施详情(预约类型为2时有值) |
| - createdAt | DateTime| 创建时间                      |

### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderNo": "20230601140012345678",
    "userId": 10001,
    "username": "zhangsan",
    "venueId": 1,
    "venueName": "星动篮球馆",
    "bookingDate": "2023-06-01",
    "startTime": "14:00:00",
    "endTime": "16:00:00",
    "bookingType": 2,
    "bookingTypeName": "设施预约",
    "totalAmount": 160.00,
    "status": 2,
    "statusName": "已预约",
    "remarks": "预约羽毛球场地",
    "facilities": [
      {
        "id": 1,
        "orderId": 1,
        "facilityId": 1,
        "facilityType": "羽毛球场",
        "quantity": 2,
        "unitPrice": 40.00,
        "amount": 80.00
      },
      {
        "id": 2,
        "orderId": 1,
        "facilityId": 3,
        "facilityType": "更衣室",
        "quantity": 1,
        "unitPrice": 20.00,
        "amount": 20.00
      }
    ],
    "createdAt": "2023-06-01 13:45:36"
  }
}
```

## 2. 检查场馆可用性

### 接口描述

检查场馆在指定日期和时间段的可用性，包括整体可预约状态和各设施的可用情况。

### 请求方法

GET

### 请求地址

`/api/booking-orders/availability/venue/{venueId}`

### 权限

需要用户登录

### 请求参数

| 参数名    | 类型   | 必填 | 描述                       |
| --------- | ------ | ---- | -------------------------- |
| venueId   | Long   | 是   | 路径参数，场馆ID           |
| date      | Date   | 是   | 查询日期，格式：yyyy-MM-dd |
| startTime | Time   | 是   | 开始时间，格式：HH:mm:ss   |
| endTime   | Time   | 是   | 结束时间，格式：HH:mm:ss   |

### 响应数据格式

| 参数名            | 类型    | 描述                       |
| ----------------- | ------- | -------------------------- |
| code              | Integer | 状态码，200表示成功         |
| message           | String  | 提示信息                   |
| data              | Object  | 返回数据                   |
| - venueId         | Long    | 场馆ID                     |
| - venueName       | String  | 场馆名称                   |
| - date            | Date    | 日期                       |
| - startTime       | Time    | 开始时间                   |
| - endTime         | Time    | 结束时间                   |
| - basePrice       | Decimal | 基准价格                   |
| - currentPrice    | Decimal | 当前时段价格               |
| - availableForAll | Boolean | 是否可整体预约             |
| - facilities      | Array   | 设施可用性列表             |

facilities参数说明：

| 参数名            | 类型    | 描述                |
| ----------------- | ------- | ------------------- |
| facilityId        | Long    | 设施ID              |
| facilityType      | String  | 设施类型            |
| totalQuantity     | Integer | 总数量              |
| availableQuantity | Integer | 可用数量            |
| available         | Boolean | 是否可用            |

### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "venueId": 1,
    "venueName": "星动篮球馆",
    "date": "2023-06-01",
    "startTime": "14:00:00",
    "endTime": "16:00:00",
    "basePrice": 80.00,
    "currentPrice": 80.00,
    "availableForAll": true,
    "facilities": [
      {
        "facilityId": 1,
        "facilityType": "篮球场",
        "totalQuantity": 4,
        "availableQuantity": 4,
        "available": true
      },
      {
        "facilityId": 2,
        "facilityType": "更衣室",
        "totalQuantity": 10,
        "availableQuantity": 8,
        "available": true
      },
      {
        "facilityId": 3,
        "facilityType": "淋浴间",
        "totalQuantity": 6,
        "availableQuantity": 6,
        "available": true
      }
    ]
  }
}
```

## 3. 获取当前用户的预约订单列表

### 接口描述

获取当前登录用户的所有预约订单列表。

### 请求方法

GET

### 请求地址

`/api/booking-orders/my`

### 权限

需要用户登录

### 请求参数

无

### 响应数据格式

| 参数名      | 类型    | 描述                          |
| ----------- | ------- | ----------------------------- |
| code        | Integer | 状态码，200表示成功            |
| message     | String  | 提示信息                      |
| data        | Array   | 返回数据，预约订单列表         |

data数组项说明：

| 参数名      | 类型    | 描述                          |
| ----------- | ------- | ----------------------------- |
| id          | Long    | 预约订单ID                    |
| orderNo     | String  | 订单编号                      |
| venueId     | Long    | 场馆ID                        |
| venueName   | String  | 场馆名称                      |
| bookingDate | Date    | 预约日期                      |
| startTime   | Time    | 开始时间                      |
| endTime     | Time    | 结束时间                      |
| bookingType | Integer | 预约类型                      |
| bookingTypeName | String | 预约类型名称                |
| totalAmount | Decimal | 订单总金额                    |
| status      | Integer | 订单状态                      |
| statusName  | String  | 订单状态名称                  |
| createdAt   | DateTime| 创建时间                      |

### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "orderNo": "20230601140012345678",
      "venueId": 1,
      "venueName": "星动篮球馆",
      "bookingDate": "2023-06-01",
      "startTime": "14:00:00",
      "endTime": "16:00:00",
      "bookingType": 2,
      "bookingTypeName": "设施预约",
      "totalAmount": 160.00,
      "status": 2,
      "statusName": "已预约",
      "createdAt": "2023-06-01 13:45:36"
    },
    {
      "id": 2,
      "orderNo": "20230602150087654321",
      "venueId": 2,
      "venueName": "城市游泳中心",
      "bookingDate": "2023-06-02",
      "startTime": "15:00:00",
      "endTime": "17:00:00",
      "bookingType": 1,
      "bookingTypeName": "包场预约",
      "totalAmount": 300.00,
      "status": 2,
      "statusName": "已预约",
      "createdAt": "2023-06-01 14:30:22"
    }
  ]
}
```

## 4. 取消预约订单

### 接口描述

取消用户的预约订单。

### 请求方法

PUT

### 请求地址

`/api/booking-orders/{id}/cancel`

### 权限

需要用户登录，且只能取消自己的订单（管理员可以取消任意订单）

### 请求参数

| 参数名 | 类型 | 必填 | 描述               |
| ------ | ---- | ---- | ------------------ |
| id     | Long | 是   | 路径参数，订单ID   |

### 响应数据格式

| 参数名  | 类型    | 描述                 |
| ------- | ------- | -------------------- |
| code    | Integer | 状态码，200表示成功   |
| message | String  | 提示信息             |
| data    | null    | 返回数据，无内容      |

### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

## 5. 分页查询预约订单（管理员）

### 接口描述

管理员分页查询预约订单，支持多种条件过滤。

### 请求方法

GET

### 请求地址

`/api/booking-orders`

### 权限

需要管理员权限

### 请求参数

| 参数名      | 类型    | 必填 | 描述                           |
| ----------- | ------- | ---- | ------------------------------ |
| page        | Integer | 否   | 页码，默认1                    |
| size        | Integer | 否   | 每页大小，默认10               |
| userId      | Long    | 否   | 用户ID                         |
| venueId     | Long    | 否   | 场馆ID                         |
| status      | Integer | 否   | 状态                           |
| bookingType | Integer | 否   | 预约类型                       |
| startDate   | Date    | 否   | 开始日期，格式：yyyy-MM-dd     |
| endDate     | Date    | 否   | 结束日期，格式：yyyy-MM-dd     |

### 响应数据格式

| 参数名      | 类型    | 描述                 |
| ----------- | ------- | -------------------- |
| code        | Integer | 状态码，200表示成功   |
| message     | String  | 提示信息             |
| data        | Object  | 返回数据             |
| - records   | Array   | 订单列表             |
| - total     | Long    | 总记录数             |
| - size      | Integer | 每页大小             |
| - current   | Integer | 当前页码             |
| - pages     | Integer | 总页数               |

records数组项说明：与"获取当前用户的预约订单列表"接口返回的订单数据结构相同

### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "20230601140012345678",
        "userId": 10001,
        "username": "zhangsan",
        "venueId": 1,
        "venueName": "星动篮球馆",
        "bookingDate": "2023-06-01",
        "startTime": "14:00:00",
        "endTime": "16:00:00",
        "bookingType": 2,
        "bookingTypeName": "设施预约",
        "totalAmount": 160.00,
        "status": 2,
        "statusName": "已预约",
        "createdAt": "2023-06-01 13:45:36"
      }
    ],
    "total": 15,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```
