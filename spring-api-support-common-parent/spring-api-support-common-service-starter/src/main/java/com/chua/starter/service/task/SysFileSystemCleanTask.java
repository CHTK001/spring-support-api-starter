package com.chua.starter.service.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.entity.service.SysFileSystemPart;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemPartService;
import com.chua.starter.service.SysFileSystemService;
import com.chua.starter.service.SysFileSystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.Scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
* 文件系统清理定时任务
* 定时清理过期文件和临时文件
*
* @since 2024/12/20
* @author CH
 */
// @SpringBootConfiguration
/**
 * Sys文件SystemCleanTask类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class SysFileSystemCleanTask {

    /** 日志 */
    private static final Logger log = LoggerFactory.getLogger(SysFileSystemCleanTask.class);

    /** SYS文件系统服务 */
    private final SysFileSystemService sysFileSystemService;
    /** SYS文件systempart服务 */
    private final SysFileSystemPartService sysFileSystemPartService;
    /** SYS文件系统设置服务 */
    private final SysFileSystemSettingService sysFileSystemSettingService;

    @Scheduled(cron = "0 0 4 * * ?")
    /** cleanexpired文件 */
    public void cleanExpiredFiles() {
        log.info("开始执行过期文件清理定时任务");
        try {
            SysFileSystemSetting setting = sysFileSystemSettingService.getEnabledSettingForFileSystem();
            if (setting == null) {
                log.info("未找到启用的文件系统配置，跳过清理任务");
                return;
            }

            Integer retentionDays = setting.getSysFileSystemSettingRetentionDays();
            if (retentionDays == null || retentionDays <= 0) {
                log.info("文件保留天数配置为永久保留({}天)，保留天数", retentionDays);
                return;
            }

            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);

            List<SysFileSystem> expiredFiles = sysFileSystemService.list(Wrappers.<SysFileSystem>lambdaQuery()
                    .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.COMPLETED)
                    .lt(SysFileSystem::getCreateTime, expireTime));

            if (expiredFiles.isEmpty()) {
                log.info("没有过期文件需要清理");
                return;
            }

            int cleanedCount = 0;
            for (SysFileSystem file : expiredFiles) {
                try {
                    deletePhysicalFile(file);
                    sysFileSystemService.removeById(file.getSysFileSystemId());
                    cleanedCount++;
                } catch (Exception e) {
                    log.error("清理文件失败: fileId={}, fileName={}",
                            file.getSysFileSystemId(), file.getSysFileSystemName(), e);
                }
            }

            log.info("过期文件清理任务执行完成，清理了{}个超过{}天的文件", cleanedCount, retentionDays);
        } catch (Exception e) {
            log.error("过期文件清理任务执行失败: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 30 4 * * ?")
    /** clean失败任务 */
    public void cleanFailedTasks() {
        log.info("开始执行失败任务清理定时任务");
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(7);

            List<SysFileSystem> failedTasks = sysFileSystemService.list(Wrappers.<SysFileSystem>lambdaQuery()
                    .in(SysFileSystem::getSysFileSystemStatus,
                            SysFileSystem.Status.ERROR,
                            SysFileSystem.Status.PENDING,
                            SysFileSystem.Status.UPLOADING)
                    .lt(SysFileSystem::getCreateTime, expireTime));

            if (failedTasks.isEmpty()) {
                log.info("没有失败任务需要清理");
                return;
            }

            int cleanedCount = 0;
            for (SysFileSystem task : failedTasks) {
                try {
                    List<SysFileSystemPart> parts = sysFileSystemPartService.list(
                            Wrappers.<SysFileSystemPart>lambdaQuery()
                                    .eq(SysFileSystemPart::getSysFileSystemPartFileId, task.getSysFileSystemId()));
                    
                    for (SysFileSystemPart part : parts) {
                        if (part.getSysFileSystemPartLocalPath() != null) {
                            deleteDirectoryQuietly(new File(part.getSysFileSystemPartLocalPath()));
                        }
                    }
                    
                    sysFileSystemPartService.remove(Wrappers.<SysFileSystemPart>lambdaQuery()
                            .eq(SysFileSystemPart::getSysFileSystemPartFileId, task.getSysFileSystemId()));
                    
                    sysFileSystemService.removeById(task.getSysFileSystemId());
                    cleanedCount++;
                } catch (Exception e) {
                    log.error("清理失败任务异常: taskId={}", task.getSysFileSystemTid(), e);
                }
            }

            log.info("失败任务清理完成，清理了{}个失败任务", cleanedCount);
        } catch (Exception e) {
            log.error("失败任务清理任务执行失败: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 5 * * ?")
    /** cleanpending合并任务 */
    public void cleanPendingMergeTasks() {
        log.info("开始执行待合并超时任务清理");
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(3);

            List<SysFileSystem> pendingTasks = sysFileSystemService.list(Wrappers.<SysFileSystem>lambdaQuery()
                    .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.PENDING_MERGE)
                    .lt(SysFileSystem::getCreateTime, expireTime));

            if (pendingTasks.isEmpty()) {
                log.info("没有待合并超时任务需要处理");
                return;
            }

            int count = 0;
            for (SysFileSystem task : pendingTasks) {
                task.setSysFileSystemStatus(SysFileSystem.Status.ERROR);
                sysFileSystemService.updateById(task);
                count++;
                log.warn("待合并任务超时，标记为错误状态: taskId={}", task.getSysFileSystemTid());
            }

            log.info("待合并超时任务处理完成，标记了{}个任务为错误状态", count);
        } catch (Exception e) {
            log.error("待合并超时任务处理失败: {}", e.getMessage(), e);
        }
    }

    /**
    * 删除physical文件
    *
    * @param file 文件
     */
    private void deletePhysicalFile(SysFileSystem file) {
        try {
            String path = file.getSysFileSystemPath();
            if (path != null && !path.isEmpty()) {
                File physicalFile = new File(path);
                if (physicalFile.exists()) {
                    deleteDirectoryQuietly(physicalFile);
                    log.debug("删除物理文件成功: {}", path);
                }
            }
        } catch (Exception e) {
            log.warn("删除物理文件失败: fileId={}, path={}",
                    file.getSysFileSystemId(), file.getSysFileSystemPath(), e);
        }
    }

    /**
    * 删除目录quietly
    *
    * @param file 文件
     */
    private void deleteDirectoryQuietly(File file) {
        if (null == file || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectoryQuietly(child);
                }
            }
        }
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }

    /**
    * manualcleanexpired文件
    *
    * @param days days
    * @return manualcleanexpired文件的结果
     */
    public int manualCleanExpiredFiles(Integer days) {
        log.info("手动触发过期文件清理任务");
        try {
            SysFileSystemSetting setting = sysFileSystemSettingService.getEnabledSettingForFileSystem();
            int retentionDays;
            
            if (days != null && days > 0) {
                retentionDays = days;
            } else if (setting != null && setting.getSysFileSystemSettingRetentionDays() != null 
                    && setting.getSysFileSystemSettingRetentionDays() > 0) {
                retentionDays = setting.getSysFileSystemSettingRetentionDays();
            } else {
                log.info("未指定清理天数且配置为永久保留，跳过清理");
                return 0;
            }

            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);

            List<SysFileSystem> expiredFiles = sysFileSystemService.list(Wrappers.<SysFileSystem>lambdaQuery()
                    .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.COMPLETED)
                    .lt(SysFileSystem::getCreateTime, expireTime));

            if (expiredFiles.isEmpty()) {
                log.info("没有过期文件需要清理");
                return 0;
            }

            int cleanedCount = 0;
            for (SysFileSystem file : expiredFiles) {
                try {
                    deletePhysicalFile(file);
                    sysFileSystemService.removeById(file.getSysFileSystemId());
                    cleanedCount++;
                } catch (Exception e) {
                    log.error("手动清理文件失败: fileId={}", file.getSysFileSystemId(), e);
                }
            }

            log.info("手动过期文件清理完成，清理了{}个文件", cleanedCount);
            return cleanedCount;
        } catch (Exception e) {
            log.error("手动过期文件清理失败: {}", e.getMessage(), e);
            return 0;
        }
    }
}