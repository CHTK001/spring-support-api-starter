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

/**
 * 用户三方绑定信息表
 */
@ApiModel(description = "用户三方绑定信息表")
@Schema(description = "用户三方绑定信息表")
@Data
@TableName(value = "sys_user_tripartite_bind")
public class SysUserTripartiteBind extends SysTenantBase {
    private static final long serialVersionUID = 1L;
    @TableId(value = "tripartite_bind_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer tripartiteBindId;

    /**
     * 系统用户
     */
    @TableField(value = "user_id")
    @ApiModelProperty(value = "系统用户")
    @Schema(description = "系统用户")
    private Integer userId;

    /**
     * 三方联系
     */
    @TableField(value = "login_code_type")
    @ApiModelProperty(value = "三方联系")
    @Schema(description = "三方联系")
    private String loginCodeType;

    /**
     * 三方用户名
     */
    @TableField(value = "login_username")
    @ApiModelProperty(value = "三方用户名")
    @Schema(description = "三方用户名")
    private String loginUsername;

    /**
     * 三方唯一ID
     */
    @TableField(value = "login_uuid")
    @ApiModelProperty(value = "三方唯一ID")
    @Schema(description = "三方唯一ID")
    private String loginUuid;

    /**
     * 三方图像
     */
    @TableField(value = "login_avatar")
    @ApiModelProperty(value = "三方图像")
    @Schema(description = "三方图像")
    private String loginAvatar;

    /**
     * 三方登录名称
     */
    @TableField(value = "login_name")
    @ApiModelProperty(value = "三方登录名称")
    @Schema(description = "三方登录名称")
    private String loginName;


}