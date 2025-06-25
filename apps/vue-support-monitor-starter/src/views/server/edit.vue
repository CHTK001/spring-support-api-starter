<template>
  <div class="server-edit-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button 
          type="text" 
          @click="handleBack"
          class="back-button"
        >
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="page-title">
          <h2>{{ isEdit ? '编辑服务器' : '新增服务器' }}</h2>
          <p v-if="isEdit && serverData.monitorSysGenServerName">
            {{ serverData.monitorSysGenServerName }}
          </p>
        </div>
      </div>
      
      <div class="header-actions">
        <el-button 
          v-if="isEdit"
          @click="handleTestConnection"
          :loading="testing"
        >
          <el-icon><Connection /></el-icon>
          测试连接
        </el-button>
        <el-button 
          @click="handleReset"
          :disabled="submitting"
        >
          重置
        </el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit"
          :loading="submitting"
        >
          <el-icon><Check /></el-icon>
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </div>
    
    <!-- 表单内容 -->
    <div class="page-content">
      <ServerForm
        ref="serverFormRef"
        v-model="serverData"
        :is-edit="isEdit"
        @create-proxy="handleCreateProxy"
      />
    </div>
    
    <!-- 代理创建/编辑对话框 -->
    <ProxyDialog
      v-model="proxyDialogVisible"
      :proxy-data="editingProxy"
      @success="handleProxySuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Connection, Check } from '@element-plus/icons-vue'
import ServerForm from './components/ServerForm.vue'
import ProxyDialog from '@/components/monitor/ProxyDialog.vue'
import { serverApi, type ServerInfo } from '@/api/monitor/server'
import type { ProxyConfig } from '@/api/monitor/proxy'

// 路由相关
const route = useRoute()
const router = useRouter()

// 响应式数据
const serverFormRef = ref()
const submitting = ref(false)
const testing = ref(false)
const proxyDialogVisible = ref(false)
const editingProxy = ref<ProxyConfig | null>(null)

// 服务器数据
const serverData = ref<Partial<ServerInfo>>({
  monitorSysGenServerName: '',
  monitorSysGenServerHost: '',
  monitorSysGenServerPort: 22,
  monitorSysGenServerProtocol: 'SSH',
  monitorSysGenServerUsername: '',
  monitorSysGenServerPassword: '',
  monitorSysGenServerDesc: '',
  monitorSysGenServerStatus: 1,
  monitorSysGenServerMonitorEnabled: 1,
  monitorSysGenServerMonitorInterval: 60,
  monitorSysGenServerTimeout: 30000,
  monitorSysGenServerMetricsRetentionDays: 30,
  monitorSysGenServerReportEnabled: 1,
  monitorSysGenServerDataReportMethod: 'NONE',
  monitorSysGenServerDockerEnabled: 0,
  monitorSysGenServerDockerConnectionType: 'SHELL',
  monitorSysGenServerProxyId: null,
  monitorSysGenServerProxyHost: '',
  monitorSysGenServerProxyPort: undefined,
  monitorSysGenServerPrometheusHost: '',
  monitorSysGenServerPrometheusPort: undefined,
  monitorSysGenServerDockerHost: '',
  monitorSysGenServerDockerPort: undefined,
  monitorSysGenServerIpAddresses: '',
  monitorSysGenServerTags: ''
})

// 计算属性
const isEdit = computed(() => !!route.params.id && route.params.id !== 'new')
const serverId = computed(() => route.params.id as string)

// 方法
const loadServerData = async () => {
  if (!isEdit.value) return
  
  try {
    const data = await serverApi.getServerById(serverId.value)
    serverData.value = data
  } catch (error: any) {
    console.error('加载服务器数据失败:', error)
    ElMessage.error('加载服务器数据失败')
    handleBack()
  }
}

const handleBack = () => {
  router.back()
}

