package com.chua.starter.school.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GaokaoPlan {

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
            @SerializedName("first_km")
            private String firstKm;
            @SerializedName("info")
            private String info;
            @SerializedName("length")
            private String length;
            @SerializedName("level2_name")
            private String level2Name;
            @SerializedName("level3_name")
            private String level3Name;
            @SerializedName("local_batch_name")
            private String localBatchName;
            @SerializedName("local_province_name")
            private String localProvinceName;
            @SerializedName("local_type_name")
            private String localTypeName;
            @SerializedName("name")
            private String name;
            @SerializedName("num")
            private String num;
            @SerializedName("province_name")
            private String provinceName;
            @SerializedName("remark")
            private String remark;
            @SerializedName("school_id")
            private String schoolId;
            @SerializedName("sg_fxk")
            private String sgFxk;
            @SerializedName("sg_info")
            private String sgInfo;
            @SerializedName("sg_name")
            private String sgName;
            @SerializedName("sg_sxk")
            private String sgSxk;
            @SerializedName("sg_type")
            private Integer sgType;
            @SerializedName("sp_fxk")
            private String spFxk;
            @SerializedName("sp_info")
            private String spInfo;
            @SerializedName("sp_name")
            private String spName;
            @SerializedName("sp_sxk")
            private String spSxk;
            @SerializedName("sp_type")
            private String spType;
            @SerializedName("sp_xuanke")
            private String spXuanke;
            @SerializedName("spcode")
            private String spcode;
            @SerializedName("special_group")
            private String specialGroup;
            @SerializedName("spname")
            private String spName2;
            @SerializedName("tuition")
            private String tuition;
            @SerializedName("tuition_unit")
            private String tuitionUnit;
            @SerializedName("year")
            private String year;
            @SerializedName("zslx_name")
            private String zslxName;
        }
    }
}
