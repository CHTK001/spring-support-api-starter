package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenBackup;

import java.util.List;

/**
 * 数据源备份服务接口
 *
 * @author CH
 * @since 2025/6/5 10:15
 */
public interface MonitorSysGenBackupService extends IService<MonitorSysGenBackup> {

    /**
     * 保存数据源备份配置
     *
     * @param backup 数据源备份对象
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> saveFor(MonitorSysGenBackup backup);

    /**
     * 根据数据源ID查询备份配置
     *
     * @param monitorSysGenId 数据源ID
     * @return 数据源备份对象
     */
    MonitorSysGenBackup findBackupById(Integer monitorSysGenId);

    /**
     * 更新数据源备份配置
     *
     * @param backup 数据源备份对象
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> updateFor(MonitorSysGenBackup backup);

    /**
     * 删除数据源备份配置
     *
     * @param id 备份记录ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> removeFor(Integer id);

    /**
     * 获取所有数据源备份配置
     *
     * @return 数据源备份列表
     */
    List<MonitorSysGenBackup> backupList();

    /**
     * 备份数据源
     *
     * @param monitorSysGenBackup 备份配置对象
     */
    void backup(MonitorSysGenBackup monitorSysGenBackup);


    /**
     * 根据ID进行数据源备份
     *
     * @param id 数据源ID
     * @return 操作结果，包含布尔值表示是否成功
     */
    ReturnResult<Boolean> backup(Integer id);
}
