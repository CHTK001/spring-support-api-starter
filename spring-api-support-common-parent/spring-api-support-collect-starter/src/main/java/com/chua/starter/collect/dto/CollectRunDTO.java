package com.chua.starter.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程运行入参。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "流程运行入参")
public class CollectRunDTO {

    /**
     * 流程ID。
     */
    @Schema(description = "流程ID")
    private Integer collectFlowId;

    /**
     * 本次运行的变量覆盖，同名覆盖流程全局变量。
     */
    @Schema(description = "变量覆盖")
    private List<CollectParamDTO> variables = new ArrayList<>();

    /**
     * 是否把采集结果写入结果表，调试草稿时传 false。
     */
    @Schema(description = "结果是否落库")
    private Boolean persist;
}
