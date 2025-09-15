# Docker容器管理功能详细说明

## 概述

本文档详细说明了spring-api-support-monitor-starter模块中的Docker容器管理功能，包括所有接口的入参、出参、使用示例和注意事项。

## DockerContainerConfig 容器配置支持功能

### 功能说明

本模块新增了对 `DockerContainerConfig` 的完整支持，提供基于配置对象的容器管理功能：

- 支持通过 `DockerContainerConfig` 对象创建和管理容器
- 提供按容器名称进行容器操作的便捷方法
- 增强了容器管理的易用性和可维护性
- 完整的容器生命周期管理支持

### 新增方法

#### DockerProtocolClient 新增方法

- **createContainer(DockerContainerConfig config)**: 基于配置对象创建容器
    - 支持镜像名称、端口映射、环境变量、卷映射等完整配置
    - 自动处理配置参数转换和验证
    - 提供详细的错误处理和日志记录

- **createAndStartContainer(DockerContainerConfig config)**: 创建并启动容器
    - 一步完成容器创建和启动操作
    - 简化容器部署流程
    - 包含完整的异常处理机制

- **updateContainer(DockerContainerConfig config)**: 更新容器配置
    - 停止并删除旧容器
    - 基于新配置创建容器
    - 实现容器配置的热更新

#### 按名称操作的容器管理方法

- **startContainerByName(String containerName)**: 按名称启动容器
- **stopContainerByName(String containerName)**: 按名称停止容器
- **restartContainerByName(String containerName)**: 按名称重启容器
- **removeContainerByName(String containerName, boolean force)**: 按名称删除容器
- **inspectContainerByName(String containerName)**: 按名称获取容器详细信息
- **getContainerLogsByName(String containerName, int lines)**: 按名称获取容器日志
- **containerExists(String containerName)**: 检查容器是否存在
- **isContainerRunning(String containerName)**: 检查容器是否运行中

### DockerContainerConfig 配置说明

#### 支持的配置属性

- **imageName**: Docker镜像名称（必填）
- **containerName**: 容器名称
- **portBindings**: 端口映射配置
- **environmentVariables**: 环境变量列表
- **volumeMappings**: 卷映射配置
- **command**: 容器启动命令
- **args**: 命令参数列表
- **workingDirectory**: 工作目录
- **networkMode**: 网络模式
- **restartPolicy**: 重启策略
- **memoryLimit**: 内存限制

#### 配置示例

```java
DockerContainerConfig config = DockerContainerConfig.builder()
    .imageName("nginx:latest")
    .containerName("my-nginx")
    .portBindings(Map.of("80", "8080"))
    .environmentVariables(List.of("ENV=production"))
    .volumeMappings(Map.of("/data", "/var/www/html"))
    .restartPolicy("unless-stopped")
    .build();
```

### 服务层集成

#### SystemSoftContainerServiceImpl 更新

已更新容器管理服务以支持新的按名称操作方法：

- **启动容器**: 优先使用容器名称，回退到容器ID
- **停止容器**: 优先使用容器名称，回退到容器ID
- **重启容器**: 优先使用容器名称，回退到容器ID
- **删除容器**: 优先使用容器名称，回退到容器ID

#### 错误处理增强

- 当容器名称和Docker ID都为空时，提供明确的错误提示
- 完整的异常捕获和日志记录
- 操作记录状态的准确更新

### 使用优势

1. **配置驱动**: 通过配置对象管理容器，提高代码可读性
2. **类型安全**: 强类型配置，减少配置错误
3. **易于维护**: 按名称操作容器，便于管理和调试
4. **完整功能**: 支持容器的完整生命周期管理
5. **向后兼容**: 保持原有API的兼容性

### 技术特性

1. **Builder模式**: 使用Builder模式构建配置对象
2. **参数验证**: 自动验证配置参数的有效性
3. **异常处理**: 完整的异常处理和错误恢复机制
4. **日志记录**: 详细的操作日志和调试信息
5. **状态管理**: 准确的容器状态跟踪和更新

## SystemSoft 软件管理功能

### 功能说明

SystemSoft实体用于管理系统软件信息，提供软件的基本信息管理、配置验证、Docker镜像关联、安装方式配置等功能。支持多种软件类型和安装方式，为软件的全生命周期管理提供基础数据支持。

### 核心实体

**SystemSoft** - 系统软件信息实体

- `systemSoftId`: 软件主键ID
- `systemSoftName`: 软件名称
- `systemSoftCode`: 软件代码（唯一标识）
- `systemSoftCategory`: 软件分类
- `systemSoftIcon`: 软件图标
- `systemSoftTags`: 软件标签
- `systemSoftDesc`: 软件描述
- `systemSoftRegistryId`: 镜像仓库ID（关联SystemSoftRegistry）
- `systemSoftDockerImage`: Docker镜像名称
- `systemSoftDefaultInstallMethod`: 默认安装方式
- `systemSoftDefaultInstallParams`: 默认安装参数
- `systemSoftStatus`: 软件状态（0:禁用, 1:启用）
- `systemSoftIsOfficial`: 是否官方软件（0:否, 1:是）
- `systemSoftRemark`: 软件备注

### 新增接口

- **SystemSoftMapper**: 软件数据访问层，提供完整的数据库操作方法
- **SystemSoftService**: 软件业务逻辑层，提供软件管理的核心业务方法
- **SystemSoftController**: 软件REST API控制器，提供HTTP接口

### 关键实现类

- **实体类**：`com.chua.starter.monitor.entity.SystemSoft`
- **Mapper接口**：`com.chua.starter.monitor.mapper.SystemSoftMapper`
- **服务接口**：`com.chua.starter.monitor.service.SystemSoftService`
- **服务实现**：`com.chua.starter.monitor.service.impl.SystemSoftServiceImpl`
- **控制器**：`com.chua.starter.monitor.controller.SystemSoftController`

### 核心功能

#### 1. 软件配置验证（validateSoftConfig）

**新增功能**：提供完整的软件配置验证机制，确保软件信息的正确性和一致性。

**验证规则**：

- **基本字段验证**：软件代码、软件名称不能为空
- **唯一性验证**：软件代码在系统中必须唯一
- **Docker镜像格式验证**：验证Docker镜像名称格式的正确性
- **安装方式验证**：验证安装方式是否为支持的类型
- **状态值验证**：验证软件状态值的有效性

**支持的安装方式**：

- `docker`: Docker容器安装
- `binary`: 二进制文件安装
- `package`: 包管理器安装
- `source`: 源码编译安装
- `script`: 脚本安装

**Docker镜像名称格式**：

- 支持格式：`image`、`image:tag`、`registry/image`、`registry/image:tag`、`registry:port/image:tag`
- 长度限制：不超过255个字符
- 字符限制：支持字母、数字、点号、下划线、斜杠、连字符

#### 2. 软件信息管理

- **分页查询**：支持按软件代码、名称、分类、状态进行分页查询
- **代码查询**：根据软件代码快速查询软件信息
- **分类查询**：根据软件分类查询软件列表
- **镜像管理**：关联Docker镜像进行同步、拉取、删除操作

### 使用场景

1. **软件信息管理**：管理系统中所有可安装的软件信息
2. **配置验证**：在软件新增和更新时进行配置有效性验证
3. **Docker集成**：与Docker镜像仓库集成，支持容器化软件管理
4. **安装配置**：提供多种安装方式的配置和参数管理
5. **分类管理**：按分类组织和管理软件，便于查找和使用
6. **状态控制**：支持软件的启用/禁用状态管理

