# 场馆评价接口文档

## 1. 创建评价

### 接口描述

用户对已完成的订单进行评价，包括环境、设施、服务、性价比等多维度评分。

### 请求方法

POST

### 请求地址

`/api/reviews`

### 权限

需要用户登录

### 注意事项

- 只能对已完成的订单进行评价
- 一个订单只能评价一次

### 请求参数

| 参数名                | 类型    | 必填 | 描述                        |
| --------------------- | ------- | ---- | --------------------------- |
| orderId               | Long    | 是   | 订单ID                      |
| content               | String  | 否   | 评价内容，最多1000个字符    |
| environmentScore      | Integer | 是   | 环境评分，范围1-5           |
| facilityScore         | Integer | 是   | 设施评分，范围1-5           |
| serviceScore          | Integer | 是   | 服务评分，范围1-5           |
| costPerformanceScore  | Integer | 是   | 性价比评分，范围1-5         |
| overallScore          | Integer | 是   | 综合评分，范围1-5           |
| images                | Array   | 否   | 评价图片URL列表             |

### 请求参数示例

```json
{
  "orderId": 123,
  "content": "场馆环境非常好，设施也很新，值得推荐！",
  "environmentScore": 5,
  "facilityScore": 4,
  "serviceScore": 5,
  "costPerformanceScore": 4,
  "overallScore": 5,
  "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"]
}
```

### 响应数据格式

| 参数名             | 类型     | 描述                            |
| ------------------ | -------- | ------------------------------- |
| code               | Integer  | 状态码，200 表示成功            |
| message            | String   | 提示信息                        |
| data               | Object   | 评价信息                        |
| - id               | Long     | 评价ID                          |
| - orderId          | Long     | 订单ID                          |
| - userId           | Long     | 用户ID                          |
| - username         | String   | 用户名                          |
| - nickname         | String   | 用户昵称                        |
| - venueId          | Long     | 场馆ID                          |
| - venueName        | String   | 场馆名称                        |
| - content          | String   | 评价内容                        |
| - environmentScore | Integer  | 环境评分                        |
| - facilityScore    | Integer  | 设施评分                        |
| - serviceScore     | Integer  | 服务评分                        |
| - costPerformanceScore | Integer | 性价比评分                    |
| - overallScore     | Integer  | 综合评分                        |
| - images           | Array    | 评价图片URL列表                 |
| - status           | Integer  | 状态：0-正常，1-已封禁         |
| - createdAt        | DateTime | 创建时间                        |
| - replies          | Array    | 回复列表                        |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderId": 123,
    "userId": 10001,
    "username": "testuser",
    "nickname": "测试用户",
    "venueId": 1,
    "venueName": "某某体育馆",
    "content": "场馆环境非常好，设施也很新，值得推荐！",
    "environmentScore": 5,
    "facilityScore": 4,
    "serviceScore": 5,
    "costPerformanceScore": 4,
    "overallScore": 5,
    "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"],
    "status": 0,
    "createdAt": "2023-05-15 14:30:00",
    "replies": []
  }
}
```

失败响应：

```json
{
  "code": 500,
  "message": "此订单已评价，不能重复评价",
  "data": null
}
```

## 2. 获取评价详情

### 接口描述

获取指定评价的详细信息。

### 请求方法

GET

### 请求地址

`/api/reviews/{id}`

### 权限

无需登录

### 请求参数

| 参数名 | 类型 | 必填 | 描述    |
| ------ | ---- | ---- | ------- |
| id     | Long | 是   | 评价ID  |

### 响应数据格式

与创建评价接口返回的数据格式相同

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderId": 123,
    "userId": 10001,
    "username": "testuser",
    "nickname": "测试用户",
    "venueId": 1,
    "venueName": "某某体育馆",
    "content": "场馆环境非常好，设施也很新，值得推荐！",
    "environmentScore": 5,
    "facilityScore": 4,
    "serviceScore": 5,
    "costPerformanceScore": 4,
    "overallScore": 5,
    "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"],
    "status": 1,
    "createdAt": "2023-05-15 14:30:00",
    "replies": [
      {
        "id": 1,
        "reviewId": 1,
        "userId": 10001,
        "content": "感谢您的评价！",
        "isAdmin": 1,
        "createdAt": "2023-05-15 15:30:00"
      }
    ]
  }
}
```

