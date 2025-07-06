package com.chua.report.client.starter.service.impl;

import com.chua.report.client.starter.pojo.ClientHealthInfo;
import com.chua.report.client.starter.properties.FileManagementProperties;
import com.chua.report.client.starter.service.ClientHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 客户端健康状态服务实现
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientHealthServiceImpl implements ClientHealthService {

    private final FileManagementProperties properties;
    
    private final AtomicReference<ClientHealthInfo> healthInfo = new AtomicReference<>();
    
    private volatile LocalDateTime startTime = LocalDateTime.now();

    @Override
    public void updateHealthStatus(String deviceId, String deviceName) {
        try {
            long validityPeriod = properties.getHealthValidityPeriod();
            
            ClientHealthInfo currentHealth = healthInfo.get();
            if (currentHealth == null) {
                // 首次创建健康信息
                ClientHealthInfo newHealth = createHealthInfo(deviceId, deviceName, validityPeriod);
                healthInfo.set(newHealth);
                log.info("客户端健康状态初始化: deviceId={}, deviceName={}, validityPeriod={}s", 
                    deviceId, deviceName, validityPeriod);
            } else {
                // 更新现有健康信息
                currentHealth.setDeviceId(deviceId);
                currentHealth.setDeviceName(deviceName);
                currentHealth.updateHealth(validityPeriod);
                log.debug("客户端健康状态更新: deviceId={}, deviceName={}, expireTime={}", 
                    deviceId, deviceName, currentHealth.getExpireTime());
            }
            
        } catch (Exception e) {
            log.error("更新客户端健康状态失败: deviceId={}, deviceName={}", deviceId, deviceName, e);
        }
    }

    @Override
    public boolean isHealthy() {
        ClientHealthInfo current = healthInfo.get();
        if (current == null) {
            return false;
        }
        
        boolean healthy = current.getHealthy() != null && current.getHealthy() && !current.isExpired();
        
        if (!healthy && current.getHealthy() != null && current.getHealthy()) {
            // 健康状态过期，标记为不健康
            current.markUnhealthy();
            log.warn("客户端健康状态过期: deviceId={}, expireTime={}", 
                current.getDeviceId(), current.getExpireTime());
        }
        
        return healthy;
    }

    @Override
    public ClientHealthInfo getHealthInfo() {
        ClientHealthInfo current = healthInfo.get();
        if (current == null) {
            return ClientHealthInfo.builder()
                .healthy(false)
                .lastUpdateTime(LocalDateTime.now())
                .build();
        }
        
        // 更新运行时长
        current.setUptime(java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds());
        
        return current;
    }

    @Override
    public long getLastUpdateTime() {
        ClientHealthInfo current = healthInfo.get();
        if (current == null || current.getLastUpdateTime() == null) {
            return 0;
        }
        
        return current.getLastUpdateTime()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    }

    @Override
    public long getHealthValidityPeriod() {
        return properties.getHealthValidityPeriod();
    }

    @Override
    public boolean isHealthExpired() {
        ClientHealthInfo current = healthInfo.get();
        return current == null || current.isExpired();
    }

    @Override
    public void resetHealth() {
        ClientHealthInfo current = healthInfo.get();
        if (current != null) {
            current.markUnhealthy();
            log.info("客户端健康状态已重置: deviceId={}", current.getDeviceId());
        }
        healthInfo.set(null);
    }

    /**
     * 创建健康信息
     */
    private ClientHealthInfo createHealthInfo(String deviceId, String deviceName, long validityPeriod) {
        try {
            ClientHealthInfo health = ClientHealthInfo.create(deviceId, deviceName, validityPeriod);
            
            // 设置客户端信息
            health.setClientVersion(getClass().getPackage().getImplementationVersion());
            health.setClientIp(getLocalIpAddress());
            health.setOsInfo(System.getProperty("os.name") + " " + System.getProperty("os.version"));
            health.setJavaVersion(System.getProperty("java.version"));
            health.setStartTime(startTime);
            health.setUptime(java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds());
            
            return health;
            
        } catch (Exception e) {
            log.warn("创建健康信息时获取系统信息失败", e);
            return ClientHealthInfo.create(deviceId, deviceName, validityPeriod);
        }
    }

    /**
     * 获取本地IP地址
     */
    private String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
