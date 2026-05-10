package com.chua.payment.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单创建数据传输对象
 *
 * @author CH
 * @since 2026-03-18
 */
@Data
@Schema(description = "订单创建参数")
public class OrderCreateDTO implements Serializable {
    @Schema(description = "商户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long merchantId;

    @Schema(description = "支付方式ID")
    private Long channelId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "业务订单号（商户侧）")
    private String businessOrderNo;

    @Schema(description = "渠道类型: WECHAT/ALIPAY/WALLET", example = "WALLET")
    private String channelType;

    @Schema(description = "渠道子类型: BALANCE/QRCODE/APP", example = "BALANCE")
    private String channelSubType;

    @Schema(description = "订单金额", example = "100.00")
    private BigDecimal orderAmount;

    @Schema(description = "优惠金额", example = "0.00")
    private BigDecimal discountAmount;

    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "订单标题")
    private String subject;

    @Schema(description = "订单描述")
    private String body;

    @Schema(description = "支付回调地址")
    private String notifyUrl;

    @Schema(description = "返回地址")
    private String returnUrl;

    @Schema(description = "过期时间（分钟）")
    private Integer expireMinutes;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "第三方订单号（微信 out_trade_no），相同订单号无法在微信重复发起支付")
    private String thirdPartyOrderNo;

    @Schema(description = "商品编号")
    private String productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "优惠券编码")
    private String couponCode;
}
