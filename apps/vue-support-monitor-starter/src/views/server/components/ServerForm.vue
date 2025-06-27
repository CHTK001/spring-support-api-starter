<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="140px"
    class="server-form"
  >
    <!-- 基础信息 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <el-icon><Server /></el-icon>
          <span>基础信息</span>
        </div>
      </template>
      
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="服务器名称" prop="monitorSysGenServerName">
            <el-input
              v-model="formData.monitorSysGenServerName"
              placeholder="请输入服务器名称"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="服务器主机" prop="monitorSysGenServerHost">
            <el-input
              v-model="formData.monitorSysGenServerHost"
              placeholder="请输入服务器主机地址"
              clearable
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="服务器端口" prop="monitorSysGenServerPort">
            <el-input-number
              v-model="formData.monitorSysGenServerPort"
              :min="1"
              :max="65535"
              placeholder="请输入端口号"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="连接协议" prop="monitorSysGenServerProtocol">
            <el-select
              v-model="formData.monitorSysGenServerProtocol"
              placeholder="请选择连接协议"
              style="width: 100%"
            >
              <el-option
                v-for="protocol in protocolOptions"
                :key="protocol.value"
                :label="protocol.label"
                :value="protocol.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="用户名" prop="monitorSysGenServerUsername">
            <el-input
              v-model="formData.monitorSysGenServerUsername"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="密码" prop="monitorSysGenServerPassword">
            <el-input
              v-model="formData.monitorSysGenServerPassword"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="服务器描述">
        <el-input
          v-model="formData.monitorSysGenServerDesc"
          type="textarea"
          :rows="3"
          placeholder="请输入服务器描述"
        />
      </el-form-item>
    </el-card>

    <!-- 代理配置 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <el-icon><Connection /></el-icon>
          <span>代理配置</span>
          <el-tooltip content="配置代理服务器用于远程连接" placement="top">
            <el-icon class="help-icon"><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
      </template>
      
      <!-- 代理选择器 -->
      <ProxySelector
        v-model="formData.monitorSysGenServerProxyId"
        label="代理服务器"
        prop="monitorSysGenServerProxyId"
        placeholder="请选择代理配置（可选）"
        :show-details="true"
        :show-actions="true"
        @change="handleProxyChange"
        @create-proxy="handleCreateProxy"
      />
      
      <!-- 代理服务器地址（手动配置） -->
      <el-row :gutter="24" v-if="!formData.monitorSysGenServerProxyId">
        <el-col :span="12">
          <el-form-item label="代理主机">
            <el-input
              v-model="formData.monitorSysGenServerProxyHost"
              placeholder="代理服务器地址（可选）"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="代理端口">
            <el-input-number
              v-model="formData.monitorSysGenServerProxyPort"
              :min="1"
              :max="65535"
              placeholder="代理端口"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-card>

    <!-- 监控配置 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>监控配置</span>
        </div>
      </template>
      
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="启用监控">
            <div class="switch-container">
              <el-switch v-model="monitorEnabled" />
              <el-tooltip content="开启后将定期收集服务器监控数据" placement="top">
                <el-icon class="switch-help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="服务器状态">
            <div class="switch-container">
              <el-switch v-model="serverEnabled" />
              <el-tooltip content="控制服务器是否启用，禁用后不会进行监控" placement="top">
                <el-icon class="switch-help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="监控间隔(秒)">
            <el-input-number
              v-model="formData.monitorSysGenServerMonitorInterval"
              :min="10"
              :max="3600"
              placeholder="监控间隔"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="连接超时(毫秒)">
            <el-input-number
              v-model="formData.monitorSysGenServerTimeout"
              :min="1000"
              :max="60000"
              placeholder="连接超时时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="指标保留天数">
        <el-input-number
          v-model="formData.monitorSysGenServerMetricsRetentionDays"
          :min="1"
          :max="365"
          placeholder="指标数据保留天数"
          style="width: 200px"
        />
      </el-form-item>
    </el-card>

    <!-- 高级配置 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <el-icon><Setting /></el-icon>
          <span>高级配置</span>
        </div>
      </template>
      
      <!-- 数据上报配置 -->
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="启用数据上报">
            <div class="switch-container">
              <el-switch v-model="reportEnabled" />
              <el-tooltip content="开启后将向监控中心上报服务器数据" placement="top">
                <el-icon class="switch-help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="上报方式">
            <div class="select-container">
              <el-select
                v-model="formData.monitorSysGenServerDataReportMethod"
                placeholder="请选择上报方式"
                style="width: 100%"
                :disabled="!reportEnabled"
              >
                <el-option label="无" value="NONE" />
                <el-option label="API上报" value="API" />
                <el-option label="Prometheus" value="PROMETHEUS" />
              </el-select>
              <el-tooltip content="选择数据上报的方式：API直接上报或通过Prometheus收集" placement="top">
                <el-icon class="select-help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- Prometheus配置 -->
      <el-row :gutter="24" v-if="formData.monitorSysGenServerDataReportMethod === 'PROMETHEUS'">
        <el-col :span="12">
          <el-form-item label="Prometheus主机">
            <el-input
              v-model="formData.monitorSysGenServerPrometheusHost"
              placeholder="Prometheus服务器地址"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Prometheus端口">
            <el-input-number
              v-model="formData.monitorSysGenServerPrometheusPort"
              :min="1"
              :max="65535"
              placeholder="Prometheus端口"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- Docker配置 -->
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="Docker支持">
            <div class="switch-container">
              <el-switch v-model="dockerEnabled" />
              <el-tooltip content="开启后可以监控和管理Docker容器" placement="top">
                <el-icon class="switch-help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="dockerEnabled">
          <el-form-item label="Docker连接方式">
            <div class="select-container">
              <el-select
                v-model="formData.monitorSysGenServerDockerConnectionType"
                placeholder="请选择连接方式"
                style="width: 100%"
              >
                <el-option label="Shell连接" value="SHELL" />
                <el-option label="TCP连接" value="TCP" />
              </el-select>
              <el-tooltip content="Shell连接通过SSH执行Docker命令，TCP连接直接连接Docker API" placement="top">
                <el-icon class="select-help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- Docker TCP配置 -->
      <el-row :gutter="24" v-if="dockerEnabled && formData.monitorSysGenServerDockerConnectionType === 'TCP'">
        <el-col :span="12">
          <el-form-item label="Docker主机">
            <el-input
              v-model="formData.monitorSysGenServerDockerHost"
              placeholder="Docker TCP地址"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Docker端口">
            <el-input-number
              v-model="formData.monitorSysGenServerDockerPort"
              :min="1"
              :max="65535"
              placeholder="Docker TCP端口"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- 多IP地址配置 -->
      <el-form-item label="额外IP地址">
        <el-input
          v-model="formData.monitorSysGenServerIpAddresses"
          type="textarea"
          :rows="2"
          placeholder="多个IP地址用逗号分隔，用于多网卡服务器"
        />
      </el-form-item>
      
      <!-- 服务器标签 -->
      <el-form-item label="服务器标签">
        <el-input
          v-model="formData.monitorSysGenServerTags"
          placeholder="多个标签用逗号分隔，用于分组管理"
          clearable
        />
      </el-form-item>
    </el-card>
  </el-form>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Server, Connection, Monitor, Setting, QuestionFilled } from '@element-plus/icons-vue'
