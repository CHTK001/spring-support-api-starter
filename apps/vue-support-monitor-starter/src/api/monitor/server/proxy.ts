import { request } from '@/utils/request'

/**
 * 服务器代理关联接口
 */
export interface ServerProxy {
  /** 服务器代理关联ID */
  monitorSysGenServerProxyId?: number
  /** 服务器ID */
  monitorSysGenServerId: number
  /** 代理ID */
  proxyId: number
  /** 代理类型 */
  monitorSysGenServerProxyType: string
  /** 代理配置 */
  monitorSysGenServerProxyConfig?: string
  /** 是否启用 0:否 1:是 */
  monitorSysGenServerProxyEnabled?: number
  /** 代理状态 0:离线 1:在线 2:连接中 3:连接失败 */
  monitorSysGenServerProxyStatus?: number
  /** 最后连接时间 */
  monitorSysGenServerProxyLastConnectTime?: string
  /** 连接失败原因 */
  monitorSysGenServerProxyConnectionError?: string
  /** 代理URL */
  monitorSysGenServerProxyUrl?: string
  /** 备注 */
  monitorSysGenServerProxyRemark?: string
  /** 创建时间 */
  monitorSysGenServerProxyCreateTime?: string
  /** 更新时间 */
  monitorSysGenServerProxyUpdateTime?: string
}

/**
 * 服务器代理查询参数
 */
export interface ServerProxyQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 服务器ID */
  monitorSysGenServerId?: number
  /** 代理ID */
  proxyId?: number
  /** 代理类型 */
  monitorSysGenServerProxyType?: string
  /** 是否启用 */
  monitorSysGenServerProxyEnabled?: number
  /** 代理状态 */
  monitorSysGenServerProxyStatus?: number
}

/**
 * 批量更新状态请求
 */
export interface BatchUpdateStatusRequest {
  /** 关联ID列表 */
  ids: number[]
  /** 状态 */
  status: number
}

/**
 * 克隆配置请求
 */
export interface CloneConfigRequest {
  /** 源服务器ID */
  sourceServerId: number
  /** 目标服务器ID */
  targetServerId: number
}

/**
 * 代理统计信息
 */
export interface ProxyStatistics {
  /** 总数 */
  totalCount: number
  /** 按状态统计 */
  statusCount: Record<number, number>
  /** 按类型统计 */
  typeCount: Record<string, number>
  /** 启用/禁用统计 */
  enabledCount: Record<number, number>
}

/**
 * 服务器代理管理API
 */
export const serverProxyApi = {
  /**
   * 分页查询服务器代理关联
   */
  getServerProxyPage: (params: ServerProxyQueryParams) => {
    return request.get('/v1/gen/server/proxy/page', { params })
  },

  /**
   * 根据服务器ID查询代理关联列表
   */
  getByServerId: (serverId: number) => {
    return request.get(`/v1/gen/server/proxy/server/${serverId}`)
  },

  /**
   * 根据代理ID查询服务器关联列表
   */
  getByProxyId: (proxyId: number) => {
    return request.get(`/v1/gen/server/proxy/proxy/${proxyId}`)
  },

  /**
   * 根据服务器ID和代理类型查询代理关联
   */
  getByServerIdAndProxyType: (serverId: number, proxyType: string) => {
    return request.get(`/v1/gen/server/proxy/server/${serverId}/type/${proxyType}`)
  },

  /**
   * 创建服务器代理关联
   */
  createServerProxy: (data: Partial<ServerProxy>) => {
    return request.post('/v1/gen/server/proxy/save', data)
  },

  /**
   * 更新服务器代理关联
   */
  updateServerProxy: (data: Partial<ServerProxy>) => {
    return request.put('/v1/gen/server/proxy/update', data)
  },

  /**
   * 删除服务器代理关联
   */
  deleteServerProxy: (id: number) => {
    return request.delete(`/v1/gen/server/proxy/delete/${id}`)
  },

  /**
   * 批量删除服务器代理关联
   */
  batchDeleteServerProxy: (ids: number[]) => {
    return request.delete('/v1/gen/server/proxy/batch-delete', { data: ids })
  },

  /**
   * 测试代理连接
   */
  testProxyConnection: (id: number) => {
    return request.post(`/v1/gen/server/proxy/test/${id}`)
  },

  /**
   * 启用代理
   */
  enableProxy: (id: number) => {
    return request.post(`/v1/gen/server/proxy/enable/${id}`)
  },

  /**
   * 禁用代理
   */
  disableProxy: (id: number) => {
    return request.post(`/v1/gen/server/proxy/disable/${id}`)
  },

  /**
   * 获取代理连接URL
   */
  getProxyUrl: (id: number) => {
    return request.get(`/v1/gen/server/proxy/url/${id}`)
  },

  /**
   * 批量更新代理状态
   */
  batchUpdateStatus: (data: BatchUpdateStatusRequest) => {
    return request.post('/v1/gen/server/proxy/batch-update-status', data)
  },

  /**
   * 获取代理统计信息
   */
  getProxyStatistics: (): Promise<ProxyStatistics> => {
    return request.get('/v1/gen/server/proxy/statistics')
  },

  /**
   * 获取服务器的所有代理类型
   */
  getServerProxyTypes: (serverId: number) => {
    return request.get(`/v1/gen/server/proxy/server/${serverId}/types`)
  },

  /**
   * 克隆服务器代理配置
   */
  cloneServerProxyConfig: (data: CloneConfigRequest) => {
    return request.post('/v1/gen/server/proxy/clone', data)
  }
}

/**
 * 代理类型枚举
 */
export enum ProxyType {
  /** Guacamole代理 */
  GUACAMOLE = 'GUACAMOLE',
  /** VNC代理 */
  VNC = 'VNC',
  /** RDP代理 */
  RDP = 'RDP',
  /** SSH代理 */
  SSH = 'SSH',
  /** HTTP代理 */
  HTTP = 'HTTP',
  /** HTTPS代理 */
  HTTPS = 'HTTPS'
}

/**
 * 代理状态枚举
 */
export enum ProxyStatus {
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
 * 代理启用状态枚举
 */
export enum ProxyEnabled {
  /** 禁用 */
  DISABLED = 0,
  /** 启用 */
  ENABLED = 1
}

/**
 * 代理类型选项
 */
export const proxyTypeOptions = [
  { label: 'Guacamole代理', value: ProxyType.GUACAMOLE },
  { label: 'VNC代理', value: ProxyType.VNC },
  { label: 'RDP代理', value: ProxyType.RDP },
  { label: 'SSH代理', value: ProxyType.SSH },
  { label: 'HTTP代理', value: ProxyType.HTTP },
  { label: 'HTTPS代理', value: ProxyType.HTTPS }
]

/**
 * 代理状态选项
 */
export const proxyStatusOptions = [
  { label: '离线', value: ProxyStatus.OFFLINE, color: 'default' },
  { label: '在线', value: ProxyStatus.ONLINE, color: 'success' },
  { label: '连接中', value: ProxyStatus.CONNECTING, color: 'processing' },
  { label: '连接失败', value: ProxyStatus.FAILED, color: 'error' }
]

/**
 * 代理启用状态选项
 */
export const proxyEnabledOptions = [
  { label: '禁用', value: ProxyEnabled.DISABLED, color: 'default' },
  { label: '启用', value: ProxyEnabled.ENABLED, color: 'success' }
]
