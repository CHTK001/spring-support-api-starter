import { request } from '@/utils/request'

/**
 * 服务器脚本接口
 */
export interface ServerScript {
  /** 脚本ID */
  id?: number
  /** 服务器ID */
  serverId: number
  /** 脚本名称 */
  name: string
  /** 脚本描述 */
  description?: string
  /** 脚本类型 */
  type: string
  /** 脚本内容 */
  content: string
  /** 脚本参数 */
  parameters?: string
  /** 执行超时时间(秒) */
  timeout?: number
  /** 是否启用 */
  enabled?: boolean
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 脚本执行记录接口
 */
export interface ScriptExecutionRecord {
  /** 执行记录ID */
  id?: number
  /** 脚本ID */
  scriptId: number
  /** 服务器ID */
  serverId: number
  /** 执行状态 */
  status: number
  /** 执行参数 */
  parameters?: string
  /** 执行结果 */
  result?: string
  /** 错误信息 */
  error?: string
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
  /** 执行时长(毫秒) */
  duration?: number
  /** 退出码 */
  exitCode?: number
  /** 创建时间 */
  createTime?: string
}

/**
 * 脚本查询参数
 */
export interface ScriptQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 服务器ID */
  serverId?: number
  /** 脚本名称 */
  name?: string
  /** 脚本类型 */
  type?: string
  /** 是否启用 */
  enabled?: boolean
}

/**
 * 脚本执行记录查询参数
 */
export interface ScriptExecutionQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 脚本ID */
  scriptId?: number
  /** 服务器ID */
  serverId?: number
  /** 执行状态 */
  status?: number
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
}

/**
 * 脚本执行请求
 */
export interface ScriptExecutionRequest {
  /** 脚本ID */
  scriptId: number
  /** 执行参数 */
  parameters?: Record<string, any>
  /** 是否异步执行 */
  async?: boolean
  /** 超时时间(秒) */
  timeout?: number
}

/**
 * 批量脚本执行请求
 */
export interface BatchScriptExecutionRequest {
  /** 脚本ID列表 */
  scriptIds: number[]
  /** 服务器ID列表 */
  serverIds?: number[]
  /** 执行参数 */
  parameters?: Record<string, any>
  /** 是否异步执行 */
  async?: boolean
  /** 超时时间(秒) */
  timeout?: number
}

/**
 * 脚本统计信息
 */
export interface ScriptStatistics {
  /** 总脚本数 */
  totalScripts: number
  /** 启用脚本数 */
  enabledScripts: number
  /** 按类型统计 */
  typeStats: Record<string, number>
  /** 按服务器统计 */
  serverStats: Record<number, number>
  /** 总执行次数 */
  totalExecutions: number
  /** 成功执行次数 */
  successExecutions: number
  /** 失败执行次数 */
  failedExecutions: number
  /** 平均执行时长 */
  averageDuration: number
}

/**
 * 服务器脚本管理API
 */
export const serverScriptApi = {
  /**
   * 分页查询服务器脚本
   */
  getServerScriptPage: (params: ScriptQueryParams) => {
    return request.get('/v1/gen/server/script/page', { params })
  },

  /**
   * 根据ID获取脚本详情
   */
  getScriptById: (id: number) => {
    return request.get(`/v1/gen/server/script/${id}`)
  },

  /**
   * 根据服务器ID获取脚本列表
   */
  getScriptsByServerId: (serverId: number) => {
    return request.get(`/v1/gen/server/script/server/${serverId}`)
  },

  /**
   * 创建脚本
   */
  createScript: (data: Partial<ServerScript>) => {
    return request.post('/v1/gen/server/script/save', data)
  },

  /**
   * 更新脚本
   */
  updateScript: (data: Partial<ServerScript>) => {
    return request.put('/v1/gen/server/script/update', data)
  },

  /**
   * 删除脚本
   */
  deleteScript: (id: number) => {
    return request.delete(`/v1/gen/server/script/delete/${id}`)
  },

  /**
   * 批量删除脚本
   */
  batchDeleteScript: (ids: number[]) => {
    return request.delete('/v1/gen/server/script/batch-delete', { data: ids })
  },

  /**
   * 启用脚本
   */
  enableScript: (id: number) => {
    return request.post(`/v1/gen/server/script/enable/${id}`)
  },

  /**
   * 禁用脚本
   */
  disableScript: (id: number) => {
    return request.post(`/v1/gen/server/script/disable/${id}`)
  },

  /**
   * 执行脚本
   */
  executeScript: (data: ScriptExecutionRequest) => {
    return request.post('/v1/gen/server/script/execute', data)
  },

  /**
   * 批量执行脚本
   */
  batchExecuteScript: (data: BatchScriptExecutionRequest) => {
    return request.post('/v1/gen/server/script/batch-execute', data)
  },

  /**
   * 停止脚本执行
   */
  stopScriptExecution: (executionId: number) => {
    return request.post(`/v1/gen/server/script/stop/${executionId}`)
  },

  /**
   * 获取脚本执行状态
   */
  getExecutionStatus: (executionId: number) => {
    return request.get(`/v1/gen/server/script/execution/status/${executionId}`)
  },

  /**
   * 分页查询脚本执行记录
   */
  getExecutionRecordPage: (params: ScriptExecutionQueryParams) => {
    return request.get('/v1/gen/server/script/execution/page', { params })
  },

  /**
   * 根据脚本ID获取执行记录
   */
  getExecutionRecordsByScriptId: (scriptId: number, limit?: number) => {
    return request.get(`/v1/gen/server/script/execution/script/${scriptId}`, { params: { limit } })
  },

  /**
   * 获取脚本执行结果
   */
  getExecutionResult: (executionId: number) => {
    return request.get(`/v1/gen/server/script/execution/result/${executionId}`)
  },

  /**
   * 获取脚本执行日志
   */
  getExecutionLogs: (executionId: number) => {
    return request.get(`/v1/gen/server/script/execution/logs/${executionId}`)
  },

  /**
   * 清理执行记录
   */
  cleanExecutionRecords: (scriptId?: number, days?: number) => {
    return request.delete('/v1/gen/server/script/execution/clean', { params: { scriptId, days } })
  },

  /**
   * 获取脚本统计信息
   */
  getScriptStatistics: (serverId?: number): Promise<ScriptStatistics> => {
    return request.get('/v1/gen/server/script/statistics', { params: { serverId } })
  },

  /**
   * 获取脚本类型列表
   */
  getScriptTypes: () => {
    return request.get('/v1/gen/server/script/types')
  },

  /**
   * 验证脚本语法
   */
  validateScript: (type: string, content: string) => {
    return request.post('/v1/gen/server/script/validate', { type, content })
  },

  /**
   * 导出脚本
   */
  exportScript: (id: number) => {
    return request.get(`/v1/gen/server/script/export/${id}`)
  },

  /**
   * 导入脚本
   */
  importScript: (serverId: number, scriptData: string) => {
    return request.post(`/v1/gen/server/script/import/${serverId}`, { scriptData })
  },

  /**
   * 克隆脚本
   */
  cloneScript: (id: number, targetServerId?: number) => {
    return request.post(`/v1/gen/server/script/clone/${id}`, { targetServerId })
  },

  /**
   * 获取脚本模板
   */
  getScriptTemplates: (type?: string) => {
    return request.get('/v1/gen/server/script/templates', { params: { type } })
  },

  /**
   * 从模板创建脚本
   */
  createScriptFromTemplate: (templateId: number, serverId: number, name: string) => {
    return request.post('/v1/gen/server/script/from-template', { templateId, serverId, name })
  }
}

