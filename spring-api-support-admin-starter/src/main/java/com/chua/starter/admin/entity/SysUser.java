package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.common.support.serialize.JsonArrayToStringDeserialize;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 用户表
 */
@ApiModel(description = "用户表")
@Schema(description = "用户表")
@Data
@TableName(value = "sys_user")
public class SysUser extends SysTenantBase {
    /**
     * 主键
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "主键不能为null")
    private Long userId;

    /**
     * 用户编号
     */
    @TableField(value = "user_code")
    @ApiModelProperty(value = "用户编号")
    @Schema(description = "用户编号")
    @Size(max = 255, message = "用户编号最大长度要小于 255")
    private String userCode;

    /**
     * 用户名
     */
    @TableField(value = "username")
    @ApiModelProperty(value = "用户名")
    @Schema(description = "用户名")
    @Size(max = 255, message = "用户名最大长度要小于 255")
    private String username;

    /**
     * 用户真实姓名
     */
    @TableField(value = "user_real_name")
    @ApiModelProperty(value = "用户真实姓名")
    @Schema(description = "用户真实姓名")
    @Size(max = 255, message = "用户真实姓名最大长度要小于 255")
    private String userRealName;

    /**
     * 用户备注
     */
    @TableField(value = "user_marker")
    @ApiModelProperty(value = "用户备注")
    @Schema(description = "用户备注")
    @Size(max = 255, message = "用户备注最大长度要小于 255")
    private String userMarker;

    /**
     * 昵称
     */
    @TableField(value = "user_nickname")
    @ApiModelProperty(value = "昵称")
    @Schema(description = "昵称")
    @Size(max = 255, message = "昵称最大长度要小于 255")
    private String userNickname;

    /**
     * 性别(1:男;2:女)
     */
    @TableField(value = "user_gender")
    @ApiModelProperty(value = "性别(1:男;0:女)")
    @Schema(description = "性别(1:男;0:女)")
    private Integer userGender;

    /**
     * 是否系统账号(1:是)
     */
    @TableField(value = "user_sys")
    @ApiModelProperty(value = " 是否系统账号(1:是)")
    @Schema(description = " 是否系统账号(1:是)")
    private Integer userSys;

    /**
     * 密码
     */
    @TableField(value = "user_password")
    @ApiModelProperty(value = "密码")
    @Schema(description = "密码")
    @Size(max = 255, message = "密码最大长度要小于 255")
    private String userPassword;

    /**
     * 注册IP
     */
    @TableField(value = "user_register_ip")
    @ApiModelProperty(value = "注册IP")
    @Schema(description = "注册IP")
    @Size(max = 255, message = "注册IP最大长度要小于 255")
    private String userRegisterIp;

    /**
     * 注册地址
     */
    @TableField(value = "user_register_address")
    @ApiModelProperty(value = "注册地址")
    @Schema(description = "注册地址")
    @Size(max = 255, message = "注册地址最大长度要小于 255")
    private String userRegisterAddress;

    /**
     * 部门ID
     */
    @TableField(value = "dept_id")
    @ApiModelProperty(value = "部门ID")
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 用户头像
     */
    @TableField(value = "user_avatar")
    @ApiModelProperty(value = "用户头像")
    @Schema(description = "用户头像")
    @Size(max = 255, message = "用户头像最大长度要小于 255")
    private String userAvatar;

    /**
     * 联系方式
     */
    @TableField(value = "user_mobile")
    @ApiModelProperty(value = "联系方式")
    @Schema(description = "联系方式")
    @Size(max = 255, message = "联系方式最大长度要小于 255")
    private String userMobile;

    /**
     * 住址
     */
    @TableField(value = "user_address")
    @ApiModelProperty(value = "住址")
    @Schema(description = "住址")
    @Size(max = 255, message = "住址最大长度要小于 255")
    private String userAddress;

    /**
     * 用户状态(1:正常;0:禁用)
     */
    @TableField(value = "user_status")
    @ApiModelProperty(value = "用户状态(1:正常;0:禁用)")
    @Schema(description = "用户状态(1:正常;0:禁用)")
    private Integer userStatus;

    /**
     * 用户邮箱
     */
    @TableField(value = "user_email")
    @ApiModelProperty(value = "用户邮箱")
    @Schema(description = "用户邮箱")
    @Size(max = 255, message = "用户邮箱最大长度要小于 255")
    private String userEmail;

    /**
     * 逻辑删除标识(0:未删除;1:已删除)
     */
    @TableField(value = "user_deleted")
    @ApiModelProperty(value = "逻辑删除标识(0:未删除;1:已删除)")
    @Schema(description = "逻辑删除标识(0:未删除;1:已删除)")
    private Integer userDeleted;

    /**
     * 盐
     */
    @TableField(value = "user_salt")
    @ApiModelProperty(value = "盐")
    @Schema(description = "盐")
    @Size(max = 255, message = "盐最大长度要小于 255")
    private String userSalt;

    /**
     * 密钥
     */
    @TableField(exist = false)
    private String userSeRan;
    /**
     * 角色ids
     */
    @TableField(exist = false)
    @Schema(description = "角色ids")
    @NotNull(message = "用户角色不能为空")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private String roleId;
    /**
     * 角色名称
     */
    @TableField(exist = false)
    @Schema(description = "角色名称")
    private String roleNames;
    /**
     * 部门名称
     */
    @TableField(exist = false)
    @Schema(description = "部门名称")
    private String deptName;
}