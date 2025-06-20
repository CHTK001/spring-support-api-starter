import request from '@/utils/request'
import type { 
  FileUploadTask, 
  FileUploadRecord, 
  PageQuery, 
  PageResult,
  Statistics 
} from '@/types'

// 文件上传任务API
export const uploadTaskApi = {
  // 获取上传任务列表
  getTaskList(params: PageQuery & {
    taskName?: string
    status?: string
    mode?: string
    type?: string
    createUser?: string
    startTime?: string
    endTime?: string
  }) {
    return request.get<PageResult<FileUploadTask>>('/v1/file-upload/tasks/page', { params })
  },

  // 创建上传任务
  createTask(data: Partial<FileUploadTask>) {
    return request.post<FileUploadTask>('/v1/file-upload/tasks/create', data)
  },

  // 批量创建上传任务
  createBatchTasks(tasks: Partial<FileUploadTask>[]) {
    return request.post<FileUploadTask[]>('/v1/file-upload/tasks/batch/create', tasks)
  },

  // 启动上传任务
  startTask(taskId: number) {
    return request.post<boolean>(`/v1/file-upload/tasks/${taskId}/start`)
  },

  // 暂停上传任务
  pauseTask(taskId: number) {
    return request.post<boolean>(`/v1/file-upload/tasks/${taskId}/pause`)
  },

  // 取消上传任务
  cancelTask(taskId: number) {
    return request.post<boolean>(`/v1/file-upload/tasks/${taskId}/cancel`)
  },

  // 重试上传任务
  retryTask(taskId: number) {
    return request.post<boolean>(`/v1/file-upload/tasks/${taskId}/retry`)
  },

  // 批量操作任务
  batchOperateTasks(taskIds: number[], operation: string) {
    return request.post<any>('/v1/file-upload/tasks/batch/operate', {
      taskIds,
      operation
    })
  },

  // 根据状态查询任务
  getTasksByStatus(status: string) {
    return request.get<FileUploadTask[]>(`/v1/file-upload/tasks/status/${status}`)
  },

  // 查询待处理的定时任务
  getPendingScheduledTasks() {
    return request.get<FileUploadTask[]>('/v1/file-upload/tasks/scheduled/pending')
  },

  // 查询可重试的失败任务
  getRetryableTasks() {
    return request.get<FileUploadTask[]>('/v1/file-upload/tasks/retryable')
  },

  // 查询超时任务
  getTimeoutTasks(timeoutMinutes = 30) {
    return request.get<FileUploadTask[]>('/v1/file-upload/tasks/timeout', {
      params: { timeoutMinutes }
    })
  },

  // 获取任务统计信息
  getTaskStatistics(params?: {
    startTime?: string
    endTime?: string
  }) {
    return request.get<Statistics>('/v1/file-upload/tasks/statistics', { params })
  },

  // 根据用户查询任务统计
  getUserTaskStatistics(createUser: string, params?: {
    startTime?: string
    endTime?: string
  }) {
    return request.get<Statistics>('/v1/file-upload/tasks/statistics/user', {
      params: { createUser, ...params }
    })
  },

  // 查询热门文件类型
  getPopularFileTypes(limit = 10) {
    return request.get<any[]>('/v1/file-upload/tasks/statistics/file-types', {
      params: { limit }
    })
  },

  // 查询大文件任务
  getLargeFileTasks(minSizeMB = 100, limit = 20) {
    return request.get<FileUploadTask[]>('/v1/file-upload/tasks/large-files', {
      params: { minSizeMB, limit }
    })
  },

  // 查询最近的任务
  getRecentTasks(limit = 20) {
    return request.get<FileUploadTask[]>('/v1/file-upload/tasks/recent', {
      params: { limit }
    })
  },

  // 根据文件MD5查询任务
  getTasksByFileMd5(fileMd5: string) {
    return request.get<FileUploadTask[]>(`/v1/file-upload/tasks/md5/${fileMd5}`)
  },

  // 查询队列中的任务数量
  getQueueTaskCount() {
    return request.get<number>('/v1/file-upload/tasks/queue/count')
  },

  // 查询今日任务统计
  getTodayTaskStatistics() {
    return request.get<Statistics>('/v1/file-upload/tasks/statistics/today')
  },

  // 验证任务参数
  validateTask(task: Partial<FileUploadTask>) {
    return request.post<any>('/v1/file-upload/tasks/validate', task)
  },

  // 复制任务
  duplicateTask(taskId: number) {
    return request.post<FileUploadTask>(`/v1/file-upload/tasks/${taskId}/duplicate`)
  },

  // 获取任务执行报告
  getTaskExecutionReport(taskId: number) {
    return request.get<any>(`/v1/file-upload/tasks/${taskId}/report`)
  },

  // 获取任务队列状态
  getTaskQueueStatus() {
    return request.get<any>('/v1/file-upload/tasks/queue/status')
  },

  // 优化任务队列
  optimizeTaskQueue() {
    return request.post<any>('/v1/file-upload/tasks/queue/optimize')
  },

  // 删除过期任务
  deleteExpiredTasks(days = 30) {
    return request.delete<number>('/v1/file-upload/tasks/expired', {
      params: { days }
    })
  }
}

