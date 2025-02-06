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
public class CarAddResult {

    @JsonProperty("successes")
    private List<SuccessesDTO> successes;
    @JsonProperty("failures")
    private List<FailuresDTO> failures;

    @NoArgsConstructor
    @Data
    public static class SuccessesDTO {
        @JsonProperty("clientId")
        private Integer clientId;
        @JsonProperty("vehicleId")
        private String vehicleId;
    }

    @NoArgsConstructor
    @Data
    public static class FailuresDTO {
        @JsonProperty("clientId")
        private Integer clientId;
        @JsonProperty("code")
        private String code;
        @JsonProperty("msg")
        private String msg;
    }
}
