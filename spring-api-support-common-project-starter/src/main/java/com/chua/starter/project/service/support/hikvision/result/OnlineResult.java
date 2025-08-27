package com.chua.starter.project.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 在线状态
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class OnlineResult {

    @JsonProperty("code")
    private String code;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("pageNo")
        private Integer pageNo;
        @JsonProperty("pageSize")
        private Integer pageSize;
        @JsonProperty("totalPage")
        private Integer totalPage;
        @JsonProperty("total")
        private Integer total;
        @JsonProperty("list")
        private List<ListDTO> list;

        @NoArgsConstructor
        @Data
        public static class ListDTO {
            @JsonProperty("deviceType")
            private String deviceType;
            @JsonProperty("deviceIndexCode")
            private Object deviceIndexCode;
            @JsonProperty("regionIndexCode")
            private String regionIndexCode;
            @JsonProperty("collectTime")
            private String collectTime;
            @JsonProperty("regionName")
            private String regionName;
            @JsonProperty("indexCode")
            private String indexCode;
            @JsonProperty("cn")
            private String cn;
            @JsonProperty("treatyType")
            private String treatyType;
            @JsonProperty("manufacturer")
            private String manufacturer;
            @JsonProperty("ip")
            private Object ip;
            @JsonProperty("port")
            private Object port;
            @JsonProperty("online")
            private Integer online;
        }
    }
}
