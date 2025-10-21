# 更新日志

## 2025-10-20
- feat(registry): 支持多默认仓库，新增取消默认接口 POST /v1/system/soft/registry/{id}/default/cancel
- feat(online): 新增在线搜索/导入接口，基于 SPI 聚合默认仓库
- feat(online): 在线检索结果增加星标计数与拉取计数两个字段（非持久化）：systemSoftStarCount、systemSoftPullCount
- fix(online): 关键词为空时后端直接返回空列表；前端本地拦截，避免无效请求
- docs: 更新 README 说明

# Changelog - spring-api-support-monitor-starter

## [Unreleased]
### Added
- MonitorSysGenServerSetting 新增 Docker 认证与超时字段（均为可选）：
  - monitor_sys_gen_server_setting_docker_username（API用户名）
  - monitor_sys_gen_server_setting_docker_password（API密码）
  - monitor_sys_gen_server_setting_docker_connect_timeout_millis（连接超时，毫秒，默认30000）
  - DockerProtocolClientFactory 已读取上述字段进行客户端构建；默认超时30秒。
- ACME 证书自动申请/管理/续期能力：
  - 新增实体：MonitorAcmeAccount、MonitorAcmeCertificate、MonitorAcmeOrder
  - 新增 Mapper 及 XML：MonitorAcmeAccountMapper、MonitorAcmeCertificateMapper、MonitorAcmeOrderMapper
  - 新增服务：AcmeCertificateService、AcmeCertificateServiceImpl
  - 新增控制器：AcmeCertificateController（接口以 ForCert 结尾）
  - 新增配置：AcmeProperties 与 AcmeConfiguration，支持每日自动检查即将到期证书并续签
  - POM 引入 utils-support-acme-starter 依赖（父 POM 管理与本模块依赖）
  - 接入 ACME 实际流程：申请时生成并保存 HTTP-01/DNS-01 挑战信息到 monitor_acme_order.acme_authz_urls，验证就绪后自动签发并回填证书链与私钥；支持续签与撤销

- SystemSoftRegistry 新增字段 `system_soft_registry_support_sync`（是否支持同步，0/1）；前端据此隐藏不同步的仓库的“同步”按钮。
- SystemSoftRegistry 新增字段 `system_soft_registry_server_id`（绑定服务器ID，必填）；添加/编辑仓库必须选定服务器，用于指定该服务器使用该仓库。
- 新增后端接口 `POST /v1/system/soft/registry/{id}/default` 用于设置默认仓库；默认仓库策略调整为“每台服务器一个默认仓库”：清除该服务器下的默认标记后再设置当前仓库为默认。新增 `GET /v1/system/soft/registry/default?serverId={serverId}` 以按服务器获取默认仓库。
- 软件仓库列表默认优先排序：`GET /v1/system/soft/registry` 与 `GET /v1/system/soft/registry/page` 返回数据时，默认仓库（
  `system_soft_registry_is_default=1`）置顶，其次按 `system_soft_registry_sort` 升序。
- 实现 `SystemSoftRegistryServiceImpl#syncRegistry`：校验仓库启用及支持同步后，调用 `SystemSoftService#syncFromRegistry`
  将软件同步至软件库。
- 实现 `SystemSoftServiceImpl#syncFromRegistry`：
  - 使用 DockerProtocolClient 在绑定服务器上列举本地镜像，适配多种签名（listImage/listImages）；
  - 提取仓库名（去标签）并同步为 SystemSoft，存在则更新、否则新增；
  - 只同步仓库名，不处理具体标签。
- 修复 com.chua.starter.monitor.mapper 缺失的 MyBatis XML 与 SQL 声明：新增并完善以下映射文件与语句：
    - MonitorArthasSessionMapper.xml（将原注解SQL迁移至XML，新增查询/更新/清理等语句）
    - MonitorSysGenMessagePushTemplateMapper.xml、MonitorSysGenSerialMapper.xml、MonitorSysGenScriptUploadRecordMapper.xml
    - SystemDataFieldCommentMapper.xml、SystemDataSettingItemMapper.xml
    - SystemServerSettingAddressRateLimitMapper.xml、SystemServerSettingIPRateLimitMapper.xml
    - SystemServerSettingServiceDiscoveryMapper.xml、SystemServerSettingServiceDiscoveryMappingMapper.xml
    -
    SystemSoftMapper.xml、SystemSoftVersionMapper.xml、SystemSoftRegistryMapper.xml、SystemSoftRecordMapper.xml、SystemSoftContainerStatsMapper.xml
