# 场馆推荐系统接口文档

## 目录

1. [记录用户浏览历史](#1-记录用户浏览历史)
2. [获取首页推荐场馆](#2-获取首页推荐场馆)
3. [获取详情页相似场馆推荐](#3-获取详情页相似场馆推荐)
4. [分页获取用户的推荐场馆列表](#4-分页获取用户的推荐场馆列表)
5. [提交推荐反馈](#5-提交推荐反馈)
6. [标记推荐为已点击](#6-标记推荐为已点击)

## 1. 记录用户浏览历史

### 接口说明

记录用户浏览场馆的历史，用于生成个性化推荐。

### 请求信息

- 请求方法：`POST`
- 请求URL：`/api/venue/recommendation/history`
- 请求体格式：`application/json`

### 请求参数

| 参数名 | 类型 | 是否必须 | 描述 |
| --- | --- | --- | --- |
| userId | Long | 否 | 用户ID (已登录用户可不传) |
| venueId | Long | 是 | 场馆ID |
| stayDuration | Integer | 否 | 停留时长(秒) |
| source | Integer | 否 | 来源: 0-首页推荐, 1-搜索结果, 2-分类浏览, 3-其他场馆跳转 |
| deviceInfo | String | 否 | 设备信息 |

### 响应信息

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 请求示例

```json
{
  "venueId": 1,
  "stayDuration": 120,
  "source": 0,
  "deviceInfo": "Chrome/Windows"
}
```

## 2. 获取首页推荐场馆

### 接口说明

获取用户首页的推荐场馆列表，已登录用户返回个性化推荐，未登录用户返回热门推荐。

### 请求信息

- 请求方法：`GET`
- 请求URL：`/api/venue/recommendation/home`

### 请求参数

| 参数名 | 类型 | 是否必须 | 描述 |
| --- | --- | --- | --- |
| limit | Integer | 否 | 限制返回的记录数量，默认为6 |

### 响应信息

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "venueId": 2,
      "venueName": "篮球馆",
      "venueTypeName": "球类场馆",
      "coverImage": "https://example.com/basketball.jpg",
      "address": "北京市朝阳区...",
      "basePrice": 100.00,
      "description": "专业篮球场地...",
      "recommendationScore": 8.7,
      "recommendationType": 0,
      "recommendationTypeName": "猜你喜欢",
      "recommendationReason": "根据您的浏览历史推荐"
    }
    // 更多推荐记录...
  ]
}
```

## 3. 获取详情页相似场馆推荐

### 接口说明

在场馆详情页获取与当前场馆相似的其他场馆推荐。

### 请求信息

- 请求方法：`GET`
- 请求URL：`/api/venue/recommendation/similar/{venueId}`

### 请求参数

| 参数名 | 类型 | 是否必须 | 描述 |
| --- | --- | --- | --- |
| venueId | Long | 是 | 当前场馆ID (路径参数) |
| limit | Integer | 否 | 限制返回的记录数量，默认为4 |

### 响应信息

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2,
      "venueId": 3,
      "venueName": "羽毛球馆",
      "venueTypeName": "球类场馆",
      "coverImage": "https://example.com/badminton.jpg",
      "address": "北京市海淀区...",
      "basePrice": 80.00,
      "description": "专业羽毛球场地...",
      "recommendationScore": 7.5,
      "recommendationType": 0,
      "recommendationTypeName": "相似场馆",
      "recommendationReason": "与您正在浏览的场馆类似"
    }
    // 更多推荐记录...
  ]
}
```

## 4. 分页获取用户的推荐场馆列表

### 接口说明

分页获取登录用户的所有推荐场馆列表。

### 请求信息

- 请求方法：`GET`
- 请求URL：`/api/venue/recommendation/list`

### 请求参数

| 参数名 | 类型 | 是否必须 | 描述 |
| --- | --- | --- | --- |
| pageNum | Integer | 否 | 页码，默认为1 |
| pageSize | Integer | 否 | 每页记录数，默认为10 |

### 响应信息

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "venueId": 2,
        "venueName": "篮球馆",
        "venueTypeName": "球类场馆",
        "coverImage": "https://example.com/basketball.jpg",
        "address": "北京市朝阳区...",
        "basePrice": 100.00,
        "description": "专业篮球场地...",
        "recommendationScore": 8.7,
        "recommendationType": 0,
        "recommendationTypeName": "猜你喜欢",
        "recommendationReason": "根据您的浏览历史推荐"
      }
      // 更多推荐记录...
    ],
    "total": 24,
    "size": 10,
    "current": 1,
    "pages": 3
  }
}
```

## 5. 提交推荐反馈

### 接口说明

用户对推荐场馆提交反馈，如喜欢或不感兴趣。

### 请求信息

- 请求方法：`POST`
- 请求URL：`/api/venue/recommendation/feedback`
- 请求体格式：`application/json`

### 请求参数

| 参数名 | 类型 | 是否必须 | 描述 |
| --- | --- | --- | --- |
| recommendationId | Long | 否 | 推荐记录ID |
| venueId | Long | 是 | 场馆ID |
| feedbackType | Integer | 是 | 反馈类型: 0-喜欢, 1-不感兴趣, 2-不再推荐此类场馆 |
| reason | String | 否 | 反馈原因 |

### 响应信息

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 请求示例

```json
{
  "recommendationId": 1,
  "venueId": 2,
  "feedbackType": 0,
  "reason": "喜欢这种风格的场馆"
}
```

## 6. 标记推荐为已点击

### 接口说明

标记推荐记录为已点击，用于优化推荐算法。

### 请求信息

- 请求方法：`POST`
- 请求URL：`/api/venue/recommendation/click/{recommendationId}`

### 请求参数

| 参数名 | 类型 | 是否必须 | 描述 |
| --- | --- | --- | --- |
| recommendationId | Long | 是 | 推荐记录ID (路径参数) |

### 响应信息

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

## 状态码说明

| 状态码 | 说明 |
| --- | --- |
| 200 | 请求成功 |
| 401 | 未授权/未登录 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 注意事项

1. 所有需要用户身份的接口，请在请求头中携带有效的token
2. 推荐数据会定期更新，建议客户端定时刷新
3. 用户反馈会用于优化未来的推荐结果 