/**
 * 服务配置项接口
 */
export interface ServiceConfig {
  [key: string]: any;
}

/**
 * 服务配置项响应接口
 */
export interface ServiceConfigResponse {
  config: string; // JSON字符串
}
