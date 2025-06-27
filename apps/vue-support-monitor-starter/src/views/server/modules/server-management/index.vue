<template>
  <div class="server-container">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">
          <IconifyIconOnline icon="ri:server-line" class="mr-2" />
          服务器管理
          <el-tooltip :content="`当前共有 ${totalCount} 台服务器`" placement="bottom" :show-after="500">
            <el-tag type="info" effect="plain" class="server-count">
              共 <span class="count-num">{{ totalCount }}</span> 台
            </el-tag>
          </el-tooltip>
          <el-tooltip content="WebSocket连接状态: 已连接" placement="bottom" :show-after="500">
            <el-tag
              type="success"
              effect="light"
              size="small"
              class="ml-2"
            >
              已连接
            </el-tag>
          </el-tooltip>
        </h2>
      </div>
    </div>

    <!-- 主体内容区域 -->
    <div class="main-content">
      <!-- 左侧服务器列表 -->
      <div class="left-panel" :class="{ 'minimized': leftPanelMinimized }" :style="{ width: leftPanelMinimized ? '60px' : leftPanelWidth + 'px' }">
        <!-- 面板头部控制栏 -->
        <div class="panel-header">
          <div class="panel-title" v-if="!leftPanelMinimized">
            <IconifyIconOnline icon="ri:server-line" class="mr-2" />
            <span>服务器列表</span>
            <el-badge :value="filteredServers.length" class="ml-2" type="primary" />
          </div>
          <div class="panel-controls">
            <el-tooltip :content="leftPanelMinimized ? '展开面板' : '最小化面板'" placement="right" :show-after="300">
              <el-button
                size="small"
                text
                @click="toggleLeftPanel"
                class="minimize-btn"
              >
                <IconifyIconOnline :icon="leftPanelMinimized ? 'ri:arrow-right-s-line' : 'ri:arrow-left-s-line'" />
              </el-button>
            </el-tooltip>
          </div>
        </div>

        <!-- 服务器卡片列表 -->
        <div class="server-list" v-loading="loading">
          <!-- 最小化状态下的简化服务器列表 -->
          <template v-if="leftPanelMinimized">
            <el-tooltip
              v-for="server in filteredServers"
              :key="server.id + '-mini'"
              :content="`${server.name} (${server.host}:${server.port}) - ${server.onlineStatus === ONLINE_STATUS.ONLINE ? '在线' : '离线'}`"
              placement="right"
              :show-after="300"
            >
              <div
                class="server-mini-card"
                :class="{
                  'selected': selectedServerId === server.id,
                  'online': server.onlineStatus === ONLINE_STATUS.ONLINE,
                  'offline': server.onlineStatus === ONLINE_STATUS.OFFLINE,
                  'error': server.status === SERVER_STATUS.ERROR
                }"
                @click="selectServer(server)"
              >
                <IconifyIconOnline
                  icon="ri:server-line"
                  class="server-mini-icon"
                />
              </div>
            </el-tooltip>
          </template>

          <!-- 正常状态下的完整服务器列表 -->
          <template v-else>
            <el-tooltip
              v-for="server in filteredServers"
              :key="server.id"
              :content="`${server.name} (${server.host}:${server.port}) - ${getOnlineStatusText(server.onlineStatus)}`"
              placement="right"
              :show-after="800"
              :disabled="selectedServerId === server.id"
            >
              <div
                class="server-card"
                :class="{
                  'selected': selectedServerId === server.id,
                  'online': server.onlineStatus === ONLINE_STATUS.ONLINE,
                  'offline': server.onlineStatus === ONLINE_STATUS.OFFLINE,
                  'error': server.status === SERVER_STATUS.ERROR
                }"
                @click="selectServer(server)"
              >
                <!-- 服务器卡片头部 -->
                <div class="card-header">
                  <div class="server-info">
                    <el-tooltip :content="`服务器名称: ${server.name}`" placement="top" :show-after="300">
                      <div class="server-name">{{ server.name }}</div>
                    </el-tooltip>
                    <el-tooltip :content="`服务器地址: ${server.host}:${server.port}`" placement="top" :show-after="300">
                      <div class="server-address">{{ server.host }}:{{ server.port }}</div>
                    </el-tooltip>
                  </div>
                  <div class="server-status">
                    <el-tooltip :content="`服务器状态: ${getOnlineStatusText(server.onlineStatus)}`" placement="top" :show-after="300">
                      <el-tag
                        :type="getOnlineStatusType(server.onlineStatus)"
                        size="small"
                        effect="light"
                      >
                        {{ getOnlineStatusText(server.onlineStatus) }}
                      </el-tag>
                    </el-tooltip>
                    <el-tooltip :content="`连接协议: ${server.protocol}`" placement="top" :show-after="300">
                      <IconifyIconOnline
                        :icon="getProtocolIcon(server.protocol)"
                        class="protocol-icon"
                      />
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </el-tooltip>
          </template>
        </div>
      </div>

      <!-- 右侧内容区域 -->
      <div class="right-panel">
        <el-tabs v-model="activeTab" type="border-card" class="full-height-tabs">
          <el-tab-pane label="服务器监控" name="monitor">
            <div v-if="!selectedServerId" class="welcome-panel">
              <el-empty description="请选择一个服务器">
                <el-tooltip content="点击新增第一台服务器" placement="top" :show-after="500">
                  <el-button type="primary" @click="showAddDialog">新增服务器</el-button>
                </el-tooltip>
              </el-empty>
            </div>
            <div v-else>
              <!-- 这里可以添加服务器监控组件 -->
              <div class="server-monitor-placeholder">
                <h3>服务器监控 - {{ selectedServerId }}</h3>
                <p>服务器监控功能正在开发中...</p>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="WebSocket调试" name="websocket">
            <WebSocketDebug />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { message } from "@repo/utils";
