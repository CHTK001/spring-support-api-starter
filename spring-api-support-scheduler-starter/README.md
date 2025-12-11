# spring-api-support-scheduler-starter

## 📖 模块简介

**调度任务应用** - 提供分布式任务调度、定时任务管理、任务监控等功能。

## ✨ 核心功能

### ⏰ 任务调度

- ✅ 定时任务管理
- ✅ Cron 表达式支持
- ✅ 任务执行日志
- ✅ 任务失败重试

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

## 📋 API 接口

### 任务管理接口

```
GET    /v1/job/page               # 任务分页查询
GET    /v1/job/{id}               # 获取任务详情
POST   /v1/job                    # 创建任务
PUT    /v1/job/{id}               # 更新任务
DELETE /v1/job/{id}               # 删除任务
POST   /v1/job/{id}/start         # 启动任务
POST   /v1/job/{id}/stop          # 停止任务
POST   /v1/job/{id}/execute       # 立即执行任务
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
  scheduler:
    server:
      # 是否启用调度服务
      # 默认: true
      # 说明: 设置为false时，调度服务不会启动
      enable: true

      # 任务线程池配置
      core-pool-size: 10
      max-pool-size: 20
      queue-capacity: 100

      # 任务失败重试次数
      max-retry-times: 3
```

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
