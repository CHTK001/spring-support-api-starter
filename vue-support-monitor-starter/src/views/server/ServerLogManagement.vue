<template>
  <div class="app-container">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header mb-20">
        <h2>服务器日志管理</h2>
        <p class="text-muted">集中管理和监控服务器日志信息</p>
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

      <!-- 日志级别分布图 -->
      <el-row :gutter="20" class="mb-20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>日志级别分布</span>
            </template>
            <div style="height: 300px">
              <v-chart :option="levelChartOption" autoresize />
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>日志趋势</span>
            </template>
            <div style="height: 300px">
              <v-chart :option="trendChartOption" autoresize />
            </div>
          </el-card>
        </el-col>
      </el-row>

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
          <el-form-item label="日志级别">
            <el-select v-model="searchForm.level" placeholder="请选择级别" clearable>
              <el-option label="DEBUG" value="DEBUG" />
              <el-option label="INFO" value="INFO" />
              <el-option label="WARN" value="WARN" />
              <el-option label="ERROR" value="ERROR" />
              <el-option label="FATAL" value="FATAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="日志来源">
            <el-input
              v-model="searchForm.source"
              placeholder="请输入日志来源"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.content"
              placeholder="请输入关键词"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
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
          <el-button type="primary" @click="handleLogConfig">
            <el-icon><Setting /></el-icon>
            日志配置
          </el-button>
          <el-button type="success" @click="handleExport" :loading="exporting">
            <el-icon><Download /></el-icon>
            导出日志
          </el-button>
          <el-button type="warning" @click="handleCleanup">
            <el-icon><Delete /></el-icon>
            清理日志
          </el-button>
        </div>
        <div>
          <el-switch
            v-model="autoRefresh"
            active-text="实时刷新"
            @change="handleAutoRefreshChange"
          />
          <el-button @click="handleRefresh" :loading="loading" class="ml-10">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 日志列表 -->
      <el-card>
        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          border
          :row-class-name="getRowClassName"
        >
          <el-table-column label="时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.monitorSysGenServerLogTimestamp) }}
            </template>
          </el-table-column>
          <el-table-column label="服务器" width="150">
            <template #default="{ row }">
              {{ getServerName(row.monitorSysGenServerId) }}
            </template>
          </el-table-column>
          <el-table-column label="级别" width="80">
            <template #default="{ row }">
              <el-tag :type="getLevelColor(row.monitorSysGenServerLogLevel)" size="small">
                {{ row.monitorSysGenServerLogLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="120">
            <template #default="{ row }">
              <el-text class="log-source">{{ row.monitorSysGenServerLogSource }}</el-text>
            </template>
          </el-table-column>
          <el-table-column label="日志内容" min-width="400">
            <template #default="{ row }">
              <div class="log-content" @click="handleViewDetail(row)">
                <el-text :class="getContentClass(row.monitorSysGenServerLogLevel)">
                  {{ row.monitorSysGenServerLogContent.substring(0, 200) }}
                  <span v-if="row.monitorSysGenServerLogContent.length > 200">...</span>
                </el-text>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleViewDetail(row)">
                详情
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
            :page-sizes="[20, 50, 100, 200]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 日志详情对话框 -->
    <LogDetailDialog
      v-model:visible="detailDialogVisible"
      :log-data="currentLog"
    />

    <!-- 日志配置对话框 -->
    <LogConfigDialog
      v-model:visible="configDialogVisible"
      @success="handleConfigSuccess"
    />

    <!-- 清理日志对话框 -->
    <LogCleanupDialog
      v-model:visible="cleanupDialogVisible"
      @success="handleCleanupSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Setting,
  Download,
  Delete,
  Document,
  Warning,
  InfoFilled,
  CircleCheck
} from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { serverApi } from '@/api/server'
import { logApi } from '@/api/log'
import { useWebSocketStore } from '@/stores/websocket'
import { formatDateTime } from '@/utils'
import type { Server, ServerLog } from '@/types'
import LogDetailDialog from './components/LogDetailDialog.vue'
import LogConfigDialog from './components/LogConfigDialog.vue'
import LogCleanupDialog from './components/LogCleanupDialog.vue'

// 注册ECharts组件
use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 响应式数据
const loading = ref(false)
const exporting = ref(false)
const autoRefresh = ref(false)
const tableData = ref<ServerLog[]>([])
const serverList = ref<Server[]>([])
const detailDialogVisible = ref(false)
const configDialogVisible = ref(false)
const cleanupDialogVisible = ref(false)
const currentLog = ref<ServerLog>()
const levelChartOption = ref({})
const trendChartOption = ref({})

// 搜索表单
const searchForm = reactive({
  serverId: undefined as number | undefined,
  level: '',
  source: '',
  content: '',
  timeRange: [] as string[]
})

// 分页
const pagination = reactive({
  current: 1,
  size: 50,
  total: 0
})

// WebSocket
const wsStore = useWebSocketStore()

// 自动刷新定时器
let refreshTimer: number | null = null

// 统计数据
const statistics = computed(() => {
  const total = tableData.value.length
  const errorCount = tableData.value.filter(item => item.monitorSysGenServerLogLevel === 'ERROR').length
  const warnCount = tableData.value.filter(item => item.monitorSysGenServerLogLevel === 'WARN').length
  const infoCount = tableData.value.filter(item => item.monitorSysGenServerLogLevel === 'INFO').length

  return [
    {
      key: 'total',
      title: '日志总数',
      value: total,
      color: '#409eff',
      icon: 'Document'
    },
    {
      key: 'error',
      title: '错误日志',
      value: errorCount,
      color: '#f56c6c',
      icon: 'Warning'
    },
    {
      key: 'warn',
      title: '警告日志',
      value: warnCount,
      color: '#e6a23c',
      icon: 'Warning'
    },
    {
      key: 'info',
      title: '信息日志',
      value: infoCount,
      color: '#67c23a',
      icon: 'InfoFilled'
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
      level: searchForm.level,
      source: searchForm.source,
      content: searchForm.content,
      startTime: searchForm.timeRange[0],
      endTime: searchForm.timeRange[1]
    }
    
    const response = await logApi.getLogList(params)
    if (response.data) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    console.error('获取日志列表失败:', error)
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

const fetchChartData = async () => {
  try {
    // 获取日志级别统计
    const levelResponse = await logApi.getLogLevelStatistics()
    if (levelResponse.data) {
      updateLevelChart(levelResponse.data)
    }

    // 获取日志趋势
    const trendResponse = await logApi.getLogTrend({
      interval: '1h'
    })
    if (trendResponse.data) {
      updateTrendChart(trendResponse.data)
    }
  } catch (error) {
    console.error('获取图表数据失败:', error)
  }
}

const updateLevelChart = (data: any[]) => {
  levelChartOption.value = {
    title: {
      text: '日志级别分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 0
    },
    series: [
      {
        name: '日志级别',
        type: 'pie',
        radius: ['40%', '70%'],
        data: data.map(item => ({
          name: item.level,
          value: item.count,
          itemStyle: {
            color: getLevelChartColor(item.level)
          }
        }))
      }
    ]
  }
}

const updateTrendChart = (data: any[]) => {
  const times = data.map(item => item.time)
  const counts = data.map(item => item.count)
  
  trendChartOption.value = {
    title: {
      text: '日志趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
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
        type: 'line',
        data: counts,
        smooth: true,
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(64, 158, 255, 0.3)'
              },
              {
                offset: 1,
                color: 'rgba(64, 158, 255, 0.1)'
              }
            ]
          }
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
    level: '',
    source: '',
    content: '',
    timeRange: []
  })
  handleSearch()
}

const handleRefresh = () => {
  fetchData()
  fetchChartData()
}

const handleViewDetail = (row: ServerLog) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

const handleLogConfig = () => {
  configDialogVisible.value = true
}

const handleExport = async () => {
  try {
    exporting.value = true
    await logApi.exportLogs({
      serverId: searchForm.serverId,
      level: searchForm.level,
      startTime: searchForm.timeRange[0],
      endTime: searchForm.timeRange[1],
      format: 'csv'
    })
    ElMessage.success('日志导出成功')
  } catch (error) {
    console.error('导出日志失败:', error)
  } finally {
    exporting.value = false
  }
}

const handleCleanup = () => {
  cleanupDialogVisible.value = true
}

const handleConfigSuccess = () => {
  configDialogVisible.value = false
  ElMessage.success('日志配置保存成功')
}

const handleCleanupSuccess = () => {
  cleanupDialogVisible.value = false
  fetchData()
  fetchChartData()
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

const getLevelColor = (level: string) => {
  const colorMap: Record<string, string> = {
    'DEBUG': 'info',
    'INFO': 'success',
    'WARN': 'warning',
    'ERROR': 'danger',
    'FATAL': 'danger'
  }
  return colorMap[level] || 'info'
}

const getLevelChartColor = (level: string) => {
  const colorMap: Record<string, string> = {
    'DEBUG': '#909399',
    'INFO': '#67c23a',
    'WARN': '#e6a23c',
    'ERROR': '#f56c6c',
    'FATAL': '#f56c6c'
  }
  return colorMap[level] || '#909399'
}

const getContentClass = (level: string) => {
  return `log-content-${level.toLowerCase()}`
}

const getRowClassName = ({ row }: { row: ServerLog }) => {
  return `log-row-${row.monitorSysGenServerLogLevel.toLowerCase()}`
}

// 自动刷新
const startAutoRefresh = () => {
  if (refreshTimer) return
  
  refreshTimer = window.setInterval(() => {
    fetchData()
  }, 10000) // 10秒刷新一次
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 监听WebSocket消息
wsStore.on('log-update', (data) => {
  // 如果是实时刷新模式，添加新日志到列表顶部
  if (autoRefresh.value && data.log) {
    tableData.value.unshift(data.log)
    // 保持列表长度
    if (tableData.value.length > pagination.size) {
      tableData.value = tableData.value.slice(0, pagination.size)
    }
  }
})

// 生命周期
onMounted(() => {
  fetchServerList()
  fetchData()
  fetchChartData()
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
  font-size: 14px;
}

.log-content {
  cursor: pointer;
  
  &:hover {
    background-color: var(--el-fill-color-light);
  }
}

.log-source {
  font-family: monospace;
  font-size: 12px;
}

// 日志级别样式
.log-content-error {
  color: var(--el-color-danger);
}

.log-content-warn {
  color: var(--el-color-warning);
}

.log-content-info {
  color: var(--el-color-success);
}

.log-content-debug {
  color: var(--el-color-info);
}

// 表格行样式
:deep(.log-row-error) {
  background-color: rgba(245, 108, 108, 0.05);
}

:deep(.log-row-warn) {
  background-color: rgba(230, 162, 60, 0.05);
}
</style>
