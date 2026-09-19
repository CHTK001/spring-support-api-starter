package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2024/8/7
 * @author CH
 */
@Data
@TableName("sys_app_key")
@Schema(description = "应用密钥")
public class SysAppKey implements Serializable {

    @Serial
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
     * 主键标识
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    /** SYSAPP密钥标识 */
    private Integer sysAppKeyId;

    /**
     * 应用标识
     */
    @Schema(description = "应用ID")
    /** SYSAPP密钥APPID */
    private String sysAppKeyAppId;

    /**
     * app键
     */
    @TableField("app_key")
    @Schema(description = "AppKey")
    /** SYSAPP密钥APP密钥 */
    private String sysAppKeyAppKey;

    /**
     * 应用密钥
     */
    @Schema(description = "应用密钥")
    /** SYSAPP密钥secret */
    private String sysAppKeySecret;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    /** SYSAPP密钥名称 */
    private String sysAppKeyName;

    /**
     * 用户标识
     */
    @Schema(description = "用户ID")
    /** SYS用户标识 */
    private Integer sysUserId;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用")
    /** SYSAPP密钥状态 */
    private Integer sysAppKeyStatus;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @Schema(description = "删除标记：0-未删除，1-已删除")
    /** SYSAPP密钥删除 */
    private Integer sysAppKeyDelete;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    /** SYSAPP密钥expire时间 */
    private Long sysAppKeyExpireTime;

    /**
     * 有效天数，0 表示不限时
     */
    @TableField("sys_app_key_valid_days")
    @Schema(description = "有效天数，0表示不限时")
    /** SYSAPP密钥是否有效days */
    private Integer sysAppKeyValidDays;

    /**
     * IP白名单
     */
    @Schema(description = "IP白名单")
    /** SYSAPP密钥ipwhitelist */
    private String sysAppKeyIpWhitelist;

    /**
     * 备注
     */
    @Schema(description = "备注")
    /** SYSAPP密钥评论 */
    private String sysAppKeyRemark;

    /**
     * 最后使用时间
     */
    @Schema(description = "最后使用时间")
    /** SYSAPP密钥最后used时间 */
    private Long sysAppKeyLastUsedTime;

    /**
     * 使用次数
     */
    @Schema(description = "使用次数")
    /** SYSAPP密钥USE数量 */
    private Integer sysAppKeyUseCount;

    /**
     * 创建人名称
     */
    @Schema(description = "创建人名称")
    /** 创建名称 */
    private String createName;

    /**
     * 创建人标识
     */
    @Schema(description = "创建人ID")
    /** 创建by */
    private Integer createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    /** 创建时间 */
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    /** 更新时间 */
    private Date updateTime;

    /**
     * 更新人名称
     */
    @Schema(description = "更新人名称")
    /** 更新名称 */
    private String updateName;

    /**
     * 更新人标识
     */
    @Schema(description = "更新人ID")
    /** 更新by */
    private Integer updateBy;

    /**
     * 允许的权限范围
     * <p>
     * 多个 scope 用逗号分隔，如：读取,写入,admin
     * </p>
     */
    @Schema(description = "允许的权限范围，多个用逗号分隔")
    /** SYSAPP密钥作用域 */
    private String sysAppKeyScope;

    /**
     * 允许的授权类型
     * <p>
     * 多个用逗号分隔，如：客户端_凭证,密码,授权_编码,refresh_令牌
     * </p>
     */
    @Schema(description = "允许的授权类型，多个用逗号分隔")
    /** SYSAPP密钥granttypes */
    private String sysAppKeyGrantTypes;

    /**
     * 允许的回调地址
     * <p>
     * 多个用逗号分隔，用于 授权_编码 模式的 redirect_uri 校验
     * </p>
     */
    @Schema(description = "允许的回调地址，多个用逗号分隔")
    /** SYSAPP密钥redirecturis */
    private String sysAppKeyRedirectUris;
}