### 软件状态说明

- **0**: 禁用 - 软件不可用于安装
- **1**: 启用 - 软件可正常使用

### 官方软件标识

- **0**: 非官方软件 - 第三方或自定义软件
- **1**: 官方软件 - 经过验证的官方软件

### 技术特性

1. **配置验证**：提供完整的软件配置验证机制
2. **唯一性保证**：确保软件代码的全局唯一性
3. **格式校验**：Docker镜像名称格式的严格校验
4. **扩展支持**：提供扩展字段支持自定义配置
5. **关联管理**：与镜像仓库、镜像、容器等实体的关联管理
6. **日志记录**：完整的操作日志和错误日志记录

## SystemSoftRegistry 镜像仓库管理功能

### 功能说明

SystemSoftRegistry实体用于管理Docker镜像仓库信息，提供镜像仓库的配置、连接管理、认证等功能。支持多种类型的镜像仓库，包括Docker
Hub、Harbor、阿里云、腾讯云等私有仓库。

### 核心实体

**SystemSoftRegistry** - 系统软件镜像仓库实体

- `systemSoftRegistryId`: 镜像仓库主键ID
- `systemSoftRegistryName`: 仓库名称
- `systemSoftRegistryType`: 仓库类型（docker_hub, harbor, aliyun, tencent, private）
- `systemSoftRegistryUrl`: 仓库地址
- `systemSoftRegistryUsername`: 仓库用户名
- `systemSoftRegistryPassword`: 仓库密码
- `systemSoftRegistryEmail`: 仓库邮箱
- `systemSoftRegistryIsDefault`: 是否为默认仓库（0:否, 1:是）
- `systemSoftRegistrySslEnabled`: 是否启用SSL（0:否, 1:是）
- `systemSoftRegistryTimeout`: 连接超时时间（秒）
- `systemSoftRegistryDescription`: 仓库描述
- `systemSoftRegistryConfig`: 仓库配置信息
- `systemSoftRegistryLastConnectTime`: 最后连接时间
- `systemSoftRegistryConnectStatus`: 连接状态（0:未连接, 1:连接成功, 2:连接失败）
- `systemSoftRegistryErrorMessage`: 连接错误信息
- `systemSoftRegistrySort`: 排序号
- `systemSoftRegistryStatus`: 状态（0:禁用, 1:启用）

### 字段命名规范修复

**重要更新**：为保持与其他SystemSoft相关实体的命名一致性，已将SystemSoftRegistry实体的字段命名从
`monitor_sys_soft_registry`前缀统一修改为`system_soft_registry`前缀：

| 修复前字段名                           | 修复后字段名                      | 说明     |
|----------------------------------|-----------------------------|--------|
| `monitor_sys_soft_registry_id`   | `system_soft_registry_id`   | 主键ID   |
| `monitor_sys_soft_registry_name` | `system_soft_registry_name` | 仓库名称   |
| `monitor_sys_soft_registry_type` | `system_soft_registry_type` | 仓库类型   |
| `monitor_sys_soft_registry_url`  | `system_soft_registry_url`  | 仓库地址   |
| ...                              | ...                         | 其他所有字段 |

此修复确保了SystemSoft实体中的`system_soft_registry_id`外键字段与SystemSoftRegistry实体的主键字段命名保持一致，提高了代码的可维护性和一致性。

### 新增接口

- **SystemSoftRegistryMapper**: 仓库数据访问层，提供完整的数据库操作方法
- **SystemSoftRegistryService**: 仓库业务逻辑层，提供仓库管理的核心业务方法
- **SystemSoftRegistryController**: 仓库REST API控制器，提供HTTP接口

### 关键实现类

- **实体类**：`com.chua.starter.monitor.entity.SystemSoftRegistry`
- **Mapper接口**：`com.chua.starter.monitor.mapper.SystemSoftRegistryMapper`
- **服务接口**：`com.chua.starter.monitor.service.SystemSoftRegistryService`
- **服务实现**：`com.chua.starter.monitor.service.impl.SystemSoftRegistryServiceImpl`
- **控制器**：`com.chua.starter.monitor.controller.SystemSoftRegistryController`

### 使用场景

1. **仓库配置管理**: 配置和管理多个Docker镜像仓库
2. **认证管理**: 管理仓库的用户名、密码等认证信息
3. **连接测试**: 测试仓库连接状态和可用性
4. **默认仓库**: 设置默认的镜像仓库用于软件安装
5. **SSL配置**: 支持HTTPS/SSL连接的仓库配置
6. **超时控制**: 配置仓库连接的超时时间

### 仓库类型说明

- **docker_hub**: Docker Hub官方仓库
- **harbor**: Harbor私有仓库
- **aliyun**: 阿里云容器镜像服务
- **tencent**: 腾讯云容器镜像服务
- **private**: 其他私有仓库

### 连接状态说明

- **0**: 未连接 - 仓库未进行连接测试
- **1**: 连接成功 - 仓库连接测试通过
- **2**: 连接失败 - 仓库连接测试失败

### 技术特性

1. **多仓库支持**: 支持同时配置多个镜像仓库
2. **认证安全**: 密码字段加密存储，保证安全性
3. **连接监控**: 实时监控仓库连接状态
4. **配置验证**: 提供仓库配置的有效性验证
5. **排序管理**: 支持仓库的优先级排序
6. **状态管理**: 支持仓库的启用/禁用状态控制

## 系统软件容器管理功能

### 功能说明

本模块新增了SystemSoftContainer实体和相关服务，用于管理服务器上安装的Docker容器信息：

- 与SystemSoftRecord区分：SystemSoftRecord仅作为安装记录保存，SystemSoftContainer专门存储容器运行时信息
- 支持容器状态实时监控和管理
- 提供完整的容器生命周期管理功能
- 集成WebSocket实时推送容器状态变化

### 核心实体

**SystemSoftContainer** - 系统软件容器实体

- `systemSoftContainerId`: 容器主键ID
- `systemSoftId`: 关联的软件ID
- `systemSoftVersionId`: 关联的软件版本ID
- `systemServerId`: 关联的服务器ID
- `systemSoftContainerName`: 容器名称
- `systemSoftContainerDockerId`: Docker容器ID
- `systemSoftContainerStatus`: 容器状态（CREATED/RUNNING/PAUSED/RESTARTING/REMOVING/EXITED/DEAD）
- `systemSoftContainerHealthStatus`: 健康状态（HEALTHY/UNHEALTHY/STARTING/NONE）
- `systemSoftContainerImageName`: 镜像名称
- `systemSoftContainerImageTag`: 镜像标签
- `systemSoftContainerPorts`: 端口映射配置
- `systemSoftContainerVolumes`: 卷挂载配置
- `systemSoftContainerEnvironment`: 环境变量配置
- `systemSoftContainerNetworks`: 网络配置
- `systemSoftContainerLabels`: 标签配置
- `systemSoftContainerCommand`: 启动命令
- `systemSoftContainerArgs`: 启动参数
- `systemSoftContainerWorkingDir`: 工作目录
- `systemSoftContainerUser`: 运行用户
- `systemSoftContainerRestartPolicy`: 重启策略
- `systemSoftContainerCpuLimit`: CPU限制
- `systemSoftContainerMemoryLimit`: 内存限制
- `systemSoftContainerCreatedTime`: 创建时间
- `systemSoftContainerStartedTime`: 启动时间
- `systemSoftContainerFinishedTime`: 结束时间
- `systemSoftContainerExitCode`: 退出码
- `systemSoftContainerError`: 错误信息
- `systemSoftContainerLogPath`: 日志路径
- `systemSoftContainerConfigHash`: 配置哈希值
- `systemSoftContainerRemark`: 备注信息

