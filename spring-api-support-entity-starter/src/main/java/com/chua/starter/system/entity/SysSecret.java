package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author CH
 * @since 2024/8/30
 */

/**
 * 密钥
 */
@ApiModel(description = "密钥")
@Schema(description = "密钥")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_secret")
public class SysSecret extends SysBase implements Serializable {
    @TableId(value = "sys_secret_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysSecretId;

    /**
     * 分组
     */
    @TableField(value = "sys_secret_group")
    @ApiModelProperty(value = "分组")
    @Schema(description = "分组")
    @Size(max = 255, message = "分组最大长度要小于 255")
    private String sysSecretGroup;

    /**
     * appid
     */
    @TableField(value = "sys_secret_app_id")
    @ApiModelProperty(value = "appid")
    @Schema(description = "appid")
    @Size(max = 255, message = "appid最大长度要小于 255")
    private String sysSecretAppId;

    /**
     * appsecret
     */
    @TableField(value = "sys_secret_app_secret")
    @ApiModelProperty(value = "appsecret")
    @Schema(description = "appsecret")
    @Size(max = 255, message = "appsecret最大长度要小于 255")
    private String sysSecretAppSecret;

    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_secret_delete")
    @ApiModelProperty(value = "是否删除0:未删除")
    @Schema(description = "是否删除0:未删除")
    @TableLogic(value = "0", delval = "1")
    private Integer sysSecretDelete;

    /**
     * 是否启用0:未启用
     */
    @TableField(value = "sys_secret_status")
    @ApiModelProperty(value = "是否启用0:未启用")
    @Schema(description = "是否启用0:未启用")
    private Integer sysSecretStatus;

    /**
     * cdn
     */
    @TableField(value = "sys_secret_cdn")
    @ApiModelProperty(value = "cdn")
    @Schema(description = "cdn")
    @Size(max = 255, message = "cdn最大长度要小于 255")
    private String sysSecretCdn;

    /**
     * 端点
     */
    @TableField(value = "sys_secret_endpoint")
    @ApiModelProperty(value = "端点")
    @Schema(description = "端点")
    @Size(max = 255, message = "端点最大长度要小于 255")
    private String sysSecretEndpoint;

    /**
     * 描述
     */
    @TableField(value = "sys_secret_remark")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    @Size(max = 255, message = "描述最大长度要小于 255")
    private String sysSecretRemark;
}