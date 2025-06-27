<template>
  <div class="server-config-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="handleBack" type="primary" plain>
          <IconifyIconOnline icon="ri:arrow-left-line" class="mr-1" />
          返回服务器列表
        </el-button>
      </div>
      <div class="header-center">
        <h2 class="page-title">
          <IconifyIconOnline icon="ri:settings-3-line" class="mr-2" />
          服务器配置管理
        </h2>
        <p class="page-subtitle" v-if="currentServer">
          {{ currentServer.monitorSysGenServerName }} ({{ currentServer.monitorSysGenServerHost }})
        </p>
      </div>
      <div class="header-right">
        <el-button @click="handleRefresh" plain>
          <IconifyIconOnline icon="ri:refresh-line" class="mr-1" />
          刷新
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="page-content" v-loading="loading">
      <div class="config-container">
        <!-- 左侧导航 -->
        <div class="config-nav">
          <el-menu
            v-model="activeSection"
            mode="vertical"
            class="config-menu"
            @select="handleSectionChange"
          >
            <el-menu-item index="basic">
              <IconifyIconOnline icon="ri:settings-line" />
              <span>基础配置</span>
            </el-menu-item>
            <el-menu-item index="monitor">
              <IconifyIconOnline icon="ri:eye-line" />
              <span>监控配置</span>
            </el-menu-item>
            <el-menu-item index="alert">
              <IconifyIconOnline icon="ri:alarm-warning-line" />
              <span>告警配置</span>
            </el-menu-item>
            <el-menu-item index="docker">
              <IconifyIconOnline icon="simple-icons:docker" />
              <span>Docker配置</span>
            </el-menu-item>
            <el-menu-item index="proxy">
              <IconifyIconOnline icon="ri:global-line" />
              <span>代理配置</span>
            </el-menu-item>
            <el-menu-item index="advanced">
              <IconifyIconOnline icon="ri:tools-line" />
              <span>高级配置</span>
            </el-menu-item>
          </el-menu>
        </div>

        <!-- 右侧配置内容 -->
        <div class="config-content">
          <el-card class="config-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">{{ getSectionTitle() }}</span>
                <div class="card-actions">
                  <el-button @click="handleReset" plain size="small">
                    <IconifyIconOnline icon="ri:refresh-line" class="mr-1" />
                    重置
                  </el-button>
                  <el-button @click="handleSave" type="primary" size="small" :loading="saving">
                    <IconifyIconOnline icon="ri:save-line" class="mr-1" />
                    保存配置
                  </el-button>
                </div>
              </div>
            </template>

            <!-- 配置表单 -->
            <el-form
              ref="formRef"
              :model="formData"
              :rules="rules"
              label-width="140px"
              size="default"
              class="config-form"
            >
              <!-- 基础配置 -->
              <div v-show="activeSection === 'basic'" class="config-section">
                <el-form-item label="服务器名称" prop="monitorSysGenServerName">
                  <el-input
                    v-model="formData.monitorSysGenServerName"
                    placeholder="请输入服务器名称"
                    maxlength="100"
                    show-word-limit
                  />
                </el-form-item>
                
                <el-form-item label="服务器描述" prop="monitorSysGenServerDesc">
                  <el-input
                    v-model="formData.monitorSysGenServerDesc"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入服务器描述"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
                
                <el-form-item label="服务器标签" prop="monitorSysGenServerTags">
                  <el-input
                    v-model="formData.monitorSysGenServerTags"
                    placeholder="请输入服务器标签，多个标签用逗号分隔"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
                
                <el-form-item label="连接超时时间" prop="monitorSysGenServerTimeout">
                  <el-input-number
                    v-model="formData.monitorSysGenServerTimeout"
                    :min="1000"
                    :max="300000"
                    :step="1000"
                    placeholder="连接超时时间(毫秒)"
                    style="width: 200px"
                  />
                  <span class="form-tip">毫秒，建议值：30000</span>
                </el-form-item>
                
                <el-form-item label="服务器状态" prop="monitorSysGenServerStatus">
                  <el-switch
                    v-model="formData.monitorSysGenServerStatus"
                    :active-value="1"
                    :inactive-value="0"
                    active-text="启用"
                    inactive-text="停用"
                  />
                </el-form-item>
              </div>

              <!-- 监控配置 -->
              <div v-show="activeSection === 'monitor'" class="config-section">
                <ServerSettingForm
                  v-model="settingData"
                  section="monitor"
                  @change="handleSettingChange"
                />
              </div>

              <!-- 告警配置 -->
              <div v-show="activeSection === 'alert'" class="config-section">
                <ServerSettingForm
                  v-model="settingData"
                  section="alert"
                  @change="handleSettingChange"
                />
              </div>

              <!-- Docker配置 -->
              <div v-show="activeSection === 'docker'" class="config-section">
                <ServerSettingForm
                  v-model="settingData"
                  section="docker"
                  @change="handleSettingChange"
                />
              </div>

              <!-- 代理配置 -->
              <div v-show="activeSection === 'proxy'" class="config-section">
                <ServerSettingForm
                  v-model="settingData"
                  section="proxy"
                  @change="handleSettingChange"
                />
              </div>

              <!-- 高级配置 -->
              <div v-show="activeSection === 'advanced'" class="config-section">
                <ServerSettingForm
                  v-model="settingData"
                  section="advanced"
                  @change="handleSettingChange"
                />
              </div>
            </el-form>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, defineAsyncComponent } from "vue";
