# spring-api-support-monitor-starter

本模块新增了“Arthas 管理（后端代理）”能力，支持由后端连接 arthas-console，前端仅进行渲染与操作。

## 功能概述
- 通过节点元数据键 `report.client.arthas.port`（对应 REPORT_ARTHAS_CLIENT_PORT）识别带 Arthas 的在线节点
- 由后端代理 Arthas 控制台：
  - 静态资源/HTML：HTTP 反向代理
  - WebSocket：WS 反向代理
- 前端页面独立：`arthas-managemenet`（选择在线节点 -> 连接 -> 左侧功能 + 控制台 iframe）

## 新增后端接口
- `POST /v1/arthas/connect?nodeId=xxx`
  - 说明：返回后端代理的控制台地址（示例：`/monitor/api/v1/arthas/console/{nodeId}/`）
- `POST /v1/arthas/disconnect?nodeId=xxx`
  - 说明：占位接口，当前不持有会话资源，直接返回成功
- `GET /v1/arthas/nodes`
  - 说明：仅返回元数据包含 `report.client.arthas.port` 的在线节点
- `GET /v1/arthas/console/{nodeId}/**`
  - 说明：Arthas 控制台 HTTP 反向代理（含 HTML 中 `'/ws'` -> `'/v1/arthas/console/{nodeId}/ws'` 的路径改写）
- `WS /v1/arthas/console/{nodeId}/ws`
  - 说明：Arthas 控制台 WebSocket 反向代理

## 关键实现
- 控制器：`com.chua.starter.monitor.arthas.ArthasConsoleProxyController`
- WS 代理：`com.chua.starter.monitor.arthas.ws.ArthasWebSocketProxyHandler`
- WS 注册：`com.chua.starter.monitor.configuration.GuacamoleWebSocketConfig`

## 元数据键说明
- 键名：`report.client.arthas.port`
- 示例值：`ws://arthas-tunnel:7777/ws`
- 处理逻辑：
  - HTTP 代理：`ws(s)://.../ws` -> `http(s)://.../`
  - WS 代理：保证上游为 `ws(s)://.../ws`

## 前端入口
- 页面：`apps/vue-support-monitor-starter/src/views/arthas-managemenet/index.vue`
- API：`apps/vue-support-monitor-starter/src/api/arthas-management.ts`
- 路由：`apps/vue-support-monitor-starter/src/router/modules/arthas-management.ts`

## 注意事项
- 安全：建议在拦截器中增加鉴权/白名单控制，避免代理被滥用
- 代理前缀：默认后端基座为 `/monitor/api`，如有变更请同步前端配置
- 依赖：需要存在 `RestTemplate` Bean；项目中已广泛使用，通常已配置

## 排障建议
- 控制台空白：检查节点是否正确上报 `report.client.arthas.port`
- 资源404：检查 `GET /v1/arthas/console/{nodeId}/` 是否正常返回 HTML/静态资源
- WS 失败：检查 `WS /v1/arthas/console/{nodeId}/ws` 是否可连通，以及上游 tunnel 状态

