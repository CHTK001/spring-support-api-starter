# Tenant Sync Test Guide

## 1. 文档目标

本文件用于定义租户同步模块的测试范围、最小覆盖面和验收标准。  
它覆盖：

- 单元测试
- 组件测试
- 联调测试
- 上线前回归测试

当前重点对象是：

- `TenantSyncMessageSupport`
- `TenantClientSyncHandler`
- `TenantSyncClientCustomizer`
- `TenantSyncClientInfoCustomizer`
- `TenantServerSyncPublisher`
- `TenantServerSyncRequestHandler`
- `TenantServerSyncAckHandler`
- `TenantSyncAckTracker`

## 2. 测试目标

租户同步测试至少要证明四件事：

1. 消息合法时，租户端能正确落库。
2. 消息非法时，租户端会拒绝处理。
3. 重复、乱序、超时场景下，不会把租户数据处理坏。
4. 平台端能通过 ACK 和全量补偿降低漏同步风险。

## 3. 测试环境建议

### 3.1 单元测试

建议：

- JUnit 5
- Mockito
- AssertJ

依赖对象建议全部 Mock：

- `SyncClient`
- `SyncServer`
- `TenantDataSyncProvider`
- `SysTenantMapper`
- `SysTenantServiceMapper`
- `SysMenuMapper`

### 3.2 联调测试

建议至少准备两套应用：

- 平台端应用 1 个
- 租户端应用 1 到 2 个

数据库建议：

- 平台端独立库
- 每个租户端独立库

## 4. 单元测试范围

### 4.1 `TenantSyncMessageSupport`

必须覆盖：

- `createEnvelope()` 生成 `messageId`、`timestamp`
- `createAck()` 写入 `sourceMessageId`、`sourceTopic`、`ackStatus`
- `sign()` 后 `verifySignature()` 能通过
- 签名密钥错误时校验失败
- 时间偏移超出窗口时校验失败
- `payloadFingerprint()` 对同一语义消息稳定

建议测试名：

- `shouldSignAndVerifyMessage`
- `shouldRejectMessageWhenSecretMismatch`
- `shouldRejectExpiredMessage`
- `shouldCreateAckWithSourceMessageId`
- `shouldKeepFingerprintStableForSamePayload`

### 4.2 `TenantClientSyncHandler`

必须覆盖：

- 客户端模式关闭时直接忽略
- `sync-enable=false` 时直接忽略
- 签名错误时拒绝处理
- 非本租户消息被忽略
- 重复消息不会再次调用 provider
- 乱序旧消息不会再次调用 provider
- 业务成功时发送成功 ACK
- 业务失败时发送失败 ACK
- `tenant/update`、`tenant/delete`、`tenant/service`、`tenant/menu` 四类主题都能分发

建议测试名：

- `shouldIgnoreWhenClientModeDisabled`
- `shouldRejectWhenSignatureInvalid`
- `shouldIgnoreMessageForAnotherTenant`
- `shouldNotReapplyDuplicateMessage`
- `shouldNotApplyOutOfOrderMessage`
- `shouldSendAppliedAckAfterSuccessfulHandle`
- `shouldSendFailedAckWhenProviderThrows`

### 4.3 `TenantSyncClientCustomizer`

必须覆盖：

- 自动订阅租户主题
- 注册连接监听
- 开启主动全量补偿时，连接后按延迟调度
- 如果连接后已收到同步消息，则跳过主动补偿
- 未配置 `tenantId` 时不发起全量请求
- 启用签名时，全量同步请求会带签名

### 4.4 `TenantSyncClientInfoCustomizer`

必须覆盖：

- metadata 中写入 `tenantId`
- metadata 中写入 `syncRole`
- metadata 中写入 `tenantSyncEnabled`
- metadata 中写入 `tenantSyncSignEnabled`
- metadata 中写入 `tenantSyncAckEnabled`
- capabilities 中包含 `tenant-sync`
- 启用签名时包含 `tenant-sync-signed`
- 启用 ACK 时包含 `tenant-sync-ack`

### 4.5 `TenantSyncAckTracker`

必须覆盖：

- `track()` 后能记录 pending ACK
- `acknowledge()` 成功后移除 pending
- 超时后 `drainExpired()` 返回过期消息
- `tryMarkCompensation()` 在冷却窗口内拒绝重复补偿
- `clearSession()` 会清掉该会话相关记录
- 上限超出时旧记录被淘汰

建议测试名：

- `shouldTrackPendingAck`
- `shouldRemovePendingWhenAckArrives`
- `shouldDrainExpiredPendingAcks`
- `shouldRespectCompensationCooldown`
- `shouldClearSessionState`

### 4.6 `TenantServerSyncAckHandler`

必须覆盖：

