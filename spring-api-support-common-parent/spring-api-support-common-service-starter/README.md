# spring-api-support-common-service-starter

## 模块说明

**通用服务模块** - 提供字典、文件、消息、设置等基础通用服务，所有应用模块都可以使用。

## 功能列表

### 1. 字典服务

- 数据字典管理
- 字典项管理
- 字典项属性扩展
- 支持多级字典

### 2. 区划服务

- 省市区数据查询
- 银行卡 BIN 码查询
- 区划树形结构

### 3. 文件服务

- 文件上传（支持分片）
- 文件下载
- 文件分组管理
- 多种存储方式（本地、OSS、MinIO 等）

### 4. 消息服务

- 站内消息
- 消息推送（WebSocket）
- 消息历史记录

### 5. 设置服务

- 系统设置管理
- 设置分组
- 配置项动态加载

### 6. 节假日服务

- 节假日查询
- 工作日判断

### 7. 日志服务

- 操作日志记录
- 日志查询

## 使用方式

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-service-starter</artifactId>
</dependency>
```

### 2. 配置说明

#### 文件上传配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB # 单个文件最大大小
      max-request-size: 500MB # 请求最大大小
      enabled: true

# 文件存储配置
file:
  storage:
    type: local # 存储类型：local, oss, minio
    local:
      path: ./uploads # 本地存储路径
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key: your-access-key
      secret-key: your-secret-key
      bucket: your-bucket
```

#### 消息推送配置

```yaml
# Socket.IO配置（可选）
socketio:
  enabled: true
  port: 9092
  hostname: 0.0.0.0
```

## API 接口

### 字典管理

```
GET    /v2/dict/page              # 字典分页查询
POST   /v2/dict                   # 创建字典
PUT    /v2/dict                   # 更新字典
DELETE /v2/dict/{id}              # 删除字典
GET    /v2/dict/item/list         # 字典项列表
```

### 文件管理

```
POST   /v2/file/upload            # 文件上传
POST   /v2/file/upload/chunk      # 分片上传
GET    /v2/file/download/{id}     # 文件下载
DELETE /v2/file/{id}              # 删除文件
GET    /v2/file/list              # 文件列表
```

### 消息管理

```
GET    /v2/message/list           # 消息列表
POST   /v2/message/send           # 发送消息
PUT    /v2/message/read/{id}      # 标记已读
DELETE /v2/message/{id}           # 删除消息
```

### 设置管理

```
GET    /v2/setting/list           # 设置列表
POST   /v2/setting                # 创建设置
PUT    /v2/setting                # 更新设置
GET    /v2/setting/group          # 设置分组
```

## 使用示例

### 字典服务使用

```java
@RestController
@RequiredArgsConstructor
public class MyController {

    private final SysDictService sysDictService;

    @GetMapping("/dict/{code}")
    public ReturnResult<List<SysDictItem>> getDictItems(@PathVariable String code) {
        return sysDictService.getDictItemsByCode(code);
    }
}
```

### 文件上传使用

```java
@RestController
@RequiredArgsConstructor
public class FileController {

    private final SysFileSystemService fileSystemService;

    @PostMapping("/upload")
    public ReturnResult<String> upload(@RequestParam("file") MultipartFile file) {
        return fileSystemService.upload(file);
    }
}
```

### 消息推送使用

```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SysMessageService messageService;

    public void sendNotification(String userId, String content) {
        SysMessage message = new SysMessage();
        message.setSysMessageUserId(userId);
        message.setSysMessageContent(content);
        messageService.send(message);
    }
}
```

## 注意事项

1. **OAuth 客户端必需** - 本模块依赖 `spring-support-oauth-client-starter`
2. **Redis 必需** - 部分功能需要 Redis 支持
3. **文件存储** - 根据实际需求配置存储方式
4. **消息推送** - 需要 Socket.IO 支持（可选）

## 相关模块

- `spring-support-oauth-client-starter` - OAuth 客户端 SDK
- `spring-api-support-common-entity-starter` - 实体定义
- `spring-api-support-common-configuration-starter` - 配置定义
