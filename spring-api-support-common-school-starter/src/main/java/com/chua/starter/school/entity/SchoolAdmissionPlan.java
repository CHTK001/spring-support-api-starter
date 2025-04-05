package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招生计划
 *
 * @author CH
 */
@Data
@TableName("school_admission_plan")
@Schema(description = "招生计划")
public class SchoolAdmissionPlan {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long schoolAdmissionPlanId;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID")
    private Long schoolAdmissionPlanSchoolId;

    /**
     * 学校代码
     */
    @Schema(description = "学校代码")
    private String schoolAdmissionPlanSchoolCode;

    /**
     * 专业ID
     */
    @Schema(description = "专业ID")
    private Long schoolAdmissionPlanMajorId;

    /**
     * 专业代码
     */
    @Schema(description = "专业代码")
    private String schoolAdmissionPlanMajorCode;

    /**
     * 招生年份
     */
    @Schema(description = "招生年份")
    private Integer schoolAdmissionPlanYear;

    /**
     * 招生省份
     */
    @Schema(description = "招生省份")
    private String schoolAdmissionPlanProvince;

    /**
     * 科目类型（理科、文科、综合）
     */
    @Schema(description = "科目类型（理科、文科、综合）")
    private String schoolAdmissionPlanSubjectType;

    /**
     * 计划招生人数
     */
    @Schema(description = "计划招生人数")
    private Integer schoolAdmissionPlanPlanCount;

    /**
     * 实际招生人数
     */
    @Schema(description = "实际招生人数")
    private Integer schoolAdmissionPlanActualCount;

    /**
     * 学费（元/年）
     */
    @Schema(description = "学费（元/年）")
    private Double schoolAdmissionPlanTuition;

    /**
     * 招生批次（提前批、一批、二批等）
     */
    @Schema(description = "招生批次（提前批、一批、二批等）")
    private String schoolAdmissionPlanBatch;

    /**
     * 招生类型（普通类、国家专项等）
     */
    @Schema(description = "招生类型（普通类、国家专项等）")
    private String schoolAdmissionPlanAdmissionType;

    /**
     * 选科要求
     */
    @Schema(description = "选科要求")
    private String schoolAdmissionPlanSubjectRequirements;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String schoolAdmissionPlanRemark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime schoolAdmissionPlanCreateTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime schoolAdmissionPlanUpdateTime;
} 