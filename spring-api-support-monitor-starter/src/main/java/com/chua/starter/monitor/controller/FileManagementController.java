package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.report.client.starter.pojo.FileOperationRequest;
import com.chua.report.client.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.entity.MonitorSysGenScript;
import com.chua.starter.monitor.entity.MonitorSysGenScriptUploadRecord;
import com.chua.starter.monitor.pojo.RemoteOperationResponse;
import com.chua.starter.monitor.service.MonitorSysGenScriptService;
import com.chua.starter.monitor.service.NodeRemoteService;
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
    private final com.chua.starter.monitor.service.MonitorSysGenServerService monitorSysGenServerService;
    private final com.chua.starter.monitor.service.NodeSystemService nodeSystemService;

    private final MonitorSysGenScriptService monitorSysGenScriptService;
    private final NodeRemoteService nodeRemoteService;
    private final com.chua.starter.monitor.service.FileSystemService fileSystemService;

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
            @Parameter(description = "目标目录(可选，缺省将使用默认脚本目录)") @RequestParam(required = false) String targetPath,
            @Parameter(description = "上传文件，可选。如果不传则根据scriptId生成文件", required = false) @RequestParam(value = "file", required = false) MultipartFile file,
            @Parameter(description = "是否覆盖") @RequestParam(defaultValue = "false") Boolean overwrite,
            @Parameter(description = "脚本ID（可选）") @RequestParam(required = false) Integer scriptId) {

        ReturnResult<FileOperationResponse> result;
        String fileNameForRecord = null;
        Long fileSizeForRecord = null;
        try {
            String targetDir = (targetPath == null || targetPath.trim().isEmpty()) ? "/opt/monitor/scripts" : targetPath;
            if (file != null && !file.isEmpty()) {
                // 常规路径：直接传文件
                result = fileManagementFacadeService.uploadFile(serverId, targetDir, file, overwrite);
                fileNameForRecord = file.getOriginalFilename();
                fileSizeForRecord = file.getSize();
            } else if (scriptId != null) {
                // 新需求：根据脚本ID生成文件并上传
                MonitorSysGenScript script = monitorSysGenScriptService.getScriptById(scriptId);
                if (script == null) {
                    return ReturnResult.error("脚本不存在: " + scriptId);
                }
                String scriptName = script.getMonitorSysGenScriptName();
                String ext = script.getScriptTypeEnum() != null ? script.getScriptTypeEnum().getExtension() : ".sh";
                String fileName = scriptName + ext;

                byte[] content = (script.getMonitorSysGenScriptContent() != null ? script.getMonitorSysGenScriptContent() : "").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(content);

                result = fileManagementFacadeService.uploadFile(serverId, targetDir, fileName, inputStream, overwrite);
                fileNameForRecord = fileName;
                fileSizeForRecord = (long) content.length;
            } else {
                return ReturnResult.error("参数错误: file与scriptId至少提供一个");
            }
        } catch (Exception e) {
            log.error("上传文件失败", e);
            result = ReturnResult.error("上传文件失败: " + e.getMessage());
        }

        try {
            // 保存上传记录（REMOTE）
            MonitorSysGenScriptUploadRecord record = new MonitorSysGenScriptUploadRecord();
            record.setMonitorSysGenScriptId(scriptId);
            record.setMonitorSysGenScriptUploadType("REMOTE");
            record.setMonitorSysGenScriptUploadServerId(serverId);
            record.setMonitorSysGenScriptUploadTargetPath(targetPath);
            record.setMonitorSysGenScriptUploadFileName(fileNameForRecord);
            record.setMonitorSysGenScriptUploadFileSize(fileSizeForRecord);
            record.setMonitorSysGenScriptUploadStatus(result != null && result.isOk() ? "SUCCESS" : "FAILED");
            record.setMonitorSysGenScriptUploadErrorMessage(result != null && result.isOk() ? null : (result != null ? result.getMsg() : "未知错误"));
            // 直接记录脚本名称/描述与实际IP
            if (scriptId != null) {
                try {
                    MonitorSysGenScript sc = monitorSysGenScriptService.getScriptById(scriptId);
                    if (sc != null) {
                        record.setMonitorSysGenScriptUploadScriptName(sc.getMonitorSysGenScriptName());
                        record.setMonitorSysGenScriptUploadScriptDescription(sc.getMonitorSysGenScriptDescription());
                    }
                } catch (Exception ignore) {
                }
            }
            try {
                var sv = monitorSysGenServerService.getByIdWithCache(serverId);
                if (sv != null) {
                    record.setMonitorSysGenScriptUploadIp(sv.getMonitorSysGenServerHost());
                }
            } catch (Exception ignore) {
            }
            uploadRecordService.save(record);
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 上传脚本至节点
     */
    @Operation(summary = "上传脚本至节点", description = "根据脚本ID生成文件并上传到节点")
    @PostMapping("/upload-to-node")
    public ReturnResult<RemoteOperationResponse> uploadScriptToNode(
            @Parameter(description = "节点ID", required = true) @RequestParam String nodeId,
            @Parameter(description = "目标路径", required = true) @RequestParam String remoteFilePath,
            @Parameter(description = "是否覆盖") @RequestParam(defaultValue = "false") boolean overwrite,
            @Parameter(description = "脚本ID", required = true) @RequestParam Integer scriptId) {
        try {
            MonitorSysGenScript script = monitorSysGenScriptService.getScriptById(scriptId);
            if (script == null) {
                return ReturnResult.error("脚本不存在: " + scriptId);
            }
            String scriptName = script.getMonitorSysGenScriptName();
            String ext = script.getScriptTypeEnum() != null ? script.getScriptTypeEnum().getExtension() : ".sh";
            String fileName = scriptName + ext;
            byte[] content = (script.getMonitorSysGenScriptContent() != null ? script.getMonitorSysGenScriptContent() : "").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            java.io.InputStream inputStream = new java.io.ByteArrayInputStream(content);

            RemoteOperationResponse resp = nodeRemoteService.uploadFile(nodeId, inputStream, fileName, remoteFilePath, overwrite);

            try {
                // 保存上传记录（NODE）
                com.chua.starter.monitor.entity.MonitorSysGenScriptUploadRecord record = new MonitorSysGenScriptUploadRecord();
                record.setMonitorSysGenScriptId(scriptId);
                record.setMonitorSysGenScriptUploadType("NODE");
                record.setMonitorSysGenScriptUploadNodeId(nodeId);
                record.setMonitorSysGenScriptUploadTargetPath(remoteFilePath);
                record.setMonitorSysGenScriptUploadFileName(fileName);
                record.setMonitorSysGenScriptUploadFileSize((long) content.length);
                record.setMonitorSysGenScriptUploadStatus(resp != null ? resp.getStatus() : "SUCCESS");
                record.setMonitorSysGenScriptUploadErrorMessage(resp != null ? resp.getErrorMessage() : "未知错误");
                // 直接记录脚本名称/描述与实际IP（节点）
                if (script != null) {
                    try {
                        record.setMonitorSysGenScriptUploadScriptName(script.getMonitorSysGenScriptName());
                        record.setMonitorSysGenScriptUploadScriptDescription(script.getMonitorSysGenScriptDescription());
                    } catch (Exception ignore) {
                    }
                }
                try {
                    var nodeSystem = nodeSystemService.getById(nodeId);
                    if (nodeSystem != null) {
                        record.setMonitorSysGenScriptUploadIp(nodeSystem.getNodeSystemIpAddress());
                    }
                } catch (Exception ignore) {
                }
                uploadRecordService.save(record);
            } catch (Exception e) {
                log.warn("保存脚本上传记录失败(NODE)", e);
            }

            return ReturnResult.of(resp);
        } catch (Exception e) {
            log.error("上传脚本至节点失败: nodeId={}, remoteFilePath={}, scriptId={}", nodeId, remoteFilePath, scriptId, e);
            return ReturnResult.error("上传脚本至节点失败: " + e.getMessage());
        }
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

    @Operation(summary = "分发文件", description = "从源服务器读取文件并分发到目标服务器/节点")
    @PostMapping("/distribute")
    public ReturnResult<FileOperationResponse> distribute(@RequestBody com.chua.starter.monitor.pojo.request.FileDistributeRequest req) {
        try {
            if (req == null || req.getSourceServerId() == null || req.getSourceFilePath() == null
                    || req.getTargetType() == null || req.getTargetIds() == null || req.getTargetIds().isEmpty()
                    || req.getTargetDir() == null || req.getTargetDir().isEmpty()) {
                return ReturnResult.error("参数不完整");
            }

            // 1) 读取源文件
            ReturnResult<FileOperationResponse> dl = fileManagementFacadeService
                    .downloadFile(req.getSourceServerId(), req.getSourceFilePath());
            if (!dl.isSuccess() || dl.getData() == null) {
                return ReturnResult.error("读取源文件失败: " + dl.getMsg());
            }
            FileOperationResponse src = dl.getData();
            byte[] data = null;
            if (src.getContentBytes() != null && src.getContentBytes().length > 0) {
                data = src.getContentBytes();
            } else if (src.getDownloadUrl() != null) {
                String url = src.getDownloadUrl();
                if (url.startsWith("data:")) {
                    int comma = url.indexOf(',');
                    if (comma > 0) {
                        String b64 = url.substring(comma + 1);
                        data = java.util.Base64.getDecoder().decode(b64);
                    }
                } else {
                    java.nio.file.Path p = java.nio.file.Paths.get(url);
                    if (java.nio.file.Files.exists(p)) {
                        data = java.nio.file.Files.readAllBytes(p);
                    }
                }
            }
            if (data == null || data.length == 0) {
                return ReturnResult.error("无法获取源文件内容");
            }

            String fileName = req.getSourceFilePath();
            int idx = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
            if (idx >= 0 && idx + 1 < fileName.length()) {
                fileName = fileName.substring(idx + 1);
            }

            boolean overwrite = Boolean.TRUE.equals(req.getOverwrite());

            int total = req.getTargetIds().size();
            int success = 0;
            java.util.List<String> failed = new java.util.ArrayList<>();

            // 2) 分发到目标
            if ("SERVER".equalsIgnoreCase(req.getTargetType())) {
                for (String sidStr : req.getTargetIds()) {
                    try {
                        Integer sid = Integer.valueOf(sidStr);
                        var res = fileManagementFacadeService.uploadFile(sid, req.getTargetDir(), fileName,
                                new java.io.ByteArrayInputStream(data), overwrite);
                        if (res != null && res.isSuccess()) success++;
                        else failed.add("S:" + sidStr);
                    } catch (Exception ex) {
                        failed.add("S:" + sidStr + "(" + ex.getMessage() + ")");
                    }
                }
            } else if ("NODE".equalsIgnoreCase(req.getTargetType())) {
                for (String nid : req.getTargetIds()) {
                    try {
                        var resp = nodeRemoteService.uploadFile(nid, new java.io.ByteArrayInputStream(data), fileName,
                                req.getTargetDir(), overwrite);
                        if (resp != null && "SUCCESS".equalsIgnoreCase(resp.getStatus())) success++;
                        else failed.add("N:" + nid);
                    } catch (Exception ex) {
                        failed.add("N:" + nid + "(" + ex.getMessage() + ")");
                    }
                }
            } else {
                return ReturnResult.error("不支持的目标类型: " + req.getTargetType());
            }

            String msg = String.format("分发完成：成功 %d / 总计 %d%s", success, total,
                    failed.isEmpty() ? "" : ("，失败：" + String.join(", ", failed)));
            FileOperationResponse out = FileOperationResponse.success("DISTRIBUTE", msg);
            out.setTotalCount(total);
            out.setProcessedCount(success);
            return ReturnResult.ok(out);
        } catch (Exception e) {
            log.error("分发文件失败", e);
            return ReturnResult.error("分发文件失败: " + e.getMessage());
        }
    }


}
