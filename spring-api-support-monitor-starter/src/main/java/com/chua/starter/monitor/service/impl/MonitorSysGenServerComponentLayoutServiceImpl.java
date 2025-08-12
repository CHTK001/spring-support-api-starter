package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponentLayout;
import com.chua.starter.monitor.mapper.MonitorSysGenServerComponentLayoutMapper;
import com.chua.starter.monitor.service.MonitorSysGenServerComponentLayoutService;
import com.chua.common.support.lang.code.ReturnResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器组件布局配置服务实现类
 *
 * @author CH
 * @since 2025/01/06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerComponentLayoutServiceImpl 
    extends ServiceImpl<MonitorSysGenServerComponentLayoutMapper, MonitorSysGenServerComponentLayout> 
    implements MonitorSysGenServerComponentLayoutService {

    private final MonitorSysGenServerComponentLayoutMapper layoutMapper;

    @Override
    public ReturnResult<List<MonitorSysGenServerComponentLayout>> getLayoutsByServerId(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }
            List<MonitorSysGenServerComponentLayout> layouts = layoutMapper.selectByServerId(serverId);
            return ReturnResult.success(layouts);
        } catch (Exception e) {
            log.error("获取服务器组件布局列表失败, serverId: {}", serverId, e);
            return ReturnResult.error("获取服务器组件布局列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerComponentLayout> getLayoutByServerIdAndComponentId(Integer serverId, Long componentId) {
        try {
            if (serverId == null || componentId == null) {
                return ReturnResult.error("服务器ID和组件ID不能为空");
            }
            MonitorSysGenServerComponentLayout layout = layoutMapper.selectByServerIdAndComponentId(serverId, componentId);
            return ReturnResult.success(layout);
        } catch (Exception e) {
            log.error("获取组件布局配置失败, serverId: {}, componentId: {}", serverId, componentId, e);
            return ReturnResult.error("获取组件布局配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerComponentLayout>> getEnabledLayoutsByServerId(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }
            List<MonitorSysGenServerComponentLayout> layouts = layoutMapper.selectEnabledByServerId(serverId);
            return ReturnResult.success(layouts);
        } catch (Exception e) {
            log.error("获取启用的服务器组件布局列表失败, serverId: {}", serverId, e);
            return ReturnResult.error("获取启用的服务器组件布局列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerComponentLayout> saveLayout(MonitorSysGenServerComponentLayout layout) {
        try {
            if (layout == null) {
                return ReturnResult.error("布局配置不能为空");
            }
            
            // 设置默认值
            if (layout.getMonitorSysGenServerComponentLayoutStatus() == null) {
                layout.setMonitorSysGenServerComponentLayoutStatus(1);
            }
            if (layout.getMonitorSysGenServerComponentLayoutMovable() == null) {
                layout.setMonitorSysGenServerComponentLayoutMovable(true);
            }
            if (layout.getMonitorSysGenServerComponentLayoutResizable() == null) {
                layout.setMonitorSysGenServerComponentLayoutResizable(true);
            }
            if (layout.getMonitorSysGenServerComponentLayoutZIndex() == null) {
                layout.setMonitorSysGenServerComponentLayoutZIndex(1);
            }
            
            layout.setCreateTime(LocalDateTime.now());
            layout.setUpdateTime(LocalDateTime.now());
            
            boolean success = save(layout);
            if (success) {
                return ReturnResult.success(layout);
            } else {
                return ReturnResult.error("保存布局配置失败");
            }
        } catch (Exception e) {
            log.error("保存组件布局配置失败", e);
            return ReturnResult.error("保存组件布局配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateLayout(MonitorSysGenServerComponentLayout layout) {
        try {
            if (layout == null || layout.getMonitorSysGenServerComponentLayoutId() == null) {
                return ReturnResult.error("布局配置或布局ID不能为空");
            }
            
            layout.setUpdateTime(LocalDateTime.now());
            boolean success = updateById(layout);
            return ReturnResult.success(success);
        } catch (Exception e) {
            log.error("更新组件布局配置失败", e);
            return ReturnResult.error("更新组件布局配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteLayout(Long layoutId) {
        try {
            if (layoutId == null) {
                return ReturnResult.error("布局ID不能为空");
            }
            boolean success = removeById(layoutId);
            return ReturnResult.success(success);
        } catch (Exception e) {
            log.error("删除组件布局配置失败, layoutId: {}", layoutId, e);
            return ReturnResult.error("删除组件布局配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteLayoutByComponentId(Long componentId) {
        try {
            if (componentId == null) {
                return ReturnResult.error("组件ID不能为空");
            }
            int count = layoutMapper.deleteByComponentId(componentId);
            return ReturnResult.success(count > 0);
        } catch (Exception e) {
            log.error("根据组件ID删除布局配置失败, componentId: {}", componentId, e);
            return ReturnResult.error("根据组件ID删除布局配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteLayoutsByServerId(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }
            int count = layoutMapper.deleteByServerId(serverId);
            return ReturnResult.success(count > 0);
        } catch (Exception e) {
            log.error("根据服务器ID删除所有布局配置失败, serverId: {}", serverId, e);
            return ReturnResult.error("根据服务器ID删除所有布局配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchUpdatePosition(Integer serverId, List<MonitorSysGenServerComponentLayout> layouts) {
        try {
            if (layouts == null || layouts.isEmpty()) {
                if(null == serverId) {
                    return ReturnResult.error("布局列表不能为空");
                }

                layoutMapper.deleteByServerId(serverId);
                return ReturnResult.ok();
            }
            
            // 设置更新时间
            layouts.forEach(layout -> layout.setUpdateTime(LocalDateTime.now()));
            
            int count = layoutMapper.batchUpdatePosition(layouts);
            return ReturnResult.success(count > 0);
        } catch (Exception e) {
            log.error("批量更新组件布局位置失败", e);
            return ReturnResult.error("批量更新组件布局位置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerComponentLayout> createLayout(Integer serverId, Long componentId, 
                                                                         Integer x, Integer y, Integer w, Integer h) {
        try {
            if (serverId == null || componentId == null) {
                return ReturnResult.error("服务器ID和组件ID不能为空");
            }
            
            // 检查是否已存在
            MonitorSysGenServerComponentLayout existing = layoutMapper.selectByServerIdAndComponentId(serverId, componentId);
            if (existing != null) {
                return ReturnResult.error("该服务器上已存在此组件的布局配置");
            }
            
            MonitorSysGenServerComponentLayout layout = new MonitorSysGenServerComponentLayout();
            layout.setMonitorSysGenServerId(serverId);
            layout.setMonitorSysGenServerComponentId(componentId);
            layout.setMonitorSysGenServerComponentLayoutX(x != null ? x : 0);
            layout.setMonitorSysGenServerComponentLayoutY(y != null ? y : 0);
            layout.setMonitorSysGenServerComponentLayoutW(w != null ? w : 4);
            layout.setMonitorSysGenServerComponentLayoutH(h != null ? h : 3);
            layout.setMonitorSysGenServerComponentLayoutZIndex(1);
            layout.setMonitorSysGenServerComponentLayoutMovable(true);
            layout.setMonitorSysGenServerComponentLayoutResizable(true);
            layout.setMonitorSysGenServerComponentLayoutStatus(1);
            layout.setMonitorSysGenServerComponentLayoutSortOrder(0);
            
            return saveLayout(layout);
        } catch (Exception e) {
            log.error("创建组件布局配置失败", e);
            return ReturnResult.error("创建组件布局配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateLayoutPosition(Long layoutId, Integer x, Integer y, Integer w, Integer h) {
        try {
            if (layoutId == null) {
                return ReturnResult.error("布局ID不能为空");
            }
            
            MonitorSysGenServerComponentLayout layout = getById(layoutId);
            if (layout == null) {
                return ReturnResult.error("布局配置不存在");
            }
            
            if (x != null) layout.setMonitorSysGenServerComponentLayoutX(x);
            if (y != null) layout.setMonitorSysGenServerComponentLayoutY(y);
            if (w != null) layout.setMonitorSysGenServerComponentLayoutW(w);
            if (h != null) layout.setMonitorSysGenServerComponentLayoutH(h);
            
            return updateLayout(layout);
        } catch (Exception e) {
            log.error("更新组件布局位置失败", e);
            return ReturnResult.error("更新组件布局位置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> setLayoutEnabled(Long layoutId, Boolean enabled) {
        try {
            if (layoutId == null) {
                return ReturnResult.error("布局ID不能为空");
            }
            
            MonitorSysGenServerComponentLayout layout = getById(layoutId);
            if (layout == null) {
                return ReturnResult.error("布局配置不存在");
            }
            
            layout.setMonitorSysGenServerComponentLayoutStatus(enabled != null && enabled ? 1 : 0);
            return updateLayout(layout);
        } catch (Exception e) {
            log.error("设置组件布局状态失败", e);
            return ReturnResult.error("设置组件布局状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getLayoutStatistics(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }
            List<Map<String, Object>> statistics = layoutMapper.getLayoutStatistics(serverId);
            if (statistics != null && !statistics.isEmpty()) {
                return ReturnResult.success(statistics.get(0));
            }
            return ReturnResult.success(Map.of());
        } catch (Exception e) {
            log.error("获取服务器组件布局统计信息失败, serverId: {}", serverId, e);
            return ReturnResult.error("获取服务器组件布局统计信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> copyLayoutsToServer(Integer sourceServerId, Integer targetServerId) {
        try {
            if (sourceServerId == null || targetServerId == null) {
                return ReturnResult.error("源服务器ID和目标服务器ID不能为空");
            }
            
            // 获取源服务器的布局配置
            List<MonitorSysGenServerComponentLayout> sourceLayouts = layoutMapper.selectByServerId(sourceServerId);
            if (sourceLayouts.isEmpty()) {
                return ReturnResult.error("源服务器没有布局配置");
            }
            
            // 删除目标服务器的现有布局配置
            layoutMapper.deleteByServerId(targetServerId);
            
            // 复制布局配置到目标服务器
            for (MonitorSysGenServerComponentLayout sourceLayout : sourceLayouts) {
                MonitorSysGenServerComponentLayout newLayout = new MonitorSysGenServerComponentLayout();
                newLayout.setMonitorSysGenServerId(targetServerId);
                newLayout.setMonitorSysGenServerComponentId(sourceLayout.getMonitorSysGenServerComponentId());
                newLayout.setMonitorSysGenServerComponentLayoutX(sourceLayout.getMonitorSysGenServerComponentLayoutX());
                newLayout.setMonitorSysGenServerComponentLayoutY(sourceLayout.getMonitorSysGenServerComponentLayoutY());
                newLayout.setMonitorSysGenServerComponentLayoutW(sourceLayout.getMonitorSysGenServerComponentLayoutW());
                newLayout.setMonitorSysGenServerComponentLayoutH(sourceLayout.getMonitorSysGenServerComponentLayoutH());
                newLayout.setMonitorSysGenServerComponentLayoutZIndex(sourceLayout.getMonitorSysGenServerComponentLayoutZIndex());
                newLayout.setMonitorSysGenServerComponentLayoutMovable(sourceLayout.getMonitorSysGenServerComponentLayoutMovable());
                newLayout.setMonitorSysGenServerComponentLayoutResizable(sourceLayout.getMonitorSysGenServerComponentLayoutResizable());
                newLayout.setMonitorSysGenServerComponentLayoutStatus(sourceLayout.getMonitorSysGenServerComponentLayoutStatus());
                newLayout.setMonitorSysGenServerComponentLayoutSortOrder(sourceLayout.getMonitorSysGenServerComponentLayoutSortOrder());
                newLayout.setCreateTime(LocalDateTime.now());
                newLayout.setUpdateTime(LocalDateTime.now());
                
                save(newLayout);
            }
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("复制布局配置到其他服务器失败, sourceServerId: {}, targetServerId: {}", sourceServerId, targetServerId, e);
            return ReturnResult.error("复制布局配置到其他服务器失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> resetServerLayouts(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }
            
            int count = layoutMapper.deleteByServerId(serverId);
            log.info("重置服务器布局配置完成, serverId: {}, 删除数量: {}", serverId, count);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("重置服务器布局配置失败, serverId: {}", serverId, e);
            return ReturnResult.error("重置服务器布局配置失败: " + e.getMessage());
        }
    }
}
