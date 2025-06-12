/**
 * HTTP请求工具
 */
export const http = {
  request<T>(method: string, url: string, options?: { params?: any; data?: any }): Promise<T> {
    // 这只是一个模拟实现，真正的实现需要依赖具体的HTTP库
    console.log(`HTTP ${method.toUpperCase()} ${url}`, options);
    return Promise.resolve({} as T);
  }
};

/**
 * 标准返回结果类型
 */
export interface ReturnResult<T> {
  code: number;
  msg: string;
  data: T;
  isOk: boolean;
  traceId?: string;
} 