package com.chua.starter.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学校学生VO
 *
 * @author CH
 */
@Data
@Schema(description = "学校学生VO")
public class SchoolStudentVO {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long schoolStudentId;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID")
    private Long schoolStudentSchoolId;

    /**
     * 学校代码
     */
    @Schema(description = "学校代码")
    private String schoolStudentSchoolCode;

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String schoolStudentNumber;

    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名")
    private String schoolStudentName;

    /**
     * 性别（0-女，1-男）
     */
    @Schema(description = "性别（0-女，1-男）")
    private Integer schoolStudentGender;

    /**
     * 出生日期
     */
    @Schema(description = "出生日期")
    private LocalDate schoolStudentBirthDate;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String schoolStudentIdCard;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String schoolStudentPhone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String schoolStudentEmail;

    /**
     * 家庭住址
     */
    @Schema(description = "家庭住址")
    private String schoolStudentAddress;

    /**
     * 年级ID
     */
    @Schema(description = "年级ID")
    private Long schoolStudentGradeId;

    /**
     * 年级名称
     */
    @Schema(description = "年级名称")
    private String schoolStudentGradeName;

    /**
     * 班级ID
     */
    @Schema(description = "班级ID")
    private Long schoolStudentClassId;

    /**
     * 班级名称
     */
    @Schema(description = "班级名称")
    private String schoolStudentClassName;

    /**
     * 入学日期
     */
    @Schema(description = "入学日期")
    private LocalDate schoolStudentEnrollDate;

    /**
     * 毕业日期
     */
    @Schema(description = "毕业日期")
    private LocalDate schoolStudentGraduateDate;

    /**
     * 学籍状态（0-休学，1-在读，2-毕业，3-退学）
     */
    @Schema(description = "学籍状态（0-休学，1-在读，2-毕业，3-退学）")
    private Integer schoolStudentStatus;

    /**
     * 照片URL
     */
    @Schema(description = "照片URL")
    private String schoolStudentPhotoUrl;

    /**
     * 家长姓名
     */
    @Schema(description = "家长姓名")
    private String schoolStudentParentName;

    /**
     * 家长电话
     */
    @Schema(description = "家长电话")
    private String schoolStudentParentPhone;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String schoolStudentRemark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private String updateTime;
} 