package com.chua.starter.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学校信息VO
 *
 * @author CH
 */
@Data
@Schema(description = "学校信息VO")
public class SchoolInfoVO {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long schoolInfoId;

    /**
     * 学校代码
     */
    @Schema(description = "学校代码")
    private String schoolInfoCode;

    /**
     * 学校名称
     */
    @Schema(description = "学校名称")
    private String schoolInfoName;

    /**
     * 学校英文名
     */
    @Schema(description = "学校英文名")
    private String schoolInfoEnglishName;

    /**
     * 学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）
     */
    @Schema(description = "学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）")
    private String schoolInfoType;

    /**
     * 所在省份
     */
    @Schema(description = "所在省份")
    private String schoolInfoProvince;

    /**
     * 所在城市
     */
    @Schema(description = "所在城市")
    private String schoolInfoCity;

    /**
     * 所在区县
     */
    @Schema(description = "所在区县")
    private String schoolInfoDistrict;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String schoolInfoAddress;

    /**
     * 办学层次（本科、专科）
     */
    @Schema(description = "办学层次（本科、专科）")
    private String schoolInfoEducationLevel;

    /**
     * 隶属于（教育部、地方等）
     */
    @Schema(description = "隶属于（教育部、地方等）")
    private String schoolInfoSubordinateTo;

    /**
     * 是否是985（0-否，1-是）
     */
    @Schema(description = "是否是985（0-否，1-是）")
    private Integer schoolInfoIs985;

    /**
     * 是否是211（0-否，1-是）
     */
    @Schema(description = "是否是211（0-否，1-是）")
    private Integer schoolInfoIs211;

    /**
     * 是否是双一流（0-否，1-是）
     */
    @Schema(description = "是否是双一流（0-否，1-是）")
    private Integer schoolInfoIsDoubleFirst;

    /**
     * 官网地址
     */
    @Schema(description = "官网地址")
    private String schoolInfoWebsiteUrl;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String schoolInfoPhone;

    /**
     * 建校时间
     */
    @Schema(description = "建校时间")
    private String schoolInfoFoundingYear;

    /**
     * 学校介绍
     */
    @Schema(description = "学校介绍")
    private String schoolInfoIntroduction;

    /**
     * 学校特色
     */
    @Schema(description = "学校特色")
    private String schoolInfoFeatures;

    /**
     * 学校Logo URL
     */
    @Schema(description = "学校Logo URL")
    private String schoolInfoLogoUrl;

    /**
     * 学校校徽 URL
     */
    @Schema(description = "学校校徽 URL")
    private String schoolInfoBadgeUrl;

    /**
     * 招生网址
     */
    @Schema(description = "招生网址")
    private String schoolInfoAdmissionUrl;

    /**
     * 就业率
     */
    @Schema(description = "就业率")
    private String schoolInfoEmploymentRate;

    /**
     * 高考网链接
     */
    @Schema(description = "高考网链接")
    private String schoolInfoGaokaoUrl;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime schoolInfoCreateTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime schoolInfoUpdateTime;

    /**
     * 专业列表
     */
    @Schema(description = "专业列表")
    private List<SchoolMajorVO> schoolInfoMajors;
} 