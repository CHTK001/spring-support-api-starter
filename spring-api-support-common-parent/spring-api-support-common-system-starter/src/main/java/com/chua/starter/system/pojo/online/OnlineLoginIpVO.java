package com.chua.starter.system.pojo.online;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 在线用户登录 IP 视图对象
 * <p>按登录日志聚合出的单个 IP：登录次数与最近一次登录时间。</p>
 *
 * @param ip       登录 IP
 * @param address  登录归属地
 * @param lastTime 最近登录时间
 * @param total    登录次数
 * @since 4.0.0.42
 * @author CH
 */
@Schema(description = "在线用户登录IP")
public record OnlineLoginIpVO(
        @Schema(description = "登录IP") String ip,
        @Schema(description = "登录归属地") String address,
        @Schema(description = "最近登录时间") LocalDateTime lastTime,
        @Schema(description = "登录次数") Long total) {
}
