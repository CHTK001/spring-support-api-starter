package com.chua.starter.system.pojo.dict;

import com.chua.starter.system.entity.SysDictItemProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 字典子项属性
 *
 * @author CH
 */
@Data
@Schema(description = "字典子项属性")
public class DictItemPropertyV1Request {

    /**
     * 字典子项ID
     */
    @Schema(description = "字典子项ID")
    private Integer sysDictItemId;

    /**
     * 字典子项属性
     */
    @Schema(description = "字典子项属性")
    private List<SysDictItemProperty> property;
}
