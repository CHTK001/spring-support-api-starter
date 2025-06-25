package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 延迟检测结果实体类
 *
 * @author CH
 * @since 2024/12/25
 */
@ApiModel(description = "延迟检测结果")
@Schema(description = "延迟检测结果")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatencyDetectionResult implements Serializable {

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
     * 服务器端口
     */
    @ApiModelProperty(value = "服务器端口")
    @Schema(description = "服务器端口")
    private Integer serverPort;

    /**
     * 延迟时间(毫秒)
     */
    @ApiModelProperty(value = "延迟时间(毫秒)")
    @Schema(description = "延迟时间(毫秒)")
    private Long latency;

    /**
     * 检测是否成功
     */
    @ApiModelProperty(value = "检测是否成功")
    @Schema(description = "检测是否成功")
    private Boolean success;

    /**
     * 错误消息
     */
    @ApiModelProperty(value = "错误消息")
    @Schema(description = "错误消息")
    private String errorMessage;

    /**
     * 检测时间
     */
    @ApiModelProperty(value = "检测时间")
    @Schema(description = "检测时间")
    private LocalDateTime detectTime;

    /**
     * 延迟状态
     */
    @ApiModelProperty(value = "延迟状态")
    @Schema(description = "延迟状态")
    private Integer latencyStatus;

    /**
     * 延迟状态描述
     */
    @ApiModelProperty(value = "延迟状态描述")
    @Schema(description = "延迟状态描述")
    private String latencyStatusDesc;

    /**
     * 检测方法
     */
    @ApiModelProperty(value = "检测方法")
    @Schema(description = "检测方法")
    private String detectMethod;

    /**
     * 平均延迟
     */
    @ApiModelProperty(value = "平均延迟")
    @Schema(description = "平均延迟")
    private Long avgLatency;

    /**
     * 最大延迟
     */
    @ApiModelProperty(value = "最大延迟")
    @Schema(description = "最大延迟")
    private Long maxLatency;

    /**
     * 最小延迟
     */
    @ApiModelProperty(value = "最小延迟")
    @Schema(description = "最小延迟")
    private Long minLatency;

    /**
     * 检测次数
     */
    @ApiModelProperty(value = "检测次数")
    @Schema(description = "检测次数")
    private Long totalCount;

    /**
     * 失败次数
     */
    @ApiModelProperty(value = "失败次数")
    @Schema(description = "失败次数")
    private Long failCount;

    /**
     * 成功率
     */
    @ApiModelProperty(value = "成功率")
    @Schema(description = "成功率")
    private Double successRate;

    /**
     * 创建成功的检测结果
     */
    public static LatencyDetectionResult success(Integer serverId, String serverName, String serverHost, 
                                               Integer serverPort, Long latency, String detectMethod) {
        return LatencyDetectionResult.builder()
                .serverId(serverId)
                .serverName(serverName)
                .serverHost(serverHost)
                .serverPort(serverPort)
                .latency(latency)
                .success(true)
                .detectTime(LocalDateTime.now())
                .detectMethod(detectMethod)
                .build();
    }

    /**
     * 创建失败的检测结果
     */
    public static LatencyDetectionResult failure(Integer serverId, String serverName, String serverHost, 
                                               Integer serverPort, String errorMessage) {
        return LatencyDetectionResult.builder()
                .serverId(serverId)
                .serverName(serverName)
                .serverHost(serverHost)
                .serverPort(serverPort)
                .success(false)
                .errorMessage(errorMessage)
                .detectTime(LocalDateTime.now())
                .build();
    }
}
