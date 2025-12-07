package com.chua.starter.entity.system;

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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户配置表
 *
 * @author CH
 * @since 2024/8/30
 */
@ApiModel(description = "用户配置表")
@Schema(description = "用户配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_config")
public class SysUserConfig extends SysBase implements Serializable {

    /**
     * 配置ID
     */
    @TableId(value = "sys_user_config_id", type = IdType.AUTO)
    @ApiModelProperty(value = "配置ID")
    @Schema(description = "配置ID")
    @NotNull(message = "配置ID不能为空")
    private Integer sysUserConfigId;

    /**
     * 微信OpenID
     */
    @TableField(value = "sys_user_config_wx_openid")
    @ApiModelProperty(value = "微信OpenID")
    @Schema(description = "微信OpenID")
    @Size(max = 255, message = "微信OpenID最大长度需小于255")
    private String sysUserConfigWxOpenid;

    /**
     * 微信UnionID
     */
    @TableField(value = "sys_user_config_wx_unionid")
    @ApiModelProperty(value = "微信UnionID")
    @Schema(description = "微信UnionID")
    @Size(max = 255, message = "微信UnionID最大长度需小于255")
    private String sysUserConfigWxUnionid;
}
