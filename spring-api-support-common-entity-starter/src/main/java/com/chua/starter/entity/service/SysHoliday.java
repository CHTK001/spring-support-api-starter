package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 节假日表
 *
 * @author CH
 * @since 2025/9/11
 */
@ApiModel(description = "节假日表")
@Schema(description = "节假日表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_holiday")
public class SysHoliday extends SysBase {

    /**
     * 节假日ID
     */
    @TableId(value = "sys_holiday_id", type = IdType.AUTO)
    @ApiModelProperty(value = "节假日ID")
    @Schema(description = "节假日ID")
    private Integer sysHolidayId;

    /**
     * 节假日年份
     */
    @TableField(value = "sys_holiday_year")
    @ApiModelProperty(value = "节假日年份")
    @Schema(description = "节假日年份")
    private Integer sysHolidayYear;

    /**
     * 节假日日期
     */
    @TableField(value = "sys_holiday_date")
    @ApiModelProperty(value = "节假日日期")
    @Schema(description = "节假日日期")
    private LocalDate sysHolidayDate;

    /**
     * 节假日名称
     */
    @TableField(value = "sys_holiday_name")
    @ApiModelProperty(value = "节假日名称")
    @Schema(description = "节假日名称")
    private String sysHolidayName;

    /**
     * 是否节假日
     */
    @TableField(value = "sys_holiday_is_holiday")
    @ApiModelProperty(value = "是否节假日")
    @Schema(description = "是否节假日")
    private Boolean sysHolidayIsHoliday;
}
