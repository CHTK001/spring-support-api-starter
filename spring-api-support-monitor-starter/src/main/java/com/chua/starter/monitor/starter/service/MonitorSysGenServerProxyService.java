package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerProxy;

import java.util.List;
import java.util.Map;

/**
 * 服务器代理关联服务接口
 *
 * @author CH
 * @since 2024/12/25
 */
public interface MonitorSysGenServerProxyService extends IService<MonitorSysGenServerProxy> {

    /**
     * 分页查询服务器代理关联列表
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    IPage<MonitorSysGenServerProxy> pageFor(Page<MonitorSysGenServerProxy> page, MonitorSysGenServerProxy entity);

    /**
     * 根据服务器ID查询代理关联列表
     *
     * @param serverId 服务器ID
     * @return 代理关联列表
     */
    ReturnResult<List<MonitorSysGenServerProxy>> getByServerId(Integer serverId);

    /**
     * 根据代理ID查询服务器关联列表
     *
     * @param proxyId 代理ID
     * @return 服务器关联列表
     */
    ReturnResult<List<MonitorSysGenServerProxy>> getByProxyId(Integer proxyId);

    /**
     * 根据服务器ID和代理类型查询代理关联
     *
     * @param serverId  服务器ID
     * @param proxyType 代理类型
     * @return 代理关联
     */
    ReturnResult<MonitorSysGenServerProxy> getByServerIdAndProxyType(Integer serverId, String proxyType);

    /**
     * 创建服务器代理关联
     *
     * @param serverProxy 服务器代理关联
     * @return 创建结果
     */
    ReturnResult<MonitorSysGenServerProxy> createServerProxy(MonitorSysGenServerProxy serverProxy);

    /**
     * 更新服务器代理关联
     *
     * @param serverProxy 服务器代理关联
     * @return 更新结果
     */
    ReturnResult<Boolean> updateServerProxy(MonitorSysGenServerProxy serverProxy);

    /**
     * 删除服务器代理关联
     *
     * @param id 关联ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteServerProxy(Integer id);

    /**
     * 批量删除服务器代理关联
     *
     * @param ids 关联ID列表
     * @return 删除结果
     */
    ReturnResult<Boolean> batchDeleteServerProxy(List<Integer> ids);

    /**
     * 测试代理连接
     *
     * @param id 关联ID
     * @return 测试结果
     */
    ReturnResult<Boolean> testProxyConnection(Integer id);

    /**
     * 启用代理
     *
     * @param id 关联ID
     * @return 操作结果
     */
    ReturnResult<Boolean> enableProxy(Integer id);

    /**
     * 禁用代理
     *
     * @param id 关联ID
     * @return 操作结果
     */
    ReturnResult<Boolean> disableProxy(Integer id);

    /**
     * 获取代理连接URL
     *
     * @param id 关联ID
     * @return 代理URL
     */
    ReturnResult<String> getProxyUrl(Integer id);

    /**
     * 批量更新代理状态
     *
     * @param ids    关联ID列表
     * @param status 状态
     * @return 更新结果
     */
    ReturnResult<Boolean> batchUpdateStatus(List<Integer> ids, Integer status);

    /**
     * 根据服务器ID删除所有代理关联
     *
     * @param serverId 服务器ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteByServerId(Integer serverId);



    /**
     * 获取代理统计信息
     *
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getProxyStatistics();

    /**
     * 获取服务器的所有代理类型
     *
     * @param serverId 服务器ID
     * @return 代理类型列表
     */
    ReturnResult<List<String>> getServerProxyTypes(Integer serverId);

    /**
     * 克隆服务器代理配置
     *
     * @param sourceServerId 源服务器ID
     * @param targetServerId 目标服务器ID
     * @return 克隆结果
     */
    ReturnResult<List<MonitorSysGenServerProxy>> cloneServerProxyConfig(Integer sourceServerId, Integer targetServerId);
}
