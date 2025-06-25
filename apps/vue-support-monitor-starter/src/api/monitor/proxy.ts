import { request } from '@/utils/request'

/**
 * 代理配置接口
 */
export interface ProxyConfig {
  /** 代理ID */
  proxyId?: number
  /** 代理名称 */
  proxyName: string
  /** 代理协议 */
  proxyProtocol: string
  /** 代理主机 */
  proxyHost: string
  /** 代理端口 */
  proxyPort: number
  /** 代理用户名 */
  proxyUsername?: string
  /** 代理密码 */
  proxyPassword?: string
  /** 代理描述 */
  proxyDescription?: string
  /** 代理配置参数 */
  proxyConfig?: string
  /** 是否启用 0:否 1:是 */
  proxyEnabled?: number
  /** 代理状态 0:离线 1:在线 2:连接中 3:连接失败 */
  proxyStatus?: number
  /** 最后连接时间 */
  proxyLastConnectTime?: string
  /** 连接失败原因 */
  proxyConnectionError?: string
  /** 创建时间 */
  proxyCreateTime?: string
  /** 更新时间 */
  proxyUpdateTime?: string
}

/**
 * 代理查询参数
 */
export interface ProxyQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 代理名称 */
  proxyName?: string
  /** 代理协议 */
  proxyProtocol?: string
  /** 代理主机 */
  proxyHost?: string
  /** 是否启用 */
  proxyEnabled?: number
  /** 代理状态 */
  proxyStatus?: number
}

/**
 * 代理选项接口（用于下拉选择）
 */
export interface ProxyOption {
  /** 代理ID */
  value: number
  /** 显示标签 */
  label: string
  /** 代理协议 */
  protocol: string
  /** 代理地址 */
  address: string
  /** 是否启用 */
  enabled: boolean
  /** 代理状态 */
  status: number
  /** 扩展信息 */
  extra?: Record<string, any>
}

/**
 * 代理连接测试结果
 */
export interface ProxyTestResult {
  /** 是否成功 */
  success: boolean
  /** 响应时间(毫秒) */
  responseTime?: number
  /** 错误信息 */
  error?: string
  /** 测试时间 */
  testTime: string
}

/**
 * 代理统计信息
 */
export interface ProxyStatistics {
  /** 总代理数 */
  totalProxies: number
  /** 在线代理数 */
  onlineProxies: number
  /** 离线代理数 */
  offlineProxies: number
  /** 按协议统计 */
  protocolStats: Record<string, number>
  /** 按状态统计 */
  statusStats: Record<number, number>
}

/**
 * 代理管理API
 */
export const proxyApi = {
  /**
   * 分页查询代理列表
   */
  getProxyPage: (params: ProxyQueryParams) => {
    return request.get('/v1/proxy/page', { params })
  },

  /**
   * 获取所有可用代理列表（用于选择）
   */
  getAvailableProxies: (protocol?: string) => {
    return request.get('/v1/proxy/available', { params: { protocol } })
  },

  /**
   * 获取代理选项列表（格式化为选择器选项）
   */
  getProxyOptions: (params?: { protocol?: string; enabled?: boolean }): Promise<ProxyOption[]> => {
    return request.get('/v1/proxy/options', { params })
  },

  /**
   * 根据ID获取代理详情
   */
  getProxyById: (id: number) => {
    return request.get(`/v1/proxy/${id}`)
  },

  /**
   * 创建代理
   */
  createProxy: (data: Partial<ProxyConfig>) => {
    return request.post('/v1/proxy/save', data)
  },

  /**
   * 更新代理
   */
  updateProxy: (data: Partial<ProxyConfig>) => {
    return request.put('/v1/proxy/update', data)
  },

  /**
   * 删除代理
   */
  deleteProxy: (id: number) => {
    return request.delete(`/v1/proxy/delete/${id}`)
  },

  /**
   * 批量删除代理
   */
  batchDeleteProxy: (ids: number[]) => {
    return request.delete('/v1/proxy/batch-delete', { data: ids })
  },

  /**
   * 启用代理
   */
  enableProxy: (id: number) => {
    return request.post(`/v1/proxy/enable/${id}`)
  },

  /**
   * 禁用代理
   */
  disableProxy: (id: number) => {
    return request.post(`/v1/proxy/disable/${id}`)
  },

  /**
   * 测试代理连接
   */
  testProxyConnection: (id: number): Promise<ProxyTestResult> => {
    return request.post(`/v1/proxy/test/${id}`)
  },

  /**
   * 批量测试代理连接
   */
  batchTestProxyConnection: (ids: number[]) => {
    return request.post('/v1/proxy/batch-test', ids)
  },

  /**
   * 获取代理统计信息
   */
  getProxyStatistics: (): Promise<ProxyStatistics> => {
    return request.get('/v1/proxy/statistics')
  },

  /**
   * 获取支持的代理协议列表
   */
  getSupportedProtocols: () => {
    return request.get('/v1/proxy/protocols')
  },

  /**
   * 根据协议获取代理列表
   */
  getProxiesByProtocol: (protocol: string) => {
    return request.get(`/v1/proxy/protocol/${protocol}`)
  },

  /**
   * 获取代理配置模板
   */
  getProxyConfigTemplate: (protocol: string) => {
    return request.get(`/v1/proxy/template/${protocol}`)
  },

  /**
   * 验证代理配置
   */
  validateProxyConfig: (data: Partial<ProxyConfig>) => {
    return request.post('/v1/proxy/validate', data)
  },

  /**
   * 克隆代理配置
   */
  cloneProxy: (id: number, newName: string) => {
    return request.post(`/v1/proxy/clone/${id}`, { newName })
  },

  /**
   * 导出代理配置
   */
  exportProxyConfig: (ids?: number[]) => {
    return request.post('/v1/proxy/export', ids)
  },

  /**
   * 导入代理配置
   */
  importProxyConfig: (configData: string) => {
    return request.post('/v1/proxy/import', { configData })
  }
}

