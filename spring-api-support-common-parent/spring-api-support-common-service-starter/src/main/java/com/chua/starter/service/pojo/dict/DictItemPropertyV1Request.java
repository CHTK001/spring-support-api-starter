package com.chua.starter.service.pojo.dict;

import com.chua.starter.entity.service.SysDictItemProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 字典子项属性
 *
 * @since 2025/9/28
 * @author CH
 */
@Data
@Schema(description = "字典子项属性")
public class DictItemPropertyV1Request {

    /**
     * 字典子项标识
     */
    @Schema(description = "字典子项ID")
    /** sysdictitemid */
    private Integer sysDictItemId;

    /**
     * 字典子项属性
     */
    @Schema(description = "字典子项属性")
    /** 财产 */
    private List<SysDictItemProperty> property;
}
