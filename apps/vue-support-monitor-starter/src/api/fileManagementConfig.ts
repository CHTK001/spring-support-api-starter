import { http } from "@repo/utils";

/**
 * 文件管理模式
 */
export interface FileManagementMode {
  code: string;
  name: string;
  description: string;
  enabled: boolean;
  requiresApiConfig: boolean;
  requiresNodeClient: boolean;
  requiresSshConfig: boolean;
  defaultTimeout: number;
  defaultMaxRetries: number;
}

/**
 * 文件管理配置
 */
export interface FileManagementConfig {
  serverId: number;
  serverName: string;
  isLocalServer: boolean;
  enabled: boolean;
  mode: string;
  modeName: string;
  modeDescription: string;
  apiConfig?: any;
  nodeClient?: string;
  timeout: number;
  maxRetries: number;
  supportedOperations: string[];
  capabilities: {
    supportsRealtimeOperations: boolean;
    supportsLargeFileTransfer: boolean;
    supportsPermissionManagement: boolean;
    supportsArchiveOperations: boolean;
    supportsFileSearch: boolean;
    supportsFilePreview: boolean;
  };
}

/**
 * NODE客户端信息
 */
export interface NodeClient {
  id: string;
  name: string;
  address: string;
  online: boolean;
  lastSeen?: string;
  version?: string;
}

/**
 * 配置验证结果
 */
export interface ConfigValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
}

/**
 * 获取文件管理配置
 */
export function getFileManagementConfig(serverId: number) {
  return http.get<FileManagementConfig>(`/api/file-management-config/${serverId}`);
}

/**
 * 获取可用的文件管理模式
 */
export function getAvailableFileManagementModes(serverId?: number) {
  const params = serverId ? { serverId } : {};
  return http.get<{
    modes: Record<string, FileManagementMode>;
    isLocalServer: boolean;
  }>("/api/file-management-config/modes", { params });
}

/**
 * 测试文件管理连接
 */
export function testFileManagementConnection(serverId: number) {
  return http.post<boolean>(`/api/file-management-config/${serverId}/test`);
}

/**
 * 获取NODE客户端列表
 */
export function getNodeClients(serverId: number) {
  return http.get<NodeClient[]>(`/api/file-management-config/${serverId}/node-clients`);
}

/**
 * 验证文件管理配置
 */
export function validateFileManagementConfig(serverId: number, config: any) {
  return http.post<ConfigValidationResult>(
    `/api/file-management-config/${serverId}/validate`,
    config
  );
}

/**
 * 文件管理模式常量
 */
export const FILE_MANAGEMENT_MODES = {
  NONE: "NONE",
  LOCAL: "LOCAL", 
  SSH: "SSH",
  NODE: "NODE",
  API: "API"
} as const;

/**
 * 文件管理模式显示名称
 */
export const FILE_MANAGEMENT_MODE_NAMES = {
  [FILE_MANAGEMENT_MODES.NONE]: "不启用",
  [FILE_MANAGEMENT_MODES.LOCAL]: "本地连接",
  [FILE_MANAGEMENT_MODES.SSH]: "SSH连接",
  [FILE_MANAGEMENT_MODES.NODE]: "NODE客户端",
  [FILE_MANAGEMENT_MODES.API]: "API接口"
} as const;

/**
 * 文件管理模式描述
 */
export const FILE_MANAGEMENT_MODE_DESCRIPTIONS = {
  [FILE_MANAGEMENT_MODES.NONE]: "不提供文件管理功能",
  [FILE_MANAGEMENT_MODES.LOCAL]: "直接访问本地文件系统，仅适用于本地服务器",
  [FILE_MANAGEMENT_MODES.SSH]: "通过SSH协议直接访问服务器文件系统",
  [FILE_MANAGEMENT_MODES.NODE]: "通过在线客户端代理进行文件管理",
  [FILE_MANAGEMENT_MODES.API]: "通过HTTP API接口进行文件管理"
} as const;

/**
 * 获取文件管理模式显示名称
 */
export function getFileManagementModeName(mode: string): string {
  return FILE_MANAGEMENT_MODE_NAMES[mode as keyof typeof FILE_MANAGEMENT_MODE_NAMES] || mode;
}

/**
 * 获取文件管理模式描述
 */
export function getFileManagementModeDescription(mode: string): string {
  return FILE_MANAGEMENT_MODE_DESCRIPTIONS[mode as keyof typeof FILE_MANAGEMENT_MODE_DESCRIPTIONS] || "";
}

/**
 * 判断是否需要API配置
 */
export function requiresApiConfig(mode: string): boolean {
  return mode === FILE_MANAGEMENT_MODES.API;
}

/**
 * 判断是否需要NODE客户端选择
 */
export function requiresNodeClient(mode: string): boolean {
  return mode === FILE_MANAGEMENT_MODES.NODE;
}

/**
 * 判断是否需要SSH配置
 */
export function requiresSshConfig(mode: string): boolean {
  return mode === FILE_MANAGEMENT_MODES.SSH;
}

/**
 * 判断是否为本地模式
 */
export function isLocalMode(mode: string): boolean {
  return mode === FILE_MANAGEMENT_MODES.LOCAL;
}

/**
 * 判断是否启用文件管理
 */
export function isFileManagementEnabled(mode: string): boolean {
  return mode !== FILE_MANAGEMENT_MODES.NONE;
}

/**
 * 获取默认的API配置
 */
export function getDefaultApiConfig() {
  return {
    apiHost: "localhost",
    apiPort: 8080,
    basePath: "/api/file",
    useHttps: false,
    authType: "NONE",
    connectionTimeout: 30,
    readTimeout: 60,
    maxRetries: 3,
    sslVerificationEnabled: true
  };
}

/**
 * 获取默认的文件管理配置
 */
export function getDefaultFileManagementConfig() {
  return {
    monitorSysGenServerSettingFileManagementEnabled: 0,
    monitorSysGenServerSettingFileManagementMode: FILE_MANAGEMENT_MODES.NONE,
    monitorSysGenServerSettingFileManagementApiConfig: "",
    monitorSysGenServerSettingFileManagementNodeClient: "",
    monitorSysGenServerSettingFileManagementTimeout: 60,
    monitorSysGenServerSettingFileManagementMaxRetries: 3,
    monitorSysGenServerSettingClientHealthCheckInterval: 30,
    monitorSysGenServerSettingClientHealthTimeout: 10
  };
}