### 新增接口

#### 查询接口

- `GET /v1/system/soft/container/page` - 分页查询容器列表
- `GET /v1/system/soft/container/soft/{softId}` - 根据软件ID查询容器列表
- `GET /v1/system/soft/container/server/{serverId}` - 根据服务器ID查询容器列表
- `GET /v1/system/soft/container/{id}` - 根据ID查询容器详情
- `GET /v1/system/soft/container/{id}/logs` - 获取容器日志
- `GET /v1/system/soft/container/{id}/stats` - 获取容器统计信息
- `GET /v1/system/soft/container/stats` - 获取容器状态统计
- `GET /v1/system/soft/container/running` - 获取运行中的容器列表
- `GET /v1/system/soft/container/abnormal` - 获取异常容器列表

#### 管理接口

- `POST /v1/system/soft/container/create` - 创建容器
- `POST /v1/system/soft/container/{id}/start` - 启动容器
- `POST /v1/system/soft/container/{id}/stop` - 停止容器
- `POST /v1/system/soft/container/{id}/restart` - 重启容器
- `DELETE /v1/system/soft/container/{id}` - 删除容器
- `POST /v1/system/soft/container/sync` - 同步容器状态
- `POST /v1/system/soft/container/batch` - 批量操作容器

#### WebSocket接口

- `GET /v1/system/soft/container/websocket/topics` - 获取WebSocket主题信息
- `GET /v1/system/soft/container/websocket/status` - 获取WebSocket连接状态
- `POST /v1/system/soft/container/websocket/push-statistics` - 手动推送容器统计信息
- `POST /v1/system/soft/container/websocket/push-status` - 手动推送批量容器状态

### 关键实现类

- **实体类**：`com.chua.starter.monitor.entity.SystemSoftContainer`
- **Mapper接口**：`com.chua.starter.monitor.mapper.SystemSoftContainerMapper`
- **Mapper XML**：`com.chua.starter.monitor.mapper.SystemSoftContainerMapper.xml`
- **服务接口**：`com.chua.starter.monitor.service.SystemSoftContainerService`
- **服务实现**：`com.chua.starter.monitor.service.impl.SystemSoftContainerServiceImpl`
- **控制器**：`com.chua.starter.monitor.controller.SystemSoftContainerController`
- **WebSocket服务**：`com.chua.starter.monitor.service.SystemSoftContainerWebSocketService`

### WebSocket实时推送功能

#### 功能说明

本模块集成了WebSocket实时推送功能，为容器管理提供实时状态更新和事件通知：

- 容器状态变化实时推送
- 容器日志实时推送
- 容器统计信息实时推送
- 容器事件通知推送
- 支持多客户端订阅

#### WebSocket主题说明

- **容器状态主题**：`/topic/container/status/{serverId}` - 推送指定服务器的容器状态变化
- **容器日志主题**：`/topic/container/logs/{containerId}` - 推送指定容器的实时日志
- **容器统计主题**：`/topic/container/statistics/{serverId}` - 推送指定服务器的容器统计信息
- **容器事件主题**：`/topic/container/events/{serverId}` - 推送指定服务器的容器事件通知

#### 推送时机

- **容器创建时**：推送容器创建事件和状态更新
- **容器启动时**：推送容器启动事件和状态更新
- **容器停止时**：推送容器停止事件和状态更新
- **容器重启时**：推送容器重启事件和状态更新
- **容器删除时**：推送容器删除事件
- **状态同步时**：推送批量状态更新
- **手动触发时**：支持手动推送统计信息和状态

#### 消息格式

```json
{
  "type": "CONTAINER_STATUS_CHANGE",
  "serverId": 1,
  "containerId": 123,
  "containerName": "nginx-container",
  "status": "RUNNING",
  "timestamp": "2024-01-01T12:00:00Z",
  "data": {
    "previousStatus": "CREATED",
    "currentStatus": "RUNNING"
  }
}
```

#### 使用示例

前端可通过STOMP客户端订阅相关主题：

```javascript
// 订阅容器状态变化
stompClient.subscribe('/topic/container/status/1', function(message) {
    const statusUpdate = JSON.parse(message.body);
    // 处理状态更新
});

// 订阅容器日志
stompClient.subscribe('/topic/container/logs/123', function(message) {
    const logData = JSON.parse(message.body);
    // 处理日志数据
});
```

### 与SystemSoftRecord的区别

| 特性       | SystemSoftRecord | SystemSoftContainer |
|----------|------------------|---------------------|
| **用途**   | 安装/卸载操作记录        | 容器运行时信息管理           |
| **数据特点** | 历史记录，不可变         | 实时状态，可更新            |
| **主要字段** | 操作类型、操作状态、操作时间   | 容器状态、健康状态、配置信息      |
| **生命周期** | 操作完成后不再变更        | 随容器状态实时更新           |
| **查询场景** | 审计、历史追踪          | 监控、管理、运维            |

### 使用场景

1. **容器监控**：实时查看服务器上所有容器的运行状态
2. **容器管理**：启动、停止、重启容器操作
3. **状态同步**：定期从Docker守护进程同步容器最新状态
4. **统计分析**：分析容器分布、状态分布等运维指标
5. **故障排查**：查看容器错误信息、退出码等故障信息

### 容器状态说明

- **CREATED**：容器已创建但未启动
- **RUNNING**：容器正在运行
- **PAUSED**：容器已暂停
- **RESTARTING**：容器正在重启
- **REMOVING**：容器正在删除
- **EXITED**：容器已退出
- **DEAD**：容器已死亡

### 健康状态说明

- **HEALTHY**：容器健康检查通过
- **UNHEALTHY**：容器健康检查失败
- **STARTING**：容器健康检查启动中
- **NONE**：容器未配置健康检查

## SystemSoftRecord 操作记录状态管理

### 状态常量定义

为了规范SystemSoftRecord实体中的状态字段类型，新增了`SystemSoftRecordStatus`常量类，定义了所有可能的操作状态：

| 状态值 | 常量名         | 描述  | 使用场景       |
|-----|-------------|-----|------------|
| 0   | WAITING     | 等待中 | 操作排队等待执行   |
| 1   | IN_PROGRESS | 进行中 | 操作正在执行中    |
| 2   | SUCCESS     | 成功  | 操作执行成功     |
| 3   | FAILED      | 失败  | 操作执行失败     |
| 4   | CANCELLED   | 已取消 | 操作被用户或系统取消 |

### 重要修复说明

**类型安全修复**：修复了SystemSoftRecord相关服务中状态字段的类型错误问题：

- 修复前：`record.setSystemSoftRecordStatus("IN_PROGRESS")` (字符串类型)
- 修复后：`record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS)` (Integer类型)

### 使用示例

```java
// 创建操作记录
SystemSoftRecord record = new SystemSoftRecord();
record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);

// 更新操作状态
record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);

// 状态验证
if (SystemSoftRecordStatus.isValidStatus(record.getSystemSoftRecordStatus())) {
    String description = SystemSoftRecordStatus.getStatusDescription(record.getSystemSoftRecordStatus());
    log.info("操作状态: {}", description);
}
```

