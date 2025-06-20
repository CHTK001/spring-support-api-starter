<template>
  <div class="app-container">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header mb-20">
        <h2>服务器连接状态监控</h2>
        <p class="text-muted">实时监控服务器连接状态和网络性能</p>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="mb-20">
        <el-col :xs="24" :sm="12" :md="6" v-for="stat in statistics" :key="stat.key">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-info">
                <div class="stats-title">{{ stat.title }}</div>
                <div class="stats-value" :style="{ color: stat.color }">{{ stat.value }}</div>
                <div class="stats-subtitle" v-if="stat.subtitle">{{ stat.subtitle }}</div>
              </div>
              <div class="stats-icon" :style="{ color: stat.color }">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 连接状态趋势图 -->
      <el-card class="mb-20">
        <template #header>
          <div class="flex-between">
            <span>连接状态趋势</span>
            <div>
              <el-radio-group v-model="trendInterval" @change="fetchTrendData">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
                <el-radio-button label="7d">7天</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        <div style="height: 300px">
          <v-chart :option="trendChartOption" autoresize />
        </div>
      </el-card>

      <!-- 搜索和操作栏 -->
      <el-card class="mb-20">
        <el-form :model="searchForm" inline>
          <el-form-item label="服务器">
            <el-select
              v-model="searchForm.serverId"
              placeholder="请选择服务器"
              clearable
              filterable
              style="width: 200px"
            >
              <el-option
                v-for="server in serverList"
                :key="server.monitorSysGenServerId"
                :label="server.monitorSysGenServerName"
                :value="server.monitorSysGenServerId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="连接状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="在线" value="ONLINE" />
              <el-option label="离线" value="OFFLINE" />
              <el-option label="未知" value="UNKNOWN" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
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
          <el-button type="primary" @click="handleCheckAll" :loading="checking">
            <el-icon><Connection /></el-icon>
            检查所有连接
          </el-button>
          <el-button type="success" @click="handleViewOnline">
            <el-icon><CircleCheck /></el-icon>
            查看在线服务器
          </el-button>
          <el-button type="danger" @click="handleViewOffline">
            <el-icon><CircleClose /></el-icon>
            查看离线服务器
          </el-button>
        </div>
        <div>
          <el-switch
            v-model="autoRefresh"
            active-text="自动刷新"
            @change="handleAutoRefreshChange"
          />
          <el-button @click="handleRefresh" :loading="loading" class="ml-10">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 连接状态列表 -->
      <el-card>
        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          border
        >
          <el-table-column label="服务器" min-width="200">
            <template #default="{ row }">
              <div class="server-info">
                <div class="server-name">{{ getServerName(row.monitorSysGenServerId) }}</div>
                <div class="server-host text-muted">{{ getServerHost(row.monitorSysGenServerId) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="连接状态" width="120">
            <template #default="{ row }">
              <div class="status-container">
                <span class="status-dot" :class="row.monitorSysGenServerConnectionStatus.toLowerCase()"></span>
                <el-tag :type="getStatusColor(row.monitorSysGenServerConnectionStatus)">
                  {{ getStatusText(row.monitorSysGenServerConnectionStatus) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="响应时间" width="120">
            <template #default="{ row }">
              <span v-if="row.monitorSysGenServerConnectionResponseTime">
                {{ row.monitorSysGenServerConnectionResponseTime }}ms
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="最后检查时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.monitorSysGenServerConnectionLastCheckTime) }}
            </template>
          </el-table-column>
          <el-table-column label="错误信息" min-width="200">
            <template #default="{ row }">
              <el-tooltip
                v-if="row.monitorSysGenServerConnectionErrorMessage"
                :content="row.monitorSysGenServerConnectionErrorMessage"
                placement="top"
              >
                <span class="error-message">
                  {{ row.monitorSysGenServerConnectionErrorMessage.substring(0, 50) }}
                  <span v-if="row.monitorSysGenServerConnectionErrorMessage.length > 50">...</span>
                </span>
              </el-tooltip>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleCheckConnection(row)"
                :loading="checkingMap.get(row.monitorSysGenServerId)"
              >
                检查连接
              </el-button>
              <el-button
                type="info"
                size="small"
                @click="handleViewHistory(row)"
              >
                历史记录
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

    <!-- 历史记录对话框 -->
    <ConnectionHistoryDialog
      v-model:visible="historyDialogVisible"
      :server-id="currentServerId"
      :server-name="currentServerName"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  Connection,
  CircleCheck,
  CircleClose,
  Monitor,
  Timer,
  Warning
} from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { serverApi, connectionStatusApi } from '@/api/server'
import { useWebSocketStore } from '@/stores/websocket'
import { formatDateTime, getStatusColor, getStatusText } from '@/utils'
import type { Server, ServerConnectionStatus } from '@/types'
import ConnectionHistoryDialog from './components/ConnectionHistoryDialog.vue'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 响应式数据
const loading = ref(false)
const checking = ref(false)
const autoRefresh = ref(true)
const trendInterval = ref('6h')
const tableData = ref<ServerConnectionStatus[]>([])
const serverList = ref<Server[]>([])
const checkingMap = ref<Map<number, boolean>>(new Map())
const historyDialogVisible = ref(false)
const currentServerId = ref<number>()
const currentServerName = ref<string>()

// 搜索表单
const searchForm = reactive({
  serverId: undefined as number | undefined,
  status: '',
  timeRange: [] as string[]
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 趋势图数据
const trendChartOption = ref({})

// WebSocket
const wsStore = useWebSocketStore()

// 自动刷新定时器
let refreshTimer: number | null = null

// 统计数据
const statistics = computed(() => {
  const total = tableData.value.length
  const online = tableData.value.filter(item => item.monitorSysGenServerConnectionStatus === 'ONLINE').length
  const offline = tableData.value.filter(item => item.monitorSysGenServerConnectionStatus === 'OFFLINE').length
  const avgResponseTime = tableData.value
    .filter(item => item.monitorSysGenServerConnectionResponseTime)
    .reduce((sum, item) => sum + (item.monitorSysGenServerConnectionResponseTime || 0), 0) / 
    (tableData.value.filter(item => item.monitorSysGenServerConnectionResponseTime).length || 1)

  return [
    {
      key: 'total',
      title: '服务器总数',
      value: total,
      color: '#409eff',
      icon: 'Monitor'
    },
    {
      key: 'online',
      title: '在线服务器',
      value: online,
      subtitle: `${total > 0 ? ((online / total) * 100).toFixed(1) : 0}%`,
      color: '#67c23a',
      icon: 'CircleCheck'
    },
    {
      key: 'offline',
      title: '离线服务器',
      value: offline,
      subtitle: `${total > 0 ? ((offline / total) * 100).toFixed(1) : 0}%`,
      color: '#f56c6c',
      icon: 'CircleClose'
    },
    {
      key: 'avgResponseTime',
      title: '平均响应时间',
      value: `${Math.round(avgResponseTime)}ms`,
      color: '#e6a23c',
      icon: 'Timer'
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
      serverId: searchForm.serverId,
      status: searchForm.status,
      startTime: searchForm.timeRange[0],
      endTime: searchForm.timeRange[1]
    }
    
    const response = await connectionStatusApi.getConnectionStatusList(params)
    if (response.data) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    console.error('获取连接状态列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchServerList = async () => {
  try {
    const response = await serverApi.getServerList({ current: 1, size: 1000 })
    if (response.data) {
      serverList.value = response.data.records
    }
  } catch (error) {
    console.error('获取服务器列表失败:', error)
  }
}

const fetchTrendData = async () => {
  try {
    const response = await connectionStatusApi.getConnectionTrend({
      interval: trendInterval.value
    })
    
    if (response.data) {
      updateTrendChart(response.data)
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

const updateTrendChart = (data: any[]) => {
  const times = data.map(item => item.time)
  const onlineData = data.map(item => item.online)
  const offlineData = data.map(item => item.offline)
  
  trendChartOption.value = {
    title: {
      text: '连接状态趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['在线', '离线'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: times
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '在线',
        type: 'line',
        data: onlineData,
        smooth: true,
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '离线',
        type: 'line',
        data: offlineData,
        smooth: true,
        itemStyle: {
          color: '#f56c6c'
        }
      }
    ]
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    serverId: undefined,
    status: '',
    timeRange: []
  })
  handleSearch()
}

const handleRefresh = () => {
  fetchData()
  fetchTrendData()
}

const handleCheckAll = async () => {
  try {
    checking.value = true
    const serverIds = serverList.value.map(server => server.monitorSysGenServerId)
    await connectionStatusApi.batchCheckConnectionStatus(serverIds)
    ElMessage.success('批量连接检查已启动')
    
    // 延迟刷新数据
    setTimeout(() => {
      fetchData()
    }, 3000)
  } catch (error) {
    console.error('批量检查连接失败:', error)
  } finally {
    checking.value = false
  }
}

const handleCheckConnection = async (row: ServerConnectionStatus) => {
  try {
    checkingMap.value.set(row.monitorSysGenServerId, true)
    await connectionStatusApi.checkConnectionStatus(row.monitorSysGenServerId)
    ElMessage.success('连接检查已启动')
    
    // 延迟刷新数据
    setTimeout(() => {
      fetchData()
    }, 2000)
  } catch (error) {
    console.error('检查连接失败:', error)
  } finally {
    checkingMap.value.set(row.monitorSysGenServerId, false)
  }
}

const handleViewOnline = () => {
  searchForm.status = 'ONLINE'
  handleSearch()
}

const handleViewOffline = () => {
  searchForm.status = 'OFFLINE'
  handleSearch()
}

const handleViewHistory = (row: ServerConnectionStatus) => {
  currentServerId.value = row.monitorSysGenServerId
  currentServerName.value = getServerName(row.monitorSysGenServerId)
  historyDialogVisible.value = true
}

const handleAutoRefreshChange = (value: boolean) => {
  if (value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchData()
}

// 工具方法
const getServerName = (serverId: number) => {
  const server = serverList.value.find(s => s.monitorSysGenServerId === serverId)
  return server?.monitorSysGenServerName || `服务器${serverId}`
}

const getServerHost = (serverId: number) => {
  const server = serverList.value.find(s => s.monitorSysGenServerId === serverId)
  return server ? `${server.monitorSysGenServerHost}:${server.monitorSysGenServerPort}` : '-'
}

// 自动刷新
const startAutoRefresh = () => {
  if (refreshTimer) return
  
  refreshTimer = window.setInterval(() => {
    fetchData()
  }, 30000) // 30秒刷新一次
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 监听WebSocket消息
wsStore.on('connection-change', (data) => {
  // 更新对应的连接状态
  const index = tableData.value.findIndex(item => item.monitorSysGenServerId === data.serverId)
  if (index !== -1) {
    tableData.value[index] = { ...tableData.value[index], ...data.status }
  }
})

// 生命周期
onMounted(() => {
  fetchServerList()
  fetchData()
  fetchTrendData()
  
  if (autoRefresh.value) {
    startAutoRefresh()
  }
  
  // 订阅所有服务器的连接状态
  const serverIds = serverList.value.map(server => server.monitorSysGenServerId)
  if (serverIds.length > 0) {
    wsStore.subscribeServerStatus(serverIds)
  }
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style lang="scss" scoped>
.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.text-muted {
  color: var(--el-text-color-regular);
  font-size: 12px;
}

.server-info {
  .server-name {
    font-weight: 500;
    margin-bottom: 4px;
  }
}

.status-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-message {
  color: var(--el-color-danger);
  font-size: 12px;
  cursor: pointer;
}

.stats-subtitle {
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin-top: 4px;
}
</style>
