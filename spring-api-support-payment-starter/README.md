# spring-api-support-payment-starter

独立支付服务模块。

## 作用

- 以 `spring-support-payment-starter` 为唯一支付能力内核
- 在 `spring-support-api-parent` 内提供独立启动、独立打包、独立部署入口
- 统一承载商户、渠道、订单、退款、钱包、微信支付分与运营中心接口
- 默认显式开启支付调度与运营台接口，作为中心化支付服务使用

## 启动类

- `com.chua.starter.payment.PaymentApplication`

## 文档

- `支付功能改造计划清单.MD`
- `支付功能测试清单.MD`
- `支付模块完整测试报告.MD`
- `支付中心化接入说明.MD`
- `支付Job远程调度测试清单.MD`

## 默认访问地址

- 服务端口: `18190`
- 上下文: `/payment/api`

## 可选运行模式

- 默认: 内置支付调度
- 可选: `job` 模式，通过 `application-job.yml` 接入 `spring-support-job-starter`

`job` 模式下建议配合 `spring-api-support-scheduler-starter` 作为统一任务平台使用。

## 调度开关

- `plugin.payment.scheduler.enabled=true`
- `plugin.payment.ops.enabled=true`

## Simple Console 代理模式

- `plugin.payment.scheduler.platform-access-mode=auto`
- `auto`: 有 `x-oauth-token/Authorization` 时走 scheduler 正式平台 API；无鉴权头时自动切到 scheduler `job-console` 会话模式
- `platform`: 始终走 `/v1/job-platform/**`
- `console`: 始终走 `/job-console/api/**`

简单页联调默认推荐 `auto`，这样 `payment-console` 登录后无需额外 OAuth Token 也能代理任务列表、更新和立即执行。

嵌入式业务模块如果只想复用支付核心能力，可关闭这两个开关，避免重复执行支付调度任务。

## 推荐模式

- `spring-api-support-payment-starter`: 作为中心化支付服务，显式开启 `scheduler/ops`
- 其它业务模块: 继续依赖 `spring-support-payment-starter`，但关闭 `scheduler/ops`
