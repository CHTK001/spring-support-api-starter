<template>
  <div class="file-info-panel" :class="{ 'panel-collapsed': collapsed }">
    <!-- 面板头部 -->
    <div class="panel-header">
      <div class="panel-title">
        <el-icon><InfoFilled /></el-icon>
        <span>文件信息</span>
      </div>
      <el-button
        text
        :icon="collapsed ? Expand : Fold"
        @click="toggleCollapse"
        title="折叠/展开"
      />
    </div>

    <!-- 面板内容 -->
    <div class="panel-content" v-show="!collapsed">
      <!-- 无选择状态 -->
      <div v-if="!selectedFile" class="no-selection">
        <el-empty
          description="请选择一个文件查看详细信息"
          :image-size="80"
        />
      </div>

      <!-- 单个文件信息 -->
      <div v-else-if="!isMultipleSelection" class="file-details">
        <!-- 文件预览 -->
        <div class="file-preview">
          <div class="preview-container">
            <!-- 图片预览 -->
            <img
              v-if="isImage(selectedFile)"
              :src="getFileUrl(selectedFile)"
              :alt="selectedFile.name"
              class="preview-image"
              @error="handleImageError"
            />
            <!-- 文件图标 -->
            <div v-else class="file-icon">
              <el-icon :size="48">
                <component :is="getFileIcon(selectedFile)" />
              </el-icon>
            </div>
          </div>
        </div>

        <!-- 文件基本信息 -->
        <div class="file-info">
          <div class="info-item">
            <label>文件名：</label>
            <span class="file-name" :title="selectedFile.name">
              {{ selectedFile.name }}
            </span>
          </div>

          <div class="info-item">
            <label>文件类型：</label>
            <span>{{ getFileType(selectedFile) }}</span>
          </div>

          <div class="info-item">
            <label>文件大小：</label>
            <span>{{ formatFileSize(selectedFile.size) }}</span>
          </div>

          <div class="info-item">
            <label>修改时间：</label>
            <span>{{ formatDate(selectedFile.modifiedTime) }}</span>
          </div>

          <div class="info-item">
            <label>创建时间：</label>
            <span>{{ formatDate(selectedFile.createdTime) }}</span>
          </div>

          <div class="info-item">
            <label>文件路径：</label>
            <span class="file-path" :title="selectedFile.path">
              {{ selectedFile.path }}
            </span>
          </div>

          <div class="info-item" v-if="selectedFile.permissions">
            <label>权限：</label>
            <span>{{ selectedFile.permissions }}</span>
          </div>

          <div class="info-item" v-if="selectedFile.owner">
            <label>所有者：</label>
            <span>{{ selectedFile.owner }}</span>
          </div>
        </div>

        <!-- 文件操作 -->
        <div class="file-actions">
          <el-button
            type="primary"
            :icon="View"
            @click="previewFile"
            :disabled="!canPreview(selectedFile)"
            size="small"
          >
            预览
          </el-button>
          <el-button
            :icon="Download"
            @click="downloadFile"
            size="small"
          >
            下载
          </el-button>
          <el-button
            :icon="Edit"
            @click="renameFile"
            size="small"
          >
            重命名
          </el-button>
          <el-button
            :icon="Delete"
            type="danger"
            @click="deleteFile"
            size="small"
          >
            删除
          </el-button>
        </div>

        <!-- 扩展信息 -->
        <div class="extended-info" v-if="selectedFile.metadata">
          <el-divider content-position="left">
            <span class="divider-text">扩展信息</span>
          </el-divider>

          <!-- 图片信息 -->
          <template v-if="isImage(selectedFile) && selectedFile.metadata.image">
            <div class="info-item">
              <label>分辨率：</label>
              <span>
                {{ selectedFile.metadata.image.width }} × 
                {{ selectedFile.metadata.image.height }}
              </span>
            </div>
            <div class="info-item" v-if="selectedFile.metadata.image.colorDepth">
              <label>色深：</label>
              <span>{{ selectedFile.metadata.image.colorDepth }} 位</span>
            </div>
          </template>

          <!-- 视频信息 -->
          <template v-if="isVideo(selectedFile) && selectedFile.metadata.video">
            <div class="info-item">
              <label>时长：</label>
              <span>{{ formatDuration(selectedFile.metadata.video.duration) }}</span>
            </div>
            <div class="info-item">
              <label>分辨率：</label>
              <span>
                {{ selectedFile.metadata.video.width }} × 
                {{ selectedFile.metadata.video.height }}
              </span>
            </div>
            <div class="info-item" v-if="selectedFile.metadata.video.bitrate">
              <label>比特率：</label>
              <span>{{ selectedFile.metadata.video.bitrate }} kbps</span>
            </div>
          </template>

          <!-- 音频信息 -->
          <template v-if="isAudio(selectedFile) && selectedFile.metadata.audio">
            <div class="info-item">
              <label>时长：</label>
              <span>{{ formatDuration(selectedFile.metadata.audio.duration) }}</span>
            </div>
            <div class="info-item" v-if="selectedFile.metadata.audio.bitrate">
              <label>比特率：</label>
              <span>{{ selectedFile.metadata.audio.bitrate }} kbps</span>
            </div>
            <div class="info-item" v-if="selectedFile.metadata.audio.sampleRate">
              <label>采样率：</label>
              <span>{{ selectedFile.metadata.audio.sampleRate }} Hz</span>
            </div>
          </template>
        </div>
      </div>

      <!-- 多选状态 -->
      <div v-else class="multiple-selection">
        <div class="selection-summary">
          <el-icon :size="32"><Files /></el-icon>
          <h3>已选择 {{ selectedFiles.length }} 个文件</h3>
        </div>

        <div class="selection-stats">
          <div class="stat-item">
            <label>总大小：</label>
            <span>{{ formatFileSize(totalSize) }}</span>
          </div>
          <div class="stat-item">
            <label>文件类型：</label>
            <span>{{ fileTypesText }}</span>
          </div>
        </div>

        <!-- 批量操作 -->
        <div class="batch-actions">
          <el-button
            type="primary"
            :icon="Download"
            @click="batchDownload"
            size="small"
          >
            批量下载
          </el-button>
          <el-button
            :icon="FolderOpened"
            @click="batchMove"
            size="small"
          >
            批量移动
          </el-button>
          <el-button
            :icon="Delete"
            type="danger"
            @click="batchDelete"
            size="small"
          >
            批量删除
          </el-button>
        </div>

        <!-- 选中文件列表 -->
        <div class="selected-files">
          <el-divider content-position="left">
            <span class="divider-text">选中文件</span>
          </el-divider>
          <div class="file-list">
            <div
              v-for="file in selectedFiles.slice(0, 10)"
              :key="file.id"
              class="file-item"
            >
              <el-icon><component :is="getFileIcon(file)" /></el-icon>
              <span class="file-name" :title="file.name">{{ file.name }}</span>
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
            </div>
            <div v-if="selectedFiles.length > 10" class="more-files">
              还有 {{ selectedFiles.length - 10 }} 个文件...
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
  InfoFilled,
  Expand,
  Fold,
  View,
  Download,
  Edit,
  Delete,
  Files,
  FolderOpened,
  Document,
  Picture,
  VideoPlay,
  Headphone,
  Folder
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
const props = defineProps({
  selectedFiles: {
    type: Array,
    default: () => []
  },
  collapsed: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits([
  'toggle-collapse',
  'preview',
  'download',
  'rename',
  'delete',
  'batch-download',
  'batch-move',
  'batch-delete'
])

// 计算属性
const selectedFile = computed(() => {
  return props.selectedFiles.length === 1 ? props.selectedFiles[0] : null
})

const isMultipleSelection = computed(() => {
  return props.selectedFiles.length > 1
})

const totalSize = computed(() => {
  return props.selectedFiles.reduce((total, file) => total + (file.size || 0), 0)
})

const fileTypesText = computed(() => {
  const types = [...new Set(props.selectedFiles.map(file => getFileType(file)))]
  return types.slice(0, 3).join(', ') + (types.length > 3 ? '...' : '')
})

// 方法
const toggleCollapse = () => {
  emit('toggle-collapse')
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

const getFileIcon = (file) => {
  if (file.type === 'directory') return Folder
  if (isImage(file)) return Picture
  if (isVideo(file)) return VideoPlay
  if (isAudio(file)) return Headphone
  return Document
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
    xml: 'XML', yaml: 'YAML', yml: 'YAML',
    // 压缩包
    zip: 'ZIP 压缩包', rar: 'RAR 压缩包', '7z': '7Z 压缩包',
    tar: 'TAR 压缩包', gz: 'GZ 压缩包'
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
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = Math.floor(seconds % 60)
  
  if (hours > 0) {
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const getFileUrl = (file) => {
  // 这里应该返回文件的实际URL
  return file.url || `/api/files/preview/${file.id}`
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

// 文件操作
const previewFile = () => {
  emit('preview', selectedFile.value)
}

const downloadFile = () => {
  emit('download', [selectedFile.value])
}

const renameFile = () => {
  emit('rename', selectedFile.value)
}

const deleteFile = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${selectedFile.value.name}" 吗？`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    emit('delete', [selectedFile.value])
  } catch {
    // 用户取消删除
  }
}

// 批量操作
const batchDownload = () => {
  emit('batch-download', props.selectedFiles)
}

const batchMove = () => {
  emit('batch-move', props.selectedFiles)
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${props.selectedFiles.length} 个文件吗？`,
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
</script>

<style lang="scss" scoped>
.file-info-panel {
  width: 320px;
  background: #fff;
  border-left: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;

  &.panel-collapsed {
    width: 48px;
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
    background: #f8f9fa;

    .panel-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
      color: #303133;
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;

    .no-selection {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 200px;
    }

    .file-details {
      .file-preview {
        margin-bottom: 16px;

        .preview-container {
          display: flex;
          justify-content: center;
          align-items: center;
          height: 120px;
          background: #f5f7fa;
          border-radius: 8px;
          overflow: hidden;

          .preview-image {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
          }

          .file-icon {
            color: #909399;
          }
        }
      }

      .file-info {
        margin-bottom: 16px;

        .info-item {
          display: flex;
          margin-bottom: 8px;
          font-size: 14px;

          label {
            width: 80px;
            color: #606266;
            flex-shrink: 0;
          }

          span {
            color: #303133;
            word-break: break-all;
            flex: 1;

            &.file-name,
            &.file-path {
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
        }
      }

      .file-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 16px;
      }

      .extended-info {
        .divider-text {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .multiple-selection {
      .selection-summary {
        text-align: center;
        margin-bottom: 16px;

        h3 {
          margin: 8px 0 0 0;
          color: #303133;
          font-size: 16px;
        }
      }

      .selection-stats {
        margin-bottom: 16px;

        .stat-item {
          display: flex;
          margin-bottom: 8px;
          font-size: 14px;

          label {
            width: 80px;
            color: #606266;
            flex-shrink: 0;
          }

          span {
            color: #303133;
            flex: 1;
          }
        }
      }

      .batch-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 16px;
      }

      .selected-files {
        .divider-text {
          font-size: 12px;
          color: #909399;
        }

        .file-list {
          .file-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 4px 0;
            font-size: 12px;

            .file-name {
              flex: 1;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .file-size {
              color: #909399;
              flex-shrink: 0;
            }
          }

          .more-files {
            text-align: center;
            color: #909399;
            font-size: 12px;
            padding: 8px 0;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .file-info-panel {
    width: 100%;
    border-left: none;
    border-top: 1px solid #e4e7ed;

    &.panel-collapsed {
      width: 100%;
      height: 48px;
    }
  }
}
</style>