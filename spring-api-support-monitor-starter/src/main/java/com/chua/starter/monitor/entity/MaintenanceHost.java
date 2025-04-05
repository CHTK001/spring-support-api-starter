package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护主机实体类
 * 定义维护管理中的主机信息
 *
 * @author CH
 */
@Data
@TableName("t_maintenance_host")
@Schema(description = "维护主机")
public class MaintenanceHost {

    /**
     * 维护主机ID，主键
     */
    @TableId(value = "maintenance_host_id", type = IdType.AUTO)
    @Schema(description = "维护主机ID")
    private Long maintenanceHostId;

    /**
     * 维护组ID
     */
    @TableField("maintenance_group_id")
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 主机地址
     */
    @TableField("maintenance_host_address")
    @Schema(description = "主机地址")
    private String maintenanceHostAddress;

    /**
     * 主机端口
     */
    @TableField("maintenance_host_port")
    @Schema(description = "端口")
    private Integer maintenanceHostPort;

    /**
     * 用户名
     */
    @TableField("maintenance_host_username")
    @Schema(description = "账号")
    private String maintenanceHostUsername;

    /**
     * 密码
     */
    @TableField("maintenance_host_password")
    @Schema(description = "密码")
    private String maintenanceHostPassword;

    /**
     * 是否启用
     */
    @TableField("maintenance_host_enabled")
    @Schema(description = "状态;0:停用;1:启用")
    private Boolean maintenanceHostEnabled;

    /**
     * 创建时间
     */
    @TableField("maintenance_host_create_time")
    @Schema(description = "创建时间")
    private LocalDateTime maintenanceHostCreateTime;

    /**
     * 更新时间
     */
    @TableField("maintenance_host_update_time")
    @Schema(description = "更新时间")
    private LocalDateTime maintenanceHostUpdateTime;

    /**
     * 创建者
     */
    @TableField("create_by")
    @Schema(description = "创建者")
    private String createBy;

    /**
     * 更新者
     */
    @TableField("update_by")
    @Schema(description = "更新者")
    private String updateBy;
}