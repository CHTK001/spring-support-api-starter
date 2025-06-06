package com.chua.starter.pay.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商户统计数据
 *
 * @author CH
 * @since 2023/04/20
 */
@Data
@Schema(description = "商户统计数据")
public class PayMerchantStatisticDTO {
    
    /**
     * 商户总数
     */
    @Schema(description = "商户总数")
    private Integer totalCount;
    
    /**
     * 已启用商户数
     */
    @Schema(description = "已启用商户数")
    private Integer enabledCount;
    
    /**
     * 钱包已开启商户数
     */
    @Schema(description = "钱包已开启商户数")
    private Integer walletEnabledCount;
} 