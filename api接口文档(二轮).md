# 📘 视频平台 API 接口文档

------

## 一、基础说明

### 1. 基础路径

text

```
http://localhost:8080/video_streaming_system_war_exploded/api
```



### 2. 数据格式

- 请求格式：
  - `application/x-www-form-urlencoded`
  - `multipart/form-data`（文件上传）
- 返回格式：
  - `application/json`

### 3. 认证机制

- 使用 **Token 认证**

- 登录后获取 Token，请求时在请求头中携带：

  text

  ```
  Authorization: Bearer <token>
  ```

  

- Token 有效期 30 分钟（Redis 存储）

------

## 二、统一返回格式

json

```
{
  "success": true,
  "message": "操作成功",
  "data": {}
}
```



失败时 `success` 为 false，`message` 包含错误原因，如：

json

```
{
  "success": false,
  "message": "请先登录"
}
```



------

## 三、用户模块

### 1. 用户注册

- **接口地址**：`/user/register`
- **请求方法**：GET

#### 请求参数

| 参数名   | 类型   | 必填 | 说明   |
| :------- | :----- | :--- | :----- |
| username | String | 是   | 用户名 |
| password | String | 是   | 密码   |

#### 返回示例

json

```
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
| :------- | :----- | :--- | :----- |
| username | String | 是   | 用户名 |
| password | String | 是   | 密码   |

#### 返回示例

json

```
{
  "success": true,
  "message": "登录成功",
  "token": "admin_xxxx-xxxx-xxxx",
  "isAdmin": true
}
```



------

### 3. 获取当前用户信息

- **接口地址**：`/user/getUserInfo`
- **请求方法**：GET
- **认证**：需要

#### 返回示例

json

```
{
  "success": true,
  "data": {
    "id": 1,
    "username": "admin",
    "roleId": 1
  }
}
```



------

### 4. 获取个人信息（主页用）

- **接口地址**：`/user/getProfile`
- **请求方法**：GET
- **认证**：需要

#### 返回示例

json

```
{
  "success": true,
  "data": {
    "id": 1,
    "username": "admin",
    "roleId": 1,
    "bio": "我的简介"
  }
}
```



------

### 5. 修改个人简介

- **接口地址**：`/user/updateBio`
- **请求方法**：POST
- **认证**：需要

#### 请求参数

| 参数名 | 类型   | 必填 | 说明         |
| :----- | :----- | :--- | :----------- |
| bio    | String | 是   | 个人简介内容 |

#### 返回示例

json

```
{
  "success": true,
  "message": "修改成功"
}
```



------

### 6. 查看他人主页

- **接口地址**：`/user/viewProfile`
- **请求方法**：GET
- **认证**：需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| userId | int  | 是   | 用户ID |

#### 返回示例

json

```
{
  "success": true,
  "data": {
    "id": 6,
    "username": "cat",
    "roleId": 2,
    "bio": "我不想吃菜",
    "isFollowed": false,
    "isMe": false
  }
}
```



------

### 7. 用户列表

- **接口地址**：`/user/listUsers`
- **请求方法**：GET
- **认证**：需要

#### 返回示例

json

```
{
  "success": true,
  "data": [
    {
      "id": 4,
      "username": "admin",
      "isFollowed": true,
      "isMe": false
    }
  ]
}
```



------

### 8. 退出登录

- **接口地址**：`/user/logout`
- **请求方法**：GET
- **认证**：需要

#### 返回示例

json

```
{
  "success": true,
  "message": "已退出登录"
}
```



------

## 四、管理员模块

### 1. 申请成为管理员

- **接口地址**：`/admin/apply`
- **请求方法**：GET
- **认证**：需要

#### 返回示例

json

```
{
  "success": true,
  "message": "申请成功"
}
```



------

### 2. 获取申请列表

- **接口地址**：`/admin/getlist`
- **请求方法**：GET
- **认证**：需要（管理员）

#### 返回示例

json

```
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 5,
      "username": "dog"
    }
  ]
}
```



------

### 3. 审批通过

- **接口地址**：`/admin/approve`
- **请求方法**：GET
- **认证**：需要（管理员）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| id     | int  | 是   | 申请ID |
| userId | int  | 是   | 用户ID |

#### 返回示例

json

```
{
  "success": true,
  "message": "审批成功"
}
```



------

### 4. 拒绝申请

- **接口地址**：`/admin/reject`
- **请求方法**：GET
- **认证**：需要（管理员）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| id     | int  | 是   | 申请ID |

#### 返回示例

json

```
{
  "success": true,
  "message": "已拒绝"
}
```



------

## 五、视频模块

### 1. 上传视频

- **接口地址**：`/video/upload`
- **请求方法**：POST
- **请求类型**：`multipart/form-data`
- **认证**：需要

#### 请求参数

| 参数名      | 类型   | 必填 | 说明                   |
| :---------- | :----- | :--- | :--------------------- |
| title       | String | 是   | 视频标题               |
| description | String | 否   | 视频描述               |
| category    | String | 否   | 分区（美食/探店/科技） |
| tags        | String | 否   | 自定义标签（逗号分隔） |
| couponId    | int    | 否   | 关联优惠券活动ID       |
| videoFile   | File   | 是   | 视频文件               |

#### 返回示例

json

```
{
  "success": true,
  "message": "上传成功"
}
```



------

### 2. 获取视频列表（全部）

- **接口地址**：`/video/getAllVideos`
- **请求方法**：GET

#### 返回示例

json

```
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "测试视频",
      "url": "videos/xxx.mp4",
      "description": "示例",
      "userId": 1,
      "authorName": "dog",
      "category": "美食",
      "tags": "美食,探店"
    }
  ]
}
```



------

### 3. 获取单个视频

- **接口地址**：`/video/getVideo`
- **请求方法**：GET
- **认证**：可选（登录后可获取点赞、收藏、关注状态）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| id     | int  | 是   | 视频ID |

#### 返回示例

json

```
{
  "success": true,
  "data": {
    "id": 1,
    "title": "测试视频",
    "url": "videos/xxx.mp4",
    "description": "示例",
    "userId": 5,
    "authorName": "dog",
    "category": "美食",
    "tags": "美食,探店",
    "likeCount": 3,
    "liked": false,
    "favorited": false,
    "followed": true,
    "followerCount": 10,
    "coupon": {
      "id": 1,
      "title": "满100减20",
      "remain": 50,
      "stock": 100,
      "grabbed": false
    }
  }
}
```



------

### 4. 按分类查询视频

- **接口地址**：`/video/listByCategory`
- **请求方法**：GET

#### 请求参数

| 参数名   | 类型   | 必填 | 说明     |
| :------- | :----- | :--- | :------- |
| category | String | 是   | 分区名称 |

#### 返回示例

json

```
{
  "success": true,
  "data": [ ... ]
}
```



------

### 5. 按标签查询视频

- **接口地址**：`/video/listByTags`
- **请求方法**：GET

#### 请求参数

| 参数名 | 类型   | 必填 | 说明             |
| :----- | :----- | :--- | :--------------- |
| tags   | String | 是   | 标签（逗号分隔） |

#### 返回示例

json

```
{
  "success": true,
  "data": [ ... ]
}
```



------

### 6. 获取所有标签和分类

- **接口地址**：`/video/getAllTags`
- **请求方法**：GET

#### 返回示例

json

```
{
  "success": true,
  "data": ["美食", "探店", "教程"]
}
```



------

### 7. 删除视频

- **接口地址**：`/video/deleteVideo`
- **请求方法**：GET
- **认证**：需要（作者或管理员）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| id     | int  | 是   | 视频ID |

#### 返回示例

json

```
{
  "success": true,
  "message": "删除成功"
}
```



------

## 六、动态模块

### 1. 发布动态

- **接口地址**：`/post/create`
- **请求方法**：POST
- **认证**：需要

#### 请求参数

| 参数名  | 类型   | 必填 | 说明     |
| :------ | :----- | :--- | :------- |
| content | String | 是   | 动态内容 |

#### 返回示例

json

```
{
  "success": true,
  "message": "发布成功"
}
```



------

### 2. 查看用户动态列表

- **接口地址**：`/post/list`
- **请求方法**：GET

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| userId | int  | 是   | 用户ID |

#### 返回示例

json

```
{
  "success": true,
  "data": [
    {
      "id": 1,
      "content": "今天天气真好",
      "userId": 5,
      "authorName": "dog",
      "createdAt": "2026-05-09 00:26:14",
      "favorited": false
    }
  ]
}
```



------

### 3. 获取单条动态

- **接口地址**：`/post/get`
- **请求方法**：GET

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| id     | int  | 是   | 动态ID |

#### 返回示例

json

```
{
  "success": true,
  "data": {
    "id": 1,
    "content": "今天天气真好",
    "userId": 5,
    "authorName": "dog",
    "createdAt": "2026-05-09 00:26:14"
  }
}
```



------

### 4. 删除动态

- **接口地址**：`/post/delete`
- **请求方法**：GET
- **认证**：需要（作者）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| id     | int  | 是   | 动态ID |

#### 返回示例

json

```
{
  "success": true,
  "message": "删除成功"
}
```



------

## 七、评论模块

### 1. 添加视频评论

- **接口地址**：`/comment/addComment`
- **请求方法**：GET
- **认证**：需要

#### 请求参数

| 参数名  | 类型   | 必填 | 说明     |
| :------ | :----- | :--- | :------- |
| videoId | int    | 是   | 视频ID   |
| content | String | 是   | 评论内容 |

#### 返回示例

json

```
{
  "success": true,
  "message": "评论成功"
}
```



------

### 2. 添加动态评论

- **接口地址**：`/comment/addPostComment`
- **请求方法**：GET
- **认证**：需要

#### 请求参数

| 参数名  | 类型   | 必填 | 说明     |
| :------ | :----- | :--- | :------- |
| postId  | int    | 是   | 动态ID   |
| content | String | 是   | 评论内容 |

#### 返回示例

json

```
{
  "success": true,
  "message": "评论成功"
}
```



------

### 3. 获取视频评论

- **接口地址**：`/comment/getCommentsByVideoId`
- **请求方法**：GET

#### 请求参数

| 参数名  | 类型   | 必填 | 说明                             |
| :------ | :----- | :--- | :------------------------------- |
| videoId | int    | 是   | 视频ID                           |
| sort    | String | 否   | 排序方式：time / hot（默认time） |

#### 返回示例

json

```
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 2,
      "username": "test",
      "content": "很好看",
      "likeCount": 5,
      "liked": true,
      "canDelete": false,
      "createdAt": "2026-04-20"
    }
  ]
}
```



------

### 4. 获取动态评论

- **接口地址**：`/comment/getPostComments`
- **请求方法**：GET

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| postId | int  | 是   | 动态ID |

#### 返回示例

json

```
{
  "success": true,
  "data": [ ... ]
}
```



------

### 5. 删除评论

- **接口地址**：`/comment/deleteComment`
- **请求方法**：GET
- **认证**：需要（作者/管理员）

#### 请求参数

| 参数名 | 类型 | 必填 | 说明   |
| :----- | :--- | :--- | :----- |
| id     | int  | 是   | 评论ID |

#### 返回示例

json

```
{
  "success": true,
  "message": "删除成功"
}
```



------

## 八、点赞模块

### 1. 视频点赞/取消

- **接口地址**：`/like/toggleVideoLike`
- **请求方法**：GET
- **认证**：需要

#### 请求参数

| 参数名  | 类型 | 必填 | 说明   |
| :------ | :--- | :--- | :----- |
| videoId | int  | 是   | 视频ID |

#### 返回示例

json

```
{
  "success": true,
  "liked": true,
  "likeCount": 10,
  "message": "点赞成功"
}
```



------

### 2. 评论点赞/取消

- **接口地址**：`/like/toggleCommentLike`
- **请求方法**：GET
- **认证**：需要

#### 请求参数

| 参数名    | 类型 | 必填 | 说明   |
| :-------- | :--- | :--- | :----- |
| commentId | int  | 是   | 评论ID |

#### 返回示例

json

```
{
  "success": true,
  "liked": true,
  "likeCount": 5,
  "message": "点赞成功"
}
```



------

## 九、收藏模块

### 1. 收藏/取消收藏

- **接口地址**：`/favorite/toggle`
- **请求方法**：GET
- **认证**：需要

#### 请求参数

| 参数名   | 类型   | 必填 | 说明               |
| :------- | :----- | :--- | :----------------- |
| type     | String | 是   | 类型（video/post） |
| targetId | int    | 是   | 目标ID             |

#### 返回示例

json

```
{
  "success": true,
  "message": "已收藏",
  "favorited": true
}
```



------

### 2. 收藏列表

- **接口地址**：`/favorite/list`
- **请求方法**：GET
- **认证**：需要

#### 返回示例

json

```
{
  "success": true,
  "data": [
    {
      "type": "video",
      "id": 1,
      "title": "测试视频",
      "authorName": "dog"
    },
    {
      "type": "post",
      "id": 3,
      "content": "动态内容",
      "authorName": "cat"
    }
  ]
}
```



------

## 十、关注模块

### 1. 关注/取消关注

- **接口地址**：`/follow/toggleFollow`
- **请求方法**：GET
- **认证**：需要

#### 请求参数

| 参数名 | 类型 | 必填 | 说明       |
| :----- | :--- | :--- | :--------- |
| userId | int  | 是   | 目标用户ID |

#### 返回示例

json

```
{
  "success": true,
  "followed": true,
  "followerCount": 12
}
```



------

### 2. 粉丝列表

- **接口地址**：`/follow/getFollowers`
- **请求方法**：GET
- **认证**：需要

#### 返回示例

json

```
{
  "success": true,
  "data": [
    { "id": 5, "username": "dog" }
  ]
}
```



------

### 3. 关注列表

- **接口地址**：`/follow/getFollowing`
- **请求方法**：GET
- **认证**：需要

#### 返回示例

json

```
{
  "success": true,
  "data": [
    { "id": 6, "username": "cat" }
  ]
}
```



------

## 十一、Feed 流模块

### 1. 拉取关注动态

- **接口地址**：`/feed/pull`
- **请求方法**：GET
- **认证**：需要

#### 请求参数

| 参数名   | 类型   | 必填 | 说明                                   |
| :------- | :----- | :--- | :------------------------------------- |
| limit    | int    | 否   | 每页条数（默认10）                     |
| lastTime | String | 否   | 游标时间（上一页最后一条的 createdAt） |

#### 返回示例

json

```
{
  "success": true,
  "data": {
    "items": [
      {
        "type": "video",
        "id": 1,
        "title": "视频标题",
        "authorName": "dog",
        "createdAt": "2026-05-10 08:30:29"
      },
      {
        "type": "post",
        "id": 3,
        "content": "动态内容",
        "authorName": "cat",
        "createdAt": "2026-05-10 08:15:03"
      }
    ],
    "nextCursor": "2026-05-10 08:15:03",
    "hasMore": true
  }
}
```



------

## 十二、优惠券模块

### 1. 优惠券列表（有效活动）

- **接口地址**：`/coupon/list`
- **请求方法**：GET
- **认证**：需要

#### 返回示例

json

```
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "满100减20",
      "stock": 100,
      "remain": 95,
      "startTime": "2026-05-01 00:00:00",
      "endTime": "2026-12-31 23:59:59",
      "grabbed": false,
      "code": null
    }
  ]
}
```



------

### 2. 抢购优惠券

- **接口地址**：`/coupon/grab`
- **请求方法**：GET
- **认证**：需要
- **限流**：每秒最多 3 次/用户

#### 请求参数

| 参数名   | 类型 | 必填 | 说明     |
| :------- | :--- | :--- | :------- |
| couponId | int  | 是   | 优惠券ID |

#### 返回示例（成功）

json

```
{
  "success": true,
  "message": "抢购成功，券码：ABC123DEF456"
}
```



#### 返回示例（库存不足）

json

```
{
  "success": false,
  "message": "优惠券已被抢光"
}
```



------

### 3. 管理员创建优惠券

- **接口地址**：`/coupon/create`
- **请求方法**：GET
- **认证**：需要（管理员）

#### 请求参数

| 参数名 | 类型   | 必填 | 说明     |
| :----- | :----- | :--- | :------- |
| title  | String | 是   | 活动标题 |
| stock  | int    | 是   | 库存数量 |

#### 返回示例

json

```
{
  "success": true,
  "message": "创建成功，ID=1"
}
```



------

### 4. 预热库存

- **接口地址**：`/coupon/preheat`
- **请求方法**：GET
- **认证**：需要（管理员）

#### 请求参数

| 参数名   | 类型 | 必填 | 说明     |
| :------- | :--- | :--- | :------- |
| couponId | int  | 是   | 优惠券ID |

#### 返回示例

json

```
{
  "success": true,
  "message": "预热完成"
}
```



------

### 5. 设置结束时间并开始活动

- **接口地址**：`/coupon/preheatAndSetTime`
- **请求方法**：GET
- **认证**：需要（管理员）

#### 请求参数

| 参数名   | 类型   | 必填 | 说明                            |
| :------- | :----- | :--- | :------------------------------ |
| couponId | int    | 是   | 优惠券ID                        |
| endTime  | String | 是   | 结束时间（yyyy-MM-dd HH:mm:ss） |

#### 返回示例

json

```
{
  "success": true,
  "message": "活动已开始"
}
```



------

### 6. 动态调整库存

- **接口地址**：`/coupon/adjustStock`
- **请求方法**：GET
- **认证**：需要（管理员）

#### 请求参数

| 参数名   | 类型 | 必填 | 说明                         |
| :------- | :--- | :--- | :--------------------------- |
| couponId | int  | 是   | 优惠券ID                     |
| delta    | int  | 是   | 增减量（正数增加，负数减少） |

#### 返回示例

json

```
{
  "success": true,
  "message": "调整成功，当前剩余库存: 120"
}
```



------

### 7. 结束活动

- **接口地址**：`/coupon/end`
- **请求方法**：GET
- **认证**：需要（管理员）

#### 请求参数

| 参数名   | 类型 | 必填 | 说明     |
| :------- | :--- | :--- | :------- |
| couponId | int  | 是   | 优惠券ID |

#### 返回示例

json

```
{
  "success": true,
  "message": "活动已结束"
}
```



------

## 十三、总结

### 系统功能模块

- **用户模块**：注册、登录、个人信息、他人主页、用户列表
- **管理员审批**：申请、审核通过/拒绝
- **视频模块**：上传、查询（全部/分类/标签）、删除
- **动态模块**：发布、查看、删除、评论
- **评论模块**：视频评论、动态评论、删除
- **点赞模块**：视频和评论点赞/取消
- **收藏模块**：视频和动态收藏/取消、收藏列表
- **关注模块**：关注/取关、粉丝列表、关注列表
- **Feed 流**：Pull 模式（游标分页 + Redis 缓存），Push 模式（推出+消息队列）
- **优惠券模块**：活动管理、抢购、库存调整、限流、异步通知

### 系统特点

- 基于 Token 认证（Authorization: Bearer）
- 支持文件上传（视频）
- RBAC 权限控制（用户 / 管理员）
- Redis 缓存加速 Feed 流、视频列表、优惠券库存
- 方法级限流（@RateLimit 注解）
- 单元测试覆盖核心业务（JUnit5 + Mockito）
- 可选消息队列（RocketMQ）用于异步推送