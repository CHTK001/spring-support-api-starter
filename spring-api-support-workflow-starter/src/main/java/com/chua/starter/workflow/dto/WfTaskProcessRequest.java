package com.chua.starter.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 处理任务请求
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@Schema(name = "处理任务请求")
public class WfTaskProcessRequest {

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wfTaskId;

    @Schema(description = "审批意见")
    private String comment;

    @Schema(description = "表单数据(审批时可更新实例表单)")
    private Map<String, Object> formData;

    @Schema(description = "转交目标用户ID(仅转交操作时)")
    private Integer transferUserId;

    @Schema(description = "转交目标用户姓名(仅转交操作时)")
    private String transferUserName;
}
