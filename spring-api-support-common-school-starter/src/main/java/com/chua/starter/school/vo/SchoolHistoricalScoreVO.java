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
    private Long schoolHistoricalScoreId;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID")
    private Long schoolHistoricalScoreSchoolId;

    /**
     * 学校代码
     */
    @Schema(description = "学校代码")
    private String schoolHistoricalScoreSchoolCode;

    /**
     * 学校名称
     */
    @Schema(description = "学校名称")
    private String schoolHistoricalScoreSchoolName;

    /**
     * 专业ID
     */
    @Schema(description = "专业ID")
    private Long schoolHistoricalScoreMajorId;

    /**
     * 专业代码
     */
    @Schema(description = "专业代码")
    private String schoolHistoricalScoreMajorCode;

    /**
     * 专业名称
     */
    @Schema(description = "专业名称")
    private String schoolHistoricalScoreMajorName;

    /**
     * 年份
     */
    @Schema(description = "年份")
    private Integer schoolHistoricalScoreYear;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String schoolHistoricalScoreProvince;

    /**
     * 科目类型（理科、文科、综合）
     */
    @Schema(description = "科目类型（理科、文科、综合）")
    private String schoolHistoricalScoreSubjectType;

    /**
     * 批次（提前批、一批、二批等）
     */
    @Schema(description = "批次（提前批、一批、二批等）")
    private String schoolHistoricalScoreBatch;

    /**
     * 招生类型（普通类、国家专项等）
     */
    @Schema(description = "招生类型（普通类、国家专项等）")
    private String schoolHistoricalScoreAdmissionType;

    /**
     * 最高分
     */
    @Schema(description = "最高分")
    private Integer schoolHistoricalScoreMaxScore;

    /**
     * 最低分
     */
    @Schema(description = "最低分")
    private Integer schoolHistoricalScoreMinScore;

    /**
     * 平均分
     */
    @Schema(description = "平均分")
    private Integer schoolHistoricalScoreAvgScore;

    /**
     * 录取人数
     */
    @Schema(description = "录取人数")
    private Integer schoolHistoricalScoreAdmissionCount;

    /**
     * 省控线
     */
    @Schema(description = "省控线")
    private Integer schoolHistoricalScoreProvincialLine;

    /**
     * 最低位次
     */
    @Schema(description = "最低位次")
    private Integer schoolHistoricalScoreLowestRank;

    /**
     * 录取率
     */
    @Schema(description = "录取率")
    private Double schoolHistoricalScoreAdmissionRate;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String schoolHistoricalScoreRemark;
} 