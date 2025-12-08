package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 行政区划表
 *
 * @author CH
 * @since 2024/8/7
 */
@TableName(value = "sys_city")
@Schema(description = "行政区划表")
@Data
public class SysCity implements Serializable {

    /**
     * 区划代码
     */
    @TableId(value = "sys_city_code", type = IdType.INPUT)
    @Schema(description = "区划代码")
    @ApiModelProperty("区划代码")
    private String sysCityCode;

    /**
     * 地区区号
     */
    @TableField("sys_city_area_code")
    @Schema(description = "地区区号")
    @ApiModelProperty("地区区号")
    private String sysCityAreaCode;

    /**
     * 区划名称
     */
    @TableField("sys_city_name")
    @Schema(description = "区划名称")
    @ApiModelProperty("区划名称")
    private String sysCityName;

    /**
     * 区划级别: 1省(直辖市) 2地级市(市辖区) 3县级市(区) 4乡镇(街道) 5村(社区、居委会)
     */
    @TableField("sys_city_level")
    @Schema(description = "区划级别: 1省(直辖市) 2地级市(市辖区) 3县级市(区) 4乡镇(街道) 5村(社区、居委会)")
    @ApiModelProperty("区划级别")
    private Integer sysCityLevel;

    /**
     * 父级区划代码
     */
    @TableField("sys_city_parent_code")
    @Schema(description = "父级区划代码")
    @ApiModelProperty("父级区划代码")
    private String sysCityParentCode;

    /**
     * 树路径
     */
    @TableField("sys_city_tree_path")
    @ApiModelProperty("树路径")
    @Schema(description = "树路径")
    private String sysCityTreePath;

    /**
     * 简称
     */
    @TableField("sys_city_short_name")
    @Schema(description = "简称")
    @ApiModelProperty("简称")
    private String sysCityShortName;

    /**
     * 全称
     */
    @TableField("sys_city_merger_name")
    @Schema(description = "全称")
    @ApiModelProperty("全称")
    private String sysCityMergerName;

    /**
     * 拼音
     */
    @TableField("sys_city_pinyin")
    @Schema(description = "拼音")
    @ApiModelProperty("拼音")
    private String sysCityPinyin;

    /**
     * 经度
     */
    @TableField("sys_city_lng")
    @Schema(description = "经度")
    @ApiModelProperty("经度")
    private Double sysCityLng;

    /**
     * 纬度
     */
    @TableField("sys_city_lat")
    @Schema(description = "纬度")
    @ApiModelProperty("纬度")
    private Double sysCityLat;

    /**
     * 排序
     */
    @TableField("sys_city_sort")
    @Schema(description = "排序")
    @ApiModelProperty("排序")
    private Integer sysCitySort;

    /**
     * 车牌前缀
     */
    @TableField("sys_city_car_plate")
    @Schema(description = "车牌前缀")
    private String sysCityCarPlate;

    /**
     * 车牌号
     */
    @TableField("sys_city_car_plate_no")
    @Schema(description = "车牌号")
    private String sysCityCarPlateNo;

    /**
     * 子级区划(非数据库字段)
     */
    @TableField(exist = false)
    private List<SysCity> children;
}
