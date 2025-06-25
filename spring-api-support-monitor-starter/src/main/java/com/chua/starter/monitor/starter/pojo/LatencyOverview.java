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
import java.util.Map;

/**
 * 延迟概览实体类
 *
 * @author CH
 * @since 2024/12/25
 */
@ApiModel(description = "延迟概览")
@Schema(description = "延迟概览")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatencyOverview implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 各状态服务器数量统计
     */
    @ApiModelProperty(value = "各状态服务器数量统计")
    @Schema(description = "各状态服务器数量统计")
    private Map<String, Long> statusCount;

    /**
     * 平均延迟
     */
    @ApiModelProperty(value = "平均延迟")
    @Schema(description = "平均延迟")
    private Double avgLatency;

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
     * 总服务器数
     */
    @ApiModelProperty(value = "总服务器数")
    @Schema(description = "总服务器数")
    private Integer totalServers;

    /**
     * 已检测服务器数
     */
    @ApiModelProperty(value = "已检测服务器数")
    @Schema(description = "已检测服务器数")
    private Long checkedServers;

    /**
     * 正常延迟服务器数
     */
    @ApiModelProperty(value = "正常延迟服务器数")
    @Schema(description = "正常延迟服务器数")
    private Long normalServers;

    /**
     * 高延迟服务器数
     */
    @ApiModelProperty(value = "高延迟服务器数")
    @Schema(description = "高延迟服务器数")
    private Long highLatencyServers;

    /**
     * 检测失败服务器数
     */
    @ApiModelProperty(value = "检测失败服务器数")
    @Schema(description = "检测失败服务器数")
    private Long failedServers;

    /**
     * 未检测服务器数
     */
    @ApiModelProperty(value = "未检测服务器数")
    @Schema(description = "未检测服务器数")
    private Long uncheckedServers;

    /**
     * 健康率
     */
    @ApiModelProperty(value = "健康率")
    @Schema(description = "健康率")
    private Double healthRate;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
