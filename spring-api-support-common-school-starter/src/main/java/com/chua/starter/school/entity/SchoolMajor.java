package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
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
     * 学校ID
     */
    @TableField("school_major_school_id")
    @Schema(description = "学校ID")
    private Long schoolMajorSchoolId;

    /**
     * 学校代码
     */
    @TableField("school_major_school_code")
    @Schema(description = "学校代码")
    private String schoolMajorSchoolCode;

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
     * 专业类别
     */
    @TableField("school_major_category")
    @Schema(description = "专业类别")
    private String schoolMajorCategory;

    /**
     * 专业学科
     */
    @TableField("school_major_subject")
    @Schema(description = "专业学科")
    private String schoolMajorSubject;

    /**
     * 学历层次（本科、专科）
     */
    @TableField("school_major_education_level")
    @Schema(description = "学历层次（本科、专科）")
    private String schoolMajorEducationLevel;

    /**
     * 学位授予
     */
    @TableField("school_major_degree")
    @Schema(description = "学位授予")
    private String schoolMajorDegree;

    /**
     * 修学年限
     */
    @TableField("school_major_study_duration")
    @Schema(description = "修学年限")
    private String schoolMajorStudyDuration;

    /**
     * 开设年份
     */
    @TableField("school_major_establish_year")
    @Schema(description = "开设年份")
    private String schoolMajorEstablishYear;

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

    /**
     * 培养目标
     */
    @TableField("school_major_training_objective")
    @Schema(description = "培养目标")
    private String schoolMajorTrainingObjective;

    /**
     * 核心课程
     */
    @TableField("school_major_core_courses")
    @Schema(description = "核心课程")
    private String schoolMajorCoreCourses;

    /**
     * 就业方向
     */
    @TableField("school_major_career_prospects")
    @Schema(description = "就业方向")
    private String schoolMajorCareerProspects;

    /**
     * 专业详情链接
     */
    @TableField("school_major_detail_url")
    @Schema(description = "专业详情链接")
    private String schoolMajorDetailUrl;

    /**
     * 删除标志（0-存在，1-删除）
     */
    @TableField("school_major_deleted")
    @TableLogic
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolMajorDeleted;
}