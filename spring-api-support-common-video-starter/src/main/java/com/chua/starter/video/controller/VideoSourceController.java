package com.chua.starter.video.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.service.VideoSourceService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 * 视频同步配置接口
 *
 * @author CH
 * @since 2024/6/21
 */
@RestController
@RequestMapping("v1/video/source")
@Tag(name = "视频来源配置接口")
@Api(tags = "视频来源配置接口")
@RequiredArgsConstructor
@Slf4j
public class VideoSourceController {

    private final VideoSourceService videoSourceService;

    /**
     * 获取所有同步配置
     *
     * @return 同步配置列表
     */
    @GetMapping("/page")
    @Operation(summary = "获取所有来源配置")
    public ReturnPageResult<VideoSource> getAllConfigs(Query<VideoSource> query) {
        return ReturnPageResultUtils.ok(videoSourceService.page(query.createFullPage()));
    }


    /**
     * 添加同步配置
     *
     * @param config 同步配置
     * @return 添加结果
     */
    @PostMapping("/save")
    @Operation(summary = "添加视频源配置")
    public ReturnResult<Boolean> addConfig(@RequestBody VideoSource config) {
        return ReturnResult.of(videoSourceService.save(config));
    }

    /**
     * 修改同步配置
     *
     * @param config 同步配置
     * @return 修改结果
     */
    @PutMapping("/update")
    @Operation(summary = "修改视频源配置")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = {"video:sync:config:list", "video:sync:config:detail"}, allEntries = true)
    public ReturnResult<Boolean> updateConfig(@RequestBody VideoSource config) {
        return ReturnResult.of(videoSourceService.updateById(config));
    }

    /**
     * 删除同步配置
     *
     * @param id 配置ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除视频源配置")
    public ReturnResult<Boolean> deleteConfig(@PathVariable String id) {
        return ReturnResult.of(videoSourceService.removeById(id));
    }

}