/**
 * 脚本类型枚举
 */
export enum ScriptType {
  /** Shell脚本 */
  SHELL = 'shell',
  /** Python脚本 */
  PYTHON = 'python',
  /** PowerShell脚本 */
  POWERSHELL = 'powershell',
  /** 批处理脚本 */
  BATCH = 'batch',
  /** JavaScript脚本 */
  JAVASCRIPT = 'javascript',
  /** SQL脚本 */
  SQL = 'sql'
}

/**
 * 脚本类型选项
 */
export const scriptTypeOptions = [
  { label: 'Shell脚本', value: ScriptType.SHELL, extension: '.sh' },
  { label: 'Python脚本', value: ScriptType.PYTHON, extension: '.py' },
  { label: 'PowerShell脚本', value: ScriptType.POWERSHELL, extension: '.ps1' },
  { label: '批处理脚本', value: ScriptType.BATCH, extension: '.bat' },
  { label: 'JavaScript脚本', value: ScriptType.JAVASCRIPT, extension: '.js' },
  { label: 'SQL脚本', value: ScriptType.SQL, extension: '.sql' }
]

/**
 * 脚本执行状态枚举
 */
export enum ScriptExecutionStatus {
  /** 等待中 */
  PENDING = 0,
  /** 执行中 */
  RUNNING = 1,
  /** 已完成 */
  COMPLETED = 2,
  /** 已失败 */
  FAILED = 3,
  /** 已取消 */
  CANCELLED = 4,
  /** 超时 */
  TIMEOUT = 5
}

/**
 * 脚本执行状态选项
 */
export const scriptExecutionStatusOptions = [
  { label: '等待中', value: ScriptExecutionStatus.PENDING, color: 'default' },
  { label: '执行中', value: ScriptExecutionStatus.RUNNING, color: 'processing' },
  { label: '已完成', value: ScriptExecutionStatus.COMPLETED, color: 'success' },
  { label: '已失败', value: ScriptExecutionStatus.FAILED, color: 'error' },
  { label: '已取消', value: ScriptExecutionStatus.CANCELLED, color: 'default' },
  { label: '超时', value: ScriptExecutionStatus.TIMEOUT, color: 'warning' }
]

/**
 * 获取脚本类型标签
 */
export const getScriptTypeLabel = (type: string): string => {
  const option = scriptTypeOptions.find(item => item.value === type)
  return option?.label || type
}

/**
 * 获取脚本执行状态标签
 */
export const getScriptExecutionStatusLabel = (status: number): string => {
  const option = scriptExecutionStatusOptions.find(item => item.value === status)
  return option?.label || '未知'
}

/**
 * 获取脚本执行状态颜色
 */
export const getScriptExecutionStatusColor = (status: number): string => {
  const option = scriptExecutionStatusOptions.find(item => item.value === status)
  return option?.color || 'default'
}

/**
 * 格式化执行时长
 */
export const formatExecutionDuration = (duration: number): string => {
  if (duration < 1000) {
    return `${duration}ms`
  } else if (duration < 60000) {
    return `${(duration / 1000).toFixed(1)}s`
  } else {
    const minutes = Math.floor(duration / 60000)
    const seconds = Math.floor((duration % 60000) / 1000)
    return `${minutes}m ${seconds}s`
  }
}
