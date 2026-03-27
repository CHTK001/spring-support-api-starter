# spring-api-support-scheduler-starter

## 📖 模块简介

**调度任务应用** - 提供分布式任务调度、定时任务管理、任务监控等功能。

## ✨ 核心功能

### ⏰ 任务调度

- ✅ 定时任务管理
- ✅ Cron 表达式支持
- ✅ 任务执行日志
- ✅ 任务失败重试
- ✅ 多业务命名空间任务表

### 📊 任务监控

- ✅ 任务执行状态监控
- ✅ 任务执行时间统计
- ✅ 任务失败告警
- ✅ 任务执行历史

### 🔄 分布式调度

- ✅ 分布式锁支持
- ✅ 任务分片执行
- ✅ 故障转移
- ✅ 负载均衡

## 🚀 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 2. 配置文件

**主配置文件**：`application.yml`

```yaml
spring:
  application:
    name: scheduler
  profiles:
    active: dev

server:
  port: 18083
  servlet:
    context-path: /scheduler/api
```

**插件配置文件**：`config/application-plugin.yml`

```yaml
plugin:
  scheduler:
    server:
      # 启用调度服务（默认true）
      enable: true
```

### 3. 启动服务

#### IDE 启动

运行 `com.chua.starter.scheduler.SchedulerApplication`

#### 命令行启动

```bash
cd spring-api-support-scheduler-starter
mvn spring-boot:run
```

#### 浏览器/H2 演示模式

如果只是要在本机快速验证 `job-console`、命名空间表初始化和简单登录，可以直接启用内置的 `browser` profile：

```bash
cd spring-api-support-scheduler-starter/target
java -Xmx768m -XX:MaxMetaspaceSize=256m -jar spring-api-support-scheduler-starter-4.0.0.37.jar --spring.profiles.active=browser
```

这个 profile 会自动：

- 切到 H2 内存库，避免依赖本地 MySQL/Redis
- 关闭 OAuth 客户端与远程分发轮询
- 使用 `DROP_CREATE` 重建 `sys_job*` 和命名空间前缀派生表
- 保留 `job-console` 的简单账号密码登录，默认仍是 `admin/admin123456`

### 4. 简单控制台

调度中心内置了一个轻量 `job-console` 静态页，适合直接随 Spring 模块发布：

- 控制台首页：`/scheduler/api/job-console`
- 登录页：`/scheduler/api/job-console/login`
- 登录状态：`/scheduler/api/job-console/auth/status`
- 登录接口：`POST /scheduler/api/job-console/auth/login`
- 控制台会话 API：`/scheduler/api/job-console/api/**`

默认开启简单账号密码校验，默认账号密码为：

- 用户名：`admin`
- 密码：`admin123456`

这套页面不再在某个业务 app 下单独维护，而是统一来自 monorepo 里的 Spring 共享页应用：

- 共享页面入口：[pages/spring/src/index.ts](/H:/workspace/2/vue-support-parent-starter/pages/spring/src/index.ts)
- 发布应用：[apps/vue-support-spring-pages-starter/README.md](/H:/workspace/2/vue-support-parent-starter/apps/vue-support-spring-pages-starter/README.md)

`job-console` 默认不再直连 `/v1/job-platform/**`。页面构建后的默认根路径是 `/job-console/api/**`，登录成功后的 Session 就能直接驱动页面取数和管理任务；只有在你主动改成直连平台 API 时，才需要额外填写 OAuth 认证头。

## 📋 API 接口

### 任务管理接口

```
GET    /v1/job-platform/namespaces                    # 查询任务命名空间
GET    /v1/job-platform/{namespace}/job/page          # 任务分页查询
GET    /v1/job-platform/{namespace}/job/{id}          # 获取任务详情
POST   /v1/job-platform/{namespace}/job               # 创建任务
PUT    /v1/job-platform/{namespace}/job/{id}          # 更新任务
DELETE /v1/job-platform/{namespace}/job/{id}          # 删除任务
POST   /v1/job-platform/{namespace}/job/{id}/start    # 启动任务
POST   /v1/job-platform/{namespace}/job/{id}/stop     # 停止任务
POST   /v1/job-platform/{namespace}/job/{id}/trigger  # 立即执行任务
GET    /v1/job-platform/{namespace}/job-log/page      # 任务日志分页查询
```

### 任务日志接口

```
GET    /v1/job/log/page           # 任务日志分页查询
GET    /v1/job/log/{id}           # 获取日志详情
DELETE /v1/job/log/{id}           # 删除日志
POST   /v1/job/log/clear          # 清空日志
```

## ⚙️ 配置说明

### 功能开关配置

**配置文件**：`config/application-plugin.yml`

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
        default:
          job: sys_job
          log: sys_job_log
          log-detail: sys_job_log_detail
          log-backup: sys_job_log_backup
        payment:
          prefix: payment
          executors:
            - name: payment-local
              base-url: http://127.0.0.1:18081/payment/api
              access-token: job-secret
