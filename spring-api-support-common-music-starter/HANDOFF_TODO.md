# spring-api-support-common-music-starter 交接清单

## 项目定位

- 后端接口模块: `spring-api-support-common-music-starter`
  - 这是音乐能力的核心模块。
  - 负责提供 `MusicController`、`MusicFacadeService`、`MusicSourceProvider SPI`、音频流代理、统一数据模型。
  - 代码路径:
    - `src/main/java/com/chua/starter/music/controller`
    - `src/main/java/com/chua/starter/music/service`
    - `src/main/java/com/chua/starter/music/spi`
    - `src/main/java/com/chua/starter/music/model`

- 后端实际承载接口的 Spring Boot 工程: `spring-api-support-monitor-starter`
  - 该工程已经依赖 `spring-api-support-common-music-starter`。
  - 启动它后，音乐接口可通过 `/v1/music/**` 暴露。
  - 重点确认:
    - `spring-api-support-monitor-starter/pom.xml`
    - `spring-api-support-common-music-starter/src/main/java/com/chua/starter/music/controller/MusicController.java`

- 前端项目: `vue-support-parent-starter/pages/music`
  - 这是独立前端仓库里的音乐页面模块，不在当前仓库中。
  - 负责首页、搜索、歌单、播放器、歌词、收藏、最近播放等界面与交互。
  - 与当前后端通过 HTTP 接口联调，不再使用 IPC。

## 当前代码状态

- 已有音乐 SPI 音源:
  - `tx`: QQ 音乐
  - `kw`: 酷我
  - `mg`: 咪咕
  - `wy`: 网易云
  - `kg`: 酷狗
  - `bd`: 波点
  - `git`: GitCode 公共曲库
  - `demo`: 本地演示数据

- 已落下来的后端能力:
  - 音源 SPI 扩展接口和注册中心
  - 聚合搜索歌曲
  - 聚合搜索歌单
  - 首页概览
  - 歌单分类与歌单详情
  - 歌曲详情
  - 音频流 HTTP 代理
  - 评论数、播放数、歌词等字段的模型支撑

- 当前默认音源优先级:
  - `tx -> kw -> mg -> wy -> kg -> bd -> git -> demo`

## 关键接口

- `GET /v1/music/sources`
- `GET /v1/music/overview`
- `GET /v1/music/search`
- `GET /v1/music/search/playlists`
- `GET /v1/music/playlist/categories`
- `GET /v1/music/playlist/category`
- `GET /v1/music/playlist/{source}/{playlistId}`
- `GET /v1/music/track/{source}/{trackId}`
- `GET /v1/music/track/{source}/{trackId}/stream`

## 后续优先任务

### P0: 先完成可用性

- 补真实联调验收
  - 启动 `spring-api-support-monitor-starter`
  - 实测以上全部 `/v1/music/**` 接口
  - 至少验证每个音源一次搜索、一次歌单详情、一次歌曲播放

- 处理音源失败兜底
  - 明确每个 provider 的超时、空结果、限流、失效返回
  - 聚合搜索时对单个 provider 失败做隔离，避免一个源异常拖垮全部搜索

- 补接口验收文档
  - 给前端列出每个接口的请求参数、返回字段、异常情况
  - 标清 `source=all` 的聚合行为

### P1: 完善数据源

- 把 `bd` 从“歌单/详情可用”继续补到“搜索可用”
  - 继续逆向波点前端 JS
  - 找稳定搜索接口、分类接口、榜单接口

- 增加新的 SPI 数据源
  - 建议优先做“通用远程 JSON 曲库 provider”
  - 建议再做“本地目录/本地 JSON 曲库 provider”
  - 保证新增源只通过 `MusicSourceProvider` 接入

- 给各 provider 加配置说明
  - 例如代理、Cookie、UA、备用域名、可覆盖的基础 URL
  - 用系统属性或环境变量统一约定

### P1: 完善播放能力

- 强化 `MusicStreamProxyService`
  - 继续验证 Range 请求
  - 验证浏览器拖动进度条的兼容性
  - 验证不同上游内容类型的透传

- 补歌词能力
  - 统一歌词格式
  - 明确是否返回原始 LRC
  - 处理纯文本歌词和逐字时间轴歌词的差异

- 补评论能力
  - 已有模型字段，但不是所有 provider 都真正取了评论
  - 前端需要时，继续把可拿到评论的平台补齐

### P2: 代码与测试

- 抽 provider 公共基类
  - 搜索分页、歌单分页、字段映射、评论映射存在重复逻辑
  - 可以统一到 `spi/support`

- 增加测试工程或集成测试
  - 最少补一个音乐模块的 `SpringBootTest`
  - 最好增加真实接口 smoke test
  - 避免后续升级后把已有平台打坏

- 增加缓存
  - 对搜索、歌单详情、歌曲详情做短期缓存
  - 降低第三方接口频率和失败率

## 联调建议

- 后端先跑:
  - `spring-api-support-monitor-starter`

- 前端再跑:
  - `vue-support-parent-starter/pages/music`

- 联调顺序建议:
  1. `/v1/music/sources`
  2. `/v1/music/overview`
  3. `/v1/music/search?source=all`
  4. `/v1/music/playlist/{source}/{playlistId}`
  5. `/v1/music/track/{source}/{trackId}`
  6. `/v1/music/track/{source}/{trackId}/stream`

## 在别的电脑继续时最先做的事

- 拉取当前仓库和前端仓库最新代码
- 启动 `spring-api-support-monitor-starter`
- 确认 `/v1/music/sources` 能返回音源列表
- 打开前端 `pages/music`，先接通搜索和播放
- 再按上面的 P0 -> P1 -> P2 顺序推进
