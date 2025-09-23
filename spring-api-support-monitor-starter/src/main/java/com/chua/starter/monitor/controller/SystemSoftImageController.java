package com.chua.starter.monitor.controller;

import com.chua.starter.monitor.entity.SystemSoftImageStatus;
import com.chua.starter.monitor.service.SystemSoftImageService;
import com.chua.docker.support.client.DockerCreateContainer;
import com.chua.docker.support.client.DockerPullImage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public Object list(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "软件ID") @RequestParam(required = false) Integer softId,
            @Parameter(description = "镜像状态") @RequestParam(required = false) SystemSoftImageStatus status,
            @Parameter(description = "镜像名称") @RequestParam(required = false) String imageName) {
        return systemSoftImageService.selectImageList(serverId, softId, status, imageName);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取镜像详情", description = "根据镜像ID获取镜像详细信息")
    public Object getById(@Parameter(description = "镜像ID") @PathVariable Integer id) {
        return systemSoftImageService.getImageDetails(id);
    }

    @PostMapping("/pull")
    @Operation(summary = "拉取镜像", description = "从软件拉取镜像到服务器")
    public Object pullImage(
            @Parameter(description = "软件ID") @RequestParam Integer softId,
            @Parameter(description = "服务器ID") @RequestParam Integer serverId,
            @Parameter(description = "镜像标签") @RequestParam(defaultValue = "latest") String imageTag,
            @Parameter(description = "拉取参数") @RequestBody(required = false) DockerPullImage config) {
        return systemSoftImageService.pullImage(softId, serverId, imageTag, config);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "创建容器", description = "基于镜像创建容器")
    public Object createContainer(
            @Parameter(description = "镜像ID") @PathVariable Integer id,
            @Parameter(description = "容器配置") @RequestBody(required = false) DockerCreateContainer config) {
        return systemSoftImageService.createContainer(id, config);
    }

    @DeleteMapping("/{id}/image")
    @Operation(summary = "删除镜像", description = "从服务器中删除镜像（物理删除）")
    public Object deleteImage(
            @Parameter(description = "镜像ID") @PathVariable Integer id,
            @Parameter(description = "是否强制删除") @RequestParam(defaultValue = "false") Boolean force) {
        return systemSoftImageService.removeImage(id, force);
    }

}