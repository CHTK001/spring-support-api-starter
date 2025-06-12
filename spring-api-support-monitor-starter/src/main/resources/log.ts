import { http, type ReturnResult } from "./utils";
import { SoftServiceLog, SoftServiceStatusInfo, SoftServiceInstall } from "./types";

/**
 * 安装软件服务
 */
export function fetchSoftServiceInstallLog(data: { installId: string }) {
  return http.request<ReturnResult<boolean>>("get", `/v1/soft/service/install/ssh/${data.installId}`);
}

/**
 * 获取服务操作日志
 */
export function fetchServiceOperationLog(data: { installId: number; operationType: string }) {
  return http.request<ReturnResult<string>>("get", `/v1/soft/service/log/content`, {
    params: {
      installId: data.installId,
      operationType: data.operationType
    }
  });
}

/**
 * 获取服务日志文件列表
 */
export function fetchServiceLogFiles(data: { installId: number }) {
  return http.request<ReturnResult<string[]>>("get", `/v1/soft/service/log/files/${data.installId}`);
}

/**
 * 下载服务操作日志文件
 */
export function downloadServiceOperationLog(data: { installId: number; operationType: string }) {
  const url = `/v1/soft/service/log/download?installId=${data.installId}&operationType=${data.operationType}`;
  window.open(url, '_blank');
  return Promise.resolve();
}

/**
 * 分页查询日志列表
 */
export function fetchServiceLogList(data: { 
  page: number; 
  size: number; 
  installId?: number;
  serviceId?: number;
  sshId?: string;
  type?: number;
  status?: number;
  operationType?: string;
}) {
  return http.request<ReturnResult<{ records: SoftServiceLog[]; total: number }>>("get", `/v1/soft/service/log/page`, {
    params: {
      page: data.page,
      size: data.size,
      "softServiceLog.softServiceLogInstallId": data.installId,
      "softServiceLog.softServiceLogServiceId": data.serviceId,
      "softServiceLog.softServiceLogSshId": data.sshId,
      "softServiceLog.softServiceLogType": data.type,
      "softServiceLog.softServiceLogStatus": data.status,
      "softServiceLog.softServiceLogOperationType": data.operationType
    }
  });
}

/**
 * 根据安装ID查询日志列表
 */
export function fetchLogsByInstallId(data: { installId: number }) {
  return http.request<ReturnResult<SoftServiceLog[]>>("get", `/v1/soft/service/log/list/${data.installId}`);
}

/**
 * 清理指定安装ID的日志
 */
export function cleanLogsByInstallId(data: { installId: number }) {
  return http.request<ReturnResult<boolean>>("delete", `/v1/soft/service/log/clean/${data.installId}`);
}

/**
 * 追加日志内容
 */
export function appendLogContent(data: { installId: number; operationType: string; content: string }) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/log/append`, {
    params: {
      installId: data.installId,
      operationType: data.operationType
    },
    data: data.content
  });
}

/**
 * 检查服务状态
 */
export function checkServiceStatus(data: { installId: number }) {
  return http.request<ReturnResult<SoftServiceStatusInfo>>("get", `/v1/soft/service/install/status/${data.installId}`);
}

/**
 * 批量检查服务状态
 */
export function batchCheckServiceStatus(data: { installIds: number[] }) {
  return http.request<ReturnResult<SoftServiceStatusInfo[]>>("post", `/v1/soft/service/install/status/batch`, {
    data: data.installIds
  });
}

/**
 * 更新服务状态检查命令
 */
export function updateStatusCheckCommand(data: { 
  installId: number; 
  command: string;
  commandType?: number;
  successFlag?: string;
}) {
  return http.request<ReturnResult<boolean>>("put", `/v1/soft/service/updateStatusCheckCommand`, {
    params: {
      installId: data.installId,
      command: data.command,
      commandType: data.commandType || 0,
      successFlag: data.successFlag
    }
  });
}

/**
 * 获取安装记录详情
 */
export function getInstallById(data: { installId: number }) {
  return http.request<ReturnResult<SoftServiceInstall>>("get", `/v1/soft/service/install/${data.installId}`);
}

/**
 * 根据SSH ID查询安装记录列表
 */
export function listInstallBySshId(data: { sshId: string }) {
  return http.request<ReturnResult<SoftServiceInstall[]>>("get", `/v1/soft/service/install/ssh/${data.sshId}`);
}

/**
 * 根据软件服务ID查询安装记录列表
 */
export function listInstallBySoftServiceId(data: { softServiceId: number }) {
  return http.request<ReturnResult<SoftServiceInstall[]>>("get", `/v1/soft/service/install/service/${data.softServiceId}`);
}

/**
 * 开始安装软件服务
 */
export function startInstall(data: SoftServiceInstall) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/install/start`, {
    data
  });
}

/**
 * 停止安装软件服务
 */
export function stopInstall(data: { installId: number }) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/install/stop/${data.installId}`);
}

/**
 * 启动服务
 */
export function startService(data: { installId: number }) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/install/start-service/${data.installId}`);
}

/**
 * 停止服务
 */
export function stopService(data: { installId: number }) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/install/stop-service/${data.installId}`);
}

/**
 * 重启服务
 */
export function restartService(data: { installId: number }) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/install/restart-service/${data.installId}`);
}

/**
 * 卸载服务
 */
export function uninstallService(data: { installId: number }) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/install/uninstall/${data.installId}`);
}

/**
 * 批量安装
 */
export function batchInstall(data: { softServiceId: number; sshIds: string[] }) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/install/batch`, {
    data
  });
}

/**
 * 查看服务日志
 */
export function viewServiceLog(data: { installId: number; lines?: number }) {
  return http.request<ReturnResult<string>>("get", `/v1/soft/service/install/view-log/${data.installId}`, {
    params: {
      lines: data.lines || 100
    }
  });
}

/**
 * 开始监控服务日志
 */
export function startMonitorServiceLog(data: { installId: number }) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/install/start-monitor-log/${data.installId}`);
}

/**
 * 停止监控服务日志
 */
export function stopMonitorServiceLog(data: { installId: number }) {
  return http.request<ReturnResult<boolean>>("post", `/v1/soft/service/install/stop-monitor-log/${data.installId}`);
}
