import request from '@/utils/request'
import type { 
  Server, 
  ServerConnectionStatus, 
  PageQuery, 
  PageResult,
  Statistics 
} from '@/types'

// 服务器管理API
export const serverApi = {
  // 获取服务器列表
  getServerList(params: PageQuery & {
    serverName?: string
    serverHost?: string
    serverType?: string
    serverOs?: string
    status?: number
  }) {
    return request.get<PageResult<Server>>('/v1/server/page', { params })
  },

  // 获取服务器详情
  getServerDetail(id: number) {
    return request.get<Server>(`/v1/server/${id}`)
  },

  // 创建服务器
  createServer(data: Partial<Server>) {
    return request.post<Server>('/v1/server/create', data)
  },

  // 更新服务器
  updateServer(id: number, data: Partial<Server>) {
    return request.put<Server>(`/v1/server/${id}`, data)
  },

  // 删除服务器
  deleteServer(id: number) {
    return request.delete<boolean>(`/v1/server/${id}`)
  },

  // 批量删除服务器
  batchDeleteServers(ids: number[]) {
    return request.delete<boolean>('/v1/server/batch', { data: { ids } })
  },

  // 测试服务器连接
  testConnection(id: number) {
    return request.post<boolean>(`/v1/server/${id}/test-connection`)
  },

  // 批量测试连接
  batchTestConnection(ids: number[]) {
    return request.post<any>('/v1/server/batch/test-connection', { ids })
  },

  // 获取服务器统计
  getServerStatistics() {
    return request.get<Statistics>('/v1/server/statistics')
  },

  // 获取服务器类型列表
  getServerTypes() {
    return request.get<string[]>('/v1/server/types')
  },

  // 获取操作系统列表
  getOperatingSystems() {
    return request.get<string[]>('/v1/server/os')
  }
}

// 服务器连接状态API
export const connectionStatusApi = {
  // 获取连接状态列表
  getConnectionStatusList(params: PageQuery & {
    serverId?: number
    status?: string
    startTime?: string
    endTime?: string
  }) {
    return request.get<PageResult<ServerConnectionStatus>>('/v1/server/connection-status/page', { params })
  },

  // 获取服务器连接状态
  getServerConnectionStatus(serverId: number) {
    return request.get<ServerConnectionStatus>(`/v1/server/connection-status/server/${serverId}`)
  },

  // 获取所有在线服务器
  getOnlineServers() {
    return request.get<ServerConnectionStatus[]>('/v1/server/connection-status/online')
  },

  // 获取所有离线服务器
  getOfflineServers() {
    return request.get<ServerConnectionStatus[]>('/v1/server/connection-status/offline')
  },

  // 获取连接状态统计
  getConnectionStatistics(params?: {
    startTime?: string
    endTime?: string
  }) {
    return request.get<Statistics>('/v1/server/connection-status/statistics', { params })
  },

  // 获取连接状态历史
  getConnectionHistory(serverId: number, params?: {
    startTime?: string
    endTime?: string
    limit?: number
  }) {
    return request.get<ServerConnectionStatus[]>(`/v1/server/connection-status/history/${serverId}`, { params })
  },

  // 手动检查连接状态
  checkConnectionStatus(serverId: number) {
    return request.post<ServerConnectionStatus>(`/v1/server/connection-status/check/${serverId}`)
  },

  // 批量检查连接状态
  batchCheckConnectionStatus(serverIds: number[]) {
    return request.post<any>('/v1/server/connection-status/batch/check', { serverIds })
  },

  // 获取连接状态趋势
  getConnectionTrend(params?: {
    serverId?: number
    startTime?: string
    endTime?: string
    interval?: string
  }) {
    return request.get<any[]>('/v1/server/connection-status/trend', { params })
  },

  // 获取响应时间统计
  getResponseTimeStats(serverId: number, params?: {
    startTime?: string
    endTime?: string
  }) {
    return request.get<Statistics>(`/v1/server/connection-status/response-time/${serverId}`, { params })
  },

  // 获取连接状态报告
  getConnectionReport(params?: {
    startTime?: string
    endTime?: string
    format?: string
  }) {
    return request.get<any>('/v1/server/connection-status/report', { params })
  },

  // 设置连接状态告警
  setConnectionAlert(serverId: number, data: {
    enabled: boolean
    threshold?: number
    notificationMethods?: string[]
  }) {
    return request.post<boolean>(`/v1/server/connection-status/alert/${serverId}`, data)
  }
}
