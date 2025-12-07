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
 * @since 2024/9/6
 * @author CH    
 */

/**
 * 用户三方绑定�?
 */
@ApiModel(description = "用户三方绑定�?)
@Schema(description = "用户三方绑定�?)
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_third_party")
public class SysUserThirdParty extends SysBase implements Serializable {
    @TableId(value = "sys_user_third_party_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysUserThirdPartyId;

    /**
     * 三方类型. gitee
     */
    @TableField(value = "sys_user_third_party_type")
    @ApiModelProperty(value = "三方类型. gitee")
    @Schema(description = "三方类型. gitee")
    @Size(max = 255, message = "三方类型. gitee最大长度要小于 255")
    private String sysUserThirdPartyType;

    /**
     * 三方信息名称
     */
    @TableField(value = "sys_user_third_party_name")
    @ApiModelProperty(value = "三方信息名称")
    @Schema(description = "三方信息名称")
    @Size(max = 255, message = "三方信息名称最大长度要小于 255")
    private String sysUserThirdPartyName;

    /**
     * 三方信息
     */
    @TableField(value = "sys_user_third_party_value")
    @ApiModelProperty(value = "三方信息")
    @Schema(description = "三方信息")
    @Size(max = 255, message = "三方信息最大长度要小于 255")
    private String sysUserThirdPartyValue;

    /**
     * 当前系统用户ID
     */
    @TableField(value = "sys_user_id")
    @ApiModelProperty(value = "当前系统用户ID")
    @Schema(description = "当前系统用户ID")
    private Integer sysUserId;

    /**
     * 三方用户ID
     */
    @TableField(value = "sys_user_third_party_uid")
    @ApiModelProperty(value = "三方用户ID")
    @Schema(description = "三方用户ID")
    @Size(max = 255, message = "三方用户ID最大长度要小于 255")
    private String sysUserThirdPartyUid;

    /**
     * 三方类型
     */
    public static enum Type {
        /**
         * Gitee
         */
        GITEE,
        /**
         * 微信小程�?
         */
        WX_MINI_APP
    }
}