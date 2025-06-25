import { request } from '@/utils/request'

/**
 * 文件上传任务接口
 */
export interface FileUploadTask {
  /** 任务ID */
  monitorSysGenServerFileUploadTaskId?: number
  /** 服务器ID */
  monitorSysGenServerId: number
  /** 本地文件路径 */
  monitorSysGenServerFileUploadTaskLocalPath: string
  /** 远程文件路径 */
  monitorSysGenServerFileUploadTaskRemotePath: string
  /** 文件大小 */
  monitorSysGenServerFileUploadTaskFileSize?: number
  /** 上传状态 */
  monitorSysGenServerFileUploadTaskStatus: number
  /** 上传进度 */
  monitorSysGenServerFileUploadTaskProgress?: number
  /** 错误信息 */
  monitorSysGenServerFileUploadTaskError?: string
  /** 开始时间 */
  monitorSysGenServerFileUploadTaskStartTime?: string
  /** 结束时间 */
  monitorSysGenServerFileUploadTaskEndTime?: string
  /** 创建时间 */
  monitorSysGenServerFileUploadTaskCreateTime?: string
  /** 更新时间 */
  monitorSysGenServerFileUploadTaskUpdateTime?: string
}

/**
 * 文件上传记录接口
 */
export interface FileUploadRecord {
  /** 记录ID */
  monitorSysGenServerFileUploadRecordId?: number
  /** 任务ID */
  monitorSysGenServerFileUploadTaskId: number
  /** 服务器ID */
  monitorSysGenServerId: number
  /** 文件名 */
  monitorSysGenServerFileUploadRecordFileName: string
  /** 文件路径 */
  monitorSysGenServerFileUploadRecordFilePath: string
  /** 文件大小 */
  monitorSysGenServerFileUploadRecordFileSize: number
  /** 上传状态 */
  monitorSysGenServerFileUploadRecordStatus: number
  /** 上传进度 */
  monitorSysGenServerFileUploadRecordProgress?: number
  /** 传输速度 */
  monitorSysGenServerFileUploadRecordSpeed?: number
  /** 错误信息 */
  monitorSysGenServerFileUploadRecordError?: string
  /** 开始时间 */
  monitorSysGenServerFileUploadRecordStartTime?: string
  /** 结束时间 */
  monitorSysGenServerFileUploadRecordEndTime?: string
  /** 创建时间 */
  monitorSysGenServerFileUploadRecordCreateTime?: string
  /** 更新时间 */
  monitorSysGenServerFileUploadRecordUpdateTime?: string
}

/**
 * 文件上传查询参数
 */
export interface FileUploadQueryParams {
  /** 当前页 */
  current?: number
  /** 页大小 */
  size?: number
  /** 服务器ID */
  monitorSysGenServerId?: number
  /** 上传状态 */
  status?: number
  /** 文件名 */
  fileName?: string
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
}

/**
 * 文件上传请求
 */
export interface FileUploadRequest {
  /** 服务器ID */
  serverId: number
  /** 本地文件路径 */
  localPath: string
  /** 远程文件路径 */
  remotePath: string
  /** 是否覆盖 */
  overwrite?: boolean
  /** 文件权限 */
  permissions?: string
}

/**
 * 批量文件上传请求
 */
export interface BatchFileUploadRequest {
  /** 服务器ID */
  serverId: number
  /** 文件列表 */
  files: Array<{
    localPath: string
    remotePath: string
    overwrite?: boolean
    permissions?: string
  }>
}

/**
 * 文件上传统计信息
 */
export interface FileUploadStatistics {
  /** 总任务数 */
  totalTasks: number
  /** 成功任务数 */
  successTasks: number
  /** 失败任务数 */
  failedTasks: number
  /** 进行中任务数 */
  runningTasks: number
  /** 等待中任务数 */
  pendingTasks: number
  /** 总上传大小 */
  totalSize: number
  /** 平均上传速度 */
  averageSpeed: number
  /** 按服务器统计 */
  serverStats: Record<number, number>
}

/**
 * 文件上传管理API
 */
