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
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "浏览器指纹（SHA-256哈希）")
    private String fingerprint;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录IP")
    private String ip;

    @Schema(description = "登录类型")
    private String loginType;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "浏览器")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "登录时间戳（毫秒）")
    private Long loginTime;

    @Schema(description = "最后在线时间戳（毫秒）")
    private Long lastLoginTime;

    @Schema(description = "当前在线Token数")
    private Integer tokenCount;

    @Schema(description = "状态：0-离线，1-在线")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
