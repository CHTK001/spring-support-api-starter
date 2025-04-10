package com.chua.starter.school.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GaokaoSchoolInfo {

    @SerializedName("code")
    private String code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private DataDTO data;
    @SerializedName("location")
    private String location;
    @SerializedName("encrydata")
    private String encrydata;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @SerializedName("item")
        private List<ItemDTO> item;
        @SerializedName("numFound")
        private Integer numFound;

        @NoArgsConstructor
        @Data
        public static class ItemDTO {
            @SerializedName("admissions")
            private String admissions;
            @SerializedName("answerurl")
            private String answerurl;
            @SerializedName("belong")
            private String belong;
            @SerializedName("central")
            private String central;
            @SerializedName("city_id")
            private String cityId;
            @SerializedName("city_name")
            private String cityName;
            @SerializedName("code_enroll")
            private String codeEnroll;
            @SerializedName("colleges_level")
            private String collegesLevel;
            @SerializedName("county_id")
            private String countyId;
            @SerializedName("county_name")
            private String countyName;
            @SerializedName("department")
            private String department;
            @SerializedName("doublehigh")
            private String doublehigh;
            @SerializedName("dual_class")
            private String dualClass;
            @SerializedName("dual_class_name")
            private String dualClassName;
            @SerializedName("f211")
            private Integer f211;
            @SerializedName("f985")
            private Integer f985;
            @SerializedName("hightitle")
            private String hightitle;
            @SerializedName("inner_rate")
            private Integer innerRate;
            @SerializedName("is_recruitment")
            private String isRecruitment;
            @SerializedName("level")
            private String level;
            @SerializedName("level_name")
            private String levelName;
            @SerializedName("name")
            private String name;
            @SerializedName("nature")
            private String nature;
            @SerializedName("nature_name")
            private String natureName;
            @SerializedName("outer_rate")
            private Integer outerRate;
            @SerializedName("province_id")
            private String provinceId;
            @SerializedName("province_name")
            private String provinceName;
            @SerializedName("rank")
            private String rank;
            @SerializedName("rank_type")
            private String rankType;
            @SerializedName("rate")
            private Integer rate;
            @SerializedName("recommend_master_level")
            private Integer recommendMasterLevel;
            @SerializedName("school_id")
            private Integer schoolId;
            @SerializedName("school_type")
            private String schoolType;
            @SerializedName("tag_name")
            private String tagName;
            @SerializedName("town_name")
            private String townName;
            @SerializedName("type")
            private String type;
            @SerializedName("type_name")
            private String typeName;
            @SerializedName("upgrading_level")
            private Integer upgradingLevel;
            @SerializedName("view_month")
            private String viewMonth;
            @SerializedName("view_total")
            private String viewTotal;
            @SerializedName("view_total_number")
            private String viewTotalNumber;
            @SerializedName("view_week")
            private String viewWeek;
        }
    }
}
