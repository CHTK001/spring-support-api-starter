# spring-api-support-monitor-starter

## 新增：服务器布局与组件布局管理（2026-06-08）

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

- `monitorSysGenServerLayoutId`：布局ID
- `monitorSysGenServerId`：关联服务器ID
- `monitorSysGenServerLayoutName`：布局名称
- `monitorSysGenServerLayoutConfig`：布局配置 JSON
- `monitorSysGenServerLayoutType`：布局类型（default/generated/copied/custom）
- `monitorSysGenServerLayoutDefault`：是否默认布局
- `monitorSysGenServerLayoutStatus`：布局状态
- `monitorSysGenServerLayoutDescription`：布局描述
- `monitorSysGenServerLayoutVersion`：布局版本
- `monitorSysGenServerLayoutSort`：布局排序

**MonitorSysGenServerComponentLayout**

- `monitorSysGenServerComponentLayoutId`：组件布局ID
- `monitorSysGenServerId`：关联服务器ID
- `monitorSysGenServerComponentId`：关联组件ID
- `monitorSysGenServerComponentLayoutX`：X 坐标
- `monitorSysGenServerComponentLayoutY`：Y 坐标
- `monitorSysGenServerComponentLayoutW`：宽度
- `monitorSysGenServerComponentLayoutH`：高度
- `monitorSysGenServerComponentLayoutZIndex`：层级
- `monitorSysGenServerComponentLayoutMovable`：是否可移动
- `monitorSysGenServerComponentLayoutResizable`：是否可调整大小
- `monitorSysGenServerComponentLayoutStatus`：布局状态
- `monitorSysGenServerComponentLayoutSortOrder`：排序顺序

### 任务状态

- 服务器布局管理：✅ 已完成
- 组件布局管理：✅ 已完成
- 默认布局生成：✅ 已完成
- 布局复制与重置：✅ 已完成
- 前后端字段对齐：✅ 已完成
- 可继续完善项：布局生成结果的前端可视化模板、默认布局样式预设、字段级校验提示统一化

## 新增：Spider 爬虫模块增强（2026-03-27）

- 新增 `plugin.monitor.spider` 配置前缀，支持爬虫模块开关、自动建表、页面解析深度和预览超时配置
- 新增爬虫表结构初始化能力，脚本位于 `src/main/resources/db/spider/V1.0__init_spider_tables.sql`
- 新增爬虫输出表管理接口，支持检查表是否存在、预览建表 SQL、创建表、读取表结构、同步缺失字段
- 增强网页可视化解析接口，返回更完整的 DOM 树、XPath、CSS Selector、属性信息和节点摘要，便于前端做可视化编排与 XPath 回填
- 新增 Spider AI 分析接口，可结合页面 HTML、DOM 摘要和当前选择器生成抓取建议

### 爬虫模块配置

```yaml
plugin:
  monitor:
    spider:
      enabled: true
      auto-create-table: false
      request-timeout-seconds: 30
      preview-max-depth: 10
      preview-max-children: 50
      preview-text-limit: 200
      ai:
        enabled: true
        default-model: ""
        max-tokens: 1800
        temperature: 0.2
        max-html-chars: 12000
        max-node-count: 40
        max-selector-matches: 8
```

### 爬虫模块接口

**表结构管理**

- `GET /v1/spider/table/status`：检查爬虫表是否完整
- `POST /v1/spider/table/initialize?force=false`：初始化/补齐爬虫表

### 爬虫测试页

- `GET /spider-console/lab`：打开轻量 Spider Lab，直接测试页面预览、DOM 解析、选择器命中、Spider AI 分析和表结构初始化

**数据库输出**

- `GET /v1/spider/data-source/options`：获取可用数据库数据源
- `POST /v1/spider/output/table/exists?tableName=xxx`：检查输出表是否存在
- `POST /v1/spider/output/table/preview-sql?tableName=xxx&dbType=mysql`：预览建表 SQL
- `POST /v1/spider/output/table/create?tableName=xxx&dbType=mysql`：创建输出表
- `POST /v1/spider/output/table/structure?tableName=xxx`：获取输出表结构
- `POST /v1/spider/output/table/sync-structure?tableName=xxx&dbType=mysql`：同步输出表结构

**可视化页面解析**

- `GET /v1/spider/preview?url=xxx`：获取页面 HTML 预览
- `GET /v1/spider/parse-html?url=xxx&maxDepth=10&maxChildren=50`：解析可视化 DOM 树
- `POST /v1/spider/test-selector`：测试 XPath/CSS 选择器，支持直接传 `html`
- `POST /v1/spider/inspect-selector`：获取命中节点的 XPath、CSS Selector、属性和 HTML 摘要
- `POST /v1/spider/generate-xpath`：将 CSS 路径转换为 XPath，或直接返回已选 XPath

**Spider AI**

