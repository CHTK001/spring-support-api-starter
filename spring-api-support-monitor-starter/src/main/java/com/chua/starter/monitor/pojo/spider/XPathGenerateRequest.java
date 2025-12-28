package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

/**
 * XPath生成请求
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class XPathGenerateRequest {
    private String url;
    private String elementPath;
}
