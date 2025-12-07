package com.chua.starter.service.pojo.dict;

import com.chua.starter.system.entity.SysDictItemProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 字典子项属�?
 *
 * @author CH
 */
@Data
@Schema(description = "字典子项属�?)
public class DictItemPropertyV1Request {

    /**
     * 字典子项ID
     */
    @Schema(description = "字典子项ID")
    private Integer sysDictItemId;

    /**
     * 字典子项属�?
     */
    @Schema(description = "字典子项属�?)
    private List<SysDictItemProperty> property;
}
