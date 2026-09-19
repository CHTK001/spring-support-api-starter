package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务器布局配置实体，保存服务器监控大屏的整体布局信息，
 * 布局内容以 JSON 形式存储（节点 / edges / 版本 / generatedat 等结构），前端可直接回显。
 *
 * @since 2026-07-28
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_server_layout")
@ApiModel(description = "服务器布局配置")
@Schema(description = "服务器布局配置")
public class MonitorSysGenServerLayout extends SysBase {

    /**
     * 布局主键标识，自增
     */
    @TableId(value = "monitor_sys_gen_server_layout_id", type = IdType.AUTO)
    @ApiModelProperty("布局ID")
    @Schema(description = "布局ID")
    /** 监控sysgen服务器layoutid */
    private Integer monitorSysGenServerLayoutId;

    /**
     * 关联的服务器标识，标识该布局属于哪台服务器
     */
    @TableField("monitor_sys_gen_server_id")
    @ApiModelProperty("关联服务器ID")
    @Schema(description = "关联服务器ID")
    /** 监控sysgen服务器标识 */
    private Integer monitorSysGenServerId;

    /**
     * 布局名称，用于前端展示与区分多套布局
     */
    @TableField("monitor_sys_gen_server_layout_name")
    @ApiModelProperty("布局名称")
    @Schema(description = "布局名称")
    /** 监控sysgen服务器layout名称 */
    private String monitorSysGenServerLayoutName;

    /**
     * 布局配置 JSON，包含 节点 / edges / 版本 / generatedat 等结构
     */
    @TableField("monitor_sys_gen_server_layout_config")
    @ApiModelProperty("布局配置 JSON")
    @Schema(description = "布局配置 JSON")
    /** 监控sysgen服务器layout配置 */
    private String monitorSysGenServerLayoutConfig;

    /**
     * 布局类型，取值范围：默认（默认）/generated（生成）/copied（复制）/习俗（自定义）
     */
    @TableField("monitor_sys_gen_server_layout_type")
    @ApiModelProperty("布局类型（default/generated/copied/custom）")
    @Schema(description = "布局类型（default/generated/copied/custom）")
    /** 监控sysgen服务器layout类型 */
    private String monitorSysGenServerLayoutType;

    /**
     * 是否默认布局，1-默认布局，0-非默认，同一服务器仅允许一个默认布局
     */
    @TableField("monitor_sys_gen_server_layout_default")
    @ApiModelProperty("是否默认布局（0/1）")
    @Schema(description = "是否默认布局（0/1）")
    /** 监控sysgen服务器layout默认 */
    private Integer monitorSysGenServerLayoutDefault;

    /**
     * 布局状态，1-启用，0-禁用
     */
    @TableField("monitor_sys_gen_server_layout_status")
    @ApiModelProperty("布局状态（0禁用/1启用）")
    @Schema(description = "布局状态（0禁用/1启用）")
    /** 监控sysgen服务器layout状态 */
    private Integer monitorSysGenServerLayoutStatus;

    /**
     * 布局描述，说明布局用途或来源，可为空
     */
    @TableField("monitor_sys_gen_server_layout_description")
    @ApiModelProperty("布局描述")
    @Schema(description = "布局描述")
    /** 监控sysgen服务器layout描述 */
    private String monitorSysGenServerLayoutDescription;

    /**
     * 布局版本号，每次更新配置时递增，用于前端缓存失效判断
     */
    @TableField("monitor_sys_gen_server_layout_version")
    @ApiModelProperty("布局版本号")
    @Schema(description = "布局版本号")
    /** 监控sysgen服务器layout版本 */
    private Integer monitorSysGenServerLayoutVersion;

    /**
     * 布局排序值，数值越小越靠前
     */
    @TableField("monitor_sys_gen_server_layout_sort")
    @ApiModelProperty("布局排序")
    @Schema(description = "布局排序")
    /** 监控sysgen服务器layoutsort */
    private Integer monitorSysGenServerLayoutSort;
}
