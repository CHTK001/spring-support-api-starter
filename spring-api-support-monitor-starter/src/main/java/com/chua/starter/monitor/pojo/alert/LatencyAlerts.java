package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 延迟告警信息实体类
 *
 * @author CH
 * @since 2024/12/25
 */
@ApiModel(description = "延迟告警信息")
@Schema(description = "延迟告警信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatencyAlerts implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 高延迟服务器列表
     */
    @ApiModelProperty(value = "高延迟服务器列表")
    @Schema(description = "高延迟服务器列表")
    private List<LatencyAlert> highLatencyServers;

    /**
     * 检测失败服务器列表
     */
    @ApiModelProperty(value = "检测失败服务器列表")
    @Schema(description = "检测失败服务器列表")
    private List<LatencyAlert> failedServers;

    /**
     * 总告警数量
     */
    @ApiModelProperty(value = "总告警数量")
    @Schema(description = "总告警数量")
    private Integer totalAlerts;

    /**
     * 高延迟告警数量
     */
    @ApiModelProperty(value = "高延迟告警数量")
    @Schema(description = "高延迟告警数量")
    private Integer highLatencyCount;

    /**
     * 失败告警数量
     */
    @ApiModelProperty(value = "失败告警数量")
    @Schema(description = "失败告警数量")
    private Integer failedCount;

    /**
     * 检查时间
     */
    @ApiModelProperty(value = "检查时间")
    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

    /**
     * 延迟告警详情
     */
    @ApiModel(description = "延迟告警详情")
    @Schema(description = "延迟告警详情")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatencyAlert implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 服务器ID
         */
        @ApiModelProperty(value = "服务器ID")
        @Schema(description = "服务器ID")
        private Integer serverId;

        /**
         * 服务器名称
         */
        @ApiModelProperty(value = "服务器名称")
        @Schema(description = "服务器名称")
        private String serverName;

        /**
         * 服务器主机地址
         */
        @ApiModelProperty(value = "服务器主机地址")
        @Schema(description = "服务器主机地址")
        private String serverHost;

        /**
         * 延迟时间
         */
        @ApiModelProperty(value = "延迟时间")
        @Schema(description = "延迟时间")
        private Long latency;

        /**
         * 状态描述
         */
        @ApiModelProperty(value = "状态描述")
        @Schema(description = "状态描述")
        private String status;

        /**
         * 告警级别
         */
        @ApiModelProperty(value = "告警级别")
        @Schema(description = "告警级别")
        private String alertLevel;

        /**
         * 最后检查时间
         */
        @ApiModelProperty(value = "最后检查时间")
        @Schema(description = "最后检查时间")
        private LocalDateTime lastCheckTime;

        /**
         * 告警消息
         */
        @ApiModelProperty(value = "告警消息")
        @Schema(description = "告警消息")
        private String alertMessage;
    }
}
