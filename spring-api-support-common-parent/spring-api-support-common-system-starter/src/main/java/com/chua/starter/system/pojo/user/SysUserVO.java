package com.chua.starter.system.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.starter.system.validator.group.AddGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.chua.starter.entity.system.SysRole;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象
 *
 * @author CH
 * @since 2024/8/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户视图对象")
public class SysUserVO extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "sys_user_id", type = IdType.AUTO)
    @ApiModelProperty(value = "用户ID")
    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Integer sysUserId;

    /**
     * 账号
     */
    @TableField(value = "sys_user_username")
    @ApiModelProperty(value = "账号")
    @Schema(description = "账号")
    @Size(max = 255, message = "账号最大长度需小于255")
    @NotEmpty(message = "账号不能为空", groups = {AddGroup.class})
    private String sysUserUsername;

    /**
     * 名称
     */
    @TableField(value = "sys_user_nickname")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    @Size(max = 255, message = "名称最大长度需小于255")
    private String sysUserNickname;

    /**
     * 居住地址
     */
    @TableField(value = "sys_user_full_address")
    @ApiModelProperty(value = "居住地址")
    @Schema(description = "居住地址")
    @Size(max = 400, message = "居住地址最大长度需小于400")
    private String sysUserFullAddress;

    /**
     * 头像
     */
    @TableField(value = "sys_user_avatar")
    @ApiModelProperty(value = "头像")
    @Schema(description = "头像")
    @Size(max = 4000, message = "头像最大长度需小于4000")
    private String sysUserAvatar;

    /**
     * 部门ID
     */
    @TableField(value = "sys_dept_id")
    @ApiModelProperty(value = "部门ID")
    @Schema(description = "部门ID")
    private Integer sysDeptId;

    /**
     * 是否启用: 0:禁用
     */
    @TableField(value = "sys_user_status")
    @ApiModelProperty(value = "是否启用: 0:禁用")
    @Schema(description = "是否启用: 0:禁用")
    private Integer sysUserStatus;

    /**
     * 手机号
     */
    @TableField(value = "sys_user_phone")
    @ApiModelProperty(value = "手机号")
    @Schema(description = "手机号")
    @Size(max = 255, message = "手机号最大长度需小于255")
    private String sysUserPhone;

    /**
     * 邮箱
     */
    @TableField(value = "sys_user_email")
    @ApiModelProperty(value = "邮箱")
    @Schema(description = "邮箱")
    @Size(max = 255, message = "邮箱最大长度需小于255")
    private String sysUserEmail;

    /**
     * 性别: 0:女 1:男 2:其他
     */
    @TableField(value = "sys_user_sex")
    @ApiModelProperty(value = "性别: 0:女 1:男 2:其他")
    @Schema(description = "性别: 0:女 1:男 2:其他")
    private Integer sysUserSex;

    /**
     * 是否系统账号: 0:非系统账号
     */
    @TableField(value = "sys_user_in_system")
    @ApiModelProperty(value = "是否系统账号: 0:非系统账号")
    @Schema(description = "是否系统账号: 0:非系统账号")
    private Integer sysUserInSystem;

    /**
     * 身份证
     */
    @TableField(value = "sys_user_card")
    @ApiModelProperty(value = "身份证")
    @Schema(description = "身份证")
    @Size(max = 255, message = "身份证最大长度需小于255")
    private String sysUserCard;

    /**
     * 备注
     */
    @TableField(value = "sys_user_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度需小于255")
    private String sysUserRemark;

    /**
     * 最后一次登录地点
     */
    @TableField(value = "sys_user_last_address")
    @ApiModelProperty(value = "最后一次登录地点")
    @Schema(description = "最后一次登录地点")
    @Size(max = 255, message = "最后一次登录地点最大长度需小于255")
    private String sysUserLastAddress;

    /**
     * 最后一次登录IP
     */
    @TableField(value = "sys_user_last_ip")
    @ApiModelProperty(value = "最后一次登录IP")
    @Schema(description = "最后一次登录IP")
    @Size(max = 255, message = "最后一次登录IP最大长度需小于255")
    private String sysUserLastIp;

    /**
     * 注册IP
     */
    @TableField(value = "sys_user_register_ip")
    @ApiModelProperty(value = "注册IP")
    @Schema(description = "注册IP")
    @Size(max = 255, message = "注册IP最大长度需小于255")
    private String sysUserRegisterIp;

    /**
     * 注册地点
     */
    @TableField(value = "sys_user_register_address")
    @ApiModelProperty(value = "注册地点")
    @Schema(description = "注册地点")
    @Size(max = 255, message = "注册地点最大长度需小于255")
    private String sysUserRegisterAddress;

    /**
     * 最后登录时间
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "最后登录时间")
    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime sysUserLastLoginTime;

    /**
     * 角色列表(系统生成)
     */
    @Schema(description = "角色列表(系统生成)")
    @TableField(exist = false)
    private List<SysRole> userRoles;

    /**
     * 生日
     */
    @TableField(value = "sys_user_birthday")
    @ApiModelProperty(value = "生日")
    @Schema(description = "生日")
    private String sysUserBirthday;

    /**
     * 头像框ID
     */
    @TableField(value = "sys_user_avatar_frame")
    @ApiModelProperty(value = "头像框ID")
    @Schema(description = "头像框ID")
    private Integer sysUserAvatarFrame;
}
