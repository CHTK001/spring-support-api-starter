package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponent;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.mapper.MonitorSysGenServerComponentMapper;
import com.chua.starter.monitor.service.server.MonitorSysGenServerComponentService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingService;
import com.chua.starter.redis.support.service.TimeSeriesService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
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

    @Autowired
    @Lazy
    private MonitorSysGenServerSettingService serverSettingService;

    @Autowired
    @Lazy
    private TimeSeriesService timeSeriesService;

    @Autowired
    @Lazy
    private PrometheusService prometheusService;

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    // Redis键前缀常量
    private static final String REDIS_TIME_SERIES_PREFIX = "monitor:timeseries:";
    private static final String REDIS_COMPONENT_PREFIX = "monitor:component:";
    // 服务器指标数据存储键前缀（与MonitorSysGenServerMetricsServiceImpl保持一致）
    private static final String REDIS_METRICS_PREFIX = "SERVER:server:metrics:";

    /**
     * 带缓存的根据ID获取组件信息
     *
     * @param componentId 组件ID
     * @return 组件信息
     */
    @Cacheable(value = "serverComponent", key = "#componentId", unless = "#result == null")
    public MonitorSysGenServerComponent getByIdWithCache(Long componentId) {
        return this.getById(componentId);
    }

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
            } else if ("COMPONENT".equalsIgnoreCase(expressionType)) {
                return executeComponentTimeSeriesQuery(componentId, expression, timeRange);
            } else if ("REALTIME".equalsIgnoreCase(expressionType)) {
                return executeRealtimeQuery(componentId, expression, timeRange);
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

            // 根据表达式类型路由到不同的数据查询方式
            if ("PROMETHEUS".equalsIgnoreCase(expressionType)) {
                return queryPrometheusData(componentId, expression, startTime, endTime, step);
            } else if ("COMPONENT".equalsIgnoreCase(expressionType)) {
                return queryTimeSeriesData(componentId, expression, startTime, endTime, step);
            } else if ("REALTIME".equalsIgnoreCase(expressionType)) {
                return getComponentRealtimeData(componentId);
            } else if ("SQL".equalsIgnoreCase(expressionType)) {
                return executeSqlQuery(component.getMonitorSysGenServerId(), expression, timeRange);
            } else {
                log.warn("不支持的表达式类型: {}, componentId: {}", expressionType, componentId);
                return ReturnResult.error("不支持的表达式类型: " + expressionType);
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

    @Override
    public ReturnResult<Object> queryTimeSeriesData(Long componentId, String indicatorName, Long startTime, Long endTime, Integer step) {
        log.debug("开始查询时序数据, componentId: {}, indicatorName: {}", componentId, indicatorName);

        try {
            if (timeSeriesService == null) {
                log.warn("TimeSeriesService未注入，无法查询时序数据");
                return ReturnResult.error("时序数据服务不可用");
            }

            // 获取组件信息以获取serverId
            MonitorSysGenServerComponent component = getById(componentId);
            if (component == null) {
                log.warn("查询时序数据失败, 组件不存在, componentId: {}", componentId);
                return ReturnResult.error("组件不存在");
            }

            Integer serverId = component.getMonitorSysGenServerId();
            if (serverId == null) {
                log.warn("查询时序数据失败, 组件未关联服务器, componentId: {}", componentId);
                return ReturnResult.error("组件未关联服务器");
            }

            // 构建正确的时序数据查询键，使用实际存储的键格式
            // 实际存储格式：SERVER:server:metrics:serverId:指标名称
            String timeSeriesKey = REDIS_METRICS_PREFIX + serverId + ":" + indicatorName;

            log.debug("查询时序数据键: {}, 时间范围: {} - {}", timeSeriesKey, startTime, endTime);

            // 调用时序数据服务查询
            ReturnResult<?> result = timeSeriesService.range(
                    timeSeriesKey,
                    startTime,
                    endTime,
                    false, // 不只获取最新数据
                    1000   // 不限制数量
            );

            if (result.isSuccess()) {
                log.debug("查询时序数据成功, componentId: {}, serverId: {}, indicatorName: {}, 数据量: {}",
                         componentId, serverId, indicatorName,
                         result.getData() != null ? result.getData().toString().length() : 0);
                return ReturnResult.ok(result.getData());
            } else {
                log.warn("查询时序数据失败, componentId: {}, serverId: {}, indicatorName: {}, error: {}",
                        componentId, serverId, indicatorName, result.getMsg());
                return ReturnResult.error("查询时序数据失败: " + result.getMsg());
            }
        } catch (Exception e) {
            log.error("查询时序数据异常, componentId: {}, indicatorName: {}", componentId, indicatorName, e);
            return ReturnResult.error("查询时序数据异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Object> queryPrometheusData(Long componentId, String promQL, Long startTime, Long endTime, Integer step) {
        log.debug("开始查询Prometheus数据, componentId: {}, promQL: {}", componentId, promQL);

        try {
            if (prometheusService == null) {
                log.warn("PrometheusService未注入，无法查询Prometheus数据");
                return ReturnResult.error("Prometheus服务不可用");
            }

            // 获取组件关联的服务器信息
            MonitorSysGenServerComponent component = getByIdWithCache(componentId);
            if (component == null) {
                return ReturnResult.error("组件不存在");
            }

            Integer serverId = component.getMonitorSysGenServerId();
            if (serverId == null) {
                return ReturnResult.error("组件未关联服务器");
            }

            // 获取服务器基本信息
            MonitorSysGenServer server = serverService.getByIdWithCache(serverId);
            if (server == null) {
                return ReturnResult.error("服务器配置不存在");
            }

            // 获取服务器配置信息，从中获取Prometheus配置
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                serverSettingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("服务器配置不存在或获取失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();

            // 构建Prometheus查询地址，优先使用配置中的URL
            String prometheusAddress;
            if (StringUtils.isNotBlank(setting.getMonitorSysGenServerSettingPrometheusUrl())) {
                // 使用配置中的完整URL
                prometheusAddress = setting.getMonitorSysGenServerSettingPrometheusUrl();
            } else if (StringUtils.isNotBlank(setting.getMonitorSysGenServerSettingPrometheusHost())
                    && setting.getMonitorSysGenServerSettingPrometheusPort() != null) {
                // 使用配置中的主机和端口
                prometheusAddress = "http://" + setting.getMonitorSysGenServerSettingPrometheusHost()
                    + ":" + setting.getMonitorSysGenServerSettingPrometheusPort();
            } else {
                return ReturnResult.error("未配置Prometheus地址");
            }

            // 执行Prometheus查询
            String queryResult;
            if (startTime != null && endTime != null && step != null) {
                // 范围查询
                JsonNode result = prometheusService.queryRange(prometheusAddress, promQL, startTime, endTime, step);
                queryResult = result != null ? result.toString() : "{}";
            } else {
                // 即时查询
                queryResult = prometheusService.queryPrometheus(prometheusAddress, promQL);
            }

            log.debug("查询Prometheus数据成功, componentId: {}, promQL: {}, address: {}",
                componentId, promQL, prometheusAddress);
            return ReturnResult.ok(queryResult);
        } catch (Exception e) {
            log.error("查询Prometheus数据异常, componentId: {}, promQL: {}", componentId, promQL, e);
            return ReturnResult.error("Prometheus数据查询异常");
        }
    }



    /**
     * 执行组件时序数据查询（COMPONENT类型）
     *
     * @param componentId 组件ID
     * @param expression 表达式（指标名称）
     * @param timeRange 时间范围参数
     * @return 查询结果
     */
    private ReturnResult<Object> executeComponentTimeSeriesQuery(Long componentId, String expression, Map<String, Object> timeRange) {
        log.debug("开始执行组件时序数据查询, componentId: {}, expression: {}", componentId, expression);

        try {
            if (timeSeriesService == null) {
                log.warn("TimeSeriesService未注入，无法查询时序数据");
                return ReturnResult.error("时序数据服务不可用");
            }

            // 从时间范围参数中提取时间信息
            Long startTime = extractTimeFromRange(timeRange, "startTime");
            Long endTime = extractTimeFromRange(timeRange, "endTime");
            Integer step = extractStepFromRange(timeRange);

            if (startTime == null || endTime == null) {
                return ReturnResult.error("时间范围参数不完整");
            }

            // 获取组件信息以获取serverId
            MonitorSysGenServerComponent component = getById(componentId);
            if (component == null) {
                log.warn("查询组件时序数据失败, 组件不存在, componentId: {}", componentId);
                return ReturnResult.error("组件不存在");
            }

            Integer serverId = component.getMonitorSysGenServerId();
            if (serverId == null) {
                log.warn("查询组件时序数据失败, 组件未关联服务器, componentId: {}", componentId);
                return ReturnResult.error("组件未关联服务器");
            }

            // 构建正确的时序数据查询键，使用实际存储的键格式
            // 实际存储格式：SERVER:server:metrics:serverId:指标名称
            String timeSeriesKey = REDIS_METRICS_PREFIX + serverId + ":" + expression;

            log.debug("查询组件时序数据键: {}, 时间范围: {} - {}", timeSeriesKey, startTime, endTime);

            // 调用时序数据服务查询
            ReturnResult<?> result = timeSeriesService.range(
                    timeSeriesKey,
                    startTime,
                    endTime,
                    false, // 不只获取最新数据
                    1000   // 不限制数量
            );

            if (result.isSuccess()) {
                log.debug("查询组件时序数据成功, componentId: {}, serverId: {}, expression: {}, 数据量: {}",
                         componentId, serverId, expression,
                         result.getData() != null ? result.getData().toString().length() : 0);
                return ReturnResult.ok(result.getData());
            } else {
                log.warn("查询组件时序数据失败, componentId: {}, serverId: {}, expression: {}, error: {}",
                        componentId, serverId, expression, result.getMsg());
                return ReturnResult.error("查询时序数据失败: " + result.getMsg());
            }
        } catch (Exception e) {
            log.error("执行组件时序数据查询失败, componentId: {}, expression: {}", componentId, expression, e);
            return ReturnResult.error("查询时序数据失败: " + e.getMessage());
        }
    }

    /**
     * 执行实时数据查询（REALTIME类型）
     *
     * @param componentId 组件ID
     * @param expression 表达式（实时数据源标识）
     * @param timeRange 时间范围参数
     * @return 查询结果
     */
    private ReturnResult<Object> executeRealtimeQuery(Long componentId, String expression, Map<String, Object> timeRange) {
        log.debug("开始执行实时数据查询, componentId: {}, expression: {}", componentId, expression);

        try {
            // 对于实时数据，返回当前缓存的最新数据
            // 实际的实时数据通过现有的服务器主页Socket.IO推送机制获取
            Map<String, Object> realtimeData = new HashMap<>();
            realtimeData.put("componentId", componentId);
            realtimeData.put("expression", expression);
            realtimeData.put("type", "realtime");
            realtimeData.put("message", "实时数据通过服务器主页Socket.IO推送机制获取，请使用useServerMetrics监听server_metrics消息");
            realtimeData.put("timestamp", System.currentTimeMillis());

            log.debug("实时数据查询完成, componentId: {}, expression: {}", componentId, expression);
            return ReturnResult.ok(realtimeData);
        } catch (Exception e) {
            log.error("执行实时数据查询失败, componentId: {}, expression: {}", componentId, expression, e);
            return ReturnResult.error("查询实时数据失败: " + e.getMessage());
        }
    }

    /**
     * 从时间范围参数中提取时间值
     *
     * @param timeRange 时间范围参数
     * @param key 时间键名
     * @return 时间戳（秒）
     */
    private Long extractTimeFromRange(Map<String, Object> timeRange, String key) {
        if (timeRange == null || !timeRange.containsKey(key)) {
            return null;
        }

        Object value = timeRange.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析时间参数: {} = {}", key, value);
                return null;
            }
        }
        return null;
    }

    /**
     * 从时间范围参数中提取步长值
     *
     * @param timeRange 时间范围参数
     * @return 步长（秒）
     */
    private Integer extractStepFromRange(Map<String, Object> timeRange) {
        if (timeRange == null || !timeRange.containsKey("step")) {
            return 60; // 默认步长60秒
        }

        Object value = timeRange.get("step");
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("无法解析步长参数: {}", value);
                return 60; // 默认步长60秒
            }
        }
        return 60; // 默认步长60秒
    }

}
