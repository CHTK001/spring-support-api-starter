package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.monitor.entity.MonitorSysGenBackupLog;
import com.chua.starter.monitor.mapper.MonitorSysGenBackupLogMapper;
import com.chua.starter.monitor.service.MonitorSysGenBackupLogService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import org.springframework.stereotype.Service;

/**
 * @author CH
 * @since 2025/6/5 10:15
 */
@Service
public class MonitorSysGenBackupLogServiceImpl extends ServiceImpl<MonitorSysGenBackupLogMapper, MonitorSysGenBackupLog> implements MonitorSysGenBackupLogService {

    @Override
    public ReturnPageResult<MonitorSysGenBackupLog> pageFor(Query<MonitorSysGenBackupLog> query, Integer backupId) {
        return ReturnPageResultUtils.ok(
                this.page(query.createPage(), query.lambda()
                        .eq(MonitorSysGenBackupLog::getMonitorSysGenBackupId, backupId)
                )
        );
    }
}
