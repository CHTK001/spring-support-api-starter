package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.common.support.datasource.annotation.Column;
import com.chua.common.support.datasource.annotation.Table;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 字典类型实体
 *
 * @author haoxr
 * @since 2022/12/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "字典类型")
@Table(comment = "字典类型表")
public class SysDictType extends SysBase {
    /**
     * 主键
     */
    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long dictTypeId;

    /**
     * 类型名称
     */
    @Schema(description = "类型名称")
    @NotNull(message = "类型名称不能为空")
    private String dictTypeName;

    /**
     * 类型编码
     */
    @Schema(description = "类型编码")
    @NotNull(message = "类型编码不能为空")
    private String dictTypeCode;

    /**
     * 类型顺序
     */
    @Schema(description = "类型顺序")
    private Integer dictTypeSort;
    /**
     * 状态(0:正常;1:禁用)
     */
    @Schema(description = "状态(0:正常;1:禁用)")
    @Column(defaultValue = "0", comment = "状态(0:正常;1:禁用)")
    private Integer dictTypeStatus;
    /**
     * 1:系统字典;0:非系统字典
     */
    @Schema(description = "状态(1:系统字典;0:非系统字典)")
    @Column(defaultValue = "0", comment = "状态(1:系统字典;0:非系统字典)")
    private Integer dictTypeSys;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String dictTypeRemark;
}