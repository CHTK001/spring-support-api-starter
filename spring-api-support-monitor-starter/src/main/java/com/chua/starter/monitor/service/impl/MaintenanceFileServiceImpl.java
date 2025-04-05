package com.chua.starter.monitor.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.IdUtils;
import com.chua.starter.monitor.client.SshClientManager;
import com.chua.starter.monitor.entity.MaintenanceFile;
import com.chua.starter.monitor.entity.MaintenanceHost;
import com.chua.starter.monitor.mapper.MaintenanceFileMapper;
import com.chua.starter.monitor.mapper.MaintenanceHostMapper;
import com.chua.starter.monitor.pojo.request.FileDeployRequest;
import com.chua.starter.monitor.pojo.request.FileUploadRequest;
import com.chua.starter.monitor.pojo.request.MaintenanceFileRequest;
import com.chua.starter.monitor.pojo.request.MaintenanceLogRequest;
import com.chua.starter.monitor.pojo.response.FileDeployResponse;
import com.chua.starter.monitor.pojo.response.FileUploadResponse;
import com.chua.starter.monitor.service.MaintenanceFileService;
import com.chua.starter.monitor.service.MaintenanceLogService;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.chua.socketio.support.session.SocketSessionTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 维护文件服务实现
 * 提供文件上传、文件解压等操作
 *
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceFileServiceImpl extends ServiceImpl<MaintenanceFileMapper, MaintenanceFile> implements MaintenanceFileService {

    /**
     * 维护主机数据访问对象
     */
    final MaintenanceHostMapper maintenanceHostMapper;

    /**
     * 维护文件数据访问对象
     */
    final MaintenanceFileMapper maintenanceFileMapper;
    
    /**
     * SSH客户端管理器
     */
    final SshClientManager sshClientManager;
    
    /**
     * 日志服务
     */
    final MaintenanceLogService maintenanceLogService;

    /**
     * Socket会话模板
     */
    final SocketSessionTemplate socketSessionTemplate;

    @Value("${maintenance.file.upload.path:/data/maintenance/files}")
    private String uploadBasePath;
    
    // 异步任务线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 上传文件到维护组下的所有主机
     *
     * @param file    文件对象
     * @param request 请求参数
     * @return 上传结果
     */
    @Override
    public ReturnResult<FileUploadResponse> uploadFile(MultipartFile file, FileUploadRequest request) {
        if (file == null || file.isEmpty()) {
            return ReturnResult.error("上传文件不能为空");
        }
        
        FileUploadResponse response = new FileUploadResponse();
        String taskId = IdUtils.createUlid();
        response.setTaskId(taskId);
        response.setStatus("processing");
        response.setTotalCount(1);
        
        // 异步处理文件上传
        CompletableFuture.runAsync(() -> {
            MaintenanceFile maintenanceFile = null;
            boolean success = false;
            String message = "";
            
            try {
                // 确保目录存在
                String destDir = uploadBasePath + "/" + request.getMaintenanceGroupId();
                if (request.getTargetPath() != null && !request.getTargetPath().isEmpty() && !"/".equals(request.getTargetPath())) {
                    destDir += "/" + request.getTargetPath();
                }
                
                Path dirPath = Paths.get(destDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                // 保存文件
                String originalFilename = file.getOriginalFilename();
                File destFile = new File(dirPath.toFile(), originalFilename);
                
                if (destFile.exists() && (request.getOverwrite() == null || !request.getOverwrite())) {
                    log.warn("文件已存在且不允许覆盖: {}", destFile.getAbsolutePath());
                    response.setStatus("failed");
                    response.setMessage("文件已存在且不允许覆盖");
                    response.setFailCount(1);
                    response.setSuccessCount(0);
                    message = "文件已存在且不允许覆盖";
                    return;
                }
                
                // 将文件保存到本地
                file.transferTo(destFile);
                log.info("文件已保存到本地: {}", destFile.getAbsolutePath());
                
                // 保存文件记录到数据库
                maintenanceFile = new MaintenanceFile();
                maintenanceFile.setMaintenanceGroupId(request.getMaintenanceGroupId());
                maintenanceFile.setMaintenanceFileName(originalFilename);
                maintenanceFile.setMaintenanceFilePath(request.getTargetPath() == null ? "/" : request.getTargetPath());
                maintenanceFile.setMaintenanceFileSize(file.getSize());
                maintenanceFile.setMaintenanceFileType(getFileExtension(originalFilename));
                maintenanceFile.setMaintenanceFileIsCompressed(isCompressedFile(originalFilename));
                maintenanceFile.setMaintenanceFileIsExtract(request.getExtract() != null && request.getExtract() ? 1 : 0);
                maintenanceFile.setMaintenanceFileIsOverride(request.getOverwrite() != null && request.getOverwrite() ? 1 : 0);
                maintenanceFile.setMaintenanceFileStatus(1);  // 默认启用状态
                maintenanceFile.setMaintenanceFileCreateTime(LocalDateTime.now());
                maintenanceFile.setUpdateTime(LocalDateTime.now());
                
                // 保存到数据库
                save(maintenanceFile);
                log.info("文件信息已保存到数据库，文件ID: {}", maintenanceFile.getMaintenanceFileId());

                // 记录操作日志
                logOperation(maintenanceFile, 7, "上传文件", true, null);
                
                success = true;
                message = "文件上传成功";
                response.setStatus("completed");
                response.setMessage("文件上传成功，保存路径：" + destFile.getAbsolutePath());
                response.setSuccessCount(1);
                response.setFailCount(0);
            } catch (Exception e) {
                log.error("文件上传失败: {}", e.getMessage(), e);
                success = false;
                message = "文件上传失败: " + e.getMessage();
                response.setStatus("failed");
                response.setMessage(message);
                response.setFailCount(1);
                response.setSuccessCount(0);
            }
        }, executorService);
        
        return ReturnResult.success(response);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<FileUploadResponse> uploadFiles(MultipartFile[] files, FileUploadRequest request) {
        if (files == null || files.length == 0) {
            return ReturnResult.error("上传文件不能为空");
        }
        
        FileUploadResponse response = new FileUploadResponse();
        String taskId = IdUtils.createUlid();
        response.setTaskId(taskId);
        response.setStatus("processing");
        response.setTotalCount(files.length);
        
        // 异步处理文件上传
        CompletableFuture.runAsync(() -> {
            int successCount = 0;
            int failCount = 0;
            List<MaintenanceFile> uploadedFiles = new ArrayList<>();
            
            // 确保目录存在
            String destDir = uploadBasePath + "/" + request.getMaintenanceGroupId();
            if (request.getTargetPath() != null && !request.getTargetPath().isEmpty() && !"/".equals(request.getTargetPath())) {
                destDir += "/" + request.getTargetPath();
            }
            
            try {
                Path dirPath = Paths.get(destDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        failCount++;
                        continue;
                    }
                    
                    try {
                        // 保存文件
                        String originalFilename = file.getOriginalFilename();
                        File destFile = new File(dirPath.toFile(), originalFilename);
                        
                        if (destFile.exists() && (request.getOverwrite() == null || !request.getOverwrite())) {
                            log.warn("文件已存在且不允许覆盖: {}", destFile.getAbsolutePath());
                            failCount++;
                            continue;
                        }
                        
                        file.transferTo(destFile);
                        
                        // 保存文件记录到数据库
            MaintenanceFile maintenanceFile = new MaintenanceFile();
            maintenanceFile.setMaintenanceGroupId(request.getMaintenanceGroupId());
                        maintenanceFile.setMaintenanceFileName(originalFilename);
                        maintenanceFile.setMaintenanceFilePath(request.getTargetPath() == null ? "/" : request.getTargetPath());
            maintenanceFile.setMaintenanceFileSize(file.getSize());
                        maintenanceFile.setMaintenanceFileType(getFileExtension(originalFilename));
                        maintenanceFile.setMaintenanceFileIsCompressed(isCompressedFile(originalFilename));
                        maintenanceFile.setMaintenanceFileIsExtract(request.getExtract() != null && request.getExtract() ? 1 : 0);
                        maintenanceFile.setMaintenanceFileIsOverride(request.getOverwrite() != null && request.getOverwrite() ? 1 : 0);
                        maintenanceFile.setMaintenanceFileStatus(1);
                        maintenanceFile.setMaintenanceFileCreateTime(LocalDateTime.now());
                        maintenanceFile.setUpdateTime(LocalDateTime.now());
                        
                        // 保存到数据库
                        save(maintenanceFile);
                        uploadedFiles.add(maintenanceFile);
                        
                        // 如果是压缩文件且需要解压，进行解压操作
                        if (maintenanceFile.getMaintenanceFileIsCompressed() && maintenanceFile.getMaintenanceFileIsExtract() == 1) {
                            try {
                                // 解压缩文件
                                Path filePath = Paths.get(destFile.getAbsolutePath());
                                String extractToPath = destDir + "/extracted_" + System.currentTimeMillis();
                                File extractDir = new File(extractToPath);
                                if (!extractDir.exists()) {
                                    extractDir.mkdirs();
                                }
                                
                                log.info("正在解压文件: {} 到 {}", filePath, extractToPath);
                                
                                if (originalFilename.toLowerCase().endsWith(".zip")) {
                                    extractZipFile(filePath.toFile(), extractDir);
                                } else if (originalFilename.toLowerCase().endsWith(".tar")) {
                                    extractTarFile(filePath.toFile(), extractDir);
                                } else if (originalFilename.toLowerCase().endsWith(".tar.gz") 
                                        || originalFilename.toLowerCase().endsWith(".tgz")) {
                                    extractTarGzFile(filePath.toFile(), extractDir);
                                } else if (originalFilename.toLowerCase().endsWith(".gz")) {
                                    extractGzFile(filePath.toFile(), extractDir);
                                } else if (originalFilename.toLowerCase().endsWith(".rar")) {
                                    log.warn("当前环境暂不支持RAR文件解压，请通过部署到远程主机后解压");
                                } else {
                                    log.warn("不支持的压缩文件格式: {}", originalFilename);
                                }
                                
                                // 记录解压目录
                                maintenanceFile.setMaintenanceFileExtractPath(extractToPath);
                                updateById(maintenanceFile);
                                
                                log.info("文件解压成功: {}", filePath);
                            } catch (Exception e) {
                                log.error("文件解压失败: {}", originalFilename, e);
                            }
                        }
                        
                        successCount++;
                } catch (Exception e) {
                        log.error("文件 [{}] 上传失败: {}", file.getOriginalFilename(), e.getMessage(), e);
                        failCount++;
                    }
                }
                
                response.setStatus("completed");
                response.setMessage(String.format("文件上传完成：成功 %d 个，失败 %d 个", successCount, failCount));
                response.setSuccessCount(successCount);
                response.setFailCount(failCount);
                
                // 记录操作日志 - 批量上传
                for (MaintenanceFile file : uploadedFiles) {
                    logOperation(file, 7, "批量上传文件", true, "文件上传成功");
                }
                
            } catch (Exception e) {
                log.error("批量文件上传过程中发生错误", e);
                response.setStatus("failed");
                response.setMessage("批量文件上传失败: " + e.getMessage());
                response.setSuccessCount(successCount);
                response.setFailCount(files.length - successCount);
            }
        }, executorService);

            return ReturnResult.success(response);
    }
    
    @Override
    public ReturnResult<List<MaintenanceFile>> getFilesByGroupId(Long groupId) {
        if (groupId == null) {
            return ReturnResult.error("维护组ID不能为空");
        }
        
        LambdaQueryWrapper<MaintenanceFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaintenanceFile::getMaintenanceGroupId, groupId);
        List<MaintenanceFile> files = list(queryWrapper);

        return ReturnResult.success(files);
    }
    
    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteFile(Long fileId) {
        MaintenanceFile file = getById(fileId);
        if (file == null) {
            return ReturnResult.error("文件不存在");
        }
        
        try {
            // 获取文件的本地存储路径
            String filePath = uploadBasePath + "/" + file.getMaintenanceGroupId();
            if (file.getMaintenanceFilePath() != null && !file.getMaintenanceFilePath().isEmpty() && !"/".equals(file.getMaintenanceFilePath())) {
                filePath += "/" + file.getMaintenanceFilePath();
            }
            filePath += "/" + file.getMaintenanceFileName();
            
            // 记录操作日志
            logOperation(file, 3, "删除文件", true, null);
            
            // 从数据库删除
            boolean removed = removeById(fileId);
            if (!removed) {
                return ReturnResult.error("删除文件记录失败");
            }
            
            // 然后尝试删除物理文件
            File physicalFile = new File(filePath);
            if (physicalFile.exists()) {
                boolean deleted = physicalFile.delete();
                if (!deleted) {
                    log.warn("无法删除物理文件: {}", filePath);
                }
            }
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("删除文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新文件信息
     *
     * @param request 文件信息请求
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateFile(MaintenanceFileRequest request) {
        if (request.getMaintenanceFileId() == null) {
            return ReturnResult.error("文件ID不能为空");
        }
        
        MaintenanceFile file = getById(request.getMaintenanceFileId());
        if (file == null) {
            return ReturnResult.error("文件不存在");
        }
        
        try {
            // 更新文件信息
            MaintenanceFile updateFile = new MaintenanceFile();
            updateFile.setMaintenanceFileId(request.getMaintenanceFileId());
            
            if (request.getMaintenanceFilePath() != null) {
                updateFile.setMaintenanceFilePath(request.getMaintenanceFilePath());
            }
            
            if (request.getMaintenanceFileStatus() != null) {
                updateFile.setMaintenanceFileStatus(request.getMaintenanceFileStatus());
            }
            
            if (request.getMaintenanceFileOverride() != null) {
                updateFile.setMaintenanceFileIsOverride(request.getMaintenanceFileOverride());
            }
            
            if (request.getMaintenanceFileExtract() != null) {
                updateFile.setMaintenanceFileIsExtract(request.getMaintenanceFileExtract());
            }
            
            updateFile.setUpdateTime(LocalDateTime.now());
            
            boolean updated = updateById(updateFile);
            if (!updated) {
                return ReturnResult.error("更新文件信息失败");
            }
            
            // 获取更新后的完整信息
            MaintenanceFile updatedFile = getById(request.getMaintenanceFileId());
            
            // 记录操作日志
            logOperation(updatedFile, 2, "更新文件信息", true, null);
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("更新文件信息失败: {}", e.getMessage(), e);
            return ReturnResult.error("更新文件信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 部署文件到维护组下的所有主机
     *
     * @param request 部署请求
     * @return 部署结果
     */
    @Override
    public ReturnResult<FileDeployResponse> deployFile(FileDeployRequest request) {
        if (request.getMaintenanceFileId() == null) {
            return ReturnResult.error("文件ID不能为空");
        }
        
        if (request.getMaintenanceGroupId() == null) {
            return ReturnResult.error("维护组ID不能为空");
        }
        
        MaintenanceFile file = getById(request.getMaintenanceFileId());
        if (file == null) {
            return ReturnResult.error("文件不存在");
        }
        
        // 生成唯一任务ID
        String taskId = "file-deploy-" + IdUtils.createUlid();
        FileDeployResponse response = new FileDeployResponse();
        response.setTaskId(taskId);
        
        // 异步执行部署任务
        CompletableFuture.runAsync(() -> {
            try {
                // 记录操作日志
                logOperation(file, 8, "部署文件", true, null);
                
                // 使用Socket会话通知前端开始部署
                String eventKey = "event-maintenance-file-" + request.getMaintenanceGroupId();
                log.debug("发送部署开始事件到前端，eventKey=[{}]", eventKey);
                socketSessionTemplate.send(eventKey, "开始部署文件 [" + file.getMaintenanceFileName() + "] 到维护组下的主机");
                
                // 获取文件的本地存储路径
                String filePath = uploadBasePath + "/" + file.getMaintenanceGroupId();
                if (file.getMaintenanceFilePath() != null && !file.getMaintenanceFilePath().isEmpty() && !"/".equals(file.getMaintenanceFilePath())) {
                    filePath += "/" + file.getMaintenanceFilePath();
                }
                filePath += "/" + file.getMaintenanceFileName();
                
                File localFile = new File(filePath);
                if (!localFile.exists()) {
                    String errorMsg = "文件不存在: " + filePath;
                    log.error(errorMsg);
                    socketSessionTemplate.send(eventKey, errorMsg);
                    return;
                }
                
                // 获取维护组下所有启用的主机
                LambdaQueryWrapper<MaintenanceHost> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(MaintenanceHost::getMaintenanceGroupId, request.getMaintenanceGroupId());
                queryWrapper.eq(MaintenanceHost::getMaintenanceHostEnabled, 1);
                List<MaintenanceHost> hosts = maintenanceHostMapper.selectList(queryWrapper);
                
                if (hosts.isEmpty()) {
                    String errorMsg = "维护组下没有启用的主机";
                    log.error(errorMsg);
                    socketSessionTemplate.send(eventKey, errorMsg);
                    return;
                }
                
                socketSessionTemplate.send(eventKey, "开始将文件 [" + file.getMaintenanceFileName() + "] 部署到 " + hosts.size() + " 台主机");
                
                // 部署文件到每台主机
                for (MaintenanceHost host : hosts) {
                    try {
                        log.info("正在将文件 [{}] 部署到主机 [{}]", file.getMaintenanceFileName(), host.getMaintenanceHostAddress());
                        socketSessionTemplate.send(eventKey, "正在将文件 [" + file.getMaintenanceFileName() + "] 部署到主机 [" + host.getMaintenanceHostAddress() + "]");
                        
                        // 上传文件到远程主机
                        boolean overwrite = file.getMaintenanceFileIsOverride() != null && file.getMaintenanceFileIsOverride() == 1;
                        boolean extract = file.getMaintenanceFileIsExtract() != null && file.getMaintenanceFileIsExtract() == 1;
                        
                        uploadFileToHost(localFile, host, file.getMaintenanceFilePath(), file.getMaintenanceFileName(), overwrite, extract);
                        
                        log.info("文件 [{}] 已成功部署到主机 [{}]", file.getMaintenanceFileName(), host.getMaintenanceHostAddress());
                        socketSessionTemplate.send(eventKey, "文件 [" + file.getMaintenanceFileName() + "] 已成功部署到主机 [" + host.getMaintenanceHostAddress() + "]");
                    } catch (Exception e) {
                        String errorMsg = "将文件 [" + file.getMaintenanceFileName() + "] 部署到主机 [" + host.getMaintenanceHostAddress() + "] 失败: " + e.getMessage();
                        log.error(errorMsg, e);
                        socketSessionTemplate.send(eventKey, errorMsg);
                    }
                }
                
                socketSessionTemplate.send(eventKey, "文件 [" + file.getMaintenanceFileName() + "] 部署完成");
                log.info("文件 [{}] 部署完成", file.getMaintenanceFileName());
            } catch (Exception e) {
                log.error("部署文件失败: {}", e.getMessage(), e);
            }
        }, executorService);
        
        return ReturnResult.success(response);
    }

    /**
     * 同步文件到远程主机
     * 将指定文件同步到维护组下的所有启用主机
     *
     * @param fileId 文件ID
     * @return 同步任务ID
     */
    @Override
    public ReturnResult<String> syncFile(Long fileId) {
        try {
            MaintenanceFile maintenanceFile = getById(fileId);
            if (maintenanceFile == null) {
                log.error("同步文件失败: 文件 [{}] 不存在", fileId);
                return ReturnResult.error("维护文件不存在");
            }
            
            log.info("开始同步文件 [{}]({}) 到维护组 [{}] 下的所有主机", 
                maintenanceFile.getMaintenanceFileName(), 
                fileId,
                maintenanceFile.getMaintenanceGroupId());
            
            // 生成唯一任务ID
            String taskId = "file-sync-" + IdUtils.createUlid();
            
            // 记录操作日志
            logOperation(maintenanceFile, 4, "同步维护文件", true, null);
            
            // 使用Socket会话通知前端开始同步
            String eventKey = "event-maintenance-file-" + maintenanceFile.getMaintenanceGroupId();
            log.debug("发送同步开始事件到前端，eventKey=[{}]", eventKey);
            socketSessionTemplate.send(eventKey, "开始同步文件 [" + maintenanceFile.getMaintenanceFileName() + "]");
            
            // 异步将文件同步到维护组下所有主机
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("正在执行异步同步文件任务...");
                    syncFileToHosts(maintenanceFile, taskId);
                    socketSessionTemplate.send(eventKey, "文件 [" + maintenanceFile.getMaintenanceFileName() + "] 同步完成");
                    log.info("文件 [{}] 同步完成", maintenanceFile.getMaintenanceFileName());
                } catch (Exception e) {
                    log.error("同步文件到主机失败: {}", e.getMessage(), e);
                    socketSessionTemplate.send(eventKey, "文件 [" + maintenanceFile.getMaintenanceFileName() + "] 同步失败: " + e.getMessage());
                }
            }, executorService);
            
            log.info("同步文件请求已提交，文件 [{}]({}), 任务ID: {}", 
                maintenanceFile.getMaintenanceFileName(), 
                fileId,
                taskId);
            return ReturnResult.success(taskId);
        } catch (Exception e) {
            log.error("同步维护文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("同步维护文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 同步文件到维护组下所有启用的主机
     *
     * @param file 文件信息
     * @param taskId 任务ID
     */
    private void syncFileToHosts(MaintenanceFile file, String taskId) {
        // 获取维护组下所有启用的主机
        LambdaQueryWrapper<MaintenanceHost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaintenanceHost::getMaintenanceGroupId, file.getMaintenanceGroupId());
        queryWrapper.eq(MaintenanceHost::getMaintenanceHostEnabled, 1);
        List<MaintenanceHost> hosts = maintenanceHostMapper.selectList(queryWrapper);
        
        if (hosts.isEmpty()) {
            log.warn("维护组 [{}] 下没有启用的主机，文件同步操作已跳过", file.getMaintenanceGroupId());
            String eventKey = "event-maintenance-file-" + file.getMaintenanceGroupId();
            socketSessionTemplate.send(eventKey, "同步文件失败，维护组下没有启用的主机");
            return;
        }
        
        String filePath = file.getMaintenanceFilePath();
        String fileName = file.getMaintenanceFileName();
        String eventKey = "event-maintenance-file-" + file.getMaintenanceGroupId();
        
        log.info("开始同步文件 [{}] 到维护组 [{}] 下的 {} 台主机", 
            fileName, file.getMaintenanceGroupId(), hosts.size());
        
        socketSessionTemplate.send(eventKey, "开始将文件 [" + fileName + "] 同步到 " + hosts.size() + " 台主机");
        
        // 获取本地文件路径
        String localFilePath = uploadBasePath + "/" + file.getMaintenanceGroupId();
        if (filePath != null && !filePath.isEmpty() && !"/".equals(filePath)) {
            localFilePath += "/" + filePath;
        }
        localFilePath += "/" + fileName;
        
        File localFile = new File(localFilePath);
        if (!localFile.exists()) {
            log.error("同步文件失败: 本地文件 [{}] 不存在", localFilePath);
            socketSessionTemplate.send(eventKey, "同步文件失败: 本地文件 [" + fileName + "] 不存在");
            return;
        }
        
        // 为每个主机上传文件
        List<CompletableFuture<Void>> futures = hosts.stream()
            .map(host -> CompletableFuture.runAsync(() -> {
                try {
                    log.info("正在将文件 [{}] 上传到主机 [{}]", 
                        fileName, host.getMaintenanceHostAddress());
                    
                    socketSessionTemplate.send(eventKey, "正在将文件 [" + fileName + "] 上传到主机 [" + host.getMaintenanceHostAddress() + "]");
                    
                    // 上传文件到远程主机
                    boolean overwrite = file.getMaintenanceFileOverride() != null && file.getMaintenanceFileOverride() == 1;
                    boolean extract = file.getMaintenanceFileIsExtract() != null && file.getMaintenanceFileIsExtract() == 1;
                    
                    uploadFileToHost(localFile, host, filePath, fileName, overwrite, extract);
                    
                    socketSessionTemplate.send(eventKey, "已成功将文件 [" + fileName + "] 上传到主机 [" + host.getMaintenanceHostAddress() + "]");
                    log.info("已成功将文件 [{}] 上传到主机 [{}]", fileName, host.getMaintenanceHostAddress());
                } catch (Exception e) {
                    String errorMessage = "将文件 [" + fileName + "] 上传到主机 [" + host.getMaintenanceHostAddress() + "] 失败: " + e.getMessage();
                    socketSessionTemplate.send(eventKey, errorMessage);
                    log.error(errorMessage, e);
                }
            }, executorService))
            .collect(Collectors.toList());
        
        // 等待所有上传任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        socketSessionTemplate.send(eventKey, "文件 [" + fileName + "] 同步完成");
        log.info("文件 [{}] 同步到 {} 台主机的任务已完成", fileName, hosts.size());
    }

    /**
     * 上传文件到指定主机
     *
     * @param file       文件
     * @param host       维护主机对象
     * @param targetPath 目标路径
     * @param fileName   文件名
     * @param overwrite  是否覆盖
     * @param extract    是否解压
     * @throws Exception 异常信息
     */
    private void uploadFileToHost(File file, MaintenanceHost host,
                                  String targetPath, String fileName, Boolean overwrite, Boolean extract)
            throws Exception {
        ChannelSftp channelSftp = null;

        try {
            // 使用SshClientManager获取SFTP通道
            channelSftp = sshClientManager.createSftpChannel(host);

            // 创建目标目录（如果不存在）
            try {
                channelSftp.cd(targetPath);
            } catch (SftpException e) {
                // 目录不存在，创建目录
                String[] folders = targetPath.split("/");
                StringBuilder path = new StringBuilder();
                for (String folder : folders) {
                    if (folder.isEmpty()) {
                        path.append("/");
                        continue;
                    }
                    path.append(folder).append("/");
                    try {
                        channelSftp.cd(path.toString());
                    } catch (SftpException ex) {
                        channelSftp.mkdir(path.toString());
                        channelSftp.cd(path.toString());
                    }
                }
            }

            // 检查文件是否已存在
            try {
                channelSftp.lstat(targetPath + "/" + fileName);
                // 文件存在
                if (overwrite == null || !overwrite) {
                        throw new Exception("文件已存在且未指定覆盖");
                }
                // 如果需要覆盖，先删除现有文件
                channelSftp.rm(targetPath + "/" + fileName);
            } catch (SftpException e) {
                // 文件不存在，可以继续
            }

            // 上传文件
            if(targetPath.endsWith("/")) {
                channelSftp.put(new FileInputStream(file), targetPath + fileName);
            } else {
                channelSftp.put(new FileInputStream(file), targetPath + "/" + fileName);
            }

            // 如果是压缩文件且需要解压
            if (extract) {
                // 使用exec通道执行解压命令
                ChannelExec execChannel = null;
                try {
                    // 使用SshClientManager获取Exec通道
                    execChannel = sshClientManager.createExecChannel(host);
                    String command;
                    String fullPath = targetPath.endsWith("/") ? targetPath + fileName : targetPath + "/" + fileName;

                    if (fileName.toLowerCase().endsWith(".zip")) {
                        command = "cd " + targetPath + " && unzip -o " + fullPath;
                    } else if (fileName.toLowerCase().endsWith(".tar")) {
                        command = "cd " + targetPath + " && tar -xf " + fullPath;
                    } else if (fileName.toLowerCase().endsWith(".tar.gz") || fileName.toLowerCase().endsWith(".tgz")) {
                        command = "cd " + targetPath + " && tar -xzf " + fullPath;
                    } else if (fileName.toLowerCase().endsWith(".gz")) {
                        command = "cd " + targetPath + " && gunzip -f " + fullPath;
                    } else if (fileName.toLowerCase().endsWith(".rar")) {
                        command = "cd " + targetPath + " && unrar x " + fullPath;
                    } else {
                            throw new Exception("不支持的压缩文件格式");
                    }

                    execChannel.setCommand(command);

                    execChannel.setInputStream(null);
                    execChannel.setOutputStream(System.out);
                    execChannel.setErrStream(System.err);

                    execChannel.connect(30000);

                    // 等待命令执行完成
                    int exitStatus = -1;
                    while (true) {
                        if (execChannel.isClosed()) {
                            exitStatus = execChannel.getExitStatus();
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            // 忽略
                        }
                    }

                    if (exitStatus != 0) {
                        throw new Exception("解压失败，退出状态码: " + exitStatus);
                    }
                } finally {
                    if (execChannel != null) {
                        execChannel.disconnect();
                    }
                }
            }
        } finally {
            // 关闭连接
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
        }
    }
    
    /**
     * 在远程主机上执行命令
     *
     * @param host    维护主机对象
     * @param command 要执行的命令
     * @return 命令执行结果
     * @throws Exception 异常信息
     */
    private String executeRemoteCommand(MaintenanceHost host, String command) throws Exception {
        ChannelExec channel = null;
        
        try {
            // 使用SshClientManager获取Exec通道
            channel = sshClientManager.createExecChannel(host);
            channel.setCommand(command);
            
            // 获取命令输出
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            channel.setOutputStream(outputStream);
            
            channel.setErrStream(outputStream);
            
            channel.connect(30000);
            
            // 等待命令执行完成
            while (!channel.isClosed()) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    // 忽略
                }
            }
            
            return outputStream.toString();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
    
    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    /**
     * 判断是否为压缩文件
     *
     * @param filename 文件名
     * @return 是否压缩文件
     */
    private Boolean isCompressedFile(String filename) {
        if (filename == null) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        List<String> compressedExtensions = new ArrayList<>();
        compressedExtensions.add("zip");
        compressedExtensions.add("rar");
        compressedExtensions.add("tar");
        compressedExtensions.add("gz");
        compressedExtensions.add("7z");
        
        return compressedExtensions.contains(extension);
    }
    
    /**
     * 解压ZIP文件
     *
     * @param zipFile 压缩文件
     * @param destDir 目标目录
     * @throws IOException IO异常
     */
    private void extractZipFile(File zipFile, File destDir) throws IOException {
        java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile);
        try {
            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = entries.nextElement();
                File entryDestination = new File(destDir, entry.getName());
                
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (java.io.InputStream in = zip.getInputStream(entry);
                         java.io.OutputStream out = new java.io.FileOutputStream(entryDestination)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        } finally {
            zip.close();
        }
    }
    
    /**
     * 解压TAR文件
     *
     * @param tarFile 压缩文件
     * @param destDir 目标目录
     * @throws IOException IO异常
     */
    private void extractTarFile(File tarFile, File destDir) throws IOException {
        try (org.apache.commons.compress.archivers.tar.TarArchiveInputStream tarIn = 
                new org.apache.commons.compress.archivers.tar.TarArchiveInputStream(
                        new java.io.BufferedInputStream(
                                new java.io.FileInputStream(tarFile)))) {
            
            org.apache.commons.compress.archivers.ArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                File outputFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }
                } else {
                    outputFile.getParentFile().mkdirs();
                    try (java.io.OutputStream outputFileStream = new java.io.FileOutputStream(outputFile)) {
                        org.apache.commons.compress.utils.IOUtils.copy(tarIn, outputFileStream);
                    }
                }
            }
        }
    }
    
    /**
     * 解压TAR.GZ文件
     *
     * @param tarGzFile 压缩文件
     * @param destDir   目标目录
     * @throws IOException IO异常
     */
    private void extractTarGzFile(File tarGzFile, File destDir) throws IOException {
        try (org.apache.commons.compress.archivers.tar.TarArchiveInputStream tarIn = 
                new org.apache.commons.compress.archivers.tar.TarArchiveInputStream(
                        new java.util.zip.GZIPInputStream(
                                new java.io.BufferedInputStream(
                                        new java.io.FileInputStream(tarGzFile))))) {
            
            org.apache.commons.compress.archivers.ArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                File outputFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }
                } else {
                    outputFile.getParentFile().mkdirs();
                    try (java.io.OutputStream outputFileStream = new java.io.FileOutputStream(outputFile)) {
                        org.apache.commons.compress.utils.IOUtils.copy(tarIn, outputFileStream);
                    }
                }
            }
        }
    }
    
    /**
     * 解压GZ文件
     *
     * @param gzFile  压缩文件
     * @param destDir 目标目录
     * @throws IOException IO异常
     */
    private void extractGzFile(File gzFile, File destDir) throws IOException {
        String fileName = gzFile.getName();
        String outputFileName;
        
        // 移除.gz扩展名
        if (fileName.toLowerCase().endsWith(".gz")) {
            outputFileName = fileName.substring(0, fileName.length() - 3);
        } else {
            outputFileName = fileName + ".extracted";
        }
        
        File outputFile = new File(destDir, outputFileName);
        
        try (java.util.zip.GZIPInputStream gzipIn = new java.util.zip.GZIPInputStream(
                new java.io.FileInputStream(gzFile));
             java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile)) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }

    /**
     * 记录操作日志
     *
     * @param file 文件信息
     * @param operationType 操作类型：1-创建，2-更新，3-删除，4-同步，8-部署
     * @param operationDesc 操作描述
     * @param result 操作结果
     * @param resultMessage 结果消息
     */
    private void logOperation(MaintenanceFile file, Integer operationType, String operationDesc, Boolean result, String resultMessage) {
        try {
            log.debug("记录文件 [{}]({}) 的操作日志: [{}] [{}] [{}]", 
                file.getMaintenanceFileName(), 
                file.getMaintenanceFileId(),
                operationType, 
                operationDesc,
                result ? "成功" : "失败");
                
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ipAddress = getClientIp(request);
            
            MaintenanceLogRequest logRequest = new MaintenanceLogRequest();
            logRequest.setMaintenanceGroupId(file.getMaintenanceGroupId());
            logRequest.setTargetType(4); // 4表示文件
            logRequest.setTargetId(file.getMaintenanceFileId());
            logRequest.setOperationType(operationType);
            logRequest.setOperationDesc(operationDesc);
            logRequest.setOperationResult(result);
            logRequest.setOperationMessage(resultMessage);
            logRequest.setOperationIp(ipAddress);
            
            ReturnResult<Boolean> logResult = maintenanceLogService.logOperation(logRequest);
            if (!logResult.getSuccess()) {
                log.warn("记录文件操作日志失败: {}", logResult.getMessage());
            } else {
                log.debug("文件操作日志记录成功");
            }
        } catch (Exception e) {
            log.error("记录文件操作日志异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 