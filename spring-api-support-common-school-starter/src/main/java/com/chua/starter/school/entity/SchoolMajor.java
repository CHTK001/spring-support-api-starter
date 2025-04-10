package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校专业实体类
 *
 * @author CH
 */
@Data
@TableName("school_major")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校专业")
public class SchoolMajor extends SysBase {

    /**
     * 主键ID
     */
    @TableId(value = "school_major_id", type = IdType.AUTO)
    @Schema(description = "专业ID")
    private Long schoolMajorId;

    /**
     * 学校Code
     */
    @TableField("school_code")
    @Schema(description = "学校代码")
    private String schoolCode;

    /**
     * 专业代码
     */
    @TableField("school_major_code")
    @Schema(description = "专业代码")
    private String schoolMajorCode;

    /**
     * 专业名称
     */
    @TableField("school_major_name")
    @Schema(description = "专业名称")
    private String schoolMajorName;

    /**
     * 专业类型
     */
    @TableField("school_major_type")
    @Schema(description = "专业类型")
    private String schoolMajorType;

    /**
     * 专业类型名称
     */
    @TableField("school_major_type_name")
    @Schema(description = "专业类型名称")
    private String schoolMajorTypeName;

    /**
     * 学历层次（本科、专科）
     */
    @TableField("school_major_level")
    @Schema(description = "专业层次")
    private String schoolMajorLevel;

    /**
     * 专业类别
     */
    @TableField("school_major_category_code")
    @Schema(description = "专业类别代码")
    private String schoolMajorCategoryCode;

    /**
     * 专业类别名称
     */
    @TableField("school_major_category_name")
    @Schema(description = "专业类别名称")
    private String schoolMajorCategoryName;

    /**
     * 专业父级代码
     */
    @TableField("school_major_parent_code")
    @Schema(description = "父级专业代码")
    private String schoolMajorParentCode;

    /**
     * 专业类别标签
     */
    @TableField("school_major_category_label")
    @Schema(description = "专业类别标签")
    private String schoolMajorCategoryLabel;

    /**
     * 专业年份
     */
    @TableField("school_major_year")
    @Schema(description = "专业年份")
    private String schoolMajorYear;

    /**
     * 专业内容
     */
    @TableField("school_major_content")
    @Schema(description = "专业内容")
    private String schoolMajorContent;

    /**
     * 专业课程
     */
    @TableField("school_major_course")
    @Schema(description = "专业课程")
    private String schoolMajorCourse;

    /**
     * 就业方向
     */
    @TableField("school_major_employment")
    @Schema(description = "就业方向")
    private String schoolMajorEmployment;

    /**
     * 软文级别
     */
    @TableField("school_major_ruan_ke_level")
    @Schema(description = "软科等级")
    private String schoolMajorRuanKeLevel;

    /**
     * 专业级别
     */
    @TableField("school_major_ruan_ke_rank")
    @Schema(description = "软科排名")
    private Integer schoolMajorRuanKeRank;

    /**
     * 学科排名
     */
    @TableField("school_major_xue_ke_rank")
    @Schema(description = "学科排名")
    private Integer schoolMajorXueKeRank;

    /**
     * 学科排名分数
     */
    @TableField("school_major_xue_ke_rank_score")
    @Schema(description = "学科排名分数")
    private Integer schoolMajorXueKeRankScore;
}