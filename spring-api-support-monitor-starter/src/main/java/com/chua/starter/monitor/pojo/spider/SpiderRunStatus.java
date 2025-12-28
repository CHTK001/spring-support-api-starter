package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 爬虫运行状态
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class SpiderRunStatus {
    private Integer taskId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer progress;
    private String currentUrl;
    private Integer pageCount;
    private Integer successCount;
    private Integer failCount;
    private String errorMessage;
}
