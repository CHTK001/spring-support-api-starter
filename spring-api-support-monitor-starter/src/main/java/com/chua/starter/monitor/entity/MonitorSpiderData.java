package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬虫数据实体
 *
 * @author CH
 * @since 2024/12/25
 */
@Data
@TableName("monitor_spider_data")
@ApiModel(description = "爬虫数据实体")
public class MonitorSpiderData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("数据ID")
    private Long spiderDataId;

    /**
     * 任务ID
     */
    @ApiModelProperty("任务ID")
    private Long spiderTaskId;

    /**
     * 爬取的数据内容(JSON格式)
     */
    @ApiModelProperty("爬取的数据内容")
    private String spiderDataContent;

    /**
     * 数据来源URL
     */
    @ApiModelProperty("数据来源URL")
    private String spiderDataUrl;

    /**
     * 数据哈希(用于去重)
     */
    @ApiModelProperty("数据哈希")
    private String spiderDataHash;

    /**
     * 爬取时间
     */
    @ApiModelProperty("爬取时间")
    private LocalDateTime crawlTime;
}
