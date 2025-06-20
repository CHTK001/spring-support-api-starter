<template>
  <div class="app-container">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header mb-20">
        <h2>服务器列表</h2>
        <p class="text-muted">管理和监控所有服务器信息</p>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="mb-20">
        <el-col :xs="24" :sm="12" :md="6" v-for="stat in statistics" :key="stat.key">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-info">
                <div class="stats-title">{{ stat.title }}</div>
                <div class="stats-value" :style="{ color: stat.color }">{{ stat.value }}</div>
              </div>
              <div class="stats-icon" :style="{ color: stat.color }">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 搜索和操作栏 -->
      <el-card class="mb-20">
        <el-form :model="searchForm" inline>
          <el-form-item label="服务器名称">
            <el-input
              v-model="searchForm.serverName"
              placeholder="请输入服务器名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="主机地址">
            <el-input
              v-model="searchForm.serverHost"
              placeholder="请输入主机地址"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="服务器类型">
            <el-select v-model="searchForm.serverType" placeholder="请选择类型" clearable>
              <el-option
                v-for="type in serverTypes"
                :key="type"
                :label="type"
                :value="type"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="操作系统">
            <el-select v-model="searchForm.serverOs" placeholder="请选择系统" clearable>
              <el-option
                v-for="os in operatingSystems"
                :key="os"
                :label="os"
                :value="os"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 操作按钮 -->
      <div class="mb-20 flex-between">
        <div>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加服务器
          </el-button>
          <el-button
            type="danger"
            :disabled="!selectedRows.length"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button
            type="warning"
            :disabled="!selectedRows.length"
            @click="handleBatchTestConnection"
          >
            <el-icon><Connection /></el-icon>
            批量测试连接
          </el-button>
        </div>
        <div>
          <el-button @click="handleRefresh" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-card>
        <el-table
          v-loading="loading"
          :data="tableData"
          @selection-change="handleSelectionChange"
          stripe
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="monitorSysGenServerId" label="ID" width="80" />
          <el-table-column prop="monitorSysGenServerName" label="服务器名称" min-width="150">
            <template #default="{ row }">
              <el-link type="primary" @click="handleView(row)">
                {{ row.monitorSysGenServerName }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="主机地址" min-width="180">
            <template #default="{ row }">
              {{ row.monitorSysGenServerHost }}:{{ row.monitorSysGenServerPort }}
            </template>
          </el-table-column>
          <el-table-column prop="monitorSysGenServerType" label="类型" width="100" />
          <el-table-column prop="monitorSysGenServerOs" label="操作系统" width="120" />
          <el-table-column prop="monitorSysGenServerUsername" label="用户名" width="100" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.monitorSysGenServerStatus === 1 ? 'success' : 'danger'">
                {{ row.monitorSysGenServerStatus === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="连接状态" width="100">
            <template #default="{ row }">
              <el-tooltip :content="getConnectionTooltip(row)" placement="top">
                <span class="status-dot" :class="getConnectionStatus(row)"></span>
                {{ getConnectionText(row) }}
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="monitorSysGenServerCreateTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.monitorSysGenServerCreateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="warning" size="small" @click="handleTestConnection(row)">
                测试连接
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 添加/编辑对话框 -->
    <ServerFormDialog
      v-model:visible="dialogVisible"
      :form-data="currentServer"
      :is-edit="isEdit"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Delete,
  Connection,
  Server,
  Monitor,
  Warning,
  CircleCheck
} from '@element-plus/icons-vue'
import { serverApi, connectionStatusApi } from '@/api/server'
import { useWebSocketStore } from '@/stores/websocket'
import { formatDateTime } from '@/utils'
import type { Server, ServerConnectionStatus } from '@/types'
import ServerFormDialog from './components/ServerFormDialog.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<Server[]>([])
const selectedRows = ref<Server[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentServer = ref<Partial<Server>>({})
const serverTypes = ref<string[]>([])
const operatingSystems = ref<string[]>([])
const connectionStatusMap = ref<Map<number, ServerConnectionStatus>>(new Map())

// 搜索表单
const searchForm = reactive({
  serverName: '',
  serverHost: '',
  serverType: '',
  serverOs: '',
  status: undefined as number | undefined
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// WebSocket
const wsStore = useWebSocketStore()

// 统计数据
const statistics = computed(() => {
  const total = tableData.value.length
  const enabled = tableData.value.filter(item => item.monitorSysGenServerStatus === 1).length
  const disabled = total - enabled
  const online = Array.from(connectionStatusMap.value.values())
    .filter(status => status.monitorSysGenServerConnectionStatus === 'ONLINE').length

  return [
    {
      key: 'total',
      title: '服务器总数',
      value: total,
      color: '#409eff',
      icon: 'Server'
    },
    {
      key: 'enabled',
      title: '启用服务器',
      value: enabled,
      color: '#67c23a',
      icon: 'CircleCheck'
    },
    {
      key: 'online',
      title: '在线服务器',
      value: online,
      color: '#67c23a',
      icon: 'Monitor'
    },
    {
      key: 'offline',
      title: '离线服务器',
      value: total - online,
      color: '#f56c6c',
      icon: 'Warning'
    }
  ]
})

// 方法
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await serverApi.getServerList(params)
    if (response.data) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    }
    
    // 获取连接状态
    await fetchConnectionStatus()
  } catch (error) {
    console.error('获取服务器列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchConnectionStatus = async () => {
  try {
    const serverIds = tableData.value.map(server => server.monitorSysGenServerId)
    if (serverIds.length > 0) {
      // 订阅服务器状态更新
      wsStore.subscribeServerStatus(serverIds)
      
      // 获取当前连接状态
      for (const serverId of serverIds) {
        try {
          const response = await connectionStatusApi.getServerConnectionStatus(serverId)
          if (response.data) {
            connectionStatusMap.value.set(serverId, response.data)
          }
        } catch (error) {
          // 忽略单个服务器的错误
        }
      }
    }
  } catch (error) {
    console.error('获取连接状态失败:', error)
  }
}

const fetchServerTypes = async () => {
  try {
    const response = await serverApi.getServerTypes()
    if (response.data) {
      serverTypes.value = response.data
    }
  } catch (error) {
    console.error('获取服务器类型失败:', error)
  }
}

const fetchOperatingSystems = async () => {
  try {
    const response = await serverApi.getOperatingSystems()
    if (response.data) {
      operatingSystems.value = response.data
    }
  } catch (error) {
    console.error('获取操作系统列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    serverName: '',
    serverHost: '',
    serverType: '',
    serverOs: '',
    status: undefined
  })
  handleSearch()
}

const handleRefresh = () => {
  fetchData()
}

const handleAdd = () => {
  currentServer.value = {}
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: Server) => {
  currentServer.value = { ...row }
  isEdit.value = true
  dialogVisible.value = true
}

const handleView = (row: Server) => {
  // TODO: 跳转到服务器详情页
  console.log('查看服务器:', row)
}

const handleDelete = async (row: Server) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除服务器 "${row.monitorSysGenServerName}" 吗？`,
      '确认删除',
      {
        type: 'warning'
      }
    )
    
    await serverApi.deleteServer(row.monitorSysGenServerId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除服务器失败:', error)
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个服务器吗？`,
      '确认批量删除',
      {
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.monitorSysGenServerId)
    await serverApi.batchDeleteServers(ids)
    ElMessage.success('批量删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

const handleTestConnection = async (row: Server) => {
  try {
    loading.value = true
    await serverApi.testConnection(row.monitorSysGenServerId)
    ElMessage.success('连接测试成功')
    // 刷新连接状态
    setTimeout(() => {
      fetchConnectionStatus()
    }, 1000)
  } catch (error) {
    console.error('连接测试失败:', error)
  } finally {
    loading.value = false
  }
}

const handleBatchTestConnection = async () => {
  try {
    loading.value = true
    const ids = selectedRows.value.map(row => row.monitorSysGenServerId)
    await serverApi.batchTestConnection(ids)
    ElMessage.success('批量连接测试已启动')
    // 刷新连接状态
    setTimeout(() => {
      fetchConnectionStatus()
    }, 2000)
  } catch (error) {
    console.error('批量连接测试失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection: Server[]) => {
  selectedRows.value = selection
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchData()
}

const handleFormSuccess = () => {
  dialogVisible.value = false
  fetchData()
}

// 连接状态相关方法
const getConnectionStatus = (row: Server) => {
  const status = connectionStatusMap.value.get(row.monitorSysGenServerId)
  if (!status) return 'unknown'
  return status.monitorSysGenServerConnectionStatus.toLowerCase()
}

const getConnectionText = (row: Server) => {
  const status = connectionStatusMap.value.get(row.monitorSysGenServerId)
  if (!status) return '未知'
  
  const statusMap: Record<string, string> = {
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'UNKNOWN': '未知'
  }
  
  return statusMap[status.monitorSysGenServerConnectionStatus] || '未知'
}

const getConnectionTooltip = (row: Server) => {
  const status = connectionStatusMap.value.get(row.monitorSysGenServerId)
  if (!status) return '连接状态未知'
  
  let tooltip = `状态: ${getConnectionText(row)}`
  if (status.monitorSysGenServerConnectionLastCheckTime) {
    tooltip += `\n最后检查: ${formatDateTime(status.monitorSysGenServerConnectionLastCheckTime)}`
  }
  if (status.monitorSysGenServerConnectionResponseTime) {
    tooltip += `\n响应时间: ${status.monitorSysGenServerConnectionResponseTime}ms`
  }
  if (status.monitorSysGenServerConnectionErrorMessage) {
    tooltip += `\n错误信息: ${status.monitorSysGenServerConnectionErrorMessage}`
  }
  
  return tooltip
}

// 监听WebSocket消息
wsStore.on('server-status-update', (data) => {
  if (data.serverId && data.status) {
    connectionStatusMap.value.set(data.serverId, data.status)
  }
})

// 生命周期
onMounted(() => {
  fetchData()
  fetchServerTypes()
  fetchOperatingSystems()
})
</script>

<style lang="scss" scoped>
.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.text-muted {
  color: var(--el-text-color-regular);
  font-size: 14px;
}
</style>
