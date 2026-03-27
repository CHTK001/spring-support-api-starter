# 支付 Job 远程调度测试清单

## 文档信息

- 最近更新：2026-03-24 21:55:35
- 归属模块：`spring-api-support-payment-starter`
- 测试目标：验证 `scheduler-starter` 中心推送 + `payment-starter` 远程执行器模式
- 结果口径：所有运营接口统一返回框架 `ReturnResult`，不再返回支付模块自定义 `Result(code=200)`

## 一、测试环境

| 项目 | 值 |
| --- | --- |
| 调度中心 | `http://127.0.0.1:18083/scheduler/api` |
| 支付服务 | `http://127.0.0.1:18190/payment/api` |
| OAuth 登录 | `http://127.0.0.1:18083/scheduler/api/v2/user/login` |
| 调度中心版本 | `spring-api-support-scheduler-starter-4.0.0.37.jar` |
| 支付服务版本 | `spring-api-support-payment-starter-4.0.0.37.jar` |
| 支付启动参数 | `--spring.profiles.active=browser,job --server.port=18190 --plugin.job.config-table-enabled=false --plugin.job.remote-executor.enabled=true --plugin.job.remote-executor.access-token=job-secret` |
| 调度中心启动参数 | `--server.port=18083 --spring.config.additional-location=file:/H:/workspace/2/spring-support-api-parent/.runtime/job-integration/scheduler-integration.yml` |
| 登录账号 | `sa` |
| 登录方式 | `SYSTEM` |

## 二、本轮验证结论

- [x] 支付运营接口已统一回到框架 `ReturnResult`
- [x] `payment` 侧 `overview/tasks/update/trigger` 代理链路可用
- [x] `scheduler` 平台可直接查询 `payment` 命名空间任务
- [x] `scheduler` 平台日志页已可看到 `payment-notify-cleanup` 执行记录
- [x] `payment` 在远程执行器模式下已关闭本地表轮询
- [x] `payment /api/ops/scheduler/tasks` 未携带 token 时返回单层 `ReturnResult` 错误协议
- [ ] `payment /api/ops/overview` 当前仍可匿名访问；本轮使用 `browser` profile，`plugin.oauth.enable=false`，是否符合目标部署预期仍需确认
- [ ] 本轮未重新打开浏览器验证运营中心页面展示

## 三、测试清单

| 编号 | 测试项 | 请求 | 预期 | 实际结果 | 结论 |
| --- | --- | --- | --- | --- | --- |
| T01 | 调度中心登录 | `POST /scheduler/api/v2/user/login` | 返回统一 `ReturnResult` 并拿到访问令牌 | `code=00000`，成功获取 `accessToken` | 通过 |
| T02 | 支付运营总览返回协议 | `GET /payment/api/api/ops/overview` | 返回 `ReturnResult`，`data` 为总览对象 | `code=00000`；`callbackAudits=5`；`orderNumberStrategies=3` | 通过 |
| T03 | 支付任务列表返回协议 | `GET /payment/api/api/ops/scheduler/tasks` | 返回 `ReturnResult`，可看到 `payment` 命名空间任务 | `code=00000`；任务数 `1`；`taskKey=payment-notify-cleanup`；`taskName=payment-notify-cleanup-center-20260324192620`；`nextExecutionTime=2026-03-24 22:00:00`；`lastRunStatus=SUCCESS`；`lastRunMessage=执行成功，耗时: 52ms` | 通过 |
| T04 | 支付任务更新 | `PUT /payment/api/api/ops/scheduler/tasks/payment-notify-cleanup` | 经 payment 代理更新 scheduler 平台任务 | `code=00000`；`cronExpression=0 0/30 * * * ?`；`enabled=true`；`nextExecutionTime=2026-03-24 22:00:00` | 通过 |
| T05 | 支付任务手工触发 | `POST /payment/api/api/ops/scheduler/tasks/payment-notify-cleanup/trigger` | payment 代理触发 scheduler 平台任务 | `code=00000`；`scheduled=true`；`description=integration test dispatch`；`nextExecutionTime=2026-03-24 22:00:00`；`lastRunMessage=执行成功，耗时: 322ms` | 通过 |
| T06 | 回调日志分页返回协议 | `GET /payment/api/api/ops/notify/log/page?pageNum=1&pageSize=5` | 返回 `ReturnResult` + 分页对象 | `code=00000`；`total=0` | 通过 |
| T07 | 回调异常分页返回协议 | `GET /payment/api/api/ops/notify/error/page?pageNum=1&pageSize=5` | 返回 `ReturnResult` + 分页对象 | `code=00000`；`total=0` | 通过 |
| T08 | 平台侧任务查询 | `GET /scheduler/api/v1/job-platform/payment/task/list` | 可直接看到 `payment` 命名空间任务 | `code=00000`；任务数 `1`；`jobId=1`；`taskKey=payment-notify-cleanup`；`nextExecutionTime=2026-03-24 22:00:00`；`lastRunMessage=执行成功，耗时: 322ms` | 通过 |
| T09 | 平台侧任务日志查询 | `GET /scheduler/api/v1/job-platform/payment/job-log/page?pageNum=1&pageSize=5&beanName=payment-notify-cleanup` | 触发后可看到最近执行日志 | `code=00000`；`total=4`；最新 `jobLogId=4`；`jobLogTriggerCode=SUCCESS`；`jobLogTriggerAddress=http://127.0.0.1:18190/payment/api`；`jobLogTriggerMsg=执行成功，耗时: 322ms` | 通过 |
| T10 | 支付任务列表未登录错误协议 | `GET /payment/api/api/ops/scheduler/tasks`（不带 `x-oauth-token`） | 返回单层 `ReturnResult` 错误，不出现嵌套 `Result(code=200/500)` | `code=C0400S0000`；`msg` 包含 `调用调度平台失败` 和 `用户信息不存在/请重新登录!` | 通过 |
| T11 | 支付总览匿名访问现状 | `GET /payment/api/api/ops/overview`（不带 `x-oauth-token`） | 当前实现下验证匿名访问行为 | `code=00000`；仍可返回总览数据；本轮运行配置含 `browser` profile，`plugin.oauth.enable=false` | 关注 |

