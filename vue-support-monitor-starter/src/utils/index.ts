import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'
import duration from 'dayjs/plugin/duration'

dayjs.locale('zh-cn')
dayjs.extend(relativeTime)
dayjs.extend(duration)

/**
 * 格式化日期时间
 */
export const formatDateTime = (date: string | Date | number, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return '-'
  return dayjs(date).format(format)
}

/**
 * 格式化相对时间
 */
export const formatRelativeTime = (date: string | Date | number) => {
  if (!date) return '-'
  return dayjs(date).fromNow()
}

/**
 * 格式化持续时间
 */
export const formatDuration = (ms: number) => {
  if (!ms || ms < 0) return '-'
  
  const duration = dayjs.duration(ms)
  const days = duration.days()
  const hours = duration.hours()
  const minutes = duration.minutes()
  const seconds = duration.seconds()
  
  if (days > 0) {
    return `${days}天${hours}小时${minutes}分钟`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟${seconds}秒`
  } else if (minutes > 0) {
    return `${minutes}分钟${seconds}秒`
  } else {
    return `${seconds}秒`
  }
}

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes: number) => {
  if (!bytes || bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化数字
 */
export const formatNumber = (num: number, precision = 2) => {
  if (num === null || num === undefined) return '-'
  
  if (num >= 1000000) {
    return (num / 1000000).toFixed(precision) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(precision) + 'K'
  } else {
    return num.toString()
  }
}

/**
 * 格式化百分比
 */
export const formatPercent = (value: number, total: number, precision = 1) => {
  if (!total || total === 0) return '0%'
  return ((value / total) * 100).toFixed(precision) + '%'
}

/**
 * 获取状态颜色
 */
export const getStatusColor = (status: string) => {
  const statusMap: Record<string, string> = {
    // 连接状态
    'ONLINE': 'success',
    'OFFLINE': 'danger',
    'UNKNOWN': 'info',
    
    // 任务状态
    'PENDING': 'info',
    'PROCESSING': 'warning',
    'RUNNING': 'warning',
    'COMPLETED': 'success',
    'FAILED': 'danger',
    'CANCELLED': 'info',
    'TIMEOUT': 'danger',
    
    // 上传状态
    'UPLOADING': 'warning',
    
    // 日志级别
    'DEBUG': 'info',
    'INFO': 'primary',
    'WARN': 'warning',
    'ERROR': 'danger',
    'FATAL': 'danger'
  }
  
  return statusMap[status] || 'info'
}

/**
 * 获取状态文本
 */
export const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    // 连接状态
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'UNKNOWN': '未知',
    
    // 任务状态
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'RUNNING': '执行中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'CANCELLED': '已取消',
    'TIMEOUT': '超时',
    
    // 上传状态
    'UPLOADING': '上传中',
    
    // 脚本类型
    'SHELL': 'Shell脚本',
    'PYTHON': 'Python脚本',
    'POWERSHELL': 'PowerShell脚本',
    'BATCH': '批处理脚本',
    
    // 执行模式
    'SYNC': '同步',
    'ASYNC': '异步',
    
    // 触发类型
    'MANUAL': '手动',
    'SCHEDULED': '定时',
    'API': 'API调用',
    'EVENT': '事件触发'
  }
  
  return statusMap[status] || status
}

/**
 * 防抖函数
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout
  
  return (...args: Parameters<T>) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => func.apply(this, args), wait)
  }
}

/**
 * 节流函数
 */
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle: boolean
  
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 深拷贝
 */
export const deepClone = <T>(obj: T): T => {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime()) as any
  if (obj instanceof Array) return obj.map(item => deepClone(item)) as any
  if (typeof obj === 'object') {
    const clonedObj = {} as any
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
  return obj
}

/**
 * 生成唯一ID
 */
export const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

/**
 * 下载文件
 */
export const downloadFile = (url: string, filename: string) => {
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

/**
 * 复制到剪贴板
 */
export const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('复制成功')
  } catch (err) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = text
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('复制成功')
  }
}

/**
 * 验证IP地址
 */
export const isValidIP = (ip: string) => {
  const ipRegex = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
  return ipRegex.test(ip)
}

/**
 * 验证端口号
 */
export const isValidPort = (port: number | string) => {
  const portNum = typeof port === 'string' ? parseInt(port) : port
  return portNum >= 1 && portNum <= 65535
}
