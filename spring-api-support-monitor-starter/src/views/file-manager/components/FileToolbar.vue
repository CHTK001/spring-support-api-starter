<template>
  <div class="file-toolbar">
    <!-- 主要操作按钮 -->
    <div class="toolbar-main">
      <el-button-group>
        <el-button
          type="primary"
          :icon="Upload"
          @click="handleUpload"
          :disabled="!canUpload"
        >
          上传文件
        </el-button>
        <el-button
          :icon="FolderAdd"
          @click="handleCreateFolder"
          :disabled="!canCreateFolder"
        >
          新建文件夹
        </el-button>
      </el-button-group>

      <el-button-group class="ml-2">
        <el-button
          :icon="Download"
          @click="handleDownload"
          :disabled="!canDownload"
        >
          下载
        </el-button>
        <el-button
          :icon="Edit"
          @click="handleRename"
          :disabled="!canRename"
        >
          重命名
        </el-button>
        <el-button
          :icon="Delete"
          type="danger"
          @click="handleDelete"
          :disabled="!canDelete"
        >
          删除
        </el-button>
      </el-button-group>

      <el-button-group class="ml-2">
        <el-button
          :icon="DocumentCopy"
          @click="handleCopy"
          :disabled="!canCopy"
        >
          复制
        </el-button>
        <el-button
          :icon="Scissors"
          @click="handleCut"
          :disabled="!canCut"
        >
          剪切
        </el-button>
        <el-button
          :icon="Document"
          @click="handlePaste"
          :disabled="!canPaste"
        >
          粘贴
        </el-button>
      </el-button-group>
    </div>

    <!-- 批量操作 -->
    <div class="toolbar-batch" v-if="selectedFiles.length > 0">
      <div class="batch-info">
        <el-tag type="info" size="small">
          已选择 {{ selectedFiles.length }} 个文件
        </el-tag>
        <el-button
          text
          type="primary"
          size="small"
          @click="clearSelection"
        >
          清除选择
        </el-button>
      </div>

      <el-button-group size="small">
        <el-button
          :icon="Download"
          @click="handleBatchDownload"
        >
          批量下载
        </el-button>
        <el-button
          :icon="FolderOpened"
          @click="handleBatchMove"
        >
          批量移动
        </el-button>
        <el-button
          :icon="Delete"
          type="danger"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </el-button-group>
    </div>

    <!-- 右侧工具 -->
    <div class="toolbar-tools">
      <!-- 搜索框 -->
      <el-input
        v-model="searchQuery"
        placeholder="搜索文件..."
        :prefix-icon="Search"
        clearable
        class="search-input"
        @input="handleSearch"
      />

      <!-- 排序选择 -->
      <el-select
        v-model="sortBy"
        placeholder="排序方式"
        class="sort-select"
        @change="handleSortChange"
      >
        <el-option label="名称" value="name" />
        <el-option label="大小" value="size" />
        <el-option label="类型" value="type" />
        <el-option label="修改时间" value="modifiedTime" />
      </el-select>

      <!-- 排序方向 -->
      <el-button
        :icon="sortOrder === 'asc' ? SortUp : SortDown"
        @click="toggleSortOrder"
        title="切换排序方向"
      />

      <!-- 视图模式切换 -->
      <el-button-group>
        <el-button
          :type="viewMode === 'list' ? 'primary' : 'default'"
          :icon="List"
          @click="setViewMode('list')"
          title="列表视图"
        />
        <el-button
          :type="viewMode === 'grid' ? 'primary' : 'default'"
          :icon="Grid"
          @click="setViewMode('grid')"
          title="网格视图"
        />
      </el-button-group>

      <!-- 刷新按钮 -->
      <el-button
        :icon="Refresh"
        @click="handleRefresh"
        title="刷新"
      />

      <!-- 更多操作 -->
      <el-dropdown trigger="click">
        <el-button :icon="More" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              :icon="Setting"
              @click="handleSettings"
            >
              设置
            </el-dropdown-item>
            <el-dropdown-item
              :icon="InfoFilled"
              @click="handleProperties"
              :disabled="selectedFiles.length !== 1"
            >
              属性
            </el-dropdown-item>
            <el-dropdown-item divided>
              <el-checkbox
                v-model="showHiddenFiles"
                @change="handleToggleHidden"
              >
                显示隐藏文件
              </el-checkbox>
            </el-dropdown-item>
            <el-dropdown-item>
              <el-checkbox
                v-model="showFileExtensions"
                @change="handleToggleExtensions"
              >
                显示文件扩展名
              </el-checkbox>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  Upload,
  Download,
  Delete,
  Edit,
  FolderAdd,
  FolderOpened,
  DocumentCopy,
  Document,
  Scissors,
  Search,
  List,
  Grid,
  Refresh,
  More,
  Setting,
  InfoFilled,
  SortUp,
  SortDown
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
const props = defineProps({
  selectedFiles: {
    type: Array,
    default: () => []
  },
  currentPath: {
    type: String,
    default: '/'
  },
  viewMode: {
    type: String,
    default: 'list'
  },
  sortBy: {
    type: String,
    default: 'name'
  },
  sortOrder: {
    type: String,
    default: 'asc'
  },
  canUpload: {
    type: Boolean,
    default: true
  },
  canCreateFolder: {
    type: Boolean,
    default: true
  },
  clipboard: {
    type: Object,
    default: () => ({ files: [], operation: null })
  }
})

