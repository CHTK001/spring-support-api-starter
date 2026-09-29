package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 日期查询
 *
 * <p><b>为什么保留为 class 而不是 record</b>：{@code SysUserLogQuery} 等子类
 * 直接 {@code extends DateQuery} 以复用起止日期字段，而 record 是 final、不可继承，
 * 改成 record 会让这些子类编译失败。属于「继承项目内其他类型」的既定例外，
 * 不要再尝试转 record。</p>
 *
 * <p>{@code @Data} 不可省略：它提供 startDate/endDate 的访问器，
 * 去掉后 Spring 参数绑定与 lombok 的 {@code @EqualsAndHashCode(callSuper = true)}
 * 子类都将拿不到父类属性。</p>
 *
 * @since 2024/8/20
 * @author CH
 */
@Data
@Schema(description = "日期查询")
public class DateQuery implements Serializable {

    /**
     * 开始日期
     */
    @Schema(description = "开始日期")
    private String startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    private String endDate;
}
