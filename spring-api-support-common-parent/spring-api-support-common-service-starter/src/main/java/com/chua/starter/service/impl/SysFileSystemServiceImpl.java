package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.storage.FileStorage;
import com.chua.common.support.storage.request.PutObjectRequest;
import com.chua.common.support.storage.result.PutObjectResult;
import com.chua.common.support.storage.setting.BucketSetting;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.UrlUtils;
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
import com.chua.starter.service.utils.FileStorageSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文件系统服务实现类
 *
 * @since 2024/12/10
 * @version 1.0.0
 * @author CH
 */
@Service
public class SysFileSystemServiceImpl extends ServiceImpl<SysFileSystemMapper, SysFileSystem> implements SysFileSystemService {

    /** log */
    private static final Logger log= LoggerFactory.getLogger(SysFileSystemServiceImpl.class);

    /** DEFAULCHUN大小 */
    private static final long DEFAULT_CHUNK_SIZE= 1024 * 1024 * 100L;
    /** transactionTemplate */
    private final TransactionTemplate transactionTemplate;
    /** sysFileSystemPart服务 */
    private final SysFileSystemPartService sysFileSystemPartService;
    /** sysFileSystem设置服务 */
    private final SysFileSystemSettingService sysFileSystemSettingService;
    /** fileSystemProperties */
    private final FileSystemProperties fileSystemProperties;
    /** mergeTaskRegistration */
    private final SysFileSystemMergeTaskRegistration mergeTaskRegistration;
    /** fileStorageSupport */
    private final FileStorageSupport fileStorageSupport;

    /** progressPush服务 */
    private FileUploadProgressPushService progressPushService;

    @Autowired
    /**
     * 创建 SysFileSystemServiceImpl 实例
     * @param transactionTemplate transactionTemplate
     * @param sysFileSystemPartService sysFileSystemPartService
     * @param sysFileSystemSettingService sysFileSystemSettingService
     * @param fileSystemProperties fileSystemProperties
     * @param mergeTaskRegistration mergeTaskRegistration
     * @param fileStorageSupport fileStorageSupport
     */
    public SysFileSystemServiceImpl(TransactionTemplate transactionTemplate,
                                    SysFileSystemPartService sysFileSystemPartService,
                                    @Lazy SysFileSystemSettingService sysFileSystemSettingService,
                                    FileSystemProperties fileSystemProperties,
                                    SysFileSystemMergeTaskRegistration mergeTaskRegistration,
                                    FileStorageSupport fileStorageSupport) {
        this.transactionTemplate = transactionTemplate;
        this.sysFileSystemPartService = sysFileSystemPartService;
        this.sysFileSystemSettingService = sysFileSystemSettingService;
        this.fileSystemProperties = fileSystemProperties;
        this.mergeTaskRegistration = mergeTaskRegistration;
        this.fileStorageSupport = fileStorageSupport;
    }

    @Autowired(required = false)
    public void setProgressPushService(FileUploadProgressPushService progressPushService) {
        this.progressPushService = progressPushService;
    }

    @Override
    public ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, MultipartFile multipartFile) {
        log.info("开始上传文件: fileName={}, bucket={}", multipartFile.getOriginalFilename(), request.getFileBucket());
        BucketSetting bucketSetting = createSetting(request, multipartFile.getOriginalFilename());
        try (FileStorage fileStorage = fileStorageSupport.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.of(fileStorage.putObject(PutObjectRequest.builder()
                    .inputStream(multipartFile.getInputStream())
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createSimpleUuid() + "." + FileUtils.getExtension(multipartFile.getOriginalFilename())))
                    .build()));
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, File file) {
        log.info("开始上传文件: fileName={}, bucket={}", file.getName(), request.getFileBucket());
        BucketSetting bucketSetting = createSetting(request, file.getName());
        try (FileStorage fileStorage = fileStorageSupport.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.of(fileStorage.putObject(PutObjectRequest.builder()
                    .file(file)
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createSimpleUuid() + "." + FileUtils.getExtension(file.getName())))
                    .build()));
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, String fileName, byte[] bytes) {
        log.info("开始上传文件: fileName={}, bucket={}", fileName, request.getFileBucket());
        BucketSetting bucketSetting = createSetting(request, fileName);
        try (FileStorage fileStorage = FileStorage.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.of(fileStorage.putObject(PutObjectRequest.builder()
                    .content(bytes)
                    .fileName(StringUtils.defaultString(request.getFileName(), fileName))
                    .build()));
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    private BucketSetting createSetting(SysFileUploadRequest request, String originalFilename) {
        return fileStorageSupport.createUploadBucketSetting(
                request.getOssType(),
                request.getFileBucket(),
                originalFilename,
                request.getFileName()
        );
    }

    @Override
    public ReturnResult<SysFileUploadTaskResponse> createTaskForFileSystem(SysFileUploadTaskRequest request) {
        log.info("创建分片上传任务: fileName={}, fileSize={}", request.getFileName(), request.getFileSize());

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

            SysFileUploadTaskResponse response = new SysFileUploadTaskResponse();
            response.setTaskId(taskUid);
            response.setSetSysTaskSsoNumber(chunkCount);
            response.setSysTaskSsoChuck(chunkSize);
            response.setUrl(UrlUtils.normalize(request.getFileBucket(), sysFileSystem.getSysFileSystemName()));
            log.info("分片上传任务创建成功: taskId={}, chunkCount={}, chunkSize={}MB",
                    taskUid, chunkCount, chunkSize / 1024 / 1024);
            return ReturnResult.ok(response);
        });
    }

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
            log.warn("分片正在上传中: taskId={}, seq={}", request.getTaskId(), request.getTaskSeq());
            return ReturnResult.error("分片正在上传中");
        }

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

        int uploadedChunks = (int) sysFileSystemPartService.count(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED));

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
        log.info("分片上传成功: taskId={}, seq={}, uploaded={}/{}",
                request.getTaskId(), request.getTaskSeq(), uploadedChunks, sysFileSystem.getSysFileSystemChunkTotal());
        return ReturnResult.ok(new SysFileUploadTaskPartResponse());
    }

    private void checkAndMerge(SysFileSystem sysFileSystem) {
        List<SysFileSystemPart> completedParts = sysFileSystemPartService.list(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED));

        if (completedParts.size() != sysFileSystem.getSysFileSystemChunkTotal()) {
            return;
        }

        log.info("所有分片已上传完成: taskId={}", sysFileSystem.getSysFileSystemTid());

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

        SysFileSystem existingFile = getOne(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemMd5, request.getFileMd5())
                .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.COMPLETED)
                .last("LIMIT 1"));

        if (existingFile == null) {
            log.info("未找到匹配的文件，不支持秒传: md5={}", request.getFileMd5());
            return ReturnResult.ok(SysFileInstantUploadResponse.notSupport());
        }

        if (request.getFileSize() != null && request.getFileSize() > 0) {
            if (!request.getFileSize().equals(existingFile.getSysFileSystemSize())) {
                log.info("文件大小不匹配，不支持秒传: expected={}, actual={}", request.getFileSize(), existingFile.getSysFileSystemSize());
                return ReturnResult.ok(SysFileInstantUploadResponse.notSupport());
            }
        }

        log.info("找到匹配的文件，支持秒传: fileId={}, fileName={}", existingFile.getSysFileSystemId(), existingFile.getSysFileSystemName());
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

        if (sysFileSystem.getSysFileSystemStatus() == SysFileSystem.Status.COMPLETED) {
            response.setUrl(sysFileSystem.getSysFileSystemHttpUrl());
        }

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