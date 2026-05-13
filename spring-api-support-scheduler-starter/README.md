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

---

## Job 模块 SPI 重构规划

### 背景

当前 `JobConfig` 是单体 God-object，持有 `SysJobMapper`/`SysJobLogMapper` 直接依赖 MyBatis-Plus。
下游 16 个模块编译依赖 `spring-support-job-starter`，即使只需枚举或模型类也必须引入整个 starter
（含 MyBatis、数据源等全部传递依赖）。

### 目标

将核心接口与模型提取到 `utils-support-common-starter`，提供内存 SPI 实现；
`spring-support-job-starter` 提供数据库 SPI 实现，`JobConfig` 退化为委托层。

### 架构变化

```
utils-support-common-starter (com.chua.common.support.job)
├── enums/             ← SchedulerTypeEnum, MisfireStrategyEnum, TriggerTypeEnum,
│                        JobDispatchModeEnum, JobStorageModeEnum, GlueTypeEnum
├── model/             ← JobInfo, JobLogInfo, JobScheduleInfo, TriggerParam,
│                        RemoteJobTriggerRequest, RemoteJobTriggerResponse
└── spi/               ← SPI 接口 + 内存实现
    ├── JobRepository        ← loadById/saveLog/updateLog/scheduleJobQuery/scheduleUpdate/lock
    ├── InMemoryJobRepository  ← @Spi("memory"), ConcurrentHashMap 实现
    ├── JobHandler            ← execute/init/destroy
    ├── TriggerHandler        ← start/stop
    └── JobExecutionCallback  ← onCallback

spring-support-job-starter (com.chua.starter.job)
├── entity/SysJob implements JobInfo, JobScheduleInfo   ← MyBatis 注解不丢失
├── entity/SysJobLog implements JobLogInfo
├── repository/DatabaseJobRepository @Spi("jdbc")       ← 委托 Mapper
├── scheduler/JobConfig → 委托 JobRepository             ← 保持 getInstance() 兼容
└── META-INF/extensions/com.chua.common.support.job.spi.JobRepository
```

### 执行步骤

#### Step 1：utils-common 枚举（6 个文件）

从 `spring-support-job-starter` 迁移到 `utils-support-common-starter/src/main/java/com/chua/common/support/job/enums/`：

| 枚举 | 源文件 |
|---|---|
| SchedulerTypeEnum | `job/support/scheduler/SchedulerTypeEnum.java` |
| MisfireStrategyEnum | `job/support/scheduler/MisfireStrategyEnum.java` |
| TriggerTypeEnum | `job/support/scheduler/TriggerTypeEnum.java` |
| JobDispatchModeEnum | `job/support/scheduler/JobDispatchModeEnum.java` |
| JobStorageModeEnum | `job/support/scheduler/JobStorageModeEnum.java` |
| GlueTypeEnum | `job/support/GlueTypeEnum.java` |

去掉原枚举中的 Spring/MyBatis 依赖，保持纯 Java 枚举。

#### Step 2：utils-common 模型接口（6 个文件）

路径：`utils-support-common-starter/src/main/java/com/chua/common/support/job/model/`

| 文件 | 说明 |
|---|---|
| `JobInfo.java` | 接口，定义 jobId/jobName/jobGroup/jobTriggerStatus/jobScheduleType 等 getter |
| `JobLogInfo.java` | 接口，定义 jobLogId/jobId/executorHandler/triggerCode/handleCode 等 getter |
| `JobScheduleInfo.java` | 接口，定义 jobId/jobTriggerStatus/jobTriggerNextTime/jobTriggerLastTime |
| `TriggerParam.java` | DTO，从 job-starter 同名类复制 |
| `RemoteJobTriggerRequest.java` | DTO，从 job-starter remote 包复制 |
| `RemoteJobTriggerResponse.java` | DTO，从 job-starter remote 包复制 |

#### Step 3：utils-common SPI 接口（4 个文件）

路径：`utils-support-common-starter/src/main/java/com/chua/common/support/job/spi/`

| 接口 | 核心方法 |
|---|---|
| `JobRepository` | `loadById()` `saveLog()` `updateLog()` `scheduleJobQuery()` `scheduleUpdate()` `lock()`；内部接口 `JobLock { void unlock(); }` |
| `JobHandler` | `execute()` `init()` `destroy()` |
| `TriggerHandler` | `start()` `stop()` |
| `JobExecutionCallback` | `onCallback(JobExecutionCallbackContext)` |

#### Step 4：InMemoryJobRepository

`@Spi("memory")`，基于 `ConcurrentHashMap` 的内存实现。注册文件：

```
META-INF/extensions/com.chua.common.support.job.spi.JobRepository
memory=com.chua.common.support.job.spi.InMemoryJobRepository
```

#### Step 5：实体实现接口

- `SysJob implements JobInfo, JobScheduleInfo` — 现有 getter 加 `@Override`
- `SysJobLog implements JobLogInfo` — 现有 getter 加 `@Override`
- MyBatis-Plus 注解全部保留

#### Step 6：DatabaseJobRepository

`@Spi("jdbc")`，注入 `SysJobMapper` 和 `SysJobLogMapper`，实现 `JobRepository` 全部方法。
将 `JobConfig` 中与 DB 相关的操作移到此类。

SPI 注册文件：
```
META-INF/extensions/com.chua.common.support.job.spi.JobRepository
jdbc=com.chua.starter.job.support.repository.DatabaseJobRepository
```

#### Step 7：JobConfig 退化为委托层

- 内部通过 `ServiceProvider` 或 Spring 获取 `JobRepository` 实例
- 所有 DB 操作方法委托给 `DatabaseJobRepository`
- 保持 `getInstance()` 单例兼容性（可标注 `@Deprecated`）
- 保留 `runLocal()` 本地执行逻辑

#### Step 8：自动配置注册

在 `JobConfiguration` 中确保 `DatabaseJobRepository` Bean 注册到 Spring 容器，
使 `ServiceProvider` 能通过 classpath 扫描发现实现。

### 向后兼容

- `SysJob` 现有字段和 MyBatis-Plus 注解全部保留，只加 `implements`
- `JobConfig.getInstance()` 单例保持不动
- 旧 `TriggerParam`、`RemoteJobTriggerRequest/Response` 可直接替换为新 DTO
- `JobHandler`、`TriggerHandler`、`JobExecutionCallback` 旧接口改为 `extends spi.版本`

### 验证方式

1. 编译 `utils-support-common-starter` 确认 SPI 接口和 InMemoryJobRepository 编译通过
2. 编译 `spring-support-job-starter` 确认 DatabaseJobRepository 和实体修改编译通过
3. 编译 `spring-api-support-scheduler-starter` 确认旧引用路径不受影响（包名不变）
4. `ServiceProvider.of(JobRepository.class).getNewExtension("memory")` → InMemoryJobRepository
5. `ServiceProvider.of(JobRepository.class).getNewExtension("jdbc")` → DatabaseJobRepository
