package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.datasource.annotation.ColumnLength;
import com.chua.common.support.validator.group.AddGroup;
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
import java.util.List;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */

/**
 * 用户表
 */
@ApiModel(description = "用户表")
@Schema(description = "用户表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user")
public class SysUser extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_user_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysUserId;

    /**
     * 账号
     */
    @TableField(value = "sys_user_username")
    @ApiModelProperty(value = "账号")
    @Schema(description = "账号")
    @Size(max = 255, message = "账号最大长度要小于 255")
    @NotEmpty(message = "账号不能为空", groups = {AddGroup.class})
    private String sysUserUsername;

    /**
     * 名称
     */
    @TableField(value = "sys_user_nickname")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    @Size(max = 255, message = "名称最大长度要小于 255")
    private String sysUserNickname;
    /**
     * 名称
     */
    @TableField(value = "sys_user_full_address")
    @ApiModelProperty(value = "居住地址")
    @Schema(description = "居住地址")
    @ColumnLength(400)
    @Size(max = 400, message = "居住地址最大长度要小于 400")
    private String sysUserFullAddress;

    /**
     * 密码
     */
    @TableField(value = "sys_user_password")
    @ApiModelProperty(value = "密码")
    @Schema(description = "密码")
    @Size(max = 255, message = "密码最大长度要小于 255")
    @NotEmpty(message = "密码不能为空", groups = {AddGroup.class})
    private String sysUserPassword;


    /**
     * 头像
     */
    @TableField(value = "sys_user_avatar")
    @ApiModelProperty(value = "头像")
    @Schema(description = "头像")
    @ColumnLength(4000)
    @Size(max = 4000, message = "头像最大长度要小于 4000")
    private String sysUserAvatar;

    /**
     * 盐
     */
    @TableField(value = "sys_user_salt")
    @ApiModelProperty(value = "盐")
    @Schema(description = "盐")
    @Size(max = 255, message = "盐最大长度要小于 255")
    private String sysUserSalt;

    /**
     * TOTP
     */
    @TableField(value = "sys_user_totp_code")
    @ApiModelProperty(value = "TOTP")
    @Schema(description = "TOTP")
    private String sysUserTotpCode;


    /**
     * 是否删除; 0: 未删除
     */
    @TableField(value = "sys_user_delete")
    @ApiModelProperty(value = "是否删除; 0: 未删除")
    @Schema(description = "是否删除; 0: 未删除")
    @TableLogic(value = "0", delval = "1")
    private Integer sysUserDelete;


    /**
     * 部门ID
     */
    @TableField(value = "sys_dept_id")
    @ApiModelProperty(value = "部门ID")
    @Schema(description = "部门ID")
    private Integer sysDeptId;

    /**
     * 是否启用；0：禁用
     */
    @TableField(value = "sys_user_status")
    @ApiModelProperty(value = "是否启用；0：禁用")
    @Schema(description = "是否启用；0：禁用")
    private Integer sysUserStatus;

    /**
     * 手机号
     */
    @TableField(value = "sys_user_phone")
    @ApiModelProperty(value = "手机号")
    @Schema(description = "手机号")
    @Size(max = 255, message = "手机号最大长度要小于 255")
    private String sysUserPhone;
    /**
     * 邮箱
     */
    @TableField(value = "sys_user_email")
    @ApiModelProperty(value = "邮箱")
    @Schema(description = "邮箱")
    @Size(max = 255, message = "邮箱最大长度要小于 255")
    private String sysUserEmail;

    /**
     * 性别; 0:女;1:男; 2:其他
     */
    @TableField(value = "sys_user_sex")
    @ApiModelProperty(value = "性别; 0:女;1:男; 2:其他")
    @Schema(description = "性别; 0:女;1:男; 2:其他")
    private Integer sysUserSex;

    /**
     * 登录次数
     */
    @TableField(value = "sys_user_login_count")
    @ApiModelProperty(value = "登录次数")
    @Schema(description = "登录次数")
    private Integer sysUserLongCount;

    /**
     * 是否系统账号; 0: 非系统账号
     */
    @TableField(value = "sys_user_in_system")
    @ApiModelProperty(value = "是否系统账号; 0: 非系统账号")
    @Schema(description = "是否系统账号; 0: 非系统账号")
    private Integer sysUserInSystem;

    /**
     * 身份证
     */
    @TableField(value = "sys_user_card")
    @ApiModelProperty(value = "身份证")
    @Schema(description = "身份证")
    @Size(max = 255, message = "身份证最大长度要小于 255")
    private String sysUserCard;

    /**
     * 备注
     */
    @TableField(value = "sys_user_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度要小于 255")
    private String sysUserRemark;

    /**
     * 最后一次登录地点
     */
    @TableField(value = "sys_user_last_address")
    @ApiModelProperty(value = "最后一次登录地点")
    @Schema(description = "最后一次登录地点")
    @Size(max = 255, message = "最后一次登录地点最大长度要小于 255")
    private String sysUserLastAddress;

    /**
     * 最后一次登录IP
     */
    @TableField(value = "sys_user_last_ip")
    @ApiModelProperty(value = "最后一次登录IP")
    @Schema(description = "最后一次登录IP")
    @Size(max = 255, message = "最后一次登录IP最大长度要小于 255")
    private String sysUserLastIp;
    /**
     * 注册IP
     */
    @TableField(value = "sys_user_register_ip")
    @ApiModelProperty(value = "注册IP")
    @Schema(description = "注册IP")
    @Size(max = 255, message = "注册IP最大长度要小于 255")
    private String sysUserRegisterIp;

    /**
     * 客户端ID
     */
    @TableField(value = "sys_client_id")
    @ApiModelProperty(value = "客户端ID")
    @Schema(description = "客户端ID")
    private String sysClientId;


    /**
     * TOTP
     */
    @ColumnLength(600)
    @TableField(value = "sys_user_totp")
    @ApiModelProperty(value = "TOTP")
    @Schema(description = "TOTP")
    private String sysUserTotp;


    /**
     * 角色名称
     */
    @Schema(description = "角色(系统生成)")
    @TableField(exist = false)
    private List<SysRole> userRoles;
}