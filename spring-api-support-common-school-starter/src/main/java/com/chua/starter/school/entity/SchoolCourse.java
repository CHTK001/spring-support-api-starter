package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校课程
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("school_course")
@Schema(description = "学校课程")
public class SchoolCourse extends SysBase {

    /**
     * 主键ID
     */
    @TableId(value = "school_course_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long schoolCourseId;

    /**
     * 学校ID
     */
    @TableField("school_course_school_id")
    @Schema(description = "学校ID")
    private Long schoolCourseSchoolId;

    /**
     * 学校代码
     */
    @TableField("school_course_school_code")
    @Schema(description = "学校代码")
    private String schoolCourseSchoolCode;

    /**
     * 课程编号
     */
    @TableField("school_course_code")
    @Schema(description = "课程编号")
    private String schoolCourseCode;

    /**
     * 课程名称
     */
    @TableField("school_course_name")
    @Schema(description = "课程名称")
    private String schoolCourseName;

    /**
     * 课程类别
     */
    @TableField("school_course_category")
    @Schema(description = "课程类别")
    private String schoolCourseCategory;

    /**
     * 课程类型（必修、选修）
     */
    @TableField("school_course_type")
    @Schema(description = "课程类型（必修、选修）")
    private String schoolCourseType;

    /**
     * 学分
     */
    @TableField("school_course_credit")
    @Schema(description = "学分")
    private Double schoolCourseCredit;

    /**
     * 课时
     */
    @TableField("school_course_hours")
    @Schema(description = "课时")
    private Integer schoolCourseHours;

    /**
     * 课程描述
     */
    @TableField("school_course_desc")
    @Schema(description = "课程描述")
    private String schoolCourseDesc;

    /**
     * 开课部门
     */
    @TableField("school_course_department")
    @Schema(description = "开课部门")
    private String schoolCourseDepartment;

    /**
     * 适用年级
     */
    @TableField("school_course_grade_level")
    @Schema(description = "适用年级")
    private String schoolCourseGradeLevel;

    /**
     * 授课教师ID
     */
    @TableField("school_course_teacher_id")
    @Schema(description = "授课教师ID")
    private Long schoolCourseTeacherId;

    /**
     * 授课教师姓名
     */
    @TableField("school_course_teacher_name")
    @Schema(description = "授课教师姓名")
    private String schoolCourseTeacherName;

    /**
     * 课程大纲
     */
    @TableField("school_course_syllabus")
    @Schema(description = "课程大纲")
    private String schoolCourseSyllabus;

    /**
     * 教材
     */
    @TableField("school_course_textbook")
    @Schema(description = "教材")
    private String schoolCourseTextbook;

    /**
     * 考核方式
     */
    @TableField("school_course_assessment")
    @Schema(description = "考核方式")
    private String schoolCourseAssessment;

    /**
     * 课程状态（0-停用，1-启用）
     */
    @TableField("school_course_status")
    @Schema(description = "课程状态（0-停用，1-启用）")
    private Integer schoolCourseStatus;

    /**
     * 备注
     */
    @TableField("school_course_remark")
    @Schema(description = "备注")
    private String schoolCourseRemark;

    /**
     * 删除标志（0-存在，1-删除）
     */
    @TableField("school_course_deleted")
    @TableLogic
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolCourseDeleted;
} 