/**
 * Guacamole 连接管理 Composable
 * 提供统一的连接管理、状态监控和错误处理
 */

import { ref, reactive, computed, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { guacamoleConnectionManager } from '@/utils/guacamole/connection-manager';
import type { 
  GuacamoleConnectionConfigType,
  GuacamoleConnectionState,
  GuacamoleStatistics,
  GuacamoleDisplayInfo,
  RDPConnectionConfig,
  VNCConnectionConfig
} from '@/utils/guacamole/types';

export interface UseGuacamoleConnectionOptions {
  /** 自动重连 */
  autoReconnect?: boolean;
  /** 重连间隔（毫秒） */
  reconnectInterval?: number;
  /** 最大重连次数 */
  maxReconnectAttempts?: number;
  /** 连接超时时间（毫秒） */
  connectionTimeout?: number;
  /** 是否显示连接消息 */
  showMessages?: boolean;
}

export function useGuacamoleConnection(options: UseGuacamoleConnectionOptions = {}) {
  const {
    autoReconnect = false,
    reconnectInterval = 5000,
    maxReconnectAttempts = 3,
    connectionTimeout = 30000,
    showMessages = true
  } = options;

  // 状态
  const currentServerId = ref<number | null>(null);
  const reconnectAttempts = ref(0);
  const reconnectTimer = ref<number | null>(null);
  const connectionTimer = ref<number | null>(null);

  // 连接配置
  const connectionConfig = ref<GuacamoleConnectionConfigType | null>(null);

  // 计算属性
  const connectionState = computed(() => {
    if (!currentServerId.value) return null;
    return guacamoleConnectionManager.getConnectionState(currentServerId.value);
  });

  const connectionStats = computed(() => {
    if (!currentServerId.value) return null;
    return guacamoleConnectionManager.getConnectionStats(currentServerId.value);
  });

  const displayInfo = computed(() => {
    if (!currentServerId.value) return null;
    return guacamoleConnectionManager.getDisplayInfo(currentServerId.value);
  });

  const isConnected = computed(() => {
    return connectionState.value?.connected || false;
  });

  const isConnecting = computed(() => {
    return connectionState.value?.connecting || false;
  });

  const hasError = computed(() => {
    return !!connectionState.value?.error;
  });

  // 方法
  const connect = async (config: GuacamoleConnectionConfigType): Promise<void> => {
    try {
      // 清除之前的连接
      if (currentServerId.value && currentServerId.value !== config.serverId) {
        await disconnect();
      }

      currentServerId.value = config.serverId;
      connectionConfig.value = config;
      reconnectAttempts.value = 0;

      // 设置连接超时
      if (connectionTimeout > 0) {
        connectionTimer.value = window.setTimeout(() => {
          if (isConnecting.value) {
            handleConnectionTimeout();
          }
        }, connectionTimeout);
      }

      // 创建连接
      const client = await guacamoleConnectionManager.createConnection(config);
      
      // 连接到服务器
      await guacamoleConnectionManager.connect(config.serverId);

      // 清除连接超时定时器
      if (connectionTimer.value) {
        clearTimeout(connectionTimer.value);
        connectionTimer.value = null;
      }

      if (showMessages) {
        ElMessage.success(`${config.protocol.toUpperCase()} 连接成功`);
      }

      return client;
    } catch (error) {
      // 清除连接超时定时器
      if (connectionTimer.value) {
        clearTimeout(connectionTimer.value);
        connectionTimer.value = null;
      }

      console.error('连接失败:', error);
      
      if (showMessages) {
        ElMessage.error(`连接失败: ${error}`);
      }

      // 如果启用自动重连，尝试重连
      if (autoReconnect && reconnectAttempts.value < maxReconnectAttempts) {
        scheduleReconnect();
      }

      throw error;
    }
  };

  const disconnect = async (): Promise<void> => {
    try {
      // 清除定时器
      clearTimers();

      if (currentServerId.value) {
        await guacamoleConnectionManager.disconnect(currentServerId.value);
        
        if (showMessages) {
          ElMessage.success('连接已断开');
        }
      }
    } catch (error) {
      console.error('断开连接失败:', error);
      
      if (showMessages) {
        ElMessage.error(`断开连接失败: ${error}`);
      }
    } finally {
      currentServerId.value = null;
      connectionConfig.value = null;
      reconnectAttempts.value = 0;
    }
  };

  const reconnect = async (): Promise<void> => {
    if (!connectionConfig.value) {
      throw new Error('没有可用的连接配置');
    }

    await disconnect();
    await connect(connectionConfig.value);
  };

  const scheduleReconnect = (): void => {
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value);
    }

    reconnectAttempts.value++;
    
    if (showMessages) {
      ElMessage.warning(`连接断开，${reconnectInterval / 1000}秒后尝试第${reconnectAttempts.value}次重连`);
    }

    reconnectTimer.value = window.setTimeout(async () => {
      try {
        if (connectionConfig.value) {
          await connect(connectionConfig.value);
        }
      } catch (error) {
        console.error('自动重连失败:', error);
      }
    }, reconnectInterval);
  };

  const handleConnectionTimeout = (): void => {
    if (showMessages) {
      ElMessage.error('连接超时');
    }
    
    disconnect();
  };

  const clearTimers = (): void => {
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value);
      reconnectTimer.value = null;
    }
    
    if (connectionTimer.value) {
      clearTimeout(connectionTimer.value);
      connectionTimer.value = null;
    }
  };

  // 工具方法
  const sendClipboard = (text: string): void => {
    if (currentServerId.value && isConnected.value) {
      guacamoleConnectionManager.sendClipboard(currentServerId.value, text);
    }
  };

  const sendFile = (file: File): void => {
    if (currentServerId.value && isConnected.value) {
      guacamoleConnectionManager.sendFile(currentServerId.value, file);
    }
  };

  const takeScreenshot = (): string | null => {
    if (currentServerId.value && isConnected.value) {
      return guacamoleConnectionManager.screenshot(currentServerId.value);
    }
    return null;
  };

  const setScale = (scale: number): void => {
    if (currentServerId.value && isConnected.value) {
      guacamoleConnectionManager.setScale(currentServerId.value, scale);
    }
  };

  const getClient = () => {
    if (currentServerId.value) {
      return guacamoleConnectionManager.getConnection(currentServerId.value);
    }
    return null;
  };

  // 创建特定协议的连接配置
  const createRDPConfig = (server: any, options: Partial<RDPConnectionConfig> = {}): RDPConnectionConfig => {
    return {
      serverId: server.monitorSysGenServerId,
      protocol: 'rdp',
      host: server.monitorSysGenServerHost,
      port: server.monitorSysGenServerPort || 3389,
      username: server.monitorSysGenServerUsername || '',
      password: server.monitorSysGenServerPassword || '',
      resolution: '1920x1080',
      colorDepth: 32,
      enableAudio: true,
      enableClipboard: true,
      security: 'any',
      ignoreCert: true,
      ...options
    };
  };

  const createVNCConfig = (server: any, options: Partial<VNCConnectionConfig> = {}): VNCConnectionConfig => {
    return {
      serverId: server.monitorSysGenServerId,
      protocol: 'vnc',
      host: server.monitorSysGenServerHost,
      port: server.monitorSysGenServerPort || 5900,
      username: server.monitorSysGenServerUsername || '',
      password: server.monitorSysGenServerPassword || '',
      enableClipboard: true,
      enableCursor: true,
      readOnly: false,
      swapRedBlue: false,
      colorDepth: 24,
      cursor: 'local',
      encodings: ['tight', 'hextile', 'zlib'],
      autoRetry: true,
      ...options
    };
  };

  // 格式化工具方法
  const formatDuration = (duration: number): string => {
    const seconds = Math.floor(duration / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    
    if (hours > 0) {
      return `${hours}:${(minutes % 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`;
    } else {
      return `${minutes}:${(seconds % 60).toString().padStart(2, '0')}`;
    }
  };

  const formatBytes = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // 生命周期清理
  onUnmounted(() => {
    clearTimers();
    if (currentServerId.value) {
      guacamoleConnectionManager.disconnect(currentServerId.value);
    }
  });

  return {
    // 状态
    currentServerId: readonly(currentServerId),
    connectionState,
    connectionStats,
    displayInfo,
    isConnected,
    isConnecting,
    hasError,
    reconnectAttempts: readonly(reconnectAttempts),

    // 方法
    connect,
    disconnect,
    reconnect,
    sendClipboard,
    sendFile,
    takeScreenshot,
    setScale,
    getClient,

    // 配置创建器
    createRDPConfig,
    createVNCConfig,

    // 工具方法
    formatDuration,
    formatBytes
  };
}

export default useGuacamoleConnection;
