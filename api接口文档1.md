# 📘 视频平台 API 接口文档

------

## 一、基础说明

### 1. 基础路径

```
http://localhost:8080/video_streaming_system_war_exploded/api
```

### 2. 数据格式

- 请求格式：
  - `application/x-www-form-urlencoded`
  - `multipart/form-data`（文件上传）
- 返回格式：
  - `application/json`

### 3. 登录机制

- 使用 **Session（Cookie）**
- 登录后自动携带

------

## 二、统一返回格式

```json
{
  "success": true,
  "message": "操作成功",
  "data": {}
}
```

------

## 三、用户模块

### 1. 用户注册

- **接口地址**：`/user/register`
- **请求方法**：POST

#### 请求参数

| 参数名   | 类型    | 必填 | 说明       |
| -------- | ------- | ---- | ---------- |
| username | String  | 是   | 用户名     |
| password | String  | 是   | 密码       |
| isAdmin  | Boolean | 否   | 是否管理员 |

#### 返回示例

```json
{
  "success": true,
  "message": "注册成功"
}
```

------

### 2. 用户登录

- **接口地址**：`/user/login`
- **请求方法**：GET

#### 请求参数

| 参数名   | 类型   | 必填 | 说明   |
| -------- | ------ | ---- | ------ |
| username | String | 是   | 用户名 |
| password | String | 是   | 密码   |

#### 返回示例

```json
{
  "success": true,
  "message": "登录成功"
}
```

------

### 3. 获取用户信息

- **接口地址**：`/user/getUserInfo`
- **请求方法**：GET

#### 返回示例

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "admin",
    "isAdmin": true
  }
}
```

------

### 4. 退出登录

- **接口地址**：`/user/logout`
- **请求方法**：GET

#### 返回示例

```json
{
  "success": true,
  "message": "退出成功"
}
```

------

## 四、视频模块

### 1. 上传视频

- **接口地址**：`/video/upload`
- **请求方法**：POST
- **请求类型**：`multipart/form-data`

#### 请求参数

| 参数名      | 类型   | 必填 | 说明     |
| ----------- | ------ | ---- | -------- |
| title       | String | 是   | 视频标题 |
| description | String | 否   | 视频描述 |
| videoFile   | File   | 是   | 视频文件 |

#### 返回示例

```json
{
  "success": true,
  "message": "上传成功"
}
```

------

### 2. 获取视频列表

- **接口地址**：`/video/getAllVideos`
- **请求方法**：GET

#### 返回示例

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "测试视频",
      "url": "xxx.mp4",
      "description": "示例",
      "userId": 1
    }
  ]
}
```

------

### 3. 获取单个视频

- **接口地址**：`/video/getVideo`
- **请求方法**：GET

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| ------ | ---- | ---- | ------ |
| id     | int  | 是   | 视频ID |

#### 返回示例

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "测试视频",
    "url": "xxx.mp4",
    "description": "示例"
  }
}
```

------

### 4. 删除视频

- **接口地址**：`/video/deleteVideo`
- **请求方法**：GET

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| ------ | ---- | ---- | ------ |
| id     | int  | 是   | 视频ID |

#### 返回示例

```json
{
  "success": true,
  "message": "删除成功"
}
```

------

## 五、评论模块

### 1. 添加评论

- **接口地址**：`/comment/addComment`
- **请求方法**：GET

#### 请求参数

| 参数名  | 类型   | 必填 | 说明     |
| ------- | ------ | ---- | -------- |
| videoId | int    | 是   | 视频ID   |
| content | String | 是   | 评论内容 |

#### 返回示例

```json
{
  "success": true,
  "message": "评论成功"
}
```

------

### 2. 获取评论列表

- **接口地址**：`/comment/getCommentsByVideoId`
- **请求方法**：GET

#### 请求参数

| 参数名  | 类型 | 必填 | 说明   |
| ------- | ---- | ---- | ------ |
| videoId | int  | 是   | 视频ID |

#### 返回示例

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 2,
      "username": "test",
      "content": "很好看",
      "createdAt": "2026-04-20"
    }
  ]
}
```

------

### 3. 删除评论

- **接口地址**：`/comment/deleteComment`
- **请求方法**：GET

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| ------ | ---- | ---- | ------ |
| id     | int  | 是   | 评论ID |

#### 返回示例

```json
{
  "success": true,
  "message": "删除成功"
}
```

------

## 六、总结

本系统提供以下功能接口：

- 用户模块：注册、登录、获取用户、退出
- 视频模块：上传、查询、删除
- 评论模块：增删查

### 系统特点

- 基于 Session 登录认证
- 支持文件上传（视频）
- 实现权限控制（用户 / 作者 / 管理员）
- 使用 JSON 统一返回格式

------