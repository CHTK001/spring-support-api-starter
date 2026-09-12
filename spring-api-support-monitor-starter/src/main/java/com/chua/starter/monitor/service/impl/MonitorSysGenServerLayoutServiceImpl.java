package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponentLayout;
import com.chua.starter.monitor.entity.MonitorSysGenServerLayout;
import com.chua.starter.monitor.mapper.MonitorSysGenServerLayoutMapper;
import com.chua.starter.monitor.service.layout.MonitorSysGenServerComponentLayoutService;
import com.chua.starter.monitor.service.layout.MonitorSysGenServerLayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器布局配置服务实现，负责布局的增删改查、默认布局管理、
 * 从组件生成布局、应用布局到组件以及跨服务器复制布局。
   * 布局配置以 JSON 形式存储，结构包含 节点 / edges / 版本 / generatedat。
 *
 * @since 2026-07-28
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerLayoutServiceImpl
        extends ServiceImpl<MonitorSysGenServerLayoutMapper, MonitorSysGenServerLayout>
        implements MonitorSysGenServerLayoutService {

    /**
     * 组件布局服务，用于生成布局与应用布局时读写组件位置
     */
    private final MonitorSysGenServerComponentLayoutService componentLayoutService;

    /**
     * 布局类型：默认布局
     */
    private static final String TYPE_DEFAULT = "default";

    /**
     * 布局类型：从组件生成
     */
    private static final String TYPE_GENERATED = "generated";

    /**
     * 布局类型：从其它服务器复制
     */
    private static final String TYPE_COPIED = "copied";

    /**
     * 布局类型：自定义
     */
    private static final String TYPE_CUSTOM = "custom";

    /**
     * 布局版本初始值
     */
    private static final int VERSION_INIT = 1;

    /**
     * 布局排序初始值
     */
    private static final int SORT_INIT = 0;

    /**
     * 默认布局节点层级
     */
    private static final int DEFAULT_NODE_Z_INDEX = 1;

    /**
     * 默认标记：是默认布局
     */
    private static final int DEFAULT_YES = 1;

    /**
     * 默认标记：非默认布局
     */
    private static final int DEFAULT_NO = 0;

    /**
     * 布局状态：启用
     */
    private static final int STATUS_ENABLED = 1;

    /**
     * 默认布局包含的基础监控节点（编码与名称）
     */
    private static final String[][] DEFAULT_NODES = {
            {"cpu", "CPU"},
            {"memory", "内存"},
            {"disk", "磁盘"},
            {"network", "网络"},
            {"sysinfo", "系统信息"}
    };

    /**
     * 默认节点宽度（栅格列数）
     */
    private static final int DEFAULT_NODE_WIDTH = 4;

    /**
     * 默认节点高度（栅格行数）
     */
    private static final int DEFAULT_NODE_HEIGHT = 3;

    /**
     * 每行放置的节点数量
     */
    private static final int NODES_PER_ROW = 3;

    /**
     * 生成时间格式化器，输出 ISO 风格时间字符串
     */
    private static final DateTimeFormatter GENERATED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    /** 列表by服务端id */
    public ReturnResult<List<MonitorSysGenServerLayout>> listByServerId(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        return ReturnResult.ok(list(Wrappers.<MonitorSysGenServerLayout>lambdaQuery()
                .eq(MonitorSysGenServerLayout::getMonitorSysGenServerId, serverId)
                .orderByAsc(MonitorSysGenServerLayout::getMonitorSysGenServerLayoutSort)
                .orderByDesc(MonitorSysGenServerLayout::getUpdateTime)));
    }

    @Override
    /** 获取默认layout */
    public ReturnResult<MonitorSysGenServerLayout> getDefaultLayout(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        MonitorSysGenServerLayout layout = getOne(Wrappers.<MonitorSysGenServerLayout>lambdaQuery()
                .eq(MonitorSysGenServerLayout::getMonitorSysGenServerId, serverId)
                .eq(MonitorSysGenServerLayout::getMonitorSysGenServerLayoutDefault, DEFAULT_YES)
                .last("LIMIT 1"));
        if (layout != null) {
            return ReturnResult.ok(layout);
        }
        // 不存在默认布局时自动创建
        return createDefaultLayout(serverId);
    }

    @Override
    /** 保存Layout */
    public ReturnResult<MonitorSysGenServerLayout> saveLayout(MonitorSysGenServerLayout layout) {
        if (layout == null || layout.getMonitorSysGenServerId() == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        if (StringUtils.isEmpty(layout.getMonitorSysGenServerLayoutName())) {
            return ReturnResult.illegal("布局名称不能为空");
        }
        applyDefaults(layout);
        save(layout);
        return ReturnResult.ok(layout);
    }

    @Override
    /** 更新Layout */
    public ReturnResult<MonitorSysGenServerLayout> updateLayout(MonitorSysGenServerLayout layout) {
        if (layout == null || layout.getMonitorSysGenServerLayoutId() == null) {
            return ReturnResult.illegal("布局ID不能为空");
        }
        MonitorSysGenServerLayout exist = getById(layout.getMonitorSysGenServerLayoutId());
        if (exist == null) {
            return ReturnResult.illegal("布局不存在");
        }
        // 每次更新版本号自动递增
        Integer version = exist.getMonitorSysGenServerLayoutVersion();
        layout.setMonitorSysGenServerLayoutVersion(version == null ? VERSION_INIT : version + 1);
        updateById(layout);
        return ReturnResult.ok(getById(layout.getMonitorSysGenServerLayoutId()));
    }

    @Override
    /** 删除Layout */
    public ReturnResult<Boolean> deleteLayout(Integer layoutId) {
        if (layoutId == null) {
            return ReturnResult.illegal("布局ID不能为空");
        }
        MonitorSysGenServerLayout layout = getById(layoutId);
        if (layout == null) {
            return ReturnResult.illegal("布局不存在");
        }
        if (layout.getMonitorSysGenServerLayoutDefault() != null
                && layout.getMonitorSysGenServerLayoutDefault() == DEFAULT_YES) {
            return ReturnResult.illegal("默认布局不允许删除，请先设置其它布局为默认");
        }
        return ReturnResult.ok(removeById(layoutId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 设置默认layout
     *
     * @param serverId 服务端id
     * @param layoutId layoutId
     * @return 设置默认layout的结果
     */
    public ReturnResult<Boolean> setDefaultLayout(Integer serverId, Integer layoutId) {
        if (serverId == null || layoutId == null) {
            return ReturnResult.illegal("服务器ID和布局ID不能为空");
        }
        MonitorSysGenServerLayout layout = getById(layoutId);
        if (layout == null || !serverId.equals(layout.getMonitorSysGenServerId())) {
            return ReturnResult.illegal("布局不存在或不属于该服务器");
        }
        // 取消该服务器其它布局的默认标记
        MonitorSysGenServerLayout clear = new MonitorSysGenServerLayout();
        clear.setMonitorSysGenServerLayoutDefault(DEFAULT_NO);
        update(clear, Wrappers.<MonitorSysGenServerLayout>lambdaUpdate()
                .eq(MonitorSysGenServerLayout::getMonitorSysGenServerId, serverId)
                .eq(MonitorSysGenServerLayout::getMonitorSysGenServerLayoutDefault, DEFAULT_YES));
        // 设置新的默认布局
        MonitorSysGenServerLayout patch = new MonitorSysGenServerLayout();
        patch.setMonitorSysGenServerLayoutId(layoutId);
        patch.setMonitorSysGenServerLayoutDefault(DEFAULT_YES);
        return ReturnResult.ok(updateById(patch));
    }

    @Override
    /** generate从组件 */
    public ReturnResult<MonitorSysGenServerLayout> generateFromComponents(Integer serverId, String layoutName) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        ReturnResult<List<MonitorSysGenServerComponentLayout>> componentsResult =
                componentLayoutService.listEnabledByServer(serverId);
        List<MonitorSysGenServerComponentLayout> components = componentsResult.getData();
        if (components == null || components.isEmpty()) {
            return ReturnResult.illegal("该服务器没有启用的组件布局，无法生成");
        }
        // 将组件布局转换为布局节点
        List<Map<String, Object>> nodes = new ArrayList<>(components.size());
        for (MonitorSysGenServerComponentLayout component : components) {
            Map<String, Object> node = new LinkedHashMap<>(8);
            node.put("componentId", component.getMonitorSysGenServerComponentId());
            node.put("x", component.getMonitorSysGenServerComponentLayoutX());
            node.put("y", component.getMonitorSysGenServerComponentLayoutY());
            node.put("w", component.getMonitorSysGenServerComponentLayoutW());
            node.put("h", component.getMonitorSysGenServerComponentLayoutH());
            node.put("zIndex", component.getMonitorSysGenServerComponentLayoutZIndex());
            nodes.add(node);
        }
        String name = StringUtils.isEmpty(layoutName)
                ? "生成布局-" + LocalDateTime.now().format(GENERATED_AT_FORMATTER)
                : layoutName;
        MonitorSysGenServerLayout layout = new MonitorSysGenServerLayout();
        layout.setMonitorSysGenServerId(serverId);
        layout.setMonitorSysGenServerLayoutName(name);
        layout.setMonitorSysGenServerLayoutType(TYPE_GENERATED);
        layout.setMonitorSysGenServerLayoutConfig(buildConfigJson(nodes));
        layout.setMonitorSysGenServerLayoutDescription("由服务器组件配置自动生成");
        applyDefaults(layout);
        save(layout);
        log.info("[LAYOUT] 从组件生成布局: serverId={}, layoutId={}, 节点数={}",
                serverId, layout.getMonitorSysGenServerLayoutId(), nodes.size());
        return ReturnResult.ok(layout);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 应用转为组件
     *
     * @param serverId 服务端id
     * @param layoutId layoutId
     * @return apply转为组件的结果
     */
    public ReturnResult<Integer> applyToComponents(Integer serverId, Integer layoutId) {
        if (serverId == null || layoutId == null) {
            return ReturnResult.illegal("服务器ID和布局ID不能为空");
        }
        MonitorSysGenServerLayout layout = getById(layoutId);
        if (layout == null || StringUtils.isEmpty(layout.getMonitorSysGenServerLayoutConfig())) {
            return ReturnResult.illegal("布局不存在或布局配置为空");
        }
        Map<String, Object> config = Json.fromJson(layout.getMonitorSysGenServerLayoutConfig());
        Object nodesObject = config.get("nodes");
        if (!(nodesObject instanceof List<?> nodes) || nodes.isEmpty()) {
            return ReturnResult.illegal("布局配置中没有节点信息");
        }
        int applied = 0;
        for (Object nodeObject : nodes) {
            if (!(nodeObject instanceof Map<?, ?> node)) {
                continue;
            }
            Integer componentId = toInteger(node.get("componentId"));
            if (componentId == null) {
                continue;
            }
            MonitorSysGenServerComponentLayout patch = new MonitorSysGenServerComponentLayout();
            patch.setMonitorSysGenServerId(serverId);
            patch.setMonitorSysGenServerComponentId(componentId);
            patch.setMonitorSysGenServerComponentLayoutX(toInteger(node.get("x")));
            patch.setMonitorSysGenServerComponentLayoutY(toInteger(node.get("y")));
            patch.setMonitorSysGenServerComponentLayoutW(toInteger(node.get("w")));
            patch.setMonitorSysGenServerComponentLayoutH(toInteger(node.get("h")));
            patch.setMonitorSysGenServerComponentLayoutZIndex(toInteger(node.get("zIndex")));
            componentLayoutService.saveComponentLayout(patch);
            applied++;
        }
        log.info("[LAYOUT] 应用布局到组件: serverId={}, layoutId={}, 应用数={}", serverId, layoutId, applied);
        return ReturnResult.ok(applied);
    }

    @Override
    /** 复制Layout */
    public ReturnResult<MonitorSysGenServerLayout> copyLayout(Integer sourceLayoutId, Integer targetServerId, String layoutName) {
        if (sourceLayoutId == null || targetServerId == null) {
            return ReturnResult.illegal("源布局ID和目标服务器ID不能为空");
        }
        MonitorSysGenServerLayout source = getById(sourceLayoutId);
        if (source == null) {
            return ReturnResult.illegal("源布局不存在");
        }
        MonitorSysGenServerLayout copy = new MonitorSysGenServerLayout();
        copy.setMonitorSysGenServerId(targetServerId);
        copy.setMonitorSysGenServerLayoutName(StringUtils.isEmpty(layoutName)
                ? source.getMonitorSysGenServerLayoutName() + "-副本"
                : layoutName);
        copy.setMonitorSysGenServerLayoutConfig(source.getMonitorSysGenServerLayoutConfig());
        copy.setMonitorSysGenServerLayoutType(TYPE_COPIED);
        copy.setMonitorSysGenServerLayoutDescription("复制自布局#" + sourceLayoutId);
        applyDefaults(copy);
        save(copy);
        log.info("[LAYOUT] 复制布局: sourceLayoutId={}, targetServerId={}, newLayoutId={}",
                sourceLayoutId, targetServerId, copy.getMonitorSysGenServerLayoutId());
        return ReturnResult.ok(copy);
    }

    @Override
    /** 获取Detail */
    public ReturnResult<MonitorSysGenServerLayout> getDetail(Integer layoutId) {
        if (layoutId == null) {
            return ReturnResult.illegal("布局ID不能为空");
        }
        MonitorSysGenServerLayout layout = getById(layoutId);
        if (layout == null) {
            return ReturnResult.illegal("布局不存在");
        }
        return ReturnResult.ok(layout);
    }

    @Override
    /** 列表by类型 */
    public ReturnResult<List<MonitorSysGenServerLayout>> listByType(String layoutType) {
        if (StringUtils.isEmpty(layoutType)) {
            return ReturnResult.illegal("布局类型不能为空");
        }
        return ReturnResult.ok(list(Wrappers.<MonitorSysGenServerLayout>lambdaQuery()
                .eq(MonitorSysGenServerLayout::getMonitorSysGenServerLayoutType, layoutType)
                .orderByDesc(MonitorSysGenServerLayout::getUpdateTime)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 创建默认layout
     *
     * @param serverId 服务端id
     * @return 创建默认layout的结果
     */
    public ReturnResult<MonitorSysGenServerLayout> createDefaultLayout(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        // 构建默认节点：CPU、内存、磁盘、网络、系统信息
        List<Map<String, Object>> nodes = new ArrayList<>(DEFAULT_NODES.length);
        for (int index = 0; index < DEFAULT_NODES.length; index++) {
            Map<String, Object> node = new LinkedHashMap<>(8);
            node.put("code", DEFAULT_NODES[index][0]);
            node.put("name", DEFAULT_NODES[index][1]);
            node.put("x", (index % NODES_PER_ROW) * DEFAULT_NODE_WIDTH);
            node.put("y", (index / NODES_PER_ROW) * DEFAULT_NODE_HEIGHT);
            node.put("w", DEFAULT_NODE_WIDTH);
            node.put("h", DEFAULT_NODE_HEIGHT);
            node.put("zIndex", DEFAULT_NODE_Z_INDEX);
            nodes.add(node);
        }
        MonitorSysGenServerLayout layout = new MonitorSysGenServerLayout();
        layout.setMonitorSysGenServerId(serverId);
        layout.setMonitorSysGenServerLayoutName("默认布局");
        layout.setMonitorSysGenServerLayoutType(TYPE_DEFAULT);
        layout.setMonitorSysGenServerLayoutConfig(buildConfigJson(nodes));
        layout.setMonitorSysGenServerLayoutDefault(DEFAULT_YES);
        layout.setMonitorSysGenServerLayoutDescription("系统自动创建的默认布局，包含 CPU、内存、磁盘、网络、系统信息基础节点");
        applyDefaults(layout);
 // 保证默认标记不被 apply默认 覆盖
        layout.setMonitorSysGenServerLayoutDefault(DEFAULT_YES);
        save(layout);
        log.info("[LAYOUT] 创建默认布局: serverId={}, layoutId={}", serverId, layout.getMonitorSysGenServerLayoutId());
        return ReturnResult.ok(layout);
    }

    /**
      * 构建布局配置 JSON 字符串，结构包含 节点 / edges / 版本 / generatedat。
     *
     * @param nodes 布局节点列表
     * @return 布局配置 JSON 字符串
     */
    private String buildConfigJson(List<Map<String, Object>> nodes) {
        Map<String, Object> config = new HashMap<>(8);
        config.put("nodes", nodes);
        config.put("edges", new ArrayList<>(0));
        config.put("version", 1);
        config.put("generatedAt", LocalDateTime.now().format(GENERATED_AT_FORMATTER));
        return Json.toJson(config);
    }

    /**
      * 为布局实体填充默认值：非默认布局、启用状态、版本 1、排序 0、类型 习俗。
     *
     * @param layout 布局实体
     */
    private void applyDefaults(MonitorSysGenServerLayout layout) {
        if (layout.getMonitorSysGenServerLayoutDefault() == null) {
            layout.setMonitorSysGenServerLayoutDefault(DEFAULT_NO);
        }
        if (layout.getMonitorSysGenServerLayoutStatus() == null) {
            layout.setMonitorSysGenServerLayoutStatus(STATUS_ENABLED);
        }
        if (layout.getMonitorSysGenServerLayoutVersion() == null) {
            layout.setMonitorSysGenServerLayoutVersion(VERSION_INIT);
        }
        if (layout.getMonitorSysGenServerLayoutSort() == null) {
            layout.setMonitorSysGenServerLayoutSort(SORT_INIT);
        }
        if (StringUtils.isEmpty(layout.getMonitorSysGenServerLayoutType())) {
            layout.setMonitorSysGenServerLayoutType(TYPE_CUSTOM);
        }
    }

    /**
      * 将任意对象安全地转换为 Integer，无法转换时返回 空。
     *
     * @param value 待转换对象
     * @return Integer 值或 空
     */
    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
