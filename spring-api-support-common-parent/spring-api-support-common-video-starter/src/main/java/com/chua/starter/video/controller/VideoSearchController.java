package com.chua.starter.video.controller;

import com.chua.common.support.datasearch.video.model.VideoInfoResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.service.VideoPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 视频资源接口。
 *
 * <p>面向前端 {@code vue-support-video-starter} 提供视频资源中心所需的
 * 列表检索、分类浏览、详情与数据源查询能力，统一返回
 * {@link ReturnResult} 信封。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@RestController
@RequestMapping("/video/api")
@Tag(name = "Video Resource", description = "Video aggregation search and playback API")
@RequiredArgsConstructor
public class VideoSearchController {

    /**
     * 视频平台业务服务。
     */
    private final VideoPlatformService videoPlatformService;

    /**
     * 视频列表：携带关键词时多源聚合检索，否则按分类浏览最新资源。
     *
     * @param keyword   关键词，可空
     * @param videoType 分类码（movie/tv/anime/variety/documentary），可空
     * @param year      年份，可空
     * @param district  地区，可空
     * @param tag       标签，可空
     * @param page      页码，默认 1
     * @param pageSize  每页条数，默认 24
     * @return 视频条目列表
     */
    @Operation(summary = "List videos by keyword or category")
    @GetMapping("/video/list")
    public ReturnResult<List<VideoInfoResult>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String videoType,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "24") int pageSize) {
        return ReturnResult.ok(videoPlatformService.list(
                keyword, videoType, year, district, tag, page, pageSize));
    }

    /**
     * 已注册的视频数据源名称列表。
     *
     * @return 数据源名称
     */
    @Operation(summary = "List registered video sources")
    @GetMapping("/video/sources")
    public ReturnResult<List<String>> sources() {
        return ReturnResult.ok(videoPlatformService.sources());
    }

    /**
     * 视频详情（含播放地址）。
     *
     * @param id 视频 originId
     * @return 视频详情
     */
    @Operation(summary = "Get video detail by originId")
    @GetMapping("/video/{id}")
    public ReturnResult<VideoInfoResult> detail(@PathVariable String id) {
        VideoInfoResult detail = videoPlatformService.detail(id);
        if (detail == null) {
            return ReturnResult.error("视频不存在或已下线: " + id);
        }
        return ReturnResult.ok(detail);
    }
}
