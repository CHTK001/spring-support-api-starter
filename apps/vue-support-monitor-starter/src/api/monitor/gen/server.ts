import { http, type ReturnResult } from '@repo/utils'

/**
 * 服务器基础信息接口
 */
export interface ServerInfo {
  /** 服务器配置ID */
  monitorSysGenServerId: number
  /** 服务器名称 */
  monitorSysGenServerName: string
  /** 服务器主机地址 */
  monitorSysGenServerHost: string
  /** 服务器端口 */
  monitorSysGenServerPort: number
  /** 连接协议 (SSH, RDP, TELNET, VNC等) */
  monitorSysGenServerProtocol: string
  /** 用户名 */
  monitorSysGenServerUsername?: string
  /** 密码 */
  monitorSysGenServerPassword?: string
  /** 私钥文件路径 (SSH协议使用) */
  monitorSysGenServerPrivateKey?: string
  /** 指标存储时间(天) */
  monitorSysGenServerMetricsRetentionDays?: number
  /** 监控间隔(秒) */
  monitorSysGenServerMonitorInterval?: number
  /** 连接超时时间(毫秒) */
  monitorSysGenServerTimeout?: number
  /** 服务器描述 */
  monitorSysGenServerDesc?: string
  /** 服务器状态 0:停用 1:启用 */
  monitorSysGenServerStatus: number
  /** 是否启用监控 0:否 1:是 */
  monitorSysGenServerMonitorEnabled: number
  /** 服务器标签 */
  monitorSysGenServerTags?: string
  /** 连接状态 0:离线 1:在线 2:连接中 3:连接失败 */
  monitorSysGenServerConnectionStatus: number
  /** 最后连接时间 */
  monitorSysGenServerLastConnectTime?: string
  /** 连接失败原因 */
  monitorSysGenServerConnectionError?: string
  /** 是否需要上报 0:否 1:是 */
  monitorSysGenServerReportEnabled?: number
  /** 代理配置ID */
  monitorSysGenServerProxyId?: number
  /** 数据上报方式 */
  monitorSysGenServerDataReportMethod?: string
  /** Prometheus服务器地址 */
  monitorSysGenServerPrometheusHost?: string
  /** Prometheus服务器端口 */
  monitorSysGenServerPrometheusPort?: number
  /** 代理服务器地址 */
  monitorSysGenServerProxyHost?: string
  /** 代理服务器端口 */
  monitorSysGenServerProxyPort?: number
  /** 是否本地服务器 0:否 1:是 */
  monitorSysGenServerIsLocal?: number
  /** 服务器IP地址列表 */
  monitorSysGenServerIpAddresses?: string
  /** 是否支持Docker 0:否 1:是 */
  monitorSysGenServerDockerEnabled?: number
  /** Docker连接方式 */
  monitorSysGenServerDockerConnectionType?: string
  /** Docker TCP连接地址 */
  monitorSysGenServerDockerHost?: string
  /** Docker TCP连接端口 */
  monitorSysGenServerDockerPort?: number
  /** 操作系统类型 */
  monitorSysGenServerOsType?: string
  /** 操作系统版本 */
  monitorSysGenServerOsVersion?: string
  /** 操作系统架构 */
  monitorSysGenServerOsArch?: string
  /** 创建时间 */
  monitorSysGenServerCreateTime?: string
  /** 更新时间 */
  monitorSysGenServerUpdateTime?: string
}

/**
 * 服务器显示数据接口（简化字段名）
 */
export interface ServerDisplayData {
  /** 服务器ID */
  id: number
  /** 服务器名称 */
  name: string
  /** 服务器地址 */
  host: string
  /** 端口 */
  port: number
  /** 协议 */
  protocol: string
  /** 用户名 */
  username?: string
  /** 描述 */
  description?: string
  /** 状态 */
  status: number
  /** 连接状态 */
  connectionStatus: number
  /** 在线状态（用于前端显示） */
  onlineStatus: number
  /** 是否启用监控 */
  monitorEnabled: number
  /** 标签 */
  tags?: string
  /** 最后连接时间 */
  lastConnectTime?: string
  /** 连接错误信息 */
  connectionError?: string
  /** 是否本地服务器 */
  isLocal?: number
  /** 数据上报方式 */
  dataReportMethod?: string
  /** 操作系统信息 */
  osInfo?: string
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 服务器指标显示数据接口
 */
export interface ServerMetricsDisplay {
  /** CPU使用率 (%) */
  cpuUsage: number
  /** 内存使用率 (%) */
  memoryUsage: number
  /** 磁盘使用率 (%) */
  diskUsage: number
  /** 网络入流量 (KB/s) */
  networkIn: number
  /** 网络出流量 (KB/s) */
  networkOut: number
  /** 系统负载平均值 */
  loadAverage: string
  /** 系统运行时间 (秒) */
  uptime: number
  /** 数据收集时间 */
  collectTime: string
  /** CPU核心数 */
  cpuCores?: number
  /** 进程数量 */
  processCount?: number
  /** 响应时间 (毫秒) */
  responseTime?: number
  /** 操作系统信息 */
  osInfo?: string
}

/**
 * 服务器查询参数
 */
export interface ServerQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 服务器名称 */
  monitorSysGenServerName?: string
  /** 服务器主机地址 */
  monitorSysGenServerHost?: string
  /** 连接协议 */
  monitorSysGenServerProtocol?: string
  /** 服务器状态 */
  monitorSysGenServerStatus?: number
  /** 连接状态 */
  monitorSysGenServerConnectionStatus?: number
  /** 是否启用监控 */
  monitorSysGenServerMonitorEnabled?: number
  /** 服务器标签 */
  monitorSysGenServerTags?: string
}

