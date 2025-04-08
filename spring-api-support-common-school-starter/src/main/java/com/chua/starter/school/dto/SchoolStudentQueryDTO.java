package com.chua.starter.school.dto;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.school.entity.SchoolStudent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学校学生查询DTO
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "学校学生查询参数")
public class SchoolStudentQueryDTO extends Query<SchoolStudent> {

    /**
     * 学校ID
     */
    @Schema(description = "学校ID")
    private Long schoolStudentSchoolId;

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
     * 性别
     */
    @Schema(description = "性别（0-女，1-男）")
    private Integer schoolStudentGender;

    /**
     * 年级ID
     */
    @Schema(description = "年级ID")
    private Long schoolStudentGradeId;

    /**
     * 班级ID
     */
    @Schema(description = "班级ID")
    private Long schoolStudentClassId;

    /**
     * 入学日期起始
     */
    @Schema(description = "入学日期起始")
    private LocalDate schoolStudentEnrollDateStart;

    /**
     * 入学日期结束
     */
    @Schema(description = "入学日期结束")
    private LocalDate schoolStudentEnrollDateEnd;

    /**
     * 学籍状态
     */
    @Schema(description = "学籍状态（0-休学，1-在读，2-毕业，3-退学）")
    private Integer schoolStudentStatus;
}