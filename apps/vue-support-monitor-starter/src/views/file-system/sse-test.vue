<template>
  <div class="sse-test-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>文件系统SSE测试</span>
          <el-button 
            :type="isConnected ? 'danger' : 'primary'" 
            @click="toggleConnection"
            :loading="connecting"
          >
            {{ isConnected ? '断开连接' : '建立连接' }}
          </el-button>
        </div>
      </template>

      <!-- 连接状态 -->
      <div class="connection-status">
        <el-tag :type="isConnected ? 'success' : 'danger'">
          {{ isConnected ? '已连接' : '未连接' }}
        </el-tag>
        <span v-if="clientId" class="client-id">客户端ID: {{ clientId }}</span>
      </div>

      <!-- 事件订阅 -->
      <div class="subscription-section">
        <h4>事件订阅</h4>
        <el-checkbox-group v-model="subscribedEvents" @change="updateSubscription">
          <el-checkbox label="upload_progress">上传进度</el-checkbox>
          <el-checkbox label="merge_progress">合并进度</el-checkbox>
          <el-checkbox label="file_created">文件创建</el-checkbox>
          <el-checkbox label="file_deleted">文件删除</el-checkbox>
          <el-checkbox label="file_updated">文件更新</el-checkbox>
          <el-checkbox label="system_status">系统状态</el-checkbox>
          <el-checkbox label="queue_status">队列状态</el-checkbox>
          <el-checkbox label="statistics_update">统计更新</el-checkbox>
          <el-checkbox label="error">错误消息</el-checkbox>
          <el-checkbox label="heartbeat">心跳</el-checkbox>
        </el-checkbox-group>
      </div>

      <!-- 测试按钮 -->
      <div class="test-section">
        <h4>测试功能</h4>
        <el-button @click="sendTestMessage" :disabled="!isConnected">发送测试消息</el-button>
        <el-button @click="sendHeartbeat" :disabled="!isConnected">发送心跳</el-button>
        <el-button @click="clearMessages">清空消息</el-button>
      </div>

      <!-- 消息显示 -->
      <div class="messages-section">
        <h4>接收到的消息 ({{ messages.length }})</h4>
        <div class="messages-container">
          <div 
            v-for="(message, index) in messages" 
            :key="index" 
            class="message-item"
            :class="getMessageClass(message.type)"
          >
            <div class="message-header">
              <span class="message-type">{{ message.type }}</span>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
            <div class="message-content">
              <pre>{{ JSON.stringify(message.data, null, 2) }}</pre>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const isConnected = ref(false)
const connecting = ref(false)
const clientId = ref('')
const subscribedEvents = ref([])
const messages = ref([])

// SSE连接
let eventSource = null

// 连接状态切换
const toggleConnection = async () => {
  if (isConnected.value) {
    await disconnect()
  } else {
    await connect()
  }
}

