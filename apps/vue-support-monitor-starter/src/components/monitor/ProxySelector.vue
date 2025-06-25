<template>
  <div class="proxy-selector">
    <el-form-item 
      :label="label" 
      :prop="prop"
      :rules="rules"
    >
      <el-select
        v-model="selectedProxyId"
        :placeholder="placeholder"
        :clearable="clearable"
        :disabled="disabled"
        :loading="loading"
        filterable
        remote
        :remote-method="handleRemoteSearch"
        @change="handleProxyChange"
        @clear="handleClear"
        style="width: 100%"
      >
        <el-option
          v-for="proxy in proxyOptions"
          :key="proxy.value"
          :label="proxy.label"
          :value="proxy.value"
          :disabled="!proxy.enabled"
        >
          <div class="proxy-option">
            <div class="proxy-option-main">
              <span class="proxy-name">{{ getProxyName(proxy.label) }}</span>
              <el-tag 
                :type="getStatusTagType(proxy.status)" 
                size="small"
                class="proxy-status"
              >
                {{ getProxyStatusLabel(proxy.status) }}
              </el-tag>
            </div>
            <div class="proxy-option-detail">
              <span class="proxy-protocol">{{ proxy.protocol }}</span>
              <span class="proxy-address">{{ proxy.address }}</span>
            </div>
          </div>
        </el-option>
        
        <!-- 空状态 -->
        <template #empty>
          <div class="proxy-empty">
            <el-empty 
              :image-size="60" 
              description="暂无可用代理"
            >
              <el-button 
                type="primary" 
                size="small"
                @click="handleCreateProxy"
              >
                创建代理
              </el-button>
            </el-empty>
          </div>
        </template>
      </el-select>
      
      <!-- 操作按钮 -->
      <div class="proxy-actions" v-if="showActions">
        <el-button-group>
          <el-button 
            size="small" 
            @click="handleRefresh"
            :loading="loading"
            title="刷新代理列表"
          >
            <el-icon><Refresh /></el-icon>
          </el-button>
          <el-button 
            size="small" 
            @click="handleTestConnection"
            :disabled="!selectedProxyId"
            :loading="testing"
            title="测试代理连接"
          >
            <el-icon><Connection /></el-icon>
          </el-button>
          <el-button 
            size="small" 
            @click="handleCreateProxy"
            title="创建新代理"
          >
            <el-icon><Plus /></el-icon>
          </el-button>
        </el-button-group>
      </div>
    </el-form-item>
    
    <!-- 代理详情显示 -->
    <div class="proxy-details" v-if="selectedProxy && showDetails">
      <el-card shadow="never" class="proxy-detail-card">
        <template #header>
          <div class="proxy-detail-header">
            <span>代理详情</span>
            <el-tag :type="getStatusTagType(selectedProxy.status)" size="small">
              {{ getProxyStatusLabel(selectedProxy.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" size="small">
          <el-descriptions-item label="代理协议">
            {{ selectedProxy.protocol }}
          </el-descriptions-item>
          <el-descriptions-item label="代理地址">
            {{ selectedProxy.address }}
          </el-descriptions-item>
          <el-descriptions-item label="最后连接" v-if="selectedProxy.extra?.lastConnectTime">
            {{ formatTime(selectedProxy.extra.lastConnectTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" v-if="selectedProxy.extra?.description">
            {{ selectedProxy.extra.description }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 连接错误信息 -->
        <el-alert
          v-if="selectedProxy.extra?.connectionError"
          :title="selectedProxy.extra.connectionError"
          type="error"
          :closable="false"
          show-icon
          class="proxy-error-alert"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Connection, Plus } from '@element-plus/icons-vue'
import { proxyApi, type ProxyOption, getProxyStatusLabel, getProxyStatusColor } from '@/api/monitor/proxy'

interface Props {
  /** 当前选中的代理ID */
  modelValue?: number | null
  /** 标签文本 */
  label?: string
  /** 表单属性名 */
  prop?: string
  /** 占位符 */
  placeholder?: string
  /** 是否可清空 */
  clearable?: boolean
  /** 是否禁用 */
  disabled?: boolean
  /** 验证规则 */
  rules?: any[]
  /** 过滤协议类型 */
  protocol?: string
  /** 是否显示操作按钮 */
  showActions?: boolean
  /** 是否显示代理详情 */
  showDetails?: boolean
  /** 只显示启用的代理 */
  enabledOnly?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: number | null): void
  (e: 'change', proxy: ProxyOption | null): void
  (e: 'create-proxy'): void
}

const props = withDefaults(defineProps<Props>(), {
  label: '选择代理',
  placeholder: '请选择代理配置',
  clearable: true,
  disabled: false,
  showActions: true,
  showDetails: false,
  enabledOnly: true
})

const emit = defineEmits<Emits>()

// 响应式数据
const loading = ref(false)
const testing = ref(false)
const proxyOptions = ref<ProxyOption[]>([])
const searchKeyword = ref('')

// 计算属性
const selectedProxyId = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const selectedProxy = computed(() => {
  if (!selectedProxyId.value) return null
  return proxyOptions.value.find(proxy => proxy.value === selectedProxyId.value) || null
})

// 方法
const loadProxyOptions = async (keyword?: string) => {
  try {
    loading.value = true
    const params: any = {}
    
    if (props.protocol) {
      params.protocol = props.protocol
    }
    
    if (props.enabledOnly) {
      params.enabled = true
    }
    
    const options = await proxyApi.getProxyOptions(params)
    
    // 如果有搜索关键词，进行本地过滤
    if (keyword) {
      proxyOptions.value = options.filter(option => 
        option.label.toLowerCase().includes(keyword.toLowerCase()) ||
        option.protocol.toLowerCase().includes(keyword.toLowerCase()) ||
        option.address.toLowerCase().includes(keyword.toLowerCase())
      )
    } else {
      proxyOptions.value = options
    }
  } catch (error) {
    console.error('加载代理选项失败:', error)
    ElMessage.error('加载代理列表失败')
    proxyOptions.value = []
  } finally {
    loading.value = false
  }
}

const handleRemoteSearch = (keyword: string) => {
  searchKeyword.value = keyword
  if (keyword) {
    loadProxyOptions(keyword)
  } else {
    loadProxyOptions()
  }
}

const handleProxyChange = (proxyId: number | null) => {
  const proxy = proxyId ? proxyOptions.value.find(p => p.value === proxyId) || null : null
  emit('change', proxy)
}

const handleClear = () => {
  emit('change', null)
}

const handleRefresh = () => {
  loadProxyOptions(searchKeyword.value)
}

const handleTestConnection = async () => {
  if (!selectedProxyId.value) return
  
  try {
    testing.value = true
    const result = await proxyApi.testProxyConnection(selectedProxyId.value)
    
    if (result.success) {
      ElMessage.success(`代理连接测试成功，响应时间: ${result.responseTime}ms`)
    } else {
      ElMessage.error(`代理连接测试失败: ${result.error}`)
    }
  } catch (error) {
    console.error('代理连接测试失败:', error)
    ElMessage.error('代理连接测试失败')
  } finally {
    testing.value = false
  }
}

const handleCreateProxy = () => {
  emit('create-proxy')
}

// 工具方法
const getProxyName = (label: string): string => {
  return label.split(' (')[0]
}

const getStatusTagType = (status: number): string => {
  const colorMap: Record<string, string> = {
    'success': 'success',
    'processing': 'warning',
    'error': 'danger',
    'default': 'info'
  }
  const color = getProxyStatusColor(status)
  return colorMap[color] || 'info'
}

const formatTime = (timeStr: string): string => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

// 监听协议变化，重新加载选项
watch(() => props.protocol, () => {
  loadProxyOptions()
})

// 监听协议变化，重新加载选项
watch(() => props.protocol, () => {
  loadProxyOptions()
})

// 监听启用状态变化，重新加载选项
watch(() => props.enabledOnly, () => {
  loadProxyOptions()
})

// 组件挂载时加载数据
onMounted(() => {
  loadProxyOptions()
})
</script>

<style scoped lang="scss">
.proxy-selector {
  .proxy-actions {
    margin-top: 8px;
  }
  
  .proxy-option {
    .proxy-option-main {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
      
      .proxy-name {
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
      
      .proxy-status {
        margin-left: 8px;
      }
    }
    
    .proxy-option-detail {
      display: flex;
      gap: 12px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      
      .proxy-protocol {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        padding: 2px 6px;
        border-radius: 4px;
      }
      
      .proxy-address {
        font-family: monospace;
      }
    }
  }
  
  .proxy-empty {
    padding: 20px;
  }
  
  .proxy-details {
    margin-top: 16px;
    
    .proxy-detail-card {
      border: 1px solid var(--el-border-color-light);
      
      .proxy-detail-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .proxy-error-alert {
        margin-top: 12px;
      }
    }
  }
}
</style>
