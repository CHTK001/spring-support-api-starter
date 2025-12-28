package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * SPI参数定义
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class SpiderSpiParameter {
    private String name;
    private String label;
    private String type;
    private Boolean required;
    private Object defaultValue;
    private String description;
    private List<Map<String, Object>> options;
    private Boolean sensitive;
}
