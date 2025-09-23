# Docker软件管理流程文档

## 概述

本文档描述了系统中Docker软件管理的完整流程，包括从软件仓库同步、软件安装到容器启动的全过程。

## Docker软件管理功能

### 功能特性

- **软件仓库同步**：从软件仓库同步软件信息到本地数据库
- **镜像管理**：支持Docker镜像的拉取、推送、构建等操作
- **实时进度推送**：通过WebSocket实时推送Docker操作进度
- **多服务器部署**：支持在多个服务器上同时部署软件
- **状态监控**：实时监控软件安装和运行状态

### 核心接口

#### 软件管理

- `GET /api/system/soft/page` - 获取软件列表（分页）
- `POST /api/system/soft/sync/{softId}` - 同步软件镜像
- `POST /api/system/soft/pull/{softId}` - 拉取软件镜像

#### 安装记录

- `GET /api/system/soft/record/page` - 获取安装记录列表（分页）

### WebSocket消息类型

- `docker_start` - Docker操作开始
- `docker_progress` - Docker操作进度更新
- `docker_complete` - Docker操作完成
- `docker_error` - Docker操作错误

### 数据库表结构

- `system_soft_image` - 软件镜像信息表
- `system_soft_record` - 软件安装记录表

## Docker软件版本同步功能

### 功能说明

本模块集成了 `utils-support-docker-starter`，提供Docker软件版本同步功能：

- 优先使用本地Docker客户端获取镜像信息
- 失败时自动回退到Docker Hub API
- 支持批量同步多个软件的版本信息
- 提供完整的异常处理和日志记录

### 新增接口

- `POST /v1/system-soft/sync-from-docker`
    - 说明：从Docker仓库同步软件版本信息
    - 功能：查询配置了镜像信息的软件，并同步其版本信息到数据库
    - 返回：同步结果信息（成功/失败及详细信息）

### Docker配置

在 `application.yml` 中配置Docker连接参数：

```yaml
monitor:
  docker:
    # 是否启用Docker客户端
    enabled: true
    # Docker守护进程地址
    host: tcp://localhost:2376
    # 是否启用TLS验证
    tls-verify: false
    # TLS证书路径（当tls-verify为true时需要配置）
    cert-path: /path/to/docker/certs
    # 连接超时时间（毫秒）
    connect-timeout: 30000
    # 读取超时时间（毫秒）
    read-timeout: 60000
```

### 关键实现类

- **服务实现**：`com.chua.starter.monitor.service.impl.SystemSoftServiceImpl`
    - `syncFromDocker()`: 主要同步方法
    - `getImageTags()`: 获取镜像标签（支持本地Docker和Docker Hub API）
    - `syncSoftwareVersions()`: 同步单个软件版本信息
- **Docker配置**：`com.chua.starter.monitor.configuration.DockerConfiguration`
    - 提供DockerProtocolClient Bean配置
    - 支持条件化启用/禁用
- **实体类**：
    - `com.chua.starter.monitor.entity.SystemSoft`: 软件信息实体
    - `com.chua.starter.monitor.entity.SystemSoftVersion`: 软件版本信息实体

### 依赖说明

- **新增依赖**：`utils-support-docker-starter`
- **核心功能**：
    - DockerProtocolClient: Docker客户端协议支持
    - Image: Docker镜像信息实体
    - 完整的Docker API操作能力

### 使用说明

1. **配置Docker连接**：在application.yml中配置Docker守护进程地址
2. **配置软件镜像**：在SystemSoft表中为软件配置对应的Docker镜像名称
3. **执行同步**：调用 `/v1/system-soft/sync-from-docker` 接口进行版本同步
4. **查看结果**：同步完成后可在SystemSoftVersion表中查看版本信息

### Docker排障建议

- **Docker连接失败**：检查Docker守护进程是否启动，端口是否正确
- **TLS验证失败**：检查证书路径和TLS配置是否正确
- **镜像标签获取失败**：检查镜像名称格式，确保网络连接正常
- **版本同步异常**：查看日志中的详细错误信息，检查数据库连接

## 业务流程

### 1. 软件仓库同步

用户通过前端软件页面，从软件仓库同步软件信息到本地数据库。

**流程步骤：**

