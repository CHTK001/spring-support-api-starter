package com.chua.starter.admin.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 日志统计数据
 */
@Data
@Schema(description = "日志统计")
public class LogStaticVO {
    /**
     * 数量
     */
    @Schema(description = "数量")
    private Integer cnt;
    /**
     * 日期
     */
    @Schema(description = "日期")
    private String logDay;
    /**
     * 状态
     */
    @Schema(description = "状态; 0:失败")
    private String logStatus;
}
