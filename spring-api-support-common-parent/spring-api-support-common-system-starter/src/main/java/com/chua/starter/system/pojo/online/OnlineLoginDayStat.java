package com.chua.starter.system.pojo.online;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 登录日志按用户 + 日期聚合的查询行
 *
 * @param username 用户名
 * @param date     日期
 * @param total    登录次数（成功 + 失败）
 * @param success  成功次数
 * @param fail     失败次数
 * @param lastTime 当日最近登录时间
 * @param ips      当日登录 IP，逗号分隔
 * @since 4.0.0.42
 * @author CH
 */
public record OnlineLoginDayStat(
        String username,
        LocalDate date,
        Integer total,
        Integer success,
        Integer fail,
        LocalDateTime lastTime,
        String ips) {
}
