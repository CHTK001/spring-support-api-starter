package com.chua.starter.pay.support.pojo;

import com.chua.starter.pay.support.emuns.TradeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 微信扫码支付
 *
 * @author CH
 */
@Data
@Schema(description = "扫码支付")
public class PayQrCodeRequest {

    /**
     * 商户编码
     */
    @Schema(description = "商户编码")
    private String payMerchantCode;

    /**
     * 交易类型
     */
    @Schema(description = "交易类型")
    private TradeType tradeType;

    /**
     * 商户号
     */
    @Schema(description = "商户号")
    private String merchantCode;

    /**
     * 价格
     */
    @Schema(description = "价格")
    private BigDecimal price;
}
