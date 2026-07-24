# spring-api-support-payment-starter

独立支付服务

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-payment-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 功能概览

| 类/接口 | 说明 |
|---------|------|
| `PaymentConsoleConfiguration` | 支付控制台基础配置。 |
| `PaymentConsoleWebMvcConfigurer` | 支付控制台 MVC 配置。 |
| `PaymentConsoleAuthController` | 支付控制台轻量登录接口。 |
| `PaymentConsoleController` | 支付控制台静态页入口。 |
| `PaymentApplication` | 独立支付服务启动类。 |
| `PaymentConsoleProperties` | 支付轻量控制台配置。 |
| `PaymentConsoleAuthInterceptor` | 支付控制台轻量登录拦截器。 |
| `PaymentConsoleSessionSupport` | 支付控制台登录会话工具。 |

---

## 配置说明

本模块为零配置模块，引入依赖后即可使用。

---

## 依赖关系

```
spring-api-support-payment-starter
├── spring-api-support-common-payment-starter
├── spring-support-payment-starter
├── spring-support-report-client-starter
```