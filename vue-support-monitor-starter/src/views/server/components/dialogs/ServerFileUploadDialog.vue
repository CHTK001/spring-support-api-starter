<template>
  <el-dialog
    v-model="visible"
    title="服务器文件上传"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="upload-form"
    >
      <!-- 服务器选择 -->
      <el-form-item label="目标服务器" prop="serverId">
        <el-select
          v-model="form.serverId"
          placeholder="请选择SSH服务器"
          style="width: 100%"
          @change="handleServerChange"
        >
          <el-option
            v-for="server in sshServers"
            :key="server.monitorSysGenServerId"
            :label="`${server.monitorSysGenServerName} (${server.monitorSysGenServerHost})`"
            :value="server.monitorSysGenServerId"
          />
        </el-select>
      </el-form-item>

      <!-- 任务名称 -->
      <el-form-item label="任务名称" prop="taskName">
        <el-input
          v-model="form.taskName"
          placeholder="请输入任务名称"
          maxlength="255"
          show-word-limit
        />
      </el-form-item>

      <!-- 目标路径 -->
      <el-form-item label="目标路径" prop="targetPath">
        <el-input
          v-model="form.targetPath"
          placeholder="请输入目标路径，如：/tmp/upload/"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <!-- 上传模式 -->
      <el-form-item label="上传模式" prop="uploadMode">
        <el-radio-group v-model="form.uploadMode">
          <el-radio value="REALTIME">实时上传</el-radio>
          <el-radio value="SCHEDULED">定时上传</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 定时执行时间 -->
      <el-form-item
        v-if="form.uploadMode === 'SCHEDULED'"
        label="执行时间"
        prop="scheduledTime"
      >
        <el-date-picker
          v-model="form.scheduledTime"
          type="datetime"
          placeholder="选择执行时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 优先级 -->
      <el-form-item label="优先级">
        <el-slider
          v-model="form.priority"
          :min="1"
          :max="10"
          :marks="{ 1: '最高', 5: '普通', 10: '最低' }"
          show-tooltip
        />
      </el-form-item>

      <!-- 高级选项 -->
      <el-form-item label="高级选项">
        <div class="advanced-options">
          <el-checkbox v-model="form.overwrite">覆盖已存在文件</el-checkbox>
          <el-checkbox v-model="form.backup">备份原文件</el-checkbox>
          <el-checkbox v-model="form.compress">压缩传输</el-checkbox>
          <el-checkbox v-model="form.verify">验证文件完整性</el-checkbox>
        </div>
      </el-form-item>

      <!-- 超时设置 -->
      <el-form-item label="超时时间">
        <el-input-number
          v-model="form.timeoutSeconds"
          :min="60"
          :max="3600"
          :step="60"
          controls-position="right"
          style="width: 200px"
        />
        <span class="ml-2 text-gray-500">秒</span>
      </el-form-item>

      <!-- 重试次数 -->
      <el-form-item label="最大重试次数">
        <el-input-number
          v-model="form.maxRetry"
          :min="0"
          :max="10"
          controls-position="right"
          style="width: 200px"
        />
      </el-form-item>

      <!-- 任务描述 -->
      <el-form-item label="任务描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入任务描述（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 标签 -->
      <el-form-item label="标签">
        <el-input
          v-model="form.tags"
          placeholder="多个标签用逗号分隔"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 文件选择 -->
      <el-form-item label="选择文件" prop="file">
        <div class="file-upload-area">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-remove="handleBeforeRemove"
            drag
          >
            <el-icon class="el-icon--upload">
              <UploadFilled />
            </el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持单个文件上传，文件大小不超过 1GB
              </div>
            </template>
          </el-upload>
        </div>
      </el-form-item>

      <!-- 当前活跃任务提示 -->
      <el-alert
        v-if="activeTask"
        title="该服务器当前有正在进行的上传任务"
        type="warning"
        :description="`任务名称：${activeTask.monitorSysGenServerFileUploadTaskName}，状态：${getStatusText(activeTask.monitorSysGenServerFileUploadStatus)}`"
        show-icon
        :closable="false"
        class="mb-4"
      />
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="uploading"
          :disabled="!canUpload"
          @click="handleUpload"
        >
          {{ uploading ? '上传中...' : '开始上传' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';
import type { FormInstance, FormRules, UploadFile } from 'element-plus';
import {
  createServerFileUploadTask,
  getActiveTaskByServerId,
  validateServerForUpload,
  type ServerFileUploadRequest,
  type ServerFileUploadTask,
  TASK_STATUS
} from '@/api/server-file-upload';

// Props
interface Props {
  sshServers?: Array<{
    monitorSysGenServerId: number;
    monitorSysGenServerName: string;
    monitorSysGenServerHost: string;
    monitorSysGenServerType: string;
  }>;
}

const props = withDefaults(defineProps<Props>(), {
  sshServers: () => []
});

// Emits
const emit = defineEmits<{
  success: [task: ServerFileUploadTask];
  close: [];
}>();

// 响应式数据
const visible = ref(false);
const uploading = ref(false);
const formRef = ref<FormInstance>();
const uploadRef = ref();
const selectedFile = ref<File | null>(null);
const activeTask = ref<ServerFileUploadTask | null>(null);

// 表单数据
const form = reactive<ServerFileUploadRequest & { file?: File }>({
  serverId: 0,
  taskName: '',
  targetPath: '',
  uploadMode: 'REALTIME',
  scheduledTime: '',
  priority: 5,
  overwrite: false,
  backup: false,
  compress: false,
  verify: true,
  description: '',
  tags: '',
  timeoutSeconds: 300,
  maxRetry: 3
});

// 表单验证规则
const rules: FormRules = {
  serverId: [
    { required: true, message: '请选择目标服务器', trigger: 'change' }
  ],
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 1, max: 255, message: '任务名称长度在 1 到 255 个字符', trigger: 'blur' }
  ],
  targetPath: [
    { required: true, message: '请输入目标路径', trigger: 'blur' },
    { min: 1, max: 1000, message: '目标路径长度在 1 到 1000 个字符', trigger: 'blur' }
  ],
  uploadMode: [
    { required: true, message: '请选择上传模式', trigger: 'change' }
  ],
  scheduledTime: [
    {
      validator: (rule, value, callback) => {
        if (form.uploadMode === 'SCHEDULED' && !value) {
          callback(new Error('定时上传模式下必须选择执行时间'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  file: [
    {
      validator: (rule, value, callback) => {
        if (!selectedFile.value) {
          callback(new Error('请选择要上传的文件'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ]
};

// 计算属性
const canUpload = computed(() => {
  return form.serverId && form.taskName && form.targetPath && selectedFile.value && !uploading.value;
});

// 监听服务器变化
watch(() => form.serverId, async (newServerId) => {
  if (newServerId) {
    await checkActiveTask(newServerId);
    await checkServerValidation(newServerId);
  }
});

// 方法
const open = () => {
  visible.value = true;
  resetForm();
};

const close = () => {
  visible.value = false;
  emit('close');
};

const handleClose = () => {
  close();
};

const resetForm = () => {
  formRef.value?.resetFields();
  selectedFile.value = null;
  activeTask.value = null;
  uploading.value = false;
  
  // 重置表单数据
  Object.assign(form, {
    serverId: 0,
    taskName: '',
    targetPath: '',
    uploadMode: 'REALTIME',
    scheduledTime: '',
    priority: 5,
    overwrite: false,
    backup: false,
    compress: false,
    verify: true,
    description: '',
    tags: '',
    timeoutSeconds: 300,
    maxRetry: 3
  });
};

const handleServerChange = async (serverId: number) => {
  if (serverId) {
    await checkActiveTask(serverId);
    await checkServerValidation(serverId);
  }
};

const checkActiveTask = async (serverId: number) => {
  try {
    const { data } = await getActiveTaskByServerId(serverId);
    activeTask.value = data;
  } catch (error) {
    console.warn('检查活跃任务失败:', error);
    activeTask.value = null;
  }
};

const checkServerValidation = async (serverId: number) => {
  try {
    const { data } = await validateServerForUpload(serverId);
    if (!data) {
      ElMessage.warning('该服务器不支持文件上传功能');
    }
  } catch (error) {
    console.warn('验证服务器失败:', error);
  }
};

const handleFileChange = (file: UploadFile) => {
  if (file.raw) {
    selectedFile.value = file.raw;
    
    // 自动生成任务名称
    if (!form.taskName) {
      form.taskName = `上传文件 - ${file.name}`;
    }
  }
};

const handleFileRemove = () => {
  selectedFile.value = null;
};

const handleBeforeRemove = () => {
  return ElMessageBox.confirm('确定移除该文件吗？', '提示', {
    type: 'warning'
  });
};

const handleUpload = async () => {
  if (!formRef.value || !selectedFile.value) return;

  try {
    await formRef.value.validate();
    
    uploading.value = true;
    
    const { data } = await createServerFileUploadTask(selectedFile.value, form);
    
    ElMessage.success('上传任务创建成功');
    emit('success', data);
    close();
    
  } catch (error: any) {
    console.error('创建上传任务失败:', error);
    ElMessage.error(error.message || '创建上传任务失败');
  } finally {
    uploading.value = false;
  }
};

const getStatusText = (status: string) => {
  const statusMap = {
    [TASK_STATUS.PENDING]: '待处理',
    [TASK_STATUS.PROCESSING]: '处理中',
    [TASK_STATUS.COMPLETED]: '已完成',
    [TASK_STATUS.FAILED]: '失败',
    [TASK_STATUS.CANCELLED]: '已取消'
  };
  return statusMap[status] || status;
};

// 暴露方法
defineExpose({
  open,
  close
});
</script>

<style scoped>
.upload-form {
  max-height: 60vh;
  overflow-y: auto;
}

.advanced-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-upload-area {
  width: 100%;
}

.el-icon--upload {
  font-size: 67px;
  color: #c0c4cc;
  margin: 40px 0 16px;
  line-height: 50px;
}

.dialog-footer {
  text-align: right;
}

.mb-4 {
  margin-bottom: 16px;
}

.ml-2 {
  margin-left: 8px;
}

.text-gray-500 {
  color: #6b7280;
}
</style>
