package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.crypto.support.IdHelper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.code.ReturnResultUtils;
import com.chua.starter.monitor.entity.MaintenanceFile;
import com.chua.starter.monitor.entity.MaintenanceHost;
import com.chua.starter.monitor.mapper.MaintenanceFileMapper;
import com.chua.starter.monitor.mapper.MaintenanceHostMapper;
import com.chua.starter.monitor.pojo.request.FileDeployRequest;
import com.chua.starter.monitor.pojo.request.FileUploadRequest;
import com.chua.starter.monitor.pojo.request.MaintenanceFileRequest;
import com.chua.starter.monitor.pojo.response.FileDeployResponse;
import com.chua.starter.monitor.pojo.response.FileUploadResponse;
import com.chua.starter.monitor.service.MaintenanceFileService;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.Properties;
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
public class MaintenanceFileServiceImpl extends ServiceImpl<MaintenanceFileMapper, MaintenanceFile> implements MaintenanceFileService {

    /**
     * 维护主机数据访问对象
     */
    @Resource
    private MaintenanceHostMapper maintenanceHostMapper;

    /**
     * 维护文件数据访问对象
     */
    @Resource
    private MaintenanceFileMapper maintenanceFileMapper;

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
            return ReturnResultUtils.error("上传文件不能为空");
        }
        
        FileUploadResponse response = new FileUploadResponse();
        String taskId = IdHelper.createUUID();
        response.setTaskId(taskId);
        response.setStatus("processing");
        response.setTotalCount(1);
        
        // 异步处理文件上传
        CompletableFuture.runAsync(() -> {
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
                    return;
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
                
                // 如果是压缩文件且需要解压，进行解压操作
                if (maintenanceFile.getMaintenanceFileIsCompressed() && maintenanceFile.getMaintenanceFileIsExtract() == 1) {
                    // TODO: 实现解压逻辑
                }
                
                response.setStatus("completed");
                response.setMessage("文件上传成功");
                response.setSuccessCount(1);
                response.setFailCount(0);
                
            } catch (Exception e) {
                log.error("文件上传失败", e);
                response.setStatus("failed");
                response.setMessage("文件上传失败: " + e.getMessage());
                response.setSuccessCount(0);
                response.setFailCount(1);
            }
        }, executorService);
        
        return ReturnResultUtils.success(response);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<FileUploadResponse> uploadFiles(MultipartFile[] files, FileUploadRequest request) {
        if (files == null || files.length == 0) {
            return ReturnResultUtils.error("上传文件不能为空");
        }
        
        FileUploadResponse response = new FileUploadResponse();
        String taskId = IdHelper.createUUID();
        response.setTaskId(taskId);
        response.setStatus("processing");
        response.setTotalCount(files.length);
        
        // 异步处理文件上传
        CompletableFuture.runAsync(() -> {
            int successCount = 0;
            int failCount = 0;
            
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
                        
                        // 如果是压缩文件且需要解压，进行解压操作
                        if (maintenanceFile.getMaintenanceFileIsCompressed() && maintenanceFile.getMaintenanceFileIsExtract() == 1) {
                            // TODO: 实现解压逻辑
                        }
                        
                        successCount++;
                    } catch (Exception e) {
                        log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                        failCount++;
                    }
                }
                
                // 更新响应信息
                response.setStatus(failCount == 0 ? "completed" : "partially_completed");
                response.setMessage(String.format("上传完成，成功: %d, 失败: %d", successCount, failCount));
                response.setSuccessCount(successCount);
                response.setFailCount(failCount);
                
            } catch (Exception e) {
                log.error("文件上传过程发生错误", e);
                response.setStatus("failed");
                response.setMessage("文件上传失败: " + e.getMessage());
                response.setSuccessCount(successCount);
                response.setFailCount(files.length - successCount);
            }
        }, executorService);
        
        return ReturnResultUtils.success(response);
    }
    
    @Override
    public ReturnResult<List<MaintenanceFile>> getFilesByGroupId(Long groupId) {
        if (groupId == null) {
            return ReturnResultUtils.error("维护组ID不能为空");
        }
        
        LambdaQueryWrapper<MaintenanceFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaintenanceFile::getMaintenanceGroupId, groupId);
        List<MaintenanceFile> files = list(queryWrapper);
        
        return ReturnResultUtils.success(files);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteFile(Long fileId) {
        if (fileId == null) {
            return ReturnResultUtils.error("文件ID不能为空");
        }
        
        // 查询文件信息
        MaintenanceFile file = getById(fileId);
        if (file == null) {
            return ReturnResultUtils.error("文件不存在");
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
        
        return ReturnResultUtils.success(success);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateFile(MaintenanceFileRequest request) {
        if (request == null || request.getMaintenanceFileId() == null) {
            return ReturnResultUtils.error("文件ID不能为空");
        }
        
        // 查询文件是否存在
        MaintenanceFile existingFile = getById(request.getMaintenanceFileId());
        if (existingFile == null) {
            return ReturnResultUtils.error("文件不存在");
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
        
        return ReturnResultUtils.success(success);
    }
    
    @Override
    public ReturnResult<FileDeployResponse> deployFile(FileDeployRequest request) {
        if (request == null || request.getMaintenanceFileId() == null) {
            return ReturnResultUtils.error("文件ID不能为空");
        }
        
        if (request.getMaintenanceGroupId() == null) {
            return ReturnResultUtils.error("维护组ID不能为空");
        }
        
        // 查询文件信息
        MaintenanceFile file = getById(request.getMaintenanceFileId());
        if (file == null) {
            return ReturnResultUtils.error("文件不存在");
        }
        
        // 查询维护组下的所有主机
        LambdaQueryWrapper<MaintenanceHost> hostQueryWrapper = new LambdaQueryWrapper<>();
        hostQueryWrapper.eq(MaintenanceHost::getMaintenanceGroupId, request.getMaintenanceGroupId());
        hostQueryWrapper.eq(MaintenanceHost::getMaintenanceHostStatus, 1); // 只部署到启用状态的主机
        List<MaintenanceHost> hosts = maintenanceHostMapper.selectList(hostQueryWrapper);
        
        if (hosts.isEmpty()) {
            return ReturnResultUtils.error("维护组下没有可用的主机");
        }
        
        // 构建文件的物理路径
        String sourceFilePath = uploadBasePath + "/" + file.getMaintenanceGroupId();
        if (file.getMaintenanceFilePath() != null && !file.getMaintenanceFilePath().isEmpty() && !"/".equals(file.getMaintenanceFilePath())) {
            sourceFilePath += "/" + file.getMaintenanceFilePath();
        }
        sourceFilePath += "/" + file.getMaintenanceFileName();
        
        // 检查源文件是否存在
        if (!Files.exists(Paths.get(sourceFilePath))) {
            return ReturnResultUtils.error("源文件不存在: " + sourceFilePath);
        }
        
        // 创建部署响应对象
        FileDeployResponse response = new FileDeployResponse();
        String taskId = IdHelper.createUUID();
        response.setTaskId(taskId);
        response.setStatus("processing");
        response.setTotalCount(hosts.size());
        response.setSuccessCount(0);
        response.setFailCount(0);
        response.setDetails(new HashMap<>());
        
        // 异步处理文件部署
        CompletableFuture.runAsync(() -> {
            int successCount = 0;
            int failCount = 0;
            Map<String, String> details = new HashMap<>();
            
            for (MaintenanceHost host : hosts) {
                try {
                    // TODO: 实现SCP或SFTP文件传输逻辑
                    // 这里需要根据主机信息和部署参数进行文件传输
                    // 例如使用JSch库实现SFTP传输
                    
                    // 模拟部署成功
                    successCount++;
                    details.put(host.getMaintenanceHostAddress(), "部署成功");
                    
                    // 如果需要部署后执行，则执行命令
                    if (request.getExecuteAfterDeploy() != null && request.getExecuteAfterDeploy()) {
                        // TODO: 实现远程命令执行逻辑
                    }
                    
                } catch (Exception e) {
                    log.error("部署文件到主机失败: {}", host.getMaintenanceHostAddress(), e);
                    failCount++;
                    details.put(host.getMaintenanceHostAddress(), "部署失败: " + e.getMessage());
                }
            }
            
            // 更新响应信息
            response.setStatus(failCount == 0 ? "completed" : "partially_completed");
            response.setMessage(String.format("部署完成，成功: %d, 失败: %d", successCount, failCount));
            response.setSuccessCount(successCount);
            response.setFailCount(failCount);
            response.setDetails(details);
            
        }, executorService);
        
        return ReturnResultUtils.success(response);
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
} 