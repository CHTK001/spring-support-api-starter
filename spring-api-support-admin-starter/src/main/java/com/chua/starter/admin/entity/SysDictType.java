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
 * 字典类型表
 */
@ApiModel(description = "字典类型表")
@Schema(description = "字典类型表")
@Data
@TableName(value = "sys_dict_type")
public class SysDictType {
    /**
     * 主键
     */
    @TableId(value = "dict_type_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "主键不能为null")
    private Long dictTypeId;

    /**
     * 类型名称
     */
    @TableField(value = "dict_type_name")
    @ApiModelProperty(value = "类型名称")
    @Schema(description = "类型名称")
    @Size(max = 255, message = "类型名称最大长度要小于 255")
    private String dictTypeName;

    /**
     * 类型编码
     */
    @TableField(value = "dict_type_code")
    @ApiModelProperty(value = "类型编码")
    @Schema(description = "类型编码")
    @Size(max = 255, message = "类型编码最大长度要小于 255")
    private String dictTypeCode;

    /**
     * 类型顺序
     */
    @TableField(value = "dict_type_sort")
    @ApiModelProperty(value = "类型顺序")
    @Schema(description = "类型顺序")
    private Integer dictTypeSort;

    /**
     * 状态(0:正常;1:禁用)
     */
    @TableField(value = "dict_type_status")
    @ApiModelProperty(value = "状态(0:正常;1:禁用)")
    @Schema(description = "状态(0:正常;1:禁用)")
    private Integer dictTypeStatus;

    /**
     * 状态(1:系统字典;0:非系统字典)
     */
    @TableField(value = "dict_type_sys")
    @ApiModelProperty(value = "状态(1:系统字典;0:非系统字典)")
    @Schema(description = "状态(1:系统字典;0:非系统字典)")
    private Integer dictTypeSys;

    /**
     * 备注
     */
    @TableField(value = "dict_type_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度要小于 255")
    private String dictTypeRemark;

    /**
     * 创建人
     */
    @TableField(value = "create_name")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    @Size(max = 255, message = "创建人最大长度要小于 255")
    private String createName;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    @Size(max = 255, message = "创建人最大长度要小于 255")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 更新人
     */
    @TableField(value = "update_name")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    @Size(max = 255, message = "更新人最大长度要小于 255")
    private String updateName;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    @Size(max = 255, message = "更新人最大长度要小于 255")
    private String updateBy;

    @TableField(value = "tenant_id")
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer tenantId;
}