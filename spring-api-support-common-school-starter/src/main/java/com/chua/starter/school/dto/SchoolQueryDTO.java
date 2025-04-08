package com.chua.starter.school.dto;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.school.entity.SchoolInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校查询DTO
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "学校查询DTO")
public class SchoolQueryDTO extends Query<SchoolInfo> {

    /**
     * 学校名称（模糊查询）
     */
    @Schema(description = "学校名称（模糊查询）")
    private String schoolName;

    /**
     * 学校代码
     */
    @Schema(description = "学校代码")
    private String schoolCode;

    /**
     * 学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）
     */
    @Schema(description = "学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）")
    private String schoolType;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String schoolProvince;

    /**
     * 城市
     */
    @Schema(description = "城市")
    private String schoolCity;

    /**
     * 办学层次（本科、专科）
     */
    @Schema(description = "办学层次（本科、专科）")
    private String schoolEducationLevel;

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
}