- `GET /v1/spider/ai/status`：读取 Spider AI 状态与支持模型列表
- `POST /v1/spider/ai/analyze`：分析当前页面、HTML 片段和选择器，输出字段建议、选择器建议和抓取策略

## 新增：同步功能迁移至 spring-support-sync-starter（2026-02-09）

- 同步相关实体、Mapper、Service、配置与 WebSocket 推送能力已统一迁移到 spring-support-sync-starter
- 本模块仅保留 Sync 控制器与业务编排，依赖 spring-support-sync-starter 提供的同步能力
- 同步任务配置前缀统一使用 `plugin.sync`
- 同步任务实时推送 Topic 固定如下：
  - `monitor:sync:status`
  - `monitor:sync:log`
  - `monitor:sync:progress`
  - `monitor:sync:metrics`
- 同步任务 Job 处理器 Bean：
  - `syncTaskJobHandler`：支持按任务ID触发
  - `jsonSyncTaskHandler`：支持 JSON 配置触发
  - `DynamicSyncTaskHandler`：支持 Groovy 脚本触发

### 同步任务配置

```yaml
plugin:
  sync:
    enabled: true
    auto-create-table: false
    websocket-enabled: true
    scheduler-enabled: true
    default-batch-size: 1000
    default-consume-timeout: 30000
    default-retry-count: 3
    default-retry-interval: 1000
    log-retention-days: 30
    log-cleanup-enabled: true
```

### 同步任务接口

**任务管理**

- `GET /v1/sync/task/list`：分页查询任务列表
  - 参数：`page`、`size`、`taskName`、`taskStatus`、`orderBy`、`desc`
- `POST /v1/sync/task/create`：创建任务
- `PUT /v1/sync/task/update`：更新任务
- `DELETE /v1/sync/task/delete/{taskId}`：删除任务
- `POST /v1/sync/task/start/{taskId}`：启动任务
- `POST /v1/sync/task/stop/{taskId}`：停止任务
- `POST /v1/sync/task/execute/{taskId}`：手动执行一次
- `POST /v1/sync/task/copy/{taskId}?newName=xxx`：复制任务
- `GET /v1/sync/task/design/{taskId}`：获取任务设计
- `POST /v1/sync/task/design/{taskId}`：保存任务设计
- `POST /v1/sync/task/validate`：验证任务设计
- `GET /v1/sync/task/logs/{taskId}?page=1&size=10`：获取任务执行日志
- `GET /v1/sync/task/log/{logId}`：获取日志详情
- `GET /v1/sync/task/nodes/{taskId}`：获取任务节点列表
- `GET /v1/sync/task/connections/{taskId}`：获取任务连线列表
- `GET /v1/sync/task/statistics`：获取全局执行统计
  - 参数：`startTime`、`endTime`、`granularity`（hour/day）
- `GET /v1/sync/task/statistics/{taskId}`：获取指定任务统计
  - 参数：`startTime`、`endTime`、`granularity`（hour/day）

**表管理**

- `GET /v1/sync/task/table/status`：检查同步表状态
- `POST /v1/sync/task/table/initialize?force=false`：初始化同步表

**输出表管理**

- `POST /v1/sync/task/output/table/exists?tableName=xxx`：检查输出表是否存在（Body：nodeConfig）
- `POST /v1/sync/task/output/table/create?tableName=xxx`：创建输出表（Body：nodeConfig + columns）
- `POST /v1/sync/task/output/table/structure?tableName=xxx`：获取输出表结构（Body：nodeConfig）
- `POST /v1/sync/task/output/table/preview-sql?tableName=xxx&dbType=mysql`：预览建表SQL（Body：columns）
- `POST /v1/sync/task/output/table/sync-structure?tableName=xxx`：同步输出表结构（Body：nodeConfig + columns）

**SPI 管理**

- `GET /v1/sync/spi/all`：获取所有SPI类型列表
- `GET /v1/sync/spi/input`：获取Input SPI列表
- `GET /v1/sync/spi/output`：获取Output SPI列表
- `GET /v1/sync/spi/datacenter`：获取DataCenter SPI列表
- `GET /v1/sync/spi/filter`：获取Filter SPI列表
- `GET /v1/sync/spi/parameters?spiType=INPUT&spiName=xxx`：获取SPI参数定义
- `POST /v1/sync/spi/validate?spiType=INPUT&spiName=xxx`：验证SPI配置（Body：config）
- `POST /v1/sync/spi/test?spiType=INPUT&spiName=xxx`：测试SPI连接（Body：config）

### 同步任务数据结构

**MonitorSyncTask（任务基础信息）**

