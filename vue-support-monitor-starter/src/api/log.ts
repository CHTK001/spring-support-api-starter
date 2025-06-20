import request from '@/utils/request'
import type { 
  ServerLog, 
  ServerLogConfig, 
  PageQuery, 
  PageResult,
  Statistics 
} from '@/types'

// 服务器日志API
export const logApi = {
  // 获取日志列表
  getLogList(params: PageQuery & {
    serverId?: number
    level?: string
    source?: string
    content?: string
    startTime?: string
    endTime?: string
  }) {
    return request.get<PageResult<ServerLog>>('/v1/server/logs/page', { params })
  },

  // 获取日志详情
  getLogDetail(id: number) {
    return request.get<ServerLog>(`/v1/server/logs/${id}`)
  },

  // 根据服务器ID获取日志
  getLogsByServerId(serverId: number, params?: PageQuery & {
    level?: string
    startTime?: string
    endTime?: string
  }) {
    return request.get<PageResult<ServerLog>>(`/v1/server/logs/server/${serverId}`, { params })
  },

  // 根据日志级别获取日志
  getLogsByLevel(level: string, params?: PageQuery) {
    return request.get<PageResult<ServerLog>>(`/v1/server/logs/level/${level}`, { params })
  },

  // 获取最近的日志
  getRecentLogs(limit = 100) {
    return request.get<ServerLog[]>(`/v1/server/logs/recent`, { params: { limit } })
  },

  // 搜索日志
  searchLogs(params: {
    keyword: string
    serverId?: number
    level?: string
    startTime?: string
    endTime?: string
  } & PageQuery) {
    return request.get<PageResult<ServerLog>>('/v1/server/logs/search', { params })
  },

  // 获取日志统计
  getLogStatistics(params?: {
    serverId?: number
    startTime?: string
    endTime?: string
  }) {
    return request.get<Statistics>('/v1/server/logs/statistics', { params })
  },

  // 获取日志级别统计
  getLogLevelStatistics(serverId?: number) {
    return request.get<any[]>('/v1/server/logs/statistics/level', { 
      params: serverId ? { serverId } : undefined 
    })
  },

  // 获取日志趋势
  getLogTrend(params?: {
    serverId?: number
    startTime?: string
    endTime?: string
    interval?: string
  }) {
    return request.get<any[]>('/v1/server/logs/trend', { params })
  },

  // 删除过期日志
  deleteExpiredLogs(days: number) {
    return request.delete<number>('/v1/server/logs/expired', { params: { days } })
  },

  // 导出日志
  exportLogs(params: {
    serverId?: number
    level?: string
    startTime?: string
    endTime?: string
    format?: string
  }) {
    return request.download('/v1/server/logs/export', 'logs.csv', { params })
  },

  // 清理日志
  cleanupLogs(serverId: number, days: number) {
    return request.post<number>(`/v1/server/logs/cleanup/${serverId}`, { days })
  }
}

// 日志配置API
export const logConfigApi = {
  // 根据服务器ID获取日志配置
  getConfigByServerId(serverId: number) {
    return request.get<ServerLogConfig>(`/v1/server/log-config/server/${serverId}`)
  },

  // 保存或更新日志配置
  saveOrUpdateConfig(config: Partial<ServerLogConfig>) {
    return request.post<ServerLogConfig>('/v1/server/log-config/save', config)
  },

  // 获取所有启用的日志配置
  getEnabledConfigs() {
    return request.get<ServerLogConfig[]>('/v1/server/log-config/enabled')
  },

  // 获取启用实时日志的配置
  getRealTimeEnabledConfigs() {
    return request.get<ServerLogConfig[]>('/v1/server/log-config/realtime-enabled')
  },

  // 根据保留天数获取配置
  getConfigsByRetentionDays(retentionDays: number) {
    return request.get<ServerLogConfig[]>(`/v1/server/log-config/retention/${retentionDays}`)
  },

  // 获取需要清理的配置
  getConfigsForCleanup() {
    return request.get<ServerLogConfig[]>('/v1/server/log-config/cleanup')
  },

  // 更新配置状态
  updateConfigStatus(serverId: number, status: number) {
    return request.put<boolean>(`/v1/server/log-config/server/${serverId}/status`, null, {
      params: { status }
    })
  },

  // 更新日志启用状态
  updateLogEnabled(serverId: number, enabled: number) {
    return request.put<boolean>(`/v1/server/log-config/server/${serverId}/enabled`, null, {
      params: { enabled }
    })
  },

  // 更新实时日志启用状态
  updateRealTimeEnabled(serverId: number, enabled: number) {
    return request.put<boolean>(`/v1/server/log-config/server/${serverId}/realtime-enabled`, null, {
      params: { enabled }
    })
  },

  // 批量更新配置状态
  batchUpdateConfigStatus(serverIds: number[], status: number) {
    return request.put<boolean>('/v1/server/log-config/batch/status', {
      serverIds,
      status
    })
  },

  // 获取配置统计信息
  getConfigStatistics() {
    return request.get<Statistics>('/v1/server/log-config/statistics')
  },

  // 根据收集间隔获取配置
  getConfigsByCollectionInterval(interval: number) {
    return request.get<ServerLogConfig[]>(`/v1/server/log-config/interval/${interval}`)
  },

  // 获取过期配置
  getExpiredConfigs(days: number) {
    return request.get<ServerLogConfig[]>(`/v1/server/log-config/expired/${days}`)
  },

  // 检查服务器是否有日志配置
  hasConfigByServerId(serverId: number) {
    return request.get<boolean>(`/v1/server/log-config/server/${serverId}/exists`)
  },

  // 获取默认配置模板
  getDefaultTemplate() {
    return request.get<ServerLogConfig>('/v1/server/log-config/template/default')
  },

  // 为服务器创建默认配置
  createDefaultConfig(serverId: number) {
    return request.post<ServerLogConfig>(`/v1/server/log-config/server/${serverId}/default`)
  },

  // 复制配置到其他服务器
  copyConfigToServers(sourceServerId: number, targetServerIds: number[]) {
    return request.post<boolean>(`/v1/server/log-config/copy/${sourceServerId}`, targetServerIds)
  },

  // 验证配置有效性
  validateConfig(config: Partial<ServerLogConfig>) {
    return request.post<any>('/v1/server/log-config/validate', config)
  },

  // 重置配置为默认值
  resetConfigToDefault(serverId: number) {
    return request.post<ServerLogConfig>(`/v1/server/log-config/server/${serverId}/reset`)
  }
}
