# 设备数据上报功能

## 功能概述

设备数据上报功能允许 `spring-support-report-client-starter` 客户端定时向 `spring-api-support-monitor-starter` 服务器上报设备指标数据和基本信息。服务器会根据设备的IP地址自动匹配已配置的服务器，并将上报的数据关联存储。

## 核心特性

### 🔄 自动IP匹配
- 根据设备上报的IP地址自动匹配服务器配置
- 支持一对一的IP地址映射关系
- 自动缓存IP到服务器ID的映射关系

### 📊 数据关联存储
- 将设备指标数据转换为服务器指标数据格式
- 自动更新服务器基本信息（名称、描述、系统信息等）
- 实时更新服务器在线状态

### 📈 统计监控
- 实时统计设备上报次数和成功率
- 监控设备活跃状态和健康状况
- 生成每日设备上报统计报告

## API接口

### 1. 设备指标数据上报
```http
POST /api/v1/monitor/device/metrics
Content-Type: application/json

{
  "deviceId": "device-001",
  "deviceName": "生产服务器",
  "ipAddress": "192.168.1.100",
  "port": 8080,
  "cpuUsage": 45.2,
  "memoryUsage": 68.5,
  "diskUsage": 32.1,
  "online": true,
  "collectTime": "2024-12-19T10:30:00"
}
```

**响应示例：**
```json
{
  "code": "00000",
  "msg": "success",
  "data": {
    "deviceId": "device-001",
    "ipAddress": "192.168.1.100",
    "serverId": 123,
    "matched": true,
    "saved": true,
    "timestamp": 1703001000000
  }
}
```

### 2. 设备基本信息上报
```http
POST /api/v1/monitor/device/info
Content-Type: application/json

{
  "deviceId": "device-001",
  "deviceName": "生产服务器",
  "ipAddress": "192.168.1.100",
  "hostname": "prod-server-01",
  "osName": "Linux",
  "osVersion": "Ubuntu 20.04",
  "cpuCores": 8,
  "totalMemory": 16777216000,
  "online": true
}
```

### 3. 设备心跳检测
```http
POST /api/v1/monitor/device/ping
Content-Type: application/json

{
  "deviceId": "device-001",
  "timestamp": 1703001000000
}
```

### 4. 获取设备统计信息
```http
GET /api/v1/monitor/device/stats?deviceId=device-001
```

### 5. 获取设备列表
```http
GET /api/v1/monitor/device/list
```

## 数据流程

### 1. 设备数据收集
```
spring-support-report-client-starter
├── OshiDeviceDataService (收集系统指标)
├── DeviceReportTask (定时上报任务)
└── HttpReportPushService (HTTP推送服务)
```

### 2. 服务器数据处理
```
spring-api-support-monitor-starter
├── DeviceReportController (接收API)
├── DeviceReportService (业务处理)
├── IP地址匹配 (findServerByIpAddress)
├── 数据转换存储 (saveServerMetrics)
└── 状态更新 (updateServerOnlineStatus)
```

### 3. 数据关联逻辑
1. **IP地址匹配**：根据设备IP地址查找匹配的服务器配置
2. **数据转换**：将设备指标数据转换为服务器指标数据格式
3. **信息更新**：更新服务器基本信息和在线状态
4. **统计记录**：记录上报统计信息和设备活跃状态

## 配置说明

### 客户端配置 (spring-support-report-client-starter)
```yaml
report:
  client:
    enable: true
    address: "http://monitor-server:8080"
    device-id: "device-001"
    device-name: "生产服务器"
    push-interval: 60  # 推送间隔(秒)
    initial-delay: 10  # 初始延迟(秒)
    timeout: 30000     # 超时时间(毫秒)
```

### 服务器配置 (spring-api-support-monitor-starter)
```yaml
# 服务器配置表中需要配置对应的服务器信息
# monitor_sys_gen_server 表
# - monitor_sys_gen_server_host: 设备IP地址
# - monitor_sys_gen_server_status: 1 (启用状态)
```

## 监控和日志

### 关键日志
- 设备数据上报成功/失败日志
- IP地址匹配结果日志
- 服务器信息更新日志
- 设备统计和健康状态日志

### 定时任务
- **设备统计缓存清理**：每小时清理长时间未活跃的设备
- **每日统计报告**：每天上午9点生成设备上报统计报告
- **健康状态检查**：每30分钟检查设备上报健康状态

### 统计指标
- 设备总数和活跃设备数
- 指标上报次数和信息上报次数
- IP匹配成功率和失败率
- 设备最后活跃时间

## 故障排除

### 常见问题

1. **设备上报失败**
   - 检查网络连接和服务器地址配置
   - 验证API接口是否正常响应
   - 查看客户端和服务器端日志

2. **IP地址匹配失败**
   - 确认服务器配置表中存在对应IP的记录
   - 检查服务器状态是否为启用状态
   - 验证IP地址格式是否正确

3. **数据存储失败**
   - 检查数据库连接状态
   - 验证数据格式是否正确
   - 查看服务器端异常日志

### 调试方法
1. 启用DEBUG日志查看详细处理过程
2. 使用心跳接口测试连接状态
3. 查看设备统计信息了解上报情况
4. 检查服务器指标数据是否正确存储

## 扩展开发

### 自定义数据处理
可以继承 `DeviceReportService` 来实现自定义的数据处理逻辑：

```java
@Service
public class CustomDeviceReportService extends DeviceReportServiceImpl {
    
    @Override
    public ReturnResult<Map<String, Object>> processDeviceMetrics(DeviceMetrics metrics) {
        // 自定义处理逻辑
        return super.processDeviceMetrics(metrics);
    }
}
```

### 自定义IP匹配规则
可以重写 `findServerByIpAddress` 方法来实现自定义的IP匹配规则：

```java
@Override
public Integer findServerByIpAddress(String ipAddress) {
    // 自定义IP匹配逻辑
    // 例如：支持IP段匹配、域名解析等
    return customIpMatcher.findServer(ipAddress);
}
```

## 性能优化

### 缓存策略
- IP地址到服务器ID的映射缓存
- 设备统计信息内存缓存
- 定期清理过期缓存数据

### 批量处理
- 支持批量设备数据上报
- 异步处理大量设备数据
- 数据库批量插入优化

### 监控告警
- 设备长时间未上报告警
- 数据处理异常告警
- 系统资源使用监控
