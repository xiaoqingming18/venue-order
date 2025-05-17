# 用户反馈 API 文档

本文档描述了用户反馈相关的 API 接口。

## 数据表设计

```sql
-- 用户反馈表
CREATE TABLE `user_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '反馈类型：1-问题反馈，2-功能建议，3-投诉，4-其他',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '反馈标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '反馈内容',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系方式',
  `images` json NULL COMMENT '反馈图片URL列表',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已回复，3-已关闭',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_status` (`status` ASC) USING BTREE,
  INDEX `idx_created_at` (`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户反馈表' ROW_FORMAT = DYNAMIC;

-- 反馈回复表
CREATE TABLE `feedback_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `feedback_id` bigint NOT NULL COMMENT '反馈ID',
  `admin_id` bigint NOT NULL COMMENT '管理员ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回复内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_feedback_id` (`feedback_id` ASC) USING BTREE,
  INDEX `idx_admin_id` (`admin_id` ASC) USING BTREE,
  CONSTRAINT `fk_reply_feedback` FOREIGN KEY (`feedback_id`) REFERENCES `user_feedback` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_reply_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '反馈回复表' ROW_FORMAT = DYNAMIC;

-- 用户消息表
CREATE TABLE `user_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '消息类型：1-系统通知，2-订单消息，3-反馈回复，4-其他',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID，根据消息类型关联到相应记录',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_type` (`type` ASC) USING BTREE,
  INDEX `idx_is_read` (`is_read` ASC) USING BTREE,
  INDEX `idx_created_at` (`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户消息表' ROW_FORMAT = DYNAMIC;
```

## API 接口说明

### 1. 提交反馈

- 请求方式：POST
- 请求URL：`/api/user/feedback/submit`
- 请求参数：

| 参数名  | 类型   | 必填 | 说明                                                |
| ------- | ------ | ---- | --------------------------------------------------- |
| type    | Int    | 是   | 反馈类型：1-问题反馈，2-功能建议，3-投诉，4-其他    |
| title   | String | 是   | 反馈标题                                            |
| content | String | 是   | 反馈内容                                            |
| contact | String | 否   | 联系方式                                            |
| images  | Array  | 否   | 图片URL数组                                         |

- 请求示例：

```json
{
  "type": 1,
  "title": "APP闪退问题",
  "content": "在使用场馆预约功能时，APP经常闪退",
  "contact": "13800138000",
  "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"]
}
```

- 响应参数：

| 参数名  | 类型    | 说明       |
| ------- | ------- | ---------- |
| code    | Integer | 状态码     |
| message | String  | 响应消息   |
| data    | Long    | 反馈ID     |

- 响应示例：

```json
{
  "code": 200,
  "message": "反馈提交成功",
  "data": 1
}
```

### 2. 获取反馈详情

- 请求方式：GET
- 请求URL：`/api/user/feedback/detail/{id}`
- 请求参数：

| 参数名 | 类型 | 必填 | 说明   |
| ------ | ---- | ---- | ------ |
| id     | Long | 是   | 反馈ID |

- 响应参数：

| 参数名     | 类型    | 说明                                             |
| ---------- | ------- | ------------------------------------------------ |
| code       | Integer | 状态码                                           |
| message    | String  | 响应消息                                         |
| data       | Object  | 反馈详情对象                                     |
| - id       | Long    | 反馈ID                                           |
| - userId   | Long    | 用户ID                                           |
| - username | String  | 用户名                                           |
| - nickname | String  | 用户昵称                                         |
| - type     | Integer | 反馈类型：1-问题反馈，2-功能建议，3-投诉，4-其他 |
| - typeName | String  | 反馈类型名称                                     |
| - title    | String  | 反馈标题                                         |
| - content  | String  | 反馈内容                                         |
| - contact  | String  | 联系方式                                         |
| - images   | Array   | 图片URL数组                                      |
| - status   | Integer | 状态：0-待处理，1-处理中，2-已回复，3-已关闭     |
| - statusName | String | 状态名称                                        |
| - createdAt | String | 创建时间                                         |
| - updatedAt | String | 更新时间                                         |
| - replies   | Array  | 回复列表                                         |

- 响应示例：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "userId": 10001,
    "username": "user123",
    "nickname": "用户昵称",
    "type": 1,
    "typeName": "问题反馈",
    "title": "APP闪退问题",
    "content": "在使用场馆预约功能时，APP经常闪退",
    "contact": "13800138000",
    "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"],
    "status": 2,
    "statusName": "已回复",
    "createdAt": "2023-05-20 10:30:00",
    "updatedAt": "2023-05-21 14:20:00",
    "replies": [
      {
        "id": 1,
        "feedbackId": 1,
        "adminId": 10000,
        "adminUsername": "admin",
        "adminNickname": "管理员",
        "content": "感谢您的反馈，我们已经定位到问题，将在下个版本修复",
        "createdAt": "2023-05-21 14:20:00"
      }
    ]
  }
}
```

### 3. 获取用户反馈列表

- 请求方式：GET
- 请求URL：`/api/user/feedback/list`
- 请求参数：

| 参数名   | 类型    | 必填 | 说明     |
| -------- | ------- | ---- | -------- |
| pageNum  | Integer | 否   | 页码，默认1 |
| pageSize | Integer | 否   | 每页大小，默认10 |

- 响应参数：

| 参数名       | 类型    | 说明             |
| ------------ | ------- | ---------------- |
| code         | Integer | 状态码           |
| message      | String  | 响应消息         |
| data         | Object  | 分页数据对象     |
| - total      | Long    | 总记录数         |
| - pages      | Integer | 总页数           |
| - pageNum    | Integer | 当前页码         |
| - pageSize   | Integer | 每页大小         |
| - records    | Array   | 反馈记录列表     |

- 响应示例：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 2,
    "pages": 1,
    "pageNum": 1,
    "pageSize": 10,
    "records": [
      {
        "id": 2,
        "userId": 10001,
        "username": "user123",
        "nickname": "用户昵称",
        "type": 2,
        "typeName": "功能建议",
        "title": "希望增加团队预约功能",
        "content": "希望能增加团队预约功能，方便组织团队活动",
        "contact": "13800138000",
        "images": [],
        "status": 0,
        "statusName": "待处理",
        "createdAt": "2023-05-22 09:15:00",
        "updatedAt": "2023-05-22 09:15:00"
      },
      {
        "id": 1,
        "userId": 10001,
        "username": "user123",
        "nickname": "用户昵称",
        "type": 1,
        "typeName": "问题反馈",
        "title": "APP闪退问题",
        "content": "在使用场馆预约功能时，APP经常闪退",
        "contact": "13800138000",
        "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"],
        "status": 2,
        "statusName": "已回复",
        "createdAt": "2023-05-20 10:30:00",
        "updatedAt": "2023-05-21 14:20:00"
      }
    ]
  }
}
```

