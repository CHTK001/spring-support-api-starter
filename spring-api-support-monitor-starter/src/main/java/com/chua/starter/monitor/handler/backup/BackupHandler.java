package com.chua.starter.monitor.handler.backup;

import com.chua.starter.monitor.entity.MonitorSysGenBackup;

/**
 * 备份处理
 *
 * @author CH
 * @since 2025/6/5 10:48
 */
public interface BackupHandler {
    /**
     * 执行备份操作
     *
     * @param monitorSysGenBackup 备份参数对象
     */
    void call(MonitorSysGenBackup monitorSysGenBackup) throws Exception;

    /**
     * 还原数据库
     *
     * @param backupFile          备份文件
     * @param monitorSysGenBackup 备份参数对象
     */
    void restoreDatabase(String backupFile, MonitorSysGenBackup monitorSysGenBackup) throws Exception;
}
