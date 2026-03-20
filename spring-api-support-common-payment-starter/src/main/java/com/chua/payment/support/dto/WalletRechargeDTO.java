package com.chua.payment.support.dto;

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
public class WalletRechargeDTO {
    private Long merchantId;
    private Long userId;
    private String rechargeNo;
    private BigDecimal amount;
    private String operator;
    private String remark;
}
