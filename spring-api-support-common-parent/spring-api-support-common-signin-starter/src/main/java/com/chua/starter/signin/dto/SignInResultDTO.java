package com.chua.starter.signin.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 签到结果 DTO
 * <p>签到操作成功后返回给前端的结果，包含签到状态、积分、连续天数等信息。
 * 如果用户当天已签到，{@code alreadySigned} 为 true。</p>
 *
 * @param success 签到是否成功
 * @param alreadySigned 是否已经签到过（今日已签到）
 * @param msg 提示消息
 * @param todayPoints 本次签到获得的积分
 * @param streak 连续签到天数
 * @param totalPoints 累计总积分
 * @param totalDays 累计签到天数
 * @param pointsBreakdown 积分明细列表
 *
 * @since 4.0.0.42
 * @author CH
 */
public record SignInResultDTO(
        boolean success,
        boolean alreadySigned,
        String msg,
        Integer todayPoints,
        Integer streak,
        Integer totalPoints,
        Integer totalDays,
        List<SignInPointDTO> pointsBreakdown
) {

    /**
     * 规范构造器：积分明细列表做防御性拷贝。
     *
     * <p>value class 前置条件——集合组件必须深不可变。
     * 未签到成功场景（防抖失败、今日已签到）该列表允许为 null，故保留 null 语义；
     * 明细元素同样按可含 null 的安全写法包装，避免 {@code List.copyOf} 抛 NPE。</p>
     *
     * @param pointsBreakdown 积分明细列表
     */
    public SignInResultDTO {
        pointsBreakdown = pointsBreakdown == null
                ? null
                : Collections.unmodifiableList(new ArrayList<>(pointsBreakdown));
    }
}