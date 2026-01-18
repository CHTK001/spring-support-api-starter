package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 角色表
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "角色表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role")
public class SysRole extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(value = "sys_role_id", type = IdType.AUTO)
    @Schema(description = "角色ID")
    @NotNull(message = "角色ID不能为空", groups = {UpdateGroup.class})
    private Integer sysRoleId;

    /**
     * 角色名称
     */
    @TableField(value = "sys_role_name")
    @Schema(description = "角色名称")
    @Size(max = 255, message = "角色名称最大长度需小于255")
    private String sysRoleName;

    /**
     * 角色编码，唯一
     */
    @TableField(value = "sys_role_code")
    @Schema(description = "角色编码，唯一")
    @Size(max = 255, message = "角色编码最大长度需小于255")
    @NotEmpty(message = "角色编码不能为空", groups = {AddGroup.class})
    private String sysRoleCode;

    /**
     * 是否启用; 0:不启用
     */
    @TableField(value = "sys_role_status")
    @Schema(description = "是否启用; 0:不启用")
    private Integer sysRoleStatus;

    /**
     * 优先级
     */
    @TableField(value = "sys_role_sort")
    @Schema(description = "优先级")
    private Integer sysRoleSort;

    /**
     * 是否系统角色; 0:非系统角色
     */
    @TableField(value = "sys_role_in_system")
    @Schema(description = "是否系统角色; 0:非系统角色")
    private Integer sysRoleInSystem;

    /**
     * 备注
     */
    @TableField(value = "sys_role_remark")
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度需小于255")
    private String sysRoleRemark;

    /**
     * 角色是否可读
     */
    @TableField(value = "sys_role_readable")
    @Schema(description = "角色是否可读")
    private Integer sysRoleReadable = 0x0000_0001;

    /**
     * 角色是否可写
     */
    @TableField(value = "sys_role_writeable")
    @Schema(description = "角色是否可写")
    private Integer sysRoleWriteable;

    /**
     * 角色是否可执行
     */
    @TableField(value = "sys_role_executable")
    @Schema(description = "角色是否可执行")
    private Integer sysRoleExecutable;

    /**
     * 角色看板类型
     */
    @TableField(value = "sys_role_board_card")
    @Schema(description = "角色看板类型")
    private Integer sysRoleBoardCard;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableField(exist = false)
    private Integer sysUserId;
}
