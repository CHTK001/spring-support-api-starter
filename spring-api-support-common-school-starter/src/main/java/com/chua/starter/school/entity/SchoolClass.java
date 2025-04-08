package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校班级
 *
 * @author CH
 * <p>
 * 建表语句：
 */
@Data
@TableName("school_class")
@EqualsAndHashCode(callSuper = true)
public class SchoolClass extends SysBase {

    /**
     * 主键
     */
    @TableId("school_class_id")
    private Long schoolClassId;

    /**
     * 学校ID
     */
    @TableField("school_class_school_id")
    private Long schoolClassSchoolId;

    /**
     * 学校代码
     */
    @TableField("school_class_school_code")
    private String schoolClassSchoolCode;

    /**
     * 年级ID
     */
    @TableField("school_class_grade_id")
    private Long schoolClassGradeId;

    /**
     * 班级名称
     */
    @TableField("school_class_name")
    private String schoolClassName;

    /**
     * 班级编号
     */
    @TableField("school_class_code")
    private String schoolClassCode;

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
     * 状态（0-禁用，1-启用）
     */
    @TableField("school_class_status")
    private Integer schoolClassStatus;

    /**
     * 排序
     */
    @TableField("school_class_sort")
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