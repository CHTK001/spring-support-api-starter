import request from '@/utils/request'
import type { 
  ServerScript, 
  ScriptExecution, 
  PageQuery, 
  PageResult,
  Statistics 
} from '@/types'

// 服务器脚本API
export const scriptApi = {
  // 获取脚本列表
  getScriptList(params: PageQuery & {
    scriptName?: string
    scriptType?: string
    category?: string
    createUser?: string
    isTemplate?: number
    isPublic?: number
    status?: number
  }) {
    return request.get<PageResult<ServerScript>>('/v1/server/scripts/page', { params })
  },

  // 获取脚本详情
  getScriptDetail(id: number) {
    return request.get<ServerScript>(`/v1/server/scripts/${id}`)
  },

  // 创建脚本
  createScript(data: Partial<ServerScript>) {
    return request.post<ServerScript>('/v1/server/scripts/create', data)
  },

  // 更新脚本
  updateScript(id: number, data: Partial<ServerScript>) {
    return request.put<ServerScript>(`/v1/server/scripts/${id}`, data)
  },

  // 删除脚本
  deleteScript(id: number) {
    return request.delete<boolean>(`/v1/server/scripts/${id}`)
  },

  // 批量删除脚本
  batchDeleteScripts(ids: number[]) {
    return request.delete<boolean>('/v1/server/scripts/batch', { data: { ids } })
  },

  // 复制脚本
  duplicateScript(id: number) {
    return request.post<ServerScript>(`/v1/server/scripts/${id}/duplicate`)
  },

  // 获取脚本模板
  getScriptTemplates() {
    return request.get<ServerScript[]>('/v1/server/scripts/templates')
  },

  // 获取公开脚本
  getPublicScripts() {
    return request.get<ServerScript[]>('/v1/server/scripts/public')
  },

  // 根据分类获取脚本
  getScriptsByCategory(category: string) {
    return request.get<ServerScript[]>(`/v1/server/scripts/category/${category}`)
  },

  // 根据类型获取脚本
  getScriptsByType(type: string) {
    return request.get<ServerScript[]>(`/v1/server/scripts/type/${type}`)
  },

  // 搜索脚本
  searchScripts(keyword: string, params?: PageQuery) {
    return request.get<PageResult<ServerScript>>('/v1/server/scripts/search', {
      params: { keyword, ...params }
    })
  },

  // 获取脚本统计
  getScriptStatistics() {
    return request.get<Statistics>('/v1/server/scripts/statistics')
  },

  // 获取脚本分类列表
  getScriptCategories() {
    return request.get<string[]>('/v1/server/scripts/categories')
  },

  // 获取脚本类型列表
  getScriptTypes() {
    return request.get<string[]>('/v1/server/scripts/types')
  },

  // 验证脚本语法
  validateScript(data: {
    content: string
    type: string
    language?: string
  }) {
    return request.post<any>('/v1/server/scripts/validate', data)
  },

  // 更新脚本状态
  updateScriptStatus(id: number, status: number) {
    return request.put<boolean>(`/v1/server/scripts/${id}/status`, null, {
      params: { status }
    })
  },

  // 批量更新脚本状态
  batchUpdateScriptStatus(ids: number[], status: number) {
    return request.put<boolean>('/v1/server/scripts/batch/status', {
      ids,
      status
    })
  },

  // 导出脚本
  exportScript(id: number) {
    return request.download(`/v1/server/scripts/${id}/export`, 'script.txt')
  },

  // 导入脚本
  importScript(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.upload<ServerScript>('/v1/server/scripts/import', formData)
  }
}

