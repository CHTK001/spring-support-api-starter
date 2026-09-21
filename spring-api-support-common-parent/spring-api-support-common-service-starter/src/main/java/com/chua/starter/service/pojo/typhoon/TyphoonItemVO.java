package com.chua.starter.service.pojo.typhoon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 活跃台风列表项出参（顶栏台风的唯一契约）。
 *
 * <p>字段命名与前端 {@code TyphoonItem} 一一对应。数值字段用包装类型，
 * 数据源缺该字段时返回 null，前端按「未知」处理而不是 0。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Schema(description = "活跃台风列表项")
@Data
public class TyphoonItemVO {

    /**
     * 台风编号（如 202618）
    */
    @Schema(description = "台风编号")
    private String id;

    /**
     * 中文名
    */
    @Schema(description = "中文名")
    private String name;

    /**
     * 强度等级中文名（如台风、强台风）
    */
    @Schema(description = "强度等级中文名")
    private String strong;

    /**
     * 中心纬度
    */
    @Schema(description = "中心纬度")
    private Double lat;

    /**
     * 中心经度
    */
    @Schema(description = "中心经度")
    private Double lng;

    /**
     * 观测时间
    */
    @Schema(description = "观测时间")
    private String time;

    /**
     * 风速（米/秒）
    */
    @Schema(description = "风速（米/秒）")
    private Double speed;

    /**
     * 中心气压（hPa）
    */
    @Schema(description = "中心气压（hPa）")
    private Double pressure;

    /**
     * 数据来源（zhejiang-typhoon 等）
    */
    @Schema(description = "数据来源")
    private String source;
}
