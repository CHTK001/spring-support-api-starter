/**
 * 请求相关接口定义
 */

/**
 * HTTP 响应接口
 */
export interface IResponse<T = any> {
  /** 状态码 */
  code: number
  /** 响应消息 */
  msg: string
  /** 响应数据 */
  data: T
  /** 响应时间戳 */
  timestamp?: number
  /** 请求ID */
  requestId?: string
  /** 是否成功 */
  success?: boolean
}

/**
 * 分页响应接口
 */
export interface IPageResponse<T = any> extends IResponse<IPageData<T>> {}

/**
 * 分页数据接口
 */
export interface IPageData<T = any> {
  /** 数据列表 */
  records: T[]
  /** 当前页码 */
  current: number
  /** 每页条数 */
  size: number
  /** 总条数 */
  total: number
  /** 总页数 */
  pages: number
  /** 是否有下一页 */
  hasNext?: boolean
  /** 是否有上一页 */
  hasPrevious?: boolean
}

/**
 * 请求配置接口
 */
export interface IRequestConfig {
  /** 请求URL */
  url?: string
  /** 请求方法 */
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS'
  /** 请求头 */
  headers?: Record<string, string>
  /** 查询参数 */
  params?: Record<string, any>
  /** 请求体数据 */
  data?: any
  /** 超时时间（毫秒） */
  timeout?: number
  /** 响应类型 */
  responseType?: 'json' | 'text' | 'blob' | 'arraybuffer' | 'document'
  /** 是否携带凭证 */
  withCredentials?: boolean
  /** 基础URL */
  baseURL?: string
  /** 请求拦截器 */
  transformRequest?: Array<(data: any, headers: Record<string, string>) => any>
  /** 响应拦截器 */
  transformResponse?: Array<(data: any) => any>
  /** 上传进度回调 */
  onUploadProgress?: (progressEvent: ProgressEvent) => void
  /** 下载进度回调 */
  onDownloadProgress?: (progressEvent: ProgressEvent) => void
  /** 取消令牌 */
  cancelToken?: any
  /** 自定义配置 */
  [key: string]: any
}

/**
 * 请求错误接口
 */
export interface IRequestError {
  /** 错误码 */
  code: string | number
  /** 错误消息 */
  message: string
  /** 错误详情 */
  details?: any
  /** 请求配置 */
  config?: IRequestConfig
  /** 响应数据 */
  response?: {
    status: number
    statusText: string
    headers: Record<string, string>
    data: any
  }
  /** 是否为网络错误 */
  isNetworkError?: boolean
  /** 是否为超时错误 */
  isTimeoutError?: boolean
  /** 是否为取消错误 */
  isCancelError?: boolean
}

/**
 * 文件上传配置接口
 */
export interface IUploadConfig extends IRequestConfig {
  /** 文件字段名 */
  name?: string
  /** 文件对象 */
  file?: File | Blob
  /** 文件列表 */
  files?: File[] | FileList
  /** 额外的表单数据 */
  formData?: Record<string, any>
  /** 是否支持多文件上传 */
  multiple?: boolean
  /** 允许的文件类型 */
  accept?: string
  /** 最大文件大小（字节） */
  maxSize?: number
  /** 最大文件数量 */
  maxCount?: number
  /** 上传前的钩子函数 */
  beforeUpload?: (file: File) => boolean | Promise<boolean>
  /** 上传成功的回调 */
  onSuccess?: (response: any, file: File) => void
  /** 上传失败的回调 */
  onError?: (error: IRequestError, file: File) => void
  /** 上传进度的回调 */
  onProgress?: (percent: number, file: File) => void
}

/**
 * 文件下载配置接口
 */
export interface IDownloadConfig extends IRequestConfig {
  /** 下载文件名 */
  filename?: string
  /** 是否自动下载 */
  autoDownload?: boolean
  /** 下载成功的回调 */
  onSuccess?: (blob: Blob, filename: string) => void
  /** 下载失败的回调 */
  onError?: (error: IRequestError) => void
  /** 下载进度的回调 */
  onProgress?: (percent: number) => void
}

/**
 * WebSocket 配置接口
 */
export interface IWebSocketConfig {
  /** WebSocket URL */
  url: string
  /** 连接协议 */
  protocols?: string | string[]
  /** 重连配置 */
  reconnect?: {
    /** 是否自动重连 */
    enabled: boolean
    /** 重连间隔（毫秒） */
    interval: number
    /** 最大重连次数 */
    maxAttempts: number
  }
  /** 心跳配置 */
  heartbeat?: {
    /** 是否启用心跳 */
    enabled: boolean
    /** 心跳间隔（毫秒） */
    interval: number
    /** 心跳消息 */
    message: string
  }
  /** 连接超时时间（毫秒） */
  timeout?: number
  /** 连接成功回调 */
  onOpen?: (event: Event) => void
  /** 消息接收回调 */
  onMessage?: (event: MessageEvent) => void
  /** 连接关闭回调 */
  onClose?: (event: CloseEvent) => void
  /** 连接错误回调 */
  onError?: (event: Event) => void
  /** 重连回调 */
  onReconnect?: (attempt: number) => void
}

/**
 * 缓存配置接口
 */
export interface ICacheConfig {
  /** 缓存键 */
  key: string
  /** 缓存时间（毫秒） */
  ttl?: number
  /** 存储类型 */
  storage?: 'memory' | 'localStorage' | 'sessionStorage' | 'indexedDB'
  /** 是否启用缓存 */
  enabled?: boolean
  /** 缓存策略 */
  strategy?: 'cache-first' | 'network-first' | 'cache-only' | 'network-only'
  /** 缓存更新回调 */
  onUpdate?: (data: any) => void
  /** 缓存过期回调 */
  onExpire?: (key: string) => void
}

/**
 * 请求重试配置接口
 */
export interface IRetryConfig {
  /** 是否启用重试 */
  enabled: boolean
  /** 重试次数 */
  times: number
  /** 重试间隔（毫秒） */
  delay: number
  /** 重试条件 */
  condition?: (error: IRequestError) => boolean
  /** 重试回调 */
  onRetry?: (attempt: number, error: IRequestError) => void
}

/**
 * 请求队列配置接口
 */
export interface IQueueConfig {
  /** 是否启用队列 */
  enabled: boolean
  /** 最大并发数 */
  concurrency: number
  /** 队列超时时间（毫秒） */
  timeout: number
  /** 队列优先级 */
  priority?: number
  /** 队列回调 */
  onQueue?: (config: IRequestConfig) => void
  /** 出队回调 */
  onDequeue?: (config: IRequestConfig) => void
}

/**
 * API 接口基础配置
 */
export interface IApiConfig {
  /** API 基础URL */
  baseURL: string
  /** API 版本 */
  version?: string
  /** 超时时间 */
  timeout: number
  /** 请求头 */
  headers: Record<string, string>
  /** 重试配置 */
  retry?: IRetryConfig
  /** 缓存配置 */
  cache?: ICacheConfig
  /** 队列配置 */
  queue?: IQueueConfig
}

/**
 * 批量请求配置接口
 */
export interface IBatchConfig {
  /** 请求列表 */
  requests: IRequestConfig[]
  /** 是否并发执行 */
  concurrent?: boolean
  /** 最大并发数 */
  maxConcurrency?: number
  /** 是否在出错时停止 */
  stopOnError?: boolean
  /** 批量成功回调 */
  onSuccess?: (responses: IResponse[]) => void
  /** 批量失败回调 */
  onError?: (errors: IRequestError[]) => void
  /** 进度回调 */
  onProgress?: (completed: number, total: number) => void
}
