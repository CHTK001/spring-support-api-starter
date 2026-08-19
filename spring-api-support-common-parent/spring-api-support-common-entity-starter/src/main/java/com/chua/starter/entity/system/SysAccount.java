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
 * @since 2024/8/7
 * @author CH
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
    /** SYSaccountID */
    private Integer sysAccountId;

    /**
     * 账号类型，如: wechat、alipay
     */
    @TableField(value = "sys_account_type")
    @Schema(description = "账号类型，如: wechat、alipay")
    /** SYSaccount类型 */
    private String sysAccountType;

    /**
     * 第三方OpenID
     */
    @TableField(value = "sys_account_openid")
    @Schema(description = "第三方OpenID")
    /** SYSaccountopenid */
    private String sysAccountOpenid;

    /**
     * 第三方UnionID
     */
    @TableField(value = "sys_account_unionid")
    @Schema(description = "第三方UnionID")
    /** SYSaccountunionid */
    private String sysAccountUnionid;

    /**
     * 账号名称/昵称
     */
    @TableField(value = "sys_account_name")
    @Schema(description = "账号名称/昵称")
    /** SYSaccount名称 */
    private String sysAccountName;

    /**
     * 头像URL
     */
    @TableField(value = "sys_account_avatar")
    @Schema(description = "头像URL")
    /** SYSaccountavatar */
    private String sysAccountAvatar;

    /**
     * 手机号
     */
    @TableField(value = "sys_account_phone")
    @Schema(description = "手机号")
    /** SYSaccountphone */
    private String sysAccountPhone;

    /**
     * 邮箱
     */
    @TableField(value = "sys_account_email")
    @Schema(description = "邮箱")
    /** SYSaccountemail */
    private String sysAccountEmail;

    /**
     * 是否禁用: true=禁用
     */
    @TableField(value = "sys_account_forbidden")
    @Schema(description = "是否禁用: true=禁用")
    /** SYSaccountforbidden */
    private Boolean sysAccountForbidden;

    /**
     * 登录次数
     */
    @TableField(value = "sys_account_login_count")
    @Schema(description = "登录次数")
    /** SYSaccountlogin数量 */
    private Integer sysAccountLoginCount;

    /**
     * 最后登录IP
     */
    @TableField(value = "sys_account_last_ip")
    @Schema(description = "最后登录IP")
    /** SYSaccount最后IP */
    private String sysAccountLastIp;

    /**
     * 最后登录地址
     */
    @TableField(value = "sys_account_last_ip_address")
    @Schema(description = "最后登录地址")
    /** SYSaccount最后IP地址 */
    private String sysAccountLastIpAddress;

    /**
     * 绑定的本地用户 ID(0 或 null 表示未绑定)
     */
    @TableField(value = "sys_account_user_id")
    @Schema(description = "绑定的本地用户 ID")
    /** SYSaccount用户ID */
    private Integer sysAccountUserId;
}
