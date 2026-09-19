package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 监控资产实体，记录需监控的目标（服务器、Redis、MySQL等），
 * 是采集任务与网关的关联核心维度。
 *
 * @since 2026-08-11
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ms_monitor")
@ApiModel(description = "监控资产")
@Schema(description = "监控资产")
public class MsMonitor extends SysBase {

    /**
     * 监控资产主键标识，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    /** 标识 */
    private Long id;

    /**
     * 监控资产名称，用于前端展示，例如 生产Web-01
     */
    @TableField("monitor_name")
    @ApiModelProperty("监控名称")
    @Schema(description = "监控名称")
    /** 监控名称 */
    private String monitorName;

    /**
     * 监控资产主机地址，例如 192.168.1.10 或 Redis.example.com
     */
    @TableField("monitor_host")
    @ApiModelProperty("主机地址")
    @Schema(description = "主机地址")
    /** 监控主机 */
    private String monitorHost;

    /**
     * 监控资产端口，例如 6379
     */
    @TableField("monitor_port")
    @ApiModelProperty("端口")
    @Schema(description = "端口")
    /** 监控端口 */
    private Integer monitorPort;

    /**
     * 服务类型，取值 服务端/Redis/MySQL/PG/ORACLE/MSSQL
     */
    @TableField("service_type")
    @ApiModelProperty("服务类型（SERVER/REDIS/MYSQL/PG/ORACLE/MSSQL）")
    @Schema(description = "服务类型")
    /** 服务类型 */
    private String serviceType;

    /**
     * 服务器操作系统类型，取值 Linux/窗口/macOS
     */
    @TableField("server_type")
    @ApiModelProperty("服务器类型（LINUX/WINDOWS/MACOS）")
    @Schema(description = "服务器类型")
    /** 服务器类型 */
    private String serverType;

    /**
     * 监控资产分组，用于归类，例如 生产环境/测试环境
     */
    @TableField("monitor_group")
    @ApiModelProperty("监控分组")
    @Schema(description = "监控分组")
    /** 监控分组 */
    private String monitorGroup;

    /**
     * 监控资产启用状态，1=启用，0=禁用
     */
    @TableField("monitor_enabled")
    @ApiModelProperty("启用状态（0禁用/1启用）")
    @Schema(description = "启用状态（0禁用/1启用）")
    /** 监控是否启用 */
    private Integer monitorEnabled;

    /**
     * 监控资产描述信息，可为空
     */
    @TableField("monitor_description")
    @ApiModelProperty("描述")
    @Schema(description = "描述")
    /** 监控描述 */
    private String monitorDescription;

    /**
     * 逻辑删除标记，0=未删除，1=已删除
     */
    @TableField("deleted")
    @TableLogic
    @ApiModelProperty("逻辑删除")
    @Schema(description = "逻辑删除")
    /** 删除标记 */
    private Integer deleted;

    /**
     * 关联服务器主机 标识（ms_服务端_主机.标识，用于联动服务器台账配置）
     */
    @TableField("ms_server_host_id")
    @ApiModelProperty("关联服务器主机ID")
    @Schema(description = "关联服务器主机ID")
    /** 关联服务器主机标识 */
    private Long msServerHostId;
}