import ProxySelector from '@/components/monitor/ProxySelector.vue'
import type { ServerInfo } from '@/api/monitor/server'
import type { ProxyOption } from '@/api/monitor/proxy'

interface Props {
  /** 表单数据 */
  modelValue: Partial<ServerInfo>
  /** 是否为编辑模式 */
  isEdit?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: Partial<ServerInfo>): void
  (e: 'create-proxy'): void
}

const props = withDefaults(defineProps<Props>(), {
  isEdit: false
})

const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref()

// 表单数据
const formData = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 协议选项
const protocolOptions = [
  { label: 'SSH', value: 'SSH' },
  { label: 'RDP', value: 'RDP' },
  { label: 'VNC', value: 'VNC' },
  { label: 'Telnet', value: 'TELNET' },
  { label: 'HTTP', value: 'HTTP' },
  { label: 'HTTPS', value: 'HTTPS' }
]

// 计算属性 - 开关状态
const monitorEnabled = computed({
  get: () => formData.value.monitorSysGenServerMonitorEnabled === 1,
  set: (value) => {
    formData.value.monitorSysGenServerMonitorEnabled = value ? 1 : 0
  }
})

const serverEnabled = computed({
  get: () => formData.value.monitorSysGenServerStatus === 1,
  set: (value) => {
    formData.value.monitorSysGenServerStatus = value ? 1 : 0
  }
})

