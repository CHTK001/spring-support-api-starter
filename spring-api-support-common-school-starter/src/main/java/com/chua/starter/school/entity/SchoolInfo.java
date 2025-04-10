package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.common.support.annotations.ApiIgnore;
import com.chua.starter.common.support.group.IgnoreGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学校信息
 *
 * @author CH
 * 
 */
@Data
@TableName("school_info")
@Schema(description = "学校信息")
public class SchoolInfo extends SysBase {

    /**
     * 主键ID
     */
    @TableId(value = "school_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long schoolId;

    /**
     * 学校代码
     */
    @TableField("school_code")
    @Schema(description = "学校代码")
    private String schoolCode;

    /**
     * 学校名称
     */
    @TableField("school_name")
    @Schema(description = "学校名称")
    private String schoolName;

    /**
     * 学校英文名
     */
    @TableField("school_english_name")
    @Schema(description = "学校英文名")
    private String schoolEnglishName;

    /**
     * 学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）
     */
    @TableField("school_type")
    @Schema(description = "学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）")
    private String schoolType;

    /**
     * 所在省份
     */
    @TableField("school_province_id")
    @Schema(description = "所在省份")
    private String schoolProvinceId;

    /**
     * 所在城市
     */
    @TableField("school_city_id")
    @Schema(description = "所在城市")
    private String schoolCityId;

    /**
     * 所在区县
     */
    @TableField("school_district_id")
    @Schema(description = "所在区县")
    private String schoolDistrictId;

    /**
     * 详细地址
     */
    @TableField("school_address")
    @Schema(description = "详细地址")
    private String schoolAddress;
    /**
     * 创建时间
     */
    @TableField("school_create_time")
    @Schema(description = "建校时间")
    private String schoolCreateTime;

    /**
     * 办学层次（本科、专科）
     */
    @TableField("school_education_level")
    @Schema(description = "办学层次（本科、专科）")
    private String schoolEducationLevel;
    /**
     * 办学性质（985、211、普通）
     */
    @TableField("school_education_native")
    @Schema(description = "办学性质（985、211、普通）")
    private String schoolEducationNative;

    /**
     * 隶属于（教育部、地方等）
     */
    @TableField("school_subordinate_to")
    @Schema(description = "隶属于（教育部、地方等）")
    private String schoolSubordinateTo;

    /**
     * 是否是985（0-否，1-是）
     */
    @TableField("school_is_985")
    @Schema(description = "是否是985（0-否，1-是）")
    private Integer schoolIs985;
    /**
     * 是否是双一流类型
     */
    @TableField("school_dual")
    @Schema(description = "是否是双一流类型")
    private String schoolDual;

    /**
     * 是否是211（0-否，1-是）
     */
    @TableField("school_is_211")
    @Schema(description = "是否是211（0-否，1-是）")
    private Integer schoolIs211;

    /**
     * 是否是双一流（0-否，1-是）
     */
    @TableField("school_is_double_first")
    @Schema(description = "是否是双一流（0-否，1-是）")
    private Integer schoolIsDoubleFirst;

    /**
     * 官网地址
     */
    @TableField("school_website_url")
    @Schema(description = "官网地址")
    private String schoolWebsiteUrl;

    /**
     * 联系电话
     */
    @TableField("school_phone")
    @Schema(description = "联系电话")
    private String schoolPhone;
    /**
     * 邮编
     */
    @TableField("school_post_code")
    @Schema(description = "邮编")
    private String schoolPostCode;
    /**
     * 备注
     */
    @TableField("school_remark")
    @Schema(description = "备注")
    private String schoolRemark;

    /**
     * 建校时间
     */
    @TableField("school_founding_year")
    @Schema(description = "建校时间")
    private String schoolFoundingYear;

    /**
     * 学校介绍
     */
    @TableField("school_introduction")
    @Schema(description = "学校介绍")
    private String schoolIntroduction;

    /**
     * 学校特色
     */
    @TableField("school_features")
    @Schema(description = "学校特色")
    private String schoolFeatures;

    /**
     * 学校Logo URL
     */
    @TableField("school_logo_url")
    @Schema(description = "学校Logo URL")
    private String schoolLogoUrl;

    /**
     * 学校校徽 URL
     */
    @TableField("school_badge_url")
    @Schema(description = "学校校徽 URL")
    private String schoolBadgeUrl;

    /**
     * 招生网址
     */
    @TableField("school_admission_url")
    @Schema(description = "招生网址")
    private String schoolAdmissionUrl;
    /**
     * "招生状态": 1（表示正在招生）
     */
    @TableField("school_admission")
    @Schema(description = "招生状态")
    private Integer schoolAdmission;
    /**
     * "双高计划": 0（非双高院校）
     */
    @TableField("school_double_high")
    @Schema(description = "双高计划: 0（非双高院校）")
    private Integer schoolDoubleHigh;

    /**
     * 就业率
     */
    @TableField("school_employment_rate")
    @Schema(description = "就业率")
    private String schoolEmploymentRate;
    /**
     * 标签
     */
    @TableField("school_tag")
    @Schema(description = "标签")
    private String schoolTag;
    /**
     * 月访问量
     */
    @TableField("school_view_month")
    @Schema(description = "月访问量")
    private Integer schoolViewMonth;
    /**
     * 总访问量
     */
    @TableField("school_view_total")
    @Schema(description = "总访问量")
    private Integer schoolViewTotal;

    /**
     * 排名
     */
    @TableField("school_rank")
    @Schema(description = "排名")
    private Integer schoolRank;
    /**
     * 博士人数
     */
    @TableField("school_num_doctor")
    @Schema(description = "博士人数")
    private Integer schoolNumDoctor;
    /**
     * 硕士人数
     */
    @TableField("school_num_master")
    @Schema(description = "硕士人数")
    private Integer schoolNumMaster;
    /**
     * 国家一级重点学科
     */
    @TableField("school_num_subject")
    @Schema(description = "国家一级重点学科")
    private Integer schoolNumSubject;

    /**
     * 高考网链接
     */
    @TableField("school_gaokao_url")
    @Schema(description = "高考网链接")
    private String schoolGaokaoUrl;

    /**
     * 学校内容
     */
    @TableField("school_content")
    @Schema(description = "学校内容")
    private String schoolContent;
    /**
     * 学校邮箱
     */
    @TableField("school_email")
    @Schema(description = "学校邮箱")
    private String schoolEmail;
    /**
     * 删除标志（0-存在，1-删除）
     */
    @TableField("school_deleted")
    @TableLogic
    @ApiIgnore(IgnoreGroup.class)
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolDeleted;
}