import { request } from '@/utils/request'

/**
 * 服务器详情组件接口
 */
export interface ServerDetailComponent {
  /** 组件ID */
  monitorSysGenServerDetailComponentId?: number
  /** 服务器ID */
  monitorSysGenServerId: number
  /** 组件名称 */
  monitorSysGenServerDetailComponentName: string
  /** 组件类型 */
  monitorSysGenServerDetailComponentType: string
  /** 组件配置 */
  monitorSysGenServerDetailComponentConfig?: string
  /** 组件位置X */
  monitorSysGenServerDetailComponentX?: number
  /** 组件位置Y */
  monitorSysGenServerDetailComponentY?: number
  /** 组件宽度 */
  monitorSysGenServerDetailComponentWidth?: number
  /** 组件高度 */
  monitorSysGenServerDetailComponentHeight?: number
  /** 组件层级 */
  monitorSysGenServerDetailComponentZIndex?: number
  /** 是否可见 */
  monitorSysGenServerDetailComponentVisible?: number
  /** 是否启用 */
  monitorSysGenServerDetailComponentEnabled?: number
  /** 刷新间隔(秒) */
  monitorSysGenServerDetailComponentRefreshInterval?: number
  /** 组件样式 */
  monitorSysGenServerDetailComponentStyle?: string
  /** 组件数据源 */
  monitorSysGenServerDetailComponentDataSource?: string
  /** 查询表达式 */
  monitorSysGenServerDetailComponentQueryExpression?: string
  /** 查询类型 */
  monitorSysGenServerDetailComponentQueryType?: string
  /** 创建时间 */
  monitorSysGenServerDetailComponentCreateTime?: string
  /** 更新时间 */
  monitorSysGenServerDetailComponentUpdateTime?: string
}

/**
 * 组件查询参数
 */
export interface ComponentQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 服务器ID */
  monitorSysGenServerId?: number
  /** 组件类型 */
  monitorSysGenServerDetailComponentType?: string
  /** 是否可见 */
  monitorSysGenServerDetailComponentVisible?: number
  /** 是否启用 */
  monitorSysGenServerDetailComponentEnabled?: number
}

/**
 * 组件配置接口
 */
export interface ComponentConfig {
  /** 标题 */
  title?: string
  /** 描述 */
  description?: string
  /** 图表类型 */
  chartType?: string
  /** 颜色配置 */
  colors?: string[]
  /** 阈值配置 */
  thresholds?: Array<{
    value: number
    color: string
    label?: string
  }>
  /** 单位 */
  unit?: string
  /** 小数位数 */
  decimals?: number
  /** 最小值 */
  min?: number
  /** 最大值 */
  max?: number
  /** 是否显示图例 */
  showLegend?: boolean
  /** 是否显示网格 */
  showGrid?: boolean
  /** 是否显示工具提示 */
  showTooltip?: boolean
  /** 自定义样式 */
  customStyle?: Record<string, any>
}

/**
 * 组件布局信息
 */
export interface ComponentLayout {
  /** 组件ID */
  id: number
  /** X坐标 */
  x: number
  /** Y坐标 */
  y: number
  /** 宽度 */
  width: number
  /** 高度 */
  height: number
  /** 层级 */
  zIndex?: number
}

/**
 * 批量更新布局请求
 */
export interface BatchUpdateLayoutRequest {
  /** 服务器ID */
  serverId: number
  /** 组件布局列表 */
  layouts: ComponentLayout[]
}

/**
 * 组件数据查询请求
 */
export interface ComponentDataQueryRequest {
  /** 组件ID */
  componentId: number
  /** 时间范围 */
  timeRange?: {
    start: string
    end: string
  }
  /** 查询参数 */
  queryParams?: Record<string, any>
}

/**
 * 服务器详情组件管理API
 */
