# spring-api-support-video-starter

视频平台应用 —— 装配 [`spring-api-support-common-video-starter`](../spring-api-support-common-parent/spring-api-support-common-video-starter/README.md)（视频检索 + 电视直播 + 配置数据库），面向前端 [`vue-support-video-starter`](../../../vue-support-parent-starter/apps/vue-support-video-starter)。

## 运行

默认 **H2 自包含**（零外部依赖），端口 **18172**，无 context-path：

```bash
# 1) 安装视频模块到本地仓库
mvn -pl spring-api-support-common-parent/spring-api-support-common-video-starter -am install -DskipTests
# 2) 打包并启动应用
mvn -pl spring-api-support-video-starter -am package -DskipTests
java -jar spring-api-support-video-starter/target/spring-api-support-video-starter-4.0.0.42.jar
```

启动后：

- 视频检索：`GET /video/api/video/list?keyword=...`
- 电视直播：`GET /video/api/live/channels`、`GET /video/api/live/{id}/play.m3u8?src=0`
- 配置概览/热加载/导入导出/同步：`GET /video/api/config`、`POST /video/api/config/reload`、`GET /video/api/config/export`、`POST /video/api/config/import`、`POST /video/api/config/sync`
- Swagger UI：`/swagger-ui.html`；H2 控制台：`/h2-console`（JDBC `jdbc:h2:file:./data/video_h2`，用户 `sa` 空密码）

前端开发：`vue-support-video-starter` 的 Vite 代理 `/video/api` 已指向 `http://localhost:18172`。

## 配置

| 项 | 默认 | 说明 |
| --- | --- | --- |
| `server.port` | `18172` | 服务端口 |
| `spring.profiles.active` | `h2` | 默认 H2；可切 `mysql` 接入外部库 |
| `spring.video.live-enabled` | `true` | 直播开关 |
| `spring.video.catalog-url` | 内置资源站 | 采集接口（可在配置库 `video` 分组覆盖） |
| `spring.video.config-sync-url` | 空 | 远程配置快照地址（定时同步用） |
| `plugin.job.enable` / `role` | `true` / `SINGLE` | job-starter：每 10 分钟执行 `video.config.sync` |
| `plugin.oauth.enable` | `false` | 单机演示关闭认证过滤（开放 `/video/api`） |

配置数据库：H2 下由 `H2SchemaConfiguration` 建表；生产 MySQL 用 `spring-api-support-common-service-starter` 自带的 `db/init/*.sql`。

## 说明

- H2 专用建表脚本：`src/main/resources/db/init/video/h2/V1.0__init_video_h2.sql`（通用 MySQL DDL 在 H2 下不执行），含配置表 `sys_setting` 与 job-starter 的 `sys_job / sys_job_log / sys_job_cluster`。
- 直播源为公开/官方 CDN，存在地区限制（央视官方源在中国大陆以外通常不可播），可通过 `spring.video.live-channel-file` 或配置库 `video.liveCatalog` 换成自有源。
