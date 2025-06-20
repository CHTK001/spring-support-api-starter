<template>
  <el-dialog
    :model-value="visible"
    :title="`${serverName} - 连接历史记录`"
    width="800px"
    @update:model-value="$emit('update:visible', $event)"
    @open="handleOpen"
  >
    <!-- 时间范围选择 -->
    <div class="mb-20">
      <el-date-picker
        v-model="timeRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        format="YYYY-MM-DD HH:mm:ss"
        value-format="YYYY-MM-DD HH:mm:ss"
        @change="fetchData"
      />
      <el-button type="primary" @click="fetchData" :loading="loading" class="ml-10">
        查询
      </el-button>
    </div>

    <!-- 响应时间趋势图 -->
    <el-card class="mb-20">
      <template #header>
        <span>响应时间趋势</span>
      </template>
      <div style="height: 200px">
        <v-chart :option="chartOption" autoresize />
      </div>
    </el-card>

    <!-- 历史记录表格 -->
    <el-table
      v-loading="loading"
      :data="historyData"
      stripe
      border
      max-height="300"
    >
      <el-table-column label="检查时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.monitorSysGenServerConnectionLastCheckTime) }}
        </template>
      </el-table-column>
      <el-table-column label="连接状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusColor(row.monitorSysGenServerConnectionStatus)">
            {{ getStatusText(row.monitorSysGenServerConnectionStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="响应时间" width="100">
        <template #default="{ row }">
          <span v-if="row.monitorSysGenServerConnectionResponseTime">
            {{ row.monitorSysGenServerConnectionResponseTime }}ms
          </span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="错误信息" min-width="200">
        <template #default="{ row }">
          <span v-if="row.monitorSysGenServerConnectionErrorMessage" class="error-message">
            {{ row.monitorSysGenServerConnectionErrorMessage }}
          </span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { connectionStatusApi } from '@/api/server'
import { formatDateTime, getStatusColor, getStatusText } from '@/utils'
import type { ServerConnectionStatus } from '@/types'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent
])

// Props
interface Props {
  visible: boolean
  serverId?: number
  serverName?: string
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

// 响应式数据
const loading = ref(false)
const historyData = ref<ServerConnectionStatus[]>([])
const timeRange = ref<string[]>([])
const chartOption = ref({})

// 方法
const fetchData = async () => {
  if (!props.serverId) return
  
  loading.value = true
  try {
    const params = {
      startTime: timeRange.value[0],
      endTime: timeRange.value[1],
      limit: 100
    }
    
    const response = await connectionStatusApi.getConnectionHistory(props.serverId, params)
    if (response.data) {
      historyData.value = response.data
      updateChart()
    }
  } catch (error) {
    console.error('获取连接历史失败:', error)
  } finally {
    loading.value = false
  }
}

const updateChart = () => {
  const times = historyData.value.map(item => 
    formatDateTime(item.monitorSysGenServerConnectionLastCheckTime, 'MM-DD HH:mm')
  )
  const responseTimes = historyData.value.map(item => 
    item.monitorSysGenServerConnectionResponseTime || 0
  )
  
  chartOption.value = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        return `时间: ${data.name}<br/>响应时间: ${data.value}ms`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '响应时间(ms)'
    },
    series: [
      {
        type: 'line',
        data: responseTimes,
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

const handleOpen = () => {
  // 设置默认时间范围为最近24小时
  const now = new Date()
  const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000)
  
  timeRange.value = [
    yesterday.toISOString().slice(0, 19).replace('T', ' '),
    now.toISOString().slice(0, 19).replace('T', ' ')
  ]
  
  fetchData()
}

// 监听serverId变化
watch(
  () => props.serverId,
  (newId) => {
    if (newId && props.visible) {
      fetchData()
    }
  }
)
</script>

<style lang="scss" scoped>
.dialog-footer {
  text-align: right;
}

.text-muted {
  color: var(--el-text-color-regular);
}

.error-message {
  color: var(--el-color-danger);
  font-size: 12px;
}
</style>
