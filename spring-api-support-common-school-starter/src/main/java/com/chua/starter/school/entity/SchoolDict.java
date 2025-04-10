package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字段
 */
@ApiModel(description = "字段")
@Schema(description = "字段")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "school_dict")
public class SchoolDict extends SysBase {
    @TableId(value = "school_dict_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer schoolDictId;

    /**
     * 字典编码
     */
    @TableField(value = "school_dict_code")
    @ApiModelProperty(value = "字典编码")
    @Schema(description = "字典编码")
    private String schoolDictCode;

    /**
     * 字典名称
     */
    @TableField(value = "school_dict_name")
    @ApiModelProperty(value = "字典名称")
    @Schema(description = "字典名称")
    private String schoolDictName;

    /**
     * 字典类型
     */
    @TableField(value = "school_dict_type")
    @ApiModelProperty(value = "字典类型")
    @Schema(description = "字典类型")
    private String schoolDictType;
}