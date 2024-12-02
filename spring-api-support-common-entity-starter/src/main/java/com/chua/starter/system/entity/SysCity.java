package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 区划
 * @TableName 行政区划
 */
@TableName(value = "sys_city")
@Data
public class SysCity implements Serializable {

    @Schema(description = "区划代码")
    @ApiModelProperty("区划代码")
    private Double code;

    @Schema(description = "地区区号")
    @ApiModelProperty("地区区号")
    private String areaCode;

    @Schema(description = "区划名称")
    @ApiModelProperty("区划名称")
    private String name;

    @Schema(description = "区划级别，1省(直辖市)，2地级市（市辖区），3县级市（区），4乡镇（街道），5村（社区、居委会）")
    @ApiModelProperty("区划级别，1省(直辖市)，2地级市（市辖区），3县级市（区），4乡镇（街道），5村（社区、居委会）")
    private Integer level;

    @Schema(description = "父级区划代码")
    @ApiModelProperty("父级区划代码")
    private Double pcode;

    @Schema(description = "简称")
    @ApiModelProperty("简称")
    private String shortName;

    @Schema(description = "全称")
    @ApiModelProperty("全称")
    private String mergerName;

    @Schema(description = "拼音")
    @ApiModelProperty("拼音")
    private String pinyin;

    @Schema(description = "经度")
    @ApiModelProperty("经度")
    private Double lng;

    @Schema(description = "纬度")
    @ApiModelProperty("纬度")
    private Double lat;

    @Schema(description = "排序")
    @ApiModelProperty("排序")
    private Integer sort;


    /**
     * 子级
     */
    @TableField(exist = false)
    private List<SysCity> children;
}