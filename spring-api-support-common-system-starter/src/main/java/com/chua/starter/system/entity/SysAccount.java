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
 * 璐﹀彿绠＄悊
 *
 * @author CH
 * @since 2024/8/7
 */
@Schema(description = "鐢ㄦ埛绠＄悊")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_account")
public class SysAccount extends SysBase {

    @TableId(value = "sys_account_id", type = IdType.AUTO)
    @Schema(description = "璐﹀彿ID")
    private Integer sysAccountId;

    /**
     * 璐﹀彿绫诲瀷; wechat
     */
    @TableField(value = "sys_account_type")
    @Schema(description = "璐﹀彿绫诲瀷; wechat")
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
     * 鍚嶇О
     */
    @TableField(value = "sys_account_name")
    @Schema(description = "鍚嶇О")
    private String sysAccountName;

    /**
     * 澶村儚
     */
    @TableField(value = "sys_account_avatar")
    @Schema(description = "澶村儚")
    private String sysAccountAvatar;

    /**
     * 鎵嬫満鍙"     */
    @TableField(value = "sys_account_phone")
    @Schema(description = "鎵嬫満鍙")
    private String sysAccountPhone;

    /**
     * 閭
     */
    @TableField(value = "sys_account_email")
    @Schema(description = "閭")
    private String sysAccountEmail;

    /**
     * 鏄惁绂佺敤
     */
    @TableField(value = "sys_account_forbidden")
    @Schema(description = "鏄惁绂佺敤")
    private Boolean sysAccountForbidden;

    /**
     * 鐧诲綍娆℃暟
     */
    @TableField(value = "sys_account_login_count")
    @Schema(description = "鐧诲綍娆℃暟")
    private Integer sysAccountLoginCount;

    /**
     * 鏈€鍚庣櫥褰昳p
     */
    @TableField(value = "sys_account_last_ip")
    @Schema(description = "鏈€鍚庣櫥褰昳p")
    private String sysAccountLastIp;

    /**
     * 鐧诲綍鍦板潃
     */
    @TableField(value = "sys_account_last_ip_address")
    @Schema(description = "鐧诲綍鍦板潃")
    private String sysAccountLastIpAddress;
}