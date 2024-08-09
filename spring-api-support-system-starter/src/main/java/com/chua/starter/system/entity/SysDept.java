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
    @NotNull(message = "不能为null")
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