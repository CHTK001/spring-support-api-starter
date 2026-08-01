package com.chua.spider.support.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 爬虫启停状态枚举。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Getter
@Schema(description = "爬虫启停状态")
public enum SpiderStatus {

    /**
     * 已启用
     */
    ENABLED(1, "已启用"),

    /**
     * 已禁用
     */
    DISABLED(0, "已禁用");

    /**
     * 状态码
     */
    private final int code;

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
    SpiderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
