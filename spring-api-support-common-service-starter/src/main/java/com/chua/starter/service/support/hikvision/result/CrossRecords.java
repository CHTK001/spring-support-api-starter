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
public class CrossRecords {

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
        @JsonProperty("crossRecordSyscode")
        private String crossRecordSyscode;
        @JsonProperty("parkSyscode")
        private String parkSyscode;
        @JsonProperty("parkName")
        private String parkName;
        @JsonProperty("entranceSyscode")
        private String entranceSyscode;
        @JsonProperty("entranceName")
        private String entranceName;
        @JsonProperty("roadwaySyscode")
        private String roadwaySyscode;
        @JsonProperty("roadwayName")
        private String roadwayName;
        @JsonProperty("vehicleOut")
        private Integer vehicleOut;
        @JsonProperty("releaseMode")
        private Integer releaseMode;
        @JsonProperty("releaseResult")
        private Integer releaseResult;
        @JsonProperty("releaseWay")
        private Integer releaseWay;
        @JsonProperty("releaseReason")
        private Integer releaseReason;
        @JsonProperty("plateNo")
        private String plateNo;
        @JsonProperty("cardNo")
        private String cardNo;
        @JsonProperty("vehicleColor")
        private Integer vehicleColor;
        @JsonProperty("vehicleType")
        private Integer vehicleType;
        @JsonProperty("plateColor")
        private Integer plateColor;
        @JsonProperty("plateType")
        private Integer plateType;
        @JsonProperty("carCategory")
        private String carCategory;
        @JsonProperty("carCategoryName")
        private String carCategoryName;
        @JsonProperty("vehiclePicUri")
        private String vehiclePicUri;
        @JsonProperty("plateNoPicUri")
        private String plateNoPicUri;
        @JsonProperty("facePicUri")
        private String facePicUri;
        @JsonProperty("aswSyscode")
        private String aswSyscode;
        @JsonProperty("crossTime")
        private String crossTime;
        @JsonProperty("createTime")
        private String createTime;
    }
}
