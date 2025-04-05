package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.FileUploadRequest;
import com.chua.starter.monitor.pojo.response.FileUploadResponse;
import com.chua.starter.monitor.service.MaintenanceFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
     * @param file               文件
     * @param maintenanceGroupId 维护组ID
     * @param targetPath         目标路径
     * @param overwrite          是否覆盖
     * @param extract            是否解压
     * @return 上传结果
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public ReturnResult<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("groupId") Long maintenanceGroupId,
            @RequestParam("targetPath") String targetPath,
            @RequestParam(value = "overwrite", required = false) Boolean overwrite,
            @RequestParam(value = "extract", required = false) Boolean extract) {

        FileUploadRequest request = new FileUploadRequest();
        request.setMaintenanceGroupId(maintenanceGroupId);
        request.setTargetPath(targetPath);
        request.setOverwrite(overwrite);
        request.setExtract(extract);

        return maintenanceFileService.uploadFile(file, request);
    }
} 