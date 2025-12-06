# Spring API Support Common Tenant Starter

租户管理模块 - 提供多租户支持，包括租户管理、租户服务绑定、租户数据同步等功能。

## 功能特性

- **租户管理**: 提供租户的增删改查功能
- **服务绑定**: 支持租户与服务的绑定关系管理
- **数据同步**: 基于 SyncServer 的租户数据实时同步
- **定向下发**: 根据租户编码定向发送数据给指定客户端

## 依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-tenant-starter</artifactId>
    <version>4.0.0</version>
</dependency>
```

## 模块依赖

- `spring-api-support-common-entity-starter` - 实体模块
- `spring-api-support-common-service-starter` - 服务模块
- `spring-support-mybatis-tenant-starter` - MyBatis 多租户支持
- `spring-support-sync-starter` - 数据同步服务（可选）

## API 接口

| 方法   | 路径              | 描述         |
| ------ | ----------------- | ------------ |
| GET    | `/v1/tenant/page` | 分页查询租户 |
| GET    | `/v1/tenant/{id}` | 获取租户详情 |
| POST   | `/v1/tenant`      | 保存租户     |
| PUT    | `/v1/tenant`      | 更新租户     |
| DELETE | `/v1/tenant/{id}` | 删除租户     |
| POST   | `/v1/tenant/bind` | 绑定租户服务 |
| POST   | `/v1/tenant/sync` | 同步租户数据 |

## 数据同步

当配置了 `SyncServer` 时，租户数据变更会自动同步给连接的客户端：

- **广播模式**: 默认广播给所有客户端
- **定向下发**: 如果客户端 metadata 中包含 `tenantCode`，则只发送给匹配的客户端

客户端连接时需要在 metadata 中设置 `tenantCode` 以接收定向推送：

```java
clientInfo.getMetadata().put("tenantCode", "your-tenant-code");
```

## 作者

- CH

## 版本

- 1.0.0 (2024/12/06)
