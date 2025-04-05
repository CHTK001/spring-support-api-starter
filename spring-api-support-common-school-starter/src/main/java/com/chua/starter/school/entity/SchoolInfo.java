package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校信息
 *
 * @author CH
 */
@Data
@TableName("school_info")
@Schema(description = "学校信息")
public class SchoolInfo {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long schoolId;

    /**
     * 学校代码
     */
    @Schema(description = "学校代码")
    private String schoolCode;

    /**
     * 学校名称
     */
    @Schema(description = "学校名称")
    private String schoolName;

    /**
     * 学校英文名
     */
    @Schema(description = "学校英文名")
    private String schoolEnglishName;

    /**
     * 学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）
     */
    @Schema(description = "学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）")
    private String schoolType;

    /**
     * 所在省份
     */
    @Schema(description = "所在省份")
    private String schoolProvince;

    /**
     * 所在城市
     */
    @Schema(description = "所在城市")
    private String schoolCity;

    /**
     * 所在区县
     */
    @Schema(description = "所在区县")
    private String schoolDistrict;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String schoolAddress;

    /**
     * 办学层次（本科、专科）
     */
    @Schema(description = "办学层次（本科、专科）")
    private String schoolEducationLevel;

    /**
     * 隶属于（教育部、地方等）
     */
    @Schema(description = "隶属于（教育部、地方等）")
    private String schoolSubordinateTo;

    /**
     * 是否是985（0-否，1-是）
     */
    @Schema(description = "是否是985（0-否，1-是）")
    private Integer schoolIs985;

    /**
     * 是否是211（0-否，1-是）
     */
    @Schema(description = "是否是211（0-否，1-是）")
    private Integer schoolIs211;

    /**
     * 是否是双一流（0-否，1-是）
     */
    @Schema(description = "是否是双一流（0-否，1-是）")
    private Integer schoolIsDoubleFirst;

    /**
     * 官网地址
     */
    @Schema(description = "官网地址")
    private String schoolWebsiteUrl;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String schoolPhone;

    /**
     * 建校时间
     */
    @Schema(description = "建校时间")
    private String schoolFoundingYear;

    /**
     * 学校介绍
     */
    @Schema(description = "学校介绍")
    private String schoolIntroduction;

    /**
     * 学校特色
     */
    @Schema(description = "学校特色")
    private String schoolFeatures;

    /**
     * 学校Logo URL
     */
    @Schema(description = "学校Logo URL")
    private String schoolLogoUrl;

    /**
     * 学校校徽 URL
     */
    @Schema(description = "学校校徽 URL")
    private String schoolBadgeUrl;

    /**
     * 招生网址
     */
    @Schema(description = "招生网址")
    private String schoolAdmissionUrl;

    /**
     * 就业率
     */
    @Schema(description = "就业率")
    private String schoolEmploymentRate;

    /**
     * 高考网链接
     */
    @Schema(description = "高考网链接")
    private String schoolGaokaoUrl;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime schoolCreateTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime schoolUpdateTime;
} 