export const serverDetailComponentApi = {
  /**
   * 分页查询服务器详情组件
   */
  getComponentPage: (params: ComponentQueryParams) => {
    return request.get('/v1/gen/server/detail-component/page', { params })
  },

  /**
   * 根据服务器ID获取组件列表
   */
  getComponentsByServerId: (serverId: number) => {
    return request.get(`/v1/gen/server/detail-component/server/${serverId}`)
  },

  /**
   * 根据ID获取组件详情
   */
  getComponentById: (id: number) => {
    return request.get(`/v1/gen/server/detail-component/${id}`)
  },

  /**
   * 创建组件
   */
  createComponent: (data: Partial<ServerDetailComponent>) => {
    return request.post('/v1/gen/server/detail-component/save', data)
  },

  /**
   * 更新组件
   */
  updateComponent: (data: Partial<ServerDetailComponent>) => {
    return request.put('/v1/gen/server/detail-component/update', data)
  },

  /**
   * 删除组件
   */
  deleteComponent: (id: number) => {
    return request.delete(`/v1/gen/server/detail-component/delete/${id}`)
  },

  /**
   * 批量删除组件
   */
  batchDeleteComponent: (ids: number[]) => {
    return request.delete('/v1/gen/server/detail-component/batch-delete', { data: ids })
  },

  /**
   * 启用组件
   */
  enableComponent: (id: number) => {
    return request.post(`/v1/gen/server/detail-component/enable/${id}`)
  },

  /**
   * 禁用组件
   */
  disableComponent: (id: number) => {
    return request.post(`/v1/gen/server/detail-component/disable/${id}`)
  },

  /**
   * 显示组件
   */
  showComponent: (id: number) => {
    return request.post(`/v1/gen/server/detail-component/show/${id}`)
  },

  /**
   * 隐藏组件
   */
  hideComponent: (id: number) => {
    return request.post(`/v1/gen/server/detail-component/hide/${id}`)
  },

  /**
   * 更新组件布局
   */
  updateComponentLayout: (id: number, layout: Partial<ComponentLayout>) => {
    return request.put(`/v1/gen/server/detail-component/layout/${id}`, layout)
  },

  /**
   * 批量更新组件布局
   */
  batchUpdateLayout: (data: BatchUpdateLayoutRequest) => {
    return request.put('/v1/gen/server/detail-component/batch-layout', data)
  },

  /**
   * 重置组件布局
   */
  resetComponentLayout: (serverId: number) => {
    return request.post(`/v1/gen/server/detail-component/reset-layout/${serverId}`)
  },

  /**
   * 获取组件数据
   */
  getComponentData: (data: ComponentDataQueryRequest) => {
    return request.post('/v1/gen/server/detail-component/data', data)
  },

  /**
   * 刷新组件数据
   */
  refreshComponentData: (id: number) => {
    return request.post(`/v1/gen/server/detail-component/refresh/${id}`)
  },

  /**
   * 批量刷新组件数据
   */
  batchRefreshComponentData: (serverId: number, componentIds?: number[]) => {
    return request.post(`/v1/gen/server/detail-component/batch-refresh/${serverId}`, { componentIds })
  },

  /**
   * 获取组件类型列表
   */
  getComponentTypes: () => {
    return request.get('/v1/gen/server/detail-component/types')
  },

  /**
   * 获取组件模板
   */
  getComponentTemplates: (type?: string) => {
    return request.get('/v1/gen/server/detail-component/templates', { params: { type } })
  },

  /**
   * 从模板创建组件
   */
  createComponentFromTemplate: (templateId: number, serverId: number, config?: ComponentConfig) => {
    return request.post('/v1/gen/server/detail-component/from-template', { templateId, serverId, config })
  },

  /**
   * 克隆组件
   */
  cloneComponent: (id: number, targetServerId?: number) => {
    return request.post(`/v1/gen/server/detail-component/clone/${id}`, { targetServerId })
  },

  /**
   * 导出组件配置
   */
  exportComponentConfig: (serverId: number, componentIds?: number[]) => {
    return request.post(`/v1/gen/server/detail-component/export/${serverId}`, { componentIds })
  },

  /**
   * 导入组件配置
   */
  importComponentConfig: (serverId: number, configData: string) => {
    return request.post(`/v1/gen/server/detail-component/import/${serverId}`, { configData })
  },

  /**
   * 获取组件预览
   */
  getComponentPreview: (id: number) => {
    return request.get(`/v1/gen/server/detail-component/preview/${id}`)
  },

  /**
   * 验证组件配置
   */
  validateComponentConfig: (type: string, config: ComponentConfig) => {
    return request.post('/v1/gen/server/detail-component/validate', { type, config })
  }
}

/**
 * 组件类型枚举
 */
export enum ComponentType {
  /** 指标图表 */
  METRIC_CHART = 'metric_chart',
  /** 状态指示器 */
  STATUS_INDICATOR = 'status_indicator',
  /** 数值显示 */
  VALUE_DISPLAY = 'value_display',
  /** 进度条 */
  PROGRESS_BAR = 'progress_bar',
  /** 仪表盘 */
  GAUGE = 'gauge',
  /** 表格 */
  TABLE = 'table',
  /** 日志显示 */
  LOG_DISPLAY = 'log_display',
  /** 终端 */
  TERMINAL = 'terminal',
  /** 文件浏览器 */
  FILE_BROWSER = 'file_browser',
  /** 自定义组件 */
  CUSTOM = 'custom'
}

/**
 * 组件类型选项
 */
export const componentTypeOptions = [
  { label: '指标图表', value: ComponentType.METRIC_CHART, icon: 'chart-line' },
  { label: '状态指示器', value: ComponentType.STATUS_INDICATOR, icon: 'status' },
  { label: '数值显示', value: ComponentType.VALUE_DISPLAY, icon: 'number' },
  { label: '进度条', value: ComponentType.PROGRESS_BAR, icon: 'progress' },
  { label: '仪表盘', value: ComponentType.GAUGE, icon: 'gauge' },
  { label: '表格', value: ComponentType.TABLE, icon: 'table' },
  { label: '日志显示', value: ComponentType.LOG_DISPLAY, icon: 'file-text' },
  { label: '终端', value: ComponentType.TERMINAL, icon: 'terminal' },
  { label: '文件浏览器', value: ComponentType.FILE_BROWSER, icon: 'folder' },
  { label: '自定义组件', value: ComponentType.CUSTOM, icon: 'setting' }
]

/**
 * 查询类型枚举
 */
export enum QueryType {
  /** Prometheus查询 */
  PROMETHEUS = 'prometheus',
  /** SQL查询 */
  SQL = 'sql',
  /** API查询 */
  API = 'api',
  /** 静态数据 */
  STATIC = 'static'
}

/**
 * 查询类型选项
 */
export const queryTypeOptions = [
  { label: 'Prometheus查询', value: QueryType.PROMETHEUS },
  { label: 'SQL查询', value: QueryType.SQL },
  { label: 'API查询', value: QueryType.API },
  { label: '静态数据', value: QueryType.STATIC }
]

/**
 * 获取组件类型标签
 */
export const getComponentTypeLabel = (type: string): string => {
  const option = componentTypeOptions.find(item => item.value === type)
  return option?.label || type
}

/**
 * 获取组件类型图标
 */
export const getComponentTypeIcon = (type: string): string => {
  const option = componentTypeOptions.find(item => item.value === type)
  return option?.icon || 'setting'
}

/**
 * 获取查询类型标签
 */
export const getQueryTypeLabel = (type: string): string => {
  const option = queryTypeOptions.find(item => item.value === type)
  return option?.label || type
}