// 文件上传记录API
export const uploadRecordApi = {
  // 获取上传记录列表
  getRecordList(params: PageQuery & {
    taskId?: number
    serverId?: number
    status?: string
    startTime?: string
    endTime?: string
  }) {
    return request.get<PageResult<FileUploadRecord>>('/v1/file-upload/records/page', { params })
  },

  // 根据任务ID查询上传记录
  getRecordsByTaskId(taskId: number) {
    return request.get<FileUploadRecord[]>(`/v1/file-upload/records/task/${taskId}`)
  },

  // 根据服务器ID查询上传记录
  getRecordsByServerId(serverId: number, limit = 50) {
    return request.get<FileUploadRecord[]>(`/v1/file-upload/records/server/${serverId}`, {
      params: { limit }
    })
  },

  // 根据状态查询上传记录
  getRecordsByStatus(status: string) {
    return request.get<FileUploadRecord[]>(`/v1/file-upload/records/status/${status}`)
  },

  // 查询正在上传的记录
  getUploadingRecords() {
    return request.get<FileUploadRecord[]>('/v1/file-upload/records/uploading')
  },

  // 查询失败的上传记录
  getFailedRecords(limit = 50) {
    return request.get<FileUploadRecord[]>('/v1/file-upload/records/failed', {
      params: { limit }
    })
  },

  // 创建上传记录
  createRecord(record: Partial<FileUploadRecord>) {
    return request.post<FileUploadRecord>('/v1/file-upload/records/create', record)
  },

  // 批量创建上传记录
  createBatchRecords(records: Partial<FileUploadRecord>[]) {
    return request.post<FileUploadRecord[]>('/v1/file-upload/records/batch/create', records)
  },

  // 更新上传进度
  updateProgress(recordId: number, progress: number, transferredBytes: number, speed: number) {
    return request.put<boolean>(`/v1/file-upload/records/${recordId}/progress`, null, {
      params: { progress, transferredBytes, speed }
    })
  },

  // 更新上传状态
  updateStatus(recordId: number, status: string, errorMessage?: string, errorCode?: string) {
    return request.put<boolean>(`/v1/file-upload/records/${recordId}/status`, null, {
      params: { status, errorMessage, errorCode }
    })
  },

  // 更新验证状态
  updateVerificationStatus(recordId: number, verificationStatus: string, remoteMd5?: string) {
    return request.put<boolean>(`/v1/file-upload/records/${recordId}/verification`, null, {
      params: { verificationStatus, remoteMd5 }
    })
  },

  // 取消上传记录
  cancelRecord(recordId: number) {
    return request.post<boolean>(`/v1/file-upload/records/${recordId}/cancel`)
  },

  // 重试上传记录
  retryRecord(recordId: number) {
    return request.post<boolean>(`/v1/file-upload/records/${recordId}/retry`)
  },

  // 验证文件完整性
  verifyFileIntegrity(recordId: number) {
    return request.post<boolean>(`/v1/file-upload/records/${recordId}/verify`)
  },

  // 获取任务的上传统计
  getTaskUploadStatistics(taskId: number) {
    return request.get<Statistics>(`/v1/file-upload/records/statistics/task/${taskId}`)
  },

  // 获取服务器上传统计
  getServerUploadStatistics(serverId: number, params?: {
    startTime?: string
    endTime?: string
  }) {
    return request.get<Statistics>(`/v1/file-upload/records/statistics/server/${serverId}`, { params })
  },

  // 获取上传速度排行
  getUploadSpeedRanking(limit = 10) {
    return request.get<any[]>('/v1/file-upload/records/statistics/speed-ranking', {
      params: { limit }
    })
  },

  // 获取上传时间分布
  getUploadTimeDistribution(params?: {
    startTime?: string
    endTime?: string
  }) {
    return request.get<any[]>('/v1/file-upload/records/statistics/time-distribution', { params })
  },

  // 查询长时间上传的记录
  getLongTimeUploadingRecords(hours = 2) {
    return request.get<FileUploadRecord[]>('/v1/file-upload/records/long-time-uploading', {
      params: { hours }
    })
  },

  // 删除过期记录
  deleteExpiredRecords(days = 30) {
    return request.delete<number>('/v1/file-upload/records/expired', {
      params: { days }
    })
  },

  // 获取上传进度详情
  getUploadProgressDetails(taskId: number) {
    return request.get<any>(`/v1/file-upload/records/progress/${taskId}`)
  },

  // 生成上传报告
  generateUploadReport(taskId: number) {
    return request.get<any>(`/v1/file-upload/records/report/${taskId}`)
  }
}
