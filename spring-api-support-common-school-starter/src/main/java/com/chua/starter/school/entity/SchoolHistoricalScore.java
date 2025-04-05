package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历年分数
 *
 * @author CH
 */
@Data
@TableName("school_historical_score")
@Schema(description = "历年分数")
public class SchoolHistoricalScore {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime schoolHistoricalScoreCreateTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime schoolHistoricalScoreUpdateTime;
} 