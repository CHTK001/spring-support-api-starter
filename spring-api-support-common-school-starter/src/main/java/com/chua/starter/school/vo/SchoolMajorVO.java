package com.chua.starter.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学校专业VO
 *
 * @author CH
 */
@Data
@Schema(description = "学校专业VO")
public class SchoolMajorVO {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long schoolMajorId;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID")
    private Long schoolMajorSchoolId;

    /**
     * 学校代码
     */
    @Schema(description = "学校代码")
    private String schoolMajorSchoolCode;

    /**
     * 学校名称
     */
    @Schema(description = "学校名称")
    private String schoolMajorSchoolName;

    /**
     * 专业代码
     */
    @Schema(description = "专业代码")
    private String schoolMajorCode;

    /**
     * 专业名称
     */
    @Schema(description = "专业名称")
    private String schoolMajorName;

    /**
     * 专业类别
     */
    @Schema(description = "专业类别")
    private String schoolMajorCategory;

    /**
     * 专业学科
     */
    @Schema(description = "专业学科")
    private String schoolMajorSubject;

    /**
     * 学历层次（本科、专科）
     */
    @Schema(description = "学历层次（本科、专科）")
    private String schoolMajorEducationLevel;

    /**
     * 学位授予
     */
    @Schema(description = "学位授予")
    private String schoolMajorDegree;

    /**
     * 修学年限
     */
    @Schema(description = "修学年限")
    private String schoolMajorStudyDuration;

    /**
     * 开设年份
     */
    @Schema(description = "开设年份")
    private String schoolMajorEstablishYear;

    /**
     * 专业排名
     */
    @Schema(description = "专业排名")
    private Integer schoolMajorRanking;

    /**
     * 是否为特色专业（0-否，1-是）
     */
    @Schema(description = "是否为特色专业（0-否，1-是）")
    private Integer schoolMajorIsFeatured;

    /**
     * 是否为重点专业（0-否，1-是）
     */
    @Schema(description = "是否为重点专业（0-否，1-是）")
    private Integer schoolMajorIsKeyMajor;

    /**
     * 培养目标
     */
    @Schema(description = "培养目标")
    private String schoolMajorTrainingObjective;

    /**
     * 核心课程
     */
    @Schema(description = "核心课程")
    private String schoolMajorCoreCourses;

    /**
     * 就业方向
     */
    @Schema(description = "就业方向")
    private String schoolMajorCareerProspects;

    /**
     * 专业详情链接
     */
    @Schema(description = "专业详情链接")
    private String schoolMajorDetailUrl;

    /**
     * 历年分数列表
     */
    @Schema(description = "历年分数列表")
    private List<SchoolHistoricalScoreVO> schoolMajorHistoricalScores;

    /**
     * 招生计划列表
     */
    @Schema(description = "招生计划列表")
    private List<SchoolAdmissionPlanVO> schoolMajorAdmissionPlans;
} 