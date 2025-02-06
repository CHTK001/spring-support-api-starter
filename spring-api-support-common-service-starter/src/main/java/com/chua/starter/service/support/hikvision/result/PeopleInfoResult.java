package com.chua.starter.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CH
 */
@NoArgsConstructor
@Data
public class PeopleInfoResult {


    @JsonProperty("personId")
    private String personId;
    @JsonProperty("faceId")
    private String faceId;
}
