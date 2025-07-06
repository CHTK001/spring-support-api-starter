# Prometheus地址配置和缓存优化修复说明

## 问题描述

在`MonitorSysGenServerComponentServiceImpl`类的`queryPrometheusData`方法中发现两个性能和配置问题：

1. **Prometheus地址构建问题**：直接使用服务器的主机和端口构建Prometheus查询地址，而不是从服务器配置中获取专门的Prometheus配置
2. **缓存性能问题**：服务器和组件的`getById`方法没有缓存，导致重复数据库查询

## 修复内容

### 1. Prometheus地址配置修复

**修复前的问题代码**：
```java
// 构建Prometheus查询地址
String prometheusAddress = server.getMonitorSysGenServerHost() + ":" + server.getMonitorSysGenServerPort();
```

**修复后的正确实现**：
```java
// 获取服务器配置信息，从中获取Prometheus配置
ReturnResult<MonitorSysGenServerSetting> settingResult = 
    serverSettingService.getByServerId(serverId);
if (!settingResult.isOk() || settingResult.getData() == null) {
    return ReturnResult.error("服务器配置不存在或获取失败");
}

MonitorSysGenServerSetting setting = settingResult.getData();

// 构建Prometheus查询地址，优先使用配置中的URL
String prometheusAddress;
if (StringUtils.isNotBlank(setting.getMonitorSysGenServerSettingPrometheusUrl())) {
    // 使用配置中的完整URL
    prometheusAddress = setting.getMonitorSysGenServerSettingPrometheusUrl();
} else if (StringUtils.isNotBlank(setting.getMonitorSysGenServerSettingPrometheusHost()) 
        && setting.getMonitorSysGenServerSettingPrometheusPort() != null) {
    // 使用配置中的主机和端口
    prometheusAddress = "http://" + setting.getMonitorSysGenServerSettingPrometheusHost() 
        + ":" + setting.getMonitorSysGenServerSettingPrometheusPort();
} else {
    // 回退到服务器基本信息
    prometheusAddress = "http://" + server.getMonitorSysGenServerHost() + ":9090";
    log.warn("未配置Prometheus地址，使用默认端口9090: {}", prometheusAddress);
}
```

**修复优势**：
- 支持从服务器配置中获取专门的Prometheus配置
- 支持完整URL配置或主机+端口配置
- 提供回退机制，确保兼容性
- 支持不同服务器使用不同的Prometheus配置

### 2. 缓存性能优化

#### 2.1 组件服务缓存方法

**新增方法**：
```java
/**
 * 带缓存的根据ID获取组件信息
 *
 * @param componentId 组件ID
 * @return 组件信息
 */
@Cacheable(value = "serverComponent", key = "#componentId", unless = "#result == null")
public MonitorSysGenServerComponent getByIdWithCache(Long componentId) {
    return this.getById(componentId);
}
```

#### 2.2 服务器服务缓存方法

**接口新增**：
```java
/**
 * 带缓存的根据ID获取服务器信息
 *
 * @param serverId 服务器ID
 * @return 服务器信息
 */
MonitorSysGenServer getByIdWithCache(Integer serverId);
```

**实现类新增**：
```java
@Override
@Cacheable(value = "server", key = "#serverId", unless = "#result == null")
public MonitorSysGenServer getByIdWithCache(Integer serverId) {
    return this.getById(serverId);
}
```

#### 2.3 方法调用更新

**修复前**：
```java
MonitorSysGenServerComponent component = getById(componentId);
MonitorSysGenServer server = serverService.getById(String.valueOf(component.getMonitorSysGenServerId()));
```

**修复后**：
```java
MonitorSysGenServerComponent component = getByIdWithCache(componentId);
MonitorSysGenServer server = serverService.getByIdWithCache(serverId);
```

### 3. 查询功能增强

**新增范围查询支持**：
```java
// 执行Prometheus查询
String queryResult;
if (startTime != null && endTime != null && step != null) {
    // 范围查询
    JsonNode result = prometheusService.queryRange(prometheusAddress, promQL, startTime, endTime, step);
    queryResult = result != null ? result.toString() : "{}";
} else {
    // 即时查询
    queryResult = prometheusService.queryPrometheus(prometheusAddress, promQL);
}
```

## 涉及的配置字段

### MonitorSysGenServerSetting表中的Prometheus配置字段：

- `monitorSysGenServerSettingPrometheusUrl` - 完整的Prometheus URL
- `monitorSysGenServerSettingPrometheusHost` - Prometheus主机地址
- `monitorSysGenServerSettingPrometheusPort` - Prometheus端口
- `monitorSysGenServerSettingPrometheusQueryPath` - 查询路径
- `monitorSysGenServerSettingPrometheusUsername` - 认证用户名
- `monitorSysGenServerSettingPrometheusPassword` - 认证密码
- `monitorSysGenServerSettingPrometheusTimeout` - 超时配置

## 修复文件列表

1. **MonitorSysGenServerComponentServiceImpl.java**
   - 修复`queryPrometheusData`方法的Prometheus地址构建逻辑
   - 新增`getByIdWithCache`缓存方法
   - 添加必要的导入和依赖注入

2. **MonitorSysGenServerService.java**
   - 新增`getByIdWithCache`接口方法

3. **MonitorSysGenServerServiceImpl.java**
   - 实现`getByIdWithCache`缓存方法
   - 添加缓存相关导入

## 性能提升

1. **缓存优化**：
   - 减少重复的数据库查询
   - 提高组件和服务器信息获取性能
   - 使用Spring Cache抽象，支持多种缓存实现

2. **配置灵活性**：
   - 支持每个服务器独立的Prometheus配置
   - 支持多种配置方式（完整URL、主机+端口）
   - 提供配置回退机制

## 测试建议

1. **功能测试**：
   - 测试使用完整Prometheus URL的配置
   - 测试使用主机+端口的配置
   - 测试回退到默认配置的情况

2. **性能测试**：
   - 验证缓存是否生效
   - 测试重复查询的性能提升
   - 监控数据库查询次数减少

3. **兼容性测试**：
   - 确保现有功能不受影响
   - 验证不同配置场景下的正确性

## 注意事项

1. 缓存配置需要在Spring配置中启用`@EnableCaching`
2. 建议配置合适的缓存过期时间和大小限制
3. 在服务器或组件信息更新时，需要考虑缓存失效策略
4. Prometheus配置变更后，建议清理相关缓存
