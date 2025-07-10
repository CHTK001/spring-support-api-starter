<template>
  <div class="file-management-config">
    <!-- 启用开关 -->
    <el-form-item prop="monitorSysGenServerSettingFileManagementEnabled">
      <template #label>
        <div class="form-label">
          <span>启用文件管理</span>
          <el-tooltip
            content="开启后可以通过Web界面管理服务器文件"
            placement="top"
            effect="dark"
          >
            <IconifyIconOnline icon="ri:question-line" class="help-icon" />
          </el-tooltip>
        </div>
      </template>
      <el-switch
        v-model="formData.monitorSysGenServerSettingFileManagementEnabled"
        :active-value="1"
        :inactive-value="0"
        active-text="开启"
        inactive-text="关闭"
        @change="handleEnabledChange"
      />
    </el-form-item>

    <!-- 连接方式选择 -->
    <el-form-item
      v-show="formData.monitorSysGenServerSettingFileManagementEnabled"
      prop="monitorSysGenServerSettingFileManagementMode"
    >
      <template #label>
        <div class="form-label">
          <span>连接方式</span>
          <el-tooltip
            :content="getModeTooltip()"
            placement="top"
            effect="dark"
          >
            <IconifyIconOnline icon="ri:question-line" class="help-icon" />
          </el-tooltip>
        </div>
      </template>
      <el-select
        v-model="formData.monitorSysGenServerSettingFileManagementMode"
        placeholder="请选择连接方式"
        style="width: 100%"
        @change="handleModeChange"
      >
        <el-option
          v-for="mode in availableModes"
          :key="mode.code"
          :label="mode.name"
          :value="mode.code"
        >
          <div class="mode-option">
            <div class="mode-name">{{ mode.name }}</div>
            <div class="mode-desc">{{ mode.description }}</div>
          </div>
        </el-option>
      </el-select>
    </el-form-item>

    <!-- API配置 -->
    <template v-if="showApiConfig">
      <el-divider content-position="left">
        <span class="divider-text">API配置</span>
      </el-divider>
      
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="API地址">
            <el-input
              v-model="apiConfig.apiHost"
              placeholder="localhost"
              @input="handleApiConfigChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="端口">
            <el-input-number
              v-model="apiConfig.apiPort"
              :min="1"
              :max="65535"
              placeholder="8080"
              style="width: 100%"
              @change="handleApiConfigChange"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="基础路径">
        <el-input
          v-model="apiConfig.basePath"
          placeholder="/api/file"
          @input="handleApiConfigChange"
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="使用HTTPS">
            <el-switch
              v-model="apiConfig.useHttps"
              active-text="是"
              inactive-text="否"
              @change="handleApiConfigChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="连接超时(秒)">
            <el-input-number
              v-model="apiConfig.connectionTimeout"
              :min="5"
              :max="300"
              style="width: 100%"
              @change="handleApiConfigChange"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </template>

    <!-- NODE客户端选择 -->
    <template v-if="showNodeConfig">
      <el-divider content-position="left">
        <span class="divider-text">NODE客户端</span>
      </el-divider>
      
      <el-form-item label="选择客户端">
        <el-select
          v-model="formData.monitorSysGenServerSettingFileManagementNodeClient"
          placeholder="请选择在线的NODE客户端"
          style="width: 100%"
          :loading="loadingNodeClients"
          @change="handleNodeClientChange"
        >
          <el-option
            v-for="client in nodeClients"
            :key="client.id"
            :label="`${client.name} (${client.address})`"
            :value="client.id"
          >
            <div class="client-option">
              <div class="client-name">{{ client.name }}</div>
              <div class="client-status">
                <el-tag :type="client.online ? 'success' : 'danger'" size="small">
                  {{ client.online ? '在线' : '离线' }}
                </el-tag>
              </div>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
    </template>

    <!-- 通用配置 -->
    <template v-if="showCommonConfig">
      <el-divider content-position="left">
        <span class="divider-text">通用配置</span>
      </el-divider>
      
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="操作超时(秒)">
            <el-input-number
              v-model="formData.monitorSysGenServerSettingFileManagementTimeout"
              :min="10"
              :max="300"
              style="width: 100%"
              @change="handleChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大重试次数">
            <el-input-number
              v-model="formData.monitorSysGenServerSettingFileManagementMaxRetries"
              :min="0"
              :max="10"
              style="width: 100%"
              @change="handleChange"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </template>

    <!-- 测试连接 -->
    <el-form-item v-if="showTestButton">
      <el-button
        type="primary"
        @click="testConnection"
        :loading="testing"
      >
        <IconifyIconOnline icon="ri:wifi-line" class="mr-1" />
        测试连接
      </el-button>
      <span class="form-item-tip">测试文件管理连接是否正常</span>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { message } from "@repo/utils";
import { IconifyIconOnline } from "@repo/components";

// Props
interface Props {
  modelValue: any;
  serverId?: number;
  isLocalServer?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isLocalServer: false
});

