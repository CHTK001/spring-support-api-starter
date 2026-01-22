package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务器分组实体类
 *
 * @author CH
 * @since 2025/01/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_sys_gen_server_group")
@ApiModel(value = "MonitorSysGenServerGroup", description = "服务器分组")
@Schema(description = "服务器分组")
public class MonitorSysGenServerGroup extends SysBase {

    /**
     * 分组ID
     */
    @TableId(value = "monitor_sys_gen_server_group_id", type = IdType.AUTO)
    @ApiModelProperty(value = "分组ID")
    @Schema(description = "分组ID")
    @NotNull(groups = {UpdateGroup.class}, message = "分组ID不能为空")
    private Integer monitorSysGenServerGroupId;

    /**
     * 分组名称
     */
    @TableField(value = "monitor_sys_gen_server_group_name")
    @ApiModelProperty(value = "分组名称")
    @Schema(description = "分组名称")
    @NotBlank(groups = {AddGroup.class, UpdateGroup.class}, message = "分组名称不能为空")
    @Size(max = 100, message = "分组名称最大长度要小于 100")
    private String monitorSysGenServerGroupName;

    /**
     * 分组描述
     */
    @TableField(value = "monitor_sys_gen_server_group_desc")
    @ApiModelProperty(value = "分组描述")
    @Schema(description = "分组描述")
    @Size(max = 500, message = "分组描述最大长度要小于 500")
    private String monitorSysGenServerGroupDesc;

    /**
     * 分组颜色
     */
    @TableField(value = "monitor_sys_gen_server_group_color")
    @ApiModelProperty(value = "分组颜色")
    @Schema(description = "分组颜色(十六进制颜色值)")
    @Size(max = 20, message = "分组颜色最大长度要小于 20")
    private String monitorSysGenServerGroupColor;

    /**
     * 分组图标
     */
    @TableField(value = "monitor_sys_gen_server_group_icon")
    @ApiModelProperty(value = "分组图标")
    @Schema(description = "分组图标")
    @Size(max = 100, message = "分组图标最大长度要小于 100")
    private String monitorSysGenServerGroupIcon;

    /**
     * 分组状态 0:禁用 1:启用
     */
    @TableField(value = "monitor_sys_gen_server_group_status")
    @ApiModelProperty(value = "分组状态")
    @Schema(description = "分组状态 0:禁用 1:启用")
    private Integer monitorSysGenServerGroupStatus;

    /**
     * 是否默认分组 0:否 1:是
     */
    @TableField(value = "monitor_sys_gen_server_group_is_default")
    @ApiModelProperty(value = "是否默认分组")
    @Schema(description = "是否默认分组 0:否 1:是")
    private Integer monitorSysGenServerGroupIsDefault;

    /**
     * 排序号
     */
    @TableField(value = "monitor_sys_gen_server_group_sort")
    @ApiModelProperty(value = "排序号")
    @Schema(description = "排序号")
    private Integer monitorSysGenServerGroupSort;

    /**
     * 备注
     */
    @TableField(value = "monitor_sys_gen_server_group_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 1000, message = "备注最大长度要小于 1000")
    private String monitorSysGenServerGroupRemark;
}