import { ElMessageBox } from "element-plus";
import {
  getServerPageList,
  deleteServer,
  testServerConnection,
  connectServer as connectServerApi,
  disconnectServer as disconnectServerApi,
  SERVER_STATUS,
  ONLINE_STATUS,
  type ServerDisplayData,
  type ServerInfo,
  type ServerMetricsDisplay,
  mapServerListToDisplayData,
} from "@/api/monitor/gen/server";
import WebSocketDebug from "./components/WebSocketDebug.vue";

// 响应式状态
const loading = ref(false);
const totalCount = ref(0);
const viewMode = ref("card");

// 筛选和搜索
const searchKeyword = ref("");
const filterGroup = ref("");
const filterProtocol = ref("");
const filterStatus = ref("");
const activeGroup = ref("all");

// 左右面板
const leftPanelWidth = ref(400);
const leftPanelMinimized = ref(false);
const leftPanelOriginalWidth = ref(400);
const selectedServerId = ref("");
const currentComponent = ref("");
const activeTab = ref("monitor");

// 服务器数据
const servers = ref<ServerDisplayData[]>([]);
const serverGroups = ref<string[]>([]);
const selectedServer = computed(() =>
  servers.value.find(s => s.id === selectedServerId.value)
);

// 服务器指标数据
const serverMetrics = ref<Map<string, ServerMetricsDisplay>>(new Map());

// 计算属性
const filteredServers = computed(() => {
  let result = servers.value;

  // 按分组筛选
  if (activeGroup.value !== "all") {
    result = result.filter(server => server.group === activeGroup.value);
  }

  // 按协议筛选
  if (filterProtocol.value) {
    result = result.filter(server => server.protocol === filterProtocol.value);
  }

  // 按状态筛选
  if (filterStatus.value) {
    result = result.filter(server => String(server.status) === filterStatus.value);
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(server =>
      server.name.toLowerCase().includes(keyword) ||
      server.host.toLowerCase().includes(keyword) ||
      (server.description && server.description.toLowerCase().includes(keyword))
    );
  }

  return result;
});

/**
 * 加载服务器列表
 */
