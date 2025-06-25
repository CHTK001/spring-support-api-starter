<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑代理' : '创建代理'"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="proxy-form"
    >
      <el-form-item label="代理名称" prop="proxyName">
        <el-input
          v-model="formData.proxyName"
          placeholder="请输入代理名称"
          clearable
        />
      </el-form-item>
      
      <el-form-item label="代理协议" prop="proxyProtocol">
        <el-select
          v-model="formData.proxyProtocol"
          placeholder="请选择代理协议"
          style="width: 100%"
          @change="handleProtocolChange"
        >
          <el-option
            v-for="protocol in protocolOptions"
            :key="protocol.value"
            :label="protocol.label"
            :value="protocol.value"
          >
            <div class="protocol-option">
              <span>{{ protocol.label }}</span>
              <span class="protocol-desc">{{ protocol.description }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      
      <el-row :gutter="16">
        <el-col :span="16">
          <el-form-item label="代理主机" prop="proxyHost">
            <el-input
              v-model="formData.proxyHost"
              placeholder="请输入代理服务器地址"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="代理端口" prop="proxyPort">
            <el-input-number
              v-model="formData.proxyPort"
              :min="1"
              :max="65535"
              placeholder="端口"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- 认证信息 -->
      <el-row :gutter="16" v-if="needAuth">
        <el-col :span="12">
          <el-form-item label="用户名" prop="proxyUsername">
            <el-input
              v-model="formData.proxyUsername"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="密码" prop="proxyPassword">
            <el-input
              v-model="formData.proxyPassword"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="代理描述">
        <el-input
          v-model="formData.proxyDescription"
          type="textarea"
          :rows="3"
          placeholder="请输入代理描述"
        />
      </el-form-item>
      
      <!-- 高级配置 -->
      <el-collapse v-model="activeCollapse" class="advanced-config">
        <el-collapse-item title="高级配置" name="advanced">
          <el-form-item label="代理配置">
            <el-input
              v-model="formData.proxyConfig"
              type="textarea"
              :rows="4"
              placeholder="JSON格式的代理配置参数（可选）"
            />
            <div class="config-hint">
              <el-text type="info" size="small">
                支持JSON格式的高级配置参数，如连接池、超时设置等
              </el-text>
            </div>
          </el-form-item>
          
          <el-form-item label="启用状态">
            <el-switch
              v-model="proxyEnabled"
              active-text="启用"
              inactive-text="禁用"
            />
          </el-form-item>
        </el-collapse-item>
      </el-collapse>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleTestConnection"
          :loading="testing"
          :disabled="!canTest"
        >
          测试连接
        </el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit"
          :loading="submitting"
        >
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { proxyApi, type ProxyConfig, proxyProtocolOptions } from '@/api/monitor/proxy'

interface Props {
  /** 对话框可见性 */
  modelValue: boolean
  /** 编辑的代理数据 */
  proxyData?: ProxyConfig | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success', proxy: ProxyConfig): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式数据
const formRef = ref()
const submitting = ref(false)
const testing = ref(false)
const activeCollapse = ref<string[]>([])

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => !!props.proxyData?.proxyId)

const protocolOptions = proxyProtocolOptions

// 表单数据
const formData = ref<Partial<ProxyConfig>>({
  proxyName: '',
  proxyProtocol: '',
  proxyHost: '',
  proxyPort: undefined,
  proxyUsername: '',
  proxyPassword: '',
  proxyDescription: '',
  proxyConfig: '',
  proxyEnabled: 1
})

// 代理启用状态
const proxyEnabled = computed({
  get: () => formData.value.proxyEnabled === 1,
  set: (value) => {
    formData.value.proxyEnabled = value ? 1 : 0
  }
})

// 是否需要认证
const needAuth = computed(() => {
  const authProtocols = ['GUACAMOLE', 'SSH', 'HTTP', 'HTTPS', 'SOCKS5']
  return authProtocols.includes(formData.value.proxyProtocol || '')
})

// 是否可以测试连接
const canTest = computed(() => {
  return formData.value.proxyHost && 
         formData.value.proxyPort && 
         formData.value.proxyProtocol
})

// 表单验证规则
const formRules = reactive({
  proxyName: [
    { required: true, message: '请输入代理名称', trigger: 'blur' },
    { min: 2, max: 50, message: '代理名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  proxyProtocol: [
    { required: true, message: '请选择代理协议', trigger: 'change' }
  ],
  proxyHost: [
    { required: true, message: '请输入代理主机地址', trigger: 'blur' },
    { 
      pattern: /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?)*$/,
      message: '请输入有效的IP地址或域名',
      trigger: 'blur'
    }
  ],
  proxyPort: [
    { required: true, message: '请输入代理端口', trigger: 'blur' },
    { type: 'number', min: 1, max: 65535, message: '端口号范围为 1-65535', trigger: 'blur' }
  ],
  proxyUsername: [
    { 
      validator: (rule: any, value: string, callback: Function) => {
        if (needAuth.value && !value) {
          callback(new Error('请输入用户名'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 方法
const initFormData = () => {
  if (props.proxyData) {
    formData.value = { ...props.proxyData }
  } else {
    formData.value = {
      proxyName: '',
      proxyProtocol: '',
      proxyHost: '',
      proxyPort: undefined,
      proxyUsername: '',
      proxyPassword: '',
      proxyDescription: '',
      proxyConfig: '',
      proxyEnabled: 1
    }
  }
}

const handleProtocolChange = (protocol: string) => {
  // 根据协议设置默认端口
  const defaultPorts: Record<string, number> = {
    'HTTP': 8080,
    'HTTPS': 8443,
    'SOCKS5': 1080,
    'GUACAMOLE': 8080,
    'VNC': 5900,
    'RDP': 3389,
    'SSH': 22
  }
  
  if (defaultPorts[protocol] && !formData.value.proxyPort) {
    formData.value.proxyPort = defaultPorts[protocol]
  }
}

const handleTestConnection = async () => {
  try {
    // 先验证表单
    await formRef.value?.validate()
    
    testing.value = true
    
    // 如果是新建代理，需要先创建临时代理进行测试
    let testProxyId: number
    
    if (isEdit.value) {
      testProxyId = formData.value.proxyId!
      // 更新代理配置
      await proxyApi.updateProxy(formData.value)
    } else {
      // 创建临时代理
      const tempProxy = await proxyApi.createProxy({
        ...formData.value,
        proxyName: `${formData.value.proxyName}_temp_${Date.now()}`
      })
      testProxyId = tempProxy.proxyId!
    }
    
    // 测试连接
    const result = await proxyApi.testProxyConnection(testProxyId)
    
    if (result.success) {
      ElMessage.success(`连接测试成功！响应时间: ${result.responseTime}ms`)
    } else {
      ElMessage.error(`连接测试失败: ${result.error}`)
    }
    
    // 如果是临时代理，删除它
    if (!isEdit.value) {
      await proxyApi.deleteProxy(testProxyId)
    }
    
  } catch (error: any) {
    console.error('测试连接失败:', error)
    ElMessage.error(error.message || '测试连接失败')
  } finally {
    testing.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    submitting.value = true
    
    let result: ProxyConfig
    
    if (isEdit.value) {
      await proxyApi.updateProxy(formData.value)
      result = formData.value as ProxyConfig
      ElMessage.success('代理更新成功')
    } else {
      result = await proxyApi.createProxy(formData.value)
      ElMessage.success('代理创建成功')
    }
    
    emit('success', result)
    handleClose()
    
  } catch (error: any) {
    console.error('保存代理失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  activeCollapse.value = []
  dialogVisible.value = false
}

// 监听对话框打开，初始化表单数据
watch(dialogVisible, (visible) => {
  if (visible) {
    nextTick(() => {
      initFormData()
    })
  }
})
</script>

<style scoped lang="scss">
.proxy-form {
  .protocol-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .protocol-desc {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
  
  .advanced-config {
    margin-top: 16px;
    
    .config-hint {
      margin-top: 4px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
