package com.chua.starter.video.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.pojo.request.VideoSearchRequest;
import com.chua.starter.video.service.VideoDownloadService;
import com.chua.starter.video.service.VideoInfoService;
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
 * 视频信息接口
 *
 * @author CH
 * @since 2024/6/21
 */
@RestController
@RequestMapping("v1/video")
@Tag(name = "视频信息接口")
@Api(tags = "视频信息接口")
@RequiredArgsConstructor
@Slf4j
public class VideoInfoController {

    private final VideoInfoService videoInfoService;
    private final VideoDownloadService videoDownloadService;

    /**
     * 视频搜索与分页查询
     *
     * @param request 搜索参数
     * @return 搜索结果
     */
    @PostMapping("/page")
    @Operation(summary = "视频搜索与分页查询")
    public ReturnPageResult<VideoInfo> searchVideos(@RequestBody VideoSearchRequest request) {
        return ReturnPageResultUtils.ok(videoInfoService.searchVideos(request));
    }

    /**
     * 获取视频详情
     *
     * @param id 视频ID
     * @return 视频详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取视频详情")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = "video:info:detail", key = "#id", unless = "#result.data == null")
    public ReturnResult<VideoInfo> getVideoById(@PathVariable String id) {
        return videoInfoService.getVideoById(id);
    }

    /**
     * 添加视频
     *
     * @param videoInfo 视频信息
     * @return 添加结果
     */
    @PostMapping("/save")
    @Operation(summary = "添加视频")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = {"video:info:list", "video:info:detail"}, allEntries = true)
    public ReturnResult<Boolean> addVideo(@RequestBody VideoInfo videoInfo) {
        boolean result = videoInfoService.addVideo(videoInfo);
        return result ? ReturnResult.success(true) : ReturnResult.error("添加失败");
    }

    /**
     * 修改视频
     *
     * @param videoInfo 视频信息
     * @return 修改结果
     */
    @PutMapping("/update")
    @Operation(summary = "修改视频")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = {"video:info:list", "video:info:detail"}, allEntries = true, key = "#videoInfo.videoId")
    public ReturnResult<Boolean> updateVideo(@RequestBody VideoInfo videoInfo) {
        if (videoInfo.getVideoId() == null) {
            return ReturnResult.error("视频ID不能为空");
        }

        boolean result = videoInfoService.updateVideo(videoInfo);
        return result ? ReturnResult.success(true) : ReturnResult.error("修改失败");
    }

    /**
     * 删除视频
     *
     * @param videoId 视频ID
     * @return 删除结果
     */
    @DeleteMapping("/{videoId}")
    @Operation(summary = "删除视频")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = {"video:info:list", "video:info:detail"}, allEntries = true, key = "#videoId")
    public ReturnResult<Boolean> deleteVideo(@PathVariable String videoId) {
        boolean result = videoInfoService.deleteVideo(videoId);
        return result ? ReturnResult.success(true) : ReturnResult.error("删除失败");
    }
}
