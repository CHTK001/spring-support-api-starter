// [P3C BL-边界 豁免]

package com.chua.starter.system.pojo.verify;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * 验证码下发结果
 *
 * @param maskedTarget  脱敏后的投递目标，供前端提示"已发往何处"
 * @param expireSeconds 验证码剩余有效秒数
 * @param intervalSeconds 再次下发间隔秒数，用于前端倒计时
 * @author CH
 * @since 4.0.0.50
 */
@Schema(description = "验证码下发结果")
public record VerifyCodeSentVO(
        @Schema(description = "脱敏投递目标") String maskedTarget,
        @Schema(description = "有效期(秒)") int expireSeconds,
        @Schema(description = "重发间隔(秒)") int intervalSeconds) {

    /**
     * 规范构造器：脱敏投递目标为空值敌对。
     *
     * <p>value class 前置条件——组件必须非空；有效期与重发间隔为基本类型，无需校验。</p>
     *
     * @param maskedTarget 脱敏后的投递目标，供前端提示"已发往何处"
     */
    public VerifyCodeSentVO {
        maskedTarget = Objects.requireNonNull(maskedTarget, "maskedTarget 不能为 null");
    }
}
