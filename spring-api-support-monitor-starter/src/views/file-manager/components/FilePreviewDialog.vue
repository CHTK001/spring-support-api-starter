<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="80%"
    :before-close="handleClose"
    class="file-preview-dialog"
    destroy-on-close
  >
    <div v-if="file" class="preview-container">
      <!-- 文件信息头部 -->
      <div class="file-info-header">
        <div class="file-basic-info">
          <el-icon class="file-icon" :size="24">
            <component :is="getFileIcon(file)" />
          </el-icon>
          <div class="file-details">
            <h3 class="file-name">{{ file.name }}</h3>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
              <span class="file-type">{{ getFileType(file) }}</span>
              <span class="file-date">{{ formatDate(file.modifiedTime) }}</span>
            </div>
          </div>
        </div>
        
        <div class="file-actions">
          <el-button
            type="primary"
            :icon="Download"
            @click="downloadFile"
          >
            下载
          </el-button>
          <el-button
            :icon="DocumentCopy"
            @click="copyPath"
          >
            复制路径
          </el-button>
        </div>
      </div>

      <!-- 预览内容 -->
      <div class="preview-content">
        <!-- 图片预览 -->
        <div v-if="isImage(file)" class="image-preview">
          <div class="image-container">
            <img
              :src="getFileUrl(file)"
              :alt="file.name"
              class="preview-image"
              @load="handleImageLoad"
              @error="handleImageError"
            />
          </div>
          <div class="image-info" v-if="imageInfo">
            <div class="info-item">
              <span class="label">尺寸:</span>
              <span class="value">{{ imageInfo.width }} × {{ imageInfo.height }}</span>
            </div>
            <div class="info-item">
              <span class="label">格式:</span>
              <span class="value">{{ file.name.split('.').pop()?.toUpperCase() }}</span>
            </div>
          </div>
        </div>

        <!-- 文本预览 -->
        <div v-else-if="isText(file)" class="text-preview">
          <div class="text-toolbar">
            <el-button-group>
              <el-button
                :type="textWrap ? 'primary' : 'default'"
                :icon="textWrap ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
                @click="textWrap = !textWrap"
                size="small"
              >
                {{ textWrap ? '取消换行' : '自动换行' }}
              </el-button>
              <el-button
                :icon="'el-icon-document-copy'"
                @click="copyContent"
                size="small"
              >
                复制内容
              </el-button>
            </el-button-group>
          </div>
          <div class="text-container" :class="{ 'text-wrap': textWrap }">
            <pre v-if="textContent" class="text-content">{{ textContent }}</pre>
            <div v-else class="loading-text">
              <el-skeleton :rows="10" animated />
            </div>
          </div>
        </div>

        <!-- PDF预览 -->
        <div v-else-if="isPdf(file)" class="pdf-preview">
          <div class="pdf-toolbar">
            <el-button-group>
              <el-button
                :icon="ZoomOut"
                @click="pdfScale -= 0.1"
                :disabled="pdfScale <= 0.5"
                size="small"
              >
                缩小
              </el-button>
              <el-button size="small">{{ Math.round(pdfScale * 100) }}%</el-button>
              <el-button
                :icon="ZoomIn"
                @click="pdfScale += 0.1"
                :disabled="pdfScale >= 2"
                size="small"
              >
                放大
              </el-button>
            </el-button-group>
          </div>
          <div class="pdf-container">
            <iframe
              :src="getPdfUrl(file)"
              class="pdf-frame"
              :style="{ transform: `scale(${pdfScale})` }"
            ></iframe>
          </div>
        </div>

        <!-- 音频预览 -->
        <div v-else-if="isAudio(file)" class="audio-preview">
          <div class="audio-container">
            <div class="audio-info">
              <el-icon class="audio-icon" :size="64">
                <Headphone />
              </el-icon>
              <h4>{{ file.name }}</h4>
              <p class="audio-meta">{{ getFileType(file) }} • {{ formatFileSize(file.size) }}</p>
            </div>
            <audio
              :src="getFileUrl(file)"
              controls
              class="audio-player"
              preload="metadata"
            >
              您的浏览器不支持音频播放
            </audio>
          </div>
        </div>

        <!-- 视频预览 -->
        <div v-else-if="isVideo(file)" class="video-preview">
          <div class="video-container">
            <video
              :src="getFileUrl(file)"
              controls
              class="video-player"
              preload="metadata"
            >
              您的浏览器不支持视频播放
            </video>
          </div>
          <div class="video-info" v-if="videoInfo">
            <div class="info-item">
              <span class="label">时长:</span>
              <span class="value">{{ formatDuration(videoInfo.duration) }}</span>
            </div>
            <div class="info-item">
              <span class="label">尺寸:</span>
              <span class="value">{{ videoInfo.width }} × {{ videoInfo.height }}</span>
            </div>
          </div>
        </div>

        <!-- 代码预览 -->
        <div v-else-if="isCode(file)" class="code-preview">
          <div class="code-toolbar">
            <el-button-group>
              <el-button
                :icon="'el-icon-document-copy'"
                @click="copyContent"
                size="small"
              >
                复制代码
              </el-button>
              <el-button
                :icon="'el-icon-download'"
                @click="downloadFile"
                size="small"
              >
                下载文件
              </el-button>
            </el-button-group>
          </div>
          <div class="code-container">
            <pre v-if="textContent" class="code-content"><code :class="getCodeLanguage(file)">{{ textContent }}</code></pre>
            <div v-else class="loading-text">
              <el-skeleton :rows="10" animated />
            </div>
          </div>
        </div>

        <!-- 不支持的文件类型 -->
        <div v-else class="unsupported-preview">
          <div class="unsupported-container">
            <el-icon class="unsupported-icon" :size="64">
              <Document />
            </el-icon>
            <h3>无法预览此文件类型</h3>
            <p class="unsupported-desc">
              {{ getFileType(file) }} 文件暂不支持在线预览
            </p>
            <div class="unsupported-actions">
              <el-button
                type="primary"
                :icon="Download"
                @click="downloadFile"
              >
                下载文件
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          v-if="canEdit(file)"
          type="primary"
          @click="editFile"
        >
          编辑
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import {
  Download,
  DocumentCopy,
  ZoomIn,
  ZoomOut,
  Headphone,
  VideoPlay,
  Document,
  Picture,
  Folder
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  file: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'download', 'edit'])

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const textContent = ref('')
const textWrap = ref(true)
const imageInfo = ref(null)
const videoInfo = ref(null)
const pdfScale = ref(1)

