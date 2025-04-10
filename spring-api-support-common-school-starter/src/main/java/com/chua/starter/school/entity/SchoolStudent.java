package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.common.support.annotations.ApiIgnore;
import com.chua.starter.common.support.group.IgnoreGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学校学生
 *
 * @author CH
 */
@Data
@TableName("school_student")
@EqualsAndHashCode(callSuper = true)
public class SchoolStudent extends SysBase {

    /**
     * 主键
     */
    @TableId("school_student_id")
    private Long schoolStudentId;

    /**
     * 学校ID
     */
    @TableField("school_student_school_id")
    private Long schoolStudentSchoolId;

    /**
     * 学校代码
     */
    @TableField("school_student_school_code")
    private String schoolStudentSchoolCode;

    /**
     * 学号
     */
    @TableField("school_student_number")
    private String schoolStudentNumber;

    /**
     * 学生姓名
     */
    @TableField("school_student_name")
    private String schoolStudentName;

    /**
     * 性别（0-女，1-男）
     */
    @TableField("school_student_gender")
    private Integer schoolStudentGender;

    /**
     * 出生日期
     */
    @TableField("school_student_birth_date")
    private LocalDate schoolStudentBirthDate;

    /**
     * 身份证号
     */
    @TableField("school_student_id_card")
    private String schoolStudentIdCard;

    /**
     * 联系电话
     */
    @TableField("school_student_phone")
    private String schoolStudentPhone;

    /**
     * 邮箱
     */
    @TableField("school_student_email")
    private String schoolStudentEmail;

    /**
     * 家庭住址
     */
    @TableField("school_student_address")
    private String schoolStudentAddress;

    /**
     * 年级ID
     */
    @TableField("school_student_grade_id")
    private Long schoolStudentGradeId;

    /**
     * 年级名称
     */
    @TableField("school_student_grade_name")
    private String schoolStudentGradeName;

    /**
     * 班级ID
     */
    @TableField("school_student_class_id")
    private Long schoolStudentClassId;

    /**
     * 班级名称
     */
    @TableField("school_student_class_name")
    private String schoolStudentClassName;

    /**
     * 入学日期
     */
    @TableField("school_student_enroll_date")
    private LocalDate schoolStudentEnrollDate;

    /**
     * 毕业日期
     */
    @TableField("school_student_graduate_date")
    private LocalDate schoolStudentGraduateDate;

    /**
     * 学籍状态（0-休学，1-在读，2-毕业，3-退学）
     */
    @TableField("school_student_status")
    private Integer schoolStudentStatus;

    /**
     * 照片URL
     */
    @TableField("school_student_photo_url")
    private String schoolStudentPhotoUrl;

    /**
     * 家长姓名
     */
    @TableField("school_student_parent_name")
    private String schoolStudentParentName;

    /**
     * 家长联系电话
     */
    @TableField("school_student_parent_phone")
    private String schoolStudentParentPhone;

    /**
     * 备注
     */
    @TableField("school_student_remark")
    private String schoolStudentRemark;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("school_student_deleted")
    @ApiIgnore(IgnoreGroup.class)
    private Integer schoolStudentDeleted;
} 