## 四、关键返回样例

### 4.1 支付运营接口统一协议

```json
{
  "code": "00000",
  "data": [
    {
      "taskKey": "payment-notify-cleanup",
      "taskName": "payment-notify-cleanup-center-20260324192620",
      "cronExpression": "0 0/30 * * * ?",
      "enabled": true
    }
  ],
  "msg": "",
  "timestamp": 1774354558xxx
}
```

说明：

- 已不再返回 `com.chua.payment.support.common.Result`
- `PaymentOpsController` 返回原始业务对象，统一由公共响应层包装为 `ReturnResult`

### 4.2 平台侧任务查询结果

```json
{
  "code": "00000",
  "data": [
    {
      "jobId": 1,
      "taskKey": "payment-notify-cleanup",
      "nextExecutionTime": "2026-03-24 22:00:00",
      "lastRunMessage": "执行成功，耗时: 322ms"
    }
  ],
  "msg": "",
  "timestamp": 1774354558xxx
}
```

### 4.3 平台侧任务日志结果

```json
{
  "code": "00000",
  "data": {
    "total": 4,
    "records": [
      {
        "jobLogId": 4,
        "jobLogTriggerBean": "payment-notify-cleanup",
        "jobLogTriggerCode": "SUCCESS",
        "jobLogExecuteCode": "SUCCESS",
        "jobLogTriggerAddress": "http://127.0.0.1:18190/payment/api",
        "jobLogTriggerMsg": "执行成功，耗时: 322ms"
      }
    ]
  },
  "msg": "",
  "timestamp": 1774360431xxx
}
```

### 4.4 未登录错误协议样例

```json
{
  "code": "C0400S0000",
  "msg": "调用调度平台失败: 403 Forbidden ... 用户信息不存在/请重新登录!",
  "timestamp": 1774360485xxx
}
```

## 五、已知问题

- 本轮联调使用 `browser` profile，`plugin.oauth.enable=false`，因此 `payment /api/ops/overview` 当前可匿名访问；如果目标环境要求运营接口统一登录，需要在部署配置中启用 OAuth 后再回归。
- 本轮是接口级联调，不包含浏览器页面实际点击验证。

## 六、后续测试建议

- [ ] 明确 `payment` 运营接口的部署鉴权策略；如果目标环境要求统一登录，需要将 `plugin.oauth.enable` 调整为 `true` 后再做一轮回归
- [ ] 启动浏览器验证支付运营中心调度页，确认前端展示和按钮行为与接口结果一致
- [ ] 补一条失败任务样本，验证平台侧是否能看到失败日志、错误信息与重试入口
