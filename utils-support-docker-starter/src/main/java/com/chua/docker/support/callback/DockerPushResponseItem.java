package com.chua.docker.support.callback;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dockerjava.api.model.ErrorDetail;
import com.github.dockerjava.api.model.ResponseItem;
import com.github.dockerjava.core.RemoteApiVersion;
import lombok.Data;

import java.util.HashMap;

/**
 * 推送响应
 *
 * @author CH
 * @since 2025/9/14 12:36
 */
@Data
public class DockerPushResponseItem {
    HashMap<String, Object> rawValues = new HashMap<>();
    @JsonProperty("stream")
    private String stream;
    @JsonProperty("status")
    private String status;
    @JsonProperty("progressDetail")
    private ResponseItem.ProgressDetail progressDetail;
    @Deprecated
    @JsonProperty("progress")
    private String progress;
    @JsonProperty("id")
    private String id;
    @JsonProperty("from")
    private String from;
    @JsonProperty("time")
    private Long time;
    @JsonProperty("errorDetail")
    private ErrorDetail errorDetail;
    @Deprecated
    @JsonProperty("error")
    private String error;
    /**
     * @since {@link RemoteApiVersion#VERSION_1_22}
     */
    @JsonProperty("aux")
    private ResponseItem.AuxDetail aux;
}