失败响应：

```json
{
  "code": 500,
  "message": "评价不存在",
  "data": null
}
```

## 3. 修改评价

### 接口描述

修改已提交的评价内容。

### 请求方法

PUT

### 请求地址

`/api/reviews/{id}`

### 权限

需要用户登录

### 注意事项

- 只能修改自己的评价
- 评价提交后7天内可修改
- 已封禁的评价不能修改

### 请求参数

与创建评价接口的请求参数相同（无需orderId）

### 请求参数示例

```json
{
  "content": "场馆环境还不错，设施有点旧了，总体满意！",
  "environmentScore": 4,
  "facilityScore": 3,
  "serviceScore": 4,
  "costPerformanceScore": 3,
  "overallScore": 4,
  "images": ["http://example.com/image1.jpg"]
}
```

### 响应数据格式

与创建评价接口的响应数据格式相同

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "orderId": 123,
    "userId": 10001,
    "username": "testuser",
    "nickname": "测试用户",
    "venueId": 1,
    "venueName": "某某体育馆",
    "content": "场馆环境还不错，设施有点旧了，总体满意！",
    "environmentScore": 4,
    "facilityScore": 3,
    "serviceScore": 4,
    "costPerformanceScore": 3,
    "overallScore": 4,
    "images": ["http://example.com/image1.jpg"],
    "status": 0,
    "createdAt": "2023-05-15 14:30:00",
    "replies": []
  }
}
```

失败响应：

```json
{
  "code": 500,
  "message": "评价已超过修改期限(7天)",
  "data": null
}
```

## 4. 删除评价

### 接口描述

删除已提交的评价。

### 请求方法

DELETE

### 请求地址

`/api/reviews/{id}`

### 权限

需要用户登录

### 注意事项

- 只能删除自己的评价
- 删除评价会同时删除该评价下的所有回复

### 请求参数

| 参数名 | 类型 | 必填 | 描述    |
| ------ | ---- | ---- | ------- |
| id     | Long | 是   | 评价ID  |

### 响应数据格式

| 参数名  | 类型    | 描述                     |
| ------- | ------- | ------------------------ |
| code    | Integer | 状态码，200 表示成功     |
| message | String  | 提示信息                 |
| data    | null    | 无数据返回               |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

失败响应：

```json
{
  "code": 500,
  "message": "无权删除此评价",
  "data": null
}
```

## 5. 获取我的评价列表

### 接口描述

获取当前登录用户的所有评价列表。

### 请求方法

GET

### 请求地址

`/api/reviews/my`

### 权限

需要用户登录

### 响应数据格式

| 参数名  | 类型    | 描述                     |
| ------- | ------- | ------------------------ |
| code    | Integer | 状态码，200 表示成功     |
| message | String  | 提示信息                 |
| data    | Array   | 评价列表                 |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "orderId": 123,
      "userId": 10001,
      "username": "testuser",
      "nickname": "测试用户",
      "venueId": 1,
      "venueName": "某某体育馆",
      "content": "场馆环境非常好，设施也很新，值得推荐！",
      "environmentScore": 5,
      "facilityScore": 4,
      "serviceScore": 5,
      "costPerformanceScore": 4,
      "overallScore": 5,
      "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"],
      "status": 1,
      "createdAt": "2023-05-15 14:30:00",
      "replies": []
    },
    {
      "id": 2,
      "orderId": 456,
      "userId": 10001,
      "username": "testuser",
      "nickname": "测试用户",
      "venueId": 2,
      "venueName": "某某足球场",
      "content": "场地很大，设施齐全",
      "environmentScore": 4,
      "facilityScore": 5,
      "serviceScore": 4,
      "costPerformanceScore": 4,
      "overallScore": 4,
      "images": [],
      "status": 0,
      "createdAt": "2023-05-16 10:20:00",
      "replies": []
    }
  ]
}
```

## 6. 获取场馆评价列表

### 接口描述

获取指定场馆的所有正常状态的评价。

### 请求方法

GET

### 请求地址

`/api/reviews/venue/{venueId}`

### 权限

无需登录

### 请求参数

