<template>
  <div class="app-container">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header mb-20">
        <h2>服务器脚本管理</h2>
        <p class="text-muted">管理和执行服务器脚本</p>
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
          <el-form-item label="脚本名称">
            <el-input
              v-model="searchForm.scriptName"
              placeholder="请输入脚本名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="脚本类型">
            <el-select v-model="searchForm.scriptType" placeholder="请选择类型" clearable>
              <el-option label="Shell脚本" value="SHELL" />
              <el-option label="Python脚本" value="PYTHON" />
              <el-option label="PowerShell脚本" value="POWERSHELL" />
              <el-option label="批处理脚本" value="BATCH" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
              <el-option
                v-for="category in categories"
                :key="category"
                :label="category"
                :value="category"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="创建用户">
            <el-input
              v-model="searchForm.createUser"
              placeholder="请输入用户"
              clearable
            />
          </el-form-item>
          <el-form-item label="模板">
            <el-select v-model="searchForm.isTemplate" placeholder="请选择" clearable>
              <el-option label="是" :value="1" />
              <el-option label="否" :value="0" />
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
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建脚本
          </el-button>
          <el-button type="success" @click="handleViewTemplates">
            <el-icon><Collection /></el-icon>
            脚本模板
          </el-button>
          <el-button
            type="danger"
            :disabled="!selectedRows.length"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
        <div>
          <el-button @click="handleRefresh" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 脚本列表 -->
      <el-card>
        <el-table
          v-loading="loading"
          :data="tableData"
          @selection-change="handleSelectionChange"
          stripe
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="monitorSysGenServerScriptName" label="脚本名称" min-width="150">
            <template #default="{ row }">
              <div class="script-name">
                <el-link type="primary" @click="handleView(row)">
                  {{ row.monitorSysGenServerScriptName }}
                </el-link>
                <div class="script-tags">
                  <el-tag v-if="row.monitorSysGenServerScriptIsTemplate" size="small" type="success">
                    模板
                  </el-tag>
                  <el-tag v-if="row.monitorSysGenServerScriptIsPublic" size="small" type="info">
                    公开
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="脚本类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getTypeColor(row.monitorSysGenServerScriptType)">
                {{ getStatusText(row.monitorSysGenServerScriptType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="monitorSysGenServerScriptCategory" label="分类" width="100" />
          <el-table-column prop="monitorSysGenServerScriptLanguage" label="语言" width="80" />
          <el-table-column label="执行统计" width="120">
            <template #default="{ row }">
              <div class="execution-stats">
                <span>{{ row.monitorSysGenServerScriptExecutionCount || 0 }}</span>
                <span class="text-muted">/</span>
                <span class="text-success">{{ row.monitorSysGenServerScriptSuccessCount || 0 }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="成功率" width="100">
            <template #default="{ row }">
              <span v-if="row.monitorSysGenServerScriptExecutionCount > 0">
                {{ getSuccessRate(row) }}%
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="最后执行" width="180">
            <template #default="{ row }">
              <span v-if="row.monitorSysGenServerScriptLastExecutionTime">
                {{ formatDateTime(row.monitorSysGenServerScriptLastExecutionTime) }}
              </span>
              <span v-else class="text-muted">未执行</span>
            </template>
          </el-table-column>
          <el-table-column prop="monitorSysGenServerScriptCreateUser" label="创建用户" width="100" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.monitorSysGenServerScriptStatus === 1 ? 'success' : 'danger'">
                {{ row.monitorSysGenServerScriptStatus === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleExecute(row)">
                执行
              </el-button>
              <el-button type="info" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button type="warning" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-dropdown @command="(command) => handleDropdownCommand(command, row)">
                <el-button size="small">
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="duplicate">复制</el-dropdown-item>
                    <el-dropdown-item command="history">执行历史</el-dropdown-item>
                    <el-dropdown-item command="export">导出</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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

    <!-- 脚本编辑对话框 -->
    <ScriptEditorDialog
      v-model:visible="editorDialogVisible"
      :script-data="currentScript"
      :is-edit="isEdit"
      @success="handleEditorSuccess"
    />

    <!-- 脚本执行对话框 -->
    <ScriptExecuteDialog
      v-model:visible="executeDialogVisible"
      :script-data="currentScript"
      @success="handleExecuteSuccess"
    />

    <!-- 脚本详情对话框 -->
    <ScriptDetailDialog
      v-model:visible="detailDialogVisible"
      :script-data="currentScript"
    />

    <!-- 执行历史对话框 -->
    <ScriptHistoryDialog
      v-model:visible="historyDialogVisible"
      :script-id="currentScriptId"
      :script-name="currentScriptName"
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
  Collection,
  DocumentCopy,
  VideoPlay,
  CircleCheck,
  Warning,
  ArrowDown
} from '@element-plus/icons-vue'
import { scriptApi } from '@/api/script'
import { formatDateTime, getStatusText } from '@/utils'
import type { ServerScript } from '@/types'
import ScriptEditorDialog from './components/ScriptEditorDialog.vue'
import ScriptExecuteDialog from './components/ScriptExecuteDialog.vue'
import ScriptDetailDialog from './components/ScriptDetailDialog.vue'
import ScriptHistoryDialog from './components/ScriptHistoryDialog.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<ServerScript[]>([])
const selectedRows = ref<ServerScript[]>([])
const categories = ref<string[]>([])
const editorDialogVisible = ref(false)
const executeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const isEdit = ref(false)
const currentScript = ref<Partial<ServerScript>>({})
const currentScriptId = ref<number>()
const currentScriptName = ref<string>()

// 搜索表单
const searchForm = reactive({
  scriptName: '',
  scriptType: '',
  category: '',
  createUser: '',
  isTemplate: undefined as number | undefined
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 统计数据
const statistics = computed(() => {
  const total = tableData.value.length
  const templates = tableData.value.filter(item => item.monitorSysGenServerScriptIsTemplate === 1).length
  const enabled = tableData.value.filter(item => item.monitorSysGenServerScriptStatus === 1).length
  const totalExecutions = tableData.value.reduce((sum, item) => sum + (item.monitorSysGenServerScriptExecutionCount || 0), 0)

  return [
    {
      key: 'total',
      title: '脚本总数',
      value: total,
      color: '#409eff',
      icon: 'DocumentCopy'
    },
    {
      key: 'templates',
      title: '脚本模板',
      value: templates,
      color: '#67c23a',
      icon: 'Collection'
    },
    {
      key: 'enabled',
      title: '启用脚本',
      value: enabled,
      color: '#67c23a',
      icon: 'CircleCheck'
    },
    {
      key: 'executions',
      title: '总执行次数',
      value: totalExecutions,
      color: '#e6a23c',
      icon: 'VideoPlay'
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
    
    const response = await scriptApi.getScriptList(params)
    if (response.data) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    console.error('获取脚本列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const response = await scriptApi.getScriptCategories()
    if (response.data) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('获取脚本分类失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    scriptName: '',
    scriptType: '',
    category: '',
    createUser: '',
    isTemplate: undefined
  })
  handleSearch()
}

const handleRefresh = () => {
  fetchData()
}

const handleCreate = () => {
  currentScript.value = {}
  isEdit.value = false
  editorDialogVisible.value = true
}

const handleEdit = (row: ServerScript) => {
  currentScript.value = { ...row }
  isEdit.value = true
  editorDialogVisible.value = true
}

const handleView = (row: ServerScript) => {
  currentScript.value = row
  detailDialogVisible.value = true
}

const handleExecute = (row: ServerScript) => {
  currentScript.value = row
  executeDialogVisible.value = true
}

const handleViewTemplates = () => {
  searchForm.isTemplate = 1
  handleSearch()
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个脚本吗？`,
      '确认批量删除',
      { type: 'warning' }
    )
    
    const ids = selectedRows.value.map(row => row.monitorSysGenServerScriptId)
    await scriptApi.batchDeleteScripts(ids)
    ElMessage.success('批量删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

const handleDropdownCommand = async (command: string, row: ServerScript) => {
  switch (command) {
    case 'duplicate':
      await handleDuplicate(row)
      break
    case 'history':
      handleViewHistory(row)
      break
    case 'export':
      await handleExport(row)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

const handleDuplicate = async (row: ServerScript) => {
  try {
    await scriptApi.duplicateScript(row.monitorSysGenServerScriptId)
    ElMessage.success('脚本复制成功')
    fetchData()
  } catch (error) {
    console.error('复制脚本失败:', error)
  }
}

const handleViewHistory = (row: ServerScript) => {
  currentScriptId.value = row.monitorSysGenServerScriptId
  currentScriptName.value = row.monitorSysGenServerScriptName
  historyDialogVisible.value = true
}

const handleExport = async (row: ServerScript) => {
  try {
    await scriptApi.exportScript(row.monitorSysGenServerScriptId)
    ElMessage.success('脚本导出成功')
  } catch (error) {
    console.error('导出脚本失败:', error)
  }
}

const handleDelete = async (row: ServerScript) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除脚本 "${row.monitorSysGenServerScriptName}" 吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await scriptApi.deleteScript(row.monitorSysGenServerScriptId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除脚本失败:', error)
    }
  }
}

const handleSelectionChange = (selection: ServerScript[]) => {
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

const handleEditorSuccess = () => {
  editorDialogVisible.value = false
  fetchData()
}

const handleExecuteSuccess = () => {
  executeDialogVisible.value = false
  // 可以刷新执行统计
}

// 工具方法
const getTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'SHELL': 'primary',
    'PYTHON': 'success',
    'POWERSHELL': 'warning',
    'BATCH': 'info'
  }
  return colorMap[type] || 'info'
}

const getSuccessRate = (row: ServerScript) => {
  if (!row.monitorSysGenServerScriptExecutionCount || row.monitorSysGenServerScriptExecutionCount === 0) {
    return 0
  }
  const rate = ((row.monitorSysGenServerScriptSuccessCount || 0) / row.monitorSysGenServerScriptExecutionCount) * 100
  return Math.round(rate)
}

// 生命周期
onMounted(() => {
  fetchData()
  fetchCategories()
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

.text-success {
  color: var(--el-color-success);
}

.script-name {
  .script-tags {
    margin-top: 4px;
    
    .el-tag {
      margin-right: 4px;
    }
  }
}

.execution-stats {
  font-family: monospace;
  font-size: 12px;
}
</style>
