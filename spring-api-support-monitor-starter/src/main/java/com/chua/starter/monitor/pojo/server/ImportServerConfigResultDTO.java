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
 * 导入服务器配置结果DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ImportServerConfigResultDTO", description = "导入服务器配置结果DTO")
@Schema(description = "导入服务器配置结果DTO")
public class ImportServerConfigResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 错误信息列表
     */
    @ApiModelProperty(value = "错误信息列表")
    @Schema(description = "错误信息列表")
    private List<String> errors;

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
