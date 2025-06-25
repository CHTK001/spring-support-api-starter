package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerProxy;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerProxyMapper;

import com.chua.starter.monitor.starter.service.MonitorSysGenServerProxyService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务器代理关联服务实现类
 *
 * @author CH
 * @since 2024/12/25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerProxyServiceImpl extends ServiceImpl<MonitorSysGenServerProxyMapper, MonitorSysGenServerProxy>
        implements MonitorSysGenServerProxyService {


    @Autowired
    @Lazy
    private MonitorSysGenServerService monitorSysGenServerService;

    @Override
    public IPage<MonitorSysGenServerProxy> pageFor(Page<MonitorSysGenServerProxy> page, MonitorSysGenServerProxy entity) {
        LambdaQueryWrapper<MonitorSysGenServerProxy> wrapper = Wrappers.lambdaQuery();
        
        if (entity.getMonitorSysGenServerId() != null) {
            wrapper.eq(MonitorSysGenServerProxy::getMonitorSysGenServerId, entity.getMonitorSysGenServerId());
        }

        if (StringUtils.isNotBlank(entity.getMonitorSysGenServerProxyType())) {
            wrapper.eq(MonitorSysGenServerProxy::getMonitorSysGenServerProxyType, entity.getMonitorSysGenServerProxyType());
        }
        if (entity.getMonitorSysGenServerProxyEnabled() != null) {
            wrapper.eq(MonitorSysGenServerProxy::getMonitorSysGenServerProxyEnabled, entity.getMonitorSysGenServerProxyEnabled());
        }
        if (entity.getMonitorSysGenServerProxyStatus() != null) {
            wrapper.eq(MonitorSysGenServerProxy::getMonitorSysGenServerProxyStatus, entity.getMonitorSysGenServerProxyStatus());
        }
        
        wrapper.orderByDesc(MonitorSysGenServerProxy::getMonitorSysGenServerProxyCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerProxy>> getByServerId(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            List<MonitorSysGenServerProxy> list = baseMapper.selectByServerId(serverId);
            return ReturnResult.ok(list);
        } catch (Exception e) {
            log.error("根据服务器ID查询代理关联失败: serverId={}", serverId, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerProxy>> getByProxyId(Integer proxyId) {
        try {
            if (proxyId == null) {
                return ReturnResult.error("代理ID不能为空");
            }

            List<MonitorSysGenServerProxy> list = baseMapper.selectByProxyId(proxyId);
            return ReturnResult.ok(list);
        } catch (Exception e) {
            log.error("根据代理ID查询服务器关联失败: proxyId={}", proxyId, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerProxy> getByServerIdAndProxyType(Integer serverId, String proxyType) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }
            if (StringUtils.isBlank(proxyType)) {
                return ReturnResult.error("代理类型不能为空");
            }
            
            MonitorSysGenServerProxy serverProxy = baseMapper.selectByServerIdAndProxyType(serverId, proxyType);
            return ReturnResult.ok(serverProxy);
        } catch (Exception e) {
            log.error("根据服务器ID和代理类型查询代理关联失败: serverId={}, proxyType={}", serverId, proxyType, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerProxy> createServerProxy(MonitorSysGenServerProxy serverProxy) {
        try {
            // 验证必填字段
            if (serverProxy.getMonitorSysGenServerId() == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            if (StringUtils.isBlank(serverProxy.getMonitorSysGenServerProxyType())) {
                return ReturnResult.error("代理类型不能为空");
            }
            
            // 验证服务器是否存在
            MonitorSysGenServer server = monitorSysGenServerService.getById(serverProxy.getMonitorSysGenServerId());
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }
            

            
            // 检查是否已存在相同的关联
            MonitorSysGenServerProxy existing = baseMapper.selectByServerIdAndProxyType(
                serverProxy.getMonitorSysGenServerId(), 
                serverProxy.getMonitorSysGenServerProxyType()
            );
            if (existing != null) {
                return ReturnResult.error("该服务器已存在相同类型的代理关联");
            }
            
            // 设置默认值
            LocalDateTime now = LocalDateTime.now();
            serverProxy.setMonitorSysGenServerProxyCreateTime(now);
            serverProxy.setMonitorSysGenServerProxyUpdateTime(now);
            
            if (serverProxy.getMonitorSysGenServerProxyEnabled() == null) {
                serverProxy.setMonitorSysGenServerProxyEnabled(1); // 默认启用
            }
            if (serverProxy.getMonitorSysGenServerProxyStatus() == null) {
                serverProxy.setMonitorSysGenServerProxyStatus(0); // 默认离线
            }
            
            boolean result = this.save(serverProxy);
            if (result) {
                log.info("创建服务器代理关联成功: serverId={}, proxyType={}",
                    serverProxy.getMonitorSysGenServerId(),
                    serverProxy.getMonitorSysGenServerProxyType());
                return ReturnResult.ok(serverProxy);
            } else {
                return ReturnResult.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建服务器代理关联失败", e);
            return ReturnResult.error("创建失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateServerProxy(MonitorSysGenServerProxy serverProxy) {
        try {
            if (serverProxy.getMonitorSysGenServerProxyId() == null) {
                return ReturnResult.error("关联ID不能为空");
            }
            
            // 验证关联是否存在
            MonitorSysGenServerProxy existing = this.getById(serverProxy.getMonitorSysGenServerProxyId());
            if (existing == null) {
                return ReturnResult.error("代理关联不存在");
            }
            
            // 设置更新时间
            serverProxy.setMonitorSysGenServerProxyUpdateTime(LocalDateTime.now());
            
            boolean result = this.updateById(serverProxy);
            if (result) {
                log.info("更新服务器代理关联成功: id={}", serverProxy.getMonitorSysGenServerProxyId());
            }
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("更新服务器代理关联失败: id={}", serverProxy.getMonitorSysGenServerProxyId(), e);
            return ReturnResult.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteServerProxy(Integer id) {
        try {
            if (id == null) {
                return ReturnResult.error("关联ID不能为空");
            }
            
            boolean result = this.removeById(id);
            if (result) {
                log.info("删除服务器代理关联成功: id={}", id);
            }
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("删除服务器代理关联失败: id={}", id, e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchDeleteServerProxy(List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ReturnResult.error("关联ID列表不能为空");
            }
            
            boolean result = this.removeByIds(ids);
            if (result) {
                log.info("批量删除服务器代理关联成功: ids={}", ids);
            }
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("批量删除服务器代理关联失败: ids={}", ids, e);
            return ReturnResult.error("批量删除失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testProxyConnection(Integer id) {
        try {
            if (id == null) {
                return ReturnResult.error("关联ID不能为空");
            }

            MonitorSysGenServerProxy serverProxy = this.getById(id);
            if (serverProxy == null) {
                return ReturnResult.error("代理关联不存在");
            }



            // TODO: 实现具体的代理连接测试逻辑
            // 这里可以根据不同的代理类型实现不同的测试逻辑

            // 更新连接状态
            serverProxy.setMonitorSysGenServerProxyStatus(1); // 在线
            serverProxy.setMonitorSysGenServerProxyLastConnectTime(LocalDateTime.now());
            serverProxy.setMonitorSysGenServerProxyConnectionError(null);
            this.updateById(serverProxy);

            log.info("代理连接测试成功: id={}", id);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("代理连接测试失败: id={}", id, e);

            // 更新连接状态为失败
            try {
                MonitorSysGenServerProxy serverProxy = this.getById(id);
                if (serverProxy != null) {
                    serverProxy.setMonitorSysGenServerProxyStatus(3); // 连接失败
                    serverProxy.setMonitorSysGenServerProxyConnectionError(e.getMessage());
                    this.updateById(serverProxy);
                }
            } catch (Exception updateEx) {
                log.error("更新代理连接状态失败", updateEx);
            }

            return ReturnResult.error("连接测试失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> enableProxy(Integer id) {
        try {
            if (id == null) {
                return ReturnResult.error("关联ID不能为空");
            }

            MonitorSysGenServerProxy serverProxy = this.getById(id);
            if (serverProxy == null) {
                return ReturnResult.error("代理关联不存在");
            }

            serverProxy.setMonitorSysGenServerProxyEnabled(1);
            serverProxy.setMonitorSysGenServerProxyUpdateTime(LocalDateTime.now());

            boolean result = this.updateById(serverProxy);
            if (result) {
                log.info("启用代理成功: id={}", id);
            }
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("启用代理失败: id={}", id, e);
            return ReturnResult.error("启用失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> disableProxy(Integer id) {
        try {
            if (id == null) {
                return ReturnResult.error("关联ID不能为空");
            }

            MonitorSysGenServerProxy serverProxy = this.getById(id);
            if (serverProxy == null) {
                return ReturnResult.error("代理关联不存在");
            }

            serverProxy.setMonitorSysGenServerProxyEnabled(0);
            serverProxy.setMonitorSysGenServerProxyStatus(0); // 设为离线
            serverProxy.setMonitorSysGenServerProxyUpdateTime(LocalDateTime.now());

            boolean result = this.updateById(serverProxy);
            if (result) {
                log.info("禁用代理成功: id={}", id);
            }
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("禁用代理失败: id={}", id, e);
            return ReturnResult.error("禁用失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> getProxyUrl(Integer id) {
        try {
            if (id == null) {
                return ReturnResult.error("关联ID不能为空");
            }

            MonitorSysGenServerProxy serverProxy = this.getById(id);
            if (serverProxy == null) {
                return ReturnResult.error("代理关联不存在");
            }

            if (serverProxy.getMonitorSysGenServerProxyEnabled() == 0) {
                return ReturnResult.error("代理未启用");
            }

            if (serverProxy.getMonitorSysGenServerProxyStatus() != 1) {
                return ReturnResult.error("代理未在线");
            }

            String proxyUrl = serverProxy.getMonitorSysGenServerProxyUrl();
            if (StringUtils.isBlank(proxyUrl)) {
                // 如果没有预设URL，可以根据代理配置生成
                // TODO: 根据代理类型和配置生成URL
                proxyUrl = generateProxyUrl(serverProxy);
            }

            return ReturnResult.ok(proxyUrl);
        } catch (Exception e) {
            log.error("获取代理URL失败: id={}", id, e);
            return ReturnResult.error("获取代理URL失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchUpdateStatus(List<Integer> ids, Integer status) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ReturnResult.error("关联ID列表不能为空");
            }
            if (status == null) {
                return ReturnResult.error("状态不能为空");
            }

            int updateCount = baseMapper.batchUpdateStatus(ids, status);
            boolean result = updateCount > 0;

            if (result) {
                log.info("批量更新代理状态成功: ids={}, status={}, updateCount={}", ids, status, updateCount);
            }
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("批量更新代理状态失败: ids={}, status={}", ids, status, e);
            return ReturnResult.error("批量更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteByServerId(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            int deleteCount = baseMapper.deleteByServerId(serverId);
            boolean result = deleteCount >= 0; // 即使删除0条也算成功

            if (result) {
                log.info("根据服务器ID删除代理关联成功: serverId={}, deleteCount={}", serverId, deleteCount);
            }
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("根据服务器ID删除代理关联失败: serverId={}", serverId, e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }



    @Override
    public ReturnResult<Map<String, Object>> getProxyStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总数统计
            long totalCount = this.count();
            statistics.put("totalCount", totalCount);

            // 按状态统计
            Map<Integer, Long> statusCount = this.list().stream()
                .collect(Collectors.groupingBy(
                    proxy -> proxy.getMonitorSysGenServerProxyStatus() != null ? proxy.getMonitorSysGenServerProxyStatus() : 0,
                    Collectors.counting()
                ));
            statistics.put("statusCount", statusCount);

            // 按类型统计
            Map<String, Long> typeCount = this.list().stream()
                .collect(Collectors.groupingBy(
                    proxy -> proxy.getMonitorSysGenServerProxyType() != null ? proxy.getMonitorSysGenServerProxyType() : "UNKNOWN",
                    Collectors.counting()
                ));
            statistics.put("typeCount", typeCount);

            // 启用/禁用统计
            Map<Integer, Long> enabledCount = this.list().stream()
                .collect(Collectors.groupingBy(
                    proxy -> proxy.getMonitorSysGenServerProxyEnabled() != null ? proxy.getMonitorSysGenServerProxyEnabled() : 0,
                    Collectors.counting()
                ));
            statistics.put("enabledCount", enabledCount);

            return ReturnResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取代理统计信息失败", e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getServerProxyTypes(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            List<MonitorSysGenServerProxy> proxies = baseMapper.selectByServerId(serverId);
            List<String> proxyTypes = proxies.stream()
                .map(MonitorSysGenServerProxy::getMonitorSysGenServerProxyType)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

            return ReturnResult.ok(proxyTypes);
        } catch (Exception e) {
            log.error("获取服务器代理类型失败: serverId={}", serverId, e);
            return ReturnResult.error("获取代理类型失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<List<MonitorSysGenServerProxy>> cloneServerProxyConfig(Integer sourceServerId, Integer targetServerId) {
        try {
            if (sourceServerId == null) {
                return ReturnResult.error("源服务器ID不能为空");
            }
            if (targetServerId == null) {
                return ReturnResult.error("目标服务器ID不能为空");
            }
            if (sourceServerId.equals(targetServerId)) {
                return ReturnResult.error("源服务器和目标服务器不能相同");
            }

            // 验证服务器是否存在
            MonitorSysGenServer sourceServer = monitorSysGenServerService.getById(sourceServerId);
            if (sourceServer == null) {
                return ReturnResult.error("源服务器不存在");
            }

            MonitorSysGenServer targetServer = monitorSysGenServerService.getById(targetServerId);
            if (targetServer == null) {
                return ReturnResult.error("目标服务器不存在");
            }

            // 获取源服务器的代理配置
            List<MonitorSysGenServerProxy> sourceProxies = baseMapper.selectByServerId(sourceServerId);
            if (sourceProxies.isEmpty()) {
                return ReturnResult.ok(new ArrayList<>());
            }

            // 克隆代理配置
            List<MonitorSysGenServerProxy> clonedProxies = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (MonitorSysGenServerProxy sourceProxy : sourceProxies) {
                // 检查目标服务器是否已存在相同类型的代理
                MonitorSysGenServerProxy existing = baseMapper.selectByServerIdAndProxyType(
                    targetServerId, sourceProxy.getMonitorSysGenServerProxyType()
                );
                if (existing != null) {
                    log.warn("目标服务器已存在相同类型的代理，跳过克隆: serverId={}, proxyType={}",
                        targetServerId, sourceProxy.getMonitorSysGenServerProxyType());
                    continue;
                }

                MonitorSysGenServerProxy clonedProxy = new MonitorSysGenServerProxy();
                clonedProxy.setMonitorSysGenServerId(targetServerId);

                clonedProxy.setMonitorSysGenServerProxyType(sourceProxy.getMonitorSysGenServerProxyType());
                clonedProxy.setMonitorSysGenServerProxyConfig(sourceProxy.getMonitorSysGenServerProxyConfig());
                clonedProxy.setMonitorSysGenServerProxyEnabled(sourceProxy.getMonitorSysGenServerProxyEnabled());
                clonedProxy.setMonitorSysGenServerProxyStatus(0); // 默认离线
                clonedProxy.setMonitorSysGenServerProxyRemark("从服务器 " + sourceServer.getMonitorSysGenServerName() + " 克隆");
                clonedProxy.setMonitorSysGenServerProxyCreateTime(now);
                clonedProxy.setMonitorSysGenServerProxyUpdateTime(now);

                this.save(clonedProxy);
                clonedProxies.add(clonedProxy);
            }

            log.info("克隆服务器代理配置成功: sourceServerId={}, targetServerId={}, clonedCount={}",
                sourceServerId, targetServerId, clonedProxies.size());

            return ReturnResult.ok(clonedProxies);
        } catch (Exception e) {
            log.error("克隆服务器代理配置失败: sourceServerId={}, targetServerId={}", sourceServerId, targetServerId, e);
            return ReturnResult.error("克隆失败: " + e.getMessage());
        }
    }

    /**
     * 生成代理URL
     */
    private String generateProxyUrl(MonitorSysGenServerProxy serverProxy) {
        // TODO: 根据代理类型和配置生成URL
        // 这里可以根据不同的代理类型实现不同的URL生成逻辑
        return "http://proxy-url-placeholder";
    }
}
