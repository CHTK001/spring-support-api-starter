package com.chua.starter.admin.result;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
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
    private JSONArray layout;

    /**
     * 组件
     */
    @TableField(value = "copms_list")
    @ApiModelProperty(value = "组件")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private JSONArray copmsList;
}
