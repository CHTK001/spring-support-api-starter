package com.chua.starter.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 历年分数VO
 *
 * @author CH
 */
@Data
@Schema(description = "历年分数VO")
public class SchoolHistoricalScoreVO {

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
     * 年份
     */
    @Schema(description = "年份")
    private Integer year;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 科目类型（理科、文科、综合）
     */
    @Schema(description = "科目类型（理科、文科、综合）")
    private String subjectType;

    /**
     * 批次（提前批、一批、二批等）
     */
    @Schema(description = "批次（提前批、一批、二批等）")
    private String batch;

    /**
     * 招生类型（普通类、国家专项等）
     */
    @Schema(description = "招生类型（普通类、国家专项等）")
    private String admissionType;

    /**
     * 最高分
     */
    @Schema(description = "最高分")
    private Integer maxScore;

    /**
     * 最低分
     */
    @Schema(description = "最低分")
    private Integer minScore;

    /**
     * 平均分
     */
    @Schema(description = "平均分")
    private Integer avgScore;

    /**
     * 录取人数
     */
    @Schema(description = "录取人数")
    private Integer admissionCount;

    /**
     * 省控线
     */
    @Schema(description = "省控线")
    private Integer provincialLine;

    /**
     * 最低位次
     */
    @Schema(description = "最低位次")
    private Integer lowestRank;

    /**
     * 录取率
     */
    @Schema(description = "录取率")
    private Double admissionRate;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
} 