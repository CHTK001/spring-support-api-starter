# Tenant Sync Access Guide

## 1. 适用场景

`spring-api-support-common-tenant-starter` 当前的租户同步能力适合两类场景：

1. 平台单机模式  
   平台端自己维护租户、服务、菜单数据，不启用租户同步客户端。
2. 平台端 + 多租户端模式  
   平台端维护权威租户数据，租户端接收同步消息并落本地库。

当前设计定位是：

- 平台端是权威源
- 租户端是本地投影库
- 同步链路是“增量通知 + 全量补偿 + 本地幂等落库”
- 不是跨数据库强一致复制

## 2. 依赖关系

接入方只需要引入租户 starter，内部已经依赖同步 starter。

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-tenant-starter</artifactId>
    <version>4.0.0.37</version>
</dependency>
```

## 3. 角色划分

### 3.1 平台端

平台端负责：

- 管理 `sys_tenant`
- 管理租户绑定服务和菜单
- 在租户、服务、菜单变化后发布同步消息
- 在 ACK 超时后，对指定租户会话补发全量快照

关键类：

- `TenantServerConfiguration`
- `TenantServerSyncPublisher`
- `TenantServerSyncRequestHandler`
- `TenantServerSyncAckHandler`

### 3.2 租户端

租户端负责：

- 接收平台端同步消息
- 校验签名
- 做重复消息和乱序消息控制
- 通过 `TenantDataSyncProvider` 落本地库
- 处理完成后上报业务 ACK

关键类：

- `TenantClientConfiguration`
- `TenantClientSyncHandler`
- `TenantSyncClientCustomizer`
- `TenantSyncClientInfoCustomizer`
- `TenantDataSyncProvider`

## 4. 同步主题

当前租户同步使用以下主题：

| 主题 | 说明 |
| --- | --- |
| `tenant/update` | 租户基础信息新增/更新 |
| `tenant/delete` | 租户删除或停用后的删除通知 |
| `tenant/service` | 租户绑定服务和菜单集合 |
| `tenant/menu` | 菜单局部变更 |
| `tenant/sync/request` | 租户端主动请求全量同步 |
| `tenant/sync/ack` | 租户端对业务处理结果上报 ACK |

## 5. 接入方式

### 5.1 平台单机模式

这种模式下不需要租户同步链路。

```yaml
plugin:
  mybatis-plus:
    tenant:
      server:
        enable: false
      client:
        enable: false
```

适用于：

- 平台自己直接访问本地数据库
- 不需要把租户数据分发到外部租户服务

### 5.2 平台端模式

平台端至少需要开启：

- `plugin.sync.type=server`
- `plugin.sync.server.enable=true`
- `plugin.mybatis-plus.tenant.server.enable=true`
- `plugin.mybatis-plus.tenant.server.sync-enable=true`

推荐配置：

```yaml
plugin:
  sync:
    type: server
    server:
      enable: true
      host: 0.0.0.0
      port: 19380
      protocol: rsocket-sync
      heartbeat: true
      heartbeat-interval: 30
      connect-timeout: 10000

  mybatis-plus:
    tenant:
      server:
        enable: true
        sync-enable: true
        sync-full-push-on-register: true
        sync-full-push-interval-seconds: 300
        sync-broadcast-fallback: false
        sync-skip-unchanged-full-push: true
        sync-sign-secret: your-shared-secret
        sync-sign-allowed-clock-skew-seconds: 300
        sync-ack-enabled: true
        sync-ack-timeout-seconds: 45
        sync-ack-check-interval-seconds: 15
        sync-ack-compensation-cooldown-seconds: 60
        sync-ack-max-pending: 8192
```

### 5.3 租户端模式

租户端至少需要开启：

- `plugin.sync.type=client`
- `plugin.sync.client.enable=true`
- `plugin.mybatis-plus.tenant.client.enable=true`
- `plugin.mybatis-plus.tenant.client.sync-enable=true`
- `plugin.mybatis-plus.tenant.client.tenant-id=<当前租户ID>`

推荐配置：

```yaml
plugin:
  sync:
    type: client
    client:
      enable: true
      protocol: rsocket
      server-host: platform-host
      server-port: 19380
      heartbeat: true
      heartbeat-interval: 30
      connect-timeout: 10000
      auto-reconnect: true
      max-reconnect-attempts: -1
      reconnect-interval: 5000
      auto-register: true

  mybatis-plus:
    tenant:
      client:
        enable: true
        sync-enable: true
        tenant-id: tenant-a
        sync-auto-subscribe-topics: true
        sync-request-full-push-on-connect: false
        sync-request-full-push-delay-millis: 3000
        sync-sign-secret: your-shared-secret
        sync-sign-allowed-clock-skew-seconds: 300
        sync-ack-enabled: true
        sync-deduplicate-messages: true
        sync-deduplicate-window-seconds: 3600
        sync-deduplicate-max-size: 4096
        sync-ignore-out-of-order-messages: true
