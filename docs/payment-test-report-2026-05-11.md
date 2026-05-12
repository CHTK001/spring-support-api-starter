# 支付系统测试报告

> 测试日期: 2026-05-11
> 测试环境: 
> - 后端: spring-api-support-payment-test-starter (port 8085)
> - 前端: vue-support-payment-starter (port 8848)
> - 数据库: MySQL (pay_global_config 等)
> - 商户: 示例商户 (ID=1)
> - 全局配置: paymentNotifyBaseUrl = https://www.qirenit.com/hongning-pay

---

## 1. 支付宝支付回调流程

### 测试数据
| 字段 | 值 |
|------|-----|
| 订单号 | ORD1778404813275DD0675 |
| 订单ID | 23 |
| 支付方式 | ALIPAY/PAY_WEB（支付宝网页支付） |
| 金额 | ¥1.00 |
| 商品 | 测试商品 |
| 用户ID | 1 |
| 商户 | 示例商户 (ID=1) |

### 测试步骤与结果

#### 1.1 发起支付
- **API**: `PUT /api/order/23/pay-start`
- **请求体**: `{"operator": "payment-console"}`
- **响应**: `{"code": "00000", "data": true}`
- **状态流转**: PENDING → **PAYING** ✅

#### 1.2 支付成功（模拟支付宝回调）
- **API**: `PUT /api/order/23/pay-success`
- **请求体**: `{"operator": "payment-console", "paidAmount": 1.00}`
- **响应**: `{"code": "00000", "data": true}`
- **状态流转**: PAYING → **PAID** ✅
- **实付金额**: ¥1.00
- **支付时间**: 2026-05-11 09:52:36

#### 1.3 退款成功（模拟支付宝退款回调）
- **API**: `PUT /api/order/23/refund-success`
- **请求体**: `{"operator": "payment-console", "refundAmount": 1.00}`
- **响应**: `{"code": "00000", "data": true}`
- **状态流转**: PAID → **SYS_REFUNDED** ✅
- **退款金额**: ¥1.00（全额退款）

#### 1.4 完整状态机流转
```
CREATE → PENDING → PAYING → PAID → SYS_REFUNDED
```

### 涉及的后端文件
| 文件 | 说明 |
|------|------|
| `OrderController.java` | Controller: pay-start、pay-success、refund-success |
| `PaymentOrderServiceImpl.java` | 业务逻辑: 状态变更、回填金额 |
| `OrderStateMachineServiceImpl.java` | 状态机引擎: 事件驱动状态流转 |
| `OrderStateMachineConfig.java` | 状态机配置: PENDING→PAYING→PAID 定义 |

---

## 2. 钱包提现到支付宝

### 测试数据
| 字段 | 值 |
|------|-----|
| 用户ID | 1 |
| 商户ID | 1 |
| 金额 | ¥1.00 |
| 支付宝账号 | alipay@test.com |
| 开户行 | 支付宝 |
| 户名 | 测试用户 |

### 测试步骤与结果

#### 2.1 提现前余额查询
- **API**: `GET /api/wallet/account?userId=1&merchantId=1`
- **可用余额**: ¥1,379.99

#### 2.2 发起提现
- **API**: `POST /api/wallet/account/withdraw`
- **请求体**:
```json
{
  "merchantId": 1,
  "userId": 1,
  "amount": 1.00,
  "bankAccount": "alipay@test.com",
  "bankName": "支付宝",
  "accountName": "测试用户",
  "operator": "payment-console",
  "remark": "钱包提现到支付宝测试"
}
```
- **响应**: `{"code": "00000", "data": {"success": true, "status": "SUCCESS", "withdrawNo": "WDW1778466453116A1FA4F", "tradeNo": "WALLET_WITHDRAW_WDW1778466453116A1FA4F"}}`
- **状态**: SUCCESS ✅
- **备注**: 钱包提现是**同步完成**的（非异步回调），无 simulate-notify 接口

#### 2.3 余额扣减验证
- **前**: ¥1,379.99
- **后**: ¥1,378.99
- **扣减**: -¥1.00 ✅

#### 2.4 流水记录
- **API**: `GET /api/wallet/account/log/page?bizType=WITHDRAW`
- **记录**: WITHDRAW / OUT ¥1.0 → 余额 ¥1,378.99
- **备注**: "提现到支付宝(alipay@test.com): 钱包提现到支付宝测试" ✅

### 涉及的后端文件
| 文件 | 说明 |
|------|------|
| `WalletAccountController.java` | Controller: /api/wallet/account/withdraw |
| `WalletBalancePaymentChannel.java` | Channel: 提现业务核心逻辑（创建订单→扣余额→标记成功） |
| `WalletAccountServiceImpl.java` | 账户服务: 余额变更（CAS乐观锁+重试） |
| `WalletOrderServiceImpl.java` | 订单服务: 创建/标记钱包订单 |
| `WalletLimitServiceImpl.java` | 限额校验: 单笔+日累计限额 |

---

## 3. 优惠券管理

### 测试数据
| 字段 | 值 |
|------|-----|
| 商户ID | 1（示例商户） |
| 测试优惠券编码 | TEST001 |
| 测试优惠券名称 | 测试满减券 |
| 优惠类型 | FIXED（固定金额） |
| 优惠金额 | ¥10.00 |
| 使用门槛 | ¥100.00 |
| 发行总量 | 100 |

