<template>
  <div class="app-container">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header mb-20">
        <h2>文件上传管理</h2>
        <p class="text-muted">管理和监控文件上传任务</p>
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
          <el-form-item label="任务名称">
            <el-input
              v-model="searchForm.taskName"
              placeholder="请输入任务名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待处理" value="PENDING" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="失败" value="FAILED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="上传模式">
            <el-select v-model="searchForm.mode" placeholder="请选择模式" clearable>
              <el-option label="立即上传" value="IMMEDIATE" />
              <el-option label="定时上传" value="SCHEDULED" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建用户">
            <el-input
              v-model="searchForm.createUser"
              placeholder="请输入用户"
              clearable
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
          <el-button type="primary" @click="handleCreateTask">
            <el-icon><Plus /></el-icon>
            创建上传任务
          </el-button>
          <el-button
            type="success"
            :disabled="!selectedRows.length"
            @click="handleBatchStart"
          >
            <el-icon><VideoPlay /></el-icon>
            批量启动
          </el-button>
          <el-button
            type="warning"
            :disabled="!selectedRows.length"
            @click="handleBatchPause"
          >
            <el-icon><VideoPause /></el-icon>
            批量暂停
          </el-button>
          <el-button
            type="danger"
            :disabled="!selectedRows.length"
            @click="handleBatchCancel"
          >
            <el-icon><Close /></el-icon>
            批量取消
          </el-button>
        </div>
        <div>
          <el-button @click="handleRefresh" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 任务列表 -->
      <el-card>
        <el-table
          v-loading="loading"
          :data="tableData"
          @selection-change="handleSelectionChange"
          stripe
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="monitorSysGenFileUploadTaskName" label="任务名称" min-width="150">
            <template #default="{ row }">
              <el-link type="primary" @click="handleViewDetail(row)">
                {{ row.monitorSysGenFileUploadTaskName }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="monitorSysGenFileUploadFileName" label="文件名" min-width="200" />
          <el-table-column label="文件大小" width="100">
            <template #default="{ row }">
              {{ formatFileSize(row.monitorSysGenFileUploadFileSize) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.monitorSysGenFileUploadStatus)">
                {{ getStatusText(row.monitorSysGenFileUploadStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="150">
            <template #default="{ row }">
              <div v-if="row.monitorSysGenFileUploadStatus === 'PROCESSING'">
                <el-progress
                  :percentage="getTaskProgress(row.monitorSysGenFileUploadTaskId)"
                  :status="getProgressStatus(row.monitorSysGenFileUploadStatus)"
                />
              </div>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="上传模式" width="100">
            <template #default="{ row }">
              {{ getStatusText(row.monitorSysGenFileUploadMode) }}
            </template>
          </el-table-column>
          <el-table-column prop="monitorSysGenFileUploadCreateUser" label="创建用户" width="100" />
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.monitorSysGenFileUploadCreateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.monitorSysGenFileUploadStatus === 'PENDING'"
                type="success"
                size="small"
                @click="handleStart(row)"
              >
                启动
              </el-button>
              <el-button
                v-if="row.monitorSysGenFileUploadStatus === 'PROCESSING'"
                type="warning"
                size="small"
                @click="handlePause(row)"
              >
                暂停
              </el-button>
              <el-button
                v-if="['PENDING', 'PROCESSING'].includes(row.monitorSysGenFileUploadStatus)"
                type="danger"
                size="small"
                @click="handleCancel(row)"
              >
                取消
              </el-button>
              <el-button
                v-if="row.monitorSysGenFileUploadStatus === 'FAILED'"
                type="primary"
                size="small"
                @click="handleRetry(row)"
              >
                重试
              </el-button>
              <el-button type="info" size="small" @click="handleViewDetail(row)">
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
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 创建任务对话框 -->
    <UploadTaskDialog
      v-model:visible="taskDialogVisible"
      @success="handleTaskSuccess"
    />

    <!-- 任务详情对话框 -->
    <UploadTaskDetailDialog
      v-model:visible="detailDialogVisible"
      :task-data="currentTask"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  VideoPlay,
  VideoPause,
  Close,
  Upload,
  Clock,
  CircleCheck,
  Warning
} from '@element-plus/icons-vue'
import { uploadTaskApi } from '@/api/upload'
import { useWebSocketStore } from '@/stores/websocket'
import { formatDateTime, formatFileSize, getStatusColor, getStatusText } from '@/utils'
import type { FileUploadTask } from '@/types'
import UploadTaskDialog from './components/UploadTaskDialog.vue'
import UploadTaskDetailDialog from './components/UploadTaskDetailDialog.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<FileUploadTask[]>([])
const selectedRows = ref<FileUploadTask[]>([])
const taskDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentTask = ref<FileUploadTask>()
const progressMap = ref<Map<number, number>>(new Map())

// 搜索表单
const searchForm = reactive({
  taskName: '',
  status: '',
  mode: '',
  createUser: ''
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
  const pending = tableData.value.filter(item => item.monitorSysGenFileUploadStatus === 'PENDING').length
  const processing = tableData.value.filter(item => item.monitorSysGenFileUploadStatus === 'PROCESSING').length
  const completed = tableData.value.filter(item => item.monitorSysGenFileUploadStatus === 'COMPLETED').length

  return [
    {
      key: 'total',
      title: '任务总数',
      value: total,
      color: '#409eff',
      icon: 'Upload'
    },
    {
      key: 'pending',
      title: '待处理',
      value: pending,
      color: '#909399',
      icon: 'Clock'
    },
    {
      key: 'processing',
      title: '处理中',
      value: processing,
      color: '#e6a23c',
      icon: 'VideoPlay'
    },
    {
      key: 'completed',
      title: '已完成',
      value: completed,
      color: '#67c23a',
      icon: 'CircleCheck'
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
    
    const response = await uploadTaskApi.getTaskList(params)
    if (response.data) {
      tableData.value = response.data.records
      pagination.total = response.data.total
      
      // 订阅处理中任务的进度更新
      const processingTasks = tableData.value.filter(task => 
        task.monitorSysGenFileUploadStatus === 'PROCESSING'
      )
      processingTasks.forEach(task => {
        wsStore.subscribeUploadProgress(task.monitorSysGenFileUploadTaskId)
      })
    }
  } catch (error) {
    console.error('获取上传任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    taskName: '',
    status: '',
    mode: '',
    createUser: ''
  })
  handleSearch()
}

const handleRefresh = () => {
  fetchData()
}

const handleCreateTask = () => {
  taskDialogVisible.value = true
}

const handleStart = async (row: FileUploadTask) => {
  try {
    await uploadTaskApi.startTask(row.monitorSysGenFileUploadTaskId)
    ElMessage.success('任务启动成功')
    fetchData()
  } catch (error) {
    console.error('启动任务失败:', error)
  }
}

const handlePause = async (row: FileUploadTask) => {
  try {
    await uploadTaskApi.pauseTask(row.monitorSysGenFileUploadTaskId)
    ElMessage.success('任务暂停成功')
    fetchData()
  } catch (error) {
    console.error('暂停任务失败:', error)
  }
}

const handleCancel = async (row: FileUploadTask) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消任务 "${row.monitorSysGenFileUploadTaskName}" 吗？`,
      '确认取消',
      { type: 'warning' }
    )
    
    await uploadTaskApi.cancelTask(row.monitorSysGenFileUploadTaskId)
    ElMessage.success('任务取消成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消任务失败:', error)
    }
  }
}

const handleRetry = async (row: FileUploadTask) => {
  try {
    await uploadTaskApi.retryTask(row.monitorSysGenFileUploadTaskId)
    ElMessage.success('任务重试成功')
    fetchData()
  } catch (error) {
    console.error('重试任务失败:', error)
  }
}

const handleBatchStart = async () => {
  try {
    const taskIds = selectedRows.value.map(row => row.monitorSysGenFileUploadTaskId)
    await uploadTaskApi.batchOperateTasks(taskIds, 'start')
    ElMessage.success('批量启动成功')
    fetchData()
  } catch (error) {
    console.error('批量启动失败:', error)
  }
}

const handleBatchPause = async () => {
  try {
    const taskIds = selectedRows.value.map(row => row.monitorSysGenFileUploadTaskId)
    await uploadTaskApi.batchOperateTasks(taskIds, 'pause')
    ElMessage.success('批量暂停成功')
    fetchData()
  } catch (error) {
    console.error('批量暂停失败:', error)
  }
}

const handleBatchCancel = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要取消选中的 ${selectedRows.value.length} 个任务吗？`,
      '确认批量取消',
      { type: 'warning' }
    )
    
    const taskIds = selectedRows.value.map(row => row.monitorSysGenFileUploadTaskId)
    await uploadTaskApi.batchOperateTasks(taskIds, 'cancel')
    ElMessage.success('批量取消成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量取消失败:', error)
    }
  }
}

const handleViewDetail = (row: FileUploadTask) => {
  currentTask.value = row
  detailDialogVisible.value = true
}

const handleSelectionChange = (selection: FileUploadTask[]) => {
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

const handleTaskSuccess = () => {
  taskDialogVisible.value = false
  fetchData()
}

// 工具方法
const getTaskProgress = (taskId: number) => {
  return progressMap.value.get(taskId) || 0
}

const getProgressStatus = (status: string) => {
  if (status === 'PROCESSING') return undefined
  if (status === 'COMPLETED') return 'success'
  if (status === 'FAILED') return 'exception'
  return undefined
}

// 监听WebSocket消息
wsStore.on('upload-progress', (data) => {
  if (data.taskId && data.progress !== undefined) {
    progressMap.value.set(data.taskId, data.progress)
  }
})

// 生命周期
onMounted(() => {
  fetchData()
})

onUnmounted(() => {
  // 清理WebSocket订阅
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
