// 服务器相关类型定义

export interface Server {
  monitorSysGenServerId: number
  monitorSysGenServerName: string
  monitorSysGenServerHost: string
  monitorSysGenServerPort: number
  monitorSysGenServerUsername: string
  monitorSysGenServerPassword: string
  monitorSysGenServerDescription?: string
  monitorSysGenServerType: string
  monitorSysGenServerOs: string
  monitorSysGenServerStatus: number
  monitorSysGenServerCreateTime: string
  monitorSysGenServerUpdateTime: string
}

// 服务器连接状态
export interface ServerConnectionStatus {
  monitorSysGenServerConnectionStatusId: number
  monitorSysGenServerId: number
  monitorSysGenServerConnectionStatus: string
  monitorSysGenServerConnectionLastCheckTime: string
  monitorSysGenServerConnectionResponseTime: number
  monitorSysGenServerConnectionErrorMessage?: string
  monitorSysGenServerConnectionCreateTime: string
  monitorSysGenServerConnectionUpdateTime: string
}

// 服务器日志
export interface ServerLog {
  monitorSysGenServerLogId: number
  monitorSysGenServerId: number
  monitorSysGenServerLogLevel: string
  monitorSysGenServerLogSource: string
  monitorSysGenServerLogContent: string
  monitorSysGenServerLogTimestamp: string
  monitorSysGenServerLogCreateTime: string
}

// 日志配置
export interface ServerLogConfig {
  monitorSysGenServerLogConfigId: number
  monitorSysGenServerId: number
  monitorSysGenServerLogEnabled: number
  monitorSysGenServerLogLevel: string
  monitorSysGenServerLogRetentionDays: number
  monitorSysGenServerLogRealTimeEnabled: number
  monitorSysGenServerLogCollectionInterval: number
  monitorSysGenServerLogCreateTime: string
  monitorSysGenServerLogUpdateTime: string
}

// 文件上传任务
export interface FileUploadTask {
  monitorSysGenFileUploadTaskId: number
  monitorSysGenFileUploadTaskName: string
  monitorSysGenFileUploadFileName: string
  monitorSysGenFileUploadFilePath: string
  monitorSysGenFileUploadFileSize: number
  monitorSysGenFileUploadFileMd5: string
  monitorSysGenFileUploadTargetServerIds: string
  monitorSysGenFileUploadTargetPaths: string
  monitorSysGenFileUploadMode: string
  monitorSysGenFileUploadType: string
  monitorSysGenFileUploadStatus: string
  monitorSysGenFileUploadScheduledTime?: string
  monitorSysGenFileUploadPriority: number
  monitorSysGenFileUploadRetryCount: number
  monitorSysGenFileUploadMaxRetry: number
  monitorSysGenFileUploadTimeoutSeconds: number
  monitorSysGenFileUploadOverwriteEnabled: number
  monitorSysGenFileUploadBackupEnabled: number
  monitorSysGenFileUploadCompressEnabled: number
  monitorSysGenFileUploadVerifyEnabled: number
  monitorSysGenFileUploadDescription?: string
  monitorSysGenFileUploadTags?: string
  monitorSysGenFileUploadCreateUser: string
  monitorSysGenFileUploadCreateTime: string
  monitorSysGenFileUploadUpdateTime: string
  monitorSysGenFileUploadStartTime?: string
  monitorSysGenFileUploadEndTime?: string
}

// 文件上传记录
export interface FileUploadRecord {
  monitorSysGenFileUploadRecordId: number
  monitorSysGenFileUploadTaskId: number
  monitorSysGenServerId: number
  monitorSysGenFileUploadTargetPath: string
  monitorSysGenFileUploadStatus: string
  monitorSysGenFileUploadProgress: number
  monitorSysGenFileUploadSpeed: number
  monitorSysGenFileUploadTransferredBytes: number
  monitorSysGenFileUploadTotalBytes: number
  monitorSysGenFileUploadStartTime?: string
  monitorSysGenFileUploadEndTime?: string
  monitorSysGenFileUploadDurationMs?: number
  monitorSysGenFileUploadErrorMessage?: string
  monitorSysGenFileUploadErrorCode?: string
  monitorSysGenFileUploadRetryCount: number
  monitorSysGenFileUploadFileMd5Remote?: string
  monitorSysGenFileUploadVerificationStatus?: string
  monitorSysGenFileUploadBackupPath?: string
  monitorSysGenFileUploadCreateTime: string
  monitorSysGenFileUploadUpdateTime: string
}

