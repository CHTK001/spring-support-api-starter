# spring-api-support-common-tenant-starter

多租户管理模块，提供租户、服务、菜单的管理和同步功能。

## 文档入口

- [租户同步接入文档](./docs/TENANT_SYNC_ACCESS_GUIDE.md)
- [租户同步测试文档](./docs/TENANT_SYNC_TEST_GUIDE.md)

## 功能特性

- **租户管理**: 租户的增删改查、状态管理
- **服务管理**: 服务模块的定义和菜单关联
- **租户服务绑定**: 为租户分配服务权限
- **数据同步**: 服务端与客户端之间的租户数据同步
- **MyBatis-Plus 租户拦截器**: 自动添加租户条件

## 模块结构

```
com.chua.starter.tenant
├── entity/                    # 实体类
│   ├── SysTenant             # 租户实体
│   ├── SysService            # 服务实体
│   ├── SysServiceModule      # 服务模块实体
│   ├── SysServiceModuleItem  # 服务模块关联项
│   └── SysTenantService      # 租户服务关联
├── properties/
│   └── TenantProperties      # 租户配置属性
├── configuration/            # 配置类
│   ├── TenantAutoConfiguration
│   ├── TenantMybatisConfiguration
│   ├── TenantServerConfiguration
│   └── TenantClientConfiguration
├── spi/                       # SPI 接口
│   ├── TenantDataSyncProvider        # 同步提供者接口
│   └── DefaultTenantDataSyncProvider # 默认 JDBC 实现
├── client/                    # 客户端模块
│   └── TenantClientSyncHandler      # 客户端同步处理器
└── server/                    # 服务端模块
    ├── controller/
    ├── service/
    ├── mapper/
    ├── pojo/
    └── sync/
        └── TenantServerSyncPublisher # 同步消息发布器
```

## 配置说明

```yaml
plugin:
  mybatis-plus:
    tenant:
      # 通用配置
      tenant-id-column: sys_tenant_id    # 租户ID字段名
      ignore-table:                       # 忽略租户过滤的表
        - sys_tenant
        - sys_service
        - sys_service_module
      
      # 服务端配置
      server:
        enable: true                      # 启用服务端模式
        sync-enable: true                 # 启用同步功能
        sync-module: rsocket-sync         # 同步模块类型
      
      # 客户端配置
      client:
        enable: false                     # 启用客户端模式
        sync-enable: true                 # 启用同步功能
        sync-module: rsocket-sync         # 同步模块类型
```

## 使用方式

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-tenant-starter</artifactId>
    <version>4.0.0</version>
</dependency>
```

### 2. 服务端模式

服务端负责租户数据的管理和同步推送：

```yaml
plugin:
  mybatis-plus:
    tenant:
      server:
        enable: true
        sync-enable: true
```

### 3. 客户端模式

客户端接收服务端的同步消息并更新本地数据：

```yaml
plugin:
  mybatis-plus:
    tenant:
      client:
        enable: true
        sync-enable: true
```

### 4. 自定义同步实现

实现 `TenantDataSyncProvider` 接口可自定义同步逻辑：

```java
@Component
public class CustomTenantDataSyncProvider implements TenantDataSyncProvider {
    
    @Override
    public int getOrder() {
        return 0; // 优先级，值越小优先级越高
    }
    
    @Override
    public boolean syncTenantUser(SysTenant tenant, String operation) {
        // 自定义用户同步逻辑
        return true;
    }
    
    @Override
    public boolean syncTenantMenu(Integer tenantId, List<Integer> menuIds) {
        // 自定义菜单同步逻辑
        return true;
    }
}
```

## API 接口

### 租户管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v2/tenant/page` | 分页查询租户 |
| GET | `/v2/tenant/list` | 查询租户列表 |
| GET | `/v2/tenant/get` | 获取租户详情 |
| POST | `/v2/tenant/save` | 保存租户 |
| PUT | `/v2/tenant/update` | 更新租户 |
| DELETE | `/v2/tenant/delete` | 删除租户 |
| PUT | `/v2/tenant/bind/service` | 绑定租户服务 |
| PUT | `/v2/tenant/status` | 更新租户状态 |
| GET | `/v2/tenant/endpoint` | 查询租户服务器地址 |
| PUT | `/v2/tenant/init-database` | 初始化/补齐租户库业务表（幂等） |

### 按租户切库

平台可以为每个租户登记一套独立数据库（`sys_tenant_host/port/ip/dbname/sql_username/sql_password`），
请求命中租户时整条链路路由到该库。租户是否使用独立库由租户记录（前端「实例来源 + 库名」）决定，默认开启；显式设为 `false` 可整体关闭。

```yaml
plugin:
  mybatis-plus:
    tenant:
      server:
        enable: true
        datasource-routing-enable: true          # 总开关
        datasource-routing-tenant-source: session # session / header / session-or-header
        datasource-routing-fail-closed: true     # 有租户身份但拿不到租户库时拒绝（默认 true）
        datasource-routing-prefix: "tenant-"     # 注册名前缀，不能带下划线数字（会被当作负载均衡组）
        datasource-routing-url-template: "jdbc:mysql://{host}{port}/{dbname}?useUnicode=true"
        datasource-routing-auto-provision: true   # 建租户时自动建库建账号（仅 MySQL，默认开启）
        datasource-routing-auto-init-scripts: true # 建库后用同一批 db/init 脚本初始化业务表
        datasource-routing-init-data-enable: true  # 初始化时一并执行 __initdata_ 种子脚本
        # datasource-routing-init-script-locations: "classpath*:db/init/*.sql,classpath*:db/migration/*.sql"
```