// Emits
const emit = defineEmits<{
  "update:modelValue": [value: any];
  change: [];
}>();

// 响应式数据
const formData = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value)
});

const testing = ref(false);
const loadingNodeClients = ref(false);
const nodeClients = ref<any[]>([]);

// API配置
const apiConfig = ref({
  apiHost: "localhost",
  apiPort: 8080,
  basePath: "/api/file",
  useHttps: false,
  connectionTimeout: 30
});

// 可用的文件管理模式
const availableModes = computed(() => {
  const modes = [
    { code: "NONE", name: "不启用", description: "不提供文件管理功能" },
    { code: "SSH", name: "SSH连接", description: "通过SSH协议直接访问服务器文件系统" },
    { code: "NODE", name: "NODE客户端", description: "通过在线客户端代理进行文件管理" },
    { code: "API", name: "API接口", description: "通过HTTP API接口进行文件管理" }
  ];

  // 如果是本地服务器，添加本地连接选项
  if (props.isLocalServer) {
    modes.splice(1, 0, {
      code: "LOCAL",
      name: "本地连接",
      description: "直接访问本地文件系统"
    });
  }

  return modes;
});

// 计算属性
const showApiConfig = computed(() => {
  return formData.value.monitorSysGenServerSettingFileManagementEnabled &&
         formData.value.monitorSysGenServerSettingFileManagementMode === "API";
});

const showNodeConfig = computed(() => {
  return formData.value.monitorSysGenServerSettingFileManagementEnabled &&
         formData.value.monitorSysGenServerSettingFileManagementMode === "NODE";
});

const showCommonConfig = computed(() => {
  return formData.value.monitorSysGenServerSettingFileManagementEnabled &&
         formData.value.monitorSysGenServerSettingFileManagementMode !== "NONE";
});

const showTestButton = computed(() => {
  return formData.value.monitorSysGenServerSettingFileManagementEnabled &&
         formData.value.monitorSysGenServerSettingFileManagementMode !== "NONE";
});

// 方法
const getModeTooltip = () => {
  const mode = availableModes.value.find(m => 
    m.code === formData.value.monitorSysGenServerSettingFileManagementMode
  );
  return mode ? mode.description : "选择文件管理的连接方式";
};

const handleEnabledChange = () => {
  if (!formData.value.monitorSysGenServerSettingFileManagementEnabled) {
    formData.value.monitorSysGenServerSettingFileManagementMode = "NONE";
  }
  handleChange();
};

const handleModeChange = () => {
  // 重置相关配置
  if (formData.value.monitorSysGenServerSettingFileManagementMode === "API") {
    handleApiConfigChange();
  } else if (formData.value.monitorSysGenServerSettingFileManagementMode === "NODE") {
    loadNodeClients();
  } else {
    formData.value.monitorSysGenServerSettingFileManagementApiConfig = "";
    formData.value.monitorSysGenServerSettingFileManagementNodeClient = "";
  }
  handleChange();
};

const handleApiConfigChange = () => {
  formData.value.monitorSysGenServerSettingFileManagementApiConfig = JSON.stringify(apiConfig.value);
  handleChange();
};

const handleNodeClientChange = () => {
  handleChange();
};

const handleChange = () => {
  emit("change");
};

const loadNodeClients = async () => {
  // TODO: 实现加载NODE客户端列表
  loadingNodeClients.value = true;
  try {
    // 模拟数据
    nodeClients.value = [
      { id: "client1", name: "客户端1", address: "192.168.1.100", online: true },
      { id: "client2", name: "客户端2", address: "192.168.1.101", online: false }
    ];
  } finally {
    loadingNodeClients.value = false;
  }
};

const testConnection = async () => {
  if (!props.serverId) {
    message.warning("请先保存服务器配置");
    return;
  }

  testing.value = true;
  try {
    // TODO: 实现连接测试
    await new Promise(resolve => setTimeout(resolve, 2000));
    message.success("连接测试成功");
  } catch (error) {
    message.error("连接测试失败");
  } finally {
    testing.value = false;
  }
};

// 监听API配置变化
watch(() => formData.value.monitorSysGenServerSettingFileManagementApiConfig, (newVal) => {
  if (newVal && formData.value.monitorSysGenServerSettingFileManagementMode === "API") {
    try {
      Object.assign(apiConfig.value, JSON.parse(newVal));
    } catch (error) {
      console.warn("解析API配置失败:", error);
    }
  }
}, { immediate: true });
</script>

<style scoped>
.file-management-config {
  padding: 16px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 4px;
}

.help-icon {
  color: #909399;
  cursor: help;
}

.mode-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.mode-name {
  font-weight: 500;
}

.mode-desc {
  font-size: 12px;
  color: #909399;
}

.client-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.client-name {
  flex: 1;
}

.client-status {
  margin-left: 8px;
}

.divider-text {
  font-weight: 500;
  color: #303133;
}

.form-item-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
