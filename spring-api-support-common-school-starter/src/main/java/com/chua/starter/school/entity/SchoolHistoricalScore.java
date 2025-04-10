package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校历史分数线实体类
 * 用于存储学校历年招生分数线信息，包括最高分、最低分、平均分、省控线等
 *
 * @author CH
 */
@Data
@TableName("school_historical_score")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校历史分数线")
public class SchoolHistoricalScore extends SysBase {

    /**
     * 历史分数线ID，主键
     */
    @TableId(value = "school_historical_score_id", type = IdType.AUTO)
    @Schema(description = "历史分数线ID")
    private Long schoolHistoricalScoreId;

    /**
     * 学校ID，关联学校信息
     */
    @TableField("school_historical_score_school_id")
    @Schema(description = "学校ID")
    private Long schoolHistoricalScoreSchoolId;

    /**
     * 学校代码，用于关联学校
     */
    @TableField("school_historical_score_school_code")
    @Schema(description = "学校代码")
    private String schoolHistoricalScoreSchoolCode;

    /**
     * 专业ID，关联专业信息
     */
    @TableField("school_historical_score_major_id")
    @Schema(description = "专业ID")
    private Long schoolHistoricalScoreMajorId;

    /**
     * 专业代码，用于关联专业
     */
    @TableField("school_historical_score_major_code")
    @Schema(description = "专业代码")
    private String schoolHistoricalScoreMajorCode;

    /**
     * 年份，表示分数线的年度
     */
    @TableField("school_historical_score_year")
    @Schema(description = "年份")
    private Integer schoolHistoricalScoreYear;

    /**
     * 省份，表示分数线所属的省份或地区
     */
    @TableField("school_historical_score_province")
    @Schema(description = "省份")
    private String schoolHistoricalScoreProvince;

    /**
     * 科目类型，如理科、文科、综合等
     */
    @TableField("school_historical_score_subject_type")
    @Schema(description = "科目类型")
    private String schoolHistoricalScoreSubjectType;

    /**
     * 批次，如提前批、本科一批、本科二批等
     */
    @TableField("school_historical_score_batch")
    @Schema(description = "批次")
    private String schoolHistoricalScoreBatch;

    /**
     * 录取类型，如普通类、国家专项等
     */
    @TableField("school_historical_score_admission_type")
    @Schema(description = "录取类型")
    private String schoolHistoricalScoreAdmissionType;

    /**
     * 最高分，表示录取的最高分数
     */
    @TableField("school_historical_score_max_score")
    @Schema(description = "最高分")
    private Integer schoolHistoricalScoreMaxScore;

    /**
     * 最低分，表示录取的最低分数
     */
    @TableField("school_historical_score_min_score")
    @Schema(description = "最低分")
    private Integer schoolHistoricalScoreMinScore;

    /**
     * 平均分，表示录取的平均分数
     */
    @TableField("school_historical_score_avg_score")
    @Schema(description = "平均分")
    private Integer schoolHistoricalScoreAvgScore;

    /**
     * 录取人数，表示录取的学生数量
     */
    @TableField("school_historical_score_admission_count")
    @Schema(description = "录取人数")
    private Integer schoolHistoricalScoreAdmissionCount;

    /**
     * 省控线，表示省份的最低录取控制线
     */
    @TableField("school_historical_score_provincial_line")
    @Schema(description = "省控线")
    private Integer schoolHistoricalScoreProvincialLine;

    /**
     * 最低位次，表示录取的最低排名位次
     */
    @TableField("school_historical_score_lowest_rank")
    @Schema(description = "最低位次")
    private Integer schoolHistoricalScoreLowestRank;

    /**
     * 报考人数，表示报考该专业的学生数量
     */
    @TableField("school_historical_score_applied_count")
    @Schema(description = "报名人数")
    private Integer schoolHistoricalScoreAppliedCount;

    /**
     * 录取率，表示录取人数与报考人数的比率
     */
    @TableField("school_historical_score_admission_rate")
    @Schema(description = "录取率")
    private Double schoolHistoricalScoreAdmissionRate;

    /**
     * 备注，补充说明分数线的特殊情况
     */
    @TableField("school_historical_score_remark")
    @Schema(description = "备注")
    private String schoolHistoricalScoreRemark;

    /**
     * 删除标志，0-存在，1-删除
     */
    @TableField("school_historical_score_deleted")
    @TableLogic
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolHistoricalScoreDeleted;
}