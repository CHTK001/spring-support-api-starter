# Spring API Support OAuth Starter

## 📖 模块简介

Spring API Support OAuth Starter 是一个功能完整的 OAuth 认证服务端/单点登录（SSO）系统。该模块提供了完整的 OAuth 认证服务，支持多种通信协议，包括 HTTP、gRPC、TCP、UDP 等，可以作为企业级单点登录解决方案的核心服务。

## ✨ 核心功能

### 🔐 OAuth 认证服务

- **用户登录认证** - 支持用户名密码登录
- **第三方登录** - 支持 Gitee、GitHub 等第三方平台登录
- **令牌管理** - 完整的 Token 生成、验证、刷新、撤销机制
- **会话管理** - 基于 Redis 的分布式会话管理
- **用户信息服务** - 提供用户详细信息查询

### 🌐 多协议支持

- **HTTP 协议** - 标准的 RESTful API
- **gRPC 协议** - 高性能的 RPC 通信
- **TCP 协议** - 二进制 TCP 通信
- **UDP 协议** - 无连接 UDP 通信
- **Armeria 协议** - 基于 Armeria 框架的通信

### 👥 用户管理

- **用户 CRUD** - 完整的用户增删改查
- **角色管理** - 灵活的角色权限体系
- **部门管理** - 组织架构管理
- **租户管理** - 多租户数据隔离
- **用户日志** - 完整的用户操作日志

### 🔒 安全机制

- **签名验证** - 请求签名校验
- **防重放攻击** - 时间戳和 UUID 验证
- **AK/SK 认证** - 应用密钥认证
- **加密传输** - 支持多种加密算法
- **IP 白名单** - IP 访问控制

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-oauth-starter</artifactId>
    <version>4.0.0</version>
</dependency>
```

### 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE oauth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：

```bash
# 位于 src/main/resources/sql/ 目录下
mysql -u root -p oauth < oauth.sql
mysql -u root -p oauth < sys_app_key.sql
```

### 基础配置

```yaml
spring:
  application:
    name: oauth-server
  profiles:
    active: dev

  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/oauth?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  # Redis 配置
  redis:
    host: localhost
    port: 6379
    password:
    database: 0

server:
  port: 19180
  servlet:
    context-path: /oauth

# OAuth 服务配置
plugin:
  oauth:
    server:
      # 支持的协议
      protocols: [http, grpc, tcp, udp]
      # gRPC 端口
      grpc-port: 9090
      # Token 过期时间（秒）
      token-timeout: 7200
      # 刷新 Token 过期时间（秒）
      refresh-token-timeout: 604800
```

## 📋 数据库设计

### 核心表结构

#### sys_user - 用户表

```sql
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) COMMENT '真实姓名',
  `email` varchar(100) COMMENT '邮箱',
  `phone` varchar(20) COMMENT '手机号',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_admin` tinyint DEFAULT 0 COMMENT '是否管理员',
  `dept_id` bigint COMMENT '部门ID',
  `tenant_id` bigint COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) COMMENT='用户表';
```

#### sys_role - 角色表

```sql
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) COMMENT '描述',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `tenant_id` bigint COMMENT '租户ID',
  PRIMARY KEY (`role_id`)
) COMMENT='角色表';
```

#### sys_app_key - 应用密钥表

```sql
CREATE TABLE `sys_app_key` (
  `app_id` bigint NOT NULL AUTO_INCREMENT COMMENT '应用ID',
  `app_key` varchar(100) NOT NULL COMMENT 'AppKey',
  `app_secret` varchar(100) NOT NULL COMMENT 'AppSecret',
  `app_name` varchar(100) COMMENT '应用名称',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`app_id`),
  UNIQUE KEY `uk_app_key` (`app_key`)
) COMMENT='应用密钥表';
```

## 🔧 协议使用说明

### 1. HTTP 协议

#### 用户登录

```bash
POST /oauth/api/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456",
  "appKey": "your_app_key",
  "timestamp": 1701234567890,
  "sign": "calculated_signature"
}
```

#### Token 验证

```bash
POST /oauth/api/authenticate
Content-Type: application/json

{
  "token": "your_access_token",
  "appKey": "your_app_key",
  "timestamp": 1701234567890,
  "sign": "calculated_signature"
}
```

#### Token 刷新

```bash
POST /oauth/api/upgrade
Content-Type: application/json

{
  "refreshToken": "your_refresh_token",
  "appKey": "your_app_key",
  "timestamp": 1701234567890,
  "sign": "calculated_signature"
}
```

### 2. gRPC 协议

#### 服务定义

```protobuf
service OAuthService {
  rpc Login(LoginRequest) returns (LoginResponse);
  rpc Authenticate(AuthRequest) returns (AuthResponse);
  rpc Upgrade(UpgradeRequest) returns (UpgradeResponse);
  rpc Logout(LogoutRequest) returns (LogoutResponse);
}
```

#### 客户端调用示例

```java
// 创建 gRPC 客户端
ManagedChannel channel = ManagedChannelBuilder
    .forAddress("localhost", 9090)
    .usePlaintext()
    .build();

OAuthServiceGrpc.OAuthServiceBlockingStub stub =
    OAuthServiceGrpc.newBlockingStub(channel);

// 登录请求
LoginRequest request = LoginRequest.newBuilder()
    .setUsername("admin")
    .setPassword("123456")
    .setAppKey("your_app_key")
    .setTimestamp(System.currentTimeMillis())
    .setSign("calculated_signature")
    .build();

LoginResponse response = stub.login(request);
```

### 3. TCP/UDP 协议

