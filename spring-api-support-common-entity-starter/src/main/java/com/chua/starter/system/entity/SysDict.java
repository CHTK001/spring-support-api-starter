package com.chua.starter.system.entity;

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
 *
 * @since 2024/8/7
 * @author CH    
 */

/**
 * 字典表
 */
@ApiModel(description = "字典表")
@Schema(description = "字典表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dict")
public class SysDict extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_dict_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null")
    private Integer sysDictId;
    /**
     * 父级id
     */
    @TableField(value = "sys_dict_pid")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "pid不能为null")
    private Integer sysDictPid;
    /**
     * 字典名称
     */
    @TableField(value = "sys_dict_name")
    @ApiModelProperty(value = "字典名称")
    @Schema(description = "字典名称")
    @Size(max = 255, message = "字典名称最大长度要小于 255")
    private String sysDictName;

    /**
     * 状态; 0:隐藏
     */
    @TableField(value = "sys_dict_status")
    @ApiModelProperty(value = "状态; 0:隐藏")
    @Schema(description = "状态; 0:隐藏")
    private Integer sysDictStatus;

    /**
     * 是否内置
     */
    @Schema(description = "是否内置; 0:非内置")
    @TableField(value = "sys_dict_in_system")
    private Integer sysDictInSystem;

    /**
     * 字典编码, 唯一
     */
    @TableField(value = "sys_dict_code")
    @ApiModelProperty(value = "字典编码, 唯一")
    @Schema(description = "字典编码, 唯一")
    @Size(max = 255, message = "字典编码, 唯一最大长度要小于 255")
    private String sysDictCode;
    /**
     * 字典i18n
     */
    @TableField(value = "sys_dict_i18n")
    @ApiModelProperty(value = "字典i18n, 唯一")
    @Schema(description = "字典i18n, 唯一")
    @Size(max = 255, message = "字典i18n, 唯一最大长度要小于 255")
    private String sysDictI18n;

    /**
     * 描述
     */
    @TableField(value = "sys_dict_remark")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    @Size(max = 255, message = "描述最大长度要小于 255")
    private String sysDictRemark;


    /**
     * 子级
     */
    @TableField(exist = false)
    private List<SysDict> children;
}