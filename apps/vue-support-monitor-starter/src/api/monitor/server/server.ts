import { request } from '@/utils/request'

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
 * 批量操作请求
 */
export interface BatchOperationRequest {
  /** 服务器ID列表 */
  ids: string[]
  /** 操作类型 */
  action: string
}

/**
 * 克隆服务器请求
 */
export interface CloneServerRequest {
  /** 源服务器ID */
  sourceId: string
  /** 目标服务器名称 */
  targetName: string
}

/**
 * 数据上报配置请求
 */
export interface ReportConfigRequest {
  /** 是否启用上报 */
  reportEnabled?: boolean
  /** 数据上报方式 */
  dataReportMethod?: string
  /** Prometheus主机 */
  prometheusHost?: string
  /** Prometheus端口 */
  prometheusPort?: number
  /** 代理类型 */
  proxyType?: string
  /** 代理主机 */
  proxyHost?: string
  /** 代理端口 */
  proxyPort?: number
}

/**
 * 服务器基础管理API
 */
export const serverApi = {
  /**
   * 分页查询服务器列表
   */
  getServerPage: (params: ServerQueryParams) => {
    return request.get('/v1/gen/server/page', { params })
  },

  /**
   * 根据ID获取服务器详情
   */
  getServerById: (id: string) => {
    return request.get(`/v1/gen/server/${id}`)
  },

  /**
   * 创建服务器
   */
  createServer: (data: Partial<ServerInfo>) => {
    return request.post('/v1/gen/server/save', data)
  },

  /**
   * 更新服务器
   */
  updateServer: (data: Partial<ServerInfo>) => {
    return request.put('/v1/gen/server/update', data)
  },

  /**
   * 删除服务器
   */
  deleteServer: (id: string) => {
    return request.delete('/v1/gen/server/delete', { params: { id } })
  },

  /**
   * 测试服务器连接
   */
  testConnection: (id: string) => {
    return request.get('/v1/gen/server/test', { params: { id } })
  },

  /**
   * 连接服务器
   */
  connectServer: (id: string) => {
    return request.post('/v1/gen/server/connect', { id })
  },

  /**
   * 断开服务器连接
   */
  disconnectServer: (id: string) => {
    return request.post('/v1/gen/server/disconnect', { id })
  },

  /**
   * 获取服务器连接状态
   */
  getConnectionStatus: (id: string) => {
    return request.get('/v1/gen/server/status', { params: { id } })
  },

  /**
   * 执行服务器命令
   */
  executeCommand: (id: string, command: string) => {
    return request.post('/v1/gen/server/execute', command, { params: { id } })
  },

  /**
   * 获取服务器信息
   */
  getServerInfo: (id: string) => {
    return request.get('/v1/gen/server/info', { params: { id } })
  },

  /**
   * 发送数据到服务器
   */
  sendData: (id: string, data: string) => {
    return request.post('/v1/gen/server/send', data, { params: { id } })
  },

  /**
   * 调整终端大小
   */
  resizeTerminal: (id: string, width: number, height: number) => {
    return request.post('/v1/gen/server/resize', { id, width, height })
  },

  /**
   * 启用服务器监控
   */
  enableMonitoring: (id: string) => {
    return request.post('/v1/gen/server/enable-monitoring', { id })
  },

  /**
   * 停用服务器监控
   */
  disableMonitoring: (id: string) => {
    return request.post('/v1/gen/server/disable-monitoring', { id })
  },

  /**
   * 获取服务器监控状态
   */
  getMonitoringStatus: (id: string) => {
    return request.get('/v1/gen/server/monitoring-status', { params: { id } })
  },

  /**
   * 手动收集服务器指标
   */
  collectMetrics: (id: string) => {
    return request.post('/v1/gen/server/collect-metrics', { id })
  },

  /**
   * 获取服务器列表（按标签分组）
   */
  getServersByTags: () => {
    return request.get('/v1/gen/server/by-tags')
  },

  /**
   * 批量操作服务器
   */
  batchOperation: (data: BatchOperationRequest) => {
    return request.post('/v1/gen/server/batch-operation', data)
  },

  /**
   * 获取服务器统计信息
   */
  getServerStatistics: () => {
    return request.get('/v1/gen/server/statistics')
  },

  /**
   * 导出服务器配置
   */
  exportServerConfig: (ids?: string[]) => {
    return request.post('/v1/gen/server/export', ids)
  },

  /**
   * 导入服务器配置
   */
  importServerConfig: (configData: string) => {
    return request.post('/v1/gen/server/import', configData)
  },

  /**
   * 克隆服务器配置
   */
  cloneServer: (data: CloneServerRequest) => {
    return request.post('/v1/gen/server/clone', data)
  },

  /**
   * 获取Guacamole代理连接URL
   */
  getGuacamoleProxyUrl: (id: string) => {
    return request.get('/v1/gen/server/proxy/guacamole', { params: { id } })
  },

  /**
   * 测试代理连接
   */
  testProxyConnection: (id: string) => {
    return request.post('/v1/gen/server/proxy/test', { id })
  },

  /**
   * 获取服务器支持的数据上报方式
   */
  getSupportedReportMethods: (id: string) => {
    return request.get('/v1/gen/server/report/methods', { params: { id } })
  },

  /**
   * 根据配置查询数据
   */
  queryDataByConfig: (id: string, queryType: string, expression: string, timeRange: Record<string, any>) => {
    return request.post('/v1/gen/server/query/data', timeRange, {
      params: { id, queryType, expression }
    })
  },

  /**
   * 手动检测服务器信息
   */
  detectServerInfo: (id: string) => {
    return request.post('/v1/gen/server/detect-info', { id })
  },

  /**
   * 批量检测服务器信息
   */
  batchDetectServerInfo: () => {
    return request.post('/v1/gen/server/batch-detect-info')
  },

  /**
   * 测试本地IP检测功能
   */
  testLocalIpDetection: (host?: string) => {
    return request.get('/v1/gen/server/test-local-ip', { params: { host } })
  },

  /**
   * 更新服务器数据上报配置
   */
  updateReportConfig: (id: string, config: ReportConfigRequest) => {
    return request.post('/v1/gen/server/report/config', config, { params: { id } })
  },

  /**
   * 获取服务器数据上报配置
   */
  getReportConfig: (id: string) => {
    return request.get('/v1/gen/server/report/config', { params: { id } })
  }
}
