package com.chua.starter.system.pojo.email;

import lombok.Data;

import java.util.Map;

/**
 * 邮件请求
 * @author CH
 * @since 2024/9/4
 */
@Data
public class EmailRequest {
    /**
     * 邮件内容
     */
    private String content;
    /**
     * 邮件标题
     */
    private String title;
    /**
     * 邮件接收人
     */
    private String to;
    /**
     * 邮件模板名称
     */
    private String templateName;
    /**
     * 邮件模板参数
     */
    private Map<String, Object> maps;
}
