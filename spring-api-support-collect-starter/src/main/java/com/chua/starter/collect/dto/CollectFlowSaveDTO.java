package com.chua.starter.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流程保存入参。
 *
 * <p>设计器一次提交完整图：流程基本信息 + DSL 对象。服务层校验 DSL 后才落库，
 * 因此不存在"存了张跑不通的图"的状态。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "流程保存入参")
public class CollectFlowSaveDTO {

    /**
     * 主键ID，新增留空。
     */
    @Schema(description = "主键ID，新增留空")
    private Integer collectFlowId;

    /**
     * 流程名称。
     */
    @Schema(description = "流程名称")
    private String collectFlowName;

    /**
     * 所属分类ID，留空归入未分类 0。
     */
    @Schema(description = "所属分类ID")
    private Integer collectFlowCategoryId;

    /**
     * 流程编码，留空由服务层生成。
     */
    @Schema(description = "流程编码，留空自动生成")
    private String collectFlowCode;

    /**
     * 定时表达式，留空表示只允许手动运行。
     */
    @Schema(description = "定时表达式 6字段")
    private String collectFlowCron;

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
     * 流程图 DSL。
     */
    @Schema(description = "流程图DSL")
    private CollectDslDTO dsl;
}
