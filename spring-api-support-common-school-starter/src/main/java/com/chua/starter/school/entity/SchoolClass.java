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
 * 学校班级实体类
 * 用于存储班级信息，包括班级名称、年级、所属学校等
 *
 * @author CH
 * <p>
 * 建表语句：
 */
@Data
@TableName("school_class")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校班级")
public class SchoolClass extends SysBase {

    /**
     * 班级ID，主键
     */
    @TableId("school_class_id")
    @Schema(description = "班级ID")
    private Long schoolClassId;

    /**
     * 学校ID，关联学校信息
     */
    @TableField("school_class_school_id")
    @Schema(description = "学校ID")
    private Long schoolClassSchoolId;

    /**
     * 学校代码
     */
    @TableField("school_class_school_code")
    private String schoolClassSchoolCode;

    /**
     * 年级ID，关联年级信息
     */
    @TableField("school_class_grade_id")
    @Schema(description = "年级ID")
    private Long schoolClassGradeId;

    /**
     * 班级名称，如1班、2班等
     */
    @TableField("school_class_name")
    @Schema(description = "班级名称")
    private String schoolClassName;

    /**
     * 班级代码，用于标识班级
     */
    @TableField("school_class_code")
    @Schema(description = "班级代码")
    private String schoolClassCode;

    /**
     * 班级类型，如普通班、实验班等
     */
    @TableField("school_class_type")
    private String schoolClassType;

    /**
     * 班级容量
     */
    @TableField("school_class_capacity")
    private Integer schoolClassCapacity;

    /**
     * 当前人数
     */
    @TableField("school_class_current_count")
    private Integer schoolClassCurrentCount;

    /**
     * 班主任ID
     */
    @TableField("school_class_teacher_id")
    private Long schoolClassTeacherId;

    /**
     * 班主任姓名
     */
    @TableField("school_class_teacher_name")
    private String schoolClassTeacherName;

    /**
     * 班级描述
     */
    @TableField("school_class_description")
    private String schoolClassDescription;

    /**
     * 班级状态，0-禁用，1-启用
     */
    @TableField("school_class_status")
    @Schema(description = "班级状态")
    private Integer schoolClassStatus;

    /**
     * 班级排序，用于显示顺序
     */
    @TableField("school_class_sort")
    @Schema(description = "班级排序")
    private Integer schoolClassSort;

    /**
     * 备注
     */
    @TableField("school_class_remark")
    private String schoolClassRemark;

    /**
     * 学生人数
     */
    @TableField("school_class_student_count")
    private Integer schoolClassStudentCount;

    /**
     * 教室ID
     */
    @TableField("school_class_room_id")
    private Long schoolClassRoomId;

    /**
     * 教室名称
     */
    @TableField("school_class_room_name")
    private String schoolClassRoomName;

    /**
     * 删除标志（0-存在，1-删除）
     */
    @TableField("school_class_deleted")
    @TableLogic
    private Integer schoolClassDeleted;
} 