package com.chua.starter.project.pojo.vincent;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文生图提交任务响应
 *
 * @author CH
 */
@NoArgsConstructor
@Data
@Schema(description = "文生图提交任务响应")
public class VincentDiagramV1AddTaskResponse {

    @SerializedName("output")
    private OutputDTO output;
    @SerializedName("request_id")
    private String requestId;

    @NoArgsConstructor
    @Data
    public static class OutputDTO {
        @SerializedName("task_status")
        private String taskStatus;
        @SerializedName("task_id")
        private String taskId;
        private List<String> images;
    }
}
