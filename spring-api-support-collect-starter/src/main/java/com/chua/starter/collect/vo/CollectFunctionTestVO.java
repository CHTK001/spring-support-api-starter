package com.chua.starter.collect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据采集-自定义函数试跑出参。
 *
 * <p>把返回值连同运行时类型一起给出，闭包返回 GString 时界面能看到它不是 String，
 * 这正是脚本里拼字符串最容易踩的地方。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "数据采集-自定义函数试跑出参")
public class CollectFunctionTestVO {

    /**
     * 是否调用成功。
     */
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 函数名。
     */
    @Schema(description = "函数名")
    private String functionName;

    /**
     * 闭包声明的形参个数。
     */
    @Schema(description = "形参个数")
    private Integer parameters;

    /**
     * 返回值的文本形式。
     */
    @Schema(description = "返回值")
    private String result;

    /**
     * 返回值的运行时类型。
     */
    @Schema(description = "返回值类型")
    private String resultType;

    /**
     * 调用耗时毫秒。
     */
    @Schema(description = "耗时毫秒")
    private Long costMs;

    /**
     * 失败说明。
     */
    @Schema(description = "失败说明")
    private String errorMessage;
}
