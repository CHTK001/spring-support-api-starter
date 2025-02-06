package com.chua.starter.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PreviewUrlResult {

    @JsonProperty("code")
    private String code;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("url")
        private String url;
    }
}
