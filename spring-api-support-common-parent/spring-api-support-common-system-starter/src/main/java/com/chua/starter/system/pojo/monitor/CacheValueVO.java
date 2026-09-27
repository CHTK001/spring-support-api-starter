package com.chua.starter.system.pojo.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 单个缓存 key 的探测结果
 *
 * @since 4.0.0.50
 * @author CH
 */
@Data
@Schema(description = "缓存 key 探测结果")
public class CacheValueVO implements Serializable {

    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;

    @Schema(description = "key")
    private String key;

    @Schema(description = "数据类型: string/hash/list/set/zset/none")
    private String type;

    @Schema(description = "剩余 TTL(秒), -1=永久, -2=不存在")
    private Long ttl;

    @Schema(description = "预览值(string 为原始值, 其他结构为序列化预览)")
    private String value;

    @Schema(description = "集合/列表元素数量")
    private Long size;

    @Schema(description = "是否可在线编辑（仅 Spring Cache 管理的缓存可编辑）")
    private Boolean editable;

    @Schema(description = "业务 JSON（去掉 Jackson 类型信封，供在线编辑；仅 Spring Cache 缓存有值）")
    private String plainValue;

    @Schema(description = "值根类型全限定名（仅 Spring Cache 缓存有值）")
    private String rootType;
}
