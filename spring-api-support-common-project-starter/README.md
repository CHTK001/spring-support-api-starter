# spring-api-support-common-project-starter

## 模块说明

**项目管理模块** - 提供统一的项目管理功能，支持 AI 项目、邮件项目、短信项目、设备项目等多种项目类型。

## 设计理念

### 统一的项目管理模式

每个项目类型都包含以下核心要素：

- **配置管理** - AK/SK、API 密钥、服务器配置
- **模板管理** - 提示词模板、邮件模板、短信模板
- **日志记录** - 任务日志、发送日志、事件日志
- **权限控制** - 项目级别的权限管理

这种设计的优势：

- ✅ 统一的管理界面和操作流程
- ✅ 便于扩展新的项目类型
- ✅ 统一的权限控制和审计
- ✅ 配置和数据隔离

## 功能列表

### 1. AI 项目管理

#### AI 分组

- AI 对话分组管理
- 分组消息记录
- 对话历史

#### AI 模型配置

- 多种 AI 模型支持（OpenAI、智谱、讯飞等）
- 模型参数配置
- AK/SK 管理

#### 文生图功能

- 图片生成任务
- 风格模板管理
- 任务历史记录

### 2. 邮件项目管理

#### 邮件配置

- SMTP 服务器配置
- 发件人设置
- 邮箱账号管理

#### 邮件模板

- 模板 CRUD
- 变量占位符
- 模板预览

#### 发送日志

- 发送记录
- 发送状态追踪
- 失败重试

### 3. 短信项目管理

#### 短信配置

- 运营商配置（阿里云、腾讯云等）
- AK/SK 管理
- 签名管理

#### 短信模板

- 模板 CRUD
- 模板审核状态
- 变量管理

#### 发送日志

- 发送记录
- 发送状态
- 费用统计

### 4. 设备项目管理

#### 设备管理

- 设备注册
- 设备状态监控
- 设备分组

#### 通道管理

- 设备通道配置
- 视频流管理
- 通道状态

#### 事件记录

- 设备事件
- 告警记录
- 事件推送

## 使用方式

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.chua</groupId>
    <artifactId>spring-api-support-common-project-starter</artifactId>
</dependency>
```

### 2. 配置说明

#### AI 项目配置

```yaml
# AI模型配置
ai:
  models:
    openai:
      api-key: your-openai-key
      base-url: https://api.openai.com/v1
    zhipu:
      api-key: your-zhipu-key
    xunfei:
      app-id: your-app-id
      api-key: your-api-key
      api-secret: your-api-secret
```

#### 邮件项目配置

```yaml
# 邮件配置
mail:
  default:
    host: smtp.example.com
    port: 465
    username: noreply@example.com
    password: your-password
    protocol: smtps
    properties:
      mail.smtp.auth: true
      mail.smtp.ssl.enable: true
```

#### 短信项目配置

```yaml
# 短信配置
sms:
  provider: aliyun # 运营商：aliyun, tencent
  aliyun:
    access-key: your-access-key
    secret-key: your-secret-key
    sign-name: your-sign-name
  tencent:
    secret-id: your-secret-id
    secret-key: your-secret-key
    app-id: your-app-id
```

#### 设备项目配置

```yaml
# 设备配置
device:
  protocol: gb28181 # 协议类型
  sip:
    domain: your-domain
    id: your-sip-id
    password: your-password
```

## API 接口

### AI 项目接口

```
# AI分组
GET    /v2/ai/group/list          # 分组列表
POST   /v2/ai/group               # 创建分组
DELETE /v2/ai/group/{id}          # 删除分组

# AI对话
POST   /v2/ai/chat                # 发送消息
GET    /v2/ai/history/{groupId}   # 对话历史

# 文生图
POST   /v2/ai/image/generate      # 生成图片
GET    /v2/ai/image/task/{id}     # 任务状态
```

### 邮件项目接口

```
# 邮件模板
GET    /v2/email/template/list    # 模板列表
POST   /v2/email/template         # 创建模板
PUT    /v2/email/template         # 更新模板

# 邮件发送
POST   /v2/email/send             # 发送邮件
GET    /v2/email/log/list         # 发送日志
```

### 短信项目接口

```
# 短信模板
GET    /v2/sms/template/list      # 模板列表
POST   /v2/sms/template           # 创建模板

# 短信发送
POST   /v2/sms/send               # 发送短信
GET    /v2/sms/log/list           # 发送日志
```

### 设备项目接口

```
# 设备管理
GET    /v2/device/list            # 设备列表
POST   /v2/device/register        # 设备注册
PUT    /v2/device/status          # 更新状态

# 通道管理
GET    /v2/device/channel/list    # 通道列表
POST   /v2/device/channel/start   # 启动通道
POST   /v2/device/channel/stop    # 停止通道
```

## 使用示例

### AI 项目使用

```java
@Service
@RequiredArgsConstructor
public class AiService {

    private final SysAiGroupService aiGroupService;
    private final BigModelService bigModelService;

    public String chat(String groupId, String message) {
        // 发送消息到AI模型
        return bigModelService.chat(groupId, message);
    }
}
```

### 邮件项目使用

```java
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SysEmailTemplateService templateService;
    private final SysEmailService emailService;

    public void sendEmail(String to, String templateCode, Map<String, Object> params) {
        // 使用模板发送邮件
        emailService.sendByTemplate(to, templateCode, params);
    }
}
```

### 短信项目使用

```java
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SysEmailTemplateService templateService;

    public void sendSms(String mobile, String templateCode, Map<String, Object> params) {
        // 使用模板发送短信
        smsService.sendByTemplate(mobile, templateCode, params);
    }
}
```

### 设备项目使用

```java
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final SysDeviceService deviceService;
    private final SysDeviceChannelService channelService;

    public void startVideo(String deviceId, String channelId) {
        // 启动视频流
        channelService.startStream(deviceId, channelId);
    }
}
```

## 项目管理最佳实践

### 1. 配置隔离

每个项目维护独立的配置：

```java
// AI项目配置
SysAiModule aiModule = new SysAiModule();
aiModule.setApiKey("your-api-key");
aiModule.setModel("gpt-4");

// 邮件项目配置
SysEmailTemplate template = new SysEmailTemplate();
template.setSmtpHost("smtp.example.com");
template.setSmtpPort(465);
```

### 2. 权限控制

项目级别的权限控制：

```java
@PreAuthorize("hasProjectPermission(#projectId, 'AI:CHAT')")
public String chat(String projectId, String message) {
    // AI对话
}
```

### 3. 日志审计

所有项目操作都有日志记录：

```java
// 自动记录邮件发送日志
SysEmailLog log = new SysEmailLog();
log.setRecipient(to);
log.setStatus("SUCCESS");
emailLogService.save(log);
```

## 注意事项

1. **AK/SK 安全** - 敏感信息加密存储
2. **模板审核** - 短信模板需要运营商审核
3. **费用控制** - 短信、AI 调用需要费用控制
4. **并发限制** - AI、邮件、短信都需要限流

## 相关模块

- `spring-api-support-common-service-starter` - 通用服务
- `spring-api-support-common-entity-starter` - 实体定义
- `utils-support-zhipu-starter` - 智谱 AI
- `utils-support-xunfei-starter` - 讯飞 AI
- `utils-support-email-starter` - 邮件发送
- `spring-support-tencent-starter` - 腾讯云服务
