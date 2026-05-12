package com.chua.starter.video.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 视频报告统计数据
 *
 * @author CH
 * @since 2025/9/16
 */
@Data
@Schema(description = "视频报告统计数据")
public class VideoReportStatistics {

    @Schema(description = "总视频数")
    private long totalVideos;

    @Schema(description = "总下载数")
    private long totalDownloads;

    @Schema(description = "总关键词数")
    private long totalKeywords;

    @Schema(description = "视频源数")
    private long totalSources;

    @Schema(description = "同步配置数")
    private long totalSyncConfigs;

    @Schema(description = "按分类分布")
    private List<NameCount> categoryDistribution;

    @Schema(description = "按年份分布")
    private List<NameCount> yearDistribution;

    @Schema(description = "按地区分布")
    private List<NameCount> districtDistribution;

    @Schema(description = "按类型分布")
    private List<NameCount> typeDistribution;

    @Schema(description = "最近7天新增")
    private List<NameCount> recentDailyAdded;

    @Schema(description = "播放量前十")
    private List<Map<String, Object>> topViewed;

    @Schema(description = "评分前十")
    private List<Map<String, Object>> topRated;

    @Data
    @Schema(description = "名称-数量键值对")
    public static class NameCount {
        @Schema(description = "名称")
        private String name;
        @Schema(description = "数量")
        private long count;

        public NameCount() {}

        public NameCount(String name, long count) {
            this.name = name;
            this.count = count;
        }
    }
}
