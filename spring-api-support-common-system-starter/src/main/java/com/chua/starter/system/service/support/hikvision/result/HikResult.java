package com.chua.starter.system.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结果
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class HikResult {


    @JsonProperty("code")
    private String code;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("data")
    private Boolean data;
}
