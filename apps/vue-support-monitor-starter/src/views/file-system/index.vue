<template>
  <div class="file-system-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <IconifyIconOnline icon="ri:file-list-3-line" class="mr-2" />
          文件管理
        </h2>
        <p class="page-description">分片上传、文件管理、HTTP访问控制</p>
      </div>
      <div class="header-right">
        <!-- SSE连接状态 -->
        <div class="connection-status">
          <el-tooltip :content="connectionStatusText" placement="bottom">
            <div class="status-indicator" :class="connectionStatusClass">
              <IconifyIconOnline
                :icon="connectionStatusIcon"
                class="status-icon"
              />
              <span class="status-text">{{ connectionStatusText }}</span>
            </div>
          </el-tooltip>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="showUploadDialog = true">
            <IconifyIconOnline icon="ri:upload-line" class="mr-1" />
            上传文件
          </el-button>
          <el-button @click="showSettingsDialog = true">
            <IconifyIconOnline icon="ri:settings-line" class="mr-1" />
            设置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats-container">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <IconifyIconOnline icon="ri:file-list-line" />
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ statistics.totalFiles }}</div>
                <div class="stats-label">总文件数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon size">
                <IconifyIconOnline icon="ri:hard-drive-line" />
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ formatFileSize(statistics.totalSize) }}</div>
                <div class="stats-label">总大小</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon pending">
                <IconifyIconOnline icon="ri:time-line" />
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ statistics.pendingFiles }}</div>
                <div class="stats-label">待处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon completed">
                <IconifyIconOnline icon="ri:check-line" />
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ statistics.completedFiles }}</div>
                <div class="stats-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 实时状态 -->
    <div class="realtime-status" v-if="sseState.connected">
      <el-card class="status-card">
        <div class="status-content">
          <div class="status-item">
            <span class="status-label">上传中:</span>
            <span class="status-value">{{ realtimeStatus.uploadingCount }}</span>
          </div>
          <div class="status-item">
            <span class="status-label">合并中:</span>
            <span class="status-value">{{ realtimeStatus.mergingCount }}</span>
          </div>
          <div class="status-item">
            <span class="status-label">队列长度:</span>
            <span class="status-value">{{ realtimeStatus.queueLength }}</span>
          </div>
          <div class="status-item">
            <span class="status-label">总速度:</span>
            <span class="status-value">{{ formatSpeed(realtimeStatus.totalSpeed) }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 文件列表 -->
    <div class="file-list-container">
      <div class="list-header">
        <div class="search-bar">
          <el-input
            v-model="searchQuery.fileName"
            placeholder="搜索文件名..."
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <IconifyIconOnline icon="ri:search-line" />
            </template>
          </el-input>
          <el-select
            v-model="searchQuery.fileStatus"
            placeholder="文件状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" :value="null" />
            <el-option label="待合并" :value="0" />
            <el-option label="合并中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="合并失败" :value="3" />
          </el-select>
          <el-button type="primary" @click="handleSearch">
            <IconifyIconOnline icon="ri:search-line" class="mr-1" />
            搜索
          </el-button>
        </div>
        <div class="list-actions">
          <el-button @click="refreshData">
            <IconifyIconOnline icon="ri:refresh-line" class="mr-1" />
            刷新
          </el-button>
          <el-button 
            type="danger" 
            :disabled="selectedFiles.length === 0"
            @click="handleBatchDelete"
          >
            <IconifyIconOnline icon="ri:delete-bin-line" class="mr-1" />
            批量删除
          </el-button>
        </div>
      </div>

      <!-- 文件表格 -->
      <div class="file-table">
        <ScTable
          ref="tableRef"
          :url="getFileSystemPage"
          :params="searchQuery"
          table-name="file-system"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="文件名" min-width="200">
            <template #default="{ row }">
              <div class="file-name-cell">
                <IconifyIconOnline
                  :icon="getFileIcon(row.fileSystemType)"
                  class="file-icon"
                />
                <span class="file-name" :title="row.fileSystemName">
                  {{ row.fileSystemName }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="文件大小" width="120" align="right">
            <template #default="{ row }">
              {{ formatFileSize(row.fileSystemSize) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.fileSystemStatus)">
                {{ getStatusText(row.fileSystemStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上传时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="handleDownload(row)"
                :disabled="row.fileSystemStatus !== 2"
              >
                下载
              </el-button>
              <el-button
                type="success"
                size="small"
                @click="handleToggleHttpAccess(row)"
                :disabled="row.fileSystemStatus !== 2"
              >
                {{ row.fileSystemHttpAccessEnabled ? '禁用' : '启用' }}HTTP
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </ScTable>
      </div>
    </div>

    <!-- 上传对话框 -->
    <FileUploadDialog
      v-model="showUploadDialog"
      @success="handleUploadSuccess"
    />

    <!-- 设置对话框 -->
    <FileSystemSettings
      v-model="showSettingsDialog"
      @settings-updated="handleSettingsUpdated"
    />

    <!-- 上传队列状态 -->
    <UploadQueueStatus
      ref="queueStatusRef"
      :queue-status="queueStatus"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { formatBytes } from "@pureadmin/utils";
import dayjs from "dayjs";
import type {
  FileSystem,
  FileStatistics,
  UploadQueueStatus as QueueStatus,
} from "@/api/monitor/filesystem";
import {
  getFileSystemPage,
  getFileStatistics,
  deleteFile,
  batchDeleteFiles,
  downloadFile,
  toggleHttpAccess,
  cleanExpiredFiles,
  retryMergeTask,
  manualMergeFile,
} from "@/api/monitor/filesystem";
import { useFileSystemSSE } from "@/composables/useFileSystemSSE";
import FileUploadDialog from "./components/FileUploadDialog.vue";
import UploadQueueStatus from "./components/UploadQueueStatus.vue";
import FileSystemSettings from "./components/FileSystemSettings.vue";

// SSE连接
const {
  state: sseState,
  realtimeStatus,
  queueStatus,
  connect: connectSSE,
  disconnect: disconnectSSE,
  onMessage,
  MESSAGE_TYPE,
} = useFileSystemSSE();

// 响应式数据
const showUploadDialog = ref(false);
const showSettingsDialog = ref(false);
const selectedFiles = ref<FileSystem[]>([]);
const queueStatusRef = ref();
const tableRef = ref();

// 统计信息
const statistics = ref<FileStatistics>({
  totalFiles: 0,
  totalSize: 0,
  pendingFiles: 0,
  mergingFiles: 0,
  completedFiles: 0,
  failedFiles: 0,
});

// 搜索条件
const searchQuery = reactive({
  fileName: "",
  fileStatus: null as number | null,
});

// 连接状态计算属性
const connectionStatusText = computed(() => {
  if (sseState.value.connecting) return "连接中";
  if (sseState.value.connected) return "已连接";
  if (sseState.value.error) return "连接错误";
  return "未连接";
});

const connectionStatusClass = computed(() => {
  if (sseState.value.connecting) return "connecting";
  if (sseState.value.connected) return "connected";
  if (sseState.value.error) return "error";
  return "disconnected";
});

const connectionStatusIcon = computed(() => {
  if (sseState.value.connecting) return "ri:loader-4-line";
  if (sseState.value.connected) return "ri:wifi-line";
  if (sseState.value.error) return "ri:wifi-off-line";
  return "ri:wifi-off-line";
});

// 生命周期
onMounted(() => {
  loadStatistics();
  connectSSE(); // 连接SSE
  
  // 注册SSE消息处理器
  onMessage(MESSAGE_TYPE.UPLOAD_COMPLETED, () => {
    refreshData();
  });
  
  onMessage(MESSAGE_TYPE.MERGE_COMPLETED, () => {
    refreshData();
  });
  
  onMessage(MESSAGE_TYPE.FILE_DELETED, () => {
    refreshData();
  });
});

onUnmounted(() => {
  disconnectSSE(); // 断开SSE连接
});

/**
 * 加载统计信息
 */
const loadStatistics = async () => {
  try {
    console.log("正在加载文件系统统计信息...");
    const res = await getFileStatistics();
    console.log("统计信息API响应:", res);

    if (res.code === "00000" && res.data) {
      statistics.value = res.data;
      console.log("统计信息数据:", statistics.value);
    } else {
      console.error("统计信息API返回错误:", res);
    }
  } catch (error) {
    console.error("加载统计信息失败:", error);
  }
};

/**
 * 刷新数据
 */
const refreshData = () => {
  tableRef.value?.refresh();
  loadStatistics();
};

/**
 * 搜索处理
 */
const handleSearch = () => {
  tableRef.value?.refresh();
};

/**
 * 选择变化处理
 */
const handleSelectionChange = (selection: FileSystem[]) => {
  selectedFiles.value = selection;
};

/**
 * 格式化文件大小
 */
const formatFileSize = (size: number) => {
  return formatBytes(size);
};

/**
 * 格式化速度
 */
const formatSpeed = (speed: number) => {
  return formatBytes(speed) + "/s";
};

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  return time ? dayjs(time).format("YYYY-MM-DD HH:mm:ss") : "-";
};

/**
 * 获取文件图标
 */
const getFileIcon = (fileType: string) => {
  const iconMap: Record<string, string> = {
    'image': 'ri:image-line',
    'video': 'ri:video-line',
    'audio': 'ri:music-line',
    'document': 'ri:file-text-line',
    'archive': 'ri:file-zip-line',
    'code': 'ri:code-line',
  };
  return iconMap[fileType] || 'ri:file-line';
};

/**
 * 获取状态类型
 */
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',  // 待合并
    1: 'primary',  // 合并中
    2: 'success',  // 已完成
    3: 'danger',   // 合并失败
  };
  return typeMap[status] || 'info';
};

/**
 * 获取状态文本
 */
const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待合并',
    1: '合并中',
    2: '已完成',
    3: '合并失败',
  };
  return textMap[status] || '未知';
};

/**
 * 下载文件
 */
const handleDownload = async (file: FileSystem) => {
  try {
    await downloadFile(file.fileSystemId);
    ElMessage.success("下载开始");
  } catch (error) {
    console.error("下载失败:", error);
    ElMessage.error("下载失败");
  }
};

/**
 * 切换HTTP访问
 */
const handleToggleHttpAccess = async (file: FileSystem) => {
  try {
    const res = await toggleHttpAccess(file.fileSystemId, !file.fileSystemHttpAccessEnabled);
    if (res.code === "00000") {
      ElMessage.success(file.fileSystemHttpAccessEnabled ? "HTTP访问已禁用" : "HTTP访问已启用");
      refreshData();
    } else {
      ElMessage.error(res.message || "操作失败");
    }
  } catch (error) {
    console.error("切换HTTP访问失败:", error);
    ElMessage.error("操作失败");
  }
};

/**
 * 删除文件
 */
const handleDelete = async (file: FileSystem) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${file.fileSystemName}" 吗？`,
      "确认删除",
      {
        type: "warning",
      }
    );

    const res = await deleteFile(file.fileSystemId);
    if (res.code === "00000") {
      ElMessage.success("删除成功");
      refreshData();
    } else {
      ElMessage.error(res.message || "删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning("请选择要删除的文件");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`,
      "确认批量删除",
      {
        type: "warning",
      }
    );

    const fileIds = selectedFiles.value.map(file => file.fileSystemId);
    const res = await batchDeleteFiles(fileIds);
    if (res.code === "00000") {
      ElMessage.success("批量删除成功");
      refreshData();
    } else {
      ElMessage.error(res.message || "批量删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("批量删除失败:", error);
      ElMessage.error("批量删除失败");
    }
  }
};

