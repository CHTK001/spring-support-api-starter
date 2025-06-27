# Guacamole 远程桌面组件

基于 `guacamole-common-js` 重新实现的 RDP 和 VNC 远程桌面组件，提供更好的用户体验和功能。

## 组件结构

```
remote/
├── RemoteDesktop.vue          # 通用远程桌面组件（协议选择）
├── RDPDesktopNew.vue         # RDP 远程桌面组件
├── VNCDesktopNew.vue         # VNC 远程桌面组件
├── RDPDesktop.vue            # 原有 RDP 组件（保留）
├── VNCDesktop.vue            # 原有 VNC 组件（保留）
└── SSHTerminal.vue           # SSH 终端组件
```

## 核心工具类

```
utils/guacamole/
├── index.ts                  # GuacamoleClient 主类
├── types.ts                  # 类型定义
├── connection-manager.ts     # 连接管理器
└── README.md                # 工具类文档
```

## 使用方法

### 1. 通用远程桌面组件

```vue
<template>
  <RemoteDesktop
    :server="serverInfo"
    :auto-select-protocol="true"
    default-protocol="rdp"
    @close="handleClose"
    @protocol-change="handleProtocolChange"
  />
</template>

<script setup>
import RemoteDesktop from './components/remote/RemoteDesktop.vue';

const serverInfo = {
  monitorSysGenServerId: 1,
  monitorSysGenServerName: '测试服务器',
  monitorSysGenServerHost: '192.168.1.100',
  monitorSysGenServerPort: 3389,
  monitorSysGenServerProtocol: 'RDP',
  monitorSysGenServerUsername: 'administrator',
  monitorSysGenServerPassword: 'password'
};

const handleClose = () => {
  console.log('远程桌面已关闭');
};

const handleProtocolChange = (protocol) => {
  console.log('协议已切换:', protocol);
};
</script>
```

### 2. 直接使用 RDP 组件

```vue
<template>
  <RDPDesktopNew
    :server="serverInfo"
    @close="handleClose"
  />
</template>

<script setup>
import RDPDesktopNew from './components/remote/RDPDesktopNew.vue';
</script>
```

### 3. 直接使用 VNC 组件

```vue
<template>
  <VNCDesktopNew
    :server="serverInfo"
    @close="handleClose"
  />
</template>

<script setup>
import VNCDesktopNew from './components/remote/VNCDesktopNew.vue';
</script>
```

### 4. 使用 Composable

```vue
<script setup>
import { useGuacamoleConnection } from '@/composables/useGuacamoleConnection';

const {
  connectionState,
  connectionStats,
  displayInfo,
  isConnected,
  isConnecting,
  connect,
  disconnect,
  reconnect,
  sendClipboard,
  sendFile,
  takeScreenshot,
  setScale,
  createRDPConfig,
  createVNCConfig,
  formatDuration,
  formatBytes
} = useGuacamoleConnection({
  autoReconnect: true,
  reconnectInterval: 5000,
  maxReconnectAttempts: 3,
  connectionTimeout: 30000,
  showMessages: true
});

// 连接 RDP
const connectRDP = async () => {
  const config = createRDPConfig(serverInfo, {
    resolution: '1920x1080',
    colorDepth: 32,
    enableAudio: true,
    enableClipboard: true,
    security: 'any',
    ignoreCert: true
  });
  
  await connect(config);
};

// 连接 VNC
const connectVNC = async () => {
  const config = createVNCConfig(serverInfo, {
    colorDepth: 24,
    enableClipboard: true,
    enableCursor: true,
    readOnly: false,
    encodings: ['tight', 'hextile', 'zlib']
  });
  
  await connect(config);
};
</script>
```

## 功能特性

### RDP 组件特性

- ✅ 支持多种分辨率配置
- ✅ 支持 16/24/32 位颜色深度
- ✅ 支持音频传输
- ✅ 支持剪贴板同步
- ✅ 支持文件传输
- ✅ 支持多种安全模式（RDP/NLA/TLS/自动）
- ✅ 支持远程应用程序
- ✅ 支持证书忽略
- ✅ 支持全屏模式
- ✅ 支持缩放控制
- ✅ 支持截图功能
- ✅ 支持快捷键（Ctrl+Alt+Del、Windows键）

### VNC 组件特性

- ✅ 支持多种颜色深度（8/16/24/32位）
- ✅ 支持本地/远程光标
- ✅ 支持剪贴板同步
- ✅ 支持文件传输
- ✅ 支持只读模式
- ✅ 支持红蓝颜色交换
- ✅ 支持多种编码方式（Raw/RRE/CoRRE/Hextile/Zlib/Tight）
- ✅ 支持自动重试
- ✅ 支持全屏模式
- ✅ 支持缩放控制
- ✅ 支持截图功能
- ✅ 支持屏幕刷新

