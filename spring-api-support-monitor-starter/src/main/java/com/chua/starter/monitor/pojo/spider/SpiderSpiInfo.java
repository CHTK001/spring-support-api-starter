package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

import java.util.List;

/**
 * SPI组件信息
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class SpiderSpiInfo {
    private String name;
    private String displayName;
    private String description;
    private String type;
    private String icon;
    private String color;
    private Boolean available;
    private Integer order;
    private List<SpiderSpiParameter> parameters;
}