要点：

- **租户标识来源默认 `session`**，只认登录会话里的 `tenantId`。`X-Tenant-Id` 请求头可被客户端伪造，
  默认不生效；只有确需跨租户查询时才改成 `header` 或 `session-or-header`，并在接口上自行校验归属。
  登录成功与令牌校验都会把 `tenantId` 单独写进会话（`AuthSessionUtils.setTenantId`），
  过滤器不依赖认证服务能否从 `userInfo` 反解。
- **过滤器排在 `AuthFilter` 之后**（`LOWEST_PRECEDENCE - 5`）。租户标识由认证结果写入会话，
  排在认证之前会导致新会话的第一个请求读不到租户而回落主库（表现为「有时切、有时不切」）。
- **三种情形分开处理**，不要混成「一律回退主库」：

  | 情形 | 行为 |
  | --- | --- |
  | 无租户身份（登录、未登录页、健康检查） | 走主库 |
  | 有租户身份，但未登记库地址（存量共享库租户） | 走主库 |
  | 有租户身份，已登记库但连不上 / 口令错 / 建池超时 | **返回 503**（`fail-closed`） |
  | 有租户身份，但租户记录已被删除 | **返回 503** |

  最后两种若静默回退主库，租户用户的查询会读到平台共享库的数据、写操作直接落主库，
  属于数据越权而非可用性降级。`datasource-routing-fail-closed: false` 可恢复旧的回退行为，
  仅用于「部分租户已切库」的迁移期。
- `/v2/tenant/**`、`/v2/service/**` 等平台管理接口永不切库，否则会去租户库读 `sys_tenant`。
- `datasource-routing-auto-provision` 开启后需配 `admin-url/username/password`（需 CREATE DATABASE 权限），
  `admin-url` 要连实例而非某个库，例如 `jdbc:mysql://127.0.0.1:3306/mysql`。
- **建库只建库和账号，业务表由 `TenantDatabaseInitializer` 紧接着初始化**：拿租户自己的账号连上刚建好的库，
  执行与平台主库同一批 `db/init` 迁移脚本（`Flyway` SPI）。版本记录写在**租户库自己的**
  `sys_database_version`，因此：

  | 场景 | 结果 |
  | --- | --- |
  | 首次建租户库 | 建表 + 基础数据（`sys_role`/`sys_user` 等） |
  | 重复调用 `PUT /v2/tenant/init-database` | 只跑未执行过的脚本，已跑过的跳过 |
  | 存量租户库升级后补跑新增 `V1.x__alter_xxx.sql` | 增量脚本会执行 |

  `datasource-routing-auto-init-scripts: false` 可关掉这一步（只建库不建表，交给租户应用自行迁移）；
  `datasource-routing-init-data-enable: false` 只跳过种子数据。脚本位置默认与平台
  `plugin.datasource.script.script-path` 同源，改一边即可，不必维护两份表结构。
  建表失败不会让建租户接口报错（库已建好、租户记录已落库），改为记 WARN 并由
  `PUT /v2/tenant/init-database` 补做。
- **`/v2/setting` 的缓存键带租户维度**：切库后它会读租户库的 `sys_setting`，
  若缓存键不含租户，先访问的租户会把配置写进全局键，其它租户读到别人的配置。
  因此配置变更时该 cacheName 整体失效（`allEntries`），而不是按 key 精确清理。

### 租户服务器地址解析

租户库解决「数据在哪」，服务器地址解决「服务在哪」。`GET /v2/tenant/endpoint?sysTenantId=xxx`
按可信度返回：

1. `source=sync`、`online=true`——租户端进程连上平台的同步服务端后自报的地址，有连接为证；
2. `source=config`、`online=false`——退回 `sys_tenant_home_url` 人工登记值，仅代表「填了这个地址」。

租户端用 `client.tenant-base-url` 上报自己的对外地址；留空时按「首选非回环 IPv4 + server.port +
context-path」推导，容器或反向代理环境下通常不准，生产环境建议显式配置。只接受 `http(s)`，
其它 scheme 会被拒收，避免下游消费时变成任意协议访问。

## 数据库表

- `sys_tenant` - 租户表
- `sys_service` - 服务表
- `sys_service_module` - 服务模块表
- `sys_service_module_item` - 服务模块关联项表
- `sys_tenant_service` - 租户服务关联表

初始化 SQL 位于: `resources/db/init/V1.0__init_tenant.sql`

## 依赖模块

- `spring-api-support-common-system-starter` - 系统用户、菜单实体
- `spring-support-mybatis-starter` - MyBatis-Plus 基础支持
- `spring-support-sync-starter` - 同步通信模块（可选）

## 版本历史

### 4.0.0
- 从 `spring-support-mybatis-tenant-starter` 迁移
- 新增 SPI 机制支持自定义同步实现
- 优化配置结构，统一 tenantIdColumn 和 ignoreTable
- 集成服务端同步消息发布功能
