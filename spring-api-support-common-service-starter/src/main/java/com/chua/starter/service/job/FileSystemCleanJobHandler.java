package com.chua.starter.service.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.core.utils.FileUtils;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.entity.service.SysFileSystemPart;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.job.support.handler.JobHandler;
import com.chua.starter.job.support.thread.JobContext;
import com.chua.starter.service.SysFileSystemPartService;
import com.chua.starter.service.SysFileSystemService;
import com.chua.starter.service.SysFileSystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件系统清理任务处理器
 * <p>
 * 通过 spring-job 模块实现动态配置的文件清理定时任务。
 * 可以在 monitor_job 表中配置：
 * - job_execute_bean: fileSystemCleanJobHandler
 * - job_execute_param: 清理类型 (expired/failed/pending)
 * - job_schedule_conf: cron表达式
 * </p>
 *
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@Component("fileSystemCleanJobHandler")
@RequiredArgsConstructor
@ConditionalOnClass(name = "com.chua.starter.job.support.handler.JobHandler")
public class FileSystemCleanJobHandler implements JobHandler {

    private final SysFileSystemService sysFileSystemService;
    private final SysFileSystemPartService sysFileSystemPartService;
    private final SysFileSystemSettingService sysFileSystemSettingService;

    @Override
    public void execute() throws Exception {
        String param = JobContext.getJobParam();
        String cleanType = param != null ? param.trim().toLowerCase() : "expired";

        log.info("开始执行文件清理任务, 类型: {}", cleanType);

        switch (cleanType) {
            case "expired" -> cleanExpiredFiles();
            case "failed" -> cleanFailedTasks();
            case "pending" -> cleanPendingMergeTasks();
            case "all" -> {
                cleanExpiredFiles();
                cleanFailedTasks();
                cleanPendingMergeTasks();
            }
            default -> {
                log.warn("未知的清理类型: {}, 默认执行过期文件清理", cleanType);
                cleanExpiredFiles();
            }
        }
    }

    /**
     * 清理过期文件
     */
    private void cleanExpiredFiles() {
        log.info("开始清理过期文件");
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

            log.info("过期文件清理完成，清理了{}个超过{}天的文件", cleanedCount, retentionDays);
        } catch (Exception e) {
            log.error("过期文件清理失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 清理失败的上传任务
     */
    private void cleanFailedTasks() {
        log.info("开始清理失败任务");
        try {
            // 清理7天前的失败任务
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
                    // 删除分片记录
                    List<SysFileSystemPart> parts = sysFileSystemPartService.list(
                            Wrappers.<SysFileSystemPart>lambdaQuery()
                                    .eq(SysFileSystemPart::getSysFileSystemPartFileId, task.getSysFileSystemId()));
                    
                    for (SysFileSystemPart part : parts) {
                        if (part.getSysFileSystemPartLocalPath() != null) {
                            FileUtils.forceDeleteDirectory(new File(part.getSysFileSystemPartLocalPath()));
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
            log.error("失败任务清理失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 清理待合并超时的任务
     */
    private void cleanPendingMergeTasks() {
        log.info("开始清理待合并超时任务");
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
            throw e;
        }
    }

    /**
     * 删除物理文件
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

    @Override
    public void init() throws Exception {
        log.debug("FileSystemCleanJobHandler 初始化");
    }

    @Override
    public void destroy() throws Exception {
        log.debug("FileSystemCleanJobHandler 销毁");
    }
}