### 涉及的修复文件

- `SystemSoftContainerServiceImpl.java`：修复容器操作记录状态设置
- `SystemSoftServiceImpl.java`：修复软件操作记录状态设置
- `SystemSoftRecordStatus.java`：新增状态常量定义类

## SystemSoftContainer 条件查询功能

### 新增方法说明

**listByCondition方法**：根据多种条件灵活查询容器列表，支持以下查询参数：

| 参数名             | 类型      | 说明         | 示例              |
|-----------------|---------|------------|-----------------|
| serverId        | Integer | 服务器ID      | 1               |
| softId          | Integer | 软件ID       | 2               |
| containerStatus | String  | 容器状态       | "RUNNING"       |
| containerName   | String  | 容器名称（模糊查询） | "nginx"         |
| imageId         | String  | 镜像ID       | "sha256:abc123" |
| hostPort        | Integer | 主机端口       | 8080            |

### 使用示例

```java
// 查询指定服务器的所有运行中容器
Map<String, Object> queryParams = new HashMap<>();
queryParams.put("serverId", 1);
queryParams.put("containerStatus", "RUNNING");
List<SystemSoftContainer> containers = containerService.listByCondition(queryParams);

// 查询包含特定名称的容器
Map<String, Object> nameQuery = new HashMap<>();
nameQuery.put("containerName", "nginx");
List<SystemSoftContainer> nginxContainers = containerService.listByCondition(nameQuery);

// 查询使用特定端口的容器
Map<String, Object> portQuery = new HashMap<>();
portQuery.put("hostPort", 8080);
List<SystemSoftContainer> portContainers = containerService.listByCondition(portQuery);
```

### 应用场景

- **WebSocket实时推送**：用于获取特定条件的容器列表进行状态推送
- **容器统计分析**：按服务器、状态等维度统计容器信息
- **批量操作筛选**：根据条件筛选需要操作的容器
- **监控告警查询**：查询异常状态或特定条件的容器

## SystemSoftImage 镜像管理功能

### 功能说明

SystemSoftImage实体用于管理Docker镜像信息，提供镜像的全生命周期管理，包括镜像信息存储、版本管理、使用统计、清理优化等功能。

### 最新修复

- **字段不一致问题修复**: 修复实体类缺少 `system_soft_id` 字段的问题，确保与数据库表结构完全一致
- **映射文件完善**: 新增完整的 `SystemSoftImageMapper.xml` 映射文件，包含所有字段映射和SQL实现
- **数据一致性验证**: 验证实体类字段与数据库表字段的完全一致性，确保数据操作的正确性

### 核心实体

- **SystemSoftImage**: Docker镜像信息实体，包含镜像基本信息、版本信息、大小统计、使用情况等

### 新增接口

- **SystemSoftImageMapper**: 镜像数据访问层，提供30+个数据库操作方法
- **SystemSoftImageService**: 镜像业务逻辑层，提供镜像管理的核心业务方法
- **SystemSoftImageController**: 镜像REST API控制器，提供25个HTTP接口

### 关键实现类

- **SystemSoftImageServiceImpl**: 镜像服务实现类，包含完整的业务逻辑和异常处理
- **SystemSoftImageMapper.xml**: MyBatis映射文件，包含所有SQL实现

### 与SystemSoftRecord的区别

- **SystemSoftRecord**: 专注于软件版本记录和同步
- **SystemSoftImage**: 专注于Docker镜像的详细管理
- **SystemSoftContainer**: 专注于容器实例的运行状态管理

### 使用场景

1. **镜像信息管理**: 存储和查询Docker镜像的详细信息
2. **版本管理**: 管理镜像的不同版本和标签
3. **使用统计**: 统计镜像的拉取次数、使用情况
4. **存储优化**: 识别和清理未使用的镜像
5. **架构管理**: 管理不同架构的镜像版本
6. **仓库管理**: 管理不同仓库的镜像信息

### 镜像状态说明

- **0**: 未使用 - 镜像已下载但未被容器使用
- **1**: 使用中 - 镜像正在被一个或多个容器使用
- **2**: 已停用 - 镜像已被标记为停用状态

### 官方镜像标识

- **0**: 非官方镜像 - 第三方或私有镜像
- **1**: 官方镜像 - Docker Hub官方维护的镜像

### 主要功能特性

1. **多维度查询**: 支持按服务器、名称、标签、仓库、架构等多种方式查询
2. **统计分析**: 提供镜像数量、大小、使用情况等统计功能
3. **版本管理**: 支持镜像版本列表查询和版本比较
4. **批量操作**: 支持批量更新状态、批量删除等操作
5. **搜索功能**: 支持关键词搜索镜像
6. **同步功能**: 支持与Docker服务器同步镜像信息
7. **清理优化**: 支持查询和清理未使用的镜像
8. **热门排行**: 支持查询热门镜像和最新镜像

## SystemSoftImage 接口文档

### 查询接口

#### 1. 分页查询镜像列表

**接口地址：** `GET /v1/system/soft/image/page`

**接口说明：** 分页查询镜像列表，支持按服务器ID、状态、关键词筛选

**请求参数：**

| 参数名      | 类型      | 必填 | 默认值 | 说明           |
|----------|---------|----|-----|--------------|
| current  | Long    | 否  | 1   | 当前页          |
| size     | Long    | 否  | 10  | 每页大小         |
| serverId | Integer | 否  | -   | 服务器ID        |
| status   | String  | 否  | -   | 镜像状态         |
| keyword  | String  | 否  | -   | 关键词（镜像名称或标签） |

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "systemSoftImageId": 1,
        "systemSoftId": 10,
        "systemSoftImageServerId": 5,
        "systemSoftImageServerName": "生产服务器",
        "systemSoftImageImageId": "sha256:abc123",
        "systemSoftImageName": "nginx",
        "systemSoftImageTag": "1.21.6",
        "systemSoftImageFullName": "nginx:1.21.6",
        "systemSoftImageRepository": "docker.io/library",
        "systemSoftImageSize": 142857600,
        "systemSoftImageCreated": "2024-01-01T10:00:00Z",
        "systemSoftImageArchitecture": "amd64",
        "systemSoftImageOsType": "linux",
        "systemSoftImageDigest": "sha256:def456",
        "systemSoftImageStatus": "AVAILABLE",
        "systemSoftImageIsOfficial": 1,
        "systemSoftImageDescription": "高性能Web服务器",
        "systemSoftImageTagCount": 50,
        "systemSoftImagePullCount": 1000000,
        "systemSoftImageStarCount": 15000,
        "systemSoftImageLastPulled": "2024-01-01T12:00:00Z",
        "systemSoftImageLastPushed": "2024-01-01T11:00:00Z",
        "systemSoftImageRemark": "官方镜像"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 2. 根据软件ID查询镜像列表

**接口地址：** `GET /v1/system/soft/image/soft/{softId}`

**接口说明：** 根据软件ID查询该软件的所有镜像版本

**路径参数：**

| 参数名    | 类型      | 必填 | 说明   |
|--------|---------|----|------|
| softId | Integer | 是  | 软件ID |

**响应参数：** 返回镜像列表，格式同分页查询接口

#### 3. 根据服务器ID查询镜像列表

