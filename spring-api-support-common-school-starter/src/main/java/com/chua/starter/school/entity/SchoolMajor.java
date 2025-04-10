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
 * 用于存储学校专业信息，包括专业名称、专业代码、专业类型等
 *
 * @author CH
 */
@Data
@TableName("school_major")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校专业")
public class SchoolMajor extends SysBase {

    /**
     * 专业ID，主键
     */
    @TableId(value = "school_major_id", type = IdType.AUTO)
    @Schema(description = "专业ID")
    private Long schoolMajorId;

    /**
     * 学校代码，关联学校信息
     */
    @TableField("school_code")
    @Schema(description = "学校代码")
    private String schoolCode;

    /**
     * 专业代码，用于标识专业
     */
    @TableField("school_major_code")
    @Schema(description = "专业代码")
    private String schoolMajorCode;

    /**
     * 专业名称，如计算机科学与技术、软件工程等
     */
    @TableField("school_major_name")
    @Schema(description = "专业名称")
    private String schoolMajorName;

    /**
     * 专业类型，如工学、理学、医学等
     */
    @TableField("school_major_type")
    @Schema(description = "专业类型")
    private String schoolMajorType;

    /**
     * 专业类型名称，专业类型的具体描述
     */
    @TableField("school_major_type_name")
    @Schema(description = "专业类型名称")
    private String schoolMajorTypeName;

    /**
     * 专业层次，如本科、专科、硕士、博士等
     */
    @TableField("school_major_level")
    @Schema(description = "专业层次")
    private String schoolMajorLevel;

    /**
     * 专业类别代码，用于分类专业
     */
    @TableField("school_major_category_code")
    @Schema(description = "专业类别代码")
    private String schoolMajorCategoryCode;

    /**
     * 专业类别名称，专业类别的具体描述
     */
    @TableField("school_major_category_name")
    @Schema(description = "专业类别名称")
    private String schoolMajorCategoryName;

    /**
     * 父级专业代码，用于表示专业的层级关系
     */
    @TableField("school_major_parent_code")
    @Schema(description = "父级专业代码")
    private String schoolMajorParentCode;

    /**
     * 专业类别标签，用于标记专业特点
     */
    @TableField("school_major_category_label")
    @Schema(description = "专业类别标签")
    private String schoolMajorCategoryLabel;

    /**
     * 专业年份，表示专业的开设年份或数据年份
     */
    @TableField("school_major_year")
    @Schema(description = "专业年份")
    private String schoolMajorYear;

    /**
     * 专业内容，描述专业的主要内容和特点
     */
    @TableField("school_major_content")
    @Schema(description = "专业内容")
    private String schoolMajorContent;

    /**
     * 专业课程，描述专业的主要课程设置
     */
    @TableField("school_major_course")
    @Schema(description = "专业课程")
    private String schoolMajorCourse;

    /**
     * 就业方向，描述专业的主要就业方向和岗位
     */
    @TableField("school_major_employment")
    @Schema(description = "就业方向")
    private String schoolMajorEmployment;

    /**
     * 软科等级，表示专业在软科排名中的等级
     */
    @TableField("school_major_ruan_ke_level")
    @Schema(description = "软科等级")
    private String schoolMajorRuanKeLevel;

    /**
     * 软科排名，表示专业在软科排名中的位置
     */
    @TableField("school_major_ruan_ke_rank")
    @Schema(description = "软科排名")
    private Integer schoolMajorRuanKeRank;

    /**
     * 学科排名，表示专业在学科评估中的排名
     */
    @TableField("school_major_xue_ke_rank")
    @Schema(description = "学科排名")
    private Integer schoolMajorXueKeRank;

    /**
     * 学科排名分数，表示专业在学科评估中的得分
     */
    @TableField("school_major_xue_ke_rank_score")
    @Schema(description = "学科排名分数")
    private Integer schoolMajorXueKeRankScore;
}