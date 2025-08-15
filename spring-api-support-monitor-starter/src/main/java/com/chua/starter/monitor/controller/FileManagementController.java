package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.report.client.starter.pojo.FileOperationRequest;
import com.chua.report.client.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.service.impl.FileManagementFacadeServiceImpl;
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
import java.util.List;

/**
 * 文件管理控制器
 * 
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@RestController
@RequestMapping("v1/file-management")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件管理相关接口")
public class FileManagementController {

    private final FileManagementFacadeServiceImpl fileManagementFacadeService;
    private final com.chua.starter.monitor.service.MonitorSysGenScriptUploadRecordService uploadRecordService;

    @Operation(summary = "列出文件", description = "获取指定路径下的文件列表")
    @GetMapping("/list")
    public ReturnResult<FileOperationResponse> listFiles(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String path,
            @Parameter(description = "是否包含隐藏文件") @RequestParam(defaultValue = "false") Boolean includeHidden,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "asc") String sortOrder) {

        return fileManagementFacadeService.listFiles(serverId, path, includeHidden, sortBy, sortOrder);
    }

    @Operation(summary = "获取文件树", description = "获取指定路径的文件树结构，支持懒加载模式")
    @GetMapping("/tree")
    public ReturnResult<FileOperationResponse> getFileTree(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String path,
            @Parameter(description = "最大深度") @RequestParam(defaultValue = "3") Integer maxDepth,
            @Parameter(description = "是否包含隐藏文件") @RequestParam(defaultValue = "false") Boolean includeHidden,
            @Parameter(description = "是否启用懒加载模式") @RequestParam(defaultValue = "false") Boolean lazyLoad,
            @Parameter(description = "每页文件数量限制") @RequestParam(defaultValue = "1000") Integer pageSize,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") Integer pageIndex) {

        return fileManagementFacadeService.getFileTree(serverId, path, maxDepth, includeHidden, lazyLoad, pageSize,
                pageIndex);
    }

    @Operation(summary = "上传文件", description = "上传文件到指定路径")
    @PostMapping("/upload")
    public ReturnResult<FileOperationResponse> uploadFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "目标路径", required = true) @RequestParam String targetPath,
            @Parameter(description = "上传文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "是否覆盖") @RequestParam(defaultValue = "false") Boolean overwrite,
            @Parameter(description = "脚本ID（可选）") @RequestParam(required = false) Integer scriptId) {

        ReturnResult<FileOperationResponse> result = fileManagementFacadeService.uploadFile(serverId, targetPath, file, overwrite);
        try {
            // 保存上传记录（REMOTE）
            com.chua.starter.monitor.entity.MonitorSysGenScriptUploadRecord record = new com.chua.starter.monitor.entity.MonitorSysGenScriptUploadRecord();
            record.setMonitorSysGenScriptId(scriptId);
            record.setMonitorSysGenScriptUploadType("REMOTE");
            record.setMonitorSysGenScriptUploadServerId(serverId);
            record.setMonitorSysGenScriptUploadTargetPath(targetPath);
            record.setMonitorSysGenScriptUploadFileName(file.getOriginalFilename());
            record.setMonitorSysGenScriptUploadFileSize(file.getSize());
            record.setMonitorSysGenScriptUploadStatus(result != null && result.isOk() ? "SUCCESS" : "FAILED");
            record.setMonitorSysGenScriptUploadErrorMessage(result != null && result.isOk() ? null : (result != null ? result.getMsg() : "未知错误"));
            uploadRecordService.save(record);
        } catch (Exception e) {
            log.warn("保存脚本上传记录失败(REMOTE)", e);
        }

        return result;
    }

    @Operation(summary = "下载文件", description = "下载指定文件")
    @GetMapping("/download")
    public void downloadFile(@Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String filePath,
            HttpServletResponse response) {

        try {
            ReturnResult<FileOperationResponse> result = fileManagementFacadeService.downloadFile(serverId, filePath);

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

    @Operation(summary = "预览文件", description = "获取文件内容用于预览")
    @GetMapping("/preview")
    public ResponseEntity<byte[]> previewFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String filePath,
            @Parameter(description = "预览类型") @RequestParam(defaultValue = "auto") String previewType,
            @Parameter(description = "最大文件大小(MB)") @RequestParam(defaultValue = "10") Integer maxSizeMB) {

        return fileManagementFacadeService.previewFile(serverId, filePath, previewType, maxSizeMB);
    }

    @Operation(summary = "删除文件", description = "删除指定文件或目录")
    @DeleteMapping("/delete")
    public ReturnResult<FileOperationResponse> deleteFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String path,
            @Parameter(description = "是否递归删除") @RequestParam(defaultValue = "false") Boolean recursive) {

        return fileManagementFacadeService.deleteFile(serverId, path, recursive);
    }

    @Operation(summary = "重命名文件", description = "重命名文件或目录")
    @PostMapping("/rename")
    public ReturnResult<FileOperationResponse> renameFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "原文件路径", required = true) @RequestParam String oldPath,
            @Parameter(description = "新文件名", required = true) @RequestParam String newName) {

        return fileManagementFacadeService.renameFile(serverId, oldPath, newName);
    }

    @Operation(summary = "创建目录", description = "创建新目录")
    @PostMapping("/mkdir")
    public ReturnResult<FileOperationResponse> createDirectory(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "目录路径", required = true) @RequestParam String path,
            @Parameter(description = "是否递归创建") @RequestParam(defaultValue = "true") Boolean recursive) {

        return fileManagementFacadeService.createDirectory(serverId, path, recursive);
    }

    @Operation(summary = "执行文件操作", description = "执行复杂的文件操作")
    @PostMapping("/execute")
    public ReturnResult<FileOperationResponse> executeOperation(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "操作请求", required = true) @RequestBody FileOperationRequest request) {

        return fileManagementFacadeService.executeOperation(serverId, request);
    }

    @Operation(summary = "测试连接", description = "测试文件管理连接")
    @GetMapping("/test")
    public ReturnResult<Boolean> testConnection(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId) {

        return fileManagementFacadeService.testConnection(serverId);
    }

    @Operation(summary = "复制文件", description = "复制文件或目录")
    @PostMapping("/copy")
    public ReturnResult<FileOperationResponse> copyFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "源路径", required = true) @RequestParam String sourcePath,
            @Parameter(description = "目标路径", required = true) @RequestParam String targetPath,
            @Parameter(description = "是否覆盖") @RequestParam(defaultValue = "false") Boolean overwrite) {

        return fileManagementFacadeService.copyFile(serverId, sourcePath, targetPath, overwrite);
    }

    @Operation(summary = "移动文件", description = "移动文件或目录")
    @PostMapping("/move")
    public ReturnResult<FileOperationResponse> moveFile(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "源路径", required = true) @RequestParam String sourcePath,
            @Parameter(description = "目标路径", required = true) @RequestParam String targetPath,
            @Parameter(description = "是否覆盖") @RequestParam(defaultValue = "false") Boolean overwrite) {

        return fileManagementFacadeService.moveFile(serverId, sourcePath, targetPath, overwrite);
    }

    @Operation(summary = "批量删除文件", description = "批量删除多个文件或目录")
    @PostMapping("/batch-delete")
    public ReturnResult<FileOperationResponse> batchDeleteFiles(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径列表", required = true) @RequestBody List<String> paths,
            @Parameter(description = "是否递归删除") @RequestParam(defaultValue = "false") Boolean recursive) {

        return fileManagementFacadeService.batchDeleteFiles(serverId, paths, recursive);
    }

    @Operation(summary = "读取文件内容", description = "读取文本文件内容")
    @GetMapping("/read")
    public ReturnResult<String> readFileContent(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String filePath,
            @Parameter(description = "文件编码") @RequestParam(defaultValue = "UTF-8") String encoding) {

        return fileManagementFacadeService.readFileContent(serverId, filePath, encoding);
    }

    @Operation(summary = "保存文件内容", description = "保存文本文件内容")
    @PostMapping("/write")
    public ReturnResult<FileOperationResponse> writeFileContent(
            @Parameter(description = "服务器ID", required = true) @RequestParam Integer serverId,
            @Parameter(description = "文件路径", required = true) @RequestParam String filePath,
            @Parameter(description = "文件内容", required = true) @RequestBody String content,
            @Parameter(description = "文件编码") @RequestParam(defaultValue = "UTF-8") String encoding,
            @Parameter(description = "是否创建备份") @RequestParam(defaultValue = "true") Boolean createBackup) {

        return fileManagementFacadeService.writeFileContent(serverId, filePath, content, encoding, createBackup);
    }

}
