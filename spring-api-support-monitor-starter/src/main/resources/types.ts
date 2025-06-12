export interface SoftService {
  createName: string;
  createBy: number;
  createTime: string;
  updateTime: string;
  updateName: string;
  updateBy: number;
  softServiceId: number;
  softServiceName: string;
  softServiceCategory: string;
  softServiceLogo: string;
  softServiceInstallCommand: string;
  softServiceUninstallCommand: string;
  softServiceStartCommand: string;
  softServiceStopCommand: string;
  softServiceRestartCommand: string;
  softServiceInstalledCommand: string;
  softServiceRemark: string;
  softServiceVersion: string;
  softServiceDownloadUrl: string;
  softServiceTags: string;
  softServiceOs: string;
  sshId: string;
  isInstalled: boolean;
  installedServers?: InstalledServer[]; // 安装这个服务的设备列表
  installCount: number;
  favoriteCount: number;
  versions?: SoftServiceVersion[]; // 软件版本列表
  abstractChannelSession?: string; // 状态命令
  softServiceStatusCheckSuccessFlag?: string; // 服务状态检查成功标识
  
  // 命令类型：0-单行命令，1-脚本
  softServiceInstallCommandType?: number;
  softServiceUninstallCommandType?: number;
  softServiceStartCommandType?: number;
  softServiceStopCommandType?: number;
  softServiceRestartCommandType?: number;
  softServiceStatusCheckCommandType?: number;
  
  // 命令执行成功和失败标识
  softServiceStatusCheckFailureFlag?: string; // 服务状态检查失败标识
  softServiceInstallSuccessFlag?: string;     // 安装成功标识
  softServiceInstallFailureFlag?: string;     // 安装失败标识
  softServiceUninstallSuccessFlag?: string;   // 卸载成功标识
  softServiceUninstallFailureFlag?: string;   // 卸载失败标识
  softServiceStartSuccessFlag?: string;       // 启动成功标识
  softServiceStartFailureFlag?: string;       // 启动失败标识
  softServiceStopSuccessFlag?: string;        // 停止成功标识
  softServiceStopFailureFlag?: string;        // 停止失败标识
  softServiceRestartSuccessFlag?: string;     // 重启成功标识
  softServiceRestartFailureFlag?: string;     // 重启失败标识
  
  // 状态检查命令
  softServiceStatusCheckCommand?: string;     // 状态检查命令
  
  // 是否收藏
  isFavorite?: boolean;                       // 是否收藏
}

/**
 * 软件服务安装的服务器信息
 */
export interface InstalledServer {
  sshId: string;
  sshName: string;
  sshHost: string;
  installId: number;
  installStatus: number;
  installStatusDesc: string;
  runStatus: number;
  runStatusDesc: string;
}

/**
 * 软件服务版本
 */
export interface SoftServiceVersion {
  versionId: number;
  softServiceId: number;
  version: string;
  downloadUrl: string;
  releaseTime: string;
  releaseNotes: string;
}

/**
 * 软件服务安装记录
 */
export interface SoftServiceInstall {
  installId: number;
  softServiceId: number;
  sshId: string;
  installStatus: number; // 0-未安装，1-安装中，2-安装成功，3-安装失败
  installRunStatus: number; // 0-未运行，1-运行中
  installTime: number;
  installPath?: string;
  installVersion?: string;
  installLogPath?: string;
  installLastStartTime?: number;
  installLastStopTime?: number;
  installLastCheckTime?: number;
  
  // 缓存的服务信息
  softServiceName?: string;
  sshName?: string;
  sshHost?: string;
  sshPort?: number;
  
  // 缓存的命令信息
  softServiceInstallCommand?: string;
  softServiceInstallCommandType?: number;
  softServiceUninstallCommand?: string;
  softServiceUninstallCommandType?: number;
  softServiceStartCommand?: string;
  softServiceStartCommandType?: number;
  softServiceStopCommand?: string;
  softServiceStopCommandType?: number;
  softServiceRestartCommand?: string;
  softServiceRestartCommandType?: number;
  softServiceStatusCheckCommand?: string;
  softServiceStatusCheckCommandType?: number;
  
  // 缓存的命令执行成功和失败标识
  softServiceStatusCheckSuccessFlag?: string;
  softServiceStatusCheckFailureFlag?: string;
  softServiceInstallSuccessFlag?: string;
  softServiceInstallFailureFlag?: string;
  softServiceUninstallSuccessFlag?: string;
  softServiceUninstallFailureFlag?: string;
  softServiceStartSuccessFlag?: string;
  softServiceStartFailureFlag?: string;
  softServiceStopSuccessFlag?: string;
  softServiceStopFailureFlag?: string;
  softServiceRestartSuccessFlag?: string;
  softServiceRestartFailureFlag?: string;
}

/**
 * 软件服务状态信息
 */
export interface SoftServiceStatusInfo {
  installId: number;
  softServiceId: number;
  softServiceName: string;
  installStatus: number; // 0-未安装，1-安装中，2-安装成功，3-安装失败
  installStatusDesc: string;
  runStatus: number; // 0-未运行，1-运行中
  runStatusDesc: string;
  installPath?: string;
  exists: boolean;
  message?: string;
  checkTime: number;
}

/**
 * 软件服务日志
 */
export interface SoftServiceLog {
  softServiceLogId?: number;
  softServiceLogInstallId: number;
  softServiceLogServiceId?: number;
  softServiceLogSshId?: string;
  softServiceLogType?: number; // 1-安装，2-卸载，3-启动，4-停止，5-重启，6-状态检查
  softServiceLogContent?: string;
  softServiceLogStatus?: number; // 0-进行中，1-成功，2-失败
  softServiceLogTime?: number;
  softServiceLogFilePath?: string;
  softServiceLogOperationType: string; // install, uninstall, start, stop, restart, status
  softServiceLogServiceName?: string;
  softServiceLogSshName?: string;
  softServiceLogSshHost?: string;
  softServiceLogFileSize?: number;
  softServiceLogDuration?: number;
  softServiceLogCommand?: string;
  softServiceLogError?: string;
  softServiceLogEndTime?: number;
}