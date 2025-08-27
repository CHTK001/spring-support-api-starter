package com.chua.starter.project.service.support.hikvision.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人脸添加返回结果
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class PeopleFaceResult {


    @JsonProperty("faceId")
    private String faceId;
    @JsonProperty("faceUrl")
    private String faceUrl;
    @JsonProperty("personId")
    private String personId;
}