/**
 * 代理协议枚举
 */
export enum ProxyProtocol {
  /** Guacamole代理 */
  GUACAMOLE = 'GUACAMOLE',
  /** HTTP代理 */
  HTTP = 'HTTP',
  /** HTTPS代理 */
  HTTPS = 'HTTPS',
  /** SOCKS5代理 */
  SOCKS5 = 'SOCKS5',
  /** VNC代理 */
  VNC = 'VNC',
  /** RDP代理 */
  RDP = 'RDP',
  /** SSH代理 */
  SSH = 'SSH'
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
 * 代理协议选项
 */
export const proxyProtocolOptions = [
  { label: 'Guacamole代理', value: ProxyProtocol.GUACAMOLE, description: '支持VNC、RDP、SSH等多协议' },
  { label: 'HTTP代理', value: ProxyProtocol.HTTP, description: 'HTTP协议代理' },
  { label: 'HTTPS代理', value: ProxyProtocol.HTTPS, description: 'HTTPS协议代理' },
  { label: 'SOCKS5代理', value: ProxyProtocol.SOCKS5, description: 'SOCKS5协议代理' },
  { label: 'VNC代理', value: ProxyProtocol.VNC, description: 'VNC远程桌面代理' },
  { label: 'RDP代理', value: ProxyProtocol.RDP, description: 'RDP远程桌面代理' },
  { label: 'SSH代理', value: ProxyProtocol.SSH, description: 'SSH隧道代理' }
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

/**
 * 获取代理协议标签
 */
export const getProxyProtocolLabel = (protocol: string): string => {
  const option = proxyProtocolOptions.find(item => item.value === protocol)
  return option?.label || protocol
}

/**
 * 获取代理协议描述
 */
export const getProxyProtocolDescription = (protocol: string): string => {
  const option = proxyProtocolOptions.find(item => item.value === protocol)
  return option?.description || ''
}

/**
 * 获取代理状态标签
 */
export const getProxyStatusLabel = (status: number): string => {
  const option = proxyStatusOptions.find(item => item.value === status)
  return option?.label || '未知'
}

/**
 * 获取代理状态颜色
 */
export const getProxyStatusColor = (status: number): string => {
  const option = proxyStatusOptions.find(item => item.value === status)
  return option?.color || 'default'
}

/**
 * 格式化代理地址
 */
export const formatProxyAddress = (proxy: ProxyConfig): string => {
  return `${proxy.proxyHost}:${proxy.proxyPort}`
}

/**
 * 格式化代理显示标签
 */
export const formatProxyLabel = (proxy: ProxyConfig): string => {
  return `${proxy.proxyName} (${proxy.proxyProtocol} - ${formatProxyAddress(proxy)})`
}

/**
 * 将代理配置转换为选项格式
 */
export const convertToProxyOption = (proxy: ProxyConfig): ProxyOption => {
  return {
    value: proxy.proxyId!,
    label: formatProxyLabel(proxy),
    protocol: proxy.proxyProtocol,
    address: formatProxyAddress(proxy),
    enabled: proxy.proxyEnabled === 1,
    status: proxy.proxyStatus || 0,
    extra: {
      description: proxy.proxyDescription,
      lastConnectTime: proxy.proxyLastConnectTime,
      connectionError: proxy.proxyConnectionError
    }
  }
}
