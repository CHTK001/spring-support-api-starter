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
public class CarSearchResult {


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
        @JsonProperty("vehicleId")
        private String vehicleId;
        @JsonProperty("plateNo")
        private String plateNo;
        @JsonProperty("cardNo")
        private String cardNo;
        @JsonProperty("personId")
        private String personId;
        @JsonProperty("plateType")
        private String plateType;
        @JsonProperty("plateColor")
        private String plateColor;
        @JsonProperty("vehicleType")
        private String vehicleType;
        @JsonProperty("vehicleColor")
        private String vehicleColor;
        @JsonProperty("regionIndexCode")
        private String regionIndexCode;
        @JsonProperty("vehicleGroup")
        private String vehicleGroup;
    }
}
