<template>
  <div class="file-manager">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon :size="24" class="page-icon"><Folder /></el-icon>
        <h1 class="page-title">文件管理</h1>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item
            v-for="(item, index) in breadcrumbs"
            :key="index"
            :to="index === breadcrumbs.length - 1 ? undefined : { path: item.path }"
            @click="navigateToPath(item.path)"
          >
            {{ item.name }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 工具栏 -->
    <FileToolbar
      :selected-files="selectedFiles"
      :current-path="currentPath"
      :view-mode="viewMode"
      :sort-by="sortBy"
      :sort-order="sortOrder"
      :can-upload="true"
      :can-create-folder="true"
      :clipboard="clipboard"
      @upload="showUploadDialog = true"
      @create-folder="showCreateFolderDialog = true"
      @download="downloadFiles"
      @rename="renameFile"
      @delete="deleteFiles"
      @copy="copyFiles"
      @cut="cutFiles"
      @paste="pasteFiles"
      @batch-download="batchDownload"
      @batch-move="batchMove"
      @batch-delete="batchDelete"
      @clear-selection="clearSelection"
      @search="handleSearch"
      @sort-change="handleSortChange"
      @view-mode-change="handleViewModeChange"
      @refresh="refreshFiles"
      @settings="showSettings"
      @properties="showProperties"
      @toggle-hidden="toggleHiddenFiles"
      @toggle-extensions="toggleFileExtensions"
    />

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 目录树侧边栏 -->
      <div class="sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
        <DirectoryTree
          :current-path="currentPath"
          :expanded-paths="expandedPaths"
          @navigate="navigateToPath"
          @create-folder="createFolderInPath"
          @refresh="refreshDirectoryTree"
        />
      </div>

      <!-- 文件列表区域 -->
      <div class="content-area">
        <FileList
          :files="filteredFiles"
          :view-mode="viewMode"
          :selected-files="selectedFiles"
          :current-path="currentPath"
          :sort-by="sortBy"
          :sort-order="sortOrder"
          :loading="loading"
          @select="handleFileSelect"
          @open="openFile"
          @preview="previewFile"
          @download="downloadFile"
          @rename="renameFile"
          @delete="deleteFile"
          @navigate="navigateToPath"
        />
      </div>
      
      <!-- 文件信息面板 -->
      <FileInfoPanel
        :selected-files="selectedFiles"
        :collapsed="infoPanelCollapsed"
        @toggle-collapse="toggleInfoPanel"
        @preview="previewFile"
        @download="downloadFiles"
        @rename="renameFile"
        @delete="deleteFiles"
        @batch-download="batchDownload"
        @batch-move="batchMove"
        @batch-delete="batchDelete"
      />
    </div>

    <!-- 对话框组件 -->
    <FilePreviewDialog
      v-model="showPreviewDialog"
      :file="previewFile"
      @download="downloadFile"
    />

    <FileUploadDialog
      v-model="showUploadDialog"
      :target-path="currentPath"
      @uploaded="handleFileUploaded"
    />

    <CreateFolderDialog
      v-model="showCreateFolderDialog"
      :current-path="currentPath"
      @created="handleFolderCreated"
    />

    <RenameDialog
      v-model="showRenameDialog"
      :file="renameTarget"
      @renamed="handleFileRenamed"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  Folder,
  Upload,
  Download,
  Delete,
  Edit,
  FolderAdd,
  Search,
  List,
  Grid,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 组件导入
import FilePreviewDialog from './components/FilePreviewDialog.vue'
import FileUploadDialog from './components/FileUploadDialog.vue'
import CreateFolderDialog from './components/CreateFolderDialog.vue'
import RenameDialog from './components/RenameDialog.vue'
import DirectoryTree from './components/DirectoryTree.vue'
import FileList from './components/FileList.vue'
import FileToolbar from './components/FileToolbar.vue'
import FileInfoPanel from './components/FileInfoPanel.vue'

// 响应式数据
const searchQuery = ref('')
const sortBy = ref('name')
const sortOrder = ref('asc')
const viewMode = ref('list')
const selectedFiles = ref([])
const currentPath = ref('/')
const loading = ref(false)
const infoPanelCollapsed = ref(false)
const sidebarCollapsed = ref(false)
const clipboard = ref({ files: [], operation: null })
const expandedPaths = ref(new Set(['/']))

// 对话框状态
const showPreviewDialog = ref(false)
const showUploadDialog = ref(false)
const showCreateFolderDialog = ref(false)
const showRenameDialog = ref(false)

// 文件数据
const files = ref([])
const previewFile = ref(null)
const renameTarget = ref(null)

// 计算属性
const breadcrumbs = computed(() => {
  const parts = currentPath.value.split('/').filter(Boolean)
  const crumbs = [{ name: '根目录', path: '/' }]
  
  let path = ''
  parts.forEach(part => {
    path += '/' + part
    crumbs.push({ name: part, path })
  })
  
  return crumbs
})

const filteredFiles = computed(() => {
  let result = [...files.value]
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(file => 
      file.name.toLowerCase().includes(query)
    )
  }
  
  // 排序
  result.sort((a, b) => {
    let aValue, bValue
    
    switch (sortBy.value) {
      case 'name':
        aValue = a.name.toLowerCase()
        bValue = b.name.toLowerCase()
        break
      case 'size':
        aValue = a.size || 0
        bValue = b.size || 0
        break
      case 'type':
        aValue = a.type || ''
        bValue = b.type || ''
        break
      case 'modifiedTime':
        aValue = a.modifiedTime || 0
        bValue = b.modifiedTime || 0
        break
      default:
        aValue = a.name.toLowerCase()
        bValue = b.name.toLowerCase()
    }
    
    if (aValue < bValue) return sortOrder.value === 'asc' ? -1 : 1
    if (aValue > bValue) return sortOrder.value === 'asc' ? 1 : -1
    return 0
  })
  
  return result
})

