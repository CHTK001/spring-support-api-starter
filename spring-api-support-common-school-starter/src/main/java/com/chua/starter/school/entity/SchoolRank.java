package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校排名实体类
 * 用于存储学校在各类排名中的位置信息，包括排名类型、排名值等
 *
 * @author CH
 */
@Data
@TableName("school_rank")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校排名")
public class SchoolRank extends SysBase {

    /**
     * 排名ID，主键
     */
    @TableId(value = "school_rank_id", type = IdType.AUTO)
    @Schema(description = "排名ID")
    private Long schoolRankId;

    /**
     * 学校代码，关联学校信息
     */
    @TableField("school_code")
    @Schema(description = "学校代码")
    private String schoolCode;

    /**
     * 排名值，表示学校在该排名中的位置
     */
    @TableField("school_rank_value")
    @Schema(description = "排名值")
    private Integer schoolRankValue;

    /**
     * 排名类型，如QS排名、软科排名、校友会排名等
     */
    @TableField("school_rank_type")
    @Schema(description = "排名类型")
    private String schoolRankType;

    /**
     * 排名名称，对排名的具体描述
     */
    @TableField("school_rank_name")
    @Schema(description = "排名名称")
    private String schoolRankName;
    
    /**
     * 排名年份，表示排名的年度
     */
    @TableField("school_rank_year")
    @Schema(description = "排名年份")
    private Integer schoolRankYear;
    
    /**
     * 排名机构，表示发布排名的机构
     */
    @TableField("school_rank_institution")
    @Schema(description = "排名机构")
    private String schoolRankInstitution;
    
    /**
     * 排名得分，表示在排名中的得分
     */
    @TableField("school_rank_score")
    @Schema(description = "排名得分")
    private Double schoolRankScore;
    
    /**
     * 排名变化，与上一年相比的排名变化，正数表示上升，负数表示下降
     */
    @TableField("school_rank_change")
    @Schema(description = "排名变化")
    private Integer schoolRankChange;
    
    /**
     * 排名备注，补充说明排名的特殊情况
     */
    @TableField("school_rank_remark")
    @Schema(description = "排名备注")
    private String schoolRankRemark;
    
    /**
     * 删除标志，0-存在，1-删除
     */
    @TableField("school_rank_deleted")
    @TableLogic
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolRankDeleted;
}