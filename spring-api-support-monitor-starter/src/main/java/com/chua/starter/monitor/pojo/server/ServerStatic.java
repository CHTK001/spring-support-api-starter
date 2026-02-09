package com.chua.starter.monitor.pojo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 服务器信息统计
 *
 * @author CH
 * @since 2025/9/1 13:31
 */
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

    public Integer getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(Integer dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public Integer getDataSourceTotal() {
        return dataSourceTotal;
    }

    public void setDataSourceTotal(Integer dataSourceTotal) {
        this.dataSourceTotal = dataSourceTotal;
    }

    public Integer getDataSourceOnline() {
        return dataSourceOnline;
    }

    public void setDataSourceOnline(Integer dataSourceOnline) {
        this.dataSourceOnline = dataSourceOnline;
    }

    public Integer getDataSourceOffline() {
        return dataSourceOffline;
    }

    public void setDataSourceOffline(Integer dataSourceOffline) {
        this.dataSourceOffline = dataSourceOffline;
    }
}