// 方法
const loadFiles = async (path = currentPath.value) => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟文件数据
    files.value = [
      {
        id: '1',
        name: '文档.pdf',
        type: 'file',
        size: 1024000,
        modifiedTime: Date.now() - 86400000,
        createdTime: Date.now() - 172800000,
        path: path + '/文档.pdf',
        permissions: 'rw-r--r--',
        owner: 'admin'
      },
      {
        id: '2',
        name: '图片',
        type: 'directory',
        size: 0,
        modifiedTime: Date.now() - 43200000,
        createdTime: Date.now() - 259200000,
        path: path + '/图片',
        permissions: 'rwxr-xr-x',
        owner: 'admin'
      },
      {
        id: '3',
        name: '示例图片.jpg',
        type: 'file',
        size: 2048000,
        modifiedTime: Date.now() - 21600000,
        createdTime: Date.now() - 345600000,
        path: path + '/示例图片.jpg',
        permissions: 'rw-r--r--',
        owner: 'admin',
        url: 'https://via.placeholder.com/800x600/4CAF50/white?text=示例图片'
      }
    ]
  } catch (error) {
    ElMessage.error('加载文件列表失败')
    console.error('Load files error:', error)
  } finally {
    loading.value = false
  }
}

const refreshFiles = () => {
  loadFiles()
}

const navigateToPath = (path) => {
  currentPath.value = path
  selectedFiles.value = []
  loadFiles(path)
}

const handleFileSelect = (selection) => {
  selectedFiles.value = selection
}

const openFile = (file) => {
  if (file.type === 'directory') {
    navigateToPath(file.path)
  } else {
    previewFile(file)
  }
}

const previewFile = (file) => {
  previewFile.value = file
  showPreviewDialog.value = true
}

const downloadFile = (file) => {
  downloadFiles([file])
}

const downloadFiles = (fileList) => {
  fileList.forEach(file => {
    ElMessage.success(`开始下载: ${file.name}`)
    // 实际下载逻辑
  })
}

const renameFile = (file) => {
  renameTarget.value = file
  showRenameDialog.value = true
}

const deleteFile = (file) => {
  deleteFiles([file])
}

