# 文件管理系统文档

## 概述

文件管理系统是一个统一的文件操作接口，支持SSH和API两种模式，可以根据服务器配置自动选择合适的实现方式。

## 系统架构

### 核心组件

1. **统一接口层** - `FileManagementService`
2. **实现层** - `SshFileManagementServiceImpl` 和 `ApiFileManagementServiceImpl`
3. **控制器层** - `FileManagementController`
4. **数据模型** - `FileInfo`, `FileOperationRequest`, `FileOperationResponse`
5. **配置管理** - `FileManagementMode`, `FileManagementApiConfig`

### 支持的文件管理模式

- **SSH模式** - 基于SFTP协议的文件操作
- **API模式** - 基于HTTP API的文件操作（需要客户端健康状态验证）
- **NONE模式** - 禁用文件管理功能

## 核心功能

### 基础文件操作

1. **文件列表** - `GET /api/file-management/list`
   - 获取指定路径下的文件列表
   - 支持隐藏文件显示、排序等选项

2. **文件树** - `GET /api/file-management/tree`
   - 获取目录树结构
   - 支持最大深度限制

3. **文件上传** - `POST /api/file-management/upload`
   - 支持文件上传到指定路径
   - 支持覆盖选项

4. **文件下载** - `GET /api/file-management/download`
   - 支持文件下载
   - 自动处理文件名编码

5. **文件删除** - `DELETE /api/file-management/delete`
   - 支持文件和目录删除
   - 支持递归删除

6. **文件重命名** - `PUT /api/file-management/rename`
   - 支持文件和目录重命名

7. **创建目录** - `POST /api/file-management/mkdir`
   - 支持目录创建
   - 支持递归创建

8. **连接测试** - `GET /api/file-management/test`
   - 测试文件管理连接状态

### 高级功能

1. **复杂操作** - `POST /api/file-management/execute`
   - 支持复杂的文件操作（仅API模式）
   - 基于 `FileOperationRequest` 的操作请求

## SSH模式实现

### 特性
- 基于JSch库的SFTP协议
- 支持SSH会话缓存（5分钟超时）
- 支持文件权限管理
- 支持递归目录操作

### 配置要求
- 服务器必须配置SSH连接信息
- 文件管理模式设置为 `SSH`

### 支持的操作
- 文件列表、文件树、上传、下载
- 文件删除、重命名、目录创建
- 权限查看、连接测试

## API模式实现

### 特性
- 基于RestTemplate的HTTP客户端
- 支持客户端健康状态验证
- 支持多种认证方式（NONE, BASIC, TOKEN, API_KEY）
- 支持配置缓存（5分钟超时）

### 配置要求
- 客户端必须处于健康状态
- 配置API端点和认证信息
- 文件管理模式设置为 `API`

### 支持的操作
- 所有SSH模式支持的操作
- 复杂文件操作（压缩、解压、搜索等）
- 文件预览、权限管理

## 数据模型

### FileInfo
```java
public class FileInfo {
    private String name;           // 文件名
    private String path;           // 文件路径
    private Long size;             // 文件大小
    private Boolean isDirectory;   // 是否为目录
    private String permissions;    // 文件权限
    private LocalDateTime modifyTime; // 修改时间
    private List<FileInfo> children; // 子文件（用于树结构）
}
```

### FileOperationRequest
```java
public class FileOperationRequest {
    private String operation;      // 操作类型
    private String path;           // 文件路径
    private String targetPath;     // 目标路径
    private String newName;        // 新名称
    private Boolean recursive;     // 是否递归
    private Boolean overwrite;     // 是否覆盖
    private Map<String, Object> parameters; // 额外参数
}
```

### FileOperationResponse
```java
public class FileOperationResponse {
    private Boolean success;       // 操作是否成功
    private String operation;      // 操作类型
    private String message;        // 响应消息
    private List<FileInfo> files;  // 文件列表
    private FileInfo fileTree;     // 文件树
    private String downloadUrl;    // 下载URL
    private Integer totalCount;    // 总数量
    private Integer processedCount; // 已处理数量
    private Integer failedCount;   // 失败数量
}
```

## 配置说明

### 服务器设置
在 `MonitorSysGenServerSetting` 表中配置：

```java
// 文件管理模式
private FileManagementMode monitorSysGenServerSettingFileManagementMode;

// API配置（JSON格式）
private String monitorSysGenServerSettingFileManagementApiConfig;

// 超时设置
private Integer monitorSysGenServerSettingFileManagementTimeout;
```

### API配置示例
```json
{
  "baseUrl": "http://client-server:8080",
  "authType": "BASIC",
  "username": "admin",
  "password": "password",
  "timeout": 30000,
  "retryCount": 3,
  "healthCheckUrl": "http://client-server:8080/health"
}
```

## 使用示例

### 获取文件列表
```bash
curl -X GET "http://localhost:8080/api/file-management/list?serverId=1&path=/home&includeHidden=false&sortBy=name&sortOrder=asc"
```

### 上传文件
```bash
curl -X POST "http://localhost:8080/api/file-management/upload" \
  -F "serverId=1" \
  -F "targetPath=/upload" \
  -F "file=@test.txt" \
  -F "overwrite=false"
```

### 下载文件
```bash
curl -X GET "http://localhost:8080/api/file-management/download?serverId=1&filePath=/home/test.txt" \
  --output downloaded_file.txt
```

## 错误处理

系统提供统一的错误处理机制：

1. **配置错误** - 服务器文件管理模式未配置
2. **连接错误** - SSH连接失败或API连接失败
3. **权限错误** - 文件操作权限不足
4. **文件错误** - 文件不存在或操作失败
5. **健康检查错误** - 客户端健康状态检查失败（API模式）

## 性能优化

1. **连接缓存** - SSH会话和API配置缓存
2. **批量操作** - 支持批量文件操作
3. **流式传输** - 大文件上传下载使用流式处理
4. **异步处理** - 复杂操作支持异步执行

## 安全考虑

1. **认证验证** - 支持多种认证方式
2. **路径验证** - 防止路径遍历攻击
3. **权限检查** - 文件操作权限验证
4. **健康检查** - API模式需要客户端健康状态验证
5. **连接加密** - SSH连接默认加密传输

## 扩展性

系统设计支持未来扩展：

1. **新的文件管理模式** - 可以添加新的实现方式
2. **自定义操作** - 支持自定义文件操作
3. **插件机制** - 支持文件操作插件
4. **监控集成** - 可以集成文件操作监控

## 测试

提供完整的单元测试覆盖：

- 控制器层测试
- 服务层测试
- 集成测试
- 错误场景测试

运行测试：
```bash
mvn test -Dtest=FileManagementControllerTest
```
