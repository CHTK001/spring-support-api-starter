package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

@ApiModel(description="sys_message_config")
@Schema
@Data
@TableName(value = "sys_message_config")
public class SysMessageConfig  extends SysTenantBase {
    @TableId(value = "config_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    private Integer configId;

    /**
     * 配置名称
     */
    @TableField(value = "config_name")
    @ApiModelProperty(value="配置名称")
    @Schema(description="配置名称")
    private String configName;

    /**
     * 推送类型
     */
    @TableField(value = "config_type")
    @ApiModelProperty(value="推送类型")
    @Schema(description="推送类型")
    private String configType;

    /**
     * AK
     */
    @TableField(value = "config_access_key")
    @ApiModelProperty(value="AK")
    @Schema(description="AK")
    private String configAccessKey;

    /**
     * SK
     */
    @TableField(value = "config_secret_key")
    @ApiModelProperty(value="SK")
    @Schema(description="SK")
    private String configSecretKey;

    /**
     * 远程服务器地址
     */
    @TableField(value = "config_endpoint")
    @ApiModelProperty(value="远程服务器地址")
    @Schema(description="远程服务器地址")
    private String configEndpoint;

    /**
     * 企业名称
     */
    @TableField(value = "config_ecname")
    @ApiModelProperty(value="企业名称")
    @Schema(description="企业名称")
    private String configEcname;

    /**
     * 映射ID
     */
    @TableField(value = "config_mapper_template")
    @ApiModelProperty(value="映射ID")
    @Schema(description="映射ID")
    private String configMapperTemplate;


    /**
     * 短信推送实现方式
     */
    @TableField(value = "config_sms_type")
    @ApiModelProperty(value="短信推送实现方式")
    @Schema(description="短信推送实现方式")
    private String configSmsType;
}