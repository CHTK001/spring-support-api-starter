package com.chua.starter.video.pojo;

import lombok.Getter;

/**
 * 功能类型
 *
 * @author CH
 * @since 2025/9/20 16:14
 */
@Getter
public enum FunctionType {

    SYNC("同步"),
    SEARCH("搜索");

    private final String value;

    FunctionType(String value) {
        this.value = value;
    }
}
