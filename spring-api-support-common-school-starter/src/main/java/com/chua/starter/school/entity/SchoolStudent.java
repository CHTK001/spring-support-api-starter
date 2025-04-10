package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.common.support.annotations.ApiIgnore;
import com.chua.starter.common.support.group.IgnoreGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学校学生实体类
 * 用于存储学生信息，包括学生姓名、学号、所属班级等
 *
 * @author CH
 */
@Data
@TableName("school_student")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校学生")
public class SchoolStudent extends SysBase {

    /**
     * 学生ID，主键
     */
    @TableId("school_student_id")
    @Schema(description = "学生ID")
    private Long schoolStudentId;

    /**
     * 学校ID
     */
    @TableField("school_student_school_id")
    @Schema(description = "学校ID")
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
    @Schema(description = "学生学号")
    private String schoolStudentNumber;

    /**
     * 学生姓名
     */
    @TableField("school_student_name")
    @Schema(description = "学生姓名")
    private String schoolStudentName;

    /**
     * 性别（0-女，1-男）
     */
    @TableField("school_student_gender")
    @Schema(description = "学生性别")
    private Integer schoolStudentGender;

    /**
     * 出生日期
     */
    @TableField("school_student_birth_date")
    @Schema(description = "学生出生日期")
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
    @Schema(description = "班级ID")
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
    @Schema(description = "入学日期")
    private LocalDate schoolStudentEnrollDate;

    /**
     * 毕业日期
     */
    @TableField("school_student_graduate_date")
    @Schema(description = "毕业日期")
    private LocalDate schoolStudentGraduateDate;

    /**
     * 学籍状态（0-休学，1-在读，2-毕业，3-退学）
     */
    @TableField("school_student_status")
    @Schema(description = "学生状态")
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
    @Schema(description = "是否删除")
    private Integer schoolStudentDeleted;
} 