const handleReset = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置表单吗？所有未保存的修改将丢失。',
      '确认重置',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    if (isEdit.value) {
      await loadServerData()
    } else {
      // 重置为默认值
      Object.assign(serverData.value, {
        monitorSysGenServerName: '',
        monitorSysGenServerHost: '',
        monitorSysGenServerPort: 22,
        monitorSysGenServerProtocol: 'SSH',
        monitorSysGenServerUsername: '',
        monitorSysGenServerPassword: '',
        monitorSysGenServerDesc: '',
        monitorSysGenServerStatus: 1,
        monitorSysGenServerMonitorEnabled: 1,
        monitorSysGenServerMonitorInterval: 60,
        monitorSysGenServerTimeout: 30000,
        monitorSysGenServerMetricsRetentionDays: 30,
        monitorSysGenServerReportEnabled: 1,
        monitorSysGenServerDataReportMethod: 'NONE',
        monitorSysGenServerDockerEnabled: 0,
        monitorSysGenServerDockerConnectionType: 'SHELL',
        monitorSysGenServerProxyId: null,
        monitorSysGenServerProxyHost: '',
        monitorSysGenServerProxyPort: undefined,
        monitorSysGenServerPrometheusHost: '',
        monitorSysGenServerPrometheusPort: undefined,
        monitorSysGenServerDockerHost: '',
        monitorSysGenServerDockerPort: undefined,
        monitorSysGenServerIpAddresses: '',
        monitorSysGenServerTags: ''
      })
    }
    
    serverFormRef.value?.resetForm()
    ElMessage.success('表单已重置')
  } catch (error) {
    // 用户取消重置
  }
}

const handleTestConnection = async () => {
  if (!isEdit.value) {
    ElMessage.warning('请先保存服务器配置后再测试连接')
    return
  }
  
  try {
    testing.value = true
    const result = await serverApi.testConnection(serverId.value)
    
    if (result.success) {
      ElMessage.success('服务器连接测试成功')
    } else {
      ElMessage.error(`连接测试失败: ${result.error || '未知错误'}`)
    }
  } catch (error: any) {
    console.error('连接测试失败:', error)
    ElMessage.error('连接测试失败')
  } finally {
    testing.value = false
  }
}

const handleSubmit = async () => {
  try {
    // 验证表单
    const isValid = await serverFormRef.value?.validate()
    if (!isValid) {
      ElMessage.error('请检查表单填写是否正确')
      return
    }
    
    submitting.value = true
    
    if (isEdit.value) {
      await serverApi.updateServer(serverData.value)
      ElMessage.success('服务器更新成功')
    } else {
      const result = await serverApi.createServer(serverData.value)
      ElMessage.success('服务器创建成功')
      
      // 创建成功后跳转到编辑页面
      router.replace(`/server/edit/${result.monitorSysGenServerId}`)
      return
    }
    
  } catch (error: any) {
    console.error('保存服务器失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleCreateProxy = () => {
  editingProxy.value = null
  proxyDialogVisible.value = true
}

const handleProxySuccess = (proxy: ProxyConfig) => {
  // 代理创建/更新成功后，自动选中该代理
  serverData.value.monitorSysGenServerProxyId = proxy.proxyId
  ElMessage.success('代理配置成功，已自动选中')
}

// 页面加载时初始化数据
onMounted(() => {
  if (isEdit.value) {
    loadServerData()
  }
})
</script>

<style scoped lang="scss">
.server-edit-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: var(--el-bg-color);
    border-bottom: 1px solid var(--el-border-color-light);
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .back-button {
        display: flex;
        align-items: center;
        gap: 4px;
        color: var(--el-text-color-regular);
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
      
      .page-title {
        h2 {
          margin: 0;
          font-size: 18px;
          font-weight: 500;
          color: var(--el-text-color-primary);
        }
        
        p {
          margin: 4px 0 0 0;
          font-size: 14px;
          color: var(--el-text-color-secondary);
        }
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .page-content {
    flex: 1;
    padding: 24px;
    overflow-y: auto;
    background: var(--el-bg-color-page);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .server-edit-page {
    .page-header {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;
      
      .header-left,
      .header-actions {
        justify-content: center;
      }
    }
    
    .page-content {
      padding: 16px;
    }
  }
}
</style>
