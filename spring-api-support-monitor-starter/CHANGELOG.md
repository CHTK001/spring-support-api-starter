# Changelog - spring-api-support-monitor-starter

## [Unreleased]
### Added
- Arthas 控制台后端代理能力：
  - POST `/v1/arthas/connect`：返回后端代理的控制台URL
  - POST `/v1/arthas/disconnect`：断开（占位）
  - GET `/v1/arthas/nodes`：仅返回包含 `report.client.arthas.port` 的在线节点
  - GET `/v1/arthas/console/{nodeId}/**`：控制台静态资源/HTML HTTP 代理（带内嵌`/ws`路径改写）
  - WS `/v1/arthas/console/{nodeId}/ws`：WebSocket 反向代理
- 工具类：`ArthasProxyUtils` 统一元数据键与地址处理

### Changed
- WebSocket 配置：在 `GuacamoleWebSocketConfig` 注册了 Arthas WS 端点 `/v1/arthas/console/**/ws`
- 前端页面采用后端代理地址加载控制台（iframe）

### Notes
- 元数据键名：`report.client.arthas.port`（对应 REPORT_ARTHAS_CLIENT_PORT）
- 示例值：`ws://arthas-tunnel:7777/ws`
- 安全建议：在 WS/HTTP 代理前增加鉴权/白名单控制

