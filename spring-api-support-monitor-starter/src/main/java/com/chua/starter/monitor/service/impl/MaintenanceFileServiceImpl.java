package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.IdUtils;
import com.alibaba.fastjson2.JSON;
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
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
                
                file.transferTo(destFile);
                
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
                maintenanceFile.setMaintenanceFileStatus(1);
                maintenanceFile.setMaintenanceFileCreateTime(LocalDateTime.now());
                maintenanceFile.setUpdateTime(LocalDateTime.now());
                
                // 保存到数据库
                save(maintenanceFile);
                
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
                
                success = true;
                message = "文件上传成功";
                response.setStatus("completed");
                response.setMessage("文件上传成功");
                response.setSuccessCount(1);
                response.setFailCount(0);
                
            } catch (Exception e) {
                log.error("文件上传失败", e);
                message = "文件上传失败: " + e.getMessage();
                response.setStatus("failed");
                response.setMessage(message);
                response.setSuccessCount(0);
                response.setFailCount(1);
            }
            
            // 记录操作日志
            if (maintenanceFile != null) {
                logOperation(maintenanceFile, 7, "上传文件", success, message);
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteFile(Long fileId) {
        if (fileId == null) {
            return ReturnResult.error("文件ID不能为空");
        }
        
        // 查询文件信息
        MaintenanceFile file = getById(fileId);
        if (file == null) {
            return ReturnResult.error("文件不存在");
        }
        
        // 构建文件的物理路径
        String filePath = uploadBasePath + "/" + file.getMaintenanceGroupId();
        if (file.getMaintenanceFilePath() != null && !file.getMaintenanceFilePath().isEmpty() && !"/".equals(file.getMaintenanceFilePath())) {
            filePath += "/" + file.getMaintenanceFilePath();
        }
        filePath += "/" + file.getMaintenanceFileName();
        
        // 删除物理文件
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            // 即使物理文件删除失败，也继续删除数据库记录
        }
        
        // 删除数据库记录
        boolean success = removeById(fileId);

        return ReturnResult.success(success);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateFile(MaintenanceFileRequest request) {
        if (request == null || request.getMaintenanceFileId() == null) {
            return ReturnResult.error("文件ID不能为空");
        }
        
        // 查询文件是否存在
        MaintenanceFile existingFile = getById(request.getMaintenanceFileId());
        if (existingFile == null) {
            return ReturnResult.error("文件不存在");
        }
        
        // 更新文件信息
        MaintenanceFile updateFile = new MaintenanceFile();
        updateFile.setMaintenanceFileId(request.getMaintenanceFileId());
        
        if (request.getMaintenanceFileName() != null) {
            updateFile.setMaintenanceFileName(request.getMaintenanceFileName());
        }
        
        if (request.getMaintenanceFilePath() != null) {
            updateFile.setMaintenanceFilePath(request.getMaintenanceFilePath());
        }
        
        if (request.getMaintenanceFileStatus() != null) {
            updateFile.setMaintenanceFileStatus(request.getMaintenanceFileStatus());
        }
        
        if (request.getMaintenanceFileIsExtract() != null) {
            updateFile.setMaintenanceFileIsExtract(request.getMaintenanceFileIsExtract());
        }
        
        if (request.getMaintenanceFileIsOverride() != null) {
            updateFile.setMaintenanceFileIsOverride(request.getMaintenanceFileIsOverride());
        }
        
        updateFile.setUpdateTime(LocalDateTime.now());
        
        // 更新数据库记录
        boolean success = updateById(updateFile);

        return ReturnResult.success(success);
    }
    
    @Override
    public ReturnResult<FileDeployResponse> deployFile(FileDeployRequest request) {
        if (request.getMaintenanceFileId() == null) {
            return ReturnResult.error("文件ID不能为空");
        }
        
        if (request.getMaintenanceHostId() == null) {
            return ReturnResult.error("主机ID不能为空");
        }
        
        FileDeployResponse response = new FileDeployResponse();
        String taskId = IdUtils.createUlid();
        response.setTaskId(taskId);
        response.setStatus("processing");
        
        // 异步处理文件部署
        CompletableFuture.runAsync(() -> {
            boolean success = false;
            String message = "";
            
            try {
                // 获取文件信息
                MaintenanceFile file = getById(request.getMaintenanceFileId());
                if (file == null) {
                    response.setStatus("failed");
                    response.setMessage("文件不存在");
                    return;
                }
                
                // 获取主机信息
                MaintenanceHost host = maintenanceHostMapper.selectById(request.getMaintenanceHostId());
                if (host == null) {
                    response.setStatus("failed");
                    response.setMessage("主机不存在");
                    return;
                }
                
                // 构建源文件路径
                String filePath = uploadBasePath + "/" + file.getMaintenanceGroupId();
                if (file.getMaintenanceFilePath() != null && !file.getMaintenanceFilePath().isEmpty() && !"/".equals(file.getMaintenanceFilePath())) {
                    filePath += "/" + file.getMaintenanceFilePath();
                }
                filePath += "/" + file.getMaintenanceFileName();
                
                // 构建目标路径
                String targetPath = request.getTargetPath();
                if (targetPath == null || targetPath.isEmpty()) {
                    targetPath = "/tmp";
                }
                if (!targetPath.endsWith("/")) {
                    targetPath += "/";
                }
                targetPath += file.getMaintenanceFileName();
                
                // 部署文件到主机
                deployFileToHost(host, filePath, targetPath, request.getOverwrite() != null && request.getOverwrite());
                
                // 如果需要解压
                if (file.getMaintenanceFileIsCompressed() == 1 && (request.getExtract() != null && request.getExtract())) {
                    String extractCommand = getExtractCommand(file.getMaintenanceFileName(), targetPath);
                    if (extractCommand != null) {
                        String extractFolder = request.getExtractPath();
                        if (extractFolder == null || extractFolder.isEmpty()) {
                            extractFolder = "/tmp/extracted_" + System.currentTimeMillis();
                        }
                        
                        executeCommand(host, "mkdir -p " + extractFolder);
                        executeCommand(host, "cd " + extractFolder + " && " + extractCommand);
                    }
                }
                
                success = true;
                message = "文件部署成功";
                response.setStatus("completed");
                response.setMessage("文件部署成功");
                
            } catch (Exception e) {
                log.error("文件部署失败", e);
                message = "文件部署失败: " + e.getMessage();
                response.setStatus("failed");
                response.setMessage(message);
            }
            
            // 记录操作日志
            try {
                MaintenanceFile file = getById(request.getMaintenanceFileId());
                if (file != null) {
                    logOperation(file, 8, "部署文件到主机", success, message);
                }
            } catch (Exception e) {
                log.error("记录文件部署日志失败", e);
            }
        }, executorService);
        
        return ReturnResult.success(response);
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
     * @param file           文件对象
     * @param operationType  操作类型
     * @param operationDesc  操作描述
     * @param result         操作结果
     * @param resultMessage  结果消息
     */
    private void logOperation(MaintenanceFile file, Integer operationType, String operationDesc, Boolean result, String resultMessage) {
        try {
            MaintenanceLogRequest logRequest = new MaintenanceLogRequest();
            logRequest.setMaintenanceGroupId(file.getMaintenanceGroupId());
            logRequest.setMaintenanceFileId(file.getMaintenanceFileId());
            logRequest.setOperationType(operationType);
            logRequest.setTargetType(4); // 4表示维护文件
            
            // 构建操作详情JSON
            Map<String, Object> details = new HashMap<>();
            details.put("maintenanceFileId", file.getMaintenanceFileId());
            details.put("maintenanceFileName", file.getMaintenanceFileName());
            details.put("maintenanceFilePath", file.getMaintenanceFilePath());
            details.put("operationDesc", operationDesc);
            logRequest.setOperationDetails(JSON.toJSONString(details));
            
            // 设置操作结果
            logRequest.setOperationResult(result);
            logRequest.setResultMessage(resultMessage);
            
            // 获取客户端IP
            String ipAddress = "127.0.0.1";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ipAddress = getClientIp(request);
                }
            } catch (Exception e) {
                // 忽略获取IP过程中的异常
            }
            logRequest.setIpAddress(ipAddress);
            
            // 调用日志服务记录日志
            maintenanceLogService.logOperation(logRequest);
        } catch (Exception e) {
            // 记录日志失败不应影响主业务逻辑
            log.error("记录操作日志失败: {}", e.getMessage(), e);
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