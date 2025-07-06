package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.enums.FileManagementMode;
import com.chua.starter.monitor.starter.pojo.FileOperationRequest;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.impl.ApiFileManagementServiceImpl;
import com.chua.starter.monitor.starter.service.impl.NodeFileManagementServiceImpl;
import com.chua.starter.monitor.starter.service.impl.SshFileManagementServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件管理控制器
 * 
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@RestController
@RequestMapping("/api/file-management")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件管理相关接口")
public class FileManagementController {

    private final MonitorSysGenServerSettingService serverSettingService;
    private final SshFileManagementServiceImpl sshFileManagementService;
    private final NodeFileManagementServiceImpl nodeFileManagementService;
    private final ApiFileManagementServiceImpl apiFileManagementService;

    @Operation(summary = "列出文件", description = "获取指定路径下的文件列表")
    @GetMapping("/list")
    public ReturnResult<FileOperationResponse> listFiles(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String path,
            @Parameter(description = "是否包含隐藏文件") @RequestParam(defaultValue = "false") Boolean includeHidden,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "asc") String sortOrder) {
        
        try {
            FileManagementMode mode = getFileManagementMode(serverId);
            if (mode == null) {
                return ReturnResult.error("服务器文件管理模式未配置");
            }
            
            switch (mode) {
                case SSH:
                    return sshFileManagementService.listFiles(serverId, path, includeHidden, sortBy, sortOrder);
                case NODE:
                    return nodeFileManagementService.listFiles(serverId, path, includeHidden, sortBy, sortOrder);
                case API:
                    return apiFileManagementService.listFiles(serverId, path, includeHidden, sortBy, sortOrder);
                case NONE:
                default:
                    return ReturnResult.error("文件管理功能未启用");
            }
        } catch (Exception e) {
            log.error("列出文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("列出文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取文件树", description = "获取指定路径的文件树结构")
    @GetMapping("/tree")
    public ReturnResult<FileOperationResponse> getFileTree(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String path,
            @Parameter(description = "最大深度") @RequestParam(defaultValue = "3") Integer maxDepth,
            @Parameter(description = "是否包含隐藏文件") @RequestParam(defaultValue = "false") Boolean includeHidden) {
        
        try {
            FileManagementMode mode = getFileManagementMode(serverId);
            if (mode == null) {
                return ReturnResult.error("服务器文件管理模式未配置");
            }
            
            switch (mode) {
                case SSH:
                    return sshFileManagementService.getFileTree(serverId, path, maxDepth, includeHidden);
                case NODE:
                    return nodeFileManagementService.getFileTree(serverId, path, maxDepth, includeHidden);
                case API:
                    return apiFileManagementService.getFileTree(serverId, path, maxDepth, includeHidden);
                case NONE:
                default:
                    return ReturnResult.error("文件管理功能未启用");
            }
        } catch (Exception e) {
            log.error("获取文件树失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取文件树失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传文件", description = "上传文件到指定路径")
    @PostMapping("/upload")
    public ReturnResult<FileOperationResponse> uploadFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "目标路径", required = true) @RequestParam String targetPath,
            @Parameter(description = "上传文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "是否覆盖") @RequestParam(defaultValue = "false") Boolean overwrite) {
        
        try {
            FileManagementMode mode = getFileManagementMode(serverId);
            if (mode == null) {
                return ReturnResult.error("服务器文件管理模式未配置");
            }
            
            switch (mode) {
                case SSH:
                    return sshFileManagementService.uploadFile(serverId, targetPath, file, overwrite);
                case NODE:
                    return nodeFileManagementService.uploadFile(serverId, targetPath, file, overwrite);
                case API:
                    return apiFileManagementService.uploadFile(serverId, targetPath, file, overwrite);
                case NONE:
                default:
                    return ReturnResult.error("文件管理功能未启用");
            }
        } catch (Exception e) {
            log.error("上传文件失败: serverId={}, targetPath={}", serverId, targetPath, e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "下载文件", description = "下载指定文件")
    @GetMapping("/download")
    public void downloadFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String filePath,
            HttpServletResponse response) {
        
        try {
            FileManagementMode mode = getFileManagementMode(serverId);
            if (mode == null) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getWriter().write("服务器文件管理模式未配置");
                return;
            }
            
            ReturnResult<FileOperationResponse> result;
            switch (mode) {
                case SSH:
                    result = sshFileManagementService.downloadFile(serverId, filePath);
                    break;
                case NODE:
                    result = nodeFileManagementService.downloadFile(serverId, filePath);
                    break;
                case API:
                    result = apiFileManagementService.downloadFile(serverId, filePath);
                    break;
                case NONE:
                default:
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    response.getWriter().write("文件管理功能未启用");
                    return;
            }
            
            if (!result.isSuccess()) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.getWriter().write(result.getMsg());
                return;
            }
            
            // 处理文件下载
            String downloadUrl = result.getData().getDownloadUrl();
            if (downloadUrl != null) {
                File file = new File(downloadUrl);
                if (file.exists()) {
                    String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
                    
                    response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");
                    response.setContentLengthLong(file.length());
                    
                    try (FileInputStream fis = new FileInputStream(file);
                         OutputStream os = response.getOutputStream()) {
                        
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        os.flush();
                    }
                    
                    // 删除临时文件
                    file.delete();
                }
            }
            
        } catch (Exception e) {
            log.error("下载文件失败: serverId={}, filePath={}", serverId, filePath, e);
            try {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.getWriter().write("下载文件失败: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("写入错误响应失败", ioException);
            }
        }
    }

    @Operation(summary = "删除文件", description = "删除指定文件或目录")
    @DeleteMapping("/delete")
    public ReturnResult<FileOperationResponse> deleteFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String path,
            @Parameter(description = "是否递归删除") @RequestParam(defaultValue = "false") Boolean recursive) {
        
        try {
            FileManagementMode mode = getFileManagementMode(serverId);
            if (mode == null) {
                return ReturnResult.error("服务器文件管理模式未配置");
            }
            
            switch (mode) {
                case SSH:
                    return sshFileManagementService.deleteFile(serverId, path, recursive);
                case NODE:
                    return nodeFileManagementService.deleteFile(serverId, path, recursive);
                case API:
                    return apiFileManagementService.deleteFile(serverId, path, recursive);
                case NONE:
                default:
                    return ReturnResult.error("文件管理功能未启用");
            }
        } catch (Exception e) {
            log.error("删除文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("删除文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "重命名文件", description = "重命名文件或目录")
    @PutMapping("/rename")
    public ReturnResult<FileOperationResponse> renameFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "原文件路径", required = true) @RequestParam String oldPath,
            @Parameter(description = "新文件名", required = true) @RequestParam String newName) {
        
        try {
            FileManagementMode mode = getFileManagementMode(serverId);
            if (mode == null) {
                return ReturnResult.error("服务器文件管理模式未配置");
            }
            
            switch (mode) {
                case SSH:
                    return sshFileManagementService.renameFile(serverId, oldPath, newName);
                case NODE:
                    return nodeFileManagementService.renameFile(serverId, oldPath, newName);
                case API:
                    return apiFileManagementService.renameFile(serverId, oldPath, newName);
                case NONE:
                default:
                    return ReturnResult.error("文件管理功能未启用");
            }
        } catch (Exception e) {
            log.error("重命名文件失败: serverId={}, oldPath={}, newName={}", serverId, oldPath, newName, e);
            return ReturnResult.error("重命名文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "创建目录", description = "创建新目录")
    @PostMapping("/mkdir")
    public ReturnResult<FileOperationResponse> createDirectory(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "目录路径", required = true) @RequestParam String path,
            @Parameter(description = "是否递归创建") @RequestParam(defaultValue = "true") Boolean recursive) {
        
        try {
            FileManagementMode mode = getFileManagementMode(serverId);
            if (mode == null) {
                return ReturnResult.error("服务器文件管理模式未配置");
            }
            
            switch (mode) {
                case SSH:
                    return sshFileManagementService.createDirectory(serverId, path, recursive);
                case NODE:
                    return nodeFileManagementService.createDirectory(serverId, path, recursive);
                case API:
                    return apiFileManagementService.createDirectory(serverId, path, recursive);
                case NONE:
                default:
                    return ReturnResult.error("文件管理功能未启用");
            }
        } catch (Exception e) {
            log.error("创建目录失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("创建目录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "执行文件操作", description = "执行复杂的文件操作")
    @PostMapping("/execute")
    public ReturnResult<FileOperationResponse> executeOperation(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "操作请求", required = true) @RequestBody FileOperationRequest request) {
        
        try {
            FileManagementMode mode = getFileManagementMode(serverId);
            if (mode == null) {
                return ReturnResult.error("服务器文件管理模式未配置");
            }
            
            switch (mode) {
                case SSH:
                    // SSH模式暂不支持复杂操作
                    return ReturnResult.error("SSH模式暂不支持此操作");
                case NODE:
                    return nodeFileManagementService.executeOperation(serverId, request);
                case API:
                    return apiFileManagementService.executeOperation(serverId, request);
                case NONE:
                default:
                    return ReturnResult.error("文件管理功能未启用");
            }
        } catch (Exception e) {
            log.error("执行文件操作失败: serverId={}, operation={}", serverId, request.getOperation(), e);
            return ReturnResult.error("执行文件操作失败: " + e.getMessage());
        }
    }

    @Operation(summary = "测试连接", description = "测试文件管理连接")
    @GetMapping("/test")
    public ReturnResult<Boolean> testConnection(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId) {
        
        try {
            FileManagementMode mode = getFileManagementMode(serverId);
            if (mode == null) {
                return ReturnResult.error("服务器文件管理模式未配置");
            }
            
            switch (mode) {
                case SSH:
                    return sshFileManagementService.testConnection(serverId);
                case NODE:
                    return nodeFileManagementService.testConnection(serverId);
                case API:
                    return apiFileManagementService.testConnection(serverId);
                case NONE:
                default:
                    return ReturnResult.error("文件管理功能未启用");
            }
        } catch (Exception e) {
            log.error("测试连接失败: serverId={}", serverId, e);
            return ReturnResult.error("测试连接失败: " + e.getMessage());
        }
    }

    // 辅助方法
    private FileManagementMode getFileManagementMode(Integer serverId) {
        try {
            ReturnResult<MonitorSysGenServerSetting> byServerId = serverSettingService.getByServerId(serverId);
            if(byServerId.isOk()) {
                return byServerId.getData().getFileManagementMode();
            }
            return null;
        } catch (Exception e) {
            log.error("获取文件管理模式失败: serverId={}", serverId, e);
            return null;
        }
    }


}