const deleteFiles = async (fileList) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${fileList.length} 个文件吗？`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    // 实际删除逻辑
    fileList.forEach(file => {
      const index = files.value.findIndex(f => f.id === file.id)
      if (index > -1) {
        files.value.splice(index, 1)
      }
    })
    
    selectedFiles.value = []
    ElMessage.success(`成功删除 ${fileList.length} 个文件`)
  } catch {
    // 用户取消删除
  }
}

const copyFiles = (fileList) => {
  clipboard.value = { files: fileList, operation: 'copy' }
  ElMessage.success(`已复制 ${fileList.length} 个文件`)
}

const cutFiles = (fileList) => {
  clipboard.value = { files: fileList, operation: 'cut' }
  ElMessage.success(`已剪切 ${fileList.length} 个文件`)
}

const pasteFiles = (clipboardData) => {
  const { files: fileList, operation } = clipboardData
  const action = operation === 'cut' ? '移动' : '复制'
  
  // 实际粘贴逻辑
  ElMessage.success(`${action}了 ${fileList.length} 个文件到当前目录`)
  
  if (operation === 'cut') {
    clipboard.value = { files: [], operation: null }
  }
  
  refreshFiles()
}

const batchDownload = (fileList) => {
  downloadFiles(fileList)
}

const batchMove = (fileList) => {
  ElMessage.info('批量移动功能开发中...')
}

const batchDelete = (fileList) => {
  deleteFiles(fileList)
}

const clearSelection = () => {
  selectedFiles.value = []
}

const handleSearch = (query) => {
  searchQuery.value = query
}

const handleSortChange = ({ sortBy: newSortBy, sortOrder: newSortOrder }) => {
  sortBy.value = newSortBy
  sortOrder.value = newSortOrder
}

const handleViewModeChange = (mode) => {
  viewMode.value = mode
}

const showSettings = () => {
  ElMessage.info('设置功能开发中...')
}

const showProperties = (file) => {
  ElMessage.info(`查看 ${file.name} 的属性`)
}

const toggleHiddenFiles = (show) => {
  ElMessage.info(`${show ? '显示' : '隐藏'}隐藏文件`)
}

const toggleFileExtensions = (show) => {
  ElMessage.info(`${show ? '显示' : '隐藏'}文件扩展名`)
}

const toggleInfoPanel = () => {
  infoPanelCollapsed.value = !infoPanelCollapsed.value
}

const createFolderInPath = (path) => {
  currentPath.value = path
  showCreateFolderDialog.value = true
}

const refreshDirectoryTree = () => {
  ElMessage.success('目录树已刷新')
}

const handleFileUploaded = (uploadedFiles) => {
  ElMessage.success(`成功上传 ${uploadedFiles.length} 个文件`)
  refreshFiles()
}

const handleFolderCreated = (folderInfo) => {
  ElMessage.success(`成功创建文件夹: ${folderInfo.name}`)
  refreshFiles()
}

const handleFileRenamed = (renameInfo) => {
  ElMessage.success(`文件重命名成功: ${renameInfo.oldName} -> ${renameInfo.newName}`)
  refreshFiles()
}

// 生命周期
onMounted(() => {
  loadFiles()
})

// 监听路径变化
watch(currentPath, (newPath) => {
  loadFiles(newPath)
})
</script>

<style lang="scss" scoped>
.file-manager {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 24px;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .page-icon {
        color: #409eff;
      }

      .page-title {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }

      .breadcrumb {
        :deep(.el-breadcrumb__item) {
          .el-breadcrumb__inner {
            cursor: pointer;
            
            &:hover {
              color: #409eff;
            }
          }
          
          &:last-child .el-breadcrumb__inner {
            cursor: default;
            color: #606266;
            
            &:hover {
              color: #606266;
            }
          }
        }
      }
    }
  }

  .main-content {
    flex: 1;
    display: flex;
    overflow: hidden;

    .sidebar {
      width: 280px;
      background: #fff;
      border-right: 1px solid #e4e7ed;
      transition: width 0.3s ease;
      overflow: hidden;

      &.sidebar-collapsed {
        width: 0;
      }
    }

    .content-area {
      flex: 1;
      background: #fff;
      overflow: hidden;
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .file-manager {
    .main-content {
      .sidebar {
        width: 240px;
      }
    }
  }
}

@media (max-width: 768px) {
  .file-manager {
    .page-header {
      padding: 12px 16px;
      
      .header-left {
        gap: 12px;
        
        .page-title {
          font-size: 18px;
        }
      }
    }

    .main-content {
      flex-direction: column;
      
      .sidebar {
        width: 100%;
        height: 200px;
        border-right: none;
        border-bottom: 1px solid #e4e7ed;
        
        &.sidebar-collapsed {
          height: 0;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .file-manager {
    .page-header {
      padding: 8px 12px;
      
      .header-left {
        gap: 8px;
        
        .page-title {
          font-size: 16px;
        }
        
        .breadcrumb {
          display: none;
        }
      }
    }
  }
}
</style>