package com.chua.starter.monitor.scheduler;

import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.monitor.entity.MonitorSysGenBackup;
import com.chua.starter.monitor.service.MonitorSysGenBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 定时任务
 *
 * @author CH
 * @since 2025/6/5 10:38
 */
@SpringBootConfiguration
@RequiredArgsConstructor
public class MonitorSysGenBackupScheduler {

    final MonitorSysGenBackupService monitorSysGenBackupService;
    final ExecutorService executor = ThreadUtils.newVirtualThreadExecutor();

    @Scheduled(cron = "0 0,30 0 * * ?")
    public void backup() {
        List<MonitorSysGenBackup> backupList = monitorSysGenBackupService.backupList();
        for (MonitorSysGenBackup monitorSysGenBackup : backupList) {
            executor.execute(() -> {
                monitorSysGenBackupService.backup(monitorSysGenBackup);
            });
        }
    }
}
