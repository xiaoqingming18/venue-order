# 用户收藏场馆 API 文档

本文档描述了用户收藏场馆相关的 API 接口。

## 数据表设计

```sql
-- 用户收藏场馆表
DROP TABLE IF EXISTS `user_favorite_venue`;
CREATE TABLE `user_favorite_venue`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `notes` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收藏备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_venue`(`user_id` ASC, `venue_id` ASC) USING BTREE COMMENT '用户和场馆的唯一组合索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏场馆表' ROW_FORMAT = DYNAMIC;
```

## API 接口说明

### 1. 添加收藏

- 请求方式：POST
- 请求URL：`/api/user/favorite/add`
- 请求参数：

| 参数名  | 类型   | 必填 | 说明     |
| ------- | ------ | ---- | -------- |
| venueId | Long   | 是   | 场馆ID   |
| notes   | String | 否   | 收藏备注 |

- 请求示例：

```json
{
  "venueId": 1,
  "notes": "这个场馆很不错"
}
```

- 响应参数：

| 参数名     | 类型     | 说明       |
| ---------- | -------- | ---------- |
| code       | Integer  | 状态码     |
| message    | String   | 响应消息   |
| data       | Object   | 收藏记录   |

- 响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 10001,
    "venueId": 1,
    "notes": "这个场馆很不错",
    "createdAt": "2025-05-16T12:00:00",
    "updatedAt": "2025-05-16T12:00:00"
  }
}
```

### 2. 取消收藏

- 请求方式：DELETE
- 请求URL：`/api/user/favorite/remove/{venueId}`
- 请求参数：

| 参数名  | 类型 | 必填 | 说明     |
| ------- | ---- | ---- | -------- |
| venueId | Long | 是   | 场馆ID   |

- 响应参数：

| 参数名     | 类型     | 说明       |
| ---------- | -------- | ---------- |
| code       | Integer  | 状态码     |
| message    | String   | 响应消息   |
| data       | Boolean  | 是否成功   |

- 响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 3. 通过收藏ID取消收藏

- 请求方式：DELETE
- 请求URL：`/api/user/favorite/{favoriteId}`
- 请求参数：

| 参数名     | 类型 | 必填 | 说明     |
| ---------- | ---- | ---- | -------- |
| favoriteId | Long | 是   | 收藏ID   |

- 响应参数：

| 参数名     | 类型     | 说明       |
| ---------- | -------- | ---------- |
| code       | Integer  | 状态码     |
| message    | String   | 响应消息   |
| data       | Boolean  | 是否成功   |

- 响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 4. 更新收藏备注

- 请求方式：PUT
- 请求URL：`/api/user/favorite/{favoriteId}/notes`
- 请求参数：

| 参数名     | 类型   | 必填 | 说明     |
| ---------- | ------ | ---- | -------- |
| favoriteId | Long   | 是   | 收藏ID   |
| notes      | String | 是   | 收藏备注 |

- 请求示例：

```json
{
  "notes": "更新后的收藏备注"
}
```

- 响应参数：

| 参数名     | 类型     | 说明       |
| ---------- | -------- | ---------- |
| code       | Integer  | 状态码     |
| message    | String   | 响应消息   |
| data       | Boolean  | 是否成功   |

- 响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 5. 获取收藏列表

- 请求方式：GET
- 请求URL：`/api/user/favorite/list`
- 请求参数：无
- 响应参数：

| 参数名     | 类型     | 说明       |
| ---------- | -------- | ---------- |
| code       | Integer  | 状态码     |
| message    | String   | 响应消息   |
| data       | Array    | 收藏列表   |

- 响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 10001,
      "venueId": 1,
      "venueName": "羽毛球一号馆",
      "coverImage": "http://example.com/image.jpg",
      "address": "测试地址",
      "venueTypeName": "羽毛球馆",
      "basePrice": "15.00",
      "notes": "这个场馆很不错",
      "createdAt": "2025-05-16T12:00:00"
    },
    {
      "id": 2,
      "userId": 10001,
      "venueId": 2,
      "venueName": "狗狗羽毛球馆",
      "coverImage": "http://example.com/image2.jpg",
      "address": "狗狗场馆地址",
      "venueTypeName": "羽毛球馆",
      "basePrice": "30.00",
      "notes": "这个场馆也不错",
      "createdAt": "2025-05-17T12:00:00"
    }
  ]
}
```

### 6. 分页获取收藏列表

- 请求方式：GET
- 请求URL：`/api/user/favorite/page`
- 请求参数：

| 参数名   | 类型    | 必填 | 说明     | 默认值 |
| -------- | ------- | ---- | -------- | ------ |
| pageNum  | Integer | 否   | 页码     | 1      |
| pageSize | Integer | 否   | 每页大小 | 10     |

- 响应参数：

| 参数名     | 类型     | 说明           |
| ---------- | -------- | -------------- |
| code       | Integer  | 状态码         |
| message    | String   | 响应消息       |
| data       | Object   | 分页收藏列表   |

- 响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 10001,
        "venueId": 1,
        "venueName": "羽毛球一号馆",
        "coverImage": "http://example.com/image.jpg",
        "address": "测试地址",
        "venueTypeName": "羽毛球馆",
        "basePrice": "15.00",
        "notes": "这个场馆很不错",
        "createdAt": "2025-05-16T12:00:00"
      },
      {
        "id": 2,
        "userId": 10001,
        "venueId": 2,
        "venueName": "狗狗羽毛球馆",
        "coverImage": "http://example.com/image2.jpg",
        "address": "狗狗场馆地址",
        "venueTypeName": "羽毛球馆",
        "basePrice": "30.00",
        "notes": "这个场馆也不错",
        "createdAt": "2025-05-17T12:00:00"
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 7. 获取收藏详情

- 请求方式：GET
- 请求URL：`/api/user/favorite/{favoriteId}`
- 请求参数：

| 参数名     | 类型 | 必填 | 说明     |
| ---------- | ---- | ---- | -------- |
| favoriteId | Long | 是   | 收藏ID   |

- 响应参数：

| 参数名     | 类型     | 说明       |
| ---------- | -------- | ---------- |
| code       | Integer  | 状态码     |
| message    | String   | 响应消息   |
| data       | Object   | 收藏详情   |

- 响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 10001,
    "venueId": 1,
    "venueName": "羽毛球一号馆",
    "coverImage": "http://example.com/image.jpg",
    "address": "测试地址",
    "venueTypeName": "羽毛球馆",
    "basePrice": "15.00",
    "notes": "这个场馆很不错",
    "createdAt": "2025-05-16T12:00:00"
  }
}
```

### 8. 检查是否已收藏

- 请求方式：GET
- 请求URL：`/api/user/favorite/check/{venueId}`
- 请求参数：

| 参数名  | 类型 | 必填 | 说明     |
| ------- | ---- | ---- | -------- |
| venueId | Long | 是   | 场馆ID   |

- 响应参数：

| 参数名     | 类型     | 说明         |
| ---------- | -------- | ------------ |
| code       | Integer  | 状态码       |
| message    | String   | 响应消息     |
| data       | Boolean  | 是否已收藏   |

- 响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

## 错误码说明

| 错误码 | 说明                       |
| ------ | -------------------------- |
| 200    | 操作成功                   |
| 401    | 用户未登录或登录已过期     |
| 403    | 无权访问                   |
| 500    | 服务器内部错误             | 