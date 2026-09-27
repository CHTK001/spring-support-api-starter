package com.chua.starter.system.pojo.online;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 在线用户视图对象
 *
 * @since 2024/12/15
 * @author CH
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "在线用户信息")
public class OnlineUserVO {

    /**
     * 用户标识
     */
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 登录IP
     */
    @Schema(description = "登录IP")
    @JsonProperty("ip")
    private String loginIp;

    /**
     * 登录地址
     */
    @Schema(description = "登录地址")
    @JsonProperty("address")
    private String loginAddress;

    /**
     * 浏览器
     */
    @Schema(description = "浏览器")
    private String browser;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统")
    private String os;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    /**
     * 最后访问时间
     */
    @Schema(description = "最后访问时间")
    private LocalDateTime lastAccessTime;

    /**
     * 令牌
     */
    @Schema(description = "Token")
    private String token;

    /**
     * 登录类型
     */
    @Schema(description = "登录类型")
    private String loginType;
}
