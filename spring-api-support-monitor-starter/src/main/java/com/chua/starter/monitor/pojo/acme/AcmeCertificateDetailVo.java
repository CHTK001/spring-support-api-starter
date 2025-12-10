package com.chua.starter.monitor.pojo.acme;

import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import com.chua.starter.monitor.entity.MonitorAcmeCertificate;
import com.chua.starter.monitor.entity.MonitorAcmeOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * ACME 证书详情 VO
 * 包含证书信息、关联账户信息和订单信息
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-06
 */
@Data
@ApiModel(description = "ACME 证书详情 VO")
@Schema(description = "ACME 证书详情 VO")
public class AcmeCertificateDetailVo {

    /**
     * 证书信息
     */
    @ApiModelProperty("证书信息")
    @Schema(description = "证书信息")
    private MonitorAcmeCertificate certificate;

    /**
     * 关联账户信息
     */
    @ApiModelProperty("关联账户信息")
    @Schema(description = "关联账户信息")
    private MonitorAcmeAccount account;

    /**
     * 关联订单列表
     */
    @ApiModelProperty("关联订单列表")
    @Schema(description = "关联订单列表")
    private List<MonitorAcmeOrder> orders;

    /**
     * 距离到期天数
     */
    @ApiModelProperty("距离到期天数")
    @Schema(description = "距离到期天数")
    private Long daysUntilExpiry;

    /**
     * 是否即将到期（30天内）
     */
    @ApiModelProperty("是否即将到期")
    @Schema(description = "是否即将到期")
    private Boolean soonExpiring;
}
