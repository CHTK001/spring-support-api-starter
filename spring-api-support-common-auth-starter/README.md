# spring-api-support-common-auth-starter

## 模块说明

**统一认证授权模块** - 提供登录、注册、权限验证等功能，所有应用模块共享统一的认证逻辑。

## 设计理念

### 职责单一

- ✅ 专注于认证和授权功能
- ✅ 不包含业务服务逻辑
- ✅ 独立的模块，避免循环依赖

### 模块定位

- **common 模块**：被非 common 模块依赖
- **职责**：提供统一的登录、注册、Token 管理等认证功能
- **实现**：基于 OAuth 2.0 协议，使用 `spring-support-oauth-client-starter`

## 功能特性

### 1. 登录功能

- ✅ 密码登录（WEB）
- ✅ 租户登录（TENANT）
- ✅ 第三方登录（微信、支付宝等）
- ✅ 验证码校验
- ✅ 登录失败次数限制

### 2. Token 管理

- ✅ JWT Token 生成
- ✅ Token 刷新机制
- ✅ Token 验证
- ✅ 自动过期管理

### 3. 用户信息

- ✅ 获取当前登录用户信息
- ✅ 用户权限和角色信息
- ✅ 多租户支持

### 4. 第三方登录

- ✅ 第三方登录授权
- ✅ 第三方账号绑定
- ✅ 第三方账号解绑
- ✅ 查询绑定信息

## 模块结构

```
spring-api-support-common-auth-starter/
├── controller/
│   └── LoginController.java              # 登录控制器
├── service/
│   ├── SysLoginService.java              # 登录服务接口
│   └── impl/
│       └── SysLoginServiceImpl.java      # 登录服务实现
├── handler/
│   ├── LoginHandler.java                 # 登录处理器接口
│   ├── DefaultLoginHandler.java          # 默认登录处理器
│   └── TenantLoginHandler.java           # 租户登录处理器
├── pojo/
│   ├── RefreshToken.java                 # 刷新Token请求
│   └── ThirdPartyUnbindRequest.java      # 第三方解绑请求
├── result/
│   ├── LoginResult.java                  # 登录响应
│   └── UserInfoVO.java                   # 用户信息视图对象
├── mapper/
│   └── SysUserThirdPartyMapper.java      # 第三方绑定Mapper
└── config/
    └── AuthAutoConfiguration.java        # 自动配置
```

## 依赖关系

```
应用模块 (system-starter, monitor-starter等)
    ↓ 依赖
spring-api-support-common-auth-starter (认证模块)
    ↓ 依赖
spring-api-support-common-entity-starter (实体定义)
spring-api-support-common-configuration-starter (配置定义)
spring-support-oauth-client-starter (OAuth客户端)
```

## 使用方式

### 1. 添加依赖

在应用模块中添加依赖：

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-auth-starter</artifactId>
</dependency>
```

### 2. 配置 OAuth

```yaml
spring:
  oauth:
    client:
      server-url: http://localhost:8080
      client-id: your-client-id
      client-secret: your-client-secret
      token-name: x-oauth-token
```

### 3. 使用登录接口

所有应用模块自动拥有统一的登录接口：

```
POST /v2/user/login          # 用户登录
POST /v2/user/refresh-token  # 刷新Token
DELETE /v2/user/logout       # 用户登出
GET /v2/user/me              # 获取当前用户信息
```

## API 接口

### 登录接口

**请求示例：**

```json
{
  "username": "admin",
  "password": "123456",
  "loginType": "WEB",
  "accountType": "default",
  "verifyCodeKey": "1234",
  "verifyCodeUlid": "uuid"
}
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expires": 7200000,
    "userInfo": {
      "sysUserId": 1,
      "sysUserUsername": "admin",
      "sysUserNickname": "管理员",
      "avatar": "https://...",
      "roles": ["admin"],
      "perms": ["*:*:*"]
    }
  }
}
```

## 扩展登录处理器

系统使用 SPI 机制支持自定义登录处理器：

### 1. 实现 LoginHandler 接口

```java
@Spi("CUSTOM")
public class CustomLoginHandler implements LoginHandler {

    @Override
    public ReturnResult<LoginResult> login(SysLoginServiceImpl sysLoginService,
                                          LoginData loginData,
                                          HttpServletRequest request,
                                          HttpServletResponse response,
                                          String fingerprint) {
        // 自定义登录逻辑
        return ReturnResult.success(loginResult);
    }
}
```

### 2. 注册 SPI

在 `META-INF/services/com.chua.starter.auth.handler.LoginHandler` 中添加：

```
com.your.package.CustomLoginHandler
```

### 3. 使用自定义处理器

登录时指定 `loginType` 为 `CUSTOM`：

```json
{
  "username": "admin",
  "password": "123456",
  "loginType": "CUSTOM"
}
```

## 优势

### 1. 职责单一

- ✅ 只负责认证相关功能
- ✅ 不包含业务服务逻辑
- ✅ 代码清晰易维护

### 2. 统一接口

- ✅ 所有应用模块使用相同的登录接口
- ✅ 前端只需对接一套 API
- ✅ 减少重复代码

### 3. 易于扩展

- ✅ SPI 机制支持自定义登录处理器
- ✅ 支持多种登录方式
- ✅ 灵活的配置选项

### 4. 避免循环依赖

- ✅ auth-starter 不依赖 service-starter
- ✅ service-starter 不依赖 auth-starter
- ✅ 清晰的依赖层次

## 注意事项

1. **避免循环依赖**：auth-starter 和 service-starter 互不依赖
2. **OAuth 配置必需**：需要配置 OAuth 服务端地址
3. **验证码功能**：需要 Redis 支持
4. **第三方登录**：需要在 OAuth 服务端配置第三方应用信息

## 相关模块

- `spring-support-oauth-client-starter` - OAuth 客户端实现
- `spring-api-support-oauth-starter` - OAuth 服务端实现
- `spring-api-support-common-entity-starter` - 实体定义
- `spring-api-support-common-configuration-starter` - 配置定义
