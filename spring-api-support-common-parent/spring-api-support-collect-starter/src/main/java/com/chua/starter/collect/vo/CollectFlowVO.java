package com.chua.starter.collect.vo;

import com.chua.starter.collect.dto.CollectDslDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程定义出参。
 *
 * <p>列表接口不填充 {@code dsl}，详情与设计器接口才填充，避免分页响应里塞满流程图。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "数据采集-流程出参")
public class CollectFlowVO {

    /**
     * 主键ID。
     */
    @Schema(description = "主键ID")
    private Integer collectFlowId;

    /**
     * 流程名称。
     */
    @Schema(description = "流程名称")
    private String collectFlowName;

    /**
     * 所属分类ID。
     */
    @Schema(description = "所属分类ID")
    private Integer collectFlowCategoryId;

    /**
     * 所属分类名称。
     */
    @Schema(description = "所属分类名称")
    private String collectFlowCategoryName;

    /**
     * 流程编码。
     */
    @Schema(description = "流程编码")
    private String collectFlowCode;

    /**
     * 定时表达式。
     */
    @Schema(description = "定时表达式")
    private String collectFlowCron;

    /**
     * 调度状态，0 停用 1 启用。
     */
    @Schema(description = "调度状态 0停用 1启用")
    private Integer collectFlowStatus;

    /**
     * 同一流程允许并发运行的任务数。
     */
    @Schema(description = "允许并发任务数")
    private Integer collectFlowConcurrency;

    /**
     * 流程内并行线程数。
     */
    @Schema(description = "流程内并行线程数")
    private Integer collectFlowThreadCount;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String collectFlowRemark;

    /**
     * 最近一次开始执行时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "最近执行时间")
    private LocalDateTime collectFlowLastExecTime;

    /**
     * 下次触发时间，取自调度台账，未登记时为空。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "下次触发时间")
    private LocalDateTime collectFlowNextExecTime;

    /**
     * 累计执行次数。
     */
    @Schema(description = "累计执行次数")
    private Integer collectFlowExecCount;

    /**
     * 节点数，来自 DSL 摘要。
     */
    @Schema(description = "节点数")
    private Integer nodeCount;

    /**
     * 连线数，来自 DSL 摘要。
     */
    @Schema(description = "连线数")
    private Integer edgeCount;

    /**
     * 本机是否有该流程的运行中任务。
     */
    @Schema(description = "是否运行中")
    private Boolean running;

    /**
     * 调度台账里已登记该流程，且任务处理器可用。
     */
    @Schema(description = "调度已登记")
    private Boolean scheduled;

    /**
     * 流程图 DSL，仅详情接口返回。
     */
    @Schema(description = "流程图DSL")
    private CollectDslDTO dsl;

    /**
     * 创建人姓名。
     */
    @Schema(description = "创建人姓名")
    private String createName;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
