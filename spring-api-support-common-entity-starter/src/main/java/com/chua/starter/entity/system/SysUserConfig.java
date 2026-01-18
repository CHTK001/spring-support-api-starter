package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
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
@Schema(description = "用户配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_config")
public class SysUserConfig extends SysBase implements Serializable {

    /**
     * 配置ID
     */
    @TableId(value = "sys_user_config_id", type = IdType.AUTO)
    @Schema(description = "配置ID")
    @NotNull(message = "配置ID不能为空")
    private Integer sysUserConfigId;

    /**
     * 微信OpenID
     */
    @TableField(value = "sys_user_config_wx_openid")
    @Schema(description = "微信OpenID")
    @Size(max = 255, message = "微信OpenID最大长度需小于255")
    private String sysUserConfigWxOpenid;

    /**
     * 微信UnionID
     */
    @TableField(value = "sys_user_config_wx_unionid")
    @Schema(description = "微信UnionID")
    @Size(max = 255, message = "微信UnionID最大长度需小于255")
    private String sysUserConfigWxUnionid;
}
