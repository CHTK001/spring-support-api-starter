package com.chua.starter.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author CH
 */
@NoArgsConstructor
@Data
public class DeviceResourceResourcesResult {


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
        @JsonProperty("indexCode")
        private String indexCode;
        @JsonProperty("name")
        private String name;
        @JsonProperty("resourceType")
        private String resourceType = "";
        @JsonProperty("deviceType")
        private String deviceType;
        private Integer online;
        private Integer status = 1;
        private String netAddress;
        private String validateCode;
        private String latitude;
        private String longitude;
        @JsonProperty("version")
        private String version;
        @JsonProperty("doorNo")
        private Integer doorNo;
        @JsonProperty("description")
        private String description;
        @JsonProperty("parentIndexCodes")
        private String parentIndexCodes;
        @JsonProperty("regionIndexCode")
        private String regionIndexCode;
        @JsonProperty("regionPath")
        private String regionPath;
        @JsonProperty("channelType")
        private String channelType;
        @JsonProperty("channelNo")
        private String channelNo;
        private Integer channelCount = 1;
        @JsonProperty("installLocation")
        private String installLocation;
        @JsonProperty("capabilitySet")
        private String capabilitySet;
        @JsonProperty("controlOneId")
        private String controlOneId;
        @JsonProperty("controlTwoId")
        private String controlTwoId;
        @JsonProperty("readerInId")
        private String readerInId;
        @JsonProperty("readerOutId")
        private String readerOutId;
        @JsonProperty("comId")
        private String comId;
        @JsonProperty("createTime")
        private String createTime;
        @JsonProperty("updateTime")
        private String updateTime;
    }
}
