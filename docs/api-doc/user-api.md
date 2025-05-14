# 用户认证接口文档

## 1. 用户登录

### 接口描述

用户登录接口，验证用户名和密码，成功后返回 JWT token。

### 请求方法

POST

### 请求地址

`/api/auth/login`

### 权限

无需认证

### 注意事项

- 连续登录失败次数过多会记录，但目前未限制账号
- 返回的 token 需要在后续请求的 Header 中携带

### 请求参数

| 参数名   | 类型   | 必填 | 描述   |
| -------- | ------ | ---- | ------ |
| username | String | 是   | 用户名 |
| password | String | 是   | 密码   |

### 请求参数示例

```json
{
  "username": "admin",
  "password": "123456"
}
```

### 响应数据格式

| 参数名     | 类型    | 描述                       |
| ---------- | ------- | -------------------------- |
| code       | Integer | 状态码，200 表示成功       |
| message    | String  | 提示信息                   |
| data       | Object  | 返回数据                   |
| - token    | String  | JWT 令牌，用于后续接口认证 |
| - username | String  | 用户名                     |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "Bearer eyJhbGciOiJIUzI1NiJ9...",
    "username": "admin"
  }
}
```

失败响应：

```json
{
  "code": 500,
  "message": "用户名或密码错误",
  "data": null
}
```

## 2. 用户注册

### 接口描述

用户注册接口，创建新用户账号并返回 JWT token。

### 请求方法

POST

### 请求地址

`/api/auth/register`

### 权限

无需认证

### 注意事项

- 用户名、邮箱、手机号都必须是唯一的
- 注册成功后会自动登录并返回 token

### 请求参数

| 参数名          | 类型   | 必填 | 描述                           |
| --------------- | ------ | ---- | ------------------------------ |
| username        | String | 是   | 用户名，长度 4-20 个字符       |
| password        | String | 是   | 密码，长度 6-20 个字符         |
| confirmPassword | String | 是   | 确认密码，必须与密码一致       |
| email           | String | 否   | 电子邮箱，必须是有效的邮箱格式 |
| phone           | String | 否   | 手机号码，必须是有效的手机格式 |
| nickname        | String | 否   | 用户昵称，不填则默认为用户名   |

### 请求参数示例

```json
{
  "username": "testuser",
  "password": "password123",
  "confirmPassword": "password123",
  "email": "test@example.com",
  "phone": "13800138000",
  "nickname": "测试用户"
}
```

### 响应数据格式

| 参数名     | 类型    | 描述                       |
| ---------- | ------- | -------------------------- |
| code       | Integer | 状态码，200 表示成功       |
| message    | String  | 提示信息                   |
| data       | Object  | 返回数据                   |
| - token    | String  | JWT 令牌，用于后续接口认证 |
| - username | String  | 用户名                     |

### 响应示例

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "Bearer eyJhbGciOiJIUzI1NiJ9...",
    "username": "testuser"
  }
}
```

失败响应：

```json
{
  "code": 500,
  "message": "用户名已存在",
  "data": null
}
```

## 3. 如何使用 Token 进行接口认证

对于需要认证的接口，需要在请求头中携带 Token：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

未携带 Token 或 Token 无效时，会返回 401 错误：

```json
{
  "code": 401,
  "message": "未授权，请先登录",
  "data": null
}
```
