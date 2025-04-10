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
 * 学校专业
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("school_major")
@Schema(description = "学校专业")
public class SchoolMajor extends SysBase {

    /**
     * 主键ID
     */
    @TableId(value = "school_major_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long schoolMajorId;

    /**
     * 学校Code
     */
    @TableField("school_code")
    @Schema(description = "学校Code")
    private String schoolCode;


    /**
     * 专业代码
     */
    @TableField("school_major_code")
    @Schema(description = "专业代码")
    private String schoolMajorCode;
    /**
     * 专业父级代码
     */
    @TableField("school_major_parent_code")
    @Schema(description = "专业父级代码")
    private String schoolMajorParentCode;

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
    @Schema(description = "学历层次")
    private String schoolMajorLevel;
    /**
     * 专业类别
     */
    @TableField("school_major_category_code")
    @Schema(description = "专业类别")
    private String schoolMajorCategoryCode;
    /**
     * 软文级别
     */
    @TableField("school_major_ruan_ke_level")
    @Schema(description = "软文级别")
    private String schoolMajorRuanKeLevel;
    /**
     * 专业级别
     */
    @TableField("school_major_ruan_ke_rank")
    @Schema(description = "专业级别")
    private String schoolMajorRuanKeRank;

    /**
     * 学科排名
     */
    @TableField("school_major_xue_ke_rank")
    @Schema(description = "学科排名")
    private String schoolMajorXueKeRankScore;
    
    /**
     * 专业年份
     */
    @TableField("school_major_year")
    @Schema(description = "专业年份")
    private String schoolMajorYear;
    
    /**
     * 专业类别名称
     */
    @TableField("school_major_category_name")
    @Schema(description = "专业类别名称")
    private String schoolMajorCategoryName;
    
    /**
     * 专业类别标签
     */
    @TableField("school_major_category_label")
    @Schema(description = "专业类别标签")
    private String schoolMajorCategoryLabel;
    
    /**
     * 学科排名分数
     */
    @TableField("school_major_xue_ke_rank_score")
    @Schema(description = "学科排名分数")
    private String schoolMajorXueKeRankScore;
    
    /**
     * 专业年份
     */
    @TableField("school_major_year")
    @Schema(description = "专业年份")
    private String schoolMajorYear;
    
    /**
     * 专业类别名称
     */
    @TableField("school_major_category_name")
    @Schema(description = "专业类别名称")
    private String schoolMajorCategoryName;
    
    /**
     * 专业类别标签
     */
    @TableField("school_major_category_label")
    @Schema(description = "专业类别标签")
    private String schoolMajorCategoryLabel;

    /**
     * 学科排名
     */
    @TableField("school_major_xue_ke_rank_score")
    @Schema(description = "学科排名")
    private String schoolMajorXueKeRankScore;
    /**
     * 专业类别名称
     */
    @TableField("school_major_category_name")
    @Schema(description = "专业类别名称")
    private String schoolMajorCategoryName;
    /**
     * 专业类别标签
     */
    @TableField("school_major_category_label")
    @Schema(description = "专业类别标签")
    private String schoolMajorCategoryLabel;


    /**
     * 年份
     */
    @TableField("school_major_year")
    @Schema(description = "年份")
    private String schoolMajorYear;

    /**
     * 专业排名
     */
    @TableField("school_major_ranking")
    @Schema(description = "专业排名")
    private Integer schoolMajorRanking;

    /**
     * 是否为特色专业（0-否，1-是）
     */
    @TableField("school_major_is_featured")
    @Schema(description = "是否为特色专业（0-否，1-是）")
    private Integer schoolMajorIsFeatured;

    /**
     * 是否为重点专业（0-否，1-是）
     */
    @TableField("school_major_is_key_major")
    @Schema(description = "是否为重点专业（0-否，1-是）")
    private Integer schoolMajorIsKeyMajor;

}