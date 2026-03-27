# 请求追踪策略模块集成手册

## 1. 适用范围

本文档说明如何在 Spring 模块中接入请求追踪策略能力，包括：

- 注解采集
- 配置存储
- 原始记录存储
- 行为结果存储
- AI 摘要联动
- 运行时热更新接口

## 2. 模块依赖

### 2.1 最小依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-configuration-starter</artifactId>
</dependency>
```

### 2.2 使用数据库存储时

如果希望使用以下默认数据库实现：

- `configStorageType=database`
- `rawStorageType=database`
- `behaviorStorageType=database`

还需要引入：

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-service-starter</artifactId>
</dependency>
```

数据库版 SPI 实现在该模块中提供，不在 `common-configuration-starter` 内部直接落表。

### 2.3 使用 Redis 配置存储时

需要具备 Redis 依赖与连接配置：

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-support-redis-starter</artifactId>
</dependency>
```

### 2.4 使用 AI 摘要时

若需要真实大模型摘要能力，需要准备全局 `llm` 配置和对应 `BigModelClient`。
未配置时不会报错，会自动回退为规则摘要。

## 3. 接入步骤

### 3.1 标注策略注解

只对标注 `@RequestTraceStrategy` 的控制器或方法生效。

```java
@RestController
@RequestMapping("/api/order")
@RequestTraceStrategy("order:behavior")
public class OrderController {

    @GetMapping("/detail")
    public ReturnResult<?> detail() {
        return ReturnResult.ok();
    }
}
```

也可以只标在方法上：

```java
@PostMapping("/submit")
@RequestTraceStrategy("order:submit")
public ReturnResult<?> submit() {
    return ReturnResult.ok();
}
```

### 3.2 配置运行时属性

```yaml
request:
  trace:
    strategy:
      config-storage-type: database
      ai-summary-enabled: true
      ai-model: deepseek-chat
      ai-rate-limit-enabled: true
      ai-rate-limit-window-seconds: 60
      ai-rate-limit-max-requests: 30
      ai-timeout-ms: 1500
