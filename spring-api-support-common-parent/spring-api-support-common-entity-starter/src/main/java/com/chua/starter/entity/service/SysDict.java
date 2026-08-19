package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 字典表
 *
 * @since 2024/8/7
 * @author CH
 */
@Schema(description = "字典表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dict")
public class SysDict extends SysBase implements Serializable {
    /** Serial版本UID */
    private static final long serialVersionUID = 1L;

    /**
     * 字典ID
     */
    @TableId(value = "sys_dict_id", type = IdType.AUTO)
    @Schema(description = "字典ID")
    @NotNull(message = "字典ID不能为空")
    /** SYSdictID */
    private Integer sysDictId;

    /**
     * 父级ID
     */
    @TableField(value = "sys_dict_pid")
    @Schema(description = "父级ID")
    @NotNull(message = "父级ID不能为空")
    /** SYSdictPID */
    private Integer sysDictPid;

    /**
     * 排序
     */
    @TableField(value = "sys_dict_sort")
    @Schema(description = "排序")
    /** SYSdictsort */
    private Integer sysDictSort;

    /**
     * 字典名称
     */
    @TableField(value = "sys_dict_name")
    @Schema(description = "字典名称")
    @Size(max = 255, message = "字典名称最大长度需小于255")
    /** SYSdict名称 */
    private String sysDictName;

    /**
     * 状态: 0:隐藏 1:显示
     */
    @TableField(value = "sys_dict_status")
    @Schema(description = "状态: 0:隐藏 1:显示")
    /** SYSdict状态 */
    private Integer sysDictStatus;

    /**
     * 是否内置: 0:非内置 1:内置
     */
    @Schema(description = "是否内置: 0:非内置 1:内置")
    @TableField(value = "sys_dict_in_system")
    /** SYSdictINsystem */
    private Integer sysDictInSystem;

    /**
     * 字典编码(唯一)
     */
    @TableField(value = "sys_dict_code")
    @Schema(description = "字典编码(唯一)")
    @Size(max = 255, message = "字典编码最大长度需小于255")
    /** SYSdict代码 */
    private String sysDictCode;

    /**
     * 字典国际化key
     */
    @TableField(value = "sys_dict_i18n")
    @Schema(description = "字典国际化key")
    @Size(max = 255, message = "字典国际化key最大长度需小于255")
    /** SYSdicti18n */
    private String sysDictI18n;

    /**
     * 描述
     */
    @TableField(value = "sys_dict_remark")
    @Schema(description = "描述")
    @Size(max = 255, message = "描述最大长度需小于255")
    /** SYSdictremark */
    private String sysDictRemark;

    /**
     * 子级字典(非数据库字段)
     */
    @TableField(exist = false)
    /** Children */
    private List<SysDict> children;
}