### 4. 获取反馈回复列表

- 请求方式：GET
- 请求URL：`/api/feedback/reply/list/{feedbackId}`
- 请求参数：

| 参数名    | 类型 | 必填 | 说明   |
| --------- | ---- | ---- | ------ |
| feedbackId | Long | 是   | 反馈ID |

- 响应参数：

| 参数名         | 类型    | 说明       |
| -------------- | ------- | ---------- |
| code           | Integer | 状态码     |
| message        | String  | 响应消息   |
| data           | Array   | 回复列表   |
| - id           | Long    | 回复ID     |
| - feedbackId   | Long    | 反馈ID     |
| - adminId      | Long    | 管理员ID   |
| - adminUsername | String | 管理员用户名 |
| - adminNickname | String | 管理员昵称 |
| - content      | String  | 回复内容   |
| - createdAt    | String  | 创建时间   |

- 响应示例：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "feedbackId": 1,
      "adminId": 10000,
      "adminUsername": "admin",
      "adminNickname": "管理员",
      "content": "感谢您的反馈，我们已经定位到问题，将在下个版本修复",
      "createdAt": "2023-05-21 14:20:00"
    }
  ]
}
```

### 5. 获取用户消息列表

- 请求方式：GET
- 请求URL：`/api/user/message/list`
- 请求参数：

| 参数名   | 类型    | 必填 | 说明     |
| -------- | ------- | ---- | -------- |
| pageNum  | Integer | 否   | 页码，默认1 |
| pageSize | Integer | 否   | 每页大小，默认10 |

- 响应参数：

| 参数名       | 类型    | 说明             |
| ------------ | ------- | ---------------- |
| code         | Integer | 状态码           |
| message      | String  | 响应消息         |
| data         | Object  | 分页数据对象     |
| - total      | Long    | 总记录数         |
| - pages      | Integer | 总页数           |
| - pageNum    | Integer | 当前页码         |
| - pageSize   | Integer | 每页大小         |
| - records    | Array   | 消息记录列表     |

- 响应示例：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 2,
    "pages": 1,
    "pageNum": 1,
    "pageSize": 10,
    "records": [
      {
        "id": 2,
        "userId": 10001,
        "type": 3,
        "typeName": "反馈回复",
        "title": "您的反馈已收到回复",
        "content": "您提交的"APP闪退问题"反馈已收到回复，请查看详情。",
        "relatedId": 1,
        "isRead": 0,
        "createdAt": "2023-05-21 14:20:00"
      },
      {
        "id": 1,
        "userId": 10001,
        "type": 1,
        "typeName": "系统通知",
        "title": "欢迎使用场馆预约系统",
        "content": "感谢您使用我们的场馆预约系统，有任何问题可以通过反馈功能告诉我们。",
        "relatedId": null,
        "isRead": 1,
        "createdAt": "2023-05-15 10:00:00"
      }
    ]
  }
}
```

