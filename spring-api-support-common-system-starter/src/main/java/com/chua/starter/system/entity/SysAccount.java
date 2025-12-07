package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号管理
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "用户管理")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_account")
public class SysAccount extends SysBase {

    @TableId(value = "sys_account_id", type = IdType.AUTO)
    @Schema(description = "账号ID")
    private Integer sysAccountId;

    /**
     * 账号类型; wechat
     */
    @TableField(value = "sys_account_type")
    @Schema(description = "账号类型; wechat")
    private String sysAccountType;

    /**
     * open_id
     */
    @TableField(value = "sys_account_openid")
    @Schema(description = "open_id")
    private String sysAccountOpenid;

    /**
     * union_id
     */
    @TableField(value = "sys_account_unionid")
    @Schema(description = "union_id")
    private String sysAccountUnionid;

    /**
     * 名称
     */
    @TableField(value = "sys_account_name")
    @Schema(description = "名称")
    private String sysAccountName;

    /**
     * 头像
     */
    @TableField(value = "sys_account_avatar")
    @Schema(description = "头像")
    private String sysAccountAvatar;

    /**
     * 手机号
     */
    @TableField(value = "sys_account_phone")
    @Schema(description = "手机号")
    private String sysAccountPhone;

    /**
     * 邮箱
     */
    @TableField(value = "sys_account_email")
    @Schema(description = "邮箱")
    private String sysAccountEmail;

    /**
     * 是否禁用
     */
    @TableField(value = "sys_account_forbidden")
    @Schema(description = "是否禁用")
    private Boolean sysAccountForbidden;

    /**
     * 登录次数
     */
    @TableField(value = "sys_account_login_count")
    @Schema(description = "登录次数")
    private Integer sysAccountLoginCount;

    /**
     * 最后登录ip
     */
    @TableField(value = "sys_account_last_ip")
    @Schema(description = "最后登录ip")
    private String sysAccountLastIp;

    /**
     * 登录地址
     */
    @TableField(value = "sys_account_last_ip_address")
    @Schema(description = "登录地址")
    private String sysAccountLastIpAddress;
}