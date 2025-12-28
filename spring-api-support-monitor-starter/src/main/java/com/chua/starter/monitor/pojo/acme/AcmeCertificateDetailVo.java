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

    /**
     * curl 自检命令列表（用于验证HTTP-01挑战是否可访问）
     */
    @ApiModelProperty("curl 自检命令列表")
    @Schema(description = "curl 自检命令列表，用于验证HTTP-01挑战是否可从外网访问")
    private java.util.List<String> curlCommands;

    /**
     * 当前步骤（用于前端展示申请流程）
     * 1 - 提交申请（pending）
     * 2 - 等待验证（validating）
     * 3 - 签发完成（valid）或失败/吐销（failed/revoked/expired）
     */
    @ApiModelProperty("当前步骤: 1-提交申请, 2-等待验证, 3-签发完成")
    @Schema(description = "当前步骤: 1-提交申请, 2-等待验证, 3-签发完成")
    private Integer currentStep;

    /**
     * 步骤描述（当前步骤的中文描述）
     */
    @ApiModelProperty("步骤描述")
    @Schema(description = "当前步骤的中文描述")
    private String stepDescription;
}
