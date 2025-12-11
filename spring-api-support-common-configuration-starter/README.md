# spring-api-support-common-configuration-starter

## 📖 模块简介

**公共配置模块** - 提供全局配置类、拦截器、过滤器等基础设施，作为 Common 模块被所有应用模块共享。

## 🎯 模块定位

- **基础层模块** - 提供公共配置和基础设施
- **职责** - 统一管理全局配置、拦截器、过滤器等
- **原则** - 保持配置的通用性和可复用性

## ✨ 核心功能

### 🔧 全局配置

- ✅ 统一异常处理
- ✅ 统一响应格式
- ✅ 跨域配置
- ✅ 日期格式化配置

### 🛡️ 拦截器

- ✅ 认证拦截器
- ✅ 权限拦截器
- ✅ 日志拦截器
- ✅ 限流拦截器

### 🔍 过滤器

- ✅ 请求日志过滤器
- ✅ XSS 防护过滤器
- ✅ 跨域过滤器

## 📦 模块结构

```
spring-api-support-common-configuration-starter/
├── config/
│   ├── GlobalExceptionHandler.java      # 全局异常处理
│   ├── WebMvcConfig.java                # Web MVC配置
│   ├── CorsConfig.java                  # 跨域配置
│   └── JacksonConfig.java               # Jackson配置
├── interceptor/
│   ├── AuthInterceptor.java             # 认证拦截器
│   ├── PermissionInterceptor.java       # 权限拦截器
│   └── LogInterceptor.java              # 日志拦截器
├── filter/
│   ├── RequestLogFilter.java            # 请求日志过滤器
│   └── XssFilter.java                   # XSS防护过滤器
└── advice/
    ├── ResponseBodyAdvice.java          # 响应体增强
    └── RequestBodyAdvice.java           # 请求体增强
```

## 💡 使用方式

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-configuration-starter</artifactId>
</dependency>
```

### 2. 自动配置

添加依赖后，所有配置会自动生效，无需额外配置。

### 3. 自定义配置

如需自定义配置，可以覆盖默认 Bean：

```java
@Configuration
public class CustomConfig {

    @Bean
    @Primary
    public GlobalExceptionHandler customExceptionHandler() {
        return new CustomGlobalExceptionHandler();
    }
}
```

## 🎨 功能说明

### 1. 统一异常处理

自动捕获并处理所有异常，返回统一格式：

```json
{
  "code": 500,
  "msg": "系统异常",
  "data": null,
  "timestamp": 1701234567890
}
```

### 2. 统一响应格式

所有接口自动包装为统一格式：

```json
{
  "code": 200,
  "msg": "成功",
  "data": {
    "userId": 1,
    "username": "admin"
  }
}
```

### 3. 跨域配置

自动配置跨域支持，允许所有来源访问：

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        // ...
    }
}
```

### 4. Jackson 配置

统一的 JSON 序列化配置：

- 日期格式：`yyyy-MM-dd HH:mm:ss`
- 忽略 null 值
- 支持 Java 8 时间类型
- Long 类型转 String（避免前端精度丢失）

## 🔧 配置项说明

虽然本模块主要提供自动配置，但仍支持部分配置项：

```yaml
spring:
  jackson:
    # 日期格式
    date-format: yyyy-MM-dd HH:mm:ss
    # 时区
    time-zone: GMT+8
    # 序列化配置
    serialization:
      write-dates-as-timestamps: false
```

## 🎯 设计原则

### 1. 约定优于配置

- ✅ 提供合理的默认配置
- ✅ 减少配置工作量
- ✅ 保持配置的一致性

### 2. 可扩展性

- ✅ 支持自定义配置
- ✅ 支持配置覆盖
- ✅ 灵活的扩展点

### 3. 最小依赖

- ✅ 只依赖必要的框架
- ✅ 不引入业务依赖
- ✅ 保持模块的独立性

## 🔗 依赖关系

```
所有业务模块
    ↓ 依赖
spring-api-support-common-configuration-starter (配置模块)
    ↓ 依赖
Spring Boot
Spring Web
Jackson
```

## 📝 开发规范

### 添加新的配置类

```java
/**
 * 自定义配置
 *
 * @author CH
 * @since 2024/12/11
 */
@Configuration
public class CustomConfig {

    /**
     * 自定义Bean
     *
     * @return Bean实例
     */
    @Bean
    @ConditionalOnMissingBean
    public CustomBean customBean() {
        return new CustomBean();
    }
}
```

### 添加新的拦截器

```java
/**
 * 自定义拦截器
 *
 * @author CH
 * @since 2024/12/11
 */
@Component
public class CustomInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) {
        // 拦截逻辑
        return true;
    }
}
```

## 🔗 相关模块

- [spring-api-support-common-entity-starter](../spring-api-support-common-entity-starter) - 实体定义模块
- [spring-support-common-starter](../../spring-support-parent-starter/spring-support-common-starter) - 公共基础模块

## 📄 许可证

本项目采用 Apache License 2.0 许可证。

---

**作者**: CH  
**版本**: 4.0.0  
**更新时间**: 2024/12/11
