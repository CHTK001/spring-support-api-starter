# spring-api-support-common-video-starter

通用视频服务模块 —— 面向前端 [`vue-support-video-starter`](../../../vue-support-parent-starter/apps/vue-support-video-starter) 提供视频资源中心的列表检索、分类浏览、详情与数据源接口。

## 能力

- **多源聚合检索**：携带关键词时，委托 [`utils-support-datasearch-starter`](../../../utils-support-parent-starter) 的视频 SPI（`VideoSearchService` + `ResourceProvider`）并发检索 MacCMS / 豆瓣 / B 站 / 网盘等全部已注册数据源。
- **分类浏览**：无关键词时（首页、频道页），通过 MacCMS 兼容资源站采集协议拉取最新资源，并按前端分类码（`movie/tv/anime/variety/documentary`）过滤。
- **本地索引**：资源站目录由 job-starter 的 `@Job` 增量落进 `video_info`，列表、筛选、排序、分页与详情改查本地库；库空、未引入 MyBatis-Plus 或查询异常时自动回落资源站直查（与改造前行为一致）。
- **视频详情**：优先命中详情缓存，其次取本地索引的 `video_payload` 快照（含播放线路与下载资源），再走资源站 `ac=detail`，最后按 originId 回退聚合检索。
- **电视直播**：内置央视 / 卫视 / 特色频道目录，多候选流自动回退；HLS 经后端反向代理（重写 m3u8 分片与密钥地址），规避公开源无 CORS、http 混合内容与 Referer 限制，并内置同根域白名单防 SSRF。
- **配置数据库存储**：视频采集地址、兜底关键词、分类关键词与整套直播目录持久化到通用配置表 `sys_setting`（group=`video`），复用 `/v2/setting` 管理端、变更历史与导入导出；首次启动用内置 JSON 播种，存储不可用时自动回退内置 JSON。
- **页面化配置**：前端 `vue-support-video-starter` 的配置页（路由 `/config`）直接读写上述配置（`/config`、`/config/export`、`/config/import`、`/config/reload`、`/config/sync`），无需改配置文件。
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
| GET | `/video/api/config/export` | 导出配置快照（标量 + 整段直播目录），用于备份/跨环境迁移 |
| POST | `/video/api/config/import?overwrite=true` | 导入配置快照并热加载 |
| POST | `/video/api/config/sync` | 手动触发配置同步（拉取远程快照 + 热加载） |
| POST | `/video/api/config/reload` | 从配置数据库热加载配置与直播目录 |
| POST | `/video/api/config/catalog/sync` | 手动触发目录同步（资源站 → `video_info`），返回 `pages/fetched/upserted/total` |
| POST | `/video/api/config/catalog/detail/refresh` | 手动刷新连载剧集详情快照（集数 / 网盘地址） |

## 配置

前缀 `spring.video`：

| 键 | 默认值 | 说明 |
| --- | --- | --- |
| `enabled` | `true` | 是否启用视频服务模块 |
| `catalog-url` | `https://cj.lziapi.com/api.php/provide/vod/` | MacCMS 兼容采集接口，置空则关闭分类浏览 |
| `catalog-store-enabled` | `true` | 是否用本地索引承接列表/详情（关闭或未引入 MyBatis-Plus 时全部直连资源站） |
| `catalog-sync-enabled` | `true` | 是否注册目录同步任务（依赖 job-starter） |
| `catalog-sync-pages` | `3` | 每轮目录同步拉取的页数 |
| `catalog-detail-refresh-limit` | `20` | 每轮连载详情刷新的条数上限 |
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
| `config-sync-enabled` | `true` | 是否注册配置同步任务（依赖 job-starter） |
| `config-sync-url` | 空 | 远程配置快照（`VideoConfigSnapshot` JSON）地址；置空则仅本地重载 |
| `config-sync-overwrite` | `true` | 配置同步是否覆盖已存在项 |

## 配置数据库存储

视频/直播配置持久化在通用配置表 `sys_setting` 的 `video` 分组，随通用配置服务（`spring-api-support-common-service-starter`）自动启用；`/v2/setting?sysSettingGroup=video` 可直接查看与维护。

| 配置名称 | 类型 | 说明 |
| --- | --- | --- |
| `catalogUrl` | String | 资源站采集接口地址（覆盖 `spring.video.catalog-url`） |
| `defaultKeyword` | String | 兜底检索关键词 |
| `browseKeywords` | JSON | 分类码 → 关键词映射 |
| `liveCatalog` | JSON | 直播目录整段：分组 + 频道 + 候选源 |

