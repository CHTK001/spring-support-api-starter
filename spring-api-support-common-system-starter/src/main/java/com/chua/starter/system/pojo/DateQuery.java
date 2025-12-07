package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 鏃ユ湡鏌ヨ
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "鏃ユ湡鏌ヨ")
public class DateQuery {

    /**
     * 寮€濮嬫棩鏈"     */
    @Schema(description = "寮€濮嬫棩鏈")
    private String startDate;

    /**
     * 缁撴潫鏃ユ湡
     */
    @Schema(description = "缁撴潫鏃ユ湡")
    private String endDate;
}