package com.chua.starter.school.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 高考专业数据DTO
 * 用于解析高考网API返回的专业数据
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class GaokaoSpecialty {

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
        @SerializedName("special_code")
        private String specialCode;
        
        @SerializedName("special_name")
        private String specialName;
        
        @SerializedName("special_type")
        private String specialType;
        
        @SerializedName("type_name")
        private String typeName;
        
        @SerializedName("degree")
        private String degree;
        
        @SerializedName("level2_code")
        private String level2Code;
        
        @SerializedName("level2_name")
        private String level2Name;
        
        @SerializedName("level1_code")
        private String level1Code;
        
        @SerializedName("level1_name")
        private String level1Name;
        
        @SerializedName("limit_year")
        private Integer limitYear;
        
        @SerializedName("what_major")
        private String whatMajor;
        
        @SerializedName("course")
        private String course;
        
        @SerializedName("what_work")
        private String whatWork;
    }
} 