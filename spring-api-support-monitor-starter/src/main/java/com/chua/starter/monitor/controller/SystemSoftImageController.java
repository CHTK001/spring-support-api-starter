package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.annotations.OnRouterEvent;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.service.SystemSoftImageService;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统软件镜像管理控制器
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
@Slf4j
@RestController
@RequestMapping("/v1/system/soft/image")
@RequiredArgsConstructor
@Api(tags = "系统软件镜像管理")
@Tag(name = "系统软件镜像管理", description = "系统软件镜像管理相关接口")
public class SystemSoftImageController {

    private final SystemSoftImageService systemSoftImageService;

    /**
     * 分页查询镜像列表
     *
     * @param current  当前页
     * @param size     每页大小
     * @param serverId 服务器ID
     * @param status   镜像状态
     * @param keyword  关键词
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询镜像列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页", defaultValue = "1", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "每页大小", defaultValue = "10", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "serverId", value = "服务器ID", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "镜像状态", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "关键词", dataType = "string", paramType = "query")
    })
    @OnRouterEvent("image-page")
    public ReturnPageResult<SystemSoftImage> getImagePage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer serverId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            log.info("分页查询镜像列表 - current: {}, size: {}, serverId: {}, status: {}, keyword: {}",
                    current, size, serverId, status, keyword);
            IPage<SystemSoftImage> result = systemSoftImageService.getImagePage(current, size, serverId, status, keyword);
            return ReturnPageResultUtils.ok(result);
        } catch (Exception e) {
            log.error("分页查询镜像列表失败", e);
            return ReturnPageResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据软件ID查询镜像列表
     *
     * @param softId 软件ID
     * @return 镜像列表
     */
    @GetMapping("/soft/{softId}")
    @ApiOperation("根据软件ID查询镜像列表")
    @ApiImplicitParam(name = "softId", value = "软件ID", required = true, dataType = "int", paramType = "path")
    @OnRouterEvent("image-by-soft")
    public ReturnResult<List<SystemSoftImage>> getImagesBySoftId(@PathVariable Integer softId) {
        try {
            log.info("根据软件ID查询镜像列表 - softId: {}", softId);
            List<SystemSoftImage> result = systemSoftImageService.getImagesBySoftId(softId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("根据软件ID查询镜像列表失败 - softId: {}", softId, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据服务器ID查询镜像列表
     *
     * @param serverId 服务器ID
     * @return 镜像列表
     */
    @GetMapping("/server/{serverId}")
    @ApiOperation("根据服务器ID查询镜像列表")
    @ApiImplicitParam(name = "serverId", value = "服务器ID", required = true, dataType = "int", paramType = "path")
    @OnRouterEvent("image-by-server")
    public ReturnResult<List<SystemSoftImage>> getImagesByServerId(@PathVariable Integer serverId) {
        try {
            log.info("根据服务器ID查询镜像列表 - serverId: {}", serverId);
            List<SystemSoftImage> result = systemSoftImageService.getImagesByServerId(serverId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("根据服务器ID查询镜像列表失败 - serverId: {}", serverId, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 同步单个镜像状态
     *
     * @param imageId 镜像ID
     * @return 同步结果
     */
    @PostMapping("/sync/{imageId}")
    @ApiOperation("同步单个镜像状态")
    @ApiImplicitParam(name = "imageId", value = "镜像ID", required = true, dataType = "int", paramType = "path")
    @OnRouterEvent("image-sync")
    public ReturnResult<Boolean> syncImageStatus(@PathVariable Integer imageId) {
        try {
            log.info("同步单个镜像状态 - imageId: {}", imageId);
            boolean result = systemSoftImageService.syncImageStatus(imageId);
            if (result) {
                return ReturnResult.ok(true, "镜像状态同步成功");
            } else {
                return ReturnResult.error("镜像状态同步失败");
            }
        } catch (Exception e) {
            log.error("同步单个镜像状态失败 - imageId: {}", imageId, e);
            return ReturnResult.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步镜像状态
     *
     * @param serverId 服务器ID
     * @return 同步结果
     */
    @PostMapping("/sync")
    @ApiOperation("同步镜像状态")
    @ApiImplicitParam(name = "serverId", value = "服务器ID", dataType = "int", paramType = "query")
    @OnRouterEvent("image-sync-all")
    public ReturnResult<Integer> syncAllImageStatus(@RequestParam(required = false) Integer serverId) {
        try {
            log.info("同步镜像状态 - serverId: {}", serverId);
            int result = systemSoftImageService.syncAllImageStatus(serverId);
            return ReturnResult.ok(result, "同步完成，更新了 " + result + " 个镜像状态");
        } catch (Exception e) {
            log.error("同步镜像状态失败 - serverId: {}", serverId, e);
            return ReturnResult.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步指定服务器的所有镜像状态
     *
     * @param serverId 服务器ID
     * @return 同步结果
     */
    @PostMapping("/sync/server/{serverId}")
    @ApiOperation("同步指定服务器的所有镜像状态")
    @ApiImplicitParam(name = "serverId", value = "服务器ID", required = true, dataType = "int", paramType = "path")
    @OnRouterEvent("image-sync-server")
    public ReturnResult<Boolean> syncAllImagesByServerId(@PathVariable Integer serverId) {
        try {
            log.info("同步指定服务器的所有镜像状态 - serverId: {}", serverId);
            boolean result = systemSoftImageService.syncAllImagesByServerId(serverId);
            if (result) {
                return ReturnResult.ok(true, "服务器镜像状态同步成功");
            } else {
                return ReturnResult.error("服务器镜像状态同步失败");
            }
        } catch (Exception e) {
            log.error("同步指定服务器的所有镜像状态失败 - serverId: {}", serverId, e);
            return ReturnResult.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 根据镜像名称和标签查询镜像
     *
     * @param imageName 镜像名称
     * @param imageTag  镜像标签
     * @param serverId  服务器ID
     * @return 镜像信息
     */
    @GetMapping("/detail")
    @ApiOperation("根据镜像名称和标签查询镜像")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imageName", value = "镜像名称", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "imageTag", value = "镜像标签", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "serverId", value = "服务器ID", required = true, dataType = "int", paramType = "query")
    })
    @OnRouterEvent("image-detail")
    public ReturnResult<SystemSoftImage> getImageByNameAndTag(
            @RequestParam String imageName,
            @RequestParam String imageTag,
            @RequestParam Integer serverId) {
        try {
            log.info("根据镜像名称和标签查询镜像 - imageName: {}, imageTag: {}, serverId: {}",
                    imageName, imageTag, serverId);
            SystemSoftImage result = systemSoftImageService.getImageByNameAndTag(imageName, imageTag, serverId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("根据镜像名称和标签查询镜像失败 - imageName: {}, imageTag: {}, serverId: {}",
                    imageName, imageTag, serverId, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 检查镜像是否存在
     *
     * @param imageName 镜像名称
     * @param imageTag  镜像标签
     * @param serverId  服务器ID
     * @return 是否存在
     */
    @GetMapping("/exists")
    @ApiOperation("检查镜像是否存在")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imageName", value = "镜像名称", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "imageTag", value = "镜像标签", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "serverId", value = "服务器ID", required = true, dataType = "int", paramType = "query")
    })
    @OnRouterEvent("image-exists")
    public ReturnResult<Boolean> checkImageExists(
            @RequestParam String imageName,
            @RequestParam String imageTag,
            @RequestParam Integer serverId) {
        try {
            log.info("检查镜像是否存在 - imageName: {}, imageTag: {}, serverId: {}",
                    imageName, imageTag, serverId);
            boolean result = systemSoftImageService.checkImageExists(imageName, imageTag, serverId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("检查镜像是否存在失败 - imageName: {}, imageTag: {}, serverId: {}",
                    imageName, imageTag, serverId, e);
            return ReturnResult.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用的镜像列表
     *
     * @return 可用镜像列表
     */
    @GetMapping("/available")
    @ApiOperation("获取可用的镜像列表")
    @OnRouterEvent("image-available")
    public ReturnResult<List<SystemSoftImage>> getAvailableImages() {
        try {
            log.info("获取可用的镜像列表");
            List<SystemSoftImage> result = systemSoftImageService.getAvailableImages();
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取可用的镜像列表失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取异常状态的镜像列表
     *
     * @return 异常镜像列表
     */
    @GetMapping("/abnormal")
    @ApiOperation("获取异常状态的镜像列表")
    @OnRouterEvent("image-abnormal")
    public ReturnResult<List<SystemSoftImage>> getAbnormalImages() {
        try {
            log.info("获取异常状态的镜像列表");
            List<SystemSoftImage> result = systemSoftImageService.getAbnormalImages();
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取异常状态的镜像列表失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据关键字搜索镜像
     *
     * @param keyword 关键字
     * @return 镜像列表
     */
    @GetMapping("/search")
    @ApiOperation("根据关键字搜索镜像")
    @ApiImplicitParam(name = "keyword", value = "关键字", required = true, dataType = "string", paramType = "query")
    @OnRouterEvent("image-search")
    public ReturnResult<List<SystemSoftImage>> searchImagesByKeyword(@RequestParam String keyword) {
        try {
            log.info("根据关键字搜索镜像 - keyword: {}", keyword);
            List<SystemSoftImage> result = systemSoftImageService.searchImagesByKeyword(keyword);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("根据关键字搜索镜像失败 - keyword: {}", keyword, e);
            return ReturnResult.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 更新镜像状态
     *
     * @param imageId 镜像ID
     * @param status  新状态
     * @return 更新结果
     */
    @PutMapping("/status/{imageId}")
    @ApiOperation("更新镜像状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imageId", value = "镜像ID", required = true, dataType = "int", paramType = "path"),
            @ApiImplicitParam(name = "status", value = "新状态", required = true, dataType = "string", paramType = "query")
    })
    @OnRouterEvent("image-status-update")
    public ReturnResult<Boolean> updateImageStatus(@PathVariable Integer imageId, @RequestParam String status) {
        try {
            log.info("更新镜像状态 - imageId: {}, status: {}", imageId, status);
            boolean result = systemSoftImageService.updateImageStatus(imageId, status);
            if (result) {
                return ReturnResult.ok(true, "镜像状态更新成功");
            } else {
                return ReturnResult.error("镜像状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新镜像状态失败 - imageId: {}, status: {}", imageId, status, e);
            return ReturnResult.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除镜像记录
     *
     * @param imageId 镜像ID
     * @return 删除结果
     */
    @DeleteMapping("/{imageId}")
    @ApiOperation("删除镜像记录")
    @ApiImplicitParam(name = "imageId", value = "镜像ID", required = true, dataType = "int", paramType = "path")
    @OnRouterEvent("image-delete")
    public ReturnResult<Boolean> deleteImage(@PathVariable Integer imageId) {
        try {
            log.info("删除镜像记录 - imageId: {}", imageId);
            boolean result = systemSoftImageService.deleteImage(imageId);
            if (result) {
                return ReturnResult.ok(true, "镜像记录删除成功");
            } else {
                return ReturnResult.error("镜像记录删除失败");
            }
        } catch (Exception e) {
            log.error("删除镜像记录失败 - imageId: {}", imageId, e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 统计镜像数量
     *
     * @param serverId 服务器ID
     * @param status   状态
     * @return 镜像数量
     */
    @GetMapping("/count")
    @ApiOperation("统计镜像数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "serverId", value = "服务器ID", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态", dataType = "string", paramType = "query")
    })
    @OnRouterEvent("image-count")
    public ReturnResult<Long> countImages(
            @RequestParam(required = false) Integer serverId,
            @RequestParam(required = false) String status) {
        try {
            log.info("统计镜像数量 - serverId: {}, status: {}", serverId, status);
            Long result = systemSoftImageService.countImages(serverId, status);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("统计镜像数量失败 - serverId: {}, status: {}", serverId, status, e);
            return ReturnResult.error("统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新拉取的镜像列表
     *
     * @param limit 限制数量
     * @return 最新镜像列表
     */
    @GetMapping("/latest")
    @ApiOperation("获取最新拉取的镜像列表")
    @ApiImplicitParam(name = "limit", value = "限制数量", defaultValue = "10", dataType = "int", paramType = "query")
    @OnRouterEvent("image-latest")
    public ReturnResult<List<SystemSoftImage>> getLatestImages(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("获取最新拉取的镜像列表 - limit: {}", limit);
            List<SystemSoftImage> result = systemSoftImageService.getLatestImages(limit);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取最新拉取的镜像列表失败 - limit: {}", limit, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }
}