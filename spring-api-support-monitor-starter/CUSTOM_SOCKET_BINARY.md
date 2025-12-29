# CustomSocketIOListener 二进制数据功能说明

## 概述

`CustomSocketIOListener` 现已支持二进制数据的双向传输，可用于：
- 从远程服务器读取文件
- 获取屏幕截图
- 执行自定义二进制命令
- 主动推送二进制数据到客户端

## 事件主题

### 1. 二进制数据请求 (remote:custom:binary_request)

客户端向服务器请求二进制数据。

### 2. 二进制数据响应 (remote:custom:binary_data)

服务器向客户端发送二进制数据（自动响应或主动推送）。

## 客户端使用示例

### 请求文件下载

```javascript
// Socket.IO 客户端
const socket = io('http://localhost:9090/socket.io');

// 请求下载文件
socket.emit('remote:custom:binary_request', {
    serverId: 1,
    protocol: 'SSH',  // 或 'CUSTOM'
    requestType: 'file',
    filePath: '/path/to/remote/file.txt'
});

// 接收二进制数据
socket.on('remote:custom:binary_data', (binaryData) => {
    console.log('接收到文件数据:', binaryData.byteLength, 'bytes');
    
    // 将二进制数据转换为 Blob 并下载
    const blob = new Blob([binaryData]);
    const url = URL.createObjectURL(blob);
    
    const a = document.createElement('a');
    a.href = url;
    a.download = 'downloaded-file.txt';
    a.click();
    
    URL.revokeObjectURL(url);
});

// 处理错误
socket.on('remote:custom:error', (error) => {
    console.error('错误:', error);
});
```

### 请求屏幕截图

```javascript
// 请求屏幕截图
socket.emit('remote:custom:binary_request', {
    serverId: 1,
    protocol: 'SSH',
    requestType: 'screenshot'
});

// 接收截图
socket.on('remote:custom:binary_data', (binaryData) => {
    // 显示截图
    const blob = new Blob([binaryData], { type: 'image/jpeg' });
    const imageUrl = URL.createObjectURL(blob);
    
    const img = document.getElementById('screenshot');
    img.src = imageUrl;
});
```

### 执行自定义二进制命令

```javascript
// 执行命令并获取二进制输出
socket.emit('remote:custom:binary_request', {
    serverId: 1,
    protocol: 'SSH',
    requestType: 'custom',
    command: 'cat /var/log/syslog | gzip'  // 返回压缩的日志
});

// 接收压缩数据
socket.on('remote:custom:binary_data', (binaryData) => {
    console.log('接收到压缩数据:', binaryData.byteLength, 'bytes');
    // 解压缩数据...
});
```

## 服务端扩展

### 实现自定义连接服务

要支持二进制数据功能，需要实现 `ServerConnectionService` 接口中的以下方法：

```java
@Service
public class CustomServerConnectionServiceImpl implements ServerConnectionService {
    
    @Override
    public String getProtocol() {
        return "CUSTOM";
    }
    
    /**
     * 读取远程文件（覆盖默认实现）
     */
    @Override
    public byte[] readFile(Integer serverId, String filePath) {
        try {
            // 实现文件读取逻辑
            // 例如: 通过SSH/SFTP读取文件
            SftpClient sftp = getSftpClient(serverId);
            return sftp.readFileBytes(filePath);
        } catch (Exception e) {
            log.error("读取文件失败: serverId={}, path={}", serverId, filePath, e);
            return null;
        }
    }
    
    /**
     * 捕获屏幕截图（覆盖默认实现）
     */
    @Override
    public byte[] captureScreen(Integer serverId) {
        try {
            // 实现屏幕捕获逻辑
            // 例如: 使用VNC或RDP协议捕获屏幕
            RemoteDesktopClient rdp = getRemoteDesktopClient(serverId);
            BufferedImage screenshot = rdp.captureScreen();
            
            // 转换为JPEG字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenshot, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("捕获屏幕失败: serverId={}", serverId, e);
            return null;
        }
    }
    
    /**
     * 执行二进制命令（可选覆盖）
     */
    @Override
    public byte[] executeBinaryCommand(Integer serverId, String command) {
        try {
            // 自定义实现
            // 默认实现会将文本结果转换为字节数组
            SshClient ssh = getSshClient(serverId);
            byte[] result = ssh.executeBinaryCommand(command);
            return result;
        } catch (Exception e) {
            log.error("执行二进制命令失败: serverId={}, command={}", serverId, command, e);
            return null;
        }
    }
    
    // 其他必需的接口方法实现...
}
```

