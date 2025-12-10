package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户端健康状态上报数据
 * 
 * @author CH
 * @since 2024/12/13
 */
@ApiModel(description = "客户端健康状态上报数据")
@Schema(description = "客户端健康状态上报数据")
@Data
public class ClientHealthReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户端IP地址
     */
    @ApiModelProperty(value = "客户端IP地址", required = true)
    @Schema(description = "客户端IP地址")
    @NotBlank(message = "客户端IP地址不能为空")
    private String clientIp;

    /**
     * 客户端端口号
     */
    @ApiModelProperty(value = "客户端端口号", required = true)
    @Schema(description = "客户端端口号")
    @NotNull(message = "客户端端口号不能为空")
    @Min(value = 1, message = "端口号必须大于0")
    @Max(value = 65535, message = "端口号必须小于等于65535")
    private Integer clientPort;

    /**
     * 上报时间戳
     */
    @ApiModelProperty(value = "上报时间戳", required = true)
    @Schema(description = "上报时间戳")
    @NotNull(message = "上报时间戳不能为空")
    private LocalDateTime reportTime;

    /**
     * 客户端版本号
     */
    @ApiModelProperty(value = "客户端版本号")
    @Schema(description = "客户端版本号")
    private String clientVersion;

    /**
     * 客户端主机名
     */
    @ApiModelProperty(value = "客户端主机名")
    @Schema(description = "客户端主机名")
    private String hostname;

    /**
     * 操作系统信息
     */
    @ApiModelProperty(value = "操作系统信息")
    @Schema(description = "操作系统信息")
    private String osInfo;

    /**
     * CPU使用率（百分比）
     */
    @ApiModelProperty(value = "CPU使用率")
    @Schema(description = "CPU使用率（百分比）")
    @Min(value = 0, message = "CPU使用率不能小于0")
    @Max(value = 100, message = "CPU使用率不能大于100")
    private Double cpuUsage;

    /**
     * 内存使用率（百分比）
     */
    @ApiModelProperty(value = "内存使用率")
    @Schema(description = "内存使用率（百分比）")
    @Min(value = 0, message = "内存使用率不能小于0")
    @Max(value = 100, message = "内存使用率不能大于100")
    private Double memoryUsage;

    /**
     * 磁盘使用率（百分比）
     */
    @ApiModelProperty(value = "磁盘使用率")
    @Schema(description = "磁盘使用率（百分比）")
    @Min(value = 0, message = "磁盘使用率不能小于0")
    @Max(value = 100, message = "磁盘使用率不能大于100")
    private Double diskUsage;

    /**
     * 网络延迟（毫秒）
     */
    @ApiModelProperty(value = "网络延迟")
    @Schema(description = "网络延迟（毫秒）")
    @Min(value = 0, message = "网络延迟不能小于0")
    private Long networkLatency;

    /**
     * 额外信息（JSON格式）
     */
    @ApiModelProperty(value = "额外信息")
    @Schema(description = "额外信息（JSON格式）")
    private String extraInfo;

    /**
     * 获取客户端地址
     * 
     * @return 客户端地址（IP:端口）
     */
    public String getClientAddress() {
        return clientIp + ":" + clientPort;
    }

    /**
     * 判断健康状态是否正常
     * 
     * @return true表示健康状态正常
     */
    public boolean isHealthy() {
        // 基本的健康检查逻辑
        if (cpuUsage != null && cpuUsage > 90) {
            return false;
        }
        if (memoryUsage != null && memoryUsage > 90) {
            return false;
        }
        if (diskUsage != null && diskUsage > 95) {
            return false;
        }
        if (networkLatency != null && networkLatency > 5000) {
            return false;
        }
        return true;
    }

    /**
     * 获取健康状态描述
     * 
     * @return 健康状态描述
     */
    public String getHealthStatus() {
        if (isHealthy()) {
            return "健康";
        }
        
        StringBuilder status = new StringBuilder();
        if (cpuUsage != null && cpuUsage > 90) {
            status.append("CPU使用率过高(").append(cpuUsage).append("%) ");
        }
        if (memoryUsage != null && memoryUsage > 90) {
            status.append("内存使用率过高(").append(memoryUsage).append("%) ");
        }
        if (diskUsage != null && diskUsage > 95) {
            status.append("磁盘使用率过高(").append(diskUsage).append("%) ");
        }
        if (networkLatency != null && networkLatency > 5000) {
            status.append("网络延迟过高(").append(networkLatency).append("ms) ");
        }
        
        return status.toString().trim();
    }

    /**
     * 创建默认的健康状态上报
     * 
     * @param clientIp 客户端IP
     * @param clientPort 客户端端口
     * @return 健康状态上报对象
     */
    public static ClientHealthReport createDefault(String clientIp, Integer clientPort) {
        ClientHealthReport report = new ClientHealthReport();
        report.setClientIp(clientIp);
        report.setClientPort(clientPort);
        report.setReportTime(LocalDateTime.now());
        return report;
    }
}
