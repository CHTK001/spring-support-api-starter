// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.storage.FileStorage;
import com.chua.common.support.storage.request.PutObjectRequest;
import com.chua.common.support.storage.result.PutObjectResult;
import com.chua.common.support.storage.setting.BucketSetting;
import com.chua.common.support.lang.code.ReturnResult;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
* @版本 1.0.0
 */
@Slf4j
@Service
public class SysFileSystemServiceImpl extends ServiceImpl<SysFileSystemMapper, SysFileSystem> implements SysFileSystemService {

    /**
    * 默认分片大小：100MB
     */
    private static final long DEFAULT_CHUNK_SIZE = 1024 * 1024 * 100L;
    private final TransactionTemplate transactionTemplate; // transactionTemplate
    private final SysFileSystemPartService sysFileSystemPartService; // sys文件系统part服务
    private final SysFileSystemSettingService sysFileSystemSettingService; // sys文件系统setting服务
    private final FileSystemProperties fileSystemProperties; // 文件系统属性
    private final SysFileSystemMergeTaskRegistration mergeTaskRegistration; // 合并任务registration
    private final FileStorageSupport fileStorageSupport; // 文件storage支持

    // 原先有个 @Autowired(required = false) 的 FileUploadProgressPushService 可选依赖，
    // 用于分片上传时向前端推进度。但该类型在 HEAD 与 MERGE_HEAD 里都不存在（不是合并丢文件，
    // 是上游提交本身缺这个类），保留它就编译不过；它又是 required=false + null 判空，
    // 任何情况下注入值只能是 null，那段 if 块不可达，故整段移除。
    // 等真正实现了推送通道（WebSocket / SSE / MQ）再按原样加回。

