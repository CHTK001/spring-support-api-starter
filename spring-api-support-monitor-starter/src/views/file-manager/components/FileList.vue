<template>
  <div class="file-list">
    <!-- 列表头部 -->
    <div class="list-header">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-section">
        <el-breadcrumb separator="/">
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

      <!-- 选择信息和控制 -->
      <div class="selection-info" v-if="selectedFiles.length > 0">
        <span class="selection-count">
          已选择 {{ selectedFiles.length }} 个文件
        </span>
        <el-button
          text
          type="primary"
          size="small"
          @click="clearSelection"
        >
          清除选择
        </el-button>
      </div>

      <!-- 视图控制 -->
      <div class="view-controls">
        <el-button-group>
          <el-button
            :type="viewMode === 'list' ? 'primary' : 'default'"
            :icon="List"
            @click="changeViewMode('list')"
            title="列表视图"
          />
          <el-button
            :type="viewMode === 'grid' ? 'primary' : 'default'"
            :icon="Grid"
            @click="changeViewMode('grid')"
            title="网格视图"
          />
        </el-button-group>

        <!-- 排序控制 -->
        <el-select
          :model-value="sortBy"
          @change="changeSortBy"
          size="small"
          class="sort-select"
        >
          <el-option label="名称" value="name" />
          <el-option label="大小" value="size" />
          <el-option label="类型" value="type" />
          <el-option label="修改时间" value="modifiedTime" />
        </el-select>

        <el-button
          :icon="sortOrder === 'asc' ? SortUp : SortDown"
          @click="toggleSortOrder"
          size="small"
          title="切换排序方向"
        />

        <el-button
          :icon="Refresh"
          @click="refreshList"
          size="small"
          title="刷新"
        />
      </div>
    </div>

    <!-- 文件列表内容 -->
    <div class="list-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 空状态 -->
      <div v-else-if="files.length === 0" class="empty-state">
        <el-empty
          description="此目录为空"
          :image-size="120"
        >
          <el-button type="primary" @click="$emit('upload')">
            上传文件
          </el-button>
        </el-empty>
      </div>

      <!-- 列表视图 -->
      <div v-else-if="viewMode === 'list'" class="list-view">
        <el-table
          :data="files"
          :row-selection="true"
          @selection-change="handleSelectionChange"
          @row-click="handleRowClick"
          @row-dblclick="handleRowDoubleClick"
          stripe
          highlight-current-row
          class="file-table"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column label="名称" min-width="200">
            <template #default="{ row }">
              <div class="file-name-cell">
                <el-icon class="file-icon" :size="20">
                  <component :is="getFileIcon(row)" />
                </el-icon>
                <span class="file-name" :title="row.name">{{ row.name }}</span>
                <el-tag
                  v-if="row.type === 'directory'"
                  size="small"
                  type="info"
                  class="type-tag"
                >
                  文件夹
                </el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="大小" width="120" sortable="custom">
            <template #default="{ row }">
              <span v-if="row.type !== 'directory'">{{ formatFileSize(row.size) }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              {{ getFileType(row) }}
            </template>
          </el-table-column>
          
          <el-table-column label="修改时间" width="180" sortable="custom">
            <template #default="{ row }">
              {{ formatDate(row.modifiedTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  text
                  type="primary"
                  :icon="View"
                  @click.stop="previewFile(row)"
                  :disabled="!canPreview(row)"
                  size="small"
                >
                  预览
                </el-button>
                <el-button
                  text
                  type="primary"
                  :icon="Download"
                  @click.stop="downloadFile(row)"
                  size="small"
                >
                  下载
                </el-button>
                <el-button
                  text
                  type="warning"
                  :icon="Edit"
                  @click.stop="renameFile(row)"
                  size="small"
                >
                  重命名
                </el-button>
                <el-button
                  text
                  type="danger"
                  :icon="Delete"
                  @click.stop="deleteFile(row)"
                  size="small"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 网格视图 -->
      <div v-else class="grid-view">
        <div class="file-grid">
          <div
            v-for="file in files"
            :key="file.id"
            class="file-card"
            :class="{ 
              selected: isSelected(file),
              'is-directory': file.type === 'directory'
            }"
            @click="handleCardClick(file, $event)"
            @dblclick="handleCardDoubleClick(file)"
          >
            <!-- 选择框 -->
            <div class="card-selection">
              <el-checkbox
                :model-value="isSelected(file)"
                @change="toggleSelection(file)"
                @click.stop
              />
            </div>

            <!-- 文件缩略图/图标 -->
            <div class="card-thumbnail">
              <img
                v-if="isImage(file) && file.thumbnail"
                :src="file.thumbnail"
                :alt="file.name"
                class="thumbnail-image"
                @error="handleImageError"
              />
              <div v-else class="file-icon-large">
                <el-icon :size="48">
                  <component :is="getFileIcon(file)" />
                </el-icon>
              </div>
            </div>

            <!-- 文件信息 -->
            <div class="card-info">
              <div class="file-name" :title="file.name">{{ file.name }}</div>
              <div class="file-details">
                <span class="file-type">{{ getFileType(file) }}</span>
                <span v-if="file.type !== 'directory'" class="file-size">
                  {{ formatFileSize(file.size) }}
                </span>
              </div>
              <div class="file-date">{{ formatDate(file.modifiedTime) }}</div>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions">
              <el-button
                text
                :icon="View"
                @click.stop="previewFile(file)"
                :disabled="!canPreview(file)"
                size="small"
                title="预览"
              />
              <el-button
                text
                :icon="Download"
                @click.stop="downloadFile(file)"
                size="small"
                title="下载"
              />
              <el-dropdown trigger="click" @click.stop>
                <el-button text :icon="More" size="small" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      :icon="Edit"
                      @click="renameFile(file)"
                    >
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item
                      :icon="DocumentCopy"
                      @click="copyFile(file)"
                    >
                      复制
                    </el-dropdown-item>
                    <el-dropdown-item
                      :icon="Scissors"
                      @click="cutFile(file)"
                    >
                      剪切
                    </el-dropdown-item>
                    <el-dropdown-item divided />
                    <el-dropdown-item
                      :icon="Delete"
                      @click="deleteFile(file)"
                    >
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  List,
  Grid,
  SortUp,
  SortDown,
  Refresh,
  View,
  Download,
  Edit,
  Delete,
  More,
  DocumentCopy,
  Scissors,
  Folder,
  FolderOpened,
  Document,
  Picture,
  VideoPlay,
  Headphone
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  files: {
    type: Array,
    default: () => []
  },
  viewMode: {
    type: String,
    default: 'list'
  },
  selectedFiles: {
    type: Array,
    default: () => []
  },
  currentPath: {
    type: String,
    default: '/'
  },
  sortBy: {
    type: String,
    default: 'name'
  },
  sortOrder: {
    type: String,
    default: 'asc'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits([
  'select',
  'open',
  'preview',
  'download',
  'rename',
  'delete',
  'navigate',
  'view-mode-change',
  'sort-change',
  'refresh',
  'copy',
  'cut'
])

// 计算属性
const breadcrumbs = computed(() => {
  const parts = props.currentPath.split('/').filter(Boolean)
  const crumbs = [{ name: '根目录', path: '/' }]
  
  let path = ''
  parts.forEach(part => {
    path += '/' + part
    crumbs.push({ name: part, path })
  })
  
  return crumbs
})

// 方法
const isSelected = (file) => {
  return props.selectedFiles.some(f => f.id === file.id)
}

const handleSelectionChange = (selection) => {
  emit('select', selection)
}

const toggleSelection = (file) => {
  const isCurrentlySelected = isSelected(file)
  let newSelection
  
  if (isCurrentlySelected) {
    newSelection = props.selectedFiles.filter(f => f.id !== file.id)
  } else {
    newSelection = [...props.selectedFiles, file]
  }
  
  emit('select', newSelection)
}

const clearSelection = () => {
  emit('select', [])
}

const handleRowClick = (row, column, event) => {
  if (event.ctrlKey || event.metaKey) {
    toggleSelection(row)
  } else {
    emit('select', [row])
  }
}

const handleRowDoubleClick = (row) => {
  emit('open', row)
}

const handleCardClick = (file, event) => {
  if (event.ctrlKey || event.metaKey) {
    toggleSelection(file)
  } else {
    emit('select', [file])
  }
}

const handleCardDoubleClick = (file) => {
  emit('open', file)
}

const navigateToPath = (path) => {
  emit('navigate', path)
}

const changeViewMode = (mode) => {
  emit('view-mode-change', mode)
}

const changeSortBy = (value) => {
  emit('sort-change', { sortBy: value, sortOrder: props.sortOrder })
}

const toggleSortOrder = () => {
  const newOrder = props.sortOrder === 'asc' ? 'desc' : 'asc'
  emit('sort-change', { sortBy: props.sortBy, sortOrder: newOrder })
}

const refreshList = () => {
  emit('refresh')
}

const previewFile = (file) => {
  emit('preview', file)
}

const downloadFile = (file) => {
  emit('download', file)
}

const renameFile = (file) => {
  emit('rename', file)
}

const deleteFile = (file) => {
  emit('delete', file)
}

const copyFile = (file) => {
  emit('copy', [file])
}

const cutFile = (file) => {
  emit('cut', [file])
}

// 工具方法
const getFileIcon = (file) => {
  if (file.type === 'directory') return Folder
  if (isImage(file)) return Picture
  if (isVideo(file)) return VideoPlay
  if (isAudio(file)) return Headphone
  return Document
}

const isImage = (file) => {
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
  const ext = file.name.split('.').pop()?.toLowerCase()
  return imageExts.includes(ext)
}

const isVideo = (file) => {
  const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm', 'mkv']
  const ext = file.name.split('.').pop()?.toLowerCase()
  return videoExts.includes(ext)
}

const isAudio = (file) => {
  const audioExts = ['mp3', 'wav', 'flac', 'aac', 'ogg', 'wma']
  const ext = file.name.split('.').pop()?.toLowerCase()
  return audioExts.includes(ext)
}

const getFileType = (file) => {
  if (file.type === 'directory') return '文件夹'
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (!ext) return '未知类型'
  
  const typeMap = {
    // 图片
    jpg: 'JPEG 图片', jpeg: 'JPEG 图片', png: 'PNG 图片', gif: 'GIF 图片',
    bmp: 'BMP 图片', webp: 'WebP 图片', svg: 'SVG 图片',
    // 视频
    mp4: 'MP4 视频', avi: 'AVI 视频', mov: 'MOV 视频', wmv: 'WMV 视频',
    flv: 'FLV 视频', webm: 'WebM 视频', mkv: 'MKV 视频',
    // 音频
    mp3: 'MP3 音频', wav: 'WAV 音频', flac: 'FLAC 音频', aac: 'AAC 音频',
    ogg: 'OGG 音频', wma: 'WMA 音频',
    // 文档
    pdf: 'PDF 文档', doc: 'Word 文档', docx: 'Word 文档', xls: 'Excel 表格',
    xlsx: 'Excel 表格', ppt: 'PowerPoint 演示', pptx: 'PowerPoint 演示',
    txt: '文本文件', rtf: 'RTF 文档',
    // 代码
    js: 'JavaScript', ts: 'TypeScript', html: 'HTML', css: 'CSS',
    vue: 'Vue 组件', jsx: 'JSX', tsx: 'TSX', json: 'JSON',
    xml: 'XML', yaml: 'YAML', yml: 'YAML'
  }
  
  return typeMap[ext] || `${ext.toUpperCase()} 文件`
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const canPreview = (file) => {
  return isImage(file) || isVideo(file) || isAudio(file) || 
         ['txt', 'json', 'xml', 'html', 'css', 'js', 'vue', 'md'].includes(
           file.name.split('.').pop()?.toLowerCase()
         )
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
}
</script>

<style lang="scss" scoped>
.file-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;

  .list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
    background: #f8f9fa;
    flex-wrap: wrap;
    gap: 12px;

    .breadcrumb-section {
      flex: 1;
      min-width: 200px;

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

    .selection-info {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 8px;
      background: #e1f3d8;
      border-radius: 4px;
      font-size: 14px;
      color: #67c23a;

      .selection-count {
        font-weight: 500;
      }
    }

    .view-controls {
      display: flex;
      align-items: center;
      gap: 8px;

      .sort-select {
        width: 100px;
      }
    }
  }

  .list-content {
    flex: 1;
    overflow: hidden;

    .loading-state {
      padding: 16px;
    }

    .empty-state {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      min-height: 300px;
    }

    .list-view {
      height: 100%;
      overflow: auto;

      .file-table {
        :deep(.el-table__body-wrapper) {
          height: calc(100vh - 200px);
          overflow-y: auto;
        }

        .file-name-cell {
          display: flex;
          align-items: center;
          gap: 8px;

          .file-icon {
            color: #409eff;
            flex-shrink: 0;
          }

          .file-name {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .type-tag {
            flex-shrink: 0;
          }
        }

        .action-buttons {
          display: flex;
          gap: 4px;
        }

        .text-muted {
          color: #909399;
        }
      }
    }

    .grid-view {
      height: 100%;
      overflow: auto;
      padding: 16px;

      .file-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        gap: 16px;

        .file-card {
          position: relative;
          background: #fff;
          border: 1px solid #e4e7ed;
          border-radius: 8px;
          padding: 12px;
          cursor: pointer;
          transition: all 0.2s;
          overflow: hidden;

          &:hover {
            border-color: #409eff;
            box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);

            .card-actions {
              opacity: 1;
            }
          }

          &.selected {
            border-color: #67c23a;
            background: #f0f9ff;
          }

          &.is-directory {
            border-color: #e6a23c;
            
            &:hover {
              border-color: #e6a23c;
              box-shadow: 0 2px 8px rgba(230, 162, 60, 0.2);
            }
          }

          .card-selection {
            position: absolute;
            top: 8px;
            left: 8px;
            z-index: 2;
          }

          .card-thumbnail {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 80px;
            margin-bottom: 8px;
            background: #f5f7fa;
            border-radius: 4px;
            overflow: hidden;

            .thumbnail-image {
              max-width: 100%;
              max-height: 100%;
              object-fit: cover;
            }

            .file-icon-large {
              color: #409eff;
            }
          }

          .card-info {
            .file-name {
              font-weight: 500;
              color: #303133;
              margin-bottom: 4px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              font-size: 14px;
            }

            .file-details {
              display: flex;
              justify-content: space-between;
              margin-bottom: 4px;
              font-size: 12px;
              color: #909399;

              .file-type {
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }

              .file-size {
                flex-shrink: 0;
                margin-left: 8px;
              }
            }

            .file-date {
              font-size: 12px;
              color: #c0c4cc;
            }
          }

          .card-actions {
            position: absolute;
            top: 8px;
            right: 8px;
            display: flex;
            gap: 4px;
            opacity: 0;
            transition: opacity 0.2s;
            background: rgba(255, 255, 255, 0.9);
            border-radius: 4px;
            padding: 2px;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .file-list {
    .list-content {
      .grid-view {
        .file-grid {
          grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .file-list {
    .list-header {
      flex-direction: column;
      align-items: stretch;
      gap: 8px;

      .breadcrumb-section {
        min-width: auto;
      }

      .view-controls {
        justify-content: center;
      }
    }

    .list-content {
      .grid-view {
        padding: 12px;
        
        .file-grid {
          grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
          gap: 12px;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .file-list {
    .list-header {
      padding: 8px 12px;
    }

    .list-content {
      .grid-view {
        padding: 8px;
        
        .file-grid {
          grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
          gap: 8px;

          .file-card {
            padding: 8px;

            .card-thumbnail {
              height: 60px;
            }
          }
        }
      }
    }
  }
}
</style>