# spring-support-api-starter

## 项目简介

**Spring Boot 3 企业级 API 服务脚手架** - 基于 Spring Boot 3.x 和 Java 21，提供完整的企业级后端服务解决方案。

### 核心特性

- ✅ **模块化设计** - 基于 Maven 多模块，职责分明
- ✅ **OAuth 2.0 认证** - 统一的单点登录系统
- ✅ **项目管理** - 统一的 AI、邮件、短信、设备项目管理
- ✅ **系统管理** - RBAC 权限、用户、角色、菜单管理
- ✅ **通用服务** - 字典、文件、消息、设置等基础服务
- ✅ **监控运维** - 完整的系统监控和运维工具

### 技术栈

- **Java 21** - 最新 LTS 版本，支持 Record、Pattern Matching 等新特性
- **Spring Boot 3.5.5** - 最新稳定版
- **MyBatis Plus** - 数据库 ORM 框架
- **Redis** - 缓存和 Token 存储
- **OAuth 2.0** - 认证授权协议
- **Swagger/OpenAPI** - API 文档

## 项目结构

```
spring-support-api-starter/
├── 基础层（Foundation Layer）
│   ├── spring-api-support-common-entity-starter          # 实体定义
│   └── spring-api-support-common-configuration-starter   # 配置定义
│
├── 核心层（Core Layer - Common模块）
│   ├── spring-api-support-common-oauth-starter           # OAuth认证模块
│   ├── spring-api-support-common-service-starter         # 通用服务模块
│   ├── spring-api-support-common-system-starter          # 系统服务模块
│   ├── spring-api-support-common-project-starter         # 项目管理模块
│   └── spring-api-support-common-video-starter           # 视频服务模块
│
└── 应用层（Application Layer）
    ├── spring-api-support-oauth-starter                  # OAuth服务端应用
    ├── spring-api-support-system-starter                 # 系统管理应用
    ├── spring-api-support-monitor-starter                # 监控运维应用
    ├── spring-api-support-tenanted-starter               # 多租户应用
    └── spring-api-support-scheduler-starter              # 调度任务应用
```

## 模块说明

### 📦 基础层模块

| 模块                             | 职责                       | 文档 |
| -------------------------------- | -------------------------- | ---- |
| **common-entity-starter**        | 统一实体定义，按业务域分包 | -    |
| **common-configuration-starter** | 公共配置类、拦截器、过滤器 | -    |

### 🔧 核心层模块（Common）

| 模块                       | 职责                             | 文档                                                                 |
| -------------------------- | -------------------------------- | -------------------------------------------------------------------- |
| **common-oauth-starter**   | OAuth 认证、登录、Token 管理     | [📖 查看文档](./spring-api-support-common-oauth-starter/README.md)   |
| **common-service-starter** | 字典、文件、消息、设置等通用服务 | [📖 查看文档](./spring-api-support-common-service-starter/README.md) |
| **common-system-starter**  | 用户、角色、菜单、部门等系统服务 | [📖 查看文档](./spring-api-support-common-system-starter/README.md)  |
| **common-project-starter** | AI、邮件、短信、设备等项目管理   | [📖 查看文档](./spring-api-support-common-project-starter/README.md) |
| **common-video-starter**   | 视频相关服务                     | -                                                                    |

### 🚀 应用层模块

| 模块                  | 职责                 | 端口  | 文档                                                        |
| --------------------- | -------------------- | ----- | ----------------------------------------------------------- |
| **oauth-starter**     | OAuth 单点登录服务端 | 19180 | [📖 查看文档](./spring-api-support-oauth-starter/README.md) |
| **system-starter**    | 系统管理应用         | 8080  | -                                                           |
| **monitor-starter**   | 监控运维应用         | 8081  | -                                                           |
| **tenanted-starter**  | 多租户应用           | 8082  | -                                                           |
| **scheduler-starter** | 调度任务应用         | 8083  | -                                                           |

## 依赖关系

```
应用层模块
    ↓
Common层模块
    ↓
基础层模块

特殊依赖：
- common-oauth-starter → oauth-client-starter (OAuth客户端SDK)
- common-service-starter → oauth-client-starter (使用@UserValue注解)
- oauth-starter → common-oauth-starter (实现认证接口)
- system-starter → common-oauth-starter (使用登录服务)
```

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 2. 功能开关配置

每个应用模块都有独立的功能开关配置，位于 `src/main/resources/config/application-plugin.yml`：

**监控服务**：

```yaml
plugin:
  monitor:
    server:
      enable: true # 默认启用
```

**OAuth 认证服务**：