    @Autowired
    public SysFileSystemServiceImpl(TransactionTemplate transactionTemplate,
                                    SysFileSystemPartService sysFileSystemPartService,
                                    SysFileSystemSettingService sysFileSystemSettingService,
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

    @Override
    public ReturnResult<PutObjectResult> uploadForFileSystem(SysFileUploadRequest request, MultipartFile multipartFile) {
        log.info("开始上传文件: fileName={}, bucket={}", multipartFile.getOriginalFilename(), request.getFileBucket());
        BucketSetting bucketSetting = createSetting(request, multipartFile.getOriginalFilename());

        try (FileStorage fileStorage = fileStorageSupport.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.ok(fileStorage.putObject(PutObjectRequest.builder()
                    .content(multipartFile.getInputStream().readAllBytes())
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createSimpleUuid() + "." + FileUtils.getExtension(multipartFile.getOriginalFilename())))
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

        try (FileStorage fileStorage = fileStorageSupport.createStorage(request.getOssType(), bucketSetting)) {
            return ReturnResult.ok(fileStorage.putObject(PutObjectRequest.builder()
                    .file(file)
                    .fileName(StringUtils.defaultString(request.getFileName(), IdUtils.createSimpleUuid() + "." + FileUtils.getExtension(file.getName())))
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
            return ReturnResult.ok(fileStorage.putObject(PutObjectRequest.builder()
                    .content(bytes)
                    .fileName(StringUtils.defaultString(request.getFileName(), fileName))
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
            SysFileUploadTaskResponse response = new SysFileUploadTaskResponse(
                    taskUid,
                    chunkCount,
                    chunkSize,
                    UrlUtils.normalize(request.getFileBucket(), sysFileSystem.getSysFileSystemName()));
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
        log.info("上传分片: taskId={}, seq={}", request.taskId(), request.taskSeq());

        SysFileSystem sysFileSystem = getOne(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemTid, request.taskId())
        );

        if (sysFileSystem == null) {
            log.warn("任务不存在: taskId={}", request.taskId());
            return ReturnResult.error("任务不存在");
        }

        if (sysFileSystem.getSysFileSystemStatus() == SysFileSystem.Status.COMPLETED) {
            log.warn("任务已完成: taskId={}", request.taskId());
            return ReturnResult.error("任务已上传完成");
        }

        SysFileSystemPart part = sysFileSystemPartService.getOne(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                .eq(SysFileSystemPart::getSysFileSystemPartSeq, request.taskSeq() + 1)
        );

        if (part == null) {
            log.warn("分片不存在: taskId={}, seq={}", request.taskId(), request.taskSeq());
            return ReturnResult.error("分片不存在");
        }

        if (part.getSysFileSystemPartStatus() == SysFileSystemPart.Status.COMPLETED) {
            log.warn("分片已上传: taskId={}, seq={}", request.taskId(), request.taskSeq());
            return ReturnResult.error("分片已上传");
        }

        if (part.getSysFileSystemPartStatus() == SysFileSystemPart.Status.UPLOADING) {
            log.warn("分片正在上传: taskId={}, seq={}", request.taskId(), request.taskSeq());
            return ReturnResult.error("分片正在上传中");
        }

        // 更新分片状态
        part.setSysFileSystemPartStatus(SysFileSystemPart.Status.COMPLETED);
        part.setSysFileSystemPartMd5(request.fileMd5());
        part.setSysFileSystemPartLocalPath(FileUtils.normalize(fileSystemProperties.getTempPath(), request.taskId(), request.taskSeq() + ""));

        try {
            part.transferFrom(multipartFile);
        } catch (Exception e) {
            log.error("分片保存失败: taskId={}, seq={}", request.taskId(), request.taskSeq(), e);
            return ReturnResult.error("分片保存失败，请重试");
        }

        sysFileSystemPartService.updateById(part);

        // 查询已上传分片数量并推送进度
        int uploadedChunks = (int) sysFileSystemPartService.count(Wrappers.<SysFileSystemPart>lambdaQuery()
                .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED));

        // 上传进度推送：依赖的 FileUploadProgressPushService 尚不存在（见字段处说明），
        // 进度只落在日志里，前端要推进度得等推送通道实现后再接回。

        checkAndMerge(sysFileSystem);
        log.info("分片上传成功: taskId={}, seq={}, uploadedChunks={}/{}", 
                request.taskId(), request.taskSeq(), uploadedChunks, sysFileSystem.getSysFileSystemChunkTotal());
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
        log.info("检查秒传: md5={}, fileSize={}, fileName={}", request.fileMd5(), request.fileSize(), request.fileName());

        if (StringUtils.isEmpty(request.fileMd5())) {
            return ReturnResult.error("文件MD5不能为空");
        }

        // 查找已完成上传且MD5匹配的文件
        SysFileSystem existingFile = getOne(Wrappers.<SysFileSystem>lambdaQuery()
                .eq(SysFileSystem::getSysFileSystemMd5, request.fileMd5())
                .eq(SysFileSystem::getSysFileSystemStatus, SysFileSystem.Status.COMPLETED)
                .last("LIMIT 1"));

        if (existingFile == null) {
            log.info("未找到匹配的文件，不支持秒传: md5={}", request.fileMd5());
            return ReturnResult.ok(SysFileInstantUploadResponse.notSupport());
        }

        // 如果提供了文件大小，进一步验证
        if (request.fileSize() != null && request.fileSize() > 0) {
            if (!request.fileSize().equals(existingFile.getSysFileSystemSize())) {
                log.info("文件大小不匹配，不支持秒传: expected={}, actual={}", 
                        request.fileSize(), existingFile.getSysFileSystemSize());
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
            return ReturnResult.ok(notFoundTaskInfo());
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
            return ReturnResult.ok(notFoundTaskInfo());
        }

        return ReturnResult.ok(buildTaskInfoResponse(sysFileSystem));
    }

    /**
     * 构造「任务不存在」的响应
     *
     * <p>只有 {@code exists} 有值，其余组件全为 null。抽成方法是因为
     * {@link SysFileTaskInfoResponse} 是 record，必须一次性走满 11 个参数的规范构造器；
     * 在两处各写一遍 10 个 null，一旦该 record 以后加组件就会静默漏掉一处。</p>
     *
     * @return exists=false、其余组件为 null 的响应
     */
    private SysFileTaskInfoResponse notFoundTaskInfo() {
        return new SysFileTaskInfoResponse(
                null, null, null, null, null, null, null, null, null, null, Boolean.FALSE);
    }

    /**
    * 构建任务信息响应
    *
    * @param sysFileSystem 文件系统记录
    * @return 任务信息响应
     */
    private SysFileTaskInfoResponse buildTaskInfoResponse(SysFileSystem sysFileSystem) {
        // 查询已上传的分片。必须在构造之前算完：SysFileTaskInfoResponse 是 record，
        // 没有 setter，构造完就不可再改，所以不能沿用原来「先 new 再逐项 set」的写法。
        List<SysFileSystemPart> completedParts = sysFileSystemPartService.list(
                Wrappers.<SysFileSystemPart>lambdaQuery()
                        .eq(SysFileSystemPart::getSysFileSystemPartFileId, sysFileSystem.getSysFileSystemId())
                        .eq(SysFileSystemPart::getSysFileSystemPartStatus, SysFileSystemPart.Status.COMPLETED)
                        .orderByAsc(SysFileSystemPart::getSysFileSystemPartSeq));

        List<Integer> uploadedPartSeqs = completedParts.stream()
                .map(SysFileSystemPart::getSysFileSystemPartSeq)
                .toList();

        // 只有已完成才有可访问地址，其余情况为 null
        String url = sysFileSystem.getSysFileSystemStatus() == SysFileSystem.Status.COMPLETED
                ? sysFileSystem.getSysFileSystemHttpUrl()
                : null;

        // 组件顺序必须与 record 声明一致：taskId, fileName, fileMd5, fileSize, chunkSize,
        // totalChunks, uploadedChunks, uploadedParts, status, url, exists
        return new SysFileTaskInfoResponse(
                sysFileSystem.getSysFileSystemTid(),
                sysFileSystem.getSysFileSystemName(),
                sysFileSystem.getSysFileSystemMd5(),
                sysFileSystem.getSysFileSystemSize(),
                sysFileSystem.getSysFileSystemChunkSize(),
                sysFileSystem.getSysFileSystemChunkTotal(),
                uploadedPartSeqs.size(),
                uploadedPartSeqs,
                sysFileSystem.getSysFileSystemStatus(),
                url,
                Boolean.TRUE);
    }
}