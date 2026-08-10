package com.chua.starter.system.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.starter.system.validator.group.AddGroup;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 用户登录视图对象
 *
 * @author haoxr
 * @version 1.0.0
 * @since 2022/1/14
 */
@Schema(description = "当前登录用户视图对象")
@Data
public class UserInfoVO {

    /**
     * 用户ID
     */
    @TableId(value = "sys_user_id", type = IdType.AUTO)
    @ApiModelProperty(value = "用户ID")
    @Schema(description = "用户ID")
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
     * 头像地址
     */
    @Schema(description = "头像地址")
    private String avatar;

    /**
     * 头像框ID
     */
    @Schema(description = "头像框ID")
    private Integer sysUserAvatarFrame;

    /**
     * 生日
     */
    @Schema(description = "生日")
    private String sysUserBirthday;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "主部门ID")
    private Integer sysDeptId;

    @Schema(description = "负责部门ID集合")
    private List<Integer> managedDeptIds;

    /**
     * 用户角色编码集合
     */
    @Schema(description = "用户角色编码集合")
    private Set<String> roles;

    /**
     * 用户权限标识集合
     */
    @Schema(description = "用户权限标识集合")
    private Set<String> perms;

    @Schema(description = "当前用户协议版本")
    private String agreementVersion;

    @Schema(description = "当前用户协议更新时间")
    private String agreementUpdatedAt;

    @Schema(description = "用户已确认的协议版本")
    private String agreementAcceptedVersion;

    @Schema(description = "用户确认协议时间")
    private String agreementAcceptedAt;

    @Schema(description = "是否需要确认最新协议")
    private Boolean agreementNeedConfirm;
}
