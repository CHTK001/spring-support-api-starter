<template>
  <div class="server-setting-form">
    <!-- 监控配置 -->
    <div v-if="section === 'monitor'" class="setting-section">
      <el-form-item label="启用监控" prop="monitorSysGenServerSettingMonitorEnabled">
        <el-switch
          v-model="formData.monitorSysGenServerSettingMonitorEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
        <span class="form-tip">开启后将收集服务器性能指标</span>
      </el-form-item>

      <el-form-item label="监控间隔" prop="monitorSysGenServerSettingMonitorInterval">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingMonitorInterval"
          :min="30"
          :max="3600"
          :step="30"
          placeholder="监控间隔(秒)"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">秒，建议值：60</span>
      </el-form-item>

      <el-form-item label="数据保留天数" prop="monitorSysGenServerSettingMetricsRetentionDays">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingMetricsRetentionDays"
          :min="1"
          :max="365"
          :step="1"
          placeholder="数据保留天数"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">天，超过此时间的历史数据将被清理</span>
      </el-form-item>

      <el-form-item label="数据上报方式" prop="monitorSysGenServerSettingDataReportMethod">
        <el-select
          v-model="formData.monitorSysGenServerSettingDataReportMethod"
          placeholder="请选择数据上报方式"
          style="width: 200px"
          @change="handleChange"
        >
          <el-option label="本地收集" value="LOCAL" />
          <el-option label="API上报" value="API" />
          <el-option label="Prometheus" value="PROMETHEUS" />
        </el-select>
        <span class="form-tip">选择指标数据的收集方式</span>
      </el-form-item>
    </div>

    <!-- 告警配置 -->
    <div v-if="section === 'alert'" class="setting-section">
      <el-form-item label="启用告警" prop="monitorSysGenServerSettingAlertEnabled">
        <el-switch
          v-model="formData.monitorSysGenServerSettingAlertEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
        <span class="form-tip">开启后将发送告警通知</span>
      </el-form-item>

      <!-- 告警阈值配置 -->
      <el-divider content-position="left">告警阈值</el-divider>

      <el-form-item label="CPU告警阈值" prop="monitorSysGenServerSettingCpuAlertThreshold">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingCpuAlertThreshold"
          :min="1"
          :max="100"
          :step="1"
          placeholder="CPU使用率阈值(%)"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">%，超过此值将触发告警</span>
      </el-form-item>

      <el-form-item label="内存告警阈值" prop="monitorSysGenServerSettingMemoryAlertThreshold">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingMemoryAlertThreshold"
          :min="1"
          :max="100"
          :step="1"
          placeholder="内存使用率阈值(%)"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">%，超过此值将触发告警</span>
      </el-form-item>

      <el-form-item label="磁盘告警阈值" prop="monitorSysGenServerSettingDiskAlertThreshold">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingDiskAlertThreshold"
          :min="1"
          :max="100"
          :step="1"
          placeholder="磁盘使用率阈值(%)"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">%，超过此值将触发告警</span>
      </el-form-item>

      <el-form-item label="网络告警阈值" prop="monitorSysGenServerSettingNetworkAlertThreshold">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingNetworkAlertThreshold"
          :min="1"
          :max="1000"
          :step="10"
          placeholder="网络使用率阈值(Mbps)"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">Mbps，超过此值将触发告警</span>
      </el-form-item>

      <el-form-item label="响应时间阈值" prop="monitorSysGenServerSettingResponseTimeAlertThreshold">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingResponseTimeAlertThreshold"
          :min="100"
          :max="60000"
          :step="100"
          placeholder="响应时间阈值(毫秒)"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">毫秒，超过此值将触发告警</span>
      </el-form-item>

      <!-- 通知配置 -->
      <el-divider content-position="left">通知配置</el-divider>

      <el-form-item label="告警方式" prop="monitorSysGenServerSettingAlertNotificationMethod">
        <el-select
          v-model="formData.monitorSysGenServerSettingAlertNotificationMethod"
          placeholder="请选择告警方式"
          style="width: 200px"
          @change="handleChange"
        >
          <el-option label="邮件" value="EMAIL" />
          <el-option label="短信" value="SMS" />
          <el-option label="钉钉" value="DINGTALK" />
          <el-option label="企业微信" value="WECHAT" />
          <el-option label="Webhook" value="WEBHOOK" />
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="formData.monitorSysGenServerSettingAlertNotificationMethod === 'EMAIL'"
        label="邮件地址"
        prop="monitorSysGenServerSettingAlertNotificationAddress"
      >
        <el-input
          v-model="formData.monitorSysGenServerSettingAlertNotificationAddress"
          placeholder="请输入邮件地址，多个用逗号分隔"
          maxlength="500"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item
        v-if="formData.monitorSysGenServerSettingAlertNotificationMethod === 'SMS'"
        label="手机号码"
        prop="monitorSysGenServerSettingAlertNotificationAddress"
      >
        <el-input
          v-model="formData.monitorSysGenServerSettingAlertNotificationAddress"
          placeholder="请输入手机号码，多个用逗号分隔"
          maxlength="500"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item
        v-if="formData.monitorSysGenServerSettingAlertNotificationMethod === 'DINGTALK'"
        label="钉钉机器人Token"
        prop="monitorSysGenServerSettingAlertNotificationAddress"
      >
        <el-input
          v-model="formData.monitorSysGenServerSettingAlertNotificationAddress"
          placeholder="请输入钉钉机器人Token"
          maxlength="500"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item
        v-if="formData.monitorSysGenServerSettingAlertNotificationMethod === 'WECHAT'"
        label="企业微信Webhook"
        prop="monitorSysGenServerSettingAlertNotificationAddress"
      >
        <el-input
          v-model="formData.monitorSysGenServerSettingAlertNotificationAddress"
          placeholder="请输入企业微信Webhook地址"
          maxlength="500"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item
        v-if="formData.monitorSysGenServerSettingAlertNotificationMethod === 'WEBHOOK'"
        label="Webhook URL"
        prop="monitorSysGenServerSettingAlertNotificationAddress"
      >
        <el-input
          v-model="formData.monitorSysGenServerSettingAlertNotificationAddress"
          placeholder="请输入Webhook URL"
          maxlength="500"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item label="告警静默时间" prop="monitorSysGenServerSettingAlertSilenceDuration">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingAlertSilenceDuration"
          :min="1"
          :max="1440"
          :step="1"
          placeholder="告警静默时间(分钟)"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">分钟，同类型告警在此时间内不重复发送</span>
      </el-form-item>

      <el-form-item label="自动恢复通知" prop="monitorSysGenServerSettingAutoRecoveryNotificationEnabled">
        <el-switch
          v-model="formData.monitorSysGenServerSettingAutoRecoveryNotificationEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
        <span class="form-tip">告警恢复时是否发送通知</span>
      </el-form-item>
    </div>

    <!-- Docker配置 -->
    <div v-if="section === 'docker'" class="setting-section">
      <el-form-item label="启用Docker监控" prop="monitorSysGenServerSettingDockerEnabled">
        <el-switch
          v-model="formData.monitorSysGenServerSettingDockerEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
        <span class="form-tip">开启后将监控Docker容器</span>
      </el-form-item>

      <el-form-item 
        v-if="formData.monitorSysGenServerSettingDockerEnabled"
        label="连接方式" 
        prop="monitorSysGenServerSettingDockerConnectionType"
      >
        <el-select
          v-model="formData.monitorSysGenServerSettingDockerConnectionType"
          placeholder="请选择连接方式"
          style="width: 200px"
          @change="handleChange"
        >
          <el-option label="Shell命令" value="SHELL" />
          <el-option label="Docker API" value="API" />
          <el-option label="Unix Socket" value="SOCKET" />
        </el-select>
      </el-form-item>

      <el-form-item 
        v-if="formData.monitorSysGenServerSettingDockerEnabled && formData.monitorSysGenServerSettingDockerConnectionType === 'API'"
        label="Docker主机" 
        prop="monitorSysGenServerSettingDockerHost"
      >
        <el-input
          v-model="formData.monitorSysGenServerSettingDockerHost"
          placeholder="请输入Docker主机地址"
          maxlength="100"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item 
        v-if="formData.monitorSysGenServerSettingDockerEnabled && formData.monitorSysGenServerSettingDockerConnectionType === 'API'"
        label="Docker端口" 
        prop="monitorSysGenServerSettingDockerPort"
      >
        <el-input-number
          v-model="formData.monitorSysGenServerSettingDockerPort"
          :min="1"
          :max="65535"
          placeholder="Docker端口"
          style="width: 200px"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item 
        v-if="formData.monitorSysGenServerSettingDockerEnabled && formData.monitorSysGenServerSettingDockerConnectionType === 'API'"
        label="启用TLS" 
        prop="monitorSysGenServerSettingDockerTlsEnabled"
      >
        <el-switch
          v-model="formData.monitorSysGenServerSettingDockerTlsEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
      </el-form-item>
    </div>

    <!-- 代理配置 -->
    <div v-if="section === 'proxy'" class="setting-section">
      <el-form-item label="启用代理" prop="monitorSysGenServerSettingProxyEnabled">
        <el-switch
          v-model="formData.monitorSysGenServerSettingProxyEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
        <span class="form-tip">通过代理服务器连接</span>
      </el-form-item>

      <el-form-item 
        v-if="formData.monitorSysGenServerSettingProxyEnabled"
        label="代理类型" 
        prop="monitorSysGenServerSettingProxyType"
      >
        <el-select
          v-model="formData.monitorSysGenServerSettingProxyType"
          placeholder="请选择代理类型"
          style="width: 200px"
          @change="handleChange"
        >
          <el-option label="HTTP代理" value="HTTP" />
          <el-option label="SOCKS5代理" value="SOCKS5" />
        </el-select>
      </el-form-item>

      <el-form-item 
        v-if="formData.monitorSysGenServerSettingProxyEnabled"
        label="代理主机" 
        prop="monitorSysGenServerSettingProxyHost"
      >
        <el-input
          v-model="formData.monitorSysGenServerSettingProxyHost"
          placeholder="请输入代理主机地址"
          maxlength="100"
          @change="handleChange"
        />
      </el-form-item>

      <el-form-item 
        v-if="formData.monitorSysGenServerSettingProxyEnabled"
        label="代理端口" 
        prop="monitorSysGenServerSettingProxyPort"
      >
        <el-input-number
          v-model="formData.monitorSysGenServerSettingProxyPort"
          :min="1"
          :max="65535"
          placeholder="代理端口"
          style="width: 200px"
          @change="handleChange"
        />
      </el-form-item>
    </div>

    <!-- 高级配置 -->
    <div v-if="section === 'advanced'" class="setting-section">
      <el-form-item label="进程监控" prop="monitorSysGenServerSettingProcessMonitorEnabled">
        <el-switch
          v-model="formData.monitorSysGenServerSettingProcessMonitorEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
        <span class="form-tip">监控系统进程</span>
      </el-form-item>

      <el-form-item label="服务监控" prop="monitorSysGenServerSettingServiceMonitorEnabled">
        <el-switch
          v-model="formData.monitorSysGenServerSettingServiceMonitorEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
        <span class="form-tip">监控系统服务</span>
      </el-form-item>

      <el-form-item label="端口监控" prop="monitorSysGenServerSettingPortMonitorEnabled">
        <el-switch
          v-model="formData.monitorSysGenServerSettingPortMonitorEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
        <span class="form-tip">监控网络端口</span>
      </el-form-item>

      <el-form-item label="日志监控" prop="monitorSysGenServerSettingLogMonitorEnabled">
        <el-switch
          v-model="formData.monitorSysGenServerSettingLogMonitorEnabled"
          :active-value="1"
          :inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          @change="handleChange"
        />
        <span class="form-tip">监控系统日志</span>
      </el-form-item>

      <el-form-item label="连接超时时间" prop="monitorSysGenServerSettingConnectionTimeout">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingConnectionTimeout"
          :min="5"
          :max="300"
          :step="5"
          placeholder="连接超时时间(秒)"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">秒，建议值：30</span>
      </el-form-item>

      <el-form-item label="读取超时时间" prop="monitorSysGenServerSettingReadTimeout">
        <el-input-number
          v-model="formData.monitorSysGenServerSettingReadTimeout"
          :min="10"
          :max="600"
          :step="10"
          placeholder="读取超时时间(秒)"
          style="width: 200px"
          @change="handleChange"
        />
        <span class="form-tip">秒，建议值：60</span>
      </el-form-item>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch, defineProps, defineEmits } from "vue";
