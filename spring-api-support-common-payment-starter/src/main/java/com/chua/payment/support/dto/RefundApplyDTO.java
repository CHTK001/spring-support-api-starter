package com.chua.payment.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款申请 DTO
 */
@Data
@Schema(description = "退款申请参数")
public class RefundApplyDTO {

    @Schema(description = "退款金额", example = "10.00")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "退款类型: USER-用户退款, SYSTEM-系统退款", example = "SYSTEM")
    private String refundType = "SYSTEM";
}
