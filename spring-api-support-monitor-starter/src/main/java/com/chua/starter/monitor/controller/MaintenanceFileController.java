package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceFile;
import com.chua.starter.monitor.pojo.request.FileDeployRequest;
import com.chua.starter.monitor.pojo.request.FileUploadRequest;
import com.chua.starter.monitor.pojo.request.MaintenanceFileRequest;
import com.chua.starter.monitor.pojo.response.FileDeployResponse;
import com.chua.starter.monitor.pojo.response.FileUploadResponse;
import com.chua.starter.monitor.service.MaintenanceFileService;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 维护文件管理控制器
 * 提供文件上传等接口
 *
 * @author CH
 */
@RestController
@RequestMapping("/v1/maintenance/file")
@Tag(name = "维护文件管理")
public class MaintenanceFileController {


    /**
     * 维护文件服务
     */
    @Resource
    private MaintenanceFileService maintenanceFileService;

    /**
     * 上传文件
     *
     * @param file              文件
     * @param maintenanceGroupId 维护组ID
     * @param targetPath 目标路径
     * @param extract 是否解压 (1:是, 0:否)
     * @param overwrite 是否覆盖 (1:是, 0:否)
     * @return 上传结果
     */
    @PutMapping("/upload")
    @Operation(summary = "上传文件")
    public ReturnResult<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("maintenanceGroupId") Long maintenanceGroupId,
            @RequestParam(value = "targetPath", defaultValue = "/") String targetPath,
            @RequestParam(value = "extract", required = false) Boolean extract,
            @RequestParam(value = "overwrite", required = false) Boolean overwrite) {

        if (maintenanceGroupId == null) {
            return ReturnResult.error("维护组ID不能为空");
        }

        FileUploadRequest request = new FileUploadRequest();
        request.setMaintenanceGroupId(maintenanceGroupId);
        request.setTargetPath(targetPath);
        request.setExtract(extract);
        request.setOverwrite(overwrite);

        return maintenanceFileService.uploadFile(file, request);
    }
    
    /**
     * 批量上传文件
     *
     * @param files             文件数组
     * @param maintenanceGroupId 维护组ID
     * @param targetPath 目标路径
     * @param extract 是否解压 (1:是, 0:否)
     * @param overwrite 是否覆盖 (1:是, 0:否)
     * @return 上传结果
     */
    @PutMapping("/upload/batch")
    @Operation(summary = "批量上传文件")
    public ReturnResult<FileUploadResponse> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("maintenanceGroupId") Long maintenanceGroupId,
            @RequestParam(value = "targetPath", defaultValue = "/") String targetPath,
            @RequestParam(value = "extract", required = false) Boolean extract,
            @RequestParam(value = "overwrite", required = false) Boolean overwrite) {

        if (maintenanceGroupId == null) {
            return ReturnResult.error("维护组ID不能为空");
        }

        FileUploadRequest request = new FileUploadRequest();
        request.setMaintenanceGroupId(maintenanceGroupId);
        request.setTargetPath(targetPath);
        request.setExtract(extract);
        request.setOverwrite(overwrite);

        return maintenanceFileService.uploadFiles(files, request);
    }
    
    /**
     * 获取维护文件列表
     *
     * @param groupId 维护组ID
     * @return 文件列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取维护文件列表")
    public ReturnPageResult<MaintenanceFile> getFileList(
            @RequestParam("groupId") Long groupId, @ParameterObject Query<MaintenanceFile> query) {
        return maintenanceFileService.getFilesByGroupId(groupId, query);
    }
    
    /**
     * 删除维护文件
     *
     * @param id 文件ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除维护文件")
    public ReturnResult<Boolean> deleteFile(@PathVariable("id") Long id) {
        return maintenanceFileService.deleteFile(id);
    }
    
    /**
     * 更新维护文件信息
     *
     * @param request 文件信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新维护文件信息")
    public ReturnResult<Boolean> updateFile(@RequestBody MaintenanceFileRequest request) {
        return maintenanceFileService.updateFile(request);
    }
    
    /**
     * 部署文件到所有主机
     *
     * @param id 文件ID
     * @param request 部署参数
     * @return 部署结果
     */
    @PostMapping("/deploy/{id}")
    @Operation(summary = "部署文件到所有主机")
    public ReturnResult<FileDeployResponse> deployFile(
            @PathVariable("id") Long id,
            @RequestBody FileDeployRequest request) {
        request.setMaintenanceFileId(id);
        return maintenanceFileService.deployFile(request);
    }
    
    /**
     * 同步文件到远程主机
     *
     * @param id 文件ID
     * @return 同步任务ID
     */
    @PostMapping("/{id}/sync")
    @Operation(summary = "同步文件到远程主机", description = "将指定文件同步到维护组下所有启用的主机，返回同步任务ID")
    public ReturnResult<String> syncFile(@PathVariable("id") Long id) {
        return maintenanceFileService.syncFile(id);
    }

    /**
     * 替换文件
     *
     * @param file      新文件
     * @param fileId    被替换的文件ID
     * @param overwrite 是否覆盖（默认为true）
     * @return 替换结果
     */
    @PostMapping("/replace/{fileId}")
    @Operation(summary = "替换文件")
    public ReturnResult<FileUploadResponse> replaceFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable("fileId") Long fileId,
            @RequestParam(value = "overwrite", defaultValue = "true") Boolean overwrite) {

        if (fileId == null) {
            return ReturnResult.error("文件ID不能为空");
        }

        return maintenanceFileService.replaceFile(file, fileId, overwrite);
    }
    
    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件响应
     */
    @GetMapping("/download/{fileId}")
    @Operation(summary = "下载文件")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") Long fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("文件ID不能为空");
        }
        
        try {
            // 获取文件信息
            MaintenanceFile file = maintenanceFileService.getById(fileId);
            if (file == null) {
                throw new IllegalArgumentException("文件不存在");
            }
            
            // 构建文件路径
            String filePath = maintenanceFileService.getFullFilePath(fileId);
            
            // 创建文件资源
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("文件不可读或不存在: " + filePath);
            }
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(file.getMaintenanceFileName(), StandardCharsets.UTF_8));
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }
} 