# 服务器管理功能示例

## 概述

本文档展示了新增的服务器管理功能，包括：
- 自动检测本地服务器
- 多IP地址支持
- Docker支持检测
- 操作系统信息自动获取

## 新增字段说明

### 服务器实体新增字段

| 字段名 | 类型 | 说明 | 自动检测 |
|--------|------|------|----------|
| `monitorSysGenServerIsLocal` | Integer | 是否本地服务器 (0:否 1:是) | ✅ |
| `monitorSysGenServerIpAddresses` | String | IP地址列表 (JSON格式) | ✅ |
| `monitorSysGenServerDockerEnabled` | Integer | 是否支持Docker (0:否 1:是) | ✅ |
| `monitorSysGenServerDockerConnectionType` | String | Docker连接方式 (SHELL/TCP) | ✅ |
| `monitorSysGenServerDockerHost` | String | Docker TCP连接地址 | ❌ |
| `monitorSysGenServerDockerPort` | Integer | Docker TCP连接端口 | ✅ |
| `monitorSysGenServerOsType` | String | 操作系统类型 | ✅ |
| `monitorSysGenServerOsVersion` | String | 操作系统版本 | ✅ |
| `monitorSysGenServerOsArch` | String | 操作系统架构 | ✅ |

## API使用示例

### 1. 创建服务器配置

```json
POST /v1/gen/server/save
{
    "monitorSysGenServerName": "本地开发服务器",
    "monitorSysGenServerHost": "localhost",
    "monitorSysGenServerPort": 22,
    "monitorSysGenServerProtocol": "SSH",
    "monitorSysGenServerUsername": "admin",
    "monitorSysGenServerPassword": "password123"
}
```

**响应示例（自动检测后）：**
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "monitorSysGenServerId": 1,
        "monitorSysGenServerName": "本地开发服务器",
        "monitorSysGenServerHost": "localhost",
        "monitorSysGenServerIsLocal": 1,
        "monitorSysGenServerIpAddresses": "[\"192.168.1.100\",\"10.0.0.1\"]",
        "monitorSysGenServerDockerEnabled": 1,
        "monitorSysGenServerDockerConnectionType": "SHELL",
        "monitorSysGenServerDockerPort": 2375,
        "monitorSysGenServerOsType": "WINDOWS",
        "monitorSysGenServerOsVersion": "10.0",
        "monitorSysGenServerOsArch": "X86_64"
    }
}
```

### 2. 手动检测服务器信息

```http
POST /v1/gen/server/detect-info?id=1
```

**响应示例：**
```json
{
    "code": 200,
    "msg": "服务器信息检测完成",
    "data": {
        "serverId": 1,
        "serverName": "本地开发服务器",
        "host": "localhost",
        "isLocal": 1,
        "ipAddresses": "[\"192.168.1.100\",\"10.0.0.1\"]",
        "osType": "WINDOWS",
        "osVersion": "10.0",
        "osArch": "X86_64",
        "dockerEnabled": 1,
        "dockerConnectionType": "SHELL",
        "dockerPort": 2375,
        "detectedAt": "2024-12-24T10:30:00"
    }
}
```

### 3. 批量检测服务器信息

```http
POST /v1/gen/server/batch-detect-info
```

**响应示例：**
```json
{
    "code": 200,
    "msg": "批量检测完成",
    "data": {
        "totalCount": 3,
        "successCount": 2,
        "failureCount": 1,
        "successList": [
            {
                "serverId": 1,
                "serverName": "本地开发服务器",
                "host": "localhost",
                "isLocal": 1,
                "osType": "WINDOWS",
                "dockerEnabled": 1
            },
            {
                "serverId": 2,
                "serverName": "远程生产服务器",
                "host": "192.168.1.200",
                "isLocal": 0,
                "osType": null,
                "dockerEnabled": 0
            }
        ],
        "failureList": [
            {
                "serverId": 3,
                "serverName": "测试服务器",
                "host": "invalid-host",
                "error": "主机地址无效"
            }
        ],
        "detectedAt": "2024-12-24T10:35:00"
    }
}
```

## 自动检测规则

### 本地服务器检测
- `localhost`
- `127.0.0.1`
- `0.0.0.0`
- `::1`
- 本机网卡IP地址

### 操作系统类型映射
- Windows → `WINDOWS`
- Linux → `LINUX`
- macOS/Darwin → `MACOS`
- Unix → `UNIX`
- Solaris/SunOS → `SOLARIS`
- 其他 → `UNKNOWN`

### 架构类型映射
- amd64/x86_64 → `X86_64`
- x86/i386/i686 → `X86`
- arm64/aarch64 → `ARM64`
- arm → `ARM`

### Docker连接方式推荐
- Windows → `TCP`
- Linux/macOS → `SHELL`

## 注意事项

1. **本地服务器标识不可修改**：`monitorSysGenServerIsLocal` 字段由系统自动检测，不允许手动修改。

2. **IP地址自动更新**：本地服务器的IP地址列表会在每次主机地址变更时自动更新。

3. **Docker检测**：Docker支持检测基于 `docker --version` 命令的执行结果。

4. **操作系统信息**：只有本地服务器才会自动检测操作系统信息，远程服务器需要通过连接后获取。

5. **性能考虑**：批量检测可能耗时较长，建议在后台异步执行。

## 数据库迁移

执行以下SQL脚本添加新字段：

```sql
-- 见 src/main/resources/db/migration/V1.0.4__Add_Server_Management_Fields.sql
```

## 前端集成建议

1. **服务器列表页面**：显示本地/远程标识、操作系统信息、Docker支持状态
2. **服务器详情页面**：展示完整的检测信息，包括多个IP地址
3. **批量操作**：提供批量检测功能，显示进度和结果
4. **实时更新**：通过WebSocket实时更新检测状态
