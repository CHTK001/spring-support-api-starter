package com.chua.starter.system.model.cache;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 缓存信息项
 *
 * @param cacheName 缓存名称(逻辑名, 用于刷新接口入参)
 * @param cacheKey 缓存Key(实际Redis Key前缀)
 * @param description 缓存描述
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "缓存信息项")
public record SysCacheInfo(
        @Schema(description = "缓存名称") String cacheName,
        @Schema(description = "缓存Key") String cacheKey,
        @Schema(description = "缓存描述") String description
) {
}