> `sys_setting_value` 已放宽为 `TEXT`，直播目录以整段 JSON 存于 `liveCatalog`，可直接在 `/v2/setting` 编辑。
> 兼容早期按行存储的 `liveGroup.* / liveChannel.* / liveSource.*`：读取时优先整段 JSON，缺失则回退逐行组装。
>
> **已有库升级**：脚本模式为 `ONCE`，仅空库执行，存量库请手动执行
> `ALTER TABLE sys_setting MODIFY COLUMN sys_setting_value TEXT NULL COMMENT '配置值 - 配置项的值';`
> （未升级时写入超长目录会被数据库拒绝，模块自动回退内置 JSON，不影响启动）。

行为说明：

1. **首次启动播种**：`video` 分组尚无频道行时，用内置 `video/live-channels.json`（或 `live-channel-file`）写入数据库。
2. **数据库优先**：之后目录与标量均以数据库为准；在 `/v2/setting` 修改后调用 `POST /video/api/config/reload` 热加载即时生效。
3. **降级**：宿主未引入通用配置服务，或 `video` 分组为空时，回退 `spring.video.*` 与内置 JSON。
4. **迁移/备份**：`GET /video/api/config/export` 导出领域化快照（标量 + 整段直播目录），在目标环境 `POST /video/api/config/import` 导入并自动热加载；`overwrite=false` 可只补缺失项。（与模块其他接口一致未内置鉴权，公网暴露时请置于网关鉴权之后。）

### 配置同步（job-starter）

宿主引入 `spring-support-job-starter` 时，模块自动注册任务 `video.config.sync`（`@Job` 注解，默认 cron `0 0/10 * * * ?`，每 10 分钟）：

1. 若配置了 `spring.video.config-sync-url`，拉取远程 `VideoConfigSnapshot` 并导入配置库；
2. 随后热加载配置与直播目录，使本节点即时生效（多实例亦可通过共享配置库收敛）。

- 手动触发：`POST /video/api/config/sync`
- 关闭任务：`spring.video.config-sync-enabled=false`
- 调度时间可在 job-starter 治理端调整（`sys_job.sys_job_schedule_time`）

## 本地索引（video_info）

列表与详情不再每次直连第三方：资源站目录同步进 `video_info` 后，检索读本地库、详情读快照。

### 表结构

一张表：标量列承接筛选 / 排序 / 分页，详情（播放线路 + 剧集 + 网盘/直链）整体以 JSON 存 `video_payload`，不为线路和下载再拆子表；`video_origin_id` 唯一键保证同一资源站条目只保留一行。

建表脚本随模块交付，无需手工执行：

| 环境 | 脚本 |
| --- | --- |
| MySQL | `src/main/resources/db/init/V1.0__init_all_api_common_video.sql`（随宿主脚本模式执行） |
| H2（`h2` profile） | `spring-api-support-video-starter/src/main/resources/db/h2/V1.0__init_video_h2.sql` |

### 读路径

1. `GET /video/list`：本地库有数据 → 按 `keyword/videoType/year/district/tag` 过滤 + 排序 + 分页；
2. 库空、库未装配（未引入 MyBatis-Plus）或查询抛错 → 回落资源站直查，行为与改造前一致；
3. **纯筛选查空即为空**（不回落，避免跨条件串数据）；带关键词时库里没命中才回落上游，避免本地库偏小导致搜不到；
4. `GET /video/{id}`：详情缓存 → 本地 `video_payload`（含线路/下载）→ 资源站 `ac=detail` → 聚合检索兜底。

上游直查的结果会顺手回写本地库（写入失败只告警，不影响本次返回），首次访问的热数据会自然入库。列表行只是标量索引，不含线路与网盘，因此**不写入详情缓存**。

### 同步任务

| 任务 | cron | 说明 |
| --- | --- | --- |
| `video.catalog.sync` | `0 0/5 * * * ?` | 拉取最新 `catalog-sync-pages` 页并按 originId upsert |
| `video.catalog.detail` | `0 0 0/6 * * ?` | 刷新连载详情（集数、网盘地址随更新变化），每轮最多 `catalog-detail-refresh-limit` 条 |

- 依赖 job-starter：多实例由 leader election 保证单节点执行，结果写入 job 日志。
- 手动触发：`POST /video/api/config/catalog/sync`、`POST /video/api/config/catalog/detail/refresh`。
- 关闭任务：`catalog-sync-enabled=false`（不注册定时任务，手动接口仍可用）；整体停用本地索引：`catalog-store-enabled=false`。
- 采集站点不可达时同步记录 WARN 并返回 `fetched=0`，既有库数据继续对外服务。

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
