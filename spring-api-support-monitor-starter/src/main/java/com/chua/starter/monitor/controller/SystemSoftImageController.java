package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftImage;
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

import java.util.List;

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

    /**
     * 分页查询镜像列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param serverId 服务器ID
     * @param softId 软件ID
     * @param status 镜像状态
     * @param imageName 镜像名称
     * @return 分页镜像列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询镜像列表", description = "分页获取镜像列表")
    public ReturnResult<IPage<SystemSoftImage>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "软件ID") @RequestParam(required = false) Integer softId,
            @Parameter(description = "镜像状态") @RequestParam(required = false) SystemSoftImageStatus status,
            @Parameter(description = "镜像名称") @RequestParam(required = false) String imageName) {

        try {
            log.debug("分页查询镜像列表, page: {}, size: {}", page, size);

            IPage<SystemSoftImage> pageResult = systemSoftImageService.page(new Page<>(page, size));
            log.debug("分页查询镜像列表成功, count: {}", pageResult.getRecords().size());
            return ReturnResult.ok(pageResult);

        } catch (Exception e) {
            log.error("分页查询镜像列表失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据条件查询镜像列表
     *
     * @param serverId  服务器ID
     * @param softId    软件ID
     * @param status    镜像状态
     * @param imageName 镜像名称
     * @return 镜像列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询镜像列表", description = "根据条件查询镜像列表")
    public ReturnResult<List<SystemSoftImage>> list(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "软件ID") @RequestParam(required = false) Integer softId,
            @Parameter(description = "镜像状态") @RequestParam(required = false) SystemSoftImageStatus status,
            @Parameter(description = "镜像名称") @RequestParam(required = false) String imageName) {
        return systemSoftImageService.selectImageListForImage(serverId, softId, status, imageName);
    }

    /**
     * 根据镜像ID获取镜像详细信息
     *
     * @param id 镜像ID
     * @return 镜像详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取镜像详情", description = "根据镜像ID获取镜像详细信息")
    public ReturnResult<SystemSoftImage> getById(@Parameter(description = "镜像ID") @PathVariable Integer id) {
        return systemSoftImageService.getImageDetails(id);
    }

    /**
     * 从软件拉取镜像到服务器
     *
     * @param softId   软件ID
     * @param serverId 服务器ID
     * @param imageTag 镜像标签
     * @param config   拉取参数
     * @return 拉取结果
     */
    @PostMapping("/pull")
    @Operation(summary = "拉取镜像", description = "从软件拉取镜像到服务器")
    public ReturnResult<SystemSoftImage> pullImage(
            @Parameter(description = "软件ID") @RequestParam Integer softId,
            @Parameter(description = "服务器ID") @RequestParam Integer serverId,
            @Parameter(description = "镜像标签") @RequestParam(defaultValue = "latest") String imageTag,
            @Parameter(description = "拉取参数") @RequestBody(required = false) DockerPullImage config) {
        return systemSoftImageService.pullImageForImage(softId, serverId, imageTag, config);
    }

    /**
     * 基于镜像创建容器
     *
     * @param id     镜像ID
     * @param config 容器配置
     * @return 创建结果
     */
    @PostMapping("/{id}/start")
    @Operation(summary = "创建容器", description = "基于镜像创建容器")
    public ReturnResult<Boolean> createContainer(
            @Parameter(description = "镜像ID") @PathVariable Integer id,
            @Parameter(description = "容器配置") @RequestBody(required = false) DockerCreateContainer config) {
        return systemSoftImageService.createContainer(id, config);
    }

    /**
     * 从服务器中删除镜像（物理删除）
     *
     * @param id    镜像ID
     * @param force 是否强制删除
     * @return 删除结果
     */
    @DeleteMapping("/{id}/image")
    @Operation(summary = "删除镜像", description = "从服务器中删除镜像（物理删除）")
    public ReturnResult<Boolean> deleteImage(
            @Parameter(description = "镜像ID") @PathVariable Integer id,
            @Parameter(description = "是否强制删除") @RequestParam(defaultValue = "false") Boolean force) {
        return systemSoftImageService.removeImage(id, force);
    }

}