| 参数名  | 类型    | 必填 | 描述                 |
| ------- | ------- | ---- | -------------------- |
| venueId | Long    | 是   | 场馆ID               |
| page    | Integer | 否   | 页码，默认1          |
| size    | Integer | 否   | 每页记录数，默认10   |

### 响应数据格式

| 参数名     | 类型    | 描述                     |
| ---------- | ------- | ------------------------ |
| code       | Integer | 状态码，200 表示成功     |
| message    | String  | 提示信息                 |
| data       | Object  | 分页数据                 |
| - records  | Array   | 评价列表                 |
| - total    | Long    | 总记录数                 |
| - size     | Integer | 每页记录数               |
| - current  | Integer | 当前页码                 |
| - pages    | Integer | 总页数                   |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "orderId": 123,
        "userId": 10001,
        "username": "testuser",
        "nickname": "测试用户",
        "venueId": 1,
        "venueName": "某某体育馆",
        "content": "场馆环境非常好，设施也很新，值得推荐！",
        "environmentScore": 5,
        "facilityScore": 4,
        "serviceScore": 5,
        "costPerformanceScore": 4,
        "overallScore": 5,
        "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"],
        "status": 1,
        "createdAt": "2023-05-15 14:30:00",
        "replies": []
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

## 7. 回复评价

### 接口描述

对评价进行回复。

### 请求方法

POST

### 请求地址

`/api/reviews/{id}/replies`

### 权限

需要用户登录

### 注意事项

- 普通用户只能回复自己的评价
- 管理员可以回复任何评价

### 请求参数

| 参数名  | 类型   | 必填 | 描述                  |
| ------- | ------ | ---- | --------------------- |
| id      | Long   | 是   | 评价ID                |
| content | String | 是   | 回复内容，最多500字符 |

### 请求参数示例

```json
{
  "content": "谢谢您的评价，我们会继续努力提升服务质量！"
}
```

### 响应数据格式

| 参数名     | 类型     | 描述                     |
| ---------- | -------- | ------------------------ |
| code       | Integer  | 状态码，200 表示成功     |
| message    | String   | 提示信息                 |
| data       | Object   | 回复信息                 |
| - id       | Long     | 回复ID                   |
| - reviewId | Long     | 评价ID                   |
| - userId   | Long     | 用户ID                   |
| - content  | String   | 回复内容                 |
| - isAdmin  | Integer  | 是否管理员回复：0-否，1-是 |
| - createdAt | DateTime | 创建时间                |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "reviewId": 1,
    "userId": 10001,
    "content": "谢谢您的评价，我们会继续努力提升服务质量！",
    "isAdmin": 0,
    "createdAt": "2023-05-15 16:30:00"
  }
}
```

失败响应：

```json
{
  "code": 500,
  "message": "无权回复此评价",
  "data": null
}
```

## 8. 删除回复

### 接口描述

删除评价回复。

### 请求方法

DELETE

### 请求地址

`/api/reviews/replies/{replyId}`

### 权限

需要用户登录

### 注意事项

- 只能删除自己的回复
- 管理员可以删除任何回复

### 请求参数

| 参数名  | 类型 | 必填 | 描述    |
| ------- | ---- | ---- | ------- |
| replyId | Long | 是   | 回复ID  |

### 响应数据格式

| 参数名  | 类型    | 描述                     |
| ------- | ------- | ------------------------ |
| code    | Integer | 状态码，200 表示成功     |
| message | String  | 提示信息                 |
| data    | null    | 无数据返回               |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

失败响应：

```json
{
  "code": 500,
  "message": "无权删除此回复",
  "data": null
}
```

## 9. 上传评价图片

### 接口描述

上传评价相关的图片。

### 请求方法

POST

### 请求地址

`/api/reviews/upload-image`

### 权限

需要用户登录

### 注意事项

- 图片大小不应超过2MB
- 支持的图片格式: jpg, jpeg, png

### 请求参数

| 参数名 | 类型 | 必填 | 描述     |
| ------ | ---- | ---- | -------- |
| file   | File | 是   | 图片文件 |

### 响应数据格式

| 参数名     | 类型   | 描述                 |
| ---------- | ------ | -------------------- |
| code       | Integer | 状态码，200 表示成功 |
| message    | String | 提示信息             |
| data       | Object | 返回数据             |
| - fileName | String | 文件名称             |
| - fileUrl  | String | 文件访问URL          |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "fileName": "review_image_1.jpg",
    "fileUrl": "http://example.com/images/review_image_1.jpg"
  }
}
```

