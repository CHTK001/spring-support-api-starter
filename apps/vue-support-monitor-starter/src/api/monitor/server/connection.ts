import { request } from '@/utils/request'

/**
 * 服务器连接状态接口
 */
export interface ServerConnectionStatus {
  /** 连接状态ID */
  monitorSysGenServerConnectionStatusId?: number
  /** 服务器ID */
  monitorSysGenServerId: number
  /** 连接状态 0:离线 1:在线 2:连接中 3:连接失败 */
  monitorSysGenServerConnectionStatusStatus: number
  /** 连接开始时间 */
  monitorSysGenServerConnectionStatusStartTime?: string
  /** 连接结束时间 */
  monitorSysGenServerConnectionStatusEndTime?: string
  /** 连接持续时间(秒) */
  monitorSysGenServerConnectionStatusDuration?: number
  /** 连接失败原因 */
  monitorSysGenServerConnectionStatusError?: string
  /** 连接IP地址 */
  monitorSysGenServerConnectionStatusIp?: string
  /** 连接端口 */
  monitorSysGenServerConnectionStatusPort?: number
  /** 连接协议 */
  monitorSysGenServerConnectionStatusProtocol?: string
  /** 创建时间 */
  monitorSysGenServerConnectionStatusCreateTime?: string
  /** 更新时间 */
  monitorSysGenServerConnectionStatusUpdateTime?: string
}

/**
 * 连接状态查询参数
 */
export interface ConnectionStatusQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 服务器ID */
  monitorSysGenServerId?: number
  /** 连接状态 */
  monitorSysGenServerConnectionStatusStatus?: number
  /** 连接协议 */
  monitorSysGenServerConnectionStatusProtocol?: string
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
}

/**
 * 连接状态统计信息
 */
export interface ConnectionStatusStatistics {
  /** 总连接数 */
  totalConnections: number
  /** 在线连接数 */
  onlineConnections: number
  /** 离线连接数 */
  offlineConnections: number
  /** 连接中数量 */
  connectingConnections: number
  /** 连接失败数量 */
  failedConnections: number
  /** 平均连接时长 */
  averageDuration: number
  /** 按协议统计 */
  protocolStats: Record<string, number>
  /** 按状态统计 */
  statusStats: Record<number, number>
}

/**
 * 服务器连接状态管理API
 */
export const serverConnectionApi = {
  /**
   * 测试单个服务器连接
   */
  testServerConnection: (id: number) => {
    return request.post(`/v1/server/connection/test/${id}`)
  },

  /**
   * 批量测试服务器连接
   */
  batchTestServerConnection: (serverIds: number[]) => {
    return request.post('/v1/server/connection/batch-test', serverIds)
  },

  /**
   * 获取所有服务器连接状态
   */
  getAllServerConnectionStatus: () => {
    return request.get('/v1/server/connection/status/all')
  },

  /**
   * 根据服务器ID获取连接状态
   */
  getServerConnectionStatus: (serverId: number) => {
    return request.get(`/v1/server/connection/status/${serverId}`)
  },

  /**
   * 分页查询连接状态历史
   */
  getConnectionStatusPage: (params: ConnectionStatusQueryParams) => {
    return request.get('/v1/server/connection/page', { params })
  },

  /**
   * 根据服务器ID获取连接状态历史
   */
  getConnectionStatusHistory: (serverId: number, params?: { startTime?: string; endTime?: string }) => {
    return request.get(`/v1/server/connection/history/${serverId}`, { params })
  },

  /**
   * 获取连接状态统计
   */
  getConnectionStatusStatistics: (): Promise<ConnectionStatusStatistics> => {
    return request.get('/v1/server/connection/statistics')
  },

  /**
   * 获取长时间未连接的服务器
   */
  getLongTimeNoConnectServers: (minutes: number = 60) => {
    return request.get('/v1/server/connection/long-time-no-connect', { params: { minutes } })
  },

  /**
   * 强制断开服务器连接
   */
  forceDisconnectServer: (serverId: number) => {
    return request.post(`/v1/server/connection/force-disconnect/${serverId}`)
  },

  /**
   * 重置服务器连接状态
   */
  resetConnectionStatus: (serverId: number) => {
    return request.post(`/v1/server/connection/reset/${serverId}`)
  },

  /**
   * 获取服务器连接日志
   */
  getConnectionLogs: (serverId: number, params?: { startTime?: string; endTime?: string; limit?: number }) => {
    return request.get(`/v1/server/connection/logs/${serverId}`, { params })
  },

  /**
   * 导出连接状态报告
   */
  exportConnectionReport: (params?: { serverIds?: number[]; startTime?: string; endTime?: string }) => {
    return request.post('/v1/server/connection/export', params)
  }
}

/**
 * 连接状态枚举
 */
export enum ConnectionStatus {
  /** 离线 */
  OFFLINE = 0,
  /** 在线 */
  ONLINE = 1,
  /** 连接中 */
  CONNECTING = 2,
  /** 连接失败 */
  FAILED = 3
}

/**
 * 连接状态选项
 */
export const connectionStatusOptions = [
  { label: '离线', value: ConnectionStatus.OFFLINE, color: 'default' },
  { label: '在线', value: ConnectionStatus.ONLINE, color: 'success' },
  { label: '连接中', value: ConnectionStatus.CONNECTING, color: 'processing' },
  { label: '连接失败', value: ConnectionStatus.FAILED, color: 'error' }
]

/**
 * 连接协议枚举
 */
export enum ConnectionProtocol {
  SSH = 'SSH',
  RDP = 'RDP',
  VNC = 'VNC',
  TELNET = 'TELNET',
  HTTP = 'HTTP',
  HTTPS = 'HTTPS'
}

/**
 * 连接协议选项
 */
export const connectionProtocolOptions = [
  { label: 'SSH', value: ConnectionProtocol.SSH },
  { label: 'RDP', value: ConnectionProtocol.RDP },
  { label: 'VNC', value: ConnectionProtocol.VNC },
  { label: 'Telnet', value: ConnectionProtocol.TELNET },
  { label: 'HTTP', value: ConnectionProtocol.HTTP },
  { label: 'HTTPS', value: ConnectionProtocol.HTTPS }
]

/**
 * 获取连接状态标签
 */
export const getConnectionStatusLabel = (status: number): string => {
  const option = connectionStatusOptions.find(item => item.value === status)
  return option?.label || '未知'
}

/**
 * 获取连接状态颜色
 */
export const getConnectionStatusColor = (status: number): string => {
  const option = connectionStatusOptions.find(item => item.value === status)
  return option?.color || 'default'
}

/**
 * 获取连接协议标签
 */
export const getConnectionProtocolLabel = (protocol: string): string => {
  const option = connectionProtocolOptions.find(item => item.value === protocol)
  return option?.label || protocol
}
