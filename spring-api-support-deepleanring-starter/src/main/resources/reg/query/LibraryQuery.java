package com.chua.starter.learning.reg.query;

import com.chua.starter.learning.reg.entity.SysFace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 上传信息
 * @author CH
 */
@Data
@Schema(description = "索引")
public class LibraryQuery extends SysFace {
    @Schema(description = "文件地址")
    private String url;
    /**
     * es索引
     */
    @Schema(description = "es索引")
    private String indexName;
}
