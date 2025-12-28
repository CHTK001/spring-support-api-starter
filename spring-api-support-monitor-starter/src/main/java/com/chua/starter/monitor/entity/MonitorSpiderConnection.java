package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬虫节点连接实体
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
@TableName("monitor_spider_connection")
@ApiModel(description = "爬虫节点连接实体")
public class MonitorSpiderConnection implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 连接ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("连接ID")
    private Integer connectionId;

    /**
     * 任务ID
     */
    @ApiModelProperty("任务ID")
    private Integer spiderTaskId;

    /**
     * 源节点ID
     */
    @ApiModelProperty("源节点ID")
    private Integer sourceNodeId;

    /**
     * 目标节点ID
     */
    @ApiModelProperty("目标节点ID")
    private Integer targetNodeId;

    /**
     * 源端口
     */
    @ApiModelProperty("源端口")
    private String sourcePort;

    /**
     * 目标端口
     */
    @ApiModelProperty("目标端口")
    private String targetPort;

    /**
     * 连接类型
     */
    @ApiModelProperty("连接类型")
    private String connectionType;

    /**
     * 连接标签
     */
    @ApiModelProperty("连接标签")
    private String connectionLabel;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