import { useRoute, useRouter } from "vue-router";
import { message } from "@repo/utils";
import { getServerInfo, updateServer, type ServerInfo } from "@/api/monitor/server";
import { getServerSettingByServerId, saveOrUpdateServerSetting, type ServerSetting } from "@/api/monitor/server/setting";

// 异步组件
const ServerSettingForm = defineAsyncComponent(() => import("./components/ServerSettingForm.vue"));

// 路由相关
const route = useRoute();
const router = useRouter();

// 响应式状态
const loading = ref(false);
const saving = ref(false);
const activeSection = ref("basic");
const formRef = ref();

// 服务器ID
const serverId = computed(() => {
  const id = route.params.id || route.query.serverId;
  return id ? Number(id) : null;
});

// 当前服务器信息
const currentServer = ref<ServerInfo | null>(null);

// 基础配置表单数据
const formData = reactive({
  monitorSysGenServerId: null as number | null,
  monitorSysGenServerName: "",
  monitorSysGenServerDesc: "",
  monitorSysGenServerTags: "",
  monitorSysGenServerTimeout: 30000,
  monitorSysGenServerStatus: 1,
});

// 服务器设置数据
const settingData = ref<Partial<ServerSetting>>({});

// 表单验证规则
const rules = {
  monitorSysGenServerName: [
    { required: true, message: "请输入服务器名称", trigger: "blur" },
    { min: 1, max: 100, message: "服务器名称长度在 1 到 100 个字符", trigger: "blur" }
  ],
  monitorSysGenServerTimeout: [
    { required: true, message: "请输入连接超时时间", trigger: "blur" },
    { type: "number", min: 1000, max: 300000, message: "超时时间范围：1000-300000毫秒", trigger: "blur" }
  ]
};

/**
 * 获取当前节的标题
 */
const getSectionTitle = () => {
  const titles = {
    basic: "基础配置",
    monitor: "监控配置", 
    alert: "告警配置",
    docker: "Docker配置",
    proxy: "代理配置",
    advanced: "高级配置"
  };
  return titles[activeSection.value] || "配置";
};

/**
 * 处理节切换
 */
const handleSectionChange = (section: string) => {
  activeSection.value = section;
};

/**
 * 处理设置数据变化
 */
const handleSettingChange = (data: Partial<ServerSetting>) => {
  settingData.value = { ...settingData.value, ...data };
};