- 移除 MonitorArthasSessionMapper.java 中的注解式 SQL，符合“SQL 必须在 XML 中”的项目规范。

- 新增 v1 兼容接口（与前端 docker-management.ts 对齐）：
    - `GET /v1/system/soft/page`：系统软件分页查询

- Docker软件管理功能增强：
    - 新增 `DockerResultCallback` 接口，支持Docker操作的回调处理
    - 新增 `DefaultDockerBuildCallback`、`DefaultDockerPushCallback` 默认回调实现
    - 新增 `WebSocketDockerPullCallback` WebSocket回调实现，支持实时进度推送
    - 扩展 `DockerProtocolClient`，新增带回调的 `buildImage` 和 `pushImage` 方法
    - 更新 `SystemSoftServiceImpl`，集成WebSocket实时进度推送功能
    - 支持Docker镜像拉取、构建、推送操作的实时进度监控
    - 提供完整的Docker操作流程文档和API接口说明
    - 支持多服务器软件部署和状态监控
- ExtendedDockerProtocolClient扩展接口功能：
    - 新增 `ExtendedDockerProtocolClient` 接口，扩展原始 DockerProtocolClient 功能
    - 新增 `ExtendedDockerProtocolClientImpl` 实现类，提供增强的镜像拉取方法
    - 新增 `DockerConfiguration` 配置类，自动注册扩展客户端Bean
    - 提供 `pullImage(String imageName, String imageTag)` 方法，支持分离式镜像名称和标签参数
    - 提供 `pullImage(String imageName, String imageTag, boolean forceUpdate)` 方法，支持强制更新镜像
    - 提供 `pullImage(String fullImageName)` 方法，支持完整镜像名称拉取
    - 更新 `SystemSoftServiceImpl` 和 `SystemSoftContainerServiceImpl` 使用扩展客户端
    - 提供完整的日志记录和异常处理机制
    - 向后兼容原始 DockerProtocolClient 的所有功能
- SystemSoftImage字段不一致问题修复：
    - 修复 `SystemSoftImage` 实体类缺少 `system_soft_id` 字段的问题
    - 新增 `SystemSoftImageMapper.xml` 映射文件，包含完整的字段映射和SQL实现
    - 提供30+个数据库操作方法，支持镜像信息的全生命周期管理
    - 验证实体类字段与数据库表字段的一致性，确保数据映射正确
    - 支持按软件ID、服务器ID、镜像状态、镜像名称等多种条件查询
    - 支持镜像统计、搜索、大小范围查询、时间范围查询等高级功能
- 修复SystemSoftImageController中的返回值类型问题：
    - 将所有接口的返回值从 `HttpResponse` 统一修改为 `ReturnResult`
    - 确保与项目其他控制器的返回值规范保持一致
    - 提高前端接口调用的兼容性和稳定性
- 修复容器统计信息接口数据格式问题：
    - 修复 `getContainerStats` 接口返回数据格式不符合文档规范的问题
    - 将Docker原始统计数据转换为标准格式，包含 `cpuUsage`、`memoryUsage`、`networkIO`、`diskIO` 等字段
    - 新增数据转换方法 `convertToStandardFormat`，确保返回数据结构的一致性
    - 添加完善的异常处理和日志记录，提高接口的健壮性
    - 支持CPU使用率百分比计算、内存使用情况统计、网络IO统计、磁盘IO统计等功能
- DockerContainerConfig容器配置支持功能：
    - 新增 `DockerContainerConfig` 实体类，支持完整的Docker容器配置管理
    - 新增 `DockerContainerConfigMapper` 接口和XML映射文件，提供数据库操作支持
    - 新增 `DockerContainerConfigService` 服务接口和实现类，提供业务逻辑处理
    - 新增 `DockerContainerConfigController` REST API控制器，提供HTTP接口
    - 支持容器配置的增删改查、批量操作、条件查询等完整功能
    - 新增 `DockerProtocolClient` 中按容器名称操作的方法：
        - `startContainerByName`：按容器名称启动容器
        - `stopContainerByName`：按容器名称停止容器
        - `restartContainerByName`：按容器名称重启容器
        - `removeContainerByName`：按容器名称删除容器
    - 更新 `SystemSoftContainerServiceImpl` 服务类，优先使用容器名称进行操作
    - 提供容器配置模板功能，支持快速创建标准化容器配置
    - 支持容器配置验证，确保配置参数的正确性和完整性
