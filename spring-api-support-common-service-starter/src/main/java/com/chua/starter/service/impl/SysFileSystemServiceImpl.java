package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.binary.ByteSource;
import com.chua.common.support.binary.ByteSourceArray;
import com.chua.common.support.binary.ByteSourceFile;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.oss.FileStorage;
import com.chua.common.support.oss.extension.NamingResolver;
import com.chua.common.support.oss.metadata.MapMetadata;
import com.chua.common.support.oss.metadata.Metadata;
import com.chua.common.support.oss.request.PutObjectRequest;
import com.chua.common.support.oss.result.PutObjectResult;
import com.chua.common.support.oss.setting.BucketSetting;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.UrlUtils;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.entity.service.SysFileSystemPart;
import com.chua.starter.service.SysFileSystemPartService;
import com.chua.starter.service.SysFileSystemService;
import com.chua.starter.service.mapper.SysFileSystemMapper;
import com.chua.starter.service.pojo.sso.*;
import com.chua.starter.service.properties.SsoProperties;
import com.chua.starter.service.task.SysFileSystemMergeTaskRegistration;
import com.chua.starter.service.task.SysFileSystemWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文件系统服务实现类
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileSystemServiceImpl extends ServiceImpl<SysFileSystemMapper, SysFileSystem> implements SysFileSystemService {

    private final Environment environment;
    /**
     * 默认分片大小：100MB
     */
    private static final long CHUNK_SIZE = 1024 * 1024 * 100L;
    private final TransactionTemplate transactionTemplate;
    private final SysFileSystemPartService sysFileSystemPartService;
    private final SsoProperties ssoProperties;
    private final SysFileSystemMergeTaskRegistration mergeTaskRegistration;

    @Override
    public ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, MultipartFile multipartFile) {
        log.info("开始上传文件: fileName={}, bucket={}", multipartFile.getOriginalFilename(), request.getFileBucket());
        BucketSetting bucketSetting = createSetting(request, multipartFile.getOriginalFilename());

        try (FileStorage fileStorage = FileStorage.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.guess(fileStorage.putObject(PutObjectRequest.builder()
                    .byteSource(new ByteSourceArray(multipartFile.getInputStream()))
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createUlid() + "." + FileUtils.getExtension(multipartFile.getOriginalFilename())))
                    .metadata(new MapMetadata(Collections.emptyMap()))
                    .build()));
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, File file) {
        log.info("开始上传文件: fileName={}, bucket={}", file.getName(), request.getFileBucket());
        BucketSetting bucketSetting = createSetting(request, file.getName());

        try (FileStorage fileStorage = FileStorage.createStorage(request.getFileBucket(), bucketSetting)) {
            return ReturnResult.guess(fileStorage.putObject(PutObjectRequest.builder()
                    .byteSource(new ByteSourceFile(file))
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createUlid() + "." + FileUtils.getExtension(file.getName())))
                    .metadata(new MapMetadata(Collections.emptyMap()))
                    .build()));
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, String fileName, byte[] bytes) {
        log.info("开始上传文件: fileName={}, bucket={}", fileName, request.getFileBucket());
        BucketSetting bucketSetting = createSetting(request, fileName);
        try (FileStorage fileStorage = FileStorage.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.guess(fileStorage.putObject(PutObjectRequest.builder()
                    .byteSource(new ByteSourceArray(bytes))
                    .fileName(StringUtils.defaultString(request.getFileName(), fileName))
                    .metadata(new MapMetadata(Collections.emptyMap()))
                    .build()));
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 创建存储设置
     *
     * @param request          请求参数
     * @param originalFilename 原始文件名
     * @return 存储设置
     */
    private BucketSetting createSetting(SysFileUploadRequest request, String originalFilename) {
        return BucketSetting.builder()
                .bucket(request.getFileBucket())
                .nameResolver(new NamingResolver() {
                    @Override
                    public String transform(Metadata metadata, ByteSource byteSource) {
                        return StringUtils.defaultString(request.getFileName(), IdUtils.createUlid() + "." + FileUtils.getExtension(originalFilename));
                    }
                })
                .accessKeyId(environment.getProperty("plugin.sso." + request.getOssType().toLowerCase() + ".app-id"))
                .accessKeySecret(environment.getProperty("plugin.sso." + request.getOssType().toLowerCase() + ".app-secret"))
                .charset(StandardCharsets.UTF_8)
                .endpoint(environment.getProperty("plugin.sso." + request.getOssType().toLowerCase() + ".endpoint", "oss/system"))
                .region(environment.getProperty("plugin.sso." + request.getOssType().toLowerCase() + ".region"))
                .build();
    }

    @Override
    public ReturnResult<SysFileUploadTaskResponse> createTaskForFileSystem(SysFileUploadTaskRequest request) {
        log.info("创建分片上传任务: fileName={}, fileSize={}", request.getFileName(), request.getFileSize());
        long fileSize = request.getFileSize();
        int chunkCount = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
        String taskUid = IdUtils.createUlid();

        SysFileSystem sysFileSystem = new SysFileSystem();
        sysFileSystem.setSysFileSystemTid(taskUid);
        sysFileSystem.setSysFileSystemName(StringUtils.defaultString(request.getFileName(), IdUtils.createUlid() + "." + FileUtils.getExtension(request.getFileType())));
        sysFileSystem.setSysFileSystemOriginalName(request.getFileName());
        sysFileSystem.setSysFileSystemChunkTotal(chunkCount);
        sysFileSystem.setSysFileSystemChunkUploaded(0);
        sysFileSystem.setSysFileSystemChunkSize(CHUNK_SIZE);
        sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.PENDING);
        sysFileSystem.setSysFileSystemSummary(request.getFileSummary());
        sysFileSystem.setSysFileSystemSize(fileSize);
        sysFileSystem.setSysFileSystemMd5(request.getFileMd5());
        sysFileSystem.setSysFileSystemType(request.getSsoType());
        sysFileSystem.setSysFileSystemBucket(request.getFileBucket());

        return transactionTemplate.execute(status -> {
            save(sysFileSystem);

            // 创建分片记录
            List<SysFileSystemPart> partList = new ArrayList<>(chunkCount);
            for (int i = 0; i < chunkCount; i++) {
                SysFileSystemPart part = new SysFileSystemPart();
                part.setSysFileSystemPartSeq(i + 1);
                part.setSysFileSystemPartMd5(request.getFileMd5());
                part.setSysFileSystemPartStatus(SysFileSystemPart.Status.PENDING);
                part.setSysFileSystemPartFileId(sysFileSystem.getSysFileSystemId());
                partList.add(part);
            }
            sysFileSystemPartService.saveBatch(partList);

            // 构建响应
            SysFileUploadTaskResponse response = new SysFileUploadTaskResponse();
            response.setTaskId(taskUid);
            response.setSetSysTaskSsoNumber(chunkCount);
            response.setSysTaskSsoChuck(CHUNK_SIZE);
            response.setUrl(UrlUtils.normalize(request.getFileBucket(), sysFileSystem.getSysFileSystemName()));
            log.info("分片上传任务创建成功: taskId={}, chunkCount={}", taskUid, chunkCount);
            return ReturnResult.ok(response);
        });
    }

    @Override
    public ReturnResult<SysFileUploadTaskPartResponse> uploadTaskForFileSystem(SysFileUploadTaskPartRequest request, MultipartFile multipartFile) {
        log.info("上传分片: taskId={}, seq={}", request.getTaskId(), request.getTaskSeq());

        SysFileSystem sysFileSystem = getOne(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemTid, request.getTaskId())
        );

        if (sysFileSystem == null) {
            log.warn("任务不存在: taskId={}", request.getTaskId());
            return ReturnResult.error("任务不存在");
        }

        if (sysFileSystem.getSysFileSystemStatus() == SysFileSystem.Status.COMPLETED) {
            log.warn("任务已完成: taskId={}", request.getTaskId());
            return ReturnResult.error("任务已上传完成");
        }

        SysFileSystemPart part = sysFileSystemPartService.getOne(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                .eq(SysFileSystemPart::getSysFileSystemPartSeq, request.getTaskSeq() + 1)
        );

        if (part == null) {
            log.warn("分片不存在: taskId={}, seq={}", request.getTaskId(), request.getTaskSeq());
            return ReturnResult.error("分片不存在");
        }

        if (part.getSysFileSystemPartStatus() == SysFileSystemPart.Status.COMPLETED) {
            log.warn("分片已上传: taskId={}, seq={}", request.getTaskId(), request.getTaskSeq());
            return ReturnResult.error("分片已上传");
        }

        if (part.getSysFileSystemPartStatus() == SysFileSystemPart.Status.UPLOADING) {
            log.warn("分片正在上传: taskId={}, seq={}", request.getTaskId(), request.getTaskSeq());
            return ReturnResult.error("分片正在上传中");
        }

        // 更新分片状态
        part.setSysFileSystemPartStatus(SysFileSystemPart.Status.COMPLETED);
        part.setSysFileSystemPartMd5(request.getFileMd5());
        part.setSysFileSystemPartLocalPath(FileUtils.normalize(ssoProperties.getTempPath(), request.getTaskId(), request.getTaskSeq() + ""));

        try {
            part.transferFrom(multipartFile);
        } catch (Exception e) {
            log.error("分片保存失败: taskId={}, seq={}", request.getTaskId(), request.getTaskSeq(), e);
            return ReturnResult.error("分片保存失败，请重试");
        }

        sysFileSystemPartService.updateById(part);
        checkAndMerge(sysFileSystem);
        log.info("分片上传成功: taskId={}, seq={}", request.getTaskId(), request.getTaskSeq());
        return ReturnResult.ok(new SysFileUploadTaskPartResponse());
    }

    /**
     * 检查是否所有分片都已上传完成，如果是则触发合并
     *
     * @param sysFileSystem 文件系统记录
     */
    private void checkAndMerge(SysFileSystem sysFileSystem) {
        List<SysFileSystemPart> completedParts = sysFileSystemPartService.list(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED));

        if (completedParts.size() != sysFileSystem.getSysFileSystemChunkTotal()) {
            return;
        }

        log.info("所有分片已上传完成，触发合并任务: taskId={}", sysFileSystem.getSysFileSystemTid());
        sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.PENDING_MERGE);
        baseMapper.updateById(sysFileSystem);
        mergeTaskRegistration.register(new SysFileSystemWrapper(sysFileSystem, this));
    }
}
