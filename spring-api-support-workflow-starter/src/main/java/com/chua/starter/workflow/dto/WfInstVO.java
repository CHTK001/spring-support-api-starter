package com.chua.starter.workflow.dto;

import com.chua.starter.workflow.entity.WfInst;
import com.chua.starter.workflow.entity.WfTask;
import com.chua.starter.workflow.entity.WfTaskHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 流程实例视图(含任务和历史)
 *
 * @author CH
 * @since 2026/5/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "流程实例视图")
public class WfInstVO extends WfInst {

    @Schema(description = "流程定义名称")
    private String wfDefName;

    @Schema(description = "当前待办任务")
    private List<WfTask> currentTasks;

    @Schema(description = "所有任务")
    private List<WfTask> tasks;

    @Schema(description = "操作历史")
    private List<WfTaskHistory> histories;
}