失败响应：

```json
{
  "code": 500,
  "message": "上传图片失败: 文件格式不支持",
  "data": null
}
```

## 10. 获取场馆评价统计数据

### 接口描述

获取指定场馆的评价统计数据。

### 请求方法

GET

### 请求地址

`/api/reviews/stats/venue/{venueId}`

### 权限

无需登录

### 请求参数

| 参数名  | 类型 | 必填 | 描述    |
| ------- | ---- | ---- | ------- |
| venueId | Long | 是   | 场馆ID  |

### 响应数据格式

| 参数名                 | 类型    | 描述                     |
| ---------------------- | ------- | ------------------------ |
| code                   | Integer | 状态码，200 表示成功     |
| message                | String  | 提示信息                 |
| data                   | Object  | 统计数据                 |
| - venueId              | Long    | 场馆ID                   |
| - venueName            | String  | 场馆名称                 |
| - totalCount           | Integer | 评价总数                 |
| - pendingCount         | Integer | 正常评价数量             |
| - approvedCount        | Integer | 已封禁数量               |
| - rejectedCount        | Integer | （已弃用）               |
| - environmentAvgScore  | Double  | 环境平均评分             |
| - facilityAvgScore     | Double  | 设施平均评分             |
| - serviceAvgScore      | Double  | 服务平均评分             |
| - costPerformanceAvgScore | Double | 性价比平均评分          |
| - overallAvgScore      | Double  | 综合平均评分             |
| - goodReviewRate       | Double  | 好评率（4-5分）百分比    |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "venueId": 1,
    "venueName": "某某体育馆",
    "totalCount": 10,
    "pendingCount": 2,
    "approvedCount": 7,
    "rejectedCount": 1,
    "environmentAvgScore": 4.5,
    "facilityAvgScore": 4.2,
    "serviceAvgScore": 4.6,
    "costPerformanceAvgScore": 4.0,
    "overallAvgScore": 4.4,
    "goodReviewRate": 90.0
  }
}
```

## 11. 管理员接口 - 分页获取所有评价

### 接口描述

管理员获取所有评价的分页列表。

### 请求方法

GET

### 请求地址

`/api/admin/reviews`

### 权限

需要管理员权限

### 请求参数

| 参数名  | 类型    | 必填 | 描述                   |
| ------- | ------- | ---- | ---------------------- |
| page    | Integer | 否   | 页码，默认1            |
| size    | Integer | 否   | 每页记录数，默认10     |
| userId  | Long    | 否   | 用户ID，筛选指定用户的评价 |
| venueId | Long    | 否   | 场馆ID，筛选指定场馆的评价 |
| status  | Integer | 否   | 状态：0-正常，1-已封禁 |

### 响应数据格式

与获取场馆评价列表接口的响应格式相同

## 12. 管理员接口 - 封禁评价

### 接口描述

管理员封禁违规评价。

### 请求方法

POST

### 请求地址

`/api/admin/reviews/{id}/ban`

### 权限

需要管理员权限

### 请求参数

| 参数名  | 类型    | 必填 | 描述                       |
| ------- | ------- | ---- | -------------------------- |
| id      | Long    | 是   | 评价ID                     |
| reason  | String  | 是   | 封禁原因                   |

### 请求参数示例

```json
{
  "reason": "评价包含不当内容，违反平台规定"
}
```

### 响应数据格式

与获取评价详情接口的响应格式相同

## 13. 管理员接口 - 解除评价封禁

### 接口描述

管理员解除评价的封禁状态。

### 请求方法

POST

### 请求地址

`/api/admin/reviews/{id}/unban`

### 权限

需要管理员权限

### 请求参数

| 参数名  | 类型    | 必填 | 描述       |
| ------- | ------- | ---- | ---------- |
| id      | Long    | 是   | 评价ID     |

### 响应数据格式

与获取评价详情接口的响应格式相同

## 14. 管理员接口 - 管理员回复评价

### 接口描述

管理员回复评价。

### 请求方法

POST

### 请求地址

`/api/admin/reviews/{id}/admin-reply`

### 权限

需要管理员权限

### 请求参数

| 参数名  | 类型   | 必填 | 描述                  |
| ------- | ------ | ---- | --------------------- |
| id      | Long   | 是   | 评价ID                |
| content | String | 是   | 回复内容，最多500字符 |

### 请求参数示例

```json
{
  "content": "感谢您的评价，我们会持续改进服务质量！"
}
```

### 响应数据格式

与回复评价接口的响应格式相同

## 15. 管理员接口 - 管理员删除评价

### 接口描述

管理员删除评价。

### 请求方法

DELETE

### 请求地址

`/api/admin/reviews/{id}`

### 权限

需要管理员权限

### 请求参数

| 参数名 | 类型 | 必填 | 描述    |
| ------ | ---- | ---- | ------- |
| id     | Long | 是   | 评价ID  |

### 响应数据格式

与删除评价接口的响应格式相同

## 16. 管理员接口 - 获取所有场馆评价统计数据

### 接口描述

获取所有场馆的评价统计数据。

### 请求方法

GET

### 请求地址

`/api/admin/reviews/stats`

### 权限

需要管理员权限

### 请求参数

| 参数名 | 类型    | 必填 | 描述               |
| ------ | ------- | ---- | ------------------ |
| page   | Integer | 否   | 页码，默认1        |
| size   | Integer | 否   | 每页记录数，默认10 |

### 响应数据格式

| 参数名     | 类型    | 描述                     |
| ---------- | ------- | ------------------------ |
| code       | Integer | 状态码，200 表示成功     |
| message    | String  | 提示信息                 |
| data       | Object  | 分页数据                 |
| - records  | Array   | 场馆评价统计列表         |
| - total    | Long    | 总记录数                 |
| - size     | Integer | 每页记录数               |
| - current  | Integer | 当前页码                 |
| - pages    | Integer | 总页数                   |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "venueId": 1,
        "venueName": "某某体育馆",
        "totalCount": 10,
        "pendingCount": 2,
        "approvedCount": 7,
        "rejectedCount": 1,
        "environmentAvgScore": 4.5,
        "facilityAvgScore": 4.2,
        "serviceAvgScore": 4.6,
        "costPerformanceAvgScore": 4.0,
        "overallAvgScore": 4.4,
        "goodReviewRate": 90.0
      },
      {
        "venueId": 2,
        "venueName": "某某足球场",
        "totalCount": 5,
        "pendingCount": 1,
        "approvedCount": 4,
        "rejectedCount": 0,
        "environmentAvgScore": 4.2,
        "facilityAvgScore": 4.5,
        "serviceAvgScore": 4.0,
        "costPerformanceAvgScore": 3.8,
        "overallAvgScore": 4.1,
        "goodReviewRate": 80.0
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

## 17. 用户举报评价

### 接口描述

用户举报违规评价。

### 请求方法

POST

### 请求地址

`/api/reviews/{id}/report`

### 权限

需要用户登录

### 请求参数

| 参数名       | 类型   | 必填 | 描述                  |
| ------------ | ------ | ---- | --------------------- |
| id           | Long   | 是   | 评价ID                |
| reason       | String | 是   | 举报原因，最多100个字符 |
| reasonDetail | String | 否   | 举报详细说明，最多500个字符 |

### 请求参数示例

```json
{
  "reason": "含有不当言论",
  "reasonDetail": "评价内容中包含侮辱性言论和不实信息"
}
```

### 响应数据格式

| 参数名              | 类型     | 描述                            |
| ------------------- | -------- | ------------------------------- |
| code                | Integer  | 状态码，200 表示成功            |
| message             | String   | 提示信息                        |
| data                | Object   | 举报信息                        |
| - id                | Long     | 举报ID                          |
| - reviewId          | Long     | 评价ID                          |
| - reporterId        | Long     | 举报人ID                        |
| - reporterUsername  | String   | 举报人用户名                    |
| - reporterNickname  | String   | 举报人昵称                      |
| - reason            | String   | 举报原因                        |
| - reasonDetail      | String   | 举报详细说明                    |
| - status            | Integer  | 状态：0-待处理，1-已通过，2-已拒绝 |
| - createdAt         | DateTime | 创建时间                        |
| - updatedAt         | DateTime | 更新时间                        |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "reviewId": 123,
    "reporterId": 10001,
    "reporterUsername": "testuser",
    "reporterNickname": "测试用户",
    "reason": "含有不当言论",
    "reasonDetail": "评价内容中包含侮辱性言论和不实信息",
    "status": 0,
    "createdAt": "2023-06-15 14:30:00",
    "updatedAt": "2023-06-15 14:30:00"
  }
}
```

