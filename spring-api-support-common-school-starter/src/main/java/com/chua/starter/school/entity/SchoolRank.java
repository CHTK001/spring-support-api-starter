package com.chua.starter.school.entity;

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
 * 排名
 */
@ApiModel(description = "排名")
@Schema(description = "排名")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "school_rank")
public class SchoolRank extends SysBase {
    @TableId(value = "school_rank_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer schoolRankId;

    /**
     * 排名
     */
    @TableField(value = "school_rank_value")
    @ApiModelProperty(value = "排名")
    @Schema(description = "排名")
    private String schoolRankValue;

    /**
     * 排名类型
     */
    @TableField(value = "school_rank_type")
    @ApiModelProperty(value = "排名类型")
    @Schema(description = "排名类型")
    private String schoolRankType;

    /**
     * 排名名称
     */
    @TableField(value = "school_rank_name")
    @ApiModelProperty(value = "排名名称")
    @Schema(description = "排名名称")
    private String schoolRankName;

    /**
     * 学校编码
     */
    @TableField(value = "school_code")
    @ApiModelProperty(value = "学校编码")
    @Schema(description = "学校编码")
    private String schoolCode;
}