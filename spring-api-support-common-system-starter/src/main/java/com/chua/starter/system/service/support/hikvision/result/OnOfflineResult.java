package com.chua.starter.system.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 在离线情况
 */
@NoArgsConstructor
@Data
public class OnOfflineResult {


    @JsonProperty("code")
    private String code;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("total")
        private Integer total;
        @JsonProperty("list")
        private List<ListDTO> list;

        @NoArgsConstructor
        @Data
        public static class ListDTO {
            @JsonProperty("collectTime")
            private String collectTime;
            @JsonProperty("online")
            private Integer online;
        }
    }
}