- `syncTaskId`：任务ID（更新时必填）
- `syncTaskName`：任务名称（必填）
- `syncTaskDesc`：任务描述
- `syncTaskStatus`：任务状态（STOPPED/RUNNING/ERROR）
- `syncTaskBatchSize`：批次大小
- `syncTaskConsumeTimeout`：消费超时时间(毫秒)
- `syncTaskRetryCount`：重试次数
- `syncTaskRetryInterval`：重试间隔(毫秒)
- `syncTaskSyncInterval`：同步间隔(毫秒)
- `syncTaskAckEnabled`：ACK开关（0/1）
- `syncTaskTransactionEnabled`：事务开关（0/1）
- `syncTaskCron`：Cron 表达式
- `syncTaskLayout`：前端连线布局JSON

**SyncTaskQuery（任务列表查询参数）**

- `page`：页码
- `size`：每页大小
- `taskName`：任务名称（模糊匹配）
- `taskStatus`：任务状态（STOPPED/RUNNING/ERROR）
- `orderBy`：排序字段（默认 syncTaskCreateTime）
- `desc`：是否降序（默认 true）

**SyncTaskDesign（任务设计）**

- `task`：任务基础信息（MonitorSyncTask）
- `nodes`：节点列表（MonitorSyncNode）
- `connections`：连线列表（MonitorSyncConnection）
- `layout`：前端布局数据

**ColumnDefinition（输出建表列定义）**

- `name`：列名
- `type`：列类型（VARCHAR/INT/BIGINT/TEXT/DATETIME/DATE/DECIMAL/BOOLEAN/FLOAT/DOUBLE/BLOB/JSON）
- `length`：列长度
- `scale`：小数位数
- `nullable`：是否可为空
- `defaultValue`：默认值
- `primaryKey`：是否主键
- `autoIncrement`：是否自增
- `comment`：列注释
- `order`：列顺序
- `sourceField`：源字段名

**SpiInfo（SPI信息）**

- `name`：SPI名称
- `displayName`：显示名称
- `description`：描述
- `type`：类型（INPUT/OUTPUT/DATA_CENTER/FILTER）
- `className`：实现类
- `icon`：图标
- `color`：颜色
- `parameters`：参数列表（SpiParameter）
- `order`：排序
- `available`：是否可用

**SpiParameter（SPI参数定义）**

- `name`：参数名称
- `label`：显示名称
- `description`：描述
- `type`：参数类型（string/number/boolean/select/password/textarea/json）
- `defaultValue`：默认值
- `required`：是否必填
- `sensitive`：是否敏感
- `placeholder`：占位符
- `options`：可选值列表

## 新增：Docker 软件库在线搜索与多默认仓库（2025-10-20）

- 在线搜索优化：关键词为空直接返回空列表，避免不必要请求；前端亦本地拦截，减少网络开销。
- 在线搜索返回扩展字段（非持久化，仅用于展示）：`systemSoftStarCount`、`systemSoftPullCount`。

- 支持为多个仓库设置“默认”，新增接口取消默认：POST /v1/system/soft/registry/{id}/default/cancel
- 新增在线搜索接口（SPI聚合默认仓库）：
  - GET /v1/system/soft/online/search?keyword=xxx&page=1&size=10
  - POST /v1/system/soft/online/import
- SPI 说明：实现 `SoftOnlineSearchProvider` 并标注 `@Component` 即可被聚合检索使用
- 前端配套：apps/vue-support-monitor-starter 已接入“在线搜索”弹窗

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

### v1 兼容接口（Docker/服务器）

- `GET /v1/system/soft/page`：系统软件分页查询（与前端 docker-management.ts 保持一致）

> 服务器分页接口请使用已存在的 `GET /v1/gen/server/page`
> 注意：本模块默认 `server.servlet.context-path=/monitor/api`，因此最终访问路径示例：`/monitor/api/v1/system/soft/page`

### 软件仓库管理 - 连接测试

- `POST /v1/system/soft/registry/{id}/test`
    - 说明：根据仓库ID，读取仓库URL并进行连通性检测；优先使用 HTTP HEAD，失败回退 GET；以 200-399 为成功。
    - 入参：路径参数 `id`（整数，必填）
    - 出参：`ReturnResult<Boolean>`，`true` 表示可达；同时会落库更新连接状态、错误信息与最后连接时间。

### 软件仓库管理 - 同步软件到软件库

- 新增实体字段：`system_soft_registry_support_sync`（是否支持同步，0/1）
    - 前端需依据该字段控制“同步”按钮展示：为 0 时隐藏按钮。
- `POST /v1/system/soft/registry/{id}/sync`
    - 说明：当仓库启用且支持同步时，从指定仓库同步软件到软件库。
    - 入参：路径参数 `id`（整数，必填）
    - 出参：`ReturnResult<Boolean>`，并在消息中返回同步数量描述。
- `POST /api/monitor/system-soft/sync?registryId={id}`
    - 说明：由 SystemSoftController 提供的同步入口；当 `registryId` 为空时使用“默认仓库”。
    - 出参：`ReturnResult<Integer>`，返回新增/更新的软件条数。