**接口地址：** `GET /v1/system/soft/image/server/{serverId}`

**接口说明：** 根据服务器ID查询该服务器上的所有镜像

**路径参数：**

| 参数名      | 类型      | 必填 | 说明    |
|----------|---------|----|-------|
| serverId | Integer | 是  | 服务器ID |

**响应参数：** 返回镜像列表，格式同分页查询接口

#### 4. 根据镜像名称和标签查询镜像

**接口地址：** `GET /v1/system/soft/image/detail`

**接口说明：** 根据镜像名称和标签查询具体镜像信息

**请求参数：**

| 参数名       | 类型      | 必填 | 说明    |
|-----------|---------|----|-------|
| imageName | String  | 是  | 镜像名称  |
| imageTag  | String  | 是  | 镜像标签  |
| serverId  | Integer | 否  | 服务器ID |

**响应参数：** 返回单个镜像信息，格式同分页查询接口中的records项

### 管理接口

#### 1. 同步单个镜像状态

**接口地址：** `POST /v1/system/soft/image/sync/{imageId}`

**接口说明：** 从Docker API同步指定镜像的状态到数据库，适用于页面点击查看镜像状态时先同步最新状态

**路径参数：**

| 参数名     | 类型      | 必填 | 说明   |
|---------|---------|----|------|
| imageId | Integer | 是  | 镜像ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "镜像状态同步成功",
  "data": true
}
```

**使用场景：**

- 用户点击查看镜像详情时，先同步最新状态再显示
- 镜像状态异常时，手动同步确认当前状态
- 实时获取镜像的最新信息和使用情况

#### 2. 同步镜像状态

**接口地址：** `POST /v1/system/soft/image/sync`

**接口说明：** 从Docker API同步镜像状态到数据库

**请求参数：**

| 参数名      | 类型      | 必填 | 说明                |
|----------|---------|----|-------------------|
| serverId | Integer | 否  | 服务器ID（为空时同步所有服务器） |

**响应参数：**

```json
{
  "code": 200,
  "message": "同步完成，更新了 5 个镜像状态",
  "data": 5
}
```

#### 3. 同步指定服务器的所有镜像状态

**接口地址：** `POST /v1/system/soft/image/sync/server/{serverId}`

**接口说明：** 同步指定服务器的所有镜像状态

**路径参数：**

| 参数名      | 类型      | 必填 | 说明    |
|----------|---------|----|-------|
| serverId | Integer | 是  | 服务器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "服务器镜像状态同步成功",
  "data": true
}
```

#### 4. 检查镜像是否存在

**接口地址：** `GET /v1/system/soft/image/exists`

**接口说明：** 检查指定镜像是否存在于服务器上

**请求参数：**

| 参数名       | 类型      | 必填 | 说明    |
|-----------|---------|----|-------|
| imageName | String  | 是  | 镜像名称  |
| imageTag  | String  | 是  | 镜像标签  |
| serverId  | Integer | 否  | 服务器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

#### 5. 获取可用镜像列表

**接口地址：** `GET /v1/system/soft/image/available`

**接口说明：** 获取所有可用状态的镜像

**响应参数：** 返回镜像列表，格式同分页查询接口

#### 6. 获取异常镜像列表

**接口地址：** `GET /v1/system/soft/image/abnormal`

**接口说明：** 获取所有异常状态的镜像

**响应参数：** 返回镜像列表，格式同分页查询接口

#### 7. 搜索镜像

**接口地址：** `GET /v1/system/soft/image/search`

**接口说明：** 根据关键词搜索镜像

**请求参数：**

| 参数名     | 类型     | 必填 | 说明    |
|---------|--------|----|-------|
| keyword | String | 是  | 搜索关键词 |

**响应参数：** 返回镜像列表，格式同分页查询接口

#### 8. 更新镜像状态

**接口地址：** `PUT /v1/system/soft/image/{imageId}/status`

**接口说明：** 更新指定镜像的状态

**路径参数：**

| 参数名     | 类型      | 必填 | 说明   |
|---------|---------|----|------|
| imageId | Integer | 是  | 镜像ID |

**请求参数：**

| 参数名    | 类型     | 必填 | 说明   |
|--------|--------|----|------|
| status | String | 是  | 镜像状态 |

**响应参数：**

```json
{
  "code": 200,
  "message": "镜像状态更新成功",
  "data": true
}
```

#### 9. 删除镜像记录

**接口地址：** `DELETE /v1/system/soft/image/{imageId}`

**接口说明：** 删除指定的镜像记录

**路径参数：**

| 参数名     | 类型      | 必填 | 说明   |
|---------|---------|----|------|
| imageId | Integer | 是  | 镜像ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "镜像记录删除成功",
  "data": true
}
```

#### 10. 统计镜像数量

**接口地址：** `GET /v1/system/soft/image/count`

**接口说明：** 统计镜像数量

**请求参数：**

| 参数名      | 类型      | 必填 | 说明    |
|----------|---------|----|-------|
| serverId | Integer | 否  | 服务器ID |
| status   | String  | 否  | 镜像状态  |

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 25
}
```

#### 11. 获取最新镜像列表

**接口地址：** `GET /v1/system/soft/image/latest`

**接口说明：** 获取最新的镜像列表

**请求参数：**

| 参数名   | 类型      | 必填 | 默认值 | 说明     |
|-------|---------|----|-----|--------|
| limit | Integer | 否  | 10  | 返回数量限制 |

**响应参数：** 返回镜像列表，格式同分页查询接口

### 镜像状态说明

| 状态值       | 说明   |
|-----------|------|
| AVAILABLE | 可用   |
| PULLING   | 拉取中  |
| BUILDING  | 构建中  |
| FAILED    | 失败   |
| DELETED   | 已删除  |
| UNKNOWN   | 未知状态 |

### 使用示例

#### 1. 查看镜像详情并同步状态

```bash
# 1. 先同步镜像状态
curl -X POST "http://localhost:8080/v1/system/soft/image/sync/123"

# 2. 查看镜像详情
curl -X GET "http://localhost:8080/v1/system/soft/image/detail?imageName=nginx&imageTag=1.21.6&serverId=5"
```

#### 2. 搜索和筛选镜像

```bash
# 搜索nginx相关镜像
curl -X GET "http://localhost:8080/v1/system/soft/image/search?keyword=nginx"

# 分页查询指定服务器的镜像
curl -X GET "http://localhost:8080/v1/system/soft/image/page?current=1&size=10&serverId=5&status=AVAILABLE"
```

#### 3. 镜像状态管理

```bash
# 获取异常镜像列表
curl -X GET "http://localhost:8080/v1/system/soft/image/abnormal"

# 更新镜像状态
curl -X PUT "http://localhost:8080/v1/system/soft/image/123/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "AVAILABLE"}'

# 同步所有镜像状态
curl -X POST "http://localhost:8080/v1/system/soft/image/sync"
```

## 目录

