package com.chua.starter.service.pojo.dict;

import com.chua.starter.entity.service.SysDictItemProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * 规范构造器：属性列表做防御性拷贝。
     *
     * <p>value class 前置条件——集合组件必须深不可变。该列表由请求体反序列化而来，
     * 元素允许为 null，因此不能使用会拒绝 null 元素的 {@link List#copyOf(java.util.Collection)}；
     * 列表整体为 null 的语义也保持不变，服务层据此按「属性列表为空」直接返回成功。
     * {@code sysDictItemId} 允许为 null 时由服务层返回业务错误，故不做空值校验。</p>
     */
    public DictItemPropertyV1Request {
        property = property == null ? null : Collections.unmodifiableList(new ArrayList<>(property));
    }
}