```

命名空间用于把不同业务的任务表隔离开。调度平台通过 `namespace` 选择对应物理表，但字段定义保持 `spring-support-job-starter` 内置模型不变。未显式指定完整表名时，会按前缀自动派生出 `payment_sys_job / payment_sys_job_log / payment_sys_job_log_detail / payment_sys_job_log_backup`。

当命名空间配置了 `executors` 后，`scheduler-starter` 会承担类似 XXL-Job admin 的职责：

- 轮询命名空间任务表
- 计算下次触发时间并抢占本次触发
- 调用远程业务服务暴露的 `/v1/job-executor/dispatch`
- 由业务服务里的 `spring-support-job-starter` 执行真正的 `@Job` 方法

## 支付任务接入说明

当前 `scheduler-starter` 已支持管理 `payment` 命名空间对应的任务表，例如：

- `payment_sys_job`
- `payment_sys_job_log`
- `payment_sys_job_log_detail`
- `payment_sys_job_log_backup`

但要注意：

- 仅有“任务表管理能力”并不等于“任务可执行”
- 如果某类任务对应的 Bean 不在 `scheduler-starter` 进程内，平台只能管表，不能真正执行该任务

因此对支付任务有两种模式：

1. 本地执行模式
   - `spring-api-support-payment-starter` 使用 `plugin.payment.scheduler.engine=job`
   - 支付服务自己装载支付任务 Bean，并通过 `spring-support-job-starter` 本地轮询执行
   - `scheduler-starter` 只作为统一任务管理平台

2. 中心推送模式
   - `scheduler-starter` 轮询 `payment` 命名空间任务表，并把命中的任务推送到配置的 payment 执行器
   - payment 服务需要开启 `plugin.job.remote-executor.enabled=true`
   - payment 服务应关闭 `plugin.job.config-table-enabled`，避免和调度中心同时轮询同一批任务
   - 如果 payment 执行器也访问同一套 job 表，日志仍会落到统一的 `payment_sys_job_log*`

当前仓库已经具备这两种模式的基础设施，但第 2 种模式还需要你按部署环境补齐执行器地址和访问口令。

## 🔧 使用示例

### 创建定时任务

```bash
curl -X POST http://localhost:18083/scheduler/api/v1/job \
  -H "Content-Type: application/json" \
  -H "x-oauth-token: your_token" \
  -d '{
    "jobName": "数据同步任务",
    "jobGroup": "SYNC",
    "cronExpression": "0 0 2 * * ?",
    "beanName": "dataSyncJob",
    "methodName": "execute",
    "params": "{\"type\":\"full\"}",
    "status": 1
  }'
```

### 立即执行任务

```bash
curl -X POST http://localhost:18083/scheduler/api/v1/job/1/execute \
  -H "x-oauth-token: your_token"
```

## 🏗️ 依赖关系

```
spring-api-support-scheduler-starter (调度应用)
    ↓ 依赖
spring-api-support-common-oauth-starter (OAuth认证模块)
    ↓ 依赖
spring-api-support-common-entity-starter (实体定义)
spring-api-support-common-configuration-starter (配置定义)
```

## 📊 数据库表结构

### sys_job - 任务表

```sql
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(100) NOT NULL COMMENT '任务名称',
  `job_group` varchar(50) COMMENT '任务分组',
  `cron_expression` varchar(100) COMMENT 'Cron表达式',
  `bean_name` varchar(100) COMMENT 'Bean名称',
  `method_name` varchar(100) COMMENT '方法名称',
  `params` text COMMENT '参数',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-停止，1-运行',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`job_id`)
) COMMENT='定时任务表';
```

### sys_job_log - 任务日志表

```sql
CREATE TABLE `sys_job_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `job_id` bigint NOT NULL COMMENT '任务ID',
  `execute_time` datetime COMMENT '执行时间',
  `duration` bigint COMMENT '执行时长(ms)',
  `status` tinyint COMMENT '状态：0-失败，1-成功',
  `error_msg` text COMMENT '错误信息',
  PRIMARY KEY (`log_id`)
) COMMENT='任务执行日志表';
```

## 📝 API 文档

启动服务后，访问 Swagger 文档：

```
http://localhost:18083/scheduler/api/doc.html
```

## 🐛 故障排除

### 常见问题

#### 1. 服务无法启动

检查配置：

```yaml
plugin:
  scheduler:
    server:
      enable: true # 确保已启用
```

#### 2. 任务不执行

- 检查任务状态是否为"运行"
- 检查 Cron 表达式是否正确
- 查看任务日志获取错误信息

#### 3. 分布式锁失败

检查 Redis 配置：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

## 🔗 相关模块

- [spring-api-support-common-oauth-starter](../spring-api-support-common-oauth-starter) - OAuth 认证模块
- [spring-api-support-monitor-starter](../spring-api-support-monitor-starter) - 监控运维模块

## 📄 许可证

本项目采用 Apache License 2.0 许可证。

---

**作者**: CH  
**版本**: 4.0.0  
**更新时间**: 2024/12/11