/**
 * 返回服务器列表
 */
const handleBack = () => {
  router.push("/server/index");
};

/**
 * 刷新数据
 */
const handleRefresh = () => {
  loadServerData();
};

/**
 * 重置表单
 */
const handleReset = () => {
  loadServerData();
};

/**
 * 保存配置
 */
const handleSave = async () => {
  try {
    // 表单验证
    const isValid = await formRef.value?.validate().catch(() => false);
    if (!isValid) return;

    saving.value = true;

    // 保存基础配置
    if (activeSection.value === "basic") {
      const result = await updateServer(formData);
      if (result.code === "00000") {
        message.success("基础配置保存成功");
        // 重新加载服务器信息
        await loadServerInfo();
      } else {
        message.error(result.msg || "基础配置保存失败");
        return;
      }
    } else {
      // 保存服务器设置
      const submitData = {
        ...settingData.value,
        monitorSysGenServerId: serverId.value,
      };

      const result = await saveOrUpdateServerSetting(submitData);
      if (result.code === "00000") {
        message.success("配置保存成功");
        // 重新加载设置数据
        await loadServerSetting();
      } else {
        message.error(result.msg || "配置保存失败");
        return;
      }
    }
  } catch (error) {
    console.error("保存配置失败:", error);
    message.error("保存失败");
  } finally {
    saving.value = false;
  }
};

/**
 * 加载服务器信息
 */
const loadServerInfo = async () => {
  if (!serverId.value) return;

  try {
    const result = await getServerInfo(serverId.value);
    if (result.code === "00000" && result.data) {
      currentServer.value = result.data;
      
      // 填充基础配置表单
      Object.assign(formData, {
        monitorSysGenServerId: result.data.monitorSysGenServerId,
        monitorSysGenServerName: result.data.monitorSysGenServerName || "",
        monitorSysGenServerDesc: result.data.monitorSysGenServerDesc || "",
        monitorSysGenServerTags: result.data.monitorSysGenServerTags || "",
        monitorSysGenServerTimeout: result.data.monitorSysGenServerTimeout || 30000,
        monitorSysGenServerStatus: result.data.monitorSysGenServerStatus ?? 1,
      });
    }
  } catch (error) {
    console.error("加载服务器信息失败:", error);
    message.error("加载服务器信息失败");
  }
};

/**
 * 加载服务器设置
 */
const loadServerSetting = async () => {
  if (!serverId.value) return;

  try {
    const result = await getServerSettingByServerId(serverId.value);
    if (result.code === "00000" && result.data) {
      settingData.value = result.data;
    }
  } catch (error) {
    console.error("加载服务器设置失败:", error);
    // 设置默认值
    settingData.value = {};
  }
};

/**
 * 加载所有数据
 */
const loadServerData = async () => {
  if (!serverId.value) {
    message.error("服务器ID不能为空");
    handleBack();
    return;
  }

  loading.value = true;
  try {
    await Promise.all([
      loadServerInfo(),
      loadServerSetting()
    ]);
  } finally {
    loading.value = false;
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadServerData();
});
</script>

<style scoped>
.server-config-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-center {
  flex: 1;
  text-align: center;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-subtitle {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.page-content {
  flex: 1;
  padding: 24px;
  overflow: hidden;
}

.config-container {
  display: flex;
  height: 100%;
  gap: 24px;
}

.config-nav {
  width: 200px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.config-menu {
  border: none;
  border-radius: 8px;
}

.config-content {
  flex: 1;
  overflow: auto;
}

.config-card {
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.config-form {
  padding: 20px;
}

.config-section {
  max-width: 600px;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

:deep(.el-menu-item) {
  border-radius: 6px;
  margin: 4px 8px;
}

:deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
}

:deep(.el-card__body) {
  padding: 0;
  height: calc(100% - 60px);
  overflow: auto;
}
</style>