- SystemSoftContainer条件查询功能：
    - 新增 `listByCondition` 方法，支持多条件灵活查询容器列表
    - 支持按服务器ID、软件ID、容器状态、容器名称、镜像ID、主机端口等条件查询
    - 修复 `SystemSoftContainerWebSocketService` 中缺失的方法调用问题
    - 提供完整的参数说明和使用示例
    - 适用于WebSocket推送、统计分析、批量操作等多种场景
- SystemSoftRecord状态管理优化：
    - 新增 `SystemSoftRecordStatus` 常量类，规范操作记录状态值定义
    - 修复 `SystemSoftContainerServiceImpl` 中状态字段类型错误（字符串 → Integer）
    - 修复 `SystemSoftServiceImpl` 中状态字段类型错误（字符串 → Integer）
    - 统一所有操作记录的状态设置方式，提高类型安全性
    - 支持状态验证和描述获取功能
- SystemSoft软件配置验证功能：
    - 新增 `validateSoftConfig` 方法，提供完整的软件配置验证机制
    - 支持基本字段验证、唯一性验证、Docker镜像格式验证
    - 支持安装方式验证和状态值验证
    - 确保软件信息的正确性和一致性
- SystemSoftRegistry字段命名修复：
    - 修复实体类字段命名规范问题
    - 统一字段命名风格，提高代码可读性
    - 确保数据库字段映射的一致性
- Arthas 控制台后端代理能力：
  - POST `/v1/arthas/connect`：返回后端代理的控制台URL
  - POST `/v1/arthas/disconnect`：断开（占位）
  - GET `/v1/arthas/nodes`：仅返回包含 `report.client.arthas.port` 的在线节点
  - GET `/v1/arthas/console/{nodeId}/**`：控制台静态资源/HTML HTTP 代理（带内嵌`/ws`路径改写）
  - WS `/v1/arthas/console/{nodeId}/ws`：WebSocket 反向代理
- 工具类：`ArthasProxyUtils` 统一元数据键与地址处理
- Docker软件版本同步功能：
    - 集成 `utils-support-docker-starter` 模块
    - POST `/v1/system-soft/sync-from-docker`：从Docker仓库同步软件版本信息
    - 优先使用本地Docker客户端，失败时回退到Docker Hub API
    - 支持批量同步多个软件的版本信息
    - 新增 `DockerConfiguration` 配置类，支持Docker连接参数配置
    - 完善`SystemSoftServiceImpl`实现，集成DockerProtocolClient
- SystemSoftContainer容器管理功能：
    - 新增 `SystemSoftContainer` 实体类，用于存储Docker容器信息
    - 新增 `SystemSoftContainerMapper` 接口和XML映射文件
    - 新增 `SystemSoftContainerService` 服务接口和实现类
    - 新增 `SystemSoftContainerController` REST API控制器
    - 支持容器查询、状态更新、统计分析等完整功能
    - 提供容器状态同步、批量管理等高级功能
    - 集成Docker API实现容器生命周期管理：
        - POST `/v1/system/soft/container/create`：创建容器
        - POST `/v1/system/soft/container/{id}/start`：启动容器
        - POST `/v1/system/soft/container/{id}/stop`：停止容器
        - POST `/v1/system/soft/container/{id}/restart`：重启容器
        - DELETE `/v1/system/soft/container/{id}`：删除容器
        - GET `/v1/system/soft/container/{id}/logs`：获取容器日志
        - GET `/v1/system/soft/container/{id}/stats`：获取容器统计信息
        - POST `/v1/system/soft/container/sync`：同步容器状态
        - POST `/v1/system/soft/container/batch`：批量操作容器
- SystemSoftContainerWebSocketService实时推送功能：
    - 新增 `SystemSoftContainerWebSocketService` WebSocket服务类
    - 支持容器状态变化实时推送
    - 支持容器日志实时推送
    - 支持容器统计信息实时推送
    - 支持容器事件通知推送
    - WebSocket主题支持：
        - `/topic/container/status/{serverId}`：容器状态变化推送
        - `/topic/container/logs/{containerId}`：容器日志实时推送
        - `/topic/container/statistics/{serverId}`：容器统计信息推送
        - `/topic/container/events/{serverId}`：容器事件通知推送
    - WebSocket管理接口：
        - GET `/v1/system/soft/container/websocket/topics`：获取主题信息
        - GET `/v1/system/soft/container/websocket/status`：获取连接状态
        - POST `/v1/system/soft/container/websocket/push-statistics`：手动推送统计信息
        - POST `/v1/system/soft/container/websocket/push-status`：手动推送状态信息