export const fileUploadApi = {
  /**
   * 分页查询文件上传任务
   */
  getFileUploadTaskPage: (params: FileUploadQueryParams) => {
    return request.get('/v1/gen/server/file-upload/task/page', { params })
  },

  /**
   * 分页查询文件上传记录
   */
  getFileUploadRecordPage: (params: FileUploadQueryParams) => {
    return request.get('/v1/gen/server/file-upload/record/page', { params })
  },

  /**
   * 根据服务器ID获取上传任务
   */
  getTasksByServerId: (serverId: number) => {
    return request.get(`/v1/gen/server/file-upload/task/server/${serverId}`)
  },

  /**
   * 根据任务ID获取上传记录
   */
  getRecordsByTaskId: (taskId: number) => {
    return request.get(`/v1/gen/server/file-upload/record/task/${taskId}`)
  },

  /**
   * 创建文件上传任务
   */
  createUploadTask: (data: FileUploadRequest) => {
    return request.post('/v1/gen/server/file-upload/task/create', data)
  },

  /**
   * 批量创建文件上传任务
   */
  batchCreateUploadTask: (data: BatchFileUploadRequest) => {
    return request.post('/v1/gen/server/file-upload/task/batch-create', data)
  },

  /**
   * 开始文件上传
   */
  startUpload: (taskId: number) => {
    return request.post(`/v1/gen/server/file-upload/task/start/${taskId}`)
  },

  /**
   * 暂停文件上传
   */
  pauseUpload: (taskId: number) => {
    return request.post(`/v1/gen/server/file-upload/task/pause/${taskId}`)
  },

  /**
   * 恢复文件上传
   */
  resumeUpload: (taskId: number) => {
    return request.post(`/v1/gen/server/file-upload/task/resume/${taskId}`)
  },

  /**
   * 取消文件上传
   */
  cancelUpload: (taskId: number) => {
    return request.post(`/v1/gen/server/file-upload/task/cancel/${taskId}`)
  },

  /**
   * 重试文件上传
   */
  retryUpload: (taskId: number) => {
    return request.post(`/v1/gen/server/file-upload/task/retry/${taskId}`)
  },

  /**
   * 删除上传任务
   */
  deleteUploadTask: (taskId: number) => {
    return request.delete(`/v1/gen/server/file-upload/task/delete/${taskId}`)
  },

  /**
   * 批量删除上传任务
   */
  batchDeleteUploadTask: (taskIds: number[]) => {
    return request.delete('/v1/gen/server/file-upload/task/batch-delete', { data: taskIds })
  },

  /**
   * 获取上传进度
   */
  getUploadProgress: (taskId: number) => {
    return request.get(`/v1/gen/server/file-upload/task/progress/${taskId}`)
  },

  /**
   * 获取上传统计
   */
  getUploadStatistics: (serverId?: number): Promise<FileUploadStatistics> => {
    return request.get('/v1/gen/server/file-upload/statistics', { params: { serverId } })
  },

  /**
   * 清理完成的上传任务
   */
  cleanCompletedTasks: (serverId?: number, days?: number) => {
    return request.post('/v1/gen/server/file-upload/task/clean', { serverId, days })
  },

  /**
   * 获取服务器文件列表
   */
  getServerFileList: (serverId: number, path: string = '/') => {
    return request.get(`/v1/gen/server/file-upload/files/${serverId}`, { params: { path } })
  },

  /**
   * 创建服务器目录
   */
  createServerDirectory: (serverId: number, path: string, permissions?: string) => {
    return request.post(`/v1/gen/server/file-upload/directory/${serverId}`, { path, permissions })
  },

  /**
   * 删除服务器文件或目录
   */
  deleteServerFile: (serverId: number, path: string) => {
    return request.delete(`/v1/gen/server/file-upload/files/${serverId}`, { params: { path } })
  },

  /**
   * 重命名服务器文件或目录
   */
  renameServerFile: (serverId: number, oldPath: string, newPath: string) => {
    return request.post(`/v1/gen/server/file-upload/rename/${serverId}`, { oldPath, newPath })
  },

  /**
   * 获取文件内容
   */
  getFileContent: (serverId: number, path: string) => {
    return request.get(`/v1/gen/server/file-upload/content/${serverId}`, { params: { path } })
  },

  /**
   * 保存文件内容
   */
  saveFileContent: (serverId: number, path: string, content: string) => {
    return request.post(`/v1/gen/server/file-upload/content/${serverId}`, { path, content })
  }
}

/**
 * 上传状态枚举
 */
export enum UploadStatus {
  /** 等待中 */
  PENDING = 0,
  /** 上传中 */
  UPLOADING = 1,
  /** 已完成 */
  COMPLETED = 2,
  /** 已失败 */
  FAILED = 3,
  /** 已暂停 */
  PAUSED = 4,
  /** 已取消 */
  CANCELLED = 5
}

/**
 * 上传状态选项
 */
export const uploadStatusOptions = [
  { label: '等待中', value: UploadStatus.PENDING, color: 'default' },
  { label: '上传中', value: UploadStatus.UPLOADING, color: 'processing' },
  { label: '已完成', value: UploadStatus.COMPLETED, color: 'success' },
  { label: '已失败', value: UploadStatus.FAILED, color: 'error' },
  { label: '已暂停', value: UploadStatus.PAUSED, color: 'warning' },
  { label: '已取消', value: UploadStatus.CANCELLED, color: 'default' }
]

/**
 * 获取上传状态标签
 */
export const getUploadStatusLabel = (status: number): string => {
  const option = uploadStatusOptions.find(item => item.value === status)
  return option?.label || '未知'
}

/**
 * 获取上传状态颜色
 */
export const getUploadStatusColor = (status: number): string => {
  const option = uploadStatusOptions.find(item => item.value === status)
  return option?.color || 'default'
}

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化传输速度
 */
export const formatTransferSpeed = (bytesPerSecond: number): string => {
  return formatFileSize(bytesPerSecond) + '/s'
}
