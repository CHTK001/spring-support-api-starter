package com.chua.starter.collect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据采集-通知渠道出参。
 *
 * <p>比通用选项多出「接收目标怎么填」，让前端按渠道切换输入框提示，
 * 第三方注册的渠道不必改前端就能带出自己的填法说明。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@NoArgsConstructor
@Schema(description = "数据采集-通知渠道出参")
public class CollectNotifyChannelVO {

    /**
     * 渠道标识，与通知配置的渠道字段一致。
     */
    @Schema(description = "渠道标识")
    private String value;

    /**
     * 渠道显示名。
     */
    @Schema(description = "渠道名称")
    private String label;

    /**
     * 接收目标的填写说明。
     */
    @Schema(description = "接收目标填写说明")
    private String targetHint;

    /**
     * 构造渠道出参。
     *
     * @param value      渠道标识
     * @param label      渠道显示名
     * @param targetHint 接收目标填写说明
     */
    public CollectNotifyChannelVO(String value, String label, String targetHint) {
        this.value = value;
        this.label = label;
        this.targetHint = targetHint;
    }
}