失败响应：

```json
{
  "code": 500,
  "message": "您已举报过该评价",
  "data": null
}
```

## 18. 获取我的举报列表

### 接口描述

获取当前登录用户提交的所有举报。

### 请求方法

GET

### 请求地址

`/api/reviews/my-reports`

### 权限

需要用户登录

### 请求参数

| 参数名  | 类型    | 必填 | 描述                 |
| ------- | ------- | ---- | -------------------- |
| page    | Integer | 否   | 页码，默认1          |
| size    | Integer | 否   | 每页记录数，默认10   |

### 响应数据格式

| 参数名     | 类型    | 描述                     |
| ---------- | ------- | ------------------------ |
| code       | Integer | 状态码，200 表示成功     |
| message    | String  | 提示信息                 |
| data       | Object  | 分页数据                 |
| - records  | Array   | 举报列表                 |
| - total    | Long    | 总记录数                 |
| - size     | Integer | 每页记录数               |
| - current  | Integer | 当前页码                 |
| - pages    | Integer | 总页数                   |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "reviewId": 123,
        "reporterId": 10001,
        "reporterUsername": "testuser",
        "reporterNickname": "测试用户",
        "reason": "含有不当言论",
        "reasonDetail": "评价内容中包含侮辱性言论和不实信息",
        "status": 0,
        "createdAt": "2023-06-15 14:30:00",
        "updatedAt": "2023-06-15 14:30:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

