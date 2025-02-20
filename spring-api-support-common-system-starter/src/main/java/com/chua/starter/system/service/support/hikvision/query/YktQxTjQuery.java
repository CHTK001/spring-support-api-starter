package com.chua.starter.system.service.support.hikvision.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * 添加权限配置
 *
 * @author CH
 */
@Data
@Builder
public class YktQxTjQuery {


    @JsonProperty("personDatas")
    @Singular("personData")
    private List<PersonDatasDTO> personDatas;
    @JsonProperty("resourceInfos")
    @Singular("resourceInfo")
    private List<ResourceInfosDTO> resourceInfos;
    @JsonProperty("startTime")
    private String startTime;
    @JsonProperty("endTime")
    private String endTime;

    @Data
    @Builder
    public static class PersonDatasDTO {
        @JsonProperty("indexCodes")
        @Singular("indexCodes")
        private List<String> indexCodes;
        @JsonProperty("personDataType")
        private String personDataType;
    }

    @Data
    @Builder
    public static class ResourceInfosDTO {
        @JsonProperty("resourceIndexCode")
        private String resourceIndexCode;
        @JsonProperty("resourceType")
        private String resourceType;
        @JsonProperty("channelNos")
        @Singular("channelNo")
        private List<Integer> channelNos;
    }
}
