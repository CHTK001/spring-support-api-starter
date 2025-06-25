import { request } from '@/utils/request'

/**
 * 服务器日志接口
 */
export interface ServerLog {
  /** 日志ID */
  id?: number
  /** 服务器ID */
  serverId: number
  /** 日志级别 */
  level: string
  /** 日志消息 */
  message: string
  /** 日志来源 */
  source?: string
  /** 日志标签 */
  tags?: string
  /** 创建时间 */
  createTime?: string
  /** 时间戳 */
  timestamp?: number
}

/**
 * 日志查询参数
 */
export interface LogQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 服务器ID */
  serverId?: number
  /** 日志级别 */
  level?: string
  /** 关键词搜索 */
  keyword?: string
  /** 日志来源 */
  source?: string
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
  /** 时间范围(分钟) */
  timeRange?: number
}

/**
 * 实时日志订阅参数
 */
export interface LogSubscribeParams {
  /** 服务器ID */
  serverId: number
  /** 日志级别过滤 */
  levels?: string[]
  /** 关键词过滤 */
  keywords?: string[]
  /** 来源过滤 */
  sources?: string[]
  /** 最大行数 */
  maxLines?: number
}

/**
 * 日志统计信息
 */
export interface LogStatistics {
  /** 总日志数 */
  totalLogs: number
  /** 按级别统计 */
  levelStats: Record<string, number>
  /** 按来源统计 */
  sourceStats: Record<string, number>
  /** 按服务器统计 */
  serverStats: Record<number, number>
  /** 最新日志时间 */
  latestLogTime: string
  /** 错误日志数 */
  errorCount: number
  /** 警告日志数 */
  warningCount: number
}

/**
 * 日志导出参数
 */
export interface LogExportParams {
  /** 服务器ID */
  serverId?: number
  /** 日志级别 */
  levels?: string[]
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
  /** 导出格式 */
  format?: 'txt' | 'csv' | 'json'
  /** 最大行数 */
  maxLines?: number
}

/**
 * 服务器日志管理API
 */
export const serverLogApi = {
  /**
   * 分页查询服务器日志
   */
  getServerLogPage: (params: LogQueryParams) => {
    return request.get('/v1/gen/server/log/page', { params })
  },

  /**
   * 根据服务器ID获取最新日志
   */
  getLatestLogs: (serverId: number, limit: number = 100) => {
    return request.get(`/v1/gen/server/log/latest/${serverId}`, { params: { limit } })
  },

  /**
   * 搜索日志
   */
  searchLogs: (params: LogQueryParams) => {
    return request.get('/v1/gen/server/log/search', { params })
  },

  /**
   * 获取日志统计信息
   */
  getLogStatistics: (serverId?: number, timeRange?: string): Promise<LogStatistics> => {
    return request.get('/v1/gen/server/log/statistics', { params: { serverId, timeRange } })
  },

  /**
   * 获取日志级别列表
   */
  getLogLevels: (serverId?: number) => {
    return request.get('/v1/gen/server/log/levels', { params: { serverId } })
  },

  /**
   * 获取日志来源列表
   */
  getLogSources: (serverId?: number) => {
    return request.get('/v1/gen/server/log/sources', { params: { serverId } })
  },

  /**
   * 清理过期日志
   */
  cleanExpiredLogs: (serverId: number, days: number) => {
    return request.delete(`/v1/gen/server/log/expired/${serverId}`, { params: { days } })
  },

  /**
   * 批量清理过期日志
   */
  batchCleanExpiredLogs: (days: number) => {
    return request.delete('/v1/gen/server/log/batch-clean', { params: { days } })
  },

  /**
   * 导出日志
   */
  exportLogs: (params: LogExportParams) => {
    return request.post('/v1/gen/server/log/export', params)
  },

  /**
   * 获取日志文件列表
   */
  getLogFiles: (serverId: number, path?: string) => {
    return request.get(`/v1/gen/server/log/files/${serverId}`, { params: { path } })
  },

  /**
   * 读取日志文件内容
   */
  readLogFile: (serverId: number, filePath: string, params?: {
    lines?: number
    offset?: number
    tail?: boolean
  }) => {
    return request.get(`/v1/gen/server/log/file/content/${serverId}`, { 
      params: { filePath, ...params } 
    })
  },

  /**
   * 监控日志文件变化
   */
  watchLogFile: (serverId: number, filePath: string) => {
    return request.post(`/v1/gen/server/log/file/watch/${serverId}`, { filePath })
  },

  /**
   * 停止监控日志文件
   */
  unwatchLogFile: (serverId: number, filePath: string) => {
    return request.post(`/v1/gen/server/log/file/unwatch/${serverId}`, { filePath })
  },

  /**
   * 获取系统日志
   */
  getSystemLogs: (serverId: number, logType: string, params?: {
    lines?: number
    since?: string
    until?: string
  }) => {
    return request.get(`/v1/gen/server/log/system/${serverId}/${logType}`, { params })
  },

  /**
   * 获取应用日志
   */
  getApplicationLogs: (serverId: number, appName: string, params?: {
    lines?: number
    since?: string
    until?: string
  }) => {
    return request.get(`/v1/gen/server/log/application/${serverId}/${appName}`, { params })
  },

  /**
   * 创建日志告警规则
   */
  createLogAlertRule: (data: {
    serverId: number
    name: string
    pattern: string
    level: string
    action: string
    description?: string
  }) => {
    return request.post('/v1/gen/server/log/alert-rules', data)
  },

  /**
   * 获取日志告警规则
   */
  getLogAlertRules: (serverId: number) => {
    return request.get(`/v1/gen/server/log/alert-rules/${serverId}`)
  },

  /**
   * 更新日志告警规则
   */
  updateLogAlertRule: (ruleId: number, data: any) => {
    return request.put(`/v1/gen/server/log/alert-rules/${ruleId}`, data)
  },

  /**
   * 删除日志告警规则
   */
  deleteLogAlertRule: (ruleId: number) => {
    return request.delete(`/v1/gen/server/log/alert-rules/${ruleId}`)
  }
}

