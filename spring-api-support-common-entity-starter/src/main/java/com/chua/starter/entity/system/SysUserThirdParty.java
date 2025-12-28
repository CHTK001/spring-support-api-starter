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
 * 用户第三方绑定表
 *
 * @author CH
 * @since 2024/9/6
 */
@ApiModel(description = "用户第三方绑定表")
@Schema(description = "用户第三方绑定表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_third_party")
public class SysUserThirdParty extends SysBase implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "sys_user_third_party_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空")
    private Integer sysUserThirdPartyId;

    /**
     * 第三方类型，如: gitee、wx_mini_app
     */
    @TableField(value = "sys_user_third_party_type")
    @ApiModelProperty(value = "第三方类型，如: gitee、wx_mini_app")
    @Schema(description = "第三方类型，如: gitee、wx_mini_app")
    @Size(max = 255, message = "第三方类型最大长度需小于255")
    private String sysUserThirdPartyType;

    /**
     * 第三方信息名称
     */
    @TableField(value = "sys_user_third_party_name")
    @ApiModelProperty(value = "第三方信息名称")
    @Schema(description = "第三方信息名称")
    @Size(max = 255, message = "第三方信息名称最大长度需小于255")
    private String sysUserThirdPartyName;

    /**
     * 第三方信息值
     */
    @TableField(value = "sys_user_third_party_value")
    @ApiModelProperty(value = "第三方信息值")
    @Schema(description = "第三方信息值")
    @Size(max = 255, message = "第三方信息值最大长度需小于255")
    private String sysUserThirdPartyValue;

    /**
     * 当前系统用户ID
     */
    @TableField(value = "sys_user_id")
    @ApiModelProperty(value = "当前系统用户ID")
    @Schema(description = "当前系统用户ID")
    private Integer sysUserId;

    /**
     * 第三方用户ID
     */
    @TableField(value = "sys_user_third_party_uid")
    @ApiModelProperty(value = "第三方用户ID")
    @Schema(description = "第三方用户ID")
    @Size(max = 255, message = "第三方用户ID最大长度需小于255")
    private String sysUserThirdPartyUid;

    /**
     * 第三方类型枚举
     */
    public enum Type {
        /**
         * Gitee
         */
        GITEE,
        /**
         * Github
         */
        GITHUB,
        /**
         * 微信小程序
         */
        WX_MINI_APP,
        /**
         * 微信开放平台
         */
        WECHAT_OPEN,
        /**
         * 钉钉
         */
        DINGTALK,
        /**
         * 企业微信
         */
        WECHAT_ENTERPRISE
    }
}
