package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.common.support.datasource.annotation.Column;
import com.chua.common.support.datasource.annotation.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 字典数据表
 *
 * @author haoxr
 * @since 2022/12/17
 */
@Data
@ApiModel("字典")
@Table(comment = "字典数据表")
public class SysDict implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long dictId;

    /**
     * 字典类型编码
     */
    @ApiModelProperty("字典类型ID")
    @NotNull(message = "字典类型ID不能为空")
    private String dictTypeId;

    /**
     * 字典项名称
     */
    @ApiModelProperty("字典项名称")
    @NotNull(message = "字典项名称")
    private String dictName;

    /**
     * 字典项值
     */
    @ApiModelProperty("字典项值")
    private String dictValue;

    /**
     * 排序
     */
    @ApiModelProperty("排序")
    private Integer dictSort;

    /**
     * 状态(1:正常;0:禁用)
     */
    @ApiModelProperty("状态(1:正常;0:禁用)")
    @Column(defaultValue = "1", comment = "状态(1:正常;0:禁用)")
    private Integer dictStatus;
    /**
     * 1:系统字典;0:非系统字典
     */
    @ApiModelProperty("状态(1:系统字典;0:非系统字典)")
    @Column(defaultValue = "0", comment = "状态(1:系统字典;0:非系统字典)")
    private Integer dictSys;

    /**
     * 是否默认(1:是;0:否)
     */
    @ApiModelProperty("是否默认(1:是;0:否)")
    @Column(defaultValue = "1", comment = "是否默认(1:是;0:否)")
    private Integer dictDefaulted;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String dictRemark;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}