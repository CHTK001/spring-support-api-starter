package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.monitor.entity.MonitorSysGenBackup;
import com.chua.starter.monitor.entity.MonitorSysGenBackupLog;
import com.chua.starter.monitor.handler.backup.BackupHandler;
import com.chua.starter.monitor.mapper.MonitorSysGenBackupLogMapper;
import com.chua.starter.monitor.mapper.MonitorSysGenBackupMapper;
import com.chua.starter.monitor.service.MonitorSysGenBackupService;
import com.chua.starter.monitor.starter.entity.MonitorSysGen;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author CH
 * @since 2025/6/5 10:15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenBackupServiceImpl extends ServiceImpl<MonitorSysGenBackupMapper, MonitorSysGenBackup> implements MonitorSysGenBackupService {

    final MonitorSysGenBackupLogMapper monitorSysGenBackupLogMapper;
    final MonitorSysGenMapper monitorSysGenMapper;

    @Override
    public ReturnResult<Boolean> saveFor(MonitorSysGenBackup backup) {
        return ReturnResult.success(save(backup));
    }

    @Override
    public MonitorSysGenBackup findBackupById(Integer monitorSysGenId) {
        return getOne(
                Wrappers.<MonitorSysGenBackup>lambdaQuery()
                        .eq(MonitorSysGenBackup::getMonitorSysGenId, monitorSysGenId)
                , false);
    }

    @Override
    public ReturnResult<Boolean> updateFor(MonitorSysGenBackup backup) {
        return ReturnResult.success(updateById(backup));
    }

    @Override
    public ReturnResult<Boolean> removeFor(Integer id) {
        return ReturnResult.success(removeById(id));
    }

    @Override
    public List<MonitorSysGenBackup> backupList() {
        return baseMapper.backupList();
    }

    @Override
    public void backup(MonitorSysGenBackup monitorSysGenBackup) {
        if (null == monitorSysGenBackup.getMonitorSysGenBackupEnable() || !monitorSysGenBackup.getMonitorSysGenBackupEnable()) {
            log.info("数据源备份已经关闭");
            return;
        }
        if (monitorSysGenBackupLogMapper.exists(
                Wrappers.<MonitorSysGenBackupLog>lambdaQuery()
                        .eq(MonitorSysGenBackupLog::getMonitorSysGenId, monitorSysGenBackup.getMonitorSysGenId())
                        .eq(MonitorSysGenBackupLog::getMonitorSysGenBackupId, monitorSysGenBackup.getMonitorSysGenBackupId())
                        .eq(MonitorSysGenBackupLog::getMonitorSysGenBackupLogDay, LocalDate.now())
                        .eq(MonitorSysGenBackupLog::getMonitorSysGenBackupLogStatus, "SUCCESS")
        )) {
            log.warn("数据源备份已经存在");
            return;
        }

        Integer monitorSysGenId = monitorSysGenBackup.getMonitorSysGenId();
        if (null == monitorSysGenId) {
            log.warn("数据源不存在");
            return;
        }
        MonitorSysGen monitorSysGen = monitorSysGenMapper.selectById(monitorSysGenId);
        if (null == monitorSysGen) {
            log.warn("数据源不存在");
            return;
        }

        String genJdbcType = monitorSysGen.getGenJdbcType();
        if (StringUtils.isEmpty(genJdbcType)) {
            log.warn("数据源配置不存在");
            return;
        }


        BackupHandler backupHandler = ServiceProvider.of(BackupHandler.class).getNewExtension(genJdbcType, monitorSysGen);
        if (null == backupHandler) {
            log.warn("暂不支持[{}]的数据源备份", genJdbcType);
            registerLog(monitorSysGenBackup, "暂不支持的数据源备份", "FAILURE");
            return;
        }

        try {
            backupHandler.call(monitorSysGenBackup);
        } catch (Exception e) {
            log.error("数据源备份失败", e);
            registerLog(monitorSysGenBackup, e.getMessage(), "FAILURE");
        }
    }

    @Override
    public ReturnResult<Boolean> backup(Integer id) {
        MonitorSysGenBackup monitorSysGenBackup = this.getById(id);
        if (null == monitorSysGenBackup) {
            return ReturnResult.error("数据源不存在");
        }
        backup(monitorSysGenBackup);
        return ReturnResult.success();
    }

    private void registerLog(MonitorSysGenBackup monitorSysGenBackup, String message, String status) {
        MonitorSysGenBackupLog backupLog = new MonitorSysGenBackupLog();
        backupLog.setMonitorSysGenBackupId(monitorSysGenBackup.getMonitorSysGenBackupId());
        backupLog.setMonitorSysGenBackupLogStatus(status);
        backupLog.setMonitorSysGenBackupLogErrorMessage(message);
        backupLog.setMonitorSysGenBackupLogDay(LocalDate.now());
        backupLog.setMonitorSysGenId(monitorSysGenBackup.getMonitorSysGenId());
        backupLog.setCreateTime(LocalDateTime.now());
        backupLog.setUpdateTime(LocalDateTime.now());
        backupLog.setMonitorSysGenBackupLogTime(LocalDateTime.now());
        monitorSysGenBackupLogMapper.insert(backupLog);
    }
}