- [实体说明](#实体说明)
- [查询接口](#查询接口)
- [管理接口](#管理接口)
- [WebSocket接口](#websocket接口)
- [使用示例](#使用示例)
- [错误码说明](#错误码说明)
- [最佳实践](#最佳实践)

## 实体说明

### SystemSoftContainer 容器实体

| 字段名                              | 类型            | 说明         | 示例值                         |
|----------------------------------|---------------|------------|-----------------------------|
| systemSoftContainerId            | Integer       | 容器主键ID     | 1                           |
| systemSoftId                     | Integer       | 关联的软件ID    | 10                          |
| systemSoftVersionId              | Integer       | 关联的软件版本ID  | 25                          |
| systemServerId                   | Integer       | 关联的服务器ID   | 5                           |
| systemSoftContainerName          | String        | 容器名称       | "nginx-web-server"          |
| systemSoftContainerDockerId      | String        | Docker容器ID | "a1b2c3d4e5f6"              |
| systemSoftContainerStatus        | String        | 容器状态       | "RUNNING"                   |
| systemSoftContainerHealthStatus  | String        | 健康状态       | "HEALTHY"                   |
| systemSoftContainerImageName     | String        | 镜像名称       | "nginx"                     |
| systemSoftContainerImageTag      | String        | 镜像标签       | "1.21.6"                    |
| systemSoftContainerPorts         | String        | 端口映射配置     | "80:8080,443:8443"          |
| systemSoftContainerVolumes       | String        | 卷挂载配置      | "/data:/var/www/html"       |
| systemSoftContainerEnvironment   | String        | 环境变量配置     | "ENV=prod,DEBUG=false"      |
| systemSoftContainerNetworks      | String        | 网络配置       | "bridge"                    |
| systemSoftContainerLabels        | String        | 标签配置       | "app=web,version=1.0"       |
| systemSoftContainerCommand       | String        | 启动命令       | "nginx -g 'daemon off;'"    |
| systemSoftContainerArgs          | String        | 启动参数       | "-c /etc/nginx/nginx.conf"  |
| systemSoftContainerWorkingDir    | String        | 工作目录       | "/var/www/html"             |
| systemSoftContainerUser          | String        | 运行用户       | "nginx"                     |
| systemSoftContainerRestartPolicy | String        | 重启策略       | "always"                    |
| systemSoftContainerCpuLimit      | String        | CPU限制      | "1.5"                       |
| systemSoftContainerMemoryLimit   | String        | 内存限制       | "512m"                      |
| systemSoftContainerCreatedTime   | LocalDateTime | 创建时间       | "2024-01-01T10:00:00"       |
| systemSoftContainerStartedTime   | LocalDateTime | 启动时间       | "2024-01-01T10:01:00"       |
| systemSoftContainerFinishedTime  | LocalDateTime | 结束时间       | "2024-01-01T18:00:00"       |
| systemSoftContainerExitCode      | Integer       | 退出码        | 0                           |
| systemSoftContainerError         | String        | 错误信息       | ""                          |
| systemSoftContainerLogPath       | String        | 日志路径       | "/var/log/nginx/access.log" |
| systemSoftContainerConfigHash    | String        | 配置哈希值      | "abc123def456"              |
| systemSoftContainerRemark        | String        | 备注信息       | "Web服务器容器"                  |

### 容器状态枚举

| 状态值        | 说明  | 描述        |
|------------|-----|-----------|
| CREATED    | 已创建 | 容器已创建但未启动 |
| RUNNING    | 运行中 | 容器正在运行    |
| PAUSED     | 已暂停 | 容器已暂停     |
| RESTARTING | 重启中 | 容器正在重启    |
| REMOVING   | 删除中 | 容器正在删除    |
| EXITED     | 已退出 | 容器已退出     |
| DEAD       | 已死亡 | 容器已死亡     |

### 健康状态枚举

| 状态值       | 说明  | 描述        |
|-----------|-----|-----------|
| HEALTHY   | 健康  | 容器健康检查通过  |
| UNHEALTHY | 不健康 | 容器健康检查失败  |
| STARTING  | 启动中 | 容器健康检查启动中 |
| NONE      | 无检查 | 容器未配置健康检查 |

## 查询接口

### 1. 分页查询容器列表

**接口地址：** `GET /v1/system/soft/container/page`

**接口说明：** 根据条件分页查询容器列表

**请求参数：**

| 参数名      | 类型      | 必填 | 默认值 | 说明             |
|----------|---------|----|-----|----------------|
| current  | Long    | 否  | 1   | 当前页            |
| size     | Long    | 否  | 10  | 每页大小           |
| serverId | Integer | 否  | -   | 服务器ID          |
| status   | String  | 否  | -   | 容器状态           |
| keyword  | String  | 否  | -   | 关键词（容器名称、镜像名称） |

**响应参数（使用ReturnResult）：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "systemSoftContainerId": 1,
        "systemSoftId": 10,
        "systemServerId": 5,
        "systemSoftContainerName": "nginx-web-server",
        "systemSoftContainerStatus": "RUNNING",
        "systemSoftContainerHealthStatus": "HEALTHY",
        "systemSoftContainerImageName": "nginx",
        "systemSoftContainerImageTag": "1.21.6",
        "systemSoftContainerCreatedTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "success": true
}
```

### 2. 根据软件ID查询容器列表

**接口地址：** `GET /v1/system/soft/container/soft/{softId}`

**接口说明：** 根据软件ID查询该软件的所有容器

**路径参数：**

| 参数名    | 类型      | 必填 | 说明   |
|--------|---------|----|------|
| softId | Integer | 是  | 软件ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "systemSoftContainerId": 1,
      "systemSoftId": 10,
      "systemSoftContainerName": "nginx-web-server",
      "systemSoftContainerStatus": "RUNNING"
    }
  ]
}
```

### 3. 根据服务器ID查询容器列表

**接口地址：** `GET /v1/system/soft/container/server/{serverId}`

**接口说明：** 根据服务器ID查询该服务器的所有容器

**路径参数：**

| 参数名      | 类型      | 必填 | 说明    |
|----------|---------|----|-------|
| serverId | Integer | 是  | 服务器ID |

**响应参数：** 同上

### 4. 根据ID查询容器详情

**接口地址：** `GET /v1/system/soft/container/{id}`

**接口说明：** 根据容器ID查询容器详细信息

**路径参数：**

| 参数名 | 类型      | 必填 | 说明   |
|-----|---------|----|------|
| id  | Integer | 是  | 容器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "systemSoftContainerId": 1,
    "systemSoftId": 10,
    "systemSoftVersionId": 25,
    "systemServerId": 5,
    "systemSoftContainerName": "nginx-web-server",
    "systemSoftContainerDockerId": "a1b2c3d4e5f6",
    "systemSoftContainerStatus": "RUNNING",
    "systemSoftContainerHealthStatus": "HEALTHY",
    "systemSoftContainerImageName": "nginx",
    "systemSoftContainerImageTag": "1.21.6",
    "systemSoftContainerPorts": "80:8080,443:8443",
    "systemSoftContainerVolumes": "/data:/var/www/html",
    "systemSoftContainerEnvironment": "ENV=prod,DEBUG=false",
    "systemSoftContainerCreatedTime": "2024-01-01T10:00:00",
    "systemSoftContainerStartedTime": "2024-01-01T10:01:00"
  }
}
```

### 5. 获取容器日志

**接口地址：** `GET /v1/system/soft/container/{id}/logs`

**接口说明：** 获取指定容器的日志

**路径参数：**

| 参数名 | 类型      | 必填 | 说明   |
|-----|---------|----|------|
| id  | Integer | 是  | 容器ID |

**请求参数：**

| 参数名   | 类型      | 必填 | 默认值 | 说明   |
|-------|---------|----|-----|------|
| lines | Integer | 否  | 100 | 日志行数 |

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "2024-01-01 10:00:00 [INFO] Starting nginx\n2024-01-01 10:00:01 [INFO] nginx started successfully"
}
```

### 6. 获取容器统计信息

**接口地址：** `GET /v1/system/soft/container/{id}/stats`

**接口说明：** 获取指定容器的资源使用统计信息

**路径参数：**

| 参数名 | 类型      | 必填 | 说明   |
|-----|---------|----|------|
| id  | Integer | 是  | 容器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "cpuUsage": 15.5,
    "memoryUsage": 256,
    "memoryLimit": 512,
    "networkRx": 1024,
    "networkTx": 2048,
    "diskRead": 512,
    "diskWrite": 1024,
    "timestamp": "2024-01-01T12:00:00Z"
  }
}
```

### 7. 获取容器状态统计

**接口地址：** `GET /v1/system/soft/container/stats`

**接口说明：** 获取各状态容器的数量统计

**请求参数：**

| 参数名      | 类型      | 必填 | 说明    |
|----------|---------|----|-------|
| serverId | Integer | 否  | 服务器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 10,
    "running": 8,
    "stopped": 1,
    "paused": 0,
    "exited": 1,
    "dead": 0
  }
}
```

### 8. 获取运行中的容器列表

**接口地址：** `GET /v1/system/soft/container/running`

**接口说明：** 获取所有运行中的容器

**响应参数：** 返回容器列表，格式同查询接口

### 9. 获取异常容器列表

**接口地址：** `GET /v1/system/soft/container/abnormal`

**接口说明：** 获取所有异常状态的容器

**响应参数：** 返回容器列表，格式同查询接口

### 10. 更新容器配置

**接口地址：** `PUT /v1/system/soft/container/{id}`

**接口说明：** 更新指定容器的配置信息

**路径参数：**

| 参数名 | 类型      | 必填 | 说明   |
|-----|---------|----|------|
| id  | Integer | 是  | 容器ID |

**请求参数：**

```json
{
  "containerName": "nginx-web-updated",
  "imageTag": "1.22.0",
  "ports": "80:8080,443:8443",
  "envVars": "ENV=production,DEBUG=false",
  "volumes": "/data:/var/www/html,/logs:/var/log/nginx",
  "command": "nginx -g 'daemon off;'",
  "args": "-c /etc/nginx/nginx.conf",
  "autoRestart": 1
}
```

**请求参数说明：**

| 参数名           | 类型      | 必填 | 说明     |
|---------------|---------|----|--------|
| containerName | String  | 否  | 容器名称   |
| imageTag      | String  | 否  | 镜像标签   |
| ports         | String  | 否  | 端口映射配置 |
| envVars       | String  | 否  | 环境变量配置 |
| volumes       | String  | 否  | 卷挂载配置  |
| command       | String  | 否  | 启动命令   |
| args          | String  | 否  | 启动参数   |
| autoRestart   | Integer | 否  | 自动重启设置 |

**响应参数：**

```json
{
  "code": 200,
  "message": "容器配置更新成功",
  "data": true
}
```

## 管理接口

### 1. 创建容器

**接口地址：** `POST /v1/system/soft/container/create`

**接口说明：** 根据软件镜像创建新容器

**请求参数：**

```json
{
  "softId": 10,
  "serverId": 5,
  "containerName": "nginx-web-server",
  "imageTag": "1.21.6",
  "ports": "80:8080,443:8443",
  "envVars": "ENV=prod,DEBUG=false",
  "volumes": "/data:/var/www/html",
  "command": "nginx -g 'daemon off;'",
  "args": "-c /etc/nginx/nginx.conf"
}
```

**请求参数说明：**

| 参数名           | 类型      | 必填 | 说明     |
|---------------|---------|----|--------|
| softId        | Integer | 是  | 软件ID   |
| serverId      | Integer | 是  | 服务器ID  |
| containerName | String  | 是  | 容器名称   |
| imageTag      | String  | 是  | 镜像标签   |
| ports         | String  | 否  | 端口映射配置 |
| envVars       | String  | 否  | 环境变量配置 |
| volumes       | String  | 否  | 卷挂载配置  |
| command       | String  | 否  | 启动命令   |
| args          | String  | 否  | 启动参数   |

**响应参数：**

```json
{
  "code": 200,
  "message": "容器创建成功",
  "data": true
}
```

### 2. 启动容器

**接口地址：** `POST /v1/system/soft/container/{id}/start`

**接口说明：** 启动指定的容器

**路径参数：**

| 参数名 | 类型      | 必填 | 说明   |
|-----|---------|----|------|
| id  | Integer | 是  | 容器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "容器启动成功",
  "data": true
}
```

### 3. 停止容器

**接口地址：** `POST /v1/system/soft/container/{id}/stop`

**接口说明：** 停止指定的容器

**路径参数：**

| 参数名 | 类型      | 必填 | 说明   |
|-----|---------|----|------|
| id  | Integer | 是  | 容器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "容器停止成功",
  "data": true
}
```

### 4. 重启容器

**接口地址：** `POST /v1/system/soft/container/{id}/restart`

**接口说明：** 重启指定的容器

**路径参数：**

| 参数名 | 类型      | 必填 | 说明   |
|-----|---------|----|------|
| id  | Integer | 是  | 容器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "容器重启成功",
  "data": true
}
```

### 5. 删除容器

**接口地址：** `DELETE /v1/system/soft/container/{id}`

**接口说明：** 删除指定的容器

**路径参数：**

| 参数名 | 类型      | 必填 | 说明   |
|-----|---------|----|------|
| id  | Integer | 是  | 容器ID |

**请求参数：**

| 参数名   | 类型      | 必填 | 默认值   | 说明     |
|-------|---------|----|-------|--------|
| force | Boolean | 否  | false | 是否强制删除 |

**响应参数：**

```json
{
  "code": 200,
  "message": "容器删除成功",
  "data": true
}
```

### 6. 同步容器状态

**接口地址：** `POST /v1/system/soft/container/sync`

**接口说明：** 从Docker API同步容器状态到数据库

**请求参数：**

| 参数名      | 类型      | 必填 | 说明                |
|----------|---------|----|-------------------|
| serverId | Integer | 否  | 服务器ID（为空时同步所有服务器） |

**响应参数：**

```json
{
  "code": 200,
  "message": "同步完成",
  "data": 5
}
```

### 6.1. 同步单个容器状态

**接口地址：** `POST /v1/system/soft/container/{id}/sync`

**接口说明：** 从Docker API同步指定容器的状态到数据库，适用于页面点击查看容器状态时先同步最新状态

**路径参数：**

| 参数名 | 类型      | 必填 | 说明   |
|-----|---------|----|------|
| id  | Integer | 是  | 容器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "容器状态同步成功",
  "data": {
    "systemSoftContainerId": 123,
    "containerName": "nginx-web-server",
    "containerStatus": "running",
    "containerDockerStatus": "Up 2 hours",
    "containerCpuUsage": 15.5,
    "containerMemoryUsage": 256,
    "containerNetworkRx": 1024,
    "containerNetworkTx": 2048,
    "containerUpdateTime": "2024-01-01T12:00:00Z"
  }
}
```

