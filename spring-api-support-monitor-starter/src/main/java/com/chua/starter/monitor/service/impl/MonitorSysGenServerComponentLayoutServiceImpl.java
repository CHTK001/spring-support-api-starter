package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
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
 * @author CH
 * @since 2026-07-28
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

    @Override
    public ReturnResult<List<MonitorSysGenServerComponentLayout>> listByServer(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        return ReturnResult.ok(list(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId)
                .orderByAsc(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentLayoutSortOrder)));
    }

    @Override
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
    public ReturnResult<Boolean> deleteByComponent(Integer componentId) {
        if (componentId == null) {
            return ReturnResult.illegal("组件ID不能为空");
        }
        return ReturnResult.ok(remove(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerComponentId, componentId)));
    }

    @Override
    public ReturnResult<Boolean> deleteByServer(Integer serverId) {
        if (serverId == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        return ReturnResult.ok(remove(Wrappers.<MonitorSysGenServerComponentLayout>lambdaQuery()
                .eq(MonitorSysGenServerComponentLayout::getMonitorSysGenServerId, serverId)));
    }

    @Override
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
            layout.setMonitorSysGenServerComponentLayoutX(0);
        }
        if (layout.getMonitorSysGenServerComponentLayoutY() == null) {
            layout.setMonitorSysGenServerComponentLayoutY(0);
        }
        if (layout.getMonitorSysGenServerComponentLayoutW() == null) {
            layout.setMonitorSysGenServerComponentLayoutW(4);
        }
        if (layout.getMonitorSysGenServerComponentLayoutH() == null) {
            layout.setMonitorSysGenServerComponentLayoutH(3);
        }
        if (layout.getMonitorSysGenServerComponentLayoutZIndex() == null) {
            layout.setMonitorSysGenServerComponentLayoutZIndex(1);
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
            layout.setMonitorSysGenServerComponentLayoutSortOrder(0);
        }
    }
}