/**
 * 服务器状态常量
 */
export const SERVER_STATUS = {
  DISABLED: 0,  // 停用
  ENABLED: 1,   // 启用
  ERROR: 2      // 错误
} as const

/**
 * 在线状态常量
 */
export const ONLINE_STATUS = {
  OFFLINE: 0,     // 离线
  ONLINE: 1,      // 在线
  CONNECTING: 2,  // 连接中
  FAILED: 3       // 连接失败
} as const

/**
 * 分页查询服务器列表
 */
export const getServerPageList = (params: { page?: number; pageSize?: number; [key: string]: any }) => {
  // 转换参数格式以匹配后端接口
  const queryParams = {
    current: params.page || 1,
    size: params.pageSize || 10,
    ...params
  }
  return http.request<ReturnResult<{ records: ServerInfo[]; total: number; current: number; size: number }>>('get', '/v1/gen/server/page', { params: queryParams })
}

/**
 * 根据ID获取服务器详情
 */
export const getServerById = (id: string) => {
  return http.request<ReturnResult<ServerInfo>>('get', `/v1/gen/server/${id}`)
}

/**
 * 创建服务器
 */
export const createServer = (data: Partial<ServerInfo>) => {
  return http.request<ReturnResult<ServerInfo>>('post', '/v1/gen/server/save', { data })
}

/**
 * 更新服务器
 */
export const updateServer = (data: Partial<ServerInfo>) => {
  return http.request<ReturnResult<boolean>>('put', '/v1/gen/server/update', { data })
}

/**
 * 删除服务器
 */
export const deleteServer = (id: string) => {
  return http.request<ReturnResult<boolean>>('delete', '/v1/gen/server/delete', { params: { id } })
}

/**
 * 测试服务器连接
 */
export const testServerConnection = (id: string) => {
  return http.request<ReturnResult<boolean>>('get', '/v1/gen/server/test', { params: { id } })
}

/**
 * 连接服务器
 */
export const connectServer = (id: string) => {
  return http.request<ReturnResult<boolean>>('post', '/v1/gen/server/connect', { data: { id } })
}

/**
 * 断开服务器连接
 */
export const disconnectServer = (id: string) => {
  return http.request<ReturnResult<boolean>>('post', '/v1/gen/server/disconnect', { data: { id } })
}

/**
 * 获取服务器连接状态
 */
export const getConnectionStatus = (id: string) => {
  return http.request<ReturnResult<any>>('get', '/v1/gen/server/status', { params: { id } })
}

/**
 * 测试本地IP检测功能
 */
export const testLocalIpDetection = (host?: string) => {
  return http.request<ReturnResult<any>>('get', '/v1/gen/server/test-local-ip', { params: { host } })
}

/**
 * 获取服务器统计信息
 */
export const getServerStatistics = () => {
  return http.request<ReturnResult<any>>('get', '/v1/gen/server/statistics')
}

/**
 * 将服务器列表数据映射为显示数据
 */
export const mapServerListToDisplayData = (serverList: ServerInfo[]): ServerDisplayData[] => {
  return serverList.map(server => ({
    id: server.monitorSysGenServerId,
    name: server.monitorSysGenServerName,
    host: server.monitorSysGenServerHost,
    port: server.monitorSysGenServerPort,
    protocol: server.monitorSysGenServerProtocol,
    username: server.monitorSysGenServerUsername,
    description: server.monitorSysGenServerDesc,
    status: server.monitorSysGenServerStatus,
    connectionStatus: server.monitorSysGenServerConnectionStatus,
    onlineStatus: server.monitorSysGenServerConnectionStatus, // 使用连接状态作为在线状态
    monitorEnabled: server.monitorSysGenServerMonitorEnabled,
    tags: server.monitorSysGenServerTags,
    lastConnectTime: server.monitorSysGenServerLastConnectTime,
    connectionError: server.monitorSysGenServerConnectionError,
    isLocal: server.monitorSysGenServerIsLocal,
    dataReportMethod: server.monitorSysGenServerDataReportMethod,
    osInfo: `${server.monitorSysGenServerOsType || ''} ${server.monitorSysGenServerOsVersion || ''}`.trim(),
    createTime: server.monitorSysGenServerCreateTime,
    updateTime: server.monitorSysGenServerUpdateTime
  }))
}
