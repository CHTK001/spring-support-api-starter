package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.FileSystem;
import com.chua.starter.monitor.starter.entity.FileSystemGroup;
import com.chua.starter.monitor.starter.entity.FileSystemPart;
import com.chua.starter.monitor.starter.entity.FileSystemSetting;
import com.chua.starter.monitor.starter.mapper.FileSystemMapper;
import com.chua.starter.monitor.starter.pojo.FileInitUploadDTO;
import com.chua.starter.monitor.starter.pojo.FileSystemStatisticsDTO;
import com.chua.starter.monitor.starter.pojo.FileSystemStatusCountDTO;
import com.chua.starter.monitor.starter.pojo.FileUploadStatusDTO;
import com.chua.starter.monitor.starter.service.FileSystemGroupService;
import com.chua.starter.monitor.starter.service.FileSystemPartService;
import com.chua.starter.monitor.starter.service.FileSystemService;
import com.chua.starter.monitor.starter.service.FileSystemSettingService;
import com.chua.starter.monitor.starter.service.FileSystemSseService;
import com.chua.starter.monitor.starter.task.FileSystemMergeQueue;
import com.chua.starter.monitor.starter.task.FileSystemMergeTaskProcessor;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件系统管理服务实现类
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileSystemServiceImpl extends ServiceImpl<FileSystemMapper, FileSystem> implements FileSystemService {

    private final FileSystemPartService fileSystemPartService;
    private final FileSystemSettingService fileSystemSettingService;
    private final FileSystemGroupService fileSystemGroupService;
    private final FileSystemSseService fileSystemSseService;
    private final FileSystemMergeQueue mergeQueue;
    private final FileSystemMergeTaskProcessor mergeTaskProcessor;

    @Override
    public ReturnPageResult<FileSystem> pageFor(Query<FileSystem> query, FileSystem fileSystem) {
        log.info("分页查询文件列表: query={}, fileSystem={}", query, fileSystem);

        LambdaQueryWrapper<FileSystem> wrapper = new LambdaQueryWrapper<>();

        // 文件名模糊查询
        if (StringUtils.hasText(fileSystem.getFileSystemName())) {
            wrapper.like(FileSystem::getFileSystemName, fileSystem.getFileSystemName());
        }

        // 原始文件名模糊查询
        if (StringUtils.hasText(fileSystem.getFileSystemOriginalName())) {
            wrapper.like(FileSystem::getFileSystemOriginalName, fileSystem.getFileSystemOriginalName());
        }

        // 文件类型查询
        if (StringUtils.hasText(fileSystem.getFileSystemType())) {
            wrapper.eq(FileSystem::getFileSystemType, fileSystem.getFileSystemType());
        }

        // 文件状态查询
        if (fileSystem.getFileSystemStatus() != null) {
            wrapper.eq(FileSystem::getFileSystemStatus, fileSystem.getFileSystemStatus());
        }

        // HTTP访问状态查询
        if (fileSystem.getFileSystemHttpAccessEnabled() != null) {
            wrapper.eq(FileSystem::getFileSystemHttpAccessEnabled, fileSystem.getFileSystemHttpAccessEnabled());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(FileSystem::getCreateTime);

        IPage<FileSystem> page = page(query.createPage(), wrapper);
        return PageResultUtils.ok(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<FileInitUploadDTO> initChunkUpload(String fileName, Long fileSize, String fileMd5,
            Long chunkSize, Integer groupId) {
        log.info("初始化分片上传: fileName={}, fileSize={}, fileMd5={}, chunkSize={}, groupId={}", fileName, fileSize, fileMd5,
                chunkSize, groupId);

        try {
            // 检查文件是否已存在
            FileSystem existingFile = baseMapper.findByMd5(fileMd5);
            if (existingFile != null && existingFile.getFileSystemStatus() == FileSystem.FileStatus.COMPLETED) {
                log.info("文件已存在且已完成: fileId={}", existingFile.getFileSystemId());
                FileInitUploadDTO result = FileInitUploadDTO.builder().fileId(existingFile.getFileSystemId())
                        .fileName(existingFile.getFileSystemName()).fileSize(existingFile.getFileSystemSize())
                        .fileMd5(existingFile.getFileSystemMd5()).chunkTotal(existingFile.getFileSystemChunkTotal())
                        .chunkSize(0L).exists(true).message("文件已存在").build();
                return ReturnResult.ok(result);
            }

            // 获取系统配置
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null) {
                return ReturnResult.error("文件系统配置未启用");
            }

            // 检查文件大小限制
            if (setting.getFileSystemSettingMaxFileSizeMb() != null
                    && fileSize > setting.getFileSystemSettingMaxFileSizeMb() * 1024 * 1024L) {
                return ReturnResult.error("文件大小超过限制: " + setting.getFileSystemSettingMaxFileSizeMb() + "MB");
            }

            // 检查文件类型白名单
            if (StringUtils.hasText(setting.getFileSystemSettingAllowedFileTypes())) {
                String fileExtension = getFileExtension(fileName);
                if (StringUtils.hasText(fileExtension)) {
                    String[] allowedTypes = setting.getFileSystemSettingAllowedFileTypes().split(",");
                    boolean isAllowed = Arrays.stream(allowedTypes).map(String::trim)
                            .anyMatch(type -> type.equalsIgnoreCase(fileExtension));
                    if (!isAllowed) {
                        return ReturnResult.error("不支持的文件类型: " + fileExtension);
                    }
                }
            }

            // 计算分片数量
            long defaultChunkSize = setting.getFileSystemSettingChunkSizeMb() * 1024 * 1024L;
            long actualChunkSize = chunkSize > 0 ? chunkSize : defaultChunkSize;
            int chunkTotal = (int) Math.ceil((double) fileSize / actualChunkSize);

            // 处理文件分组
            Integer finalGroupId = processFileGroup(groupId, fileName);

            // 创建文件记录
            FileSystem fileSystem = new FileSystem();
            fileSystem.setFileSystemName(generateFileName(fileName));
            fileSystem.setFileSystemOriginalName(fileName);
            fileSystem.setFileSystemSize(fileSize);
            fileSystem.setFileSystemMd5(fileMd5);
            fileSystem.setFileSystemType(getFileType(fileName));
            fileSystem.setFileSystemExtension(getFileExtension(fileName));
            fileSystem.setFileSystemChunkTotal(chunkTotal);
            fileSystem.setFileSystemChunkUploaded(0);
            fileSystem.setFileSystemStatus(FileSystem.FileStatus.PENDING_MERGE);
            fileSystem.setFileSystemHttpAccessEnabled(false);
            fileSystem.setFileSystemUploadStartTime(LocalDateTime.now());
            fileSystem.setFileSystemGroupId(finalGroupId);

            // 生成文件存储路径
            String storagePath = generateStoragePath(setting.getFileSystemSettingStorageRootPath(),
                    fileSystem.getFileSystemName(), finalGroupId);
            fileSystem.setFileSystemPath(storagePath);

            save(fileSystem);

            // 创建分片记录
            for (int i = 0; i < chunkTotal; i++) {
                FileSystemPart part = new FileSystemPart();
                part.setFileSystemPartFileId(fileSystem.getFileSystemId());
                part.setFileSystemPartChunkNumber(i);
                part.setFileSystemPartChunkStatus(FileSystemPart.ChunkStatus.PENDING);
                part.setFileSystemPartRetryCount(0);
                fileSystemPartService.save(part);
            }

            FileInitUploadDTO result = FileInitUploadDTO.builder().fileId(fileSystem.getFileSystemId())
                    .chunkTotal(chunkTotal).chunkSize(actualChunkSize).fileName(fileSystem.getFileSystemName())
                    .fileSize(fileSize).fileMd5(fileMd5).build();

            // 发送文件创建消息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileId", fileSystem.getFileSystemId());
            fileInfo.put("fileName", fileSystem.getFileSystemName());
            fileInfo.put("originalName", fileSystem.getFileSystemOriginalName());
            fileInfo.put("fileSize", fileSystem.getFileSystemSize());
            fileInfo.put("chunkTotal", chunkTotal);
            fileInfo.put("status", "initialized");
            fileSystemSseService.sendFileCreated(fileInfo);

            log.info("分片上传初始化成功: fileId={}, chunkTotal={}", fileSystem.getFileSystemId(), chunkTotal);
            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("初始化分片上传失败", e);
            return ReturnResult.error("初始化分片上传失败: " + e.getMessage());
        }
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String originalName) {
        String extension = getFileExtension(originalName);
        String baseName = originalName;
        if (StringUtils.hasText(extension)) {
            baseName = originalName.substring(0, originalName.lastIndexOf('.'));
        }
        String timestamp = String.valueOf(System.currentTimeMillis());
        return baseName + "_" + timestamp + (StringUtils.hasText(extension) ? "." + extension : "");
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String fileName) {
        String extension = getFileExtension(fileName);
        if (!StringUtils.hasText(extension)) {
            return "unknown";
        }

        // 根据扩展名判断文件类型
        switch (extension.toLowerCase()) {
        case "jpg":
        case "jpeg":
        case "png":
        case "gif":
        case "bmp":
        case "webp":
            return "image";
        case "mp4":
        case "avi":
        case "mov":
        case "wmv":
        case "flv":
        case "mkv":
            return "video";
        case "mp3":
        case "wav":
        case "flac":
        case "aac":
        case "ogg":
            return "audio";
        case "pdf":
            return "pdf";
        case "doc":
        case "docx":
            return "word";
        case "xls":
        case "xlsx":
            return "excel";
        case "ppt":
        case "pptx":
            return "powerpoint";
        case "txt":
            return "text";
        case "zip":
        case "rar":
        case "7z":
        case "tar":
        case "gz":
            return "archive";
        default:
            return "other";
        }
    }

    /**
     * 处理文件分组 如果没有指定分组或分组不存在，则根据文件类型自动分配到默认分组
     */
    private Integer processFileGroup(Integer groupId, String fileName) {
        // 如果指定了分组ID，验证分组是否存在且启用
        if (groupId != null) {
            FileSystemGroup group = fileSystemGroupService.getById(groupId);
            if (group != null && group.getFileSystemGroupStatus() == FileSystemGroup.GroupStatus.ENABLED) {
                log.info("使用指定分组: groupId={}, groupName={}", groupId, group.getFileSystemGroupName());
                return groupId;
            } else {
                log.warn("指定的分组不存在或已禁用: groupId={}", groupId);
            }
        }

        // 根据文件类型自动分配到默认分组
        String fileExtension = getFileExtension(fileName);
        Integer defaultGroupId = fileSystemGroupService.getGroupByFileType(fileExtension);
        if (defaultGroupId != null) {
            log.info("自动分配到默认分组: fileExtension={}, groupId={}", fileExtension, defaultGroupId);
            return defaultGroupId;
        }

        log.warn("无法确定文件分组，使用null: fileName={}", fileName);
        return null;
    }

    /**
     * 生成存储路径（不带分组）
     */
    private String generateStoragePath(String rootPath, String fileName) {
        return generateStoragePath(rootPath, fileName, null);
    }

    /**
     * 生成存储路径（支持分组）
     */
    private String generateStoragePath(String rootPath, String fileName, Integer groupId) {
        if (!StringUtils.hasText(rootPath)) {
            rootPath = System.getProperty("java.io.tmpdir") + File.separator + "filesystem";
        }

        StringBuilder pathBuilder = new StringBuilder(rootPath);

        // 如果有分组，添加分组路径
        if (groupId != null) {
            FileSystemGroup group = fileSystemGroupService.getById(groupId);
            if (group != null && StringUtils.hasText(group.getFileSystemGroupFullPath())) {
                pathBuilder.append(File.separator).append(group.getFileSystemGroupFullPath());
            }
        }

        // 按日期分目录存储
        LocalDateTime now = LocalDateTime.now();
        String datePath = String.format("%04d%s%02d%s%02d", now.getYear(), File.separator, now.getMonthValue(),
                File.separator, now.getDayOfMonth());

        pathBuilder.append(File.separator).append(datePath).append(File.separator).append(fileName);

        return pathBuilder.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> uploadChunk(Integer fileId, Integer chunkNumber, MultipartFile chunkFile,
            String chunkMd5) {
        log.info("上传文件分片: fileId={}, chunkNumber={}, chunkMd5={}, fileName={}, isEmpty={}", fileId, chunkNumber,
                chunkMd5, chunkFile.getOriginalFilename(), chunkFile.isEmpty());

        try {
            // 基本参数验证
            if (chunkFile == null || chunkFile.isEmpty()) {
                return ReturnResult.error("分片文件为空");
            }
            // 检查文件是否存在
            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            // 检查分片是否存在
            FileSystemPart part = fileSystemPartService.findByFileIdAndChunkNumber(fileId, chunkNumber);
            if (part == null) {
                return ReturnResult.error("分片记录不存在");
            }

            // 检查分片是否已完成
            if (part.getFileSystemPartChunkStatus() == FileSystemPart.ChunkStatus.COMPLETED) {
                log.info("分片已完成: fileId={}, chunkNumber={}", fileId, chunkNumber);
                return ReturnResult.ok(true);
            }

            // 获取系统配置
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null) {
                return ReturnResult.error("文件系统配置未启用");
            }

            // 生成分片存储路径
            String tempPath = setting.getFileSystemSettingTempStoragePath();
            if (!StringUtils.hasText(tempPath)) {
                tempPath = System.getProperty("java.io.tmpdir") + File.separator + "filesystem" + File.separator
                        + "temp";
            }

            String chunkFileName = fileId + "_" + chunkNumber + ".chunk";
            Path chunkPath = Paths.get(tempPath, chunkFileName);

            // 创建目录
            Files.createDirectories(chunkPath.getParent());

            // 获取分片大小（在transferTo之前，避免临时文件被删除）
            long chunkSize;
            try {
                chunkSize = chunkFile.getSize();
            } catch (Exception e) {
                log.warn("无法获取分片文件大小，使用默认值: fileId={}, chunkNumber={}", fileId, chunkNumber, e);
                chunkSize = 0L; // 使用默认值，后续会通过保存的文件获取实际大小
            }

            // 保存分片文件
            try {
                chunkFile.transferTo(chunkPath.toFile());
            } catch (IOException e) {
                log.error("保存分片文件失败: fileId={}, chunkNumber={}", fileId, chunkNumber, e);
                throw e; // 重新抛出异常，让外层catch处理
            }

            // 如果之前无法获取大小，现在从保存的文件获取
            if (chunkSize <= 0) {
                try {
                    chunkSize = Files.size(chunkPath);
                } catch (IOException e) {
                    log.error("无法获取保存后的分片文件大小: fileId={}, chunkNumber={}", fileId, chunkNumber, e);
                    chunkSize = 0L;
                }
            }

            // 验证分片MD5
            if (StringUtils.hasText(chunkMd5)) {
                String actualMd5 = calculateFileMd5(chunkPath.toFile());
                if (!chunkMd5.equalsIgnoreCase(actualMd5)) {
                    log.error("分片MD5验证失败: fileId={}, chunkNumber={}, expected={}, actual={}", fileId, chunkNumber,
                            chunkMd5, actualMd5);
                    // 删除已保存的分片文件
                    try {
                        Files.deleteIfExists(chunkPath);
                    } catch (IOException deleteEx) {
                        log.warn("删除MD5验证失败的分片文件失败: {}", chunkPath, deleteEx);
                    }
                    return ReturnResult.error("分片MD5验证失败");
                }
            }

            // 更新分片记录
            part.setFileSystemPartChunkSize(chunkSize);
            part.setFileSystemPartChunkMd5(chunkMd5);
            part.setFileSystemPartChunkPath(chunkPath.toString());
            part.setFileSystemPartChunkStatus(FileSystemPart.ChunkStatus.COMPLETED);
            part.setFileSystemPartUploadTime(LocalDateTime.now());
            fileSystemPartService.updateById(part);

            // 更新文件的已上传分片数
            int completedChunks = fileSystemPartService.countCompletedChunks(fileId);
            baseMapper.updateChunkUploaded(fileId, completedChunks);

            // 计算并发送上传进度
            double progress = fileSystem.getFileSystemChunkTotal() > 0
                    ? ((double) completedChunks / fileSystem.getFileSystemChunkTotal() * 100)
                    : 0;
            fileSystemSseService.sendUploadProgress(fileId, progress,
                    String.format("已上传 %d/%d 个分片", completedChunks, fileSystem.getFileSystemChunkTotal()));

            // 检查是否所有分片都已上传完成
            if (completedChunks == fileSystem.getFileSystemChunkTotal()) {
                log.info("所有分片上传完成，开始合并处理: fileId={}", fileId);
                // 更新文件状态为待合并
                baseMapper.updateStatus(fileId, FileSystem.FileStatus.PENDING_MERGE);
                // 发送上传完成消息
                fileSystemSseService.sendFileOperationResult("upload", fileId, true, "文件上传完成，开始合并");

                // 无论是否支持手动合并，都将文件加入自动合并队列
                log.info("文件上传完成，加入自动合并队列: fileId={}", fileId);
                mergeQueue.enqueue(fileId);
            }

            log.info("分片上传成功: fileId={}, chunkNumber={}, completedChunks={}/{}", fileId, chunkNumber, completedChunks,
                    fileSystem.getFileSystemChunkTotal());
            return ReturnResult.ok(true);

        } catch (IOException e) {
            log.error("分片上传失败: fileId={}, chunkNumber={}", fileId, chunkNumber, e);
            return ReturnResult.error("分片上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("分片上传异常: fileId={}, chunkNumber={}", fileId, chunkNumber, e);
            return ReturnResult.error("分片上传异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileUploadStatusDTO> checkUploadStatus(Integer fileId) {
        log.info("检查上传状态: fileId={}", fileId);

        try {
            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            double progress = fileSystem.getFileSystemChunkTotal() > 0
                    ? ((double) fileSystem.getFileSystemChunkUploaded() / fileSystem.getFileSystemChunkTotal() * 100)
                    : 0;

            // 查询缺失和失败的分片
            List<FileSystemPart> parts = fileSystemPartService
                    .list(new LambdaQueryWrapper<FileSystemPart>().eq(FileSystemPart::getFileSystemPartFileId, fileId)
                            .orderByAsc(FileSystemPart::getFileSystemPartChunkNumber));

            List<Integer> missingChunks = new ArrayList<>();
            List<Integer> failedChunks = new ArrayList<>();

            for (FileSystemPart part : parts) {
                if (part.getFileSystemPartChunkStatus() == FileSystemPart.ChunkStatus.PENDING) {
                    missingChunks.add(part.getFileSystemPartChunkNumber());
                } else if (part.getFileSystemPartChunkStatus() == FileSystemPart.ChunkStatus.FAILED) {
                    failedChunks.add(part.getFileSystemPartChunkNumber());
                }
            }

            boolean canMerge = fileSystem.getFileSystemChunkUploaded().equals(fileSystem.getFileSystemChunkTotal())
                    && fileSystem.getFileSystemStatus() == FileSystem.FileStatus.PENDING_MERGE;

            FileUploadStatusDTO result = FileUploadStatusDTO.builder().fileId(fileId)
                    .fileName(fileSystem.getFileSystemOriginalName()).fileStatus(fileSystem.getFileSystemStatus())
                    .chunkTotal(fileSystem.getFileSystemChunkTotal())
                    .chunkUploaded(fileSystem.getFileSystemChunkUploaded()).uploadProgress(progress)
                    .uploadStartTime(fileSystem.getFileSystemUploadStartTime())
                    .uploadCompleteTime(fileSystem.getFileSystemUploadCompleteTime()).missingChunks(missingChunks)
                    .failedChunks(failedChunks).canMerge(canMerge).build();

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("检查上传状态失败: fileId={}", fileId, e);
            return ReturnResult.error("检查上传状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> manualMergeFile(Integer fileId) {
        log.info("手动触发文件合并: fileId={}", fileId);

        try {
            // 检查是否支持手动合并功能
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null || !Boolean.TRUE.equals(setting.getFileSystemSettingManualMergeEnabled())) {
                return ReturnResult.error("手动合并功能未启用");
            }

            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            if (fileSystem.getFileSystemStatus() == FileSystem.FileStatus.COMPLETED) {
                return ReturnResult.error("文件已完成合并");
            }

            if (fileSystem.getFileSystemStatus() == FileSystem.FileStatus.MERGING) {
                return ReturnResult.error("文件正在合并中");
            }

            // 检查是否所有分片都已上传
            int completedChunks = fileSystemPartService.countCompletedChunks(fileId);
            if (completedChunks != fileSystem.getFileSystemChunkTotal()) {
                return ReturnResult.error("分片上传未完成，无法合并");
            }

            // 更新状态为待合并
            baseMapper.updateStatus(fileId, FileSystem.FileStatus.PENDING_MERGE);

            // 手动合并直接调用合并处理器，不依赖定时任务
            log.info("手动合并直接调用合并处理器: fileId={}", fileId);
            mergeTaskProcessor.processMergeFileAsync(fileId);

            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("手动合并文件失败: fileId={}", fileId, e);
            return ReturnResult.error("手动合并文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteFile(Integer fileId) {
        log.info("删除文件: fileId={}", fileId);

        try {
            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            // 删除物理文件
            if (StringUtils.hasText(fileSystem.getFileSystemPath())) {
                try {
                    Files.deleteIfExists(Paths.get(fileSystem.getFileSystemPath()));
                } catch (IOException e) {
                    log.warn("删除物理文件失败: {}", fileSystem.getFileSystemPath(), e);
                }
            }

            // 删除分片文件
            List<FileSystemPart> parts = fileSystemPartService.findByFileId(fileId);
            for (FileSystemPart part : parts) {
                if (StringUtils.hasText(part.getFileSystemPartChunkPath())) {
                    try {
                        Files.deleteIfExists(Paths.get(part.getFileSystemPartChunkPath()));
                    } catch (IOException e) {
                        log.warn("删除分片文件失败: {}", part.getFileSystemPartChunkPath(), e);
                    }
                }
            }

            // 删除数据库记录
            fileSystemPartService.deleteByFileId(fileId);
            removeById(fileId);

            // 发送文件删除消息
            fileSystemSseService.sendFileDeleted(fileId, fileSystem.getFileSystemName());
            fileSystemSseService.sendFileOperationResult("delete", fileId, true, "文件删除成功");

            log.info("文件删除成功: fileId={}", fileId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("删除文件失败: fileId={}", fileId, e);
            return ReturnResult.error("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> setFileGroup(Integer fileId, Integer groupId) {
        log.info("设置文件分组: fileId={}, groupId={}", fileId, groupId);

        try {
            // 检查文件是否存在
            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            // 如果指定了分组ID，验证分组是否存在且启用
            if (groupId != null) {
                FileSystemGroup group = fileSystemGroupService.getById(groupId);
                if (group == null) {
                    return ReturnResult.error("指定的分组不存在");
                }
                if (group.getFileSystemGroupStatus() != FileSystemGroup.GroupStatus.ENABLED) {
                    return ReturnResult.error("指定的分组已禁用");
                }
            }

            // 更新文件分组
            fileSystem.setFileSystemGroupId(groupId);
            boolean updated = updateById(fileSystem);

            if (updated) {
                String message = groupId != null ? "文件分组设置成功" : "文件分组移除成功";
                log.info("{}: fileId={}, groupId={}", message, fileId, groupId);
                return ReturnResult.ok(true, message);
            } else {
                return ReturnResult.error("设置文件分组失败");
            }

        } catch (Exception e) {
            log.error("设置文件分组失败: fileId={}, groupId={}", fileId, groupId, e);
            return ReturnResult.error("设置分组失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchDeleteFiles(List<Integer> fileIds) {
        log.info("批量删除文件: fileIds={}", fileIds);

        if (fileIds == null || fileIds.isEmpty()) {
            return ReturnResult.error("文件ID列表不能为空");
        }

        int successCount = 0;
        int failCount = 0;

        for (Integer fileId : fileIds) {
            try {
                ReturnResult<Boolean> result = deleteFile(fileId);
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failCount++;
                    log.warn("删除文件失败: fileId={}, error={}", fileId, result.getMsg());
                }
            } catch (Exception e) {
                failCount++;
                log.error("删除文件异常: fileId={}", fileId, e);
            }
        }

        log.info("批量删除文件完成: 成功={}, 失败={}", successCount, failCount);

        if (failCount == 0) {
            return ReturnResult.ok(true);
        } else if (successCount == 0) {
            return ReturnResult.error("所有文件删除失败");
        } else {
            return ReturnResult.ok(true, "部分文件删除成功: 成功=" + successCount + ", 失败=" + failCount);
        }
    }

    @Override
    public ReturnResult<byte[]> downloadFile(Integer fileId) {
        log.info("下载文件: fileId={}", fileId);

        try {
            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            if (fileSystem.getFileSystemStatus() != FileSystem.FileStatus.COMPLETED) {
                return ReturnResult.error("文件未完成，无法下载");
            }

            // 检查下载权限
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null || !Boolean.TRUE.equals(setting.getFileSystemSettingDownloadEnabled())) {
                return ReturnResult.error("文件下载功能未启用");
            }

            if (!StringUtils.hasText(fileSystem.getFileSystemPath())) {
                return ReturnResult.error("文件路径不存在");
            }

            Path filePath = Paths.get(fileSystem.getFileSystemPath());
            if (!Files.exists(filePath)) {
                return ReturnResult.error("物理文件不存在");
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            log.info("文件下载成功: fileId={}, size={}", fileId, fileBytes.length);
            return ReturnResult.ok(fileBytes);

        } catch (IOException e) {
            log.error("文件下载失败: fileId={}", fileId, e);
            return ReturnResult.error("文件下载失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件下载异常: fileId={}", fileId, e);
            return ReturnResult.error("文件下载异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> getHttpAccessUrl(Integer fileId) {
        log.info("获取文件HTTP访问URL: fileId={}", fileId);

        try {
            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            if (fileSystem.getFileSystemStatus() != FileSystem.FileStatus.COMPLETED) {
                return ReturnResult.error("文件未完成，无法访问");
            }

            if (!Boolean.TRUE.equals(fileSystem.getFileSystemHttpAccessEnabled())) {
                return ReturnResult.error("文件HTTP访问未启用");
            }

            // 检查系统配置
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null || !Boolean.TRUE.equals(setting.getFileSystemSettingHttpAccessEnabled())) {
                return ReturnResult.error("系统HTTP访问功能未启用");
            }

            if (StringUtils.hasText(fileSystem.getFileSystemHttpAccessUrl())) {
                return ReturnResult.ok(fileSystem.getFileSystemHttpAccessUrl());
            }

            // 生成HTTP访问URL
            String domain = setting.getFileSystemSettingHttpAccessDomain();
            if (!StringUtils.hasText(domain)) {
                domain = "http://localhost:8080";
            }

            String accessUrl = domain + "/api/v1/filesystem/access/" + fileId;

            // 更新文件记录
            fileSystem.setFileSystemHttpAccessUrl(accessUrl);
            updateById(fileSystem);

            return ReturnResult.ok(accessUrl);
        } catch (Exception e) {
            log.error("获取HTTP访问URL失败: fileId={}", fileId, e);
            return ReturnResult.error("获取HTTP访问URL失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> toggleHttpAccess(Integer fileId, Boolean enabled) {
        log.info("切换文件HTTP访问状态: fileId={}, enabled={}", fileId, enabled);

        try {
            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            if (fileSystem.getFileSystemStatus() != FileSystem.FileStatus.COMPLETED) {
                return ReturnResult.error("文件未完成，无法设置HTTP访问");
            }

            // 检查系统配置
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null || !Boolean.TRUE.equals(setting.getFileSystemSettingHttpAccessEnabled())) {
                return ReturnResult.error("系统HTTP访问功能未启用");
            }

            fileSystem.setFileSystemHttpAccessEnabled(enabled);

            // 如果启用HTTP访问且URL为空，则生成URL
            if (Boolean.TRUE.equals(enabled) && !StringUtils.hasText(fileSystem.getFileSystemHttpAccessUrl())) {
                String domain = setting.getFileSystemSettingHttpAccessDomain();
                if (!StringUtils.hasText(domain)) {
                    domain = "http://localhost:8080";
                }
                String accessUrl = domain + "/api/v1/filesystem/access/" + fileId;
                fileSystem.setFileSystemHttpAccessUrl(accessUrl);
            }

            updateById(fileSystem);

            log.info("文件HTTP访问状态更新成功: fileId={}, enabled={}", fileId, enabled);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("切换文件HTTP访问状态失败: fileId={}, enabled={}", fileId, enabled, e);
            return ReturnResult.error("切换文件HTTP访问状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileSystemStatisticsDTO> getFileStatistics() {
        log.info("获取文件统计信息");

        try {
            // 总文件数
            long totalFiles = count();

            // 各状态文件数量
            List<FileSystemStatusCountDTO> statusCounts = baseMapper.countByStatus();
            log.info("状态统计查询结果: {}", statusCounts);
            long pendingFiles = 0;
            long mergingFiles = 0;
            long completedFiles = 0;
            long failedFiles = 0;

            for (FileSystemStatusCountDTO statusCount : statusCounts) {
                Integer status = statusCount.getFileSystemStatus();
                Long count = statusCount.getCount();

                // 检查status是否为null，避免NullPointerException
                if (status == null) {
                    log.warn("发现文件状态为null的记录，跳过统计: status={}, count={}", status, count);
                    continue;
                }

                // 检查count是否为null，避免NullPointerException
                if (count == null) {
                    log.warn("发现文件数量为null的记录，跳过统计: status={}, count={}", status, count);
                    continue;
                }

                switch (status) {
                case FileSystem.FileStatus.PENDING_MERGE:
                    pendingFiles = count;
                    break; // 待合并
                case FileSystem.FileStatus.MERGING:
                    mergingFiles = count;
                    break; // 合并中
                case FileSystem.FileStatus.COMPLETED:
                    completedFiles = count;
                    break; // 已完成
                case FileSystem.FileStatus.MERGE_FAILED:
                    failedFiles = count;
                    break; // 合并失败
                default:
                    log.warn("发现未知的文件状态: {}, count: {}, statusName: {}", status, count, statusCount.getStatusName());
                    break;
                }
            }

            // 总文件大小（所有文件）
            List<FileSystem> allFiles = list();
            log.info("查询到的所有文件数量: {}", allFiles.size());
            long totalSize = allFiles.stream().mapToLong(f -> f.getFileSystemSize() != null ? f.getFileSystemSize() : 0)
                    .sum();
            log.info("计算得到的总文件大小: {} bytes", totalSize);

            // HTTP访问启用的文件数
            long httpAccessEnabledFiles = count(
                    new LambdaQueryWrapper<FileSystem>().eq(FileSystem::getFileSystemHttpAccessEnabled, true));

            // 今日上传文件统计
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LambdaQueryWrapper<FileSystem> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.ge(FileSystem::getCreateTime, todayStart);
            List<FileSystem> todayFiles = list(todayWrapper);
            long todayUploadFiles = todayFiles.size();
            long todayUploadSize = todayFiles.stream()
                    .mapToLong(f -> f.getFileSystemSize() != null ? f.getFileSystemSize() : 0).sum();

            // 平均文件大小
            long averageFileSize = totalFiles > 0 ? totalSize / totalFiles : 0;

            FileSystemStatisticsDTO statistics = FileSystemStatisticsDTO.builder().totalFiles(totalFiles)
                    .totalSize(totalSize).pendingFiles(pendingFiles).mergingFiles(mergingFiles)
                    .completedFiles(completedFiles).failedFiles(failedFiles)
                    .httpAccessEnabledFiles(httpAccessEnabledFiles).todayUploadFiles(todayUploadFiles)
                    .todayUploadSize(todayUploadSize).averageFileSize(averageFileSize).storageUsageRate(0.0) // 存储使用率需要根据实际存储配置计算
                    .build();

            log.info(
                    "最终统计结果: totalFiles={}, totalSize={}, pendingFiles={}, mergingFiles={}, completedFiles={}, failedFiles={}",
                    totalFiles, totalSize, pendingFiles, mergingFiles, completedFiles, failedFiles);

            return ReturnResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取文件统计信息失败", e);
            return ReturnResult.error("获取文件统计信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> cleanExpiredFiles() {
        log.info("清理过期文件");

        try {
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null) {
                return ReturnResult.error("文件系统配置未启用");
            }

            Integer retentionDays = setting.getFileSystemSettingFileRetentionDays();
            if (retentionDays == null || retentionDays <= 0) {
                return ReturnResult.ok(0, "文件永久保留，无需清理");
            }

            int deletedCount = baseMapper.deleteExpiredFiles(retentionDays);

            log.info("过期文件清理完成: 删除数量={}", deletedCount);
            return ReturnResult.ok(deletedCount);
        } catch (Exception e) {
            log.error("清理过期文件失败", e);
            return ReturnResult.error("清理过期文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> retryMergeTask(Integer fileId) {
        log.info("重试合并任务: fileId={}", fileId);

        try {
            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            if (fileSystem.getFileSystemStatus() != FileSystem.FileStatus.MERGE_FAILED) {
                return ReturnResult.error("只能重试合并失败的文件");
            }

            // 重置状态为待合并
            baseMapper.updateStatus(fileId, FileSystem.FileStatus.PENDING_MERGE);

            // 重试合并直接调用合并处理器
            log.info("重试合并直接调用合并处理器: fileId={}", fileId);
            mergeTaskProcessor.processMergeFileAsync(fileId);

            log.info("合并任务重试成功: fileId={}", fileId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("重试合并任务失败: fileId={}", fileId, e);
            return ReturnResult.error("重试合并任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileSystem> getFileDetail(Integer fileId) {
        log.info("获取文件详情: fileId={}", fileId);

        try {
            FileSystem fileSystem = getById(fileId);
            if (fileSystem == null) {
                return ReturnResult.error("文件不存在");
            }

            return ReturnResult.ok(fileSystem);
        } catch (Exception e) {
            log.error("获取文件详情失败: fileId={}", fileId, e);
            return ReturnResult.error("获取文件详情失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileSystem> checkFileExists(String fileMd5) {
        log.info("检查文件是否存在: fileMd5={}", fileMd5);

        try {
            if (!StringUtils.hasText(fileMd5)) {
                return ReturnResult.error("文件MD5不能为空");
            }

            FileSystem fileSystem = baseMapper.findByMd5(fileMd5);
            if (fileSystem != null) {
                return ReturnResult.ok(fileSystem);
            } else {
                return ReturnResult.ok(null, "文件不存在");
            }
        } catch (Exception e) {
            log.error("检查文件是否存在失败: fileMd5={}", fileMd5, e);
            return ReturnResult.error("检查文件是否存在失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getStorageStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 文件总数统计
            long totalFiles = count();
            statistics.put("totalFiles", totalFiles);

            // 按状态统计文件数量
            Map<String, Long> statusCounts = new HashMap<>();
            statusCounts.put("pendingMerge",
                    lambdaQuery().eq(FileSystem::getFileSystemStatus, FileSystem.FileStatus.PENDING_MERGE).count());
            statusCounts.put("merging",
                    lambdaQuery().eq(FileSystem::getFileSystemStatus, FileSystem.FileStatus.MERGING).count());
            statusCounts.put("completed",
                    lambdaQuery().eq(FileSystem::getFileSystemStatus, FileSystem.FileStatus.COMPLETED).count());
            statusCounts.put("mergeFailed",
                    lambdaQuery().eq(FileSystem::getFileSystemStatus, FileSystem.FileStatus.MERGE_FAILED).count());
            statistics.put("statusCounts", statusCounts);

            // 存储空间统计
            List<FileSystem> completedFiles = lambdaQuery()
                    .eq(FileSystem::getFileSystemStatus, FileSystem.FileStatus.COMPLETED)
                    .select(FileSystem::getFileSystemSize).list();

            long totalStorageUsed = completedFiles.stream()
                    .mapToLong(file -> file.getFileSystemSize() != null ? file.getFileSystemSize() : 0L).sum();

            statistics.put("totalStorageUsed", totalStorageUsed);
            statistics.put("totalStorageUsedMB", totalStorageUsed / (1024 * 1024));

            // 分片统计
            long totalParts = fileSystemPartService.count();
            long completedParts = fileSystemPartService.lambdaQuery()
                    .eq(FileSystemPart::getFileSystemPartChunkStatus, FileSystemPart.ChunkStatus.COMPLETED).count();
            statistics.put("totalParts", totalParts);
            statistics.put("completedParts", completedParts);
            statistics.put("pendingParts", totalParts - completedParts);

            // 上传进度统计
            if (totalParts > 0) {
                double overallProgress = (double) completedParts / totalParts * 100;
                statistics.put("overallProgress", Math.round(overallProgress * 100.0) / 100.0);
            } else {
                statistics.put("overallProgress", 0.0);
            }

            statistics.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("获取存储统计信息失败", e);
            statistics.put("error", "获取统计信息失败: " + e.getMessage());
        }

        return statistics;
    }

    /**
     * 计算文件MD5值
     */
    private String calculateFileMd5(File file) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] digest = md5.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("计算文件MD5失败: {}", file.getPath(), e);
            return null;
        }
    }

}
