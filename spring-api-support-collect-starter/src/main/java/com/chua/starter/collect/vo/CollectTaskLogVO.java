package com.chua.starter.collect.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节点执行日志出参。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "数据采集-节点日志出参")
public class CollectTaskLogVO {

    /**
     * 主键ID。
     */
    @Schema(description = "主键ID")
    private Long collectTaskLogId;

    /**
     * 任务ID。
     */
    @Schema(description = "任务ID")
    private Long collectTaskLogTaskId;

    /**
     * 该节点出自哪条流程，子流程节点日志与宿主任务同属一条任务但流程不同。
     */
    @Schema(description = "归属流程ID")
    private Integer collectTaskLogFlowId;

    /**
     * 节点ID。
     */
    @Schema(description = "节点ID")
    private String collectTaskLogNodeId;

    /**
     * 节点名称。
     */
    @Schema(description = "节点名称")
    private String collectTaskLogNodeName;

    /**
     * 节点类型。
     */
    @Schema(description = "节点类型")
    private String collectTaskLogNodeShape;

    /**
     * 循环轮次。
     */
    @Schema(description = "循环轮次")
    private Integer collectTaskLogIteration;

    /**
     * 结果 SUCCESS/FAILURE/SKIPPED。
     */
    @Schema(description = "结果")
    private String collectTaskLogStatus;

    /**
     * 耗时毫秒。
     */
    @Schema(description = "耗时毫秒")
    private Long collectTaskLogCost;

    /**
     * 摘要或异常信息。
     */
    @Schema(description = "摘要或异常信息")
    private String collectTaskLogMessage;

    /**
     * 出参预览。
     */
    @Schema(description = "出参预览")
    private String collectTaskLogOutput;

    /**
     * 记录时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "记录时间")
    private LocalDateTime createTime;
}
