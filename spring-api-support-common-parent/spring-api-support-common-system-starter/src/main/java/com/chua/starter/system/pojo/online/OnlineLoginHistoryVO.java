package com.chua.starter.system.pojo.online;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在线用户登录时间轴
 * <p>最近 N 天（默认 30 天）的登录汇总：按天补零的节点列表 + 全部登录 IP。</p>
 *
 * @param username   用户名
 * @param windowDays 统计窗口天数
 * @param total      窗口内登录次数（成功 + 失败）
 * @param success    窗口内成功次数
 * @param fail       窗口内失败次数
 * @param lastTime   最近一次成功登录时间
 * @param ips        全部登录 IP（按最近使用倒序）
 * @param items      按天节点（日期升序，无登录的日期补零）
 * @since 4.0.0.42
 * @author CH
 */
@Schema(description = "在线用户登录时间轴")
public record OnlineLoginHistoryVO(
        @Schema(description = "用户名") String username,
        @Schema(description = "统计窗口天数") Integer windowDays,
        @Schema(description = "窗口内登录次数") Integer total,
        @Schema(description = "窗口内成功次数") Integer success,
        @Schema(description = "窗口内失败次数") Integer fail,
        @Schema(description = "最近一次成功登录时间") LocalDateTime lastTime,
        @Schema(description = "全部登录IP") List<OnlineLoginIpVO> ips,
        @Schema(description = "按天登录节点") List<OnlineLoginDayVO> items) {
}
