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

    /**
     * 导出镜像到文件
     *
     * @param request 导出请求
     * @return 导出结果
     */
    @PostMapping("/export")
    @Operation(summary = "导出镜像", description = "导出镜像到文件")
    public ReturnResult<Map<String, Object>> exportImage(@RequestBody ExportImageRequest request) {
        try {
            log.debug("导出镜像, imageId: {}, serverId: {}", request.getImageId(), request.getServerId());
            return systemSoftImageService.exportImage(request.getImageId(), request.getServerId());
        } catch (Exception e) {
            log.error("导出镜像失败", e);
            return ReturnResult.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 从文件导入镜像
     *
     * @param serverId  服务器ID
     * @param filePath  文件路径
     * @param imageName 镜像名称
     * @param imageTag  镜像标签
     * @param force     是否强制导入
     * @return 导入结果
     */
    @PostMapping("/import")
    @Operation(summary = "导入镜像", description = "从文件导入镜像")
    public ReturnResult<Map<String, Object>> importImage(
            @Parameter(description = "服务器ID") @RequestParam Integer serverId,
            @Parameter(description = "文件路径") @RequestParam String filePath,
            @Parameter(description = "镜像名称") @RequestParam(required = false) String imageName,
            @Parameter(description = "镜像标签") @RequestParam(required = false) String imageTag,
            @Parameter(description = "是否强制导入") @RequestParam(defaultValue = "false") Boolean force) {
        try {
            log.debug("导入镜像, serverId: {}, filePath: {}", serverId, filePath);
            return systemSoftImageService.importImage(serverId, filePath, imageName, imageTag, force);
        } catch (Exception e) {
            log.error("导入镜像失败", e);
            return ReturnResult.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 同步镜像：从服务器同步Docker镜像到SystemSoftImage表
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @PostMapping("/sync")
    @Operation(summary = "同步镜像", description = "从指定服务器同步Docker镜像到系统")
    public ReturnResult<Map<String, Object>> syncImages(@RequestBody SyncImagesRequest request) {
        try {
            log.debug("同步镜像, serverIds: {}", request.getServerIds());
            if (request.getServerIds() == null || request.getServerIds().isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }
            return systemSoftImageService.syncImagesFromServers(request.getServerIds());
        } catch (Exception e) {
            log.error("同步镜像失败", e);
            return ReturnResult.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 导出镜像请求DTO
     */
    public static class ExportImageRequest {
        private Integer imageId;
        private Integer serverId;

        public Integer getImageId() {
            return imageId;
        }

        public void setImageId(Integer imageId) {
            this.imageId = imageId;
        }

        public Integer getServerId() {
            return serverId;
        }

        public void setServerId(Integer serverId) {
            this.serverId = serverId;
        }
    }

    /**
     * 同步镜像请求DTO
     */
    public static class SyncImagesRequest {
        private List<Integer> serverIds;

        public List<Integer> getServerIds() {
            return serverIds;
        }

        public void setServerIds(List<Integer> serverIds) {
            this.serverIds = serverIds;
        }
    }

}