package com.chua.starter.monitor.pojo;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务器信息和配置DTO
 *
 * @author CH
 * @since 2024/12/26
 */
@ApiModel(description = "服务器信息和配置")
@Schema(description = "服务器信息和配置")
@Data
public class ServerWithConfigDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务器基本信息
     */
    @ApiModelProperty(value = "服务器基本信息")
    @Schema(description = "服务器基本信息")
    private MonitorSysGenServer server;

    /**
     * 服务器配置信息
     */
    @ApiModelProperty(value = "服务器配置信息")
    @Schema(description = "服务器配置信息")
    private MonitorSysGenServerSetting setting;

    /**
     * 时间戳
     */
    @ApiModelProperty(value = "时间戳")
    @Schema(description = "时间戳")
    private Long timestamp;

    /**
     * 构造函数
     */
    public ServerWithConfigDto() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造函数
     *
     * @param server  服务器信息
     * @param setting 配置信息
     */
    public ServerWithConfigDto(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        this.server = server;
        this.setting = setting;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 静态工厂方法
     *
     * @param server  服务器信息
     * @param setting 配置信息
     * @return ServerWithConfigDto实例
     */
    public static ServerWithConfigDto of(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        return new ServerWithConfigDto(server, setting);
    }
}
