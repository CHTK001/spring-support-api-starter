package com.chua.payment.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 钱包充值 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "钱包充值参数")
public class WalletRechargeDTO {
    @Schema(description = "商户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long merchantId;
    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    @Schema(description = "充值单号（幂等用）")
    private String rechargeNo;
    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "是否创建钱包订单，默认 true；管理员直充时可设为 false 跳过订单创建", example = "true")
    private Boolean createOrder;
}
