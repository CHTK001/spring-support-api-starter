package com.chua.payment.support.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款单视图对象
 */
@Data
@Schema(description = "退款单视图")
public class RefundOrderVO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "退款单号")
    private String refundNo;

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "渠道ID", example = "1")
    private Long channelId;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "退款前订单状态")
    private String sourceOrderStatus;

    @Schema(description = "退款前订单状态说明")
    private String sourceOrderStatusDesc;

    @Schema(description = "第三方退款单号")
    private String thirdPartyRefundNo;

    @Schema(description = "退款金额", example = "10.00")
    private BigDecimal refundAmount;

    @Schema(description = "退款状态: PENDING-处理中, SUCCESS-成功, FAILED-失败", example = "PENDING")
    private String status;

    @Schema(description = "退款状态说明")
    private String statusDesc;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "通知状态: 0-未通知, 1-已通知")
    private Integer notifyStatus;

    @Schema(description = "请求参数")
    private String requestPayload;

    @Schema(description = "响应参数")
    private String responsePayload;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "备注")
    private String remark;

    /** 退款类型: USER-用户退款, SYSTEM-系统退款 */
    @Schema(description = "退款类型: USER-用户退款, SYSTEM-系统退款", example = "SYSTEM")
    private String refundType;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
