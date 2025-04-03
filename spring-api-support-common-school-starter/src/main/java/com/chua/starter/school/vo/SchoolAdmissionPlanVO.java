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
    private Long id;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID")
    private Long schoolId;

    /**
     * 学校代码
     */
    @Schema(description = "学校代码")
    private String schoolCode;

    /**
     * 学校名称
     */
    @Schema(description = "学校名称")
    private String schoolName;

    /**
     * 专业ID
     */
    @Schema(description = "专业ID")
    private Long majorId;

    /**
     * 专业代码
     */
    @Schema(description = "专业代码")
    private String majorCode;

    /**
     * 专业名称
     */
    @Schema(description = "专业名称")
    private String majorName;

    /**
     * 招生年份
     */
    @Schema(description = "招生年份")
    private Integer year;

    /**
     * 招生省份
     */
    @Schema(description = "招生省份")
    private String province;

    /**
     * 科目类型（理科、文科、综合）
     */
    @Schema(description = "科目类型（理科、文科、综合）")
    private String subjectType;

    /**
     * 计划招生人数
     */
    @Schema(description = "计划招生人数")
    private Integer planCount;

    /**
     * 实际招生人数
     */
    @Schema(description = "实际招生人数")
    private Integer actualCount;

    /**
     * 学费（元/年）
     */
    @Schema(description = "学费（元/年）")
    private Double tuition;

    /**
     * 招生批次（提前批、一批、二批等）
     */
    @Schema(description = "招生批次（提前批、一批、二批等）")
    private String batch;

    /**
     * 招生类型（普通类、国家专项等）
     */
    @Schema(description = "招生类型（普通类、国家专项等）")
    private String admissionType;

    /**
     * 选科要求
     */
    @Schema(description = "选科要求")
    private String subjectRequirements;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
} 