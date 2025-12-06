package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */

/**
 * 角色表
 */
@ApiModel(description = "角色表")
@Schema(description = "角色表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role")
public class SysRole extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_role_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "角色ID不能为空", groups = {UpdateGroup.class})
    private Integer sysRoleId;

    /**
     * 角色名称
     */
    @TableField(value = "sys_role_name")
    @ApiModelProperty(value = "角色名称")
    @Schema(description = "角色名称")
    @Size(max = 255, message = "角色名称最大长度要小于 255")
    private String sysRoleName;

    /**
     * 角色编码; 唯一
     */
    @TableField(value = "sys_role_code")
    @ApiModelProperty(value = "角色编码; 唯一")
    @Schema(description = "角色编码; 唯一")
    @Size(max = 255, message = "角色编码; 唯一最大长度要小于 255")
    @NotEmpty(message = "角色编码; 唯一不能为空", groups = {AddGroup.class})
    private String sysRoleCode;

    /**
     * 是否启用
     */
    @TableField(value = "sys_role_status")
    @ApiModelProperty(value = "是否启用; 0：不启用")
    @Schema(description = "是否启用; 0：不启用")
    private Integer sysRoleStatus;
    /**
     * 优先级
     */
    @TableField(value = "sys_role_sort")
    @ApiModelProperty(value = "优先级")
    @Schema(description = "优先级")
    private Integer sysRoleSort;

    /**
     * 是否系统角色; 0：非系统角色
     */
    @TableField(value = "sys_role_in_system")
    @ApiModelProperty(value = "是否系统角色; 0：非系统角色")
    @Schema(description = "是否系统角色; 0：非系统角色")
    private Integer sysRoleInSystem;

    /**
     * 备注
     */
    @TableField(value = "sys_role_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度要小于 255")
    private String sysRoleRemark;

    /**
     * 角色是否可读
     */
    @TableField(value = "sys_role_readable")
    @ApiModelProperty(value = "角色是否可读")
    @Schema(description = "角色是否可读")
    private Integer sysRoleReadable = 0x0000_0001;
    /**
     * 角色是否可写
     */
    @TableField(value = "sys_role_writeable")
    @ApiModelProperty(value = "角色是否可写")
    @Schema(description = "角色是否可写")
    private Integer sysRoleWriteable;
    /**
     * 角色是否可执行
     */
    @TableField(value = "sys_role_executable")
    @ApiModelProperty(value = "角色是否可执行")
    @Schema(description = "角色是否可执行")
    private Integer sysRoleExecutable;
    /**
     * 角色可看板类型
     */
    @TableField(value = "sys_role_board_card")
    @ApiModelProperty(value = "角色可看板类型")
    @Schema(description = "角色可看板类型")
    private Integer sysRoleBoardCard;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableField(exist = false)
    private Integer sysUserId;
}