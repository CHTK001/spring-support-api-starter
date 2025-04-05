package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护组实体类
 * 用于定义维护管理中的组信息
 *
 * @author CH
 */
@Data
@TableName("maintenance_group")
@Schema(description = "维护组")
public class MaintenanceGroup {

    /**
     * 维护组ID，主键
     */
    @TableId(value = "maintenance_group_id", type = IdType.AUTO)
    @Schema(description = "维护组ID")
    private Long maintenanceGroupId;

    /**
     * 维护组名称
     */
    @TableField("maintenance_group_name")
    @Schema(description = "维护组名称")
    private String maintenanceGroupName;

    /**
     * 维护组描述
     */
    @TableField("maintenance_group_description")
    @Schema(description = "维护组描述")
    private String maintenanceGroupDescription;

    /**
     * 是否启用
     */
    @TableField("maintenance_group_enabled")
    @Schema(description = "状态;0:停用;1:启用")
    private Boolean maintenanceGroupEnabled;

    /**
     * 创建时间
     */
    @TableField("maintenance_group_create_time")
    @Schema(description = "创建时间")
    private LocalDateTime maintenanceGroupCreateTime;

    /**
     * 更新时间
     */
    @TableField("maintenance_group_update_time")
    @Schema(description = "更新时间")
    private LocalDateTime maintenanceGroupUpdateTime;

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