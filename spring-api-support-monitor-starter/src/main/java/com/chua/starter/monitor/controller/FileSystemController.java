package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.FileSystem;
import com.chua.starter.monitor.entity.FileSystemSetting;
import com.chua.starter.monitor.pojo.FileInitUploadDTO;
import com.chua.starter.monitor.pojo.FileSystemStatisticsDTO;
import com.chua.starter.monitor.pojo.FileUploadStatusDTO;
import com.chua.starter.monitor.service.FileSystemService;
import com.chua.starter.monitor.service.FileSystemSettingService;
import com.chua.starter.monitor.service.FileSystemSseService;
import com.chua.starter.monitor.service.impl.FileManagementFacadeServiceImpl;
import com.chua.starter.monitor.task.FileSystemMergeQueue;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件系统管理控制器
 *
 * @author CH
 * @since 2024/12/30
 */
@Api(tags = "文件系统管理")
@Tag(name = "文件系统管理", description = "文件系统管理相关接口")
@Slf4j
@RestController
@RequestMapping("/v1/filesystem")
@RequiredArgsConstructor
public class FileSystemController {

    private final FileSystemService fileSystemService;
    private final FileSystemSettingService fileSystemSettingService;
    private final FileSystemSseService fileSystemSseService;
    private final FileSystemMergeQueue mergeQueue;
    final FileManagementFacadeServiceImpl fileManagementFacadeService;

