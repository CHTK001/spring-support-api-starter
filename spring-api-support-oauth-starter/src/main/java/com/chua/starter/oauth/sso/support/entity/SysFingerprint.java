package com.chua.starter.oauth.sso.support.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
* 浏览器指纹与用户映射实体
*
* @since 4.0.0.42
* @author CH
 */
@Data
@TableName("sys_fingerprint")
@Schema(description = "浏览器指纹与用户映射")
public class SysFingerprint implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // 串行版本uid

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id; // id

    @Schema(description = "浏览器指纹（SHA-256哈希）")
    private String fingerprint; // fingerprint

    @Schema(description = "用户ID")
    private String userId; // 用户id

    @Schema(description = "用户名")
    private String username; // 用户名

    @Schema(description = "登录IP")
    private String ip; // ip

    @Schema(description = "登录类型")
    private String loginType; // login类型

    @Schema(description = "User-Agent")
    private String userAgent; // 用户智能体

    @Schema(description = "浏览器")
    private String browser; // browser

    @Schema(description = "操作系统")
    private String os; // os

    @Schema(description = "登录时间戳（毫秒）")
    private Long loginTime; // login时间

    @Schema(description = "最后在线时间戳（毫秒）")
    private Long lastLoginTime; // 最后一个login时间

    @Schema(description = "当前在线Token数")
    private Integer tokenCount; // 令牌数量

    @Schema(description = "状态：0-离线，1-在线")
    private Integer status; // 状态

    @Schema(description = "创建时间")
    private Date createTime; // 创建时间

    @Schema(description = "更新时间")
    private Date updateTime; // 更新时间
}
