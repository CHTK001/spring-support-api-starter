package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 批量操作结果DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BatchOperationResultDTO", description = "批量操作结果DTO")
@Schema(description = "批量操作结果DTO")
public class BatchOperationResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型
     */
    @ApiModelProperty(value = "操作类型")
    @Schema(description = "操作类型")
    private String action;

    /**
     * 总数
     */
    @ApiModelProperty(value = "总数")
    @Schema(description = "总数")
    private Integer total;

    /**
     * 成功数
     */
    @ApiModelProperty(value = "成功数")
    @Schema(description = "成功数")
    private Integer success;

    /**
     * 失败数
     */
    @ApiModelProperty(value = "失败数")
    @Schema(description = "失败数")
    private Integer failed;

    /**
     * 成功的ID列表
     */
    @ApiModelProperty(value = "成功的ID列表")
    @Schema(description = "成功的ID列表")
    private List<String> successIds;

    /**
     * 失败的ID列表
     */
    @ApiModelProperty(value = "失败的ID列表")
    @Schema(description = "失败的ID列表")
    private List<String> failedIds;

    /**
     * 获取成功率
     */
    public Double getSuccessRate() {
        if (total == null || total == 0 || success == null) {
            return 0.0;
        }
        return (double) success / total * 100;
    }

    /**
     * 获取失败率
     */
    public Double getFailureRate() {
        if (total == null || total == 0 || failed == null) {
            return 0.0;
        }
        return (double) failed / total * 100;
    }

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return total != null && success != null && total.equals(success);
    }

    /**
     * 是否全部失败
     */
    public boolean isAllFailed() {
        return total != null && failed != null && total.equals(failed);
    }
}
