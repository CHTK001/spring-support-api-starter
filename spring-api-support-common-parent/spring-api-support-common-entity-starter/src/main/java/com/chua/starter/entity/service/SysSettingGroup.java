package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置组表
 *
 * @since 2025/9/10
 * @author CH
 */
@Schema(description = "配置组表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_setting_group")
public class SysSettingGroup extends SysBase {

    /**
      * 配置组标识
     */
    @TableId(value = "sys_setting_group_id", type = IdType.AUTO)
    @Schema(description = "配置组ID")
    /** SYS设置分组标识 */
    private Integer sysSettingGroupId;

    /**
     * 配置组名称
     */
    @TableField(value = "sys_setting_group_name")
    @Schema(description = "配置组名称")
    /** SYS设置分组名称 */
    private String sysSettingGroupName;

    /**
     * 配置组唯一编码
     */
    @TableField(value = "sys_setting_group_code")
    @Schema(description = "配置组唯一编码")
    /** SYS设置分组代码 */
    private String sysSettingGroupCode;

    /**
     * 配置组图标
     */
    @TableField(value = "sys_setting_group_icon")
    @Schema(description = "配置组图标")
    /** SYS设置分组icon */
    private String sysSettingGroupIcon;

    /**
     * 是否启用
     */
    @TableField(value = "sys_setting_group_enable")
    @Schema(description = "是否启用")
    /** SYS设置分组enable */
    private Boolean sysSettingGroupEnable;

    /**
     * 配置组描述
     */
    @TableField(value = "sys_setting_group_remark")
    @Schema(description = "配置组描述")
    /** SYS设置分组评论 */
    private String sysSettingGroupRemark;

    /**
     * 排序
     */
    @TableField(value = "sys_setting_group_sort")
    @Schema(description = "排序")
    /** SYS设置分组排序 */
    private Integer sysSettingGroupSort;

    /**
     * 是否使用项目组接口
     */
    @TableField(value = "sys_setting_group_use_project_interface")
    @Schema(description = "是否使用项目组接口")
    /** SYS设置分组useproject接口 */
    private Boolean sysSettingGroupUseProjectInterface;
}