// 计算属性
const dialogTitle = computed(() => {
  return props.file ? `预览 - ${props.file.name}` : '文件预览'
})

// 监听文件变化
watch(() => props.file, (newFile) => {
  if (newFile) {
    loadFileContent(newFile)
  }
}, { immediate: true })

// 方法
const handleClose = () => {
  visible.value = false
  // 重置状态
  textContent.value = ''
  imageInfo.value = null
  videoInfo.value = null
  pdfScale.value = 1
}

const loadFileContent = async (file) => {
  if (isText(file) || isCode(file)) {
    try {
      // 模拟加载文本内容
      textContent.value = '正在加载文件内容...'
      // 这里应该调用实际的API来获取文件内容
      // const response = await fetch(getFileUrl(file))
      // textContent.value = await response.text()
    } catch (error) {
      console.error('加载文件内容失败:', error)
      textContent.value = '加载文件内容失败'
    }
  }
}

const downloadFile = () => {
  emit('download', props.file)
}

const editFile = () => {
  emit('edit', props.file)
}

const copyPath = async () => {
  try {
    await navigator.clipboard.writeText(props.file.path || props.file.name)
    ElMessage.success('路径已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const copyContent = async () => {
  try {
    await navigator.clipboard.writeText(textContent.value)
    ElMessage.success('内容已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const handleImageLoad = (event) => {
  const img = event.target
  imageInfo.value = {
    width: img.naturalWidth,
    height: img.naturalHeight
  }
}

const handleImageError = () => {
  ElMessage.error('图片加载失败')
}

// 工具方法
const isImage = (file) => {
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
  const ext = file.name.split('.').pop()?.toLowerCase()
  return imageExts.includes(ext)
}

const isText = (file) => {
  const textExts = ['txt', 'md', 'json', 'xml', 'csv', 'log']
  const ext = file.name.split('.').pop()?.toLowerCase()
  return textExts.includes(ext)
}

const isCode = (file) => {
  const codeExts = ['js', 'ts', 'vue', 'html', 'css', 'scss', 'less', 'jsx', 'tsx', 'py', 'java', 'cpp', 'c', 'php', 'go', 'rs', 'rb']
  const ext = file.name.split('.').pop()?.toLowerCase()
  return codeExts.includes(ext)
}

const isPdf = (file) => {
  const ext = file.name.split('.').pop()?.toLowerCase()
  return ext === 'pdf'
}

const isAudio = (file) => {
  const audioExts = ['mp3', 'wav', 'flac', 'aac', 'ogg', 'wma']
  const ext = file.name.split('.').pop()?.toLowerCase()
  return audioExts.includes(ext)
}

const isVideo = (file) => {
  const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm', 'mkv']
  const ext = file.name.split('.').pop()?.toLowerCase()
  return videoExts.includes(ext)
}

const canEdit = (file) => {
  return isText(file) || isCode(file)
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
    xml: 'XML', yaml: 'YAML', yml: 'YAML'
  }
  
  return typeMap[ext] || `${ext.toUpperCase()} 文件`
}

const getCodeLanguage = (file) => {
  const ext = file.name.split('.').pop()?.toLowerCase()
  const langMap = {
    js: 'javascript',
    ts: 'typescript',
    vue: 'vue',
    html: 'html',
    css: 'css',
    scss: 'scss',
    less: 'less',
    jsx: 'jsx',
    tsx: 'tsx',
    py: 'python',
    java: 'java',
    cpp: 'cpp',
    c: 'c',
    php: 'php',
    go: 'go',
    rs: 'rust',
    rb: 'ruby'
  }
  return langMap[ext] || 'text'
}

const getFileUrl = (file) => {
  // 这里应该返回实际的文件URL
  return file.url || `/api/files/${file.id}/content`
}

const getPdfUrl = (file) => {
  // 这里应该返回PDF预览URL
  return file.url || `/api/files/${file.id}/preview`
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

const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.file-preview-dialog {
  :deep(.el-dialog) {
    max-height: 90vh;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-dialog__body) {
    flex: 1;
    overflow: hidden;
    padding: 0;
  }

  .preview-container {
    height: 100%;
    display: flex;
    flex-direction: column;

    .file-info-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      border-bottom: 1px solid #e4e7ed;
      background: #f8f9fa;

      .file-basic-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .file-icon {
          color: #409eff;
        }

        .file-details {
          .file-name {
            margin: 0 0 4px 0;
            font-size: 16px;
            font-weight: 500;
            color: #303133;
          }

          .file-meta {
            display: flex;
            gap: 16px;
            font-size: 14px;
            color: #909399;

            span {
              display: flex;
              align-items: center;
            }
          }
        }
      }

      .file-actions {
        display: flex;
        gap: 8px;
      }
    }

    .preview-content {
      flex: 1;
      overflow: hidden;
      padding: 20px;

      .image-preview {
        height: 100%;
        display: flex;
        flex-direction: column;

        .image-container {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #f5f7fa;
          border-radius: 8px;
          overflow: hidden;
          margin-bottom: 16px;

          .preview-image {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
          }
        }

        .image-info {
          display: flex;
          gap: 24px;
          padding: 12px;
          background: #fff;
          border: 1px solid #e4e7ed;
          border-radius: 6px;

          .info-item {
            display: flex;
            gap: 8px;

            .label {
              font-weight: 500;
              color: #606266;
            }

            .value {
              color: #303133;
            }
          }
        }
      }

      .text-preview,
      .code-preview {
        height: 100%;
        display: flex;
        flex-direction: column;

        .text-toolbar,
        .code-toolbar {
          margin-bottom: 12px;
        }

        .text-container,
        .code-container {
          flex: 1;
          border: 1px solid #e4e7ed;
          border-radius: 6px;
          overflow: hidden;

          .text-content,
          .code-content {
            height: 100%;
            margin: 0;
            padding: 16px;
            background: #fafafa;
            font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
            font-size: 14px;
            line-height: 1.5;
            overflow: auto;
            white-space: pre;

            &.text-wrap {
              white-space: pre-wrap;
            }
          }

          .loading-text {
            padding: 16px;
          }
        }
      }

      .pdf-preview {
        height: 100%;
        display: flex;
        flex-direction: column;

        .pdf-toolbar {
          margin-bottom: 12px;
        }

        .pdf-container {
          flex: 1;
          border: 1px solid #e4e7ed;
          border-radius: 6px;
          overflow: hidden;

          .pdf-frame {
            width: 100%;
            height: 100%;
            border: none;
            transform-origin: top left;
          }
        }
      }

      .audio-preview {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;

        .audio-container {
          text-align: center;

          .audio-info {
            margin-bottom: 24px;

            .audio-icon {
              color: #67c23a;
              margin-bottom: 16px;
            }

            h4 {
              margin: 0 0 8px 0;
              font-size: 18px;
              color: #303133;
            }

            .audio-meta {
              margin: 0;
              color: #909399;
            }
          }

          .audio-player {
            width: 100%;
            max-width: 400px;
          }
        }
      }

      .video-preview {
        height: 100%;
        display: flex;
        flex-direction: column;

        .video-container {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #000;
          border-radius: 8px;
          overflow: hidden;
          margin-bottom: 16px;

          .video-player {
            max-width: 100%;
            max-height: 100%;
          }
        }

        .video-info {
          display: flex;
          gap: 24px;
          padding: 12px;
          background: #fff;
          border: 1px solid #e4e7ed;
          border-radius: 6px;

          .info-item {
            display: flex;
            gap: 8px;

            .label {
              font-weight: 500;
              color: #606266;
            }

            .value {
              color: #303133;
            }
          }
        }
      }

      .unsupported-preview {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;

        .unsupported-container {
          text-align: center;

          .unsupported-icon {
            color: #c0c4cc;
            margin-bottom: 16px;
          }

          h3 {
            margin: 0 0 8px 0;
            color: #606266;
          }

          .unsupported-desc {
            margin: 0 0 24px 0;
            color: #909399;
          }

          .unsupported-actions {
            display: flex;
            justify-content: center;
          }
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .file-preview-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 5vh auto;
    }

    .preview-container {
      .file-info-header {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;

        .file-basic-info {
          justify-content: center;
        }

        .file-actions {
          justify-content: center;
        }
      }

      .preview-content {
        padding: 12px;

        .image-preview {
          .image-info {
            flex-direction: column;
            gap: 8px;
          }
        }

        .video-preview {
          .video-info {
            flex-direction: column;
            gap: 8px;
          }
        }
      }
    }
  }
}
</style>