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
     * 编码, 可自行作为唯一标识
     */
    @TableField(value = "sys_secret_code")
    @ApiModelProperty(value = "编码, 可自行作为唯一标识")
    @Schema(description = "编码, 可自行作为唯一标识")
    @Size(max = 255, message = "编码, 可自行作为唯一标识最大长度要小于 255")
    private String sysSecretCode;
    /**
     * api地址
     */
    @TableField(value = "sys_secret_api_address")
    @ApiModelProperty(value = "api地址")
    @Schema(description = "api地址")
    @Size(max = 255, message = "api地址最大长度要小于 255")
    private String sysSecretApiAddress;
    /**
     * 字典项ID(字典10000)
     */
    @TableField(value = "sys_secret_dict_item_id")
    @ApiModelProperty(value = "字典项ID(字典10000)")
    @Schema(description = "字典项ID(字典10000)")
    @Size(max = 11, message = "字典项ID(字典10000)最大长度要小于 11")
    private Integer sysSecretDictItemId;
    /**
     * 字典项ID(字典10000)名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "字典项ID(字典10000)名称")
    @Schema(description = "字典项ID(字典10000)名称")
    private String sysSecretDictItemName;

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
     * 签名
     */
    @TableField(value = "sys_secret_sign")
    @ApiModelProperty(value = "签名")
    @Schema(description = "签名")
    @Size(max = 255, message = "签名最大长度要小于 255")
    private String sysSecretSign;



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