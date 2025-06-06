package com.chua.starter.pay.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单统计数据
 *
 * @author CH
 * @since 2023/04/20
 */
@Data
@Schema(description = "订单统计数据")
public class PayOrderStatisticDTO {
    
    /**
     * 订单总数
     */
    @Schema(description = "订单总数")
    private Integer totalCount;
    
    /**
     * 已完成订单数
     */
    @Schema(description = "已完成订单数")
    private Integer completedCount;
    
    /**
     * 待支付订单数
     */
    @Schema(description = "待支付订单数")
    private Integer pendingCount;
    
    /**
     * 已关闭订单数
     */
    @Schema(description = "已关闭订单数")
    private Integer closedCount;
} 