/**
 * 上传成功处理
 */
const handleUploadSuccess = () => {
  refreshData();
};

/**
 * 设置更新处理
 */
const handleSettingsUpdated = () => {
  refreshData();
};
</script>

<style scoped lang="scss">
.file-system-page {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;

    .header-left {
      .page-title {
        margin: 0 0 8px 0;
        color: var(--el-text-color-primary);
        font-size: 24px;
        font-weight: 600;
        display: flex;
        align-items: center;
      }

      .page-description {
        margin: 0;
        color: var(--el-text-color-regular);
        font-size: 14px;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;

      .connection-status {
        .status-indicator {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          border-radius: 6px;
          font-size: 14px;
          transition: all 0.3s ease;

          &.connected {
            background: rgba(103, 194, 58, 0.1);
            color: #67c23a;
          }

          &.connecting {
            background: rgba(230, 162, 60, 0.1);
            color: #e6a23c;
            
            .status-icon {
              animation: spin 1s linear infinite;
            }
          }

          &.error,
          &.disconnected {
            background: rgba(245, 108, 108, 0.1);
            color: #f56c6c;
          }

          .status-icon {
            font-size: 16px;
          }
        }
      }
    }
  }

  .stats-container {
    margin-bottom: 20px;

    .stats-card {
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .stats-content {
        display: flex;
        align-items: center;
        padding: 8px;

        .stats-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          font-size: 24px;

          &.total {
            background: rgba(64, 158, 255, 0.1);
            color: #409eff;
          }

          &.size {
            background: rgba(103, 194, 58, 0.1);
            color: #67c23a;
          }

          &.pending {
            background: rgba(230, 162, 60, 0.1);
            color: #e6a23c;
          }

          &.completed {
            background: rgba(103, 194, 58, 0.1);
            color: #67c23a;
          }
        }

        .stats-info {
          .stats-value {
            font-size: 24px;
            font-weight: 600;
            color: var(--el-text-color-primary);
            line-height: 1;
            margin-bottom: 4px;
          }

          .stats-label {
            font-size: 14px;
            color: var(--el-text-color-regular);
          }
        }
      }
    }
  }

  .realtime-status {
    margin-bottom: 20px;

    .status-card {
      .status-content {
        display: flex;
        gap: 24px;

        .status-item {
          display: flex;
          align-items: center;
          gap: 8px;

          .status-label {
            font-size: 14px;
            color: var(--el-text-color-regular);
          }

          .status-value {
            font-size: 14px;
            font-weight: 600;
            color: var(--el-text-color-primary);
          }
        }
      }
    }
  }

  .file-list-container {
    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .search-bar {
        display: flex;
        gap: 12px;
        align-items: center;
      }

      .list-actions {
        display: flex;
        gap: 8px;
      }
    }

    .file-table {
      .file-name-cell {
        display: flex;
        align-items: center;
        gap: 8px;

        .file-icon {
          font-size: 16px;
          color: var(--el-color-primary);
        }

        .file-name {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