const loadServers = async () => {
  try {
    loading.value = true;
    const res = await getServerPageList({
      page: 1,
      pageSize: 1000, // 加载所有服务器
    }) as any;

    if (res.code == "00000") {
      // 使用字段映射转换后台数据为前端显示数据
      const serverList = res.data?.data || [];
      servers.value = mapServerListToDisplayData(serverList);
      totalCount.value = res.data?.total || 0;

      // 提取分组信息
      const groups = new Set<string>();
      servers.value.forEach(server => {
        if (server.group) {
          groups.add(server.group);
        }
      });
      serverGroups.value = Array.from(groups);
    }
  } catch (error) {
    console.error("加载服务器列表失败:", error);
  } finally {
    loading.value = false;
  }
};

/**
 * 选择服务器
 */
const selectServer = (server: any) => {
  selectedServerId.value = server.id;
  currentComponent.value = "ServerMonitor";
};

/**
 * 切换左侧面板
 */
const toggleLeftPanel = () => {
  if (leftPanelMinimized.value) {
    leftPanelMinimized.value = false;
    leftPanelWidth.value = leftPanelOriginalWidth.value;
  } else {
    leftPanelOriginalWidth.value = leftPanelWidth.value;
    leftPanelMinimized.value = true;
  }
};

/**
 * 显示添加对话框
 */
const showAddDialog = () => {
  // TODO: 实现添加服务器对话框
  console.log("显示添加服务器对话框");
};

/**
 * 获取在线状态类型
 */
const getOnlineStatusType = (status: number) => {
  // 添加类型检查和错误处理
  if (typeof status !== 'number') {
    console.warn('getOnlineStatusType: status is not a number:', status);
    return "info";
  }
  
  const statusMap: Record<number, string> = {
    0: "danger",  // 离线
    1: "success", // 在线
    2: "warning", // 未知
  };
  return statusMap[status] || "info";
};

/**
 * 获取在线状态文本
 */
const getOnlineStatusText = (status: number) => {
  // 添加类型检查和错误处理
  if (typeof status !== 'number') {
    console.warn('getOnlineStatusText: status is not a number:', status);
    return "未知状态";
  }
  
  const statusMap: Record<number, string> = {
    0: "离线",
    1: "在线",
    2: "未知",
  };
  return statusMap[status] || "未知状态";
};

/**
 * 获取协议图标
 */
const getProtocolIcon = (protocol: string) => {
  // 添加类型检查和错误处理
  if (typeof protocol !== 'string') {
    console.warn('getProtocolIcon: protocol is not a string:', protocol);
    return "ri:server-line";
  }
  
  const iconMap: Record<string, string> = {
    SSH: "ri:terminal-line",
    RDP: "ri:computer-line",
    VNC: "ri:remote-control-line",
  };
  return iconMap[protocol] || "ri:server-line";
};

// 生命周期钩子
onMounted(() => {
  loadServers();
});
</script>

<style lang="scss" scoped>
.server-container {
  height: 100vh;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 工具栏样式 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, var(--el-bg-color) 0%, var(--el-fill-color-extra-light) 100%);
  border-bottom: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
}

.page-title {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
}

.server-count {
  margin-left: 12px;
}

.count-num {
  font-weight: 700;
  color: var(--el-color-primary);
}

/* 主体内容样式 */
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.left-panel {
  background: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: relative;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-extra-light);
}

.panel-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.server-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.server-card {
  margin-bottom: 8px;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--el-bg-color);
}

.server-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.server-card.selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.server-info {
  flex: 1;
}

.server-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.server-address {
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.server-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.protocol-icon {
  font-size: 16px;
  color: var(--el-text-color-regular);
}

.server-mini-card {
  width: 44px;
  height: 44px;
  margin-bottom: 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--el-bg-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.server-mini-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.server-mini-card.selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.server-mini-icon {
  font-size: 20px;
  color: var(--el-text-color-regular);
}

.right-panel {
  flex: 1;
  background: var(--el-bg-color);
  display: flex;
  flex-direction: column;
}

.full-height-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.full-height-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: auto;
}

.server-monitor-placeholder {
  padding: 40px;
  text-align: center;
  color: var(--el-text-color-regular);
}

.welcome-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
