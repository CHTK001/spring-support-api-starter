package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校招生计划实体类
 * 用于存储学校招生计划信息，包括招生专业、招生人数、学费、招生批次等
 *
 * @author CH
 */
@Data
@TableName("school_admission_plan")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校招生计划")
public class SchoolAdmissionPlan extends SysBase {

    /**
     * 招生计划ID，主键
     */
    @TableId(value = "school_admission_plan_id", type = IdType.AUTO)
    @Schema(description = "招生计划ID")
    private Long schoolAdmissionPlanId;

    /**
     * 学校ID，关联学校信息
     */
    @TableField("school_admission_plan_school_id")
    @Schema(description = "学校ID")
    private Long schoolAdmissionPlanSchoolId;

    /**
     * 学校代码，用于关联学校
     */
    @TableField("school_admission_plan_school_code")
    @Schema(description = "学校代码")
    private String schoolAdmissionPlanSchoolCode;

    /**
     * 招生计划名称，描述招生计划的名称
     */
    @TableField("school_admission_plan_name")
    @Schema(description = "招生计划名称")
    private String schoolAdmissionPlanName;

    /**
     * 招生计划备注，补充说明招生计划的特点和要求
     */
    @TableField("school_admission_plan_remark")
    @Schema(description = "招生计划备注")
    private String schoolAdmissionPlanRemark;

    /**
     * 招生计划年份，表示招生的年度
     */
    @TableField("school_admission_plan_year")
    @Schema(description = "招生计划年份")
    private Integer schoolAdmissionPlanYear;

    /**
     * 招生计划省份，表示招生的地区范围
     */
    @TableField("school_admission_plan_province")
    @Schema(description = "招生计划省份")
    private String schoolAdmissionPlanProvince;

    /**
     * 招生计划批次，如提前批、本科一批、本科二批等
     */
    @TableField("school_admission_plan_batch")
    @Schema(description = "招生计划批次")
    private String schoolAdmissionPlanBatch;

    /**
     * 招生计划专业名称，表示招生的专业
     */
    @TableField("school_admission_plan_major_name")
    @Schema(description = "招生计划专业名称")
    private String schoolAdmissionPlanMajorName;

    /**
     * 招生计划人数，表示计划招收的学生数量
     */
    @TableField("school_admission_plan_number")
    @Schema(description = "招生计划人数")
    private Integer schoolAdmissionPlanNumber;

    /**
     * 招生计划学费，表示专业的学费标准
     */
    @TableField("school_admission_plan_tuition")
    @Schema(description = "招生计划学费")
    private Double schoolAdmissionPlanTuition;

    /**
     * 招生计划学制，表示专业的学习年限
     */
    @TableField("school_admission_plan_length")
    @Schema(description = "招生计划学制")
    private Integer schoolAdmissionPlanLength;

    /**
     * 招生计划科目要求，表示专业对高考科目的要求
     */
    @TableField("school_admission_plan_subject_requirements")
    @Schema(description = "招生计划科目要求")
    private String schoolAdmissionPlanSubjectRequirements;

    /**
     * 专业ID，关联专业信息
     */
    @TableField("school_admission_plan_major_id")
    @Schema(description = "专业ID")
    private String schoolAdmissionPlanMajorId;

    /**
     * 专业代码，用于关联专业
     */
    @TableField("school_admission_plan_major_code")
    @Schema(description = "专业代码")
    private String schoolAdmissionPlanMajorCode;

    /**
     * 实际招生人数，表示最终录取的学生数量
     */
    @TableField("school_admission_plan_actual_count")
    @Schema(description = "实际招生人数")
    private Integer schoolAdmissionPlanActualCount;

    /**
     * 科目类型，如理科、文科、综合等
     */
    @TableField("school_admission_plan_subject_type")
    @Schema(description = "科目类型（理科、文科、综合）")
    private String schoolAdmissionPlanSubjectType;

    /**
     * 计划招生人数，表示初始计划招收的学生数量
     */
    @TableField("school_admission_plan_plan_count")
    @Schema(description = "计划招生人数")
    private Integer schoolAdmissionPlanPlanCount;

    /**
     * 招生类型，如普通类、国家专项等
     */
    @TableField("school_admission_plan_admission_type")
    @Schema(description = "招生类型（普通类、国家专项等）")
    private String schoolAdmissionPlanAdmissionType;

    /**
     * 删除标志，0-存在，1-删除
     */
    @TableField("school_admission_plan_deleted")
    @TableLogic
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolAdmissionPlanDeleted;
}