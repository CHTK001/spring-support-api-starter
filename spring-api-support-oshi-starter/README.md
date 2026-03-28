# spring-api-support-oshi-starter

## 模块说明

`spring-api-support-oshi-starter` 是一个独立的 Spring Parent 模块，基于 `utils-support-oshi-starter` 提供本机系统性能采集能力，并把能力接入现有系统设置体系。

它解决的是两件事：

- 让依赖方无需引入 `monitor` 模块，也能获得 OSHI 性能采集、接口和配置初始化能力
- 把系统设置页当前前端已经动态读取的性能监控键补齐到后台，做到“谁依赖谁有能力”

## 依赖关系

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-oshi-starter</artifactId>
</dependency>
```

## 自动装配条件

模块自动装配类：

- `com.chua.starter.oshi.configuration.OshiAutoConfiguration`

生效前提：

- 当前应用是 Servlet Web 应用
- 类路径中存在 `SysSettingMapper`
- 类路径中存在 `SysSettingService`
- 类路径中存在 `LocalSystemDataCollector`

冲突保护：

- 如果类路径中已经存在 `com.chua.starter.monitor.configuration.PerformanceMonitorAutoConfiguration`
- 则本模块不会重复装配，避免和 `spring-api-support-monitor-starter` 冲突

## 已提供能力

### 1. 系统性能采集

- CPU
- 内存
- 磁盘
- 网络
- JVM
- 进程
- 操作系统与主机信息

### 2. 系统设置初始化

启动时自动补齐 `default` 分组下的性能监控设置。

基础键：

- `PerformanceMonitorOpen`
- `PerformanceMonitorSocketPushOpen`
- `PerformanceMonitorRefreshIntervalSeconds`
- `PerformanceMonitorSystemOpen`
- `PerformanceMonitorCpuOpen`
- `PerformanceMonitorMemoryOpen`
- `PerformanceMonitorDiskOpen`
- `PerformanceMonitorNetworkOpen`
- `PerformanceMonitorJvmOpen`
- `PerformanceMonitorProcessOpen`

前端兼容键：

- `ShowFpsMonitor`
- `ShowCpuMonitor`
- `ShowMemoryMonitor`
- `ShowBandwidthMonitor`
- `ShowBatteryMonitor`
- `ShowBluetoothMonitor`
- `ShowScreenMonitor`
- `PerformanceMonitorPosition`
- `PerformanceMonitorMode`
- `PerformanceMonitorLayout`
- `PerformanceMonitorDirection`

说明：

- 上述前端兼容键是当前系统设置前端构建产物实际动态读取的键
- 模块启动时会自动补齐缺失项
- 如果数据库中已存在兼容键但 `valueType/config/remark/sort` 还是旧值，也会自动对齐
- 如果只有旧键存在，`ShowFpsMonitor`、`ShowCpuMonitor`、`ShowMemoryMonitor`、`ShowBandwidthMonitor` 会优先沿用旧键值做迁移初始化

## 接口

控制器：

- `com.chua.starter.oshi.performance.SystemPerformanceController`

接口列表：

- `GET /v1/system-performance/config`
  - 获取性能监控配置
- `GET /v1/system-performance/snapshot?broadcast=false`
  - 获取当前性能快照
- `POST /v1/system-performance/broadcast`
  - 强制广播一次性能快照

## Socket 推送

固定主题：

- `system:performance:metrics`

说明：

- 当 `PerformanceMonitorSocketPushOpen=true` 时，`snapshot` 广播会推送到该主题
- 如果应用中存在 `SocketSessionTemplate`，会自动发送
- 如果不存在 Socket 能力，模块仍可正常提供 HTTP 接口

## 与前端系统设置的集成现状

截至 2026-03-28，仓库中的前端构建产物已经会动态读取系统设置键，但它当前集成的是“设置键”，不是“性能快照接口”。

已经动态读取的键包括：

- `ShowFpsMonitor`
- `ShowCpuMonitor`
- `ShowMemoryMonitor`
- `ShowBandwidthMonitor`
- `ShowBatteryMonitor`
- `ShowBluetoothMonitor`
- `ShowScreenMonitor`
- `PerformanceMonitorPosition`
- `PerformanceMonitorMode`
- `PerformanceMonitorLayout`
- `PerformanceMonitorDirection`

当前边界：

- 前端系统设置页会动态读取以上设置键并控制显示逻辑
- 当前仓库里的前端构建产物没有直接消费 `/v1/system-performance/config`
- 当前仓库里的前端构建产物也没有直接消费 `/v1/system-performance/snapshot`

这意味着：

- 现在已经完成“后台模块接入前端系统设置动态配置”
- 但“前端直接展示后台 OSHI 实时指标面板”仍需要前端后续显式对接快照接口或 Socket 主题

## 推荐接入方式

### 只需要系统设置动态集成

直接依赖本模块即可。

效果：

- 后台自动补齐 `default` 分组的性能监控设置
- 前端现有系统设置页可直接读取这些键

### 需要实时性能面板

前端新增以下任一种接法：

1. 轮询 `GET /v1/system-performance/snapshot`
2. 首次加载调用 `GET /v1/system-performance/config`
3. 启用 `PerformanceMonitorSocketPushOpen` 后订阅 `system:performance:metrics`

## 测试覆盖

当前模块已覆盖以下测试：

- `SystemPerformanceServiceImplTest`
  - 验证快照过滤与 Socket 广播逻辑
- `SystemPerformanceSettingsTest`
  - 验证前端兼容键和统一值类型
- `SystemPerformanceSettingInitializerTest`
  - 验证旧键迁移、兼容键初始化和元数据自动对齐

## 相关文件

- `src/main/java/com/chua/starter/oshi/configuration/OshiAutoConfiguration.java`
- `src/main/java/com/chua/starter/oshi/performance/SystemPerformanceSettings.java`
- `src/main/java/com/chua/starter/oshi/performance/SystemPerformanceSettingInitializer.java`
- `src/main/java/com/chua/starter/oshi/performance/SystemPerformanceController.java`

