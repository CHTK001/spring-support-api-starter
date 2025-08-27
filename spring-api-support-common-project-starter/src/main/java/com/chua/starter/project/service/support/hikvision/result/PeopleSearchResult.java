package com.chua.starter.project.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author CH
 */
@NoArgsConstructor
@Data
public class PeopleSearchResult {

    @JsonProperty("total")
    private Integer total;
    @JsonProperty("pageNo")
    private Integer pageNo;
    @JsonProperty("pageSize")
    private Integer pageSize;
    @JsonProperty("list")
    private List<ListDTO> list;

    @NoArgsConstructor
    @Data
    public static class ListDTO {
        @JsonProperty("personId")
        private String personId;
        @JsonProperty("personName")
        private String personName;
        @JsonProperty("gender")
        private Integer gender;
        @JsonProperty("phone")
        private String phone;
        @JsonProperty("jobNo")
        private String jobNo;
        @JsonProperty("orgIndexCode")
        private String orgIndexCode;
        @JsonProperty("certificateType")
        private Integer certificateType;
        @JsonProperty("certificateNo")
        private String certificateNo;
        @JsonProperty("createTime")
        private String createTime;
        @JsonProperty("updateTime")
        private String updateTime;
        @JsonProperty("orgPath")
        private String orgPath;
        @JsonProperty("orgPathName")
        private String orgPathName;
        @JsonProperty("personPhoto")
        private List<PersonPhotoDTO> personPhoto;

        @NoArgsConstructor
        @Data
        public static class PersonPhotoDTO {
            @JsonProperty("personPhotoIndexCode")
            private String personPhotoIndexCode;
            @JsonProperty("picUri")
            private String picUri;
            @JsonProperty("serverIndexCode")
            private String serverIndexCode;
            @JsonProperty("personId")
            private String personId;
        }
    }
}
