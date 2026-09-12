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
   * appid应用标识实体类
 *
 * @since 2025/01/05
 * @author CH
 */
@Data
@TableName("app_id")
@Schema(description = "AppId应用标识")
public class SysAppId implements Serializable {

    @Serial
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
    * appid 标识 - 主键自增
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "AppId ID")
    /** APPIDID */
    private Integer appIdId;

    /**
    * appid值 - 应用唯一标识符，全局唯一
     */
    @Schema(description = "AppId值")
    /** APPID值 */
    private String appIdValue;

    /**
    * appid名称 - 应用的显示名称
     */
    @Schema(description = "AppId名称")
    /** APPID名称 */
    private String appIdName;

    /**
    * 应用描述 - 应用的详细说明
     */
    @Schema(description = "应用描述")
    /** APPID描述 */
    private String appIdDescription;


    /**
    * 应用状态 - 0:禁用, 1:启用
     */
    @Schema(description = "应用状态：0-禁用，1-启用")
    /** APPID状态 */
    private Integer appIdStatus;

    /**
    * 过期时间 - Unix时间戳(毫秒)，空表示永不过期
     */
    @Schema(description = "过期时间")
    /** appidexpire时间 */
    private Long appIdExpireTime;

    /**
    * 最后使用时间 - Unix时间戳(毫秒)，最近一次使用该appid的时间
     */
    @Schema(description = "最后使用时间")
    /** APPID最后USE时间 */
    private Long appIdLastUseTime;

    /**
    * 使用次数 - 该appid累计被调用的次数
     */
    @Schema(description = "使用次数")
    /** APPIDUSE数量 */
    private Long appIdUseCount;

    /**
    * 关联用户标识 - 该appid所属的用户标识
     */
    @Schema(description = "关联用户ID")
    /** 用户标识 */
    private Integer userId;

    /**
    * 允许的权限范围 - 多个scope用逗号分隔，如"读取,写入,admin"
     */
    @Schema(description = "允许的权限范围，多个用逗号分隔")
    /** APPID作用域 */
    private String appIdScope;

    /**
    * 允许的授权类型 - 多个用逗号分隔，如"客户端_凭证,密码,授权_编码,refresh_令牌"
     */
    @Schema(description = "允许的授权类型，多个用逗号分隔")
    /** appidgranttypes */
    private String appIdGrantTypes;

    /**
    * 允许的回调地址 - 多个用逗号分隔，用于授权_编码模式的redirect_uri校验
     */
    @Schema(description = "允许的回调地址，多个用逗号分隔")
    /** appidredirecturis */
    private String appIdRedirectUris;

    /**
    * 备注信息
     */
    @Schema(description = "备注信息")
    /** appidremark */
    private String appIdRemark;

    /**
    * 删除标记 - 0:未删除, 1:已删除
     */
    @Schema(description = "删除标记：0-未删除，1-已删除")
    /** 删除标记 */
    private Integer deleteFlag;

    /**
    * 创建人姓名
     */
    @Schema(description = "创建人姓名")
    /** 创建名称 */
    private String createName;

    /**
    * 创建人标识
     */
    @Schema(description = "创建人ID")
    /** 创建by */
    private Integer createBy;

    /**
    * 创建时间
     */
    @Schema(description = "创建时间")
    /** 创建时间 */
    private Date createTime;

    /**
    * 更新时间
     */
    @Schema(description = "更新时间")
    /** 更新时间 */
    private Date updateTime;

    /**
    * 更新人姓名
     */
    @Schema(description = "更新人姓名")
    /** 更新名称 */
    private String updateName;

    /**
    * 更新人标识
     */
    @Schema(description = "更新人ID")
    /** 更新by */
    private Integer updateBy;
}

