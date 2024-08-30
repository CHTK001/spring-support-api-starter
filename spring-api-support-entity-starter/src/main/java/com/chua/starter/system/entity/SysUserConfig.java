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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * @since 2024/8/30
 * @author CH    
 */
/**
 * 用户信息 - 用户设置表
 */
@ApiModel(description="用户信息 - 用户设置表")
@Schema(description="用户信息 - 用户设置表")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_user_config")
public class SysUserConfig extends SysBase implements Serializable {
    @TableId(value = "sys_user_config_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    @NotNull(message = "不能为null")
    private Integer sysUserConfigId;

    /**
     * 微信openid
     */
    @TableField(value = "sys_user_config_wx_openid")
    @ApiModelProperty(value="微信openid")
    @Schema(description="微信openid")
    @Size(max = 255,message = "微信openid最大长度要小于 255")
    private String sysUserConfigWxOpenid;

    /**
     * 微信unionid
     */
    @TableField(value = "sys_user_config_wx_unionid")
    @ApiModelProperty(value="微信unionid")
    @Schema(description="微信unionid")
    @Size(max = 255,message = "微信unionid最大长度要小于 255")
    private String sysUserConfigWxUnionid;
}