import request from '@/utils/request'

// WebSocket消息类型常量
export const SERVER_WS_MESSAGE_TYPE = {
  SERVER_CREATED: 'server_created',
  SERVER_UPDATED: 'server_updated', 
  SERVER_DELETED: 'server_deleted',
  CONNECTION_STATUS: 'connection_status',
  MONITORING_ENABLED: 'monitoring_enabled',
  MONITORING_DISABLED: 'monitoring_disabled',
  TEST_CONNECTION: 'test_connection'
} as const

// 服务器管理API
export const serverApi = {
  // 分页查询服务器
  page(params: any) {
    return request.get('/monitor/api/v1/gen/server/page', { params })
  },

  // 获取服务器详情
  getById(id: string) {
    return request.get(`/monitor/api/v1/gen/server/${id}`)
  },

  // 创建服务器
  save(data: any) {
    return request.post('/monitor/api/v1/gen/server', data)
  },

  // 更新服务器
  updateById(id: string, data: any) {
    return request.put(`/monitor/api/v1/gen/server/${id}`, data)
  },

  // 删除服务器
  removeById(id: string) {
    return request.delete(`/monitor/api/v1/gen/server/${id}`)
  },

  // 批量删除
  removeBatchByIds(ids: string[]) {
    return request.delete('/monitor/api/v1/gen/server/batch', { data: { ids } })
  },

  // 测试连接
  testConnection(id: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/test-connection`)
  },

  // 连接服务器
  connect(id: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/connect`)
  },

  // 断开连接
  disconnect(id: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/disconnect`)
  },

  // 获取连接状态
  getConnectionStatus(id: string) {
    return request.get(`/monitor/api/v1/gen/server/${id}/connection-status`)
  },

  // 执行命令
  executeCommand(id: string, command: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/execute-command`, { command })
  },

  // 获取服务器信息
  getServerInfo(id: string) {
    return request.get(`/monitor/api/v1/gen/server/${id}/server-info`)
  },

  // 发送数据
  sendData(id: string, data: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/send-data`, { data })
  },

  // 调整终端大小
  resizeTerminal(id: string, width: number, height: number) {
    return request.post(`/monitor/api/v1/gen/server/${id}/resize-terminal`, { width, height })
  },

  // 列出文件
  listFiles(id: string, path?: string) {
    return request.get(`/monitor/api/v1/gen/server/${id}/list-files`, { params: { path } })
  },

  // 上传文件
  uploadFile(id: string, localPath: string, remotePath: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/upload-file`, { localPath, remotePath })
  },

  // 下载文件
  downloadFile(id: string, remotePath: string, localPath: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/download-file`, { remotePath, localPath })
  },

  // 启用监控
  enableMonitoring(id: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/enable-monitoring`)
  },

  // 停用监控
  disableMonitoring(id: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/disable-monitoring`)
  },

  // 获取监控状态
  getMonitoringStatus(id: string) {
    return request.get(`/monitor/api/v1/gen/server/${id}/monitoring-status`)
  },

  // 收集指标
  collectMetrics(id: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/collect-metrics`)
  },

  // 按标签分组获取服务器
  getServersByTags() {
    return request.get('/monitor/api/v1/gen/server/servers-by-tags')
  },

  // 批量操作
  batchOperation(ids: string[], action: string) {
    return request.post('/monitor/api/v1/gen/server/batch-operation', { ids, action })
  },

  // 获取服务器统计
  getServerStatistics() {
    return request.get('/monitor/api/v1/gen/server/server-statistics')
  },

  // 导出服务器配置
  exportServerConfig(ids?: string[]) {
    return request.post('/monitor/api/v1/gen/server/export-config', { ids })
  },

  // 导入服务器配置
  importServerConfig(configData: string) {
    return request.post('/monitor/api/v1/gen/server/import-config', { configData })
  },

  // 克隆服务器
  cloneServer(sourceId: string, targetName: string) {
    return request.post('/monitor/api/v1/gen/server/clone', { sourceId, targetName })
  },

  // 更新连接状态
  updateConnectionStatus(id: string, status: number, errorMessage?: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/update-connection-status`, { status, errorMessage })
  },

  // 批量测试连接
  batchTestConnection(ids: string[]) {
    return request.post('/monitor/api/v1/gen/server/batch-test-connection', { ids })
  },

  // 获取所有连接状态
  getAllConnectionStatus() {
    return request.get('/monitor/api/v1/gen/server/all-connection-status')
  },

  // 获取Guacamole代理URL
  getGuacamoleProxyUrl(id: string) {
    return request.get(`/monitor/api/v1/gen/server/${id}/guacamole-proxy-url`)
  },

  // 测试代理连接
  testProxyConnection(id: string) {
    return request.post(`/monitor/api/v1/gen/server/${id}/test-proxy-connection`)
  },

  // 获取支持的上报方式
  getSupportedReportMethods(id: string) {
    return request.get(`/monitor/api/v1/gen/server/${id}/supported-report-methods`)
  }
}

// 服务器详情组件API
export const saveServerDetailComponent = (data: any) => {
  return request.post('/monitor/api/v1/gen/server/detail-component', data)
}

// 验证组件表达式
export const validateComponentExpression = (expression: string) => {
  return request.post('/monitor/api/v1/gen/server/validate-expression', { expression })
}

// 默认导出
export default serverApi
