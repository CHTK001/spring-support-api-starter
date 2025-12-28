package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

import java.util.List;

/**
 * SPI类型列表
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class SpiderSpiTypeList {
    private List<SpiderSpiInfo> urlSource;
    private List<SpiderSpiInfo> downloader;
    private List<SpiderSpiInfo> processor;
    private List<SpiderSpiInfo> middleware;
    private List<SpiderSpiInfo> dataFilter;
    private List<SpiderSpiInfo> pipeline;
    private List<SpiderSpiInfo> scheduler;
    private List<SpiderSpiInfo> siteConfig;
    private List<SpiderSpiInfo> proxyPool;
    private List<SpiderSpiInfo> uaPool;
}
