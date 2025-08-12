package com.chua.starter.monitor.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统能力
 *
 * 表示数据源在备份、文档、文件等方面的能力支持。
 *
 * @author CH
 */
@Data
@Schema(description = "系统能力")
public class SystemCapabilities {

    /** 是否支持备份 */
    @Schema(description = "是否支持备份")
    private boolean backup;

    /** 是否支持文档 */
    @Schema(description = "是否支持文档")
    private boolean document;

    /** 是否支持文件 */
    @Schema(description = "是否支持文件")
    private boolean file;
}
