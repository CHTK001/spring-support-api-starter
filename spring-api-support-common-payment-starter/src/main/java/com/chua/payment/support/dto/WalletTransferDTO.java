package com.chua.payment.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 钱包转账DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransferDTO {
    private Long merchantId;
    private Long fromUserId;
    private Long toUserId;
    private String transferNo;
    private BigDecimal amount;
    private String operator;
    private String remark;
    /** 转账方式: WALLET-钱包转账, ALIPAY-支付宝, WECHAT-微信, BANK_CARD-银行卡 */
    private String transferMethod;
}
