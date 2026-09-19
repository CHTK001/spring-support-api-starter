package com.chua.starter.system.pojo.error;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 前端异常上报请求
 *
 * @since 2026/8/8
 * @author CH
 */
@Data
@Schema(description = "前端异常上报请求")
public class ErrorReportV1Request {

    /**
     * 异常记录列表
     */
    @Schema(description = "异常记录列表")
    @NotEmpty(message = "异常记录列表不能为空")
    @Valid
    /**
     * Records
    */
    private List<ErrorReportV1Item> records;

    /**
     * 客户端上报时间(毫秒时间戳)
     */
    @Schema(description = "客户端上报时间(毫秒时间戳)")
    @NotNull(message = "上报时间不能为空")
    /**
     * 时间
    */
    private Long time;
}
