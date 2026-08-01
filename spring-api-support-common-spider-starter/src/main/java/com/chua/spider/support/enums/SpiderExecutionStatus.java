package com.chua.spider.support.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 采集执行状态枚举。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Getter
@Schema(description = "采集执行状态")
public enum SpiderExecutionStatus {

    /**
     * 成功
     */
    SUCCESS("SUCCESS", "成功"),

    /**
     * 失败
     */
    FAILED("FAILED", "失败"),

    /**
     * 运行中
     */
    RUNNING("RUNNING", "运行中"),

    /**
     * 已跳过
     */
    SKIPPED("SKIPPED", "已跳过");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 构造枚举。
     *
     * @param code 状态码
     * @param desc 状态描述
     */
    SpiderExecutionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
