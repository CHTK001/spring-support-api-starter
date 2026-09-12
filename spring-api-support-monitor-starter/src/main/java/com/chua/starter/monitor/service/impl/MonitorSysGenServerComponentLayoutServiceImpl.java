package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponentLayout;
import com.chua.starter.monitor.mapper.MonitorSysGenServerComponentLayoutMapper;
import com.chua.starter.monitor.service.layout.MonitorSysGenServerComponentLayoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器组件布局服务实现，基于 MyBatis-Plus 提供组件布局的
 * 查询、位置更新、批量保存、启用禁用、统计、复制与重置等能力。
 *
 * @since 2026-07-28
 * @author CH
 */
@Slf4j
@Service
public class MonitorSysGenServerComponentLayoutServiceImpl
        extends ServiceImpl<MonitorSysGenServerComponentLayoutMapper, MonitorSysGenServerComponentLayout>
        implements MonitorSysGenServerComponentLayoutService {

    /**
     * 布局状态：启用
     */
    private static final int STATUS_ENABLED = 1;

    /**
     * 布局状态：禁用
     */
    private static final int STATUS_DISABLED = 0;

    /**
     * 组件布局默认 X 坐标
     */
    private static final int DEFAULT_X = 0;

    /**
     * 组件布局默认 Y 坐标
     */
    private static final int DEFAULT_Y = 0;

    /**
     * 组件布局默认宽度（栅格列数）
     */
    private static final int DEFAULT_WIDTH = 4;

    /**
     * 组件布局默认高度（栅格行数）
     */
    private static final int DEFAULT_HEIGHT = 3;

    /**
     * 组件布局默认层级
     */
    private static final int DEFAULT_Z_INDEX = 1;

    /**
     * 组件布局默认排序值
     */
    private static final int DEFAULT_SORT_ORDER = 0;

    @Override
    /** 列表by服务端 */
    public ReturnResult<List<MonitorSysGenServerComponentLayout>> listByServer(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        return ReturnResult.ok(list(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId)
                .orderByAsc(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentLayoutSortOrder)));
    }

    @Override
    /** 列表已启用by服务端 */
    public ReturnResult<List<MonitorSysGenServerComponentLayout>> listEnabledByServer(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        return ReturnResult.ok(list(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId)
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentLayoutStatus, STATUS_ENABLED)
                .orderByAsc(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentLayoutSortOrder)));
    }

    @Override
    /** 获取by组件 */
    public ReturnResult<MonitorSysGenServerComponentLayout> getByComponent(Integer serverId, Integer componentId) {
        if (serverId == null || componentId == null) {
            return ReturnResult.illegal("服务器ID和组件ID不能为空");
        }
        MonitorSysGenServerComponentLayout layout = getOne(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId)
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentId, componentId)
                .last("LIMIT 1"));
        if (layout == null) {
            return ReturnResult.illegal("组件布局不存在");
        }
        return ReturnResult.ok(layout);
    }

    @Override
    /** 保存组件layout */
    public ReturnResult<MonitorSysGenServerComponentLayout> saveComponentLayout(MonitorSysGenServerComponentLayout layout) {
        if (layout == null || layout.getMonitorSysGenServerId() == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        // 同一服务器同一组件已存在布局时改为更新
        if (layout.getMonitorSysGenServerComponentLayoutId() == null
                && layout.getMonitorSysGenServerComponentId() != null) {
            MonitorSysGenServerComponentLayout exist = getOne(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                    .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, layout.getMonitorSysGenServerId())
                    .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentId, layout.getMonitorSysGenServerComponentId())
                    .last("LIMIT 1"));
            if (exist != null) {
                layout.setMonitorSysGenServerComponentLayoutId(exist.getMonitorSysGenServerComponentLayoutId());
            }
        }
        applyDefaults(layout);
        saveOrUpdate(layout);
        return ReturnResult.ok(layout);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
    * 批量更新位置
    *
    * @param layouts layouts
    * @return batch更新位置的结果
     */
    public ReturnResult<Integer> batchUpdatePosition(List<MonitorSysGenServerComponentLayout> layouts) {
        if (layouts == null || layouts.isEmpty()) {
            return ReturnResult.ok(0);
        }
        int updated = 0;
        for (MonitorSysGenServerComponentLayout layout : layouts) {
            if (layout.getMonitorSysGenServerComponentLayoutId() == null) {
                continue;
            }
            MonitorSysGenServerComponentLayout patch = new MonitorSysGenServerComponentLayout();
            patch.setMonitorSysGenServerComponentLayoutId(layout.getMonitorSysGenServerComponentLayoutId());
            patch.setMonitorSysGenServerComponentLayoutX(layout.getMonitorSysGenServerComponentLayoutX());
            patch.setMonitorSysGenServerComponentLayoutY(layout.getMonitorSysGenServerComponentLayoutY());
            patch.setMonitorSysGenServerComponentLayoutW(layout.getMonitorSysGenServerComponentLayoutW());
            patch.setMonitorSysGenServerComponentLayoutH(layout.getMonitorSysGenServerComponentLayoutH());
            patch.setMonitorSysGenServerComponentLayoutZIndex(layout.getMonitorSysGenServerComponentLayoutZIndex());
            if (updateById(patch)) {
                updated++;
            }
        }
        log.info("[LAYOUT] 批量更新组件位置完成: 请求={}, 成功={}", layouts.size(), updated);
        return ReturnResult.ok(updated);
    }

    @Override
    /** 更新位置 */
    public ReturnResult<Boolean> updatePosition(Integer layoutId, MonitorSysGenServerComponentLayout layout) {
        if (layoutId == null || layout == null) {
            return ReturnResult.illegal("组件布局ID不能为空");
        }
        MonitorSysGenServerComponentLayout patch = new MonitorSysGenServerComponentLayout();
        patch.setMonitorSysGenServerComponentLayoutId(layoutId);
        patch.setMonitorSysGenServerComponentLayoutX(layout.getMonitorSysGenServerComponentLayoutX());
        patch.setMonitorSysGenServerComponentLayoutY(layout.getMonitorSysGenServerComponentLayoutY());
        patch.setMonitorSysGenServerComponentLayoutW(layout.getMonitorSysGenServerComponentLayoutW());
        patch.setMonitorSysGenServerComponentLayoutH(layout.getMonitorSysGenServerComponentLayoutH());
        patch.setMonitorSysGenServerComponentLayoutZIndex(layout.getMonitorSysGenServerComponentLayoutZIndex());
        return ReturnResult.ok(updateById(patch));
    }

    @Override
    /** 更新状态 */
    public ReturnResult<Boolean> updateStatus(Integer layoutId, Integer status) {
        if (layoutId == null || status == null) {
            return ReturnResult.illegal("组件布局ID和状态不能为空");
        }
        MonitorSysGenServerComponentLayout patch = new MonitorSysGenServerComponentLayout();
        patch.setMonitorSysGenServerComponentLayoutId(layoutId);
        patch.setMonitorSysGenServerComponentLayoutStatus(status);
        return ReturnResult.ok(updateById(patch));
    }

    @Override
    /** Statistics */
    public ReturnResult<Map<String, Object>> statistics(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        long total = count(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId));
        long enabled = count(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId)
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentLayoutStatus, STATUS_ENABLED));
        Map<String, Object> result = new HashMap<>(4);
        result.put("total", total);
        result.put("enabled", enabled);
        result.put("disabled", total - enabled);
        return ReturnResult.ok(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
    * 复制转为服务端
    *
    * @param sourceServerId 源服务端id
    * @param targetServerId Target服务端id
    * @return 副本转为服务端的结果
     */
    public ReturnResult<Integer> copyToServer(Integer sourceServerId, Integer targetServerId) {
        if (sourceServerId == null || targetServerId == null) {
            return ReturnResult.illegal("源服务器ID和目标服务器ID不能为空");
        }
        if (sourceServerId.equals(targetServerId)) {
            return ReturnResult.illegal("源服务器与目标服务器不能相同");
        }
        List<MonitorSysGenServerComponentLayout> sourceLayouts = list(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, sourceServerId));
        if (sourceLayouts.isEmpty()) {
            return ReturnResult.illegal("源服务器没有可复制的组件布局");
        }
        // 先清空目标服务器原有布局，再写入复制内容
        remove(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, targetServerId));
        for (MonitorSysGenServerComponentLayout layout : sourceLayouts) {
            layout.setMonitorSysGenServerComponentLayoutId(null);
            layout.setMonitorSysGenServerId(targetServerId);
            layout.setCreateTime(null);
            layout.setUpdateTime(null);
        }
        saveBatch(sourceLayouts);
        log.info("[LAYOUT] 复制组件布局: source={}, target={}, count={}", sourceServerId, targetServerId, sourceLayouts.size());
        return ReturnResult.ok(sourceLayouts.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 重置服务端
     *
     * @param serverId 服务端id
     * @return reset服务端的结果
     */
    public ReturnResult<Integer> resetServer(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        long total = count(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId));
        remove(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId));
        log.info("[LAYOUT] 重置服务器组件布局: serverId={}, 删除数量={}", serverId, total);
        return ReturnResult.ok((int) total);
    }

    @Override
    /** 删除by组件 */
    public ReturnResult<Boolean> deleteByComponent(Integer componentId) {
        if (componentId == null) {
            return ReturnResult.illegal("组件ID不能为空");
        }
        return ReturnResult.ok(remove(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentId, componentId)));
    }

    @Override
    /** 删除by服务端 */
    public ReturnResult<Boolean> deleteByServer(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        return ReturnResult.ok(remove(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId)));
    }

    @Override
    /** 保存chart配置 */
    public ReturnResult<Boolean> saveChartConfig(Integer serverId, Integer componentId, String chartConfig) {
        if (serverId == null || componentId == null) {
            return ReturnResult.illegal("服务器ID和组件ID不能为空");
        }
        MonitorSysGenServerComponentLayout layout = getOne(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId)
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentId, componentId)
                .last("LIMIT 1"));
        if (layout == null) {
            // 不存在布局记录时自动创建一条默认记录承载图表配置
            layout = new MonitorSysGenServerComponentLayout();
            layout.setMonitorSysGenServerId(serverId);
            layout.setMonitorSysGenServerComponentId(componentId);
            applyDefaults(layout);
        }
        layout.setMonitorSysGenServerComponentLayoutChartConfig(chartConfig);
        return ReturnResult.ok(saveOrUpdate(layout));
    }

    @Override
    /** 获取chart配置 */
    public ReturnResult<String> getChartConfig(Integer serverId, Integer componentId) {
        if (serverId == null || componentId == null) {
            return ReturnResult.illegal("服务器ID和组件ID不能为空");
        }
        MonitorSysGenServerComponentLayout layout = getOne(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId)
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentId, componentId)
                .last("LIMIT 1"));
        if (layout == null) {
            return ReturnResult.ok(null);
        }
        return ReturnResult.ok(layout.getMonitorSysGenServerComponentLayoutChartConfig());
    }

    /**
    * 为组件布局填充默认值：位置默认 (0,0)，尺寸默认 4x3，
    * 层级默认 1，可移动可缩放，状态默认启用，排序默认 0。
    *
    * @param layout 组件布局实体
     */
    private void applyDefaults(MonitorSysGenServerComponentLayout layout) {
        if (layout.getMonitorSysGenServerComponentLayoutX() == null) {
            layout.setMonitorSysGenServerComponentLayoutX(DEFAULT_X);
        }
        if (layout.getMonitorSysGenServerComponentLayoutY() == null) {
            layout.setMonitorSysGenServerComponentLayoutY(DEFAULT_Y);
        }
        if (layout.getMonitorSysGenServerComponentLayoutW() == null) {
            layout.setMonitorSysGenServerComponentLayoutW(DEFAULT_WIDTH);
        }
        if (layout.getMonitorSysGenServerComponentLayoutH() == null) {
            layout.setMonitorSysGenServerComponentLayoutH(DEFAULT_HEIGHT);
        }
        if (layout.getMonitorSysGenServerComponentLayoutZIndex() == null) {
            layout.setMonitorSysGenServerComponentLayoutZIndex(DEFAULT_Z_INDEX);
        }
        if (layout.getMonitorSysGenServerComponentLayoutMovable() == null) {
            layout.setMonitorSysGenServerComponentLayoutMovable(STATUS_ENABLED);
        }
        if (layout.getMonitorSysGenServerComponentLayoutResizable() == null) {
            layout.setMonitorSysGenServerComponentLayoutResizable(STATUS_ENABLED);
        }
        if (layout.getMonitorSysGenServerComponentLayoutStatus() == null) {
            layout.setMonitorSysGenServerComponentLayoutStatus(STATUS_ENABLED);
        }
        if (layout.getMonitorSysGenServerComponentLayoutSortOrder() == null) {
            layout.setMonitorSysGenServerComponentLayoutSortOrder(DEFAULT_SORT_ORDER);
        }
    }
}
