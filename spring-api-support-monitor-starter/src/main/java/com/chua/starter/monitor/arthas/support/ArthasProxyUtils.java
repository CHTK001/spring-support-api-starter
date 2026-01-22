package com.chua.starter.monitor.arthas.support;

import com.chua.common.support.network.net.NetAddress;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Arthas 代理相关工具
 * - 统一元数据键名与地址转换逻辑
 */
@Slf4j
@UtilityClass
public class ArthasProxyUtils {

    /** 节点元数据中的 Arthas Tunnel 地址键名 */
    public static final String ARTHAS_META_KEY = "report.client.arthas.port";

    /**
     * 将元数据中的地址（ws(s)://host:port/ws 或 http(s)://host:port/）规范化为 http(s) 基础地址（以 / 结尾）
     */
    public static String toHttpBase(String metaValue) {
        if (StringUtils.isBlank(metaValue)) return null;
        try {
            String url = metaValue.trim().replaceAll("/+$", "");
            if (StringUtils.startsWithIgnoreCase(url, "ws://")) {
                url = "http://" + url.substring("ws://".length());
            } else if (StringUtils.startsWithIgnoreCase(url, "wss://")) {
                url = "https://" + url.substring("wss://".length());
            }
            if (StringUtils.endsWithIgnoreCase(url, "/ws")) {
                url = url.substring(0, url.length() - 3);
            }
            if (!url.endsWith("/")) url = url + "/";
            NetAddress.of(url);
            return url;
        } catch (Exception e) {
            log.warn("解析 Arthas HTTP 基础地址失败: {}", metaValue, e);
            return null;
        }
    }

    /**
     * 将元数据中的地址规范化为 ws(s) 地址（以 /ws 结尾）
     */
    public static String toWsUrl(String metaValue) {
        if (StringUtils.isBlank(metaValue)) return null;
        String url = metaValue.trim();
        if (StringUtils.startsWithIgnoreCase(url, "http://")) {
            url = "ws://" + url.substring("http://".length());
        } else if (StringUtils.startsWithIgnoreCase(url, "https://")) {
            url = "wss://" + url.substring("https://".length());
        }
        if (!StringUtils.endsWithIgnoreCase(url, "/ws")) {
            if (url.endsWith("/")) url = url + "ws"; else url = url + "/ws";
        }
        return url;
    }
}

