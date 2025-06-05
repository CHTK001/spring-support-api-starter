package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenBackup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author CH
 * @since 2025/6/5 11:16
 */
@Mapper
public interface MonitorSysGenBackupMapper extends BaseMapper<MonitorSysGenBackup> {
    /**
     * 备份列表
     *
     * @return 备份列表
     */
    List<MonitorSysGenBackup> backupList();
}