const reportEnabled = computed({
  get: () => formData.value.monitorSysGenServerReportEnabled === 1,
  set: (value) => {
    formData.value.monitorSysGenServerReportEnabled = value ? 1 : 0
    if (!value) {
      formData.value.monitorSysGenServerDataReportMethod = 'NONE'
    }
  }
})

const dockerEnabled = computed({
  get: () => formData.value.monitorSysGenServerDockerEnabled === 1,
  set: (value) => {
    formData.value.monitorSysGenServerDockerEnabled = value ? 1 : 0
    if (!value) {
      formData.value.monitorSysGenServerDockerConnectionType = 'SHELL'
      formData.value.monitorSysGenServerDockerHost = ''
      formData.value.monitorSysGenServerDockerPort = undefined
    }
  }
})

// 表单验证规则
const formRules = reactive({
  monitorSysGenServerName: [
    { required: true, message: '请输入服务器名称', trigger: 'blur' },
    { min: 2, max: 50, message: '服务器名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  monitorSysGenServerHost: [
    { required: true, message: '请输入服务器主机地址', trigger: 'blur' },
    { 
      pattern: /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?)*$/,
      message: '请输入有效的IP地址或域名',
      trigger: 'blur'
    }
  ],
  monitorSysGenServerPort: [
    { required: true, message: '请输入服务器端口', trigger: 'blur' },
    { type: 'number', min: 1, max: 65535, message: '端口号范围为 1-65535', trigger: 'blur' }
  ],
  monitorSysGenServerProtocol: [
    { required: true, message: '请选择连接协议', trigger: 'change' }
  ],
  monitorSysGenServerUsername: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
})

// 方法
const handleProxyChange = (proxy: ProxyOption | null) => {
  if (proxy) {
    // 清空手动配置的代理地址
    formData.value.monitorSysGenServerProxyHost = ''
    formData.value.monitorSysGenServerProxyPort = undefined
  }
}

const handleCreateProxy = () => {
  emit('create-proxy')
}

// 表单验证方法
const validate = async (): Promise<boolean> => {
  try {
    await formRef.value?.validate()
    return true
  } catch (error) {
    return false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
}

// 暴露方法给父组件
defineExpose({
  validate,
  resetForm
})
</script>

<style scoped lang="scss">
.server-form {
  .form-card {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;

      .help-icon {
        margin-left: auto;
        color: var(--el-color-info);
        cursor: help;
      }
    }
  }

  // Switch容器样式
  .switch-container {
    display: flex;
    align-items: center;
    gap: 8px;
    height: 32px; // 与输入框高度保持一致

    .switch-help-icon {
      color: var(--el-color-info);
      cursor: help;
      font-size: 14px;
      transition: color 0.3s ease;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  // Select容器样式
  .select-container {
    display: flex;
    align-items: center;
    gap: 8px;

    .select-help-icon {
      color: var(--el-color-info);
      cursor: help;
      font-size: 14px;
      transition: color 0.3s ease;
      flex-shrink: 0;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;

    // 确保表单项标签和内容垂直居中对齐
    .el-form-item__label {
      line-height: 32px;
      height: 32px;
      display: flex;
      align-items: center;
    }

    .el-form-item__content {
      line-height: 32px;
      min-height: 32px;
      display: flex;
      align-items: center;
    }
  }

  :deep(.el-input-number) {
    width: 100%;
  }

  // Switch组件样式优化
  :deep(.el-switch) {
    height: 20px;
    line-height: 20px;

    .el-switch__core {
      height: 20px;
      line-height: 18px;
      border-radius: 10px;
      transition: all 0.3s ease;
    }

    .el-switch__action {
      width: 16px;
      height: 16px;
      top: 2px;
      left: 2px;
      border-radius: 50%;
      transition: all 0.3s ease;
    }

    &.is-checked .el-switch__action {
      left: calc(100% - 18px);
    }
  }
}
</style>
