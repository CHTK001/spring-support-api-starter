import { http } from "@/utils/http";

// ==================== 类型定义 ====================

/**
 * 服务器文件上传任务
 */
export interface ServerFileUploadTask {
  monitorSysGenServerFileUploadTaskId: number;
  monitorSysGenServerId: number;
  monitorSysGenServerFileUploadTaskName: string;
  monitorSysGenServerFileUploadFileName: string;
  monitorSysGenServerFileUploadFilePath: string;
  monitorSysGenServerFileUploadFileSize: number;
  monitorSysGenServerFileUploadFileMd5: string;
  monitorSysGenServerFileUploadTargetPath: string;
  monitorSysGenServerFileUploadMode: string;
  monitorSysGenServerFileUploadType: string;
  monitorSysGenServerFileUploadStatus: string;
  monitorSysGenServerFileUploadScheduledTime?: string;
  monitorSysGenServerFileUploadPriority: number;
  monitorSysGenServerFileUploadRetryCount: number;
  monitorSysGenServerFileUploadMaxRetry: number;
  monitorSysGenServerFileUploadTimeoutSeconds: number;
  monitorSysGenServerFileUploadOverwriteEnabled: number;
  monitorSysGenServerFileUploadBackupEnabled: number;
  monitorSysGenServerFileUploadCompressEnabled: number;
  monitorSysGenServerFileUploadVerifyEnabled: number;
  monitorSysGenServerFileUploadDescription?: string;
  monitorSysGenServerFileUploadTags?: string;
  monitorSysGenServerFileUploadCreateUser?: string;
  monitorSysGenServerFileUploadCreateTime?: string;
  monitorSysGenServerFileUploadUpdateTime?: string;
  monitorSysGenServerFileUploadStartTime?: string;
  monitorSysGenServerFileUploadEndTime?: string;
  monitorSysGenServerFileUploadTaskStatus: number;
}

/**
 * 服务器文件上传记录
 */
export interface ServerFileUploadRecord {
  monitorSysGenServerFileUploadRecordId: number;
  monitorSysGenServerFileUploadTaskId: number;
  monitorSysGenServerId: number;
  monitorSysGenServerFileUploadTargetPath: string;
  monitorSysGenServerFileUploadStatus: string;
  monitorSysGenServerFileUploadProgress: number;
  monitorSysGenServerFileUploadSpeed: number;
  monitorSysGenServerFileUploadTransferredBytes: number;
  monitorSysGenServerFileUploadTotalBytes: number;
  monitorSysGenServerFileUploadStartTime?: string;
  monitorSysGenServerFileUploadEndTime?: string;
  monitorSysGenServerFileUploadDurationMs: number;
  monitorSysGenServerFileUploadErrorMessage?: string;
  monitorSysGenServerFileUploadErrorCode?: string;
  monitorSysGenServerFileUploadRetryCount: number;
  monitorSysGenServerFileUploadFileMd5Remote?: string;
  monitorSysGenServerFileUploadVerificationStatus: string;
  monitorSysGenServerFileUploadBackupPath?: string;
  monitorSysGenServerFileUploadConnectionInfo?: string;
  monitorSysGenServerFileUploadExtraData?: string;
  monitorSysGenServerFileUploadCreateTime?: string;
  monitorSysGenServerFileUploadUpdateTime?: string;
  monitorSysGenServerFileUploadRecordStatus: number;
}

/**
 * 服务器文件上传请求参数
 */
export interface ServerFileUploadRequest {
  serverId: number;
  taskName: string;
  targetPath: string;
  uploadMode?: string;
  scheduledTime?: string;
  priority?: number;
  overwrite?: boolean;
  backup?: boolean;
  compress?: boolean;
  verify?: boolean;
  description?: string;
  tags?: string;
  timeoutSeconds?: number;
  maxRetry?: number;
}

/**
 * 分页查询参数
 */
export interface ServerFileUploadTaskPageParams {
  current: number;
  size: number;
  serverId?: number;
  taskName?: string;
  status?: string;
  mode?: string;
  createUser?: string;
  startTime?: string;
  endTime?: string;
}

// ==================== 常量和枚举 ====================

/**
 * 上传模式枚举
 */
export const UPLOAD_MODE = {
  REALTIME: "REALTIME",
  SCHEDULED: "SCHEDULED",
} as const;

export type UploadMode = typeof UPLOAD_MODE[keyof typeof UPLOAD_MODE];

/**
 * 上传类型枚举
 */
export const UPLOAD_TYPE = {
  SINGLE: "SINGLE",
  BATCH: "BATCH",
} as const;

export type UploadType = typeof UPLOAD_TYPE[keyof typeof UPLOAD_TYPE];

/**
 * 任务状态枚举
 */
export const TASK_STATUS = {
  PENDING: "PENDING",
  PROCESSING: "PROCESSING",
  COMPLETED: "COMPLETED",
  FAILED: "FAILED",
  CANCELLED: "CANCELLED",
} as const;

export type TaskStatus = typeof TASK_STATUS[keyof typeof TASK_STATUS];

/**
 * 上传状态枚举
 */
export const UPLOAD_STATUS = {
  PENDING: "PENDING",
  UPLOADING: "UPLOADING",
  COMPLETED: "COMPLETED",
  FAILED: "FAILED",
  CANCELLED: "CANCELLED",
} as const;

