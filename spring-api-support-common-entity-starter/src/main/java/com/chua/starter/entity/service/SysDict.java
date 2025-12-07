package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
 * @author CH
 * @since 2024/8/7
 */
@ApiModel(description = "字典表")
@Schema(description = "字典表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dict")
public class SysDict extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字典ID
     */
    @TableId(value = "sys_dict_id", type = IdType.AUTO)
    @ApiModelProperty(value = "字典ID")
    @Schema(description = "字典ID")
    @NotNull(message = "字典ID不能为空")
    private Integer sysDictId;

    /**
     * 父级ID
     */
    @TableField(value = "sys_dict_pid")
    @ApiModelProperty(value = "父级ID")
    @Schema(description = "父级ID")
    @NotNull(message = "父级ID不能为空")
    private Integer sysDictPid;

    /**
     * 排序
     */
    @TableField(value = "sys_dict_sort")
    @ApiModelProperty(value = "排序")
    @Schema(description = "排序")
    private Integer sysDictSort;

    /**
     * 字典名称
     */
    @TableField(value = "sys_dict_name")
    @ApiModelProperty(value = "字典名称")
    @Schema(description = "字典名称")
    @Size(max = 255, message = "字典名称最大长度需小于255")
    private String sysDictName;

    /**
     * 状态: 0:隐藏 1:显示
     */
    @TableField(value = "sys_dict_status")
    @ApiModelProperty(value = "状态: 0:隐藏 1:显示")
    @Schema(description = "状态: 0:隐藏 1:显示")
    private Integer sysDictStatus;

    /**
     * 是否内置: 0:非内置 1:内置
     */
    @Schema(description = "是否内置: 0:非内置 1:内置")
    @TableField(value = "sys_dict_in_system")
    private Integer sysDictInSystem;

    /**
     * 字典编码(唯一)
     */
    @TableField(value = "sys_dict_code")
    @ApiModelProperty(value = "字典编码(唯一)")
    @Schema(description = "字典编码(唯一)")
    @Size(max = 255, message = "字典编码最大长度需小于255")
    private String sysDictCode;

    /**
     * 字典国际化key
     */
    @TableField(value = "sys_dict_i18n")
    @ApiModelProperty(value = "字典国际化key")
    @Schema(description = "字典国际化key")
    @Size(max = 255, message = "字典国际化key最大长度需小于255")
    private String sysDictI18n;

    /**
     * 描述
     */
    @TableField(value = "sys_dict_remark")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    @Size(max = 255, message = "描述最大长度需小于255")
    private String sysDictRemark;

    /**
     * 子级字典(非数据库字段)
     */
    @TableField(exist = false)
    private List<SysDict> children;
}