```yaml
plugin:
  oauth:
    server:
      enable: true # 默认启用
```

**系统服务**：

```yaml
plugin:
  system:
    server:
      enable: true # 默认启用
```

> 💡 **说明**：所有应用服务默认启用。如需关闭某个服务，将对应的 `enable` 设置为 `false` 即可。

### 3. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE oauth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
-- OAuth数据库脚本：spring-api-support-oauth-starter/src/main/resources/db/init/
-- System数据库脚本：需要根据实际情况执行
```

### 3. 配置文件

复制配置文件模板：

```bash
cp application-example.yml application.yml
```

修改配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oauth
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动服务

#### 方式一：IDE 启动

- OAuth 服务端：运行 `com.chua.starter.oauth.sso.AuthApplication`
- 系统应用：运行对应的 Application 类

#### 方式二：命令行启动

```bash
# 启动OAuth服务端
cd spring-api-support-oauth-starter
mvn spring-boot:run

# 启动系统应用
cd spring-api-support-system-starter
mvn spring-boot:run
```

## 使用指南

### 创建新的应用模块

1. **添加依赖**

```xml
<dependencies>
    <!-- OAuth认证功能 -->
    <dependency>
        <groupId>com.chua</groupId>
        <artifactId>spring-api-support-common-oauth-starter</artifactId>
    </dependency>

    <!-- 系统基础功能 -->
    <dependency>
        <groupId>com.chua</groupId>
        <artifactId>spring-api-support-common-system-starter</artifactId>
    </dependency>

    <!-- 通用服务 -->
    <dependency>
        <groupId>com.chua</groupId>
        <artifactId>spring-api-support-common-service-starter</artifactId>
    </dependency>
</dependencies>
```

2. **配置 OAuth 客户端**

```yaml
spring:
  oauth:
    client:
      server-url: http://localhost:19180
      client-id: your-client-id
      client-secret: your-client-secret
      token-name: x-oauth-token
```

3. **使用登录功能**

所有应用自动拥有统一的登录接口：

```
POST /v2/user/login          # 用户登录
POST /v2/user/refresh-token  # 刷新Token
DELETE /v2/user/logout       # 用户登出
GET /v2/user/me              # 获取当前用户信息
```

## 架构设计

### 设计原则

1. **职责单一** - 每个模块只负责一个领域的功能
2. **依赖清晰** - 避免循环依赖，依赖方向自上而下
3. **命名统一** - common 前缀表示被非 common 模块依赖
4. **版本统一** - 使用 `${project.version}` 统一管理版本

### Common 模块设计

**Common 模块** 是被非 Common 模块依赖的基础模块：

- ✅ 提供可复用的服务和功能
- ✅ 不包含应用启动类
- ✅ 可以被多个应用模块依赖
- ✅ 职责单一，易于维护

### 项目管理设计

**project-starter** 采用统一的项目管理模式：

每个项目类型（AI、邮件、短信、设备）都包含：

- **配置管理** - AK/SK、API 密钥等
- **模板管理** - 提示词模板、邮件模板、短信模板
- **日志记录** - 任务日志、发送日志、事件日志
- **权限控制** - 项目级别的权限管理

这种设计的优势：

- ✅ 统一的管理模式
- ✅ 易于扩展新项目类型
- ✅ 便于权限控制和审计

## 开发规范

### 版本管理

所有内部模块使用统一版本管理：

```xml
<!-- 父POM中定义 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.chua</groupId>
            <artifactId>spring-api-support-common-oauth-starter</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- 子模块中使用（不指定版本） -->
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-oauth-starter</artifactId>
</dependency>
```

### 代码规范

- 遵循阿里巴巴 Java 开发规范
- 使用 Lombok 简化代码
- 所有公共 API 编写 JavaDoc
- 重要逻辑添加注释

### 提交规范

```
feat: 新功能
fix: 修复bug
refactor: 重构
docs: 文档更新
style: 代码格式调整
test: 测试相关
chore: 构建/工具链相关
```

## 相关文档

- [OAuth 认证模块使用文档](./spring-api-support-common-oauth-starter/README.md)
- [通用服务模块使用文档](./spring-api-support-common-service-starter/README.md)
- [系统服务模块使用文档](./spring-api-support-common-system-starter/README.md)
- [项目管理模块使用文档](./spring-api-support-common-project-starter/README.md)
- [OAuth 服务端部署文档](./spring-api-support-oauth-starter/README.md)

## 许可证

[根据实际情况填写]

## 联系方式

- 作者：CH
- 邮箱：[根据实际情况填写]
