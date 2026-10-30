package com.chua.starter.system.pojo.online;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在线用户单日登录情况
 * <p>时间轴上的一个节点：某一天的登录总次数、成功/失败次数与当日 IP 列表。</p>
 *
 * @param date     日期（yyyy-MM-dd）
 * @param total    登录次数（成功 + 失败）
 * @param success  成功次数
 * @param fail     失败次数
 * @param ips      当日登录 IP 列表
 * @param lastTime 当日最近一次登录时间
 * @since 4.0.0.42
 * @author CH
 */
@Schema(description = "在线用户单日登录情况")
public record OnlineLoginDayVO(
        @Schema(description = "日期(yyyy-MM-dd)") String date,
        @Schema(description = "登录次数(成功+失败)") Integer total,
        @Schema(description = "成功次数") Integer success,
        @Schema(description = "失败次数") Integer fail,
        @Schema(description = "当日登录IP") List<String> ips,
        @Schema(description = "当日最近登录时间") LocalDateTime lastTime) {
}
