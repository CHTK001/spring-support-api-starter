package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */

/**
 * 组织机构
 */
@ApiModel(description = "组织机构")
@Schema(description = "组织机构")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dept")
public class SysDept extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_dept_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "机构ID不能为空", groups = UpdateGroup.class)
    private Integer sysDeptId;

    /**
     * 机构名称
     */
    @TableField(value = "sys_dept_name")
    @ApiModelProperty(value = "机构名称")
    @Schema(description = "机构名称")
    @Size(max = 255, message = "机构名称最大长度要小于 255")
    private String sysDeptName;

    /**
     * 部门负责人
     */
    @TableField(value = "sys_dept_principal")
    @ApiModelProperty(value = "部门负责人")
    @Schema(description = "部门负责人")
    @Size(max = 255, message = "部门负责人最大长度要小于 255")
    private String sysDeptPrincipal;
    /**
     * 负责人联系方式
     */
    @TableField(value = "sys_dept_contact")
    @ApiModelProperty(value = "负责人联系方式")
    @Schema(description = "负责人联系方式")
    @Size(max = 255, message = "负责人联系方式最大长度要小于 255")
    private String sysDeptContact;
    /**
     * 优先级
     */
    @TableField(value = "sys_dept_sort")
    @ApiModelProperty(value = "优先级")
    @Schema(description = "优先级")
    @Size(max = 255, message = "优先级最大长度要小于 255")
    private Integer sysDeptSort;
    /**
     * 机构编码
     */
    @TableField(value = "sys_dept_code")
    @ApiModelProperty(value = "机构编码")
    @Schema(description = "机构编码")
    @Size(max = 255, message = "机构名称最大长度要小于 255")
    @NotBlank(message = "机构编码不能为空", groups = {AddGroup.class})
    private String sysDeptCode;
    /**
     * 父机构
     */
    @TableField(value = "sys_dept_pid")
    @ApiModelProperty(value = "父机构")
    @Schema(description = "父机构")
    private Integer sysDeptPid;

    /**
     * 机构链路
     */
    @TableField(value = "sys_dept_tree_id")
    @ApiModelProperty(value = "机构链路")
    @Schema(description = "机构链路")
    @Size(max = 255, message = "机构链路最大长度要小于 255")
    private String sysDeptTreeId;
}