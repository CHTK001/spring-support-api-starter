package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬虫节点实体
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
@TableName("monitor_spider_node")
@ApiModel(description = "爬虫节点实体")
public class MonitorSpiderNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("节点ID")
    private Integer nodeId;

    /**
     * 任务ID
     */
    @ApiModelProperty("任务ID")
    private Integer spiderTaskId;

    /**
     * 节点唯一标识
     */
    @ApiModelProperty("节点唯一标识")
    private String nodeKey;

    /**
     * 节点名称
     */
    @ApiModelProperty("节点名称")
    private String nodeName;

    /**
     * 节点类型: URL_SOURCE, DOWNLOADER, PROCESSOR, MIDDLEWARE, DATA_FILTER, PIPELINE, SCHEDULER, SITE_CONFIG, PROXY_POOL, UA_POOL
     */
    @ApiModelProperty("节点类型")
    private String nodeType;

    /**
     * SPI组件名称
     */
    @ApiModelProperty("SPI组件名称")
    private String spiName;

    /**
     * 节点配置(JSON格式)
     */
    @ApiModelProperty("节点配置")
    private String nodeConfig;

    /**
     * 节点描述
     */
    @ApiModelProperty("节点描述")
    private String nodeDescription;

    /**
     * X坐标
     */
    @ApiModelProperty("X坐标")
    private Integer positionX;

    /**
     * Y坐标
     */
    @ApiModelProperty("Y坐标")
    private Integer positionY;

    /**
     * 节点顺序
     */
    @ApiModelProperty("节点顺序")
    private Integer nodeOrder;

    /**
     * 是否启用
     */
    @ApiModelProperty("是否启用")
    private Boolean nodeEnabled;

    /**
     * 是否需要配置
     */
    @ApiModelProperty("是否需要配置")
    private Boolean requiresConfig;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
