package com.chua.report.client.starter.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户端健康信息
 * @author CH
 * @since 2024/12/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientHealthInfo {

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 是否健康
     */
    private Boolean healthy;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdateTime;

    /**
     * 健康状态有效期（秒）
     */
    private Long validityPeriod;

    /**
     * 健康状态过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 客户端版本
     */
    private String clientVersion;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 客户端端口
     */
    private Integer clientPort;

    /**
     * 操作系统信息
     */
    private String osInfo;

    /**
     * Java版本
     */
    private String javaVersion;

    /**
     * 启动时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 运行时长（秒）
     */
    private Long uptime;

    /**
     * 额外属性
     */
    private java.util.Map<String, Object> properties;

    /**
     * 创建健康信息
     */
    public static ClientHealthInfo create(String deviceId, String deviceName, long validityPeriod) {
        LocalDateTime now = LocalDateTime.now();
        return ClientHealthInfo.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .healthy(true)
                .lastUpdateTime(now)
                .validityPeriod(validityPeriod)
                .expireTime(now.plusSeconds(validityPeriod))
                .build();
    }

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 获取剩余有效时间（秒）
     */
    public long getRemainingTime() {
        if (expireTime == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expireTime)) {
            return 0;
        }
        
        return java.time.Duration.between(now, expireTime).getSeconds();
    }

    /**
     * 更新健康状态
     */
    public void updateHealth(long validityPeriod) {
        LocalDateTime now = LocalDateTime.now();
        this.healthy = true;
        this.lastUpdateTime = now;
        this.validityPeriod = validityPeriod;
        this.expireTime = now.plusSeconds(validityPeriod);
    }

    /**
     * 标记为不健康
     */
    public void markUnhealthy() {
        this.healthy = false;
        this.expireTime = LocalDateTime.now();
    }
}