### 通用特性

- ✅ 实时连接状态监控
- ✅ 连接统计信息（延迟、流量、帧率等）
- ✅ 自动重连机制
- ✅ 错误处理和提示
- ✅ 响应式设计
- ✅ 移动端适配
- ✅ 键盘和鼠标事件处理
- ✅ WebSocket 连接管理
- ✅ 连接超时处理

## 配置选项

### RDP 配置

```typescript
interface RDPConnectionConfig {
  serverId: number;
  protocol: 'rdp';
  host: string;
  port: number;
  username?: string;
  password?: string;
  resolution?: string;           // 分辨率
  colorDepth?: number;          // 颜色深度
  enableAudio?: boolean;        // 启用音频
  enableClipboard?: boolean;    // 启用剪贴板
  enableDrive?: boolean;        // 启用驱动器重定向
  enablePrinting?: boolean;     // 启用打印机重定向
  security?: string;            // 安全模式
  ignoreCert?: boolean;         // 忽略证书
  disableAuth?: boolean;        // 禁用认证
  remoteApp?: string;           // 远程应用程序
  remoteAppDir?: string;        // 远程应用程序目录
  remoteAppArgs?: string;       // 远程应用程序参数
}
```

### VNC 配置

```typescript
interface VNCConnectionConfig {
  serverId: number;
  protocol: 'vnc';
  host: string;
  port: number;
  username?: string;
  password?: string;
  enableClipboard?: boolean;    // 启用剪贴板
  enableCursor?: boolean;       // 启用光标
  readOnly?: boolean;           // 只读模式
  swapRedBlue?: boolean;        // 交换红蓝颜色
  colorDepth?: number;          // 颜色深度
  cursor?: string;              // 光标类型
  encodings?: string[];         // 编码类型
  autoRetry?: boolean;          // 自动重试
}
```

## 事件处理

### 连接事件

```typescript
// 连接状态变化
connectionState.value?.state // 连接状态码
connectionState.value?.connected // 是否已连接
connectionState.value?.connecting // 是否正在连接
connectionState.value?.error // 错误信息

// 统计信息
connectionStats.value?.latency // 延迟
connectionStats.value?.bytesReceived // 接收字节数
connectionStats.value?.bytesSent // 发送字节数
connectionStats.value?.frameRate // 帧率

// 显示信息
displayInfo.value?.width // 显示宽度
displayInfo.value?.height // 显示高度
displayInfo.value?.scale // 缩放比例
```

### 用户交互

```typescript
// 发送剪贴板内容
sendClipboard('Hello World');

// 发送文件
const file = new File(['content'], 'test.txt', { type: 'text/plain' });
sendFile(file);

// 截图
const screenshot = takeScreenshot(); // 返回 base64 数据URL

// 设置缩放
setScale(1.5); // 150% 缩放
```

## 样式定制

组件使用 SCSS 编写样式，支持主题定制：

```scss
.rdp-desktop-new {
  --primary-color: #409eff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;
}
```

## 浏览器兼容性

- ✅ Chrome 80+
- ✅ Firefox 75+
- ✅ Safari 13+
- ✅ Edge 80+

## 注意事项

1. **WebSocket 连接**：确保后端 WebSocket 服务正常运行
2. **网络延迟**：高延迟网络环境下可能影响用户体验
3. **安全性**：生产环境建议使用 HTTPS/WSS 协议
4. **性能**：大分辨率和高颜色深度会增加带宽消耗
5. **移动端**：触摸设备上的鼠标操作可能需要特殊处理

## 故障排除

### 常见问题

1. **连接失败**
   - 检查服务器地址和端口
   - 确认用户名密码正确
   - 检查网络连接

2. **显示异常**
   - 尝试调整分辨率和颜色深度
   - 检查浏览器兼容性
   - 清除浏览器缓存

3. **性能问题**
   - 降低分辨率和颜色深度
   - 关闭不必要的功能（音频、剪贴板等）
   - 检查网络带宽

### 调试模式

```typescript
// 启用调试日志
const connection = useGuacamoleConnection({
  showMessages: true // 显示连接消息
});

// 监听连接状态
watch(connectionState, (state) => {
  console.log('连接状态变化:', state);
});

// 监听统计信息
watch(connectionStats, (stats) => {
  console.log('统计信息:', stats);
});
```