- 返回值规范化修复：
    - 修复 `SystemSoftContainerController` 控制器返回值类型
    - 将所有 `HttpResponse` 返回值统一改为 `ReturnResult`
    - 影响容器管理相关的16个接口
    - 保持响应格式一致，确保前端兼容性
- 容器配置更新功能：
    - 新增 `PUT /v1/system/soft/container/{id}` 接口
    - 支持更新容器的名称、镜像标签、端口映射、环境变量、数据卷、启动命令等配置
    - 新增 `UpdateContainerRequest` 请求对象，包含OpenAPI注解
    - 支持部分字段更新，未传入的字段保持原值
    - 容器名称重复性检查（排除当前容器）
    - 完整的操作日志记录和WebSocket实时事件推送
    - 事务保证数据一致性
- SystemSoftImage镜像管理功能：
    - 新增 `SystemSoftImage` 实体类，包含镜像ID、名称、标签、仓库地址、大小、架构等完整信息
    - 新增 `SystemSoftImageMapper` 接口，提供30+个数据库操作方法
    - 新增 `SystemSoftImageMapper.xml` 映射文件，包含完整的SQL实现
    - 新增 `SystemSoftImageService` 服务接口，定义镜像管理的核心业务方法
    - 新增 `SystemSoftImageServiceImpl` 服务实现类，包含完整的业务逻辑和异常处理
    - 新增 `SystemSoftImageController` REST API控制器，提供25个HTTP接口
    - 支持镜像多维度查询（按服务器、名称、标签、仓库、架构等）
    - 支持镜像统计分析（数量统计、大小统计、使用情况统计）
    - 支持镜像版本管理（版本列表、版本比较、标签管理）
    - 支持镜像批量操作（批量更新状态、批量删除、批量同步）
    - 支持镜像搜索功能（关键词搜索、模糊匹配）
    - 支持镜像同步功能（与Docker服务器同步镜像信息）
    - 支持镜像清理优化（查询未使用镜像、清理过期镜像）
    - 支持热门镜像排行（按拉取次数、使用频率排序）
    - 集成Docker实际操作功能：
        - 镜像拉取：支持从Docker仓库拉取镜像到指定服务器
        - 镜像删除：支持删除指定镜像，可选择强制删除
        - 镜像推送：支持将本地镜像推送到Docker仓库
        - 镜像标记：支持为镜像添加新的标签
        - 镜像清理：支持清理未使用的镜像释放存储空间
        - 镜像导出：支持将镜像导出为tar文件
        - 镜像导入：支持从tar文件导入镜像
        - 实时日志推送：使用WebSocket实时推送Docker操作日志
        - 批量操作：支持批量删除多个镜像
        - Docker Hub搜索：支持搜索Docker Hub上的公共镜像

### Changed

- 添加仓库配置校验：`SystemSoftRegistryServiceImpl#validateRegistryConfig` 新增对 `system_soft_registry_server_id` 的必填校验。
- SystemSoftRegistry 仓库连接测试逻辑：`SystemSoftRegistryServiceImpl#testConnection` 改为使用 URL 连通性检测（优先
  HEAD，失败回退 GET），判定 HTTP 200-399 为成功；按照规则自动更新连接状态与错误信息。

### Changed
- WebSocket 配置：在 `GuacamoleWebSocketConfig` 注册了 Arthas WS 端点 `/v1/arthas/console/**/ws`
- 前端页面采用后端代理地址加载控制台（iframe）
- `SystemSoftServiceImpl` 重构：
    - 使用依赖注入方式获取DockerProtocolClient
    - 优化镜像标签获取逻辑，支持本地Docker和远程API双重策略
    - 增强异常处理和日志记录
- 新增Docker配置支持：在 `application.yml` 中添加 `monitor.docker` 配置项

### Dependencies

- 新增依赖：`utils-support-docker-starter`
    - 提供DockerProtocolClient客户端支持
    - 支持完整的Docker API操作能力
    - 包含Docker镜像信息实体类

### Notes
- 元数据键名：`report.client.arthas.port`（对应 REPORT_ARTHAS_CLIENT_PORT）
- 示例值：`ws://arthas-tunnel:7777/ws`
- 安全建议：在 WS/HTTP 代理前增加鉴权/白名单控制
- Docker配置：支持TLS验证、连接超时等参数配置
- Docker同步策略：本地Docker优先，Docker Hub API备用
- SystemSoftContainer实体：新增容器运行时信息管理功能
    - 与SystemSoftRecord区分：Record作为安装记录，Container管理运行时状态
    - 支持容器状态实时监控和管理
    - 提供完整的容器生命周期管理API
    - 集成WebSocket实时推送容器状态变化

