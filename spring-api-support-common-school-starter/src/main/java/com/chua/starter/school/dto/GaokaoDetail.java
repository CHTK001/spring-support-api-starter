package com.chua.starter.school.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GaokaoDetail {

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
        @SerializedName("school_id")
        private String schoolId;
        @SerializedName("data_code")
        private String dataCode;
        @SerializedName("name")
        private String name;
        @SerializedName("type")
        private String type;
        @SerializedName("school_type")
        private String schoolType;
        @SerializedName("school_nature")
        private String schoolNature;
        @SerializedName("level")
        private String level;
        @SerializedName("code_enroll")
        private String codeEnroll;
        @SerializedName("zs_code")
        private String zsCode;
        @SerializedName("belong")
        private String belong;
        @SerializedName("f985")
        private String f985;
        @SerializedName("f211")
        private String f211;
        @SerializedName("department")
        private String department;
        @SerializedName("admissions")
        private String admissions;
        @SerializedName("central")
        private String central;
        @SerializedName("dual_class")
        private String dualClass;
        @SerializedName("is_seal")
        private String isSeal;
        @SerializedName("applied_grade")
        private String appliedGrade;
        @SerializedName("vocational")
        private String vocational;
        @SerializedName("num_subject")
        private String numSubject;
        @SerializedName("num_master")
        private String numMaster;
        @SerializedName("num_doctor")
        private String numDoctor;
        @SerializedName("num_academician")
        private String numAcademician;
        @SerializedName("num_library")
        private String numLibrary;
        @SerializedName("num_lab")
        private String numLab;
        @SerializedName("province_id")
        private String provinceId;
        @SerializedName("city_id")
        private String cityId;
        @SerializedName("county_id")
        private String countyId;
        @SerializedName("is_ads")
        private String isAds;
        @SerializedName("is_recruitment")
        private String isRecruitment;
        @SerializedName("create_date")
        private String createDate;
        @SerializedName("area")
        private Integer area;
        @SerializedName("old_name")
        private String oldName;
        @SerializedName("is_fenxiao")
        private String isFenxiao;
        @SerializedName("status")
        private String status;
        @SerializedName("add_id")
        private String addId;
        @SerializedName("add_time")
        private String addTime;
        @SerializedName("update_id")
        private String updateId;
        @SerializedName("update_time")
        private String updateTime;
        @SerializedName("ad_level")
        private String adLevel;
        @SerializedName("short")
        private String shortX;
        @SerializedName("e_pc")
        private String ePc;
        @SerializedName("e_app")
        private String eApp;
        @SerializedName("ruanke_rank")
        private String ruankeRank;
        @SerializedName("single")
        private String single;
        @SerializedName("colleges_level")
        private String collegesLevel;
        @SerializedName("doublehigh")
        private String doublehigh;
        @SerializedName("wsl_rank")
        private String wslRank;
        @SerializedName("qs_rank")
        private String qsRank;
        @SerializedName("xyh_rank")
        private String xyhRank;
        @SerializedName("is_sell")
        private String isSell;
        @SerializedName("eol_rank")
        private String eolRank;
        @SerializedName("school_batch")
        private String schoolBatch;
        @SerializedName("us_rank")
        private String usRank;
        @SerializedName("is_logo")
        private String isLogo;
        @SerializedName("num_master2")
        private String numMaster2;
        @SerializedName("num_doctor2")
        private String numDoctor2;
        @SerializedName("ai_status")
        private String aiStatus;
        @SerializedName("is_ads2")
        private String isAds2;
        @SerializedName("coop_money")
        private String coopMoney;
        @SerializedName("bdold_name")
        private String bdoldName;
        @SerializedName("college_employment")
        private String collegeEmployment;
        @SerializedName("xyq_id")
        private String xyqId;
        @SerializedName("senior_status")
        private String seniorStatus;
        @SerializedName("senior_show")
        private String seniorShow;
        @SerializedName("last_updated")
        private String lastUpdated;
        @SerializedName("gbh_num")
        private String gbhNum;
        @SerializedName("motto")
        private String motto;
        @SerializedName("upgrading_rate")
        private String upgradingRate;
        @SerializedName("recommend_master_rate")
        private String recommendMasterRate;
        @SerializedName("recommend_master_level")
        private Integer recommendMasterLevel;
        @SerializedName("is_show_xcxcode")
        private Integer isShowXcxcode;
        @SerializedName("level_name")
        private String levelName;
        @SerializedName("type_name")
        private String typeName;
        @SerializedName("school_type_name")
        private String schoolTypeName;
        @SerializedName("school_nature_name")
        private String schoolNatureName;
        @SerializedName("dual_class_name")
        private String dualClassName;
        @SerializedName("xueke_rank")
        private XuekeRankDTO xuekeRank;
        @SerializedName("province_single")
        private ProvinceSingleDTO provinceSingle;
        @SerializedName("single_year")
        private Integer singleYear;
        @SerializedName("remark")
        private List<?> remark;
        @SerializedName("province_name")
        private String provinceName;
        @SerializedName("city_name")
        private String cityName;
        @SerializedName("town_name")
        private String townName;
        @SerializedName("weiwangzhan")
        private String weiwangzhan;
        @SerializedName("yjszs")
        private String yjszs;
        @SerializedName("xiaoyuan")
        private String xiaoyuan;
        @SerializedName("urllinks")
        private UrllinksDTO urllinks;
        @SerializedName("email")
        private String email;
        @SerializedName("school_email")
        private String schoolEmail;
        @SerializedName("address")
        private String address;
        @SerializedName("postcode")
        private String postcode;
        @SerializedName("site")
        private String site;
        @SerializedName("school_site")
        private String schoolSite;
        @SerializedName("phone")
        private String phone;
        @SerializedName("school_phone")
        private String schoolPhone;
        @SerializedName("miniprogram")
        private String miniprogram;
        @SerializedName("content")
        private String content;
        @SerializedName("video")
        private VideoDTO video;
        @SerializedName("video_pc")
        private VideoPcDTO videoPc;
        @SerializedName("is_video")
        private Integer isVideo;
        @SerializedName("dualclass")
        private List<DualclassDTO> dualclass;
        @SerializedName("school_special_num")
        private Integer schoolSpecialNum;
        @SerializedName("special")
        private List<SpecialDTO> special;
        @SerializedName("nature_name")
        private String natureName;
        @SerializedName("province_score_min")
        private ProvinceScoreMinDTO provinceScoreMin;
        @SerializedName("pro_type_min")
        private ProTypeMinDTO proTypeMin;
        @SerializedName("pro_type")
        private ProTypeDTO proType;
        @SerializedName("province_score_year")
        private String provinceScoreYear;
        @SerializedName("qs_world")
        private String qsWorld;
        @SerializedName("rank")
        private RankDTO rank;
        @SerializedName("fenxiao")
        private List<FenxiaoDTO> fenxiao;
        @SerializedName("gbh_url")
        private String gbhUrl;
        @SerializedName("is_yikao")
        private Integer isYikao;
        @SerializedName("yk_feature")
        private List<String> ykFeature;
        @SerializedName("yk_type")
        private List<String> ykType;
        @SerializedName("is_international_undergraduate")
        private Integer isInternationalUndergraduate;
        @SerializedName("is_evaluation")
        private Integer isEvaluation;
        @SerializedName("is_special_project")
        private Integer isSpecialProject;

        @NoArgsConstructor
        @Data
        public static class XuekeRankDTO {
            @SerializedName("C+")
            private String _$C169;// FIXME check this code
            @SerializedName("B-")
            private String b;
            @SerializedName("C-")
            private String c;
            @SerializedName("A+")
            private String _$A152;// FIXME check this code
            @SerializedName("A")
            private String a;
            @SerializedName("A-")
            private String a1;
            @SerializedName("B+")
            private String _$B239;// FIXME check this code
        }

        @NoArgsConstructor
        @Data
        public static class ProvinceSingleDTO {
        }

        @NoArgsConstructor
        @Data
        public static class UrllinksDTO {
            @SerializedName("71013")
            private _$71013DTO $71013;
            @SerializedName("71006")
            private _$71006DTO $71006;
            @SerializedName("71004")
            private _$71004DTO $71004;

            @NoArgsConstructor
            @Data
            public static class _$71013DTO {
                @SerializedName("school_id")
                private String schoolId;
                @SerializedName("link")
                private String link;
                @SerializedName("type")
                private String type;
                @SerializedName("img_url")
                private String imgUrl;
                @SerializedName("title")
                private String title;
            }

            @NoArgsConstructor
            @Data
            public static class _$71006DTO {
                @SerializedName("school_id")
                private String schoolId;
                @SerializedName("link")
                private String link;
                @SerializedName("type")
                private String type;
                @SerializedName("img_url")
                private String imgUrl;
                @SerializedName("title")
                private String title;
            }

            @NoArgsConstructor
            @Data
            public static class _$71004DTO {
                @SerializedName("school_id")
                private String schoolId;
                @SerializedName("link")
                private String link;
                @SerializedName("type")
                private String type;
                @SerializedName("img_url")
                private String imgUrl;
                @SerializedName("title")
                private String title;
            }
        }

        @NoArgsConstructor
        @Data
        public static class VideoDTO {
            @SerializedName("school_id")
            private String schoolId;
            @SerializedName("url")
            private String url;
            @SerializedName("url_type")
            private Integer urlType;
            @SerializedName("img_url")
            private String imgUrl;
        }

        @NoArgsConstructor
        @Data
        public static class VideoPcDTO {
            @SerializedName("school_id")
            private String schoolId;
            @SerializedName("url")
            private String url;
            @SerializedName("url_type")
            private Integer urlType;
            @SerializedName("img_url")
            private String imgUrl;
        }

        @NoArgsConstructor
        @Data
        public static class ProvinceScoreMinDTO {
            @SerializedName("12")
            private _$12DTO $12;
            @SerializedName("13")
            private _$13DTO $13;
            @SerializedName("14")
            private _$14DTO $14;
            @SerializedName("15")
            private _$15DTO $15;
            @SerializedName("21")
            private _$21DTO $21;
            @SerializedName("22")
            private _$22DTO $22;
            @SerializedName("23")
            private _$23DTO $23;
            @SerializedName("32")
            private _$32DTO $32;
            @SerializedName("33")
            private _$33DTO $33;
            @SerializedName("34")
            private _$34DTO $34;
            @SerializedName("35")
            private _$35DTO $35;
            @SerializedName("36")
            private _$36DTO $36;
            @SerializedName("37")
            private _$37DTO $37;
            @SerializedName("41")
            private _$41DTO $41;
            @SerializedName("42")
            private _$42DTO $42;
            @SerializedName("43")
            private _$43DTO $43;
            @SerializedName("44")
            private _$44DTO $44;
            @SerializedName("45")
            private _$45DTO $45;
            @SerializedName("46")
            private _$46DTO $46;
            @SerializedName("50")
            private _$50DTO $50;
            @SerializedName("51")
            private _$51DTO $51;
            @SerializedName("52")
            private _$52DTO $52;
            @SerializedName("53")
            private _$53DTO $53;
            @SerializedName("61")
            private _$61DTO $61;
            @SerializedName("62")
            private _$62DTO $62;
            @SerializedName("63")
            private _$63DTO $63;
            @SerializedName("64")
            private _$64DTO $64;
            @SerializedName("65")
            private _$65DTO $65;
            @SerializedName("11")
            private _$11DTO $11;
            @SerializedName("31")
            private _$31DTO $31;

            @NoArgsConstructor
            @Data
            public static class _$12DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$13DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$14DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$15DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$21DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$22DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$23DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$32DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$33DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$34DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$35DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$36DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$37DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$41DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$42DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$43DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$44DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$45DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$46DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$50DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$51DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$52DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$53DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$61DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$62DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$63DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$64DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$65DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$11DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }

            @NoArgsConstructor
            @Data
            public static class _$31DTO {
                @SerializedName("province_id")
                private String provinceId;
                @SerializedName("type")
                private String type;
                @SerializedName("min")
                private String min;
                @SerializedName("year")
                private String year;
            }
        }

        @NoArgsConstructor
        @Data
        public static class ProTypeMinDTO {
            @SerializedName("12")
            private List<_$12DTO> $12;
            @SerializedName("13")
            private List<_$13DTO> $13;
            @SerializedName("14")
            private List<_$14DTO> $14;
            @SerializedName("15")
            private List<_$15DTO> $15;
            @SerializedName("21")
            private List<_$21DTO> $21;
            @SerializedName("22")
            private List<_$22DTO> $22;
            @SerializedName("23")
            private List<_$23DTO> $23;
            @SerializedName("32")
            private List<_$32DTO> $32;
            @SerializedName("33")
            private List<_$33DTO> $33;
            @SerializedName("34")
            private List<_$34DTO> $34;
            @SerializedName("35")
            private List<_$35DTO> $35;
            @SerializedName("36")
            private List<_$36DTO> $36;
            @SerializedName("37")
            private List<_$37DTO> $37;
            @SerializedName("41")
            private List<_$41DTO> $41;
            @SerializedName("42")
            private List<_$42DTO> $42;
            @SerializedName("43")
            private List<_$43DTO> $43;
            @SerializedName("44")
            private List<_$44DTO> $44;
            @SerializedName("45")
            private List<_$45DTO> $45;
            @SerializedName("46")
            private List<_$46DTO> $46;
            @SerializedName("50")
            private List<_$50DTO> $50;
            @SerializedName("51")
            private List<_$51DTO> $51;
            @SerializedName("52")
            private List<_$52DTO> $52;
            @SerializedName("53")
            private List<_$53DTO> $53;
            @SerializedName("61")
            private List<_$61DTO> $61;
            @SerializedName("62")
            private List<_$62DTO> $62;
            @SerializedName("63")
            private List<_$63DTO> $63;
            @SerializedName("64")
            private List<_$64DTO> $64;
            @SerializedName("65")
            private List<_$65DTO> $65;
            @SerializedName("11")
            private List<_$11DTO> $11;
            @SerializedName("31")
            private List<_$31DTO> $31;

            @NoArgsConstructor
            @Data
            public static class _$12DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("3")
                    private String $3;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$13DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$14DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("1")
                    private String $1;
                    @SerializedName("2")
                    private String $2;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$15DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("1")
                    private String $1;
                    @SerializedName("2")
                    private String $2;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$21DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$22DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$23DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$32DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$33DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("3")
                    private String $3;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$34DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$35DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$36DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$37DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("3")
                    private String $3;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$41DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("1")
                    private String $1;
                    @SerializedName("2")
                    private String $2;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$42DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$43DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$44DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$45DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$46DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("3")
                    private String $3;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$50DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$51DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("1")
                    private String $1;
                    @SerializedName("2")
                    private String $2;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$52DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$53DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("1")
                    private String $1;
                    @SerializedName("2")
                    private String $2;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$61DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("1")
                    private String $1;
                    @SerializedName("2")
                    private String $2;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$62DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2073")
                    private String $2073;
                    @SerializedName("2074")
                    private String $2074;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$63DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("2")
                    private String $2;
                    @SerializedName("1")
                    private String $1;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$64DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("1")
                    private String $1;
                    @SerializedName("2")
                    private String $2;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$65DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("1")
                    private String $1;
                    @SerializedName("2")
                    private String $2;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$11DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("3")
                    private String $3;
                }
            }

            @NoArgsConstructor
            @Data
            public static class _$31DTO {
                @SerializedName("year")
                private Integer year;
                @SerializedName("type")
                private TypeDTO type;

                @NoArgsConstructor
                @Data
                public static class TypeDTO {
                    @SerializedName("3")
                    private String $3;
                }
            }
        }

        @NoArgsConstructor
        @Data
        public static class ProTypeDTO {
            @SerializedName("12")
            private List<String> $12;
            @SerializedName("13")
            private List<String> $13;
            @SerializedName("14")
            private List<String> $14;
            @SerializedName("15")
            private List<String> $15;
            @SerializedName("21")
            private List<String> $21;
            @SerializedName("22")
            private List<String> $22;
            @SerializedName("23")
            private List<String> $23;
            @SerializedName("32")
            private List<String> $32;
            @SerializedName("33")
            private List<String> $33;
            @SerializedName("34")
            private List<String> $34;
            @SerializedName("35")
            private List<String> $35;
            @SerializedName("36")
            private List<String> $36;
            @SerializedName("37")
            private List<String> $37;
            @SerializedName("41")
            private List<String> $41;
            @SerializedName("42")
            private List<String> $42;
            @SerializedName("43")
            private List<String> $43;
            @SerializedName("44")
            private List<String> $44;
            @SerializedName("45")
            private List<String> $45;
            @SerializedName("46")
            private List<String> $46;
            @SerializedName("50")
            private List<String> $50;
            @SerializedName("51")
            private List<String> $51;
            @SerializedName("52")
            private List<String> $52;
            @SerializedName("53")
            private List<String> $53;
            @SerializedName("61")
            private List<String> $61;
            @SerializedName("62")
            private List<String> $62;
            @SerializedName("63")
            private List<String> $63;
            @SerializedName("64")
            private List<String> $64;
            @SerializedName("65")
            private List<String> $65;
            @SerializedName("11")
            private List<String> $11;
            @SerializedName("31")
            private List<String> $31;
        }

        @NoArgsConstructor
        @Data
        public static class RankDTO {
            @SerializedName("ruanke_rank")
            private String ruankeRank;
            @SerializedName("xyh_rank")
            private String xyhRank;
            @SerializedName("qs_world")
            private String qsWorld;
            @SerializedName("us_rank")
            private String usRank;
            @SerializedName("rk_mb")
            private String rkMb;
            @SerializedName("rk_dl")
            private String rkDl;
            @SerializedName("rk_zw")
            private String rkZw;
            @SerializedName("rk_yy")
            private String rkYy;
            @SerializedName("rk_cj")
            private String rkCj;
            @SerializedName("rk_yuy")
            private String rkYuy;
            @SerializedName("rk_zf")
            private String rkZf;
            @SerializedName("rk_ty")
            private String rkTy;
            @SerializedName("rk_mz")
            private String rkMz;
            @SerializedName("xyh_gz_1")
            private String xyhGz1;
            @SerializedName("xyh_gz_2")
            private String xyhGz2;
            @SerializedName("xyh_gz_3")
            private String xyhGz3;
            @SerializedName("xyh_mb_1")
            private String xyhMb1;
            @SerializedName("xyh_mb_2")
            private String xyhMb2;
            @SerializedName("xyh_mb_3")
            private String xyhMb3;
            @SerializedName("tws_china")
            private String twsChina;
        }

        @NoArgsConstructor
        @Data
        public static class DualclassDTO {
            @SerializedName("id")
            private String id;
            @SerializedName("school_id")
            private String schoolId;
            @SerializedName("class")
            private String classX;
        }

        @NoArgsConstructor
        @Data
        public static class SpecialDTO {
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
            @SerializedName("level_name")
            private String levelName;
        }

        @NoArgsConstructor
        @Data
        public static class FenxiaoDTO {
            @SerializedName("fx_name")
            private String fxName;
            @SerializedName("yuanxi")
            private List<YuanxiDTO> yuanxi;

            @NoArgsConstructor
            @Data
            public static class YuanxiDTO {
                @SerializedName("id")
                private String id;
                @SerializedName("name")
                private String name;
            }
        }
    }
}