### 主动推送二进制数据

```java
@Service
public class BinaryDataPushService {
    
    @Autowired
    private CustomSocketIOListener customSocketIOListener;
    
    /**
     * 向指定服务器ID的客户端推送二进制数据
     */
    public void pushFileToClient(Integer serverId, byte[] fileData) {
        customSocketIOListener.pushBinaryData(serverId, fileData);
    }
    
    /**
     * 广播二进制数据到所有连接的客户端
     */
    public void broadcastUpdate(byte[] updateData) {
        customSocketIOListener.broadcastBinaryData(updateData);
    }
    
    /**
     * 示例: 定期推送服务器截图
     */
    @Scheduled(fixedRate = 5000)
    public void pushPeriodicScreenshot() {
        for (Integer serverId : getMonitoredServerIds()) {
            try {
                byte[] screenshot = captureServerScreen(serverId);
                if (screenshot != null) {
                    customSocketIOListener.pushBinaryData(serverId, screenshot);
                }
            } catch (Exception e) {
                log.error("推送截图失败: serverId={}", serverId, e);
            }
        }
    }
}
```

## 功能特性

### 1. 自动错误处理
- ✅ 参数验证（serverId, requestType, filePath等）
- ✅ 连接状态检查
- ✅ 功能支持检测（如果连接服务不支持某功能会返回null）
- ✅ 异常捕获和日志记录

### 2. 请求类型支持

| 请求类型 | 说明 | 必需参数 | 返回数据 |
|---------|------|---------|---------|
| `file` | 读取远程文件 | `filePath` | 文件二进制内容 |
| `screenshot` | 屏幕截图 | 无 | 图片二进制(JPEG/PNG) |
| `custom` | 自定义命令 | `command` | 命令输出的二进制 |

### 3. 主动推送功能
- 向指定客户端推送: `pushBinaryData(serverId, data)`
- 广播到所有客户端: `broadcastBinaryData(data)`

### 4. 日志级别
- `DEBUG`: 详细的请求和响应信息
- `INFO`: 成功的操作和统计信息
- `WARN`: 不支持的功能或参数错误
- `ERROR`: 异常和失败情况

## 性能建议

### 1. 文件大小限制

建议在服务端限制文件大小：

```java
@Override
public byte[] readFile(Integer serverId, String filePath) {
    try {
        File file = new File(filePath);
        long fileSize = file.length();
        
        // 限制文件大小（例如: 10MB）
        if (fileSize > 10 * 1024 * 1024) {
            log.warn("文件过大: serverId={}, size={}", serverId, fileSize);
            return null;
        }
        
        return Files.readAllBytes(file.toPath());
    } catch (Exception e) {
        log.error("读取文件失败: {}", e.getMessage(), e);
        return null;
    }
}
```

### 2. 数据压缩

对于大型数据，建议压缩后传输：

```java
public byte[] compressData(byte[] data) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
        gzip.write(data);
    }
    return baos.toByteArray();
}

@Override
public byte[] readFile(Integer serverId, String filePath) {
    try {
        byte[] fileData = Files.readAllBytes(Paths.get(filePath));
        // 压缩后返回
        return compressData(fileData);
    } catch (Exception e) {
        log.error("读取文件失败: {}", e.getMessage(), e);
        return null;
    }
}
```

客户端解压：

