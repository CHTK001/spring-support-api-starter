package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerComponent;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerComponentMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerComponentService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器组件配置服务实现类
 *
 * @author CH
 * @since 2025/01/03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerComponentServiceImpl extends ServiceImpl<MonitorSysGenServerComponentMapper, MonitorSysGenServerComponent> 
        implements MonitorSysGenServerComponentService {

    private final MonitorSysGenServerComponentMapper componentMapper;
    private final MonitorSysGenServerService serverService;

    @Override
    public ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerId(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.error("服务器ID不能为空");
        }

        try {
            List<MonitorSysGenServerComponent> components = componentMapper.selectByServerId(serverId);
            return ReturnResult.ok(components);
        } catch (Exception e) {
            log.error("获取服务器组件列表失败, serverId: {}", serverId, e);
            return ReturnResult.error("获取组件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerComponent> getComponentById(Long componentId) {
        if (componentId == null) {
            return ReturnResult.error("组件ID不能为空");
        }

        try {
            MonitorSysGenServerComponent component = getById(componentId);
            if (component == null) {
                return ReturnResult.error("组件不存在");
            }
            return ReturnResult.ok(component);
        } catch (Exception e) {
            log.error("获取组件信息失败, componentId: {}", componentId, e);
            return ReturnResult.error("获取组件信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateComponent(MonitorSysGenServerComponent component) {
        if (component == null || component.getMonitorSysGenServerComponentId() == null) {
            return ReturnResult.error("组件信息不能为空");
        }

        try {
            component.setMonitorSysGenServerComponentUpdateTime(LocalDateTime.now());
            boolean result = updateById(component);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("更新组件信息失败, componentId: {}", component.getMonitorSysGenServerComponentId(), e);
            return ReturnResult.error("更新组件信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerIdAndType(Integer serverId, String componentType) {
        if (serverId == null || StringUtils.isEmpty(componentType)) {
            return ReturnResult.error("参数不能为空");
        }

        try {
            List<MonitorSysGenServerComponent> components = componentMapper.selectByServerIdAndType(serverId, componentType);
            return ReturnResult.ok(components);
        } catch (Exception e) {
            log.error("获取服务器组件列表失败, serverId: {}, componentType: {}", serverId, componentType, e);
            return ReturnResult.error("获取组件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerIdAndExpressionType(Integer serverId, String expressionType) {
        if (serverId == null || StringUtils.isEmpty(expressionType)) {
            return ReturnResult.error("参数不能为空");
        }

        try {
            List<MonitorSysGenServerComponent> components = componentMapper.selectByServerIdAndExpressionType(serverId, expressionType);
            return ReturnResult.ok(components);
        } catch (Exception e) {
            log.error("获取服务器组件列表失败, serverId: {}, expressionType: {}", serverId, expressionType, e);
            return ReturnResult.error("获取组件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerComponent>> getEnabledComponentsByServerId(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.error("服务器ID不能为空");
        }

        try {
            List<MonitorSysGenServerComponent> components = componentMapper.selectEnabledByServerId(serverId);
            return ReturnResult.ok(components);
        } catch (Exception e) {
            log.error("获取启用的服务器组件列表失败, serverId: {}", serverId, e);
            return ReturnResult.error("获取启用的组件列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchUpdatePosition(List<MonitorSysGenServerComponent> components) {
        if (components == null || components.isEmpty()) {
            return ReturnResult.error("组件列表不能为空");
        }

        try {
            // 更新时间戳
            LocalDateTime now = LocalDateTime.now();
            components.forEach(component -> component.setMonitorSysGenServerComponentUpdateTime(now));
            
            int updateCount = componentMapper.batchUpdatePosition(components);
            return ReturnResult.ok(updateCount > 0);
        } catch (Exception e) {
            log.error("批量更新组件位置失败", e);
            return ReturnResult.error("批量更新组件位置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> initDefaultComponentsForServer(Integer serverId) {
        try {
            // 获取所有模板
            List<MonitorSysGenServerComponent> templates = componentMapper.selectTemplates();
            if (templates.isEmpty()) {
                return ReturnResult.ok(true);
            }
            
            // 提取模板ID
            List<Long> templateIds = templates.stream()
                .map(MonitorSysGenServerComponent::getMonitorSysGenServerComponentId)
                .toList();
            
            // 复制模板到服务器
            int copyCount = componentMapper.copyTemplatesForServer(serverId, templateIds);
            return ReturnResult.ok(copyCount > 0);
        } catch (Exception e) {
            log.error("为服务器初始化默认组件失败, serverId: {}", serverId, e);
            return ReturnResult.error("初始化默认组件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getComponentStatistics(Integer serverId) {
        try {
            List<Map<String, Object>> statistics = componentMapper.getComponentStatistics(serverId);
            return ReturnResult.ok(Map.of("statistics", statistics));
        } catch (Exception e) {
            log.error("获取服务器组件统计信息失败, serverId: {}", serverId, e);
            return ReturnResult.error("获取组件统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Object> executeComponentQuery(Long componentId, Map<String, Object> timeRange) {
        try {
            MonitorSysGenServerComponent component = getById(componentId);
            if (component == null) {
                return ReturnResult.error("组件不存在");
            }

            // 根据表达式类型执行不同的查询
            String expressionType = component.getMonitorSysGenServerComponentExpressionType();
            String expression = component.getMonitorSysGenServerComponentExpression();
            Integer serverId = component.getMonitorSysGenServerId();

            if ("PROMETHEUS".equalsIgnoreCase(expressionType)) {
                return executePrometheusQuery(serverId, expression, timeRange);
            } else if ("SQL".equalsIgnoreCase(expressionType)) {
                return executeSqlQuery(serverId, expression, timeRange);
            } else {
                return ReturnResult.error("不支持的表达式类型: " + expressionType);
            }
        } catch (Exception e) {
            log.error("执行组件查询失败, componentId: {}", componentId, e);
            return ReturnResult.error("执行组件查询失败: " + e.getMessage());
        }
    }

    /**
     * 执行Prometheus查询
     */
    private ReturnResult<Object> executePrometheusQuery(Integer serverId, String expression, Map<String, Object> timeRange) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // TODO: 实现Prometheus查询逻辑，参考PrometheusReportHandler的实现
            log.debug("执行Prometheus查询: serverId={}, expression={}", serverId, expression);
            return ReturnResult.ok("Prometheus查询功能待实现");
        } catch (Exception e) {
            log.error("执行Prometheus查询失败", e);
            return ReturnResult.error("Prometheus查询失败: " + e.getMessage());
        }
    }

    /**
     * 执行SQL查询
     */
    private ReturnResult<Object> executeSqlQuery(Integer serverId, String expression, Map<String, Object> timeRange) {
        try {
            // TODO: 实现SQL查询逻辑
            log.debug("执行SQL查询: serverId={}, expression={}", serverId, expression);
            return ReturnResult.ok("SQL查询功能待实现");
        } catch (Exception e) {
            log.error("执行SQL查询失败", e);
            return ReturnResult.error("SQL查询失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> validateComponentExpression(String expressionType, String expression, Integer serverId) {
        try {
            if (StringUtils.isEmpty(expressionType) || StringUtils.isEmpty(expression)) {
                return ReturnResult.error("表达式类型和表达式内容不能为空");
            }

            // TODO: 实现表达式验证逻辑
            // 对于PROMETHEUS类型，验证PromQL语法
            // 对于SQL类型，验证SQL语法
            log.debug("验证组件表达式: expressionType={}, expression={}", expressionType, expression);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("验证组件表达式失败, expressionType: {}, expression: {}", expressionType, expression, e);
            return ReturnResult.error("验证表达式失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerComponent> cloneComponent(Long sourceComponentId, Integer targetServerId) {
        try {
            MonitorSysGenServerComponent sourceComponent = getById(sourceComponentId);
            if (sourceComponent == null) {
                return ReturnResult.error("源组件不存在");
            }

            // 创建新组件
            MonitorSysGenServerComponent newComponent = new MonitorSysGenServerComponent();
            newComponent.setMonitorSysGenServerId(targetServerId);
            newComponent.setMonitorSysGenServerComponentName(sourceComponent.getMonitorSysGenServerComponentName() + "_副本");
            newComponent.setMonitorSysGenServerComponentTitle(sourceComponent.getMonitorSysGenServerComponentTitle());
            newComponent.setMonitorSysGenServerComponentType(sourceComponent.getMonitorSysGenServerComponentType());
            newComponent.setMonitorSysGenServerComponentExpressionType(sourceComponent.getMonitorSysGenServerComponentExpressionType());
            newComponent.setMonitorSysGenServerComponentExpression(sourceComponent.getMonitorSysGenServerComponentExpression());
            newComponent.setMonitorSysGenServerComponentPosition(sourceComponent.getMonitorSysGenServerComponentPosition());
            newComponent.setMonitorSysGenServerComponentConfig(sourceComponent.getMonitorSysGenServerComponentConfig());
            newComponent.setMonitorSysGenServerComponentRefreshInterval(sourceComponent.getMonitorSysGenServerComponentRefreshInterval());
            newComponent.setMonitorSysGenServerComponentEnabled(sourceComponent.getMonitorSysGenServerComponentEnabled());
            newComponent.setMonitorSysGenServerComponentSortOrder(sourceComponent.getMonitorSysGenServerComponentSortOrder());
            newComponent.setMonitorSysGenServerComponentDesc(sourceComponent.getMonitorSysGenServerComponentDesc());
            newComponent.setMonitorSysGenServerComponentCreateTime(LocalDateTime.now());
            newComponent.setMonitorSysGenServerComponentUpdateTime(LocalDateTime.now());

            boolean result = save(newComponent);
            if (result) {
                return ReturnResult.ok(newComponent);
            } else {
                return ReturnResult.error("克隆组件失败");
            }
        } catch (Exception e) {
            log.error("克隆组件失败, sourceComponentId: {}, targetServerId: {}", sourceComponentId, targetServerId, e);
            return ReturnResult.error("克隆组件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteComponentsByServerId(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.error("服务器ID不能为空");
        }

        try {
            boolean result = lambdaUpdate()
                    .eq(MonitorSysGenServerComponent::getMonitorSysGenServerId, serverId)
                    .remove();
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("删除服务器组件失败, serverId: {}", serverId, e);
            return ReturnResult.error("删除服务器组件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> toggleComponentEnabled(Long componentId, Boolean enabled) {
        if (componentId == null || enabled == null) {
            return ReturnResult.error("参数不能为空");
        }

        try {
            boolean result = lambdaUpdate()
                    .eq(MonitorSysGenServerComponent::getMonitorSysGenServerComponentId, componentId)
                    .set(MonitorSysGenServerComponent::getMonitorSysGenServerComponentEnabled, enabled ? 1 : 0)
                    .set(MonitorSysGenServerComponent::getMonitorSysGenServerComponentUpdateTime, LocalDateTime.now())
                    .update();
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("切换组件启用状态失败, componentId: {}, enabled: {}", componentId, enabled, e);
            return ReturnResult.error("切换组件启用状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateComponentSortOrder(Long componentId, Integer sortOrder) {
        if (componentId == null || sortOrder == null) {
            return ReturnResult.error("参数不能为空");
        }

        try {
            boolean result = lambdaUpdate()
                    .eq(MonitorSysGenServerComponent::getMonitorSysGenServerComponentId, componentId)
                    .set(MonitorSysGenServerComponent::getMonitorSysGenServerComponentSortOrder, sortOrder)
                    .set(MonitorSysGenServerComponent::getMonitorSysGenServerComponentUpdateTime, LocalDateTime.now())
                    .update();
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("更新组件排序失败, componentId: {}, sortOrder: {}", componentId, sortOrder, e);
            return ReturnResult.error("更新组件排序失败: " + e.getMessage());
        }
    }
}
