package com.chua.starter.monitor.pojo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量操作请求
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
@Data
@ApiModel(description = "批量操作请求")
@Schema(description = "批量操作请求")
public class BatchOperateRequest {

    /**
     * 容器ID列表
     */
    @NotEmpty(message = "容器ID列表不能为空")
    @ApiModelProperty(value = "容器ID列表", required = true, example = "[1, 2, 3]")
    @Schema(description = "容器ID列表", example = "[1, 2, 3]", required = true)
    private List<Integer> containerIds;

    /**
     * 操作类型
     */
    @NotEmpty(message = "操作类型不能为空")
    @ApiModelProperty(value = "操作类型", required = true, example = "start", allowableValues = "start,stop,restart,remove")
    @Schema(description = "操作类型：start-启动，stop-停止，restart-重启，remove-删除", example = "start", required = true, allowableValues = {"start", "stop", "restart", "remove"})
    private String operation;
}