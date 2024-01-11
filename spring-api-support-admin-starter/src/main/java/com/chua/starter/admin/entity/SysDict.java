package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 字典数据表
 */
@ApiModel(description = "字典数据表")
@Schema(description = "字典数据表")
@Data
@TableName(value = "sys_dict")
public class SysDict {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @TableId(value = "dict_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "主键不能为null")
    private Long dictId;

    /**
     * 字典类型ID
     */
    @TableField(value = "dict_type_id")
    @ApiModelProperty(value = "字典类型ID")
    @Schema(description = "字典类型ID")
    @Size(max = 255, message = "字典类型ID最大长度要小于 255")
    private String dictTypeId;

    /**
     * 字典项名称
     */
    @TableField(value = "dict_name")
    @ApiModelProperty(value = "字典项名称")
    @Schema(description = "字典项名称")
    @Size(max = 255, message = "字典项名称最大长度要小于 255")
    private String dictName;

    /**
     * 字典项值
     */
    @TableField(value = "dict_value")
    @ApiModelProperty(value = "字典项值")
    @Schema(description = "字典项值")
    @Size(max = 255, message = "字典项值最大长度要小于 255")
    private String dictValue;

    /**
     * 排序
     */
    @TableField(value = "dict_sort")
    @ApiModelProperty(value = "排序")
    @Schema(description = "排序")
    private Integer dictSort;

    /**
     * 状态(1:正常;0:禁用)
     */
    @TableField(value = "dict_status")
    @ApiModelProperty(value = "状态(1:正常;0:禁用)")
    @Schema(description = "状态(1:正常;0:禁用)")
    private Integer dictStatus;

    /**
     * 状态(1:系统字典;0:非系统字典)
     */
    @TableField(value = "dict_sys")
    @ApiModelProperty(value = "状态(1:系统字典;0:非系统字典)")
    @Schema(description = "状态(1:系统字典;0:非系统字典)")
    private Integer dictSys;

    /**
     * 是否默认(1:是;0:否)
     */
    @TableField(value = "dict_defaulted")
    @ApiModelProperty(value = "是否默认(1:是;0:否)")
    @Schema(description = "是否默认(1:是;0:否)")
    private Integer dictDefaulted;

    /**
     * 备注
     */
    @TableField(value = "dict_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度要小于 255")
    private String dictRemark;

    @TableField(value = "tenant_id")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer tenantId;

    @TableField(value = "create_name")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 255, message = "最大长度要小于 255")
    private String createName;

    @TableField(value = "create_by")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer createBy;

    @TableField(value = "create_time")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Date createTime;

    @TableField(value = "update_time")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Date updateTime;

    @TableField(value = "update_name")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @Size(max = 255, message = "最大长度要小于 255")
    private String updateName;

    @TableField(value = "update_by")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer updateBy;
}