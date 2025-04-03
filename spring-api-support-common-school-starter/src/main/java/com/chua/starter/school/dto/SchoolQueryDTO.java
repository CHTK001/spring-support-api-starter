package com.chua.starter.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学校查询DTO
 *
 * @author CH
 */
@Data
@Schema(description = "学校查询DTO")
public class SchoolQueryDTO {

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
     * 省份
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）
     */
    @Schema(description = "学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）")
    private String schoolType;

    /**
     * 办学层次（本科、专科）
     */
    @Schema(description = "办学层次（本科、专科）")
    private String educationLevel;

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
     * 排序字段
     */
    @Schema(description = "排序字段")
    private String sortField;

    /**
     * 排序方式（asc/desc）
     */
    @Schema(description = "排序方式（asc/desc）")
    private String sortOrder;

    /**
     * 页码
     */
    @Schema(description = "页码")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private Integer pageSize = 10;
} 