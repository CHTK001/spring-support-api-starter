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
import java.util.List;

/**
 * 批量延迟检测结果实体类
 *
 * @author CH
 * @since 2024/12/25
 */
@ApiModel(description = "批量延迟检测结果")
@Schema(description = "批量延迟检测结果")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatencyBatchDetectionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总检测数量
     */
    @ApiModelProperty(value = "总检测数量")
    @Schema(description = "总检测数量")
    private Integer totalCount;

    /**
     * 成功数量
     */
    @ApiModelProperty(value = "成功数量")
    @Schema(description = "成功数量")
    private Integer successCount;

    /**
     * 失败数量
     */
    @ApiModelProperty(value = "失败数量")
    @Schema(description = "失败数量")
    private Integer failCount;

    /**
     * 成功率
     */
    @ApiModelProperty(value = "成功率")
    @Schema(description = "成功率")
    private Double successRate;

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
     * 检测时间
     */
    @ApiModelProperty(value = "检测时间")
    @Schema(description = "检测时间")
    private LocalDateTime detectTime;

    /**
     * 检测耗时(毫秒)
     */
    @ApiModelProperty(value = "检测耗时(毫秒)")
    @Schema(description = "检测耗时(毫秒)")
    private Long duration;

    /**
     * 详细检测结果列表
     */
    @ApiModelProperty(value = "详细检测结果列表")
    @Schema(description = "详细检测结果列表")
    private List<LatencyDetectionResult> results;

    /**
     * 创建批量检测结果
     */
    public static LatencyBatchDetectionResult create(List<LatencyDetectionResult> results, long duration) {
        int totalCount = results.size();
        int successCount = (int) results.stream().mapToLong(r -> r.getSuccess() ? 1 : 0).sum();
        int failCount = totalCount - successCount;
        
        double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0;
        
        // 计算延迟统计
        List<Long> successLatencies = results.stream()
                .filter(r -> r.getSuccess() && r.getLatency() != null)
                .map(LatencyDetectionResult::getLatency)
                .toList();
        
        Double avgLatency = null;
        Long maxLatency = null;
        Long minLatency = null;
        
        if (!successLatencies.isEmpty()) {
            avgLatency = successLatencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
            maxLatency = successLatencies.stream().mapToLong(Long::longValue).max().orElse(0L);
            minLatency = successLatencies.stream().mapToLong(Long::longValue).min().orElse(0L);
        }
        
        return LatencyBatchDetectionResult.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failCount)
                .successRate(successRate)
                .avgLatency(avgLatency)
                .maxLatency(maxLatency)
                .minLatency(minLatency)
                .detectTime(LocalDateTime.now())
                .duration(duration)
                .results(results)
                .build();
    }
}