// 建立SSE连接
const connect = async () => {
  if (isConnected.value) return

  connecting.value = true
  
  try {
    // 生成客户端ID
    clientId.value = `client_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    
    // 建立SSE连接
    const url = `/api/v1/filesystem/sse/connect?clientId=${clientId.value}`
    eventSource = new EventSource(url)

    // 连接成功
    eventSource.onopen = () => {
      isConnected.value = true
      connecting.value = false
      ElMessage.success('SSE连接建立成功')
      addMessage('system', 'connected', { message: '连接成功', clientId: clientId.value })
    }

    // 接收消息
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        addMessage('message', event.type || 'message', data)
      } catch (e) {
        addMessage('error', 'parse_error', { error: '解析消息失败', raw: event.data })
      }
    }

    // 监听特定事件
    const eventTypes = [
      'connected', 'upload_progress', 'merge_progress', 'file_created', 
      'file_deleted', 'file_updated', 'system_status', 'queue_status',
      'statistics_update', 'error', 'heartbeat', 'test'
    ]

    eventTypes.forEach(eventType => {
      eventSource.addEventListener(eventType, (event) => {
        try {
          const data = JSON.parse(event.data)
          addMessage(eventType, eventType, data)
        } catch (e) {
          addMessage('error', 'parse_error', { 
            error: '解析消息失败', 
            eventType, 
            raw: event.data 
          })
        }
      })
    })

    // 连接错误
    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      isConnected.value = false
      connecting.value = false
      ElMessage.error('SSE连接错误')
      addMessage('error', 'connection_error', { error: '连接错误' })
    }

  } catch (error) {
    connecting.value = false
    ElMessage.error('建立SSE连接失败: ' + error.message)
    addMessage('error', 'connect_failed', { error: error.message })
  }
}

// 断开连接
const disconnect = async () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  
  isConnected.value = false
  
  try {
    // 通知服务器断开连接
    if (clientId.value) {
      await fetch(`/api/v1/filesystem/sse/disconnect?clientId=${clientId.value}`, {
        method: 'POST'
      })
    }
  } catch (error) {
    console.error('断开连接失败:', error)
  }
  
  ElMessage.info('SSE连接已断开')
  addMessage('system', 'disconnected', { message: '连接断开' })
}

// 更新事件订阅
const updateSubscription = async () => {
  if (!isConnected.value || !clientId.value) return

  try {
    const eventTypes = subscribedEvents.value.join(',')
    const response = await fetch(`/api/v1/filesystem/sse/subscribe?clientId=${clientId.value}&eventTypes=${eventTypes}`, {
      method: 'POST'
    })
    
    if (response.ok) {
      ElMessage.success('订阅更新成功')
      addMessage('system', 'subscription_updated', { 
        subscribedEvents: subscribedEvents.value 
      })
    } else {
      ElMessage.error('订阅更新失败')
    }
  } catch (error) {
    ElMessage.error('订阅更新失败: ' + error.message)
  }
}

// 发送测试消息
const sendTestMessage = async () => {
  if (!isConnected.value || !clientId.value) return

  try {
    const testData = {
      message: '这是一条测试消息',
      timestamp: Date.now(),
      random: Math.random()
    }

    const response = await fetch(`/api/v1/filesystem/sse/test?clientId=${clientId.value}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(testData)
    })

    if (response.ok) {
      ElMessage.success('测试消息发送成功')
    } else {
      ElMessage.error('测试消息发送失败')
    }
  } catch (error) {
    ElMessage.error('发送测试消息失败: ' + error.message)
  }
}

// 发送心跳
const sendHeartbeat = async () => {
  if (!isConnected.value || !clientId.value) return

  try {
    const response = await fetch(`/api/v1/filesystem/sse/heartbeat?clientId=${clientId.value}`, {
      method: 'POST'
    })

    if (response.ok) {
      ElMessage.success('心跳发送成功')
    } else {
      ElMessage.error('心跳发送失败')
    }
  } catch (error) {
    ElMessage.error('发送心跳失败: ' + error.message)
  }
}

// 添加消息
const addMessage = (category, type, data) => {
  messages.value.unshift({
    category,
    type,
    data,
    timestamp: Date.now()
  })
  
  // 限制消息数量
  if (messages.value.length > 100) {
    messages.value = messages.value.slice(0, 100)
  }
}

// 清空消息
const clearMessages = () => {
  messages.value = []
  ElMessage.info('消息已清空')
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString()
}

// 获取消息样式类
const getMessageClass = (type) => {
  if (type.includes('error')) return 'message-error'
  if (type.includes('success') || type === 'connected') return 'message-success'
  if (type.includes('progress')) return 'message-progress'
  if (type === 'heartbeat') return 'message-heartbeat'
  return 'message-info'
}

// 组件挂载时自动连接
onMounted(() => {
  // 可以选择自动连接
  // connect()
})

// 组件卸载时断开连接
onUnmounted(() => {
  disconnect()
})
</script>

<style scoped>
.sse-test-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.connection-status {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.client-id {
  font-size: 12px;
  color: #666;
}

.subscription-section,
.test-section,
.messages-section {
  margin-bottom: 20px;
}

.subscription-section h4,
.test-section h4,
.messages-section h4 {
  margin-bottom: 10px;
  color: #333;
}

.test-section .el-button {
  margin-right: 10px;
}

.messages-container {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.message-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.message-item:last-child {
  border-bottom: none;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.message-type {
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-content {
  font-size: 12px;
}

.message-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.message-error {
  background-color: #fef0f0;
}

.message-error .message-type {
  background-color: #f56c6c;
  color: white;
}

.message-success {
  background-color: #f0f9ff;
}

.message-success .message-type {
  background-color: #67c23a;
  color: white;
}

.message-progress {
  background-color: #fdf6ec;
}

.message-progress .message-type {
  background-color: #e6a23c;
  color: white;
}

.message-heartbeat {
  background-color: #f4f4f5;
}

.message-heartbeat .message-type {
  background-color: #909399;
  color: white;
}

.message-info .message-type {
  background-color: #409eff;
  color: white;
}
</style>
