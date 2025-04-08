package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 招生计划
 *
 * @author CH
 * 
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("school_admission_plan")
@Schema(description = "招生计划")
public class SchoolAdmissionPlan extends SysBase {

    /**
     * 主键ID
     */
    @TableId(value = "school_admission_plan_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long schoolAdmissionPlanId;

    /**
     * 学校ID
     */
    @TableField("school_admission_plan_school_id")
    @Schema(description = "学校ID")
    private Long schoolAdmissionPlanSchoolId;

    /**
     * 学校代码
     */
    @TableField("school_admission_plan_school_code")
    @Schema(description = "学校代码")
    private String schoolAdmissionPlanSchoolCode;

    /**
     * 专业ID
     */
    @TableField("school_admission_plan_major_id")
    @Schema(description = "专业ID")
    private String schoolAdmissionPlanMajorId;

    /**
     * 专业代码
     */
    @TableField("school_admission_plan_major_code")
    @Schema(description = "专业代码")
    private String schoolAdmissionPlanMajorCode;

    /**
     * 招生年份
     */
    @TableField("school_admission_plan_year")
    @Schema(description = "招生年份")
    private Integer schoolAdmissionPlanYear;

    /**
     * 招生省份
     */
    @TableField("school_admission_plan_province")
    @Schema(description = "招生省份")
    private String schoolAdmissionPlanProvince;

    /**
     * 科目类型（理科、文科、综合）
     */
    @TableField("school_admission_plan_subject_type")
    @Schema(description = "科目类型（理科、文科、综合）")
    private String schoolAdmissionPlanSubjectType;

    /**
     * 计划招生人数
     */
    @TableField("school_admission_plan_plan_count")
    @Schema(description = "计划招生人数")
    private Integer schoolAdmissionPlanPlanCount;

    /**
     * 实际招生人数
     */
    @TableField("school_admission_plan_actual_count")
    @Schema(description = "实际招生人数")
    private Integer schoolAdmissionPlanActualCount;

    /**
     * 学费（元/年）
     */
    @TableField("school_admission_plan_tuition")
    @Schema(description = "学费（元/年）")
    private Double schoolAdmissionPlanTuition;

    /**
     * 招生批次（提前批、一批、二批等）
     */
    @TableField("school_admission_plan_batch")
    @Schema(description = "招生批次（提前批、一批、二批等）")
    private String schoolAdmissionPlanBatch;

    /**
     * 招生类型（普通类、国家专项等）
     */
    @TableField("school_admission_plan_admission_type")
    @Schema(description = "招生类型（普通类、国家专项等）")
    private String schoolAdmissionPlanAdmissionType;

    /**
     * 选科要求
     */
    @TableField("school_admission_plan_subject_requirements")
    @Schema(description = "选科要求")
    private String schoolAdmissionPlanSubjectRequirements;

    /**
     * 备注
     */
    @TableField("school_admission_plan_remark")
    @Schema(description = "备注")
    private String schoolAdmissionPlanRemark;

    /**
     * 删除标志（0-存在，1-删除）
     */
    @TableField("school_admission_plan_deleted")
    @TableLogic
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolAdmissionPlanDeleted;
}