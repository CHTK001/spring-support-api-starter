<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑服务器' : '添加服务器'"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="服务器名称" prop="monitorSysGenServerName">
        <el-input
          v-model="form.monitorSysGenServerName"
          placeholder="请输入服务器名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="主机地址" prop="monitorSysGenServerHost">
        <el-input
          v-model="form.monitorSysGenServerHost"
          placeholder="请输入IP地址或域名"
        />
      </el-form-item>

      <el-form-item label="端口号" prop="monitorSysGenServerPort">
        <el-input-number
          v-model="form.monitorSysGenServerPort"
          :min="1"
          :max="65535"
          placeholder="请输入端口号"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="用户名" prop="monitorSysGenServerUsername">
        <el-input
          v-model="form.monitorSysGenServerUsername"
          placeholder="请输入登录用户名"
        />
      </el-form-item>

      <el-form-item label="密码" prop="monitorSysGenServerPassword">
        <el-input
          v-model="form.monitorSysGenServerPassword"
          type="password"
          placeholder="请输入登录密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="服务器类型" prop="monitorSysGenServerType">
        <el-select
          v-model="form.monitorSysGenServerType"
          placeholder="请选择服务器类型"
          style="width: 100%"
        >
          <el-option
            v-for="type in serverTypes"
            :key="type"
            :label="type"
            :value="type"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="操作系统" prop="monitorSysGenServerOs">
        <el-select
          v-model="form.monitorSysGenServerOs"
          placeholder="请选择操作系统"
          style="width: 100%"
        >
          <el-option
            v-for="os in operatingSystems"
            :key="os"
            :label="os"
            :value="os"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="描述">
        <el-input
          v-model="form.monitorSysGenServerDescription"
          type="textarea"
          :rows="3"
          placeholder="请输入服务器描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-radio-group v-model="form.monitorSysGenServerStatus">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleTestConnection" :loading="testing">
          测试连接
        </el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { serverApi } from '@/api/server'
import { isValidIP, isValidPort } from '@/utils'
import type { Server } from '@/types'

// Props
interface Props {
  visible: boolean
  formData: Partial<Server>
  isEdit: boolean
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const testing = ref(false)
const serverTypes = ref<string[]>([])
const operatingSystems = ref<string[]>([])

// 表单数据
const form = reactive<Partial<Server>>({
  monitorSysGenServerName: '',
  monitorSysGenServerHost: '',
  monitorSysGenServerPort: 22,
  monitorSysGenServerUsername: '',
  monitorSysGenServerPassword: '',
  monitorSysGenServerType: '',
  monitorSysGenServerOs: '',
  monitorSysGenServerDescription: '',
  monitorSysGenServerStatus: 1
})

// 表单验证规则
const rules: FormRules = {
  monitorSysGenServerName: [
    { required: true, message: '请输入服务器名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  monitorSysGenServerHost: [
    { required: true, message: '请输入主机地址', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入主机地址'))
        } else if (!isValidIP(value) && !/^[a-zA-Z0-9.-]+$/.test(value)) {
          callback(new Error('请输入有效的IP地址或域名'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  monitorSysGenServerPort: [
    { required: true, message: '请输入端口号', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入端口号'))
        } else if (!isValidPort(value)) {
          callback(new Error('端口号必须在 1-65535 之间'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  monitorSysGenServerUsername: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  monitorSysGenServerPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  monitorSysGenServerType: [
    { required: true, message: '请选择服务器类型', trigger: 'change' }
  ],
  monitorSysGenServerOs: [
    { required: true, message: '请选择操作系统', trigger: 'change' }
  ]
}

// 方法
const fetchServerTypes = async () => {
  try {
    const response = await serverApi.getServerTypes()
    if (response.data) {
      serverTypes.value = response.data
    }
  } catch (error) {
    console.error('获取服务器类型失败:', error)
  }
}

const fetchOperatingSystems = async () => {
  try {
    const response = await serverApi.getOperatingSystems()
    if (response.data) {
      operatingSystems.value = response.data
    }
  } catch (error) {
    console.error('获取操作系统列表失败:', error)
  }
}

const handleTestConnection = async () => {
  if (!formRef.value) return
  
  try {
    // 验证必要字段
    await formRef.value.validateField([
      'monitorSysGenServerHost',
      'monitorSysGenServerPort',
      'monitorSysGenServerUsername',
      'monitorSysGenServerPassword'
    ])
    
    testing.value = true
    
    // 如果是编辑模式且有ID，直接测试
    if (props.isEdit && form.monitorSysGenServerId) {
      await serverApi.testConnection(form.monitorSysGenServerId)
    } else {
      // 新建模式，需要先创建临时服务器进行测试
      // 这里可以调用一个专门的测试接口
      ElMessage.info('连接测试功能需要先保存服务器信息')
      return
    }
    
    ElMessage.success('连接测试成功')
  } catch (error) {
    console.error('连接测试失败:', error)
  } finally {
    testing.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    if (props.isEdit && form.monitorSysGenServerId) {
      // 更新服务器
      await serverApi.updateServer(form.monitorSysGenServerId, form)
      ElMessage.success('更新成功')
    } else {
      // 创建服务器
      await serverApi.createServer(form)
      ElMessage.success('创建成功')
    }
    
    emit('success')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  emit('update:visible', false)
  resetForm()
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    monitorSysGenServerName: '',
    monitorSysGenServerHost: '',
    monitorSysGenServerPort: 22,
    monitorSysGenServerUsername: '',
    monitorSysGenServerPassword: '',
    monitorSysGenServerType: '',
    monitorSysGenServerOs: '',
    monitorSysGenServerDescription: '',
    monitorSysGenServerStatus: 1
  })
}

// 监听表单数据变化
watch(
  () => props.formData,
  (newData) => {
    if (newData && Object.keys(newData).length > 0) {
      Object.assign(form, newData)
    } else {
      resetForm()
    }
  },
  { immediate: true, deep: true }
)

// 生命周期
onMounted(() => {
  fetchServerTypes()
  fetchOperatingSystems()
})
</script>

<style lang="scss" scoped>
.dialog-footer {
  text-align: right;
}
</style>
