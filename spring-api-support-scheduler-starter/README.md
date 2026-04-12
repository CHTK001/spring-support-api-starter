# Spring API Support Scheduler Starter

`spring-api-support-scheduler-starter` 是调度中心模块。

它负责：

- 统一管理任务表
- 统一轮询待执行任务
- 统一选择执行器
- 统一把任务下发到业务客户端

它不负责真正执行业务 Bean。真正执行 `@Job` 的仍然是各业务服务里的 `spring-support-job-starter`。

## 模块分工

| 角色 | 模块 | 作用 |
| --- | --- | --- |
| 调度中心 | `spring-api-support-scheduler-starter` | 轮询、选路、下发 |
| 执行器客户端 | 业务服务 + `spring-support-job-starter` | 接收、执行、记录日志 |

## 启动入口

启动类：

- `com.chua.starter.scheduler.SchedulerApplication`

## 中心模式完整示例

### 1. STATIC 模式

```yaml
plugin:
  job:
    enable: true
    config-table-enabled: true
    table-init-mode: UPDATE

  scheduler:
    job-platform:
      enabled: true
      remote-dispatch-enabled: true
      dispatch-interval-ms: 1000
      dispatch-batch-size: 200
      namespaces:
        payment:
          prefix: payment
          executor-mode: STATIC
          executors:
            - name: payment-node-1
              base-url: http://127.0.0.1:18081/payment/api
              access-token: job-secret
              dispatch-path: /v1/job-executor/dispatch
              connect-timeout-ms: 3000
              read-timeout-ms: 10000
```

说明：

- `executor-mode=STATIC` 表示中心只使用静态地址
- `executors` 是中心维护的客户端地址列表

### 2. DISCOVERY 模式

```yaml
plugin:
  job:
    enable: true
    config-table-enabled: true
    table-init-mode: UPDATE

  scheduler:
    job-platform:
      enabled: true
      remote-dispatch-enabled: true
      dispatch-interval-ms: 1000
      dispatch-batch-size: 200
      namespaces:
        payment:
          prefix: payment
          executor-mode: DISCOVERY
          discovery:
            enabled: true
            protocol: nacos
            namespace: /job-executor/payment
            base-path: /payment/api
            access-token: job-secret
            dispatch-path: /v1/job-executor/dispatch
            connect-timeout-ms: 3000
            read-timeout-ms: 10000
```

说明：

- `executor-mode=DISCOVERY` 表示中心通过执行器来源 SPI 查找在线节点
- `DISCOVERY` 和 `STATIC` 现在是两个独立 SPI，不再互相回退
- 如果某个 SPI 实现缺失，调度中心仍可启动，只是该命名空间暂时拿不到执行器

## 客户端示例

业务客户端配置见：

- [spring-support-job-starter/README.md](/g:/work/spring-support-parent-starter/spring-support-job-starter/README.md)

最关键的客户端配置只有两点：

- `plugin.job.config-table-enabled=false`
- `plugin.job.remote-executor.enabled=true`

## 执行器来源 SPI

配置项：

- `plugin.scheduler.job-platform.namespaces.<namespace>.executor-mode`

当前内置：

- `STATIC`
- `DISCOVERY`

扩展方式：

1. 实现 `SchedulerExecutorResolver`
2. 在实现类上标注 `@Spi("你的模式名")`
3. 注册到 `META-INF/services/com.chua.starter.scheduler.support.executor.SchedulerExecutorResolver`

## 控制台

中心内置 `job-console`：

- `/job-console`
- `/job-console/login`
- `/job-console/api/**`

默认账号：

- 用户名：`admin`
- 密码：`admin123456`

## 业务验证

业务场景验证结果见：

- [调度单元测试报告.md](/g:/work/spring-support-parent-starter/spring-support-job-starter/调度单元测试报告.md)
