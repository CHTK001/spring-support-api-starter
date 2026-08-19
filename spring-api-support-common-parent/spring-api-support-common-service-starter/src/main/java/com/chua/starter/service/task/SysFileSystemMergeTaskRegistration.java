package com.chua.starter.service.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.storage.FileStorage;
import com.chua.common.support.storage.request.PutObjectRequest;
import com.chua.common.support.storage.result.ObjectResult;
import com.chua.common.support.storage.result.PutObjectResult;
import com.chua.common.support.storage.setting.BucketSetting;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.entity.service.SysFileSystemPart;
import com.chua.starter.service.SysFileSystemPartService;
import com.chua.starter.service.utils.FileStorageSupport;
import com.chua.starter.service.properties.FileSystemProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @since 2024/12/10
 * @version 1.0.0
 * @author CH
 */
@Component
@RequiredArgsConstructor
public class SysFileSystemMergeTaskRegistration {

    /** log */
    private static final Logger log= LoggerFactory.getLogger(SysFileSystemMergeTaskRegistration.class);

    /** sysFileSystemPart服务 */
    private final SysFileSystemPartService sysFileSystemPartService;
    /** fileSystemProperties */
    private final FileSystemProperties fileSystemProperties;
    /** fileStorageSupport */
    private final FileStorageSupport fileStorageSupport;

    /** executor */
    private ThreadPoolExecutor executor;

    @PostConstruct
    /** 初始化 */
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

    /** 注册 */
    public void register(SysFileSystemWrapper wrapper) {
        log.info("注册合并任务: taskId={}", wrapper.getSysFileSystem().getSysFileSystemTid());
        executor.submit(() -> executeMerge(wrapper));
    }

    /** 执行合并 */
    private void executeMerge(SysFileSystemWrapper wrapper) {
        SysFileSystem sysFileSystem = wrapper.getSysFileSystem();
        String taskId = sysFileSystem.getSysFileSystemTid();

        log.info("开始执行合并任务: taskId={}", taskId);

        try {
            sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.MERGING);
            wrapper.getSysFileSystemService().updateById(sysFileSystem);

            List<SysFileSystemPart> parts = sysFileSystemPartService.list(Wrappers.<SysFileSystemPart>lambdaQuery()
                    .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                    .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED)
                    .orderByAsc(SysFileSystemPart::getSysFileSystemPartSeq));

            if (parts.size() != sysFileSystem.getSysFileSystemChunkTotal()) {
                log.info("分片数量不匹配: expected={}, actual={}", sysFileSystem.getSysFileSystemChunkTotal(), parts.size());
                throw new RuntimeException("分片数量不匹配");
            }

            byte[] mergedData = mergeChunks(parts);
            log.info("分片合并完成: taskId={}, size={}", taskId, mergedData.length);

            PutObjectResult result = uploadToStorage(sysFileSystem, mergedData);
            if (result == null || result.getResultCode() == null || result.getResultCode() != ObjectResult.ResultCode.SUCCESS) {
                throw new RuntimeException("上传存储失败");
            }

            sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.COMPLETED);
            sysFileSystem.setSysFileSystemPath(result.getUrl());
            String bucketName = sysFileSystem.getSysFileSystemBucket();
            if (StringUtils.isEmpty(bucketName)) {
                bucketName = "default";
            }
            sysFileSystem.setSysFileSystemHttpUrl("/file-storage/file?bucket=" + bucketName + "&key=" + result.getKey());
            wrapper.getSysFileSystemService().updateById(sysFileSystem);

            cleanTempFiles(parts, taskId);

            log.info("合并任务完成: taskId={}", taskId);
        } catch (Exception e) {
            log.error("合并任务失败: taskId={}", taskId, e);
            sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.ERROR);
            wrapper.getSysFileSystemService().updateById(sysFileSystem);
        }
    }

    /** 合并Chunks */
    private byte[] mergeChunks(List<SysFileSystemPart> parts) throws IOException {
        parts.sort(Comparator.comparing(SysFileSystemPart::getSysFileSystemPartSeq));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (SysFileSystemPart part : parts) {
                byte[] chunkData = part.copyByteArray();
                outputStream.write(chunkData);
            }
            return outputStream.toByteArray();
        }
    }

    /** UploadToStorage */
    private PutObjectResult uploadToStorage(SysFileSystem sysFileSystem, byte[] data) {
        BucketSetting bucketSetting = fileStorageSupport.createUploadBucketSetting(
                sysFileSystem.getSysFileSystemType(),
                sysFileSystem.getSysFileSystemBucket(),
                sysFileSystem.getSysFileSystemOriginalName(),
                sysFileSystem.getSysFileSystemName()
        );

        try (FileStorage fileStorage = fileStorageSupport.createStorage(sysFileSystem.getSysFileSystemType(), bucketSetting)) {
            return fileStorage.putObject(PutObjectRequest.builder()
                    .content(data)
                    .fileName(sysFileSystem.getSysFileSystemName())
                    .build());
        } catch (Exception e) {
            log.error("上传存储失败", e);
            return null;
        }
    }

    /** CleanTempFiles */
    private void cleanTempFiles(List<SysFileSystemPart> parts, String taskId) {
        try {
            for (SysFileSystemPart part : parts) {
                String localPath = part.getSysFileSystemPartLocalPath();
                if (localPath != null) {
                    forceDelete(new File(localPath));
                }
            }

            String taskDir = FileUtils.normalize(fileSystemProperties.getTempPath(), taskId);
            forceDelete(new File(taskDir));
            log.info("临时文件清理完成: taskId={}", taskId);
        } catch (Exception e) {
            log.warn("临时文件清理失败: taskId={}", taskId, e);
        }
    }

    /** Force删除 */
    private void forceDelete(File file) {
        if (null == file || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    forceDelete(child);
                }
            }
        }
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }
}