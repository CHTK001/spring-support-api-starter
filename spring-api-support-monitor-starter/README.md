# spring-api-support-monitor-starter

服务器监控中心后端模块，提供服务器主机管理、服务器布局、ACME 证书自动化等能力。

> 注：Docker 软件库（在线搜索/私有仓库同步）功能于 2026-07-28 暂时移除，相关接口（`/v1/system/soft/*`）暂不可用，后续按需要恢复。

---

## 服务器布局与组件布局管理（2026-06-08）

- 服务器布局配置已完成完整闭环：列表、默认布局、创建、更新、删除、按类型查询、从组件生成、应用到组件、复制到其他服务器
- 组件布局配置已完成完整闭环：按服务器/组件查询、启用禁用、位置更新、批量保存、统计、复制、重置
- 服务器布局默认配置已支持自动生成，默认包含 CPU、内存、磁盘、网络、系统信息等基础节点
- 布局配置采用 JSON 存储，前端可直接回显 nodes / edges / version / generatedAt 等结构

### 布局接口

**服务器布局**

- `GET /v1/gen/server/layout/list/{serverId}`：获取服务器布局列表
- `GET /v1/gen/server/layout/default/{serverId}`：获取服务器默认布局
- `POST /v1/gen/server/layout/save`：保存服务器布局配置
- `PUT /v1/gen/server/layout/update`：更新服务器布局配置
- `DELETE /v1/gen/server/layout/delete/{layoutId}`：删除服务器布局配置
- `PUT /v1/gen/server/layout/default/{serverId}/{layoutId}`：设置默认布局
- `POST /v1/gen/server/layout/generate/{serverId}?layoutName=xxx`：从组件配置生成布局
- `POST /v1/gen/server/layout/apply/{serverId}/{layoutId}`：应用布局到服务器组件
- `POST /v1/gen/server/layout/copy/{sourceLayoutId}/{targetServerId}?layoutName=xxx`：复制布局到其他服务器
- `GET /v1/gen/server/layout/detail/{layoutId}`：获取布局详情
- `GET /v1/gen/server/layout/type/{layoutType}`：根据类型获取布局列表
- `POST /v1/gen/server/layout/create-default/{serverId}`：创建默认布局
- `POST /v1/gen/server/layout/component/config/{serverId}`：保存组件图表配置
- `GET /v1/gen/server/layout/component/config/{serverId}/{componentId}`：获取组件图表配置
- `POST /v1/gen/server/layout/component/config/batch/{serverId}`：批量保存组件图表配置

**组件布局**

- `GET /monitor/server/component/layout/server/{serverId}`：获取服务器组件布局列表
- `GET /monitor/server/component/layout/server/{serverId}/enabled`：获取启用的组件布局列表
- `GET /monitor/server/component/layout/server/{serverId}/component/{componentId}`：获取指定组件布局
- `POST /monitor/server/component/layout`：保存组件布局配置
- `PUT /monitor/server/component/layout`：更新组件布局配置
- `DELETE /monitor/server/component/layout/{layoutId}`：删除组件布局配置
- `DELETE /monitor/server/component/layout/component/{componentId}`：按组件删除布局
- `DELETE /monitor/server/component/layout/server/{serverId}`：按服务器删除全部布局
- `POST /monitor/server/component/layout/batch/position`：批量更新位置
- `POST /monitor/server/component/layout/create`：创建组件布局
- `PUT /monitor/server/component/layout/{layoutId}/position`：更新组件位置
- `PUT /monitor/server/component/layout/{layoutId}/status`：启用/禁用布局
- `GET /monitor/server/component/layout/server/{serverId}/statistics`：获取布局统计
- `POST /monitor/server/component/layout/copy`：复制布局到其他服务器
- `POST /monitor/server/component/layout/server/{serverId}/reset`：重置服务器布局

### 布局字段

**MonitorSysGenServerLayout**

| 字段 | 说明 |
|------|------|
| monitorSysGenServerLayoutId | 布局ID |
| monitorSysGenServerId | 关联服务器ID |
| monitorSysGenServerLayoutName | 布局名称 |
| monitorSysGenServerLayoutConfig | 布局配置 JSON |
| monitorSysGenServerLayoutType | 布局类型（default/generated/copied/custom） |
| monitorSysGenServerLayoutDefault | 是否默认布局 |
| monitorSysGenServerLayoutStatus | 布局状态 |
| monitorSysGenServerLayoutDescription | 布局描述 |
| monitorSysGenServerLayoutVersion | 布局版本 |
| monitorSysGenServerLayoutSort | 布局排序 |

**MonitorSysGenServerComponentLayout**

| 字段 | 说明 |
|------|------|
| monitorSysGenServerComponentLayoutId | 组件布局ID |
| monitorSysGenServerId | 关联服务器ID |
| monitorSysGenServerComponentId | 关联组件ID |
| monitorSysGenServerComponentLayoutX | X 坐标 |
| monitorSysGenServerComponentLayoutY | Y 坐标 |
| monitorSysGenServerComponentLayoutW | 宽度 |
| monitorSysGenServerComponentLayoutH | 高度 |
| monitorSysGenServerComponentLayoutZIndex | 层级 |
| monitorSysGenServerComponentLayoutMovable | 是否可移动 |
| monitorSysGenServerComponentLayoutResizable | 是否可调整大小 |
| monitorSysGenServerComponentLayoutStatus | 布局状态 |
| monitorSysGenServerComponentLayoutSortOrder | 排序顺序 |

### 任务状态

- 服务器布局管理：✅ 已完成
- 组件布局管理：✅ 已完成
- 默认布局生成：✅ 已完成
- 布局复制与重置：✅ 已完成
- 前后端字段对齐：✅ 已完成

---

## ACME 证书管理

自动化 SSL 证书管理，支持 Let's Encrypt 证书申请、续签、吊销。

### 接口

- `POST /v1/cert/account/save`：保存/更新 ACME 账户
- `POST /v1/cert/apply`：申请证书
- `POST /v1/cert/{certId}/renew`：续签证书
- `POST /v1/cert/{certId}/revoke`：吊销证书
- `GET /v1/cert/{certId}`：获取证书详情

### 配置

```yaml
monitor:
  acme:
    enabled: true
    renew-days-before: 30
```

### 流程说明

1. 申请时若验证资源未就绪，返回 certId 并把验证指引写入 `monitor_acme_order.acme_authz_urls`
2. 按 JSON 中的 httpPath/httpContent 或 dnsName/dnsValue 完成验证
3. 成功签发后，证书链与私钥（PEM）落库到 `monitor_acme_certificate`

### 自动续期

每日 03:00 检查即将到期证书并触发续签。

---

## 注意事项

- Mapper 规范：所有 SQL 放置于 `resources/mapper/*.xml`，禁止出现在 Java 注解中
- 安全：建议在拦截器中增加鉴权/白名单控制
- 代理前缀：默认后端基座为 `/monitor/api`
