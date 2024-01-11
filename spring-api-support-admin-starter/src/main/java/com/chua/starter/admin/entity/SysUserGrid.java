package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.common.support.serialize.JsonArrayToStringDeserialize;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户首页布局
 *
 * @author CH
 */
@Schema(description = "sys_user_grid")
@Data
@TableName(value = "sys_user_grid")
@JsonIgnoreProperties({"grid", "userId"})
public class SysUserGrid extends SysTenantBase {
    /**
     * 主键
     */
    @TableId(value = "grid", type = IdType.AUTO)
    @Schema(title = "主键")
    private Long grid;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @Schema(title = "用户ID")
    private Long userId;

    /**
     * 布局
     */
    @TableField(value = "layout")
    @Schema(title = "布局")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private String layout;

    /**
     * 组件
     */
    @TableField(value = "copms_list")
    @Schema(title = "组件")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private String copmsList;
}