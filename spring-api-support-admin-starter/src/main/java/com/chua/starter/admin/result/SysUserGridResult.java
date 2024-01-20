package com.chua.starter.admin.result;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chua.common.support.json.JsonArray;
import com.chua.starter.common.support.serialize.JsonArrayToStringDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 结果
 * @author CH
 */
@Data
public class SysUserGridResult {

    /**
     * 布局
     */
    @TableField(value = "layout")
    @ApiModelProperty(value = "布局")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private JsonArray layout;

    /**
     * 组件
     */
    @TableField(value = "copms_list")
    @ApiModelProperty(value = "组件")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private JsonArray copmsList;
}