// Emits
const emit = defineEmits([
  'upload',
  'create-folder',
  'download',
  'rename',
  'delete',
  'copy',
  'cut',
  'paste',
  'batch-download',
  'batch-move',
  'batch-delete',
  'clear-selection',
  'search',
  'sort-change',
  'view-mode-change',
  'refresh',
  'settings',
  'properties',
  'toggle-hidden',
  'toggle-extensions'
])

// 响应式数据
const searchQuery = ref('')
const sortBy = ref(props.sortBy)
const sortOrder = ref(props.sortOrder)
const showHiddenFiles = ref(false)
const showFileExtensions = ref(true)

// 计算属性
const canDownload = computed(() => props.selectedFiles.length > 0)
const canRename = computed(() => props.selectedFiles.length === 1)
const canDelete = computed(() => props.selectedFiles.length > 0)
const canCopy = computed(() => props.selectedFiles.length > 0)
const canCut = computed(() => props.selectedFiles.length > 0)
const canPaste = computed(() => {
  return props.clipboard.files.length > 0 && props.clipboard.operation
})

// 监听props变化
watch(() => props.sortBy, (newVal) => {
  sortBy.value = newVal
})

watch(() => props.sortOrder, (newVal) => {
  sortOrder.value = newVal
})

// 方法
const handleUpload = () => {
  emit('upload')
}

const handleCreateFolder = () => {
  emit('create-folder')
}

const handleDownload = () => {
  if (props.selectedFiles.length === 0) {
    ElMessage.warning('请先选择要下载的文件')
    return
  }
  emit('download', props.selectedFiles)
}

const handleRename = () => {
  if (props.selectedFiles.length !== 1) {
    ElMessage.warning('请选择一个文件进行重命名')
    return
  }
  emit('rename', props.selectedFiles[0])
}

const handleDelete = async () => {
  if (props.selectedFiles.length === 0) {
    ElMessage.warning('请先选择要删除的文件')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${props.selectedFiles.length} 个文件吗？`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    emit('delete', props.selectedFiles)
  } catch {
    // 用户取消删除
  }
}

const handleCopy = () => {
  if (props.selectedFiles.length === 0) {
    ElMessage.warning('请先选择要复制的文件')
    return
  }
  emit('copy', props.selectedFiles)
  ElMessage.success(`已复制 ${props.selectedFiles.length} 个文件`)
}

const handleCut = () => {
  if (props.selectedFiles.length === 0) {
    ElMessage.warning('请先选择要剪切的文件')
    return
  }
  emit('cut', props.selectedFiles)
  ElMessage.success(`已剪切 ${props.selectedFiles.length} 个文件`)
}

const handlePaste = () => {
  if (!canPaste.value) {
    ElMessage.warning('剪贴板为空')
    return
  }
  emit('paste', props.clipboard)
}

const handleBatchDownload = () => {
  emit('batch-download', props.selectedFiles)
}

const handleBatchMove = () => {
  emit('batch-move', props.selectedFiles)
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量删除选中的 ${props.selectedFiles.length} 个文件吗？`,
      '确认批量删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    emit('batch-delete', props.selectedFiles)
  } catch {
    // 用户取消删除
  }
}

const clearSelection = () => {
  emit('clear-selection')
}

const handleSearch = (query) => {
  emit('search', query)
}

const handleSortChange = (value) => {
  emit('sort-change', { sortBy: value, sortOrder: sortOrder.value })
}

const toggleSortOrder = () => {
  const newOrder = sortOrder.value === 'asc' ? 'desc' : 'asc'
  sortOrder.value = newOrder
  emit('sort-change', { sortBy: sortBy.value, sortOrder: newOrder })
}

const setViewMode = (mode) => {
  emit('view-mode-change', mode)
}

const handleRefresh = () => {
  emit('refresh')
}

const handleSettings = () => {
  emit('settings')
}

const handleProperties = () => {
  if (props.selectedFiles.length !== 1) {
    ElMessage.warning('请选择一个文件查看属性')
    return
  }
  emit('properties', props.selectedFiles[0])
}

const handleToggleHidden = (value) => {
  emit('toggle-hidden', value)
}

const handleToggleExtensions = (value) => {
  emit('toggle-extensions', value)
}
</script>

<style lang="scss" scoped>
.file-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  gap: 16px;
  flex-wrap: wrap;

  .toolbar-main {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    min-width: 0;
  }

  .toolbar-batch {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 12px;
    background: #f0f9ff;
    border: 1px solid #bae6fd;
    border-radius: 6px;
    flex-shrink: 0;

    .batch-info {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .toolbar-tools {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;

    .search-input {
      width: 200px;
    }

    .sort-select {
      width: 120px;
    }
  }

  .ml-2 {
    margin-left: 8px;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .file-toolbar {
    .toolbar-tools {
      .search-input {
        width: 160px;
      }
    }
  }
}

@media (max-width: 768px) {
  .file-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;

    .toolbar-main {
      justify-content: center;
      flex-wrap: wrap;
    }

    .toolbar-batch {
      flex-direction: column;
      align-items: stretch;
      gap: 8px;

      .batch-info {
        justify-content: center;
      }
    }

    .toolbar-tools {
      justify-content: center;
      flex-wrap: wrap;

      .search-input {
        width: 100%;
        max-width: 300px;
      }

      .sort-select {
        width: auto;
        min-width: 120px;
      }
    }
  }
}

@media (max-width: 480px) {
  .file-toolbar {
    padding: 8px 12px;

    .toolbar-main {
      :deep(.el-button-group) {
        display: flex;
        flex-wrap: wrap;
        gap: 4px;

        .el-button {
          margin: 0;
          border-radius: 4px;
        }
      }
    }

    .toolbar-tools {
      .search-input {
        width: 100%;
      }
    }
  }
}
</style>