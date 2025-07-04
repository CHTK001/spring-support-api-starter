# FileUploadQueueTask 迁移指南

## 概述

本文档描述了将 `FileUploadQueueTask` 功能迁移到 `ServerTaskManager` 统一任务管理系统的完整过程。

## 迁移目标

1. **统一任务管理**：将文件上传相关任务集成到 ServerTaskManager 中
2. **配置驱动**：通过服务器配置控制任务执行
3. **提高性能**：使用虚拟线程技术
4. **增强可维护性**：遵循项目架构规范

## 已完成的工作

### 1. 后端迁移

#### 1.1 删除原有文件
- ✅ 删除 `FileUploadQueueTask.java` 类
- ✅ 移除相关的定时任务配置

#### 1.2 ServerTaskManager 增强
- ✅ 添加文件上传队列处理任务（每10秒执行）
- ✅ 添加文件上传状态检查任务（每30秒执行）
- ✅ 添加文件上传临时文件清理任务（每小时执行）
- ✅ 实现完整的队列处理逻辑

#### 1.3 实体类扩展
在 `MonitorSysGenServerSetting` 中添加了以下配置字段：

**文件上传配置**
- `monitorSysGenServerSettingFileUploadEnabled` - 是否启用文件上传队列处理
- `monitorSysGenServerSettingFileUploadInterval` - 队列处理间隔（秒）
- `monitorSysGenServerSettingFileUploadStatusCheckInterval` - 状态检查间隔（秒）
- `monitorSysGenServerSettingFileUploadCleanupInterval` - 临时文件清理间隔（小时）
- `monitorSysGenServerSettingFileUploadTimeout` - 上传超时时间（分钟）
- `monitorSysGenServerSettingFileUploadMaxRetries` - 最大重试次数

**任务调度配置**
- `monitorSysGenServerSettingPortCheckInterval` - 端口检查间隔
- `monitorSysGenServerSettingOnlineCheckEnabled` - 是否启用在线状态检查
- `monitorSysGenServerSettingOnlineCheckInterval` - 在线状态检查间隔
- `monitorSysGenServerSettingLatencyCheckEnabled` - 是否启用延迟检测
- `monitorSysGenServerSettingLatencyCheckInterval` - 延迟检测间隔
- `monitorSysGenServerSettingLogCleanupEnabled` - 是否启用日志清理
- `monitorSysGenServerSettingTempFileCleanupEnabled` - 是否启用临时文件清理
- `monitorSysGenServerSettingTempFileRetentionHours` - 临时文件保留时间
- `monitorSysGenServerSettingWebSocketCleanupEnabled` - 是否启用WebSocket清理
- `monitorSysGenServerSettingWebSocketCleanupInterval` - WebSocket清理间隔

**Prometheus 配置**
- `monitorSysGenServerSettingPrometheusUrl` - Prometheus服务器URL
- `monitorSysGenServerSettingPrometheusQueryPath` - Prometheus查询路径
- `monitorSysGenServerSettingPrometheusAuthEnabled` - 是否启用Prometheus认证
- `monitorSysGenServerSettingPrometheusUsername` - Prometheus用户名
- `monitorSysGenServerSettingPrometheusPassword` - Prometheus密码
- `monitorSysGenServerSettingPrometheusTimeout` - Prometheus连接超时时间
- `monitorSysGenServerSettingPrometheusLabels` - Prometheus标签

### 2. 前端适配

#### 2.1 配置界面更新
- ✅ 在服务器设置表单中添加文件上传配置选项
- ✅ 提供启用/禁用开关和时间间隔配置
- ✅ 添加详细的帮助提示和合理的默认值

#### 2.2 默认值配置
```javascript
// 文件上传配置默认值
monitorSysGenServerSettingFileUploadEnabled: 1,
monitorSysGenServerSettingFileUploadInterval: 10,
monitorSysGenServerSettingFileUploadStatusCheckInterval: 30,
monitorSysGenServerSettingFileUploadCleanupInterval: 1,
monitorSysGenServerSettingFileUploadTimeout: 30,
monitorSysGenServerSettingFileUploadMaxRetries: 3,
```

### 3. 编译错误修复
- ✅ 修复 ServerTaskManager 中引用不存在字段的编译错误
- ✅ 修复 PrometheusDataCollectorService 中的编译错误
- ✅ 确保所有相关组件正常编译

## 数据库迁移

### 执行步骤

1. **备份数据库**
```bash
mysqldump -u username -p database_name > backup_$(date +%Y%m%d_%H%M%S).sql
```

2. **验证当前表结构**
```sql
source sql/migration/verify_database_compatibility.sql
```

3. **执行迁移脚本**
```sql
source sql/migration/add_server_setting_fields.sql
```

4. **验证迁移结果**
```sql
-- 检查新字段是否添加成功
DESCRIBE monitor_sys_gen_server_setting;
```

### 迁移文件
- `sql/migration/add_server_setting_fields.sql` - 数据库迁移脚本
- `sql/migration/verify_database_compatibility.sql` - 兼容性验证脚本

## 部署指南

### 1. 部署前准备
- 确保数据库备份完成
- 验证测试环境迁移成功
- 准备回滚方案

### 2. 部署步骤
1. 停止应用服务
2. 执行数据库迁移脚本
3. 部署新版本代码
4. 启动应用服务
5. 验证功能正常

### 3. 验证清单
- [ ] 文件上传队列处理正常
- [ ] 配置界面显示正确
- [ ] 任务调度按配置执行
- [ ] 日志输出正常
- [ ] 性能指标正常

## 配置说明

### 默认配置
所有新字段都有合理的默认值，确保向后兼容：
- 文件上传功能默认启用
- 处理间隔设置为10秒
- 状态检查间隔设置为30秒
- 临时文件清理间隔设置为1小时

### 性能调优建议
1. **处理间隔**：根据文件上传频率调整处理间隔
2. **并发控制**：监控虚拟线程使用情况
3. **内存使用**：关注队列大小和内存占用
4. **磁盘空间**：定期清理临时文件

## 故障排除

### 常见问题

1. **任务不执行**
   - 检查配置是否启用
   - 查看日志输出
   - 验证服务器状态

2. **性能问题**
   - 调整处理间隔
   - 检查队列积压情况
   - 监控系统资源

3. **配置不生效**
   - 重启应用服务
   - 检查配置缓存
   - 验证数据库更新

### 日志监控
关键日志关键字：
- `文件上传队列处理`
- `文件上传状态检查`
- `清理上传临时文件`
- `ServerTaskManager`

## 回滚方案

如果迁移出现问题，可以按以下步骤回滚：

1. **停止应用服务**
2. **恢复数据库备份**
3. **部署原版本代码**
4. **启动应用服务**

## 总结

FileUploadQueueTask 迁移已成功完成，实现了：
- 统一的任务管理架构
- 配置驱动的任务执行
- 更好的性能和可维护性
- 完整的前端配置支持

迁移后的系统具有更好的扩展性和一致性，为后续功能开发奠定了良好基础。