TCP 和 UDP 协议使用二进制格式传输，适合高性能场景。

## 🔐 安全机制

### 签名算法

所有请求都需要签名验证，签名算法如下：

```java
// 1. 按参数名排序
Map<String, String> params = new TreeMap<>();
params.put("username", "admin");
params.put("password", "123456");
params.put("appKey", "your_app_key");
params.put("timestamp", "1701234567890");

// 2. 拼接字符串
StringBuilder sb = new StringBuilder();
for (Map.Entry<String, String> entry : params.entrySet()) {
    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
}
sb.append("appSecret=").append(appSecret);

// 3. MD5 加密
String sign = DigestUtils.md5Hex(sb.toString());
```

### AK/SK 管理

1. 在 `sys_app_key` 表中创建应用：

```sql
INSERT INTO sys_app_key (app_key, app_secret, app_name, status)
VALUES ('ak_test_123', 'sk_test_456', '测试应用', 1);
```

2. 使用 AppKey 和 AppSecret 进行签名验证

### IP 白名单

在配置文件中设置 IP 白名单：

```yaml
plugin:
  oauth:
    server:
      ip-whitelist:
        - 192.168.1.0/24
        - 10.0.0.0/8
```

## 🔄 第三方登录集成

### Gitee 登录

1. 配置 Gitee 应用信息：

```yaml
plugin:
  oauth:
    third-party:
      gitee:
        client-id: your_client_id
        client-secret: your_client_secret
        redirect-uri: http://your-domain/oauth/callback/gitee
```

2. 前端跳转到授权页面：

```javascript
window.location.href = "/oauth/api/third-party/gitee/authorize";
```

3. 处理回调：

```java
// 系统会自动处理回调，创建或绑定用户
// 返回 Token 给前端
```

## 📊 监控和日志

### 用户操作日志

所有用户操作都会记录到 `sys_user_log` 表：

- 登录日志
- 登出日志
- Token 刷新日志
- 权限验证日志

### 查询日志

```sql
SELECT * FROM sys_user_log
WHERE user_id = 1
ORDER BY create_time DESC
LIMIT 10;
```

## ⚙️ 高级配置

### 多租户配置

```yaml
plugin:
  oauth:
    tenant:
      enable: true
      # 租户数据源配置
      datasources:
        - tenant-id: 1
          url: jdbc:mysql://localhost:3306/tenant1
        - tenant-id: 2
          url: jdbc:mysql://localhost:3306/tenant2
```

### Redis Token 管理

```yaml
spring:
  redis:
    # Token 存储前缀
    key-prefix: oauth:token:
    # 使用 Redis 集群
    cluster:
      nodes:
        - 192.168.1.1:6379
        - 192.168.1.2:6379
        - 192.168.1.3:6379
```

### gRPC TLS 配置

```yaml
plugin:
  oauth:
    server:
      grpc-enable-tls: true
      grpc-cert-chain-file: /path/to/cert.pem
      grpc-private-key-file: /path/to/key.pem
```

## 🏗️ 架构设计

### 协议层架构

```
┌─────────────────────────────────────────┐
│         Protocol Interface              │
├─────────────────────────────────────────┤
│  HttpProtocol  │  GrpcProtocol  │ ...  │
├─────────────────────────────────────────┤
│      AuthInformation (认证信息处理)      │
├─────────────────────────────────────────┤
│      Authorization (认证授权逻辑)        │
├─────────────────────────────────────────┤
│      Service Layer (业务服务层)         │
├─────────────────────────────────────────┤
│      Data Access Layer (数据访问层)     │
└─────────────────────────────────────────┘
```

### 认证流程

```
1. 客户端请求 → 2. 协议解析 → 3. 签名验证 → 4. 用户认证
   ↓
5. Token 生成 → 6. Redis 存储 → 7. 返回响应
```

## 📝 API 文档

启动服务后，访问 Swagger 文档：

```
http://localhost:19180/oauth/swagger-ui/index.html
```

## 🐛 故障排除

### 常见问题

#### 1. 签名验证失败

- 检查参数排序是否正确
- 确认 AppSecret 是否正确
- 验证时间戳是否在有效范围内（±5 分钟）

#### 2. Token 验证失败

- 检查 Token 是否过期
- 确认 Redis 连接是否正常
- 验证 Token 格式是否正确

#### 3. gRPC 连接失败

- 检查 gRPC 端口是否开放
- 确认防火墙设置
- 验证 TLS 证书配置

### 调试日志

启用详细日志：

```yaml
logging:
  level:
    com.chua.starter.oauth.sso: DEBUG
    com.chua.starter.oauth.sso.support.protocol: TRACE
```

## 📦 部署指南

### Docker 部署

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/spring-api-support-oauth-starter.jar /app.jar
EXPOSE 19180 9090
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes 部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: oauth-server
spec:
  replicas: 3
  selector:
    matchLabels:
      app: oauth-server
  template:
    metadata:
      labels:
        app: oauth-server
    spec:
      containers:
        - name: oauth-server
          image: oauth-server:latest
          ports:
            - containerPort: 19180
            - containerPort: 9090
```

## 🔗 相关模块

- [spring-support-oauth-client-starter](../spring-support-oauth-client-starter) - OAuth 客户端模块
- [spring-support-mybatis-starter](../spring-support-mybatis-starter) - MyBatis 支持模块
- [spring-support-redis-starter](../spring-support-redis-starter) - Redis 支持模块

## 📄 许可证

本项目采用 Apache License 2.0 许可证。

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

---

**作者**: CH  
**版本**: 4.0.0  
**更新时间**: 2024/12/02
