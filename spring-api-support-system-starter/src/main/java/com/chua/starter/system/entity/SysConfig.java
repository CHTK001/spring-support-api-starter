package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @since 2024/8/28
 * @author CH    
 */
/**
 * 系统设置
 */
@ApiModel(description="系统设置")
@Schema(description="系统设置")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_config")
public class SysConfig extends SysBase implements Serializable {
    @TableId(value = "sys_config_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    @NotNull(message = "不能为null")
    private Integer sysConfigId;

    /**
     * 配置分组
     */
    @TableField(value = "sys_config_group")
    @ApiModelProperty(value="配置分组")
    @Schema(description="配置分组")
    @Size(max = 255,message = "配置分组最大长度要小于 255")
    private String sysConfigGroup;

    /**
     * 配置名称
     */
    @TableField(value = "sys_config_name")
    @ApiModelProperty(value="配置名称")
    @Schema(description="配置名称")
    @Size(max = 255,message = "配置名称最大长度要小于 255")
    private String sysConfigName;

    /**
     * 配置值
     */
    @TableField(value = "sys_config_value")
    @ApiModelProperty(value="配置值")
    @Schema(description="配置值")
    @Size(max = 255,message = "配置值最大长度要小于 255")
    private String sysConfigValue;

    /**
     * 配置类型
     */
    @TableField(value = "sys_config_type")
    @ApiModelProperty(value="配置类型")
    @Schema(description="配置类型")
    @Size(max = 255,message = "配置类型最大长度要小于 255")
    private String sysConfigType;

    /**
     * 配置描述
     */
    @TableField(value = "sys_config_remark")
    @ApiModelProperty(value="配置描述")
    @Schema(description="配置描述")
    @Size(max = 255,message = "配置描述最大长度要小于 255")
    private String sysConfigRemark;
}