package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
* 节假日表
*
* @since 2025/9/11
* @author CH
 */
@Schema(description = "节假日表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_holiday")
public class SysHoliday extends SysBase {

    /**
    * 节假日标识
     */
    @TableId(value = "sys_holiday_id", type = IdType.AUTO)
    @Schema(description = "节假日ID")
    /** sysholidayid */
    private Integer sysHolidayId;

    /**
    * 节假日年份
     */
    @TableField(value = "sys_holiday_year")
    @Schema(description = "节假日年份")
    /** sysholidayyear */
    private Integer sysHolidayYear;

    /**
    * 节假日日期
     */
    @TableField(value = "sys_holiday_date")
    @Schema(description = "节假日日期")
    /** sysholiday日期 */
    private LocalDate sysHolidayDate;

    /**
    * 节假日名称
     */
    @TableField(value = "sys_holiday_name")
    @Schema(description = "节假日名称")
    /** sysholiday名称 */
    private String sysHolidayName;

    /**
    * 是否节假日
     */
    @TableField(value = "sys_holiday_is_holiday")
    @Schema(description = "是否节假日")
    /** sysholidayisholiday */
    private Boolean sysHolidayIsHoliday;
}
