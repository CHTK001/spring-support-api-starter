package com.chua.starter.admin.pojo;

import lombok.Data;

/**
 * 日志统计数据
 */
@Data
public class LogStaticVO {
    /**
     * 数量
     */
    private Integer cnt;
    /**
     * 日期
     */
    private String logDay;
    /**
     * 状态
     */
    private String logStatus;
}