1. 用户在前端选择软件仓库
2. 系统从仓库获取软件列表
3. 将软件信息存储到 `system_soft_image` 表
4. 更新软件状态为可用

### 2. 软件安装部署

用户选择软件并指定目标服务器进行安装。

**流程步骤：**

1. 用户在软件列表中选择要安装的软件
2. 选择目标服务器（支持单个或多个服务器）
3. 系统创建安装记录到 `system_soft_record` 表
4. 系统拉取Docker镜像到指定服务器
5. 实时推送安装进度到前端

### 3. 容器启动管理

基于已安装的镜像启动Docker容器。

**流程步骤：**

1. 用户选择已安装的镜像
2. 配置容器启动参数
3. 启动Docker容器
4. 监控容器运行状态

## 数据库设计

### system_soft_image 表

软件镜像信息表

| 字段名                            | 类型           | 说明         |
|--------------------------------|--------------|------------|
| system_soft_image_id           | BIGINT       | 主键ID       |
| system_soft_image_name         | VARCHAR(255) | 软件名称       |
| system_soft_image_version      | VARCHAR(100) | 软件版本       |
| system_soft_image_docker_image | VARCHAR(500) | Docker镜像名称 |
| system_soft_image_description  | TEXT         | 软件描述       |
| system_soft_image_status       | VARCHAR(50)  | 软件状态       |
| system_soft_image_create_time  | DATETIME     | 创建时间       |
| system_soft_image_update_time  | DATETIME     | 更新时间       |

### system_soft_record 表

软件安装记录表

| 字段名                           | 类型          | 说明    |
|-------------------------------|-------------|-------|
| system_soft_record_id         | BIGINT      | 主键ID  |
| system_soft_record_soft_id    | BIGINT      | 软件ID  |
| system_soft_record_server_id  | BIGINT      | 服务器ID |
| system_soft_record_status     | VARCHAR(50) | 安装状态  |
| system_soft_record_start_time | DATETIME    | 开始时间  |
| system_soft_record_end_time   | DATETIME    | 结束时间  |
| system_soft_record_remark     | TEXT        | 备注信息  |

## API接口文档

### 1. 软件管理接口

#### 1.1 获取软件列表

**接口地址：** `GET /api/system/soft/page`

**请求参数：**

| 参数名    | 类型      | 必填 | 说明         |
|--------|---------|----|------------|
| page   | Integer | 否  | 页码，默认1     |
| size   | Integer | 否  | 每页大小，默认10  |
| name   | String  | 否  | 软件名称（模糊查询） |
| status | String  | 否  | 软件状态       |

**响应示例：**

