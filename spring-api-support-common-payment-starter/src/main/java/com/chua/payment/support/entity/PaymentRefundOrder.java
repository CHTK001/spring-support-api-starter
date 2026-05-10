package com.chua.payment.support.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款单实体
 */
@Data
@Schema(description = "退款单")
@TableName("pay_refund_order")
public class PaymentRefundOrder {

    @TableId(value = "pay_refund_order_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableField("pay_refund_order_refund_no")
    @Schema(description = "退款单号")
    private String refundNo;

    @TableField("pay_refund_order_order_id")
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @TableField("pay_refund_order_order_no")
    @Schema(description = "订单号")
    private String orderNo;

    @TableField("pay_refund_order_merchant_id")
    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    @TableField("pay_refund_order_channel_id")
    @Schema(description = "渠道ID", example = "1")
    private Long channelId;

    @TableField("pay_refund_order_source_order_status")
    @Schema(description = "退款前订单状态")
    private String sourceOrderStatus;

    @TableField("pay_refund_order_third_party_refund_no")
    @Schema(description = "第三方退款单号")
    private String thirdPartyRefundNo;

    @TableField("pay_refund_order_refund_amount")
    @Schema(description = "退款金额", example = "10.00")
    private BigDecimal refundAmount;

    @TableField("pay_refund_order_status")
    @Schema(description = "退款状态: PENDING-处理中, SUCCESS-成功, FAILED-失败", example = "PENDING")
    private String status;

    @TableField("pay_refund_order_reason")
    @Schema(description = "退款原因")
    private String reason;

    @TableField("pay_refund_order_notify_status")
    @Schema(description = "通知状态: 0-未通知, 1-已通知")
    private Integer notifyStatus;

    @TableField("pay_refund_order_request_payload")
    @Schema(description = "请求参数")
    private String requestPayload;

    @TableField("pay_refund_order_response_payload")
    @Schema(description = "响应参数")
    private String responsePayload;

    @TableField("pay_refund_order_operator")
    @Schema(description = "操作人")
    private String operator;

    @TableField("pay_refund_order_remark")
    @Schema(description = "备注")
    private String remark;

    /** 退款类型: USER-用户退款, SYSTEM-系统退款 */
    @TableField("pay_refund_order_refund_type")
    @Schema(description = "退款类型: USER-用户退款, SYSTEM-系统退款", example = "SYSTEM")
    private String refundType;

    @TableField(value = "pay_refund_order_created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "pay_refund_order_updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
