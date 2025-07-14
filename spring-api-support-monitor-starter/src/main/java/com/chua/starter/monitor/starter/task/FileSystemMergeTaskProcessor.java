package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.entity.FileSystem;
import com.chua.starter.monitor.starter.entity.FileSystemPart;
import com.chua.starter.monitor.starter.entity.FileSystemSetting;
import com.chua.starter.monitor.starter.service.FileSystemPartService;
import com.chua.starter.monitor.starter.service.FileSystemService;
import com.chua.starter.monitor.starter.service.FileSystemSettingService;
import com.chua.starter.monitor.starter.service.FileSystemSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

/**
 * 文件系统合并任务处理器
 * 
 * 负责处理文件分片的合并任务，支持： 1. 定时检测待合并的文件 2. 并发控制（根据配置限制同时执行的合并任务数量） 3. 失败重试机制 4.
 * 合并完成后的清理工作
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileSystemMergeTaskProcessor {

    @Autowired
    @Lazy
    private FileSystemService fileSystemService;
    private final FileSystemPartService fileSystemPartService;
    private final FileSystemSettingService fileSystemSettingService;
    private final FileSystemSseService fileSystemSseService;
    private final FileSystemMergeQueue mergeQueue;

    // 合并任务信号量，用于控制并发数量
    private volatile Semaphore mergeSemaphore = new Semaphore(1); // 默认初始化为1个许可
    private volatile int currentMergeTaskLimit = 1;

    // 数据库查询缓存，减少频繁查询
    private volatile long lastDatabaseQueryTime = 0;
    private static final long DATABASE_QUERY_INTERVAL = 10000; // 10秒查询一次数据库

    /**
     * 定时处理合并队列中的文件 每1秒执行一次
     */
    @Scheduled(fixedDelay = 1000)
    public void processPendingMergeFiles() {
        try {
            // 获取系统配置
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null) {
                log.debug("未找到启用的文件系统配置，跳过合并处理");
                return;
            }

            log.debug("开始处理自动合并队列");

            // 更新合并任务限制
            updateMergeTaskLimit(setting.getFileSystemSettingMergeTaskLimit());

            // 从队列中处理待合并的文件
            processQueuedMergeFiles();

            // 定期从数据库补充队列数据
            refreshQueueFromDatabase();

        } catch (Exception e) {
            log.error("处理合并队列失败", e);
        }
    }

    /**
     * 处理队列中的合并任务
     */
    private void processQueuedMergeFiles() {
        // 批量处理队列中的文件ID
        Integer[] fileIds = mergeQueue.dequeueBatch(5); // 每次最多处理5个

        for (Integer fileId : fileIds) {
            if (fileId == null)
                continue;

            try {
                // 检查文件状态和分片完成情况
                FileSystem fileSystem = fileSystemService.getById(fileId);
                if (fileSystem == null) {
                    log.warn("文件不存在，跳过合并: fileId={}", fileId);
                    continue;
                }

                // 只处理待合并状态的文件
                if (fileSystem.getFileSystemStatus() != FileSystem.FileStatus.PENDING_MERGE) {
                    log.debug("文件状态不是待合并，跳过: fileId={}, status={}", fileId, fileSystem.getFileSystemStatus());
                    continue;
                }

                // 检查是否所有分片都已上传完成
                int completedChunks = fileSystemPartService.countCompletedChunks(fileId);
                if (completedChunks == fileSystem.getFileSystemChunkTotal()) {
                    // 异步执行合并任务
                    processMergeFileAsync(fileId);
                } else {
                    // 分片未完成，重新放回队列稍后处理
                    mergeQueue.enqueue(fileId);
                    log.debug("文件分片未完成，重新入队: fileId={}, completed={}/{}", fileId, completedChunks,
                            fileSystem.getFileSystemChunkTotal());
                }

            } catch (Exception e) {
                log.error("处理队列中的合并文件失败: fileId={}", fileId, e);
            }
        }
    }

    /**
     * 定期从数据库刷新队列数据
     */
    private void refreshQueueFromDatabase() {
        long currentTime = System.currentTimeMillis();

        // 只有在队列为空或者距离上次查询超过间隔时间才查询数据库
        if (!mergeQueue.isEmpty() && (currentTime - lastDatabaseQueryTime) < DATABASE_QUERY_INTERVAL) {
            return;
        }

        try {
            // 查询待合并的文件
            List<FileSystem> pendingFiles = fileSystemService.lambdaQuery()
                    .eq(FileSystem::getFileSystemStatus, FileSystem.FileStatus.PENDING_MERGE).last("LIMIT 50") // 限制查询数量
                    .list();

            if (!pendingFiles.isEmpty()) {
                log.debug("从数据库发现 {} 个待合并的文件，添加到队列", pendingFiles.size());

                // 将文件ID添加到队列
                for (FileSystem fileSystem : pendingFiles) {
                    mergeQueue.enqueue(fileSystem.getFileSystemId());
                }
            }

            lastDatabaseQueryTime = currentTime;

        } catch (Exception e) {
            log.error("从数据库刷新合并队列失败", e);
        }
    }

    /**
     * 定时处理失败的合并任务 每5分钟执行一次
     */
    @Scheduled(fixedDelay = 300000)
    public void retryFailedMergeFiles() {
        log.debug("开始重试失败的合并任务");

        try {
            // 获取系统配置
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null) {
                return;
            }

            // 查询合并失败的文件（限制重试次数）
            List<FileSystem> failedFiles = fileSystemService.lambdaQuery()
                    .eq(FileSystem::getFileSystemStatus, FileSystem.FileStatus.MERGE_FAILED).last("LIMIT 10").list();

            if (failedFiles.isEmpty()) {
                log.debug("没有需要重试的失败文件");
                return;
            }

            log.info("发现 {} 个需要重试的失败文件", failedFiles.size());

            // 重试每个失败的文件
            for (FileSystem fileSystem : failedFiles) {
                try {
                    // 重置状态为待合并
                    fileSystemService.lambdaUpdate().eq(FileSystem::getFileSystemId, fileSystem.getFileSystemId())
                            .set(FileSystem::getFileSystemStatus, FileSystem.FileStatus.PENDING_MERGE).update();

                    log.info("重置失败文件状态为待合并: fileId={}", fileSystem.getFileSystemId());
                } catch (Exception e) {
                    log.error("重置失败文件状态失败: fileId={}", fileSystem.getFileSystemId(), e);
                }
            }

        } catch (Exception e) {
            log.error("重试失败合并任务失败", e);
        }
    }

    /**
     * 异步执行文件合并任务
     */
    @Async("fileSystemTaskExecutor")
    public CompletableFuture<Void> processMergeFileAsync(Integer fileId) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 确保合并许可信号量已初始化
                ensureMergeSemaphoreInitialized();

                // 获取合并许可
                if (mergeSemaphore.tryAcquire()) {
                    try {
                        processMergeFile(fileId);
                    } finally {
                        mergeSemaphore.release();
                    }
                } else {
                    log.warn("合并任务达到并发限制，将文件重新加入队列: fileId={}", fileId);
                    // 如果无法获取许可，将文件重新加入队列稍后处理
                    mergeQueue.enqueue(fileId);
                }
            } catch (Exception e) {
                log.error("异步合并文件失败: fileId={}", fileId, e);
            }
        });
    }

    /**
     * 执行文件合并
     */
    private void processMergeFile(Integer fileId) {
        log.info("开始合并文件: fileId={}", fileId);

        try {
            // 获取文件信息
            FileSystem fileSystem = fileSystemService.getById(fileId);
            if (fileSystem == null) {
                log.error("文件不存在: fileId={}", fileId);
                return;
            }

            // 更新状态为合并中
            fileSystemService.lambdaUpdate().eq(FileSystem::getFileSystemId, fileId)
                    .set(FileSystem::getFileSystemStatus, FileSystem.FileStatus.MERGING).update();

            // 发送合并开始消息
            fileSystemSseService.sendMergeProgress(fileId, 0.0, "开始合并");

            // 获取所有分片
            List<FileSystemPart> parts = fileSystemPartService.findByFileId(fileId);
            if (parts.isEmpty()) {
                throw new RuntimeException("没有找到文件分片");
            }

            // 按分片序号排序
            parts.sort((a, b) -> Integer.compare(a.getFileSystemPartChunkNumber(), b.getFileSystemPartChunkNumber()));

            // 检查分片完整性
            for (int i = 0; i < parts.size(); i++) {
                FileSystemPart part = parts.get(i);
                if (part.getFileSystemPartChunkNumber() != i) {
                    throw new RuntimeException("分片序号不连续: 期望=" + i + ", 实际=" + part.getFileSystemPartChunkNumber());
                }
                if (part.getFileSystemPartChunkStatus() != FileSystemPart.ChunkStatus.COMPLETED) {
                    throw new RuntimeException("分片未完成: chunkNumber=" + i);
                }
                if (!StringUtils.hasText(part.getFileSystemPartChunkPath())
                        || !Files.exists(Paths.get(part.getFileSystemPartChunkPath()))) {
                    throw new RuntimeException("分片文件不存在: chunkNumber=" + i);
                }
            }

            // 创建目标文件目录
            Path targetPath = Paths.get(fileSystem.getFileSystemPath());
            Files.createDirectories(targetPath.getParent());

            // 合并分片文件
            mergeChunks(parts, targetPath, fileId);

            // 验证合并后的文件
            if (!Files.exists(targetPath)) {
                throw new RuntimeException("合并后的文件不存在");
            }

            long mergedFileSize = Files.size(targetPath);
            if (!fileSystem.getFileSystemSize().equals(mergedFileSize)) {
                throw new RuntimeException(
                        "合并后文件大小不匹配: 期望=" + fileSystem.getFileSystemSize() + ", 实际=" + mergedFileSize);
            }

            // 更新文件状态为已完成
            fileSystemService.lambdaUpdate().eq(FileSystem::getFileSystemId, fileId)
                    .set(FileSystem::getFileSystemStatus, FileSystem.FileStatus.COMPLETED)
                    .set(FileSystem::getFileSystemUploadCompleteTime, LocalDateTime.now()).update();

            // 发送合并完成消息
            fileSystemSseService.sendMergeProgress(fileId, 100.0, "合并完成");
            fileSystemSseService.sendFileOperationResult("merge", fileId, true, "文件合并成功");

            // 清理分片文件
            cleanupChunks(parts);

            log.info("文件合并成功: fileId={}, targetPath={}, size={}", fileId, targetPath, mergedFileSize);

        } catch (Exception e) {
            log.error("文件合并失败: fileId={}", fileId, e);

            // 更新状态为合并失败
            try {
                fileSystemService.lambdaUpdate().eq(FileSystem::getFileSystemId, fileId)
                        .set(FileSystem::getFileSystemStatus, FileSystem.FileStatus.MERGE_FAILED).update();

                // 发送合并失败消息
                fileSystemSseService.sendMergeProgress(fileId, 0.0, "合并失败: " + e.getMessage());
                fileSystemSseService.sendFileOperationResult("merge", fileId, false, "文件合并失败: " + e.getMessage());
            } catch (Exception updateException) {
                log.error("更新文件状态失败: fileId={}", fileId, updateException);
            }
        }
    }

    /**
     * 合并分片文件
     */
    private void mergeChunks(List<FileSystemPart> parts, Path targetPath, Integer fileId) throws IOException {
        log.debug("开始合并分片文件: targetPath={}, chunkCount={}", targetPath, parts.size());

        try (FileOutputStream fos = new FileOutputStream(targetPath.toFile());
                FileChannel targetChannel = fos.getChannel()) {

            long position = 0;
            int totalChunks = parts.size();

            for (int i = 0; i < parts.size(); i++) {
                FileSystemPart part = parts.get(i);
                Path chunkPath = Paths.get(part.getFileSystemPartChunkPath());

                try (FileInputStream fis = new FileInputStream(chunkPath.toFile());
                        FileChannel sourceChannel = fis.getChannel()) {

                    long chunkSize = sourceChannel.size();
                    long transferred = sourceChannel.transferTo(0, chunkSize, targetChannel);

                    if (transferred != chunkSize) {
                        throw new IOException("分片传输不完整: chunkNumber=" + part.getFileSystemPartChunkNumber() + ", 期望="
                                + chunkSize + ", 实际=" + transferred);
                    }

                    position += transferred;

                    // 计算并发送合并进度
                    double progress = ((double) (i + 1) / totalChunks) * 100.0;
                    fileSystemSseService.sendMergeProgress(fileId, progress,
                            String.format("正在合并分片 %d/%d", i + 1, totalChunks));

                    log.debug("分片合并完成: chunkNumber={}, size={}, position={}, progress={}%",
                            part.getFileSystemPartChunkNumber(), chunkSize, position, String.format("%.1f", progress));
                }
            }

            log.debug("所有分片合并完成: totalSize={}", position);
        }
    }

    /**
     * 清理分片文件
     */
    private void cleanupChunks(List<FileSystemPart> parts) {
        log.debug("开始清理分片文件: chunkCount={}", parts.size());

        for (FileSystemPart part : parts) {
            try {
                if (StringUtils.hasText(part.getFileSystemPartChunkPath())) {
                    Path chunkPath = Paths.get(part.getFileSystemPartChunkPath());
                    Files.deleteIfExists(chunkPath);
                    log.debug("分片文件删除成功: chunkNumber={}, path={}", part.getFileSystemPartChunkNumber(),
                            part.getFileSystemPartChunkPath());
                }
            } catch (Exception e) {
                log.warn("删除分片文件失败: chunkNumber={}, path={}", part.getFileSystemPartChunkNumber(),
                        part.getFileSystemPartChunkPath(), e);
            }
        }

        // 删除分片记录
        try {
            fileSystemPartService.deleteByFileId(parts.get(0).getFileSystemPartFileId());
            log.debug("分片记录删除成功: fileId={}", parts.get(0).getFileSystemPartFileId());
        } catch (Exception e) {
            log.warn("删除分片记录失败: fileId={}", parts.get(0).getFileSystemPartFileId(), e);
        }
    }

    /**
     * 更新合并任务限制
     */
    private void updateMergeTaskLimit(Integer newLimit) {
        if (newLimit == null || newLimit <= 0) {
            newLimit = 1;
        }

        if (currentMergeTaskLimit != newLimit) {
            currentMergeTaskLimit = newLimit;
            mergeSemaphore = new Semaphore(currentMergeTaskLimit);
            log.info("更新合并任务并发限制: {}", currentMergeTaskLimit);
        }
    }

    /**
     * 确保合并许可信号量已初始化
     */
    private void ensureMergeSemaphoreInitialized() {
        if (mergeSemaphore == null) {
            synchronized (this) {
                if (mergeSemaphore == null) {
                    mergeSemaphore = new Semaphore(currentMergeTaskLimit);
                    log.info("初始化合并任务信号量: permits={}", currentMergeTaskLimit);
                }
            }
        }
    }
}
