package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校年级实体类
 * 用于存储学校年级信息，包括年级名称、年级代码、年级类型等
 *
 * @author CH
 */
@Data
@TableName("school_grade")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校年级")
public class SchoolGrade extends SysBase {

    /**
     * 年级ID，主键
     */
    @TableId("school_grade_id")
    @Schema(description = "年级ID")
    private Long schoolGradeId;

    /**
     * 学校ID
     */
    @TableField("school_grade_school_id")
    @Schema(description = "学校ID")
    private Long schoolGradeSchoolId;

    /**
     * 学校代码
     */
    @TableField("school_grade_school_code")
    private String schoolGradeSchoolCode;

    /**
     * 年级名称，如一年级、二年级等
     */
    @TableField("school_grade_name")
    @Schema(description = "年级名称")
    private String schoolGradeName;

    /**
     * 年级代码，用于标识年级
     */
    @TableField("school_grade_code")
    @Schema(description = "年级代码")
    private String schoolGradeCode;

    /**
     * 学制阶段（1-小学，2-初中，3-高中）
     */
    @TableField("school_grade_phase")
    private Integer schoolGradePhase;

    /**
     * 入学年份
     */
    @TableField("school_grade_enroll_year")
    private Integer schoolGradeEnrollYear;

    /**
     * 毕业年份
     */
    @TableField("school_grade_graduate_year")
    private Integer schoolGradeGraduateYear;

    /**
     * 班级数量
     */
    @TableField("school_grade_class_count")
    private Integer schoolGradeClassCount;

    /**
     * 学生数量
     */
    @TableField("school_grade_student_count")
    private Integer schoolGradeStudentCount;

    /**
     * 年级主任ID
     */
    @TableField("school_grade_director_id")
    private Long schoolGradeDirectorId;

    /**
     * 年级主任姓名
     */
    @TableField("school_grade_director_name")
    private String schoolGradeDirectorName;

    /**
     * 年级状态，0-禁用，1-启用
     */
    @TableField("school_grade_status")
    @Schema(description = "年级状态")
    private Integer schoolGradeStatus;

    /**
     * 排序
     */
    @TableField("school_grade_sort")
    private Integer schoolGradeSort;

    /**
     * 备注
     */
    @TableField("school_grade_remark")
    private String schoolGradeRemark;

    /**
     * 删除标志（0-存在，1-删除）
     */
    @TableField("school_grade_deleted")
    @TableLogic
    private Integer schoolGradeDeleted;
} 