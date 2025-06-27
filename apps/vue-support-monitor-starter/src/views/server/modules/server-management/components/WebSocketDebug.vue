<template>
  <div class="websocket-debug">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>WebSocket 消息调试</span>
          <div class="header-actions">
            <el-button 
              :type="state.connected ? 'danger' : 'primary'"
              @click="state.connected ? disconnect() : connect()"
            >
              {{ state.connected ? '断开连接' : '连接WebSocket' }}
            </el-button>
            <el-button @click="clearMessages">清空消息</el-button>
          </div>
        </div>
      </template>

      <!-- 连接状态 -->
      <div class="connection-status">
        <el-tag 
          :type="state.connected ? 'success' : state.connecting ? 'warning' : 'danger'"
          size="large"
        >
          {{ state.connected ? '已连接' : state.connecting ? '连接中' : '未连接' }}
        </el-tag>
        <span v-if="state.error" class="error-message">{{ state.error }}</span>
      </div>

      <!-- 消息统计 -->
      <div class="message-stats">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-statistic title="总消息数" :value="messages.length" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="未处理类型" :value="unhandledTypes.length" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="server_metrics" :value="messageTypeCounts.server_metrics || 0" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="shell_output" :value="messageTypeCounts.shell_output || 0" />
          </el-col>
        </el-row>
      </div>

      <!-- 未处理的消息类型 -->
      <div v-if="unhandledTypes.length > 0" class="unhandled-types">
        <h4>未处理的消息类型:</h4>
        <el-tag v-for="type in unhandledTypes" :key="type" type="warning" style="margin-right: 8px;">
          {{ type }}
        </el-tag>
      </div>

      <!-- 消息列表 -->
      <div class="message-list">
        <h4>最近消息 (最多显示50条):</h4>
        <el-table :data="messages" height="400" stripe>
          <el-table-column prop="timestamp" label="时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.timestamp) }}
            </template>
          </el-table-column>
          <el-table-column prop="messageType" label="消息类型" width="180">
            <template #default="{ row }">
              <el-tag :type="getMessageTypeColor(row.messageType)">
                {{ row.messageType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="serverId" label="服务器ID" width="100" />
          <el-table-column prop="serverName" label="服务器名称" width="150" />
          <el-table-column label="数据" min-width="200">
            <template #default="{ row }">
              <el-popover placement="top" width="400" trigger="click">
                <template #reference>
                  <el-button size="small" type="text">查看数据</el-button>
                </template>
                <pre>{{ JSON.stringify(row.data, null, 2) }}</pre>
              </el-popover>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useServerWebSocket } from '@/composables/useServerWebSocket';
import type { ServerWebSocketMessage } from '@/api/server';

// WebSocket 连接
const { state, connect, disconnect, onMessage, getUnhandledMessageTypes } = useServerWebSocket();

// 消息列表
const messages = ref<ServerWebSocketMessage[]>([]);

// 消息类型统计
const messageTypeCounts = reactive<Record<string, number>>({});

// 未处理的消息类型
const unhandledTypes = computed(() => getUnhandledMessageTypes());

/**
 * 清空消息
 */
const clearMessages = () => {
  messages.value = [];
  Object.keys(messageTypeCounts).forEach(key => {
    messageTypeCounts[key] = 0;
  });
};

/**
 * 格式化时间
 */
const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleString();
};

/**
 * 获取消息类型颜色
 */
const getMessageTypeColor = (messageType: string) => {
  const colorMap: Record<string, string> = {
    'server_metrics': 'success',
    'shell_output': 'primary',
    'ssh_data': 'primary',
    'connection_status_change': 'warning',
    'server_online': 'success',
    'server_offline': 'danger',
    'ssh_error': 'danger',
  };
  return colorMap[messageType] || 'info';
};

/**
 * 添加消息到列表
 */
const addMessage = (message: ServerWebSocketMessage) => {
  // 添加到消息列表
  messages.value.unshift(message);
  
  // 限制消息数量
  if (messages.value.length > 50) {
    messages.value = messages.value.slice(0, 50);
  }
  
  // 更新统计
  const type = message.messageType;
  messageTypeCounts[type] = (messageTypeCounts[type] || 0) + 1;
};

// 监听所有消息类型
onMounted(() => {
  // 监听所有可能的消息类型
  const messageTypes = [
    'connection_status_change',
    'server_online',
    'server_offline', 
    'server_update',
    'server_delete',
    'server_add',
    'server_metrics',
    'connection_test_result',
    'ssh_data',
    'ssh_input',
    'ssh_connect',
    'ssh_disconnect',
    'ssh_resize',
    'ssh_error',
    'shell_output',
    'vnc_data',
    'vnc_input',
    'rdp_data',
    'rdp_input'
  ];

  messageTypes.forEach(type => {
    onMessage(type, (message) => {
      console.log(`收到 ${type} 消息:`, message);
      addMessage(message);
    });
  });

  // 自动连接
  connect();
});

onUnmounted(() => {
  disconnect();
});
</script>

<style scoped>
.websocket-debug {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.connection-status {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.error-message {
  color: #f56c6c;
  font-size: 14px;
}

.message-stats {
  margin-bottom: 20px;
}

.unhandled-types {
  margin-bottom: 20px;
  padding: 12px;
  background-color: #fdf6ec;
  border-radius: 4px;
}

.unhandled-types h4 {
  margin: 0 0 8px 0;
  color: #e6a23c;
}

.message-list h4 {
  margin-bottom: 12px;
}

pre {
  max-height: 300px;
  overflow-y: auto;
  font-size: 12px;
  line-height: 1.4;
}
</style>
