package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户账号管理表
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "用户账号管理表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_account")
public class SysAccount extends SysBase {

    /**
     * 账号ID
     */
    @TableId(value = "sys_account_id", type = IdType.AUTO)
    @Schema(description = "账号ID")
    private Integer sysAccountId;

    /**
     * 账号类型，如: wechat、alipay
     */
    @TableField(value = "sys_account_type")
    @Schema(description = "账号类型，如: wechat、alipay")
    private String sysAccountType;

    /**
     * 第三方OpenID
     */
    @TableField(value = "sys_account_openid")
    @Schema(description = "第三方OpenID")
    private String sysAccountOpenid;

    /**
     * 第三方UnionID
     */
    @TableField(value = "sys_account_unionid")
    @Schema(description = "第三方UnionID")
    private String sysAccountUnionid;

    /**
     * 账号名称/昵称
     */
    @TableField(value = "sys_account_name")
    @Schema(description = "账号名称/昵称")
    private String sysAccountName;

    /**
     * 头像URL
     */
    @TableField(value = "sys_account_avatar")
    @Schema(description = "头像URL")
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
     * 是否禁用: true=禁用
     */
    @TableField(value = "sys_account_forbidden")
    @Schema(description = "是否禁用: true=禁用")
    private Boolean sysAccountForbidden;

    /**
     * 登录次数
     */
    @TableField(value = "sys_account_login_count")
    @Schema(description = "登录次数")
    private Integer sysAccountLoginCount;

    /**
     * 最后登录IP
     */
    @TableField(value = "sys_account_last_ip")
    @Schema(description = "最后登录IP")
    private String sysAccountLastIp;

    /**
     * 最后登录地址
     */
    @TableField(value = "sys_account_last_ip_address")
    @Schema(description = "最后登录地址")
    private String sysAccountLastIpAddress;
}
