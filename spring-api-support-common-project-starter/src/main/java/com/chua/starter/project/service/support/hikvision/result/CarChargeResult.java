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
public class CarChargeResult {

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
        @JsonProperty("regionIndexCode")
        private String regionIndexCode;
        @JsonProperty("vehicleId")
        private String vehicleId;
        @JsonProperty("plateNo")
        private String plateNo;
        @JsonProperty("cardNo")
        private String cardNo;
        @JsonProperty("personId")
        private String personId;
        @JsonProperty("personName")
        private String personName;
        @JsonProperty("categoryCode")
        private String categoryCode;
        @JsonProperty("categoryName")
        private String categoryName;
        @JsonProperty("validity")
        private List<ValidityDTO> validity;

        @NoArgsConstructor
        @Data
        public static class ValidityDTO {
            @JsonProperty("parkSyscode")
            private String parkSyscode;
            @JsonProperty("parkName")
            private String parkName;
            @JsonProperty("functionTime")
            private List<FunctionTimeDTO> functionTime;

            @NoArgsConstructor
            @Data
            public static class FunctionTimeDTO {
                @JsonProperty("startTime")
                private String startTime;
                @JsonProperty("endTime")
                private String endTime;
            }
        }
    }
}
