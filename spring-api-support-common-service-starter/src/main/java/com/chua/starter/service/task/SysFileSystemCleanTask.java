package com.chua.starter.service.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.core.utils.FileUtils;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.entity.service.SysFileSystemPart;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemPartService;
import com.chua.starter.service.SysFileSystemService;
import com.chua.starter.service.SysFileSystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件系统清理定时任务
 * 定时清理过期文件和临时文件
 *
 * @author CH
 * @since 2024/12/20
 */
@SpringBootConfiguration
@RequiredArgsConstructor
@Slf4j
public class SysFileSystemCleanTask {

    private final SysFileSystemService sysFileSystemService;
    private final SysFileSystemPartService sysFileSystemPartService;
    private final SysFileSystemSettingService sysFileSystemSettingService;

    /**
     * 定时清理过期文件
     * 每天凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
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
                log.info("文件保留天数配置为永久保留({}天)，跳过清理任务", retentionDays);
                return;
            }

            // 计算过期时间
            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);

            // 查询过期的已完成文件
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
                    // 删除物理文件
                    deletePhysicalFile(file);
                    // 删除数据库记录
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

    /**
     * 定时清理失败的上传任务和临时文件
     * 每天凌晨4点30分执行
     */
    @Scheduled(cron = "0 30 4 * * ?")
    public void cleanFailedTasks() {
        log.info("开始执行失败任务清理定时任务");
        try {
            // 清理7天前的失败任务
            LocalDateTime expireTime = LocalDateTime.now().minusDays(7);

            // 查询失败或超时的上传任务
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
                    // 删除分片记录
                    List<SysFileSystemPart> parts = sysFileSystemPartService.list(
                            Wrappers.<SysFileSystemPart>lambdaQuery()
                                    .eq(SysFileSystemPart::getSysFileSystemPartFileId, task.getSysFileSystemId()));
                    
                    for (SysFileSystemPart part : parts) {
                        // 删除临时文件
                        if (part.getSysFileSystemPartLocalPath() != null) {
                            FileUtils.forceDeleteDirectory(new File(part.getSysFileSystemPartLocalPath()));
                        }
                    }
                    
                    // 删除分片数据库记录
                    sysFileSystemPartService.remove(Wrappers.<SysFileSystemPart>lambdaQuery()
                            .eq(SysFileSystemPart::getSysFileSystemPartFileId, task.getSysFileSystemId()));
                    
                    // 删除任务记录
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

    /**
     * 定时清理待合并超时的任务
     * 每天凌晨5点执行
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void cleanPendingMergeTasks() {
        log.info("开始执行待合并超时任务清理");
        try {
            // 清理超过3天仍在待合并状态的任务
            LocalDateTime expireTime = LocalDateTime.now().minusDays(3);

            List<SysFileSystem> pendingTasks = sysFileSystemService.list(Wrappers.<SysFileSystem>lambdaQuery()
                    .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.PENDING_MERGE)
                    .lt(SysFileSystem::getCreateTime, expireTime));

            if (pendingTasks.isEmpty()) {
                log.info("没有待合并超时任务需要处理");
                return;
            }

            // 将超时的待合并任务标记为错误状态
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
     * 删除物理文件
     *
     * @param file 文件记录
     */
    private void deletePhysicalFile(SysFileSystem file) {
        try {
            String path = file.getSysFileSystemPath();
            if (path != null && !path.isEmpty()) {
                File physicalFile = new File(path);
                if (physicalFile.exists()) {
                    FileUtils.forceDelete(physicalFile);
                    log.debug("删除物理文件成功: {}", path);
                }
            }
        } catch (Exception e) {
            log.warn("删除物理文件失败: fileId={}, path={}", 
                    file.getSysFileSystemId(), file.getSysFileSystemPath(), e);
        }
    }

    /**
     * 手动触发过期文件清理
     *
     * @param days 清理超过指定天数的文件，如果为null则使用配置值
     * @return 清理的文件数量
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

            log.info("手动过期文件清理完成，清理了{}个超过{}天的文件", cleanedCount, retentionDays);
            return cleanedCount;
        } catch (Exception e) {
            log.error("手动过期文件清理失败: {}", e.getMessage(), e);
            return 0;
        }
    }
}