## 19. 管理员接口 - 获取评价封禁记录

### 接口描述

获取指定评价的封禁记录。

### 请求方法

GET

### 请求地址

`/api/admin/reviews/{id}/ban-record`

### 权限

需要管理员权限

### 请求参数

| 参数名  | 类型    | 必填 | 描述       |
| ------- | ------- | ---- | ---------- |
| id      | Long    | 是   | 评价ID     |

### 响应数据格式

| 参数名     | 类型     | 描述                     |
| ---------- | -------- | ------------------------ |
| code       | Integer  | 状态码，200 表示成功     |
| message    | String   | 提示信息                 |
| data       | Object   | 封禁记录                 |
| - id       | Long     | 封禁ID                   |
| - reviewId | Long     | 评价ID                   |
| - adminId  | Long     | 操作管理员ID             |
| - reportId | Long     | 关联举报ID，可能为null   |
| - reason   | String   | 封禁原因                 |
| - createdAt | DateTime | 创建时间                |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "reviewId": 123,
    "adminId": 10000,
    "reportId": 5,
    "reason": "评价包含不当内容，违反平台规定",
    "createdAt": "2023-06-15 15:30:00"
  }
}
```

## 20. 管理员接口 - 分页获取所有封禁记录

### 接口描述

分页获取所有评价封禁记录。

### 请求方法

GET

### 请求地址

`/api/admin/reviews/ban-records`

### 权限

需要管理员权限

### 请求参数

| 参数名  | 类型    | 必填 | 描述                 |
| ------- | ------- | ---- | -------------------- |
| page    | Integer | 否   | 页码，默认1          |
| size    | Integer | 否   | 每页记录数，默认10   |

### 响应数据格式

| 参数名     | 类型    | 描述                     |
| ---------- | ------- | ------------------------ |
| code       | Integer | 状态码，200 表示成功     |
| message    | String  | 提示信息                 |
| data       | Object  | 分页数据                 |
| - records  | Array   | 封禁记录列表             |
| - total    | Long    | 总记录数                 |
| - size     | Integer | 每页记录数               |
| - current  | Integer | 当前页码                 |
| - pages    | Integer | 总页数                   |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "reviewId": 123,
        "adminId": 10000,
        "reportId": 5,
        "reason": "评价包含不当内容，违反平台规定",
        "createdAt": "2023-06-15 15:30:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

## 21. 管理员接口 - 分页获取所有举报记录

### 接口描述

管理员分页获取所有评价举报记录。

### 请求方法

GET

### 请求地址

`/api/admin/reviews/reports`

### 权限

需要管理员权限

### 请求参数

| 参数名  | 类型    | 必填 | 描述                                   |
| ------- | ------- | ---- | -------------------------------------- |
| page    | Integer | 否   | 页码，默认1                            |
| size    | Integer | 否   | 每页记录数，默认10                     |
| status  | Integer | 否   | 状态：0-待处理，1-已通过，2-已拒绝     |

### 响应数据格式

与获取我的举报列表接口的响应格式相同

## 22. 管理员接口 - 获取举报详情

### 接口描述

管理员获取指定举报的详细信息。

### 请求方法

GET

### 请求地址

`/api/admin/reviews/reports/{id}`

### 权限

需要管理员权限

### 请求参数

| 参数名  | 类型    | 必填 | 描述       |
| ------- | ------- | ---- | ---------- |
| id      | Long    | 是   | 举报ID     |

### 响应数据格式

| 参数名               | 类型     | 描述                            |
| -------------------- | -------- | ------------------------------- |
| code                 | Integer  | 状态码，200 表示成功            |
| message              | String   | 提示信息                        |
| data                 | Object   | 举报信息                        |
| - id                 | Long     | 举报ID                          |
| - reviewId           | Long     | 评价ID                          |
| - reporterId         | Long     | 举报人ID                        |
| - reporterUsername   | String   | 举报人用户名                    |
| - reporterNickname   | String   | 举报人昵称                      |
| - reason             | String   | 举报原因                        |
| - reasonDetail       | String   | 举报详细说明                    |
| - status             | Integer  | 状态：0-待处理，1-已通过，2-已拒绝 |
| - adminId            | Long     | 处理管理员ID                    |
| - adminUsername      | String   | 管理员用户名                    |
| - adminNotes         | String   | 管理员处理备注                  |
| - createdAt          | DateTime | 创建时间                        |
| - updatedAt          | DateTime | 更新时间                        |
| - review             | Object   | 关联的评价信息                  |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "reviewId": 123,
    "reporterId": 10001,
    "reporterUsername": "testuser",
    "reporterNickname": "测试用户",
    "reason": "含有不当言论",
    "reasonDetail": "评价内容中包含侮辱性言论和不实信息",
    "status": 0,
    "adminId": null,
    "adminUsername": null,
    "adminNotes": null,
    "createdAt": "2023-06-15 14:30:00",
    "updatedAt": "2023-06-15 14:30:00",
    "review": {
      "id": 123,
      "orderId": 456,
      "userId": 10002,
      "username": "user2",
      "nickname": "用户2",
      "venueId": 1,
      "venueName": "某某体育馆",
      "content": "这里有问题的评价内容...",
      "environmentScore": 4,
      "facilityScore": 3,
      "serviceScore": 2,
      "costPerformanceScore": 1,
      "overallScore": 2,
      "images": [],
      "status": 0,
      "createdAt": "2023-06-14 10:30:00",
      "replies": []
    }
  }
}
```

## 23. 管理员接口 - 审核举报

### 接口描述

管理员审核用户提交的评价举报。

### 请求方法

PUT

### 请求地址

`/api/admin/reviews/reports/{id}/audit`

### 权限

需要管理员权限

### 请求参数

| 参数名      | 类型    | 必填 | 描述                       |
| ----------- | ------- | ---- | -------------------------- |
| id          | Long    | 是   | 举报ID                     |
| status      | Integer | 是   | 审核状态：1-通过，2-拒绝   |
| adminNotes  | String  | 否   | 管理员处理备注，最多500字符 |
| banReview   | Boolean | 否   | 是否同时封禁评价，默认false |
| banReason   | String  | 否   | 封禁原因，最多500字符      |

### 请求参数示例

```json
{
  "status": 1,
  "adminNotes": "举报属实，内容违规",
  "banReview": true,
  "banReason": "评价包含不当内容，违反平台规定"
}
```

### 响应数据格式

与获取举报详情接口的响应格式相同
