package com.chua.starter.system.pojo.error;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 前端异常上报请求
 *
 * @param records 异常记录列表
 * @param time 客户端上报时间(毫秒时间戳)
 *
 * @since 2026/8/8
 * @author CH
 */
@Schema(description = "前端异常上报请求")
public record ErrorReportV1Request(
        @Schema(description = "异常记录列表") @NotEmpty(message = "异常记录列表不能为空") @Valid List<ErrorReportV1Item> records,
        @Schema(description = "客户端上报时间(毫秒时间戳)") @NotNull(message = "上报时间不能为空") Long time
) {

    /**
     * 规范构造器：异常记录列表做防御性拷贝，上报时间为空值敌对。
     *
     * <p>value class 前置条件——集合组件必须深不可变，必填组件必须非空。
     * 入参侧显式容错（列表可为 null、列表元素可为 null），故两者都保留原有语义：
     * 列表为 null 时仍返回 null，非 null 时用可含 null 元素的不可变包装拷贝。</p>
     *
     * @param records 异常记录列表
     * @param time    客户端上报时间(毫秒时间戳)
     */
    public ErrorReportV1Request {
        records = records == null
                ? null
                : Collections.unmodifiableList(new ArrayList<>(records));
        time = Objects.requireNonNull(time, "time 不能为 null");
    }
}