- 主题不匹配不处理
- ACK 签名错误时拒绝
- 会话未绑定租户时拒绝
- ACK 中 `tenantId` 与当前会话租户不一致时拒绝
- 缺少 `sourceMessageId` 时拒绝
- 成功 ACK 时返回 `ackSuccess=true`
- 失败 ACK 时记录状态但不抛异常

### 4.7 `TenantServerSyncPublisher`

必须覆盖：

- 指定租户客户端时定向发送
- 无目标客户端且 `sync-broadcast-fallback=false` 时不广播
- 无目标客户端且 `sync-broadcast-fallback=true` 时广播
- 启用签名时发送消息会带签名
- 客户端声明支持 ACK 时会被跟踪
- ACK 超时后触发 `publishFullSyncToSession(...)`
- 冷却窗口内不会重复补偿
- 定时全量同步在快照未变化时跳过

## 5. 组件测试范围

组件测试建议不 Mock `TenantClientSyncHandler` 和 `TenantServerSyncPublisher`，只 Mock数据库和网络边界。

建议验证：

- 平台端增量消息发布时，租户端 handler 能收到并处理
- ACK 能从租户端回到平台端
- ACK 超时后平台端会触发定向全量补偿
- 全量补偿后租户端库状态与平台端一致

## 6. 联调测试用例

### 6.1 基础联通

1. 平台端启动 `SyncServer`
2. 租户端启动 `SyncClient`
3. 平台端确认连接列表里有租户客户端
4. 校验 metadata 中的 `tenantId`、`syncRole`、`tenantSyncAckEnabled`

### 6.2 全量首推

1. 平台端预置一个租户和菜单
2. 租户端连接平台端
3. 平台端触发注册后全量推送
4. 租户端本地库出现对应租户和菜单数据

### 6.3 增量更新

1. 平台端修改租户名称
2. 平台端发布 `tenant/update`
3. 租户端本地租户表更新
4. 平台端收到 ACK

### 6.4 重复消息

1. 对同一 `messageId` 连续投递两次
2. 租户端只执行一次落库
3. 第二次返回 `duplicate` ACK 状态

### 6.5 乱序消息

1. 先发送时间戳较新的消息
2. 再发送同主题、同租户、时间戳较旧的消息
3. 租户端拒绝应用旧消息
4. 平台端收到 `out-of-order` ACK 状态

### 6.6 签名错误

1. 平台端或租户端故意改用错误密钥
2. 对端必须拒绝处理
3. 不得写入本地库

### 6.7 ACK 超时补偿

1. 人工让租户端不发送 ACK 或阻断 ACK topic
2. 等待超时时间过去
3. 平台端触发定向全量补偿
4. 恢复 ACK 后，链路重新恢复正常

## 7. 回归测试清单

每次修改以下类时必须跑回归：

- `TenantSyncMessageSupport`
- `TenantClientSyncHandler`
- `TenantServerSyncPublisher`
- `TenantServerSyncRequestHandler`
- `TenantServerSyncAckHandler`
- `TenantProperties`

至少要跑的命令：

```bash
mvn -pl spring-api-support-common-system-starter,spring-api-support-common-tenant-starter -am test
mvn -pl spring-api-support-common-system-starter,spring-api-support-common-tenant-starter -am -DskipTests compile
mvn --% -pl spring-api-support-common-tenant-starter -DskipTests -Dspotbugs.onlyAnalyze=com.chua.starter.tenant.client.TenantClientSyncHandler,com.chua.starter.tenant.client.TenantSyncClientCustomizer,com.chua.starter.tenant.client.TenantSyncClientInfoCustomizer,com.chua.starter.tenant.properties.TenantProperties,com.chua.starter.tenant.server.sync.TenantServerSyncPublisher,com.chua.starter.tenant.server.sync.TenantServerSyncRequestHandler,com.chua.starter.tenant.server.sync.TenantServerSyncAckHandler,com.chua.starter.tenant.server.sync.TenantSyncAckTracker,com.chua.starter.tenant.sync.TenantSyncMessageSupport com.github.spotbugs:spotbugs-maven-plugin:4.9.8.2:check
```

## 8. 验收标准

租户同步功能在以下条件同时满足时可视为通过：

1. 平台端和租户端正常建立连接。
2. 签名错误消息不会被处理。
3. 重复消息不会重复入库。
4. 乱序旧消息不会覆盖新数据。
5. 正常消息处理后会回 ACK。
6. ACK 超时后能触发定向全量补偿。
7. 平台端不会默认把某租户全量数据广播给所有租户端。
8. 租户端落库逻辑支持重复执行且结果一致。

## 9. 建议的测试实现顺序

建议按这个顺序补自动化测试：

1. `TenantSyncMessageSupport`
2. `TenantSyncAckTracker`
3. `TenantServerSyncAckHandler`
4. `TenantClientSyncHandler`
5. `TenantSyncClientCustomizer`
6. `TenantServerSyncPublisher`

这样能先把纯函数和状态机测稳，再测带网络语义的发布器。