/**
 * 日志级别枚举
 */
export enum LogLevel {
  /** 调试 */
  DEBUG = 'DEBUG',
  /** 信息 */
  INFO = 'INFO',
  /** 警告 */
  WARN = 'WARN',
  /** 错误 */
  ERROR = 'ERROR',
  /** 致命错误 */
  FATAL = 'FATAL'
}

/**
 * 日志级别选项
 */
export const logLevelOptions = [
  { label: '调试', value: LogLevel.DEBUG, color: 'default' },
  { label: '信息', value: LogLevel.INFO, color: 'blue' },
  { label: '警告', value: LogLevel.WARN, color: 'orange' },
  { label: '错误', value: LogLevel.ERROR, color: 'red' },
  { label: '致命错误', value: LogLevel.FATAL, color: 'red' }
]

/**
 * 系统日志类型枚举
 */
export enum SystemLogType {
  /** 系统日志 */
  SYSLOG = 'syslog',
  /** 内核日志 */
  KERNEL = 'kernel',
  /** 认证日志 */
  AUTH = 'auth',
  /** 邮件日志 */
  MAIL = 'mail',
  /** 定时任务日志 */
  CRON = 'cron',
  /** 启动日志 */
  BOOT = 'boot'
}

/**
 * 系统日志类型选项
 */
export const systemLogTypeOptions = [
  { label: '系统日志', value: SystemLogType.SYSLOG },
  { label: '内核日志', value: SystemLogType.KERNEL },
  { label: '认证日志', value: SystemLogType.AUTH },
  { label: '邮件日志', value: SystemLogType.MAIL },
  { label: '定时任务日志', value: SystemLogType.CRON },
  { label: '启动日志', value: SystemLogType.BOOT }
]

/**
 * 获取日志级别标签
 */
export const getLogLevelLabel = (level: string): string => {
  const option = logLevelOptions.find(item => item.value === level)
  return option?.label || level
}

/**
 * 获取日志级别颜色
 */
export const getLogLevelColor = (level: string): string => {
  const option = logLevelOptions.find(item => item.value === level)
  return option?.color || 'default'
}

/**
 * 格式化日志时间
 */
export const formatLogTime = (timestamp: string | number): string => {
  const date = new Date(typeof timestamp === 'string' ? timestamp : timestamp * 1000)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
