package com.chua.starter.service.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.io.binary.ByteSourceArray;
import com.chua.common.support.storage.oss.FileStorage;
import com.chua.common.support.storage.oss.metadata.MapMetadata;
import com.chua.common.support.storage.oss.request.PutObjectRequest;
import com.chua.common.support.storage.oss.result.PutObjectResult;
import com.chua.common.support.storage.oss.setting.BucketSetting;
import com.chua.common.support.core.utils.FileUtils;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.entity.service.SysFileSystemPart;
import com.chua.starter.service.SysFileSystemPartService;
import com.chua.starter.service.properties.FileSystemProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 文件系统合并任务注册器
 * 用于管理文件分片合并任务
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysFileSystemMergeTaskRegistration {

    private final SysFileSystemPartService sysFileSystemPartService;
    private final FileSystemProperties fileSystemProperties;
    private final Environment environment;

    private ThreadPoolExecutor executor;

    @PostConstruct
    public void init() {
        int corePoolSize = fileSystemProperties.getMergeTaskPoolSize();
        executor = new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                r -> new Thread(r, "file-system-merge-task"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("文件系统合并任务线程池初始化完成, 核心线程数: {}", corePoolSize);
    }

    /**
     * 注册合并任务
     *
     * @param wrapper 文件系统包装对象
     */
    public void register(SysFileSystemWrapper wrapper) {
        log.info("注册合并任务: taskId={}", wrapper.getSysFileSystem().getSysFileSystemTid());
        executor.submit(() -> executeMerge(wrapper));
    }

    /**
     * 执行合并操作
     *
     * @param wrapper 文件系统包装对象
     */
    private void executeMerge(SysFileSystemWrapper wrapper) {
        SysFileSystem sysFileSystem = wrapper.getSysFileSystem();
        String taskId = sysFileSystem.getSysFileSystemTid();

        log.info("开始执行合并任务: taskId={}", taskId);

        try {
            // 更新状态为合并中
            sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.MERGING);
            wrapper.getSysFileSystemService().updateById(sysFileSystem);

            // 获取所有分片并按序号排序
            List<SysFileSystemPart> parts = sysFileSystemPartService.list(Wrappers.<SysFileSystemPart>lambdaQuery()
                    .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                    .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED)
                    .orderByAsc(SysFileSystemPart::getSysFileSystemPartSeq));

            if (parts.size() != sysFileSystem.getSysFileSystemChunkTotal()) {
                log.error("分片数量不匹配: expected={}, actual={}", sysFileSystem.getSysFileSystemChunkTotal(), parts.size());
                throw new RuntimeException("分片数量不匹配");
            }

            // 合并分片
            byte[] mergedData = mergeChunks(parts);
            log.info("分片合并完成: taskId={}, size={}", taskId, mergedData.length);

            // 上传到存储
            PutObjectResult result = uploadToStorage(sysFileSystem, mergedData);
            if (result == null || !result.getResultCode().isSuccessful()) {
                throw new RuntimeException("上传存储失败");
            }

            // 更新状态为完成
            sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.COMPLETED);
            sysFileSystem.setSysFileSystemPath(result.getUrl());
            sysFileSystem.setSysFileSystemHttpUrl(result.getUrl());
            wrapper.getSysFileSystemService().updateById(sysFileSystem);

            // 清理临时文件
            cleanTempFiles(parts, taskId);

            log.info("合并任务完成: taskId={}", taskId);
        } catch (Exception e) {
            log.error("合并任务失败: taskId={}", taskId, e);
            sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.ERROR);
            wrapper.getSysFileSystemService().updateById(sysFileSystem);
        }
    }

    /**
     * 合并分片数据
     *
     * @param parts 分片列表
     * @return 合并后的字节数组
     */
    private byte[] mergeChunks(List<SysFileSystemPart> parts) throws IOException {
        // 按序号排序
        parts.sort(Comparator.comparing(SysFileSystemPart::getSysFileSystemPartSeq));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (SysFileSystemPart part : parts) {
                byte[] chunkData = part.copyByteArray();
                outputStream.write(chunkData);
            }
            return outputStream.toByteArray();
        }
    }

    /**
     * 上传到存储
     *
     * @param sysFileSystem 文件系统对象
     * @param data          文件数据
     * @return 上传结果
     */
    private PutObjectResult uploadToStorage(SysFileSystem sysFileSystem, byte[] data) {
        BucketSetting bucketSetting = BucketSetting.builder()
                .bucket(sysFileSystem.getSysFileSystemBucket())
                .accessKeyId(environment.getProperty("plugin.sso." + sysFileSystem.getSysFileSystemType().toLowerCase() + ".app-id"))
                .accessKeySecret(environment.getProperty("plugin.sso." + sysFileSystem.getSysFileSystemType().toLowerCase() + ".app-secret"))
                .charset(StandardCharsets.UTF_8)
                .endpoint(environment.getProperty("plugin.sso." + sysFileSystem.getSysFileSystemType().toLowerCase() + ".endpoint", "oss/system"))
                .region(environment.getProperty("plugin.sso." + sysFileSystem.getSysFileSystemType().toLowerCase() + ".region"))
                .build();

        try (FileStorage fileStorage = FileStorage.createStorage(sysFileSystem.getSysFileSystemType(), bucketSetting)) {
            return fileStorage.putObject(PutObjectRequest.builder()
                    .byteSource(new ByteSourceArray(data))
                    .fileName(sysFileSystem.getSysFileSystemName())
                    .metadata(new MapMetadata(Collections.emptyMap()))
                    .build());
        } catch (Exception e) {
            log.error("上传存储失败", e);
            return null;
        }
    }

    /**
     * 清理临时文件
     *
     * @param parts  分片列表
     * @param taskId 任务ID
     */
    private void cleanTempFiles(List<SysFileSystemPart> parts, String taskId) {
        try {
            for (SysFileSystemPart part : parts) {
                String localPath = part.getSysFileSystemPartLocalPath();
                if (localPath != null) {
                    FileUtils.forceDeleteDirectory(new File(localPath));
                }
            }

            // 删除任务目录
            String taskDir = FileUtils.normalize(fileSystemProperties.getTempPath(), taskId);
            FileUtils.deleteDirectory(new File(taskDir));
            log.info("临时文件清理完成: taskId={}", taskId);
        } catch (Exception e) {
            log.warn("临时文件清理失败: taskId={}", taskId, e);
        }
    }
}
