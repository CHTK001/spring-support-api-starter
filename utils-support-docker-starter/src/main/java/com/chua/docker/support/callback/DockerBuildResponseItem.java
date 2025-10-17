package com.chua.docker.support.callback;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dockerjava.api.model.ErrorDetail;
import com.github.dockerjava.api.model.ResponseItem;
import lombok.Data;

import java.util.HashMap;

/**
 * Docker镜像构建响应项
 * 用于封装Docker镜像构建过程中的响应数据
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/1/17
 */
@Data
public class DockerBuildResponseItem {

    /**
     * 原始值映射，用于存储未映射的字段
     */
    HashMap<String, Object> rawValues = new HashMap<>();
    /**
     * 流信息，包含构建过程中的文本输出
     */
    @JsonProperty("stream")
    private String stream;
    /**
     * 状态信息，如"Step 1/5", "Successfully built"等
     */
    @JsonProperty("status")
    private String status;
    /**
     * 进度详情，包含当前进度和总进度信息
     */
    @JsonProperty("progressDetail")
    private ResponseItem.ProgressDetail progressDetail;
    /**
     * 进度字符串表示（已废弃，建议使用progressDetail）
     */
    @Deprecated
    @JsonProperty("progress")
    private String progress;
    /**
     * 构建步骤ID或镜像ID
     */
    @JsonProperty("id")
    private String id;
    /**
     * 来源信息
     */
    @JsonProperty("from")
    private String from;
    /**
     * 时间戳
     */
    @JsonProperty("time")
    private Long time;
    /**
     * 错误详情，当构建过程中出现错误时包含详细错误信息
     */
    @JsonProperty("errorDetail")
    private ErrorDetail errorDetail;
    /**
     * 错误信息（已废弃，建议使用errorDetail）
     */
    @Deprecated
    @JsonProperty("error")
    private String error;
    /**
     * 辅助详情信息
     */
    @JsonProperty("aux")
    private ResponseItem.AuxDetail aux;

    /**
     * 判断是否为错误响应
     *
     * @return 如果包含错误信息则返回true，否则返回false
     */
    public boolean hasError() {
        return errorDetail != null || (error != null && !error.trim().isEmpty());
    }

    /**
     * 获取错误消息
     *
     * @return 错误消息字符串，如果没有错误则返回null
     */
    public String getErrorMessage() {
        if (errorDetail != null && errorDetail.getMessage() != null) {
            return errorDetail.getMessage();
        }
        return error;
    }

    /**
     * 判断是否包含进度信息
     *
     * @return 如果包含进度信息则返回true，否则返回false
     */
    public boolean hasProgress() {
        return progressDetail != null && progressDetail.getCurrent() != null && progressDetail.getTotal() != null;
    }

    /**
     * 获取进度百分比
     *
     * @return 进度百分比（0-100），如果无法计算则返回-1
     */
    public double getProgressPercentage() {
        if (!hasProgress()) {
            return -1;
        }
        long current = progressDetail.getCurrent();
        long total = progressDetail.getTotal();
        if (total == 0) {
            return 0;
        }
        return (double) current / total * 100;
    }

    /**
     * 判断是否为构建步骤信息
     *
     * @return 如果是构建步骤则返回true
     */
    public boolean isBuildStep() {
        return stream != null && (stream.contains("Step ") || stream.contains("---> "));
    }

    /**
     * 判断是否为构建成功信息
     *
     * @return 如果构建成功则返回true
     */
    public boolean isBuildSuccess() {
        return stream != null && stream.contains("Successfully built");
    }
}