```javascript
// 使用 pako 库解压 gzip
import pako from 'pako';

socket.on('remote:custom:binary_data', (compressedData) => {
    try {
        // 解压数据
        const decompressed = pako.ungzip(new Uint8Array(compressedData));
        console.log('解压后大小:', decompressed.byteLength);
    } catch (e) {
        console.error('解压失败:', e);
    }
});
```

### 3. 分片传输

对于超大文件，建议分片传输（在服务端实现）：

```java
public void sendLargeFileTClient(SocketSession session, Integer serverId, String filePath) {
    try {
        byte[] fileData = Files.readAllBytes(Paths.get(filePath));
        int chunkSize = 64 * 1024; // 64KB per chunk
        int totalChunks = (int) Math.ceil((double) fileData.length / chunkSize);
        
        for (int i = 0; i < totalChunks; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, fileData.length);
            byte[] chunk = Arrays.copyOfRange(fileData, start, end);
            
            // 先发送分片元数据
            session.send("file:chunk:meta", Map.of(
                "chunkIndex", i,
                "totalChunks", totalChunks,
                "chunkSize", chunk.length,
                "fileName", new File(filePath).getName()
            ));
            
            // 发送分片数据
            session.sendBinary("file:chunk:data", chunk);
            
            // 避免发送过快
            Thread.sleep(10);
        }
    } catch (Exception e) {
        log.error("发送大文件失败: {}", e.getMessage(), e);
    }
}
```

## 安全注意事项

### 1. 路径遍历防护

```java
@Override
public byte[] readFile(Integer serverId, String filePath) {
    try {
        // 防止路径遍历攻击
        Path normalizedPath = Paths.get(filePath).normalize();
        Path basePath = Paths.get("/safe/base/directory");
        
        if (!normalizedPath.startsWith(basePath)) {
            log.warn("非法文件路径: serverId={}, path={}", serverId, filePath);
            return null;
        }
        
        return Files.readAllBytes(normalizedPath);
    } catch (Exception e) {
        log.error("读取文件失败: {}", e.getMessage(), e);
        return null;
    }
}
```

### 2. 权限检查

```java
@Override
public byte[] readFile(Integer serverId, String filePath) {
    // 检查用户权限
    if (!hasReadPermission(serverId, filePath)) {
        log.warn("无权限读取文件: serverId={}, path={}", serverId, filePath);
        return null;
    }
    
    // 读取文件...
}
```

### 3. 命令白名单

```java
private static final Set<String> ALLOWED_COMMANDS = Set.of(
    "cat", "ls", "pwd", "date", "hostname"
);

@Override
public byte[] executeBinaryCommand(Integer serverId, String command) {
    String[] parts = command.split("\\s+");
    String cmd = parts[0];
    
    if (!ALLOWED_COMMANDS.contains(cmd)) {
        log.warn("禁止执行命令: serverId={}, command={}", serverId, cmd);
        return null;
    }
    
    // 执行命令...
}
```

## 故障排查

### 问题1: 收到空数据

**可能原因:**
- 连接服务不支持该功能（方法返回null）
- 文件不存在或无权限
- 参数错误

**解决方案:**
检查服务端日志，确认是否有WARN级别的日志提示不支持或参数错误。

### 问题2: 数据传输失败

**可能原因:**
- 数据过大超过maxFrameSize限制
- 网络连接不稳定

**解决方案:**
1. 增加 `maxFrameSize` 配置
2. 使用分片传输
3. 添加重试机制

### 问题3: 客户端无法接收数据

**可能原因:**
- 事件名称不匹配
- 客户端未正确处理二进制数据

**解决方案:**
确保客户端监听 `remote:custom:binary_data` 事件，并正确处理 ArrayBuffer 类型数据。

## 总结

CustomSocketIOListener 的二进制数据功能提供了灵活、安全的数据传输方案。通过实现 `ServerConnectionService` 接口中的方法，可以轻松扩展支持各种远程操作场景。
