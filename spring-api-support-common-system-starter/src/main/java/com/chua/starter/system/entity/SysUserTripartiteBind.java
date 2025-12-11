package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户三方绑定信息表
 *
 * @author CH
 * @since 1.0.0
 */
@ApiModel(description = "用户三方绑定信息表")
@Schema(description = "用户三方绑定信息表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_tripartite_bind")
public class SysUserTripartiteBind extends SysTenantBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    @ApiModelProperty(value = "用户ID")
    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    /**
     * 登录类型
     */
    @TableField(value = "login_code_type")
    @ApiModelProperty(value = "登录类型")
    @Schema(description = "登录类型")
    @Size(max = 255, message = "登录类型最大长度为255")
    private String loginCodeType;

    /**
     * 登录用户名
     */
    @TableField(value = "login_username")
    @ApiModelProperty(value = "登录用户名")
    @Schema(description = "登录用户名")
    @Size(max = 255, message = "登录用户名最大长度为255")
    private String loginUsername;

    /**
     * 登录UUID
     */
    @TableField(value = "login_uuid")
    @ApiModelProperty(value = "登录UUID")
    @Schema(description = "登录UUID")
    @Size(max = 255, message = "登录UUID最大长度为255")
    private String loginUuid;

    /**
     * 登录头像
     */
    @TableField(value = "login_avatar")
    @ApiModelProperty(value = "登录头像")
    @Schema(description = "登录头像")
    @Size(max = 255, message = "登录头像最大长度为255")
    private String loginAvatar;

    /**
     * 登录名称
     */
    @TableField(value = "login_name")
    @ApiModelProperty(value = "登录名称")
    @Schema(description = "登录名称")
    @Size(max = 255, message = "登录名称最大长度为255")
    private String loginName;
}
