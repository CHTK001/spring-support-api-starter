package com.chua.starter.monitor.pojo.acme;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 证书统计信息
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-06
 */
@Data
@ApiModel(description = "证书统计信息")
@Schema(description = "证书统计信息")
public class CertStats {

    /**
     * 账户总数
     */
    @ApiModelProperty("账户总数")
    @Schema(description = "账户总数")
    private Long accountCount;

    /**
     * 证书总数
     */
    @ApiModelProperty("证书总数")
    @Schema(description = "证书总数")
    private Long certCount;

    /**
     * 有效证书数
     */
    @ApiModelProperty("有效证书数")
    @Schema(description = "有效证书数")
    private Long validCount;

    /**
     * 待验证证书数
     */
    @ApiModelProperty("待验证证书数")
    @Schema(description = "待验证证书数")
    private Long pendingCount;

    /**
     * 即将到期证书数（30天内）
     */
    @ApiModelProperty("即将到期证书数")
    @Schema(description = "即将到期证书数")
    private Long expiringCount;

    /**
     * 已过期证书数
     */
    @ApiModelProperty("已过期证书数")
    @Schema(description = "已过期证书数")
    private Long expiredCount;

    /**
     * 已吊销证书数
     */
    @ApiModelProperty("已吊销证书数")
    @Schema(description = "已吊销证书数")
    private Long revokedCount;

    /**
     * 失败证书数
     */
    @ApiModelProperty("失败证书数")
    @Schema(description = "失败证书数")
    private Long failedCount;
}
