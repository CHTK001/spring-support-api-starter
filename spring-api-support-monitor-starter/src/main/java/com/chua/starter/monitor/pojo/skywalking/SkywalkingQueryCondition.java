package com.chua.starter.monitor.pojo.skywalking;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SkyWalking 查询条件
 *
 * @author CH
 * @since 2024-12-18
 */
@Data
@ApiModel(description = "SkyWalking 查询条件")
@Schema(description = "SkyWalking 查询条件")
public class SkywalkingQueryCondition {

    @ApiModelProperty("SkyWalking 配置ID")
    @Schema(description = "SkyWalking 配置ID")
    private Integer configId;

    @ApiModelProperty("服务ID")
    @Schema(description = "服务ID")
    private String serviceId;

    @ApiModelProperty("服务实例ID")
    @Schema(description = "服务实例ID")
    private String serviceInstanceId;

    @ApiModelProperty("端点ID")
    @Schema(description = "端点ID")
    private String endpointId;

    @ApiModelProperty("端点名称")
    @Schema(description = "端点名称")
    private String endpointName;

    @ApiModelProperty("Trace ID")
    @Schema(description = "Trace ID")
    private String traceId;

    @ApiModelProperty("开始时间 (格式: yyyy-MM-dd HHmm)")
    @Schema(description = "开始时间 (格式: yyyy-MM-dd HHmm)")
    private String startTime;

    @ApiModelProperty("结束时间 (格式: yyyy-MM-dd HHmm)")
    @Schema(description = "结束时间 (格式: yyyy-MM-dd HHmm)")
    private String endTime;

    @ApiModelProperty("步长 (DAY, HOUR, MINUTE)")
    @Schema(description = "步长 (DAY, HOUR, MINUTE)")
    private String step;

    @ApiModelProperty("最小持续时间(毫秒)")
    @Schema(description = "最小持续时间(毫秒)")
    private Integer minTraceDuration;

    @ApiModelProperty("最大持续时间(毫秒)")
    @Schema(description = "最大持续时间(毫秒)")
    private Integer maxTraceDuration;

    @ApiModelProperty("Trace 状态 (ALL, SUCCESS, ERROR)")
    @Schema(description = "Trace 状态 (ALL, SUCCESS, ERROR)")
    private String traceState;

    @ApiModelProperty("查询排序 (BY_START_TIME, BY_DURATION)")
    @Schema(description = "查询排序 (BY_START_TIME, BY_DURATION)")
    private String queryOrder;

    @ApiModelProperty("页码")
    @Schema(description = "页码")
    private Integer pageNum;

    @ApiModelProperty("每页大小")
    @Schema(description = "每页大小")
    private Integer pageSize;

    @ApiModelProperty("层 (GENERAL, MESH 等)")
    @Schema(description = "层 (GENERAL, MESH 等)")
    private String layer;

    @ApiModelProperty("关键词")
    @Schema(description = "关键词")
    private String keyword;

    @ApiModelProperty("限制数量")
    @Schema(description = "限制数量")
    private Integer limit;

    /**
     * 获取分页偏移量
     */
    public Integer getOffset() {
        if (pageNum == null || pageSize == null) {
            return 0;
        }
        return (pageNum - 1) * pageSize;
    }
}
