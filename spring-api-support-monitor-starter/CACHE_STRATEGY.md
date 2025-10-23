# 服务器信息缓存策略

## 概述

为减少数据库查询，提高系统性能，`MonitorSysGenServerService` 提供了多个带缓存的查询方法。

## 缓存方法

### 1. `getByServerId(Integer serverId)`

**推荐使用的缓存方法**

```java
MonitorSysGenServer server = serverService.getByServerId(serverId);
```

**特点：**
- ✅ 使用 Spring Cache 机制
- ✅ 缓存空间：`server:info`
- ✅ 缓存键：`serverId`
- ✅ 空值不缓存（`unless = "#result == null"`）
- ✅ 包含参数验证和日志记录
- ✅ 语义清晰，明确是根据服务器ID查询

**使用场景：**
- Docker客户端创建时获取服务器SSH配置
- 批量操作时频繁读取服务器信息
- 监控数据收集时获取服务器基本信息

### 2. `getByIdWithCache(Integer serverId)`

**传统缓存方法（保持向后兼容）**

```java
MonitorSysGenServer server = serverService.getByIdWithCache(serverId);
```

**特点：**
- 使用 Spring Cache 机制
- 缓存空间：`server`
- 缓存键：`serverId`
- 空值不缓存

### 3. `getById(Serializable id)`

**MyBatis-Plus 默认方法（无缓存）**

```java
MonitorSysGenServer server = serverService.getById(serverId);
```

**特点：**
- ❌ 无缓存，每次都查询数据库
- 适合需要实时数据的场景

## 缓存空间说明

| 缓存空间 | 用途 | 方法 |
|---------|------|------|
| `server` | 服务器基本信息缓存（传统） | `getByIdWithCache` |
| `server:info` | 服务器基本信息缓存（推荐） | `getByServerId` |

## 缓存失效策略

### 自动失效场景

1. **删除服务器时**
```java
@CacheEvict(value = {"server", "server:info"}, key = "#id")
public boolean removeById(Serializable id)
```

2. **更新服务器时**
```java
@CacheEvict(value = {"server", "server:info"}, key = "#entity.monitorSysGenServerId")
public boolean updateById(MonitorSysGenServer entity)
```

### 手动清除缓存

如需手动清除缓存，可以使用：

```java
@Autowired
private CacheManager cacheManager;

// 清除指定服务器缓存
Cache cache = cacheManager.getCache("server:info");
if (cache != null) {
    cache.evict(serverId);
}

// 清除所有服务器缓存
cache.clear();
```

## 性能对比

| 场景 | 无缓存 | 有缓存 | 性能提升 |
|------|--------|--------|----------|
| 单次查询 | ~10ms | ~0.1ms | 100倍 |
| 100次查询 | ~1000ms | ~10ms | 100倍 |
| Docker客户端创建 | 每次查DB | 首次查DB | 显著提升 |

## 使用建议

### ✅ 推荐场景

1. **频繁读取服务器信息**
```java
// Docker客户端工厂
MonitorSysGenServer server = serverService.getByServerId(serverId);
```

2. **批量操作**
```java
for (Integer serverId : serverIds) {
    MonitorSysGenServer server = serverService.getByServerId(serverId);
    // 处理服务器...
}
```

3. **监控数据收集**
```java
MonitorSysGenServer server = serverService.getByServerId(serverId);
String serverName = server.getMonitorSysGenServerName();
```

### ❌ 不推荐场景

1. **需要实时数据**
```java
// 连接状态等实时数据，不要用缓存
MonitorSysGenServer server = serverService.getById(serverId);
Integer status = server.getMonitorSysGenServerConnectionStatus();
```

2. **数据更新后立即查询**
```java
// 更新后立即查询，缓存已失效，应该直接查询
serverService.updateById(server);
// 此时缓存已失效，会重新查询数据库
MonitorSysGenServer updated = serverService.getByServerId(serverId);
```

## 缓存配置

确保在 Spring Boot 配置中启用缓存：

```yaml
spring:
  cache:
    type: redis  # 或 caffeine、ehcache等
    redis:
      time-to-live: 3600000  # 1小时
      cache-null-values: false
```

在启动类上添加注解：

```java
@EnableCaching
@SpringBootApplication
public class Application {
    // ...
}
```

## 监控和调试

### 查看缓存命中率

```java
@Autowired
private CacheManager cacheManager;

public void printCacheStats() {
    Cache cache = cacheManager.getCache("server:info");
    if (cache instanceof CaffeineCache) {
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
            (com.github.benmanes.caffeine.cache.Cache<Object, Object>) 
            ((CaffeineCache) cache).getNativeCache();
        CacheStats stats = nativeCache.stats();
        log.info("缓存统计 - 命中率: {}, 请求次数: {}", 
                stats.hitRate(), stats.requestCount());
    }
}
```

### 开启缓存日志

```yaml
logging:
  level:
    org.springframework.cache: DEBUG
```

## 迁移指南

### 从 `getById` 迁移到 `getByServerId`

**修改前：**
```java
MonitorSysGenServer server = serverService.getById(serverId);
```

**修改后：**
```java
MonitorSysGenServer server = serverService.getByServerId(serverId);
```

### 从 `getByIdWithCache` 迁移到 `getByServerId`

**修改前：**
```java
MonitorSysGenServer server = serverService.getByIdWithCache(serverId);
```

**修改后：**
```java
MonitorSysGenServer server = serverService.getByServerId(serverId);
```

## 注意事项

1. **缓存一致性**：更新和删除操作会自动失效缓存
2. **空值处理**：null值不会被缓存，避免缓存穿透
3. **缓存时间**：默认根据配置的TTL，可根据需要调整
4. **内存占用**：大量服务器时注意监控缓存内存使用
5. **集群环境**：建议使用Redis等分布式缓存

## 示例代码

### DockerProtocolClientFactory 使用示例

```java
@Component
@RequiredArgsConstructor
public class DockerProtocolClientFactory {
    
    private final MonitorSysGenServerService serverService;
    
    private DockerProtocolClient createLocalClient(MonitorSysGenServerSetting setting) {
        Integer serverId = setting.getMonitorSysGenServerId();
        
        // ✅ 使用缓存方法，减少数据库查询
        MonitorSysGenServer server = serverService.getByServerId(serverId);
        if (server == null) {
            log.warn("服务器信息不存在 - serverId: {}", serverId);
            return null;
        }
        
        // 使用服务器信息创建Docker客户端
        String host = server.getMonitorSysGenServerHost();
        String username = server.getMonitorSysGenServerUsername();
        // ...
    }
}
```

## 总结

- ✅ 优先使用 `getByServerId()`（推荐）
- ✅ 兼容使用 `getByIdWithCache()`（传统）
- ❌ 避免频繁使用 `getById()`（无缓存）
- ✅ 更新和删除操作自动失效缓存
- ✅ 大幅减少数据库查询，提升系统性能

