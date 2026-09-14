package com.chua.starter.video.controller;

import com.chua.common.support.datasearch.network.lang.code.ReturnPageResult;
import com.chua.common.support.datasearch.video.model.VideoInfoResult;
import com.chua.common.support.datasearch.video.model.VideoSearch;
import com.chua.common.support.datasearch.video.spi.VideoSearchService;
import com.chua.common.support.lang.code.ReturnResult;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
* 视频聚合检索接口
*
* <p>把 datasearch-starter 的 {@link VideoSearchService}（ResourceProvider SPI 聚合）
* 暴露为 HTTP 接口，供 vue-support-video-starter 前端调用。</p>
*
* @author CH
* @since 4.0.0.42
 */
@Slf4j
@RestController
@RequestMapping("video/api/video")
@Tag(name = "视频资源检索")
@Api(tags = "视频资源检索")
public class VideoSearchController {

    /** 默认分页大小 */
    private static final int DEFAULT_PAGE_SIZE = 24;
    /** 最大分页大小 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 视频聚合检索服务（遍历全部未封禁的 ResourceProvider） */
    private final VideoSearchService videoSearchService = new VideoSearchService();

    /**
    * 分页检索视频列表（关键词/类型筛选，多源聚合）。
    *
    * @param keyword 关键词（可空，空则返回各源首页内容）
    * @param videoType 类型（movie/tv/anime/variety/documentary，可空）
    * @param page 页码（从 1 开始）
    * @param pageSize 每页数量
    * @return 视频列表分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "分页检索视频列表")
    public ReturnResult<List<VideoInfoResult>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "videoType", required = false) String videoType,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "24") int pageSize) {

        VideoSearch search = new VideoSearch();
        search.setKeyword(keyword);
        search.setCategory(videoType);
        search.setPage(Math.max(page, 1));
        search.setPageSize(Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE));

        ReturnPageResult<VideoInfoResult> result = videoSearchService.search(search);
        if (result.getData() == null || result.getData().getData() == null) {
            return ReturnResult.ok(List.of());
        }
        return ReturnResult.ok(result.getData().getData());
    }

    /**
    * 视频详情（按聚合结果中的 originId 匹配）。
    *
    * <p>聚合检索本身不提供单条详情接口，这里以 originId 为关键词
    * 重新检索并匹配对应条目。</p>
    *
    * @param id 视频 ID（各数据源的 videoOriginId）
    * @return 视频详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "视频详情")
    public ReturnResult<VideoInfoResult> detail(@PathVariable("id") String id) {
        if (id == null || id.isBlank()) {
            return ReturnResult.error("视频 ID 不能为空");
        }
        VideoSearch search = new VideoSearch();
        search.setKeyword(id);
        search.setPage(1);
        search.setPageSize(DEFAULT_PAGE_SIZE);

        ReturnPageResult<VideoInfoResult> result = videoSearchService.search(search);
        if (result.getData() == null || result.getData().getData() == null) {
            return ReturnResult.error("未找到视频: " + id);
        }
        List<VideoInfoResult> matches = result.getData().getData().stream()
                .filter(v -> id.equals(String.valueOf(v.getVideoOriginId()))
                        || id.equals(String.valueOf(v.getVideoId())))
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            return ReturnResult.error("未找到视频: " + id);
        }
        return ReturnResult.ok(matches.get(0));
    }
}