// 服务器脚本
export interface ServerScript {
  monitorSysGenServerScriptId: number
  monitorSysGenServerScriptName: string
  monitorSysGenServerScriptDescription?: string
  monitorSysGenServerScriptContent: string
  monitorSysGenServerScriptType: string
  monitorSysGenServerScriptLanguage: string
  monitorSysGenServerScriptVersion: string
  monitorSysGenServerScriptCategory?: string
  monitorSysGenServerScriptTags?: string
  monitorSysGenServerScriptParameters?: string
  monitorSysGenServerScriptTimeoutSeconds: number
  monitorSysGenServerScriptRetryCount: number
  monitorSysGenServerScriptIsTemplate: number
  monitorSysGenServerScriptIsPublic: number
  monitorSysGenServerScriptExecutionMode: string
  monitorSysGenServerScriptWorkingDirectory?: string
  monitorSysGenServerScriptEnvironmentVars?: string
  monitorSysGenServerScriptDependencies?: string
  monitorSysGenServerScriptValidationRules?: string
  monitorSysGenServerScriptCreateUser: string
  monitorSysGenServerScriptCreateTime: string
  monitorSysGenServerScriptUpdateTime: string
  monitorSysGenServerScriptLastExecutionTime?: string
  monitorSysGenServerScriptExecutionCount: number
  monitorSysGenServerScriptSuccessCount: number
  monitorSysGenServerScriptStatus: number
}

// 脚本执行记录
export interface ScriptExecution {
  monitorSysGenServerScriptExecutionId: number
  monitorSysGenServerScriptId: number
  monitorSysGenServerId: number
  monitorSysGenServerScriptExecutionName?: string
  monitorSysGenServerScriptExecutionParameters?: string
  monitorSysGenServerScriptExecutionStatus: string
  monitorSysGenServerScriptExecutionMode: string
  monitorSysGenServerScriptExecutionPriority: number
  monitorSysGenServerScriptExecutionStartTime?: string
  monitorSysGenServerScriptExecutionEndTime?: string
  monitorSysGenServerScriptExecutionDurationMs?: number
  monitorSysGenServerScriptExecutionExitCode?: number
  monitorSysGenServerScriptExecutionOutput?: string
  monitorSysGenServerScriptExecutionErrorOutput?: string
  monitorSysGenServerScriptExecutionWorkingDirectory?: string
  monitorSysGenServerScriptExecutionEnvironmentVars?: string
  monitorSysGenServerScriptExecutionProcessId?: string
  monitorSysGenServerScriptExecutionTriggerType: string
  monitorSysGenServerScriptExecutionTriggerUser?: string
  monitorSysGenServerScriptExecutionRetryCount: number
  monitorSysGenServerScriptExecutionMaxRetry: number
  monitorSysGenServerScriptExecutionParentId?: number
  monitorSysGenServerScriptExecutionScheduledTime?: string
  monitorSysGenServerScriptExecutionTimeoutSeconds: number
  monitorSysGenServerScriptExecutionTags?: string
  monitorSysGenServerScriptExecutionMetadata?: string
  monitorSysGenServerScriptExecutionCreateTime: string
  monitorSysGenServerScriptExecutionUpdateTime: string
}

// 分页查询参数
export interface PageQuery {
  current: number
  size: number
}

// 分页响应
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 统计数据
export interface Statistics {
  [key: string]: any
}

// 图表数据
export interface ChartData {
  name: string
  value: number
  [key: string]: any
}

// 选项类型
export interface Option {
  label: string
  value: string | number
  disabled?: boolean
  [key: string]: any
}

// 表单规则
export interface FormRule {
  required?: boolean
  message?: string
  trigger?: string | string[]
  min?: number
  max?: number
  pattern?: RegExp
  validator?: (rule: any, value: any, callback: any) => void
}

// 表格列配置
export interface TableColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  fixed?: boolean | string
  sortable?: boolean
  formatter?: (row: any, column: any, cellValue: any, index: number) => any
  [key: string]: any
}