### 测试步骤与结果

#### 3.1 查询优惠券列表
- **API**: `GET /api/coupon/page?page=1&size=10`
- **响应**: `{"code": "00000", ...}`
- **结果**: 返回分页数据，包含已有优惠券（新人8折券、满100减10）✅
- **注意**: 查询参数 `status` 为空时不参与过滤（修复了 NPE 空指针问题）

#### 3.2 创建优惠券
- **API**: `POST /api/coupon`
- **请求体**:
```json
{
  "couponCode": "TEST001",
  "couponName": "测试满减券",
  "discountType": "FIXED",
  "discountValue": 10,
  "minAmount": 100,
  "totalCount": 100,
  "merchantId": 1,
  "description": "测试优惠券数据"
}
```
- **响应**: `{"code": "00000", "data": {"id": 3, "couponCode": "TEST001", ...}}`
- **结果**: 创建成功 ✅

#### 3.3 按编码查询
- **API**: `GET /api/coupon/code/TEST001`
- **响应**: `{"code": "00000", "data": {"id": 3, "couponCode": "TEST001", "couponName": "测试满减券", ...}}`
- **结果**: 正确返回编码匹配的优惠券 ✅

#### 3.4 启用/禁用
- **API**: `PUT /api/coupon/{id}/disable` → `PUT /api/coupon/{id}/enable`
- **响应**: 均为 `{"code": "00000", "data": true}`
- **结果**: 状态切换正常 ✅

### 修复的问题
| 问题 | 修复 |
|------|------|
| 查询时 `status` 参数为 null 导致 NPE | 三元表达式增加 `status != null &&` 前置判断 |
| `pay_coupon_template` 表缺少 `couponCode`/`startTime`/`endTime` 列 | 创建补丁脚本 `V1.2__coupon_template_new_fields.sql` |
| CouponController 未打包到 test classpath | 执行 `mvn install` 到本地仓库后重启 DevTools |

### 涉及的后端文件
| 文件 | 说明 |
|------|------|
| `CouponController.java` | Controller: page、create、getByCode、enable/disable、delete |
| `CouponTemplate.java` | 实体: 映射 pay_coupon_template 表 |
| `CouponTemplateService / Impl` | 业务逻辑: 创建/更新/删除 |
| `CouponTemplateVO.java` | VO: 前后端数据转换 |
| `V1.2__coupon_template_new_fields.sql` | 补丁: 补充 couponCode、startTime、endTime 列 |

---

## 4. 环境配置

### 4.1 支付全局配置
| 配置项 | 值 |
|--------|-----|
| paymentNotifyBaseUrl | https://www.qirenit.com/hongning-pay |
| paymentReturnUrl | https://www.qirenit.com/hongning-pay |
| paymentAutoRefreshSeconds | 30 |

### 4.2 示例回调URL（自动生成）
| 回调类型 | 示例URL |
|----------|---------|
| 支付宝支付回调 | `{baseUrl}/api/notify/alipay/pay/{orderNo}/{merchantId}` |
| 微信支付回调 | `{baseUrl}/api/notify/wechat/pay/{orderNo}/{merchantId}` |
| 微信退款回调 | `{baseUrl}/api/notify/wechat/refund/{refundNo}/{merchantId}` |
| 钱包充值回调（支付宝） | `{baseUrl}/api/notify/wallet/alipay/RECHARGE/{orderNo}` |
| 钱包转账回调（支付宝） | `{baseUrl}/api/notify/wallet/alipay/TRANSFER/{orderNo}` |
| 钱包提现回调（支付宝） | `{baseUrl}/api/notify/wallet/alipay/WITHDRAW/{orderNo}` |
| 钱包充值回调（微信） | `{baseUrl}/api/notify/wallet/wechat/RECHARGE/{orderNo}` |
| 钱包转账回调（微信） | `{baseUrl}/api/notify/wallet/wechat/TRANSFER/{orderNo}` |
| 钱包提现回调（微信） | `{baseUrl}/api/notify/wallet/wechat/WITHDRAW/{orderNo}` |

### 4.3 商户渠道配置
| 商户 | 渠道 | 状态 |
|------|------|------|
| 示例商户 (ID=1) | ALIPAY/PAY_WEB | 已启用 |
| 示例商户 (ID=1) | WALLET/BALANCE | 已启用 |

---

## 5. 已知事项

1. **支付宝回调**: 外部支付渠道，真实回调需要支付宝服务器主动通知；测试环境通过手动 API 标记模拟
2. **微信支付**: 与支付宝类似，未在本次测试覆盖
3. **钱包限额**: 提现受 `MerchantWalletLimit` 配置限制（单笔/日累计），本次使用默认值未超限
4. **幂等性**: 提现使用 Redis 幂等 key `payment:idempotent:order:WITHDRAW:{merchantId}:{userId}:{withdrawNo}`
5. **钱包回调通道分离**: 钱包回调 URL 已按通道分离（`/api/notify/wallet/alipay/...` 和 `/api/notify/wallet/wechat/...`），支付宝使用 `@RequestParam` 传参验签，微信使用 HTTP Header（Wechatpay-Serial 等）AES-GCM 解密。当前钱包操作为同步完成，回调端点仅记录通知日志。
