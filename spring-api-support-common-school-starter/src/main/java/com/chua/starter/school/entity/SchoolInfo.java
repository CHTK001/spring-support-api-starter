package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.common.support.annotations.ApiIgnore;
import com.chua.starter.common.support.group.IgnoreGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校信息实体类
 * 用于存储学校的基本信息，包括学校代码、名称、地址、联系方式等
 *
 * @author CH
 */
@Data
@TableName("school_info")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学校信息")
public class SchoolInfo extends SysBase {

    /**
     * 学校ID，主键
     */
    @TableId(value = "school_id", type = IdType.AUTO)
    @Schema(description = "学校ID")
    private Long schoolId;

    /**
     * 学校代码，唯一标识一所学校
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
     * 学校英文名称
     */
    @TableField("school_english_name")
    @Schema(description = "学校英文名")
    private String schoolEnglishName;

    /**
     * 学校类型，如综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等
     */
    @TableField("school_type")
    @Schema(description = "学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）")
    private String schoolType;

    /**
     * 学校所在省份ID
     */
    @TableField("school_province_id")
    @Schema(description = "学校所在省份ID")
    private String schoolProvinceId;

    /**
     * 学校所在城市ID
     */
    @TableField("school_city_id")
    @Schema(description = "学校所在城市ID")
    private String schoolCityId;

    /**
     * 学校所在区县ID
     */
    @TableField("school_district_id")
    @Schema(description = "学校所在区县ID")
    private String schoolDistrictId;

    /**
     * 学校详细地址
     */
    @TableField("school_address")
    @Schema(description = "学校地址")
    private String schoolAddress;

    /**
     * 学校创建时间
     */
    @TableField("school_create_time")
    @Schema(description = "学校创建时间")
    private String schoolCreateTime;

    /**
     * 学校办学层次，如本科、专科
     */
    @TableField("school_education_level")
    @Schema(description = "学校办学层次")
    private String schoolEducationLevel;

    /**
     * 学校办学性质，如公办、民办
     */
    @TableField("school_education_native")
    @Schema(description = "学校办学性质")
    private String schoolEducationNative;

    /**
     * 学校主管部门，如教育部、地方等
     */
    @TableField("school_subordinate_to")
    @Schema(description = "学校主管部门")
    private String schoolSubordinateTo;

    /**
     * 是否是985院校，0-否，1-是
     */
    @TableField("school_is_985")
    @Schema(description = "是否985院校(0:否,1:是)")
    private Integer schoolIs985;

    /**
     * 学校双一流等级
     */
    @TableField("school_dual")
    @Schema(description = "学校双一流等级")
    private String schoolDual;

    /**
     * 是否是211院校，0-否，1-是
     */
    @TableField("school_is_211")
    @Schema(description = "是否211院校(0:否,1:是)")
    private Integer schoolIs211;

    /**
     * 是否是双一流院校，0-否，1-是
     */
    @TableField("school_is_double_first")
    @Schema(description = "是否是双一流（0-否，1-是）")
    private Integer schoolIsDoubleFirst;

    /**
     * 学校官网地址
     */
    @TableField("school_website_url")
    @Schema(description = "学校网站URL")
    private String schoolWebsiteUrl;

    /**
     * 学校联系电话
     */
    @TableField("school_phone")
    @Schema(description = "学校电话")
    private String schoolPhone;

    /**
     * 学校邮编
     */
    @TableField("school_post_code")
    @Schema(description = "学校邮编")
    private String schoolPostCode;

    /**
     * 学校备注信息
     */
    @TableField("school_remark")
    @Schema(description = "备注")
    private String schoolRemark;

    /**
     * 学校建校时间
     */
    @TableField("school_founding_year")
    @Schema(description = "建校时间")
    private String schoolFoundingYear;

    /**
     * 学校介绍
     */
    @TableField("school_introduction")
    @Schema(description = "学校简介")
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
    @Schema(description = "学校logo URL")
    private String schoolLogoUrl;

    /**
     * 学校校徽 URL
     */
    @TableField("school_badge_url")
    @Schema(description = "学校校徽 URL")
    private String schoolBadgeUrl;

    /**
     * 学校招生网址
     */
    @TableField("school_admission_url")
    @Schema(description = "招生网址")
    private String schoolAdmissionUrl;

    /**
     * 学校录取批次
     */
    @TableField("school_admission")
    @Schema(description = "学校录取批次")
    private Integer schoolAdmission;

    /**
     * 是否是双高计划院校，0-否，1-是
     */
    @TableField("school_double_high")
    @Schema(description = "双高计划: 0（非双高院校）")
    private Integer schoolDoubleHigh;

    /**
     * 学校就业率
     */
    @TableField("school_employment_rate")
    @Schema(description = "就业率")
    private String schoolEmploymentRate;

    /**
     * 学校标签
     */
    @TableField("school_tag")
    @Schema(description = "学校标签")
    private String schoolTag;

    /**
     * 学校月访问量
     */
    @TableField("school_view_month")
    @Schema(description = "学校月访问量")
    private Integer schoolViewMonth;

    /**
     * 学校总访问量
     */
    @TableField("school_view_total")
    @Schema(description = "学校总访问量")
    private Integer schoolViewTotal;

    /**
     * 学校排名
     */
    @TableField("school_rank")
    @Schema(description = "学校排名")
    private Integer schoolRank;

    /**
     * 学校博士点数量
     */
    @TableField("school_num_doctor")
    @Schema(description = "博士点数量")
    private Integer schoolNumDoctor;

    /**
     * 学校硕士点数量
     */
    @TableField("school_num_master")
    @Schema(description = "硕士点数量")
    private Integer schoolNumMaster;

    /**
     * 学校学科数量
     */
    @TableField("school_num_subject")
    @Schema(description = "学科数量")
    private Integer schoolNumSubject;

    /**
     * 学校高考网链接
     */
    @TableField("school_gaokao_url")
    @Schema(description = "高考网链接")
    private String schoolGaokaoUrl;

    /**
     * 学校简介内容
     */
    @TableField("school_content")
    @Schema(description = "学校简介")
    private String schoolContent;

    /**
     * 学校邮箱
     */
    @TableField("school_email")
    @Schema(description = "学校邮箱")
    private String schoolEmail;

    /**
     * 删除标志，0-存在，1-删除
     */
    @TableField("school_deleted")
    @TableLogic
    @ApiIgnore(IgnoreGroup.class)
    @Schema(description = "删除标志（0-存在，1-删除）")
    private Integer schoolDeleted;
}