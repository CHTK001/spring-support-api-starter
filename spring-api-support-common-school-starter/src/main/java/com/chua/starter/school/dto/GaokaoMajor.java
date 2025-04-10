package com.chua.starter.school.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GaokaoMajor {

    @SerializedName("code")
    private String code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private DataDTO data;
    @SerializedName("md5")
    private String md5;
    @SerializedName("time")
    private String time;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @SerializedName("1")
        private List<_$1DTO> $1;
        @SerializedName("2")
        private String $2;
        @SerializedName("special_detail")
        private SpecialDetailDTO specialDetail;
        @SerializedName("special")
        private List<SpecialDTO> special;
        @SerializedName("nation_feature")
        private List<NationFeatureDTO> nationFeature;
        @SerializedName("educational_strength")
        private EducationalStrengthDTO educationalStrength;

        @NoArgsConstructor
        @Data
        public static class SpecialDetailDTO {
            @SerializedName("1")
            private List<_$1DTO> $1;
            @SerializedName("2")
            private List<?> $2;
            @SerializedName("3")
            private List<_$3DTO> $3;
            @SerializedName("4")
            private List<_$4DTO> $4;

            @NoArgsConstructor
            @Data
            public static class _$1DTO {
                @SerializedName("id")
                private String id;
                @SerializedName("school_id")
                private String schoolId;
                @SerializedName("special_id")
                private String specialId;
                @SerializedName("nation_feature")
                private String nationFeature;
                @SerializedName("province_feature")
                private String provinceFeature;
                @SerializedName("is_important")
                private String isImportant;
                @SerializedName("limit_year")
                private String limitYear;
                @SerializedName("year")
                private String year;
                @SerializedName("level3_weight")
                private String level3Weight;
                @SerializedName("nation_first_class")
                private String nationFirstClass;
                @SerializedName("xueke_rank")
                private String xuekeRank;
                @SerializedName("xueke_rank_score")
                private String xuekeRankScore;
                @SerializedName("ruanke_rank")
                private String ruankeRank;
                @SerializedName("ruanke_level")
                private String ruankeLevel;
                @SerializedName("is_video")
                private Integer isVideo;
                @SerializedName("special_name")
                private String specialName;
                @SerializedName("special_type")
                private String specialType;
                @SerializedName("type_name")
                private String typeName;
                @SerializedName("level3_name")
                private String level3Name;
                @SerializedName("level3_code")
                private String level3Code;
                @SerializedName("level2_name")
                private String level2Name;
                @SerializedName("level2_id")
                private String level2Id;
                @SerializedName("level2_code")
                private String level2Code;
                @SerializedName("code")
                private String code;
            }

            @NoArgsConstructor
            @Data
            public static class _$3DTO {
                @SerializedName("id")
                private String id;
                @SerializedName("school_id")
                private String schoolId;
                @SerializedName("name")
                private String name;
            }

            @NoArgsConstructor
            @Data
            public static class _$4DTO {
                @SerializedName("id")
                private String id;
                @SerializedName("school_id")
                private String schoolId;
                @SerializedName("class")
                private String classX;
            }
        }

        @NoArgsConstructor
        @Data
        public static class EducationalStrengthDTO {
            @SerializedName("num_doctor")
            private String numDoctor;
            @SerializedName("num_doctor2")
            private String numDoctor2;
            @SerializedName("num_master")
            private String numMaster;
            @SerializedName("num_master2")
            private String numMaster2;
            @SerializedName("num_subject")
            private String numSubject;
            @SerializedName("gbh_num")
            private String gbhNum;
            @SerializedName("a_subject")
            private String aSubject;
            @SerializedName("dualclass")
            private Integer dualclass;
            @SerializedName("nation_feature")
            private Integer nationFeature;
        }

        @NoArgsConstructor
        @Data
        public static class _$1DTO {
            @SerializedName("id")
            private String id;
            @SerializedName("school_id")
            private String schoolId;
            @SerializedName("special_id")
            private String specialId;
            @SerializedName("nation_feature")
            private String nationFeature;
            @SerializedName("province_feature")
            private String provinceFeature;
            @SerializedName("is_important")
            private String isImportant;
            @SerializedName("limit_year")
            private String limitYear;
            @SerializedName("year")
            private String year;
            @SerializedName("level3_weight")
            private String level3Weight;
            @SerializedName("nation_first_class")
            private String nationFirstClass;
            @SerializedName("xueke_rank")
            private String xuekeRank;
            @SerializedName("xueke_rank_score")
            private String xuekeRankScore;
            @SerializedName("ruanke_rank")
            private String ruankeRank;
            @SerializedName("ruanke_level")
            private String ruankeLevel;
            @SerializedName("is_video")
            private Integer isVideo;
            @SerializedName("special_name")
            private String specialName;
            @SerializedName("special_type")
            private String specialType;
            @SerializedName("type_name")
            private String typeName;
            @SerializedName("level3_name")
            private String level3Name;
            @SerializedName("level3_code")
            private String level3Code;
            @SerializedName("level2_name")
            private String level2Name;
            @SerializedName("level2_id")
            private String level2Id;
            @SerializedName("level2_code")
            private String level2Code;
            @SerializedName("code")
            private String code;
        }

        @NoArgsConstructor
        @Data
        public static class SpecialDTO {
            @SerializedName("level3_weight")
            private String level3Weight;
            @SerializedName("code")
            private String code;
            @SerializedName("name")
            private String name;
            @SerializedName("special")
            private List<SpecialDTO1> special;

            @NoArgsConstructor
            @Data
            public static class SpecialDTO1 {
                @SerializedName("id")
                private String id;
                @SerializedName("school_id")
                private String schoolId;
                @SerializedName("special_id")
                private String specialId;
                @SerializedName("nation_feature")
                private String nationFeature;
                @SerializedName("province_feature")
                private String provinceFeature;
                @SerializedName("is_important")
                private String isImportant;
                @SerializedName("limit_year")
                private String limitYear;
                @SerializedName("year")
                private String year;
                @SerializedName("level3_weight")
                private String level3Weight;
                @SerializedName("nation_first_class")
                private String nationFirstClass;
                @SerializedName("xueke_rank")
                private String xuekeRank;
                @SerializedName("xueke_rank_score")
                private String xuekeRankScore;
                @SerializedName("ruanke_rank")
                private String ruankeRank;
                @SerializedName("ruanke_level")
                private String ruankeLevel;
                @SerializedName("is_video")
                private Integer isVideo;
                @SerializedName("special_name")
                private String specialName;
                @SerializedName("special_type")
                private String specialType;
                @SerializedName("type_name")
                private String typeName;
                @SerializedName("level3_name")
                private String level3Name;
                @SerializedName("level3_code")
                private String level3Code;
                @SerializedName("level2_name")
                private String level2Name;
                @SerializedName("level2_id")
                private String level2Id;
                @SerializedName("level2_code")
                private String level2Code;
                @SerializedName("code")
                private String code;
            }
        }

        @NoArgsConstructor
        @Data
        public static class NationFeatureDTO {
            @SerializedName("id")
            private String id;
            @SerializedName("school_id")
            private String schoolId;
            @SerializedName("special_id")
            private String specialId;
            @SerializedName("nation_feature")
            private String nationFeature;
            @SerializedName("province_feature")
            private String provinceFeature;
            @SerializedName("is_important")
            private String isImportant;
            @SerializedName("limit_year")
            private String limitYear;
            @SerializedName("year")
            private String year;
            @SerializedName("level3_weight")
            private String level3Weight;
            @SerializedName("nation_first_class")
            private String nationFirstClass;
            @SerializedName("xueke_rank")
            private String xuekeRank;
            @SerializedName("xueke_rank_score")
            private String xuekeRankScore;
            @SerializedName("ruanke_rank")
            private String ruankeRank;
            @SerializedName("ruanke_level")
            private String ruankeLevel;
            @SerializedName("is_video")
            private Integer isVideo;
            @SerializedName("special_name")
            private String specialName;
            @SerializedName("special_type")
            private String specialType;
            @SerializedName("type_name")
            private String typeName;
            @SerializedName("level3_name")
            private String level3Name;
            @SerializedName("level3_id")
            private String level3Id;
            @SerializedName("level3_code")
            private String level3Code;
            @SerializedName("level2_name")
            private String level2Name;
            @SerializedName("level2_id")
            private String level2Id;
            @SerializedName("level2_code")
            private String level2Code;
            @SerializedName("code")
            private String code;
        }
    }
}
