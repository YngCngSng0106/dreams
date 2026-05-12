# 梦境分享 App 后端

分享你的梦

## 技术栈

- Spring Boot 2.7.18
- MyBatis-Plus 3.5.5
- MySQL 8.x
- JWT (jjwt 0.11.5)
- WebSocket (STOMP/SockJS)
- Lombok

## 快速开始

### 1. 创建数据库

在MySQL中执行 `src/main/resources/schema.sql` 建表脚本，包含14张表和预设数据。

### 2. 配置数据库连接

修改 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dream_share?...
    username: root
    password: your_password
```

### 3. 运行

```bash
mvn spring-boot:run
```

服务启动在 http://localhost:8080

### 4. 图片上传目录

确保目录存在:

```bash
mkdir -p /data/apps/dreams/uploads/images
```

## 项目结构

```
src/main/java/com/dreamshare/
├── DreamShareApplication.java    # 启动类
├── config/                        # 配置类
│   ├── WebConfig.java            # CORS + 静态资源
│   ├── MybatisPlusConfig.java    # 分页插件
│   ├── JwtInterceptor.java       # JWT鉴权拦截器
│   ├── InterceptorConfig.java    # 拦截器注册
│   ├── WebSocketConfig.java      # WebSocket配置
│   └── MyMetaObjectHandler.java  # 自动填充
├── controller/                    # 控制器层
├── service/                       # 业务层
├── mapper/                        # 数据访问层
├── entity/                        # 实体类 (14张表)
├── dto/                           # 数据传输对象
└── utils/                         # 工具类
    ├── Result.java                # 统一返回格式
    ├── JwtUtil.java               # JWT工具
    └── DreamSimilarityCalculator.java  # 梦境相似度计算
```

## API 接口总览

### 认证 /api/auth
- POST /register — 注册
- POST /login — 登录
- POST /logout — 登出
- GET /verify — 验证token

### 用户 /api/users
- GET /me — 我的信息 (需登录)
- PUT /me — 修改信息
- GET /{userId} — 用户主页
- GET /{userId}/dreams — 用户梦境列表
- GET /{userId}/discussions — 用户讨论组列表

### 梦境 /api/dreams
- POST — 创建梦境 (需登录)
- GET /{dreamId} — 梦境详情
- PUT /{dreamId} — 更新梦境
- DELETE /{dreamId} — 删除梦境
- GET — 我的梦境列表
- GET /feed — 探索流
- GET /{dreamId}/similar — 相似梦境
- POST /{dreamId}/like — 点赞
- POST /{dreamId}/unlike — 取消点赞
- GET /{dreamId}/stats — 梦境统计

### 分类 /api/categories
- GET — 分类列表
- POST — 添加分类 (管理员)

### 讨论组 /api/discussions
- POST — 创建 (需登录)
- GET /{id} — 详情
- PUT /{id} — 更新
- DELETE /{id} — 删除
- GET — 列表
- GET /my — 我的讨论组 (需登录)
- GET /{id}/members — 成员列表
- POST /{id}/join — 加入 (需登录)
- POST /{id}/leave — 退出 (需登录)
- POST /{id}/invite — 邀请 (需登录)
- GET /recommended — 推荐 (需登录)

### 评论 /api/comments
- POST — 发表评论 (需登录)
- GET /{discussionId} — 评论列表
- PUT /{commentId} — 编辑 (需登录)
- DELETE /{commentId} — 删除 (需登录)
- POST /{commentId}/like — 点赞 (需登录)
- POST /{commentId}/unlike — 取消点赞 (需登录)

### 关注 /api/follow
- POST /{userId} — 关注 (需登录)
- DELETE /{userId} — 取关 (需登录)
- GET /following/{userId} — 关注列表
- GET /followers/{userId} — 粉丝列表
- GET /status/{userId} — 关注关系 (需登录)

### 通知 /api/notifications
- GET — 通知列表 (需登录)
- GET /unread-count — 未读数 (需登录)
- PUT /{id}/read — 标记已读 (需登录)
- PUT /read-all — 全部已读 (需登录)

### 搜索 /api/search
- GET — 综合搜索
- GET /tags — 热门标签

### 设置 /api/settings
- GET — 获取设置 (需登录)
- PUT — 更新设置 (需登录)
- PUT /password — 修改密码 (需登录)
- POST /delete-account — 注销账号 (需登录)

### 统计 /api/stats
- GET /me — 个人统计 (需登录)

### 上传 /api/upload
- POST /image — 上传图片 (需登录)

### WebSocket
- /ws — STOMP端点
- /user/topic/notifications — 个人通知推送

## 内容审核

每10分钟自动扫描PENDING状态的内容，匹配audit_keyword表中的关键词:
- LOW: 标记为REJECTED但不删除
- MEDIUM: 标记为REJECTED
- HIGH: 标记为REJECTED并软删除

## 相似梦境匹配

多维度加权算法: 分类(30%) + 关键词(30%) + 地点(20%) + 清晰度(10%) + 是否重复(10%)
相似度 > 0.3 视为匹配