import type { ServerSetting } from "@/api/monitor/server/setting";

// 定义属性
const props = defineProps<{
  modelValue: Partial<ServerSetting>;
  section: "monitor" | "alert" | "docker" | "proxy" | "advanced";
}>();

// 定义事件
const emit = defineEmits<{
  "update:modelValue": [value: Partial<ServerSetting>];
  change: [value: Partial<ServerSetting>];
}>();

// 表单数据
const formData = reactive<Partial<ServerSetting>>({
  // 监控配置默认值
  monitorSysGenServerSettingMonitorEnabled: 0,
  monitorSysGenServerSettingMonitorInterval: 60,
  monitorSysGenServerSettingMetricsRetentionDays: 30,
  monitorSysGenServerSettingDataReportMethod: "API",

  // 告警配置默认值
  monitorSysGenServerSettingAlertEnabled: 0,
  monitorSysGenServerSettingCpuAlertThreshold: 80,
  monitorSysGenServerSettingMemoryAlertThreshold: 80,
  monitorSysGenServerSettingDiskAlertThreshold: 90,
  monitorSysGenServerSettingNetworkAlertThreshold: 100,
  monitorSysGenServerSettingResponseTimeAlertThreshold: 5000,
  monitorSysGenServerSettingAlertNotificationMethod: "EMAIL",
  monitorSysGenServerSettingAlertSilenceDuration: 30,
  monitorSysGenServerSettingAutoRecoveryNotificationEnabled: 1,

  // Docker配置默认值
  monitorSysGenServerSettingDockerEnabled: 0,
  monitorSysGenServerSettingDockerConnectionType: "SHELL",
  monitorSysGenServerSettingDockerPort: 2376,
  monitorSysGenServerSettingDockerTlsEnabled: 0,

  // 代理配置默认值
  monitorSysGenServerSettingProxyEnabled: 0,
  monitorSysGenServerSettingProxyType: "HTTP",

  // 高级配置默认值
  monitorSysGenServerSettingProcessMonitorEnabled: 0,
  monitorSysGenServerSettingServiceMonitorEnabled: 0,
  monitorSysGenServerSettingPortMonitorEnabled: 0,
  monitorSysGenServerSettingLogMonitorEnabled: 0,
  monitorSysGenServerSettingConnectionTimeout: 0,
  monitorSysGenServerSettingReadTimeout: 60,
});

/**
 * 处理数据变化
 */
const handleChange = () => {
  emit("update:modelValue", formData);
  emit("change", formData);
};

// 监听外部数据变化
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue) {
      Object.assign(formData, newValue);
    }
  },
  { immediate: true, deep: true }
);

// 监听表单数据变化
watch(
  formData,
  (newValue) => {
    emit("update:modelValue", newValue);
  },
  { deep: true }
);
</script>

<style scoped>
.server-setting-form {
  padding: 20px 0;
}

.setting-section {
  max-width: 600px;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}
</style>
