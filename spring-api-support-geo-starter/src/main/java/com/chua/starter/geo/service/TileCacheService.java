package com.chua.starter.geo.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.chua.starter.geo.config.GeoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 瓦片本地缓存服务，基于 Caffeine 实现 LRU 淘汰策略。
 * 缓存瓦片字节数据，减少重复网络请求。
 *
 * @author CH
 * @since 1.0.0
 */
@Slf4j
@Service
public class TileCacheService {

    /** Caffeine 缓存实例 */
    private Cache<String, byte[]> cache;

    /** Geo 配置属性 */
    private final GeoProperties properties;

    /**
     * 构造函数。
     *
     * @param properties Geo 配置属性
     */
    public TileCacheService(GeoProperties properties) {
        this.properties = properties;
    }

    /**
     * 应用启动后初始化缓存。
     *
     * @param event 应用启动事件
     */
    @EventListener(ApplicationStartedEvent.class)
    public void init() {
        int maxMb = properties.getTile().getCacheMaxSizeMb();
        // 每条约 ~1KB，估算最大条目数
        long maxEntries = (long) maxMb * 1024;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxEntries)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
                .build();
        log.info("瓦片缓存初始化完成: max={} MB", maxMb);
    }

    /**
     * 从缓存中获取瓦片数据。
     *
     * @param key 缓存键，格式 "{z}_{x}_{y}"
     * @return 缓存的瓦片字节数组，未命中返回 null
     */
    public byte[] get(String key) {
        return cache.getIfPresent(key);
    }

    /**
     * 将瓦片数据写入缓存。
     *
     * @param key  缓存键，格式 "{z}_{x}_{y}"
     * @param data 瓦片字节数据
     */
    public void put(String key, byte[] data) {
        cache.put(key, data);
    }

    /**
     * 清空所有缓存数据。
     */
    public void clear() {
        cache.invalidateAll();
        log.info("瓦片缓存已清空");
    }
}