**使用场景：**

- 用户点击查看容器详情时，先同步最新状态再显示
- 容器状态异常时，手动同步确认当前状态
- 实时获取容器的最新运行状态和资源使用情况

### 7. 批量操作容器

**接口地址：** `POST /v1/system/soft/container/batch`

**接口说明：** 批量启动、停止、重启或删除容器

**请求参数：**

```json
{
  "containerIds": [1, 2, 3],
  "operation": "start"
}
```

**请求参数说明：**

| 参数名          | 类型            | 必填 | 说明                              |
|--------------|---------------|----|---------------------------------|
| containerIds | List<Integer> | 是  | 容器ID列表                          |
| operation    | String        | 是  | 操作类型（start/stop/restart/remove） |

**响应参数：**

```json
{
  "code": 200,
  "message": "批量操作完成",
  "data": {
    "total": 3,
    "success": 2,
    "failed": 1,
    "results": [
      {
        "containerId": 1,
        "success": true,
        "message": "操作成功"
      },
      {
        "containerId": 2,
        "success": true,
        "message": "操作成功"
      },
      {
        "containerId": 3,
        "success": false,
        "message": "容器不存在"
      }
    ]
  }
}
```

## WebSocket接口

### 1. 获取WebSocket主题信息

**接口地址：** `GET /v1/system/soft/container/websocket/topics`

