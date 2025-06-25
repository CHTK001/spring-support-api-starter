/**
 * HTTP 请求工具
 * 基于 @repo/utils 的 http 工具进行封装
 */

import { http, type ReturnResult } from '@repo/utils'

/**
 * 请求方法类型
 */
export type RequestMethod = 'get' | 'post' | 'put' | 'delete' | 'patch'

/**
 * 请求配置接口
 */
export interface RequestConfig {
  /** 请求参数 */
  params?: Record<string, any>
  /** 请求体数据 */
  data?: any
  /** 请求头 */
  headers?: Record<string, string>
  /** 超时时间 */
  timeout?: number
}

/**
 * 请求工具类
 */
class RequestUtil {
  /**
   * 发送 GET 请求
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise<ReturnResult<T>>
   */
  get<T = any>(url: string, config?: RequestConfig): Promise<ReturnResult<T>> {
    return http.request<ReturnResult<T>>('get', url, config)
  }

  /**
   * 发送 POST 请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<ReturnResult<T>>
   */
  post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ReturnResult<T>> {
    const requestConfig = { ...config, data }
    return http.request<ReturnResult<T>>('post', url, requestConfig)
  }

  /**
   * 发送 PUT 请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<ReturnResult<T>>
   */
  put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ReturnResult<T>> {
    const requestConfig = { ...config, data }
    return http.request<ReturnResult<T>>('put', url, requestConfig)
  }

  /**
   * 发送 DELETE 请求
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise<ReturnResult<T>>
   */
  delete<T = any>(url: string, config?: RequestConfig): Promise<ReturnResult<T>> {
    return http.request<ReturnResult<T>>('delete', url, config)
  }

  /**
   * 发送 PATCH 请求
   * @param url 请求地址
   * @param data 请求数据
   * @param config 请求配置
   * @returns Promise<ReturnResult<T>>
   */
  patch<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ReturnResult<T>> {
    const requestConfig = { ...config, data }
    return http.request<ReturnResult<T>>('patch', url, requestConfig)
  }

  /**
   * 通用请求方法
   * @param method 请求方法
   * @param url 请求地址
   * @param config 请求配置
   * @returns Promise<ReturnResult<T>>
   */
  request<T = any>(method: RequestMethod, url: string, config?: RequestConfig): Promise<ReturnResult<T>> {
    return http.request<ReturnResult<T>>(method, url, config)
  }
}

/**
 * 导出请求实例
 */
export const request = new RequestUtil()

/**
 * 默认导出
 */
export default request

/**
 * 快捷方法导出
 */
export const { get, post, put, delete: del, patch } = request

/**
 * 响应数据处理工具
 */
export class ResponseUtil {
  /**
   * 检查响应是否成功
   * @param response 响应数据
   * @returns 是否成功
   */
  static isSuccess(response: ReturnResult<any>): boolean {
    return response && response.code === 200
  }

  /**
   * 获取响应数据
   * @param response 响应数据
   * @param defaultValue 默认值
   * @returns 数据或默认值
   */
  static getData<T>(response: ReturnResult<T>, defaultValue: T = null as T): T {
    return this.isSuccess(response) ? response.data : defaultValue
  }

  /**
   * 获取响应消息
   * @param response 响应数据
   * @param defaultMessage 默认消息
   * @returns 消息
   */
  static getMessage(response: ReturnResult<any>, defaultMessage: string = '操作失败'): string {
    return response?.msg || defaultMessage
  }

  /**
   * 获取响应状态码
   * @param response 响应数据
   * @returns 状态码
   */
  static getCode(response: ReturnResult<any>): number {
    return response?.code || 500
  }
}

/**
 * 请求错误处理
 */
export class RequestErrorHandler {
  /**
   * 处理请求错误
   * @param error 错误对象
   * @param showMessage 是否显示错误消息
   */
  static handle(error: any, showMessage: boolean = true): void {
    console.error('请求错误:', error)
    
    if (showMessage) {
      const message = this.getErrorMessage(error)
      // 这里可以集成消息提示组件
      console.warn('错误消息:', message)
    }
  }

  /**
   * 获取错误消息
   * @param error 错误对象
   * @returns 错误消息
   */
  static getErrorMessage(error: any): string {
    if (error?.response?.data?.msg) {
      return error.response.data.msg
    }
    
    if (error?.message) {
      return error.message
    }
    
    if (error?.code) {
      switch (error.code) {
        case 'NETWORK_ERROR':
          return '网络连接失败，请检查网络设置'
        case 'TIMEOUT':
          return '请求超时，请稍后重试'
        case 'CANCELED':
          return '请求已取消'
        default:
          return `请求失败 (${error.code})`
      }
    }
    
    return '未知错误，请稍后重试'
  }
}

/**
 * 请求拦截器配置
 */
export interface RequestInterceptor {
  /** 请求前拦截 */
  onRequest?: (config: RequestConfig) => RequestConfig | Promise<RequestConfig>
  /** 请求错误拦截 */
  onRequestError?: (error: any) => any
  /** 响应拦截 */
  onResponse?: (response: any) => any
  /** 响应错误拦截 */
  onResponseError?: (error: any) => any
}

/**
 * 请求拦截器管理
 */
export class RequestInterceptorManager {
  private static interceptors: RequestInterceptor[] = []

  /**
   * 添加拦截器
   * @param interceptor 拦截器
   */
  static add(interceptor: RequestInterceptor): void {
    this.interceptors.push(interceptor)
  }

  /**
   * 移除拦截器
   * @param interceptor 拦截器
   */
  static remove(interceptor: RequestInterceptor): void {
    const index = this.interceptors.indexOf(interceptor)
    if (index > -1) {
      this.interceptors.splice(index, 1)
    }
  }

  /**
   * 获取所有拦截器
   * @returns 拦截器列表
   */
  static getAll(): RequestInterceptor[] {
    return [...this.interceptors]
  }

  /**
   * 清空所有拦截器
   */
  static clear(): void {
    this.interceptors.length = 0
  }
}
