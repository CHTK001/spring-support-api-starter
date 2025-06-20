import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import NProgress from 'nprogress'

// 响应数据类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
}

// 分页响应类型
export interface PageResponse<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 请求配置扩展
interface RequestConfig extends AxiosRequestConfig {
  showLoading?: boolean
  showError?: boolean
  loadingText?: string
}

class Request {
  private instance: AxiosInstance
  private loadingInstance: any = null
  
  constructor() {
    this.instance = axios.create({
      baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      }
    })
    
    this.setupInterceptors()
  }
  
  private setupInterceptors() {
    // 请求拦截器
    this.instance.interceptors.request.use(
      (config: any) => {
        NProgress.start()
        
        // 显示加载动画
        if (config.showLoading !== false) {
          this.showLoading(config.loadingText)
        }
        
        // 添加认证token
        const token = localStorage.getItem('token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        
        return config
      },
      (error) => {
        NProgress.done()
        this.hideLoading()
        return Promise.reject(error)
      }
    )
    
    // 响应拦截器
    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        NProgress.done()
        this.hideLoading()
        
        const { data } = response
        
        // 检查业务状态码
        if (data.code === 200 || data.success) {
          return data
        } else {
          // 业务错误
          if (response.config.showError !== false) {
            ElMessage.error(data.message || '请求失败')
          }
          return Promise.reject(new Error(data.message || '请求失败'))
        }
      },
      (error) => {
        NProgress.done()
        this.hideLoading()
        
        let message = '网络错误'
        
        if (error.response) {
          const { status, data } = error.response
          
          switch (status) {
            case 400:
              message = data.message || '请求参数错误'
              break
            case 401:
              message = '未授权，请重新登录'
              // 清除token并跳转到登录页
              localStorage.removeItem('token')
              window.location.href = '/login'
              break
            case 403:
              message = '拒绝访问'
              break
            case 404:
              message = '请求地址不存在'
              break
            case 500:
              message = '服务器内部错误'
              break
            default:
              message = data.message || `连接错误${status}`
          }
        } else if (error.code === 'ECONNABORTED') {
          message = '请求超时'
        } else if (error.message) {
          message = error.message
        }
        
        if (error.config?.showError !== false) {
          ElMessage.error(message)
        }
        
        return Promise.reject(error)
      }
    )
  }
  
  private showLoading(text = '加载中...') {
    this.loadingInstance = ElLoading.service({
      text,
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }
  
  private hideLoading() {
    if (this.loadingInstance) {
      this.loadingInstance.close()
      this.loadingInstance = null
    }
  }
  
  // GET请求
  get<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.get(url, config)
  }
  
  // POST请求
  post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.post(url, data, config)
  }
  
  // PUT请求
  put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.put(url, data, config)
  }
  
  // DELETE请求
  delete<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.delete(url, config)
  }
  
  // 上传文件
  upload<T = any>(url: string, formData: FormData, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.instance.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
  
  // 下载文件
  download(url: string, filename?: string, config?: RequestConfig): Promise<void> {
    return this.instance.get(url, {
      ...config,
      responseType: 'blob'
    }).then((response: any) => {
      const blob = new Blob([response.data])
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    })
  }
}

export const request = new Request()
export default request
