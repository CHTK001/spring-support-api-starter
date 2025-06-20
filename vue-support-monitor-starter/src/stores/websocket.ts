import { defineStore } from 'pinia'
import { ref, onUnmounted } from 'vue'
import mitt from 'mitt'

// 事件类型定义
type Events = {
  'server-status-update': any
  'log-update': any
  'upload-progress': any
  'script-execution': any
  'connection-change': any
}

const emitter = mitt<Events>()

export const useWebSocketStore = defineStore('websocket', () => {
  const ws = ref<WebSocket | null>(null)
  const connected = ref(false)
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = 5
  const reconnectInterval = 3000
  
  // 连接WebSocket
  const connect = () => {
    try {
      const wsUrl = import.meta.env.VITE_APP_WS_URL || 'ws://localhost:8080/ws'
      ws.value = new WebSocket(wsUrl)
      
      ws.value.onopen = () => {
        console.log('WebSocket连接已建立')
        connected.value = true
        reconnectAttempts.value = 0
        
        // 发送心跳
        startHeartbeat()
      }
      
      ws.value.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          handleMessage(data)
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }
      
      ws.value.onclose = () => {
        console.log('WebSocket连接已关闭')
        connected.value = false
        stopHeartbeat()
        
        // 自动重连
        if (reconnectAttempts.value < maxReconnectAttempts) {
          setTimeout(() => {
            reconnectAttempts.value++
            console.log(`尝试重连WebSocket (${reconnectAttempts.value}/${maxReconnectAttempts})`)
            connect()
          }, reconnectInterval)
        }
      }
      
      ws.value.onerror = (error) => {
        console.error('WebSocket连接错误:', error)
      }
      
    } catch (error) {
      console.error('创建WebSocket连接失败:', error)
    }
  }
  
  // 断开连接
  const disconnect = () => {
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
    connected.value = false
    stopHeartbeat()
  }
  
  // 发送消息
  const send = (message: any) => {
    if (ws.value && connected.value) {
      ws.value.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket未连接，无法发送消息')
    }
  }
  
  // 处理接收到的消息
  const handleMessage = (data: any) => {
    const { type, payload } = data
    
    switch (type) {
      case 'server-status-update':
        emitter.emit('server-status-update', payload)
        break
      case 'log-update':
        emitter.emit('log-update', payload)
        break
      case 'upload-progress':
        emitter.emit('upload-progress', payload)
        break
      case 'script-execution':
        emitter.emit('script-execution', payload)
        break
      case 'connection-change':
        emitter.emit('connection-change', payload)
        break
      case 'heartbeat':
        // 心跳响应，不需要处理
        break
      default:
        console.log('未知消息类型:', type, payload)
    }
  }
  
  // 心跳机制
  let heartbeatTimer: number | null = null
  
  const startHeartbeat = () => {
    heartbeatTimer = window.setInterval(() => {
      send({ type: 'heartbeat', timestamp: Date.now() })
    }, 30000) // 30秒发送一次心跳
  }
  
  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }
  
  // 订阅事件
  const on = <K extends keyof Events>(event: K, handler: (data: Events[K]) => void) => {
    emitter.on(event, handler)
  }
  
  // 取消订阅
  const off = <K extends keyof Events>(event: K, handler: (data: Events[K]) => void) => {
    emitter.off(event, handler)
  }
  
  // 订阅服务器状态更新
  const subscribeServerStatus = (serverIds: number[]) => {
    send({
      type: 'subscribe',
      topic: 'server-status',
      serverIds
    })
  }
  
  // 订阅日志更新
  const subscribeLogUpdates = (serverId: number) => {
    send({
      type: 'subscribe',
      topic: 'log-updates',
      serverId
    })
  }
  
  // 订阅上传进度
  const subscribeUploadProgress = (taskId: number) => {
    send({
      type: 'subscribe',
      topic: 'upload-progress',
      taskId
    })
  }
  
  // 订阅脚本执行
  const subscribeScriptExecution = (executionId: number) => {
    send({
      type: 'subscribe',
      topic: 'script-execution',
      executionId
    })
  }
  
  // 组件卸载时清理
  onUnmounted(() => {
    disconnect()
  })
  
  return {
    connected,
    reconnectAttempts,
    connect,
    disconnect,
    send,
    on,
    off,
    subscribeServerStatus,
    subscribeLogUpdates,
    subscribeUploadProgress,
    subscribeScriptExecution
  }
})
