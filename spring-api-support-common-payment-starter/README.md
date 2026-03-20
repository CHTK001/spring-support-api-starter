# 支付系统模块

## 功能概述

支付系统提供完整的支付解决方案,包括:

- 商户管理(增删改查/激活/停用)
- 支付渠道配置(微信/支付宝/综合支付/钱包)
- 订单管理(创建/支付/退款/删除)
- 订单状态机流转
- 退款管理
- 交易流水记录
- 钱包账户管理
- API密钥加密存储
- Provider SPI 扩展机制

## 技术栈

- Spring Boot 3.x
- MyBatis Plus
- Redis
- Spring State Machine 4.0
- 微信支付SDK
- 支付宝SDK
- SPI 扩展机制

## 快速开始

### 1. 数据库初始化

#### 方式一：全新安装
执行 `src/main/resources/db/init/V1.0__init_pay.sql` 及后续版本脚本创建数据库表。

#### 方式二：已有系统环境
如果系统已初始化,使用增量迁移脚本:
- 系统菜单权限: `spring-api-support-system-starter/src/main/resources/db/migration/V1.1__add_payment_menus.sql`
- 支付表结构: `src/main/resources/db/init/V1.0__init_pay.sql` ~ `V1.4__payment_refund_notify_support.sql`

### 2. 配置文件

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/system
    username: root
    password: password

  redis:
    host: localhost
    port: 6379

# 支付 Provider SPI 配置
plugin:
  payment:
    provider:
      # 微信支付实现选择: wechat-pay(官方) | mock(本地联调) | default
      wechat: wechat-pay
      # 支付宝实现选择: alipay(官方) | alipay-mock(本地联调) | default
      alipay: alipay
      # 综合支付实现
      composite: default
      # 钱包支付实现
      wallet: default
```

### 3. 回调 URL 配置

支付回调地址格式:
- 微信支付: `{baseUrl}/api/notify/wechat/{merchantId}`
- 支付宝: `{baseUrl}/api/notify/alipay/{merchantId}`

其中 `{baseUrl}` 为应用部署的公网地址,例如:
- 开发环境: `http://localhost:8080/system/api`
- 生产环境: `https://yourdomain.com/system/api`

**注意**: 回调地址必须是公网可访问的 HTTPS 地址(生产环境)。

### 4. API文档

启动后访问: http://localhost:8080/swagger-ui.html

## 核心功能

### 商户管理

- 创建商户
- 更新商户信息
- 激活/停用商户
- 商户列表查询

### 渠道配置

- 微信支付配置
- 支付宝配置(支持沙盒)
- 综合支付配置
- 钱包配置
- API密钥AES加密存储

### 订单管理

- 创建订单
- 发起支付(真实支付接口)
- 订单状态流转(状态机)
- 订单取消/完成
- 订单退款
- 订单删除
- 状态同步
- 超时自动取消

### 退款管理

- 退款订单查询
- 标记退款成功/失败
- 退款状态追踪

### 交易流水

- 流水记录查询
- 按商户/订单号筛选

### 钱包管理

- 钱包账户查询
- 钱包充值
- 余额变更日志
- 乐观锁并发控制

### 状态机

订单状态流转:
- 待支付(PENDING) → 支付中(PAYING) → 支付成功(PAID) → 已完成(COMPLETED)
- 待支付(PENDING) → 已取消(CANCELLED)
- 支付成功(PAID) → 退款中(REFUNDING) → 已退款(REFUNDED)
- 支付中(PAYING) → 支付失败(FAILED)

## Provider SPI 扩展

支付模块支持通过 SPI 机制扩展支付实现:

### 可用 Provider

#### 微信支付
- `wechat-pay`: 官方微信支付网关实现
- `mock` / `wechat-mock`: 本地联调 mock 实现
- `default`: 默认实现

#### 支付宝
- `alipay`: 官方支付宝网关实现
- `mock` / `alipay-mock`: 本地联调 mock 实现
- `default`: 默认实现

### 配置方式

#### 全局配置(application.yml)
```yaml
plugin:
  payment:
    provider:
      wechat: wechat-pay
      alipay: alipay
```

#### 渠道级配置(extConfig)
在创建/更新渠道时,可通过 `extConfig` 字段指定该渠道使用的 Provider:
```json
{
  "providerSpi": "mock",
  "customField": "value"
}
```

渠道级配置优先级高于全局配置。

## 前端集成

前端页面位于 `vue-support-parent-starter/apps/vue-support-system-parent`:
- 商户管理: `src/views/manage/payment-merchant.vue`
- 订单管理: `src/views/manage/payment-order.vue`
- 退款管理: `src/views/manage/payment-refund.vue`
- 交易流水: `src/views/manage/payment-transaction.vue`
- 钱包管理: `src/views/manage/payment-wallet.vue`

路由配置: `src/router/modules/payment.ts`

权限配置:
- 商户管理: `payment:merchant:*`, `payment:channel:*`
- 订单管理: `payment:order:*`
- 退款管理: `payment:refund:*`
- 交易流水: `payment:transaction:view`
- 钱包管理: `payment:wallet:view`, `payment:wallet:recharge`

## 安全特性

- API密钥 AES 加密存储
- 敏感字段查询时返回脱敏标识
- 钱包余额更新使用乐观锁
- 权限控制(后端 `@Permission` + 前端 `hasAuth()`)
- 支付回调签名验证

## 注意事项

1. **生产环境必须使用 HTTPS**: 支付回调地址必须是 HTTPS 协议
2. **回调地址配置**: 确保回调地址公网可访问,并在支付平台配置白名单
3. **Provider 选择**: 开发环境可使用 mock 实现,生产环境必须使用官方实现
4. **密钥安全**: apiKey/privateKey 等敏感信息加密存储,不要明文记录日志
5. **并发控制**: 钱包充值/扣款使用乐观锁,失败时需重试
6. **超时配置**: 订单超时自动取消时间可通过商户 `autoCloseMinutes` 配置

## 开通流程引导

系统内置完整的支付方式开通引导,包括:
- 官方文档链接
- 申请地址
- 沙盒环境地址
- 所需材料清单
- 开通步骤说明
- 注意事项提示

前端通过"开通指引"按钮展示引导信息。

## 作者

CH

## 版本

4.0.0.37
