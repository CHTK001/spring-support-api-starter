# Spring API Support Tenanted Starter

## 简介

租户端应用模块，集成了租户同步协议客户端功能，支持从平台端实时接收租户元数据更新。

## 核心功能

- **租户数据隔离**：基于 MyBatis-Plus 的多租户插件
- **元数据同步**：通过长连接实时接收平台端推送的租户配置
- **管理员账号同步**：自动同步租户管理员账号信息
- **服务列表同步**：自动同步租户可用服务列表

## 依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-tenanted-starter</artifactId>
    <version>4.0.0</version>
</dependency>
```

## 配置说明

### 完整配置示例

```yaml
plugin:
  mybatis-plus:
    tenant:
      # 是否启用租户功能
      enable: true
      # 租户ID字段名
      tenant-id: sys_tenant_id
      # 是否自动添加租户字段（生产环境请设置为false）
      auto-add-column: false
      # 忽略的表
      ignore-table:
        - sys_user
        - sys_role
        - sys_config
        - sys_dict
        - sys_tenant
      # 同步协议配置
      sync-protocol:
        # 是否启用同步协议
        enable: true
        # 程序类型：client-客户端
        type: client
        # 协议类型（rsocket 或 websocket-sync）
        protocol: rsocket
        # 服务端地址
        server-address: ws://localhost:19280
        # 心跳开关
        heartbeat: true
        # 心跳间隔（秒）
        heartbeat-interval: 30
        # 连接超时时间（毫秒）
        connect-timeout: 10000
        # 重连间隔（秒）
        reconnect-interval: 5
        # 最大重连次数，-1表示无限重连
        max-reconnect-attempts: -1
        # 元数据同步配置
        metadata-sync:
          # 是否启用元数据同步
          enable: true
          # 同步间隔（秒）
          interval: 300
          # 初始延迟（秒）
          initial-delay: 60
```

### 配置项详解

| 配置项                                 | 类型    | 默认值               | 说明                                |
| -------------------------------------- | ------- | -------------------- | ----------------------------------- |
| `enable`                               | Boolean | false                | 是否启用租户功能                    |
| `tenant-id`                            | String  | sys_tenant_id        | 租户 ID 字段名                      |
| `auto-add-column`                      | Boolean | false                | 是否自动添加租户字段                |
| `ignore-table`                         | Set     | 空                   | 忽略的表列表                        |
| `sync-protocol.enable`                 | Boolean | false                | 是否启用同步协议                    |
| `sync-protocol.type`                   | String  | client               | 程序类型                            |
| `sync-protocol.protocol`               | String  | rsocket              | 协议类型（rsocket、websocket-sync） |
| `sync-protocol.server-address`         | String  | ws://localhost:19280 | 服务端地址                          |
| `sync-protocol.heartbeat`              | Boolean | true                 | 是否启用心跳                        |
| `sync-protocol.heartbeat-interval`     | Integer | 30                   | 心跳间隔（秒）                      |
| `sync-protocol.connect-timeout`        | Integer | 10000                | 连接超时（毫秒）                    |
| `sync-protocol.reconnect-interval`     | Integer | 5                    | 重连间隔（秒）                      |
| `sync-protocol.max-reconnect-attempts` | Integer | -1                   | 最大重连次数                        |
| `metadata-sync.enable`                 | Boolean | false                | 是否启用元数据同步                  |
| `metadata-sync.interval`               | Integer | 300                  | 同步间隔（秒）                      |
| `metadata-sync.initial-delay`          | Integer | 60                   | 初始延迟（秒）                      |

## 元数据消费者

模块内置了 `AdminAccountMetadataConsumer`，用于处理从平台端推送的元数据：

```java
@Spi("admin-account")
public class AdminAccountMetadataConsumer implements TenantMetadataConsumer {

    @Override
    public void consumeMetadata(String tenantId, Map<String, Object> metadata) {
        // 处理管理员账号
        Object adminAccount = metadata.get("adminAccount");
        // 处理服务列表
        Object services = metadata.get("services");
        // 处理租户配置
        Object config = metadata.get("config");
    }
}
```

### 自定义消费者

可以实现 `TenantMetadataConsumer` 接口创建自定义消费者：

```java
@Spi("custom-consumer")
public class CustomMetadataConsumer implements TenantMetadataConsumer {

    @Override
    public String getName() {
        return "custom-consumer";
    }

    @Override
    public void consumeMetadata(String tenantId, Map<String, Object> metadata) {
        // 自定义处理逻辑
    }
}
```

并在 `META-INF/services/com.chua.tenant.support.sync.TenantMetadataConsumer` 中注册：

```
com.example.CustomMetadataConsumer
```

## 架构说明

```
┌─────────────────────┐         WebSocket         ┌─────────────────────┐
│    平台端 (Server)   │ ◄──────────────────────► │   租户端 (Client)    │
│                     │                           │                     │
│  TenantSyncServer   │    元数据推送/同步请求     │  TenantSyncClient   │
│  MetadataProvider   │ ◄──────────────────────► │  MetadataConsumer   │
└─────────────────────┘                           └─────────────────────┘
```

## 通信主题

| 主题                   | 方向            | 说明                 |
| ---------------------- | --------------- | -------------------- |
| `tenant/metadata`      | Server → Client | 服务端主动推送元数据 |
| `tenant/sync/request`  | Client → Server | 客户端请求同步       |
| `tenant/sync/response` | Server → Client | 服务端响应同步请求   |
| `tenant/health`        | 双向            | 健康检查             |

## 注意事项

1. **网络配置**：确保客户端可以访问平台端的 WebSocket 服务
2. **防火墙**：开放对应端口（默认 19280）
3. **SSL/TLS**：生产环境建议使用 `wss://` 协议
4. **重连机制**：默认无限重连，可根据需要调整

## 日志配置

```yaml
logging:
  level:
    com.chua.tenant.support: DEBUG
    com.chua.starter.tenant: DEBUG
```

## 许可证

Apache License 2.0

---

**作者**: CH  
**版本**: 1.0.0  
**更新时间**: 2024/12/03
