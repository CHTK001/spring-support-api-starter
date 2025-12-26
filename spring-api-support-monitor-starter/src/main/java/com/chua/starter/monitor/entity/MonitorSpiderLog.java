package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬虫日志实体
 *
 * @author CH
 * @since 2024/12/25
 */
@Data
@TableName("monitor_spider_log")
@ApiModel(description = "爬虫日志实体")
public class MonitorSpiderLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("日志ID")
    private Long spiderLogId;

    /**
     * 任务ID
     */
    @ApiModelProperty("任务ID")
    private Long spiderTaskId;

    /**
     * 日志级别: INFO/WARN/ERROR
     */
    @ApiModelProperty("日志级别")
    private String spiderLogLevel;

    /**
     * 日志内容
     */
    @ApiModelProperty("日志内容")
    private String spiderLogMessage;

    /**
     * 相关URL
     */
    @ApiModelProperty("相关URL")
    private String spiderLogUrl;

    /**
     * 日志时间
     */
    @ApiModelProperty("日志时间")
    private LocalDateTime spiderLogTime;
}
