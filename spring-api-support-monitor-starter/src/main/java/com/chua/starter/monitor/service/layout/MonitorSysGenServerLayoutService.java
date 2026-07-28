package com.chua.starter.monitor.service.layout;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerLayout;

import java.util.List;

/**
 * 服务器布局配置服务接口，提供布局的增删改查、默认布局管理、
 * 从组件生成布局、应用布局到组件以及跨服务器复制布局等能力。
 *
 * @author CH
 * @since 2026-07-28
 */
public interface MonitorSysGenServerLayoutService extends IService<MonitorSysGenServerLayout> {

    /**
     * 查询指定服务器的全部布局列表，按排序值与更新时间排序。
     *
     * @param serverId 服务器ID
     * @return 布局列表
     */
    ReturnResult<List<MonitorSysGenServerLayout>> listByServerId(Integer serverId);

    /**
     * 获取指定服务器的默认布局；若不存在默认布局则自动创建一份并返回。
     *
     * @param serverId 服务器ID
     * @return 默认布局
     */
    ReturnResult<MonitorSysGenServerLayout> getDefaultLayout(Integer serverId);

    /**
     * 保存布局配置，自动初始化版本号、状态等默认值。
     *
     * @param layout 布局实体
     * @return 保存后的布局（含主键）
     */
    ReturnResult<MonitorSysGenServerLayout> saveLayout(MonitorSysGenServerLayout layout);

    /**
     * 更新布局配置，每次更新版本号自动递增。
     *
     * @param layout 布局实体（必须包含布局ID）
     * @return 更新后的布局
     */
    ReturnResult<MonitorSysGenServerLayout> updateLayout(MonitorSysGenServerLayout layout);

    /**
     * 删除布局配置，默认布局不允许删除。
     *
     * @param layoutId 布局ID
     * @return 是否删除成功
     */
    ReturnResult<Boolean> deleteLayout(Integer layoutId);

    /**
     * 设置指定布局为服务器默认布局，同时取消该服务器其它布局的默认标记。
     *
     * @param serverId 服务器ID
     * @param layoutId 布局ID
     * @return 是否设置成功
     */
    ReturnResult<Boolean> setDefaultLayout(Integer serverId, Integer layoutId);

    /**
     * 根据服务器当前启用的组件布局生成一份新的布局配置（类型为 generated）。
     *
     * @param serverId   服务器ID
     * @param layoutName 布局名称，为空时自动生成
     * @return 生成的布局
     */
    ReturnResult<MonitorSysGenServerLayout> generateFromComponents(Integer serverId, String layoutName);

    /**
     * 将布局配置中的节点位置应用到服务器的组件布局记录上。
     *
     * @param serverId 服务器ID
     * @param layoutId 布局ID
     * @return 应用成功的组件数量
     */
    ReturnResult<Integer> applyToComponents(Integer serverId, Integer layoutId);

    /**
     * 复制布局到其它服务器（类型为 copied）。
     *
     * @param sourceLayoutId 源布局ID
     * @param targetServerId 目标服务器ID
     * @param layoutName     新布局名称，为空时使用「源名称-副本」
     * @return 复制生成的新布局
     */
    ReturnResult<MonitorSysGenServerLayout> copyLayout(Integer sourceLayoutId, Integer targetServerId, String layoutName);

    /**
     * 获取布局详情。
     *
     * @param layoutId 布局ID
     * @return 布局详情
     */
    ReturnResult<MonitorSysGenServerLayout> getDetail(Integer layoutId);

    /**
     * 根据布局类型查询布局列表。
     *
     * @param layoutType 布局类型（default/generated/copied/custom）
     * @return 布局列表
     */
    ReturnResult<List<MonitorSysGenServerLayout>> listByType(String layoutType);

    /**
     * 为服务器创建默认布局，默认包含 CPU、内存、磁盘、网络、系统信息等基础节点。
     *
     * @param serverId 服务器ID
     * @return 创建的默认布局
     */
    ReturnResult<MonitorSysGenServerLayout> createDefaultLayout(Integer serverId);
}