```

## 6. 签名和 ACK 约束

### 6.1 签名校验

平台端和租户端启用签名后，双方必须使用同一份 `sync-sign-secret`。

消息签名特性：

- 算法固定为 `HmacSHA256`
- 使用稳定序列化后的消息体计算签名
- 消息里必须包含 `messageId` 和 `timestamp`
- 服务端会校验租户端主动请求和 ACK 的签名
- 客户端会校验平台端同步消息签名

### 6.2 业务 ACK

业务 ACK 不是网络层 ACK，而是“租户端处理完成后的业务回执”。

行为如下：

1. 平台端发消息
2. 租户端验签、去重、乱序判断、执行业务落库
3. 租户端发送 `tenant/sync/ack`
4. 平台端收到 ACK 后清理待确认记录
5. 若超时未收到 ACK，平台端对该租户会话做一次全量补偿

当前 ACK 机制适用于单平台节点部署。  
如果后续平台端做水平扩容，需要把 ACK 状态移到共享存储。

## 7. 本地落库要求

租户端必须实现或复用 `TenantDataSyncProvider`，并保证同步逻辑满足以下要求：

- 幂等
- 可重复执行
- 能接受“先增量、后全量补偿”的顺序
- 删除、替换、局部更新语义清晰

默认实现 `DefaultTenantDataSyncProvider` 更偏向 JDBC 表同步，如果业务表结构不同，建议自己实现。

示例：

```java
@Component
public class CustomTenantDataSyncProvider implements TenantDataSyncProvider {

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public boolean syncTenantUser(SysTenant tenant, String operation) {
        return true;
    }

    @Override
    public boolean syncTenantMenu(String tenantId, List<Integer> menuIds) {
        return true;
    }

    @Override
    public boolean syncMenuData(String tenantId, List<Map<String, Object>> menus, String operation) {
        return true;
    }
}
```

## 8. 运行流程

### 8.1 平台端发布增量

平台端业务变更后调用：

- `publishTenantUpdate(...)`
- `publishTenantDelete(...)`
- `publishServiceUpdate(...)`
- `publishMenuUpdate(...)`

### 8.2 租户端启动

租户端启动后会：

1. 注册租户 metadata 和 capabilities
2. 自动订阅租户同步主题
3. 等待平台端推送
4. 如开启主动补偿且在延迟窗口内没收到消息，则请求一次全量同步

### 8.3 ACK 超时补偿

当租户端消息未在超时时间内回 ACK 时：

1. 平台端扫描待确认消息
2. 判断是否满足冷却时间
3. 对该租户会话发起定向全量补偿
4. 不会默认广播给所有租户客户端

## 9. 失败语义

要明确区分两类“丢失”：

1. 已经落库的数据  
   不会因为同步模块重启而丢失。
2. 还没落库的消息  
   仍然可能因为网络中断、进程退出、处理异常而失败。

所以当前链路的保证是：

- 已落库数据持久化
- 链路靠签名、去重、乱序控制、业务 ACK、全量补偿降低漏同步概率
- 不是强一致复制协议

## 10. 上线前检查项

上线前至少确认以下项目：

| 检查项 | 要求 |
| --- | --- |
| 平台端 `sync-broadcast-fallback` | 建议保持 `false` |
| 平台端和租户端 `sync-sign-secret` | 必须一致 |
| 租户端 `tenant-id` | 必须配置 |
| `TenantDataSyncProvider` | 业务字段映射正确 |
| 租户端本地库 | 已准备好接收租户、菜单、服务数据 |
| 全量补偿 | 已验证能在 ACK 超时后触发 |
| 去重和乱序控制 | 已在测试环境跑通 |

## 11. 推荐接入顺序

建议按下面顺序实施：

1. 平台端先跑通租户管理和菜单绑定
2. 单独验证平台端 `SyncServer`
3. 单独验证租户端 `SyncClient`
4. 打通签名校验
5. 打通租户端本地落库
6. 打通 ACK 回执和超时补偿
7. 最后再压测全量同步和多租户并发
