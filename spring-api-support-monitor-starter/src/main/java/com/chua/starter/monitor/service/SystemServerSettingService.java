package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.SpiOption;
import com.chua.starter.monitor.entity.SystemServerSetting;
import com.chua.starter.monitor.pojo.ServerSettingOrderRequest;

import java.util.List;
import java.util.Map;

/**
 * 系统服务器配置表服务接口
 *
 * @author CH
 * @since 2025/01/07
 */
public interface SystemServerSettingService extends IService<SystemServerSetting> {

    /**
     * 分页查询配置列表
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    IPage<SystemServerSetting> pageFor(Page<SystemServerSetting> page, SystemServerSetting entity);

    /**
     * 根据服务器ID查询配置列表
     *
     * @param serverId 服务器ID
     * @return 配置列表
     */
    ReturnResult<List<SystemServerSetting>> getByServerId(Integer serverId);

    /**
     * 获取可用的ServletFilter对象列表
     *
     * @param serverId 服务器ID（可选，用于获取特定协议的Filter）
     * @return ServletFilter对象列表
     */
    ReturnResult<List<SpiOption>> getAvailableServletFilterObjects(Integer serverId);

    /**
     * 获取服务器已安装的ServletFilter配置
     *
     * @param serverId 服务器ID
     * @return 已安装的ServletFilter配置列表
     */
    ReturnResult<List<SystemServerSetting>> getServerInstalledFilters(Integer serverId);

    /**
     * 为服务器安装ServletFilter
     *
     * @param serverId    服务器ID
     * @param filterType  Filter类型
     * @return 安装结果
     */
    ReturnResult<SystemServerSetting> installServletFilter(Integer serverId, String filterType);

    /**
     * 卸载ServletFilter
     *
     * @param settingId 配置ID
     * @return 卸载结果
     */
    ReturnResult<Boolean> uninstallServletFilter(Integer settingId);

    /**
     * 启用ServletFilter
     *
     * @param settingId 配置ID
     * @return 启用结果
     */
    ReturnResult<Boolean> enableServletFilter(Integer settingId);

    /**
     * 禁用ServletFilter
     *
     * @param settingId 配置ID
     * @return 禁用结果
     */
    ReturnResult<Boolean> disableServletFilter(Integer settingId);

    /**
     * 更新ServletFilter排序
     *
     * @param orderUpdates 排序更新列表，格式：[{settingId: 1, order: 1}, ...]
     * @return 更新结果
     */
    ReturnResult<Boolean> updateServletFilterOrder(List<Map<String, Integer>> orderUpdates);

    /**
     * 获取ServletFilter配置项定义
     *
     * @param filterType Filter类型
     * @return 配置项定义列表
     */
    ReturnResult<List<Map<String, Object>>> getServletFilterConfigItems(String filterType);

    /**
     * 更新ServletFilter配置
     *
     * @param settingId 配置ID
     * @param config    配置数据
     * @return 更新结果
     */
    ReturnResult<Boolean> updateServletFilterConfig(Integer settingId, Map<String, Object> config);

    /**
     * 应用配置到运行中的服务器
     *
     * @param serverId 服务器ID
     * @return 应用结果
     */
    ReturnResult<Boolean> applyConfigToRunningServer(Integer serverId);

    /**
     * 克隆服务器配置
     *
     * @param sourceServerId 源服务器ID
     * @param targetServerId 目标服务器ID
     * @return 克隆结果
     */
    ReturnResult<Boolean> cloneServerSettings(Integer sourceServerId, Integer targetServerId);

    /**
     * 根据服务器ID和启用状态查询配置列表
     *
     * @param serverId 服务器ID
     * @param enabled  启用状态
     * @return 配置列表
     */
    ReturnResult<List<SystemServerSetting>> getByServerIdAndEnabled(Integer serverId, Boolean enabled);

    /**
     * 批量启用/禁用ServletFilter
     *
     * @param settingIds 配置ID列表
     * @param enabled    启用状态
     * @return 操作结果
     */
    ReturnResult<Boolean> batchUpdateEnabled(List<Integer> settingIds, Boolean enabled);

    /**
     * 排序服务器已安装的ServletFilter配置
     *
     * @param settingOrders 排序更新列表，格式：[{settingId: 1, order: 1}, ...]
     * @return 排序结果
     */
    ReturnResult<Boolean> orderServerInstalledFilters(List<ServerSettingOrderRequest> settingOrders);

    /**
     * 获取ServletFilter配置
     *
     * @param id 配置ID
     * @return 配置数据
     */
    ReturnResult<Map<String, Object>> getServletFilterConfig(Integer id);
}
