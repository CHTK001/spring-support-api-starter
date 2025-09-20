package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.service.SystemSoftImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统软件镜像管理控制器
 *
 * @author CH
 * @since 2025/01/15
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/system-soft-image")
@RequiredArgsConstructor
@Tag(name = "系统软件镜像管理", description = "系统软件镜像管理相关接口")
public class SystemSoftImageController {

    private final SystemSoftImageService systemSoftImageService;

    @GetMapping("/list")
    @Operation(summary = "查询镜像列表", description = "根据条件查询镜像列表")
    public ReturnResult<List<SystemSoftImage>> list(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "软件ID") @RequestParam(required = false) Integer softId,
            @Parameter(description = "镜像状态") @RequestParam(required = false) String status,
            @Parameter(description = "镜像名称") @RequestParam(required = false) String imageName) {
        return systemSoftImageService.selectImageList(serverId, softId, status, imageName);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取镜像详情", description = "根据镜像ID获取镜像详细信息")
    public ReturnResult<SystemSoftImage> getById(@Parameter(description = "镜像ID") @PathVariable Integer id) {
        return systemSoftImageService.getImageDetails(id);
    }

    @PostMapping
    @Operation(summary = "新增镜像", description = "新增镜像信息")
    public ReturnResult<SystemSoftImage> add(@Parameter(description = "镜像信息") @RequestBody @Validated SystemSoftImage systemSoftImage) {
        systemSoftImage.setSystemSoftImageId(null); // 确保是新增操作
        return systemSoftImageService.saveOrUpdate(systemSoftImage);
    }

    @PutMapping
    @Operation(summary = "更新镜像", description = "更新镜像信息")
    public ReturnResult<SystemSoftImage> update(@Parameter(description = "镜像信息") @RequestBody @Validated SystemSoftImage systemSoftImage) {
        if (systemSoftImage.getSystemSoftImageId() == null) {
            return ReturnResult.error("更新操作必须提供镜像ID");
        }
        return systemSoftImageService.saveOrUpdate(systemSoftImage);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除镜像记录", description = "删除镜像记录（不删除实际镜像）")
    public ReturnResult<Boolean> delete(@Parameter(description = "镜像ID") @PathVariable Integer id) {
        return systemSoftImageService.removeById(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除镜像记录", description = "批量删除镜像记录")
    public ReturnResult<Boolean> deleteBatch(@Parameter(description = "镜像ID列表") @RequestBody List<Integer> ids) {
        return systemSoftImageService.removeByIds(ids);
    }

    @PostMapping("/pull")
    @Operation(summary = "拉取镜像", description = "从软件拉取镜像到服务器")
    public ReturnResult<SystemSoftImage> pullImage(
            @Parameter(description = "软件ID") @RequestParam Integer softId,
            @Parameter(description = "服务器ID") @RequestParam Integer serverId,
            @Parameter(description = "镜像标签") @RequestParam(defaultValue = "latest") String imageTag,
            @Parameter(description = "拉取参数") @RequestBody(required = false) Map<String, Object> params) {
        return systemSoftImageService.pullImage(softId, serverId, imageTag, params);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "启动镜像", description = "启动镜像（创建并启动容器）")
    public ReturnResult<Boolean> startImage(
            @Parameter(description = "镜像ID") @PathVariable Integer id,
            @Parameter(description = "启动参数") @RequestBody(required = false) Map<String, Object> params) {
        return systemSoftImageService.startImage(id, params);
    }

    @PostMapping("/{id}/stop")
    @Operation(summary = "停止镜像", description = "停止镜像（停止容器）")
    public ReturnResult<Boolean> stopImage(@Parameter(description = "镜像ID") @PathVariable Integer id) {
        return systemSoftImageService.stopImage(id);
    }

    @PostMapping("/{id}/restart")
    @Operation(summary = "重启镜像", description = "重启镜像（重启容器）")
    public ReturnResult<Boolean> restartImage(@Parameter(description = "镜像ID") @PathVariable Integer id) {
        return systemSoftImageService.restartImage(id);
    }

    @DeleteMapping("/{id}/image")
    @Operation(summary = "删除镜像", description = "从服务器中删除镜像（物理删除）")
    public ReturnResult<Boolean> deleteImage(
            @Parameter(description = "镜像ID") @PathVariable Integer id,
            @Parameter(description = "是否强制删除") @RequestParam(defaultValue = "false") Boolean force) {
        return systemSoftImageService.deleteImage(id, force);
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "获取镜像日志", description = "获取镜像的运行日志")
    public ReturnResult<String> getImageLogs(@Parameter(description = "镜像ID") @PathVariable Integer id) {
        return systemSoftImageService.getImageLogs(id);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步镜像状态", description = "同步指定服务器的镜像状态")
    public ReturnResult<Integer> syncImageStatus(
            @Parameter(description = "服务器ID") @RequestParam Integer serverId) {
        return systemSoftImageService.syncImageStatus(serverId);
    }

    @GetMapping("/check-exists")
    @Operation(summary = "检查镜像是否存在", description = "检查指定镜像是否在服务器上存在")
    public ReturnResult<Boolean> checkImageExists(
            @Parameter(description = "镜像名称") @RequestParam String imageName,
            @Parameter(description = "镜像标签") @RequestParam String imageTag,
            @Parameter(description = "服务器ID") @RequestParam Integer serverId) {
        return systemSoftImageService.checkImageExists(imageName, imageTag, serverId);
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量更新状态", description = "批量更新镜像状态")
    public ReturnResult<Boolean> batchUpdateStatus(
            @Parameter(description = "镜像ID列表") @RequestParam List<Integer> imageIds,
            @Parameter(description = "新状态") @RequestParam String status) {
        return systemSoftImageService.batchUpdateStatus(imageIds, status);
    }

    @GetMapping("/software/{softId}")
    @Operation(summary = "按软件获取镜像", description = "根据软件ID获取镜像列表")
    public ReturnResult<List<SystemSoftImage>> getImagesBySoftId(@Parameter(description = "软件ID") @PathVariable Integer softId) {
        return systemSoftImageService.getImagesBySoftId(softId);
    }

    @GetMapping("/server/{serverId}")
    @Operation(summary = "按服务器获取镜像", description = "根据服务器ID获取镜像列表")
    public ReturnResult<List<SystemSoftImage>> getImagesByServerId(@Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        return systemSoftImageService.getImagesByServerId(serverId);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取镜像统计", description = "获取镜像统计信息")
    public ReturnResult<Map<String, Object>> getImageStatistics(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId) {
        return systemSoftImageService.getImageStatistics(serverId);
    }
}