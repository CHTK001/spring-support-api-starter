package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @since 2024/8/30
 */

/**
 * 密钥
 */
@Schema(description = "密钥")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_secret")
public class SysSecret extends SysBase implements Serializable {
    @TableId(value = "sys_secret_id", type = IdType.AUTO)
    @Schema(description = "")
    @NotNull(message = "不能为null")
    /** SYSsecretID */
    private Integer sysSecretId;

    /**
     * 分组
     */
    @TableField(value = "sys_secret_group")
    @Schema(description = "分组")
    @Size(max = 255, message = "分组最大长度要小于 255")
    /** SYSsecret分组 */
    private String sysSecretGroup;

    /**
     * 编码, 可自行作为唯一标识
     */
    @TableField(value = "sys_secret_code")
    @Schema(description = "编码, 可自行作为唯一标识")
    @Size(max = 255, message = "编码, 可自行作为唯一标识最大长度要小于 255")
    /** SYSsecret代码 */
    private String sysSecretCode;
    /**
     * 功能
     */
    @TableField(value = "sys_secret_function")
    @Schema(description = "功能")
    @Size(max = 255, message = "功能最大长度要小于 255")
    /** SYSsecretfunction */
    private String sysSecretFunction;
    /**
     * api地址
     */
    @TableField(value = "sys_secret_api_address")
    @Schema(description = "api地址")
    @Size(max = 255, message = "api地址最大长度要小于 255")
    /** SYSsecretAPI地址 */
    private String sysSecretApiAddress;
    /**
     * appid
     */
    @TableField(value = "sys_secret_app_id")
    @Schema(description = "appid")
    @Size(max = 255, message = "appid最大长度要小于 255")
    /** SYSsecretAPPID */
    private String sysSecretAppId;

    /**
     * appsecret
     */
    @TableField(value = "sys_secret_app_secret")
    @Schema(description = "appsecret")
    @Size(max = 255, message = "appsecret最大长度要小于 255")
    /** SYSsecretAPPsecret */
    private String sysSecretAppSecret;
    /**
     * 签名
     */
    @TableField(value = "sys_secret_sign")
    @Schema(description = "签名")
    @Size(max = 255, message = "签名最大长度要小于 255")
    /** SYSsecretsign */
    private String sysSecretSign;


    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_secret_delete")
    @Schema(description = "是否删除0:未删除")
    @TableLogic(value = "0", delval = "1")
    /** SYSsecretdelete */
    private Integer sysSecretDelete;

    /**
     * 是否启用0:未启用
     */
    @TableField(value = "sys_secret_status")
    @Schema(description = "是否启用0:未启用")
    /** SYSsecret状态 */
    private Integer sysSecretStatus;

    /**
     * cdn
     */
    @TableField(value = "sys_secret_cdn")
    @Schema(description = "cdn")
    @Size(max = 255, message = "cdn最大长度要小于 255")
    /** SYSsecretCDN */
    private String sysSecretCdn;

    /**
     * 端点
     */
    @TableField(value = "sys_secret_endpoint")
    @Schema(description = "端点")
    @Size(max = 255, message = "端点最大长度要小于 255")
    /** SYSsecretendpoint */
    private String sysSecretEndpoint;

    /**
     * 描述
     */
    @TableField(value = "sys_secret_remark")
    @Schema(description = "描述")
    @Size(max = 255, message = "描述最大长度要小于 255")
    /** SYSsecretremark */
    private String sysSecretRemark;
}