### 6. 获取未读消息数量

- 请求方式：GET
- 请求URL：`/api/user/message/unread-count`
- 响应参数：

| 参数名  | 类型    | 说明       |
| ------- | ------- | ---------- |
| code    | Integer | 状态码     |
| message | String  | 响应消息   |
| data    | Integer | 未读消息数 |

- 响应示例：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": 3
}
```

### 7. 标记消息为已读

- 请求方式：POST
- 请求URL：`/api/user/message/mark-read/{messageId}`
- 请求参数：

| 参数名    | 类型 | 必填 | 说明   |
| --------- | ---- | ---- | ------ |
| messageId | Long | 是   | 消息ID |

- 响应参数：

| 参数名  | 类型    | 说明       |
| ------- | ------- | ---------- |
| code    | Integer | 状态码     |
| message | String  | 响应消息   |
| data    | Boolean | 是否成功   |

- 响应示例：

```json
{
  "code": 200,
  "message": "标记成功",
  "data": true
}
```

### 8. 标记所有消息为已读

- 请求方式：POST
- 请求URL：`/api/user/message/mark-all-read`
- 响应参数：

| 参数名  | 类型    | 说明       |
| ------- | ------- | ---------- |
| code    | Integer | 状态码     |
| message | String  | 响应消息   |
| data    | Boolean | 是否成功   |

- 响应示例：

```json
{
  "code": 200,
  "message": "标记全部已读成功",
  "data": true
}
```

### 9. 删除用户消息

- 请求方式：DELETE
- 请求URL：`/api/user/message/delete/{messageId}`
- 请求参数：

| 参数名    | 类型 | 必填 | 说明   |
| --------- | ---- | ---- | ------ |
| messageId | Long | 是   | 消息ID |

- 响应参数：

| 参数名  | 类型    | 说明       |
| ------- | ------- | ---------- |
| code    | Integer | 状态码     |
| message | String  | 响应消息   |
| data    | Boolean | 是否成功   |

- 响应示例：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": true
}
``` 