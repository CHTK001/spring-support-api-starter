package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 日期查询
 * @author CH
 * @since 2024/8/23
 */
@Data
@Schema(description = "日期查询")
public class DateQuery {

    /**
     * 开始时�?
     */
    @Schema(description = "开始时�?)
    private Date startDate;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private Date endDate;
}
