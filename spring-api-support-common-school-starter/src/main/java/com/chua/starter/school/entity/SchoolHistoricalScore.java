package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校历史分数线实体类
 *
 * @author CH
 */
@Data
@TableName("school_historical_score")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校历史分数线")
public class SchoolHistoricalScore extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "school_historical_score_id", type = IdType.AUTO)
    @Schema(description = "历史分数线ID")
    private Long schoolHistoricalScoreId;

    /**
     * 学校ID
     */
    @TableField("school_historical_score_school_id")
    @Schema(description = "学校ID")
    private Long schoolHistoricalScoreSchoolId;

    /**
     * 学校代码
     */
    @TableField("school_historical_score_school_code")
    @Schema(description = "学校代码")
    private String schoolHistoricalScoreSchoolCode;

    /**
     * 专业ID
     */
    @TableField("school_historical_score_major_id")
    @Schema(description = "专业ID")
    private Long schoolHistoricalScoreMajorId;

    /**
     * 专业代码
     */
    @TableField("school_historical_score_major_code")
    @Schema(description = "专业代码")
    private String schoolHistoricalScoreMajorCode;

    /**
     * 年份
     */
    @TableField("school_historical_score_year")
    @Schema(description = "年份")
    private Integer schoolHistoricalScoreYear;

    /**
     * 省份
     */
    @TableField("school_historical_score_province")
    @Schema(description = "省份")
    private String schoolHistoricalScoreProvince;

    /**
     * 科目类型（理科、文科、综合）
     */
    @TableField("school_historical_score_subject_type")
    @Schema(description = "科目类型")
    private String schoolHistoricalScoreSubjectType;

    /**
     * 批次（提前批、一批、二批等）
     */
    @TableField("school_historical_score_batch")
    @Schema(description = "批次")
    private String schoolHistoricalScoreBatch;

    /**
     * 招生类型（普通类、国家专项等）
     */
    @TableField("school_historical_score_admission_type")
    @Schema(description = "录取类型")
    private String schoolHistoricalScoreAdmissionType;

    /**
     * 最高分
     */
    @TableField("school_historical_score_max_score")
    @Schema(description = "最高分")
    private Integer schoolHistoricalScoreMaxScore;

    /**
     * 最低分
     */
    @TableField("school_historical_score_min_score")
    @Schema(description = "最低分")
    private Integer schoolHistoricalScoreMinScore;

    /**
     * 平均分
     */
    @TableField("school_historical_score_avg_score")
    @Schema(description = "平均分")
    private Integer schoolHistoricalScoreAvgScore;

    /**
     * 录取人数
     */
    @TableField("school_historical_score_admission_count")
    @Schema(description = "录取人数")
    private Integer schoolHistoricalScoreAdmissionCount;

    /**
     * 省控线
     */
    @TableField("school_historical_score_provincial_line")
    @Schema(description = "省控线")
    private Integer schoolHistoricalScoreProvincialLine;

    /**
     * 最低位次
     */
    @TableField("school_historical_score_lowest_rank")
    @Schema(description = "最低位次")
    private Integer schoolHistoricalScoreLowestRank;

    /**
     * 报考人数
     */
    @TableField("school_historical_score_applied_count")
    @Schema(description = "报名人数")
    private Integer schoolHistoricalScoreAppliedCount;

    /**
     * 录取率
     */
    @TableField("school_historical_score_admission_rate")
    @Schema(description = "录取率")
    private Double schoolHistoricalScoreAdmissionRate;

    /**
     * 备注
     */
    @TableField("school_historical_score_remark")
    @Schema(description = "备注")
    private String schoolHistoricalScoreRemark;

    /**
     * 删除标志（0-存在，1-删除）
     */
    @TableField("school_historical_score_deleted")
    @TableLogic
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolHistoricalScoreDeleted;
}