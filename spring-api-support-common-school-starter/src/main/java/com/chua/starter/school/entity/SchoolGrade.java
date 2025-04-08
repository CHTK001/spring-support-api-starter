package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校年级
 *
 * @author CH
 */
@Data
@TableName("school_grade")
@EqualsAndHashCode(callSuper = true)
public class SchoolGrade extends SysBase {

    /**
     * 主键
     */
    @TableId("school_grade_id")
    private Long schoolGradeId;

    /**
     * 学校ID
     */
    @TableField("school_grade_school_id")
    private Long schoolGradeSchoolId;

    /**
     * 学校代码
     */
    @TableField("school_grade_school_code")
    private String schoolGradeSchoolCode;

    /**
     * 年级名称
     */
    @TableField("school_grade_name")
    private String schoolGradeName;

    /**
     * 年级编码
     */
    @TableField("school_grade_code")
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
     * 状态（0-禁用，1-启用）
     */
    @TableField("school_grade_status")
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