**接口说明：** 获取WebSocket推送主题列表

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "containerStatus": "/topic/container/status/{serverId}",
    "containerLogs": "/topic/container/logs/{containerId}",
    "containerStatistics": "/topic/container/statistics/{serverId}",
    "containerEvents": "/topic/container/events/{serverId}"
  }
}
```

### 2. 获取WebSocket连接状态

**接口地址：** `GET /v1/system/soft/container/websocket/status`

**接口说明：** 获取WebSocket连接状态信息

**响应参数：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "enabled": true,
    "activeConnections": 5,
    "totalSessions": 10,
    "uptime": "2024-01-01T10:00:00Z"
  }
}
```

### 3. 手动推送容器统计信息

**接口地址：** `POST /v1/system/soft/container/websocket/push-statistics`

**接口说明：** 手动推送容器统计信息到WebSocket客户端

**请求参数：**

| 参数名      | 类型      | 必填 | 说明    |
|----------|---------|----|-------|
| serverId | Integer | 是  | 服务器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "推送成功",
  "data": "统计信息已推送到主题: /topic/container/statistics/1"
}
```

### 4. 手动推送批量容器状态

**接口地址：** `POST /v1/system/soft/container/websocket/push-status`

**接口说明：** 手动推送批量容器状态到WebSocket客户端

**请求参数：**

| 参数名      | 类型      | 必填 | 说明    |
|----------|---------|----|-------|
| serverId | Integer | 是  | 服务器ID |

**响应参数：**

```json
{
  "code": 200,
  "message": "推送成功",
  "data": "容器状态已推送到主题: /topic/container/status/1"
}
```

## 使用示例

### 1. 创建并启动容器的完整流程

```bash
# 1. 创建容器
curl -X POST "http://localhost:8080/v1/system/soft/container/create" \
  -H "Content-Type: application/json" \
  -d '{
    "softId": 10,
    "serverId": 5,
    "containerName": "nginx-web-server",
    "imageTag": "1.21.6",
    "ports": "80:8080",
    "envVars": "ENV=prod"
  }'

# 2. 启动容器
curl -X POST "http://localhost:8080/v1/system/soft/container/1/start"

# 3. 查看容器状态
curl -X GET "http://localhost:8080/v1/system/soft/container/1"

# 4. 查看容器日志
curl -X GET "http://localhost:8080/v1/system/soft/container/1/logs?lines=50"
```

### 2. WebSocket客户端订阅示例

```javascript
// 使用STOMP客户端订阅容器状态变化
const stompClient = new StompJs.Client({
  brokerURL: 'ws://localhost:8080/ws',
  onConnect: function (frame) {
    console.log('Connected: ' + frame);
    
    // 订阅容器状态变化
    stompClient.subscribe('/topic/container/status/1', function (message) {
      const statusUpdate = JSON.parse(message.body);
      console.log('容器状态更新:', statusUpdate);
      // 更新UI显示
      updateContainerStatus(statusUpdate);
    });
    
    // 订阅容器日志
    stompClient.subscribe('/topic/container/logs/123', function (message) {
      const logData = JSON.parse(message.body);
      console.log('容器日志:', logData);
      // 追加日志到界面
      appendLog(logData);
    });
    
    // 订阅容器统计信息
    stompClient.subscribe('/topic/container/statistics/1', function (message) {
      const stats = JSON.parse(message.body);
      console.log('容器统计:', stats);
      // 更新统计图表
      updateStatistics(stats);
    });
  }
});

stompClient.activate();
```

### 3. 批量操作示例

```bash
# 批量启动容器
curl -X POST "http://localhost:8080/v1/system/soft/container/batch" \
  -H "Content-Type: application/json" \
  -d '{
    "containerIds": [1, 2, 3],
    "operation": "start"
  }'

# 批量停止容器
curl -X POST "http://localhost:8080/v1/system/soft/container/batch" \
  -H "Content-Type: application/json" \
  -d '{
    "containerIds": [1, 2, 3],
    "operation": "stop"
  }'
```

## 错误码说明

| 错误码  | 说明         | 解决方案               |
|------|------------|--------------------|
| 400  | 请求参数错误     | 检查请求参数格式和必填项       |
| 404  | 容器不存在      | 检查容器ID是否正确         |
| 500  | 服务器内部错误    | 查看服务器日志，检查Docker连接 |
| 1001 | Docker连接失败 | 检查Docker服务是否启动     |
| 1002 | 容器操作失败     | 检查容器状态和Docker权限    |
| 1003 | 镜像不存在      | 检查镜像名称和标签是否正确      |
| 1004 | 端口冲突       | 检查端口映射配置           |
| 1005 | 资源不足       | 检查服务器资源使用情况        |

## 最佳实践

### 1. 容器命名规范

- 使用有意义的名称：`{软件名}-{环境}-{实例号}`
- 示例：`nginx-prod-01`、`mysql-dev-master`

### 2. 端口映射建议

- 避免使用系统保留端口（1-1023）
- 使用统一的端口范围，如：8000-9000
- 格式：`宿主机端口:容器端口`

### 3. 环境变量配置

- 使用键值对格式：`KEY1=value1,KEY2=value2`
- 避免在环境变量中存储敏感信息
- 使用配置文件或密钥管理系统

### 4. 卷挂载建议

- 数据持久化：将重要数据挂载到宿主机
- 配置文件：将配置文件挂载为只读
- 格式：`宿主机路径:容器路径[:权限]`

### 5. 资源限制

- 设置合理的CPU和内存限制
- 避免容器占用过多系统资源
- 根据应用特性调整限制值

### 6. 监控和日志

- 定期检查容器状态
- 配置日志轮转避免磁盘空间不足
- 使用WebSocket实时监控容器状态变化

### 7. 安全建议

- 不要以root用户运行容器
- 定期更新镜像版本
- 限制容器网络访问权限
- 使用安全的镜像仓库

---

**注意事项：**

1. 所有接口都需要适当的权限验证
2. 容器操作可能需要较长时间，建议设置合理的超时时间
3. WebSocket连接需要保持心跳以避免连接断开
4. 批量操作时注意服务器资源使用情况
5. 定期清理不需要的容器和镜像以释放存储空间

**作者：** [CH]  
**创建时间：** 2024-01-01  
**版本：** 1.0.0