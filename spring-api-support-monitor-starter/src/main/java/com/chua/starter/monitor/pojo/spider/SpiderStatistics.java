package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

/**
 * 爬虫统计信息
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class SpiderStatistics {
    private Integer totalCount;
    private Integer runningCount;
    private Integer enabledCount;
    private Integer todayDataCount;
    private Integer totalDataCount;
}
