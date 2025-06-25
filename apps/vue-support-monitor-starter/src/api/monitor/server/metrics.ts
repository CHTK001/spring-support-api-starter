import { request } from '@/utils/request'

/**
 * 服务器指标接口
 */
export interface ServerMetrics {
  /** 指标ID */
  monitorSysGenServerMetricsId?: number
  /** 服务器ID */
  monitorSysGenServerId: number
  /** 指标类型 */
  monitorSysGenServerMetricsType: string
  /** 指标名称 */
  monitorSysGenServerMetricsName: string
  /** 指标值 */
  monitorSysGenServerMetricsValue: number
  /** 指标单位 */
  monitorSysGenServerMetricsUnit?: string
  /** 指标标签 */
  monitorSysGenServerMetricsTags?: string
  /** 指标描述 */
  monitorSysGenServerMetricsDescription?: string
  /** 收集时间 */
  monitorSysGenServerMetricsCollectTime: string
  /** 创建时间 */
  monitorSysGenServerMetricsCreateTime?: string
}

/**
 * 指标查询参数
 */
export interface MetricsQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 服务器ID */
  monitorSysGenServerId?: number
  /** 指标类型 */
  monitorSysGenServerMetricsType?: string
  /** 指标名称 */
  monitorSysGenServerMetricsName?: string
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
  /** 时间范围(分钟) */
  timeRange?: number
}

/**
 * 指标统计信息
 */
export interface MetricsStatistics {
  /** 总指标数 */
  totalMetrics: number
  /** 按类型统计 */
  typeStats: Record<string, number>
  /** 按服务器统计 */
  serverStats: Record<number, number>
  /** 最新收集时间 */
  latestCollectTime: string
  /** 平均值统计 */
  averageStats: Record<string, number>
}

/**
 * 时间序列数据点
 */
export interface TimeSeriesDataPoint {
  /** 时间戳 */
  timestamp: string
  /** 值 */
  value: number
  /** 标签 */
  labels?: Record<string, string>
}

/**
 * 时间序列数据
 */
export interface TimeSeriesData {
  /** 指标名称 */
  metricName: string
  /** 数据点 */
  dataPoints: TimeSeriesDataPoint[]
  /** 单位 */
  unit?: string
}

/**
 * 指标聚合类型
 */
export enum MetricsAggregationType {
  /** 平均值 */
  AVG = 'avg',
  /** 最大值 */
  MAX = 'max',
  /** 最小值 */
  MIN = 'min',
  /** 总和 */
  SUM = 'sum',
  /** 计数 */
  COUNT = 'count'
}

/**
 * 服务器指标管理API
 */
