package com.chua.starter.monitor.pojo.acme;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 申请证书请求
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@Data
@ApiModel(description = "申请证书请求")
@Schema(description = "申请证书请求")
public class AcmeApplyCertRequest {

    @ApiModelProperty("ACME账户ID")
    @Schema(description = "ACME账户ID")
    private Integer acmeAccountId;

    @ApiModelProperty("主域名")
    @Schema(description = "主域名")
    private String primaryDomain;

    @ApiModelProperty("备用域名列表")
    @Schema(description = "备用域名列表")
    private List<String> sanDomains;

    @ApiModelProperty("挑战类型: HTTP-01 或 DNS-01")
    @Schema(description = "挑战类型: HTTP-01 或 DNS-01")
    private String challengeType;
}
