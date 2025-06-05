package com.chua.starter.monitor.handler.backup;

import com.chua.common.support.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.NumberUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.socketio.support.MsgEvent;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.common.support.cmd.CommandExecutor;
import com.chua.starter.monitor.entity.MonitorSysGenBackup;
import com.chua.starter.monitor.entity.MonitorSysGenBackupLog;
import com.chua.starter.monitor.mapper.MonitorSysGenBackupLogMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGen;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * mysql  备份
 *
 * @author CH
 * @since 2025/6/5 11:03
 */
@Slf4j
public class MysqlBackupHandler implements BackupHandler {

    private final MonitorSysGen monitorSysGen;
    private final String dbUrl;
    @AutoInject
    private SocketSessionTemplate messagingTemplate;
    @AutoInject
    private MonitorSysGenBackupLogMapper monitorSysGenBackupLogMapper;

    public MysqlBackupHandler(MonitorSysGen monitorSysGen) {
        this.monitorSysGen = monitorSysGen;
        this.dbUrl = monitorSysGen.getGenDriverUrl();
    }

    @Override
    public void call(MonitorSysGenBackup monitorSysGenBackup) throws Exception {
        String dbName = extractDatabaseName(dbUrl);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFile = FileUtils.normalize(StringUtils.defaultString(
                monitorSysGenBackup.getMonitorSysGenBackupPath(), "./backup-file"
        ), String.valueOf(monitorSysGenBackup.getMonitorSysGenBackupId()), "/backup_" + timestamp + ".sql");
        String compressedFile = backupFile + ".gz";

        DataSourceOptions dataSourceOptions = monitorSysGen.newDatabaseOptions();
        // 构建备份命令
        String command = String.format("mysqldump -u%s -p%s --databases %s > %s",
                monitorSysGen.getGenUser(), dataSourceOptions.getPassword(), dbName, backupFile);

        // 执行命令，设置30分钟超时
        CommandExecutor.CommandResult result = CommandExecutor.executeWithRealtimeOutput(command,
                TimeUnit.MINUTES.toMillis(NumberUtils.defaultIfNullOrPositive(
                        monitorSysGenBackup.getMonitorSysGenBackupTimeout(), 30).intValue()),
                it -> {
                    messagingTemplate.send("/topic/command/backup/" + monitorSysGenBackup.getMonitorSysGenBackupId(),
                            MsgEvent.builder().msg("备份进度: " + it)
                                    .type("INFO")
                                    .build());
                },
                it -> {
                    messagingTemplate.send("/topic/command/backup/" + monitorSysGenBackup.getMonitorSysGenBackupId(),
                            MsgEvent.builder().msg("备份进度: " + it)
                                    .type("ERROR")
                                    .build());
                });

        if (result.isSuccess()) {
            // 压缩备份文件
            compressFile(backupFile, compressedFile);
            Files.deleteIfExists(Paths.get(backupFile));
            registerLog(monitorSysGenBackup);
            // 清理旧备份
            cleanOldBackups(monitorSysGenBackup);
            System.out.println("备份成功: " + compressedFile);
            messagingTemplate.send("/topic/command/backup/" + monitorSysGenBackup.getMonitorSysGenBackupId(),
                    MsgEvent.builder().msg("备份进度: 备份成功" + compressedFile)
                            .type("INFO")
                            .build());
        } else {
            System.err.println("备份失败，退出码: " + result.getExitCode());
            System.err.println("错误输出: " + result.getError());
            messagingTemplate.send("/topic/command/backup/" + monitorSysGenBackup.getMonitorSysGenBackupId(),
                    MsgEvent.builder().msg("备份进度: " + result.getError())
                            .type("ERROR")
                            .build());
            throw new IOException("备份失败: " + result.getError());
        }
    }

