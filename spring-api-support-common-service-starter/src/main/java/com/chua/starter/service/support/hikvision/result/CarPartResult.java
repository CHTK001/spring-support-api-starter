package com.chua.starter.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CH
 */
@NoArgsConstructor
@Data
public class CarPartResult {

    @JsonProperty("parkIndexCode")
    private String parkIndexCode;
    @JsonProperty("parkName")
    private String parkName;
    @JsonProperty("parentParkIndexCode")
    private String parentParkIndexCode;
    @JsonProperty("createTime")
    private String createTime;
    @JsonProperty("updateTime")
    private String updateTime;
}
