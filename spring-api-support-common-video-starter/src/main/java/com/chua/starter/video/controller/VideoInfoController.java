package com.chua.starter.video.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.entity.VideoKeyword;
import com.chua.starter.video.entity.VideoPlayAddress;
import com.chua.starter.video.pojo.VideoInfoResponse;
import com.chua.common.support.video.model.VideoInfoResult;
import com.chua.common.support.video.model.VideoSearch;
import com.chua.starter.video.pojo.request.VideoSearchRequest;
import com.chua.starter.video.service.ResourceSearchManager;
import com.chua.starter.video.service.VideoDownloadService;
import com.chua.starter.video.service.VideoInfoService;
import com.chua.starter.video.service.VideoKeywordService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;
import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_HOUR;

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
    private final VideoKeywordService videoKeywordService;
    private final ResourceSearchManager resourceSearchManager;


    /**
     * 获取视频信息
     *
     * @param videoId 视频id
     * @return 视频信息
     */
    @Operation(summary = "获取视频播放地址")
    @GetMapping("/play/{videoId}")
    public ReturnResult<List<VideoPlayAddress>> playAddress(@PathVariable("videoId") Integer videoId) {
        return ReturnResult.success(videoInfoService.playAddress(videoId));
    }
    /**
     * 视频搜索与分页查询
     *
     * @param request 搜索参数
     * @return 搜索结果
     */
    @PostMapping("/page")
    @Operation(summary = "视频搜索与分页查询")
    public ReturnPageResult<VideoInfoResponse> searchVideos(@RequestBody VideoSearchRequest request) {
        return ReturnPageResultUtils.ok(videoInfoService.searchVideos(request));
    }

    /**
     * 全网搜索（同时搜索数据库 + 所有外部资源提供者）
     *
     * @param keyword  关键词
     * @param type     视频类型
     * @param category 分类
     * @param year     年份
     * @param page     页码
     * @return 搜索结果
     */
    @GetMapping("/search-all")
    @Operation(summary = "全网搜索（数据库 + 所有外部Provider）")
    public ReturnPageResult<VideoInfoResult> searchAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        VideoSearch videoSearch = new VideoSearch();
        videoSearch.setKeyword(keyword);
        videoSearch.setVideoType(type);
        videoSearch.setCategory(category);
        videoSearch.setYear(year);
        videoSearch.setPage(page);
        videoSearch.setPageSize(pageSize);
        return resourceSearchManager.searchFromAllResources(videoSearch);
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
    public ReturnResult<VideoInfoResult> getVideoById(@PathVariable String id) {
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

    /**
     * 获取热门搜索关键词
     *
     * @return 热门搜索关键词列表
     */
    @GetMapping("/hotKeywords")
    @Operation(summary = "获取热门搜索关键词")
    @Cacheable(cacheManager = REDIS_CACHE_HOUR, cacheNames = "video:keyword:hot", keyGenerator = "customTenantedKeyGenerator", unless = "#result.data == null")
    public ReturnResult<List<VideoKeyword>> getHotKeywords() {
        List<VideoKeyword> keywords = videoKeywordService.getHotKeywords(10);
        return ReturnResult.success(keywords);
    }
}
