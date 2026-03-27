# 支付Job模式完整测试清单

## 测试目标

验证 `spring-api-support-payment-starter` 在 `job` 模式下的运营接口、任务代理、前端调度页与支付业务相关接口均可正常工作，并确认返回格式统一为 `ReturnResult`。

## 测试环境

- 测试日期: 2026-03-25
- 支付服务: `spring-api-support-payment-starter`
- 调度中心: `spring-api-support-scheduler-starter`
- OAuth: `spring-api-support-oauth-starter`
- 运行模式: `browser,job`
- 当前状态: 已完成

## 测试清单

| 编号 | 测试项 | 目标 | 状态 | 结果摘要 |
| --- | --- | --- | --- | --- |
| P01 | 服务概览 | `/api/ops/overview` 返回单层 `ReturnResult` | 通过 | 返回 `code=00000`，包含 `callbackAudits=5`、`orderNumberStrategies=3`，无内层 `Result(code=200)` |
| P02 | 调度任务列表 | `/api/ops/scheduler/tasks` 返回代理后的任务列表 | 通过 | 返回 1 条代理任务，`taskKey=payment-notify-cleanup`，`nextExecutionTime=2026-03-24 23:00:00`，最近结果 `SUCCESS` |
| P03 | 调度任务更新 | `/api/ops/scheduler/tasks/{taskKey}` 可更新 cron 和启停 | 通过 | 通过 payment 代理先停用、改为 `0 15/30 * * * ?` 再恢复 `0 0/30 * * * ?`；`scheduled` 和 `nextExecutionTime` 均同步变化 |
| P04 | 调度任务触发 | `/api/ops/scheduler/tasks/{taskKey}/trigger` 可立即执行 | 通过 | 代理触发返回 `code=00000`，最近一次执行结果刷新为 `SUCCESS`，耗时 `134ms` |
| P05 | 回调日志分页 | `/api/ops/notify/log/page` 返回单层 `ReturnResult` | 通过 | 返回 `code=00000`，当前 `total=0`，分页结构完整且无双包装 |
| P06 | 回调异常分页 | `/api/ops/notify/error/page` 返回单层 `ReturnResult` | 通过 | 返回 `code=00000`，当前 `total=0`，分页结构完整且无双包装 |
| P07 | 未认证错误路径 | 未带 token 的受保护接口返回单层 `ReturnResult` | 通过 | 未认证访问任务列表返回 payment 侧单层错误 `C0400S0000`，消息中包含 scheduler `403 Forbidden`，无内层 `Result(code=200)` |
| P08 | 前端调度页渲染 | `/operations` 调度任务页展示任务状态、cron、最近结果 | 通过 | 通过静态代理恢复前端后，调度页成功展示 `payment-notify-cleanup-center-20260324192620`，显示 `Cron=0 0/30 * * * ?`、`下次执行=2026-03-25 00:00:00`、最近结果 `成功`、最近消息 `执行成功，耗时: 10ms` |
| P09 | 前端立即执行 | 页面点击“立即执行”后任务结果刷新 | 通过 | 在页面 DOM 中直接触发“立即执行”按钮后，浏览器记录到 `POST /api/ops/scheduler/tasks/payment-notify-cleanup/trigger`，随后自动发起 `GET /api/ops/scheduler/tasks`、`GET /api/ops/notify/error/page`、`GET /api/ops/notify/log/page`，最新任务结果刷新为 `执行成功，耗时: 581ms` |
| P10 | 返回值统一 | 运营接口不再出现 payment `Result(code=200)` 双包装 | 通过 | 已核对 `overview/tasks/update/trigger/notify-log/notify-error` 六类接口，统一返回单层 `ReturnResult(code=00000|C0400S0000)` |
| P11 | 低内存浏览器任务模式启动 | `browser,job` 在 H2 下可用低内存启动，控制台可登录且 job 轮询命中 `payment_sys_job` | 通过 | 使用 `-Xmx768m -XX:MaxMetaspaceSize=256m` 自定义启动后，`/payment-console/auth/status`、登录和 `index.html` 均返回 200；日志显示 `Payment][MyBatis]` 已装配动态表拦截器，轮询 SQL 已从 `sys_job` 切换为 `payment_sys_job` |
| P12 | Simple Console 任务代理 | 无 OAuth Token 的 payment 简单页可自动登录 scheduler 控制台并代理任务接口 | 通过 | `payment-console` 登录态下访问 `/api/ops/scheduler/tasks` 返回 `code=00000`；payment 会先向 scheduler `job-console/auth/login` 申请会话，再复用 Cookie 访问 `/job-console/api/payment/task/list`，不再命中原先的 `403 用户信息不存在` |

## 执行记录

- 2026-03-24 22:00:00 初始化清单，等待 Job 侧测试完成后开始。
- 2026-03-24 22:55:38 P01 完成：`overview` 返回单层 `ReturnResult`，概览数据结构完整。
- 2026-03-24 22:55:38 P02 完成：支付服务成功代理 scheduler 平台任务列表，任务状态与中心端一致。
- 2026-03-24 22:56:21 P03 完成：支付服务代理更新任务成功，启停和 cron 变更可直接透传到 scheduler 平台。
- 2026-03-24 22:56:21 P04 完成：支付服务代理手动触发成功，任务最近一次执行结果更新为 `SUCCESS`。
- 2026-03-24 22:57:07 P05 完成：回调日志分页返回空记录集，分页结构和返回协议正常。
- 2026-03-24 22:57:07 P06 完成：回调异常分页返回空记录集，分页结构和返回协议正常。
- 2026-03-24 22:57:07 P07 完成：未认证访问任务列表返回 payment 侧单层错误响应，没有双包装。
- 2026-03-24 22:57:07 P10 完成：运营接口统一返回 `ReturnResult`，未再出现 payment 模块自有 `Result(code=200)` 协议。
- 2026-03-24 23:39:48 P08 完成：前端运营中心调度面板成功渲染任务行，页面可见任务键、cron、下次执行、最近结果与最近消息；截图见 `.runtime/job-integration/payment-operations-scheduler-tab-pass.png`。
- 2026-03-24 23:49:56 P09 完成：页面“立即执行”按钮成功发起 `POST /api/ops/scheduler/tasks/payment-notify-cleanup/trigger`，随后自动刷新任务列表与回调分页；平台日志新增 `jobLogId=9`，任务最近消息更新为 `执行成功，耗时: 581ms`。
- 2026-03-25 19:40:55 P11 完成：使用 `java @payment-browser-job.args` 在 `browser,job` 下以 `-Xmx768m -XX:MaxMetaspaceSize=256m` 成功启动 payment 服务；`/payment/api/payment-console/auth/status` 返回 `code=00000`，默认 `admin/admin123456` 登录成功，`/payment/api/payment-console/index.html` 返回 200；启动日志确认 `DynamicTableNameInnerInterceptor` 已生效，轮询 SQL 为 `SELECT COUNT(*) AS total FROM payment_sys_job ...`。
- 2026-03-25 22:04:11 P12 完成：重新启动 `payment/scheduler` 简单控制台后，`payment-console` 登录态下访问 `/api/ops/scheduler/tasks` 返回 `code=00000`；代理链路已改为在无 OAuth 头时自动切换到 scheduler `job-console` 会话模式，先登录获取 Cookie，再访问 `/job-console/api/payment/task/list`，对应 smoke 结果见 `.runtime/spring-pages/payment-simple-console-smoke.json`。
