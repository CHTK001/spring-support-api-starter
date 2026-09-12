package com.chua.starter.geo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
* Geo 服务全局配置属性，绑定 application.yml 中 geo.* 前缀的配置项。
*
* @author CH
* @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "geo")
public class GeoProperties {

    /** 瓦片下载配置 */
    private TileCfg tile = new TileCfg();

    /** 图像导出配置 */
    private ExportCfg export = new ExportCfg();

    /** 代理配置 */
    private ProxyCfg proxy = new ProxyCfg();

    /** 天地图 令牌 */
    private String tiandituToken;

    /** 是否允许无效 HTTPS 证书（安全风险，生产环境应禁用） */
    private boolean allowInvalidCerts;

    /** 瓦片下载配置 */
    @Data
    public static class TileCfg {
        /** 瓦片大小（像素） */
        private int size = 256;
        /** 最大并发下载数 */
        private int maxConcurrency = 50;
        /** 重试次数 */
        private int retryTimes = 2;
        /** 请求超时（秒） */
        private int timeoutSeconds = 15;
        /** 请求间隔（毫秒） */
        private int delayMs = 20;
        /** 是否启用本地缓存 */
        private boolean cacheEnabled = true;
        /** 缓存容量上限（MB） */
        private int cacheMaxSizeMb = 2048;
        /** 默认缩放级别 */
        private int defaultZoom = 15;
    }

    /** 导出配置 */
    @Data
    public static class ExportCfg {
        /** TIFF 压缩方式：无/lzw/deflate */
        private String compression = "lzw";
        /** 是否构建影像金字塔 */
        private boolean buildPyramid = true;
        /** 是否生成 .prj 侧面文件 */
        private boolean generateSidecars = true;
    }

    /** 代理配置 */
    @Data
    public static class ProxyCfg {
        /** 是否启用代理 */
        private boolean enabled = false;
        /** 代理地址，如 http://127.0.0.1:7890 */
        private String url = "";
    }
}
