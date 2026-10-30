package com.chua.starter.system.pojo.online;

import java.time.LocalDateTime;

/**
 * 登录日志按用户 + IP 聚合的查询行
 *
 * @param username 用户名
 * @param ip       登录 IP
 * @param address  登录归属地（该 IP 最近一次解析结果）
 * @param lastTime 最近登录时间
 * @param total    登录次数
 * @since 4.0.0.42
 * @author CH
 */
public record OnlineLoginIpStat(
        String username,
        String ip,
        String address,
        LocalDateTime lastTime,
        Long total) {
}
