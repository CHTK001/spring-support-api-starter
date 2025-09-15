package com.chua.starter.video.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * PanSou API 配置类
 * 用于配置网盘搜索服务相关参数
 *
 * @author CH
 * @version 1.0
 * @since 2024/12/19
 */
@Data
@Component
@ConfigurationProperties(prefix = "plugin.pansou")
public class PanSouConfig {

    /**
     * PanSou API 基础URL
     * 默认使用官方演示地址
     */
    private String baseUrl = "https://so.252035.xyz";

    /**
     * API 搜索端点
     */
    private String searchEndpoint = "/api/search";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 30000;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 每页返回结果数量
     */
    private int pageSize = 20;

    /**
     * 最大搜索结果数量
     */
    private int maxResults = 100;

    /**
     * 启用的插件列表
     * 默认启用常用插件
     */
    private List<String> enabledPlugins = Arrays.asList(
            "labi", "zhizhen", "shandian", "duoduo", "muou", "wanou",
            "hunhepan", "jikepan", "panwiki", "pansearch", "panta", "qupansou"
    );

    /**
     * 搜索的TG频道列表
     */
    private List<String> channels = Arrays.asList(
            "tgsearchers3", "Aliyun_4K_Movies", "bdbdndn11", "yunpanx"
    );

    /**
     * 是否启用异步搜索
     */
    private boolean asyncEnabled = true;

    /**
     * 异步响应超时时间（秒）
     */
    private int asyncResponseTimeout = 4;

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 缓存有效期（分钟）
     */
    private int cacheTtl = 60;

    /**
     * 用户代理字符串
     */
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    /**
     * 是否启用调试日志
     */
    private boolean debugEnabled = false;

    /**
     * 代理配置（可选）
     */
    private String proxy;

    /**
     * API密钥（如果需要）
     */
    private String apiKey;

    /**
     * 获取完整的搜索URL
     *
     * @return 完整的搜索URL
     */
    public String getSearchUrl() {
        return baseUrl + searchEndpoint;
    }

    /**
     * 检查配置是否有效
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        return baseUrl != null && !baseUrl.trim().isEmpty()
                && searchEndpoint != null && !searchEndpoint.trim().isEmpty();
    }

    /**
     * 获取启用插件的逗号分隔字符串
     *
     * @return 插件列表字符串
     */
    public String getEnabledPluginsString() {
        if (enabledPlugins == null || enabledPlugins.isEmpty()) {
            return "";
        }
        return String.join(",", enabledPlugins);
    }

    /**
     * 获取频道的逗号分隔字符串
     *
     * @return 频道列表字符串
     */
    public String getChannelsString() {
        if (channels == null || channels.isEmpty()) {
            return "";
        }
        return String.join(",", channels);
    }
}