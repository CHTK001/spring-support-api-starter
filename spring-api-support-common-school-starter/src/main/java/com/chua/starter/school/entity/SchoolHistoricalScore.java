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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 