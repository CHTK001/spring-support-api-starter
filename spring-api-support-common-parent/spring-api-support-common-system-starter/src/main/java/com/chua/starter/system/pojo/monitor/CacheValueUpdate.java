package com.chua.starter.system.pojo.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 缓存值修改请求
 *
 * <p>仅支持 Spring {@code Cache} 管理的缓存（值类型为序列化后的 string）。
 * {@code value} 为「业务 JSON」——不含 Jackson 默认类型信息（{@code @class} /
 * {@code ["java.util.Xxx", ...]} 信封），由后端按原类型还原后写回。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Data
@Schema(description = "缓存值修改请求")
public class CacheValueUpdate implements Serializable {

    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;

    @Schema(description = "缓存模块名（CacheManager 中的 cacheName）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String module;

    @Schema(description = "缓存 key（Redis 实际 key，如 cacheWithRedis:cache:role）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;

    @Schema(description = "业务 JSON 字符串（不含类型信封）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String value;
}