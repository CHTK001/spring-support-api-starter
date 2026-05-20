package com.chua.starter.video.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.video.model.VideoDownload;
import com.chua.starter.video.entity.VideoKeyword;
import com.chua.common.support.video.model.VideoSource;
import com.chua.common.support.video.model.VideoSyncConfig;
import com.chua.starter.video.mapper.VideoDownloadMapper;
import com.chua.starter.video.mapper.VideoInfoMapper;
import com.chua.starter.video.mapper.VideoKeywordMapper;
import com.chua.starter.video.mapper.VideoSourceMapper;
import com.chua.starter.video.mapper.VideoSyncConfigMapper;
import com.chua.starter.video.pojo.response.VideoReportStatistics;
import com.chua.starter.video.pojo.response.VideoReportStatistics.NameCount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视频报告控制器
 *
 * @author CH
 * @since 2025/9/16
 */
@RestController
@RequestMapping("v1/video/report")
@Tag(name = "视频报告接口")
@RequiredArgsConstructor
public class VideoReportController {

    private final VideoInfoMapper videoInfoMapper;
    private final VideoDownloadMapper videoDownloadMapper;
    private final VideoKeywordMapper videoKeywordMapper;
    private final VideoSourceMapper videoSourceMapper;
    private final VideoSyncConfigMapper videoSyncConfigMapper;

    /**
     * 获取视频统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取视频统计数据")
    public ReturnResult<VideoReportStatistics> getStatistics() {
        VideoReportStatistics stats = new VideoReportStatistics();

        // 基础计数
        stats.setTotalVideos(videoInfoMapper.selectCount(Wrappers.emptyWrapper()));
        stats.setTotalDownloads(videoDownloadMapper.selectCount(Wrappers.emptyWrapper()));
        stats.setTotalKeywords(videoKeywordMapper.selectCount(Wrappers.emptyWrapper()));
        stats.setTotalSources(videoSourceMapper.selectCount(Wrappers.emptyWrapper()));
        stats.setTotalSyncConfigs(videoSyncConfigMapper.selectCount(Wrappers.emptyWrapper()));

        // 分类分布
        List<Map<String, Object>> categoryData = videoInfoMapper.countByCategory();
        stats.setCategoryDistribution(categoryData.stream()
                .map(m -> new NameCount(String.valueOf(m.get("name")), ((Number) m.get("count")).longValue()))
                .collect(Collectors.toList()));

        // 年份分布
        List<Map<String, Object>> yearData = videoInfoMapper.countByYear();
        stats.setYearDistribution(yearData.stream()
                .map(m -> new NameCount(String.valueOf(m.get("name")), ((Number) m.get("count")).longValue()))
                .collect(Collectors.toList()));

        // 地区分布
        List<Map<String, Object>> districtData = videoInfoMapper.countByDistrict();
        stats.setDistrictDistribution(districtData.stream()
                .map(m -> new NameCount(String.valueOf(m.get("name")), ((Number) m.get("count")).longValue()))
                .collect(Collectors.toList()));

        // 类型分布
        List<Map<String, Object>> typeData = videoInfoMapper.countByType();
        stats.setTypeDistribution(typeData.stream()
                .map(m -> new NameCount(String.valueOf(m.get("name")), ((Number) m.get("count")).longValue()))
                .collect(Collectors.toList()));

        // 最近7天新增
        List<Map<String, Object>> recentData = videoInfoMapper.countRecentDaily();
        stats.setRecentDailyAdded(recentData.stream()
                .map(m -> new NameCount(String.valueOf(m.get("name")), ((Number) m.get("count")).longValue()))
                .collect(Collectors.toList()));

        // 播放量前十
        stats.setTopViewed(videoInfoMapper.selectTopViewed());

        // 评分前十
        stats.setTopRated(videoInfoMapper.selectTopRated());

        return ReturnResult.success(stats);
    }
}
