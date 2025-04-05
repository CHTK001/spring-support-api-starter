package com.chua.starter.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 招生计划VO
 *
 * @author CH
 */
@Data
@Schema(description = "招生计划VO")
public class SchoolAdmissionPlanVO {

    /**
     * 主键ID
     */
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
     * 学校名称
     */
    @Schema(description = "学校名称")
    private String schoolAdmissionPlanSchoolName;

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
     * 专业名称
     */
    @Schema(description = "专业名称")
    private String schoolAdmissionPlanMajorName;

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
} 