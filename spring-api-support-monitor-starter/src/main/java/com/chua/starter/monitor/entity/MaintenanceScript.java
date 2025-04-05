package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护脚本实体类
 * 定义维护管理中的脚本信息
 *
 * @author CH
 */
@Data
@TableName("t_maintenance_script")
@Schema(description = "维护脚本")
public class MaintenanceScript {

    /**
     * 脚本ID，主键
     */
    @TableId(value = "maintenance_script_id", type = IdType.AUTO)
    @Schema(description = "脚本ID")
    private Long maintenanceScriptId;

    /**
     * 维护组ID
     */
    @TableField("maintenance_group_id")
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 脚本名称
     */
    @TableField("maintenance_script_name")
    @Schema(description = "脚本名称")
    private String maintenanceScriptName;

    /**
     * 脚本内容
     */
    @TableField("maintenance_script_content")
    @Schema(description = "脚本内容")
    private String maintenanceScriptContent;

    /**
     * 脚本保存路径
     */
    @TableField("maintenance_script_path")
    @Schema(description = "脚本路径")
    private String maintenanceScriptPath;

    /**
     * 脚本描述
     */
    @TableField("maintenance_script_desc")
    @Schema(description = "脚本描述")
    private String maintenanceScriptDesc;

    /**
     * 脚本状态：0-停用，1-启用
     */
    @TableField("maintenance_script_status")
    @Schema(description = "状态;0:停用;1:启用")
    private Integer maintenanceScriptStatus;

    /**
     * 创建时间
     */
    @TableField("maintenance_script_create_time")
    @Schema(description = "创建时间")
    private LocalDateTime maintenanceScriptCreateTime;

    /**
     * 更新时间
     */
    @TableField("maintenance_script_update_time")
    @Schema(description = "更新时间")
    private LocalDateTime maintenanceScriptUpdateTime;

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