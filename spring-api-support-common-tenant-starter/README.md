# spring-api-support-common-tenant-starter

多租户管理模块，提供租户、服务、菜单的管理和同步功能。

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
