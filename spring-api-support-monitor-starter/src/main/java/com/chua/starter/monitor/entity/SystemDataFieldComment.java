package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据管理-字段注释
 *
 * 记录特定数据源(`systemDataFieldCommentSettingId`)下某个节点(`systemDataFieldCommentNodePath`)或具体的库/表/列的注释。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("数据管理-字段注释")
@Schema(name = "SystemDataFieldComment")
@TableName("system_data_field_comment")
public class SystemDataFieldComment extends SysBase {

    @TableId(value = "system_data_field_comment_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    private Integer systemDataFieldCommentId;

    @TableField("system_data_field_comment_setting_id")
    @ApiModelProperty("数据源配置ID")
    @Schema(description = "数据源配置ID")
    private Integer systemDataFieldCommentSettingId;

    @TableField("system_data_field_comment_node_path")
    @ApiModelProperty("节点路径（唯一键之一）")
    @Schema(description = "节点路径（唯一键之一）")
    private String systemDataFieldCommentNodePath;

    @TableField("system_data_field_comment_database_name")
    @ApiModelProperty("库/Database 名称")
    @Schema(description = "库/Database 名称")
    private String systemDataFieldCommentDatabaseName;

    @TableField("system_data_field_comment_table_name")
    @ApiModelProperty("表名")
    @Schema(description = "表名")
    private String systemDataFieldCommentTableName;

    @TableField("system_data_field_comment_column_name")
    @ApiModelProperty("列名")
    @Schema(description = "列名")
    private String systemDataFieldCommentColumnName;

    @TableField("system_data_field_comment_comment")
    @ApiModelProperty("注释内容")
    @Schema(description = "注释内容")
    private String systemDataFieldCommentComment;
}


