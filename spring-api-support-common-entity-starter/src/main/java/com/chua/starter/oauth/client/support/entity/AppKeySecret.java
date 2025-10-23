package com.chua.starter.oauth.client.support.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * AppKey请求实体
 * <p>
 * 用于基于 AppID/AppSecret 的签名认证
 * </p>
 *
 * @author CH
 * @since 2025/10/23
 */
@Data
@ApiModel(description = "AppKey请求实体")
@Schema(description = "AppKey请求实体")
public class AppKeySecret implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用ID（AccessKey/AppId）
     */
    @ApiModelProperty(value = "应用ID", required = true)
    @Schema(description = "应用ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;

    /**
     * 用户代码（等价于uid）
     */
    @ApiModelProperty(value = "用户代码")
    @Schema(description = "用户代码")
    private String userCode;

    /**
     * 时间戳（Unix时间戳，秒）
     */
    @ApiModelProperty(value = "时间戳", required = true)
    @Schema(description = "时间戳", requiredMode = Schema.RequiredMode.REQUIRED)
    private String xTime;

    /**
     * 随机数
     */
    @ApiModelProperty(value = "随机数", required = true)
    @Schema(description = "随机数", requiredMode = Schema.RequiredMode.REQUIRED)
    private String xRandom;

    /**
     * 请求体（JSON字符串）
     */
    @ApiModelProperty(value = "请求体")
    @Schema(description = "请求体")
    private String body;

    /**
     * 签名
     * <p>
     * 签名算法：HmacSHA256(x-body=MD5(body)&x-random=xRandom&x-time=xTime)
     * </p>
     */
    @ApiModelProperty(value = "签名", required = true)
    @Schema(description = "签名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String xSign;
}

