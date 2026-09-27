# spring-api-support-common-video-starter

通用视频服务模块 —— 面向前端 [`vue-support-video-starter`](../../../vue-support-parent-starter/apps/vue-support-video-starter) 提供视频资源中心的列表检索、分类浏览、详情与数据源接口。

## 能力

- **多源聚合检索**：携带关键词时，委托 [`utils-support-datasearch-starter`](../../../utils-support-parent-starter) 的视频 SPI（`VideoSearchService` + `ResourceProvider`）并发检索 MacCMS / 豆瓣 / B 站 / 网盘等全部已注册数据源。
- **分类浏览**：无关键词时（首页、频道页），通过 MacCMS 兼容资源站采集协议拉取最新资源，并按前端分类码（`movie/tv/anime/variety/documentary`）过滤。
- **视频详情**：优先命中最近浏览条目的内存缓存，其次走资源站 `ac=detail`，最后按 originId 回退聚合检索。
- **电视直播**：内置央视 / 卫视 / 特色频道目录，多候选流自动回退；HLS 经后端反向代理（重写 m3u8 分片与密钥地址），规避公开源无 CORS、http 混合内容与 Referer 限制，并内置同根域白名单防 SSRF。
- **配置数据库存储**：视频采集地址、兜底关键词、分类关键词与整套直播目录持久化到通用配置表 `sys_setting`（group=`video`），复用 `/v2/setting` 管理端、变更历史与导入导出；首次启动用内置 JSON 播种，存储不可用时自动回退内置 JSON。
- **统一信封**：所有接口返回 `ReturnResult`（成功码 `00000`）。

## 接口

基础路径 `/video/api`（前端 Vite 开发代理 `/video/api` → 后端端口）。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/video/api/video/list` | 列表：`keyword`、`videoType`、`year`、`district`、`tag`、`page`、`pageSize` |
| GET | `/video/api/video/{id}` | 按 `videoOriginId` 查询详情（含播放地址） |
| GET | `/video/api/video/sources` | 已注册的视频数据源名称列表 |
| GET | `/video/api/live/groups` | 直播分组（央视 / 卫视 / 特色） |
| GET | `/video/api/live/channels` | 直播频道：`group`、`cctv` 过滤 |
| GET | `/video/api/live/channel/{id}` | 直播频道详情（含候选源数量） |
| GET | `/video/api/live/{id}/play.m3u8?src=N` | 频道主播放列表（经后端 HLS 代理，`src` 为候选源下标） |
| GET | `/video/api/live/{id}/resource?src=N&u=...` | 代理子清单 / 分片（内部使用） |
| GET | `/video/api/config` | 当前生效配置概览（存储模式、采集地址、关键词、直播频道数） |
| POST | `/video/api/config/reload` | 从配置数据库热加载配置与直播目录 |

## 配置

前缀 `spring.video`：

| 键 | 默认值 | 说明 |
| --- | --- | --- |
| `enabled` | `true` | 是否启用视频服务模块 |
| `catalog-url` | `https://cj.lziapi.com/api.php/provide/vod/` | MacCMS 兼容采集接口，置空则关闭分类浏览 |
| `default-keyword` | 空 | 分类浏览不可用时的兜底检索关键词 |
| `max-results` | `60` | 单次返回的最大条目数 |
| `detail-cache-size` | `500` | 详情缓存容量（LRU） |
| `connect-timeout` | `8000` | 连接超时（毫秒） |
| `read-timeout` | `15000` | 读取超时（毫秒） |
| `browse-keywords` | 空 | 分类码 → 检索关键词映射，如 `movie: 电影` |
| `live-enabled` | `true` | 是否启用直播模块 |
| `live-channel-file` | 空 | 外部直播目录：`.json` 目录或 `.m3u/.m3u8` 播放列表；置空用内置 `video/live-channels.json` |
| `live-allowed-hosts` | 空 | HLS 代理额外允许的主机后缀白名单（跨 CDN 分片时使用） |
| `live-proxy-max-bytes` | `8388608` | HLS 代理单次响应最大字节数 |

## 配置数据库存储

视频/直播配置持久化在通用配置表 `sys_setting` 的 `video` 分组，随通用配置服务（`spring-api-support-common-service-starter`）自动启用；`/v2/setting?sysSettingGroup=video` 可直接查看与维护。

| 配置名称 | 类型 | 说明 |
| --- | --- | --- |
| `catalogUrl` | String | 资源站采集接口地址（覆盖 `spring.video.catalog-url`） |
| `defaultKeyword` | String | 兜底检索关键词 |
| `browseKeywords` | JSON | 分类码 → 关键词映射 |
| `liveGroup.<id>` | JSON | 直播分组：`{"label":"央视","cctv":true,"sort":1}` |
| `liveChannel.<id>` | JSON | 直播频道：`{"name":"CCTV-1 综合","group":"cctv","sort":1}` |
| `liveSource.<id>.<n>` | String | 频道第 n 路候选流地址 |

> 受 `sys_setting_value`（`varchar(255)`）限制，配置值均 ≤255 字符，直播目录因此**按分组 / 频道 / 源逐行存储**。

行为说明：

1. **首次启动播种**：`video` 分组尚无频道行时，用内置 `video/live-channels.json`（或 `live-channel-file`）写入数据库。
2. **数据库优先**：之后目录与标量均以数据库为准；在 `/v2/setting` 修改后调用 `POST /video/api/config/reload` 热加载即时生效。
3. **降级**：宿主未引入通用配置服务，或 `video` 分组为空时，回退 `spring.video.*` 与内置 JSON。

## 启用

添加依赖：

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-video-starter</artifactId>
    <version>4.0.0.42</version>
</dependency>
```

模块通过 `META-INF/spring/...EnableAutoConfiguration.imports` 自动装配，无需额外配置。

配置落库需要宿主引入通用配置服务（系统应用已包含）；未引入时自动回退内置 JSON：

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-service-starter</artifactId>
</dependency>
```
