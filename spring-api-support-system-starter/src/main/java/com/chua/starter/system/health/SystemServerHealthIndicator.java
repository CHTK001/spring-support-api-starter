package com.chua.starter.system.health;

import com.chua.starter.system.properties.SystemProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统服务健康检查指示器
 * <p>
 * 基于SpringBoot Actuator实现，检查系统服务的核心依赖状态：
 * - Redis连接状态（缓存存储）
 * - 数据库连接状态（系统数据）
 * - 服务配置状态
 * </p>
 * <p>
 * 访问路径: /actuator/health/system
 * </p>
 *
 * @author CH
 * @since 2024/12/11
 */
@Slf4j
@Component("systemServerHealth")
@RequiredArgsConstructor
public class SystemServerHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate stringRedisTemplate;
    private final DataSource dataSource;
    private final SystemProperties systemProperties;

    /**
     * Redis健康检查Key
     */
    private static final String HEALTH_CHECK_KEY = "system:health:check";

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;
        
        // 1. 检查Redis连接
        try {
            checkRedisConnection(details);
        } catch (Exception e) {
            isHealthy = false;
            details.put("redis", "DOWN - " + e.getMessage());
            log.error("【健康检查】Redis连接异常: {}", e.getMessage());
        }

        // 2. 检查数据库连接
        try {
            checkDatabaseConnection(details);
        } catch (Exception e) {
            isHealthy = false;
            details.put("database", "DOWN - " + e.getMessage());
            log.error("【健康检查】数据库连接异常: {}", e.getMessage());
        }

        // 3. 添加服务配置信息
        addServiceInfo(details);

        // 返回健康状态
        if (isHealthy) {
            return Health.up()
                    .withDetails(details)
                    .build();
        } else {
            return Health.down()
                    .withDetails(details)
                    .build();
        }
    }

    /**
     * 检查Redis连接
     */
    private void checkRedisConnection(Map<String, Object> details) {
        long startTime = System.currentTimeMillis();
        
        // 执行简单的读写测试
        String testValue = String.valueOf(System.currentTimeMillis());
        stringRedisTemplate.opsForValue().set(HEALTH_CHECK_KEY, testValue);
        String readValue = stringRedisTemplate.opsForValue().get(HEALTH_CHECK_KEY);
        
        long latency = System.currentTimeMillis() - startTime;
        
        if (testValue.equals(readValue)) {
            Map<String, Object> redisInfo = new HashMap<>();
            redisInfo.put("status", "UP");
            redisInfo.put("latency", latency + "ms");
            
            // 获取Redis连接信息
            RedisConnectionFactory factory = stringRedisTemplate.getConnectionFactory();
            if (factory != null) {
                redisInfo.put("connection", factory.getClass().getSimpleName());
            }
            
            details.put("redis", redisInfo);
        } else {
            throw new RuntimeException("Redis读写测试失败");
        }
    }

    /**
     * 检查数据库连接
     */
    private void checkDatabaseConnection(Map<String, Object> details) {
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            long latency = System.currentTimeMillis() - startTime;
            
            if (isValid) {
                Map<String, Object> dbInfo = new HashMap<>();
                dbInfo.put("status", "UP");
                dbInfo.put("latency", latency + "ms");
                dbInfo.put("database", connection.getMetaData().getDatabaseProductName());
                dbInfo.put("version", connection.getMetaData().getDatabaseProductVersion());
                details.put("database", dbInfo);
            } else {
                throw new RuntimeException("数据库连接无效");
            }
        } catch (Exception e) {
            throw new RuntimeException("数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * 添加服务配置信息
     */
    private void addServiceInfo(Map<String, Object> details) {
        Map<String, Object> serviceInfo = new HashMap<>();
        serviceInfo.put("enable", systemProperties.isEnable());
        serviceInfo.put("cacheEnabled", systemProperties.getCache().isEnable());
        serviceInfo.put("cacheExpire", systemProperties.getCache().getExpire() + "s");
        serviceInfo.put("logEnabled", systemProperties.getLog().isEnable());
        serviceInfo.put("logAsync", systemProperties.getLog().isAsync());
        details.put("service", serviceInfo);
    }
}