// 脚本执行API
export const scriptExecutionApi = {
  // 获取执行记录列表
  getExecutionList(params: PageQuery & {
    scriptId?: number
    serverId?: number
    status?: string
    triggerType?: string
    triggerUser?: string
    startTime?: string
    endTime?: string
  }) {
    return request.get<PageResult<ScriptExecution>>('/v1/server/script-executions/page', { params })
  },

  // 获取执行记录详情
  getExecutionDetail(id: number) {
    return request.get<ScriptExecution>(`/v1/server/script-executions/${id}`)
  },

  // 执行脚本
  executeScript(data: {
    scriptId: number
    serverId: number
    parameters?: any
    executionMode?: string
    priority?: number
    timeoutSeconds?: number
  }) {
    return request.post<ScriptExecution>('/v1/server/script-executions/execute', data)
  },

  // 批量执行脚本
  batchExecuteScript(data: {
    scriptId: number
    serverIds: number[]
    parameters?: any
    executionMode?: string
    priority?: number
    timeoutSeconds?: number
  }) {
    return request.post<ScriptExecution[]>('/v1/server/script-executions/batch/execute', data)
  },

  // 停止执行
  stopExecution(id: number) {
    return request.post<boolean>(`/v1/server/script-executions/${id}/stop`)
  },

  // 重试执行
  retryExecution(id: number) {
    return request.post<boolean>(`/v1/server/script-executions/${id}/retry`)
  },

  // 根据脚本ID获取执行记录
  getExecutionsByScriptId(scriptId: number, params?: PageQuery) {
    return request.get<PageResult<ScriptExecution>>(`/v1/server/script-executions/script/${scriptId}`, { params })
  },

  // 根据服务器ID获取执行记录
  getExecutionsByServerId(serverId: number, params?: PageQuery) {
    return request.get<PageResult<ScriptExecution>>(`/v1/server/script-executions/server/${serverId}`, { params })
  },

  // 根据状态获取执行记录
  getExecutionsByStatus(status: string) {
    return request.get<ScriptExecution[]>(`/v1/server/script-executions/status/${status}`)
  },

  // 获取正在执行的记录
  getRunningExecutions() {
    return request.get<ScriptExecution[]>('/v1/server/script-executions/running')
  },

  // 获取失败的执行记录
  getFailedExecutions(limit = 50) {
    return request.get<ScriptExecution[]>('/v1/server/script-executions/failed', {
      params: { limit }
    })
  },

  // 获取最近的执行记录
  getRecentExecutions(limit = 20) {
    return request.get<ScriptExecution[]>('/v1/server/script-executions/recent', {
      params: { limit }
    })
  },

  // 获取执行统计
  getExecutionStatistics(params?: {
    scriptId?: number
    serverId?: number
    startTime?: string
    endTime?: string
  }) {
    return request.get<Statistics>('/v1/server/script-executions/statistics', { params })
  },

  // 获取执行趋势
  getExecutionTrend(params?: {
    scriptId?: number
    serverId?: number
    startTime?: string
    endTime?: string
    interval?: string
  }) {
    return request.get<any[]>('/v1/server/script-executions/trend', { params })
  },

  // 获取执行时间分布
  getExecutionTimeDistribution(params?: {
    scriptId?: number
    serverId?: number
    startTime?: string
    endTime?: string
  }) {
    return request.get<any[]>('/v1/server/script-executions/time-distribution', { params })
  },

  // 获取执行成功率统计
  getExecutionSuccessRate(params?: {
    scriptId?: number
    serverId?: number
    startTime?: string
    endTime?: string
  }) {
    return request.get<any>('/v1/server/script-executions/success-rate', { params })
  },

  // 获取长时间执行的记录
  getLongTimeExecutions(hours = 1) {
    return request.get<ScriptExecution[]>('/v1/server/script-executions/long-time', {
      params: { hours }
    })
  },

  // 获取执行输出
  getExecutionOutput(id: number) {
    return request.get<{
      output: string
      errorOutput: string
    }>(`/v1/server/script-executions/${id}/output`)
  },

  // 获取执行日志
  getExecutionLogs(id: number) {
    return request.get<any[]>(`/v1/server/script-executions/${id}/logs`)
  },

  // 下载执行输出
  downloadExecutionOutput(id: number) {
    return request.download(`/v1/server/script-executions/${id}/output/download`, 'execution-output.txt')
  },

  // 删除过期执行记录
  deleteExpiredExecutions(days = 30) {
    return request.delete<number>('/v1/server/script-executions/expired', {
      params: { days }
    })
  },

  // 清理执行记录
  cleanupExecutions(params: {
    scriptId?: number
    serverId?: number
    status?: string
    days?: number
  }) {
    return request.post<number>('/v1/server/script-executions/cleanup', params)
  },

  // 生成执行报告
  generateExecutionReport(params?: {
    scriptId?: number
    serverId?: number
    startTime?: string
    endTime?: string
    format?: string
  }) {
    return request.get<any>('/v1/server/script-executions/report', { params })
  }
}
