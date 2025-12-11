package com.chua.starter.oauth.sso.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AppKey Token实体
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Data
@TableName("sys_token")
@Schema(description = "AppKey Token实体")
public class SysToken {

    /**
     * Token ID
     */
    @TableId(value = "sys_token_id", type = IdType.AUTO)
    @Schema(description = "Token ID")
    private Integer sysTokenId;

    /**
     * AppKey（应用标识）
     */
    @TableField(value = "sys_token_app_key")
    @Schema(description = "AppKey")
    private String sysTokenAppKey;

    /**
     * AppSecret（应用密钥）
     */
    @TableField(value = "sys_token_app_secret")
    @Schema(description = "AppSecret")
    private String sysTokenAppSecret;

    /**
     * 应用名称
     */
    @TableField(value = "sys_token_app_name")
    @Schema(description = "应用名称")
    private String sysTokenAppName;

    /**
     * 关联用户ID
     */
    @TableField(value = "sys_token_user_id")
    @Schema(description = "关联用户ID")
    private String sysTokenUserId;

    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField(value = "sys_token_status")
    @Schema(description = "状态")
    private Integer sysTokenStatus;

    /**
     * 过期时间
     */
    @TableField(value = "sys_token_expire_time")
    @Schema(description = "过期时间")
    private LocalDateTime sysTokenExpireTime;

    /**
     * 创建时间
     */
    @TableField(value = "sys_token_create_time")
    @Schema(description = "创建时间")
    private LocalDateTime sysTokenCreateTime;

    /**
     * 更新时间
     */
    @TableField(value = "sys_token_update_time")
    @Schema(description = "更新时间")
    private LocalDateTime sysTokenUpdateTime;

    /**
     * 备注
     */
    @TableField(value = "sys_token_remark")
    @Schema(description = "备注")
    private String sysTokenRemark;
}