    @Override
    public void restoreDatabase(String backupFile, MonitorSysGenBackup monitorSysGenBackup) throws Exception {
        String dbName = extractDatabaseName(dbUrl);

        // 如果是压缩文件，先解压
        String sqlFile = backupFile;
        if (backupFile.endsWith(".gz")) {
            sqlFile = backupFile.substring(0, backupFile.length() - 3);
            decompressFile(backupFile, sqlFile);
        }
        DataSourceOptions dataSourceOptions = monitorSysGen.newDatabaseOptions();

        String command = String.format("mysql -u%s -p%s %s < %s",
                monitorSysGen.getGenUser(), dataSourceOptions.getPassword(), dbName, sqlFile);

        // 执行命令，设置1小时超时
        CommandExecutor.CommandResult result = CommandExecutor.executeWithRealtimeOutput(command, TimeUnit.HOURS.toMillis(1), it -> {
            messagingTemplate.send("/topic/command/backup/" + monitorSysGenBackup.getMonitorSysGenBackupId(),
                    MsgEvent.builder().msg("恢复进度: " + it)
                            .type("INFO")
                            .build());
        }, it -> {
            messagingTemplate.send("/topic/command/backup/" + monitorSysGenBackup.getMonitorSysGenBackupId(),
                    MsgEvent.builder().msg("恢复进度: " + it)
                            .type("ERROR")
                            .build());
        });

        if (backupFile.endsWith(".gz")) {
            Files.deleteIfExists(Paths.get(sqlFile));
        }

        if (!result.isSuccess()) {
            throw new IOException("恢复失败，退出码: " + result.getExitCode() +
                    "\n错误输出: " + result.getError());
        }
    }

    private void decompressFile(String sourceFile, String destFile) throws IOException {
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(sourceFile));
             FileOutputStream fos = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

    private void registerLog(MonitorSysGenBackup monitorSysGenBackup) {
        try {
            MonitorSysGenBackupLog backupLog = new MonitorSysGenBackupLog();
            backupLog.setMonitorSysGenBackupId(monitorSysGenBackup.getMonitorSysGenBackupId());
            backupLog.setMonitorSysGenBackupLogStatus("SUCCESS");
            backupLog.setMonitorSysGenBackupLogDay(LocalDate.now());
            backupLog.setMonitorSysGenBackupLogTime(LocalDateTime.now());
            backupLog.setCreateTime(LocalDateTime.now());
            backupLog.setUpdateTime(LocalDateTime.now());
            monitorSysGenBackupLogMapper.insert(backupLog);
        } catch (Exception e) {
            log.error("注册备份日志失败", e);
        }
    }

    private String extractDatabaseName(String jdbcUrl) {
        if (null == jdbcUrl) {
            return "jdbc:mysql://" + monitorSysGen.getGenHost() + ":" + monitorSysGen.getGenPort() + "/" + monitorSysGen.getGenDatabase();
        }
        // jdbc:mysql://localhost:3306/db_name?...
        return jdbcUrl.substring(jdbcUrl.lastIndexOf("/") + 1, jdbcUrl.contains("?") ?
                jdbcUrl.indexOf("?") : jdbcUrl.length());
    }

    private void compressFile(String sourceFile, String destFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(destFile))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }
        }
    }

    private void cleanOldBackups(MonitorSysGenBackup monitorSysGenBackup) throws IOException {
        int retentionDays = NumberUtils.defaultIfNullOrPositive(
                monitorSysGenBackup.getMonitorSysGenBackupRetentionDays(), 90
        ).intValue();

        String backupPath = FileUtils.normalize(StringUtils.defaultString(
                monitorSysGenBackup.getMonitorSysGenBackupPath(), "./backup-file"
        ), String.valueOf(monitorSysGenBackup.getMonitorSysGenBackupId()));
        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);

        Files.list(Paths.get(backupPath))
                .filter(path -> path.toString().endsWith(".sql.gz"))
                .filter(path -> {
                    try {
                        String filename = path.getFileName().toString();
                        String dateStr = filename.substring(7, 15); // 提取yyyyMMdd
                        LocalDate fileDate = LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
                        return fileDate.isBefore(cutoffDate);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("删除旧备份失败: " + path);
                    }
                });
    }
}
