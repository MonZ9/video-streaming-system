# 🎬 视频平台项目（Video Streaming System）

## 一、项目简介

本项目是一个基于 Java Web 的视频平台系统，支持用户注册、登录、视频上传、浏览及评论功能，并实现了基础权限控制（普通用户 / 视频作者 / 管理员）。

------

## 二、技术栈

- 后端：Java + Servlet
- 前端：HTML + CSS + JavaScript
- 数据库：MySQL
- 构建工具：Maven
- 服务器：Tomcat
- 数据格式：JSON
- 登录机制：Session（Cookie）

------

## 三、运行环境要求

| 环境   | 版本建议  |
| ------ | --------- |
| JDK    | 1.8+      |
| MySQL  | 5.7 / 8.0 |
| Maven  | 3.x       |
| Tomcat | 8.5 / 9   |

------

## 四、项目依赖

项目使用 Maven 管理依赖，主要依赖如下：

- mysql-connector-java（数据库驱动）
- fastjson（JSON处理）
- servlet-api（Web支持）
- slf4j（日志）

执行以下命令自动下载依赖：

```bash
mvn clean install
```

------

## 五、数据库配置

### 1. 创建数据库

```sql
CREATE DATABASE video_db;
```

### 2. 修改数据库连接

文件位置：

```
com.video.util.DbUtil
```

修改以下内容：

```java
private static final String URL = "jdbc:mysql://localhost:3306/video_db?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "123456";
```

------

## 六、项目运行步骤

### 方式一：IDEA 运行（推荐）

1. 用 IntelliJ IDEA 打开项目
2. 配置 Tomcat
3. 部署 Artifact（war_exploded）
4. 启动服务器

访问地址：

```
http://localhost:8080/video_streaming_system_war_exploded/
```

------

### 方式二：命令行运行

#### 1. 打包项目

```bash
mvn clean package
```

#### 2. 部署到 Tomcat

将生成的 war 包复制到：

```
tomcat/webapps/
```

#### 3. 启动 Tomcat

```bash
startup.bat
```

------

## 七、默认访问路径

| 页面     | 地址           |
| -------- | -------------- |
| 首页     | /              |
| 登录     | /login.html    |
| 注册     | /register.html |
| 视频列表 | /home.html     |

------

## 八、主要功能

### 用户模块

- 注册
- 登录
- 获取用户信息
- 退出登录

### 视频模块

- 上传视频
- 查看视频
- 删除视频

### 评论模块

- 添加评论
- 查看评论
- 删除评论（权限控制）

------

## 九、日志说明

日志文件位置：

```
/logs/app.log
```

系统启动时自动创建。

------

## 十、注意事项

1. 请确保 MySQL 已启动
2. 端口 8080 未被占用
3. 上传视频大小受 Tomcat 配置限制（默认100MB）
4. 浏览器需支持 HTML5 video 标签

------

## 十一、项目特点

- 使用自定义连接池（DbUtil）
- 简单缓存机制（VideoService）
- Session 登录认证
- 权限控制（用户 / 作者 / 管理员）
- 统一 JSON 返回格式

------

## 十二、作者说明

本项目用于学习 Java Web 开发，适合作为课程设计或练手项目。
