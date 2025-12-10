package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenGlobalSetting;
import com.chua.starter.monitor.mapper.MonitorSysGenGlobalSettingMapper;
import com.chua.starter.monitor.service.server.MonitorSysGenGlobalSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 全局配置设置Service实现类
 * <p>
 * 使用本地缓存优化性能，避免频繁查询数据库
 * </p>
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenGlobalSettingServiceImpl 
        extends ServiceImpl<MonitorSysGenGlobalSettingMapper, MonitorSysGenGlobalSetting>
        implements MonitorSysGenGlobalSettingService {

    /**
     * 全局配置固定ID（系统只允许存在一条记录）
     */
    private static final Integer GLOBAL_SETTING_ID = 1;
    
    /**
     * 全局配置缓存（使用AtomicReference保证线程安全）
     */
    private final AtomicReference<MonitorSysGenGlobalSetting> cachedSetting = new AtomicReference<>();
    
    /**
     * 缓存最后更新时间
     */
    private volatile long lastCacheUpdateTime = 0L;
    
    /**
     * 缓存过期时间（毫秒）- 30秒
     */
    private static final long CACHE_EXPIRE_TIME = 30_000L;

    @Override
    public ReturnResult<MonitorSysGenGlobalSetting> getGlobalSetting() {
        try {
            // 检查缓存是否有效
            MonitorSysGenGlobalSetting cached = cachedSetting.get();
            long now = System.currentTimeMillis();
            
            if (cached != null && (now - lastCacheUpdateTime) < CACHE_EXPIRE_TIME) {
                log.debug("从缓存获取全局配置");
                return ReturnResult.ok(cached);
            }
            
            // 缓存失效，从数据库查询（只查询ID=1的记录）
            log.debug("缓存失效，从数据库查询全局配置");
            MonitorSysGenGlobalSetting setting = getById(GLOBAL_SETTING_ID);
            
            if (setting == null) {
                // 如果不存在，创建默认配置（ID固定为1）
                MonitorSysGenGlobalSetting defaultSetting = MonitorSysGenGlobalSetting.getDefaultSetting();
                defaultSetting.setMonitorSysGenGlobalSettingId(GLOBAL_SETTING_ID);
                boolean saved = save(defaultSetting);
                if (saved) {
                    log.info("创建默认全局配置成功（ID=1）");
                    updateCache(defaultSetting);
                    return ReturnResult.ok(defaultSetting);
                } else {
                    log.error("创建默认全局配置失败");
                    return ReturnResult.error("创建默认全局配置失败");
                }
            }
            
            updateCache(setting);
            return ReturnResult.ok(setting);
        } catch (Exception e) {
            log.error("获取全局配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取全局配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenGlobalSetting> saveOrUpdateGlobalSetting(MonitorSysGenGlobalSetting setting) {
        try {
            if (setting == null) {
                return ReturnResult.error("配置信息不能为空");
            }

            // 强制设置ID为1，确保只操作全局配置记录
            setting.setMonitorSysGenGlobalSettingId(GLOBAL_SETTING_ID);
            
            // 检查记录是否存在
            MonitorSysGenGlobalSetting existing = getById(GLOBAL_SETTING_ID);
            
            boolean success;
            if (existing == null) {
                // 不存在则新增
                success = save(setting);
                if (success) {
                    log.info("创建全局配置成功（ID=1）");
                } else {
                    log.error("创建全局配置失败");
                    return ReturnResult.error("创建全局配置失败");
                }
            } else {
                // 存在则更新
                success = updateById(setting);
                if (success) {
                    log.info("更新全局配置成功（ID=1）");
                } else {
                    log.error("更新全局配置失败");
                    return ReturnResult.error("更新全局配置失败");
                }
            }
            
            // 更新缓存
            updateCache(setting);
            return ReturnResult.ok(setting);
        } catch (Exception e) {
            log.error("保存或更新全局配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("保存或更新全局配置失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isGlobalDispatcherEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingDispatcherEnabled);
    }

    @Override
    public boolean isStatusSummaryEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingStatusSummaryEnabled);
    }

    @Override
    public boolean isPerformanceTrendsEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingPerformanceTrendsEnabled);
    }

    @Override
    public boolean isConnectionStatsEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingConnectionStatsEnabled);
    }

    @Override
    public boolean isHealthStatusEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingHealthStatusEnabled);
    }

    @Override
    public boolean isGlobalAlertEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingGlobalAlertEnabled);
    }

    @Override
    public boolean isCleanupEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingCleanupEnabled);
    }

    @Override
    public boolean isConfigRefreshEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingConfigRefreshEnabled);
    }

    @Override
    public boolean isFileUploadCheckEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingFileUploadCheckEnabled);
    }

    @Override
    public boolean isDataCleanupEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingDataCleanupEnabled);
    }

    @Override
    public boolean isBackupEnabled() {
        return checkEnabled(MonitorSysGenGlobalSetting::getMonitorSysGenGlobalSettingBackupEnabled);
    }

    /**
     * 检查指定配置项是否启用
     * <p>
     * 优先从缓存读取，避免频繁查询数据库
     * </p>
     *
     * @param getter 配置项的getter方法引用
     * @return true:启用 false:禁用
     */
    private boolean checkEnabled(java.util.function.Function<MonitorSysGenGlobalSetting, Integer> getter) {
        try {
            ReturnResult<MonitorSysGenGlobalSetting> result = getGlobalSetting();
            if (result.isOk() && result.getData() != null) {
                Integer value = getter.apply(result.getData());
                return value != null && value == 1;
            }
            // 默认启用
            return true;
        } catch (Exception e) {
            log.error("检查配置项启用状态失败: {}", e.getMessage(), e);
            // 发生异常时默认启用
            return true;
        }
    }

    /**
     * 更新缓存
     *
     * @param setting 全局配置
     */
    private void updateCache(MonitorSysGenGlobalSetting setting) {
        cachedSetting.set(setting);
        lastCacheUpdateTime = System.currentTimeMillis();
        log.debug("全局配置缓存已更新");
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        cachedSetting.set(null);
        lastCacheUpdateTime = 0L;
        log.info("全局配置缓存已清除");
    }
}