export const serverMetricsApi = {
  /**
   * 分页查询服务器指标
   */
  getServerMetricsPage: (params: MetricsQueryParams) => {
    return request.get('/v1/gen/server/metrics/page', { params })
  },

  /**
   * 根据服务器ID获取最新指标
   */
  getLatestMetrics: (serverId: number) => {
    return request.get(`/v1/gen/server/metrics/latest/${serverId}`)
  },

  /**
   * 根据服务器ID和指标类型获取时间序列数据
   */
  getTimeSeriesData: (serverId: number, metricType: string, params?: {
    startTime?: string
    endTime?: string
    aggregation?: MetricsAggregationType
    interval?: string
  }) => {
    return request.get(`/v1/gen/server/metrics/timeseries/${serverId}/${metricType}`, { params })
  },

  /**
   * 获取多个指标的时间序列数据
   */
  getMultipleTimeSeriesData: (serverId: number, metricTypes: string[], params?: {
    startTime?: string
    endTime?: string
    aggregation?: MetricsAggregationType
    interval?: string
  }) => {
    return request.post(`/v1/gen/server/metrics/timeseries/${serverId}/multiple`, metricTypes, { params })
  },

  /**
   * 获取服务器指标统计
   */
  getMetricsStatistics: (serverId?: number): Promise<MetricsStatistics> => {
    return request.get('/v1/gen/server/metrics/statistics', { params: { serverId } })
  },

  /**
   * 手动收集服务器指标
   */
  collectMetrics: (serverId: number) => {
    return request.post(`/v1/gen/server/metrics/collect/${serverId}`)
  },

  /**
   * 批量收集服务器指标
   */
  batchCollectMetrics: (serverIds: number[]) => {
    return request.post('/v1/gen/server/metrics/batch-collect', serverIds)
  },

  /**
   * 删除过期指标数据
   */
  deleteExpiredMetrics: (serverId: number, days: number) => {
    return request.delete(`/v1/gen/server/metrics/expired/${serverId}`, { params: { days } })
  },

  /**
   * 获取指标类型列表
   */
  getMetricTypes: (serverId?: number) => {
    return request.get('/v1/gen/server/metrics/types', { params: { serverId } })
  },

  /**
   * 获取指标名称列表
   */
  getMetricNames: (serverId?: number, metricType?: string) => {
    return request.get('/v1/gen/server/metrics/names', { params: { serverId, metricType } })
  },

  /**
   * 导出指标数据
   */
  exportMetrics: (params: {
    serverId?: number
    metricTypes?: string[]
    startTime?: string
    endTime?: string
    format?: 'csv' | 'json' | 'excel'
  }) => {
    return request.post('/v1/gen/server/metrics/export', params)
  },

  /**
   * 获取指标告警规则
   */
  getMetricAlertRules: (serverId: number) => {
    return request.get(`/v1/gen/server/metrics/alert-rules/${serverId}`)
  },

  /**
   * 创建指标告警规则
   */
  createMetricAlertRule: (data: {
    serverId: number
    metricType: string
    metricName: string
    operator: string
    threshold: number
    duration: number
    severity: string
    description?: string
  }) => {
    return request.post('/v1/gen/server/metrics/alert-rules', data)
  },

  /**
   * 更新指标告警规则
   */
  updateMetricAlertRule: (ruleId: number, data: any) => {
    return request.put(`/v1/gen/server/metrics/alert-rules/${ruleId}`, data)
  },

  /**
   * 删除指标告警规则
   */
  deleteMetricAlertRule: (ruleId: number) => {
    return request.delete(`/v1/gen/server/metrics/alert-rules/${ruleId}`)
  },

  /**
   * 获取指标趋势分析
   */
  getMetricsTrend: (serverId: number, metricType: string, params?: {
    period?: 'hour' | 'day' | 'week' | 'month'
    comparison?: boolean
  }) => {
    return request.get(`/v1/gen/server/metrics/trend/${serverId}/${metricType}`, { params })
  },

  /**
   * 获取指标异常检测结果
   */
  getMetricsAnomalyDetection: (serverId: number, metricType: string, params?: {
    sensitivity?: 'low' | 'medium' | 'high'
    timeRange?: string
  }) => {
    return request.get(`/v1/gen/server/metrics/anomaly/${serverId}/${metricType}`, { params })
  }
}

/**
 * 指标类型枚举
 */
export enum MetricType {
  /** CPU使用率 */
  CPU_USAGE = 'cpu_usage',
  /** 内存使用率 */
  MEMORY_USAGE = 'memory_usage',
  /** 磁盘使用率 */
  DISK_USAGE = 'disk_usage',
  /** 网络IO */
  NETWORK_IO = 'network_io',
  /** 磁盘IO */
  DISK_IO = 'disk_io',
  /** 负载平均值 */
  LOAD_AVERAGE = 'load_average',
  /** 进程数 */
  PROCESS_COUNT = 'process_count',
  /** 连接数 */
  CONNECTION_COUNT = 'connection_count',
  /** 温度 */
  TEMPERATURE = 'temperature',
  /** 电源 */
  POWER = 'power'
}

/**
 * 指标类型选项
 */
export const metricTypeOptions = [
  { label: 'CPU使用率', value: MetricType.CPU_USAGE, unit: '%' },
  { label: '内存使用率', value: MetricType.MEMORY_USAGE, unit: '%' },
  { label: '磁盘使用率', value: MetricType.DISK_USAGE, unit: '%' },
  { label: '网络IO', value: MetricType.NETWORK_IO, unit: 'MB/s' },
  { label: '磁盘IO', value: MetricType.DISK_IO, unit: 'MB/s' },
  { label: '负载平均值', value: MetricType.LOAD_AVERAGE, unit: '' },
  { label: '进程数', value: MetricType.PROCESS_COUNT, unit: '个' },
  { label: '连接数', value: MetricType.CONNECTION_COUNT, unit: '个' },
  { label: '温度', value: MetricType.TEMPERATURE, unit: '°C' },
  { label: '电源', value: MetricType.POWER, unit: 'W' }
]

/**
 * 聚合类型选项
 */
export const aggregationTypeOptions = [
  { label: '平均值', value: MetricsAggregationType.AVG },
  { label: '最大值', value: MetricsAggregationType.MAX },
  { label: '最小值', value: MetricsAggregationType.MIN },
  { label: '总和', value: MetricsAggregationType.SUM },
  { label: '计数', value: MetricsAggregationType.COUNT }
]

/**
 * 时间间隔选项
 */
export const intervalOptions = [
  { label: '1分钟', value: '1m' },
  { label: '5分钟', value: '5m' },
  { label: '15分钟', value: '15m' },
  { label: '30分钟', value: '30m' },
  { label: '1小时', value: '1h' },
  { label: '6小时', value: '6h' },
  { label: '12小时', value: '12h' },
  { label: '1天', value: '1d' }
]
