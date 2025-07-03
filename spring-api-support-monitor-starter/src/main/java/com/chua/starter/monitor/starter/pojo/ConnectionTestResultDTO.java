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
import java.util.Map;

/**
 * 连接测试结果DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ConnectionTestResultDTO", description = "连接测试结果DTO")
@Schema(description = "连接测试结果DTO")
public class ConnectionTestResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 测试地址
     */
    @ApiModelProperty(value = "测试地址")
    @Schema(description = "测试地址")
    private String address;

    /**
     * 连接类型
     */
    @ApiModelProperty(value = "连接类型")
    @Schema(description = "连接类型")
    private String connectionType;

    /**
     * 是否连接成功
     */
    @ApiModelProperty(value = "是否连接成功")
    @Schema(description = "是否连接成功")
    private Boolean success;

    /**
     * 响应时间 (毫秒)
     */
    @ApiModelProperty(value = "响应时间 (毫秒)")
    @Schema(description = "响应时间 (毫秒)")
    private Long responseTime;

    /**
     * HTTP状态码
     */
    @ApiModelProperty(value = "HTTP状态码")
    @Schema(description = "HTTP状态码")
    private Integer httpStatusCode;

    /**
     * 响应消息
     */
    @ApiModelProperty(value = "响应消息")
    @Schema(description = "响应消息")
    private String message;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 测试时间
     */
    @ApiModelProperty(value = "测试时间")
    @Schema(description = "测试时间")
    private LocalDateTime testTime;

    /**
     * 扩展信息
     */
    @ApiModelProperty(value = "扩展信息")
    @Schema(description = "扩展信息")
    private Map<String, Object> extendedInfo;

    /**
     * 获取连接状态描述
     */
    public String getStatusDescription() {
        if (success == null) {
            return "未知";
        }
        return success ? "连接成功" : "连接失败";
    }

    /**
     * 获取响应时间描述
     */
    public String getResponseTimeDescription() {
        if (responseTime == null) {
            return "未知";
        }
        if (responseTime < 100) {
            return "极快 (" + responseTime + "ms)";
        } else if (responseTime < 500) {
            return "快 (" + responseTime + "ms)";
        } else if (responseTime < 1000) {
            return "正常 (" + responseTime + "ms)";
        } else if (responseTime < 3000) {
            return "慢 (" + responseTime + "ms)";
        } else {
            return "很慢 (" + responseTime + "ms)";
        }
    }

    /**
     * 是否为HTTP连接
     */
    public boolean isHttpConnection() {
        return "HTTP".equalsIgnoreCase(connectionType) || "HTTPS".equalsIgnoreCase(connectionType);
    }

    /**
     * 是否为Socket连接
     */
    public boolean isSocketConnection() {
        return "SOCKET".equalsIgnoreCase(connectionType) || "TCP".equalsIgnoreCase(connectionType);
    }

    /**
     * 是否为Prometheus连接
     */
    public boolean isPrometheusConnection() {
        return "PROMETHEUS".equalsIgnoreCase(connectionType);
    }
}
