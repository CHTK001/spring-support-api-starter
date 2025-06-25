package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerDetailComponent;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerDetailComponentMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerDetailComponentService;
import com.chua.common.support.lang.code.ReturnResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器详情页组件配置服务实现
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerDetailComponentServiceImpl 
    extends ServiceImpl<MonitorSysGenServerDetailComponentMapper, MonitorSysGenServerDetailComponent> 
    implements MonitorSysGenServerDetailComponentService {

    private final MonitorSysGenServerDetailComponentMapper componentMapper;

    @Override
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentsByServerId(Integer serverId) {
        try {
            List<MonitorSysGenServerDetailComponent> components = componentMapper.selectByServerId(serverId);
            return ReturnResult.ok(components);
        } catch (Exception e) {
            log.error("获取服务器组件列表失败, serverId: {}", serverId, e);
            return ReturnResult.error("获取组件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentsByServerIdAndType(Integer serverId, String componentType) {
        try {
            List<MonitorSysGenServerDetailComponent> components = componentMapper.selectByServerIdAndType(serverId, componentType);
            return ReturnResult.ok(components);
        } catch (Exception e) {
            log.error("根据类型获取服务器组件列表失败, serverId: {}, componentType: {}", serverId, componentType, e);
            return ReturnResult.error("获取组件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentsByServerIdAndExpressionType(Integer serverId, String expressionType) {
        try {
            List<MonitorSysGenServerDetailComponent> components = componentMapper.selectByServerIdAndExpressionType(serverId, expressionType);
            return ReturnResult.ok(components);
        } catch (Exception e) {
            log.error("根据表达式类型获取服务器组件列表失败, serverId: {}, expressionType: {}", serverId, expressionType, e);
            return ReturnResult.error("获取组件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getEnabledComponentsByServerId(Integer serverId) {
        try {
            List<MonitorSysGenServerDetailComponent> components = componentMapper.selectEnabledByServerId(serverId);
            return ReturnResult.ok(components);
        } catch (Exception e) {
            log.error("获取启用的服务器组件列表失败, serverId: {}", serverId, e);
            return ReturnResult.error("获取启用组件列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerDetailComponent> saveOrUpdateComponent(MonitorSysGenServerDetailComponent component) {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            if (component.getMonitorSysGenServerDetailComponentId() == null) {
                // 新增
                component.setMonitorSysGenServerDetailComponentCreateTime(now);
                component.setMonitorSysGenServerDetailComponentUpdateTime(now);
                component.setMonitorSysGenServerDetailComponentEnabled(1); // 默认启用
                
                // 设置默认排序序号
                if (component.getMonitorSysGenServerDetailComponentSortOrder() == null) {
                    component.setMonitorSysGenServerDetailComponentSortOrder(0);
                }
            } else {
                // 更新
                component.setMonitorSysGenServerDetailComponentUpdateTime(now);
            }
            
            boolean success = saveOrUpdate(component);
            if (success) {
                return ReturnResult.ok(component);
            } else {
                return ReturnResult.error("保存组件失败");
            }
        } catch (Exception e) {
            log.error("保存或更新组件失败", e);
            return ReturnResult.error("保存组件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchUpdateComponentPosition(List<MonitorSysGenServerDetailComponent> components) {
        try {
            if (components == null || components.isEmpty()) {
                return ReturnResult.ok(true);
            }
            
            int updateCount = componentMapper.batchUpdatePosition(components);
            return ReturnResult.ok(updateCount > 0);
        } catch (Exception e) {
            log.error("批量更新组件位置失败", e);
            return ReturnResult.error("批量更新组件位置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteComponent(Long componentId) {
        try {
            boolean success = removeById(componentId);
            return ReturnResult.ok(success);
        } catch (Exception e) {
            log.error("删除组件失败, componentId: {}", componentId, e);
            return ReturnResult.error("删除组件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteComponentsByServerId(Integer serverId) {
        try {
            int deleteCount = componentMapper.deleteByServerId(serverId);
            return ReturnResult.ok(deleteCount >= 0);
        } catch (Exception e) {
            log.error("删除服务器所有组件失败, serverId: {}", serverId, e);
            return ReturnResult.error("删除服务器组件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentTemplates() {
        try {
            List<MonitorSysGenServerDetailComponent> templates = componentMapper.selectTemplates();
            return ReturnResult.ok(templates);
        } catch (Exception e) {
            log.error("获取组件模板列表失败", e);
            return ReturnResult.error("获取组件模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> initDefaultComponentsForServer(Integer serverId) {
        try {
            // 获取所有模板
            List<MonitorSysGenServerDetailComponent> templates = componentMapper.selectTemplates();
            if (templates.isEmpty()) {
                return ReturnResult.ok(true);
            }
            
            // 提取模板ID
            List<Long> templateIds = templates.stream()
                .map(MonitorSysGenServerDetailComponent::getMonitorSysGenServerDetailComponentId)
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
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> copyTemplatesForServer(Integer serverId, List<Long> templateIds) {
        try {
            if (templateIds == null || templateIds.isEmpty()) {
                return ReturnResult.ok(true);
            }
            
            int copyCount = componentMapper.copyTemplatesForServer(serverId, templateIds);
            return ReturnResult.ok(copyCount > 0);
        } catch (Exception e) {
            log.error("复制模板组件到服务器失败, serverId: {}, templateIds: {}", serverId, templateIds, e);
            return ReturnResult.error("复制模板组件失败: " + e.getMessage());
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
            // TODO: 实现组件查询逻辑，根据组件的表达式类型调用不同的查询服务
            // 这里需要集成Prometheus查询服务和SQL查询服务
            return ReturnResult.ok("查询功能待实现");
        } catch (Exception e) {
            log.error("执行组件查询失败, componentId: {}", componentId, e);
            return ReturnResult.error("执行组件查询失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> validateComponentExpression(String expressionType, String expression, Integer serverId) {
        try {
            // TODO: 实现表达式验证逻辑
            // 对于PROMETHEUS类型，验证PromQL语法
            // 对于SQL类型，验证SQL语法
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("验证组件表达式失败, expressionType: {}, expression: {}", expressionType, expression, e);
            return ReturnResult.error("验证表达式失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerDetailComponent> cloneComponent(Long sourceComponentId, Integer targetServerId) {
        try {
            MonitorSysGenServerDetailComponent sourceComponent = getById(sourceComponentId);
            if (sourceComponent == null) {
                return ReturnResult.error("源组件不存在");
            }
            
            // 创建克隆组件
            MonitorSysGenServerDetailComponent clonedComponent = new MonitorSysGenServerDetailComponent();
            clonedComponent.setMonitorSysGenServerId(targetServerId);
            clonedComponent.setMonitorSysGenServerDetailComponentName(sourceComponent.getMonitorSysGenServerDetailComponentName() + "_copy");
            clonedComponent.setMonitorSysGenServerDetailComponentTitle(sourceComponent.getMonitorSysGenServerDetailComponentTitle() + " (副本)");
            clonedComponent.setMonitorSysGenServerDetailComponentType(sourceComponent.getMonitorSysGenServerDetailComponentType());
            clonedComponent.setMonitorSysGenServerDetailComponentExpressionType(sourceComponent.getMonitorSysGenServerDetailComponentExpressionType());
            clonedComponent.setMonitorSysGenServerDetailComponentExpression(sourceComponent.getMonitorSysGenServerDetailComponentExpression());
            clonedComponent.setMonitorSysGenServerDetailComponentPosition(sourceComponent.getMonitorSysGenServerDetailComponentPosition());
            clonedComponent.setMonitorSysGenServerDetailComponentChartConfig(sourceComponent.getMonitorSysGenServerDetailComponentChartConfig());
            clonedComponent.setMonitorSysGenServerDetailComponentRefreshInterval(sourceComponent.getMonitorSysGenServerDetailComponentRefreshInterval());
            clonedComponent.setMonitorSysGenServerDetailComponentSortOrder(sourceComponent.getMonitorSysGenServerDetailComponentSortOrder());
            clonedComponent.setMonitorSysGenServerDetailComponentDesc(sourceComponent.getMonitorSysGenServerDetailComponentDesc());
            
            return saveOrUpdateComponent(clonedComponent);
        } catch (Exception e) {
            log.error("克隆组件失败, sourceComponentId: {}, targetServerId: {}", sourceComponentId, targetServerId, e);
            return ReturnResult.error("克隆组件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> toggleComponentEnabled(Long componentId, Boolean enabled) {
        try {
            MonitorSysGenServerDetailComponent component = getById(componentId);
            if (component == null) {
                return ReturnResult.error("组件不存在");
            }
            
            component.setMonitorSysGenServerDetailComponentEnabled(enabled ? 1 : 0);
            component.setMonitorSysGenServerDetailComponentUpdateTime(LocalDateTime.now());
            
            boolean success = updateById(component);
            return ReturnResult.ok(success);
        } catch (Exception e) {
            log.error("切换组件启用状态失败, componentId: {}, enabled: {}", componentId, enabled, e);
            return ReturnResult.error("切换组件状态失败: " + e.getMessage());
        }
    }
}
