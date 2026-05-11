package com.chua.payment.support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付全局配置
 */
@Data
@TableName("pay_global_config")
public class PaymentGlobalConfig {

    @TableId(value = "pay_global_config_id", type = IdType.AUTO)
    private Long id;

    @TableField("pay_global_config_config_key")
    private String configKey;

    @TableField("pay_global_config_notify_base_url")
    private String paymentNotifyBaseUrl;

    @TableField("pay_global_config_return_url")
    private String paymentReturnUrl;

    @TableField("pay_global_config_callback_path_template")
    private String paymentCallbackPathTemplate;

    @TableField("pay_global_config_auto_refresh_seconds")
    private Integer paymentAutoRefreshSeconds;

    @TableField("pay_global_config_remark")
    private String remark;

    @TableField("pay_global_config_sample_pay_notify_url")
    private String paymentSamplePayNotifyUrl;

    @TableField("pay_global_config_sample_refund_notify_url")
    private String paymentSampleRefundNotifyUrl;

    @TableField("pay_global_config_sample_wallet_recharge_notify_url")
    private String paymentSampleWalletRechargeNotifyUrl;

    @TableField("pay_global_config_sample_wallet_transfer_notify_url")
    private String paymentSampleWalletTransferNotifyUrl;

    @TableField("pay_global_config_sample_wallet_withdraw_notify_url")
    private String paymentSampleWalletWithdrawNotifyUrl;

    @TableField("pay_global_config_create_time")
    private LocalDateTime createTime;

    @TableField("pay_global_config_update_time")
    private LocalDateTime updateTime;
}
