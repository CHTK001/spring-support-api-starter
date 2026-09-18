package com.chua.starter.system.model.cache;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 缓存信息项
 *
 * @since 4.0.0.50
 * @author CH
 */
@Data
@Schema(description = "缓存信息项")
public class SysCacheInfo {

    /**
     * 缓存名称(逻辑名, 用于刷新接口入参)
     */
    @Schema(description = "缓存名称")
    private String cacheName;

    /**
     * 缓存Key(实际Redis Key前缀)
     */
    @Schema(description = "缓存Key")
    private String cacheKey;

    /**
     * 缓存描述
     */
    @Schema(description = "缓存描述")
    private String description;

    /**
     * 构造缓存信息项
     * @param cacheName 缓存名称，不允许为 null
     * @param cacheKey 缓存键，不允许为 null
     * @param description 描述，不允许为 null
     */
    public SysCacheInfo(String cacheName, String cacheKey, String description) {
        this.cacheName = cacheName;
        this.cacheKey = cacheKey;
        this.description = description;
    }
}
