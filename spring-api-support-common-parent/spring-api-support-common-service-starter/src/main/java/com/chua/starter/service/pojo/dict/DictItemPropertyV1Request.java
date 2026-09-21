package com.chua.starter.service.pojo.dict;

import com.chua.starter.entity.service.SysDictItemProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 字典子项属性
 *
 * @param sysDictItemId 字典子项标识
 * @param property 字典子项属性
 *
 * @since 2025/9/28
 * @author CH
 */
@Schema(description = "字典子项属性")
public record DictItemPropertyV1Request(
        @Schema(description = "字典子项ID") Integer sysDictItemId,
        @Schema(description = "字典子项属性") List<SysDictItemProperty> property
) {
}
