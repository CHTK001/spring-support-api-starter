package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.common.support.serialize.JsonArrayToStringDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户首页布局
 * @author CH
 */
@ApiModel(description = "sys_user_grid")
@Data
@TableName(value = "sys_user_grid")
@JsonIgnoreProperties({"grid", "userId"})
public class SysUserGrid {
    /**
     * 主键
     */
    @TableId(value = "grid", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long grid;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    /**
     * 布局
     */
    @TableField(value = "layout")
    @ApiModelProperty(value = "布局")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private String layout;

    /**
     * 组件
     */
    @TableField(value = "copms_list")
    @ApiModelProperty(value = "组件")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private String copmsList;
}