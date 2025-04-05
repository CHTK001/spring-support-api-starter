package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceFile;
import com.chua.starter.monitor.pojo.request.FileDeployRequest;
import com.chua.starter.monitor.pojo.request.FileUploadRequest;
import com.chua.starter.monitor.pojo.request.MaintenanceFileRequest;
import com.chua.starter.monitor.pojo.response.FileDeployResponse;
import com.chua.starter.monitor.pojo.response.FileUploadResponse;
import com.chua.starter.monitor.service.MaintenanceFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    public ReturnResult<List<MaintenanceFile>> getFileList(
            @RequestParam("groupId") Long groupId) {
        return maintenanceFileService.getFilesByGroupId(groupId);
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
} 