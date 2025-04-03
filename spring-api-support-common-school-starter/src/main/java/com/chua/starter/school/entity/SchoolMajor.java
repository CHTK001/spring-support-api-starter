package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校专业
 *
 * @author CH
 */
@Data
@TableName("school_major")
@Schema(description = "学校专业")
public class SchoolMajor {

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
     * 专业类别
     */
    @Schema(description = "专业类别")
    private String majorCategory;

    /**
     * 专业学科
     */
    @Schema(description = "专业学科")
    private String majorSubject;

    /**
     * 学历层次（本科、专科）
     */
    @Schema(description = "学历层次（本科、专科）")
    private String educationLevel;

    /**
     * 学位授予
     */
    @Schema(description = "学位授予")
    private String degree;

    /**
     * 修学年限
     */
    @Schema(description = "修学年限")
    private String studyDuration;

    /**
     * 开设年份
     */
    @Schema(description = "开设年份")
    private String establishYear;

    /**
     * 专业排名
     */
    @Schema(description = "专业排名")
    private Integer ranking;

    /**
     * 是否为特色专业（0-否，1-是）
     */
    @Schema(description = "是否为特色专业（0-否，1-是）")
    private Integer isFeatured;

    /**
     * 是否为重点专业（0-否，1-是）
     */
    @Schema(description = "是否为重点专业（0-否，1-是）")
    private Integer isKeyMajor;

    /**
     * 培养目标
     */
    @Schema(description = "培养目标")
    private String trainingObjective;

    /**
     * 核心课程
     */
    @Schema(description = "核心课程")
    private String coreCourses;

    /**
     * 就业方向
     */
    @Schema(description = "就业方向")
    private String careerProspects;

    /**
     * 专业详情链接
     */
    @Schema(description = "专业详情链接")
    private String detailUrl;

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