    /**
     * 分页查询文件列表
     */
    @ApiOperation("分页查询文件列表")
    @Operation(summary = "分页查询文件列表", description = "支持按文件名、类型、状态等条件查询")
    @GetMapping("/page")
    public ReturnPageResult<FileSystem> page(Query<FileSystem> query, FileSystem fileSystem) {

        log.info("分页查询文件列表: query={}, fileSystem={}", query, fileSystem);

        try {
            if (fileSystem == null) {
                fileSystem = new FileSystem();
            }
            return fileSystemService.pageFor(query, fileSystem);
        } catch (Exception e) {
            log.error("分页查询文件列表失败", e);
            return ReturnPageResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 初始化分片上传
     */
    @ApiOperation("初始化分片上传")
    @Operation(summary = "初始化分片上传", description = "前端计算好文件MD5和大小后，调用此接口初始化分片上传")
    @PostMapping("/init-chunk-upload")
    public ReturnResult<FileInitUploadDTO> initChunkUpload(
            @ApiParam("文件名") @Parameter(description = "文件名") @RequestParam String fileName,
            @ApiParam("文件大小(字节)") @Parameter(description = "文件大小(字节)") @RequestParam Long fileSize,
            @ApiParam("文件MD5值") @Parameter(description = "文件MD5值") @RequestParam String fileMd5,
            @ApiParam("分片大小(字节)，可选") @Parameter(description = "分片大小(字节)，可选") @RequestParam(required = false, defaultValue = "0") Long chunkSize,
            @ApiParam("文件分组ID，可选") @Parameter(description = "文件分组ID，可选") @RequestParam(required = false) Integer groupId) {

        log.info("初始化分片上传: fileName={}, fileSize={}, fileMd5={}, chunkSize={}, groupId={}", fileName, fileSize, fileMd5,
                chunkSize, groupId);

        try {
            return fileSystemService.initChunkUpload(fileName, fileSize, fileMd5, chunkSize, groupId);
        } catch (Exception e) {
            log.error("初始化分片上传失败", e);
            return ReturnResult.error("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件分片
     */
    @ApiOperation("上传文件分片")
    @Operation(summary = "上传文件分片", description = "上传单个文件分片")
    @PostMapping("/upload-chunk")
    public ReturnResult<Boolean> uploadChunk(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @RequestParam Integer fileId,
            @ApiParam("分片序号") @Parameter(description = "分片序号") @RequestParam Integer chunkNumber,
            @ApiParam("分片文件") @Parameter(description = "分片文件") @RequestParam("file") MultipartFile chunkFile,
            @ApiParam("分片MD5值") @Parameter(description = "分片MD5值") @RequestParam(required = false) String chunkMd5) {

        log.info("上传文件分片: fileId={}, chunkNumber={}, chunkMd5={}", fileId, chunkNumber, chunkMd5);

        try {
            if (chunkFile.isEmpty()) {
                return ReturnResult.error("分片文件不能为空");
            }

            return fileSystemService.uploadChunk(fileId, chunkNumber, chunkFile, chunkMd5);
        } catch (Exception e) {
            log.error("上传文件分片失败", e);
            return ReturnResult.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 检查分片上传状态
     */
    @ApiOperation("检查分片上传状态")
    @Operation(summary = "检查分片上传状态", description = "查询文件的分片上传进度")
    @GetMapping("/upload-status/{fileId}")
    public ReturnResult<FileUploadStatusDTO> checkUploadStatus(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId) {

        log.info("检查分片上传状态: fileId={}", fileId);

        try {
            return fileSystemService.checkUploadStatus(fileId);
        } catch (Exception e) {
            log.error("检查分片上传状态失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发文件合并
     */
    @ApiOperation("手动触发文件合并")
    @Operation(summary = "手动触发文件合并", description = "手动触发文件分片合并")
    @PostMapping("/manual-merge/{fileId}")
    public ReturnResult<Boolean> manualMergeFile(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId) {

        log.info("手动触发文件合并: fileId={}", fileId);

        try {
            return fileSystemService.manualMergeFile(fileId);
        } catch (Exception e) {
            log.error("手动触发文件合并失败", e);
            return ReturnResult.error("合并失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @ApiOperation("删除文件")
    @Operation(summary = "删除文件", description = "删除指定文件及其分片")
    @DeleteMapping("/{fileId}")
    public ReturnResult<Boolean> deleteFile(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId) {

        log.info("删除文件: fileId={}", fileId);

        try {
            return fileSystemService.deleteFile(fileId);
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 设置文件分组
     */
    @ApiOperation("设置文件分组")
    @Operation(summary = "设置文件分组", description = "为已上传的文件设置分组")
    @PutMapping("/set-group/{fileId}")
    public ReturnResult<Boolean> setFileGroup(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId,
            @ApiParam("分组ID") @Parameter(description = "分组ID，null表示移除分组") @RequestParam(required = false) Integer groupId) {

        log.info("设置文件分组: fileId={}, groupId={}", fileId, groupId);

        try {
            return fileSystemService.setFileGroup(fileId, groupId);
        } catch (Exception e) {
            log.error("设置文件分组失败", e);
            return ReturnResult.error("设置分组失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     */
    @ApiOperation("批量删除文件")
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    @DeleteMapping("/batch")
    public ReturnResult<Boolean> batchDeleteFiles(
            @ApiParam("文件ID列表") @Parameter(description = "文件ID列表") @RequestBody List<Integer> fileIds) {

        log.info("批量删除文件: fileIds={}", fileIds);

        try {
            return fileSystemService.batchDeleteFiles(fileIds);
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 分发（以文件系统 fileId 为源）到服务器/节点
     */
    @ApiOperation("分发文件")
    @Operation(summary = "分发文件", description = "以文件系统 fileId 为源，分发至服务器或节点")
    @PostMapping("/distribute")
    public ReturnResult<Map<String, Object>> distributeFromFileSystem(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @RequestBody Map<String, Object> body) {
        try {
            Integer fileId = body.get("fileId") != null ? Integer.valueOf(body.get("fileId").toString()) : null;
            String targetType = body.get("targetType") != null ? body.get("targetType").toString() : null;
            @SuppressWarnings("unchecked")
            List<Object> ids = (List<Object>) body.get("targetIds");
            String targetDir = body.get("targetDir") != null ? body.get("targetDir").toString() : null;
            Boolean overwrite = body.get("overwrite") != null ? Boolean.valueOf(body.get("overwrite").toString()) : false;
            if (fileId == null || targetType == null || ids == null || ids.isEmpty() || targetDir == null) {
                return ReturnResult.error("参数不完整");
            }

            // 读取文件字节
            ReturnResult<byte[]> down = fileSystemService.downloadFile(fileId);
            if (!down.isSuccess() || down.getData() == null) {
                return ReturnResult.error("读取源文件失败: " + down.getMsg());
            }
            byte[] data = down.getData();

            // 获取文件名
            ReturnResult<FileSystem> detail = fileSystemService.getFileDetail(fileId);
            String fileName = detail.isSuccess() && detail.getData() != null
                    ? detail.getData().getFileSystemOriginalName()
                    : ("file-" + fileId);

            int total = ids.size();
            int success = 0;
            List<String> failed = new java.util.ArrayList<>();

            if ("SERVER".equalsIgnoreCase(targetType)) {
                for (Object sidObj : ids) {
                    try {
                        Integer sid = Integer.valueOf(String.valueOf(sidObj));
                        // 复用文件管理上传接口（以流形式）
                        var res = fileManagementFacadeService.uploadFile(sid, targetDir, fileName, new ByteArrayInputStream(data), overwrite);
                        if (res != null && res.isSuccess()) {
                            success++;
                        } else {
                            failed.add("S:" + sid);
                        }
                    } catch (Exception ex) {
                        failed.add("S:" + sidObj + "(" + ex.getMessage() + ")");
                    }
                }
            } else if ("NODE".equalsIgnoreCase(targetType)) {
                // 需要 NodeRemoteService，这里直接通过 FileManagementController 的 nodeRemoteService 更合理
                return ReturnResult.error("请使用 /v1/file-management/distribute 进行节点分发");
            } else {
                return ReturnResult.error("不支持的目标类型: " + targetType);
            }

            Map<String, Object> out = new HashMap<>();
            out.put("total", total);
            out.put("success", success);
            out.put("message", String.format("分发完成：成功 %d / 总计 %d%s", success, total,
                    failed.isEmpty() ? "" : ("，失败：" + String.join(", ", failed))));
            return ReturnResult.ok(out);
        } catch (Exception e) {
            log.error("分发文件失败(fileSystem)", e);
            return ReturnResult.error("分发文件失败: " + e.getMessage());
        }

    }

    /**
     * 下载文件
     */
    @ApiOperation("下载文件")
    @Operation(summary = "下载文件", description = "下载指定文件")
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId) {

        log.info("下载文件: fileId={}", fileId);

        try {
            ReturnResult<byte[]> result = fileSystemService.downloadFile(fileId);
            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getMsg().getBytes());
            }

            // 获取文件信息
            ReturnResult<FileSystem> fileResult = fileSystemService.getFileDetail(fileId);
            String fileName = "download";
            if (fileResult.isSuccess() && fileResult.getData() != null) {
                fileName = fileResult.getData().getFileSystemOriginalName();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok().headers(headers).body(result.getData());

        } catch (Exception e) {
            log.error("下载文件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("下载失败: " + e.getMessage()).getBytes());
        }
    }

    /**
     * 获取文件HTTP访问URL
     */
    @ApiOperation("获取文件HTTP访问URL")
    @Operation(summary = "获取文件HTTP访问URL", description = "获取文件的HTTP访问链接")
    @GetMapping("/http-url/{fileId}")
    public ReturnResult<String> getHttpAccessUrl(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId) {

        log.info("获取文件HTTP访问URL: fileId={}", fileId);

        try {
            return fileSystemService.getHttpAccessUrl(fileId);
        } catch (Exception e) {
            log.error("获取文件HTTP访问URL失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用文件HTTP访问
     */
    @ApiOperation("启用/禁用文件HTTP访问")
    @Operation(summary = "启用/禁用文件HTTP访问", description = "切换文件的HTTP访问状态")
    @PutMapping("/http-access/{fileId}")
    public ReturnResult<Boolean> toggleHttpAccess(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId,
            @ApiParam("是否启用") @Parameter(description = "是否启用") @RequestParam Boolean enabled) {

        log.info("切换文件HTTP访问状态: fileId={}, enabled={}", fileId, enabled);

        try {
            return fileSystemService.toggleHttpAccess(fileId, enabled);
        } catch (Exception e) {
            log.error("切换文件HTTP访问状态失败", e);
            return ReturnResult.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 绑定服务并开启HTTP访问（参考服务管理）
     */
    @ApiOperation("配置并开启HTTP访问")
    @Operation(summary = "配置并开启HTTP访问", description = "根据传入的服务ID或端口创建/更新服务，并绑定到文件后开启HTTP访问")
    @PutMapping("/http-access/{fileId}/config")
    public ReturnResult<Boolean> configHttpAccess(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId,
            @RequestBody com.chua.starter.monitor.pojo.request.FileHttpAccessConfigRequest request
    ) {
        try {
            return fileSystemService.configHttpAccess(fileId, request);
        } catch (Exception e) {
            log.error("配置并开启HTTP访问失败", e);
            return ReturnResult.error("配置失败: " + e.getMessage());
        }
    }


    /**
     * 获取文件统计信息
     */
    @ApiOperation("获取文件统计信息")
    @Operation(summary = "获取文件统计信息", description = "获取文件系统的统计数据")
    @GetMapping("/statistics")
    public ReturnResult<FileSystemStatisticsDTO> getFileStatistics() {

        log.info("获取文件统计信息");

        try {
            return fileSystemService.getFileStatistics();
        } catch (Exception e) {
            log.error("获取文件统计信息失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期文件
     */
    @ApiOperation("清理过期文件")
    @Operation(summary = "清理过期文件", description = "根据配置清理过期的文件")
    @PostMapping("/clean-expired")
    public ReturnResult<Integer> cleanExpiredFiles() {

        log.info("清理过期文件");

        try {
            return fileSystemService.cleanExpiredFiles();
        } catch (Exception e) {
            log.error("清理过期文件失败", e);
            return ReturnResult.error("清理失败: " + e.getMessage());
        }
    }

    /**
     * 重试失败的合并任务
     */
    @ApiOperation("重试失败的合并任务")
    @Operation(summary = "重试失败的合并任务", description = "重新尝试合并失败的文件")
    @PostMapping("/retry-merge/{fileId}")
    public ReturnResult<Boolean> retryMergeTask(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId) {

        log.info("重试失败的合并任务: fileId={}", fileId);

        try {
            return fileSystemService.retryMergeTask(fileId);
        } catch (Exception e) {
            log.error("重试失败的合并任务失败", e);
            return ReturnResult.error("重试失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件详情
     */
    @ApiOperation("获取文件详情")
    @Operation(summary = "获取文件详情", description = "获取指定文件的详细信息")
    @GetMapping("/{fileId}")
    public ReturnResult<FileSystem> getFileDetail(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId) {

        log.info("获取文件详情: fileId={}", fileId);

        try {
            return fileSystemService.getFileDetail(fileId);
        } catch (Exception e) {
            log.error("获取文件详情失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     */
    @ApiOperation("检查文件是否存在")
    @Operation(summary = "检查文件是否存在", description = "根据MD5值检查文件是否已存在")
    @GetMapping("/check-exists")
    public ReturnResult<FileSystem> checkFileExists(
            @ApiParam("文件MD5值") @Parameter(description = "文件MD5值") @RequestParam String fileMd5) {

        log.info("检查文件是否存在: fileMd5={}", fileMd5);

        try {
            return fileSystemService.checkFileExists(fileMd5);
        } catch (Exception e) {
            log.error("检查文件是否存在失败", e);
            return ReturnResult.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * HTTP访问文件（用于直接访问文件）
     */
    @ApiOperation("HTTP访问文件")
    @Operation(summary = "HTTP访问文件", description = "通过HTTP直接访问文件内容")
    @GetMapping("/access/{fileId}")
    public ResponseEntity<byte[]> accessFile(
            @ApiParam("文件ID") @Parameter(description = "文件ID") @PathVariable Integer fileId) {

        log.info("HTTP访问文件: fileId={}", fileId);

        try {
            // 检查文件是否启用HTTP访问
            ReturnResult<FileSystem> fileResult = fileSystemService.getFileDetail(fileId);
            if (!fileResult.isSuccess()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("文件不存在".getBytes());
            }

            FileSystem fileSystem = fileResult.getData();
            if (!Boolean.TRUE.equals(fileSystem.getFileSystemHttpAccessEnabled())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("文件HTTP访问未启用".getBytes());
            }

            // 下载文件内容
            ReturnResult<byte[]> result = fileSystemService.downloadFile(fileId);
            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getMsg().getBytes());
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            String contentType = getContentType(fileSystem.getFileSystemExtension());
            headers.setContentType(MediaType.parseMediaType(contentType));

            // 如果是图片、视频等可预览的文件，设置为inline；否则设置为attachment
            if (isPreviewableFile(fileSystem.getFileSystemType())) {
                headers.setContentDispositionFormData("inline", fileSystem.getFileSystemOriginalName());
            } else {
                headers.setContentDispositionFormData("attachment", fileSystem.getFileSystemOriginalName());
            }

            return ResponseEntity.ok().headers(headers).body(result.getData());

        } catch (Exception e) {
            log.error("HTTP访问文件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("访问失败: " + e.getMessage()).getBytes());
        }
    }

    /**
     * 获取内容类型
     */
    private String getContentType(String fileExtension) {
        if (fileExtension == null) {
            return "application/octet-stream";
        }

        switch (fileExtension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 判断是否为可预览的文件类型
     */
    private boolean isPreviewableFile(String fileType) {
        if (fileType == null) {
            return false;
        }

        switch (fileType.toLowerCase()) {
            case "image":
            case "video":
            case "audio":
            case "pdf":
            case "text":
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取文件系统配置
     */
    @ApiOperation("获取文件系统配置")
    @Operation(summary = "获取文件系统配置", description = "获取当前启用的文件系统配置")
    @GetMapping("/config")
    public ReturnResult<FileSystemSetting> getFileSystemConfig() {

        log.info("获取文件系统配置");

        try {
            FileSystemSetting setting = fileSystemSettingService.getEnabledSetting();
            if (setting == null) {
                // 返回默认配置
                setting = createDefaultConfig();
            }

            // 转换为前端期望的格式
            return ReturnResult.ok(setting);
        } catch (Exception e) {
            log.error("获取文件系统配置失败", e);
            return ReturnResult.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 解析允许的文件类型
     */
    private String[] parseAllowedFileTypes(String allowedFileTypes) {
        if (allowedFileTypes == null || allowedFileTypes.trim().isEmpty() || "*".equals(allowedFileTypes.trim())) {
            return new String[]{"*"};
        }
        return allowedFileTypes.split(",");
    }

    /**
     * 更新文件系统配置
     */
    @ApiOperation("更新文件系统配置")
    @Operation(summary = "更新文件系统配置", description = "更新文件系统配置信息")
    @PutMapping("/config")
    public ReturnResult<Boolean> updateFileSystemConfig(@RequestBody FileSystemSetting config) {

        log.info("更新文件系统配置: config={}", config);

        try {
            // 如果是更新现有配置
            if (config.getFileSystemSettingId() != null) {
                return fileSystemSettingService.updateSettingWithCache(config);
            } else {
                // 如果是新配置，先禁用其他配置再启用这个
                config.setFileSystemSettingEnabled(true);
                boolean result = fileSystemSettingService.save(config);
                if (result && config.getFileSystemSettingId() != null) {
                    return fileSystemSettingService.enableSetting(config.getFileSystemSettingId());
                } else {
                    return ReturnResult.error("保存配置失败");
                }
            }
        } catch (Exception e) {
            log.error("更新文件系统配置失败", e);
            return ReturnResult.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件系统实时状态
     */
    @ApiOperation("获取文件系统实时状态")
    @Operation(summary = "获取文件系统实时状态", description = "获取文件系统的实时运行状态")
    @GetMapping("/realtime-status")
    public ReturnResult<Map<String, Object>> getFileSystemRealtimeStatus() {

        log.info("获取文件系统实时状态");

        try {
            Map<String, Object> status = new HashMap<>();

            // 获取统计信息
            ReturnResult<FileSystemStatisticsDTO> statisticsResult = fileSystemService.getFileStatistics();
            if (statisticsResult.isSuccess()) {
                FileSystemStatisticsDTO stats = statisticsResult.getData();
                status.put("uploadingCount", 0); // 上传中的文件数，需要根据实际状态计算
                status.put("mergingCount", stats.getMergingFiles());
                status.put("queueLength", stats.getPendingFiles());
                status.put("totalSpeed", 0); // 这里可以根据实际需求计算
                status.put("activeConnections", 0); // 这里可以根据实际需求计算
                status.put("systemLoad", 0); // 这里可以根据实际需求计算
            } else {
                // 返回默认状态
                status.put("uploadingCount", 0);
                status.put("mergingCount", 0);
                status.put("queueLength", 0);
                status.put("totalSpeed", 0);
                status.put("activeConnections", 0);
                status.put("systemLoad", 0);
            }

            return ReturnResult.ok(status);
        } catch (Exception e) {
            log.error("获取文件系统实时状态失败", e);
            return ReturnResult.error("获取状态失败: " + e.getMessage());
        }
    }

    /**
     * 创建默认配置
     */
    private FileSystemSetting createDefaultConfig() {
        FileSystemSetting setting = new FileSystemSetting();
        setting.setFileSystemSettingName("默认配置");
        setting.setFileSystemSettingChunkUploadEnabled(true);
        setting.setFileSystemSettingChunkSizeMb(10);
        setting.setFileSystemSettingMergeTaskLimit(5);
        setting.setFileSystemSettingManualMergeEnabled(false);
        setting.setFileSystemSettingDownloadEnabled(true);
        setting.setFileSystemSettingHttpAccessEnabled(false);
        setting.setFileSystemSettingAllowedFileTypes("*");
        setting.setFileSystemSettingMaxFileSizeMb(1024);
        setting.setFileSystemSettingFileRetentionDays(30);
        setting.setFileSystemSettingEnabled(true);
        return setting;
    }

    /**
     * 获取合并队列状态
     */
    @ApiOperation("获取合并队列状态")
    @Operation(summary = "获取合并队列状态", description = "查看当前合并队列的状态信息")
    @GetMapping("/merge-queue/status")
    public ReturnResult<Map<String, Object>> getMergeQueueStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("queueSize", mergeQueue.size());
        status.put("isEmpty", mergeQueue.isEmpty());
        status.put("statistics", mergeQueue.getStatistics());
        status.put("healthStatus", mergeQueue.getHealthStatus());
        return ReturnResult.ok(status);
    }

    /**
     * 清空合并队列
     */
    @ApiOperation("清空合并队列")
    @Operation(summary = "清空合并队列", description = "清空当前合并队列中的所有待处理项")
    @PostMapping("/merge-queue/clear")
    public ReturnResult<Boolean> clearMergeQueue() {
        mergeQueue.clear();
        return ReturnResult.ok(true);
    }

    /**
     * 重置合并队列统计信息
     */
    @ApiOperation("重置合并队列统计信息")
    @Operation(summary = "重置合并队列统计信息", description = "重置合并队列的统计计数器")
    @PostMapping("/merge-queue/reset-statistics")
    public ReturnResult<Boolean> resetMergeQueueStatistics() {
        mergeQueue.resetStatistics();
        return ReturnResult.ok(true);
    }
}
