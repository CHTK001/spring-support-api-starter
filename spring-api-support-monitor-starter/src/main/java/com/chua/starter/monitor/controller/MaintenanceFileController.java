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
@RequestMapping("/api/maintenance/file")
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
     * @param files              文件列表
     * @param maintenanceGroupId 维护组ID
     * @param maintenanceFilePath 目标路径
     * @param maintenanceFileExtract 是否解压 (1:是, 0:否)
     * @param maintenanceFileOverride 是否覆盖 (1:是, 0:否)
     * @return 上传结果
     */
    @PutMapping("/upload")
    @Operation(summary = "上传文件")
    public ReturnResult<FileUploadResponse> uploadFile(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("maintenanceGroupId") Long maintenanceGroupId,
            @RequestParam(value = "maintenanceFilePath", defaultValue = "/") String maintenanceFilePath,
            @RequestParam(value = "maintenanceFileExtract", defaultValue = "0") Integer maintenanceFileExtract,
            @RequestParam(value = "maintenanceFileOverride", defaultValue = "0") Integer maintenanceFileOverride) {

        FileUploadRequest request = new FileUploadRequest();
        request.setMaintenanceGroupId(maintenanceGroupId);
        request.setTargetPath(maintenanceFilePath);
        request.setExtract(maintenanceFileExtract == 1);
        request.setOverwrite(maintenanceFileOverride == 1);

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
} 