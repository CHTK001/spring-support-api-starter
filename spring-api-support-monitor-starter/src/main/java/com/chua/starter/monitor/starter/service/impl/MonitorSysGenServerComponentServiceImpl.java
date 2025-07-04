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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器组件配置服务实现类
 *
 * <p>该服务类负责管理服务器组件的配置信息，包括：</p>
 * <ul>
 *   <li>组件的增删改查操作</li>
 *   <li>组件数据的查询和统计</li>
 *   <li>组件表达式的验证</li>
 *   <li>组件的启用/禁用管理</li>
 *   <li>组件的克隆和批量操作</li>
 * </ul>
 *
 * <p>支持的组件类型包括：</p>
 * <ul>
 *   <li>Prometheus监控组件 - 支持PromQL查询</li>
 *   <li>SQL查询组件 - 支持自定义SQL查询</li>
 *   <li>系统组件 - 预定义的系统监控组件</li>
 * </ul>
 *
 * @author CH
 * @since 2025/01/03
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerComponentServiceImpl extends ServiceImpl<MonitorSysGenServerComponentMapper, MonitorSysGenServerComponent> 
        implements MonitorSysGenServerComponentService {

    private final MonitorSysGenServerComponentMapper componentMapper;
    private final MonitorSysGenServerService serverService;

    /**
     * 根据服务器ID获取组件列表
     *
     * @param serverId 服务器ID，不能为空
     * @return 返回指定服务器的所有组件配置列表
     * @throws IllegalArgumentException 当服务器ID为空时抛出
     */
    @Override
    public ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerId(Integer serverId) {
        log.debug("开始获取服务器组件列表, serverId: {}", serverId);

        if (serverId == null) {
            log.warn("获取服务器组件列表失败, 服务器ID为空");
            return ReturnResult.error("服务器ID不能为空");
        }

        try {
            List<MonitorSysGenServerComponent> components = componentMapper.selectByServerId(serverId);
            log.debug("获取服务器组件列表成功, serverId: {}, 组件数量: {}", serverId,
                    components != null ? components.size() : 0);
            return ReturnResult.ok(components);
        } catch (Exception e) {
            log.error("获取服务器组件列表失败, serverId: {}", serverId, e);
            return ReturnResult.error("获取组件列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据组件ID获取组件信息
     *
     * @param componentId 组件ID，不能为空
     * @return 返回指定组件的详细信息
     * @throws IllegalArgumentException 当组件ID为空时抛出
     */
    @Override
    public ReturnResult<MonitorSysGenServerComponent> getComponentById(Long componentId) {
        log.debug("开始获取组件信息, componentId: {}", componentId);

        if (componentId == null) {
            log.warn("获取组件信息失败, 组件ID为空");
            return ReturnResult.error("组件ID不能为空");
        }

        try {
            MonitorSysGenServerComponent component = getById(componentId);
            if (component == null) {
                log.warn("获取组件信息失败, 组件不存在, componentId: {}", componentId);
                return ReturnResult.error("组件不存在");
            }
            log.debug("获取组件信息成功, componentId: {}, componentName: {}", componentId,
                    component.getMonitorSysGenServerComponentName());
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

    /**
     * 查询组件数据（支持时间范围）
     *
     * @param componentId 组件ID，不能为空
     * @param startTime   开始时间（Unix时间戳，秒），可选
     * @param endTime     结束时间（Unix时间戳，秒），可选
     * @param step        数据采样步长（秒），默认60秒
     * @return 返回组件在指定时间范围内的监控数据
     */
    @Override
    public ReturnResult<Object> getComponentData(Long componentId, Long startTime, Long endTime, Integer step) {
        log.debug("开始查询组件数据, componentId: {}, startTime: {}, endTime: {}, step: {}",
                componentId, startTime, endTime, step);

        if (componentId == null) {
            log.warn("查询组件数据失败, 组件ID为空");
            return ReturnResult.error("组件ID不能为空");
        }

        try {
            MonitorSysGenServerComponent component = getById(componentId);
            if (component == null) {
                log.warn("查询组件数据失败, 组件不存在, componentId: {}", componentId);
                return ReturnResult.error("组件不存在");
            }

            // 设置默认时间范围（最近30分钟）
            if (startTime == null || endTime == null) {
                long now = System.currentTimeMillis() / 1000;
                endTime = endTime != null ? endTime : now;
                startTime = startTime != null ? startTime : (now - 30 * 60);
            }

            // 根据组件的表达式类型调用不同的查询服务
            String expressionType = component.getMonitorSysGenServerComponentExpressionType();
            String expression = component.getMonitorSysGenServerComponentExpression();

            Map<String, Object> timeRange = Map.of(
                    "startTime", startTime,
                    "endTime", endTime,
                    "step", step != null ? step : 60
            );

            if ("PROMETHEUS".equalsIgnoreCase(expressionType)) {
                return executePrometheusQuery(component.getMonitorSysGenServerId(), expression, timeRange);
            } else if ("SQL".equalsIgnoreCase(expressionType)) {
                return executeSqlQuery(component.getMonitorSysGenServerId(), expression, timeRange);
            } else {
                // 组件选择类型，返回模拟数据
                log.debug("查询组件数据成功, componentId: {}, 类型: 组件选择", componentId);
                return ReturnResult.ok(Map.of(
                        "type", "component",
                        "componentId", componentId,
                        "data", "组件数据查询功能待实现",
                        "timeRange", timeRange
                ));
            }
        } catch (Exception e) {
            log.error("查询组件数据失败, componentId: {}", componentId, e);
            return ReturnResult.error("查询组件数据失败: " + e.getMessage());
        }
    }

    /**
     * 批量查询组件数据
     *
     * @param serverId  服务器ID，不能为空
     * @param startTime 开始时间（Unix时间戳，秒），可选
     * @param endTime   结束时间（Unix时间戳，秒），可选
     * @param step      数据采样步长（秒），默认60秒
     * @return 返回指定服务器所有启用组件的监控数据集合
     */
    @Override
    public ReturnResult<Map<String, Object>> getBatchComponentData(Integer serverId, Long startTime, Long endTime, Integer step) {
        log.debug("开始批量查询组件数据, serverId: {}, startTime: {}, endTime: {}, step: {}",
                serverId, startTime, endTime, step);

        if (serverId == null) {
            log.warn("批量查询组件数据失败, 服务器ID为空");
            return ReturnResult.error("服务器ID不能为空");
        }

        try {
            // 获取启用的组件列表
            ReturnResult<List<MonitorSysGenServerComponent>> componentsResult = getEnabledComponentsByServerId(serverId);
            if (!componentsResult.isSuccess() || componentsResult.getData() == null) {
                log.warn("批量查询组件数据失败, 获取组件列表失败, serverId: {}", serverId);
                return ReturnResult.ok(Map.of("components", Map.of()));
            }

            List<MonitorSysGenServerComponent> components = componentsResult.getData();
            Map<String, Object> result = new HashMap<>();

            for (MonitorSysGenServerComponent component : components) {
                ReturnResult<Object> componentData = getComponentData(
                        component.getMonitorSysGenServerComponentId(),
                        startTime,
                        endTime,
                        step
                );
                if (componentData.isSuccess()) {
                    result.put(String.valueOf(component.getMonitorSysGenServerComponentId()), componentData.getData());
                }
            }

            Map<String, Object> response = Map.of(
                    "serverId", serverId,
                    "components", result,
                    "timeRange", Map.of(
                            "startTime", startTime != null ? startTime : (System.currentTimeMillis() / 1000 - 30 * 60),
                            "endTime", endTime != null ? endTime : (System.currentTimeMillis() / 1000),
                            "step", step != null ? step : 60
                    ),
                    "timestamp", System.currentTimeMillis()
            );

            log.debug("批量查询组件数据成功, serverId: {}, 组件数量: {}", serverId, result.size());
            return ReturnResult.ok(response);
        } catch (Exception e) {
            log.error("批量查询组件数据失败, serverId: {}", serverId, e);
            return ReturnResult.error("批量查询组件数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取组件实时数据
     *
     * @param componentId 组件ID，不能为空
     * @return 返回组件的实时监控数据（最近1分钟的数据）
     */
    @Override
    public ReturnResult<Object> getComponentRealtimeData(Long componentId) {
        log.debug("开始获取组件实时数据, componentId: {}", componentId);

        try {
            // 获取实时数据，相当于查询最近1分钟的数据
            long now = System.currentTimeMillis() / 1000;
            long oneMinuteAgo = now - 60;

            ReturnResult<Object> result = getComponentData(componentId, oneMinuteAgo, now, 15);
            log.debug("获取组件实时数据成功, componentId: {}", componentId);
            return result;
        } catch (Exception e) {
            log.error("获取组件实时数据失败, componentId: {}", componentId, e);
            return ReturnResult.error("获取组件实时数据失败: " + e.getMessage());
        }
    }
}
