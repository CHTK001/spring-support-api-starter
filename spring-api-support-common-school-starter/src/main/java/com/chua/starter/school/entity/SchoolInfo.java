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
    private Long id;

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
    private String englishName;

    /**
     * 学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）
     */
    @Schema(description = "学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）")
    private String schoolType;

    /**
     * 所在省份
     */
    @Schema(description = "所在省份")
    private String province;

    /**
     * 所在城市
     */
    @Schema(description = "所在城市")
    private String city;

    /**
     * 所在区县
     */
    @Schema(description = "所在区县")
    private String district;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String address;

    /**
     * 办学层次（本科、专科）
     */
    @Schema(description = "办学层次（本科、专科）")
    private String educationLevel;

    /**
     * 隶属于（教育部、地方等）
     */
    @Schema(description = "隶属于（教育部、地方等）")
    private String subordinateTo;

    /**
     * 是否是985（0-否，1-是）
     */
    @Schema(description = "是否是985（0-否，1-是）")
    private Integer is985;

    /**
     * 是否是211（0-否，1-是）
     */
    @Schema(description = "是否是211（0-否，1-是）")
    private Integer is211;

    /**
     * 是否是双一流（0-否，1-是）
     */
    @Schema(description = "是否是双一流（0-否，1-是）")
    private Integer isDoubleFirst;

    /**
     * 官网地址
     */
    @Schema(description = "官网地址")
    private String websiteUrl;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 建校时间
     */
    @Schema(description = "建校时间")
    private String foundingYear;

    /**
     * 学校介绍
     */
    @Schema(description = "学校介绍")
    private String introduction;

    /**
     * 学校特色
     */
    @Schema(description = "学校特色")
    private String features;

    /**
     * 学校Logo URL
     */
    @Schema(description = "学校Logo URL")
    private String logoUrl;

    /**
     * 学校校徽 URL
     */
    @Schema(description = "学校校徽 URL")
    private String badgeUrl;

    /**
     * 招生网址
     */
    @Schema(description = "招生网址")
    private String admissionUrl;

    /**
     * 就业率
     */
    @Schema(description = "就业率")
    private String employmentRate;

    /**
     * 高考网链接
     */
    @Schema(description = "高考网链接")
    private String gaokaoUrl;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 