```json
{
  "code": "00000",
  "message": "success",
  "data": {
    "records": [
      {
        "systemSoftImageId": 1,
        "systemSoftImageName": "nginx",
        "systemSoftImageVersion": "1.21.0",
        "systemSoftImageDockerImage": "nginx:1.21.0",
        "systemSoftImageDescription": "高性能Web服务器",
        "systemSoftImageStatus": "AVAILABLE",
        "systemSoftImageCreateTime": "2024-01-01T10:00:00",
        "systemSoftImageUpdateTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 1.2 同步软件镜像

**接口地址：** `POST /api/system/soft/sync/{softId}`

**路径参数：**

| 参数名    | 类型   | 必填 | 说明   |
|--------|------|----|------|
| softId | Long | 是  | 软件ID |

**响应示例：**

```json
{
  "code": "00000",
  "message": "镜像同步成功",
  "data": true
}
```

#### 1.3 拉取软件镜像

**接口地址：** `POST /api/system/soft/pull/{softId}`

**路径参数：**

| 参数名    | 类型   | 必填 | 说明   |
|--------|------|----|------|
| softId | Long | 是  | 软件ID |

**响应示例：**

```json
{
  "code": "00000",
  "message": "镜像拉取成功",
  "data": true
}
```

### 2. 安装记录接口

#### 2.1 获取安装记录列表

**接口地址：** `GET /api/system/soft/record/page`

**请求参数：**

| 参数名      | 类型      | 必填 | 说明        |
|----------|---------|----|-----------|
| page     | Integer | 否  | 页码，默认1    |
| size     | Integer | 否  | 每页大小，默认10 |
| softId   | Long    | 否  | 软件ID      |
| serverId | Long    | 否  | 服务器ID     |
| status   | String  | 否  | 安装状态      |

**响应示例：**

```json
{
  "code": "00000",
  "message": "success",
  "data": {
    "records": [
      {
        "systemSoftRecordId": 1,
        "systemSoftRecordSoftId": 1,
        "systemSoftRecordServerId": 1,
        "systemSoftRecordStatus": "SUCCESS",
        "systemSoftRecordStartTime": "2024-01-01T10:00:00",
        "systemSoftRecordEndTime": "2024-01-01T10:05:00",
        "systemSoftRecordRemark": "安装成功"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

## WebSocket实时通信

### 连接地址

`ws://localhost:8080/socket.io`

### 消息类型

#### 1. Docker操作开始

**消息类型：** `docker_start`

**消息格式：**

```json
{
  "type": "docker_start",
  "operation": "pull",
  "imageName": "nginx:1.21.0",
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 2. Docker操作进度

**消息类型：** `docker_progress`

**消息格式：**

```json
{
  "type": "docker_progress",
  "operation": "pull",
  "imageName": "nginx:1.21.0",
  "status": "Downloading",
  "progress": "[==>                ] 25%",
  "id": "layer_id_123",
  "timestamp": "2024-01-01T10:01:00"
}
```

#### 3. Docker操作完成

**消息类型：** `docker_complete`

**消息格式：**

```json
{
  "type": "docker_complete",
  "operation": "pull",
  "imageName": "nginx:1.21.0",
  "success": true,
  "timestamp": "2024-01-01T10:05:00"
}
```

#### 4. Docker操作错误

**消息类型：** `docker_error`

**消息格式：**

```json
{
  "type": "docker_error",
  "operation": "pull",
  "imageName": "nginx:1.21.0",
  "error": "网络连接超时",
  "timestamp": "2024-01-01T10:03:00"
}
```

## 状态枚举

### SystemSoftRecordStatus

安装记录状态枚举

| 状态值       | 说明  |
|-----------|-----|
| PENDING   | 等待中 |
| RUNNING   | 执行中 |
| SUCCESS   | 成功  |
| FAILED    | 失败  |
| CANCELLED | 已取消 |

## 错误码说明

| 错误码  | 说明            |
|------|---------------|
| 200  | 操作成功          |
| 400  | 请求参数错误        |
| 404  | 资源不存在         |
| 500  | 服务器内部错误       |
| 1001 | Docker客户端连接失败 |
| 1002 | 镜像拉取失败        |
| 1003 | 容器启动失败        |

## 使用示例

### 前端JavaScript示例

```javascript
// 连接WebSocket
const socket = io('ws://localhost:8080');

// 监听Docker操作进度
socket.on('docker_progress', (data) => {
  console.log('Docker操作进度:', data);
  // 更新进度条
  updateProgressBar(data.progress);
});

// 监听Docker操作完成
socket.on('docker_complete', (data) => {
  console.log('Docker操作完成:', data);
  if (data.success) {
    showSuccessMessage('操作完成');
  } else {
    showErrorMessage('操作失败');
  }
});

// 拉取镜像
function pullImage(softId) {
  fetch(`/api/system/soft/pull/${softId}`, {
    method: 'POST'
  })
  .then(response => response.json())
  .then(data => {
    if (data.code === 200) {
      console.log('开始拉取镜像');
    }
  });
}
```

## 注意事项

1. **网络连接**：确保服务器能够访问Docker Hub或私有镜像仓库
2. **磁盘空间**：拉取镜像前检查服务器磁盘空间是否充足
3. **权限管理**：确保应用有足够的权限操作Docker
4. **并发控制**：避免同时对同一镜像进行多次操作
5. **错误处理**：合理处理网络超时、镜像不存在等异常情况
6. **日志记录**：详细记录操作日志便于问题排查

## 扩展功能

### 1. 镜像版本管理

- 支持多版本镜像管理
- 版本升级和回滚功能
- 版本比较和差异分析

### 2. 容器编排

- 支持Docker Compose
- 多容器应用部署
- 服务依赖管理

### 3. 监控告警

- 容器运行状态监控
- 资源使用情况监控
- 异常情况自动告警

### 4. 安全管理

- 镜像安全扫描
- 访问权限控制
- 操作审计日志