package com.chua.starter.video.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoSyncConfig;
import com.chua.starter.video.service.VideoSyncConfigService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 * 视频同步配置接口
 *
 * @author CH
 * @since 2024/6/21
 */
@RestController
@RequestMapping("v1/video/sync")
@Tag(name = "视频同步配置接口")
@Api(tags = "视频同步配置接口")
@RequiredArgsConstructor
@Slf4j
public class VideoSyncConfigController {

    private final VideoSyncConfigService videoSyncConfigService;

    /**
     * 获取所有同步配置
     *
     * @return 同步配置列表
     */
    @GetMapping("/page")
    @Operation(summary = "获取所有同步配置")
    public ReturnPageResult<VideoSyncConfig> getAllConfigs(Query<VideoSyncConfig> query) {
        return ReturnPageResultUtils.ok(videoSyncConfigService.getAllConfigs(query));
    }

    /**
     * 获取同步配置详情
     *
     * @param id 配置ID
     * @return 同步配置详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取同步配置详情")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = "video:sync:config:detail", key = "#id", unless = "#result.data == null")
    public ReturnResult<VideoSyncConfig> getConfigById(@PathVariable String id) {
        VideoSyncConfig config = videoSyncConfigService.getConfigById(id);
        return ReturnResult.ofNone(config);
    }

    /**
     * 添加同步配置
     *
     * @param config 同步配置
     * @return 添加结果
     */
    @PostMapping("/save")
    @Operation(summary = "添加同步配置")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = {"video:sync:config:list", "video:sync:config:detail"}, allEntries = true)
    public ReturnResult<Boolean> addConfig(@RequestBody VideoSyncConfig config) {
        boolean result = videoSyncConfigService.addConfig(config);
        return result ? ReturnResult.success(true) : ReturnResult.error("添加失败");
    }

    /**
     * 修改同步配置
     *
     * @param config 同步配置
     * @return 修改结果
     */
    @PutMapping("/update")
    @Operation(summary = "修改同步配置")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = {"video:sync:config:list", "video:sync:config:detail"}, allEntries = true)
    public ReturnResult<Boolean> updateConfig(@RequestBody VideoSyncConfig config) {
        if (config.getVideoSyncConfigId() == null) {
            return ReturnResult.error("配置ID不能为空");
        }

        boolean result = videoSyncConfigService.updateConfig(config);
        return result ? ReturnResult.success(true) : ReturnResult.error("修改失败");
    }

    /**
     * 删除同步配置
     *
     * @param id 配置ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除同步配置")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = {"video:sync:config:list", "video:sync:config:detail"}, allEntries = true)
    public ReturnResult<Boolean> deleteConfig(@PathVariable String id) {
        boolean result = videoSyncConfigService.deleteConfig(id);
        return result ? ReturnResult.success(true) : ReturnResult.error("删除失败");
    }

    /**
     * 启用/禁用
     *
     * @param id 配置ID
     * @return 删除结果
     */
    @PutMapping("/toggle-status/{id}")
    @Operation(summary = "启用/禁用")
    public ReturnResult<Boolean> toggleStatus(@PathVariable String id) {
        return videoSyncConfigService.toggleStatus(id);
    }
    /**
     * 同步
     *
     * @param id 配置ID
     * @return 删除结果
     */
    @PostMapping("/execute/{id}")
    @Operation(summary = "同步")
    public ReturnResult<Boolean> syncConfig(@PathVariable String id) {
        return videoSyncConfigService.syncConfig(id, true);
    }

    /**
     * 停止同步
     *
     * @param id 配置ID
     * @return 删除结果
     */
    @PostMapping("/stop/{id}")
    @Operation(summary = "停止同步")
    public ReturnResult<Boolean> stopSync(@PathVariable String id) {
        return videoSyncConfigService.stopSync(id, true);
    }

    /**
     * 获取同步状态
     *
     * @param id 配置ID
     * @return 删除结果
     */
    @GetMapping("/status/{id}")
    @Operation(summary = "获取同步状态")
    public ReturnResult<String> getStatus(@PathVariable String id) {
        return videoSyncConfigService.getSyncStatus(id);
    }
}