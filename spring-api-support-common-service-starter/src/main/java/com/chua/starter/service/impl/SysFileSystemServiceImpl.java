package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.io.binary.ByteSource;
import com.chua.common.support.io.binary.ByteSourceArray;
import com.chua.common.support.io.binary.ByteSourceFile;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.storage.oss.FileStorage;
import com.chua.common.support.storage.oss.extension.NamingResolver;
import com.chua.common.support.storage.oss.metadata.MapMetadata;
import com.chua.common.support.storage.oss.metadata.Metadata;
import com.chua.common.support.storage.oss.request.PutObjectRequest;
import com.chua.common.support.storage.oss.result.PutObjectResult;
import com.chua.common.support.storage.oss.setting.BucketSetting;
import com.chua.common.support.core.utils.FileUtils;
import com.chua.common.support.core.utils.IdUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.common.support.core.utils.UrlUtils;
import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.entity.service.SysFileSystemPart;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemPartService;
import com.chua.starter.service.SysFileSystemService;
import com.chua.starter.service.SysFileSystemSettingService;
import com.chua.starter.service.mapper.SysFileSystemMapper;
import com.chua.starter.service.pojo.sso.SysFileInstantUploadRequest;
import com.chua.starter.service.pojo.sso.SysFileInstantUploadResponse;
import com.chua.starter.service.pojo.sso.SysFileTaskInfoResponse;
import com.chua.starter.service.pojo.sso.SysFileUploadRequest;
import com.chua.starter.service.pojo.sso.SysFileUploadTaskPartRequest;
import com.chua.starter.service.pojo.sso.SysFileUploadTaskPartResponse;
import com.chua.starter.service.pojo.sso.SysFileUploadTaskRequest;
import com.chua.starter.service.pojo.sso.SysFileUploadTaskResponse;
import com.chua.starter.service.properties.FileSystemProperties;
import com.chua.starter.service.task.SysFileSystemMergeTaskRegistration;
import com.chua.starter.service.task.SysFileSystemWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SysFileSystemServiceImpl extends ServiceImpl<SysFileSystemMapper, SysFileSystem> implements SysFileSystemService {

    private final Environment environment;
    /**
     * 默认分片大小：100MB
     */
    private static final long DEFAULT_CHUNK_SIZE = 1024 * 1024 * 100L;
    private final TransactionTemplate transactionTemplate;
    private final SysFileSystemPartService sysFileSystemPartService;
    private final SysFileSystemSettingService sysFileSystemSettingService;
    private final FileSystemProperties fileSystemProperties;
    private final SysFileSystemMergeTaskRegistration mergeTaskRegistration;

    /**
     * 上传进度推送服务（可选依赖）
     */
    private FileUploadProgressPushService progressPushService;

    @Autowired
    public SysFileSystemServiceImpl(Environment environment, TransactionTemplate transactionTemplate,
                                    SysFileSystemPartService sysFileSystemPartService,
                                    SysFileSystemSettingService sysFileSystemSettingService,
                                    FileSystemProperties fileSystemProperties,
                                    SysFileSystemMergeTaskRegistration mergeTaskRegistration) {
        this.environment = environment;
        this.transactionTemplate = transactionTemplate;
        this.sysFileSystemPartService = sysFileSystemPartService;
        this.sysFileSystemSettingService = sysFileSystemSettingService;
        this.fileSystemProperties = fileSystemProperties;
        this.mergeTaskRegistration = mergeTaskRegistration;
    }

    @Autowired(required = false)
    public void setProgressPushService(FileUploadProgressPushService progressPushService) {
        this.progressPushService = progressPushService;
    }

    @Override
    public ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, MultipartFile multipartFile) {
        log.info("开始上传文件: fileName={}, bucket={}", multipartFile.getOriginalFilename(), request.getFileBucket());
        BucketSetting bucketSetting = createSetting(request, multipartFile.getOriginalFilename());

        try (FileStorage fileStorage = FileStorage.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.guess(fileStorage.putObject(PutObjectRequest.builder()
                    .byteSource(new ByteSourceArray(multipartFile.getInputStream()))
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createSimpleUuid() + "." + FileUtils.getExtension(multipartFile.getOriginalFilename())))
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
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createSimpleUuid() + "." + FileUtils.getExtension(file.getName())))
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
                        return StringUtils.defaultString(request.getFileName(), IdUtils.createSimpleUuid() + "." + FileUtils.getExtension(originalFilename));
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
        
        // 从配置获取分片大小
        long chunkSize = getChunkSizeFromSetting();
        
        long fileSize = request.getFileSize();
        int chunkCount = (int) Math.ceil((double) fileSize / chunkSize);
        String taskUid = IdUtils.createSimpleUuid();

        SysFileSystem sysFileSystem = new SysFileSystem();
        sysFileSystem.setSysFileSystemTid(taskUid);
        sysFileSystem.setSysFileSystemName(StringUtils.defaultString(request.getFileName(), IdUtils.createSimpleUuid() + "." + FileUtils.getExtension(request.getFileType())));
        sysFileSystem.setSysFileSystemOriginalName(request.getFileName());
        sysFileSystem.setSysFileSystemChunkTotal(chunkCount);
        sysFileSystem.setSysFileSystemChunkUploaded(0);
        sysFileSystem.setSysFileSystemChunkSize(chunkSize);
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
            response.setSysTaskSsoChuck(chunkSize);
            response.setUrl(UrlUtils.normalize(request.getFileBucket(), sysFileSystem.getSysFileSystemName()));
            log.info("分片上传任务创建成功: taskId={}, chunkCount={}, chunkSize={}MB", taskUid, chunkCount, chunkSize / 1024 / 1024);
            return ReturnResult.ok(response);
        });
    }

    /**
     * 从配置获取分片大小
     *
     * @return 分片大小（字节）
     */
    private long getChunkSizeFromSetting() {
        SysFileSystemSetting setting = sysFileSystemSettingService.getEnabledSettingForFileSystem();
        if (setting != null && setting.getSysFileSystemSettingChunkSizeMb() != null 
                && setting.getSysFileSystemSettingChunkSizeMb() > 0) {
            return setting.getSysFileSystemSettingChunkSizeMb() * 1024L * 1024L;
        }
        return DEFAULT_CHUNK_SIZE;
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
        part.setSysFileSystemPartLocalPath(FileUtils.normalize(fileSystemProperties.getTempPath(), request.getTaskId(), request.getTaskSeq() + ""));

        try {
            part.transferFrom(multipartFile);
        } catch (Exception e) {
            log.error("分片保存失败: taskId={}, seq={}", request.getTaskId(), request.getTaskSeq(), e);
            return ReturnResult.error("分片保存失败，请重试");
        }

        sysFileSystemPartService.updateById(part);

        // 查询已上传分片数量并推送进度
        int uploadedChunks = (int) sysFileSystemPartService.count(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED));

        // 推送上传进度
        if (progressPushService != null) {
            progressPushService.pushProgress(
                    sysFileSystem.getSysFileSystemTid(),
                    null,
                    sysFileSystem.getSysFileSystemChunkTotal(),
                    uploadedChunks,
                    request.getTaskSeq() + 1,
                    "uploading"
            );
        }

        checkAndMerge(sysFileSystem);
        log.info("分片上传成功: taskId={}, seq={}, uploadedChunks={}/{}", 
                request.getTaskId(), request.getTaskSeq(), uploadedChunks, sysFileSystem.getSysFileSystemChunkTotal());
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

        log.info("所有分片已上传完成: taskId={}", sysFileSystem.getSysFileSystemTid());

        // 检查是否启用自动合并
        SysFileSystemSetting setting = sysFileSystemSettingService.getEnabledSettingForFileSystem();
        boolean autoMergeEnabled = setting != null && Boolean.TRUE.equals(setting.getSysFileSystemSettingAutoMergeEnabled());

        if (autoMergeEnabled) {
            log.info("自动合并已启用，触发合并任务: taskId={}", sysFileSystem.getSysFileSystemTid());
            sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.PENDING_MERGE);
            baseMapper.updateById(sysFileSystem);
            mergeTaskRegistration.register(new SysFileSystemWrapper(sysFileSystem, this));
        } else {
            log.info("自动合并已关闭，等待手动合并: taskId={}", sysFileSystem.getSysFileSystemTid());
            sysFileSystem.setSysFileSystemStatus(SysFileSystem.Status.PENDING_MERGE);
            baseMapper.updateById(sysFileSystem);
        }
    }

    @Override
    public ReturnResult<SysFileInstantUploadResponse> checkInstantUploadForFileSystem(SysFileInstantUploadRequest request) {
        log.info("检查秒传: md5={}, fileSize={}, fileName={}", request.getFileMd5(), request.getFileSize(), request.getFileName());

        if (StringUtils.isEmpty(request.getFileMd5())) {
            return ReturnResult.error("文件MD5不能为空");
        }

        // 查找已完成上传且MD5匹配的文件
        SysFileSystem existingFile = getOne(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemMd5, request.getFileMd5())
                .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.COMPLETED)
                .last("LIMIT 1"));

        if (existingFile == null) {
            log.info("未找到匹配的文件，不支持秒传: md5={}", request.getFileMd5());
            return ReturnResult.ok(SysFileInstantUploadResponse.notSupport());
        }

        // 如果提供了文件大小，进一步验证
        if (request.getFileSize() != null && request.getFileSize() > 0) {
            if (!request.getFileSize().equals(existingFile.getSysFileSystemSize())) {
                log.info("文件大小不匹配，不支持秒传: expected={}, actual={}", 
                        request.getFileSize(), existingFile.getSysFileSystemSize());
                return ReturnResult.ok(SysFileInstantUploadResponse.notSupport());
            }
        }

        log.info("找到匹配的文件，支持秒传: fileId={}, fileName={}", 
                existingFile.getSysFileSystemId(), existingFile.getSysFileSystemName());

        return ReturnResult.ok(SysFileInstantUploadResponse.support(
                existingFile.getSysFileSystemId(),
                existingFile.getSysFileSystemPath(),
                existingFile.getSysFileSystemHttpUrl(),
                existingFile.getSysFileSystemName()
        ));
    }

    @Override
    public ReturnResult<SysFileTaskInfoResponse> getTaskInfoForFileSystem(String taskId) {
        log.info("获取任务信息: taskId={}", taskId);

        if (StringUtils.isEmpty(taskId)) {
            return ReturnResult.error("任务ID不能为空");
        }

        SysFileSystem sysFileSystem = getOne(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemTid, taskId));

        if (sysFileSystem == null) {
            SysFileTaskInfoResponse response = new SysFileTaskInfoResponse();
            response.setExists(false);
            return ReturnResult.ok(response);
        }

        return ReturnResult.ok(buildTaskInfoResponse(sysFileSystem));
    }

    @Override
    public ReturnResult<SysFileTaskInfoResponse> findTaskByMd5ForFileSystem(String fileMd5, Long fileSize) {
        log.info("根据MD5查找任务: md5={}, fileSize={}", fileMd5, fileSize);

        if (StringUtils.isEmpty(fileMd5)) {
            return ReturnResult.error("文件MD5不能为空");
        }

        // 查找未完成的上传任务（状态不是已完成和失败的）
        SysFileSystem sysFileSystem = getOne(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemMd5, fileMd5)
                .eq(fileSize != null && fileSize > 0, SysFileSystem::getSysFileSystemSize, fileSize)
                .notIn(SysFileSystem::getSysFileSystemStatus, 
                        SysFileSystem.Status.COMPLETED, SysFileSystem.Status.ERROR)
                .orderByDesc(SysFileSystem::getCreateTime)
                .last("LIMIT 1"));

        if (sysFileSystem == null) {
            SysFileTaskInfoResponse response = new SysFileTaskInfoResponse();
            response.setExists(false);
            return ReturnResult.ok(response);
        }

        return ReturnResult.ok(buildTaskInfoResponse(sysFileSystem));
    }

    /**
     * 构建任务信息响应
     *
     * @param sysFileSystem 文件系统记录
     * @return 任务信息响应
     */
    private SysFileTaskInfoResponse buildTaskInfoResponse(SysFileSystem sysFileSystem) {
        SysFileTaskInfoResponse response = new SysFileTaskInfoResponse();
        response.setExists(true);
        response.setTaskId(sysFileSystem.getSysFileSystemTid());
        response.setFileName(sysFileSystem.getSysFileSystemName());
        response.setFileMd5(sysFileSystem.getSysFileSystemMd5());
        response.setFileSize(sysFileSystem.getSysFileSystemSize());
        response.setChunkSize(sysFileSystem.getSysFileSystemChunkSize());
        response.setTotalChunks(sysFileSystem.getSysFileSystemChunkTotal());
        response.setStatus(sysFileSystem.getSysFileSystemStatus());

        // 如果已完成，返回URL
        if (sysFileSystem.getSysFileSystemStatus() == SysFileSystem.Status.COMPLETED) {
            response.setUrl(sysFileSystem.getSysFileSystemHttpUrl());
        }

        // 查询已上传的分片
        List<SysFileSystemPart> completedParts = sysFileSystemPartService.list(
                Wrappers.<SysFileSystemPart>lambdaQuery()
                        .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                        .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED)
                        .orderByAsc(SysFileSystemPart::getSysFileSystemPartSeq));

        List<Integer> uploadedPartSeqs = completedParts.stream()
                .map(SysFileSystemPart::getSysFileSystemPartSeq)
                .toList();

        response.setUploadedParts(uploadedPartSeqs);
        response.setUploadedChunks(uploadedPartSeqs.size());

        return response;
    }
}
