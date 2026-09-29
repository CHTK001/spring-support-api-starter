package com.chua.starter.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义函数试跑入参。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "自定义函数试跑入参")
public class CollectFunctionTestDTO {

    /**
     * 函数ID。
     */
    @Schema(description = "函数ID")
    private Integer collectFunctionId;

    /**
     * 位置实参，按闭包形参顺序填写，全部以文本传入。
     */
    @Schema(description = "位置实参")
    private List<String> arguments = new ArrayList<>();
}
