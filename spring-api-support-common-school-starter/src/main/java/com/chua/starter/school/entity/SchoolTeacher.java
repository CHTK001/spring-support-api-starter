package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学校教师实体类
 * 用于存储教师信息，包括教师姓名、工号、性别、所属学校等
 *
 * @author CH
 */
@Data
@TableName("school_teacher")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校教师")
public class SchoolTeacher extends SysBase {

    /**
     * 教师ID，主键
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "教师ID")
    private Long schoolTeacherId;

    /**
     * 学校ID，关联学校信息
     */
    @Schema(description = "学校ID")
    private Long schoolId;

    /**
     * 教师姓名
     */
    @Schema(description = "教师姓名")
    private String schoolTeacherName;

    /**
     * 教师工号
     */
    @Schema(description = "教师工号")
    private String schoolTeacherNumber;

    /**
     * 教师性别，0-女，1-男
     */
    @Schema(description = "教师性别")
    private Integer schoolTeacherSex;

    /**
     * 教师状态，0-禁用，1-启用
     */
    @Schema(description = "教师状态")
    private Integer schoolTeacherStatus;

    /**
     * 学校代码
     */
    @TableField("school_teacher_school_code")
    @Schema(description = "学校代码")
    private String schoolTeacherSchoolCode;

    /**
     * 教师年龄
     */
    @TableField("school_teacher_birth_date")
    @Schema(description = "教师年龄")
    private LocalDate schoolTeacherBirthDate;

    /**
     * 身份证号
     */
    @TableField("school_teacher_id_card")
    @Schema(description = "身份证号")
    private String schoolTeacherIdCard;

    /**
     * 联系电话
     */
    @TableField("school_teacher_phone")
    @Schema(description = "联系电话")
    private String schoolTeacherPhone;

    /**
     * 邮箱
     */
    @TableField("school_teacher_email")
    @Schema(description = "邮箱")
    private String schoolTeacherEmail;

    /**
     * 住址
     */
    @TableField("school_teacher_address")
    @Schema(description = "住址")
    private String schoolTeacherAddress;

    /**
     * 所属部门
     */
    @TableField("school_teacher_department")
    @Schema(description = "所属部门")
    private String schoolTeacherDepartment;

    /**
     * 职位
     */
    @TableField("school_teacher_position")
    @Schema(description = "职位")
    private String schoolTeacherPosition;

    /**
     * 职称
     */
    @TableField("school_teacher_title")
    @Schema(description = "职称")
    private String schoolTeacherTitle;

    /**
     * 任教科目
     */
    @TableField("school_teacher_subject")
    @Schema(description = "任教科目")
    private String schoolTeacherSubject;

    /**
     * 入职日期
     */
    @TableField("school_teacher_entry_date")
    @Schema(description = "入职日期")
    private LocalDate schoolTeacherEntryDate;

    /**
     * 离职日期
     */
    @TableField("school_teacher_leave_date")
    @Schema(description = "离职日期")
    private LocalDate schoolTeacherLeaveDate;

    /**
     * 照片URL
     */
    @TableField("school_teacher_photo_url")
    @Schema(description = "照片URL")
    private String schoolTeacherPhotoUrl;

    /**
     * 学历
     */
    @TableField("school_teacher_education")
    @Schema(description = "学历")
    private String schoolTeacherEducation;

    /**
     * 学位
     */
    @TableField("school_teacher_degree")
    @Schema(description = "学位")
    private String schoolTeacherDegree;

    /**
     * 毕业院校
     */
    @TableField("school_teacher_graduation_school")
    @Schema(description = "毕业院校")
    private String schoolTeacherGraduationSchool;

    /**
     * 专业
     */
    @TableField("school_teacher_major")
    @Schema(description = "专业")
    private String schoolTeacherMajor;

    /**
     * 备注
     */
    @TableField("school_teacher_remark")
    @Schema(description = "备注")
    private String schoolTeacherRemark;

    /**
     * 删除标志（0-存在，1-删除）
     */
    @TableField("school_teacher_deleted")
    @TableLogic
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolTeacherDeleted;
} 