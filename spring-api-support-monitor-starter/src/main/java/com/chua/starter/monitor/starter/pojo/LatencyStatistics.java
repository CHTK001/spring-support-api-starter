package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 延迟统计信息实体类
 *
 * @author CH
 * @since 2024/12/25
 */
@ApiModel(description = "延迟统计信息")
@Schema(description = "延迟统计信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatencyStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务器ID
     */
    @ApiModelProperty(value = "服务器ID")
    @Schema(description = "服务器ID")
    private Integer serverId;

    /**
     * 服务器名称
     */
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    private String serverName;

    /**
     * 当前延迟
     */
    @ApiModelProperty(value = "当前延迟")
    @Schema(description = "当前延迟")
    private Long currentLatency;

    /**
     * 平均延迟
     */
    @ApiModelProperty(value = "平均延迟")
    @Schema(description = "平均延迟")
    private Long avgLatency;

    /**
     * 最大延迟
     */
    @ApiModelProperty(value = "最大延迟")
    @Schema(description = "最大延迟")
    private Long maxLatency;

    /**
     * 最小延迟
     */
    @ApiModelProperty(value = "最小延迟")
    @Schema(description = "最小延迟")
    private Long minLatency;

    /**
     * 延迟状态
     */
    @ApiModelProperty(value = "延迟状态")
    @Schema(description = "延迟状态")
    private Integer latencyStatus;

    /**
     * 延迟状态描述
     */
    @ApiModelProperty(value = "延迟状态描述")
    @Schema(description = "延迟状态描述")
    private String latencyStatusDesc;

    /**
     * 总检测次数
     */
    @ApiModelProperty(value = "总检测次数")
    @Schema(description = "总检测次数")
    private Long totalCount;

    /**
     * 失败次数
     */
    @ApiModelProperty(value = "失败次数")
    @Schema(description = "失败次数")
    private Long failCount;

    /**
     * 成功次数
     */
    @ApiModelProperty(value = "成功次数")
    @Schema(description = "成功次数")
    private Long successCount;

    /**
     * 成功率
     */
    @ApiModelProperty(value = "成功率")
    @Schema(description = "成功率")
    private Double successRate;

    /**
     * 最后检测时间
     */
    @ApiModelProperty(value = "最后检测时间")
    @Schema(description = "最后检测时间")
    private LocalDateTime lastCheckTime;

    /**
     * 统计时间
     */
    @ApiModelProperty(value = "统计时间")
    @Schema(description = "统计时间")
    private LocalDateTime statisticsTime;
}
