package com.chua.starter.pay.support.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.chua.starter.pay.support.properties.PayProperties.Scope.GOODS;

/**
 * 支付通知
 *
 * @author CH
 * @since 2025/1/3
 */
@Data
@ConfigurationProperties(prefix = PayProperties.PRE, ignoreInvalidFields = true)
public class PayProperties {

    public static final String PRE = "plugin.pay";
    /**
     * 支付时检测未支付订单
     */
    private boolean whenPayCheckNoPayOrder;

    /**
     * whenPayCheckNoPayOrder = true生效
     * 支付检测订单范围
     * ALL: 整个租户所有未支付订单只能存在一个
     * GOODS: 单个商品未支付订单只能存在一个
     */
    private Scope checkScope = GOODS;


    /**
     * 支付时检测未支付订单
     */
    public enum Scope {

        /**
         * 商品
         */
        GOODS,

        /**
         * 全部
         */
        ALL
    }
}
