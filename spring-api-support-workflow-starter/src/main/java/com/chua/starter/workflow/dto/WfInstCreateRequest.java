package com.chua.starter.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 创建流程实例请求
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
@Schema(name = "创建流程实例请求")
public class WfInstCreateRequest {

    @NotNull(message = "流程定义ID不能为空")
    @Schema(description = "流程定义ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wfDefId;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务ID")
    private String businessId;

    @NotBlank(message = "流程标题不能为空")
    @Schema(description = "流程标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String instTitle;

    @Schema(description = "表单数据(动态JSON)")
    private Map<String, Object> formData;

    @Schema(description = "幂等键(防重复提交,业务方生成)")
    private String idempotentKey;
}