实现说明：
- 已实现 `SystemSoftServiceImpl#syncFromRegistry`，使用 DockerProtocolClient 在绑定服务器上列举镜像并同步到软件库：
  - 基于 Docker 客户端在指定服务器上列举本地镜像（去除标签，按仓库维度去重）；
  - 反射兼容 listImage/listImages 等多种签名，适应不同版本DockerProtocolClient；
  - 仅同步仓库名，不拉取标签；新增或更新 registryId/名称/代码/更新时间。

### 软件仓库管理 - 设置默认仓库

- `POST /v1/system/soft/registry/{id}/default`
    - 说明：将指定仓库设为默认仓库；现在改为“每台服务器一个默认仓库”：仅清除该仓库所属服务器下的默认标记，再设置当前仓库为默认。
    - 约束：仅“启用”的仓库可设为默认（`system_soft_registry_status=1`）。
- `GET /v1/system/soft/registry/default?serverId={serverId}`
    - 说明：按服务器获取默认仓库；不传 `serverId` 则返回任意（兼容旧逻辑）。
    - 入参：路径参数 `id`（整数，必填）
    - 出参：`ReturnResult<Boolean>`，成功返回 `true`。
- 列表排序：`GET /v1/system/soft/registry` 与 `GET /v1/system/soft/registry/page` 均已按“默认优先，其次按 sort
  升序”返回数据，默认仓库始终排在第一条。

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

- Mapper 规范：所有 SQL 均放置于 resources/mapper 下 XML 文件中，禁止出现在 Java 注解中。
- 本模块已补齐 com.chua.starter.monitor.mapper 下缺失的 XML，并迁移/补充必要的 SQL。
- 安全：建议在拦截器中增加鉴权/白名单控制，避免代理被滥用
- 代理前缀：默认后端基座为 `/monitor/api`，如有变更请同步前端配置
- 依赖：需要存在 `RestTemplate` Bean；项目中已广泛使用，通常已配置

## 排障建议
- 控制台空白：检查节点是否正确上报 `report.client.arthas.port`
- 资源404：检查 `GET /v1/arthas/console/{nodeId}/` 是否正常返回 HTML/静态资源
- WS 失败：检查 `WS /v1/arthas/console/{nodeId}/ws` 是否可连通，以及上游 tunnel 状态
- 启动报错 `ServletWebServerFactory` 缺失：确保引入 `spring-boot-starter-web` 以提供嵌入式 Servlet 容器

---

### Actuator 日志级别管理（2025-12-08）

新增通过 SyncServer 下发日志级别设置命令到指定节点的功能。

**接口：**

- `POST /v1/actuator/log-level`
  - 说明：设置指定节点的日志级别，通过 SyncServer 下发到客户端
  - 入参：
    ```json
    {
      "clientId": "可选，优先使用",
      "ipAddress": "节点IP地址，与clientId二选一",
      "port": 8080,
      "loggerName": "日志器名称，如 com.chua.xxx 或 ROOT",
      "logLevel": "日志级别: TRACE, DEBUG, INFO, WARN, ERROR, OFF"
    }
    ```
  - 出参：`ReturnResult<Boolean>`
  - Topic：`monitor/control/log-level`

**命令格式：**
```json
{
  "action": "SET_LOG_LEVEL",
  "loggerName": "com.chua.xxx",
  "level": "DEBUG",
  "timestamp": 1733664000000
}
```

**客户端处理：**
- 客户端需订阅 `monitor/control/log-level` 主题
- 收到命令后通过 `LoggingSystem` 动态修改日志级别

---

### ACME 证书管理
- 接口：
  - POST /v1/cert/account/save：保存/更新 ACME 账户
  - POST /v1/cert/apply：申请证书（首次会产出验证信息，需按返回的 HTTP-01/DNS-01 要求完成验证后重试）
  - POST /v1/cert/{certId}/renew：续签证书
  - POST /v1/cert/{certId}/revoke：吊销证书
  - GET /v1/cert/{certId}：获取证书详情
- 配置：monitor.acme.enabled=true，monitor.acme.renewDaysBefore=30
- 自动续期：每日 03:00 检查即将到期证书并触发续签

- 流程说明：
  1) 申请时若验证资源未就绪，将返回 certId 并把“验证指引”写入表 monitor_acme_order.acme_authz_urls（JSON）；证书状态置为 validating。
  2) 请按 JSON 中的 httpPath/httpContent 或 dnsName/dnsValue 完成验证；稍后再次调用申请接口或等待调度触发续签即可完成签发。
  3) 成功签发后，证书链与私钥（PEM）将落库到 monitor_acme_certificate（acme_cert_chain_pem/acme_cert_key_pem），并计算 acme_cert_renew_at。

---
