package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.protocol.server.ServletFilter;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.spi.SpiOption;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.SystemServerSetting;
import com.chua.starter.monitor.mapper.SystemServerSettingMapper;
import com.chua.starter.monitor.service.SystemServerService;
import com.chua.starter.monitor.service.SystemServerSettingItemService;
import com.chua.starter.monitor.service.SystemServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 系统服务器配置表服务实现类
 *
 * @author CH
 * @since 2025/01/07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemServerSettingServiceImpl extends ServiceImpl<SystemServerSettingMapper, SystemServerSetting>
        implements SystemServerSettingService {

    private final SystemServerService systemServerService;
    private final SystemServerSettingItemService systemServerSettingItemService;

    @Override
    public IPage<SystemServerSetting> pageFor(Page<SystemServerSetting> page, SystemServerSetting entity) {
        return baseMapper.pageFor(page, entity);
    }

    @Override
    public ReturnResult<List<SystemServerSetting>> getByServerId(Integer serverId) {
        try {
            List<SystemServerSetting> settings = baseMapper.selectByServerId(serverId);
            log.info("获取服务器配置成功: serverId={}, 配置数量={}", serverId, settings.size());
            return ReturnResult.ok(settings);
        } catch (Exception e) {
            log.error("获取服务器配置失败: serverId={}", serverId, e);
            return ReturnResult.error("获取服务器配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getAvailableServletFilterObjects() {
        try {
            List<Map<String, Object>> filterObjects = new ArrayList<>();

            // 通过SPI机制获取可用的ServletFilter
            ServiceProvider<ServletFilter> serviceProvider = ServiceProvider.of(ServletFilter.class);
            List<SpiOption> options = serviceProvider.options();

            for (SpiOption option : options) {
                try {
                    Map<String, Object> filterInfo = new HashMap<>();
                    filterInfo.put("type", option.getName());
                    // 优先使用描述，不存在则使用名称
                    String displayName = StringUtils.hasText(option.getDescribe()) ? option.getDescribe()
                            : option.getName();
                    filterInfo.put("name", displayName);
                    filterInfo.put("description", option.getDescribe());
                    filterInfo.put("describeType", option.getDescribeType());
                    filterInfo.put("describeDetail", option.getDescribeDetail());

                    // 获取实例以获取更多信息
                    try {
                        Object filterInstance = serviceProvider.getNewExtension(option.getName());
                        filterInfo.put("className", filterInstance.getClass().getName());
                        filterInfo.put("version", getFilterVersion(filterInstance));
                        filterInfo.put("configItems", getFilterConfigItems(filterInstance));
                    } catch (Exception e) {
                        log.warn("获取ServletFilter实例信息失败: filterType={}", option.getName(), e);
                        filterInfo.put("className", "未知");
                        filterInfo.put("version", "未知");
                        filterInfo.put("configItems", new ArrayList<>());
                    }

                    filterObjects.add(filterInfo);
                } catch (Exception e) {
                    log.warn("获取ServletFilter信息失败: filterType={}", option.getName(), e);
                }
            }

            // 按显示名称排序
            filterObjects.sort((a, b) -> String.valueOf(a.get("name")).compareTo(String.valueOf(b.get("name"))));

            log.info("获取可用ServletFilter成功，共{}个", filterObjects.size());
            return ReturnResult.ok(filterObjects);
        } catch (Exception e) {
            log.error("获取可用ServletFilter失败", e);
            return ReturnResult.error("获取ServletFilter失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<SystemServerSetting> installServletFilter(Integer serverId, String filterType,
                                                                  String filterName, String description) {
        try {
            // 检查是否已安装相同类型的Filter
            SystemServerSetting existingSetting = baseMapper.selectByServerIdAndType(serverId, filterType);
            if (existingSetting != null) {
                return ReturnResult.error("该类型的ServletFilter已安装");
            }

            // 获取最大排序值
            Integer maxOrder = baseMapper.getMaxOrderByServerId(serverId);

            // 创建配置记录
            SystemServerSetting setting = new SystemServerSetting();
            setting.setSystemServerSettingServerId(serverId);
            setting.setSystemServerSettingName(filterName);
            setting.setSystemServerSettingType(filterType);
            setting.setSystemServerSettingDescription(description);
            setting.setSystemServerSettingEnabled(true);
            setting.setSystemServerSettingOrder(maxOrder + 1);

            // 通过SPI获取Filter信息
            try {
                ServiceProvider serviceProvider = ServiceProvider.of("ServletFilter");
                Object filterInstance = serviceProvider.getNewExtension(filterType);
                setting.setSystemServerSettingClassName(filterInstance.getClass().getName());
                setting.setSystemServerSettingVersion(getFilterVersion(filterInstance));
            } catch (Exception e) {
                log.warn("获取Filter信息失败，使用默认值: filterType={}", filterType, e);
            }

            save(setting);

            // 创建默认配置项
            createDefaultConfigItems(setting.getSystemServerSettingId(), filterType);

            // 应用配置到运行中的服务器
            applyConfigToRunningServer(serverId);

            log.info("安装ServletFilter成功: serverId={}, filterType={}, filterName={}", serverId, filterType, filterName);
            return ReturnResult.ok(setting);
        } catch (Exception e) {
            log.error("安装ServletFilter失败: serverId={}, filterType={}", serverId, filterType, e);
            return ReturnResult.error("安装ServletFilter失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> uninstallServletFilter(Integer settingId) {
        try {
            SystemServerSetting setting = getById(settingId);
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }

            Integer serverId = setting.getSystemServerSettingServerId();

            // 删除配置项
            systemServerSettingItemService.deleteBySettingId(settingId);

            // 删除配置
            removeById(settingId);

            // 应用配置到运行中的服务器
            applyConfigToRunningServer(serverId);

            log.info("卸载ServletFilter成功: settingId={}, filterType={}", settingId, setting.getSystemServerSettingType());
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("卸载ServletFilter失败: settingId={}", settingId, e);
            return ReturnResult.error("卸载ServletFilter失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> enableServletFilter(Integer settingId) {
        try {
            SystemServerSetting setting = getById(settingId);
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }

            baseMapper.updateEnabled(settingId, true);

            // 应用配置到运行中的服务器
            applyConfigToRunningServer(setting.getSystemServerSettingServerId());

            log.info("启用ServletFilter成功: settingId={}", settingId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("启用ServletFilter失败: settingId={}", settingId, e);
            return ReturnResult.error("启用ServletFilter失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> disableServletFilter(Integer settingId) {
        try {
            SystemServerSetting setting = getById(settingId);
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }

            baseMapper.updateEnabled(settingId, false);

            // 应用配置到运行中的服务器
            applyConfigToRunningServer(setting.getSystemServerSettingServerId());

            log.info("禁用ServletFilter成功: settingId={}", settingId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("禁用ServletFilter失败: settingId={}", settingId, e);
            return ReturnResult.error("禁用ServletFilter失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> updateServletFilterOrder(List<Map<String, Integer>> orderUpdates) {
        try {
            for (Map<String, Integer> update : orderUpdates) {
                Integer settingId = update.get("settingId");
                Integer order = update.get("order");
                baseMapper.updateOrder(settingId, order);
            }

            // 获取第一个配置的服务器ID来应用配置
            if (!orderUpdates.isEmpty()) {
                Integer firstSettingId = orderUpdates.get(0).get("settingId");
                SystemServerSetting setting = getById(firstSettingId);
                if (setting != null) {
                    applyConfigToRunningServer(setting.getSystemServerSettingServerId());
                }
            }

            log.info("更新ServletFilter排序成功，更新数量: {}", orderUpdates.size());
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("更新ServletFilter排序失败", e);
            return ReturnResult.error("更新排序失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getServletFilterConfigItems(String filterType) {
        try {
            ServiceProvider serviceProvider = ServiceProvider.of("ServletFilter");
            Object filterInstance = serviceProvider.getNewExtension(filterType);
            List<Map<String, Object>> configItems = getFilterConfigItems(filterInstance);

            log.info("获取ServletFilter配置项成功: filterType={}, 配置项数量={}", filterType, configItems.size());
            return ReturnResult.ok(configItems);
        } catch (Exception e) {
            log.error("获取ServletFilter配置项失败: filterType={}", filterType, e);
            return ReturnResult.error("获取配置项失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> updateServletFilterConfig(Integer settingId, Map<String, Object> config) {
        try {
            SystemServerSetting setting = getById(settingId);
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }

            // 更新配置项
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                Map<String, Object> update = new HashMap<>();
                update.put("itemId", entry.getKey());
                update.put("value", entry.getValue());
                systemServerSettingItemService.batchUpdateItemValues(Collections.singletonList(update));
            }

            // 应用配置到运行中的服务器
            applyConfigToRunningServer(setting.getSystemServerSettingServerId());

            log.info("更新ServletFilter配置成功: settingId={}", settingId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("更新ServletFilter配置失败: settingId={}", settingId, e);
            return ReturnResult.error("更新配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> applyConfigToRunningServer(Integer serverId) {
        try {
            return systemServerService.applyConfigChanges(serverId);
        } catch (Exception e) {
            log.error("应用配置到运行中的服务器失败: serverId={}", serverId, e);
            return ReturnResult.error("应用配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> cloneServerSettings(Integer sourceServerId, Integer targetServerId) {
        try {
            List<SystemServerSetting> sourceSettings = baseMapper.selectByServerId(sourceServerId);

            for (SystemServerSetting sourceSetting : sourceSettings) {
                // 创建新配置
                SystemServerSetting newSetting = new SystemServerSetting();
                BeanUtils.copyProperties(sourceSetting, newSetting);
                newSetting.setSystemServerSettingId(null);
                newSetting.setSystemServerSettingServerId(targetServerId);
                save(newSetting);

                // 克隆配置项
                systemServerSettingItemService.batchSaveItems(newSetting.getSystemServerSettingId(),
                        systemServerSettingItemService.getBySettingId(sourceSetting.getSystemServerSettingId())
                                .getData());
            }

            log.info("克隆服务器配置成功: sourceServerId={}, targetServerId={}, 配置数量={}",
                    sourceServerId, targetServerId, sourceSettings.size());
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("克隆服务器配置失败: sourceServerId={}, targetServerId={}", sourceServerId, targetServerId, e);
            return ReturnResult.error("克隆配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemServerSetting>> getByServerIdAndEnabled(Integer serverId, Boolean enabled) {
        try {
            List<SystemServerSetting> settings = baseMapper.selectByServerIdAndEnabled(serverId, enabled);
            return ReturnResult.ok(settings);
        } catch (Exception e) {
            log.error("根据服务器ID和启用状态查询配置失败: serverId={}, enabled={}", serverId, enabled, e);
            return ReturnResult.error("查询配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> batchUpdateEnabled(List<Integer> settingIds, Boolean enabled) {
        try {
            for (Integer settingId : settingIds) {
                baseMapper.updateEnabled(settingId, enabled);
            }

            log.info("批量更新ServletFilter启用状态成功: settingIds={}, enabled={}", settingIds, enabled);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("批量更新ServletFilter启用状态失败: settingIds={}, enabled={}", settingIds, enabled, e);
            return ReturnResult.error("批量更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取Filter显示名称
     */
    private String getFilterDisplayName(Object filterInstance) {
        try {
            return (String) filterInstance.getClass().getMethod("getDisplayName").invoke(filterInstance);
        } catch (Exception e) {
            return filterInstance.getClass().getSimpleName();
        }
    }

    /**
     * 获取Filter描述
     */
    private String getFilterDescription(Object filterInstance) {
        try {
            return (String) filterInstance.getClass().getMethod("getDescription").invoke(filterInstance);
        } catch (Exception e) {
            return "无描述";
        }
    }

    /**
     * 获取Filter版本
     */
    private String getFilterVersion(Object filterInstance) {
        try {
            return (String) filterInstance.getClass().getMethod("getVersion").invoke(filterInstance);
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    /**
     * 获取Filter配置项
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getFilterConfigItems(Object filterInstance) {
        try {
            return (List<Map<String, Object>>) filterInstance.getClass().getMethod("getConfigItems")
                    .invoke(filterInstance);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 创建默认配置项
     */
    private void createDefaultConfigItems(Integer settingId, String filterType) {
        try {
            ReturnResult<List<Map<String, Object>>> configItemsResult = getServletFilterConfigItems(filterType);
            if (configItemsResult.isOk() && configItemsResult.getData() != null) {
                // 这里需要调用SystemServerSettingItemService来创建配置项
                // 具体实现在SystemServerSettingItemServiceImpl中
            }
        } catch (Exception e) {
            log.warn("创建默认配置项失败: settingId={}, filterType={}", settingId, filterType, e);
        }
    }
}
