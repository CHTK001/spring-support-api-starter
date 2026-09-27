package com.chua.starter.video.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频服务配置。
 *
 * <p>前缀 {@code spring.video}。用于描述多源聚合检索与分类浏览的行为：
 * 数据源地址、无关键词时的兜底关键词、结果上限与详情缓存等。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@ConfigurationProperties(prefix = VideoProperties.PRE, ignoreInvalidFields = true)
public class VideoProperties {

    /**
     * 配置前缀。
     */
    public static final String PRE = "spring.video";

    /**
     * 是否启用视频服务模块。
     */
    private boolean enabled = true;

    /**
     * MacCMS 兼容资源站采集接口地址。
     *
     * <p>形如 {@code https://example.com/api.php/provide/vod/}，用于无关键词的
     * 分类浏览与详情查询；置空则关闭分类浏览，退化为纯多源聚合检索。</p>
     */
    private String catalogUrl = "https://cj.lziapi.com/api.php/provide/vod/";

    /**
     * 无关键词且分类浏览不可用时的兜底检索关键词。
     */
    private String defaultKeyword = "";

    /**
     * 单次聚合返回的最大条目数，防止外部数据源返回过多。
     */
    private int maxResults = 60;

    /**
     * 详情内存缓存的容量（按 originId 缓存最近浏览过的条目）。
     */
    private int detailCacheSize = 500;

    /**
     * 连接超时（毫秒）。
     */
    private int connectTimeout = 8000;

    /**
     * 读取超时（毫秒）。
     */
    private int readTimeout = 15000;

    /**
     * 分类浏览关键词映射：前端分类码（movie/tv/anime/variety/documentary）
     * 到资源站检索关键词的映射；未配置时按最新更新返回并由服务端按分类过滤。
     */
    private Map<String, String> browseKeywords = new LinkedHashMap<>();

    /**
     * 是否启用直播模块。
     */
    private boolean liveEnabled = true;

    /**
     * 外部直播目录文件（.json 目录 / .m3u(m3u8) 播放列表），置空使用内置目录。
     */
    private String liveChannelFile = "";

    /**
     * HLS 代理额外允许的主机后缀白名单（用于跨 CDN 分片）。
     */
    private List<String> liveAllowedHosts = new ArrayList<>();

    /**
     * HLS 代理单次响应的最大字节数（默认 8MB），防止异常大文件。
     */
    private int liveProxyMaxBytes = 8 * 1024 * 1024;
}
