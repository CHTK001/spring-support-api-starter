package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 应用密钥实体类
 *
 * @author CH
 * @since 2024/8/7
 */
@Data
@TableName("sys_app_key")
@Schema(description = "应用密钥")
public class SysAppKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer sysAppKeyId;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String sysAppKeyAppId;

    /**
     * 应用密钥
     */
    @Schema(description = "应用密钥")
    private String sysAppKeySecret;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String sysAppKeyName;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Integer sysUserId;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer sysAppKeyStatus;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @Schema(description = "删除标记：0-未删除，1-已删除")
    private Integer sysAppKeyDelete;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    private Long sysAppKeyExpireTime;

    /**
     * IP白名单
     */
    @Schema(description = "IP白名单")
    private String sysAppKeyIpWhitelist;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String sysAppKeyRemark;

    /**
     * 最后使用时间
     */
    @Schema(description = "最后使用时间")
    private Long sysAppKeyLastUsedTime;

    /**
     * 使用次数
     */
    @Schema(description = "使用次数")
    private Integer sysAppKeyUseCount;

    /**
     * 创建人名称
     */
    @Schema(description = "创建人名称")
    private String createName;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Integer createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 更新人名称
     */
    @Schema(description = "更新人名称")
    private String updateName;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private Integer updateBy;

    /**
     * 允许的权限范围
     * <p>
     * 多个 scope 用逗号分隔，如：read,write,admin
     * </p>
     */
    @Schema(description = "允许的权限范围，多个用逗号分隔")
    private String sysAppKeyScope;

    /**
     * 允许的授权类型
     * <p>
     * 多个用逗号分隔，如：client_credentials,password,authorization_code,refresh_token
     * </p>
     */
    @Schema(description = "允许的授权类型，多个用逗号分隔")
    private String sysAppKeyGrantTypes;

    /**
     * 允许的回调地址
     * <p>
     * 多个用逗号分隔，用于 authorization_code 模式的 redirect_uri 校验
     * </p>
     */
    @Schema(description = "允许的回调地址，多个用逗号分隔")
    private String sysAppKeyRedirectUris;
}