```

可选值：

- `database`
- `redis`
- `memory`

非法值会自动回退到 `database`。

说明：

- 以上 `ai-*` 仅在“持久化配置不存在”时作为运行时默认值生效。
- 一旦页面或接口已经写入持久化配置，后续优先读取持久化配置。

### 3.3 配置动态策略

动态策略通过接口维护，不需要重启应用。核心字段如下：

```json
{
  "enabled": true,
  "rawStorageType": "file",
  "behaviorStorageType": "database",
  "fileStoragePath": "logs/request-trace",
  "aiSummaryEnabled": true,
  "aiModel": "deepseek-chat",
  "aiRateLimitEnabled": true,
  "aiRateLimitWindowSeconds": 60,
  "aiRateLimitMaxRequests": 30,
  "aiTimeoutMs": 1500,
  "retentionMinutes": 30,
  "maxEvents": 3000,
  "defaultWindowMinutes": 10,
  "maxWindowMinutes": 60,
  "maxTopBuckets": 10,
  "maxSuspiciousEvents": 20,
  "burstEnabled": true,
  "burstWindowSeconds": 60,
  "burstThreshold": 25,
  "hotspotEnabled": true,
  "hotspotWindowMinutes": 5,
  "hotspotThreshold": 50,
  "spamEnabled": true,
  "spamWindowSeconds": 30,
  "spamThreshold": 12,
  "crawlerEnabled": true,
  "crawlerWindowMinutes": 5,
  "crawlerRequestThreshold": 30,
  "crawlerDistinctPathThreshold": 20,
  "slowRequestEnabled": true,
  "slowRequestThresholdMs": 1500,
  "errorBurstEnabled": true,
  "errorBurstWindowSeconds": 60,
  "errorBurstThreshold": 6,
  "tokenReuseEnabled": true,
  "tokenReuseWindowMinutes": 10,
  "tokenReuseMinEvents": 3,
  "fingerprintDriftEnabled": true,
  "fingerprintDriftWindowMinutes": 30,
  "fingerprintDriftMinEvents": 3,
  "userPreferenceDriftEnabled": true,
  "userPreferenceWindowMinutes": 30,
  "userPreferenceBaselineCount": 5,
  "userPreferenceDominantCount": 4
}
```

## 4. SPI 存储说明

### 4.1 配置存储

- `database`
- `redis`
- `memory`

### 4.2 原始记录存储

- `file`
- `database`

### 4.3 行为结果存储

- `database`
- `memory`

如果当前运行环境没有装配对应 SPI，实现列表仍会展示默认类型，但实际集成时应确认依赖已齐全。

## 5. 数据表与文件

使用数据库存储时，核心数据对象为：

- `sys_request_trace_raw`
- `sys_request_trace_behavior`

使用文件存储时，原始请求默认写入：

- `logs/request-trace/request-trace-YYYY-MM-DD.log`

## 6. 运行时接口

### 6.1 获取分析快照

```http
GET /v1/request-trace/analytics?minutes=10
```

### 6.2 获取当前配置

```http
GET /v1/request-trace/analytics/config
```

### 6.3 更新配置

```http
PUT /v1/request-trace/analytics/config
Content-Type: application/json
```

### 6.4 刷新配置缓存

```http
POST /v1/request-trace/analytics/config/refresh
```

### 6.5 获取可选项

```http
GET /v1/request-trace/analytics/options
```

以上接口统一返回 `ReturnResult`。

### 6.6 页面控制台

静态控制台页面地址：

```http
GET /request-trace-live.html
```

页面特性：

- 展示 AI 分析过程结果，包括状态、来源、模型、耗时、回退原因和提示词摘要。
- 支持热更新 AI 配置，包括开关、模型、限流窗口、限流次数和超时。
- 按 `ReturnResult.code` 判断接口成功与失败，不再只看 HTTP 200。

## 7. 推荐集成验证

### 7.1 文件存储模式

1. 将 `rawStorageType` 设为 `file`。
2. 调用一个标注了 `@RequestTraceStrategy` 的接口。
3. 检查 `logs/request-trace/` 是否生成日志文件。
4. 校验文件中是否包含 `traceId`、IP、浏览器、耗时、URL。

### 7.2 数据库存储模式

1. 引入 `spring-api-support-common-service-starter`。
2. 将 `config-storage-type`、`rawStorageType`、`behaviorStorageType` 设为 `database`。
3. 调用带注解接口并触发可疑行为。
4. 检查 `sys_setting`、`sys_request_trace_raw`、`sys_request_trace_behavior` 是否有数据。

### 7.3 Redis 配置模式

1. 配置 Redis 连接。
2. 将 `config-storage-type` 设为 `redis`。
3. 调用更新配置接口。
4. 刷新配置并校验值是否一致。

### 7.4 AI 摘要模式

1. 开启 `aiSummaryEnabled=true`。
2. 配置可用的 `llm` 客户端。
3. 触发可疑请求。
4. 校验行为记录摘要是否优先使用模型输出。
5. 断开模型后再触发一次，确认能回退为规则摘要。
6. 将 `aiRateLimitMaxRequests` 调小到 `1`，连续触发两次可疑请求，确认第二次状态为 `rate_limited`。

## 8. 常见问题

### 8.1 为什么接口没有产生追踪数据

优先检查：

- 是否标注了 `@RequestTraceStrategy`
- 是否走到了 Spring MVC `HandlerMethod`
- 是否调用的是被排除的内部分析接口

### 8.2 为什么选择了 database 但没有落库

优先检查：

- 是否引入 `spring-api-support-common-service-starter`
- Mapper 是否已被 Spring 容器管理
- 数据库初始化脚本是否已执行

### 8.3 为什么 AI 没有返回模型结果

优先检查：

- `aiSummaryEnabled` 是否开启
- `llm` 全局配置是否有效
- `aiModel` 是否填写正确

未满足时会自动回退为规则摘要，这是预期行为。

## 9. 推荐联调命令

```bash
MAVEN_OPTS="-Xms256m -Xmx768m" mvn -pl spring-api-support-common-configuration-starter -am -DskipITs test
```

如需联动数据库存储实现，建议同时验证：

```bash
MAVEN_OPTS="-Xms256m -Xmx1024m" mvn -pl spring-api-support-common-service-starter -am test -DskipITs
```
