package com.chua.starter.learning.reg.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 索引查询信息
 *
 * @author CH
 */
@Data
@Schema(description = "索引")
public class IndexQuery {
    @Schema(description = "索引", required = true)
    private String indexName;

    @Schema(description = "远程图片")
    private String url;
    @Schema(description = "关键词")
    private String keyword;
    @Schema(description = "页码")
    private Integer page = 1;
    @Schema(description = "每页数量")
    private Integer pageSize = 10;
}
