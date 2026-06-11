package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponent;
import com.chua.starter.monitor.entity.MonitorSysGenServerLayout;
import com.chua.starter.monitor.mapper.MonitorSysGenServerLayoutMapper;
import com.chua.starter.monitor.service.server.MonitorSysGenServerComponentService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerLayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器布局配置服务实现类
 *
 * @author CH
 * @since 2025/01/03
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerLayoutServiceImpl
        extends ServiceImpl<MonitorSysGenServerLayoutMapper, MonitorSysGenServerLayout>
        implements MonitorSysGenServerLayoutService {

    private final Map<String, Map<String, Object>> chartConfigCache = new ConcurrentHashMap<>();
    private final MonitorSysGenServerComponentService componentService;

    @Override
    public ReturnResult<List<MonitorSysGenServerLayout>> getLayoutsByServerId(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.error("服务器ID不能为空");
        }
        try {
            return ReturnResult.ok(baseMapper.selectByServerId(serverId));
        } catch (Exception e) {
            log.error("获取服务器布局列表失败: serverId={}", serverId, e);
            return ReturnResult.error("获取布局列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerLayout> getDefaultLayout(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.error("服务器ID不能为空");
        }
        try {
            MonitorSysGenServerLayout defaultLayout = baseMapper.selectDefaultByServerId(serverId);
            return defaultLayout == null ? createDefaultLayout(serverId) : ReturnResult.ok(defaultLayout);
        } catch (Exception e) {
            log.error("获取服务器默认布局失败: serverId={}", serverId, e);
            return ReturnResult.error("获取默认布局失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerLayout> saveLayout(MonitorSysGenServerLayout layout) {
        if (layout == null) {
            return ReturnResult.error("布局配置不能为空");
        }
        try {
            if (layout.getMonitorSysGenServerLayoutStatus() == null) {
                layout.setMonitorSysGenServerLayoutStatus(1);
            }
            if (layout.getMonitorSysGenServerLayoutDefault() == null) {
                layout.setMonitorSysGenServerLayoutDefault(0);
            }
            if (layout.getMonitorSysGenServerLayoutType() == null) {
                layout.setMonitorSysGenServerLayoutType("custom");
            }
            if (layout.getMonitorSysGenServerLayoutVersion() == null) {
                layout.setMonitorSysGenServerLayoutVersion(1);
            }
            return this.save(layout) ? ReturnResult.ok(layout) : ReturnResult.error("保存布局失败");
        } catch (Exception e) {
            log.error("保存服务器布局失败: layout={}", layout, e);
            return ReturnResult.error("保存布局失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateLayout(MonitorSysGenServerLayout layout) {
        if (layout == null || layout.getMonitorSysGenServerLayoutId() == null) {
            return ReturnResult.error("布局配置不能为空");
        }
        try {
            if (layout.getMonitorSysGenServerLayoutVersion() != null) {
                layout.setMonitorSysGenServerLayoutVersion(layout.getMonitorSysGenServerLayoutVersion() + 1);
            }
            return this.updateById(layout) ? ReturnResult.ok(true) : ReturnResult.error("更新布局失败");
        } catch (Exception e) {
            log.error("更新服务器布局失败: layout={}", layout, e);
            return ReturnResult.error("更新布局失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteLayout(Integer layoutId) {
        if (layoutId == null) {
            return ReturnResult.error("布局ID不能为空");
        }
        try {
            MonitorSysGenServerLayout layout = this.getById(layoutId);
            if (layout == null) {
                return ReturnResult.error("布局不存在");
            }
            if (layout.getMonitorSysGenServerLayoutDefault() != null && layout.getMonitorSysGenServerLayoutDefault() == 1) {
                return ReturnResult.error("默认布局不能删除");
            }
            return this.removeById(layoutId) ? ReturnResult.ok(true) : ReturnResult.error("删除布局失败");
        } catch (Exception e) {
            log.error("删除服务器布局失败: layoutId={}", layoutId, e);
            return ReturnResult.error("删除布局失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> setDefaultLayout(Integer serverId, Integer layoutId) {
        if (serverId == null || layoutId == null) {
            return ReturnResult.error("参数不能为空");
        }
        try {
            MonitorSysGenServerLayout layout = this.getById(layoutId);
            if (layout == null) {
                return ReturnResult.error("布局不存在");
            }
            if (!serverId.equals(layout.getMonitorSysGenServerId())) {
                return ReturnResult.error("布局不属于指定服务器");
            }
            baseMapper.clearDefaultLayout(serverId);
            return baseMapper.setDefaultLayout(layoutId) > 0 ? ReturnResult.ok(true) : ReturnResult.error("设置默认布局失败");
        } catch (Exception e) {
            log.error("设置默认布局失败: serverId={}, layoutId={}", serverId, layoutId, e);
            return ReturnResult.error("设置默认布局失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerLayout> generateLayoutFromComponents(Integer serverId, String layoutName) {
        if (serverId == null || StringUtils.isEmpty(layoutName)) {
            return ReturnResult.error("参数不能为空");
        }
        try {
            ReturnResult<List<MonitorSysGenServerComponent>> componentsResult = componentService.getComponentsByServerId(serverId);
            if (!componentsResult.isOk() || componentsResult.getData() == null) {
                return ReturnResult.error("获取服务器组件失败");
            }

            List<Map<String, Object>> nodes = new ArrayList<>();
            for (MonitorSysGenServerComponent component : componentsResult.getData()) {
                if (StringUtils.isEmpty(component.getMonitorSysGenServerComponentPosition())) {
                    continue;
                }
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> position = Json.fromJson(component.getMonitorSysGenServerComponentPosition(), Map.class);
                    Map<String, Object> node = new HashMap<>(position);
                    node.put("componentId", component.getMonitorSysGenServerComponentId());
                    node.put("type", component.getMonitorSysGenServerComponentType());
                    node.put("name", component.getMonitorSysGenServerComponentName());
                    node.put("config", component.getMonitorSysGenServerComponentConfig());
                    nodes.add(node);
                } catch (Exception ex) {
                    log.warn("解析组件位置信息失败: componentId={}", component.getMonitorSysGenServerComponentId());
                }
            }

            Map<String, Object> graph = new HashMap<>();
            graph.put("nodes", nodes);
            graph.put("edges", new ArrayList<>());
            graph.put("version", "1.0");
            graph.put("generatedAt", System.currentTimeMillis());

            MonitorSysGenServerLayout layout = new MonitorSysGenServerLayout();
            layout.setMonitorSysGenServerId(serverId);
            layout.setMonitorSysGenServerLayoutName(layoutName);
            layout.setMonitorSysGenServerLayoutConfig(Json.toJson(graph));
            layout.setMonitorSysGenServerLayoutType("generated");
            layout.setMonitorSysGenServerLayoutStatus(1);
            layout.setMonitorSysGenServerLayoutDefault(0);
            layout.setMonitorSysGenServerLayoutVersion(1);
            layout.setMonitorSysGenServerLayoutDescription("从组件配置生成的布局");
            return saveLayout(layout);
        } catch (Exception e) {
            log.error("从组件生成布局失败: serverId={}, layoutName={}", serverId, layoutName, e);
            return ReturnResult.error("生成布局失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> applyLayoutToComponents(Integer serverId, Integer layoutId) {
        if (serverId == null || layoutId == null) {
            return ReturnResult.error("参数不能为空");
        }
        try {
            MonitorSysGenServerLayout layout = this.getById(layoutId);
            if (layout == null) {
                return ReturnResult.error("布局不存在");
            }
            if (!serverId.equals(layout.getMonitorSysGenServerId())) {
                return ReturnResult.error("布局不属于指定服务器");
            }
            if (StringUtils.isEmpty(layout.getMonitorSysGenServerLayoutConfig())) {
                return ReturnResult.error("布局配置为空");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> graph = Json.fromJson(layout.getMonitorSysGenServerLayoutConfig(), Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) graph.get("nodes");
            if (nodes == null || nodes.isEmpty()) {
                return ReturnResult.error("布局配置无效");
            }

            for (Map<String, Object> node : nodes) {
                Long componentId = null;
                Object componentIdObj = node.get("componentId");
                if (componentIdObj instanceof Integer) {
                    componentId = ((Integer) componentIdObj).longValue();
                } else if (componentIdObj instanceof Long) {
                    componentId = (Long) componentIdObj;
                }
                if (componentId == null) {
                    continue;
                }

                ReturnResult<MonitorSysGenServerComponent> componentResult = componentService.getComponentById(componentId);
                if (!componentResult.isOk() || componentResult.getData() == null) {
                    continue;
                }

                MonitorSysGenServerComponent component = componentResult.getData();
                Map<String, Object> position = new HashMap<>();
                position.put("x", node.get("x"));
                position.put("y", node.get("y"));
                position.put("w", node.get("w"));
                position.put("h", node.get("h"));
                position.put("i", node.get("i"));
                component.setMonitorSysGenServerComponentPosition(Json.toJson(position));
                if (node.get("config") != null) {
                    component.setMonitorSysGenServerComponentConfig(String.valueOf(node.get("config")));
                }
                componentService.updateComponent(component);
            }
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("应用布局到组件失败: serverId={}, layoutId={}", serverId, layoutId, e);
            return ReturnResult.error("应用布局失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerLayout> copyLayoutToServer(Integer sourceLayoutId, Integer targetServerId, String layoutName) {
        if (sourceLayoutId == null || targetServerId == null || StringUtils.isEmpty(layoutName)) {
            return ReturnResult.error("参数不能为空");
        }
        try {
            MonitorSysGenServerLayout sourceLayout = this.getById(sourceLayoutId);
            if (sourceLayout == null) {
                return ReturnResult.error("源布局不存在");
            }
            MonitorSysGenServerLayout newLayout = new MonitorSysGenServerLayout();
            newLayout.setMonitorSysGenServerId(targetServerId);
            newLayout.setMonitorSysGenServerLayoutName(layoutName);
            newLayout.setMonitorSysGenServerLayoutConfig(sourceLayout.getMonitorSysGenServerLayoutConfig());
            newLayout.setMonitorSysGenServerLayoutType("copied");
            newLayout.setMonitorSysGenServerLayoutStatus(1);
            newLayout.setMonitorSysGenServerLayoutDefault(0);
            newLayout.setMonitorSysGenServerLayoutVersion(1);
            newLayout.setMonitorSysGenServerLayoutDescription("从布局ID " + sourceLayoutId + " 复制");
            return saveLayout(newLayout);
        } catch (Exception e) {
            log.error("复制布局失败: sourceLayoutId={}, targetServerId={}, layoutName={}", sourceLayoutId, targetServerId, layoutName, e);
            return ReturnResult.error("复制布局失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerLayout> getLayoutById(Integer layoutId) {
        if (layoutId == null) {
            return ReturnResult.error("布局ID不能为空");
        }
        try {
            MonitorSysGenServerLayout layout = this.getById(layoutId);
            return layout == null ? ReturnResult.error("布局不存在") : ReturnResult.ok(layout);
        } catch (Exception e) {
            log.error("获取布局详情失败: layoutId={}", layoutId, e);
            return ReturnResult.error("获取布局详情失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLayout>> getLayoutsByType(String layoutType) {
        if (StringUtils.isEmpty(layoutType)) {
            return ReturnResult.error("布局类型不能为空");
        }
        try {
            return ReturnResult.ok(baseMapper.selectByLayoutType(layoutType));
        } catch (Exception e) {
            log.error("根据类型获取布局列表失败: layoutType={}", layoutType, e);
            return ReturnResult.error("获取布局列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerLayout> createDefaultLayout(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.error("服务器ID不能为空");
        }
        try {
            MonitorSysGenServerLayout defaultLayout = new MonitorSysGenServerLayout();
            defaultLayout.setMonitorSysGenServerId(serverId);
            defaultLayout.setMonitorSysGenServerLayoutName("默认布局");
            defaultLayout.setMonitorSysGenServerLayoutConfig(Json.toJson(createDefaultLayoutConfig()));
            defaultLayout.setMonitorSysGenServerLayoutType("default");
            defaultLayout.setMonitorSysGenServerLayoutStatus(1);
            defaultLayout.setMonitorSysGenServerLayoutDefault(1);
            defaultLayout.setMonitorSysGenServerLayoutVersion(1);
            defaultLayout.setMonitorSysGenServerLayoutDescription("系统默认布局");
            return saveLayout(defaultLayout);
        } catch (Exception e) {
            log.error("创建默认布局失败: serverId={}", serverId, e);
            return ReturnResult.error("创建默认布局失败: " + e.getMessage());
        }
    }

    private Map<String, Object> createDefaultLayoutConfig() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        nodes.add(createLayoutItem(0, 0, 6, 8, "cpu", "CPU使用率"));
        nodes.add(createLayoutItem(6, 0, 6, 8, "memory", "内存使用率"));
        nodes.add(createLayoutItem(12, 0, 6, 8, "disk", "磁盘使用情况"));
        nodes.add(createLayoutItem(18, 0, 6, 8, "network", "网络流量"));
        nodes.add(createLayoutItem(0, 8, 12, 8, "system", "系统信息"));
        Map<String, Object> config = new HashMap<>();
        config.put("nodes", nodes);
        config.put("edges", new ArrayList<>());
        config.put("version", "1.0");
        config.put("createdAt", System.currentTimeMillis());
        return config;
    }

    private Map<String, Object> createLayoutItem(int x, int y, int w, int h, String type, String name) {
        Map<String, Object> item = new HashMap<>();
        item.put("x", x);
        item.put("y", y);
        item.put("w", w);
        item.put("h", h);
        item.put("i", String.format("%s_%d_%d", type, x, y));
        item.put("type", type);
        item.put("name", name);
        return item;
    }

    @Override
    public ReturnResult<Boolean> saveComponentChartConfig(Integer serverId, Map<String, Object> componentConfig) {
        if (serverId == null || componentConfig == null) {
            return ReturnResult.error("参数不能为空");
        }
        try {
            String componentId = (String) componentConfig.get("componentId");
            if (StringUtils.isEmpty(componentId)) {
                return ReturnResult.error("组件ID不能为空");
            }
            chartConfigCache.put(String.format("server_%d_component_%s", serverId, componentId), componentConfig);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("保存组件图表配置失败: serverId={}, componentConfig={}", serverId, componentConfig, e);
            return ReturnResult.error("保存组件图表配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getComponentChartConfig(Integer serverId, String componentId) {
        if (serverId == null || StringUtils.isEmpty(componentId)) {
            return ReturnResult.error("参数不能为空");
        }
        try {
            Map<String, Object> config = chartConfigCache.get(String.format("server_%d_component_%s", serverId, componentId));
            if (config == null) {
                config = createDefaultChartConfig(componentId);
            }
            return ReturnResult.ok(config);
        } catch (Exception e) {
            log.error("获取组件图表配置失败: serverId={}, componentId={}", serverId, componentId, e);
            return ReturnResult.error("获取组件图表配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchSaveComponentChartConfig(Integer serverId, List<Map<String, Object>> configs) {
        if (serverId == null || configs == null || configs.isEmpty()) {
            return ReturnResult.error("参数不能为空");
        }

        try {
            for (Map<String, Object> config : configs) {
                ReturnResult<Boolean> result = saveComponentChartConfig(serverId, config);
                if (!result.isOk()) {
                    log.warn("批量保存组件配置时出现错误: {}", result.getMsg());
                }
            }

            log.info("批量保存组件图表配置成功: serverId={}, count={}", serverId, configs.size());
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("批量保存组件图表配置失败: serverId={}, configs={}", serverId, configs, e);
            return ReturnResult.error("批量保存组件图表配置失败: " + e.getMessage());
        }
    }

    /**
     * 创建默认图表配置
     */
    private Map<String, Object> createDefaultChartConfig(String componentId) {
        Map<String, Object> config = new HashMap<>();
        config.put("componentId", componentId);
        config.put("title", "默认图表");
        config.put("type", "line");
        config.put("unit", "");
        config.put("mainColor", "#409EFF");
        config.put("bgColor", "rgba(64, 158, 255, 0.1)");
        config.put("showLegend", true);
        config.put("smooth", false);
        config.put("fill", false);
        config.put("stacked", false);
        config.put("yAxisMin", null);
        config.put("yAxisMax", null);
        config.put("showGrid", true);
        config.put("showLabel", false);

        // 默认阈值
        List<Map<String, Object>> thresholds = new ArrayList<>();
        Map<String, Object> threshold1 = new HashMap<>();
        threshold1.put("value", 0);
        threshold1.put("color", "#67C23A");
        threshold1.put("label", "正常");
        thresholds.add(threshold1);

        Map<String, Object> threshold2 = new HashMap<>();
        threshold2.put("value", 60);
        threshold2.put("color", "#E6A23C");
        threshold2.put("label", "警告");
        thresholds.add(threshold2);

        Map<String, Object> threshold3 = new HashMap<>();
        threshold3.put("value", 80);
        threshold3.put("color", "#F56C6C");
        threshold3.put("label", "危险");
        thresholds.add(threshold3);

        config.put("thresholds", thresholds);
        config.put("animation", true);
        config.put("animationDuration", 1000);
        config.put("animationDelay", 0);

        return config;
    }
}
