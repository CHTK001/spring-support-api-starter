package com.chua.starter.system.pojo.online;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 在线用户视图对象
 *
 * @param userId         用户标识
 * @param username       用户名
 * @param nickname       昵称
 * @param deptName       部门名称
 * @param loginIp        登录IP
 * @param loginAddress   登录地址
 * @param browser        浏览器
 * @param os             操作系统
 * @param loginTime      登录时间
 * @param lastAccessTime 最后访问时间
 * @param token          令牌
 * @param loginType      登录类型
 * @since 2024/12/15
 * @author CH
 */
@Schema(description = "在线用户信息")
public record OnlineUserVO(
        @Schema(description = "用户ID") String userId,
        @Schema(description = "用户名") String username,
        @Schema(description = "昵称") String nickname,
        @Schema(description = "部门名称") String deptName,
        @Schema(description = "登录IP") @JsonProperty("ip") String loginIp,
        @Schema(description = "登录地址") @JsonProperty("address") String loginAddress,
        @Schema(description = "浏览器") String browser,
        @Schema(description = "操作系统") String os,
        @Schema(description = "登录时间") LocalDateTime loginTime,
        @Schema(description = "最后访问时间") LocalDateTime lastAccessTime,
        @Schema(description = "Token") String token,
        @Schema(description = "登录类型") String loginType) {
}
