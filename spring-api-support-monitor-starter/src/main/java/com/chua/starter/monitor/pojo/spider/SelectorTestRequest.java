package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

/**
 * 选择器测试请求
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class SelectorTestRequest {
    private String url;
    private String selector;
    private String type;
}
