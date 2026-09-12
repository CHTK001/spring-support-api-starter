package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* 日期查询
*
* @since 2024/8/20
* @author CH
 */
@Data
@Schema(description = "日期查询")
public class DateQuery {

    /**
    * 开始日期
     */
    @Schema(description = "开始日期")
    /** 开始日期 */
    private String startDate;

    /**
    * 结束日期
     */
    @Schema(description = "结束日期")
    /** 结束日期 */
    private String endDate;
}