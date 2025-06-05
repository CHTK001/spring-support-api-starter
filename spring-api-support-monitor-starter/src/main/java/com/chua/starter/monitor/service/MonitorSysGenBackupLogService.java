package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.monitor.entity.MonitorSysGenBackupLog;
import com.chua.starter.mybatis.entity.Query;

/**
 * 监控系统备份日志服务接口
 * 提供监控系统生成备份日志相关的操作
 *
 * @author CH
 * @since 2025/6/5 10:15
 */
public interface MonitorSysGenBackupLogService extends IService<MonitorSysGenBackupLog> {

    /**
     * 根据查询条件和备份ID分页查询监控系统生成备份日志
     *
     * @param query    查询条件，包含分页信息和查询参数
     * @param backupId 备份ID，用于筛选特定备份的日志
     * @return 返回分页查询结果，包含符合条件的日志列表和总记录数
     */
    ReturnPageResult<MonitorSysGenBackupLog> pageFor(Query<MonitorSysGenBackupLog> query, Integer backupId);
}
