package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * AppId应用标识实体类
 *
 * @author CH
 * @since 2025/01/05
 */
@Data
@TableName("app_id")
@Schema(description = "AppId应用标识")
public class SysAppId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * AppId ID - 主键自增
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "AppId ID")
    private Integer appIdId;

    /**
     * AppId值 - 应用唯一标识符，全局唯一
     */
    @Schema(description = "AppId值")
    private String appIdValue;

    /**
     * AppId名称 - 应用的显示名称
     */
    @Schema(description = "AppId名称")
    private String appIdName;

    /**
     * 应用描述 - 应用的详细说明
     */
    @Schema(description = "应用描述")
    private String appIdDescription;


    /**
     * 应用状态 - 0:禁用, 1:启用
     */
    @Schema(description = "应用状态：0-禁用，1-启用")
    private Integer appIdStatus;

    /**
     * 过期时间 - Unix时间戳(毫秒)，NULL表示永不过期
     */
    @Schema(description = "过期时间")
    private Long appIdExpireTime;

    /**
     * 最后使用时间 - Unix时间戳(毫秒)，最近一次使用该AppId的时间
     */
    @Schema(description = "最后使用时间")
    private Long appIdLastUseTime;

    /**
     * 使用次数 - 该AppId累计被调用的次数
     */
    @Schema(description = "使用次数")
    private Long appIdUseCount;

    /**
     * 关联用户ID - 该AppId所属的用户ID
     */
    @Schema(description = "关联用户ID")
    private Integer userId;

    /**
     * 允许的权限范围 - 多个scope用逗号分隔，如"read,write,admin"
     */
    @Schema(description = "允许的权限范围，多个用逗号分隔")
    private String appIdScope;

    /**
     * 允许的授权类型 - 多个用逗号分隔，如"client_credentials,password,authorization_code,refresh_token"
     */
    @Schema(description = "允许的授权类型，多个用逗号分隔")
    private String appIdGrantTypes;

    /**
     * 允许的回调地址 - 多个用逗号分隔，用于authorization_code模式的redirect_uri校验
     */
    @Schema(description = "允许的回调地址，多个用逗号分隔")
    private String appIdRedirectUris;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息")
    private String appIdRemark;

    /**
     * 删除标记 - 0:未删除, 1:已删除
     */
    @Schema(description = "删除标记：0-未删除，1-已删除")
    private Integer deleteFlag;

    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名")
    private String createName;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Integer createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 更新人姓名
     */
    @Schema(description = "更新人姓名")
    private String updateName;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private Integer updateBy;
}

