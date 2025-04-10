package com.chua.starter.school.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 高考分数数据DTO
 * 用于解析高考网API返回的分数数据
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class GaokaoScore {

    @SerializedName("code")
    private String code;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private DataDTO data;
    
    @SerializedName("md5")
    private String md5;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @SerializedName("item")
        private List<ItemDTO> item;
        
        @SerializedName("numFound")
        private Integer numFound;
    }

    @NoArgsConstructor
    @Data
    public static class ItemDTO {
        @SerializedName("year")
        private String year;
        
        @SerializedName("local_type_name")
        private String localTypeName;
        
        @SerializedName("local_batch_name")
        private String localBatchName;
        
        @SerializedName("zslx_name")
        private String zslxName;
        
        @SerializedName("max")
        private String max;
        
        @SerializedName("min")
        private String min;
        
        @SerializedName("average")
        private String average;
        
        @SerializedName("count")
        private String count;
        
        @SerializedName("province_score")
        private String provinceScore;
        
        @SerializedName("min_section")
        private String minSection;
        
        @SerializedName("level3_name")
        private String level3Name;
        
        @SerializedName("level3_code")
        private String level3Code;
    }
} 