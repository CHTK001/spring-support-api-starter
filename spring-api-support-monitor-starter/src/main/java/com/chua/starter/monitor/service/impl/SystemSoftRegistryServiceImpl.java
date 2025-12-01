package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftRegistry;
import com.chua.starter.monitor.mapper.SystemSoftRegistryMapper;
import com.chua.starter.monitor.service.SystemSoftRegistryService;
import com.chua.starter.monitor.service.SystemSoftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 系统软件镜像仓库服务实现类
 *
 * @author CH
 * @since 2025/01/15
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SystemSoftRegistryServiceImpl extends ServiceImpl<SystemSoftRegistryMapper, SystemSoftRegistry> implements SystemSoftRegistryService {

    private final SystemSoftRegistryMapper systemSoftRegistryMapper;
    private final SystemSoftService systemSoftService;

    /**
     * 测试仓库连接
     *
     * @param registryId 仓库ID
     * @return 连接测试结果
     */
    @Override
    public ReturnResult<Boolean> testConnection(Integer registryId) {
        log.debug("开始测试仓库连接, registryId: {}", registryId);

        try {
            SystemSoftRegistry registry = baseMapper.selectById(registryId);
            if (registry == null) {
                log.warn("仓库不存在, registryId: {}", registryId);
                return ReturnResult.error("仓库不存在");
            }

            boolean isConnected = performConnectionTest(registry);

            // 更新连接状态
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (isConnected) {
                systemSoftRegistryMapper.updateConnectStatus(registryId, 1, null, now);
                log.info("仓库连接测试成功, registryId: {}", registryId);
                return ReturnResult.ok(true, "连接测试成功");
            } else {
                systemSoftRegistryMapper.updateConnectStatus(registryId, 2, "连接超时", now);
                log.warn("仓库连接测试失败, registryId: {}", registryId);
                return ReturnResult.ok(false, "连接测试失败");
            }

        } catch (Exception e) {
            log.error("测试仓库连接异常, registryId: {}", registryId, e);
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            systemSoftRegistryMapper.updateConnectStatus(registryId, 2, e.getMessage(), now);
            return ReturnResult.error("连接测试异常: " + e.getMessage());
        }
    }

    /**
     * 设置默认仓库
     *
     * @param registryId 仓库ID
     * @return 设置结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> setDefault(Integer registryId) {
        log.debug("开始设置默认仓库, registryId: {}", registryId);

        try {
            SystemSoftRegistry registry = baseMapper.selectById(registryId);
            if (registry == null) {
                log.warn("仓库不存在, registryId: {}", registryId);
                return ReturnResult.error("仓库不存在");
            }

            if (registry.getSystemSoftRegistryStatus() != 1) {
                log.warn("仓库未启用，无法设置为默认仓库, registryId: {}", registryId);
                return ReturnResult.error("仓库未启用，无法设置为默认仓库");
            }

            // 设置新的默认仓库
            systemSoftRegistryMapper.updateDefaultFlag(registryId);

            log.info("设置默认仓库成功, registryId: {}", registryId);
            return ReturnResult.ok(true, "设置默认仓库成功");

        } catch (Exception e) {
            log.error("设置默认仓库异常, registryId: {}", registryId, e);
            return ReturnResult.error("设置默认仓库异常: " + e.getMessage());
        }
    }

    /**
     * 批量更新仓库状态
     *
     * @param registryIds 仓库ID列表
     * @param status      状态
     * @return 更新结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchUpdateStatus(List<Integer> registryIds, Integer status) {
        log.debug("开始批量更新仓库状态, registryIds: {}, status: {}", registryIds, status);

        try {
            if (registryIds == null || registryIds.isEmpty()) {
                log.warn("仓库ID列表为空");
                return ReturnResult.error("仓库ID列表不能为空");
            }

            Integer updateCount = systemSoftRegistryMapper.batchUpdateStatus(registryIds, status);

            log.info("批量更新仓库状态成功, updateCount: {}", updateCount);
            return ReturnResult.ok(true, "批量更新成功，共更新" + updateCount + "条记录");

        } catch (Exception e) {
            log.error("批量更新仓库状态异常, registryIds: {}, status: {}", registryIds, status, e);
            return ReturnResult.error("批量更新异常: " + e.getMessage());
        }
    }

    /**
     * 获取默认仓库
     *
     * @return 默认仓库信息
     */
    @Override
    public ReturnResult<SystemSoftRegistry> getDefaultRegistry() {
        log.debug("开始获取默认仓库");

        try {
            SystemSoftRegistry defaultRegistry = systemSoftRegistryMapper.selectDefaultRegistry();

            if (defaultRegistry == null) {
                log.info("未找到默认仓库");
                return ReturnResult.ok(null, "未设置默认仓库");
            }

            log.debug("获取默认仓库成功, registryId: {}", defaultRegistry.getSystemSoftRegistryId());
            return ReturnResult.ok(defaultRegistry);

        } catch (Exception e) {
            log.error("获取默认仓库异常", e);
            return ReturnResult.error("获取默认仓库异常: " + e.getMessage());
        }
    }

    /**
     * 根据服务器获取默认仓库
     */
    @Override
    public ReturnResult<SystemSoftRegistry> getDefaultRegistryByServerId(Integer serverId) {
        try {
            SystemSoftRegistry defaultRegistry = systemSoftRegistryMapper.selectDefaultRegistryByServerId(serverId);
            return ReturnResult.ok(defaultRegistry);
        } catch (Exception e) {
            log.error("根据服务器获取默认仓库异常, serverId: {}", serverId, e);
            return ReturnResult.error("获取默认仓库异常: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的仓库
     *
     * @return 启用的仓库列表
     */
    @Override
    public ReturnResult<List<SystemSoftRegistry>> getEnabledRegistries() {
        log.debug("开始获取所有启用的仓库");

        try {
            List<SystemSoftRegistry> enabledRegistries = systemSoftRegistryMapper.selectEnabledRegistries();

            log.debug("获取启用仓库成功, count: {}", enabledRegistries.size());
            return ReturnResult.ok(enabledRegistries);

        } catch (Exception e) {
            log.error("获取启用仓库异常", e);
            return ReturnResult.error("获取启用仓库异常: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取仓库列表
     *
     * @param registryType 仓库类型
     * @return 仓库列表
     */
    @Override
    public ReturnResult<List<SystemSoftRegistry>> getRegistriesByType(String registryType) {
        log.debug("开始根据类型获取仓库列表, registryType: {}", registryType);

        try {
            List<SystemSoftRegistry> registries = systemSoftRegistryMapper.selectByRegistryType(registryType);

            log.debug("根据类型获取仓库成功, registryType: {}, count: {}", registryType, registries.size());
            return ReturnResult.ok(registries);

        } catch (Exception e) {
            log.error("根据类型获取仓库异常, registryType: {}", registryType, e);
            return ReturnResult.error("获取仓库列表异常: " + e.getMessage());
        }
    }

    /**
     * 验证仓库配置
     *
     * @param registry 仓库信息
     * @return 验证结果
     */
    @Override
    public ReturnResult<String> validateRegistryConfig(SystemSoftRegistry registry) {
        log.debug("开始验证仓库配置");

        try {
            // 基本字段验证
            if (registry.getSystemSoftRegistryName() == null || registry.getSystemSoftRegistryName().trim().isEmpty()) {
                return ReturnResult.error("仓库名称不能为空");
            }

            if (registry.getSystemSoftRegistryUrl() == null || registry.getSystemSoftRegistryUrl().trim().isEmpty()) {
                return ReturnResult.error("仓库地址不能为空");
            }

            if (registry.getSystemSoftRegistryType() == null || registry.getSystemSoftRegistryType().trim().isEmpty()) {
                return ReturnResult.error("仓库类型不能为空");
            }

            // 检查名称重复
            Long nameCount = systemSoftRegistryMapper.checkRegistryNameExists(
                    registry.getSystemSoftRegistryName(), registry.getSystemSoftRegistryId());
            if (nameCount > 0) {
                return ReturnResult.error("仓库名称已存在");
            }

            // 检查地址重复
            Long urlCount = systemSoftRegistryMapper.checkRegistryUrlExists(
                    registry.getSystemSoftRegistryUrl(), registry.getSystemSoftRegistryId());
            if (urlCount > 0) {
                return ReturnResult.error("仓库地址已存在");
            }

            log.debug("仓库配置验证通过");
            return ReturnResult.ok("验证通过");

        } catch (Exception e) {
            log.error("验证仓库配置异常", e);
            return ReturnResult.error("验证配置异常: " + e.getMessage());
        }
    }

    /**
     * 同步仓库信息（同步软件到软件库）
     *
     * @param registryId 仓库ID
     * @return 同步结果
     */
    @Override
    public ReturnResult<Boolean> syncRegistry(Integer registryId) {
        log.debug("开始同步仓库信息, registryId: {}", registryId);

        try {
            SystemSoftRegistry registry = baseMapper.selectById(registryId);
            if (registry == null) {
                log.warn("仓库不存在, registryId: {}", registryId);
                return ReturnResult.error("仓库不存在");
            }

            if (registry.getSystemSoftRegistryStatus() == null || registry.getSystemSoftRegistryStatus() != 1) {
                log.warn("仓库未启用, registryId: {}", registryId);
                return ReturnResult.error("仓库未启用");
            }

            if (registry.getSystemSoftRegistrySupportSync() == null || registry.getSystemSoftRegistrySupportSync() != 1) {
                log.warn("仓库不支持同步, registryId: {}", registryId);
                return ReturnResult.error("该仓库不支持同步");
            }

            // 调用软件服务从仓库同步到软件库
            // 当前是获取服务器上存在的镜像,此接口暂时不适用
            var syncResult = systemSoftService.syncFromRegistry(registryId);
            if (!syncResult.isOk()) {
                log.warn("同步仓库失败, registryId: {}, msg: {}", registryId, syncResult.getMsg());
                return ReturnResult.error(syncResult.getMsg());
            }

            Integer count = syncResult.getData() == null ? 0 : syncResult.getData();
            log.info("仓库信息同步成功, registryId: {}, count: {}", registryId, count);
            return ReturnResult.ok(true, "同步成功，共同步" + count + "个软件");

        } catch (Exception e) {
            log.error("同步仓库信息异常, registryId: {}", registryId, e);
            return ReturnResult.error("同步异常: " + e.getMessage());
        }
    }

    /**
     * 执行连接测试
     *
     * @param registry 仓库信息
     * @return 连接是否成功
     */
    private boolean performConnectionTest(SystemSoftRegistry registry) {
        try {
            String registryUrl = registry.getSystemSoftRegistryUrl();
            Integer timeout = registry.getSystemSoftRegistryTimeout();
            int timeoutMs = (timeout == null || timeout <= 0) ? 5000 : timeout * 1000;

            log.debug("执行URL连通性测试, url: {}, timeoutMs: {}", registryUrl, timeoutMs);

            return testUrlReachable(registryUrl, timeoutMs);
        } catch (Exception e) {
            log.error("执行连接测试异常", e);
            return false;
        }
    }

    /**
     * 使用HTTP(S) URL测试是否可达：优先HEAD，失败回退GET，认为 200-399 为可达。
     *
     * @param url      测试地址
     * @param timeoutMs 超时时间(毫秒)
     * @return 是否可达
     */
    private boolean testUrlReachable(String url, int timeoutMs) {
        HttpURLConnection connection = null;
        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);
            connection.setInstanceFollowRedirects(true);
            try {
                connection.setRequestMethod("HEAD");
            } catch (Exception ignore) {
                // 某些实现不允许修改，忽略
            }
            int code;
            try {
                code = connection.getResponseCode();
            } catch (Exception ex) {
                // 回退到GET
                if (connection != null) {
                    try {
                        connection.disconnect();
                    } catch (Exception ignore) {
                    }
                }
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(timeoutMs);
                connection.setReadTimeout(timeoutMs);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("GET");
                code = connection.getResponseCode();
            }
            log.debug("URL响应码: {}, url: {}", code, url);
            return code >= 200 && code < 400;
        } catch (Exception e) {
            log.warn("URL连通性测试失败, url: {}, err: {}", url, e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
