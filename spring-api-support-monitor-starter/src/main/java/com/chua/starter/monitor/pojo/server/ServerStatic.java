package com.chua.starter.monitor.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务器信息统计
 *
 * @author CH
 * @since 2025/9/1 13:31
 */
@Data
@Schema(description = "服务器信息统计")
public class ServerStatic {

    /**
     * 数据源类型
     */
    @Schema(description = "数据源类型")
    private Integer dataSourceType;

    /**
     * 数据源总数
     */
    @Schema(description = "数据源总数")
    private Integer dataSourceTotal;
    /**
     * 数据源在线
     */
    @Schema(description = "数据源在线")
    private Integer dataSourceOnline;
    /**
     * 数据源离线
     */
    @Schema(description = "数据源离线")
    private Integer dataSourceOffline;

}
