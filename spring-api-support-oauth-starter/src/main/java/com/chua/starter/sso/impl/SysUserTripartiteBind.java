package com.chua.starter.sso.impl;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户三方绑定信息表
 */
@ApiModel(description="用户三方绑定信息表")
@Schema(description="用户三方绑定信息表")
@Data
@TableName(value = "sys_user_tripartite_bind")
public class SysUserTripartiteBind extends SysTenantBase implements Serializable {
    @TableId(value = "user_id", type = IdType.AUTO)
    @ApiModelProperty(value="")
    @Schema(description="")
    @NotNull(message = "不能为null")
    private Integer userId;

    @TableField(value = "login_code_type")
    @ApiModelProperty(value="")
    @Schema(description="")
    @Size(max = 255,message = "最大长度要小于 255")
    private String loginCodeType;

    @TableField(value = "login_username")
    @ApiModelProperty(value="")
    @Schema(description="")
    @Size(max = 255,message = "最大长度要小于 255")
    private String loginUsername;

    @TableField(value = "login_uuid")
    @ApiModelProperty(value="")
    @Schema(description="")
    @Size(max = 255,message = "最大长度要小于 255")
    private String loginUuid;

    @TableField(value = "login_avatar")
    @ApiModelProperty(value="")
    @Schema(description="")
    @Size(max = 255,message = "最大长度要小于 255")
    private String loginAvatar;

    @TableField(value = "login_name")
    @ApiModelProperty(value="")
    @Schema(description="")
    @Size(max = 255,message = "最大长度要小于 255")
    private String loginName;


    private static final long serialVersionUID = 1L;
}