export type UploadStatus = typeof UPLOAD_STATUS[keyof typeof UPLOAD_STATUS];

// ==================== API 接口 ====================

/**
 * 创建上传任务
 * @param file 上传文件
 * @param request 上传请求参数
 * @returns 创建结果
 */
export function createServerFileUploadTask(file: File, request: ServerFileUploadRequest) {
  const formData = new FormData();
  formData.append("file", file);
  
  // 添加其他参数
  Object.keys(request).forEach(key => {
    const value = request[key as keyof ServerFileUploadRequest];
    if (value !== undefined && value !== null) {
      formData.append(key, String(value));
    }
  });

  return http.request<ServerFileUploadTask>("post", "/api/monitor/server/file-upload/upload", {
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

/**
 * 分页查询上传任务
 * @param params 查询参数
 * @returns 分页结果
 */
export function getServerFileUploadTaskPage(params: ServerFileUploadTaskPageParams) {
  return http.request<{
    records: ServerFileUploadTask[];
    total: number;
    current: number;
    size: number;
  }>("get", "/api/monitor/server/file-upload/page", {
    params,
  });
}

/**
 * 根据ID查询任务
 * @param taskId 任务ID
 * @returns 任务信息
 */
export function getServerFileUploadTaskById(taskId: number) {
  return http.request<ServerFileUploadTask>("get", `/api/monitor/server/file-upload/${taskId}`);
}

/**
 * 根据服务器ID查询当前活跃任务
 * @param serverId 服务器ID
 * @returns 活跃任务
 */
export function getActiveTaskByServerId(serverId: number) {
  return http.request<ServerFileUploadTask>("get", `/api/monitor/server/file-upload/server/${serverId}/active`);
}

/**
 * 启动上传任务
 * @param taskId 任务ID
 * @returns 启动结果
 */
export function startServerFileUploadTask(taskId: number) {
  return http.request<boolean>("post", `/api/monitor/server/file-upload/${taskId}/start`);
}

/**
 * 取消上传任务
 * @param taskId 任务ID
 * @returns 取消结果
 */
export function cancelServerFileUploadTask(taskId: number) {
  return http.request<boolean>("post", `/api/monitor/server/file-upload/${taskId}/cancel`);
}

/**
 * 重试上传任务
 * @param taskId 任务ID
 * @returns 重试结果
 */
export function retryServerFileUploadTask(taskId: number) {
  return http.request<boolean>("post", `/api/monitor/server/file-upload/${taskId}/retry`);
}

/**
 * 删除上传任务
 * @param taskId 任务ID
 * @returns 删除结果
 */
export function deleteServerFileUploadTask(taskId: number) {
  return http.request<boolean>("delete", `/api/monitor/server/file-upload/${taskId}`);
}

/**
 * 根据状态查询任务
 * @param status 任务状态
 * @returns 任务列表
 */
export function getServerFileUploadTasksByStatus(status: string) {
  return http.request<ServerFileUploadTask[]>("get", `/api/monitor/server/file-upload/status/${status}`);
}

/**
 * 查询待处理的定时任务
 * @returns 定时任务列表
 */
export function getPendingScheduledTasks() {
  return http.request<ServerFileUploadTask[]>("get", "/api/monitor/server/file-upload/scheduled/pending");
}

/**
 * 查询可重试的失败任务
 * @returns 可重试任务列表
 */
export function getRetryableTasks() {
  return http.request<ServerFileUploadTask[]>("get", "/api/monitor/server/file-upload/retryable");
}

/**
 * 获取任务统计信息
 * @param serverId 服务器ID（可选）
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @returns 统计信息
 */
export function getServerFileUploadTaskStatistics(serverId?: number, startTime?: string, endTime?: string) {
  return http.request<Record<string, any>>("get", "/api/monitor/server/file-upload/statistics", {
    params: { serverId, startTime, endTime },
  });
}

/**
 * 验证服务器是否支持上传
 * @param serverId 服务器ID
 * @returns 验证结果
 */
export function validateServerForUpload(serverId: number) {
  return http.request<boolean>("get", `/api/monitor/server/file-upload/server/${serverId}/validate`);
}

/**
 * 获取任务队列状态
 * @returns 队列状态
 */
export function getTaskQueueStatus() {
  return http.request<Record<string, any>>("get", "/api/monitor/server/file-upload/queue/status");
}

/**
 * 优化任务队列
 * @returns 优化结果
 */
export function optimizeTaskQueue() {
  return http.request<Record<string, any>>("post", "/api/monitor/server/file-upload/queue/optimize");
}

/**
 * 批量取消任务
 * @param taskIds 任务ID列表
 * @returns 取消结果
 */
export function batchCancelTasks(taskIds: number[]) {
  return http.request<Record<string, any>>("post", "/api/monitor/server/file-upload/batch/cancel", {
    data: taskIds,
  });
}

/**
 * 批量重试任务
 * @param taskIds 任务ID列表
 * @returns 重试结果
 */
export function batchRetryTasks(taskIds: number[]) {
  return http.request<Record<string, any>>("post", "/api/monitor/server/file-upload/batch/retry", {
    data: taskIds,
  });
}
